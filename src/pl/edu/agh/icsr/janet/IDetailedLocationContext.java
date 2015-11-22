/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import pl.edu.agh.icsr.janet.reflect.ClassManager;
import pl.edu.agh.icsr.janet.yytree.IScope;
import pl.edu.agh.icsr.janet.yytree.YYCompilationUnit;

public interface IDetailedLocationContext extends ILocationContext {
    public ClassManager getClassManager();
    public YYCompilationUnit getCompilationUnit();
    public IScope getScope();
}