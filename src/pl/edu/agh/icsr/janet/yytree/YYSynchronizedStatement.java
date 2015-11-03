/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;
import java.io.*;

public class YYSynchronizedStatement extends YYStatement {

    YYExpression expression;
    YYStatement statement;
    int syncidx;

    public YYSynchronizedStatement(IJavaContext cxt, YYExpression e,
                                   YYStatement s) {
        super(cxt, false);
        this.expression = e;
        this.statement = s;
    }

    public void resolve() throws ParseException {
        ClassManager classMgr = getCurrentClass().getClassManager();
        expression.resolve();
        statement.resolve();
        IClassInfo exprtype = expression.getExpressionType();
        if (!exprtype.isReference()) {
            expression.reportError("Incompatible type for synchronized. " +
                "Can't convert " + exprtype + " to java.lang.Object");
        }
        addException(classMgr.NullPointerException);
        syncidx = findImpl().addSynchronizedStatement();
    }

    public YYExpression getExpression() { return expression; }
    public YYStatement getStatement() { return statement; }
    public int getSyncIdx() { return syncidx; }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }
}