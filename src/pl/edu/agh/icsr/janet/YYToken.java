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









