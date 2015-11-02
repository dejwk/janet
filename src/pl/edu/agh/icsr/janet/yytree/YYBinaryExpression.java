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