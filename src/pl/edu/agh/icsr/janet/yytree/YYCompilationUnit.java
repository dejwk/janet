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

import java.util.*;
import java.io.File;
import java.net.URL;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;

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

    Map singles;

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

    public Map getSingleImportDeclarations() throws ParseException {
        Map m = imports.getSingles();
        if (singles == null) {
            lock();
            singles = new HashMap();
            ClassManager classMgr = mgr.getClassManager();
            // exchange class names for their IClassInfo objects
            for(Iterator i = m.values().iterator(); i.hasNext();) {
                YYName t = (YYName)i.next();
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

    public List getImportOnDemandDeclarations() {
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

    private class ClsItr implements Iterator {
        Iterator i;
        YYClass next;

        public ClsItr() {
            i = iterator();
            next = findNextClass();
        }

        public boolean hasNext() {
            return next != null;
        }

        public Object next() {
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

    public Iterator getDeclaredClassesIterator() {
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
