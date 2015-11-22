/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import pl.edu.agh.icsr.janet.ParseException;

public interface IFieldInfo extends /*IVariableInfo, */IMemberInfo {
    IClassInfo getType() throws ParseException;
    String getName();
}
