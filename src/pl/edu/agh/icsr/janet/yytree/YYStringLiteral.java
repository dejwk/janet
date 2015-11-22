/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYStringLiteral extends YYExpression {
    String val;
    int stridx = -1;

    public YYStringLiteral(IJavaContext cxt, String s) {
        super(cxt);
        val = s;
    }

    public void resolve(boolean isSubexpression)
            throws CompileException {
        stridx = registerStringLiteral(val);
        expressionType = classMgr.String;
        exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public boolean isVariable() { return false; }

    public int getStringIdx() { return stridx; }
}