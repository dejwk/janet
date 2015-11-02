/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Java Language Extensions (JANET) package,
 * http://www.icsr.agh.edu.pl/janet.
 *
 * The Initial Developer of the Original Code is Dawid Kurzyniec.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Dawid Kurzyniec <dawidk@icsr.agh.edu.pl>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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
        public boolean hasNext() { return i<3; }
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