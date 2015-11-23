/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.reflect;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import pl.edu.agh.icsr.janet.AmbigiousReferenceException;
import pl.edu.agh.icsr.janet.CompilationManager;
import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.ILocationContext;
import pl.edu.agh.icsr.janet.Janet;
import pl.edu.agh.icsr.janet.MethodNotAccessibleException;
import pl.edu.agh.icsr.janet.NoAccessibleMethodsFoundException;
import pl.edu.agh.icsr.janet.NoApplicableMethodsFoundException;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.yytree.YYClass;
import pl.edu.agh.icsr.janet.yytree.YYCompilationUnit;
import pl.edu.agh.icsr.janet.yytree.YYModifierList;

public class ClassManager {

    Map<String, YYClass> compClasses = new HashMap<String, YYClass>(256);
    Map<String, IClassInfo> reflClasses = new HashMap<String, IClassInfo>(256);
    Map<String, IClassInfo> arrayClasses = new HashMap<String, IClassInfo>(256);

    private boolean locked = false;
    CompilationManager compMgr;
    Janet.Settings settings;
    public Janet.Settings getSettings() { return settings; }

    /**
     * @param compMgr compilation manager (to resolve classes from sourcepath)
     * @param settings
     */
    public ClassManager(CompilationManager compMgr, Janet.Settings settings) {
        this.compMgr = compMgr;
        this.settings = settings;
    }

    public IClassInfo forClass(Class<?> cls) {
        if (cls.isArray()) {
            try {
                return getArrayClass(forClass(cls.getComponentType()), 1);
            } catch (CompileException e) {
                throw new RuntimeException();
            }
        }
        IClassInfo c;
        String name = cls.getName();
        c = compClasses.get(name);
        if (c == null) {
            c = reflClasses.get(name);
        }
        if (c == null) {
            reflClasses.put(name, c = new ClassInfoReflected(cls, this));
        }
        return c;
    }

    public IClassInfo forName(String qname) throws ParseException {
        IClassInfo c;

        // check if already defined in parsed files
        c = compClasses.get(qname);
        if (c != null) return c;

        // check if already defined by reflection
        c = reflClasses.get(qname);
        if (c != null) return c;

        try {
            // look at the classpath
            Class<?> cls = settings.getClassLoader().loadClass(qname);
            c = new ClassInfoReflected(cls, this);
            reflClasses.put(qname, c);
            return c;
        } catch (ClassNotFoundException e) {}

        // look at the sourcepath (no inner classes supported)
        String jfile = qname.replace('.', '/') + ".java";
        URL url = settings.getSourceLoader().getResource(jfile);

        try {
            if (url != null) {
                // parse the file and add the compilation unit to the compMgr
                compMgr.parse(url, null, false);

                // if the class was defined in that source file, it should now
                // be visible through compClasses
                c = compClasses.get(qname);
                if (c != null) return c;
            }
        }
        catch (IOException e) {
            throw new ParseException(e.getMessage());
        }

        // give up: not found
        return null;
    }

    public IClassInfo getArrayClass(IClassInfo cls, int dims)
            throws CompileException {
        if (cls.isArray()) {
            return getArrayClass(cls.getComponentType(), dims+1);
        }
        IClassInfo c;
        String name = cls.getFullName() + "_" + dims;
        c = arrayClasses.get(name);
        if (c == null) {
            arrayClasses.put(name, c = new ArrayType(this, cls, dims));
        }
        return c;
    }

    public void lock() {
        locked = true;
    }

