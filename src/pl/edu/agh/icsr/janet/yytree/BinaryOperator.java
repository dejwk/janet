/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

/**
 * Title:        Janet
 * Description:  Java Native Extensions
 * Copyright:    Copyright (c) Dawid Kurzyniec
 * Company:      NA
 * @author Dawid Kurzyniec
 * @version 1.0
 */
import java.util.Collection;
import java.util.Collections;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.IDetailedLocationContext;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.reflect.ClassManager;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;

public abstract class BinaryOperator {

    YYExpression l, r;
    IClassInfo lt, rt;
    ClassManager classMgr;
    boolean isAssigned;

    public static final int PLUS        = 1;
    public static final int MINUS       = 2;
    public static final int MULTIPLY    = 3;
    public static final int DIVIDE      = 4;
    public static final int REMAINDER   = 5;
    public static final int AND         = 6;
    public static final int OR          = 7;
    public static final int XOR         = 8;
    public static final int LSHIFT      = 9;
    public static final int RSHIFT      = 10;
    public static final int LOGRSHIFT   = 11;

    private BinaryOperator(IDetailedLocationContext cxt) {
        this.classMgr = cxt.getClassManager();
    }

    public void assign(YYExpression l, YYExpression r) {
        if (isAssigned) throw new RuntimeException();
        this.l = l;
        this.r = r;
    }

    public abstract void resolve() throws ParseException;
    public abstract int getID();

    public IClassInfo getLeftType() { return lt; }
    public IClassInfo getRightType() { return rt; }
    public IClassInfo getResultType() { return lt; }

//    private static void reportError(String s) throws CompileException {
//        throw new CompileException(s);
//    }

    private final static int NUMERIC = 1;
    private final static int BOOLEAN = 2;
    private final static int STRING = 3;

    private static int checkTypes(ClassManager classMgr, IClassInfo c1,
        IClassInfo c2, String symbol, boolean numeric, boolean bool,
        boolean string) throws CompileException
    {
        if (c1 == classMgr.NATIVETYPE && c2 == classMgr.NATIVETYPE) {
            throw new CompileException(
                "at last one side of " + symbol + " operator must " +
                "have determinable type - use explicit cast");
        }
        if (numeric) {
            if (classMgr.isNumericOrNativeType(c1) &&
                    classMgr.isNumericOrNativeType(c2)) {
                return NUMERIC;
            }
        }
        if (bool) {
            if (c1 == classMgr.BOOLEAN && c2 == classMgr.BOOLEAN) {
                return BOOLEAN;
            }
        }
        if (string) {
            if (c1 == classMgr.String || c2 == classMgr.String) {
                return STRING;
            }
        }
        throw new CompileException("operator " + symbol + " cannot be applied to (" +
                    c1 + ", " + c2 + ")");
    }

    private static void checkIntegralTypes(ClassManager classMgr, String symbol,
        IClassInfo c1, IClassInfo c2) throws ParseException
    {
        if (!c1.isAssignableFrom(classMgr.LONG) ||
            !c2.isAssignableFrom(classMgr.LONG))
        {
            throw new CompileException("operator " + symbol +
                " cannot be applied to (" + c1 + ", " + c2 + ")");
        }
    }

    public Collection<IClassInfo> getExceptionsThrown() {
        return null;
    }

    public abstract String getSymbol();

    public static class Plus extends BinaryOperator {
        public Plus(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws CompileException {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            switch (checkTypes(classMgr, ltype, rtype, getSymbol(), true, false, true)) {
            case STRING:
                // this is a string concatenation, handled special way
                lt = rt = classMgr.String;
                return;
            case NUMERIC:
                lt = rt = classMgr.getBinaryNumericPromotedType(ltype, rtype);
                return;
            default:
                throw new RuntimeException();
            }
        }
        public String getSymbol() { return "+"; }
        public int getID() { return PLUS; }
    }

    public static class Minus extends BinaryOperator {
        public Minus(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws CompileException {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            checkTypes(classMgr, ltype, rtype, getSymbol(), true, false, false);
            lt = rt = classMgr.getBinaryNumericPromotedType(ltype, rtype);
        }
        public String getSymbol() { return "-"; }
        public int getID() { return MINUS; }
    }

