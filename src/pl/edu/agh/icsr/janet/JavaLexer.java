/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import java.io.*;

class JavaLexer {

    public final static int EOF = -1;

    YYToken token;
    StringBuffer lexbuf;
    StringBuffer aux_buf = new StringBuffer();
    Preprocessor in;
    YYLocation pbeg;
    YYLocation loc;

    /***************************************************************************
     * construction
     */

    public JavaLexer(Preprocessor in) {
        this.token = new YYToken();
        this.in = in;
        this.loc = in.loc();
        this.pbeg = new YYLocation();
        this.lexbuf = new StringBuffer();
    }

    /***************************************************************************
     * I/O routines
     */

    int nextChar() throws IOException, LexException {
        int c = in.nextChar();
        lexbuf.append((char)c); // EOF becomes 0xFFFF, but it doesn't matter
        return c;
    }

    void backupChar() throws LexException {
        in.backup();
        lexbuf.setLength(lexbuf.length() - 1);
    }

    void newTokenHere() {
        lexbuf.setLength(0);
        pbeg.copyFrom(loc);
    }

    /***************************************************************************
     * First-Char classification routines
     */

    public final static int hex2int(final int c) {
        return (c >= '0' && c <= '9')
            ? c - '0'
            : ((c >= 'a' && c <= 'f')
               ? c - 'a'
               : c - 'A');
    }

    public final static int octal2int(final int c) {
        return c - '0';
    }

    public final static boolean isInputCharacter(final int c) {
        return (c != EOF && c != '\n' && c != '\r');
    }

    public final static boolean isAsciiDigit(final int c) {
        return (c >= '0' && c <= '9');
    }

    public final static boolean isAsciiNonzeroDigit(final int c) {
        return (c >= '1' && c <= '9');
    }

    public final static boolean isAsciiOctalDigit(final int c) {
        return (c >= '0' && c <= '7');
    }

    public final static boolean isAsciiZeroToThree(final int c) {
        return (c >= '0' && c <= '3');
    }