    public boolean addClass(YYClass cls) throws CompileException {
        if (locked) {
            if (cls.ibuf().getOriginFile() != null) {
                throw new IllegalStateException("Can't add new class");
            }
        }
        String clname = cls.getFullName();
        IClassInfo c_ref = reflClasses.get(clname);
        if (c_ref != null) { // class can't be added after it is reflected
            throw new IllegalStateException();
        }
        YYClass cls_prv = compClasses.get(clname);
        if (cls_prv != null) {
            cls.reportError((cls.isInterface() ? "Interface " : "Class ") +
                clname + " already defined in " +
                cls_prv.ibuf().getOriginAsString());
            return false;
        } else {
            compClasses.put(clname, cls);
            cls.setLoadLibrary(compMgr.getLoadLibrary());
            cls.setLibName(compMgr.getCurrentLibName());
            return true;
        }
    }
/*
    private boolean isAccessibleTo(IClassInfo cls, String pkg_name)
            throws CompileException { // JLS 6.6
        // either public or in the same package
        if (Modifier.isPublic(cls.getModifiers())) return true;
        return cls.getPackageName().equals(pkg_name);
    }
*/
    /**
     * Checks if given method or field is accessible in declaration of class
     * cls (JLS 6.6). Passing inheritance context as true means that the
     * member declaring class is known to be a superclass of cls, and the
     * access to the protected member would be permitted in terms of JLS 6.6.2.
     */
/*    private boolean isAccessibleTo(IMemberInfo m, IClassInfo cls,
            boolean inheritanceContext) {
        IClassInfo mcls = m.getDeclaringClass();
        if (mcls == cls) {
            return true;
        } else {
            switch (m.getModifiers() & YYModifierList.ACCESS_MODIFIERS) {
            case Modifier.PUBLIC:
                // class must be accessible (public or in the same package)
                // inheritance context guarantees accessibility:
                // every class inherits public members from its direct
                // superclass, so it is finally inherited by cls
                return inheritanceContext ||
                    Modifier.isPublic(mcls.getModifiers()) ||
                    cls.getPackageName().equals(mcls.getPackageName());
            case Modifier.PROTECTED:
                if (inheritanceContext) { // as above, see JLS 8.1.3
                    return true;
                } // else continue body
            case 0: // (or continued PROTECTED) - must be in the same package
                return cls.getPackageName().equals(mcls.getPackageName());
            case Modifier.PRIVATE:
                return false;
            default:
                throw new IllegalArgumentException();
            }
        }
    }
*/
/*
    public boolean isAccessibleTo(IClassInfo cls, IClassInfo clsTo)
            throws CompileException { // JLS 6.6
        return isAccessibleTo(cls, clsTo.getPackageName());
    }
*/
    public boolean isAccessibleTo(IMemberInfo m, IClassInfo clsTo,
        boolean selfcxt) throws ParseException
    {
        IClassInfo declCls = m.getDeclaringClass();
        String pkgTo = clsTo.getPackageName();

        switch (m.getModifiers() & YYModifierList.ACCESS_MODIFIERS) {

        case Modifier.PUBLIC:
            return true;

        case Modifier.PROTECTED: // JLS 6.6.2
            // 'this.member', 'super.member' or 'member' -> ok
            if (selfcxt) return true;
            // the same package -> ok
            if (pkgTo.equals(declCls.getPackageName())) return true;
            // not superclass -> ok
            if (declCls.isSubclassOf(clsTo)) return true;
            // else not accessible
            return false;

        case 0:
            return pkgTo.equals(declCls.getPackageName());

        case Modifier.PRIVATE:
            return clsTo.equals(declCls);

        default:
            throw new RuntimeException();
        }
    }

    public SortedMap<String, IFieldInfo> getAccessibleFields(IClassInfo cls) throws ParseException {
        SortedMap<String, IFieldInfo> result = new TreeMap<String, IFieldInfo>();
        try {
            if (cls.getWorkingFlag()) { // circularity
                reportError(cls, "Circularity detected: " + cls +
                    " inherits from itself");
            } else {
                cls.setWorkingFlag(true);
                for (IClassInfo iface : cls.getInterfaces().values()) {
                    addFieldsOfClass(result, cls, iface);
                }
                if (!cls.isInterface()) {
                    IClassInfo superclass = cls.getSuperclass();
                    if (superclass != null) { // java.lang.Object
                        addFieldsOfClass(result, cls, superclass);
                    }
                }
                addFieldsOfClass(result, cls, cls);
            }
        } finally {
            cls.setWorkingFlag(false);
        }
        return result;
    }

    public SortedMap<String, ? extends IFieldInfo> getFields(IClassInfo cls, String name)
        throws ParseException
    {
        return cls.getAccessibleFields().subMap(getFieldLookupKeyFirst(name),
                                                getFieldLookupKeyLast(name));
    }

    public static String getFieldKey(IFieldInfo fld) throws CompileException {
        return fld.getName() + " in " + fld.getDeclaringClass().getJNIName();
    }

    private String getFieldLookupKeyFirst(String name) {
        return name;
    }

    private String getFieldLookupKeyLast(String name) {
        return name + ".";
    }

    private SortedMap<String, IFieldInfo> getFields(
            SortedMap<String, IFieldInfo> allfields, String name) {
        return allfields.subMap(getFieldLookupKeyFirst(name),
                                getFieldLookupKeyLast(name));
    }

