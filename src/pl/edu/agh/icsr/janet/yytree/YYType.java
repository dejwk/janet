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

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.util.*;

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

    private boolean workingFlag;

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

    public YYType(IDetailedLocationContext cxt, Class cls) {
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
        return classMgr.getQualifiedName(
            pkg == null ? "" : pkg.toString(), name) + " (not resolved)";
    }

    /**
     * Determine meaning of a type (JLS 6.5.4)
     * Inner classes NOT yet supported
     */
    private IClassInfo resolveNoDims() throws ParseException {
        IClassInfo result;
        String refpkgname = this.pkg == null ? "" : pkg.toString();
        String curpkgname = compUnit.getPackageName();
        if (refpkgname == "") { // simple name
            String qname;
            Map m = compUnit.getSingleImportDeclarations();
            result = (IClassInfo)m.get(name);
            if (result != null) return result;

            // searching in current package (including current compUnit)
            qname = classMgr.getQualifiedName(curpkgname, name);
            result = classMgr.forName(qname);
            if (result != null) return result;

            // checking type-import-on-demands
            List l = compUnit.getImportOnDemandDeclarations();
            Iterator i = l.iterator();
            IClassInfo other;
            while (i.hasNext()) {
                qname = classMgr.getQualifiedName((String)i.next(), name);
                other = classMgr.forName(qname);
                if (other != null && !other.isAccessibleTo(curpkgname)) {
                    other = null;
                }
                if (other != null) {
                    if (result == null) {
                        result = other;
                    } else {
                        reportError("Ambigious: " + result + " and " + other);
                    }
                }
            }

            if (result == null) {
                reportError(getContextDescription() + " " +
                    classMgr.getQualifiedName(refpkgname, name) + " not found");
            }
            return result;
        } else { // qualified name
            result = classMgr.forName(classMgr.getQualifiedName(refpkgname,
                name));
            if (!result.isAccessibleTo(curpkgname)) {
                reportError("Can't access " + result + ". " +
                    getContextDescription() + " should be " +
                    "public, in the same package, or an accessible member");
                return null;
            }
            return result;
        }
    }

//    private String getPackageName() { return pkg == null ? "" : pkg.toString(); }

    /**
     * Determine meaning of a type (JLS 6.5.4)
     * Inner classes NOT yet supported
     */
/*    public static IClassInfo resolveSimpleTypeName(YYCompilationUnit compUnit,
            String name) throws CompileException {
        String qname;
        IClassInfo result = null;
        YYImportDeclarationList imports = compUnit.getImportDeclarations();
        IClassInfo t = (imports != null) ? imports.findSingle(name) : null;

    }*/
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
                classMgr.getQualifiedName(refpkgname, name) + " not found");
        } else {
            refersTo = dims == 0 ? cls : cls.getArrayType(dims);
        }
        return refersTo;
    }
/*        if (refersTo != null) return refersTo; // already resolved
        lock();
        String qname;
        IClassInfo result = null;
        YYImportDeclarationList imports = compUnit.getImportDeclarations();
        if (!fully_qualified) { // simple name, JLS 6.5.4.1
            IClassInfo t = (imports != null) ? imports.findSingle(name) : null;
            if (t != null && checkAccessible(t)) {
                result = dims == 0 ? t : t.getArrayType(dims);
            } else {
                // search in this package (includes current type declaration)
                qname = getQualifiedName(compUnit.getPackageName(), name);
                result = classMgr.forName(qname);
                if (result == null) { // not found in this package
                    // search in imported packages
                    Iterator i = imports.onDemandsIterator();
                    IClassInfo other;
                    while (i.hasNext()) {
                        qname = getQualifiedName((String)i.next(), name);
                        other = classMgr.forName(qname);
                        if (other != null) {
                            if (result == null) {
                                result = other;
                            } else {
                                reportError("Ambigious: " + result + " and " +
                                    other);
                            }
                        }
                    }
                }
                if (result == null) { // class not found
                    reportError(getContextDescription() + " " + name +
                        " not found");
                } else {
                    if (checkAccessible(result)) {
                        result = dims == 0 ? result : result.getArrayType(dims);
                    }
                }
            }
        } else { // fully qualified, JLS 6.5.4.2
            qname = getQualifiedName(pkg, name);
            result = classMgr.forName(qname);
            if (result == null) {
                reportError(getContextDescription() + " " + qname +
                    " not found");
            } else {
                if (checkAccessible(result)) {
                    result = dims == 0 ? result : result.getArrayType(dims);
                }
            }
        }

        this.refersTo = result;

        // check type (always class or interface; never primitive or array)
        if (this.context == INTERFACE && !result.isInterface()) {
            reportError("" + this + " appears where interface is expected");
        } else if (this.context == CLASS && result.isInterface()) {
            reportError("" + this + " appears where class is expected");
        }
        return this.refersTo;
    }
*/
    public final String getContextDescription() {
        switch(context) {
            case CLASS: return "class";
            case INTERFACE: return "interface";
            case ANY: return "class or interface";
            default: throw new IllegalArgumentException();
        }
    }
