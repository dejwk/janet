/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.IDetailedLocationContext;
import pl.edu.agh.icsr.janet.ILocationContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.reflect.ClassManager;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public class YYType extends YYNode /*implements IClassInfo*/ {

    public static final int ANY       = 0;
    public static final int CLASS     = 1;
    public static final int INTERFACE = 2;

    YYPackage pkg;
    String name;
    int dims;
    int context; // CLASS, INTERFACE or ANY

    YYCompilationUnit compUnit;
    ClassManager classMgr;

    IClassInfo refersTo; // resolved type reference

    public YYType(IDetailedLocationContext cxt, YYPackage pkg, String name) {
        this(cxt, pkg, name, cxt.getClassManager(), cxt.getCompilationUnit());
    }

    public YYType(ILocationContext cxt, YYPackage pkg, String name,
            ClassManager cm, YYCompilationUnit cu) {
        super(cxt);
        this.pkg = pkg;
        this.name = name;
        this.context = ANY;
        this.classMgr = cm;
        this.compUnit = cu;
    }

    public YYType(IDetailedLocationContext cxt, Class<?> cls) {
        super(cxt);
        this.context = ANY;
        this.classMgr = cxt.getClassManager();
        this.compUnit = cxt.getCompilationUnit();
        this.refersTo = classMgr.forClass(cls);
    }

    public YYType addDims(ILocationContext cxt, int dims)
            throws CompileException {
        expand(cxt);
        this.dims += dims;
        if (refersTo != null) {
            refersTo = refersTo.getArrayType(dims);
        }
        return this;
    }

    private YYType setContext(int context) {
        ensureUnlocked();
        this.context = context;
        return this;
    }

    public YYType setInterfaceContext() { return setContext(INTERFACE); }
    public YYType setClassContext() { return setContext(CLASS); }

    public String toString() {
        if (refersTo != null) return refersTo.toString();
        return ClassManager.getQualifiedName(
            pkg == null ? "" : pkg.toString(), name) + " (not resolved)";
    }

    /**
     * Determine meaning of a type (JLS 6.5.4)
     * Inner classes NOT yet supported
     */
    public IClassInfo getResolvedType() throws ParseException {
        if (refersTo != null) return refersTo;
        lock();
        String refpkgname = pkg == null ? "" : pkg.toString();
        IClassInfo cls = null;
        try {
             cls = classMgr.tryResolveAsType(refpkgname, name, compUnit);
        } catch (CompileException e) {
            reportError(e.getMessage());
        }
        if (cls == null) {
            reportError(getContextDescription() + " " +
                ClassManager.getQualifiedName(refpkgname, name) + " not found");
        } else {
            refersTo = dims == 0 ? cls : cls.getArrayType(dims);
        }
        return refersTo;
    }

    public final String getContextDescription() {
        switch(context) {
            case CLASS: return "class";
            case INTERFACE: return "interface";
            case ANY: return "class or interface";
            default: throw new IllegalArgumentException();
        }
    }
}
