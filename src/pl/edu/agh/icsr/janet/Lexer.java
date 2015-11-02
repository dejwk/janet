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

import java.io.*;
import java.util.Hashtable;
import pl.edu.agh.icsr.janet.natives.IParser;
import pl.edu.agh.icsr.janet.natives.YYNativeCode;
import pl.edu.agh.icsr.janet.LexException;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.yytree.IScope;

class Lexer {

    // Lexer modes

    public static final int JAVA_TOKEN            = 0x8101;
    public static final int NATIVE_PURE           = 0x8102;
    public static final int NATIVE_STATEMENTS     = 0x8103;
    public static final int NATIVE_BLOCK          = 0x8104;
    public static final int NATIVE_EXPRESSION     = 0x8105;
    public static final int NATIVE_STRING         = 0x8106;
    public static final int NATIVE_UNICODE_STRING = 0x8107;


    YYToken token;
    JanetSourceReader ibuf;
    YYLocation pbeg;
    YYLocation loc;
    StringBuffer lexbuf;
    EmbeddedParser jeparser;
    int dbg_level;
    PrintWriter yyerr;

    String nlang_name;
    IParser nlang_parser;

    JavaLexer jlex;

    Hashtable parsers = new Hashtable();

    /***************************************************************************
     * construction
     */

    public Lexer(Preprocessor in, java.io.PrintWriter ferr, int dbg_level) {
        this.jlex = new JavaLexer(in);
        this.ibuf = in.ibuf();
        this.loc = in.loc;
        this.pbeg = jlex.pbeg;
        this.lexbuf = jlex.lexbuf;
        this.token = jlex.token;
        this.jeparser = new EmbeddedParser(this, ferr);
        jeparser.setdebug(dbg_level);
        jeparser.yyerrthrow = true;

        this.dbg_level = dbg_level;
        this.yyerr = ferr;
    }

    private int lexmode2yyreq[] = {
        -1,
        IParser.REQ_PURE,
        IParser.REQ_STATEMENTS,
        IParser.REQ_BLOCK,
        IParser.REQ_EXPRESSION,
        IParser.REQ_STRING,
        IParser.REQ_UNICODE_STRING
    };

    private int yyret2tokentype[] = {
        TokenTypes.EPSILON,
        TokenTypes.NATIVE_STATEMENTS,
        TokenTypes.NATIVE_STATEMENTS_WITH_JAVA_TAIL,
        TokenTypes.NATIVE_BLOCK,
        TokenTypes.NATIVE_EXPRESSION,
        TokenTypes.NATIVE_STRING,
        TokenTypes.NATIVE_UNICODE_STRING
    };

    public void skipWhites() throws LexException { jlex.skipWhites(); }

    public int yylex(IMutableContext cxt, int lexmode)
            throws LexException, ParseException {
        switch (lexmode) {
        case JAVA_TOKEN:
            return jlex.yylex();

        case NATIVE_PURE:
        case NATIVE_STATEMENTS:
        case NATIVE_BLOCK:
        case NATIVE_EXPRESSION:
        case NATIVE_STRING:
        case NATIVE_UNICODE_STRING:
            {
                int yyreq = lexmode2yyreq[lexmode - JAVA_TOKEN];
                int yyret = npload(cxt).parse(cxt, token, yyreq);
                int ttype = yyret2tokentype[yyret - IParser.YYRET_EPSILON];
                token.setTokenType(ttype);
            }
            break;

        default:
            throw new IllegalArgumentException();
        }
        return token.getTokenType();
    }

    public Object yylval() {
        return token.yylval();
    }

    public void setNativeLanguage(String nlang_name) {
        nlang_name = CompilationManager.getCanonicLanguageName(nlang_name);
        if (!nlang_name.equals(this.nlang_name)) {
           this.nlang_name = nlang_name;
           this.nlang_parser = null;
        }
    }

    public String getNativeLanguage() {
        return nlang_name;
    }

    IParser npload(ILocationContext cxt) throws CompileException {
        if (nlang_parser != null) {
            return nlang_parser;
        } else {
            nlang_parser = (IParser)parsers.get(nlang_name);
            if (nlang_parser == null) { // not yet loaded
                String clname = "pl.edu.agh.icsr.janet.natives." + nlang_name +
                                ".Parser";
                String errstr = "Unable to load parser for native language \"" +
                    nlang_name + "\": class " + clname + " ";
                try {
                    Class cls = Class.forName(clname);
                    nlang_parser = (IParser)cls.newInstance();
                    nlang_parser.init(this.ibuf, this.yyerr, this.loc,
                                      this.jeparser, this.dbg_level);
                } catch (ClassNotFoundException e) {
                    cxt.reportError(errstr + "not found");
                } catch (IllegalAccessException e) {
                    cxt.reportError(errstr + "is not public");
                } catch (InstantiationException e) {
                    cxt.reportError(errstr + "can't be instantiated " +
                        "(it is abstract class or interface)");
                }
                parsers.put(nlang_name, nlang_parser);
            }
            return nlang_parser;
        }
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
