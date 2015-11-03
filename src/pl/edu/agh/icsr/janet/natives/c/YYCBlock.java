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

package pl.edu.agh.icsr.janet.natives.c;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.natives.c.Tags.*;
import pl.edu.agh.icsr.janet.natives.YYNativeCode;
import pl.edu.agh.icsr.janet.yytree.YYNode;
import pl.edu.agh.icsr.janet.yytree.YYStatement;
import pl.edu.agh.icsr.janet.YYLocation;
import pl.edu.agh.icsr.janet.natives.IWriter;
import java.util.*;

public class YYCBlock extends YYCChunk {

    int content_beg, content_end;

    public YYCBlock(IJavaContext cxt) {
        super(cxt);
    }

    public void markBeg(YYLocation l) {
        content_beg = l.charno0;
    }

    public void markEnd(YYLocation l) {
        content_end = l.charno0;
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return write((Writer)w, param);
    }

    private int write(Writer w, int param) throws java.io.IOException {
        YYNode n;
        StringBuffer buf = ibuf().getbuf();
        int pos = this.beg_charno0;
        Iterator i = iterator();
        if (!i.hasNext()) { // only write {}
            if ((param & Writer.PHASE_WRITE) != 0) {
                w.write(buf.substring(pos, this.lend().charno0));
            }
        } else {

            // write block opening
            if ((param & Writer.PHASE_PREPARE) != 0) {
                this.tag = w.begDeclarationTag(this);
            } else {
                w.write(buf.substring(pos, content_beg));
                boolean hasVariables = w.writeDclUnitDeclarations((DeclarationTag)this.tag);
                if (hasVariables) w.write("\n" + w.makeIndent());
                w.writeDclUnitBegin((DeclarationTag)this.tag);
                pos = content_beg;
            }

            // write block body
            while (i.hasNext()) {
                n = (YYNode)i.next();
                if ((param & Writer.PHASE_WRITE) != 0) {
                    w.write(buf.substring(pos, n.lbeg().charno0));
                }
                n.write(w, param);
                pos = n.lend().charno0;
            }
            w.write(buf.substring(pos, content_end));
            pos = content_end;

            //write block closing
            if ((param & Writer.PHASE_PREPARE) != 0) {
                w.endDeclarationTag();
            } else {
                w.writeDclUnitEnd((DeclarationTag)this.tag);
                w.write(buf.substring(pos, this.lend().charno0));
            }
        }
        return 0;
    }
/*
    public YYStatement add(YYStatement s) {
        if (content_beg == 0) {
            markBeg(s.lbeg());
        }
        markEnd(s.lend());
        return super.add(s);
    }
*/
}