/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;
import java.io.*;

public class YYNullLiteral extends YYExpression {

    private boolean workingFlag;

    public YYNullLiteral(IJavaContext cxt) {
        super(cxt);
    }

    public void resolve(boolean isSubexpression) throws CompileException {
        expressionType = classMgr.NULL;
        exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public boolean isVariable() { return false; }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }

}