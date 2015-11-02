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

package pl.edu.agh.icsr.janet.reflect;

import pl.edu.agh.icsr.janet.*;
import java.lang.reflect.Constructor;
import java.util.*;
import pl.edu.agh.icsr.janet.yytree.YYVariableDeclarator;

public final class ConstructorInfoReflected implements IMethodInfo {

    private ClassManager classMgr;
    private Constructor cstr;
    transient Map exceptionTypes;
    transient IClassInfo[] parameterTypes;
    transient String argsignature;
    transient String signature;
    transient String jlssignature;

    public ConstructorInfoReflected(ClassManager mgr, Constructor cstr) {
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

    public Map getExceptionTypes() {
        if (exceptionTypes != null) return exceptionTypes;
        exceptionTypes = new HashMap();
        Class[] clss = cstr.getExceptionTypes();
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
        Class[] clss = cstr.getParameterTypes();
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