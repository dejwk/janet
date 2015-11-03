/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

public final class YYLocation extends jbf.YYlocation {

    public int line_beg;

    public YYLocation() {
	super(0, 0, 0);
	line_beg = 0;
    }

    public YYLocation(int line, int charno, int charno0, int line_beg) {
	super(charno0, line, charno);
        this.line_beg = line_beg;
    }

    public final void copyFrom(YYLocation loc) {
        set(loc.lineno, loc.charno, loc.charno0, loc.line_beg);
    }

    public final void set(int lineno, int charno, int charno0, int line_beg) {
	this.lineno = lineno;
	this.charno = charno;
	this.charno0 = charno0;
	this.line_beg = line_beg;
    }

    public static void xchg(YYLocation l1, YYLocation l2) {
	int tmp;
	tmp = l1.lineno;
	l1.lineno = l2.lineno;
	l2.lineno = tmp;

	tmp = l1.charno;
	l1.charno = l2.charno;
	l2.charno = tmp;

	tmp = l1.charno0;
	l1.charno0 = l2.charno0;
	l2.charno0 = tmp;

	tmp = l1.line_beg;
	l1.line_beg = l2.line_beg;
	l2.line_beg = tmp;
    }

    public void nextChar() {
	charno0++; charno++;
    }

    public void nextLine() {
	lineno++;
	charno = 0;
	line_beg = charno0;
    }

    public int getLine() { return lineno+1; }
    public int getPosInLine() { return charno+1; }

    public String toString() {
        return "" + getLine() + ":" + getPosInLine();
    }

    //    public final static YYLocation LOC_NULL = new YYLocation(-1, -1, -1);
}
