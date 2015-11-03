/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import pl.edu.agh.icsr.janet.yytree.IScope;
import pl.edu.agh.icsr.janet.yytree.YYVariableDeclarator;
import pl.edu.agh.icsr.janet.yytree.YYVariableDeclaratorList;

public interface IMutableContext extends IJavaContext {
    public void pushScope(IScope unit);
    public IScope popScope();
    public void addVariable(YYVariableDeclarator var) throws CompileException;
    public void addVariables(YYVariableDeclaratorList vars) throws CompileException;
}
