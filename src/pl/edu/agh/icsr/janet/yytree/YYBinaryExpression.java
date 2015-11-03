/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

/**
 * Title:        Janet
 * Description:  Java Native Extensions
 * Copyright:    Copyright (c) Dawid Kurzyniec
 * Company:      NA
 * @author Dawid Kurzyniec
 * @version 1.0
 */

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.util.*;

public class YYBinaryExpression extends YYExpression {

    YYExpression left, right;
    BinaryOperator op;

    public YYBinaryExpression(IDetailedLocationContext cxt, YYExpression left,
        BinaryOperator op, YYExpression right)
    {
        super(cxt);
        this.op = op;
        this.left = left;
        this.right = right;
        op.assign(left, right);
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        left.resolve(true);
        right.resolve(true);
        try {
            op.resolve();
        } catch (CompileException e) {
            reportError(e.getMessage());
        }
        left.castedImplicitlyToType = op.getLeftType();
        right.castedImplicitlyToType = op.getRightType();

        addExceptions(left.getExceptionsThrown());
        addExceptions(right.getExceptionsThrown());
        Collection c = op.getExceptionsThrown();
        if (c != null) {
            for (Iterator i = c.iterator(); i.hasNext();) {
                addException((IClassInfo)i.next());
            }
        }
        expressionType = op.getResultType();
    }

//
//    public void resolve(boolean isSubexpression) throws CompileException {
//        leftHandSide.resolve(true);
//        if (!leftHandSide.isVariable()) {
//            leftHandSide.reportError("variable required but value found " +
//                "as the left side of assignment");
//        }
//        assignment.resolve(true);
//
//        IClassInfo leftType = leftHandSide.getExpressionType();
//        IClassInfo rightType = assignment.getExpressionType();
//
//        if (!leftType.isPrimitive() && !leftType.isReference()) {
//            // should not occur, as we check that it is a variable not value
//            throw new RuntimeException();
//        }
//
//        // check types
//        switch (kind) {
//        case EQ: // any type but must be assignable
//            if (rightType.isAssignableFrom(leftType)) break;
//            if (assignment instanceof YYIntegerLiteral) {
//                YYIntegerLiteral lit = (YYIntegerLiteral)assignment;
//                if (leftType == classMgr.SHORT || leftType == classMgr.CHAR ||
//                        leftType == classMgr.BYTE) {
//                    if (lit.isCastableTo(leftType)) {
//                        break;
//                    } else {
//                        reportError("Value of integer constant is too wide " +
//                                                     "to be converted to " + leftType);
//                    }
//                }
//            }
//            reportIncompatibleTypes();
//        default:
//            throw new UnsupportedOperationException();
//        }
//
//        // OK
//        expressionType = leftType;
//        assignment.setImplicitCastType(leftType);
//
//        addExceptions(leftHandSide.getExceptionsThrown());
//        addExceptions(assignment.getExceptionsThrown());
//
//        /**
//         * In case when left side is an array:
//         * in the runtime, the actual component type may be subtype of that
//         * declared. The assignment may thus cause array store exception.
//         */
//        // see JLS 10.10, 15.25.1
//        if (leftType.isArray() &&
//                !Modifier.isFinal(leftType.getComponentType().getModifiers())) {
//            addException(classMgr.ArrayStoreException);
//        }
//
//    }

//    private void reportIncompatibleTypes() throws CompileException {
//        reportError("Incompatible types; found: " +
//            assignment.getExpressionType() + ", required: " +
//            leftHandSide.getExpressionType());
//    }
//
    public YYExpression getLeft() { return left; }
    public YYExpression getRight() { return right; }
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
            return (i == 1 ? left : i == 2 ? right : null);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }
}