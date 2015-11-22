/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.util.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYPtrFetchExpression extends YYExpression {

    YYExpression base;
    boolean toNative;

    public YYPtrFetchExpression(IJavaContext cxt, YYExpression base,
            boolean toNative) throws CompileException {
        super(cxt);
        this.base = base;
        this.toNative = toNative;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        if (isSubexpression) {
            reportError("The address fetch operator " +
                (toNative ? "'#&'" : "'&'") +
                " may not be used in the middle of expression");
        }

        base.resolve(true);
        IClassInfo basetype = base.getExpressionType();
        if (!((basetype == classMgr.String) ||
              (basetype.isArray() &&
                  basetype.getComponentType().isPrimitive()))) {
            reportError("The address fetch operator " +
                (toNative ? "'#&'" : "'&'") +
                " may be used for strings and arrays of primitive types," +
                " but not for " + basetype.getFullName());
        }
/*
        if (!
        base.getExpressionType().isArray() ||
                !base.getExpressionType().getComponentType().isPrimitive()) {
        }
*/
        expressionType = null;
        addExceptions(base.getExceptionsThrown());
        addException(classMgr.NullPointerException);

        findImpl().addReferencedPrimitiveTypeArray();
    }

    public YYExpression getBase() { return base; }
    public boolean convertToNative() { return toNative; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator<YYNode> {
        int i=0;
        DumpIterator() { i=0; }
        public boolean hasNext() { return i<1; }
        public YYNode next() {
            i++;
            return i==1 ? base : null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }
}