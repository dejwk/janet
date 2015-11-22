/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.Iterator;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;
import pl.edu.agh.icsr.janet.tree.Node;

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
        for (Node node : dimexprs) {
            YYExpression dimexpr = (YYExpression)node;
            if (dimexpr.getExpressionType() != classMgr.NATIVETYPE &&
                    !dimexpr.getExpressionType().isAssignableFrom(classMgr.INT)) {
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
    public int[] getClassIdxs() { return classidxs.clone(); }
    public IClassInfo getBaseType() { return baseType; }

    public Iterator<YYNode> getDumpIterator() { return dimexprs.getDumpIterator(); }

}