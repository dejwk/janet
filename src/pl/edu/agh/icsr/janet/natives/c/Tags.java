/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives.c;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import pl.edu.agh.icsr.janet.CompileException;
import pl.edu.agh.icsr.janet.ParseException;
import pl.edu.agh.icsr.janet.reflect.ClassManager;
import pl.edu.agh.icsr.janet.reflect.IClassInfo;
import pl.edu.agh.icsr.janet.yytree.YYCastExpression;
import pl.edu.agh.icsr.janet.yytree.YYExpression;
import pl.edu.agh.icsr.janet.yytree.YYLocalVariableAccessExpression;
import pl.edu.agh.icsr.janet.yytree.YYNativeMethodImplementation;
import pl.edu.agh.icsr.janet.yytree.YYStatement;
import pl.edu.agh.icsr.janet.yytree.YYStringLiteral;
import pl.edu.agh.icsr.janet.yytree.YYVariableDeclarator;

/**
 * Convenience class to group all Tags classes.
 */
class Tags {
    private Tags() {}

    /**
     * Attached to the block
     */
    static class DeclarationTag {
        private DeclarationTag parent;
        private FunctionDeclarationTag main;
        Vector<VariableTag> myVariables;
        //TreeMap myVariables;
        boolean usesLocalExceptions;
        private boolean requiresTryClause;
        private boolean requiresDestructClause;
        private YYStatement relatedStatement;
        //int vnum;

        private int abruptingChildren;
        boolean abruptsUsed = false;

        DeclarationTag(FunctionDeclarationTag ftag, DeclarationTag parent,
                       YYStatement relatedStatement) {
            this.main = ftag;
            this.parent = parent;
            this.relatedStatement = relatedStatement;
            myVariables = new Vector<VariableTag>();
            //myVariables = new TreeMap();
        }

        public void setUsesLocalExceptions() { usesLocalExceptions = true; }
        public void setUsesAbrupts() { abruptsUsed = true; }
        public void setRequiresTryClause() { requiresTryClause = true; }
        public boolean requiresTryClause() { return requiresTryClause; }
        public void setRequiresDestructClause() { requiresDestructClause = true; }
        public boolean requiresDestructClause() { return requiresDestructClause; }
        public Collection<VariableTag> getVariables() { return myVariables; }
        public FunctionDeclarationTag getMain() { return main; }
        public DeclarationTag getParent() { return parent; }

        VariableTag addVariable(VariableTag tag) {
            main.addVariable(tag);
            myVariables.add(tag);
    /*        if (tag.mustBeReleased()) {
                setRequiresFinally();
                variablesToRelease.add(tag);
                requiresFinally = true;
            }*/
            return tag;
        }

        void finalCheck() {
            /* determine if exceptions are thrown from here or
               come unhandled from inside of here */
            if (!relatedStatement.getExceptionsThrown().isEmpty() ||
                    abruptsUsed || // explicit returns or throws
                    abruptingChildren > 0) {
                /* if some variables need releasing, the try/destruct required */
                if ((abruptingChildren > 0 || usesLocalExceptions) && parent != null) {
                    for (int i=0, len = myVariables.size(); i<len; i++) {
                        // Even in C++, when we are declaring a destructor for a variable,
                        // we need to put it in a new exception context, to capture any
                        // longjmps.
                        if (myVariables.get(i).mustBeReleased()) {
    //                for (Iterator i = variablesIterator(); i.hasNext();) {
    //                    if (((VariableTag)i.next()).mustBeReleased()) {
                            requiresTryClause = true;
                            requiresDestructClause = true;
                            break;
                        }
                    }
                    if (requiresDestructClause) requiresTryClause = true;
                }
                if (usesLocalExceptions) {
                    requiresTryClause = true;
                }
                if (!requiresTryClause) { // delegate exceptions to the parent
                    if (parent != null) {
                        ++parent.abruptingChildren;
                    } else {
                        if (usesLocalExceptions || abruptingChildren > 0) {
                            requiresTryClause = true; // I am top-level and must handle
                        }
                    }
                }
                main.usesExceptions = true;
            }
        }

        boolean isInLocalExceptionScope() {
            return (requiresTryClause ||
                (parent != null && parent.isInLocalExceptionScope()));
        }

        // Note: we need to make sure that finally clauses are executed even in case of
        // an early return.
        boolean isInLocalReturnScope() {
            return (requiresTryClause ||
                (parent != null && parent.isInLocalReturnScope()));
        }

