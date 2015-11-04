/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.IJavaContext;
import java.util.*;

import pl.edu.agh.icsr.janet.CompileException;

public class YYImportDeclarationList extends YYNode {

    HashMap singles;
    Vector onDemands;

    public YYImportDeclarationList(IJavaContext cxt) {
        super(cxt);
        singles = new HashMap();
        onDemands = new Vector();
        onDemands.addElement("java.lang");
    }

    Map getSingles() {
        return singles;
    }

    List getOnDemands() {
        return onDemands;
    }

    public String findSingle(String singleName) {
        ensureUnlocked();
        return (String)singles.get(singleName);
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
        String oldtype = (String)singles.get(simple);
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
