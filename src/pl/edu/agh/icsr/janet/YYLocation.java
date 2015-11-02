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
