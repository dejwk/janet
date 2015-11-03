/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;

public class YYLocalVariableAccessExpression extends YYExpression {

    YYVariableDeclarator variable;
    boolean external;
    boolean parameter;

    public YYLocalVariableAccessExpression(IDetailedLocationContext cxt,
                                           YYVariableDeclarator var) {
        super(cxt);
        this.variable = var;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        expressionType = variable.getType();
        exceptions = new HashMap(); // no exceptions

        IScope scope = getCurrentMember();

        // if the variable access appears inside native function body or
        // inside native statement, check whether the declaration is inside
        // as well (it may be false for native statements accessing local
        // variables declared in outer scopes)

        if (scope instanceof YYNativeStatement) {
            YYNativeStatement stmt = (YYNativeStatement)scope;
            IScope vscope = variable.getEnclosingScope();
            while (vscope != null && vscope != stmt) {
                vscope = vscope.getEnclosingScope();
            }
            if (vscope == null) { // not in scope of the native statement
                this.external = true;
                stmt.addExternalVariable(variable);
            }
        }
    }

    public YYVariableDeclarator getVariable() {
        return variable;
    }

    public boolean isExternal() {
        return external;
    }

    public boolean isVariable() {
        return (!this.external && !Modifier.isFinal(variable.getModifiers()));
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

}
