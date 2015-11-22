/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.*;

public class YYEnclosedNativeStatements extends YYStatement {

    YYNativeCode ncode;

    public YYEnclosedNativeStatements(IJavaContext cxt, YYNativeCode ncode) {
        super(cxt, false);
        this.ncode = ncode;
    }

    public void resolve() throws ParseException {
        ncode.resolve();
        addExceptions(ncode.getExceptionsThrown());
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return ncode.write(w, param);
    }

    class DumpIterator implements Iterator<YYNode> {
        int i=0;
        DumpIterator() { i=0; }
        public boolean hasNext() { return i<1; }
        public YYNode next() {
            i++;
            return i==1 ? ncode : null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }

    public YYNativeCode getNativeCode() { return ncode; }

}
