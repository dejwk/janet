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

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.*;
import pl.edu.agh.icsr.janet.tree.Node;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYNode extends Node implements ILocationContext {

    protected int beg_lineno;
    protected int beg_charno;
    protected int beg_charno0;
    protected int beg_line_beg;
    protected int end_lineno;
    protected int end_charno;
    protected int end_charno0;
    protected int end_line_beg;

    protected JanetSourceReader ibuf;

    public YYNode(ILocationContext cxt) {
        ibuf = cxt.ibuf();
        setBounds(cxt);
    }

    public final YYNode expand(ILocationContext cxt) {
        YYLocation l;
        if ((l = cxt.lbeg()).charno0 < beg_charno0) {
            this.beg_lineno = l.lineno;
            this.beg_charno = l.charno;
            this.beg_charno0 = l.charno0;
            this.beg_line_beg = l.line_beg;
        }
        if ((l = cxt.lend()).charno0 > end_charno0) {
            this.end_lineno = l.lineno;
            this.end_charno = l.charno;
            this.end_charno0 = l.charno0;
            this.end_line_beg = l.line_beg;
        }
        return this;
    }

    protected YYNode append(YYNode n) {
        expand(n);
        return (YYNode)addSon(n);
    }
/*
    protected YYNode insert(YYNode n) {
        expand(n);
        return (YYNode)insertSon(n);
    }
*/
    protected YYNode absorb(YYNode n) {
        YYNode s = (YYNode)n.firstSon();
        for (; s != null; s = (YYNode)s.nextBrother()) {
            this.addSon(s);
        }
        return this.expand(n);
    }

/*
    public YYNode compact() {
        return this;
    }
*/
    protected final void setBounds(ILocationContext cxt) {
        YYLocation l;
        l = cxt.lbeg();
        beg_lineno = l.lineno;
        beg_charno = l.charno;
        beg_charno0 = l.charno0;
        beg_line_beg = l.line_beg;
        l = cxt.lend();
        end_lineno = l.lineno;
        end_charno = l.charno;
        end_charno0 = l.charno0;
        end_line_beg = l.line_beg;
    }

//    public

    public String toString() {
        if (beg_lineno == end_lineno) {
            return ibuf.getbuf().substring(beg_charno0, end_charno0);
        } else {
            return "" + (beg_lineno+1) + ":" + (beg_charno+1) + "  <-->  " +
                        (end_lineno+1) + ":" + (end_charno+1);
        }
    }

    public Iterator getDumpIterator() {
        return super.iterator();
    }

    public String dump() {
        return dump(0);
    }

    String dump(int level) {
        String s = "" + (beg_lineno+1) + ":" + (beg_charno+1);

        while (s.length() < 8) s += " ";
        s += "<-->  " + (end_lineno+1) + ":" + (end_charno+1);
        while (s.length() < 22) s += " ";
        String clname = this.getClass().getName().substring(
            "pl.edu.agh.icsr.janet.".length());
        s += clname + " ";
        int pos = 55 + 2*level - s.length();
        if (pos < 0) pos = 0;
        while (pos-- > 0) s+= " ";
        s += toString() + "\n";

        Iterator i = getDumpIterator();
        while (i.hasNext()) {
            s += ((YYNode)i.next()).dump(level+1);
        }
        return s;
    }

    public void reportError(String msg) throws CompileException {
        Parser.reportError(this, msg);
    }
/*
    public static void compileError(ILocationContext cxt, String msg,
            boolean errthrow) throws CompileException {
        Parser.compileError(cxt, msg, errthrow);
    }
*/
    public JanetSourceReader ibuf() { return ibuf; }

    public YYLocation lbeg() {
        return new YYLocation(beg_lineno, beg_charno, beg_charno0,
                              beg_line_beg);
    }

    public YYLocation lend() {
        return new YYLocation(end_lineno, end_charno, end_charno0,
                              end_line_beg);
    }

    /**
     * Default resolving procedure.
     */
    public void resolve() throws ParseException {
        for(Iterator i = iterator(); i.hasNext();) {
            ((YYNode)i.next()).resolve();
        }
    }

    /**
     * Writing to the output
     */
    public void write(Writer w) throws IOException {
        StringBuffer buf = ibuf().getbuf();
        int beg = this.beg_charno0;
        int pos = beg;
        Iterator i = iterator();
        while (i.hasNext()) {
            YYNode n = (YYNode)i.next();
            w.write(buf.substring(pos, n.beg_charno0));
            n.write(w);
            pos = n.end_charno0;
        }
        w.write(buf.substring(pos, this.end_charno0));
    }

    private boolean locked;

    public void lock() { locked = true; }

    public boolean isLocked() { return locked; }

    public void ensureUnlocked() {
        if (locked) {
            throw new IllegalStateException("" + this + " has been locked");
        }
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

}
