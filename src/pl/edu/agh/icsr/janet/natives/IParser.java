/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives;

import pl.edu.agh.icsr.janet.EmbeddedParser;
import pl.edu.agh.icsr.janet.IMutableContext;
import pl.edu.agh.icsr.janet.JanetSourceReader;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.YYLocation;
import pl.edu.agh.icsr.janet.YYResultReceiver;

public interface IParser {

    public static final int REQ_STATEMENTS     = 0x0021;
    public static final int REQ_BLOCK          = 0x0022;
    public static final int REQ_EXPRESSION     = 0x0023;
    public static final int REQ_STRING         = 0x0024;
    public static final int REQ_UNICODE_STRING = 0x0025;
    public static final int REQ_PURE           = 0x0026;

    public static final int YYRET_EPSILON              = 0x0041;
    public static final int YYRET_STATEMENTS           = 0x0042;
    public static final int YYRET_STATEMENTS_WITH_TAIL = 0x0043;
    public static final int YYRET_BLOCK                = 0x0044;
    public static final int YYRET_EXPRESSION           = 0x0045;
    public static final int YYRET_STRING               = 0x0046;
    public static final int YYRET_UNICODE_STRING       = 0x0047;


/*
    public void init(JanetSourceReader ibuf,
                     YYLocation pbeg, YYLocation loc,
                     YYToken token, StringBuffer lexbuf,
                     EmbeddedParser jparser);
    public void prepare(IYYContext yycxt);
*/
    public void init(JanetSourceReader ibuf, java.io.PrintWriter yyerr,
                     YYLocation loc, EmbeddedParser jparser,
                     int dbglevel);

    public int parse(IMutableContext cxt, YYResultReceiver recv,
            int req_mode) throws ParseException;
}
