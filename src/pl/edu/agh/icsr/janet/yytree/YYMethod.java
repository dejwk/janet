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

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.lang.reflect.Modifier;
import java.util.*;

public class YYMethod extends YYNode implements IMethodInfo, IScope {

    public static final int METHOD      = 1;
    public static final int CONSTRUCTOR = 2;

    public static final int METHOD_MODIFIERS =
        YYModifierList.ACCESS_MODIFIERS | Modifier.ABSTRACT | Modifier.STATIC |
        Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE;

    public static final int CONSTRUCTOR_MODIFIERS =
        YYModifierList.ACCESS_MODIFIERS;

    public static final int INTERFACE_METHOD_MODIFIERS =
        Modifier.PUBLIC | Modifier.ABSTRACT;

    public static final int NONABSTRACT_METHOD_MODIFIERS =
        Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | Modifier.NATIVE |
        Modifier.SYNCHRONIZED;

    YYClass cls;
    int type; // method or constructor

    int modifiers;
    IClassInfo returnType;
    int rettypedims;
    String name;
    YYStatement body;
    transient HashMap throwlist;
    transient YYVariableDeclarator[] parameters;
    transient IClassInfo[] paramtypes;
    transient String argsignature;
    transient String signature;
    transient String jnisignature;

    YYType unresolvedReturnType;
    YYVariableDeclaratorList unresolvedParameters;
    YYTypeList unresolvedThrows;

    public YYMethod(IJavaContext cxt, String name, int type)
            throws CompileException {
        super(cxt);
        cls = (YYClass)cxt.getScope();
        this.name = name;
        this.type = type;

        if (cls.isInterface()) {
            this.modifiers = Modifier.PUBLIC + Modifier.ABSTRACT;
        } else {
            this.modifiers = 0;
        }
    }

    // called for constructors only
    public YYMethod checkName(ILocationContext cxt) throws CompileException {
        if (!cls.getSimpleName().equals(name)) {
            cxt.reportError("Invalid method declaration; return type required");
            this.type = METHOD;
        }
        return this;
    }

    public YYMethod setModifiers(YYModifierList m) throws CompileException {
        int errm;
        int modifiers = m.modifiers;
        String stype;
        ensureUnlocked();
        if (cls.isInterface()) { // always public abstract
            errm = modifiers & ~INTERFACE_METHOD_MODIFIERS;
            stype = "Interface methods";
        } else if (isConstructor()) {
            errm = modifiers & ~CONSTRUCTOR_MODIFIERS;
            stype = "Constructors";
        } else {
            if (Modifier.isPrivate(modifiers)) modifiers |= Modifier.FINAL;
            errm = modifiers & ~METHOD_MODIFIERS;
            stype = "Methods";
            if (errm == 0 && Modifier.isAbstract(modifiers)) {
                errm = modifiers & NONABSTRACT_METHOD_MODIFIERS;
                stype = "Abstract methods";
            }
        }
        if (errm != 0) {
            YYNode n = m.findFirst(errm);
            n.reportError(stype + " can't be " + Modifier.toString(errm));
        }
        this.modifiers |= (modifiers & ~errm);
        return this;
    }

    public int getScopeType() {
        if (isConstructor()) return IScope.CONSTRUCTOR;
        if (Modifier.isStatic(modifiers)) {
            return IScope.STATIC_METHOD;
        } else {
            return IScope.INSTANCE_METHOD;
        }
    }

    public IScope getEnclosingUnit() {
        return cls;
    }

    public YYMethod setBody(YYStatement body) throws CompileException {
        ensureUnlocked();
        if (body == null) {
            if ((modifiers & (Modifier.ABSTRACT + Modifier.NATIVE)) == 0) {
                reportError(this + " requires a method body. " +
                    "Otherwise declare it as abstract");
            }
        } else {
            if (Modifier.isAbstract(modifiers)) {
                body.reportError("Abstract methods can't have a body");
            } else {
                if (!body.isPure()) {
                    this.body = body;
                    super.append(body);
                }
            }
        }
        return this;
    }

    public YYMethod setReturnType(YYType type) {
        return setReturnType(type, 0);
    }

    public YYMethod setReturnType(YYType rettype, int rettypedims) {
        ensureUnlocked();
        this.unresolvedReturnType = rettype;
        this.rettypedims = rettypedims;
        return this;
    }

    public YYMethod setThrows(YYTypeList throwlist) {
        ensureUnlocked();
        unresolvedThrows = throwlist;
        return this;
    }

    public YYMethod setParameters(YYVariableDeclaratorList params) {
        ensureUnlocked();
        unresolvedParameters = params;
        return this;
    }

    public IClassInfo getDeclaringClass() {
        return cls;
    }

    public String getName() {
        if (isConstructor()) return "<init>";
        return this.name;
    }

    public int getModifiers() {
        return modifiers;
    }

