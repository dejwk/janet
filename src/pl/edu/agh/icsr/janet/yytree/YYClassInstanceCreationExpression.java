/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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