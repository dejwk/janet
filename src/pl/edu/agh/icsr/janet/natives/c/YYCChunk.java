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
import pl.edu.agh.icsr.janet.yytree.*;
import pl.edu.agh.icsr.janet.natives.*;
import java.io.*;
import java.util.*;

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
        Iterator i = iterator();
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