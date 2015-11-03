/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.ILocationContext;

public class YYPackage extends YYNode {
    String pkgname;

    public YYPackage(ILocationContext cxt, String pkgname) {
        super(cxt);
	this.pkgname = pkgname;
    }

    public boolean equals(YYPackage pkg) {
        return (pkg != null && this.pkgname == pkg.pkgname);
    }

    public String toString() { return pkgname; }
}
