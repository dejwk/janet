/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.io.IOException;
import java.util.Iterator;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYInstanceOfExpression extends YYExpression {

    YYType unresolvedType;
    YYExpression target;

    IClassInfo testedType;
    boolean needsOnlyCheckForNull = false;
    int classidx;

    public YYInstanceOfExpression(IJavaContext cxt, YYExpression target,
                                  YYType yytype) {
        super(cxt);
        this.unresolvedType = yytype;
        this.target = target;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        target.resolve(true);
        IClassInfo tt = target.getExpressionType();
        if (!tt.isReference() && tt != classMgr.NULL) {
            reportError("Invalid expression type; required: reference " +
                "type or null, found: " + tt);
        }
        testedType = unresolvedType.getResolvedType();
        switch (tt.isCastableTo(testedType)) {
        case IClassInfo.CAST_INCORRECT:
            reportError("it is impossible for " + tt + " to be instance of " +
                testedType);
            break;
        case IClassInfo.CAST_CORRECT:
            needsOnlyCheckForNull = true;
            break;
        case IClassInfo.CAST_REQUIRES_RTCHECK:
            needsOnlyCheckForNull = false;
            classidx = registerClass(testedType);
            break;
        }

        expressionType = classMgr.BOOLEAN;
        addExceptions(target.getExceptionsThrown());
    }

    public YYExpression getTarget() {
        return target;
    }

    public IClassInfo getTestedType() {
        return testedType;
    }

    public boolean needsOnlyCheckForNull() {
        return needsOnlyCheckForNull;
    }

    public int getClassIdx() {
        return classidx;
    }

    class DumpIterator implements Iterator<YYNode> {
        int i = 0;
        DumpIterator() {}
        public boolean hasNext() { return i<1; }
        public YYNode next() {
            i++;
            return (i == 1 ? target : null);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }
}