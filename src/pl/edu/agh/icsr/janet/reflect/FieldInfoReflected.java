/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import java.lang.reflect.Field;

public final class FieldInfoReflected implements IFieldInfo {

    private ClassManager classMgr;
    private Field fld;
    transient String signature;

    public FieldInfoReflected(ClassManager mgr, Field fld) {
        this.fld = fld;
        this.classMgr = mgr;
    }

    public IClassInfo getDeclaringClass() {
        return classMgr.forClass(fld.getDeclaringClass());
    }

    public String getName() {
        return fld.getName();
    }

    public int getModifiers() {
        return fld.getModifiers();
    }

    public IClassInfo getType() {
        return classMgr.forClass(fld.getType());
    }

    public String toString() {
        return fld.toString();
    }
/*
    // JNI spec, ch.3, Type Signatures
    public String getSignature() {
        if (signature != null) return signature;
        return signature = getSignature(fld);
    }

    // JNI spec, ch.3, Type Signatures
    public static String getSignature(Field fld) {
        return ClassInfoReflected.getSignature(fld.getType());
    }*/
}