    /*
        boolean isOutermostPropagator() {
            if (parent != null && parent.isOutermostPropagator()) return false;
            return requiresTryClause;
        }
    /*
        public void writeDestructor(Writer w) throws IOException {
            for (int i=0, len = myVariables.size(); i<len; i++) {
                VariableTag v = (VariableTag)myVariables.get(i);
                if (v.mustBeReleased()) {
                    w.write("\n" + w.makeIndent() +
                          v.getVariableRelease() + ";");
                }
            }
        }
    */
    /*    public void writeInitializations(Writer w) {} */
    /*    public Iterator variablesIterator() {
            return myVariables.iterator();
        }*/

        // Only variables local to this declaration unit (with destructors here)
        private class MyVariablesIterator implements Iterator<VariableTag> {
            Iterator<VariableTag> itr;
            VariableTag currentTag;

            MyVariablesIterator() {
                itr = myVariables.iterator();
                fetchNextLocally();
            }

            public boolean hasNext() {
                return (currentTag != null);
            }

            public VariableTag next() {
                VariableTag ret = currentTag;
                fetchNextLocally();
                return ret;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void fetchNextLocally() {
                do {
                    if (itr.hasNext()) {
                        currentTag = itr.next();
                    } else {
                        currentTag = null;
                    }
                } while(currentTag != null && !currentTag.mustBeReleased());
            }
        }

        public Iterator<VariableTag> variablesIterator() {
            return new MyVariablesIterator();
        }
    }

    static class FunctionDeclarationTag {
        IClassInfo thisclass;
        TreeMap<String, VariableTag> variables;
        private VariableTag javaThisVariableTag;
        int maxMultiRefsUsed;
        boolean usesExceptions;
        boolean usesPrimitiveTypeArrays;
        DeclarationTag myDeclarationTag;

        FunctionDeclarationTag(YYNativeMethodImplementation nimpl,
                ClassManager classMgr) {
            this.thisclass = nimpl.getDeclaringClass();
            this.usesPrimitiveTypeArrays = nimpl.usesPrimitiveTypeArrays();
            variables = new TreeMap<String, VariableTag>();
        }

        public void setVariableForThis() {
            if (javaThisVariableTag == null) {
                javaThisVariableTag = new VariableTag(thisclass, "___", "this", false);
                addVariable(javaThisVariableTag);
                javaThisVariableTag.setTypeMultiRef();
            }
        }

        public void setDeclarationTag(DeclarationTag dt) {
            myDeclarationTag = dt;
        }

        public DeclarationTag getDeclarationTag() {
            return myDeclarationTag;
        }

        public VariableTag getVariableForThis() { return javaThisVariableTag; }

        VariableTag addVariable(VariableTag tag) {
            VariableTag first, last;
            first = variables.get(tag.basename);
            if (first != null) {
                last = first.prev; // cyclic list
                tag.idx = last.idx + 1;
                // attach to the bidirectional cyclic list
                tag.next = first;
                tag.prev = last;
                last.next = first.prev = tag;
            } else { // empty
                variables.put(tag.basename, tag);
                tag.next = tag.prev = tag;
            }
            tag.fdeclTag = this;
            return tag;
        }
    /*
        public void writeDestructor(Writer w) throws IOException {
            myDeclarationTag.writeDestructor(w);
        }*/

        public boolean requiresDestructClause() {
            return myDeclarationTag.requiresDestructClause();
        }

        public Iterator<VariableTag> variablesIterator() {
            return new MyVariablesIterator();
        }

        // only NOT handled by appropriate blocks
        private class MyVariablesIterator implements Iterator<VariableTag> {
            Iterator<VariableTag> itr1;//, itr2;
            //boolean finishedWithOwn;

            VariableTag currentTag;
            VariableTag currentGuardTag;

            MyVariablesIterator() {
                itr1 = variables.values().iterator();
                //itr2 = myDeclarationTag.variablesIterator();
                fetchNextLocally();
            }

            public boolean hasNext() {
                return (currentTag != null);
            }

            public VariableTag next() {
                VariableTag ret = currentTag;
                fetchNextLocally();
                return ret;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void fetchNextLocally() {
    //            if (!finishedWithOwn) {
                    do {
                        if (currentTag == null || currentTag.next == currentGuardTag) { // get next serie
                            if (itr1.hasNext()) {
                                currentTag = currentGuardTag = itr1.next();
                            } else {
                                currentTag = null;
                                break;
                            }
                        } else {
                            currentTag = currentTag.next;
                        }
                    } while (currentTag != null && currentTag.mustBeReleased());
    /*                if (currentTag == null) {
                        finishedWithOwn = true;
                        fetchNextLocally();
                    }
                } else if (itr2.hasNext()) {
                    currentTag = (VariableTag)itr2.next();
                } else {
                    currentTag = null;
                }*/
            }
        }
    }



