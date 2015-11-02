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
