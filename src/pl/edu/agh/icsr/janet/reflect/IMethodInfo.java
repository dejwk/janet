/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import pl.edu.agh.icsr.janet.*;
import java.util.*;

public interface IMethodInfo extends IMemberInfo {
    public IClassInfo getReturnType() throws ParseException;
    public boolean isConstructor();
    public String getArgumentSignature() throws ParseException;
    public String getJLSSignature() throws ParseException;
    public String getJNISignature() throws ParseException;
    public Map<String, IClassInfo> getExceptionTypes() throws ParseException;
    public IClassInfo[] getParameterTypes() throws ParseException;
}
