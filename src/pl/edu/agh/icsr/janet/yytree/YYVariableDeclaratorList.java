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
import java.util.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYVariableDeclaratorList extends YYStatement {

//    int modifiers = 0;
//    YYType type;
//    YYClass cls; // declaring class (for field declarations only)

    public YYVariableDeclaratorList(IJavaContext cxt) {
        super(cxt);
    }

    public YYVariableDeclaratorList add(YYVariableDeclarator var) {
        append(var);
        if (!var.isPure()) pure = false;
        return this;
    }

    public YYVariableDeclaratorList setModifiers(YYModifierList m) {
        return setModifiers(m.getModifiers()); // TODO: maybe check modifiers
    }

    public YYVariableDeclaratorList setDeclarationType(int dcltype) {
        Iterator i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setDeclarationType(dcltype);
        }
        return this;
    }

    public YYVariableDeclaratorList setModifiers(int modifiers) {
//        this.modifiers = m.getModifiers();
        Iterator i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setModifiers(modifiers);
        }
        return this;
    }

    public YYVariableDeclaratorList setType(YYType t) {
//        this.type = t;
        Iterator i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setType(t);
        }
        return this;
    }

    public YYVariableDeclaratorList setDeclaringClass(YYClass cls) {
//        this.type = t;
        Iterator i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setDeclaringClass(cls);
        }
        return this;
    }

    public String toString() {
        String s = "";
        for (Iterator i = iterator(); i.hasNext();) {
            s += ((YYVariableDeclarator)i.next()).toString();
            if (i.hasNext()) s += ", ";
        }
        return s;
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public String getTypeList() {
        try {
            String s = "";
            for (Iterator i = iterator(); i.hasNext();) {
                s += ((YYVariableDeclarator)i.next()).getType().getFullName();
                if (i.hasNext()) s += ", ";
            }
            return s;
        } catch (ParseException e) { throw new IllegalStateException(); }
    }
}
