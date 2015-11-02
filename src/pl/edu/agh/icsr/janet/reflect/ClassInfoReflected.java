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

import java.lang.reflect.*;
import java.util.*;
import pl.edu.agh.icsr.janet.*;

public final class ClassInfoReflected implements IClassInfo {
    private Class cls;
    private ClassManager classMgr;
    transient String signature;
    transient Map dclfields;
    transient SortedMap accfields;
    transient SortedMap dclmethods;
    transient SortedMap accmethods;
    transient Map constructors;
    transient IClassInfo superclass;
    transient IClassInfo dclclass;
    transient Map interfaces;
    transient Map assignableClasses;

    private boolean workingFlag = false;

    ClassInfoReflected(Class cls, ClassManager classMgr) {
        this.cls = cls;
        this.classMgr = classMgr;
    }

    public IClassInfo getDeclaringClass() {
        if (dclclass != null) return dclclass;
        Class dcl = cls.getDeclaringClass();
        return dclclass = (dcl == null ? null : classMgr.forClass(dcl));
    }

    public IClassInfo getSuperclass() {
        if (superclass != null) return superclass;
        Class c = cls.getSuperclass();
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

    public static boolean isClassAccessibleToPkg(Class cls, String pkg) {
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

    public Map getDeclaredFields() {
        if (dclfields != null) return dclfields;
        if (isPrimitive()) throw new UnsupportedOperationException();
        dclfields = new HashMap();
        Field[] flds = cls.getDeclaredFields();
        for (int i=0; i<flds.length; i++) {
            FieldInfoReflected fld = new FieldInfoReflected(classMgr, flds[i]);
            dclfields.put(fld.getName(), fld);
        }
        return dclfields;
    }

    public SortedMap getAccessibleFields() throws ParseException {
        if (accfields != null) return accfields;
        return accfields = classMgr.getAccessibleFields(this);
    }

    public SortedMap getFields(String name) throws ParseException {
        return classMgr.getFields(this, name);
    }

    public SortedMap getDeclaredMethods() throws CompileException {
        if (dclmethods != null) return dclmethods;
        if (isPrimitive()) throw new UnsupportedOperationException();
        dclmethods = new TreeMap();
        Method[] mths = cls.getDeclaredMethods();
        for (int i=0; i<mths.length; i++) {
            MethodInfoReflected mth = new MethodInfoReflected(classMgr, mths[i]);
            dclmethods.put(mth.getName() + mth.getJLSSignature(), mth);
        }
        return dclmethods;
    }

    public SortedMap getAccessibleMethods() throws ParseException {
        if (accmethods != null) return accmethods;
        return accmethods = classMgr.getAccessibleMethods(this);
    }

    public SortedMap getMethods(String name) throws ParseException {
        return classMgr.getMethods(this, name);
    }

    public SortedMap getMethods(String name, String jlssignature)
            throws ParseException {
        return classMgr.getMethods(this, name, jlssignature);
    }

    public Map getInterfaces() throws CompileException {
        if (interfaces != null) return interfaces;
        interfaces = new HashMap();
        Class[] intfs = cls.getInterfaces();
        for (int i=0; i<intfs.length; i++) {
            IClassInfo c = classMgr.forClass(intfs[i]);
            interfaces.put(c.getFullName(), c);
        }
        return interfaces;
    }

    public Map getConstructors() throws ParseException {
        if (constructors != null) return constructors;
        constructors = new HashMap();
        Constructor[] cstrs = cls.getDeclaredConstructors();
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