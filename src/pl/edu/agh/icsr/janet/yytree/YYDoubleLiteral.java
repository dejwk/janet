/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.*;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYDoubleLiteral extends YYExpression {
    double val;

    public YYDoubleLiteral(IJavaContext cxt, StringBuffer buf) {
        this(cxt, buf.toString());
    }

    public YYDoubleLiteral(IJavaContext cxt, String s) {
        super(cxt);
        val = Double.parseDouble(s);
    }

    public void resolve(boolean isSubexpression) throws CompileException {
        expressionType = classMgr.DOUBLE;
        exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public double getValue() { return val; }
    public boolean isVariable() { return false; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }
}