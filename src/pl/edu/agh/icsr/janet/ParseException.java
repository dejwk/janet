/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

public class ParseException extends Exception {
    public ParseException() {
	super();
    }

    public ParseException(String msg) {
	super(msg);
    }
}

