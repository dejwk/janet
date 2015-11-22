/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.HashMap;
import java.util.Iterator;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.reflect.ClassManager;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYCatchClause extends YYStatement {

    YYVariableDeclarator formalParameter;
    YYStatement body;
    int excclsidx;

    public YYCatchClause(IJavaContext cxt) {
        super(cxt, false);
    }

    public YYCatchClause setFormalParameter(YYVariableDeclarator formal) {
        this.formalParameter = formal;
        return this;
    }

    public YYCatchClause setBody(YYStatement body) {
        this.body = body;
        return this;
    }

    public YYStatement getBody() { return body; }
    public YYVariableDeclarator getFormalParameter() { return formalParameter; }
    public int getExcClsIdx() { return excclsidx; }

    public IClassInfo getCatchedExceptionType() throws ParseException {
        return formalParameter.getType();
    }

    public void resolve() throws ParseException {
        formalParameter.resolve();
        body.resolve();
        IClassInfo exctype = formalParameter.getType();
        ClassManager classMgr = getCurrentClass().getClassManager();
        if (!exctype.isAssignableFrom(classMgr.Throwable)) {
            formalParameter.reportError("Can't catch " + exctype +
                ", it must be subclass of java.lang.Throwable");
        }
        excclsidx = registerClass(exctype);
        exceptions = new HashMap<IClassInfo, YYStatement>();
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator<YYNode> {
        int i=0;
        DumpIterator() { i=0; }
        public boolean hasNext() { return i<2; }
        public YYNode next() {
            i++;
            return i==1 ? formalParameter : i==2 ? body : null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }
}