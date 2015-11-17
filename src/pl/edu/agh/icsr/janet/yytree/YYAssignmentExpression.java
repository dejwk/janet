/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import java.lang.reflect.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYAssignmentExpression extends YYExpression {

//    public static final int EQ = 1;

//    int kind;
    BinaryOperator op; // for compounds
    YYExpression leftHandSide;
    YYExpression assignment;
    //boolean needsRuntimeTypeCheck -- JNI performs the check by itself

//    public YYAssignmentExpression(IJavaContext cxt, int kind) {
//        super(cxt);
//        this.kind = kind;
//    }

//    public YYAssignmentExpression(IJavaContext cxt, BinaryOperator binop) {
//        super(cxt);
//        this.binop = binop;
//    }

    public YYAssignmentExpression(IJavaContext cxt, YYExpression leftHandSide,
        BinaryOperator op, YYExpression assignment)
    {
        super(cxt);
        this.leftHandSide = leftHandSide;
        this.assignment = assignment;
        this.op = op;
        if (op != null) {
            op.assign(leftHandSide, assignment);
        }
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        leftHandSide.resolve(true);
        if (!leftHandSide.isVariable()) {
            leftHandSide.reportError("variable required but value found " +
                "as the left side of assignment");
        }
        assignment.resolve(true);

        checkTypes();
        IClassInfo t = leftHandSide.getExpressionType();

        // OK
        expressionType = t;
        assignment.setImplicitCastType(t);

        addExceptions(leftHandSide.getExceptionsThrown());
        addExceptions(assignment.getExceptionsThrown());
        if (op != null) {
            Collection c = op.getExceptionsThrown();
            if (c != null) {
                for (Iterator i = c.iterator(); i.hasNext();) {
                    addException((IClassInfo)i.next());
                }
            }
        }

        /**
         * In case when left side is an array:
         * in the runtime, the actual component type may be subtype of that
         * declared. The assignment may thus cause array store exception.
         */
        // see JLS 10.10, 15.25.1
        if (t.isArray() &&
                !Modifier.isFinal(t.getComponentType().getModifiers())) {
            addException(classMgr.ArrayStoreException);
        }

    }

    private boolean checkTypes() throws ParseException {
        IClassInfo leftType = leftHandSide.getExpressionType();
        IClassInfo rightType = assignment.getExpressionType();

        if (!leftType.isPrimitive() && !leftType.isReference()) {
            // should not occur, as we check that it is a variable not value
            throw new RuntimeException();
        }

        if (op == null) { // simple assignment
            if (rightType.isAssignableFrom(leftType) ||
                    rightType == classMgr.NATIVETYPE && leftType.isPrimitive()) return true;
            if (assignment instanceof YYIntegerLiteral) {
                YYIntegerLiteral lit = (YYIntegerLiteral)assignment;
                if (leftType == classMgr.SHORT || leftType == classMgr.CHAR ||
                        leftType == classMgr.BYTE) {
                    if (lit.isCastableTo(leftType)) {
                        return true;
                    } else {
                        reportError("Value of integer constant is too wide " +
                                    "to be converted to " + leftType);
                    }
                }
            }
            reportIncompatibleTypes(leftType, rightType);
        } else { // compound assignment
            try {
                op.resolve();
            } catch (CompileException e) {
                reportError(e.getMessage());
            }
            // now, the operation result type must be possible to convert
            // to left type using narrowig conversion
            if (op.getResultType().isCastableTo(leftType) == IClassInfo.CAST_CORRECT) {
                return true;
            }
            reportIncompatibleTypes(leftType, op.getResultType());
        }
        return false;
    }

    private void reportIncompatibleTypes(IClassInfo t1, IClassInfo t2)
        throws CompileException
    {
        reportError("Incompatible types; found: " + t2 + ", required: " + t1);
    }

    public YYExpression getLeftHandSide() { return leftHandSide; }
    public YYExpression getAssignment() { return assignment; }
    public BinaryOperator getOperator() { return op; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator {
        int i = 0;
        DumpIterator() {}
        public boolean hasNext() { return i<2; }
        public Object next() {
            i++;
            return (i == 1 ? leftHandSide : i == 2 ? assignment : null);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

}