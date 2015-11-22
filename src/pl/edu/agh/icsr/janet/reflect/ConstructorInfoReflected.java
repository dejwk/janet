/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.yytree.YYVariableDeclarator;

public final class ConstructorInfoReflected implements IMethodInfo {

    private ClassManager classMgr;
    private Constructor<?> cstr;
    transient Map<String, IClassInfo> exceptionTypes;
    transient IClassInfo[] parameterTypes;
    transient String argsignature;
    transient String signature;
    transient String jlssignature;

    public ConstructorInfoReflected(ClassManager mgr, Constructor<?> cstr) {
        this.cstr = cstr;
        this.classMgr = mgr;
    }

    public IClassInfo getDeclaringClass() {
        return classMgr.forClass(cstr.getDeclaringClass());
    }

    public String getName() {
        return cstr.getName();
    }

    public int getModifiers() {
        return cstr.getModifiers();
    }

    public IClassInfo getReturnType() {
        throw new UnsupportedOperationException();
        //return classMgr.VOID;
    }

    public boolean isConstructor() {
        return true;
    }

    public String toString() {
        try {
            return "constructor " + getName() +
                "(" + classMgr.getTypeNames(getParameterTypes()) + ")";
        } catch(CompileException e) {
            throw new RuntimeException();
        }

    }

    public Map<String, IClassInfo> getExceptionTypes() {
        if (exceptionTypes != null) return exceptionTypes;
        exceptionTypes = new HashMap<String, IClassInfo>();
        Class<?>[] clss = cstr.getExceptionTypes();
        for (int i=0; i<clss.length; i++) {
            IClassInfo cls = classMgr.forClass(clss[i]);
            exceptionTypes.put(cls.getFullName(), cls);
        }
        return exceptionTypes;
    }

    public YYVariableDeclarator[] getParameters() {
        throw new UnsupportedOperationException();
    }

    public IClassInfo[] getParameterTypes() {
        if (parameterTypes != null) return parameterTypes;
        Class<?>[] clss = cstr.getParameterTypes();
        parameterTypes = new IClassInfo[clss.length];
        for (int i=0; i<clss.length; i++) {
            parameterTypes[i] = classMgr.forClass(clss[i]);
        }
        return parameterTypes;
    }

    // JNI spec, Resolving Native Method Names
    public String getArgumentSignature() throws CompileException {
        if (argsignature != null) return argsignature;
        IClassInfo[] params = getParameterTypes();
        String s = "";
        for (int i=0, len = params.length; i<len; i++) {
            s += params[i].getSignature();
        }
        return argsignature = s;
    }

    // JLS 8.4.2
    public String getJLSSignature() throws CompileException {
        if (jlssignature != null) return jlssignature;
        return jlssignature = "(" + getArgumentSignature() + ")";
    }

    // JNI spec, ch.3, Type Signatures
    public String getJNISignature() throws CompileException {
        if (signature != null) return signature;
        return signature = getJLSSignature() + "V";
    }

    // JNI spec, ch.3, Type Signatures
/*    public static String getJNISignature(Constructor cstr) {
        return getJLSSignature(cstr) + "V";
    }*/
}