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

package pl.edu.agh.icsr.janet.natives;

import pl.edu.agh.icsr.janet.JanetSourceReader;
import pl.edu.agh.icsr.janet.YYToken;
import pl.edu.agh.icsr.janet.YYLocation;
import pl.edu.agh.icsr.janet.IMutableContext;
import pl.edu.agh.icsr.janet.EmbeddedParser;
import pl.edu.agh.icsr.janet.LexException;
import pl.edu.agh.icsr.janet.ParseException;

import java.io.*;

public abstract class Lexer {

    public static final int NATIVE_EXPRESSION = 201;
    public static final int NATIVE_STATEMENTS = 202;
    public static final int JAVA_EXPRESSION   = 203;
    public static final int JAVA_STATEMENTS   = 204;
    public static final int JAVA_END          = 205;

    protected JanetSourceReader ibuf;
    protected YYToken token;
    protected YYLocation pbeg;
    protected YYLocation loc;
    protected StringBuffer lexbuf;
    protected int lexmode;            // native or embedded Java
    protected EmbeddedParser jeparser;

    protected Lexer(JanetSourceReader ibuf, EmbeddedParser jeparser) {
        this.ibuf = ibuf;
        this.pbeg = new YYLocation();
        this.loc = ibuf.loc();
        this.token = new YYToken();
        this.lexbuf = new StringBuffer();
        //this.mode = MODE_NATIVE;
        this.jeparser = jeparser;
    }

    protected int nextChar() throws IOException, LexException {
	int c = ibuf.nextChar();
	lexbuf.append((char)c); // EOF becomes 0xFFFF, but it doesn't matter
	return c;
    }

    protected void backupChar() throws LexException {
	ibuf.backup();
	lexbuf.setLength(lexbuf.length() - 1);
    }

    protected void newTokenHere() {
	lexbuf.setLength(0);
	pbeg.copyFrom(loc);
    }

    public abstract int yylex(IMutableContext cxt, int mode)
            throws LexException, ParseException;

    public Object yylval() {
        return token.yylval();
    }

    public YYLocation tokenloc() {
        return pbeg;
    }

    public YYLocation loc() {
        return loc;
    }

    public String yyline() {
        return ibuf.getCurrentLine();
    }

    public StringBuffer yytext() {
        return lexbuf;
    }

    public JanetSourceReader ibuf() {
        return ibuf;
    }

}

