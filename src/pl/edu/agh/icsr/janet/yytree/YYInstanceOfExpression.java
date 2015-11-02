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

import java.io.*;
import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;

public class YYInstanceOfExpression extends YYExpression {

    YYType unresolvedType;
    YYExpression target;

    IClassInfo testedType;
    boolean needsOnlyCheckForNull = false;
    int classidx;

    public YYInstanceOfExpression(IJavaContext cxt, YYExpression target,
                                  YYType yytype) {
        super(cxt);
        this.unresolvedType = yytype;
        this.target = target;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        target.resolve(true);
        IClassInfo tt = target.getExpressionType();
        if (!tt.isReference() && tt != classMgr.NULL) {
            reportError("Invalid expression type; required: reference " +
                "type or null, found: " + tt);
        }
        testedType = unresolvedType.getResolvedType();
        switch (tt.isCastableTo(testedType)) {
        case IClassInfo.CAST_INCORRECT:
            reportError("it is impossible for " + tt + " to be instance of " +
                testedType);
            break;
        case IClassInfo.CAST_CORRECT:
            needsOnlyCheckForNull = true;
            break;
        case IClassInfo.CAST_REQUIRES_RTCHECK:
            needsOnlyCheckForNull = false;
            classidx = registerClass(testedType);
            break;
        }

        expressionType = classMgr.BOOLEAN;
        addExceptions(target.getExceptionsThrown());
    }

    public YYExpression getTarget() {
        return target;
    }

    public IClassInfo getTestedType() {
        return testedType;
    }

    public boolean needsOnlyCheckForNull() {
        return needsOnlyCheckForNull;
    }

    public int getClassIdx() {
        return classidx;
    }

    class DumpIterator implements Iterator {
        int i = 0;
        DumpIterator() {}
        public boolean hasNext() { return i<1; }
        public Object next() {
            i++;
            return (i == 1 ? target : null);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }
}