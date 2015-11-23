/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.io.IOException;
import java.util.HashMap;

import pl.edu.agh.icsr.janet.IDetailedLocationContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYThis extends YYExpression { // JLS 15.7.2, 15.10.2

    public static final int THIS = 1;
    public static final int SUPER = 2;

    int reftype;

    public YYThis(IDetailedLocationContext cxt, int reftype) {
        super(cxt);
        this.reftype = reftype;
    }

    public boolean isSuper() { return reftype == SUPER; }

    public void resolve(boolean isSubexpression) throws ParseException {
        YYClass cls = getCurrentClass();
        int memberType = getCurrentMember().getScopeType();
        if ((memberType & ~IScope.INSTANCE_CONTEXT) != 0) {
            reportError("keyword " + (isSuper() ? "super" : "this") +
                "may not be used outside instance method, constructor or " +
                "instance initializer");
        }
        if (reftype == THIS) {
            this.expressionType = cls;
        } else if (reftype == SUPER) {
            this.expressionType = cls.getSuperclass();
        } else {
            throw new RuntimeException();
        }
        exceptions = new HashMap<IClassInfo, YYStatement>(); // no exceptions thrown
    }

    public boolean isVariable() { return false; }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }

}