/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.ParseException;

public final class ClassInfoReflected implements IClassInfo {
    private Class<?> cls;
    private ClassManager classMgr;
    transient String signature;
    transient Map<String, IFieldInfo> dclfields;
    transient SortedMap<String, IFieldInfo> accfields;
    transient SortedMap<String, IMethodInfo> dclmethods;
    transient SortedMap<String, IMethodInfo> accmethods;
    transient Map<String, IMethodInfo> constructors;
    transient IClassInfo superclass;
    transient IClassInfo dclclass;
    transient Map<String, IClassInfo> interfaces;
    transient Map<String, IClassInfo> assignableClasses;

    private boolean workingFlag = false;

    ClassInfoReflected(Class<?> cls, ClassManager classMgr) {
        this.cls = cls;
        this.classMgr = classMgr;
    }

    public IClassInfo getDeclaringClass() {
        if (dclclass != null) return dclclass;
        Class<?> dcl = cls.getDeclaringClass();
        return dclclass = (dcl == null ? null : classMgr.forClass(dcl));
    }

    public IClassInfo getSuperclass() {
        if (superclass != null) return superclass;
        Class<?> c = cls.getSuperclass();
        return (c == null) ? null : (superclass = classMgr.forClass(c));
    }

    public boolean isInterface() {
        return cls.isInterface();
    }

    public boolean isArray() {
        return cls.isArray();
    }

    public boolean isPrimitive() {
        return cls.isPrimitive();
    }

    public boolean isReference() {
        return !cls.isPrimitive(); // null and void never reflected here
    }

    public boolean isAccessibleTo(String pkg) {
        return isClassAccessibleToPkg(cls, pkg);
    }

    public static boolean isClassAccessibleToPkg(Class<?> cls, String pkg) {
        if (cls.isPrimitive() || Modifier.isPublic(cls.getModifiers())) {
            return true;
        }
        if (cls.isArray()) {
            return isClassAccessibleToPkg(cls.getComponentType(), pkg);
        }
        String myname = cls.getName();
        int dot = myname.lastIndexOf('.');
        return pkg.equals(dot == -1 ? "" : myname.substring(0, dot));
    }

    public String getSimpleName() {
        if (isPrimitive()) {
            return cls.getName();
        }
        String s = cls.getName();
        int dotpos = s.indexOf('.');
        int dolpos = s.indexOf('$');

        if (dolpos >= 0) {
            s = s.substring(dolpos+1);
        } else if (dotpos >= 0) {
            s = s.substring(dotpos+1);
        }
        return s;
    }

    public String getPackageName() {
        if (isPrimitive()) {
            throw new UnsupportedOperationException();
        }
        String s = cls.getName();
        int dotpos = s.lastIndexOf('.');
        if (dotpos >= 0) s = s.substring(0, dotpos);
        return s;
    }

    public String getFullName() {
        return cls.getName();
    }

    public String getJNIName() {
        if (isPrimitive()) {
            return getSignature();
        }
        IClassInfo decl = getDeclaringClass();
        if (decl != null) {
            return decl.getJNIName() + "$" + getSimpleName();
        }
        return getFullName().replace('.', '/');
    }

    public String getJNIType() {
        try {
            if (this == classMgr.VOID) return "void";
            if (isPrimitive()) return "j" + cls.getName();
            if (this == classMgr.Class) return "jclass";
            if (this == classMgr.String) return "jstring";
            if (this.isAssignableFrom(classMgr.Throwable)) return "jthrowable";
            return "jobject";
        } catch (ParseException e) {
            throw new IllegalStateException();
        }
    }

    public int getModifiers() {
        return cls.getModifiers();
    }

    public IClassInfo getArrayType() throws CompileException {
        return getArrayType(1);
    }

    public IClassInfo getArrayType(int dims) throws CompileException {
        return classMgr.getArrayClass(this, dims);
    }

    public IClassInfo getComponentType() {
        return classMgr.forClass(cls.getComponentType());
    }

    public String toString() {
        return cls.toString();
    }