    public final static boolean isAsciiHexDigit(final int c)
    {
        return ((c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F')
                || (c >= '0' && c <= '9'));
    }

    public final static boolean isLineTerminatorStart(final int c) {
        return (c == '\r' || c == '\n');
    }

    public final static boolean isJavaWhitespaceNotLineTerminator(final int c) {
        return (c == ' ' || c == '\t' || c == '\f');
    }

    public final static boolean isJavaSeparator(final int c) {
        return (c == '(' || c == ')' ||
                c == '{' || c == '}' ||
                c == '[' || c == ']' ||
                c == ';' || c == ',' || c == '.');
    }

    public final static boolean isJavaOperatorStart(final int c) {
        return (c == '=' || c == '>' || c == '<' || c == '!' ||
                c == '~' || c == '?' || c == ':' ||
                c == '+' || c == '-' || c == '*' || c == '/' ||
                c == '&' || c == '|' || c == '^' || c == '%');
    }

    /****************************************************************************
     * Reporting errors
     */
    public void lexError(final String msg) throws LexException {
        token.clear();
        token.setTokenType(TokenTypes.LEX_ERROR);
        throw new LexException(msg);
    }

    /****************************************************************************
     * Collection functions
     */

    void collectEOF() {
        token.setTokenType(TokenTypes.YYEOF);
    }

    void collectLineTerminator(final int ch0) throws IOException, LexException {
        int ch;
        if (ch0 == '\r') {
            ch = nextChar();
            if (ch != '\n') backupChar();
        }
    }

    void collectWhitespace() {
        ; // just skip it
    }

    void collectNumericLiteral(final int ch0) throws IOException, LexException {
        int ch;

        final int ZERO              = 1;
        final int BASE              = 2;
        final int HEX_BEG           = 3;
        final int HEX               = 4;
        final int ZERO_STARTED_BASE = 5;
        final int OCTAL             = 6;
        final int FRAC_BEG          = 7;
        final int FRACTION          = 8;
        final int EXP_BEG           = 9;
        final int EXPONENT          = 10;

        int state = (ch0 == '0')
            ? ZERO
            : ((ch0 == '.') ? FRAC_BEG : BASE);

havenumber:
        while (true) {
            ch = nextChar();

            switch (state) {
            case ZERO:
                if (isAsciiOctalDigit(ch)) {
                    state = OCTAL;
                } else if (isAsciiDigit(ch)) {
                    state = ZERO_STARTED_BASE;
                } else if (ch == 'x' || ch == 'X') {
                    state = HEX_BEG;
                } else if (ch == '.') {
                    state = FRACTION;
                } else if (ch == 'e' || ch == 'E') {
                    state = EXP_BEG;
                } else if (ch == 'f' || ch == 'F') {
                    token.setTokenType(TokenTypes.FLOAT_LITERAL);
                    break havenumber;
                } else if (ch == 'd' || ch == 'D') {
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                } else if (ch == 'l' || ch == 'L') {
                    token.setTokenType(TokenTypes.LONG_LITERAL);
                    break havenumber;
                } else {
                    backupChar();
                    token.setTokenType(TokenTypes.INTEGER_LITERAL);
                    break havenumber;
                }
                break;

            case BASE:
                if (isAsciiDigit(ch)) {
                    ;
                } else if (ch == '.') {
                    state = FRACTION;
                } else if (ch == 'e' || ch == 'E') {
                    state = EXP_BEG;
                } else if (ch == 'f' || ch == 'F') {
                    token.setTokenType(TokenTypes.FLOAT_LITERAL);
                    break havenumber;
                } else if (ch == 'd' || ch == 'D') {
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                } else if (ch == 'l' || ch == 'L') {
                    token.setTokenType(TokenTypes.LONG_LITERAL);
                    break havenumber;
                } else {
                    backupChar();
                    token.setTokenType(TokenTypes.INTEGER_LITERAL);
                    break havenumber;
                }
                break;

            case HEX_BEG:
                if (isAsciiHexDigit(ch)) {
                    state = HEX;
                } else {
                    lexError("bad hexadecimal literal");
                }
                break;

            case HEX:
                if (isAsciiHexDigit(ch)) {
                    ;
                } else if (ch == 'l' || ch == 'L') {
                    token.setTokenType(TokenTypes.LONG_LITERAL);
                    break havenumber;
                } else {
                    backupChar();
                    token.setTokenType(TokenTypes.INTEGER_LITERAL);
                    break havenumber;
                }
                break;

            case ZERO_STARTED_BASE:
                if (isAsciiDigit(ch)) {
                    ;
                } else if (ch == '.') {
                    state = FRACTION;
                } else if (ch == 'e' || ch == 'E') {
                    state = EXP_BEG;
                } else if (ch == 'f' || ch == 'F') {
                    token.setTokenType(TokenTypes.FLOAT_LITERAL);
                    break havenumber;
                } else if (ch == 'd' || ch == 'D') {
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                } else {
                    lexError("Wrong octal or floating-point literal");
                }
                break;

            case OCTAL:
                if (isAsciiOctalDigit(ch)) {
                    ;
                } else if (isAsciiDigit(ch)) {
                    state = ZERO_STARTED_BASE;
                } else if (ch == '.') {
                    state = FRACTION;
                } else if (ch == 'e' || ch == 'E') {
                    state = EXP_BEG;
                } else if (ch == 'f' || ch == 'F') {
                    token.setTokenType(TokenTypes.FLOAT_LITERAL);
                    break havenumber;
                } else if (ch == 'd' || ch == 'D') {
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                } else if (ch == 'l' || ch == 'L') {
                    token.setTokenType(TokenTypes.LONG_LITERAL);
                    break havenumber;
                } else {
                    backupChar();
                    token.setTokenType(TokenTypes.INTEGER_LITERAL);
                    break havenumber;
                }
                break;

            case FRAC_BEG:
                if (isAsciiDigit(ch)) {
                    state = FRACTION;
                } else {
                    lexError("bad floating-point literal format");
                }
                break;

            case FRACTION:
                if (isAsciiDigit(ch)) {
                    ;
                } else if (ch == 'e' || ch == 'E') {
                    state = EXP_BEG;
                } else if (ch == 'f' || ch == 'F') {
                    token.setTokenType(TokenTypes.FLOAT_LITERAL);
                    break havenumber;
                } else if (ch == 'd' || ch == 'D') {
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                } else {
                    backupChar();
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                }
                break;

            case EXP_BEG:
                if (isAsciiDigit(ch) || ch == '+' || ch == '-') {
                    state = EXPONENT;
                } else {
                    lexError("bad floating-point literal format");
                }
                break;

            case EXPONENT:
                if (isAsciiDigit(ch)) {
                    ;
                } else if (ch == 'f' || ch == 'F') {
                    token.setTokenType(TokenTypes.FLOAT_LITERAL);
                    break havenumber;
                } else if (ch == 'd' || ch == 'D') {
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                } else {
                    backupChar();
                    token.setTokenType(TokenTypes.DOUBLE_LITERAL);
                    break havenumber;
                }
                break;
            } // switch
        } // while (true)

        token.setText(lexbuf);
    }

    void collectCharLiteral() throws IOException, LexException {
        int ch = nextChar();
        int ch_ltr;
        if (!isInputCharacter(ch)) {
            lexError("unterminated character literal");
        }
        if (ch == '\'') {
            lexError("empty character literal");
        }
        if (ch == '\\') {
            ch_ltr = collectEscape();
        } else {
            ch_ltr = ch;
        }

        ch = nextChar();
        if (ch != '\'') {
            lexError("unterminated character literal");
        }
        token.setText(lexbuf);
        token.setYYlval(new Character((char)ch_ltr));
        token.setTokenType(TokenTypes.CHARACTER_LITERAL);
    }

    void collectStringLiteral() throws IOException, LexException {
        int ch;
        aux_buf.setLength(0);
        while (true) {
            ch = nextChar();
            if (!isInputCharacter(ch)) {
                lexError("unterminated string literal");
            } else {
                if (ch == '"') {
                    break;
                } else if (ch == '\\') {
                    aux_buf.append(collectEscape());
                } else {
                    aux_buf.append((char)ch);
                }
            }
        }
        token.setText(lexbuf);
        token.setYYlval(aux_buf.toString());
        token.setTokenType(TokenTypes.STRING_LITERAL);
    }

    char collectEscape() throws IOException, LexException {
        int ch = nextChar();

        switch (ch) {
        case 'b': return '\b';
        case 't': return '\t';
        case 'n': return '\n';
        case 'f': return '\f';
        case 'r': return '\r';
        case '"': return '\"';
        case '\'': return '\'';
        case '\\': return '\\';
        default: // must be octal escape
            int oct_ch = 0;
            int oct_digits = 0;
            if (isAsciiZeroToThree(ch)) {
                oct_digits = 2;
                oct_ch = ch;
            } else if (isAsciiOctalDigit(ch)) {
                oct_digits = 1;
                oct_ch = ch;
            } else {
                lexError("bad escape sequence: \\" + (char)ch);
            }
            for (int i = oct_digits; i > 0; --i) {
                ch = nextChar();
                if (!isAsciiOctalDigit(ch)) {
                    backupChar();
                    break;
                } else {
                    oct_ch <<= 3;
                    oct_ch += ch;
                }
            }
            return (char)oct_ch;
        }
    }

    void collectIdentifier(final int ch0) throws IOException, LexException {
        int ch = ch0;
        do {
            ch = nextChar();
        } while(ch != EOF && Character.isJavaIdentifierPart((char)ch));
        backupChar();
        String idtext = lexbuf.toString();
        token.setText(lexbuf);
        int val = findKeyword(idtext);
        if (val == TokenTypes.YYNONE) {
            token.setTokenType(TokenTypes.IDENTIFIER);
            token.setYYlval(idtext);
        } else {
            token.setTokenType(val);
        }
    }

    void collectComment(final int ch0) throws IOException, LexException {
        int ch;
        if (ch0 == '/') {
            while (isInputCharacter(ch = nextChar()));
            if (ch != EOF) { // must be line terminator (\r or \n or \r\n)
                collectLineTerminator(ch);
            }
            return;
        }

        // must be a multiline comment
        while (true) {
            ch = nextChar();
            switch (ch) {
            case EOF:
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

    void collectSeparator(final int ch0) {
        token.setTokenType(ch0);
        token.setText(lexbuf);
    }

    void collectOperator(final int ch0) throws IOException, LexException {
        boolean from_table = false;
        int ch;
        switch (ch0) {

        // single operators
        case '~':
        case '?':
        case ':':
            break;

        // operators that are single or can be followed by '='
        case '=':
        case '!':
        case '*':
        case '/':
        case '^':
        case '%':
            ch = nextChar();
            if (ch != '=') {
                backupChar();
            } else {
                from_table = true;
            }
            break;

        // operators that can be single, can be followed by '=',
        // or can be double (e.g. &&)
        case '+':
        case '-':
        case '&':
        case '|':
            ch = nextChar();
            if (ch != '=' && ch != ch0) {
                backupChar();
            } else {
                from_table = true;
            }
            break;

        // special case for '<' ('<', '<=', '<<', '<<=')
        case '<':
            ch = nextChar();
            if (ch == '<') {
                ch = nextChar();
                if (ch != '=') {
                    backupChar();
                }
                from_table = true;
            } else if (ch == '=') {
                from_table = true;
            } else {
                backupChar();
            }

        // special case for '>' ('>', '>=', '>>', '>>=', '>>>', '>>>=')
        case '>':
            ch = nextChar();
            if (ch == '>') {
                ch = nextChar();
                if (ch == '>') {
                    ch = nextChar();
                    if (ch != '=') {
                        backupChar();
                    }
                } else if (ch != '=') {
                    backupChar();
                }
                from_table = true;
            } else if (ch == '=') {
                from_table = true;
            } else {
                backupChar();
            }
            break;

        default:
            throw new IllegalArgumentException();
        }

        token.setText(lexbuf);
        if (from_table) {
            token.setTokenType(findKeyword(token.text().toString()));
        } else {
            token.setTokenType(ch0);
        }
    }

    void collectBackquote() {
        token.setTokenType('`');
        token.setText(lexbuf);
    }

    void collectNativeExpressionHeader() throws LexException, IOException {
        int ch;
        ch = nextChar();
        if (ch == '$') {
            ch = nextChar();
            if (ch == '$') {
                token.setTokenType(TokenTypes.NH_UNICODE_STRING);
            } else {
                backupChar();
                token.setTokenType(TokenTypes.NH_STRING);
            }
        } else if (ch == '&') {
            token.setTokenType(TokenTypes.NH_ARRAY_PTR);
        } else {
            backupChar();
            token.setTokenType(TokenTypes.NH_EXPRESSION);
        }
        token.setText(lexbuf);
    }
    // maybe should disregard rather than throw an exception
    void collectUnknown(int ch0) throws LexException {
        lexError("Unknown character encountered: '" + (char)ch0 + '\'');
    }

    /***************************************************************************
     * Parser interaction routines
     */

    public StringBuffer yytext() {
        return lexbuf;
    }

    public YYLocation currentloc() {
        return loc;
    }

    public YYLocation tokenloc() {
        return pbeg;
    }

    public String yyline() {
        return in.getCurrentLine();
    }

    public final JanetSourceReader ibuf() {
        return in.ibuf();
    }

    public final Object yylval() {
        return token.yylval();
    }

    public void skipWhites() throws LexException {
        try {
            for(;;) {
                pbeg.copyFrom(loc);
                int ch = nextChar();
                if (isLineTerminatorStart(ch)) {
                    collectLineTerminator(ch);
                    continue;
                } else if (isJavaWhitespaceNotLineTerminator(ch)) {
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

    // main tokenize method
    public int yylex() throws LexException {
        int ch;
        int cls;
        try {
            token.clear();
            skipWhites();
            for (;;) {
                newTokenHere();
                ch = nextChar();
                if (ch == EOF) {
                    collectEOF();
                } else if (isAsciiDigit(ch)) {
                    collectNumericLiteral(ch);
                } else if (ch == '\'') {
                    collectCharLiteral();
                } else if (ch == '"') {
                    collectStringLiteral();
                } else if (Character.isJavaIdentifierStart((char)ch)) {
                    collectIdentifier(ch);
                } else if (ch == '.') { // potential floating-point literal
                    ch = in.peek();
                    if (isAsciiDigit(ch)) {
                        collectNumericLiteral('.');
                    } else {
                        collectSeparator('.');
                    }
//                } else if (ch == '/') {
                    // comments were collected by skipWhites(); it is division
//                        collectOperator('/');
//                    }
                } else if (ch == '`') {
                    collectBackquote();
                } else if (isJavaSeparator(ch)) {
                    collectSeparator(ch);
                } else if (isJavaOperatorStart(ch)) {
                    collectOperator(ch);
                } else if (ch == '#') {
                    collectNativeExpressionHeader();
                } else {
                    collectUnknown(ch);
                }
                break;
            } // for (;;)

            token.setBeg(pbeg);
            token.setEnd(loc);
            return token.getTokenType();
        } catch (IOException e) {
            throw new LexException(e.getMessage());
        }
    }

    public static class Keyword {
        String literal;
        int type;

        Keyword(String literal, int type) {
            this.literal = literal;
            this.type = type;
        }

        public int compareTo(final String s) {
            return literal.compareTo(s);
        }
    }

    // Perhaps we should use java.util.HashMap?
    public static Keyword[] keywords = {
        new Keyword("!=",           TokenTypes.NE),
        new Keyword("%=",           TokenTypes.EQMOD),
        new Keyword("&&",           TokenTypes.ANDAND),
        new Keyword("&=",           TokenTypes.EQAND),
        new Keyword("*=",           TokenTypes.EQMUL),
        new Keyword("++",           TokenTypes.PLUSPLUS),
        new Keyword("+=",           TokenTypes.EQADD),
        new Keyword("--",           TokenTypes.MINUSMINUS),
        new Keyword("-=",           TokenTypes.EQSUB),
        new Keyword("/=",           TokenTypes.EQDIV),
        new Keyword("<<",           TokenTypes.LSHIFT),
        new Keyword("<<=",          TokenTypes.EQLSHIFT),
        new Keyword("<=",           TokenTypes.LE),
        new Keyword("==",           TokenTypes.EQEQ),
        new Keyword(">=",           TokenTypes.GE),
        new Keyword(">>",           TokenTypes.RSHIFT),
        new Keyword(">>=",          TokenTypes.EQRSHIFT),
        new Keyword(">>>",          TokenTypes.LOGRSHIFT),
        new Keyword(">>>=",         TokenTypes.EQLOGRSHIFT),
        new Keyword("^=",           TokenTypes.EQXOR),
        new Keyword("abstract",     TokenTypes.ABSTRACT),
        new Keyword("boolean",      TokenTypes.BOOLEAN),
        new Keyword("break",        TokenTypes.BREAK),
        new Keyword("byte",         TokenTypes.BYTE),
        new Keyword("case",         TokenTypes.CASE),
        new Keyword("catch",        TokenTypes.CATCH),
        new Keyword("char",         TokenTypes.CHAR),
        new Keyword("class",        TokenTypes.CLASS),
        new Keyword("const",        TokenTypes.CONST),
        new Keyword("continue",     TokenTypes.CONTINUE),
        new Keyword("default",      TokenTypes.DEFAULT),
        new Keyword("do",           TokenTypes.DO),
        new Keyword("double",       TokenTypes.DOUBLE),
        new Keyword("else",         TokenTypes.ELSE),
        new Keyword("extends",      TokenTypes.EXTENDS),
        new Keyword("false",        TokenTypes.BOOLEAN_LITERAL),
        new Keyword("final",        TokenTypes.FINAL),
        new Keyword("finally",      TokenTypes.FINALLY),
        new Keyword("float",        TokenTypes.FLOAT),
        new Keyword("for",          TokenTypes.FOR),
        new Keyword("goto",         TokenTypes.GOTO),
        new Keyword("if",           TokenTypes.IF),
        new Keyword("implements",   TokenTypes.IMPLEMENTS),
        new Keyword("import",       TokenTypes.IMPORT),
        new Keyword("instanceof",   TokenTypes.INSTANCEOF),
        new Keyword("int",          TokenTypes.INT),
        new Keyword("interface",    TokenTypes.INTERFACE),
        new Keyword("long",         TokenTypes.LONG),
        new Keyword("native",       TokenTypes.NATIVE),
        new Keyword("new",          TokenTypes.NEW),
        new Keyword("null",         TokenTypes.NULL_LITERAL),
        new Keyword("package",      TokenTypes.PACKAGE),
        new Keyword("private",      TokenTypes.PRIVATE),
        new Keyword("protected",    TokenTypes.PROTECTED),
        new Keyword("public",       TokenTypes.PUBLIC),
        new Keyword("return",       TokenTypes.RETURN),
        new Keyword("short",        TokenTypes.SHORT),
        new Keyword("static",       TokenTypes.STATIC),
        new Keyword("strictfp",     TokenTypes.STRICTFP),
        new Keyword("super",        TokenTypes.SUPER),
        new Keyword("switch",       TokenTypes.SWITCH),
        new Keyword("synchronized", TokenTypes.SYNCHRONIZED),
        new Keyword("this",         TokenTypes.THIS),
        new Keyword("throw",        TokenTypes.THROW),
        new Keyword("throws",       TokenTypes.THROWS),
        new Keyword("transient",    TokenTypes.TRANSIENT),
        new Keyword("true",         TokenTypes.BOOLEAN_LITERAL),
        new Keyword("try",          TokenTypes.TRY),
        new Keyword("void",         TokenTypes.VOID),
        new Keyword("volatile",     TokenTypes.VOLATILE),
        new Keyword("while",        TokenTypes.WHILE),
        new Keyword("|=",           TokenTypes.EQOR),
        new Keyword("||",           TokenTypes.OROR) };

    // binary search
    public int findKeyword(final String keyword) {
        int down = 0;
        int len = keywords.length;
        int max = len-1;

        while (len > 0) {
            Keyword k;
            int result;
            len = len / 2;
            while (down + len > max) {
                len = len / 2;
            }
            k = keywords[down + len];
            result = k.compareTo(keyword);

            if (result < 0) {
                down += len + 1;
            } else if (result == 0) {
                return k.type;
            }
        }
        return TokenTypes.YYNONE;
    }
}

