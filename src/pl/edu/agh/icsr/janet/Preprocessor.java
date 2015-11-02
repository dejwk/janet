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

class Preprocessor {

    boolean end_of_file = false;
    boolean backed_up = false;
    int last_char;

    boolean uesc_start = false;

    JanetSourceReader ibuf;

    YYLocation loc; // shared
    YYLocation loc_bkup = new YYLocation();

    class PreprocessorException extends LexException {
    }

    class UnicodeEscapeException extends PreprocessorException {
    }

    public Preprocessor(JanetSourceReader ibuf) {
	this.ibuf = ibuf;
	this.loc = ibuf.loc(); // shared location
    }

    public final JanetSourceReader ibuf() {
        return ibuf;
    }

    /* following must be able to find unicode escapes (like \u00ff)
     * (as described in Java Language Specification)
     */
    int readChar() throws IOException, LexException {
	int c;
	c = ibuf.nextChar();

	if (c != '\\') {
	    uesc_start = false;
	    return c;
	}

	uesc_start = !uesc_start;

	c = ibuf.nextChar();
	if (c == '\\') {
	    ibuf.backup();
	    return '\\';
	} else if (!uesc_start || c != 'u') {
	    // \\u2020 or \x are not unicode escapes
	    uesc_start = false;
	    ibuf.backup();
	    return '\\';
	} else { // now it honestly is unicode escape
	    do {
		c = ibuf.nextChar();
	    } while (c == 'u');

	    int r = 0;
	    for (int i=0; i<4; ++i) {
		if (!JavaLexer.isAsciiHexDigit(c)) {
		    throw new UnicodeEscapeException();
		}
		r = (r << 4) + JavaLexer.hex2int(c);
		c = ibuf.nextChar();
	    }

	    ibuf.backup();
	    uesc_start = false;
	    return r;
	}
    }

    public int nextChar() throws IOException, LexException {
	if (backed_up) {
	    YYLocation.xchg(loc, loc_bkup);
	    backed_up = false;
	    return last_char;
	} else {
	    loc_bkup.copyFrom(loc);
	    last_char = readChar();
	    return last_char;
	}
    }

    public void backup() throws PreprocessorException {
	if (backed_up) {
	    throw new PreprocessorException();
	}
	YYLocation.xchg(loc, loc_bkup); // restore shared location
	backed_up = true;
    }

    public YYLocation loc() {
	return loc;
    }

    public String getCurrentLine() {
	return ibuf.getCurrentLine();
    }

    public int peek() throws IOException, LexException {
	int c = nextChar();
	backup();
	return c;
    }

}

