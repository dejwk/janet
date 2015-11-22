/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.IJavaContext;

public class YYImportDeclarationList extends YYNode {

    HashMap<String, YYName> singles;
    Vector<String> onDemands;

    public YYImportDeclarationList(IJavaContext cxt) {
        super(cxt);
        singles = new HashMap<String, YYName>();
        onDemands = new Vector<String>();
        onDemands.addElement("java.lang");
    }

    Map<String, YYName> getSingles() {
        return singles;
    }

    List<String> getOnDemands() {
        return onDemands;
    }

    public String findSingle(String singleName) {
        ensureUnlocked();
        YYName name = singles.get(singleName);
        return name == null ? null : name.toString();
    }

/*    public YYImportDeclarationList add(YYNode n) throws CompileException {
        //if (locked) throw new IllegalStateException();
        if (n instanceof YYType) { // single (JLS 7.5.1)
            YYType t1 = (YYType)n;
            YYType t2 = findSingle(t1.getSimpleName());
            if (t2 != null) {
                if (t1.toString() != t2.toString()) { // qualified names differ
                    t1.reportError("Ambigious class: " + t1.toString() +
                        " and " + t2.toString());
                } else { // duplicate declaration - ignore
                }
            } else {
                singles.put(t1.getSimpleName(), t1);
            }
        } else if (n instanceof YYPackage) { // on demand (JLS 7.5.2)
            YYPackage p = (YYPackage)n;
            onDemands.addElement(p.toString());
        } else {
            throw new IllegalArgumentException();
        }

        return (YYImportDeclarationList)append(n);
    }
*/

    public YYImportDeclarationList addSingle(YYName n) throws CompileException {
        ensureUnlocked();
        String simple = n.lastNameNode().toString();
        String newtype = n.toString();
        String oldtype = findSingle(simple);
        if (oldtype != null) {
            if (!oldtype.equals(n.toString())) {
                n.reportError("Ambigious class: " + oldtype + " and " +
                    newtype);
            } else {
                // ignore
            }
        } else {
            singles.put(simple, n);
        }
        return (YYImportDeclarationList)append(n);
    }

    public YYImportDeclarationList addOnDemand(YYName n)
            throws CompileException {
        ensureUnlocked();
        onDemands.add(n.toString());
        return (YYImportDeclarationList)append(n);
    }

}
