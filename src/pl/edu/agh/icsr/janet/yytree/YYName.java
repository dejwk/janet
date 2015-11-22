/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.IDetailedLocationContext;
import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.reflect.ClassManager;

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
