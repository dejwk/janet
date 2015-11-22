/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;

public class YYConditionalExpression extends YYExpression {

    public static final int AND   = 1;
    public static final int OR    = 2;
    //public static final int COND  = 3;

    YYExpression e1;
    YYExpression e2;

    public YYConditionalExpression(IJavaContext cxt, int type,
            YYExpression e1, YYExpression e2) {
        super(cxt);
        this.e1 = e1;
        this.e2 = e2;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        e1.resolve(true);
        if (e1.getExpressionType().isAssignableFrom(classMgr.BOOLEAN)) {
            reportWrongType(e1.getExpressionType());
        }
        e2.resolve(true);
        if (e2.getExpressionType().isAssignableFrom(classMgr.BOOLEAN)) {
            reportWrongType(e2.getExpressionType());
        }

        // OK
        expressionType = classMgr.BOOLEAN;
        e1.setImplicitCastType(classMgr.BOOLEAN);
        e2.setImplicitCastType(classMgr.BOOLEAN);

        addExceptions(e1.getExceptionsThrown());
        addExceptions(e2.getExceptionsThrown());
    }

    private void reportWrongType(IClassInfo cls) throws CompileException {
        reportError("invalid type; required: boolean, found: " + cls);
    }

    class DumpIterator implements Iterator<YYNode> {
        int i = 0;
        DumpIterator() {}
        public boolean hasNext() { return i<2; }
        public YYNode next() {
            i++;
            return (i == 1 ? e1 : i == 2 ? e2 : null);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }

}