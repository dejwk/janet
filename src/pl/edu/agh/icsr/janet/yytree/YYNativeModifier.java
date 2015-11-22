/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.lang.reflect.Modifier;

import pl.edu.agh.icsr.janet.IJavaContext;

// uses constants from java.lang.reflect.Modifier

public class YYNativeModifier extends YYModifier {

    String language;

    public YYNativeModifier(IJavaContext cxt, String lang) {
        super(cxt, Modifier.NATIVE);
        this.language = lang;
    }

    public String getLanguage() { return language; }

    String getNativeString() {
        return "native \"" + language + "\" ";
    }
}
