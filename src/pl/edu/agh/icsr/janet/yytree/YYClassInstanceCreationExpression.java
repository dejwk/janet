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

import java.lang.reflect.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;
import java.util.*;

public class YYClassInstanceCreationExpression extends YYExpression {

    YYType unresolvedType;
    YYExpressionList arguments;

    IClassInfo[] argtypes;
    IMethodInfo method;
    int classidx;
    int mthidx;

    public YYClassInstanceCreationExpression(IJavaContext cxt, YYType type,
                                             YYExpressionList arguments) {
        super(cxt);
        this.unresolvedType = type;
        this.arguments = arguments;
    }

    public YYExpressionList getArguments() {
        return arguments;
    }

    public int getMethodIdx() {
        return mthidx;
    }

    public int getClassIdx() {
        return classidx;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        // resolve type
        expressionType = unresolvedType.getResolvedType();

        IClassInfo myclass = getCurrentClass();
        String mypkg = myclass.getPackageName();

        // is the type accessible?
        if (!expressionType.isAccessibleTo(mypkg) &&
                classMgr.getSettings().strictAccess()) {
            reportError(expressionType.toString() + " is not accessible " +
                "from " + myclass);
        }

        // isn't the class abstract?
        if (Modifier.isAbstract(expressionType.getModifiers())) {
            reportError(expressionType.toString() + " is abstract and can't " +
                "be instantiated");
        }

        // resolve arguments
        if (arguments != null) {
            arguments.resolve();
            argtypes = arguments.getExpressionTypes();
            addExceptions(arguments.getExceptionsThrown());
        } else {
            argtypes = new IClassInfo[0];
        }

        // lookup
        IMethodInfo mth = null;
        try {
            mth = classMgr.findTheMostSpecific(
                expressionType.getConstructors().values(), true, argtypes,
                myclass, false /* super(...) not covered */);
        } catch (NoApplicableMethodsFoundException e) {
            reportError("no constructor matching " + this);
        } catch (MethodNotAccessibleException e) {
            reportError(e.getMethod() + " is not accessible from " + myclass);
        } catch (NoAccessibleMethodsFoundException e) {
            reportError("None of constructors matching " + this +
                " are accessible from " + myclass);
        } catch (AmbigiousReferenceException e) {
            reportError("Reference to " + this + " is ambigious: both " +
                e.getObject1() + " and " + e.getObject2() + " match");
        }

        // OK
        arguments.setImplicitCastTypes(mth.getParameterTypes());

        addException(classMgr.OutOfMemoryError);
        addException(classMgr.InstantiationError);
        addException(classMgr.RuntimeException);
        addException(classMgr.Error);

        method = mth;

        classidx = registerClass(mth.getDeclaringClass(), false);
        mthidx = registerMethod(classidx, this.method);
    }

    public boolean canUseJNIThis() {
        return method.getDeclaringClass() == getCurrentClass();
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public String toString() {
        try {
            return expressionType.getFullName() +
                "(" + classMgr.getTypeNames(argtypes) + ")";
        } catch (CompileException e) {
            throw new RuntimeException();
        }
    }

    class DumpIterator implements Iterator {
        boolean argsreturned;
        Iterator i;
        DumpIterator() {
            argsreturned = (argtypes.length == 0);
            i = (arguments == null ? null : arguments.getDumpIterator());
        }
        public boolean hasNext() {
            return (i != null && i.hasNext());
        }
        public Object next() {
            if (i != null) return i.next();
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

}