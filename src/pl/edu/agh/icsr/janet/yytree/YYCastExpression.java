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
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYCastExpression extends YYExpression {

    YYType unresolvedType;
    YYExpression target;
    int clsidx;

    boolean requiresRTCheck = false;

    public YYCastExpression(IJavaContext cxt, YYType type,
                            YYExpression target) {
        super(cxt);
        this.unresolvedType = type;
        this.target = target;
    }

    /**
     * This is one of the most weird aspects of Java. See JLS 19.1.5.
     * Expression (which must be a ExpressionName) must be reclassified as
     * Type. Below are the only 4 possibilities of how the ExpressionName
     * could have been reclassified so far:
     *
     * 1. local_variable
     * 2. local_variable.field_access. ... .field_access
     * 3. field_access
     * 4. unresolved_name.field_access
     *
     * a result must be YYType, qualified (with YYPackage) in cases 2 and 4,
     * and simple in cases 1 and 3.
     */
    public YYCastExpression(IJavaContext cxt, YYExpression type,
                            YYExpression target) throws CompileException {
        super(cxt);
        this.target = target;

        if (type instanceof YYLocalVariableAccessExpression) { // case 1
            unresolvedType = new YYType(type, null,
                ((YYLocalVariableAccessExpression)type).variable.getName(),
                classMgr, getCompilationUnit());
        } else if (type instanceof YYFieldAccessExpression) { // cases 2,3,4
            YYFieldAccessExpression fld = (YYFieldAccessExpression)type;
            YYPackage pkg = null;
            if (fld.unresolvedTargetName != null) { // case 4
                pkg = fld.unresolvedTargetName.reclassifyAsPackage();
            } else if (fld.target != null) { // case 2
                pkg = tryReclassifyAsPackage(fld.target, null);
            } // case 3: pkg stays null
            unresolvedType = new YYType(type, pkg, fld.fieldName,
                classMgr, getCompilationUnit());
        }
    }

    /**
     * After cutting rightmost name node from case 2 above, there are
     * two possibilities for package name:
     *
     * 1. local_variable.field_access. ... .field_access
     * 2. local_variable
     *
     * Algorithm recursively appends subsequent package name nodes - from
     * right to left(!); when it reaches first package name node (e.g. 'java')
     * which must be a local_variable, it creates and returns new YYPackage.
     * It is passed up, and the last (rightmost) name node sets its right
     * boundary.
     */
    private YYPackage tryReclassifyAsPackage(YYExpression expr,
            String pkgname) throws CompileException {
        String suffix = pkgname == null ? "" : "." + pkgname;
        YYPackage pkg;
        if (expr instanceof YYLocalVariableAccessExpression) { // leftmost
            pkg = new YYPackage(expr,
                ((YYLocalVariableAccessExpression)expr).variable.getName() +
                suffix);
        } else if (expr instanceof YYFieldAccessExpression) {
            // must have target (see above)
            YYFieldAccessExpression fld = (YYFieldAccessExpression)target;
            pkg = tryReclassifyAsPackage(fld.target, fld.field.getName() +
                suffix);
        } else {
            reportError("Type expected, expression found");
            return null;
        }
        if (pkgname == null) { // rightmost
            pkg.expand(expr);
        }
        return pkg;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        expressionType = unresolvedType.getResolvedType();
        if (!expressionType.isPrimitive() && !expressionType.isReference()) {
            // unlike to occur, see grammar; if expression -> must be a name
            throw new RuntimeException();
        }
        target.resolve(true);

        // check whether permitted/illegal/requires runtime check
        switch (target.getExpressionType().isCastableTo(expressionType)) {
        case IClassInfo.CAST_INCORRECT:
            reportError("invalid cast from " +
                target.getExpressionType().toString() + " to " +
                expressionType);
            break;
        case IClassInfo.CAST_CORRECT:
            requiresRTCheck = false;
            break;
        case IClassInfo.CAST_REQUIRES_RTCHECK:
            requiresRTCheck = true;
            break;
        default:
            throw new RuntimeException();
        }

        //OK
        target.setImplicitCastType(expressionType);

        addExceptions(target.getExceptionsThrown());
        addException(classMgr.ClassCastException);

        if (expressionType.isReference()) {
            clsidx = registerClass(expressionType);
        }
    }

    public YYExpression getTarget() { return target; }
    public boolean requiresRuntimeCheck() { return requiresRTCheck; }
    public int getClassIdx() { return clsidx; }

    class DumpIterator implements Iterator {
        int i = 0;
        DumpIterator() {}
        public boolean hasNext() { return i<1; }
        public Object next() {
            i++;
            return (i == 1 ? target : null);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }
}
