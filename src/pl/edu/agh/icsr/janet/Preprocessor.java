/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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

