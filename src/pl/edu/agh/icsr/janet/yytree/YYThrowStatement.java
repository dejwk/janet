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

    class DumpIterator implements Iterator {
        boolean excreturned;
        public boolean hasNext() { return !excreturned; }
        public Object next() {
            if (!excreturned) { excreturned = true; return exception; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }
}