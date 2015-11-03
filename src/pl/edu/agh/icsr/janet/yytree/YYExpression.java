/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;

public abstract class YYExpression extends YYStatement
        implements ILocationContext {
    ClassManager classMgr;
    IClassInfo expressionType;
    IClassInfo castedImplicitlyToType;

    public YYExpression(IDetailedLocationContext cxt) {
        super(cxt, false, false);
        classMgr = cxt.getClassManager();
    }

    public IClassInfo getExpressionType() {
        return expressionType;
    }

    void setImplicitCastType(IClassInfo type) {
        this.castedImplicitlyToType = type;
    }

    public IClassInfo getImplicitCastType() {
        return castedImplicitlyToType;
    }

    public void resolve() throws ParseException {
        resolve(false);
    }

    public abstract void resolve(boolean isSubexpression)
            throws ParseException;

    public boolean isVariable() throws CompileException { return false; }

    public ClassManager getClassManager() { return classMgr; }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }
}