/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import java.lang.reflect.*;
import pl.edu.agh.icsr.janet.*;
import java.util.*;
import pl.edu.agh.icsr.janet.yytree.YYVariableDeclarator;

public final class DefaultConstructor implements IMethodInfo { // JLS 8.6.7

    private ClassManager classMgr;
    private IClassInfo declCls;
    transient String signature;
    transient HashMap exceptionTypes;
    transient IClassInfo[] parameterTypes;

    public DefaultConstructor(ClassManager mgr, IClassInfo cls) {
        this.classMgr = mgr;
        this.declCls = cls;
    }

    public IClassInfo getDeclaringClass() {
        return declCls;
    }

    public String getName() {
        return "<init>";
    }

    public int getModifiers() {
        return Modifier.isPublic(declCls.getModifiers()) ? Modifier.PUBLIC : 0;
    }

    public IClassInfo getReturnType() {
        //return classMgr.VOID;
        throw new UnsupportedOperationException();
    }

    public boolean isConstructor() {
        return true;
    }

    public String toString() {
        return declCls.getSimpleName() + "()";
    }

    public String getArgumentSignature() {
        return "";
    }

    // JLS 8.4.2
    public String getJLSSignature() {
        return "()";
    }

    // JNI spec, ch.3, Type Signatures
    public String getJNISignature() {
        return "()V";
    }

    public Map getExceptionTypes() {
        if (exceptionTypes != null) return exceptionTypes;
        return exceptionTypes = new HashMap();
    }

    public YYVariableDeclarator[] getParameters() {
        throw new UnsupportedOperationException();
    }

    public IClassInfo[] getParameterTypes() {
        if (parameterTypes != null) return parameterTypes;
        return parameterTypes = new IClassInfo[0];
    }
}