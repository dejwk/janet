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
            if (rightType.isAssignableFrom(leftType)) return true;
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