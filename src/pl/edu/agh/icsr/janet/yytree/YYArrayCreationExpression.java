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

import java.lang.reflect.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;
import java.util.*;

/**
 * We are not supporting array initializers yet.
 */
public class YYArrayCreationExpression extends YYExpression {

    YYType unresolvedBaseType;
    YYExpressionList dimexprs;
    int dims;
    int depth;

    IClassInfo baseType;

    //IClassInfo[] argtypes;
    //IMethodInfo method;
    //int classidx;
    //int mthidx;

    //IClassInfo[] dimtypes;
    int[] classidxs;

    public YYArrayCreationExpression(IJavaContext cxt, YYType basetype,
                                     YYExpressionList dimexprs, int emptydims) {
        super(cxt);
        this.unresolvedBaseType = basetype;
        this.dimexprs = dimexprs;
        this.depth = dimexprs.getLength();
        this.dims = depth + emptydims;
        this.classidxs = new int[depth];
    }

    public YYExpressionList getDimExprs() {
        return dimexprs;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        // resolve base type
        baseType = unresolvedBaseType.getResolvedType();

        // is the type accessible?
        IClassInfo myclass = getCurrentClass();
        String mypkg = myclass.getPackageName();
        if (!baseType.isAccessibleTo(mypkg) &&
                classMgr.getSettings().strictAccess()) {
            reportError(baseType.toString() + " is not accessible " +
                "from " + myclass);
        }

        // check dim expressions
        dimexprs.resolve();
        Iterator itr = dimexprs.iterator();
        while (itr.hasNext()) {
            YYExpression dimexpr = (YYExpression)itr.next();
            if (!dimexpr.getExpressionType().isAssignableFrom(classMgr.INT)) {
                dimexpr.reportError(dimexpr.getExpressionType() +
                    "cannot be converted to int");
            }
            dimexpr.setImplicitCastType(classMgr.INT);
        }

        expressionType = baseType.getArrayType(dims);

        IClassInfo cls = expressionType;
        for (int i=0; i<depth; i++) {
            // add referenced class
            cls = cls.getComponentType();
            if (!cls.isPrimitive()) {
                this.classidxs[i] = registerClass(cls);
            } else {
                this.classidxs[i] = -1;
            }
        }

        addException(classMgr.OutOfMemoryError);
        addException(classMgr.NegativeArraySizeException);
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public int getDims() { return dims; }
    public int getDepth() { return depth; }
    public int[] getClassIdxs() { return (int[])classidxs.clone(); }
    public IClassInfo getBaseType() { return baseType; }

    public Iterator getDumpIterator() { return dimexprs.getDumpIterator(); }

}