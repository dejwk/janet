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

package pl.edu.agh.icsr.janet.natives.c;

import java.io.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.yytree.*;

class Lexer extends pl.edu.agh.icsr.janet.natives.Lexer {

    public Lexer(JanetSourceReader ibuf, EmbeddedParser jeparser) {
        super(ibuf, jeparser);
    }

    /***************************************************************************
     * First-Char classification routines
     */

    public final static boolean isInputCharacter(final int c) {
        return (c != JanetSourceReader.EOF && c != '\n' && c != '\r');
    }

    public final static boolean isWhitespace(final int c) {
        return (c == ' ' || c == '\t' || c == '\f' || c == '\n' || c == '\r');
    }

    public final static boolean isBrace(final int c) {
        return (c == '(' || c == ')' ||
                c == '[' || c == ']' ||
                c == '{' || c == '}');
    }
    public final static boolean isStatementChar(final int c) {
        return (c == ';');
    }

    public final static boolean isExpressionChar(final int c) {
        return (isInputCharacter(c) && !isWhitespace(c) &&
                !isStatementChar(c) &&
                !isBrace(c) &&
                c != '`' &&
                c != '/' /* possible comment */ );
    }

    /***************************************************************************
     * Report error
     */
    public void lexError(final String msg) throws LexException {
        token.clear();
        token.setTokenType(TokenTypes.LEX_ERROR);
        throw new LexException(msg);
    }

    /***************************************************************************
     * Collection functions
     */

    void collectEOF() {
        token.setTokenType(TokenTypes.YYEOF);
    }

    void collectWhitespace() {
        ; // just skip it
    }

    void collectComment(final int ch0) throws IOException, LexException {
        int ch;
        if (ch0 == '/') {
            while (isInputCharacter(ch = nextChar()));
            if (ch != JanetSourceReader.EOF) { // must be line terminator
                return;
            }
            return;
        }

        // must be a multiline comment
        while (true) {
            ch = nextChar();
            switch (ch) {
            case JanetSourceReader.EOF:
                lexError("unterminated comment");

            case '*':
                ch = nextChar();
                if (ch == '/') {
                    return;
                } else {
                    backupChar();
                }
                break;

            default: // absorb
                break;
            }
        }
    }

    void collectText(final int ch0, int lexmode)
            throws IOException, LexException {
        int ch;
        do {
            ch = nextChar();
        } while(isExpressionChar(ch) ||
                (isStatementChar(ch) && lexmode == NATIVE_STATEMENTS));
        backupChar();
        token.setTokenType(TokenTypes.CTEXT);
        token.setText(lexbuf);
    }

    void collectParen(final int ch0) {
        token.setTokenType(ch0);
        token.setText(lexbuf);
    }

    void collectBrace(final int ch0) {
        token.setTokenType(ch0);
        token.setText(lexbuf);
    }

    void collectBackquote() {
        token.setTokenType('`');
        token.setText(lexbuf);
    }

    public void skipWhites() throws LexException {
        try {
            for(;;) {
                pbeg.copyFrom(loc);
                int ch = nextChar();
                if (isWhitespace(ch)) {
                    collectWhitespace();
                    continue;
                } else if (ch == '/') {
                    ch = nextChar();
                    if (ch == '*' || ch == '/') {
                        collectComment(ch);
                        continue;
                    }
                }
                loc.copyFrom(pbeg);
                break;
            }
        } catch (IOException e) {
            throw new LexException(e.getMessage());
        }
    }

    public int yylex(IMutableContext cxt, int lexmode)
            throws LexException, ParseException {
        int ch;
        try {
            token.clear();
            skipWhites();

            for(;;) {
                newTokenHere();
                switch (lexmode) {
                case NATIVE_EXPRESSION:
                case NATIVE_STATEMENTS:
                    ch = nextChar();
                    if (ch == JanetSourceReader.EOF) {
                        collectEOF();
                    } else if (isBrace(ch)) {
                        collectBrace(ch);
                    } else if (ch == '`') {
                        collectBackquote();
                    } else if (isStatementChar(ch) &&
                               lexmode == NATIVE_EXPRESSION) {
                        token.setTokenType(TokenTypes.LEX_ERROR);
                    } else {
                        collectText(ch, lexmode);
                    }
                    break;

                case JAVA_EXPRESSION:
                case JAVA_STATEMENTS:
                    switch (jeparser.yyparse(cxt, token)) {
                    case EmbeddedParser.YYRET_EPSILON:
                        token.setTokenType(TokenTypes.EPSILON);
                        break;
                    case EmbeddedParser.YYRET_EXPRESSION:
                        token.setTokenType(TokenTypes.JAVA_EXPRESSION);
                        break;
                    case EmbeddedParser.YYRET_STATEMENTS:
                        token.setTokenType(TokenTypes.JAVA_STATEMENTS);
                        break;
                    case EmbeddedParser.YYRET_ENCLOSED_STATEMENTS:
                        token.setTokenType(TokenTypes.JAVA_ENCLOSED_STATEMENTS);
                        break;
                    default:
                        throw new RuntimeException();
                    }


/*
        if (jeparser.yyparse(cxt, token, mode) == EmbeddedParser.YYACCEPT) {
            Object result = token.yylval();
            if (result instanceof YYExpression) {
                token.setTokenType(TokenTypes.JAVA_EXPRESSION);
            } else if (result instanceof YYStatement) {
                token.setTokenType(TokenTypes.JAVA_STATEMENTS);
            } else if (result == null) {
                token.setTokenType(TokenTypes.EPSILON);
            } else {
                throw new RuntimeException();
            }
        } else {
            token.setTokenType(TokenTypes.LEX_ERROR);
        }*/

//                    parseEmbeddedJava(cxt, EmbeddedParser.EXPRESSION);
//                    break;

//                    parseEmbeddedJava(cxt, EmbeddedParser.STATEMENTS);

                    break;

                }
                break;
            } /* for(;;)*/

            token.setBeg(pbeg);
            token.setEnd(ibuf.loc());
            return token.getTokenType();
        } catch (IOException e) {
            throw new LexException(e.getMessage());
        }
    }

}
