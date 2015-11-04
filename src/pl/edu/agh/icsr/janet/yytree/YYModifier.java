/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.CompileException;
import java.lang.reflect.Modifier;
import pl.edu.agh.icsr.janet.IJavaContext;

// uses constants from java.lang.reflect.Modifier

public class YYModifier extends YYNode {

    int modifier;

    public YYModifier(IJavaContext cxt, int m) {
        super(cxt);
        modifier = m;
    }

    public YYModifier nextModifier() {
        return (YYModifier)nextBrother();
    }

}
