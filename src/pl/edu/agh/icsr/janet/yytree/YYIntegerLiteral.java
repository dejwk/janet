/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;

public class YYIntegerLiteral extends YYExpression {
    int val;

    public YYIntegerLiteral(IJavaContext cxt, StringBuffer buf) {
        this(cxt, buf.toString());
    }

    public YYIntegerLiteral(IJavaContext cxt, String s) {
        super(cxt);
        val = Integer.parseInt(s);
    }

    public void resolve(boolean isSubexpression) throws CompileException {
        expressionType = classMgr.INT;
        exceptions = new HashMap();
    }

    public boolean isCastableTo(IClassInfo cls) {
        if (cls == classMgr.INT) {
            return true;
        } else if (cls == classMgr.SHORT) {
            return (int)(short)(val) == val;
        } else if (cls == classMgr.CHAR) {
            return (int)(char)(val) == val;
        } else if (cls == classMgr.BYTE) {
            return (int)(byte)(val) == val;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getValue() { return val; }
    public boolean isVariable() { return false; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }
}