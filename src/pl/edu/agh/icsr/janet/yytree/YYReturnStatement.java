/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;
import java.io.*;

public class YYReturnStatement extends YYStatement { // JLS 14.16

    YYExpression retexpr;
    ClassManager classMgr;

    public YYReturnStatement(IDetailedLocationContext cxt, YYExpression expr) {
        super(cxt, false);
        this.retexpr = expr;
        classMgr = cxt.getClassManager();
    }

    public void resolve() throws ParseException {
        if (retexpr != null) {
            retexpr.resolve(false);
        }
        IScope scope = this.getCurrentMember();
        if (!(scope instanceof IMethodInfo)) {
            this.reportError("return outside method");
        }
        IMethodInfo mth = (IMethodInfo)scope;
        IClassInfo mthtype = mth.getReturnType();
        if (retexpr == null) {
            if (mthtype != classMgr.VOID) {
                reportError("missing return value");
            }
        } else {
            IClassInfo rettype = retexpr.getExpressionType();
            if (mth.isConstructor()) {
                reportError("cannot return value from a constructor");
            } else if (mthtype == classMgr.VOID) {
                reportError("cannot return value from method returning void");
            } else if (!rettype.isAssignableFrom(mthtype)) { // check literals
                if (retexpr instanceof YYIntegerLiteral) {
                    YYIntegerLiteral lit = (YYIntegerLiteral)retexpr;
                    if (mthtype == classMgr.SHORT || mthtype == classMgr.CHAR ||
                            mthtype == classMgr.BYTE) {
                        if (!lit.isCastableTo(mthtype)) {
                            reportError("value of integer constant is too wide " +
                                                         "to be converted to " + mthtype);
                        }
                    }
                } else {
                    reportError("incompatible types; found: " + rettype +
                                ", required: " + mthtype);
                }
            }
        }
        // if we are here, assignment check was successful
        if (retexpr != null) {
            retexpr.setImplicitCastType(mthtype);
            addExceptions(retexpr.getExceptionsThrown());
        } else {
            addExceptions(new HashMap<IClassInfo, YYStatement>());
        }
    }

    public YYExpression getReturnedExpression() { return retexpr; }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator<YYNode> {
        boolean retexpreturned = false;
        public boolean hasNext() { return !retexpreturned; }
        public YYNode next() {
            if (retexpreturned) return null;
            retexpreturned = true;
            return retexpr;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }
}