    /**
     * Represents local variable needed in C code to accompany some Java
     * expression or statement.
     * It is often connected to the ExpressionTag to indicate that the
     * expression needs local variable. It is often connected to the
     * VariableDeclarators used in native code.
     */
    static final class VariableTag {
        int idx = -1;
        String basename;
        VariableTag prev, next;
        //protected FunctionDeclarationTag fdeclTag;
        protected FunctionDeclarationTag fdeclTag;

        IClassInfo cls;
        int type = SIMPLE;

        public static final int SIMPLE           = 1;
        public static final int MULTIREF         = 2;
        public static final int LOCAL_VARIABLE   = 3;

        static String typeToNameSuffix(IClassInfo cls) {
            String s = "";
            while (cls.isArray()) {
                s += "Arr";
                try {
                    cls = cls.getComponentType();
                } catch (CompileException e) { throw new RuntimeException(); }
            }
            if (cls.isPrimitive()) {
                s += cls.getSignature();
            } else {
                s += "Obj";
            }
            return s;
        }

        VariableTag(IClassInfo cls) {
            this(cls, null, null);
        }

        VariableTag(IClassInfo cls, String prefix, String name) {
            this(cls, prefix, name, name == null);
        }

        VariableTag(IClassInfo cls, String prefix, String name, boolean forcenum) {
            basename = "_janet_" +
                       (prefix != null ? prefix : "aux") +
                       typeToNameSuffix(cls) +
                       (name == null ? (forcenum ? "_" : "") : "_" + name);
            if (forcenum) idx = 1;
            this.cls = cls;
        }

        public String getName() {
            if (fdeclTag == null) throw new IllegalStateException();
            return basename + (idx >= 0 ? Integer.toString(idx) : "");
        }

        public IClassInfo getType() { return cls; }

        public boolean useMultiRef() { return type != SIMPLE; }

        public VariableTag setTypeMultiRef() {
            switch(type) {
            case SIMPLE:
                if (cls.isReference()) {
                    type = MULTIREF;
                    fdeclTag.maxMultiRefsUsed++;
                }
                break;
            case MULTIREF:
                break;
            default:
                throw new IllegalStateException();
            }
            return this;
        }

        public VariableTag setTypeLocalVariable() {
            switch(type) {
            case SIMPLE:
                if (cls.isReference()) {
                    type = LOCAL_VARIABLE;
                    fdeclTag.maxMultiRefsUsed++;
                }
                break;
            case MULTIREF:
                type = LOCAL_VARIABLE;
                break;
            case LOCAL_VARIABLE:
                break;
            default:
                throw new IllegalStateException();
            }
            return this;
        }

        boolean mustBeReleased() {
            return (type == LOCAL_VARIABLE);
        }

        String getUseInternal(boolean wantMultiRef) {
            if (!wantMultiRef) {
                return type == SIMPLE ? getName() : "_JANET_DEREF(" + getName() + ")";
            } else {
                if (type == SIMPLE && !cls.isPrimitive()) {
                    /* for primitives just ignore */
                    throw new IllegalStateException();
                }
                return getName();
            }
        }

        String getUse(boolean wantMultiRef) {
            String cast = "";
            if (!wantMultiRef && type != SIMPLE) {
                String jnitype = getType().getJNIType();
                if (!jnitype.equals("jobject")) {
                    cast = "(" + jnitype + ")";
                }
            }
            return cast + getUseInternal(wantMultiRef);
        }

        String getDeclaration() {
            String vl = fdeclTag.usesExceptions ? "volatile " : "";
            if (type == SIMPLE) {
                return (cls.isReference() ? vl : "") +
                       cls.getJNIType() + " " +
                       getName() + " = 0;";
            } else {
                return "_janet_multiref *" + vl + getName() + " = 0;";
            }
        }

        String getVariableAssignmentPrefix(boolean assigningMultiRef) {
            switch (type) {

            case SIMPLE:
                if (!cls.isReference()) {
                    return getName() + " = ";//((" + cls.getJNIType() + ")";
                }
                if (assigningMultiRef) throw new IllegalStateException();
                return "_JANET_ASSIGN_SIMPLE2SIMPLE(" + getName() + ", ";

            case MULTIREF:
                return !assigningMultiRef
                    ? "_JANET_ASSIGN_SIMPLE2MULTI(" + getName() + ", "
                    : "_JANET_ASSIGN_MULTI2MULTI(" + getName() + ", ";

            case LOCAL_VARIABLE:
                return !assigningMultiRef
                    ? "_JANET_ASSIGN_SIMPLE2LOCV(" + getName() + ", "
                    : "_JANET_ASSIGN_MULTI2LOCV(" + getName() + ", ";

            default:
                throw new RuntimeException();
            }
        }

