/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.io.IOException;
import java.util.Iterator;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.Writer;
import pl.edu.agh.icsr.janet.natives.YYNativeCode;

public class YYStaticNativeStatement extends YYStatement {

    YYClass declaringClass;
    String language;
    YYNativeCode ncode;

    public YYStaticNativeStatement(IJavaContext cxt) {
        super(cxt, false, true);
        this.declaringClass = cxt.getScope().getCurrentClass();
    }

    public YYStaticNativeStatement setImplementation(YYNativeCode ncode) {
        this.ncode = ncode;
        return this;
    }

    public YYStaticNativeStatement setNativeLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getNativeLanguage() {
        return this.language;
    }

    public YYNativeCode getImplementation() {
        return ncode;
    }

    public YYClass getDeclaringClass() {
        return this.declaringClass;
    }

    public void write(Writer w) throws IOException {
        w.getNativeWriter().writeStaticNativeStatement(this);
    }

    class DumpIterator implements Iterator<YYNode> {
        boolean bodyreturned;
        DumpIterator() {
            bodyreturned = false;
        }
        public boolean hasNext() {
            return !bodyreturned;
        }
        public YYNode next() {
            if (!bodyreturned) { bodyreturned = true; return ncode; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }

}
