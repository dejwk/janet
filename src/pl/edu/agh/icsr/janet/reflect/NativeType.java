/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Java Language Extensions (JANET) package,
 * http://www.icsr.agh.edu.pl/janet.
 *
 * The Initial Developer of the Original Code is Dawid Kurzyniec.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Dawid Kurzyniec <dawidk@icsr.agh.edu.pl>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

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

    public Map getDeclaredFields() { // final field length is not reflected
        throw new UnsupportedOperationException();
    }

    public SortedMap getAccessibleFields() throws CompileException {
        throw new UnsupportedOperationException();
    }

    public SortedMap getFields(String name) throws CompileException {
        throw new UnsupportedOperationException();
    }

    public SortedMap getDeclaredMethods() {
        throw new UnsupportedOperationException();
    }

    public Map getConstructors() {
        throw new UnsupportedOperationException();
    }

    public SortedMap getAccessibleMethods() {
        throw new UnsupportedOperationException();
    }

    public SortedMap getMethods(String name) {
        throw new UnsupportedOperationException();
    }

    public SortedMap getMethods(String name, String jlssignature) {
        throw new UnsupportedOperationException();
    }

    public Map getInterfaces() {
        throw new UnsupportedOperationException();
    }

    public void setWorkingFlag(boolean working) {
        workingFlag = working;
    }

    public boolean getWorkingFlag() {
        return workingFlag;
    }

    // null may be casted to any _primitive_ type
    public boolean isAssignableFrom(IClassInfo cls) {
        return cls.isPrimitive();
    }

    public int isCastableTo(IClassInfo clsTo) {
        if (isAssignableFrom(clsTo)) {
            return CAST_CORRECT;
        }
        return CAST_INCORRECT;
    }

    public boolean isSubclassOf(IClassInfo cls) {
        throw new UnsupportedOperationException();
    }
}
