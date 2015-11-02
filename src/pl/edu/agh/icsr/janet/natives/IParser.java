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
import pl.edu.agh.icsr.janet.EmbeddedParser;
import pl.edu.agh.icsr.janet.YYToken;
import pl.edu.agh.icsr.janet.YYResultReceiver;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.YYLocation;
import pl.edu.agh.icsr.janet.IMutableContext;
import pl.edu.agh.icsr.janet.natives.YYNativeCode;
import pl.edu.agh.icsr.janet.yytree.IScope;

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
