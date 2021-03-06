/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.Iterator;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYFinally extends YYStatement {

    YYStatement body;

    public YYFinally(IJavaContext cxt, YYStatement body) {
        super(cxt);
        this.body = body;
    }

    public YYStatement getBody() { return body; }

    public void resolve() throws ParseException {
        body.resolve();
        addExceptions(body.getExceptionsThrown());
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator<YYNode> {
        int i=0;
        DumpIterator() { i=0; }
        public boolean hasNext() { return i<1; }
        public YYNode next() {
            i++;
            return i==1 ? body : null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }
}