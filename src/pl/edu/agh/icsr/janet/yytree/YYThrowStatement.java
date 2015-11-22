/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.IWriter;
import java.util.*;

public class YYThrowStatement extends YYStatement {

    YYExpression exception;

    public YYThrowStatement(IJavaContext cxt, YYExpression exception) {
        super(cxt, false);
        this.exception = exception;
    }

    public void resolve() throws ParseException {
        ClassManager classMgr = getCurrentClass().getClassManager();
        exception.resolve();
        IClassInfo exctype = exception.getExpressionType();
        if (!exctype.isAssignableFrom(classMgr.Throwable)) {
            exception.reportError("Can't throw " + exctype + ", it must be " +
                "subclass of java.lang.Throwable");
        }
        addExceptions(exception.getExceptionsThrown());
        addException(exctype);
        addException(classMgr.NullPointerException);

        getCurrentClass().addReferencedClass(exctype);
    }

    public YYExpression getExceptionExpression() { return exception; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator<YYNode> {
        boolean excreturned;
        public boolean hasNext() { return !excreturned; }
        public YYNode next() {
            if (!excreturned) { excreturned = true; return exception; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }
}