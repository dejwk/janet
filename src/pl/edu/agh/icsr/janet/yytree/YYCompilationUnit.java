/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.util.*;
import java.io.File;
import java.net.URL;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.tree.Node;

public class YYCompilationUnit extends YYNode implements IScope {

    static final String janetHeader =
        "/**\n" +
        " * file:      %__JAVAFILENAME__%\n" +
        " * basefile:  %BASEFILE%\n" +
        " * generated: %__DATE__%\n" +
        " */\n\n";

    CompilationManager mgr;
    YYPackage pkg;
    YYImportDeclarationList imports;
    boolean markedForProcessing;
    String libName;

    Map<String, IClassInfo> singles;

    public YYCompilationUnit(IJavaContext cxt, CompilationManager mgr,
        boolean doProcess)
    {
        super(cxt);
        this.mgr = mgr;
        this.markedForProcessing = doProcess;
        mgr.addCompilationUnit(this);
    }

    public YYCompilationUnit setPackageDeclaration(YYPackage pkg) {
        ensureUnlocked();
        this.pkg = pkg;
        return this;
    }

    public YYCompilationUnit setImportDeclarations(YYImportDeclarationList imports) {
        ensureUnlocked();
        this.imports = imports;
        return this;
    }

    public YYCompilationUnit addTypeDeclaration(YYClass cls)
            throws CompileException {
        ensureUnlocked();
        if (cls != null) {
            String sname = cls.getSimpleName();
            String t = (imports != null) ? imports.findSingle(sname) : null;
            if (t != null) {
                cls.reportError((cls.isInterface() ? "interface " : "class ") +
                    "name " + sname + " clashes with imported class " + t);
            } else if (mgr.getClassManager().addClass(cls)) {
                super.append(cls);
            }
        }
        return this;
    }


/*    public String toString() {
        String s = "CompilationUnit dump: \npackage " + package_declaration +
            "\nimports: " + import_declarations;
        return s;
    }
*/

    public Map<String, IClassInfo> getSingleImportDeclarations() throws ParseException {
        Map<String, YYName> m = imports.getSingles();
        if (singles == null) {
            lock();
            singles = new HashMap<String, IClassInfo>();
            ClassManager classMgr = mgr.getClassManager();
            // exchange class names for their IClassInfo objects
            for (YYName t : m.values()) {
                String sname = t.lastNameNode().get();
                IClassInfo cls = classMgr.forName(t.toString());
                if (cls == null) {
                    t.reportError("class or interface " + t + " not found");
                } else {
                    if (!cls.isAccessibleTo(getPackageName())) {
                        t.reportError("Can't access " + cls +
                            ". Class or interface imported from another " +
                            "package must be public");
                    }
                    singles.put(sname, cls);
                }
            }
        }
        return singles;
    }

    public List<String> getImportOnDemandDeclarations() {
        return imports.getOnDemands();
    }

    public String getPackageName() {
        return pkg != null ? pkg.toString() : "";
    }

    public void write(Writer w) throws java.io.IOException {
        w.getSubstituter().setSubst("BASEFILE", ibuf().getOriginFile().getName());
        w.write(Janet.getGeneratedCodeLicense());
        w.write(janetHeader, true);
        super.write(w);
        w.getSubstituter().unsetSubst("BASEFILE");
    }

    private class ClsItr implements Iterator<YYClass> {
        Iterator<Node> i;
        YYClass next;

        public ClsItr() {
            i = iterator();
            next = findNextClass();
        }

        public boolean hasNext() {
            return next != null;
        }

        public YYClass next() {
            YYClass ret = next;
            next = findNextClass();
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        YYClass findNextClass() {
            Object o = null;
            while (i.hasNext()) {
                if ((o = i.next()) instanceof YYClass) break;
                o = null;
            }
            return (YYClass)o;
        }
    }

    public Iterator<YYClass> getDeclaredClassesIterator() {
        return new ClsItr();
    }
/*
    public void resolveMethods() throws CompileException {
        for (Iterator i = getDeclaredClasses(); i.hasNext();) {
            ((YYClass)i.next()).resolveMethods();
        }
    }
*/
    public int getScopeType() {
        return IScope.COMPILATION_UNIT;
    }

    public IScope getEnclosingScope() { return null; }
    public IScope getCurrentMember() { return null; }
    public YYClass getCurrentClass() { return null; }

    public boolean markedForProcessing() { return markedForProcessing; }

    public String toString() {
        return "Compilation unit: " + ibuf().getOriginAsString();
    }
}
