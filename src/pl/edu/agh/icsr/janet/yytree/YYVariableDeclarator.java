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
import java.lang.reflect.Modifier;
import pl.edu.agh.icsr.janet.natives.IWriter;

/**
 * This class represents a local variable declared in Java application.
 * Any local variable declaration, method parameter or catch clause variable
 * is represented by instance of this class.
 */

public class YYVariableDeclarator extends YYStatement implements IFieldInfo {

    public static final int LOCAL_VARIABLE  = 1;
    public static final int PARAMETER       = 2;
    public static final int CATCH_PARAMETER = 3;

    YYClass declaringClass; // used for fields only
    String name;
    int dims = 0;
    int modifiers = 0;
    YYType yytype;
    IClassInfo resolvedType;
    YYExpression initializer;
    int declarationType;

    public YYVariableDeclarator(IJavaContext cxt, String name) {
        this(cxt, name, true);
    }

    public YYVariableDeclarator(IJavaContext cxt, String name, boolean pure) {
        super(cxt, pure, false);
        this.name = name;
        this.dims = 0;
    }

    void setDeclaringClass(YYClass cls) {
        this.declaringClass = cls;
    }

    public YYVariableDeclarator setDeclarationType(int dcltype) {
        this.declarationType = dcltype;
        return this;
    }

    public int getDeclarationType() {
        return declarationType;
    }

    public YYVariableDeclarator setModifiers(YYModifierList m) {
        return setModifiers(m.getModifiers());
    }

    public YYVariableDeclarator setModifiers(int modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public YYVariableDeclarator setType(YYType t) {
        this.yytype = t;
        return this;
    }

    private void resolveType() throws ParseException {
        resolvedType = dims == 0 ? yytype.getResolvedType()
                                 : yytype.getResolvedType().getArrayType(dims);
        dims = 0;
        yytype = null;
    }

    public IClassInfo getDeclaringClass() {
        return declaringClass;
    }

    public int getModifiers() {
        return modifiers;
    }

    public IClassInfo getType() throws ParseException {
        if (resolvedType == null) resolveType();
        return resolvedType;
    }

    public YYVariableDeclarator setInitializer(YYExpression initializer) {
        this.initializer = initializer;
        return this;
    }

    public YYVariableDeclarator addDims(int dims_no) {
        dims += dims_no;
        return this;
    }

    public void resolve() throws ParseException {
        if (initializer != null) {
            initializer.resolve(true);
            initializer.setImplicitCastType(this.getType());
            addExceptions(initializer.getExceptionsThrown());
        } else {
            exceptions = new java.util.HashMap();
        }
    }

    public String getName() {
        return name;
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    public YYExpression getInitializer() { return initializer; }

/*    public String getSignature() throws CompileException {
        return getType().getSignature();
    }
*/
    public String toString() {
        try {
            return YYModifierList.toString(modifiers) + getType() + " " +
                name + (initializer != null ? " = " + initializer : "");
        } catch (ParseException e) { return null; }
    }
}
