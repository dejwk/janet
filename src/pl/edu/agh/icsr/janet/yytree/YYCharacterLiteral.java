/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.*;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYCharacterLiteral extends YYExpression {
    char val;

/*    public YYCharacterLiteral(IJavaContext cxt, StringBuffer buf) {
        this(cxt, buf.toString());
    }*/

    public YYCharacterLiteral(IJavaContext cxt, Character ch) {
        super(cxt);
/*        if (s.length() != 1) {
            throw new IllegalArgumentException("Literal longer " +
                "than one character");
        }*/
        val = ch.charValue();
    }

    public void resolve(boolean isSubexpression) throws CompileException {
        expressionType = classMgr.CHAR;
        exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public char getValue() { return val; }
    public boolean isVariable() { return false; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }
}