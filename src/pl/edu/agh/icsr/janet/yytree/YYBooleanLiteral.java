/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.io.IOException;
import java.util.HashMap;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYBooleanLiteral extends YYExpression {
    boolean val;

    public YYBooleanLiteral(IJavaContext cxt, StringBuffer buf) {
        this(cxt, buf.toString());
    }

    public YYBooleanLiteral(IJavaContext cxt, String s) {
        super(cxt);
        if (s.equals("true")) {
            val = true;
        } else if (s.equals("false")) {
            val = false;
        } else {
            throw new IllegalArgumentException("Invalid boolean literal: " + s);
        }
    }

    public void resolve(boolean isSubexpression) throws CompileException {
        expressionType = classMgr.BOOLEAN;
        exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public boolean isVariable() { return false; }
    public boolean getValue() { return val; }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }

}