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
