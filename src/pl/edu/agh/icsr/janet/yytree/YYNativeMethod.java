/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.Writer;
import pl.edu.agh.icsr.janet.reflect.INativeMethodInfo;

public class YYNativeMethod extends YYMethod implements INativeMethodInfo {

    String language;
    YYNativeMethodImplementation implementation;
    //YYVariableDeclaratorList unresolvedParameters;

    public YYNativeMethod(IJavaContext cxt, String name)
            throws CompileException {
        super(cxt, name, METHOD);
    }

    public YYMethod setModifiers(YYModifierList m)
            throws CompileException {
        super.setModifiers(m);
        this.language = m.getNativeLanguage();
        return this;
    }

    public YYNativeMethod setBody(YYNativeMethodImplementation body)
            throws CompileException {
        ensureUnlocked();
        if (body != null) {
            this.body = this.implementation = body;
            body.mth = this;
            implementation.setDeclaringClass(this.cls);
            cls.addNativeMethodImplementation();
        }
        return this;
    }

    public String getNativeLanguage() {
        return this.language;
    }

    public YYNativeMethodImplementation getImplementation() {
        return implementation;
    }

    public void write(Writer w) throws java.io.IOException {
        w.write(Modifier.toString(this.modifiers) + " ");
        unresolvedReturnType.write(w);
        for (int i=0; i<rettypedims; i++) w.write("[]");
        w.write(" " + name);

        // write parameters
        //YYVariableDeclarator parameters[] = this.getParameters();
        w.write("(");
        if (unresolvedParameters != null) {
            unresolvedParameters.write(w);
        }
        w.write(")");

        //write throws
        if (unresolvedThrows != null) {
            w.write(" ");
            unresolvedThrows.write(w);
        }

        w.write(";");
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