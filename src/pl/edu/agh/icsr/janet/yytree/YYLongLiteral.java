/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.HashMap;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYLongLiteral extends YYExpression {
    long val;

    public YYLongLiteral(IJavaContext cxt, StringBuffer buf) {
        this(cxt, buf.toString());
    }

    public YYLongLiteral(IJavaContext cxt, String s) {
        super(cxt);
        char ch = s.charAt(s.length()-1);
        if (ch == 'l' || ch == 'L') s = s.substring(0, s.length()-1);
        val = Long.parseLong(s);
    }

    public void resolve(boolean isSubexpression) throws CompileException {
        expressionType = classMgr.LONG;
        exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public long getValue() { return val; }
    public boolean isVariable() { return false; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

}