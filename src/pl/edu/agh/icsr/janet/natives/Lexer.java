/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