    private final SortedMap<String, IFieldInfo> addFieldsOfClass(
            SortedMap<String, IFieldInfo> result,
        IClassInfo maincls, IClassInfo addcls) throws ParseException
    {
        Iterator<? extends IFieldInfo> newfields, oldfields;

        newfields = (maincls == addcls)
            ? maincls.getDeclaredFields().values().iterator()
            : addcls.getAccessibleFields().values().iterator();
enumnew:
        while (newfields.hasNext()) { // JLS 8.3.3
            IFieldInfo newfld = newfields.next();
            if (!shouldMemberBeIncluded(maincls, addcls, newfld)) {
                continue enumnew; // field is not inherited
            }
            oldfields = getFields(result, newfld.getName()).values().iterator();
            while (oldfields.hasNext()) {
                IFieldInfo oldfld = oldfields.next();
                // maybe it's re-inherited interface field? (JLS 8.3.3.4)
                if (newfld == oldfld) {
                    continue enumnew; // ignore it
                }
                if (maincls == addcls) {
                    oldfields.remove(); // will be hidden
                }
            }
            result.put(getFieldKey(newfld), newfld);
        }
        return result;
    }

    // this is for nonarray reference types, JLS 5.1.4
    // rules for arrays are defined in ArrayType.java
    public Map<String, IClassInfo> getAssignableClasses(IClassInfo cls) throws ParseException {
        HashMap<String, IClassInfo> result = new HashMap<String, IClassInfo>();
        addAssignableClasses(cls, result);
        // any class or _interface_ is assignable from Object
        result.put("java.lang.Object", this.Object);

        return result;
    }

    private void addAssignableClasses(IClassInfo cls, HashMap<String, IClassInfo> result)
        throws ParseException
    {
        try {
            if (cls.getWorkingFlag()) { // circularity
                reportError(cls, "Circularity detected: " + cls +
                    " inherits from itself");
            } else {
                cls.setWorkingFlag(true);
                result.put(cls.getFullName(), cls);
                if (!cls.isInterface()) {
                    IClassInfo superclass = cls.getSuperclass();
                    if (superclass != null) {
                        addAssignableClasses(superclass, result);
                    }
                }
                for (IClassInfo iface : cls.getInterfaces().values()) {
                    addAssignableClasses(iface, result);
                }
            }
        } finally {
            cls.setWorkingFlag(false);
        }
    }

