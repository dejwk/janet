/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.CompileException;
import java.lang.reflect.Modifier;
import pl.edu.agh.icsr.janet.IJavaContext;

// uses constants from java.lang.reflect.Modifier

public class YYModifierList extends YYNode {

    public static final int ACCESS_MODIFIERS =
        Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;

    int modifiers;
    String language;

    public YYModifierList(IJavaContext cxt) {
        super(cxt);
    }

    public YYModifierList add(YYNativeModifier m) throws CompileException {
        language = m.language;
        return add((YYModifier)m);
    }

    public String getNativeLanguage() {
        return this.language;
    }

    public YYModifierList add(YYModifier m) throws CompileException {
        super.append(m);
        if (is(m.modifier)) {
            m.reportError("Modifier " + Modifier.toString(m.modifier) +
                         " specified twice");
        }
        if ((modifiers & ACCESS_MODIFIERS) != 0 && is(m, ACCESS_MODIFIERS)) {
            m.reportError("At most one of public, private or protected may " +
                         "be specified");
        }
        modifiers |= m.modifier;
        return this;
    }

    public YYModifier findFirst(int m) {
        YYModifier mf = (YYModifier)firstSon();
        while (mf != null & !is(mf, m)) mf = mf.nextModifier();
        return mf;
    }

    public final boolean is(int m) {
        return (modifiers & m) != 0;
    }

    public final static boolean is(YYModifier mf, int m) {
        return (mf.modifier & m) != 0;
    }

    public int getModifiers() { return modifiers; }

    public boolean isPublic() { return is(Modifier.PUBLIC); }
    public boolean isProtected() { return is(Modifier.PROTECTED); }
    public boolean isPrivate() { return is(Modifier.PRIVATE); }
    public boolean isStatic() { return is(Modifier.STATIC); }
    public boolean isAbstract() { return is(Modifier.ABSTRACT); }
    public boolean isFinal() { return is(Modifier.FINAL); }
    public boolean isNative() { return is(Modifier.NATIVE); }
    public boolean isSynchronized() { return is(Modifier.SYNCHRONIZED); }
    public boolean isTransient() { return is(Modifier.TRANSIENT); }
    public boolean isVolatile() { return is(Modifier.VOLATILE); }

    String getNativeModifierString() { // overridden in YYModifiersWithNative
        return "native ";
    }

    public static String toString(int m) { return toString(m, null); }

    public static String toString(int m, String lang) {
        return ((Modifier.isPublic(m) ? "public " : "") +
                (Modifier.isProtected(m) ? "protected " : "") +
                (Modifier.isPrivate(m) ? "private " : "") +
                (Modifier.isStatic(m) ? "static " : "") +
                (Modifier.isAbstract(m) ? "abstract " : "") +
                (Modifier.isFinal(m) ? "final " : "") +
                (Modifier.isNative(m) ? "native " +
                              (lang != null ? lang + " " : "") : "")+
                (Modifier.isSynchronized(m) ? "synchronized " : "") +
                (Modifier.isTransient(m) ? "transient " : "") +
                (Modifier.isVolatile(m) ? "volative " : "")); //.trim();
    }

}
