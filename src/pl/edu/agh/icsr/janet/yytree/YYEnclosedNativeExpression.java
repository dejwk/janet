/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.*;

public class YYEnclosedNativeExpression extends YYExpression {

    YYNativeCode ncode;

    public YYEnclosedNativeExpression(IJavaContext cxt, YYNativeCode ncode) {
        super(cxt);
        this.ncode = ncode;
    }

    public boolean isFinal() { return true; }

    public void resolve(boolean isSubexpression) throws ParseException {
        ncode.resolve();
        addExceptions(ncode.getExceptionsThrown());
        expressionType = classMgr.NATIVETYPE;
    }

    public boolean isVariable() { return false; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public YYNativeCode getNativeCode() { return ncode; }

}