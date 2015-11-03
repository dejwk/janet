/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import pl.edu.agh.icsr.janet.*;

public interface IMemberInfo {
    public IClassInfo getDeclaringClass();
    public String getName();
    public int getModifiers();
//    public boolean isAccessibleTo(IClassInfo cls) throws CompileException;
    //public String getSignature() throws CompileException;
}