    private IClassInfo resolveReturnType() throws ParseException {
        if (isConstructor()) {
            return returnType = cls.getClassManager().VOID;
        }
        IClassInfo resolved = unresolvedReturnType.getResolvedType();
        returnType = (rettypedims == 0) ? resolved
                                        : resolved.getArrayType(rettypedims);
        //rettypedims = 0;
        //unresolvedReturnType = null;
        return returnType;
    }

    public IClassInfo getReturnType() throws ParseException {
        if (returnType != null) return returnType;
        lock();
        return returnType = resolveReturnType();
    }

    public boolean isConstructor() {
        return this.type == CONSTRUCTOR;
    }

    public String getArgumentSignature() throws ParseException {
        if (argsignature != null) return argsignature;
        lock();
        String s = "";
        if (unresolvedParameters != null) {
            for (Iterator i = unresolvedParameters.iterator(); i.hasNext();) {
                s += ((YYVariableDeclarator)i.next()).getType().getSignature();
            }
        }
        return argsignature = s;
    }

    public String getJLSSignature() throws ParseException {
        if (signature != null) return signature;
        return signature = "(" + getArgumentSignature() + ")";
    }

    public String getJNISignature() throws ParseException {
        if (jnisignature != null) return jnisignature;
        return jnisignature = getJLSSignature() +
            getReturnType().getSignature();
    }

    public Map getExceptionTypes() throws ParseException { // JLS 8.4.4
        if (throwlist != null) return throwlist;
        lock();
        throwlist = new HashMap();
        if (unresolvedThrows != null) {
            ClassManager cm = cls.getClassManager();
            for(Iterator i = unresolvedThrows.iterator(); i.hasNext();) {
                YYType t = (YYType)i.next();
                IClassInfo rest = t.getResolvedType();
                String name = rest.getFullName();
                if (!rest.isAssignableFrom(cm.Throwable)) {
                    t.reportError(rest.toString() + " in throws clause " +
                        "must be subclass of java.lang.Throwable");
                } else { // repetitions ARE permitted!
                    throwlist.put(name, rest);
                }
            }
        }
        return throwlist;
    }

    public YYVariableDeclarator[] getParameters() {
        if (parameters != null) return parameters;
        lock();
        if (unresolvedParameters == null) {
            return parameters = new YYVariableDeclarator[0];
        }
        int len = unresolvedParameters.countSons();
        parameters = new YYVariableDeclarator[len];
        Iterator i;
        int idx;
        for (i = unresolvedParameters.iterator(), idx=0; i.hasNext(); idx++) {
            parameters[idx] = (YYVariableDeclarator)i.next();
        }
        return parameters;
    }

    public IClassInfo[] getParameterTypes() {
        if (paramtypes != null) return paramtypes;
        lock();
        YYVariableDeclarator[] params = getParameters();
        paramtypes = new IClassInfo[params.length];
        try {
            for (int i=0; i<params.length; i++) {
                paramtypes[i] = params[i].getType();
            }
        } catch (ParseException e) {
            throw new RuntimeException();
        }
        return paramtypes;
    }

    public IScope getEnclosingScope() { return cls; }
    public YYClass getCurrentClass() { return cls; }
    public IScope getCurrentMember() { return this; }

    public void resolve() throws ParseException {
        lock();
        if (body == null) return;
        body.resolve();
        // checking the exceptions
        Collection marked = getExceptionTypes().values();
        Iterator i = body.getExceptionsThrown().entrySet().iterator();
        ClassManager classMgr = cls.getClassManager();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            IClassInfo exc = (IClassInfo)entry.getKey();
            if (classMgr.isUncheckedException(exc)) {
                continue;
            }
            if (!ClassManager.containsException(marked, exc)) {
                YYStatement origin = (YYStatement)entry.getValue();
                origin.reportError("Exception " + exc.getFullName() +
                    " must be caught, or it must be declared in the throws" +
                    " clause of " + this);
            }
        }
    }

    public void write(Writer w) throws java.io.IOException {
        super.write(w);
    }

    public String toString() {
        try {
            return (isConstructor()
                       ? "constructor " + name
                       : "method " + getReturnType().getFullName() + " " +
                           getName()) +
                "(" + cls.classMgr.getTypeNames(getParameterTypes()) + ")";
        } catch (ParseException e) { throw new IllegalStateException(); }
    }

    public String describe() {
        try {
            String s = getModifiers() + " " +
                getReturnType().getFullName() + " " +
                getName() +
                "(" + (unresolvedParameters == null ? "" : unresolvedParameters.toString()) +
                ")" +
                " _" + getJNISignature() + "_";
            Iterator i = getExceptionTypes().values().iterator();
            if (i.hasNext()) {
                s += " throws ";
                while (i.hasNext()) {
                    s += i.next();
                    if (i.hasNext()) s+= ", ";
                }
            }
            return s;
        } catch (ParseException e) {
            return "exception has occured";
        }
    }

    public void lock() {
        if (!isLocked()) {
            cls.lock();
            super.lock();
        }
    }
}