/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.yytree.YYStatement;

public class YYNativeCode extends YYStatement {


    public YYNativeCode(IJavaContext cxt) {
        super(cxt);
    }

    public YYNativeCode setType(int type) {
        return this;
    }

    public boolean isJava() { return false; }

    public YYStatement compact() {
/*        isPureNative = true;
        YYStatement s = (YYStatement)firstSon();
        while (s != null) {
            if (s.isJava() || !s.isPure()) {
                isPureNative = false;
                break;
            }
            s = (YYStatement)s.nextBrother();
        }*/

        return this;
    }
}