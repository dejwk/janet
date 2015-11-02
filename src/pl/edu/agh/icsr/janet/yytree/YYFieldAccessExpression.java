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
            exceptions = new HashMap();
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
        Iterator i = declCls.getFields(fieldName).values().iterator();

        if (!i.hasNext()) {
            reportError("No variable " + fieldName + " defined in " + declCls);
        }
fields:
        while (i.hasNext()) {
            IFieldInfo fld = (IFieldInfo)i.next();

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

    class DumpIterator implements Iterator {
        boolean targetReturned;
        DumpIterator() { targetReturned = (target == null); }
        public boolean hasNext() { return !targetReturned; }
        public Object next() {
            if (!targetReturned) { targetReturned = true; return target; }
            return null;
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Iterator getDumpIterator() { return new DumpIterator(); }

    public String toString() { return field.toString(); }
}
