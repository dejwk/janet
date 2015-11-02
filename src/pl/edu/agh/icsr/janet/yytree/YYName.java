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

public class YYName extends YYNode implements IDetailedLocationContext {

    public static final int AMBIGIOUS  = 0;
    public static final int PACKAGE    = 1;
    public static final int TYPE       = 2;
    public static final int EXPRESSION = 3;
    public static final int METHOD     = 4;

    protected ClassManager classMgr;
    protected YYCompilationUnit compUnit;
    protected IScope dclUnit;

    public YYName(IDetailedLocationContext cxt) {
        super(cxt);
        classMgr = cxt.getClassManager();
        compUnit = cxt.getCompilationUnit();
        dclUnit = cxt.getScope();
    }

    public YYName add(YYNameNode n) {
        return (YYName)super.append(n);
    }

    public final YYNameNode firstNameNode() {
        return (YYNameNode)firstSon();
    }

    public final YYNameNode lastNameNode() {
        return (YYNameNode)lastSon();
    }

/*    public String getSeparator() {
        return ".";
    }
*/

    public YYPackage reclassifyAsPackage() {
        return lastNameNode().reclassifyAsPackage();
    }

    public YYType reclassifyAsType() {
        return lastNameNode().reclassifyAsType();
    }

    public YYExpression reclassifyAsExpression(IJavaContext cxt) {
        return lastNameNode().reclassifyAsExpression(cxt.getVariables());
    }

    public YYMethodInvocationExpression reclassifyAsMethodInvocation(
            IJavaContext cxt) {
        return lastNameNode().reclassifyAsMethodInvocation(cxt.getVariables());
    }


    /**
     * When all classes are already parsed and any semantic information is
     * known
     * @returns:
     * full package name as String, if Name represents package
     * IClassInfo, if Name represents type
     * YYFieldAccessExpression, if Name represents field access
     */
    public Object reclassify() throws ParseException {
        return lastNameNode().reclassify();
    }

    public ClassManager getClassManager() { return classMgr; }
    public YYCompilationUnit getCompilationUnit() { return compUnit; }
    public IScope getScope() { return dclUnit; }

}