        String getVariableAssignmentSuffix(boolean assigningMultiRef) {
            switch (type) {

            case SIMPLE:
                if (assigningMultiRef && !cls.isPrimitive()) {
                    throw new IllegalStateException();
                }
                return !cls.isReference() ? "" : ")";

            case MULTIREF: return ")";

            case LOCAL_VARIABLE: return ")";

            default:
                throw new RuntimeException();
            }
        }

        String getVariableRelease() {
            return (type != LOCAL_VARIABLE)
                ? null
                : "_JANET_DEC_MULTIREF(" + getName() + ")";
        }

        String getVariableCppDeleter() {
            return (type != LOCAL_VARIABLE)
                ? null
                : "_JANET_DECLARE_LOCV_DELETER(" + getName() + ")";
        }

        String getCommonMacroSuffix() {
            return type == SIMPLE
                ? ""
                : "_MULTIREF";
        }
    }


    /**
     * General tag attached to expressions. Often carry variable tag to
     * indicate that the expression needs local variable.
     */

    static class ExpressionTag {
        VariableTag variable;
        IClassInfo owntype;
        IClassInfo casttype;

        static boolean typeJNIequals(IClassInfo t1, IClassInfo t2) {
            if (t1 == t2) {
                return true;
            } else if (t1.isPrimitive() || t2.isPrimitive()) {
                return false;
            } else {
                return t1.getJNIType().equals(t2.getJNIType());
            }
        }

        /**
         * Every complex ExpressionTag (except e.g. literals) needs evaluation
         */
        ExpressionTag() {
            this(null);
        }

    /*    ExpressionTag(Expression e) {
            super(e, null);
        }*/

    /*    ExpressionTag(VariableTag variable) {
            super(null, variable, true);
        }*/

        ExpressionTag(YYExpression e/*, VariableTag variable*/) {
    //        this.needsEvaluation = false;
    /*        if (variable != null) {
                this.variable = variable;
            }*/
            if (e != null) {
                this.owntype = e.getExpressionType();
                this.casttype = e.getImplicitCastType();
                if (this.casttype == null) this.casttype = this.owntype;
            }/* else if (this.variable != null) {
                this.casttype = variable.getType();
            }*/
        }

        public VariableTag getVariableTag() { return variable; }

        public VariableTag assignVariableTag() {
            if (variable != null) throw new IllegalStateException();
            if (this.casttype == null) throw new IllegalStateException();
            variable = new VariableTag(this.casttype);
            return variable;
        }

        /**
         * Resolve to the JNI value (JVM reference or primitive type) carrying
         * the result of the expression.
         */
        String getUse() { return getUse(false); }

        /**
         * 1. When multiRef is false:
         * Resolve to the JNI value (JVM reference or primitive type) carrying
         * the result of the expression.
         * 2. When multiref is true:
         * Resolve to the Janet variable reference carrying the result of the
         * expression.
         */
        String getUse(boolean multiRef) {
            if (variable == null) {
                throw new IllegalStateException();
            }
            return variable.getUse(multiRef);
        }

        /**
         * Returns the prefix to be added before the generated evaluation
         * expression.
         */
        String getEvaluationPrefix() { return getEvaluationPrefix(false); }

    /*    String getEvaluationPrefix(boolean multiRef) {
            return getEvaluationPrefix(multiRef, false);
        }*/

        protected String getCastPrefix(boolean wantMultiRef) {
            if (wantMultiRef && getVariableTag() != null && getVariableTag().useMultiRef()) {
                return "";
            } else if (casttype != null && !typeJNIequals(casttype, owntype)) {
                return "(" + casttype.getJNIType() + ")";
            } else {
                return "";
            }
        }


        String getEvaluationPrefix(boolean multiRef) {
            return (variable == null
                      ? ""
                      : variable.getVariableAssignmentPrefix(multiRef)) +
                   getCastPrefix(multiRef);
            }

        /**
         * Returns the suffix to be added after the generated evaluation
         * expression.
         */
        String getEvaluationSuffix() { return getEvaluationSuffix(false); }

    /*    String getEvaluationSuffix(boolean multiRef) {
            return getEvaluationSuffix(multiRef, false);
        }*/