    // this is for nonarray and non-java.lang.Object reference types, JLS 5.5
    // provided that s is NOT assignable from t
    // rules for java.lang.Object are in ClassInfoReflected.java
    // rules for arrays are in ArrayType.java
    public boolean isCastableTo(IClassInfo s, IClassInfo t)
        throws ParseException
    {
        if (!t.isReference()) return false;
        if (!s.isInterface()) {
            if (!t.isInterface()) { // class -> class
                return t.isSubclassOf(s);
            } else {                // class -> interface
                return !Modifier.isFinal(s.getModifiers());
            }
        } else {
            if (!t.isInterface()) { // interface -> class
                if (!Modifier.isFinal(t.getModifiers())) {
                    return true;
                } else { // final -> must implement interface
                    return t.isAssignableFrom(s);
                }
            } else {                // interface -> interface
                if (t.isAssignableFrom(s)) return true;
                // must check for methods with the same signature and different
                // return types
                for (IMethodInfo m1 : s.getAccessibleMethods().values()) {
                    for (IMethodInfo m2 : t.getMethods(m1.getName(),
                        m1.getJLSSignature()).values()) {
                        if (m1.getReturnType() != m2.getReturnType()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
    }

    public boolean isNumericOrNativeType(IClassInfo cls) {
        try {
            return cls == NATIVETYPE || cls.isAssignableFrom(DOUBLE);
        } catch (ParseException e) { throw new RuntimeException(); }
    }

    public boolean isIntegralOrNativeType(IClassInfo cls) {
        try {
            return cls == NATIVETYPE || cls.isAssignableFrom(LONG);
        } catch (ParseException e) { throw new RuntimeException(); }
    }

    public SortedMap<String, IMethodInfo> getAccessibleMethods(IClassInfo cls)
            throws ParseException {
        SortedMap<String, IMethodInfo> result = new TreeMap<String, IMethodInfo>();
        try {
            if (cls.getWorkingFlag()) { // circularity
                reportError(cls, "Circularity detected: " + cls +
                    " inherits from itself");
            } else {
                cls.setWorkingFlag(true);
                // interface (abstract) methods first
                for (IClassInfo iface : cls.getInterfaces().values()) {
                    addMethodsOfClass(result, cls, iface);
                }
                if (!cls.isInterface()) {
                    IClassInfo superclass = cls.getSuperclass();
                    if (superclass != null) { // java.lang.Object
                        addMethodsOfClass(result, cls, superclass);
                    }
                }
                addMethodsOfClass(result, cls, cls);

                // check whether the class is not abstract, when declared
                // not to be
                if (!Modifier.isAbstract(cls.getModifiers())) {
                    for (IMethodInfo m : result.values()) {
                        if (Modifier.isAbstract(m.getModifiers())) {
                            reportError(cls, "" + cls + " should be declared " +
                                "abstract. It does not define " +
                                m + " from " + m.getDeclaringClass());
                        }
                    }
                }
            }
        } finally {
            cls.setWorkingFlag(false);
        }
        return result;
    }

    public SortedMap<String, ? extends IMethodInfo> getMethods(IClassInfo cls, String name)
            throws ParseException {
        return getMethods(cls.getAccessibleMethods(), name);
    }

    public SortedMap<String, ? extends IMethodInfo> getMethods(IClassInfo cls, String name,
        String jlssignature) throws ParseException
    {
        return getMethods(cls.getAccessibleMethods(), name, jlssignature);
    }

    public static String getMethodKey(IMethodInfo mth) throws ParseException {
        return mth.getName() + " " + mth.getJLSSignature() +
           " in " + mth.getDeclaringClass().getJNIName();
    }

    private String getMethodLookupKeyFirst(String name) {
        return name;
    }

    private String getMethodLookupKeyLast(String name) {
        return name + ".";
    }

    private String getMethodLookupKeyFirst(String name, String jlssignature) {
        return name + " " + jlssignature;
    }

    private String getMethodLookupKeyLast(String name, String jlssignature) {
        return name + " " + jlssignature + ".";
    }

    private SortedMap<String, ? extends IMethodInfo> getMethods(
            SortedMap<String, ? extends IMethodInfo> allmethods, String name) {
        return allmethods.subMap(getMethodLookupKeyFirst(name),
                                 getMethodLookupKeyLast(name));
    }

    private SortedMap<String, ? extends IMethodInfo> getMethods(
            SortedMap<String, ? extends IMethodInfo> allmethods, String name, String jlssignature) {
        return allmethods.subMap(getMethodLookupKeyFirst(name, jlssignature),
                                 getMethodLookupKeyLast(name, jlssignature));
    }

    /**
     * Determines if given member of class dclcls is inherited by its subclass
     * maincls. If maincls == dclcls, returns true. See JLS 8.2.
     */
    private boolean shouldMemberBeIncluded(IClassInfo maincls,
            IClassInfo dclcls, IMemberInfo m) throws CompileException {
        int accm = m.getModifiers() & YYModifierList.ACCESS_MODIFIERS;
        if (maincls == dclcls) return true;
        if (Modifier.isPrivate(accm)) return false;
        if (accm == 0) { // default access -> must be the same package
            return maincls.getPackageName().equals(dclcls.getPackageName());
        }
        return true;
    }

    private String inhErrorText(IMethodInfo oldmth,
                                IMethodInfo newmth,
                                boolean override) {
        return "The " + newmth +
            (override ? " declared in " : " inherited from ") +
            newmth.getDeclaringClass() +
            (override ?  " cannot override " : " is incompatible with ") +
            "the method of the same signature" +
            (override ? " declared in " : " inherited from ") +
            oldmth.getDeclaringClass() + ". ";
    }

    private String contextErrorText(IMethodInfo oldmth, IMethodInfo newmth)
            throws CompileException {
        boolean newstatic = Modifier.isStatic(newmth.getModifiers());
        return "The " +
            (newstatic ? "static " : "instance ") + newmth +
            " declared in " + newmth.getDeclaringClass() +
            " cannot " + (newstatic ? "hide " : "override ") +
            (newstatic ? "instance " : "static ") + oldmth +
            " declared in " + oldmth.getDeclaringClass() +
            ". It is illegal to " + (newstatic ? "hide " : "override ") +
            (newstatic ? "an instance " : "a static ") + "method";
    }

    private SortedMap<String, IMethodInfo> addMethodsOfClass(
            SortedMap<String, IMethodInfo> result, IClassInfo maincls,
            IClassInfo addcls) throws ParseException {
        Iterator<? extends IMethodInfo> newmethods, oldmethods;
        newmethods = (maincls == addcls)
            ? maincls.getDeclaredMethods().values().iterator()
            : addcls.getAccessibleMethods().values().iterator();
enumnew:
        while (newmethods.hasNext()) { // JLS 8.4.6
            IMethodInfo newmth = newmethods.next();
            if (!shouldMemberBeIncluded(maincls, addcls, newmth)) {
                continue enumnew; // member is not inherited
            }
            String key = getMethodKey(newmth);
            oldmethods = getMethods(result, newmth.getName(),
                newmth.getJLSSignature()).values().iterator();
            while (oldmethods.hasNext()) {
                IMethodInfo oldmth = oldmethods.next();
                boolean multiInherit = maincls != addcls &&
                            Modifier.isAbstract(newmth.getModifiers());
                Object errtarget = (maincls == addcls) ? (Object)newmth
                                                       : (Object)maincls;

                // check whether it is re-inherited interface method
                if (newmth == oldmth) { // ignore it
                    continue enumnew;
                }
                // check result type
                if (!returnTypeSubstitutable(oldmth, newmth)) {
                    reportError(errtarget,
                        inhErrorText(oldmth, newmth, !multiInherit) +
                        "They must have the same return type (was " +
                        oldmth.getReturnType() + ")");
                    continue enumnew;
                }
                // check if have the same (instance or static) context
                if (!contextCompatible(oldmth, newmth)) {
                    reportError(errtarget, contextErrorText(oldmth, newmth));
                    continue enumnew;
                }
                // check throws clause and access modifier
                // when overriding, hiding or implementing (JLS 8.4.6.3-4)
                // (but not if it is multiply inherited abstract method)
                if (!multiInherit) {
                    // check throws clause
                    String sexcs = getIncompatibleExceptions(oldmth, newmth);
                    if (sexcs != null) {
                        reportError(errtarget,
                            inhErrorText(oldmth, newmth, true) +
                            "The overridden method does not throw " + sexcs);
                        continue enumnew;
                    }
                    // check access modifier
                    String smod = accessModifiersAsShouldBe(oldmth, newmth);
                    if (smod != null) {
                        reportError(errtarget,
                            inhErrorText(oldmth, newmth, true) +
                            "The access modifier is made more restrictive" +
                            " (should be " + smod + ")");
                        continue enumnew;
                    }
                }

                // we are here -> there is no conflict
                if (!multiInherit) {
                    oldmethods.remove();
                }
            } // oldmethods.hasNext()

            // no conflicts -> add the method
            result.put(key, newmth);
        } // newmethods.hasNext()

        return result;
    }

    public boolean isUncheckedException(IClassInfo exc) throws ParseException {
        return exc.isSubclassOf(this.RuntimeException) ||
            exc.isSubclassOf(this.Error);
    }

    /* JLS 8.4.5 */
    private boolean returnTypeSubstitutable(IMethodInfo oldmth,
        IMethodInfo newmth) throws ParseException
    {
        IClassInfo oldType = oldmth.getReturnType();
        IClassInfo newType = newmth.getReturnType();
        if (oldType.isPrimitive()) {
            return equals(oldType, newType);
        } else {
            // Relaxed check - deferring to the Java compiler to enforce type substitutability.
            return true; //newType.isAssignableFrom(oldType);
        }
    }

    private boolean contextCompatible(IMethodInfo oldmth,
            IMethodInfo newmth) throws CompileException {
        if (Modifier.isStatic(newmth.getModifiers()) ^
                Modifier.isStatic(oldmth.getModifiers())) {
            return false;
        }
        return true;
    }

    private String getIncompatibleExceptions(IMethodInfo oldmth,
            IMethodInfo newmth) throws ParseException { // JLS 8.4.4
        for (IClassInfo exc : newmth.getExceptionTypes().values()) {
            if (isUncheckedException(exc)) continue;
            if (!canThrow(oldmth, exc)) {
                return exc.getFullName(); // TODO: whole list, not just first
            }
        }
        return null;
    }

    private String accessModifiersAsShouldBe(IMethodInfo oldmth,
            IMethodInfo newmth) throws CompileException {
        int newmods = newmth.getModifiers();
        switch (oldmth.getModifiers() & YYModifierList.ACCESS_MODIFIERS) {
        case Modifier.PUBLIC:
            if (!Modifier.isPublic(newmods)) {
                return "public";
            }
            break;
        case Modifier.PROTECTED:
            switch (newmods & YYModifierList.ACCESS_MODIFIERS) {
            case 0:
            case Modifier.PRIVATE:
                return "public or protected";
            }
            break;
        case 0:
            if (Modifier.isPrivate(newmods)) {
                return "public, protected or default";
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
        return null;
    }

    /**
     * Does coll1 contain all exceptions of coll2?
     */
/*    public final static boolean containsExceptions(Collection coll1,
            Collection coll2) throws CompileException {
        for (Iterator i = coll2.iterator(); i.hasNext();) {
            if (!containsException(coll1, (IClassInfo)i.next())) {
                return false;
            }
        }
        return true;
    }*/

    /**
     * Does excs contain e (or its superclass)?
     */
    public final static boolean containsException(Collection<IClassInfo> excs, IClassInfo e)
        throws ParseException
    {
//        if (excs == null) return false;
        for (IClassInfo exc : excs) {
            if (e.isSubclassOf(exc)) {
                return true;
            }
        }
        return false;
    }

    public final static boolean canThrow(IMethodInfo mth, IClassInfo e)
            throws ParseException {
        return containsException(mth.getExceptionTypes().values(), e);
    }

//    HashMap get

/*        if (this.equals(cls)) return true;
        if (classMgr == null) {
            throw new IllegalStateException();
        }
        if (isInterface()) {
            if (cls == classMgr.Object) return true;
            if (!cls.isInterface()) return false;
            Iterator i = getInterfaces().values().iterator();
            while (i.hasNext()) {
                if (((IClassInfo)i.next()).isAssignableFrom(cls)) return true;
            }
            return false;
        } else { // must be class
            if (cls.isInterface()) { // this must then implement cls
                Iterator i = getInterfaces().values().iterator();
                while (i.hasNext()) {
                    if (((IClassInfo)i.next()).isAssignableFrom(cls)) {
                        return true;
                    }
                }
                return false;
            } else { // this must be subclass of cls
                IClassInfo s = getSuperClass();
                if (s != null) {
                    return s.isAssignableFrom(cls);
                }
            }
            return false;
        }
*/


   //     YYImportDeclarationList imports = compUnit.getSingles();
   //     IClassInfo t = (imports != null) ? imports.findSingle(name) : null;
//        return null;

    //}

    public IClassInfo tryResolveAsType(String refpkgname, String clsname,
            YYCompilationUnit compUnit) throws ParseException {
        IClassInfo result;
        String curpkgname = compUnit.getPackageName();
        if (refpkgname == "") { // simple name
            String qname;
            Map<String, IClassInfo> m = compUnit.getSingleImportDeclarations();
            result = m.get(clsname);
            if (result != null) return result;

            // searching in current package (including current compUnit)
            qname = getQualifiedName(curpkgname, clsname);
            result = forName(qname);
            if (result != null) return result;

            // checking type-import-on-demands
            IClassInfo other;
            for (String decl : compUnit.getImportOnDemandDeclarations()) {
                qname = getQualifiedName(decl, clsname);
                other = forName(qname);
                if (other != null && !other.isAccessibleTo(curpkgname)) {
                    other = null;
                }
                if (other != null) {
                    if (result == null) {
                        result = other;
                    } else {
                        throw new CompileException("Ambigious: " + result +
                            " and " + other);
                    }
                }
            }

            // not found -> return null
            return result;
        } else { // qualified name
            result = forName(getQualifiedName(refpkgname, clsname));
            if (result != null && !result.isAccessibleTo(curpkgname)) {
                throw new CompileException("Can't access " + result + ". " +
                    "It should be " +
                    "public, in the same package, or an accessible member");
                //return null;
            }
            return result; // possibly null
        }
    }

    private static boolean isMoreSpecific(IMethodInfo m1, IMethodInfo m2)
        throws ParseException
    {
        if (!m1.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass())) {
            return false;
        }
        IClassInfo[] list1 = m1.getParameterTypes();
        IClassInfo[] list2 = m2.getParameterTypes();
        if (list1.length != list2.length) throw new IllegalArgumentException();
        for (int i=0; i<list1.length; i++) {
            if (!list1[i].isAssignableFrom(list2[i])) return false;
        }
        return true;
    }

    private IMethodInfo findTheMostSpecificInternal(Collection<? extends IMethodInfo> whereToSearch,
            IClassInfo[] argtypes, IClassInfo enclClass, boolean selfcxt,
            boolean allowWeak) throws ParseException {
        Iterator<? extends IMethodInfo> methods = whereToSearch.iterator();
        IMethodInfo applicable = null;
        int noOfApplicables = 0;
        List<IMethodInfo> maximallySpecifics = new LinkedList<IMethodInfo>();
methods:
        while (methods.hasNext()) {
            IMethodInfo mth = methods.next();
            // JLS 15.11.2.1

            // is the method or constructor applicable?
            IClassInfo[] params = mth.getParameterTypes();
            if (params.length != argtypes.length) continue methods;
            boolean hasNativeTypes = false;
            for (int i=0; i<argtypes.length; i++) {
                if (argtypes[i] == NATIVETYPE) {
                    hasNativeTypes = true;
                }
                if (allowWeak && argtypes[i] == NATIVETYPE) {
                    if (!params[i].isPrimitive()) continue methods;
                } else {
                  if (!argtypes[i].isAssignableFrom(params[i])) continue methods;
                }
            }
            // the method or constructor is applicable indeed
            applicable = mth;
            noOfApplicables++;

            // is the method or constructor accessible?
            if (!isAccessibleTo(mth, enclClass, selfcxt)) {
                if (this.settings.strictAccess()) continue methods;
            }

            // update list of maximally specifics
            boolean shouldBeAdded = true;
            if (!hasNativeTypes) {
                for (Iterator<IMethodInfo> j = maximallySpecifics.iterator(); j.hasNext();) {
                    IMethodInfo oldmth = j.next();
                    if (isMoreSpecific(oldmth, mth)) {
                        shouldBeAdded = false;
                        break;
                    } else if (isMoreSpecific(mth, oldmth)) {
                        j.remove();
                    }
                }
            }
            if (shouldBeAdded) {
                maximallySpecifics.add(mth);
            }
        }
        methods = maximallySpecifics.iterator();
        if (!methods.hasNext()) {
            if (noOfApplicables == 0) {
                if (allowWeak) {
                    // Not even weakly applicable methods found.
                    throw new NoApplicableMethodsFoundException();
                } else {
                    return null;
                }
            } else if (noOfApplicables == 1) {
                throw new MethodNotAccessibleException(applicable);
            } else {
                throw new NoAccessibleMethodsFoundException();
            }
        }

        IMethodInfo mth = methods.next();

        if (methods.hasNext()) { // ambigious
            throw new AmbigiousReferenceException(mth, methods.next());
        }

        return mth;
    }

    public IMethodInfo findTheMostSpecific(Collection<? extends IMethodInfo> whereToSearch,
        boolean isConstructor, IClassInfo[] argtypes, IClassInfo enclClass,
        boolean selfcxt) throws ParseException
    {
        // First try to find a non-weak match.
        IMethodInfo match = findTheMostSpecificInternal(whereToSearch, argtypes,
            enclClass, selfcxt, false);
        if (match == null) {
            // Fall back to considering weak matches (allowing implicit casts of native
            // expressions to Java primitive types)
            match = findTheMostSpecificInternal(whereToSearch, argtypes,
                                                enclClass, selfcxt, true);
        }
        return match;
    }

    public final static String getQualifiedName(String pkg, String simple) {
        return (pkg == null || pkg == "") ? simple : pkg + "." + simple;
    }

    public String toString() {
        String s = "";
        Iterator<YYClass> i;
        for (i = compClasses.values().iterator(); i.hasNext(); ) {
            YYClass c = i.next();
            try {
                s += c.getFullName() + ":\n" + c.describe() + "\n";
            } catch (ParseException e) {
                throw new RuntimeException();
            }
        }
        return s;
    }

    public IClassInfo getBinaryNumericPromotedType(IClassInfo c1,
            IClassInfo c2) {
        if (c1 == NATIVETYPE && c2 == NATIVETYPE) return NATIVETYPE;
        if (!isNumericOrNativeType(c1) || !isNumericOrNativeType(c2)) {
            throw new IllegalArgumentException();
        }
        if (c1 == DOUBLE || c2 == DOUBLE) return DOUBLE;
        if (c1 == FLOAT || c2 == FLOAT) return FLOAT;
        if (c1 == LONG || c2 == LONG) return LONG;
        return INT;
    }

    public IClassInfo Object     = forClass(java.lang.Object.class);
    public IClassInfo String     = forClass(java.lang.String.class);
    public IClassInfo Class      = forClass(java.lang.Class.class);
    public IClassInfo Cloneable  = forClass(java.lang.Cloneable.class);
    public IClassInfo Serializable  = forClass(java.io.Serializable.class);

    public IClassInfo Throwable        = forClass(java.lang.Throwable.class);
    public IClassInfo RuntimeException = forClass(RuntimeException.class);
    public IClassInfo Error            = forClass(java.lang.Error.class);

    public IClassInfo NullPointerException = forClass(NullPointerException.class);
    public IClassInfo OutOfMemoryError = forClass(OutOfMemoryError.class);
    public IClassInfo ArrayIndexOutOfBoundsException = forClass(ArrayIndexOutOfBoundsException.class);
    public IClassInfo ArrayStoreException = forClass(ArrayStoreException.class);
    public IClassInfo ArithmeticException = forClass(ArithmeticException.class);
    public IClassInfo NegativeArraySizeException = forClass(NegativeArraySizeException.class);
    public IClassInfo ClassCastException = forClass(ClassCastException.class);
    public IClassInfo InstantiationError = forClass(InstantiationError.class);

    public IClassInfo BOOLEAN    = forClass(boolean.class);
    public IClassInfo BYTE       = forClass(byte.class);
    public IClassInfo SHORT      = forClass(short.class);
    public IClassInfo CHAR       = forClass(char.class);
    public IClassInfo INT        = forClass(int.class);
    public IClassInfo LONG       = forClass(long.class);
    public IClassInfo FLOAT      = forClass(float.class);
    public IClassInfo DOUBLE     = forClass(double.class);

    public IClassInfo VOID       = forClass(void.class);
    public IClassInfo NULL       = new NullType(this);
    public IClassInfo NATIVETYPE = new NativeType(this);

    public void reportError(Object obj, String msg) throws CompileException {
        if (obj instanceof ILocationContext) {
            ((ILocationContext)obj).reportError(msg);
        } else {
            System.err.print("Error in precompiled classes: " + msg + "\n");
            throw new CompileException(msg);
        }
    }

    public static String mangle(String what) {
        String result = "";
        for (int i=0, len=what.length(); i<len; i++) {
            char c = what.charAt(i);
            switch (c) {
            case '.': result += '_'; break; // package name mangling
            case '/': result += '_'; break;
            case '_': result += "_1"; break;
            case ';': result += "_2"; break;
            case '[': result += "_3"; break;
            default:
                if (c == (c & 0x7f) && c >= 0x20 && c != '$') { // ASCII
                    result += c;
                } else {
                    String hex = Integer.toHexString(c);
                    for (int j=hex.length(); j<4; j++) hex = "0" + hex;
                    result += "_0" + hex.toUpperCase();
                }
            }
        }
        return result;
    }

    public boolean equals (IClassInfo c1, IClassInfo c2)
            throws CompileException {
//        return c1.getSignature().equals(c2.getSignature());
        return c1 == c2;
    }

    public String getTypeNames(IClassInfo[] types) throws CompileException {
        String s= "";
        for (int i=0; i<types.length; i++) {
            if (types[i] == this.NULL) {
                s += "<reference>";
            } else if (types[i] == this.NATIVETYPE) {
                s += "<primitive>";
            } else {
                s += types[i].getFullName();
            }
            if (i+1 < types.length) s+= ", ";
        }
        return s;
    }

    public static String unicode2UTF(String s) {
        final char chararray[] = s.toCharArray();
        StringBuffer buf = new StringBuffer(chararray.length);
        for (int i=0; i<chararray.length; i++) {
            appendUnicodeChar2UTFString(buf, chararray[i]);
        }
        return buf.toString();
    }

    private static void appendUnicodeChar2UTFString(StringBuffer buf, char ch) {
        if (ch == '\u0000') {
            buf.append((char)0xC0).append((char)0x80);
        } else if (ch <= '\u007F') {
            buf.append(ch);
        } else if (ch <= '\u07FF') {
            buf.append((char)(((ch>>6) & 0x1F) | 0xC0)).
                append((char)((ch & 0x3F) | 0x80));
        } else {
            buf.append((char)(((ch>>12) & 0x0F) | 0xE0)).
                append((char)(((ch>>6) & 0x3F) | 0x80)).
                append((char)((ch & 0x3F) | 0x80));
        }
    }

    public static String utf2cstring(String utf) {
        final char chararray[] = utf.toCharArray();
        StringBuffer buf = new StringBuffer(chararray.length + 10);
        for (int i=0; i<chararray.length; i++) {
            appendUTFchar2cstring(buf, chararray[i]);
        }
        return buf.toString();
    }

    private static void appendUTFchar2cstring(StringBuffer buf, char ch) {
        if (ch < 0x20 || ch > 0x7f) {
            String hex = Integer.toHexString(ch);
            if (ch<0x10) hex = "0" + hex;
            buf.append("\\x" + hex);
        } else if (ch == '\\' || ch == '?' || ch == '"' || ch == '\'') {
            buf.append("\\" + ch);
        } else {
            buf.append(ch);
        }
    }
}