/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