        String getEvaluationSuffix(boolean multiRef) {
            return (variable == null)
                ? ""
                : variable.getVariableAssignmentSuffix(multiRef);
        }

        /**
         * Returns true if this expression needs to be evaluated before its
         * result may be used (via getUse())
         */
        boolean needsEvaluation() { return getVariableTag() != null; }
    }

    static class ExpressionTagForVariableAccess extends ExpressionTag {
        private YYVariableDeclarator varDecl;
        String nname;

        public ExpressionTagForVariableAccess(YYLocalVariableAccessExpression e,
                YYVariableDeclarator varDecl, String nname) {
            super(e);
            this.varDecl = varDecl;
            try {
                this.owntype = varDecl.getType();
            } catch (ParseException ex) { throw new RuntimeException(); }
            this.nname = nname;
        }

        public VariableTag getVariableTag() { return (VariableTag)varDecl.tag; }

        public VariableTag assignVariableTag() {
            throw new UnsupportedOperationException();
        }

        String getUse(boolean wantMultiRef) {
            String s;
            if (varDecl.tag != null) {
                s = ((VariableTag)varDecl.tag).getUse(wantMultiRef);
            } else {
                s = nname;
            }
            return getCastPrefix(wantMultiRef) + s;
        }

        public boolean needsEvaluation() { return false; }

        String getEvaluationPrefix(boolean evaluatingMultiRef) {
            String s;
            if (varDecl.tag != null) {
                VariableTag tag = (VariableTag)varDecl.tag;
                s = tag.getVariableAssignmentPrefix(evaluatingMultiRef);
            } else {
                s = "";
            }
            return s + getCastPrefix(evaluatingMultiRef);
        }

        String getEvaluationSuffix(boolean evaluatingMultiRef/*, boolean localvar*/) {
            if (varDecl.tag != null) {
                VariableTag tag = (VariableTag)varDecl.tag;
                return tag.getVariableAssignmentSuffix(evaluatingMultiRef);
            } else {
                return "";
            }
        }

    }

    /**
     * The tag for 'this' and 'super'
     */
    static class ThisTag extends ExpressionTag {

        FunctionDeclarationTag ftag;

        ThisTag(FunctionDeclarationTag ftag) {
            super();
            this.ftag = ftag;
        }

        String getUse(boolean multiRef) {
            return ftag.getVariableForThis() == null
                ? "_janet_jthis"
                : ftag.getVariableForThis().getUse(multiRef);
        }
    }

    /**
     * Class for non-string literals
     */
    static class ExpressionTagForLiterals extends ExpressionTag {
        private String litValue;
        ExpressionTagForLiterals(YYExpression e, String litConvertedToC) {
            super(e);
            litValue = "((" + owntype.getJNIType() + ")" + litConvertedToC + ")";
        }

        String getUse(boolean multiRef) {
            return getCastPrefix(multiRef) + litValue;
        }

        // never needs evaluation, so parent method is OK
    }

    static class ExpressionTagForStringLiteral extends ExpressionTag {
        int stridx;
        ExpressionTagForStringLiteral(YYStringLiteral s) {
            super(s);
            stridx = s.getStringIdx();
        }
        public String getUse(boolean wantMultiRef) {
            if (variable != null) {
                return variable.getUse(wantMultiRef);
            } else {
                if (wantMultiRef) throw new RuntimeException();
                return "_janet_strings[" + stridx + "].strref";
            }
        }

        // never needs evaluation, so parent method is OK
    }

    static class OpaqueExpressionTag extends ExpressionTag {
        ExpressionTag origin;

        OpaqueExpressionTag(YYExpression e, ExpressionTag tag) {
            super(e);
            origin = tag;
        }

        public String getEvaluationPrefix(boolean assignMultiref) {
            return getCastPrefix(assignMultiref);
        }

        public String getEvaluationSuffix(boolean assignMultiref) {
            return "";
        }

        public String getUse(boolean wantMultiRef) {
            return getCastPrefix(wantMultiRef) + origin.getUse(wantMultiRef);
        }

        public VariableTag getVariableTag() { return origin.getVariableTag(); }
    }

    static class ExpressionTagForCast extends OpaqueExpressionTag {
        boolean forceEvaluation;

        ExpressionTagForCast(YYCastExpression e) {
            super(e, (ExpressionTag)e.getTarget().tag);
            this.forceEvaluation = e.requiresRuntimeCheck();
        }
        public boolean needsEvaluation() {
            return origin.needsEvaluation() || forceEvaluation;
        }
    }
}
