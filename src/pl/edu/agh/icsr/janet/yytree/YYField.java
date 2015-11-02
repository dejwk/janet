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

    public Iterator iterator() { return decls.iterator(); }

}