    // JNI spec, ch.3, Type Signatures
    public String getSignature() {
        if (signature != null) return signature;
        if (isArray()) {
            signature = "[" + getComponentType().getSignature();
            return signature;
        }
        if (isPrimitive()) {
            if (this == classMgr.VOID)    return "V";
            if (this == classMgr.BOOLEAN) return "Z";
            if (this == classMgr.BYTE)    return "B";
            if (this == classMgr.CHAR)    return "C";
            if (this == classMgr.SHORT)   return "S";
            if (this == classMgr.INT)     return "I";
            if (this == classMgr.LONG)    return "J";
            if (this == classMgr.FLOAT)   return "F";
            if (this == classMgr.DOUBLE)  return "D";
            throw new IllegalArgumentException();
        } else { // class type
            return "L" + getJNIName() + ";";
        }
    }

    public boolean equals(IClassInfo cls) throws CompileException {
        return this.getSignature() == cls.getSignature();
    }

    public Map<String, IFieldInfo> getDeclaredFields() {
        if (dclfields != null) return dclfields;
        if (isPrimitive()) throw new UnsupportedOperationException();
        dclfields = new HashMap<String, IFieldInfo>();
        Field[] flds = cls.getDeclaredFields();
        for (int i=0; i<flds.length; i++) {
            FieldInfoReflected fld = new FieldInfoReflected(classMgr, flds[i]);
            dclfields.put(fld.getName(), fld);
        }
        return dclfields;
    }

    public SortedMap<String, IFieldInfo> getAccessibleFields() throws ParseException {
        if (accfields != null) return accfields;
        return accfields = classMgr.getAccessibleFields(this);
    }

    public SortedMap<String, ? extends IFieldInfo> getFields(String name) throws ParseException {
        return classMgr.getFields(this, name);
    }

    public SortedMap<String, IMethodInfo> getDeclaredMethods() throws CompileException {
        if (dclmethods != null) return dclmethods;
        if (isPrimitive()) throw new UnsupportedOperationException();
        dclmethods = new TreeMap<String, IMethodInfo>();
        Method[] mths = cls.getDeclaredMethods();
        for (int i=0; i<mths.length; i++) {
            MethodInfoReflected mth = new MethodInfoReflected(classMgr, mths[i]);
            dclmethods.put(mth.getName() + mth.getJLSSignature(), mth);
        }
        return dclmethods;
    }

    public SortedMap<String, IMethodInfo> getAccessibleMethods() throws ParseException {
        if (accmethods != null) return accmethods;
        return accmethods = classMgr.getAccessibleMethods(this);
    }

    public SortedMap<String, ? extends IMethodInfo> getMethods(String name) throws ParseException {
        return classMgr.getMethods(this, name);
    }

    public SortedMap<String, ? extends IMethodInfo> getMethods(String name, String jlssignature)
            throws ParseException {
        return classMgr.getMethods(this, name, jlssignature);
    }

    public Map<String, IClassInfo> getInterfaces() throws CompileException {
        if (interfaces != null) return interfaces;
        interfaces = new HashMap<String, IClassInfo>();
        Class<?>[] intfs = cls.getInterfaces();
        for (int i=0; i<intfs.length; i++) {
            IClassInfo c = classMgr.forClass(intfs[i]);
            interfaces.put(c.getFullName(), c);
        }
        return interfaces;
    }

    public Map<String, IMethodInfo> getConstructors() throws ParseException {
        if (constructors != null) return constructors;
        constructors = new HashMap<String, IMethodInfo>();
        Constructor<?>[] cstrs = cls.getDeclaredConstructors();
        for (int i=0; i<cstrs.length; i++) {
            IMethodInfo c = new ConstructorInfoReflected(classMgr, cstrs[i]);
            constructors.put(c.getJLSSignature(), c);
        }
        return constructors;
    }

    public boolean isAmbigious() {
        return false;
    }

