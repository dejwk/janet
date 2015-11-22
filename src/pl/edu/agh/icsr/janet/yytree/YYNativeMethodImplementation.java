/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
    //Vector externalVariables; // used with native statement (not method)

    Set<Integer> clsidxs;
    Set<Integer> fldidxs;
    Set<Integer> mthidxs;
    Set<Integer> stridxs;

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
        this.clsidxs = new HashSet<Integer>();
        this.fldidxs = new HashSet<Integer>();
        this.mthidxs = new HashSet<Integer>();
        this.stridxs = new HashSet<Integer>();

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

    class DumpIterator implements Iterator<YYNode> {
        int i=1;
        DumpIterator() { i=0; }
        public boolean hasNext() { return i<1; }
        public YYNode next() {
            i++;
            return i==1 ? ncode : null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }

}