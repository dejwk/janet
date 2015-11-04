/* -*-Java-*- */

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/*
 * Janet grammar file for embedded C statements.
 */

%{
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/* Important information:
 * THIS FILE HAS BEEN AUTOMATICALLY GENERATED by the public domain JB tool
 * (see README.html for details).
 */

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.LexException;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.yytree.*;
%}

%union {
    YYCBlock;
    YYCChunk;
    YYStatement;

    YYExpression;
    YYStatement;

    String;
}

%token LEX_ERROR
%token EPSILON

%token<String> CTEXT CSTATEMENT_CHAR
%token<YYExpression> JAVA_EXPRESSION
%token<YYStatement> JAVA_STATEMENTS JAVA_ENCLOSED_STATEMENTS

%type<YYStatement> CStatement BrokenJavaStatement

%type<YYCBlock> CBlock
%type<YYCChunk> Goal CStatements CStatementsWithJavaTail

%start Start

%%

/**
 * '`' is always considered to begin new embedded Java statements, never to
 * enclose this native statements.
 * When '}' , 'catch', or 'finally' is encountered when parsing such an
 * "embedded" Java statements,
 * parser reduces Java statements shifted so far as 'BrokenJavaStatement' and
 * current native statements are considered enclosed.
 * The control next returns to the outer Java parser which resumes parsing
 * from this place.
 * In this way the Java code is broken into two portions.
 * The portion to the left of the '}' is parsed as Java statements embedded
 * in current native statements; the portion
 * to the right goes upward where it is parsed as an enclosing Java statement.
 */

/* create main chunk (to be returned) */
Start
    : { pushChunk(new YYCChunk(cxt)); } Outer
    ;

Outer             /* never reached */
    : Goal       {}
    | Goal error {}
    ;

Goal
    : CStatements             { $$ = $1.compact(); yyclearin(); yyreturn(YYRET_STATEMENTS); }
    | CStatementsWithJavaTail { $$ = $1.compact(); yyclearin(); yyreturn(YYRET_STATEMENTS_WITH_TAIL); }
    | /* empty */             { $$ = null; yyclearin(); yyreturn(YYRET_EPSILON); }
    ;

CStatementsWithJavaTail
    : BrokenJavaStatement             { if ($1 != null) { $$ = peekChunk().add($1);} else { $$ = peekChunk().expand(cxt); } }
    | CStatements BrokenJavaStatement { if ($2 != null) { $$ = $1.add($2);} else { $$ = $1.expand(cxt); } }
    ;

CStatements_opt
    : /* empty */ {}
    | CStatements {}
    ;

CStatements
    : CStatement              { $$ = peekChunk().add($1.compact()); }
    | CStatements CStatement  { $$ = $1.add($2.compact()); }
    ;

CStatement
    : CStatement CSTATEMENT_CHAR                     { $$ = $1.expand(cxt); }
    | CTEXT                                          { $$ = new YYCChunk(cxt); }
    | '`' JavaBegin JAVA_EXPRESSION JavaEnd '`'      { $$ = $3.expand(cxt); }
    | '`' JavaBegin JAVA_STATEMENTS JavaEnd '`'      { $$ = $3.expand(cxt); }
    | '`' JavaBegin JAVA_ENCLOSED_STATEMENTS JavaEnd { $$ = $3.expand(cxt); }

    | '(' { pushChunk(new YYCChunk(cxt)); } CStatements_opt ')' { $$ = popChunk().expand(cxt); }
    | '[' { pushChunk(new YYCChunk(cxt)); } CStatements_opt ']' { $$ = popChunk().expand(cxt); }

    | CBlock                                    { $$ = $1; }
    ;

/**
 * Broken Java Statement is the one which begins as embedded Java but does not finish with '`'.
 * It always terminates the native code parsing (see Goal production).
 * Java statements inside it are optional.
 */
BrokenJavaStatement
    : '`' JavaBegin JAVA_STATEMENTS JavaEnd { $$ = $3.expand(cxt); }
    | '`' JavaBegin EPSILON JavaEnd         { $$ = null; }
    ;

JavaBegin
    : { lexmode = Lexer.JAVA_STATEMENTS; }
    ;

JavaEnd
    : { lexmode = Lexer.NATIVE_STATEMENTS; }
    ;

CBlock
    : '{'                      { YYCBlock b = new YYCBlock(cxt); b.markBeg(cxt.lend());
                                 pushChunk(b); cxt.pushScope(b); }
          CStatements_opt      { ((YYCBlock)peekChunk()).markEnd(cxt.lend()); }
      '}'                      { cxt.popScope(); $$ = popChunk().compact().expand(cxt); }

%%
    public int getInitialLexMode() { //int yymode) {
        return Lexer.NATIVE_STATEMENTS;
    }

    Stack chunks = new Stack();
    void pushChunk(YYCChunk c) { chunks.push(c); }
    YYCChunk popChunk() { return (YYCChunk)chunks.pop(); }
    YYCChunk peekChunk() { return (YYCChunk)chunks.peek(); }

    /*    protected YYCBlock setApprType(YYCBlock b) {
        switch (this.yymode) { // what was the demans
        case IParser.STATEMENTS:
            b.setType(b.STATEMENTS);
            return b;
        case IParser.BLOCK:
            b.setType(b.isNativeBlock() ? b.BLOCK : b.STATEMENTS);
            return b;
        default:
            throw new IllegalArgumentException();
        }
        }*/
