/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.natives.*;


public class YYFieldAccessExpression extends YYExpression {

    final static int THIS       = 1;
    final static int SUPER      = 2;
    final static int EXPRESSION = 3;
    final static int TYPE       = 4;
    final static int NONE       = 5;

    YYName unresolvedTargetName;

    YYExpression target;
    int targetType;
    String fieldName;

    IFieldInfo field;
    IClassInfo declCls;

    int classidx = -1;
    int fldidx = -1;

    /**
     * Field access using a simple name
     */
    public YYFieldAccessExpression(IDetailedLocationContext cxt,
            String fldname) {
        super(cxt);
        this.target = null;
        this.fieldName = fldname;
        this.targetType = NONE;
    }
    /**
     * Field access using a primary
     */
    public YYFieldAccessExpression(IDetailedLocationContext cxt,
            YYExpression target, String fldname) {
        super(cxt);
        this.target = target;
        this.fieldName = fldname;
        if (target instanceof YYThis) {
            this.targetType = ((YYThis)target).isSuper() ? SUPER : THIS;
        } else {
            this.targetType = EXPRESSION;
        }
    }

    /**
     * Field access using super ('this' is a primary)
     */
    public YYFieldAccessExpression(IDetailedLocationContext cxt,
            YYThis target, String fldname) {
        super(cxt);
        this.target = target;
        this.fieldName = fldname;
        this.targetType = target.isSuper() ? SUPER : THIS;
    }

    /**
     * Field access using expression name, when qualifying name has not
     * been yet reclassified (as a type or as a expression)
     */
    public YYFieldAccessExpression(IDetailedLocationContext cxt,
            YYName unresolved, String fldname) {
        super(cxt);
        this.unresolvedTargetName = unresolved;
        this.fieldName = fldname;
        // type of target not yet known (may be EXPRESSION or NONE)
    }

    /**
     * Field access using expression name, when qualifying name has been
     * reclassified as type (for static fields)
     */
    public YYFieldAccessExpression(IDetailedLocationContext cxt,
            IClassInfo cls, String fldname) {
        super(cxt);
        this.target = null;
        this.declCls = cls;
        this.fieldName = fldname;
        this.targetType = TYPE;
    }

    public void resolve(boolean isSubexpression) throws ParseException {
        if (unresolvedTargetName != null) {
            // JLS 6.5.5.2
            Object o = unresolvedTargetName.reclassify();
            if (o instanceof String) {
                reportError("Class or interface " + o + " not found");
            } else if (o instanceof IClassInfo) { // static field
                this.declCls = (IClassInfo)o;
                this.target = null;
                this.targetType = TYPE;
            } else if (o instanceof YYFieldAccessExpression) {
                this.target = (YYFieldAccessExpression)o;
                this.targetType = EXPRESSION;
            } else {
                throw new RuntimeException();
            }
            unresolvedTargetName = null;
        }

        YYClass myclass = getCurrentClass();
        String mypkg = myclass.getPackageName();

        // determine declaring class
        if (target != null) {
            target.resolve(true);
            declCls = target.getExpressionType();
            addExceptions(target.getExceptionsThrown());
        } else {
            exceptions = new HashMap<IClassInfo, YYStatement>();
        }
        switch (targetType) {
        case THIS:
        case SUPER:
            break; // declaring class already fetched
        case EXPRESSION:
            // is the target's class accessible?
            if (!declCls.isAccessibleTo(mypkg)) {
                reportError(declCls.toString() + " to which the field " +
                    fieldName + " belongs is not accessible from " + myclass);
            }
            break;
        case NONE:
            declCls = myclass;
            break;
        case TYPE:
            break; // it has been already assigned
        default:
            throw new RuntimeException();
        }

        // is that a reference type?
        if (!declCls.isReference()) {
            reportError("Attempt to reference field " + fieldName + " in a " +
                declCls);
        }

        // we got declaring class and field name; time to get the field
        Iterator<? extends IFieldInfo> i = declCls.getFields(fieldName).values().iterator();

        if (!i.hasNext()) {
            reportError("No variable " + fieldName + " defined in " + declCls);
        }
fields:
        while (i.hasNext()) {
            IFieldInfo fld = i.next();

            // JLS 6.6.1 - is field accessible?
            if (!classMgr.isAccessibleTo(fld, myclass,
                    targetType != EXPRESSION && targetType != TYPE)) {
                if (classMgr.getSettings().strictAccess()) {
                    continue fields;
                }
            }

            // so, the field is accessible
            if (field == null) {
                field = fld;
            } else {
                reportError("Reference to " + fieldName + " is ambigious. " +
                    "It is defined in " + field.getDeclaringClass() + " and " +
                    fld.getDeclaringClass());
            }
        }
        if (field == null) {
            // there are never more than one inaccessible field with this
            // name, since all but one multiply inherited fields must
            // originate from superinterfaces and be therefore public
            reportError("Variable " + fieldName + " defined in " + declCls +
                " is not accessible from " + myclass);
        } else {
            this.expressionType = field.getType();
        }

        // JLS 15.10.1-2, 6.5.5.2
        if (!Modifier.isStatic(field.getModifiers())) { // instance variable
            switch (targetType) {
            case NONE:
                int enclType = getCurrentMember().getScopeType();
                if ((enclType & ~IScope.INSTANCE_CONTEXT) == 0) {
                    break;
                } // else continue body and report error
            case TYPE:
                reportError("Can't make static reference to nonstatic " +
                    "variable " + fieldName + " in " + declCls);
            case EXPRESSION:
                // computing target reference may result in a NullPointerExc.
                addException(classMgr.NullPointerException);
            }
        }

        // the only array field 'length' is handled other way
        if (!isArrayLength()) {
            classidx = registerClass(declCls, false);
            fldidx = registerField(classidx, this.field);
        }
    }

    public YYExpression getTarget() {
        return target;
    }

    public IFieldInfo getField() {
        return field;
    }

    public int getFieldIdx() {
        return fldidx;
    }

    public int getClassIdx() {
        return classidx;
    }

    public boolean isArrayLength() {
        return field.getDeclaringClass().isArray() &&
               field.getName().equals("length");
    }

    public boolean isVariable() throws CompileException {
        return !Modifier.isFinal(field.getModifiers());
    }

    public boolean isInstanceField() {
        return !Modifier.isStatic(field.getModifiers());
    }

    public boolean canUseJNIThis() {
        return (isInstanceField() && (
                   targetType == THIS || targetType == SUPER ||
                   targetType == NONE || target instanceof YYThis)) ||
               (!isInstanceField() && (
                   field.getDeclaringClass() == getCurrentClass()));
    }

    public int write(IWriter w, int param) throws IOException {
        return w.write(this, param);
    }

    class DumpIterator implements Iterator<YYNode> {
        boolean targetReturned;
        DumpIterator() { targetReturned = (target == null); }
        public boolean hasNext() { return !targetReturned; }
        public YYNode next() {
            if (!targetReturned) { targetReturned = true; return target; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator<YYNode> getDumpIterator() { return new DumpIterator(); }

    public String toString() { return field.toString(); }
}
