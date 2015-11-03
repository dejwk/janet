/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.yytree.*;

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

    class DumpIterator implements Iterator {
        Iterator i;
        DumpIterator() {
            i = YYNativeCode.this.iterator();
        }
        public boolean hasNext() { return i.hasNext(); }
        public Object next() {
            if (i.hasNext()) return i.next();
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

}