    public static class Multiply extends BinaryOperator {
        public Multiply(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws CompileException {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            checkTypes(classMgr, ltype, rtype, getSymbol(), true, false, false);
            lt = rt = classMgr.getBinaryNumericPromotedType(ltype, rtype);
        }
        public String getSymbol() { return "*"; }
        public int getID() { return MULTIPLY; }
    }

    public static class Divide extends BinaryOperator {
        public Divide(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws CompileException  {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            checkTypes(classMgr, ltype, rtype, getSymbol(), true, false, false);
            lt = rt = classMgr.getBinaryNumericPromotedType(ltype, rtype);
        }
        /* integer division by zero throws exception */
        public Collection<IClassInfo> getExceptionsThrown() {
            if (rt == classMgr.INT || rt == classMgr.LONG) {
                return Collections.singleton(classMgr.ArithmeticException);
            }
            return null;
        }

        public String getSymbol() { return "/"; }
        public int getID() { return DIVIDE; }
    }

    public static class Remainder extends BinaryOperator {
        public Remainder(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws CompileException  {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            checkTypes(classMgr, ltype, rtype, getSymbol(), true, false, false);
            lt = rt = classMgr.getBinaryNumericPromotedType(ltype, rtype);
        }

        public String getSymbol() { return "%"; }
        public int getID() { return REMAINDER; }
    }

    public static class And extends BinaryOperator {
        public And(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws CompileException  {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            int type = checkTypes(classMgr, ltype, rtype, getSymbol(), true, true, false);
            lt = rt = (type == BOOLEAN)
                ? classMgr.BOOLEAN
                : classMgr.getBinaryNumericPromotedType(ltype, rtype);
        }

        public String getSymbol() { return "&"; }
        public int getID() { return AND; }
    }

    public static class Or extends BinaryOperator {
        public Or(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws CompileException  {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            int type = checkTypes(classMgr, ltype, rtype, getSymbol(), true, true, false);
            lt = rt = (type == BOOLEAN)
                ? classMgr.BOOLEAN
                : classMgr.getBinaryNumericPromotedType(ltype, rtype);
        }

        public String getSymbol() { return "|"; }
        public int getID() { return OR; }
    }

    public static class XOr extends BinaryOperator {
        public XOr(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws ParseException {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            int type = checkTypes(classMgr, ltype, rtype, getSymbol(), true, true, false);
            lt = rt = (type == BOOLEAN)
                ? classMgr.BOOLEAN
                : classMgr.getBinaryNumericPromotedType(ltype, rtype);
        }

        public String getSymbol() { return "^"; }
        public int getID() { return XOR; }
    }

    public static class LShift extends BinaryOperator {
        public LShift(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws ParseException  {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            checkIntegralTypes(classMgr, getSymbol(), ltype, rtype);
            lt = (ltype == classMgr.LONG) ? classMgr.LONG : classMgr.INT;
            rt = (rtype == classMgr.LONG) ? classMgr.LONG : classMgr.INT;
        }
        public String getSymbol() { return "<<"; }
        public int getID() { return LSHIFT; }
    }

    public static class RShift extends BinaryOperator {
        public RShift(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws ParseException  {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            checkIntegralTypes(classMgr, getSymbol(), ltype, rtype);
            lt = (ltype == classMgr.LONG) ? classMgr.LONG : classMgr.INT;
            rt = (rtype == classMgr.LONG) ? classMgr.LONG : classMgr.INT;
        }
        public String getSymbol() { return ">>"; }
        public int getID() { return RSHIFT; }
    }

    public static class LogRShift extends BinaryOperator {
        public LogRShift(IDetailedLocationContext cxt) {
            super(cxt);
        }
        public void resolve() throws ParseException  {
            IClassInfo ltype = l.getExpressionType();
            IClassInfo rtype = r.getExpressionType();
            checkIntegralTypes(classMgr, getSymbol(), ltype, rtype);
            lt = (ltype == classMgr.LONG) ? classMgr.LONG : classMgr.INT;
            rt = (rtype == classMgr.LONG) ? classMgr.LONG : classMgr.INT;
        }
        public String getSymbol() { return ">>>"; }
        public int getID() { return LOGRSHIFT; }
    }
}
