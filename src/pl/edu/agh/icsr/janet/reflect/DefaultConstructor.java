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