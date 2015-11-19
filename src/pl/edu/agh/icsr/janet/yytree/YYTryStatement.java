/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.util.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYTryStatement extends YYStatement {

    YYStatement body;
    YYStatement catches;
    YYFinally finly;

    public YYTryStatement(IJavaContext cxt, YYStatement body) {
        super(cxt, false);
        this.body = body;
    }

    public YYTryStatement addCatches(YYStatement catches) {
        this.catches = catches;
        return this;
    }

    public YYTryStatement addFinally(YYFinally finly) {
        this.finly = finly;
        return this;
    }

    public void resolve() throws ParseException {
        body.resolve();
        addExceptions(body.getExceptionsThrown());
        if (catches != null) {
            Iterator i;
            // remove catched exception
            for (i = catches.iterator(); i.hasNext();) {
                YYCatchClause c = (YYCatchClause)i.next();
                c.resolve();
                if (!catchException(c.getCatchedExceptionType())) {
                    c.reportError("exception " +
                        c.getCatchedExceptionType().getFullName() +
                        " is never thrown in the body of the corresponding " +
                        "try statement");
                }
            }
            // add exceptions thrown
            for (i = catches.iterator(); i.hasNext();) {
                YYCatchClause c = (YYCatchClause)i.next();
                addExceptions(c.getExceptionsThrown());
            }

        }
        if (finly != null) {
            finly.resolve();
            addExceptions(finly.getExceptionsThrown());
        }
    }

    public YYStatement getBody() { return body; }
    public YYStatement getCatches() { return catches; }
    public YYStatement getFinally() { return finly; }

    public boolean catchException(IClassInfo catched) throws ParseException {
        boolean mayBeThrown = false;
        for (Iterator i = exceptions.keySet().iterator(); i.hasNext();) {
            IClassInfo exc = (IClassInfo)i.next();
            if (exc.isSubclassOf(catched)) {
                i.remove();
                mayBeThrown = true;
            } else if (catched.isSubclassOf(exc)) {
                mayBeThrown = true;
            }
        }
        return mayBeThrown;
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator {
        int i=1;
        Object next = body;
        public boolean hasNext() { return i<4; }
        public Object next() {
            Object ret = i==1 ? body : i==2 ? catches : i==3 ? finly : null;
            i++;
            if (i==2 && catches == null) i++;
            if (i==3 && finly == null) i++;
            return ret;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }
}