/*
    public final boolean checkAccessible(IClassInfo cls)
            throws CompileException {
        if (cls.isAccessibleTo(compUnit.getPackageName())) {
            return true;
        }
        reportError("Can't access " + cls + ". " + getContextDescription() +
            " must be public, in the same package, or an accessible member");
        return false;
    }
*/
/*    private final static String getQualifiedName(YYPackage pkg, String simple) {
        return pkg == null ? simple : pkg.toString() + "." + simple;
    }*/

    /**
     * Enforces determination of meaning.
     */
//    public IClassInfo getDeclaringClass() throws CompileException {
//        return getResolvedType().getDeclaringClass();
//    }
//
    /**
     * Enforces determination of meaning.
     */
/*    public IClassInfo getSuperClass() throws CompileException {
        return getResolvedType().getSuperClass();
    }
*/
    /**
     * Enforces determination of meaning.
     */
/*    public boolean isInterface() throws CompileException {
        return getResolvedType().isInterface();
    }*/

/*    public boolean isArray() {
        if (refersTo != null) return refersTo.isArray();
        return dims > 0; // refers to the non-array class
    }

    public boolean isPrimitive() {
        if (refersTo == null) return false; // primitives are always resolved
        return refersTo.isPrimitive();
    }

    public String getSimpleName() {
        return name;
    }
*/
    /**
     * Enforces determination of meaning.
     */
/*    public String getPackageName() throws CompileException {
        return getResolvedType().getPackageName();
    }
*/
    /**
     * Enforces determination of meaning.
     */
/*    public String getFullName() throws CompileException {
        return getResolvedType().getFullName();
    }*/

    /**
     * Enforces determination of meaning.
     */
/*    public int getModifiers() throws CompileException {
        return getResolvedType().getModifiers();
    }*/

    /**
     * Enforces determination of meaning.
     */
//    public boolean isAccessibleTo(String pkg) throws CompileException {
//        return getResolvedType().isAccessibleTo(pkg);
//    }
/*
    public IClassInfo getArrayType() throws CompileException {
        return getArrayType(1);
    }

    public IClassInfo getArrayType(int dims) throws CompileException {
        return classMgr.getArrayClass(this, dims);
    }
*/
    /**
     * Enforces determination of meaning.
     */
//    public IClassInfo getComponentType() throws CompileException {
//        return getResolvedType().getComponentType();
//    }

    /**
     * Enforces determination of meaning.
     */
//    public Map getDeclaredFields() throws CompileException {
//        return getResolvedType().getDeclaredFields();
//    }
//
    /**
     * Enforces determination of meaning and deep resolving of type.
     */
//    public SortedMap getAccessibleFields() throws CompileException {
//        return getResolvedType().getAccessibleFields();
//    }
//
    /**
     * Enforces determination of meaning and deep resolving of type.
     */
/*    public SortedMap getFields(String name) throws CompileException {
        return classMgr.getFields(this, name);
    }*/

    /**
     * Enforces determination of meaning.
     */
//    public SortedMap getDeclaredMethods() throws CompileException {
//        return getResolvedType().getDeclaredMethods();
//    }
//
    /**
     * Enforces determination of meaning.
     */
//    public SortedMap getAccessibleMethods() throws CompileException {
//        return getResolvedType().getAccessibleMethods();
//    }
//
    /**
     * Enforces determination of meaning.
     */
//    public String getSignature() throws CompileException {
//        return getResolvedType().getSignature();
//    }
//
    /**
     * Enforces determination of meaning.
     */
//    public Map getInterfaces() throws CompileException {
//        return getResolvedType().getInterfaces();
//    }
//
    /**
     * Enforces determination of meaning.
     */
//    public boolean isAssignableFrom(IClassInfo cls) throws CompileException {
//        return getResolvedType().isAssignableFrom(cls);
//    }
//
    /**
     * Enforces determination of meaning.
     */
//    public boolean isSubclassOf(IClassInfo cls) throws CompileException {
//        return getResolvedType().isSubclassOf(cls);
//    }
//
//
//    public boolean equals(IClassInfo cls) throws CompileException {
//        return this.getSignature() == cls.getSignature();
//    }
//
//    public void setWorkingFlag(boolean working) {
//        if (refersTo != null) refersTo.setWorkingFlag(working);
//        workingFlag = working;
//    }
//
//    public boolean getWorkingFlag() {
//        return (refersTo != null) ? refersTo.getWorkingFlag() : workingFlag;
//    }
}