    private final int getPrIdx(IClassInfo c) {
        if (c == classMgr.BOOLEAN) return 0;
        if (c == classMgr.BYTE) return 1;
        if (c == classMgr.SHORT) return 2;
        if (c == classMgr.CHAR) return 3;
        if (c == classMgr.INT) return 4;
        if (c == classMgr.LONG) return 5;
        if (c == classMgr.FLOAT) return 6;
        if (c == classMgr.DOUBLE) return 7;
        throw new IllegalArgumentException();
    }

    // sum of widening and identity primitive conversions (JLS 5.1.1-2)
    private final boolean[][] prWidCnvTable = {
    // to bool,  byte,  short, char,  int,   long,  float, double
        { true,  false, false, false, false, false, false, false }, // bool
        { false, true,  true,  false, true,  true,  true,  true  }, // byte
        { false, false, true,  false, true,  true,  true,  true  }, // short
        { false, false, false, true,  true,  true,  true,  true  }, // char
        { false, false, false, false, true,  true,  true,  true  }, // int
        { false, false, false, false, false, true,  true,  true  }, // long
        { false, false, false, false, false, false, true,  true  }, // float
        { false, false, false, false, false, false, false, true  }  // double
    };

//    // sum of narrowing, widening and identity primitive conversions
//    // (JLS 5.1.1-3)
//    private final boolean[][] prNarrCnvTable = {
//    // to bool,  byte,  short, char,  int,   long,  float, double
//        { true,  false, false, false, false, false, false, false }, // bool
//        { false, true,  true,  true,  true,  true,  true,  true  }, // byte
//        { false, true,  true,  true,  true,  true,  true,  true  }, // short
//        { false, true,  true,  true,  true,  true,  true,  true  }, // char
//        { false, true,  true,  true,  true,  true,  true,  true  }, // int
//        { false, true,  true,  true,  true,  true,  true,  true  }, // long
//        { false, true,  true,  true,  true,  true,  true,  true  }, // float
//        { false, true,  true,  true,  true,  true,  true,  true  }  // double
//    };

    public boolean isAssignableFrom(IClassInfo clsFrom) throws ParseException {
        if (this.cls.isPrimitive()) {
            if (!clsFrom.isPrimitive()) return false;
            return prWidCnvTable[getPrIdx(this)][getPrIdx(clsFrom)];
        } else { // reference, nonarray
            if (assignableClasses == null) {
                assignableClasses = classMgr.getAssignableClasses(this);
            }
            return assignableClasses.containsKey(clsFrom.getFullName());
        }
    }

    public int isCastableTo(IClassInfo clsTo) throws ParseException {
        if (isAssignableFrom(clsTo)) {
            return CAST_CORRECT;
        }
        if (this.cls.isPrimitive()) {
            if (!clsTo.isPrimitive()) return CAST_INCORRECT;
            // all numeric types are castable to each other
            // (boolean -> boolean covered by earlier isAssignableFrom())
            return (this != classMgr.BOOLEAN && clsTo != classMgr.BOOLEAN)
                       ? CAST_CORRECT
                       : CAST_INCORRECT;
        }

        if (this == classMgr.Object) {
            return (clsTo.isReference() ? CAST_REQUIRES_RTCHECK
                                        : CAST_INCORRECT);
        }
        return classMgr.isCastableTo(this, clsTo) ? CAST_REQUIRES_RTCHECK
                                                  : CAST_INCORRECT;
    }

    /**
     * The nonarray class is a subclass of another class iff it is not
     * interface, it is assignable of that class and that class is not
     * interface
     * JLS 8.4.4
     */
    public boolean isSubclassOf(IClassInfo cls) throws ParseException {
        return !isInterface() && !cls.isInterface() && isAssignableFrom(cls);
    }



    public void setWorkingFlag(boolean working) {
        workingFlag = working;
    }

    public boolean getWorkingFlag() {
        return workingFlag;
    }

    /*
    private final static int getAccessModifier(int modifiers) {
        if (Modifier.isPublic(modifiers)) return Modifier.PUBLIC;
        if (Modifier.isProtected(modifiers)) return Modifier.PROTECTED;
        if (Modifier.isPrivate(modifiers)) return Modifier.PRIVATE;
        return 0;
    }*/

}