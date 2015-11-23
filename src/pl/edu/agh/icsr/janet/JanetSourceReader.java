/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

