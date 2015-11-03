/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYRelationalExpression extends YYExpression {
    public static final int LS = 1;
    public static final int GT = 2;
    public static final int LE = 3;
    public static final int GE = 4;
    public static final int EQ = 5;
    public static final int NE = 6;

    public static final int TYPE_NUMERIC   = 0x10;
    public static final int TYPE_BOOLEAN   = 0x11;
    public static final int TYPE_REFERENCE = 0x12;

    int kind;
    int type;
    YYExpression e1;
    YYExpression e2;

    public YYRelationalExpression(IJavaContext cxt, int kind, YYExpression e1,
                                  YYExpression e2) {
        super(cxt);
        this.kind = kind;
        this.e1 = e1;
        this.e2 = e2;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        e1.resolve(true);
        e2.resolve(true);
        IClassInfo c1 = e1.getExpressionType();
        IClassInfo c2 = e2.getExpressionType();
        if (c1 == classMgr.NATIVETYPE && c2 == classMgr.NATIVETYPE) {
            reportError("At last one side of relational expression must " +
                "have determinable type (use explicit cast)");
        }
        switch (kind) {
        case LS:
        case GT:
        case LE:
        case GE:
            if (!classMgr.isNumericOrNativeType(c1)) {
                reportNotNumericType(c1);
            }
            if (!classMgr.isNumericOrNativeType(c2)) {
                reportNotNumericType(c2);
            }
            type = TYPE_NUMERIC;
            break;
        case EQ:
        case NE:
            if ((classMgr.isNumericOrNativeType(c1) &&
                    classMgr.isNumericOrNativeType(c2))) {
                type = TYPE_NUMERIC;
                break;
            }
            if ((c1 == classMgr.BOOLEAN || c1 == classMgr.NATIVETYPE) &&
                    (c2 == classMgr.BOOLEAN || c2 == classMgr.NATIVETYPE)) {
                type = TYPE_BOOLEAN;
                break;
            }
            if ((c1 == classMgr.NULL || c1.isReference()) &&
                    (c2 == classMgr.NULL || c2.isReference())) {
                type = TYPE_REFERENCE;
                break;
            }
            reportError("Incompatible types for comparison: " + c1 + " and " +
                c2);
            break;

        default:
            throw new RuntimeException();
        }

        switch(type) {
        case TYPE_NUMERIC:
            IClassInfo casttype = classMgr.getBinaryNumericPromotedType(c1, c2);
            e1.setImplicitCastType(casttype);
            e2.setImplicitCastType(casttype);
            break;
        case TYPE_BOOLEAN:
            e1.setImplicitCastType(classMgr.BOOLEAN);
            e2.setImplicitCastType(classMgr.BOOLEAN);
            break;
        }
        expressionType = classMgr.BOOLEAN;
        addExceptions(e1.getExceptionsThrown());
        addExceptions(e2.getExceptionsThrown());
    }

    public YYExpression getLeftExpression() { return e1; }
    public YYExpression getRightExpression() { return e2; }
    public int getKind() { return kind; }
    public int getType() { return type; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    private void reportNotNumericType(IClassInfo cls) throws CompileException {
        reportError("numeric primitive type expected, found: " + cls);
    }

    class DumpIterator implements Iterator {
        int i = 0;
        DumpIterator() {}
        public boolean hasNext() { return i<2; }
        public Object next() {
            i++;
            return (i == 1 ? e1 : i == 2 ? e2 : null);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

}