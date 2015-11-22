/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives.c;

import java.io.IOException;
import java.util.Iterator;

import pl.edu.agh.icsr.janet.IJavaContext;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.natives.YYNativeCode;
import pl.edu.agh.icsr.janet.tree.Node;
import pl.edu.agh.icsr.janet.yytree.YYNode;

public class YYCChunk extends YYNativeCode {

    public YYCChunk(IJavaContext cxt) {
        super(cxt);
    }

/*    public YYCChunk(IJavaContext cxt, YYStatement s) {
        super(cxt);
    }
*/
/*    public YYCChunk(IJavaContext cxt, YYExpression e) {
        super(cxt);
    }

    public YYCChunk(IJavaContext cxt, String s) {
        super(cxt);
    }*/

/*    public YYCChunk addChunk(YYStatement stmt) {
        return (YYCChunk)super.add(stmt);
    }*/

public int write(IWriter w, int param) throws IOException {
    StringBuffer buf = ibuf().getbuf();
        int beg = this.beg_charno0;
        int pos = beg;
        Iterator<Node> i = iterator();
        while (i.hasNext()) {
            YYNode n = (YYNode)i.next();
            if ((param & Writer.PHASE_WRITE) != 0) {
                w.write(buf.substring(pos, n.lbeg().charno0));
            }
            n.write(w, param);
            pos = n.lend().charno0;
        }
        if ((param & Writer.PHASE_WRITE) != 0) {
            w.write(buf.substring(pos, this.lend().charno0));
            return 1;
        }
        return 0;
    }
}