/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.lang.reflect.*;
import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;

public class YYMethodInvocationExpression extends YYExpression {

    final static int THIS       = 1;
    final static int SUPER      = 2;
    final static int EXPRESSION = 3;
    final static int TYPE       = 4;
    final static int NONE       = 5;

    // invocation modes (see JLS 15.11.3)
    public final static int IMODE_STATIC     = 0x0001;
    public final static int IMODE_NONVIRTUAL = 0x0002;
    public final static int IMODE_SUPER      = 0x0004;
    public final static int IMODE_INTERFACE  = 0x0008;
    public final static int IMODE_VIRTUAL    = 0x0010;

    YYName unresolvedTargetName;

    YYExpression target;
    int targetType;
    String methodName;
    YYExpressionList arguments;

    IMethodInfo method;
    IClassInfo declCls;
    IClassInfo[] argtypes;

    int invocationMode;
    int classidx;
    int mthidx;

    public YYMethodInvocationExpression(IDetailedLocationContext cxt,
            String mthname) {
        super(cxt);
        this.target = null;
        this.methodName = mthname;
        this.targetType = NONE;
    }

    /**
     * Method invocation using a primary
     */
    public YYMethodInvocationExpression(IDetailedLocationContext cxt,
            YYExpression target, String mthname) {
        super(cxt);
        this.target = target;
        this.methodName = mthname;
        this.targetType = EXPRESSION;
    }
    /**
     * Method invocation using super ('this' is a primary)
     */
    public YYMethodInvocationExpression(IDetailedLocationContext cxt,
            YYThis target, String mthname) {
        super(cxt);
        this.target = target;
        this.methodName = mthname;
        this.targetType = target.isSuper() ? SUPER : THIS;
    }

    /**
     * Method invocation using expression name, when qualifying name has not
     * been yet reclassified (as a type or as a expression)
     */
    public YYMethodInvocationExpression(IDetailedLocationContext cxt,
            YYName unresolved, String mthname) {
        super(cxt);
        this.unresolvedTargetName = unresolved;
        this.methodName = mthname;
        // type of target not yet known (may be EXPRESSION or NONE)
    }

    public YYMethodInvocationExpression addArguments(YYExpressionList args) {
        this.arguments = args;
        return this;
    }

