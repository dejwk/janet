/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import pl.edu.agh.icsr.janet.*;
import java.lang.reflect.*;
import java.util.*;

class ArrayType implements IClassInfo {
    ClassManager classMgr;
    IClassInfo cls;
    int dims;
    transient String signature;
    transient IClassInfo superclass; // java.lang.Object class
    transient IClassInfo comptype;
    transient Map dclfields;
    transient SortedMap accfields;
    transient SortedMap dclmethods; // clone
    transient SortedMap accmethods;
    transient Map interfaces; // always empty

    private boolean workingFlag;
/*
    public static ArrayType newArrayType(IClassInfo cls, int dims)
            throws CompileException {
        if (dims < 1) {
            throw new IllegalArgumentException();
        }
        ArrayType arr = (ArrayType)arrays.get(cls.getFullName() + dims);
        if (arr == null) {
            arrays.put(cls.getFullName() + dims, arr = new ArrayType(cls, dims));
        }
        return arr;
    }
*/
    ArrayType(ClassManager classMgr, IClassInfo cls, int dims) {
        this.classMgr = classMgr;
        this.cls = cls;
        this.dims = dims;
    }

    public IClassInfo getSuperclass() {
        if (superclass != null) return superclass;
        return superclass = classMgr.forClass(java.lang.Object.class);
    }

    public IClassInfo getArrayType() throws CompileException {
        return getArrayType(1);
    }

    public IClassInfo getArrayType(int dims) throws CompileException {
        return classMgr.getArrayClass(this.cls, this.dims + dims);
    }

    public IClassInfo getComponentType() {
        if (comptype != null) return comptype;
        if (dims == 1) {
            return comptype = cls;
        } else {
            IClassInfo result = null;
            try {
                result = classMgr.getArrayClass(cls, dims-1);
            } catch (CompileException e) { throw new IllegalStateException(); }
            return comptype = result;
        }
    }

    public String toString() {
        String arr = "";
        for (int i=0; i<dims; i++) arr += "[]";
        return cls.toString() + arr;
    }

    public String getSignature() {
        if (signature != null) return signature;
        String s = "";
        for (int i=0; i<dims; i++) s += "[";
        return signature = s + cls.getSignature();
    }

    public boolean isAccessibleTo(String pkg) throws CompileException {
        return cls.isAccessibleTo(pkg);
    }

    public IClassInfo getDeclaringClass() throws CompileException {
        return null;
    }

    public int getModifiers() {
        return Modifier.ABSTRACT + Modifier.FINAL +
            (Modifier.isPublic(cls.getModifiers()) ? Modifier.PUBLIC : 0);
    }

    public String getPackageName() throws CompileException {
        return cls.getPackageName();
    }

    public String getSimpleName() {
        throw new IllegalStateException();
/*        String s = cls.getSimpleName();
        for (int i=0; i<dims; i++) s += "[]";
        return s;*/
    }

    public String getFullName() {
        String s = cls.getFullName();
        for (int i=0; i<dims; i++) s += "[]";
        return s;
    }

    public String getJNIName() {
        return getSignature();
    }

    public String getJNIType() {
        IClassInfo compType = getComponentType();
        if (compType.isPrimitive()) {
            return "j" + compType.getFullName() + "Array";
        } else {
            return "jobjectArray";
        }
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isReference() {
        return true;
    }

    public boolean isArray() {
        return true;
    }

    public boolean isInterface() {
        return false;
    }

/*    public boolean equals(IClassInfo cls) throws CompileException {
        return this.getSignature() == cls.getSignature();
    }
*/
    public Map getDeclaredFields() { // final field length is not reflected
        if (dclfields != null) return dclfields;
        dclfields = new HashMap();
        dclfields.put("length", new ArrayLength());
        return dclfields;
    }

    public SortedMap getAccessibleFields() throws ParseException {
        if (accfields != null) return accfields;
        return accfields = classMgr.getAccessibleFields(this);
    }

    public SortedMap getFields(String name) throws ParseException {
        return classMgr.getFields(this, name);
    }

    public Map getConstructors() {
        throw new UnsupportedOperationException();
    }

    public SortedMap getDeclaredMethods() {
        throw new UnsupportedOperationException();
//        if (dclmethods != null) return dclmethods;
//        return dclmethods = new TreeMap();
    }

    public SortedMap getAccessibleMethods() throws ParseException {
        if (accmethods != null) return accmethods;
        // the same as in java.lang.Object
        return accmethods = getSuperclass().getAccessibleMethods();
    }

    public SortedMap getMethods(String name) throws ParseException {
        return classMgr.getMethods(this, name);
    }

    public SortedMap getMethods(String name, String jlssignature)
            throws ParseException {
        return classMgr.getMethods(this, name, jlssignature);
    }

    public Map getInterfaces() {
        if (interfaces != null) return interfaces;
        return interfaces = new HashMap();
    }

    public void setWorkingFlag(boolean working) {
        workingFlag = working;
    }

    public boolean getWorkingFlag() {
        return workingFlag;
    }

    public boolean isAssignableFrom(IClassInfo clsFrom) throws ParseException {
        // JLS 5.1.4
        if (clsFrom == classMgr.Object ||
                clsFrom == classMgr.Cloneable ||
                clsFrom == classMgr.Serializable) return true;
        if (!clsFrom.isArray()) return false;
        IClassInfo c1 = this.getComponentType();
        IClassInfo c2 = clsFrom.getComponentType();
        return classMgr.equals(c1, c2) ||
            (!c1.isPrimitive() && !c2.isPrimitive() && c1.isAssignableFrom(c2));
    }

    public int isCastableTo(IClassInfo clsTo) throws ParseException {
        if (isAssignableFrom(clsTo)) {
            return CAST_CORRECT;
        }
        if (!clsTo.isArray()) return CAST_INCORRECT;
        return this.getComponentType().isCastableTo(clsTo.getComponentType());
    }


    public boolean isSubclassOf(IClassInfo cls) {
        return cls == classMgr.Object;
    }


    public class ArrayLength implements IFieldInfo {

        private ArrayLength() {}

        public String getName() {
            return "length";
        }

        public IClassInfo getDeclaringClass() {
            return ArrayType.this;
        }

        public int getModifiers() {
            return Modifier.PUBLIC | Modifier.FINAL;
        }

        public IClassInfo getType() {
            return classMgr.INT;
        }

        public String toString() {
            return "public final int length";
        }

    }
}
