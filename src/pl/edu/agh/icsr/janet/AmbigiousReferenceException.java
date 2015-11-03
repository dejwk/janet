/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

public class AmbigiousReferenceException extends CompileException {

    Object o1, o2;

    public AmbigiousReferenceException(Object o1, Object o2) {
	super("ambigious reference: both " + o1.toString() + " and " +
            o2.toString() + " match");
        this.o1 = o1;
        this.o2 = o2;
    }

    public Object getObject1() { return o1; }
    public Object getObject2() { return o2; }
}
