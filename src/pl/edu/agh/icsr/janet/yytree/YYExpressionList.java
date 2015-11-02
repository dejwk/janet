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

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;

public class YYExpressionList extends YYStatement {

    private int length;
    IClassInfo[] expressionTypes;

    public YYExpressionList(IJavaContext cxt) {
        super(cxt, false, false);
        length = 0;
    }

    public YYExpressionList add(YYExpression e) {
        length++;
        return (YYExpressionList)append(e);
    }

    void setImplicitCastTypes(IClassInfo[] types) {
        int i;
        Iterator itr;
        for (itr = iterator(), i=0; itr.hasNext(); i++) {
            ((YYExpression)itr.next()).setImplicitCastType(types[i]);
        }
        if (i != types.length) throw new RuntimeException();
    }

    public IClassInfo[] getExpressionTypes() {
        if (expressionTypes != null) return expressionTypes;
        expressionTypes = new IClassInfo[length];
        int i;
        Iterator itr;
        for (itr = iterator(), i=0; itr.hasNext(); i++) {
            expressionTypes[i] = ((YYExpression)itr.next()).getExpressionType();
        }
        return expressionTypes;
    }

    public int getLength() { return length; }

    public void resolve() throws ParseException {
        for(Iterator i = iterator(); i.hasNext();) {
            YYExpression e = (YYExpression)i.next();
            e.resolve(true);
            addExceptions(e.getExceptionsThrown());
        }
        if (exceptions == null) exceptions = new HashMap();
    }

}