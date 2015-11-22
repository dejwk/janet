/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives.c;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.natives.c.Tags.*;
import pl.edu.agh.icsr.janet.tree.Node;
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
        Iterator<Node> i = iterator();
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