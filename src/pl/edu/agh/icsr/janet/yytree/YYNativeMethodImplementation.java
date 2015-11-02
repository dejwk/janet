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

import java.util.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;


public class YYNativeMethodImplementation extends YYStatement {

    public static int radkeNumbers[] = {
        0x00000003, 0x00000003, 0x00000007, 0x0000000B,
        0x00000013, 0x0000002B, 0x00000043, 0x0000008B,
        0x00000107, 0x0000020B, 0x00000407, 0x0000080F,
        0x00001003, 0x0000201B, 0x0000401B, 0x0000800B,
        0x00010003, 0x00020027, 0x00040003, 0x0008003B,
        0x00100007, 0x0020003B, 0x0040000F, 0x0080000B,
        0x0100002B, 0x02000023, 0x0400000F, 0x08000033,
        0x10000003, 0x2000000B, 0x40000003, 0
    };

    YYClass declCls;
    YYNativeCode ncode;
    INativeMethodInfo mth;
    Vector externalVariables; // used with native statement (not method)

    Set clsidxs;
    Set fldidxs;
    Set mthidxs;
    Set stridxs;

    int refarrs;
    int syncnum;
    int radkeidx = 4; // the idx of initial hashtable size, at last 0x13

    public YYNativeMethodImplementation(IJavaContext cxt) {
        super(cxt, false, true);
        this.ncode = null;

    }

    public YYNativeMethodImplementation addStatements(YYNativeCode ncode) {
        this.ncode = ncode;
        return this;
    }

    public YYNativeCode getStatements() {
        return ncode;
    }

    public void setDeclaringClass(YYClass cls) {
        this.declCls = cls;
    }

    public IClassInfo getDeclaringClass() {
        return declCls;
    }

    public INativeMethodInfo getNativeMethodHeader() { return mth; }

    public void resolve() throws ParseException {
        this.clsidxs = new HashSet();
        this.fldidxs = new HashSet();
        this.mthidxs = new HashSet();
        this.stridxs = new HashSet();

        ncode.resolve();
        addExceptions(ncode.getExceptionsThrown());
    }

    public int addReferencedClass(IClassInfo c) {
        return addReferencedClass(c, true);
    }

    public int addReferencedClass(IClassInfo c, boolean addHere) {
        if (declCls == null) {
            throw new IllegalStateException();
        }
        Integer ret = declCls.addReferencedClass(c);
        if (addHere) clsidxs.add(ret);
        return ret.intValue();
    }

    public int addReferencedField(int clsidx, IFieldInfo f)
            throws CompileException {
        Integer ret = declCls.addReferencedField(clsidx, f);
        fldidxs.add(ret);
        return ret.intValue();
    }

    public int addReferencedMethod(int clsidx, IMethodInfo m)
        throws ParseException
    {
        Integer ret = declCls.addReferencedMethod(clsidx, m);
        mthidxs.add(ret);
        return ret.intValue();
    }

    public int addReferencedStringLiteral(String lit)
            throws CompileException {
        Integer ret = declCls.addReferencedStringLiteral(lit);
        stridxs.add(ret);
        return ret.intValue();
    }

    /**
     * One more primitive type array used at the native side -> maybe
     * the larger hashtable is required (default is at last two times greater)
     */
    public void addReferencedPrimitiveTypeArray() {
        if (++refarrs * 2 > radkeNumbers[radkeidx]) radkeidx++;
    }

    public int addSynchronizedStatement() {
        return syncnum++;
    }

    public int getSynchronizedStatementsNum() {
        return syncnum;
    }

    public boolean usesPrimitiveTypeArrays() {
        return (refarrs > 0);
    }

    public int getInitialRadkeIdx() {
        return radkeidx;
    }

    public int getScopeType() {
        return IScope.NATIVE_METHOD_IMPLEMENTATION;
    }

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator {
        int i=1;
        DumpIterator() { i=0; }
        public boolean hasNext() { return i<1; }
        public Object next() {
            i++;
            return i==1 ? ncode : null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

}