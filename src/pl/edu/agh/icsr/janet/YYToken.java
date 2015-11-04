/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import pl.edu.agh.icsr.janet.yytree.YYStatement;

public class YYToken implements YYResultReceiver {

    int token_type = TokenTypes.YYNONE;

    // Token position in JanetSourceReader
    YYLocation token_beg;
    YYLocation token_end;

    // token text
    StringBuffer text;

    // semantic value
    Object yylval;

    public YYToken() {
        token_beg = new YYLocation();
        token_end = new YYLocation();
        text = new StringBuffer();
    }

    public void setTokenType(final int type) {
        token_type = type;
    }

    public int getTokenType() {
        return token_type;
    }

    public StringBuffer text() {
        return text;
    }

    public void clear() {
        token_type = TokenTypes.YYNONE;
        text.setLength(0);
        yylval = null;
    }

    public YYLocation beg() {
        return token_beg;
    }

    public void setBeg(final YYLocation loc) {
        token_beg.copyFrom(loc);
    }

    public YYLocation end() {
        return token_end;
    }

    public void setEnd(YYLocation loc) {
        token_end.copyFrom(loc);
    }

    public void setText(StringBuffer buf) {
        text.setLength(0);
        text.append(buf);
    }
/*
    public Number getFloatingPointLiteral() {
        int len = text.length();
        char last = text.charAt(len-1);

        if (last == 'f' || last == 'F') {
            return Float.valueOf(text.toString().substring(0, len-1));
        } else if (last == 'd' || last == 'D') {
            return Double.valueOf(text.toString().substring(0, len-1));
        } else {
            return Double.valueOf(text.toString().substring(0, len));
        }
    }

    public char getCharacterLiteral() {
        return character_literal;
    }

    void setCharacterLiteral(char c) {
        character_literal = c;
    }

    public String getStringLiteral() {
        return string_literal;
    }

    void setStringLiteral(String s) {
        string_literal = s;
    }

    public YYStatement getNativeBlockBody() {
        return native_block_body;
    }

    public void setNativeBlockBody(YYStatement s) {
        this.native_block_body = s;
    }
*/
    public final Object yylval() {
        return yylval;
    }

    public final void setYYlval(Object val) {
        yylval = val;
    }

    public int getEndPos() {
        return end().charno0;
    }

    public String toString() {
        return text.toString();
    }


}









