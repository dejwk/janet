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

        if (exceptions == null) exceptions = new HashMap();
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

    class DumpIterator implements Iterator {
        boolean targetReturned;
        boolean argsreturned;
        Iterator i;
        DumpIterator() {
            targetReturned = (target == null);
            argsreturned = (argtypes.length == 0);
            i = (arguments == null ? null : arguments.getDumpIterator());
        }
        public boolean hasNext() {
            return !targetReturned || (i != null && i.hasNext());
        }
        public Object next() {
            if (!targetReturned) { targetReturned = true; return target; }
            if (i != null) return i.next();
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }


    public String toString() {
        try {
            return methodName + "(" + classMgr.getTypeNames(argtypes) + ")";
        } catch (CompileException e) {
            throw new RuntimeException();
        }
    }


}