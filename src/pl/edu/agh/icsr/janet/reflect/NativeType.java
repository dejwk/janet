/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import pl.edu.agh.icsr.janet.*;
import java.util.*;

class NativeType implements IClassInfo {
    ClassManager cm;

    private boolean workingFlag;

    NativeType(ClassManager cm) {
        this.cm = cm;
    }

    public IClassInfo getSuperclass() {
        throw new UnsupportedOperationException();
    }

    public IClassInfo getArrayType()  {
        throw new UnsupportedOperationException();
    }

    public IClassInfo getArrayType(int dims) {
        throw new UnsupportedOperationException();
    }

    public IClassInfo getComponentType() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return "native type";
    }

    public String getSignature() {
        throw new UnsupportedOperationException();
    }

    public boolean isAccessibleTo(String pkg) throws CompileException {
        return true;
    }

    public IClassInfo getDeclaringClass() throws CompileException {
        throw new UnsupportedOperationException();
    }

    public int getModifiers() {
        throw new UnsupportedOperationException();
    }

    public String getSimpleName() {
        throw new UnsupportedOperationException();
    }

    public String getPackageName() {
        throw new UnsupportedOperationException();
    }

    public String getFullName() {
        throw new UnsupportedOperationException();
    }

    public String getJNIName() {
        throw new UnsupportedOperationException();
    }

    public String getJNIType() {
        throw new UnsupportedOperationException();
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isReference() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isInterface() {
        return false;
    }

    public Map<String, IFieldInfo> getDeclaredFields() { // final field length is not reflected
        throw new UnsupportedOperationException();
    }

    public SortedMap<String, IFieldInfo> getAccessibleFields() throws CompileException {
        throw new UnsupportedOperationException();
    }

    public SortedMap<String, IFieldInfo> getFields(String name) throws CompileException {
        throw new UnsupportedOperationException();
    }

    public SortedMap<String, IMethodInfo> getDeclaredMethods() {
        throw new UnsupportedOperationException();
    }

    public Map<String, IMethodInfo> getConstructors() {
        throw new UnsupportedOperationException();
    }

    public SortedMap<String, IMethodInfo> getAccessibleMethods() {
        throw new UnsupportedOperationException();
    }

    public SortedMap<String, IMethodInfo> getMethods(String name) {
        throw new UnsupportedOperationException();
    }

    public SortedMap<String, IMethodInfo> getMethods(String name, String jlssignature) {
        throw new UnsupportedOperationException();
    }

    public Map<String, IClassInfo> getInterfaces() {
        throw new UnsupportedOperationException();
    }

    public void setWorkingFlag(boolean working) {
        workingFlag = working;
    }

    public boolean getWorkingFlag() {
        return workingFlag;
    }

    public boolean isAssignableFrom(IClassInfo cls) {
        return false;
    }

    public int isCastableTo(IClassInfo clsTo) {
        if (clsTo.isPrimitive()) {
            return CAST_CORRECT;
        }
        return CAST_INCORRECT;
    }

    public boolean isSubclassOf(IClassInfo cls) {
        throw new UnsupportedOperationException();
    }
}
