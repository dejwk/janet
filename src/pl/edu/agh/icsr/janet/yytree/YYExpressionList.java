/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.tree.Node;

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
        Iterator<Node> itr;
        for (itr = iterator(), i=0; itr.hasNext(); i++) {
            ((YYExpression)itr.next()).setImplicitCastType(types[i]);
        }
        if (i != types.length) throw new RuntimeException();
    }

    public IClassInfo[] getExpressionTypes() {
        if (expressionTypes != null) return expressionTypes;
        expressionTypes = new IClassInfo[length];
        int i;
        Iterator<Node> itr;
        for (itr = iterator(), i=0; itr.hasNext(); i++) {
            expressionTypes[i] = ((YYExpression)itr.next()).getExpressionType();
        }
        return expressionTypes;
    }

    public int getLength() { return length; }

    public void resolve() throws ParseException {
        for (Iterator<Node> i = iterator(); i.hasNext();) {
            YYExpression e = (YYExpression)i.next();
            e.resolve(true);
            addExceptions(e.getExceptionsThrown());
        }
        if (exceptions == null) exceptions = new HashMap<IClassInfo, YYStatement>();
    }

}