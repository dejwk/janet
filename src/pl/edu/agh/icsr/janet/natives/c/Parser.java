/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives.c;

import java.io.PrintWriter;

import pl.edu.agh.icsr.janet.EmbeddedParser;
import pl.edu.agh.icsr.janet.IMutableContext;
import pl.edu.agh.icsr.janet.JanetSourceReader;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.YYLocation;
import pl.edu.agh.icsr.janet.YYResultReceiver;
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
