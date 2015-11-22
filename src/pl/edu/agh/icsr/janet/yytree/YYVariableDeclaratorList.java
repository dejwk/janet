/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import java.util.*;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.tree.Node;

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
        Iterator<Node> i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setDeclarationType(dcltype);
        }
        return this;
    }

    public YYVariableDeclaratorList setModifiers(int modifiers) {
//        this.modifiers = m.getModifiers();
        Iterator<Node> i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setModifiers(modifiers);
        }
        return this;
    }

    public YYVariableDeclaratorList setType(YYType t) {
//        this.type = t;
        Iterator<Node> i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setType(t);
        }
        return this;
    }

    public YYVariableDeclaratorList setDeclaringClass(YYClass cls) {
//        this.type = t;
        Iterator<Node> i = iterator();
        while (i.hasNext()) {
            ((YYVariableDeclarator)i.next()).setDeclaringClass(cls);
        }
        return this;
    }

    public String toString() {
        String s = "";
        for (Iterator<Node> i = iterator(); i.hasNext();) {
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
            for (Iterator<Node> i = iterator(); i.hasNext();) {
                s += ((YYVariableDeclarator)i.next()).getType().getFullName();
                if (i.hasNext()) s += ", ";
            }
            return s;
        } catch (ParseException e) { throw new IllegalStateException(); }
    }
}
