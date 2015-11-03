/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.util.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class YYStatement extends YYNode implements IScope {
    boolean pure = true;
    Map exceptions; // maps exceptions to statements where they originate
    boolean exceptionsShared; // is "exceptions" shared with another statement

    YYCompilationUnit compUnit;
    IScope enclosing; // class, method or statement
    //YYClass enclosingClass;
    //IScope enclosingMember;
    int dclUnitType = IScope.STATEMENT;

    public YYStatement(IDetailedLocationContext cxt) {
        this(cxt, true, false);
    }

    public YYStatement(IDetailedLocationContext cxt, boolean pure) {
        this(cxt, pure, false);
    }

    public YYStatement(IDetailedLocationContext cxt, boolean pure, boolean isBlock) {
        super(cxt);
        this.pure = pure;
        this.enclosing = cxt.getScope();
        this.compUnit = cxt.getCompilationUnit();
        if (isBlock) this.dclUnitType = IScope.BLOCK;
    }

    public boolean isPure() { return pure; }
    public boolean isJava() {return true; }

    public YYStatement add(YYStatement s) {
        YYStatement lastSon = (YYStatement)lastSon();
        if (s.isPure()) {
            if (lastSon == null && (s.isJava() == this.isJava())/* && this.isPure()*/) {
                this.expand(s);
                return this;
            } else if (lastSon != null && lastSon.isPure() && (s.isJava() == lastSon.isJava())) {
                lastSon.expand(s);
                this.expand(s);
                return this;
            }
        }
        pure = false;
        this.append(s);
        return this;
    }

    public YYStatement absorb(YYStatement s) {
        super.absorb(s);
        return this;
    }

    public YYStatement compact() {
/*        boolean pure = true;
        YYStatement n = (YYStatement)firstSon();
        while (n != null) {
            if (!n.isPure() || (n.isJava() != this.isJava())) {
                pure = false;
                break;
            }
            n = (YYStatement)n.nextBrother();
        }
        this.pure = pure;
        if (pure) {
            this.killSons(); // their code does not need to be analysed
        }*/
        return this;
    }

    public void setScopeType(int dclUnitType) {
        this.dclUnitType = dclUnitType;
    }

    public IScope getEnclosingScope() {
        return enclosing;
    }

    public YYClass getCurrentClass() {
        return enclosing.getCurrentClass();
    }

    public IScope getCurrentMember() {
        return enclosing.getCurrentMember();
    }

    public IScope getScope() {
        return enclosing;
    }

    public YYCompilationUnit getCompilationUnit() {
        return compUnit;
    }

    public int getScopeType() {
        return dclUnitType;
    }

    public Map getExceptionsThrown() {
        // must be resolved
        if (exceptions == null) throw new IllegalStateException();
        return exceptions;
    }

    public void resolve() throws ParseException {
        boolean shared = false;
        for(Iterator i = iterator(); i.hasNext();) {
            YYStatement s = (YYStatement)i.next();
            s.resolve();
            addExceptions(s.getExceptionsThrown());
        }
        if (exceptions == null) exceptions = new HashMap();
    }

    public final void addExceptions(Map excs) throws ParseException {
        if (exceptions == null || exceptions.isEmpty()) {
            exceptions = excs;
            exceptionsShared = true;
        } else {
            for (Iterator i = excs.keySet().iterator(); i.hasNext();) {
                IClassInfo e = (IClassInfo)i.next();
                addException(e);
            }
        }
    }

    public final void addException(IClassInfo e) throws ParseException {
        if (exceptions == null) {
            exceptions = new HashMap();
            exceptions.put(e, this);
        } else if (!ClassManager.containsException(exceptions.keySet(), e)) {
            if (exceptionsShared) { // clone
                exceptions = new HashMap(exceptions);
                exceptionsShared = false;
            }
            exceptions.put(e, this);
        }
    }

    YYNativeMethodImplementation findImpl() {
        IScope scope = getEnclosingScope();
        while (scope != null &&
                scope.getScopeType() != NATIVE_METHOD_IMPLEMENTATION) {
            scope = scope.getEnclosingScope();
        }
        return (YYNativeMethodImplementation)scope;
    }

    public final int registerClass(IClassInfo cls) {
        return registerClass(cls, true);
    }

    public final int registerClass(IClassInfo cls, boolean addHere) {
        YYNativeMethodImplementation impl = findImpl();
        if (impl != null) {
            return impl.addReferencedClass(cls, addHere);
        } else {
            return -1;
        }
    }

    public final int registerField(int clsidx, IFieldInfo fld) {
        YYNativeMethodImplementation impl = findImpl();
        if (impl != null) {
            try {
                return impl.addReferencedField(clsidx, fld);
            } catch (CompileException e) {
                throw new RuntimeException();
            }
        } else {
            return -1;
        }
    }

    public final int registerMethod(int clsidx, IMethodInfo mth) {
        YYNativeMethodImplementation impl = findImpl();
        if (impl != null) {
            try {
                return impl.addReferencedMethod(clsidx, mth);
            } catch (ParseException e) {
                throw new RuntimeException();
            }
        } else {
            return -1;
        }
    }

    public final int registerStringLiteral(String lit) {
        YYNativeMethodImplementation impl = findImpl();
        if (impl != null) {
            try {
                return impl.addReferencedStringLiteral(lit);
            } catch (CompileException e) {
                throw new RuntimeException();
            }
        } else {
            return -1;
        }
    }

    /* Interaction with native writer */

    public Object tag;

    public int write(IWriter w, int param) throws java.io.IOException {
        return w.write(this, param);
    }


}
