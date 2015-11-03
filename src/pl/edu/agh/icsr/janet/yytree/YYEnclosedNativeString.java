/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.*;
import java.util.*;

public class YYEnclosedNativeString extends YYExpression {

    boolean unicode;
    YYNativeCode ncode;

    public YYEnclosedNativeString(IJavaContext cxt, YYNativeCode ncode) {
        this(cxt, ncode, false);
    }

    public YYEnclosedNativeString(IJavaContext cxt, YYNativeCode ncode,
                                  boolean unicode) {
        super(cxt);
        this.unicode = unicode;
        this.ncode = ncode;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        expressionType = classMgr.String;
        ncode.resolve();
        addExceptions(ncode.getExceptionsThrown());
        addException(classMgr.NullPointerException);
    }

    public boolean isVariable() { return false; }

    public boolean isUnicode() { return unicode; }

    public YYNativeCode getNativeString() { return ncode; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }




    class DumpIterator implements Iterator {
        boolean ncodereturned;
        public boolean hasNext() { return !ncodereturned; }
        public Object next() {
            if (!ncodereturned) { ncodereturned = true; return ncode; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

}