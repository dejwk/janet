/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.Writer;
import pl.edu.agh.icsr.janet.reflect.ClassManager;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;
import pl.edu.agh.icsr.janet.reflect.INativeMethodInfo;

public class YYNativeStatement extends YYStatement implements INativeMethodInfo {

    ClassManager classMgr;
    YYNativeMethodImplementation implementation;
    YYClass declaringClass;
    String language;
    int mthIdx;
    boolean isStatic;

    transient String argsignature;
    transient Map<String, IClassInfo> exceptions;
    transient YYVariableDeclarator[] parameters;
    transient IClassInfo[] paramtypes;

    private HashSet<YYVariableDeclarator> unresolvedParameterSet;
    private Vector<YYVariableDeclarator> unresolvedParameters;

    public YYNativeStatement(IJavaContext cxt) {
        super(cxt, false, true);
        this.classMgr = cxt.getClassManager();
        IScope scope = cxt.getScope();
        this.declaringClass = scope.getCurrentClass();
        unresolvedParameterSet = new HashSet<YYVariableDeclarator>();
        unresolvedParameters = new Vector<YYVariableDeclarator>();
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

    public Map<String, IClassInfo> getExceptionTypes() {
        if (exceptions != null) return exceptions;
        lock();
        exceptions = new HashMap<String, IClassInfo>();
        for (IClassInfo cls : implementation.getExceptionsThrown().keySet()) {
            exceptions.put(cls.getFullName(), cls);
        }
        return exceptions;
    }

    public YYVariableDeclarator[] getParameters() {
        if (parameters != null) return parameters;
        lock();
        parameters = new YYVariableDeclarator[unresolvedParameters.size()];
        for (int i=0, len = unresolvedParameters.size(); i<len; i++) {
            parameters[i] = unresolvedParameters.get(i);
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
        Iterator<IClassInfo> i = getExceptionTypes().values().iterator();
        while (i.hasNext()) {
            IClassInfo exc = i.next();
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

    public Collection<Integer> getUsedClassIdxs() {
        if (implementation.clsidxs == null) {
            throw new IllegalStateException();
        }
        return implementation.clsidxs;
    }

    public Collection<Integer> getUsedFieldsIdxs() {
        if (implementation.fldidxs == null) {
            throw new IllegalStateException();
        }
        return implementation.fldidxs;
    }

    public Collection<Integer> getUsedMethodsIdxs() {
        if (implementation.mthidxs == null) {
            throw new IllegalStateException();
        }
        return implementation.mthidxs;
    }

    public Collection<Integer> getUsedStringsIdxs() {
        if (implementation.stridxs == null) {
            throw new IllegalStateException();
        }
        return implementation.stridxs;
    }

    public String getMethodName() {
        return "janetmth$" + mthIdx;
    }

    class DumpIterator implements Iterator<YYNode> {
        boolean bodyreturned;
        DumpIterator() {
            bodyreturned = false;
        }
        public boolean hasNext() {
            return !bodyreturned;
        }
        public YYNode next() {
            if (!bodyreturned) { bodyreturned = true; return implementation; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }

}