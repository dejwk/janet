/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Java Language Extensions (JANET) package,
 * http://www.icsr.agh.edu.pl/janet.
 *
 * The Initial Developer of the Original Code is Dawid Kurzyniec.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Dawid Kurzyniec <dawidk@icsr.agh.edu.pl>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package pl.edu.agh.icsr.janet;

import pl.edu.agh.icsr.janet.*;
import java.io.*;
import java.net.URL;

public class JanetSourceReader {

    public final static int EOF = -1;

    BufferedReader rdr;
    StringBuffer buffer;
    File originFile;
    URL originURL;

    YYLocation loc = new YYLocation(); // will be shared
    YYLocation loc_bkup = new YYLocation();
    boolean backed_up = false;
    boolean fully_read = false; // does buffer contain full input file

    class JanetSourceReaderException extends LexException {
    }

    JanetSourceReader(URL originURL, File originFile, int initial_capacity,
        String encoding)
        throws UnsupportedEncodingException, IOException
    {
        InputStream is = originURL.openStream();
        rdr = new BufferedReader(new InputStreamReader(is, encoding));
        buffer = new StringBuffer(initial_capacity);
        this.originFile = originFile;
        this.originURL = originURL;
    }

    public int nextChar() throws IOException {
        int c;
        loc_bkup.copyFrom(loc);
        if (loc.charno0 < buffer.length()) {
            c = buffer.charAt(loc.charno0);
        } else {
            if (fully_read) {
                c = -1;
            } else {
                c = rdr.read();
            }
            if (c != -1) buffer.append((char)c);
        }
        loc.nextChar();
        backed_up = false;

        if (c == '\r') {
            loc.nextLine();
        } else if (c == '\n' &&
                   (loc.charno0 <= 1 || buffer.charAt(loc.charno0-2) != '\r')) {
            loc.nextLine();
        }
        if (c == -1) {
            fully_read = true;
            rdr = null; // dispose reader
        }
        return c; // maybe EOF
    }

    public void backup() throws LexException {
        if (backed_up) {
            throw new JanetSourceReaderException();
        }
        loc.copyFrom(loc_bkup);
        backed_up = true;
    }

    public YYLocation loc() {
        return loc;
    }

    protected void readLine() {
        if (fully_read) return;
        int l = buffer.length();
        int c = (l == 0 ? ' ' : buffer.charAt(l-1));
        if (c != '\n' && c != '\r') {
            try {
                while(true) {
                    c = rdr.read();
                    if (c != -1) buffer.append((char)c);
                    if (c == -1 || c == '\n' || c == '\r') break;
                }
            } catch (IOException e) {}
        }
    }

    public String getCurrentLine() {
        return getLine(loc);
    }

    public String getLine(YYLocation l) {
        char c;
        if (l.lineno > loc.lineno) {
            throw new IllegalArgumentException("No such line");
        } else if (l.lineno == loc.lineno) { // maybe line not complete
            readLine();
        }
        String s = buffer.toString().substring(l.line_beg);
        int p;
        if ((p = s.indexOf('\n')) > 0) s = s.substring(0, p);
        if ((p = s.indexOf('\r')) > 0) s = s.substring(0, p);
        return s;
    }

    public StringBuffer getbuf() { return buffer; }

    public File getOriginFile() { return originFile; }
    public URL getOriginURL() { return originURL; }

    public String getOriginAsString() {
        return originFile != null
            ? originFile.toString()
            : originURL.toString();
    }
}