    public void resolve(boolean isSubexpresssion) throws ParseException {
        if (unresolvedTargetName != null) {
            // JLS 6.5.6.2, JLS 15.11
            Object o = unresolvedTargetName.reclassify();
            if (o instanceof String) {
                reportError("Class or interface " + o + " not found");
            } else if (o instanceof IClassInfo) { // static method
                this.declCls = (IClassInfo)o;
                this.target = null;
                this.targetType = TYPE;
            } else if (o instanceof YYFieldAccessExpression) {
                this.target = (YYFieldAccessExpression)o;
                this.targetType = EXPRESSION;
            } else {
                throw new RuntimeException();
            }
            unresolvedTargetName = null;
        }

        YYClass myclass = getCurrentClass();
        String mypkg = myclass.getPackageName();

        // determine declaring class
        if (target != null) {
            target.resolve(true);
            declCls = target.getExpressionType();
            addExceptions(target.getExceptionsThrown());
        }

        switch (targetType) {
        case THIS:
        case SUPER:
            break; // declaring class already fetched
        case EXPRESSION:
            // is the target's class accessible?
            if (!declCls.isAccessibleTo(mypkg) &&
                    classMgr.getSettings().strictAccess()) {
                reportError(declCls.toString() + " to which the method " +
                    methodName + " belongs is not accessible from " + myclass);
            }
            break;
        case NONE:
            declCls = myclass;
            break;
        case TYPE:
            // it has been already assigned; can't be interface (JLS 15.11.1)
            if (declCls.isInterface()) {
                reportError("interfaces can't have static methods");
            }
            break;
        default:
            throw new RuntimeException();
        }

        // is that a reference type?
        if (!declCls.isReference()) {
            reportError("Can't invoke a method on a " + declCls);
        }

        // resolve arguments
        if (arguments != null) {
            arguments.resolve();
            argtypes = arguments.getExpressionTypes();
            addExceptions(arguments.getExceptionsThrown());
        } else {
            argtypes = new IClassInfo[0];
        }

        // class, method name and arguments are known - time for method lookup
        IMethodInfo mth = null;
        try {
            mth = classMgr.findTheMostSpecific(
                declCls.getMethods(methodName).values(), false, argtypes,
                myclass, targetType != EXPRESSION && targetType != TYPE);
        } catch (NoApplicableMethodsFoundException e) {
            reportError("no method matching " + this + " found in " + declCls);
        } catch (MethodNotAccessibleException e) {
            reportError(e.getMethod().toString() + " declared in " +
                declCls + " is not accessible from " + myclass);
        } catch (NoAccessibleMethodsFoundException e) {
            reportError("None of methods matching " + this +
                " declared in " + declCls + " are accessible from " + myclass);
        } catch (AmbigiousReferenceException e) {
            reportError("Reference to " + this + " is ambigious: both " +
                e.getObject1() + " and " + e.getObject2() + " match");
        }

        // the mth is the most specific, but is it appropriate (JLS 15.11.3)?
        if (!Modifier.isStatic(mth.getModifiers())) { // instance method
            switch (targetType) {
            case NONE:
                int enclType = getCurrentMember().getScopeType();
                if ((enclType & ~IScope.INSTANCE_CONTEXT) == 0) {
                    break;
                } // else continue switch body and report error
            case TYPE:
                reportError("Can't make static reference to nonstatic " +
                    mth + " in " + declCls);
            case EXPRESSION:
                // computing target reference may result in a NullPointerExc.
                addException(classMgr.NullPointerException);
            }
        }
        // exceptions which come from arguments had been already added;
        // any method may, however, throw RuntimeException or Error
        addException(classMgr.RuntimeException);
        addException(classMgr.Error);

        // check if not void in subexpression
        if (isSubexpresssion && mth.getReturnType() == classMgr.VOID) {
            reportError("method " + mth + " is not allowed as a part " +
                "of another expression (return type required)");
        }

        // OK
        arguments.setImplicitCastTypes(mth.getParameterTypes());
        method = mth;
        expressionType = mth.getReturnType();

        if (!isStringLength()) {
            classidx = registerClass(declCls, false);
            mthidx = registerMethod(classidx, this.method);
        }

        // determine invocation mode
        if (Modifier.isStatic(method.getModifiers())) {
            invocationMode = IMODE_STATIC;
        } else if (Modifier.isPrivate(method.getModifiers())) {
            invocationMode = IMODE_NONVIRTUAL;
        } else if (this.targetType == SUPER) {
            invocationMode = IMODE_SUPER;
        } else if (Modifier.isInterface(declCls.getModifiers())) {
            invocationMode = IMODE_INTERFACE;
        } else {
            invocationMode = IMODE_VIRTUAL;
        }

        if (exceptions == null) exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public YYExpression getTarget() {
        return target;
    }

    public YYExpressionList getArguments() {
        return arguments;
    }

    public IMethodInfo getMethod() {
        return method;
    }

    public int getMethodIdx() {
        return mthidx;
    }

    public int getClassIdx() {
        return classidx;
    }

    public int getInvocationMode() {
        return invocationMode;
    }

    private boolean isInvocationModeInstance() {
        return invocationMode != IMODE_STATIC;
    }

    private boolean isInvocationModeStatic() {
        return invocationMode == IMODE_STATIC;
    }

    public boolean canUseJNIThis() {
        return (isInvocationModeInstance() && (
                   targetType == THIS || targetType == SUPER ||
                   targetType == NONE || target instanceof YYThis)) ||
               (isInvocationModeStatic() && (
                   method.getDeclaringClass() == getCurrentClass()));
    }

    public boolean isStringLength() {
        return method.getDeclaringClass() == classMgr.String &&
               method.getName().equals("length");
    }

    class DumpIterator implements Iterator<YYNode> {
        boolean targetReturned;
        boolean argsreturned;
        Iterator<YYNode> i;
        DumpIterator() {
            targetReturned = (target == null);
            argsreturned = (argtypes.length == 0);
            i = (arguments == null ? null : arguments.getDumpIterator());
        }
        public boolean hasNext() {
            return !targetReturned || (i != null && i.hasNext());
        }
        public YYNode next() {
            if (!targetReturned) { targetReturned = true; return target; }
            if (i != null) return i.next();
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }


    public String toString() {
        try {
            return methodName + "(" + classMgr.getTypeNames(argtypes) + ")";
        } catch (CompileException e) {
            throw new RuntimeException();
        }
    }


}