/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import java.util.Stack;

import pl.edu.agh.icsr.janet.yytree.IScope;
import pl.edu.agh.icsr.janet.yytree.YYVariableDeclarator;

public class VariableStack {
    Stack<YYVariableDeclarator> variables = new Stack<YYVariableDeclarator>();

    public YYVariableDeclarator find(String name) {
        for (int i=0, size = variables.size(); i<size; i++) {
            YYVariableDeclarator var;
            var = variables.get(i);
            if (var.getName().equals(name)) {
                return var;
            }
        }
        return null;
    }

    public void push(YYVariableDeclarator var) throws CompileException {
        if (var == null) return;
        YYVariableDeclarator oldvar = find(var.getName());
        if (oldvar != null) {
            var.reportError("Variable " + var.getName() +
                " already declared at line " + (oldvar.lbeg().lineno+1));
        }
        //System.out.println("adding " + var.getName());
        variables.push(var);
    }

    public void pop(IScope dclUnit) {
        while (!variables.empty() &&
                variables.peek().
                    getEnclosingScope() == dclUnit) {
        //System.out.println("popping " + ((YYVariableDeclarator)variables.peek()).getName());
            variables.pop();
        }
    }
}
