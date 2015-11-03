/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.IJavaContext;

public class YYDims extends YYNode {
    int dims = 0;

    public YYDims(IJavaContext cxt) {
        super(cxt);
    }

    public YYDims addDim(IJavaContext cxt) {
        dims++;
        expand(cxt);
        return this;
    }

    public int dims() {
        return dims;
    }
}