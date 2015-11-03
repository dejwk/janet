/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.IJavaContext;

public class YYTypeList extends YYNode {

    public YYTypeList(IJavaContext cxt) {
        super(cxt);
    }

    public YYTypeList add(YYType n) {
        return (YYTypeList)super.addSon(n);
    }
}
