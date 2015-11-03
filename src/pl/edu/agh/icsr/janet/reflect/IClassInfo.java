/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;
import pl.edu.agh.icsr.janet.*;
import java.util.*;

public interface IClassInfo {

    final static int CAST_CORRECT          = 1;
    final static int CAST_REQUIRES_RTCHECK = 2;
    final static int CAST_INCORRECT        = 3;

    IClassInfo getDeclaringClass() throws CompileException;
    IClassInfo getSuperclass() throws ParseException;
    boolean isInterface() throws CompileException;
    boolean isArray();
    boolean isPrimitive();
    boolean isReference();
    String getSimpleName();
    String getPackageName() throws CompileException;
    String getFullName(); // throws CompileException;
    int getModifiers();
    boolean isAccessibleTo(String pkg) throws CompileException;
    String getSignature();// throws CompileException;
    IClassInfo getComponentType() throws CompileException;
    IClassInfo getArrayType() throws CompileException;
    IClassInfo getArrayType(int dims) throws CompileException;
    Map getInterfaces() throws ParseException;
    Map getDeclaredFields() throws ParseException;
    SortedMap getAccessibleFields() throws ParseException;
    SortedMap getFields(String name) throws ParseException;
    SortedMap getDeclaredMethods() throws ParseException;
    SortedMap getAccessibleMethods() throws ParseException;
    SortedMap getMethods(String name) throws ParseException;
    SortedMap getMethods(String name, String jlssign) throws ParseException;
    Map getConstructors() throws ParseException;
//    boolean equals(IClassInfo cls) throws CompileException;
    // in terms of method invocation conversion (JLS 5.3)
    boolean isAssignableFrom(IClassInfo cls) throws ParseException;
    int isCastableTo(IClassInfo cls) throws ParseException;
    boolean isSubclassOf(IClassInfo cls) throws ParseException; // sub or this

    void setWorkingFlag(boolean working);
    boolean getWorkingFlag();

    String getJNIName(); // throws CompileException;
    String getJNIType();
}
