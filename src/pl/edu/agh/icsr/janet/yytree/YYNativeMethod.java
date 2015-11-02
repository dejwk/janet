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