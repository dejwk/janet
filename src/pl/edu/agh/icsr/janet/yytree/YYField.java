/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.tree.Node;

import java.lang.reflect.Modifier;
import java.util.*;

public class YYField extends YYStatement {

    public static final int FIELD_MODIFIERS =
        YYModifierList.ACCESS_MODIFIERS | Modifier.FINAL | Modifier.STATIC |
            Modifier.TRANSIENT | Modifier.VOLATILE;

    public static final int INTERFACE_FIELD_MODIFIERS =
        Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC;

    YYVariableDeclaratorList decls;

    public YYField(IJavaContext cxt, YYModifierList m, YYType t,
                  YYVariableDeclaratorList decls)
                  throws CompileException {
        super(cxt);
        YYClass declaringClass = (YYClass)cxt.getScope();
        int modifiers = checkModifiers(m, declaringClass);
        this.decls = decls;
        decls.setModifiers(modifiers);
        decls.setType(t);
        decls.setDeclaringClass(declaringClass);
    }

    private int checkModifiers(YYModifierList m, YYClass cls)
            throws CompileException { // JLS 8.3.1, 9.3
        int modifiers = (m != null) ? m.getModifiers() : 0;
        int errm;
        String stype;
        if (cls.isInterface()) { // always public final static
            modifiers |= (Modifier.PUBLIC + Modifier.FINAL + Modifier.STATIC);
            errm = modifiers & ~INTERFACE_FIELD_MODIFIERS;
            stype = "Interface constants";
        } else {
            errm = modifiers & ~FIELD_MODIFIERS;
            stype = "Fields";
        }
        if (errm != 0) {
            YYNode n = m.findFirst(errm);
            n.reportError(stype + " can't be " + Modifier.toString(errm));
            return modifiers &= ~errm;
        }
        return modifiers;
    }

    public Iterator<Node> iterator() { return decls.iterator(); }

}