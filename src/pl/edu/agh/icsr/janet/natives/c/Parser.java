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
import pl.edu.agh.icsr.janet.natives.IParser;

public class Parser implements IParser {

    Lexer lexer;
    ExpressionParser expr_parser;
    StatementsParser stat_parser;
    BlockParser block_parser;
    PureCParser pure_parser;

    public void init(JanetSourceReader ibuf, PrintWriter yyerr,
                     YYLocation loc, EmbeddedParser jeparser, int dbg_level) {
        lexer = new Lexer(ibuf, jeparser);
        (expr_parser = new ExpressionParser(lexer, yyerr)).setdebug(dbg_level);
        (stat_parser = new StatementsParser(lexer, yyerr)).setdebug(dbg_level);
        (block_parser = new BlockParser(lexer, yyerr)).setdebug(dbg_level);
        (pure_parser = new PureCParser(lexer, yyerr)).setdebug(dbg_level);
    }

    public int parse(IMutableContext cxt, YYResultReceiver recv,
            int req_mode) throws ParseException {
        switch (req_mode) {
        case IParser.REQ_PURE:
            return pure_parser.yyparse(cxt, recv);
            /* YYRET_STATEMENTS, YYRET_EPSILON */

        case IParser.REQ_STATEMENTS:
            return stat_parser.yyparse(cxt, recv);
            /* YYRET_STATEMENTS, YYRET_STATEMENTS_WITH_TAIL, YYRET_EPSILON */

        case IParser.REQ_BLOCK:
            return block_parser.yyparse(cxt, recv);
            /* YYRET_BLOCK, YYRET_EPSILON */

        case IParser.REQ_EXPRESSION:
            return expr_parser.yyparse(cxt, recv);
            /* YYRET_EXPRESSION, YYRET_EPSILON */

        case IParser.REQ_STRING:
            switch(expr_parser.yyparse(cxt, recv)) {
            case YYRET_EXPRESSION: return YYRET_STRING;
            case YYRET_EPSILON: return YYRET_EPSILON;
            default: throw new RuntimeException();
            }

        case IParser.REQ_UNICODE_STRING:
            switch(expr_parser.yyparse(cxt, recv)) {
            case YYRET_EXPRESSION: return YYRET_UNICODE_STRING;
            case YYRET_EPSILON: return YYRET_EPSILON;
            default: throw new RuntimeException();
            }

        default:
            throw new IllegalArgumentException();
        }
    }
}
