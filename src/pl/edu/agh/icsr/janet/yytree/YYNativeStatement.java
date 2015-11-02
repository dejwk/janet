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
import java.io.IOException;
import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;

public class YYNativeStatement extends YYStatement implements INativeMethodInfo {

    ClassManager classMgr;
    YYNativeMethodImplementation implementation;
    YYClass declaringClass;
    String language;
    int mthIdx;
    boolean isStatic;

    transient String argsignature;
    transient Map exceptions;
    transient YYVariableDeclarator[] parameters;
    transient IClassInfo[] paramtypes;

    private HashSet unresolvedParameterSet;
    private Vector unresolvedParameters;

    public YYNativeStatement(IJavaContext cxt) {
        super(cxt, false, true);
        this.classMgr = cxt.getClassManager();
        IScope scope = cxt.getScope();
        this.declaringClass = scope.getCurrentClass();
        unresolvedParameterSet = new HashSet();
        unresolvedParameters = new Vector();
        isStatic = ((getCurrentMember().getScopeType() & IScope.INSTANCE_CONTEXT) == 0);
    }

    public YYNativeStatement addBody(YYNativeMethodImplementation implementation) {
        this.implementation = implementation;
        implementation.mth = this;
        implementation.setDeclaringClass(declaringClass);
        mthIdx = declaringClass.addImplicitNativeMethod(this);

        return this;
    }

    public void resolve() throws ParseException {
        if (implementation != null) {
            implementation.resolve();
            addExceptions(implementation.getExceptionsThrown());
        }
    }

    public void addExternalVariable(YYVariableDeclarator var) {
        ensureUnlocked();
        if (!unresolvedParameterSet.contains(var)) {
            unresolvedParameterSet.add(var);
            unresolvedParameters.add(var);
        }
    }

    public YYNativeStatement setNativeLanguage(String language) {
        this.language = language;
        return this;
    }

    public IScope getCurrentMember() {
        return this;
    }

    public int getScopeType() {
        return isStatic ? IScope.STATIC_METHOD : IScope.INSTANCE_METHOD;
    }

    public int getModifiers() {
        return Modifier.PRIVATE |
            Modifier.NATIVE |
            (isStatic ? Modifier.STATIC: 0);
    }

    public String getName() {
        return "janetmth$" + mthIdx;
    }

    public IClassInfo getReturnType() {
        return classMgr.VOID;
    }

    public boolean isConstructor() {
        return false;
    }

    public IClassInfo getDeclaringClass() {
        return declaringClass;
    }

    public String getArgumentSignature() throws ParseException {
        if (argsignature != null) return argsignature;
        lock();
        String s = "";
        YYVariableDeclarator[] params = getParameters();
        for (int i=0; i<params.length; i++) {
            s += params[i].getType().getSignature();
        }
        return argsignature = s;
    }

    public String getJLSSignature() throws ParseException {
        return "(" + getArgumentSignature() + ")";
    }

    public String getJNISignature() throws ParseException {
        return getJLSSignature() + "V";
    }

    public Map getExceptionTypes() {
        if (exceptions != null) return exceptions;
        lock();
        Iterator i = implementation.getExceptionsThrown().keySet().iterator();
        exceptions = new HashMap();
        while (i.hasNext()) {
            IClassInfo cls = (IClassInfo)i.next();
            exceptions.put(cls.getFullName(), cls);
        }
        return exceptions;
    }

    public YYVariableDeclarator[] getParameters() {
        if (parameters != null) return parameters;
        lock();
        parameters = new YYVariableDeclarator[unresolvedParameters.size()];
        for (int i=0, len = unresolvedParameters.size(); i<len; i++) {
            parameters[i] = (YYVariableDeclarator)unresolvedParameters.get(i);
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

    public String getNativeLanguage() {
        return this.language;
    }

    public YYNativeMethodImplementation getImplementation() {
        return implementation;
    }

    public void write(Writer w) throws IOException {
        w.write(getMethodName() + "(");
        YYVariableDeclarator[] params = getParameters();
        for (int i=0; i<params.length;i++) {
            if (i>0) w.write(", ");
            w.write(params[i].getName());
        }
        w.write(");");
    }

    public void writeMethod(Writer w) throws IOException {
        w.write("%INDENT%", true);
        w.write(Modifier.toString(this.getModifiers()) + " void ");
        w.write(getMethodName() + "(");

        // write parameters
        YYVariableDeclarator[] params = getParameters();
        for (int i=0; i<params.length; i++) {
            if (i>0) w.write (",");
            w.write("\n%INDENT%            ", true);
            try {
                w.write(params[i].getType().getFullName() + " ");
                w.write(params[i].getName());
            } catch (ParseException e) {
                throw new RuntimeException();
            }
        }
        w.write(")");

        //write throws
        boolean first = true;
        Iterator i = getExceptionTypes().values().iterator();
        while (i.hasNext()) {
            IClassInfo exc = (IClassInfo)i.next();
            try {
                if (classMgr.isUncheckedException(exc)) continue;
            } catch (ParseException e) {
                throw new RuntimeException();
            }
            if (first) {
                first = false;
                w.write("\n%INDENT%        throws ", true);
            } else {
                w.write(",\n%INDENT%               ", true);
            }
            w.write(exc.getFullName());
        }
        w.write(";\n");

        w.getNativeWriter().writeNativeMethod(this);
    }

    public Collection getUsedClassIdxs() {
        if (implementation.clsidxs == null) {
            throw new IllegalStateException();
        }
        return implementation.clsidxs;
    }

    public Collection getUsedFieldsIdxs() {
        if (implementation.fldidxs == null) {
            throw new IllegalStateException();
        }
        return implementation.fldidxs;
    }

    public Collection getUsedMethodsIdxs() {
        if (implementation.mthidxs == null) {
            throw new IllegalStateException();
        }
        return implementation.mthidxs;
    }

    public Collection getUsedStringsIdxs() {
        if (implementation.stridxs == null) {
            throw new IllegalStateException();
        }
        return implementation.stridxs;
    }

    public String getMethodName() {
        return "janetmth$" + mthIdx;
    }

    class DumpIterator implements Iterator {
        boolean bodyreturned;
        DumpIterator() {
            bodyreturned = false;
        }
        public boolean hasNext() {
            return !bodyreturned;
        }
        public Object next() {
            if (!bodyreturned) { bodyreturned = true; return implementation; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

}