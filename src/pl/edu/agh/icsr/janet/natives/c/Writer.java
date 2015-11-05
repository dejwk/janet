/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives.c;

import pl.edu.agh.icsr.janet.natives.c.Tags.*;
import pl.edu.agh.icsr.janet.natives.IWriter;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.yytree.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class Writer implements IWriter {

    /**
     * Code generation consists of two phases: PREPARE and WRITE. During
     * PREPARE phase, the code is analysed to discover the local variables
     * needed to be generated etc. Thanks to that actual code generation
     * is a linear process.
     *
     * Methods int write(..., int param) are responsible for code generation.
     * 'param' as well as returned int value are the superposition of the
     * logical flags described below.
     */

    /**
     * determine the code generation phase.
     * (in)
     */
    public static final int PHASE_PREPARE       = 0x00000001;
    public static final int PHASE_WRITE         = 0x00000002;

    /**
     * States that the result of the expression, after once evaluated, will
     * then be used later. In many cases it means that the result should
     * be stored in temporary local variable.
     * (IN, phase: PREPARE)
     */
    public static final int REUSABLE            = 0x00000004;

    public static final int MULTIREF            = 0x00000008;

    /**
     * States that the result of the expression will not be
     * used in that place but expression should be evaluated just in that
     * moment to keep appropriate evaluation order.
     * In many cases this will result in no code generated and returning
     * of the WROTE_NOTHING.
     * (IN, phase: WRITE)
     */
    public static final int EVALUATE_ONLY       = 0x00000010;

    /**
     * For field, array element and local variable accesses:
     * Below two denote that given expression appears as a left side of
     * assignment, so the prefix or suffix of the assignment expression
     * should be written.
     * (IN, phase: WRITE)
     */
    public static final int ASSIGNMENT_PREFIX   = 0x00000020;
    public static final int ASSIGNMENT_SUFFIX   = 0x00000040;

    //public static final int NEEDS_TYPE_CHECKING = 0x00000080;

    //public static final int BODY_PREFIX         = 0x00000100;
    //public static final int BODY_SUFFIX         = 0x00000200;

    public static final int COMPOUND_PREFIX        = 0x00000100;
    public static final int COMPOUND_SUFFIX        = 0x00000200;

    /**
     *
     */
    //public static final int IS_SUBSEQUENT       = 0x00000020;
    //public static final int WROTE_NOTHING       = 0x00000040;

    static final String janetHeader =
        "/**\n" +
        " * file:      %CIMPLFILENAME%\n" +
        " * basefile:  %BASEFILE%\n" +
        " * generated: %__DATE__%\n" +
        " */\n" +
        "\n" +
        "#include <janet.h>\n" +
        "\n";

    Janet.Settings settings;
    ClassManager classMgr;
    YYClass currCls;
    INativeMethodInfo currMth;
    File currOut;
    int currIndent;
    final static int tabSize = 4;
    java.io.Writer fileWriter;
    pl.edu.agh.icsr.janet.Writer.Substituter subst;
    String nlangName;

    FunctionDeclarationTag functionDclTag;
    DeclarationTag currentDclTag;
    //VariableTag javaThisVariableTag;
    //int maxMultiRefsUsed;

    public void init(Janet.Settings settings,
                     pl.edu.agh.icsr.janet.Writer.Substituter subst,
                     ClassManager classMgr,
                     String nlangName) {
        this.settings = settings;
        this.classMgr = classMgr;
        this.subst = subst;
        this.nlangName = nlangName;
    }

    public void write(String s) throws IOException {
        fileWriter.write(s);
    }

    public void openFileForClass(YYClass cls) throws IOException {
        if (currCls != cls) {
            if (fileWriter != null) {
                fileWriter.flush();
                fileWriter.close();
                fileWriter = null;
            }
            this.currCls = cls;
            String filename = ClassManager.mangle(
                //settings.getQnames() ? cls.getFullName() : cls.getSimpleName())
                cls.getSimpleName()) +
                (nlangName.equals("cplusplus") ? "Impl.cc" : "Impl.c");
            File dir = pl.edu.agh.icsr.janet.Writer.getOutDirForInput(cls.ibuf(), settings);

            currOut = new File(dir, filename);
            fileWriter = new BufferedWriter(new FileWriter(currOut));
            subst.setSubst("CIMPLFILENAME", filename);
            fileWriter.write(Janet.getGeneratedCodeLicense());
            fileWriter.write(subst.substitute(janetHeader));
            fileWriter.flush();
        }
    }

    public void write(INativeMethodInfo mth) throws IOException {
        YYClass cls = (YYClass)mth.getDeclaringClass();
        openFileForClass(cls);
        this.currMth = mth;
        YYNativeMethodImplementation nimpl = mth.getImplementation();
        nimpl.write(this, PHASE_PREPARE);

        YYVariableDeclarator[] parameters;
        if (nlangName.equals("cplusplus")) {
            write("\nextern \"C\"");
        }
        try {
            parameters = mth.getParameters();
            write("\n" + mth.getReturnType().getJNIType() +
                " Janet_" +
                ClassManager.mangle(cls.getFullName()) +
                    "_" + ClassManager.mangle(mth.getName()));

            if (NativeWriter.isNativeMethodOverridden(mth)) {
                write("__");
                if (parameters != null) {
                    for (int i=0; i<parameters.length; i++) {
                        write(ClassManager.mangle(parameters[i].getType().
                            getSignature()));
                    }
                }
            }

            write("(\n    JNIEnv* _janet_jnienv" +
                (!(mth.getUsedClassIdxs().isEmpty() &&
                        mth.getUsedFieldsIdxs().isEmpty() &&
                        mth.getUsedMethodsIdxs().isEmpty())
                    ? ",\n    _janet_cls* _janet_classes"
                    : "") +
                (!mth.getUsedFieldsIdxs().isEmpty()
                    ? ",\n    _janet_fld* _janet_fields"
                    : "") +
                (!mth.getUsedMethodsIdxs().isEmpty()
                    ? ",\n    _janet_mth* _janet_methods"
                    : "") +
                (!mth.getUsedStringsIdxs().isEmpty()
                    ? ",\n    _janet_str* _janet_strings"
                    : "") +
                (nimpl.usesPrimitiveTypeArrays()
                    ? ",\n    _janet_arrHashTable* _janet_arrhtable"
                    : "") +
                (nimpl.getSynchronizedStatementsNum() > 0
                    ? ",\n    jobject* _janet_monitors"
                    : "") +
                ",\n    " + (Modifier.isStatic(mth.getModifiers())
                                 ? "jclass _janet_jthisclass"
                                 : "jobject _janet_jthis"));

            for (int i=0; i<parameters.length; i++) {
                write(",\n" + "    " +
                    parameters[i].getType().getJNIType() + " " +
                    "_janet_arg_" + ClassManager.mangle(parameters[i].getName()));
            }

            write(")\n{ ");
            currIndent = tabSize;
            nimpl.write(this, PHASE_WRITE);
            currIndent = 0;
            write("\n}\n");

            fileWriter.flush();

        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    public void write(YYStaticNativeStatement stmt) throws IOException {
        openFileForClass(stmt.getDeclaringClass());
        stmt.getImplementation().write(this, PHASE_WRITE);
        fileWriter.write("\n");
        fileWriter.flush();
    }






    DeclarationTag begDeclarationTag(YYStatement s) {
        s.tag = this.currentDclTag = new DeclarationTag(this.functionDclTag,
                                                        this.currentDclTag, s);
        return this.currentDclTag;
    }

    void endDeclarationTag() {
        currentDclTag.finalCheck();
        currentDclTag = currentDclTag.getParent();
    }

    DeclarationTag getDeclarationTag(YYStatement s) {
        return (DeclarationTag)s.tag;
    }

    boolean writeDclUnitDeclarations(DeclarationTag dt) throws IOException {
        boolean hasVariables = false;
        for (Iterator i = dt.variablesIterator(); i.hasNext();) {
            VariableTag vtag = (VariableTag)i.next();
            hasVariables = true;
            cr(); write(vtag.getDeclaration());
        }
        return hasVariables;
    }

    void writeDclUnitBegin(DeclarationTag dt) throws IOException {
        if (dt.requiresTryClause()) {
            cr(); write("_JANET_EXCEPTION_CONTEXT_BEGIN");
            cr(); write("_JANET_TRY {");
            currIndent += tabSize;
        }
    }

    private void writeDclUnitEnd1(DeclarationTag dt) throws IOException {
        if (dt.requiresTryClause()) {
            currIndent -= tabSize;
            cr();
            if (dt.requiresDestructClause()) {
                write("} _JANET_DESTRUCT {");
                currIndent += tabSize;
            }
        }
    }

    private void writeDclUnitEnd2(DeclarationTag dt) throws IOException {
        if (dt.requiresTryClause()) {
            if (dt.requiresDestructClause()) {
                currIndent -= tabSize;
            }
            cr(); write("} _JANET_END_TRY;");
            cr(); write("_JANET_EXCEPTION_CONTEXT_END_");
            write(getLocalSuffix(dt.getParent()));
        }
    }


    void writeDclUnitEnd(DeclarationTag dt) throws IOException {
        writeDclUnitEnd1(dt);
        writeDclUnitDestructors(dt);
        writeDclUnitEnd2(dt);
    }

    boolean writeDclUnitDestructors(DeclarationTag dt) throws IOException {
        boolean hasVariables = false;
        for (Iterator i = dt.variablesIterator(); i.hasNext();) {
            VariableTag vtag = (VariableTag)i.next();
            hasVariables = true;
            cr(); write(vtag.getVariableRelease() + ";");
        }
        return hasVariables;
    }

    private String getLocalInfix(DeclarationTag dt) {
        return (dt != null && dt.isInLocalExceptionScope()) ? "LOCAL" : "GLOBAL";
    }

    private String getTypeSuffix() {
        try {
            return(currMth.getReturnType() == classMgr.VOID) ? "_V" : "_0";
        } catch (ParseException e) { throw new RuntimeException(); }
    }

    private String getLocalSuffix(DeclarationTag dt) {
        boolean isLocal = (dt != null && dt.isInLocalExceptionScope());
        if (isLocal) return "LOCAL";
        return "GLOBAL" + getTypeSuffix();
    }

/*    private String getPropagateMacro(DeclarationTag dt) {
        boolean isLocal = dt.isInLocalExceptionScope();

        String s = "_JANET_" +
                   (isLocal ?  "LOCAL" : "GLOBAL") +
                   "_PROPAGATE_PENDING";
        if (!isLocal) {
            boolean isVoid;
            try {
                isVoid = (this.currMth.getReturnType() == classMgr.VOID);
            } catch (CompileException e) { throw new RuntimeException(); }
            s += (isVoid ? "_V" : "_0");
        }
        return s + "()";
    }
*/
//    public int write(YYCBlock block, int param) {
//        block.
//    }

    public int write(YYNativeMethodImplementation nimpl, int param)
            throws IOException {

        int result;

        if ((param & PHASE_PREPARE) != 0) {

            currentDclTag = null;
            functionDclTag = new FunctionDeclarationTag(nimpl, this.classMgr);
            functionDclTag.setDeclarationTag(begDeclarationTag(nimpl));
            //writeDeclarationUnit(nimpl, param | BODY_PREFIX);
            result = nimpl.getStatements().write(this, param);
            //writeDeclarationUnit(nimpl, param | BODY_SUFFIX);
            endDeclarationTag();

/*            if (functionDclTag.usesPrimitiveTypeArrays) {
                YYVariableDeclarator[] params;
                try {
                    params = nimpl.getNativeMethodHeader().getParameters();
                    for (int i=0; i<params.length; i++) {
                        YYVariableDeclarator var = params[i];
                        IClassInfo type = var.getType();
                        if (!(type.isArray() && type.getComponentType().isPrimitive())) {
                            continue;
                        }
                        if (var.tag != null) continue;
                        var.tag = functionDclTag.addVariable(
                            new VariableTag(type, "arg", var.getName(), false));
                    }
                } catch (CompileException e) { throw new RuntimeException(); }
            }
*/
        } else {

            // if needed, declare structure for multirefs

            if (functionDclTag.maxMultiRefsUsed > 0) {
                cr(); write("_JANET_DECLARE_MULTIREFS(" +
                    functionDclTag.maxMultiRefsUsed + ");");
            }

            // if needed, declare auxiliary variables for exception handling
            // and early return

            if (functionDclTag.usesExceptions) {
                cr(); write("_JANET_DECLARE_LOCAL_ABRUPT_STATEMENTS");
                try {
                    IClassInfo rtype = currMth.getReturnType();
                    if (rtype == classMgr.VOID) {
                        write("_V;");
                    } else {
                        write("(" + rtype.getJNIType() + ");");
                    }
                } catch (ParseException e) { throw new RuntimeException(); }
            }

            // write variable declarations

            boolean hasVariables = false;
            for (Iterator i = functionDclTag.variablesIterator(); i.hasNext();) {
                VariableTag v = (VariableTag)i.next();
                //if (v.mustBeReleased()) continue;
                cr(); write(v.getDeclaration());
                hasVariables = true;
            }

            if (hasVariables) { cr(); }
            hasVariables = writeDclUnitDeclarations(functionDclTag.getDeclarationTag());
            if (hasVariables) { cr(); }

            // now, write the initializers
            boolean initializers = false;

            // if needed, initialize variable for embedded this

            VariableTag fthis = functionDclTag.getVariableForThis();
            if (fthis != null) {
                initializers = true;
                cr(); write(fthis.getDeclaration() + "\n");
                cr();
                write(fthis.getVariableAssignmentPrefix(false) + "_janet_jthis" +
                      fthis.getVariableAssignmentSuffix(false) + ";");
            }

            // if needed, initialize variables for multiref arguments

            YYVariableDeclarator[] params;
            try {
                params = nimpl.getNativeMethodHeader().getParameters();
            } catch (CompileException e) { throw new RuntimeException(); }
            for (int i=0; i<params.length; i++) {
                YYVariableDeclarator var = params[i];
                VariableTag tag = (VariableTag)var.tag;
                if (tag != null) {
                    initializers = true;
                    cr();
                    write(tag.getVariableAssignmentPrefix(false) +
                        "_janet_arg_" + var.getName() +
                        tag.getVariableAssignmentSuffix(false) + ";");
                }
            }

            DeclarationTag dt = getDeclarationTag(nimpl);
            if (initializers) { // we must start a new block
                openWriteContext("{ ");
            }
            writeDclUnitBegin(dt);
            result = nimpl.getStatements().write(this, param);
            writeDclUnitEnd(dt);
            //writeDeclarationUnit(nimpl, param | BODY_SUFFIX);
            if (initializers) { // we must start a new block
                closeWriteContext("}");
            }
        }
        return result;
    }

    public int write(YYNode node, int param) throws IOException {
        for(Iterator i = node.iterator(); i.hasNext();) {
            YYNode n = (YYNode)i.next();
            n.write(this, param);
        }
        return 0;
    }

    public int write(YYStatement s, int param) throws IOException {
        boolean writeParens = ((param & PHASE_WRITE) != 0 &&
            s.getScopeType() == IScope.BLOCK);
        if (writeParens) {
            openWriteContext("{ ");
        }
        write((YYNode)s, param);
        if (writeParens) {
            closeWriteContext("}");
        }
        return 0;
    }



    public int write(YYExpression expr, int param) throws IOException {
//        if (phase == PHASE_PREPARE) {
//            expr.tag = currentDclTag.addVariable(expr);
//        }
        return 0;//WROTE_NOTHING;
    }

    private void initExpressionTag(YYExpression e,
            ExpressionTag useThisExpressionTag, int param,
            boolean forceUseVariable) {
        ExpressionTag tag;
        tag = (useThisExpressionTag == null ? new ExpressionTag(e)
                                            : useThisExpressionTag);
        e.tag = tag;
        if (forceUseVariable || (param & REUSABLE) != 0) {
            VariableTag v = tag.assignVariableTag();
            currentDclTag.addVariable(v);
            if ((param & MULTIREF) != 0) v.setTypeMultiRef();
        }
    }





    private void writeJNIMethodCall(ExpressionTag tag, ExpressionTag tgttag,
        int invocation_mode,
        IClassInfo returntype, boolean useJNIThis, YYExpressionList arguments,
        int clsidx, int mthidx) throws IOException
    {
        write("(*_janet_jnienv)->Call");
        switch (invocation_mode) {
            case YYMethodInvocationExpression.IMODE_STATIC:
                write("Static");
                break;
            case YYMethodInvocationExpression.IMODE_NONVIRTUAL:
            case YYMethodInvocationExpression.IMODE_SUPER:
                write("Nonvirtual");
                break;
        }
        write(getJNITypeInfixName(returntype));
        openWriteContextInline("Method(");
        cr(); write("_janet_jnienv,");
        cr();
        if (invocation_mode != YYMethodInvocationExpression.IMODE_STATIC) {
            if (useJNIThis) {
                write("_janet_jthis");
            } else {
                write(tgttag.getUse());
            }
        } else {
            if (useJNIThis) {
                write("_janet_jthisclass");
            } else {
                write("_janet_classes[" + clsidx + "].id");
            }
        }

        switch (invocation_mode) {
            case YYMethodInvocationExpression.IMODE_NONVIRTUAL:
            case YYMethodInvocationExpression.IMODE_SUPER:
                write(",");
                cr(); write("_janet_classes[" + clsidx + "].id");
        }

        write(",");
        cr(); write("_janet_methods[" + mthidx + "].id");

        for (Iterator i = arguments.iterator(); i.hasNext();) {
            write(",");
            cr(); write((getTag((YYExpression)i.next())).getUse());
        }
        closeWriteContext(")");
    }




    public int write(YYMethodInvocationExpression e, int param)
            throws IOException {
        YYExpression target = e.getTarget();
        YYExpressionList args = e.getArguments();
        boolean isVoid = (e.getExpressionType() == classMgr.VOID);
        boolean useJNIThis = e.canUseJNIThis();

        if ((param & PHASE_PREPARE) != 0) {

            initExpressionTag(e, null, param, !isVoid);

            if (target != null && !useJNIThis) {
                target.write(this, PHASE_PREPARE + REUSABLE);
            }

            for (Iterator i = args.iterator(); i.hasNext();) {
                YYExpression expr = (YYExpression)i.next();
                expr.write(this, PHASE_PREPARE + REUSABLE);
            }

            currentDclTag.setUsesLocalExceptions();

        } else {

            ExpressionTag myTag = getTag(e);
            ExpressionTag tgtTag = (target != null ? getTag(target) : null);
            boolean first = true;
            int indent = currIndent;
            int invoc_mode = e.getInvocationMode();

            writeBegComment(e);
            openWriteContext("(");

            // write target reference, if required
            if (target != null && !useJNIThis) {
                ExpressionTag tag = getTag(target);
                if (tag.needsEvaluation()) {
                    target.write(this, PHASE_WRITE + EVALUATE_ONLY);
                    first = false;
                }
            }

            // write parameter evaluations
            for (Iterator i = args.iterator(); i.hasNext();) {
                YYExpression arg = (YYExpression)i.next();
                ExpressionTag tag = getTag(arg);
                if (tag.needsEvaluation()) {
                    if (!first) write(",");
                    arg.write(this, PHASE_WRITE + EVALUATE_ONLY);
                    first = false;
                }
            }
            if (!first) write(",");

            // ensure target is not null
            if (invoc_mode != YYMethodInvocationExpression.IMODE_STATIC &&
                    !useJNIThis) { // check for null target
                cr();
                write("_JANET_LOCAL_ENSURE_NOT_NULL(");
                write(tgtTag.getUse());
                write(",");
                cr();
                try {
                    write("    \"trying to invoke method " +
                        e.getMethod().getName() +
                        "(" + classMgr.getTypeNames(e.getMethod().getParameterTypes()) + ")" +
                        " on a null target reference\"),");
                } catch (ParseException exc) { throw new RuntimeException(); }
            }

            if (e.isStringLength()) {

                cr();
                write(myTag.getEvaluationPrefix());
                write("JNI_GET_STRING_LENGTH(" + tgtTag.getUse() + ")");
                write(myTag.getEvaluationSuffix());

            } else {

                // write JNI invocation
                cr();
                if (!isVoid) {
                    write(myTag.getEvaluationPrefix());
                }

                // optimization for final methods. We shouldn't do that here
                // as JVMspec says it should be done in the run time during
                // class linking, but we don't support it yet, and the
                // optimization is tempting as many JVMs perform virtual method
                // calls very poorly.
                if (Modifier.isFinal(e.getMethod().getModifiers()) &&
                        invoc_mode == YYMethodInvocationExpression.IMODE_VIRTUAL) {
                    invoc_mode = YYMethodInvocationExpression.IMODE_NONVIRTUAL;
                }

                writeJNIMethodCall(myTag, tgtTag, invoc_mode,
                    e.getExpressionType(), useJNIThis, args, e.getClassIdx(),
                    e.getMethodIdx());

                if (!isVoid) {
                    write(myTag.getEvaluationSuffix());
                }
                write(",");
                cr(); write("_JANET_LOCAL_HANDLE_EXCEPTION()");
            }

            // write resulting value
            if ((param & EVALUATE_ONLY) == 0) {
                write(","); cr();
                if (isVoid) {
                    write("((void)0)");
                }
                else {
                    write(myTag.getUse((param & MULTIREF) != 0));
                }
            }

            closeWriteContext(")");
            writeEndComment(e);
        }
        return 0;
    }

    public int write(YYClassInstanceCreationExpression e, int param)
            throws IOException {
        YYExpressionList args = e.getArguments();

        if ((param & PHASE_PREPARE) != 0) {

            initExpressionTag(e, null, param, true);

            for (Iterator i = args.iterator(); i.hasNext();) {
                YYExpression expr = (YYExpression)i.next();
                expr.write(this, PHASE_PREPARE + REUSABLE);
            }

            currentDclTag.setUsesLocalExceptions();

        } else {

            ExpressionTag myTag = getTag(e);
            boolean first = true;
            int indent = currIndent;

            writeBegComment(e);
            openWriteContext("(");

            // write memory allocation
            cr();
            write(myTag.getEvaluationPrefix(false));
            openWriteContextInline("JNI_ALLOC_OBJECT(");
            cr(); write("_janet_classes[" + e.getClassIdx() + "].id");
            closeWriteContext(")");
            write(myTag.getEvaluationSuffix(false));
            write(","); cr(); write("_JANET_LOCAL_HANDLE_EXCEPTION()");

            // write parameters evaluation
            for (Iterator i = args.iterator(); i.hasNext();) {
                YYExpression arg = (YYExpression)i.next();
                ExpressionTag tag = getTag(arg);
                if (tag.needsEvaluation()) {
                    write(",");
                    arg.write(this, PHASE_WRITE + EVALUATE_ONLY);
                }
            }
            write(",");

            // write JNI invocation
            cr();
            openWriteContextInline("(*_janet_jnienv)->CallNonvirtualVoidMethod(");
            cr(); write("_janet_jnienv,");
            cr(); write(getTag(e).getUse() + ",");
            cr(); write("_janet_classes[" + e.getClassIdx() + "].id,");
            cr(); write("_janet_methods[" + e.getMethodIdx() + "].id");
            for (Iterator i = args.iterator(); i.hasNext();) {
                write(","); cr();
                write((getTag((YYExpression)i.next())).getUse());
            }
            closeWriteContext(")");
            write(",");
            cr(); write("_JANET_LOCAL_HANDLE_EXCEPTION()");

            // write resulting value
            if ((param & EVALUATE_ONLY) == 0) {
                write(",");
                cr(); write(myTag.getUse((param & MULTIREF) != 0));
            }

            closeWriteContext(")");
            writeEndComment(e);

        }
        return 0;
    }

    private void writeFieldTargetEvaluation(YYFieldAccessExpression e,
        ExpressionTag myTag, boolean useJNIThis) throws IOException
    {
        // if required, target reference evaluation and null-check
        if (e.isInstanceField() && !useJNIThis) {
            YYExpression target = e.getTarget();
            ExpressionTag ttag = getTag(target);
            if (ttag.needsEvaluation()) {
                target.write(this, PHASE_WRITE + EVALUATE_ONLY);
                write(",");
            }

            cr(); write("_JANET_LOCAL_ENSURE_NOT_NULL(");
            write(getTag(e.getTarget()).getUse());
            write(","); cr();
            write("    \"trying to access field " +
                e.getField().getName() +
                " using a null target reference\"),");
        }
        cr();
    }

    private void writeFieldGetSet(YYFieldAccessExpression e,
        ExpressionTag myTag, boolean useJNIThis, boolean set) throws IOException
    {
        if (e.isArrayLength()) {
//                String tgt = useJNIThis ? "_janet_jthisclass"
//                                        : getTag(e.getTarget()).getUse();
            ExpressionTag tgtTag = getTag(e.getTarget());

//                if (e.getExpressionType().isPrimitive() &&
//                        functionDclTag.usesPrimitiveTypeArrays) {
            if (tgtTag.getVariableTag() != null && tgtTag.getVariableTag().useMultiRef()) {
                write("_JANET_MULTIARRAY_GET_LENGTH(" + tgtTag.getUse(true) + ")");
            } else {
                write("JNI_GET_ARRAY_LENGTH(" + tgtTag.getUse(false) + ")");
            }
        } else {

            // write JNI invocation

            openWriteContextInline("(*_janet_jnienv)->" +
                (set ? "Set" : "Get") +
                (e.isInstanceField() ? "" : "Static") +
                getJNITypeInfixName(e.getExpressionType()) +
                "Field(");
            cr(); write("_janet_jnienv,");
            cr();
            if (e.isInstanceField()) {
                if (useJNIThis) {
                    write("_janet_jthis");
                } else {
                    write(getTag(e.getTarget()).getUse());
                }
            } else {
                if (useJNIThis) {
                    write("_janet_jthisclass");
                } else {
                    write("_janet_classes[" + e.getClassIdx() + "].id");
                }
            }

            write(",");
            cr(); write("_janet_fields[" + e.getFieldIdx() + "].id");
            if (set) {
                write(","); cr(); write(myTag.getUse());
            }
            closeWriteContext(")");
        }
    }




    public int write(YYFieldAccessExpression e, int param) throws IOException {

        boolean useJNIThis = e.canUseJNIThis();

        if ((param & PHASE_PREPARE) != 0) {

            initExpressionTag(e, null, param, (param & MULTIREF) != 0);

            if (e.isInstanceField() && !useJNIThis) {
                e.getTarget().write(this, PHASE_PREPARE + REUSABLE);
                currentDclTag.setUsesLocalExceptions();
            }

        } else {

//            boolean first = true;
            ExpressionTag myTag = getTag(e);

            if ((param & ASSIGNMENT_PREFIX) != 0)
            {
                openWriteContext("(");
                writeFieldTargetEvaluation(e, myTag, useJNIThis);
                write(myTag.getEvaluationPrefix(true));
            }
            else if ((param & ASSIGNMENT_SUFFIX) != 0)
            {
                write(myTag.getEvaluationSuffix(true));
                write(",");
                cr(); writeFieldGetSet(e, myTag, useJNIThis, true);
                closeWriteContext(")");
            }
            else if ((param & COMPOUND_PREFIX) != 0)
            {
                openWriteContext("(");
                writeFieldTargetEvaluation(e, myTag, useJNIThis);
                write(myTag.getEvaluationPrefix(true));
                writeFieldGetSet(e, myTag, useJNIThis, false /* get */);
                write(myTag.getEvaluationSuffix(true));
                write(","); cr(); write(myTag.getUse(false));
                closeWriteContext(")");
            }
            else if ((param & COMPOUND_SUFFIX) != 0)
            {
                writeFieldGetSet(e, myTag, useJNIThis, true /* set */);
            }
            else
            {
                writeBegComment(e);
                openWriteContext("(");
                writeFieldTargetEvaluation(e, myTag, useJNIThis);
                // assignment of the result of Get<Type>Field -> never multiref
                write(myTag.getEvaluationPrefix(false));
                writeFieldGetSet(e, myTag, useJNIThis, false /* get */);
                write(myTag.getEvaluationSuffix(false));
                closeWriteContext(")");
                writeEndComment(e);
            }
        }
        return 0;
    }

    private int writeLiteral(YYExpression e, int param) throws IOException {
        if ((param & EVALUATE_ONLY) != 0) {
            return 0;
        } else {
            write(((ExpressionTag)e.tag).getUse());
            return 0;
        }
    }

    public int write(final YYIntegerLiteral e, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            String cval = Integer.toString(e.getValue());
            e.tag = new ExpressionTagForLiterals(e, cval);
        } else {
            writeLiteral(e, param);
        }
        return 0;
    }

    public int write(final YYLongLiteral e, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            String cval = Long.toString(e.getValue());
            e.tag = new ExpressionTagForLiterals(e, cval);
        } else {
            writeLiteral(e, param);
        }
        return 0;
    }

    public int write(final YYCharacterLiteral e, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            String cval = "0x" + Integer.toHexString(e.getValue());
            e.tag = new ExpressionTagForLiterals(e, cval);
        } else {
            writeLiteral(e, param);
        }
        return 0;
    }

    public int write(final YYFloatLiteral e, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            float f = e.getValue();
            String cval;
            if (Float.isNaN(f)) {
                cval = "(0.0/0.0)";
            } else if (f == Float.NEGATIVE_INFINITY) {
                cval = "(-1.0/0.0)";
            } else if (f == Float.POSITIVE_INFINITY) {
                cval = "(1.0/0.0)";
            } else {
                cval = Float.toString(f);
            }
            e.tag = new ExpressionTagForLiterals(e, cval);
        } else {
            writeLiteral(e, param);
        }
        return 0;
    }

    public int write(final YYDoubleLiteral e, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            double f = e.getValue();
            String cval;
            if (Double.isNaN(f)) {
                cval = "(0.0/0.0)";
            } else if (f == Double.NEGATIVE_INFINITY) {
                cval = "(-1.0/0.0)";
            } else if (f == Double.POSITIVE_INFINITY) {
                cval = "(1.0/0.0)";
            } else {
                cval = Double.toString(f);
            }
            e.tag = new ExpressionTagForLiterals(e, cval);
        } else {
            writeLiteral(e, param);
        }
        return 0;
    }

    public int write(final YYBooleanLiteral e, int param) throws IOException {
        if ((param & (PHASE_PREPARE)) != 0) {
            String cval = e.getValue() ? "JNI_TRUE" : "JNI_FALSE";
            e.tag = new ExpressionTagForLiterals(e, cval);
        } else {
            writeLiteral(e, param);
        }
        return 0;
    }

    public int write(final YYNullLiteral e, int param) throws IOException {
        if ((param & (PHASE_PREPARE)) != 0) {
            e.tag = new ExpressionTag() {
                String getUse(boolean multiRef) { return "0"; }
            };
        } else {
            writeLiteral(e, param);
        }
        return 0;
    }

    public int write(YYStringLiteral e, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {

            param &= ~REUSABLE; // to avoid variable creation by initExpressionTag()
            ExpressionTag tag = new ExpressionTagForStringLiteral(e);
            initExpressionTag(e, tag, param, (param & MULTIREF) != 0);
            return param & MULTIREF;
        } else {
            ExpressionTag myTag = getTag(e);
            if ((param & EVALUATE_ONLY) != 0) {
                if (myTag.needsEvaluation()) { // just write it
                    write(e, param & ~EVALUATE_ONLY);
                }
            } else {
                writeBegComment(e);
                openWriteContext("(");
                cr(); write(myTag.getEvaluationPrefix(false));
                write("_janet_strings[" + e.getStringIdx() + "].strref");
                write(myTag.getEvaluationSuffix(false));
                closeWriteContext(")");
                writeEndComment(e);
            }
        }
        return 0;
    }


    public int write(final YYThis e, int param) throws IOException {
        if ((param & (PHASE_PREPARE)) != 0) {
            if ((param & MULTIREF) != 0) {
                functionDclTag.setVariableForThis();
            }

            e.tag = new ThisTag(functionDclTag);
            return (functionDclTag.getVariableForThis() == null) ? 0 : MULTIREF;
        } else {
            if ((param & EVALUATE_ONLY) != 0) {
                return 0;
            } else {
                write(((ExpressionTag)e.tag).getUse((param & MULTIREF) != 0));
                return 0;
            }
        }
    }

    public int write(YYInstanceOfExpression e, int param) throws IOException {
        if ((param & (PHASE_PREPARE)) != 0) {

            initExpressionTag(e, null, param, false);
            e.getTarget().write(this, PHASE_PREPARE);
            return 0;

        } else {

            ExpressionTag myTag = getTag(e);

            //if ((param & IS_SUBSEQUENT) != 0) write(",");
            writeBegComment(e);
            cr();

            write(myTag.getEvaluationPrefix());
            if (e.needsOnlyCheckForNull()) {
                openWriteContextInline("((jboolean)(");
                e.getTarget().write(this, PHASE_WRITE);
                cr(); write("? JNI_TRUE : JNI_FALSE)");
                closeWriteContext(")");
            } else {
                openWriteContextInline("_JANET_IS_INSTANCE_OF(");
                e.getTarget().write(this, PHASE_WRITE);
                write(",");
                cr(); write("_janet_classes[" + e.getClassIdx() + "].id\n");
                closeWriteContext(")");
            }
            write(myTag.getEvaluationSuffix());
            writeEndComment(e);

            return 0;
        }
    }

    public int write(YYRelationalExpression e, int param) throws IOException {

        YYExpression e1 = e.getLeftExpression();
        YYExpression e2 = e.getRightExpression();

        if ((param & (PHASE_PREPARE)) != 0) {

            initExpressionTag(e, null, param, false);
            e1.write(this, PHASE_PREPARE + REUSABLE);
            e2.write(this, PHASE_PREPARE + REUSABLE);
            // no multiref, because result is always boolean (simple type)

            return 0;
        } else {

            ExpressionTag myTag = getTag(e);

            writeBegComment(e);
            cr();
            IClassInfo casttype;
            switch (e.getType()) {
                case YYRelationalExpression.TYPE_NUMERIC:
                    write("(");
                    write(((ExpressionTag)e1.tag).getUse(false));
                    switch (e.getKind()) {
                        case YYRelationalExpression.LS: write(" < "); break;
                        case YYRelationalExpression.GT: write(" > "); break;
                        case YYRelationalExpression.LE: write(" <= "); break;
                        case YYRelationalExpression.GE: write(" >= "); break;
                        case YYRelationalExpression.EQ: write(" == "); break;
                        case YYRelationalExpression.NE: write(" != "); break;
                        default: throw new RuntimeException();
                    }
                    write(((ExpressionTag)e2.tag).getUse(false) +
                        " ? JNI_TRUE : JNI_FALSE)");
                    break;

                case YYRelationalExpression.TYPE_BOOLEAN:
                    write("(");
                    write(((ExpressionTag)e1.tag).getUse(false));
                    switch (e.getKind()) {
                        case YYRelationalExpression.EQ: write(" == "); break;
                        case YYRelationalExpression.NE: write(" != "); break;
                        default: throw new RuntimeException();
                    }
                    write(((ExpressionTag)e2.tag).getUse(false));
                    write(" ? JNI_TRUE : JNI_FALSE)");
                    break;

                case YYRelationalExpression.TYPE_REFERENCE:
                    if (e1.getExpressionType() == classMgr.NULL ||
                            e2.getExpressionType() == classMgr.NULL) {

                        write("(" + getTag(e1).getUse(false));
                        switch (e.getKind()) {
                        case YYRelationalExpression.EQ: write(" == "); break;
                        case YYRelationalExpression.NE: write(" != "); break;
                        default: throw new RuntimeException();
                        }
                        write(getTag(e2).getUse(false) +
                            " ? JNI_TRUE : JNI_FALSE)");
                    } else {
                        // if both are multirefs, may use more efficient
                        // multiref comparison
                        VariableTag v1 = getTag(e1).getVariableTag();
                        VariableTag v2 = getTag(e2).getVariableTag();
                        boolean multiref =
                            v1 != null && v1.useMultiRef() &&
                            v2 != null && v2.useMultiRef();
                        openWriteContextInline("(");
                        cr();
                        openWriteContextInline("_JANET_" +
                            (multiref ? "MULTIREF" : "SIMPLE") + "_COMPARE(");
                        cr(); write(getTag(e1).getUse(multiref) + ", ");
                        cr(); write(getTag(e2).getUse(multiref));
                        closeWriteContext(")");
                        switch (e.getKind()) {
                        case YYRelationalExpression.EQ:
                            write(" ? JNI_TRUE : JNI_FALSE");
                            break;
                        case YYRelationalExpression.NE:
                            write(" ? JNI_FALSE : JNI_TRUE");
                            break;
                        default:
                            throw new RuntimeException();
                        }
                        closeWriteContext(")");
                    }
                    break;

                default:
                    throw new RuntimeException();
            }
            writeEndComment(e);

            return 0;
        }
    }

    private DeclarationTag findContext(YYVariableDeclarator v) {
        IScope i = v.getEnclosingScope();
        while (i instanceof YYStatement) {
            YYStatement s = (YYStatement)i;
            if (s.tag != null) return (DeclarationTag)s.tag;
            i = s.getEnclosingScope();
        }
        return (DeclarationTag)this.currMth.getImplementation().tag;
    }

    private String prepareLocalVariable(YYVariableDeclarator var, int param)
            throws IOException {

        String varname = ClassManager.mangle(var.getName());
        String infix;

        DeclarationTag context;
        IScope varScope = var.getEnclosingScope();

        switch (var.getDeclarationType()) {
        case YYVariableDeclarator.PARAMETER:
            context = (DeclarationTag)this.currMth.getImplementation().tag;
            infix = "arg";
            break;
        case YYVariableDeclarator.CATCH_PARAMETER:
            context = findContext(var);
            infix = "exc";
            break;
        case YYVariableDeclarator.LOCAL_VARIABLE:
            context = findContext(var);
            infix = "var";
            if (var.tag == null) {
                try {
                    var.tag = new VariableTag(var.getType(), "var", varname);
                    context.addVariable((VariableTag)var.tag);
                } catch (ParseException exc) {
                    throw new RuntimeException();
                }
            }
            break;
        default:
            throw new RuntimeException();
        }

        if ((param & MULTIREF) != 0) {
            if (var.tag == null) {
                try {
                    var.tag = new VariableTag(var.getType(), infix, varname);
                    context.addVariable((VariableTag)var.tag);
                } catch (ParseException exc) {
                    throw new RuntimeException();
                }
            }
            ((VariableTag)var.tag).setTypeLocalVariable();

        }

        return "_janet_" + infix + "_" + varname;
    }




    public int write(final YYLocalVariableAccessExpression e, int param)
            throws IOException {
        if ((param & (PHASE_PREPARE)) != 0) {

            // PHASE_PREPARE
            YYVariableDeclarator var = e.getVariable();
            String varname = prepareLocalVariable(var, param);
            e.tag = new ExpressionTagForVariableAccess(e, var, varname);

            return (param & MULTIREF);

        } else {

            // PHASE_WRITE
            ExpressionTag tag = (ExpressionTag)e.tag;

            if ((param & EVALUATE_ONLY) != 0) {
                return 0;
            } else if ((param & ASSIGNMENT_PREFIX) != 0) {
                cr(); write(tag.getEvaluationPrefix(true));
                return MULTIREF;
            } else if ((param & ASSIGNMENT_SUFFIX) != 0) {
                write(tag.getEvaluationSuffix(true));
                return MULTIREF;
            } else if ((param & COMPOUND_PREFIX) != 0) {
                write(tag.getUse(false));
            } else if ((param & COMPOUND_SUFFIX) != 0) {
                // do nothing;
            } else {
                write(tag.getUse((param & MULTIREF) != 0));
            }
            return 0;
        }
    }



    private ExpressionTag writeAssignment(YYExpression leftHandSide,
            YYExpression assignment, int param) throws IOException {

        if ((param & PHASE_PREPARE) != 0) {

        } else {


        }
        return null;
    }

    public int write(YYAssignmentExpression e, int param) throws IOException {
        YYExpression leftHandSide = e.getLeftHandSide();
        YYExpression assignment = e.getAssignment();
        BinaryOperator op = e.getOperator();
        boolean isPrimitive = e.getExpressionType().isPrimitive();

        if ((param & PHASE_PREPARE) != 0)
        {
            if (op == null || isPrimitive) {
                // we reuse the same variable
                leftHandSide.write(this, PHASE_PREPARE + REUSABLE + MULTIREF);
                assignment.write(this, PHASE_PREPARE +
                    (isPrimitive ? 0 : MULTIREF + REUSABLE));
                e.tag = new OpaqueExpressionTag(e, (ExpressionTag)leftHandSide.tag);
            } else {
                // string concatenation; we can discard old string
                leftHandSide.write(this, PHASE_PREPARE);
                assignment.write(this, PHASE_PREPARE);
                initExpressionTag(e, null, param, false);
            }
        }
        else
        {
            writeBegComment(e);
            openWriteContext("(");
            ExpressionTag assignmentTag = getTag(assignment);
//            if (assignmentTag.needsEvaluation()) {
//                assignment.write(this, PHASE_WRITE + MULTIREF + EVALUATE_ONLY);
//                write(",");
//            }
            if (op == null) {
                leftHandSide.write(this, PHASE_WRITE + ASSIGNMENT_PREFIX);
                currIndent += tabSize;
    //            write(assignmentTag.getUse(true));
                assignment.write(this, PHASE_WRITE + MULTIREF);
                currIndent -= tabSize;
                leftHandSide.write(this, PHASE_WRITE + ASSIGNMENT_SUFFIX);
                if ((param & EVALUATE_ONLY) == 0) {
                    write(",");
                    cr(); write(getTag(e).getUse((param & MULTIREF) != 0));
                }
            } else {
                //leftHandSide.write(his, PHASE_WRITE + COMPOUND_PREFIX);
            }

            closeWriteContext(")");
            writeEndComment(e);
        }
        return MULTIREF;
    }


    public int write(YYExpressionStatement s, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            return s.getExpression().write(this, param);
        } else {
            s.getExpression().write(this, PHASE_WRITE | EVALUATE_ONLY);
            write(";");
            return 0;
        }
    }




    private void writeTargetAndIndexEvaluation(YYExpression tgt,
        ExpressionTag tgttag, YYExpression idx, ExpressionTag idxtag)
        throws IOException
    {

        // target evaluation
        if (tgttag.needsEvaluation()) {
            tgt.write(this, PHASE_WRITE | EVALUATE_ONLY);
            write(", ");
        }

        // index evaluation
        if (idxtag.needsEvaluation()) {
            idx.write(this, PHASE_WRITE | EVALUATE_ONLY);
            write(", ");
        }
        cr();
    }

    private void writeCheckBeforeArrayDeref(ExpressionTag tgttag,
        ExpressionTag idxtag, boolean isPrimitive) throws IOException
    {
        // check target against null
        cr(); write("_JANET_LOCAL_ENSURE_NOT_NULL(");
        write(tgttag.getUse());
        write(","); cr();
        write("    \"trying to access " +
            "an array using a null target reference\"),");

        // check bounds (for Object arrays, this is done by JNI)
        if (isPrimitive) {
            cr();
            if (tgttag.getVariableTag() != null &&
                    tgttag.getVariableTag().useMultiRef()) {
                write("_JANET_MULTIARRAY_CHECK_BOUNDS(" + tgttag.getUse(true));
            } else {
                write("_JANET_ARRAY_CHECK_BOUNDS(" + tgttag.getUse(false));
            }
            write(", " + idxtag.getUse() + "),");
        }
        cr();
    }

    private void writeArrayElemGetSet(YYArrayAccessExpression e,
        ExpressionTag mytag, ExpressionTag tgttag, ExpressionTag idxtag,
        boolean isPrimitive, boolean set, boolean indented) throws IOException
    {
        if (indented) { openWriteContext("("); }
        if (isPrimitive) {
            IClassInfo cls = e.getExpressionType();
            String signature = cls.getSignature();
            String jnitype  = cls.getJNIType();

            cr();
            write("((" + jnitype + "*)" +
                "_JANET_ARRAY_GET_JPTR(" +
                tgttag.getUse(true) + ", " + signature + "))[" +
                idxtag.getUse() +
                "]");
            if (set) { write(" = " + mytag.getUse(false)); }
        } else {
            cr();
            write("JNI_" + (set ? "SET" : "GET") +
                "_OBJECT_ARRAY_ELEMENT(" + tgttag.getUse(false) +
                ", " +
                idxtag.getUse() +
                (set ? ", " + mytag.getUse(false) : "") + ")");
        }
        if (indented) { closeWriteContext(")"); }

    }

    public int write(YYArrayAccessExpression e, int param) throws IOException {

        boolean isPrimitive = e.getExpressionType().isPrimitive();

        if ((param & PHASE_PREPARE) != 0) {

            initExpressionTag(e, null, param, false);

            if (isPrimitive) {
                // must be stored to be released later
                e.getTarget().write(this, PHASE_PREPARE | REUSABLE | MULTIREF);
                getTag(e.getTarget()).getVariableTag().setTypeLocalVariable();
            } else {
                e.getTarget().write(this, PHASE_PREPARE | REUSABLE);
            }
            e.getIndexExpression().write(this, PHASE_PREPARE | REUSABLE);
            currentDclTag.setUsesLocalExceptions();

        } else {

            ExpressionTag myTag = getTag(e);
            YYExpression tgt = e.getTarget();
            ExpressionTag tgtTag = getTag(tgt);
            YYExpression idx = e.getIndexExpression();
            ExpressionTag idxTag = getTag(idx);
            //parentdffboolean idxNeedsCast = (idx.getExpressionType() != classMgr.INT);

            if ((param & ASSIGNMENT_PREFIX) != 0)
            {
                openWriteContext("(");
                writeTargetAndIndexEvaluation(tgt, tgtTag, idx, idxTag);
                write(myTag.getEvaluationPrefix(true));
            }
            else if ((param & ASSIGNMENT_SUFFIX) != 0)
            {
                write(myTag.getEvaluationSuffix(true));
                write(",");
                writeCheckBeforeArrayDeref(tgtTag, idxTag, isPrimitive);
                writeArrayElemGetSet(e, myTag, tgtTag, idxTag, isPrimitive, true, false);
                closeWriteContext(")");
            }
            else if ((param & COMPOUND_PREFIX) != 0)
            {
                openWriteContext("(");
                writeTargetAndIndexEvaluation(tgt, tgtTag, idx, idxTag);
                writeCheckBeforeArrayDeref(tgtTag, idxTag, isPrimitive);
                // assigning GetObjectArrayElement or primitive value -> never multiref
                if (!isPrimitive) { // array of strings
                    write(myTag.getEvaluationPrefix(false));
                }
                writeArrayElemGetSet(e, myTag, tgtTag, idxTag, isPrimitive, false, true);
                if (!isPrimitive) {
                    write(myTag.getEvaluationSuffix(false));
                }
                closeWriteContext(")");
            }
            else if ((param & COMPOUND_SUFFIX) != 0)
            {
                if (!isPrimitive) { // store modified string back to array
                    writeArrayElemGetSet(e, myTag, tgtTag, idxTag, isPrimitive, true, true);
                }
            }
            else
            {
                writeBegComment(e);
                openWriteContext("(");
                writeTargetAndIndexEvaluation(tgt, tgtTag, idx, idxTag);
                writeCheckBeforeArrayDeref(tgtTag, idxTag, isPrimitive);
                // assigning GetObjectArrayElement or primitive value -> never multiref
                write(myTag.getEvaluationPrefix(false));
                writeArrayElemGetSet(e, myTag, tgtTag, idxTag, isPrimitive, false, true);
                write(myTag.getEvaluationSuffix(false));
                closeWriteContext(")");
                writeEndComment(e);
            }
        }
        return 0;
    }

    public int write(YYArrayCreationExpression e, int param) throws IOException {
        YYExpressionList dimexprs = e.getDimExprs();

        if ((param & PHASE_PREPARE) != 0) {

            initExpressionTag(e, null, param, false);

            for (Iterator i = dimexprs.iterator(); i.hasNext();) {
                YYExpression expr = (YYExpression)i.next();
                expr.write(this, PHASE_PREPARE + REUSABLE);
            }

            currentDclTag.setUsesLocalExceptions();

        } else {

            Iterator itr;
            int i;
            boolean first = true;
            ExpressionTag myTag = getTag(e);

            writeBegComment(e);
            openWriteContext("(");

            // write dimexprs evaluation (must be at least 1 dimexpr)
            for (itr = dimexprs.iterator(); itr.hasNext();) {
                YYExpression expr = (YYExpression)itr.next();
                ExpressionTag tag = getTag(expr);
                if (tag.needsEvaluation()) {
                    if (!first) write(",");
                    expr.write(this, PHASE_WRITE + EVALUATE_ONLY);
                    first = false;
                }
            }
            if (!first) write(",");

            // check against negative values (must be at least 1 dimexpr)
//            write("\n" + makeIndent() + "("); currIndent += tabSize;
            for (itr = dimexprs.iterator(), i=0; itr.hasNext(); i++) {
                YYExpression expr = (YYExpression)itr.next();
                cr(); write("_JANET_LOCAL_ENSURE_ARRSIZE_NONNEGATIVE(" +
                            i + ", " + getTag(expr).getUse() + "),");
            }
//            write("? _JANET_LOCAL_THROW_NEGATIVE_ARRAY_INDEX() : 0");
//            currIndent -= tabSize; write("\n" + makeIndent() + "),");

            // we are not able to follow Java semantics exactly (JLS 15.10.1);
            // there is no way to preallocate space for array prior to
            // constructing subarrays. All we can do is to defer assignment of
            // resulting reference until all arrays are successfully constructed.

            // construct the array
            int[] classidxs = e.getClassIdxs();

            cr(); write(myTag.getEvaluationPrefix(false));

            openWriteContextInline("_JANET_CREATE_ARRAY(");
            cr(); write("_janet_jnienv,");
            cr(); write(e.getDepth() + ",");
            cr(); write("_JANET__FILE__, _JANET__LINE__,");

            for (itr = dimexprs.iterator(), i=0; itr.hasNext(); i++) {
                YYExpression expr = (YYExpression)itr.next();
                cr(); write(getTag(expr).getUse() + ", ");
                if (classidxs[i] != -1) {
                    write("_janet_classes[" + classidxs[i] + "].id");
                } else {
                    // primitive base type
                    write("0, ");
                    write("(*_janet_jnienv)->New" +
                          getJNITypeInfixName(e.getBaseType()) +
                          "Array");
                }
                if (itr.hasNext()) write(",");
            }
            closeWriteContext(")");
            write(myTag.getEvaluationSuffix(false));
            closeWriteContext(")");
            writeEndComment(e);

        }
        return 0;
    }




    public int write(YYPtrFetchExpression e, int param) throws IOException {

        YYExpression base = e.getBase();
        if ((param & PHASE_PREPARE) != 0) {

            base.write(this, PHASE_PREPARE | MULTIREF);
            VariableTag vtag = getTag(base).getVariableTag();

            // use variable from base
            vtag.setTypeLocalVariable();
            //VariableTag vtag = new VariableTag(base.getExpressionType());
            //currentDclTag.addVariable(vtag);
            //vtag.setTypeLocalVariable();
            //e.tag = new ExpressionTag(vtag);
            currentDclTag.setUsesLocalExceptions();

        } else {

            ExpressionTag tag = getTag(base);
            writeBegComment(e);
            openWriteContext("(");
            //write("\n" + makeIndent() + tag.getEvaluationPrefix(true));

            if (tag.needsEvaluation()) {
                base.write(this, PHASE_WRITE + MULTIREF + EVALUATE_ONLY);
                write(",");
            }

            //write(tag.getEvaluationSuffix(true) + ", ");
            //write(", ");

            if (base.getExpressionType().isArray()) {
                // must be primitive type array

                String sign;
                String jnitype;
                IClassInfo cls;
                try {
                    cls = base.getExpressionType().getComponentType();
                    sign = cls.getSignature();
                    jnitype = cls.getJNIType();
                } catch (CompileException exc) { throw new RuntimeException(); }

                cr(); write("_JANET_LOCAL_ENSURE_NOT_NULL(");
                write(tag.getUse());
                write(","); cr();
                write("    \"trying to access " +
                    "an array using a null target reference\"),");

                if (!e.convertToNative()) {
                    cr();
                    write("(" + jnitype + "*)" + "_JANET_ARRAY_GET_JPTR(" +
                          tag.getUse(true) + ", " + sign + ")");
                } else {
                    cr(); write("(");
                    if (cls == classMgr.INT) {
                        write("int");
                    } else if (cls == classMgr.BYTE) {
                        write("signed char");
                    } else if (cls == classMgr.CHAR) {
                        write("unsigned short");
                    } else if (cls == classMgr.BOOLEAN) {
                        write("unsigned char");
                    } else if (cls == classMgr.SHORT) {
                        write("short");
                    } else if (cls == classMgr.DOUBLE) {
                        write("double");
                    } else if (cls == classMgr.FLOAT) {
                        write("float");
                    } else if (cls == classMgr.LONG) {
                        write("long");
                    }
                    write("*)_JANET_ARRAY_GET_CPTR(" +
                        tag.getUse(true) + ", " + sign + ")");
                }

            } else {
                // must be string
                cr();
                if (!e.convertToNative()) {
                    write("_JANET_STRING_GET_UNICODE(" + tag.getUse(true) + ")");
                } else {
                    write("_JANET_STRING_GET_UTF(" + tag.getUse(true) + ")");
                }
            }
            closeWriteContext(")");
            writeEndComment(e);
        }
        return 0;
    }
    System s;
    public int write(YYCastExpression e, int param) throws IOException {

        boolean rtcheck = e.requiresRuntimeCheck();
        if ((param & PHASE_PREPARE) != 0) {

            e.getTarget().write(this, PHASE_PREPARE | (param & MULTIREF) |
                (rtcheck ? REUSABLE : (param & REUSABLE)));
            e.tag = new ExpressionTagForCast(e);
            if (rtcheck) {
                currentDclTag.setUsesLocalExceptions();
            }

        } else {

            ExpressionTag myTag = getTag(e);
            YYExpression tgt = e.getTarget();
            ExpressionTag tgtTag = getTag(tgt);
            boolean usesMultiref = tgtTag.getVariableTag() != null && tgtTag.getVariableTag().useMultiRef();

            writeBegComment(e);
            openWriteContext("(");
            boolean first = true;
            write(myTag.getEvaluationPrefix(usesMultiref));
            if (!rtcheck || tgtTag.needsEvaluation()) {
                cr();
                tgt.write(this, PHASE_WRITE | (usesMultiref ? MULTIREF : 0) |
                   (rtcheck ? EVALUATE_ONLY : 0));
                first = false;
            }
            //write(getTag(e.getTarget()).getUse(usesMultiref));
            write(myTag.getEvaluationSuffix(usesMultiref));
            if (rtcheck) {
                if (!first) { write(","); }
                cr();
                openWriteContextInline("_JANET_CAST_RTCHECK(");
                cr(); write("(jobject)" + myTag.getUse(false) + ",");
                cr(); write("&_janet_classes[" + e.getClassIdx() + "]");
                closeWriteContext(")");
                first = false;
            }
            if ((param & EVALUATE_ONLY) == 0 && rtcheck) {
                if (!first) { write(","); }
                cr();
                write(myTag.getUse((param & MULTIREF) != 0));
            }
            closeWriteContext(")");
            writeEndComment(e);

        }
        return 0;
    }


    public int write(YYSynchronizedStatement s, int param) throws IOException {

        YYExpression expression = s.getExpression();
        YYStatement body = s.getStatement();

        if ((param & PHASE_PREPARE) != 0) {

            s.tag = begDeclarationTag(s);
            // will be ignored if no exceptions come from inside
            ((DeclarationTag)s.tag).setRequiresTryClause();
//            expression.write(this, PHASE_PREPARE | REUSABLE);
            expression.write(this, PHASE_PREPARE);
            body.write(this, PHASE_PREPARE);
            endDeclarationTag();

        } else {

            ExpressionTag tag = getTag(expression);
            DeclarationTag dt = (DeclarationTag)s.tag;
            writeBegComment(s);
            openWriteContext("{");
            writeDclUnitBegin(dt);
            if (tag.needsEvaluation()) {
                expression.write(this, PHASE_WRITE | EVALUATE_ONLY);
                write(";");
            }
//            write("\n" + makeIndent() + "JNI_MONITOR_ENTER(" +
//                tag.getUse(false) + "); ");
//            body.write(this, PHASE_WRITE);
//
            cr(); write("_JANET_MONITOR_ENTER(" + s.getSyncIdx() + ", " +
                        tag.getUse(false) + "); ");
            body.write(this, PHASE_WRITE);

            writeDclUnitEnd1(dt);
            writeDclUnitDestructors(dt);
//            cr(); write("JNI_MONITOR_EXIT(" + tag.getUse(false) + ");");
            cr(); write("_JANET_MONITOR_EXIT(" + s.getSyncIdx() + ");");
            writeDclUnitEnd2(dt);
            closeWriteContext("}");
            writeEndComment(s);
        }
        return 0;
    }




    public int write(YYTryStatement s, int param) throws IOException {

        YYStatement body = s.getBody();
        YYStatement catches = s.getCatches();
        YYStatement finly = s.getFinally();

        if ((param & PHASE_PREPARE) != 0) {

            begDeclarationTag(s);
            functionDclTag.usesExceptions = true;
            //writeDeclarationUnit(s, PHASE_PREPARE | BODY_PREFIX);
            currentDclTag.setRequiresTryClause();
            body.write(this, PHASE_PREPARE);
            if (catches != null) catches.write(this, PHASE_PREPARE);
            if (finly != null) finly.write(this, PHASE_PREPARE);
            endDeclarationTag();
            //writeDeclarationUnit(s, PHASE_PREPARE | BODY_SUFFIX);

        } else {

            DeclarationTag dt = (DeclarationTag)s.tag;
            writeBegComment(s);
            cr(); write("{"); currIndent += tabSize;
            cr(); write("_JANET_EXCEPTION_CONTEXT_BEGIN");
            cr(); write("_JANET_TRY "); currIndent += tabSize;
            body.write(this, PHASE_WRITE);
            if (catches != null) {
                catches.write(this, PHASE_WRITE);
            }
            if (finly != null) {
                cr(); write("_JANET_FINALLY "); currIndent += tabSize;
                finly.write(this, PHASE_WRITE);
                currIndent -= tabSize;
            }

            currIndent -= tabSize;
            cr();
            if (dt.requiresDestructClause()) {
                write(" _JANET_DESTRUCT {"); currIndent += tabSize;
            }

            writeDclUnitDestructors(dt);

            if (dt.requiresDestructClause()) {
                currIndent -= tabSize;
                cr(); write("} _JANET_END_TRY;");
            } else {
                cr(); write("_JANET_END_TRY;");
            }
            cr(); write("_JANET_EXCEPTION_CONTEXT_END_");
            write(getLocalSuffix(dt.getParent()));
            currIndent -= tabSize; cr(); write("}");
            writeEndComment(s);
        }
        return 0;
    }

    public int write(YYCatchClause s, int param) throws IOException {
        YYStatement body = s.getBody();
        YYVariableDeclarator v = s.getFormalParameter();

        if ((param & PHASE_PREPARE) != 0) {

            body.write(this, PHASE_PREPARE);
            VariableTag vtag = (VariableTag)v.tag;
            if (vtag == null) {
                try {
                    v.tag = vtag = new VariableTag(s.getCatchedExceptionType(),
                                                   "exc", ClassManager.mangle(v.getName()));
                    currentDclTag.addVariable(vtag);
                } catch (ParseException e) {
                    throw new RuntimeException();
                }
            }

        } else {

            VariableTag vtag = (VariableTag)v.tag;
            writeBegComment(s);
            cr();
            write("_JANET_CATCH" + vtag.getCommonMacroSuffix() + "(_janet_classes[" +
                s.getExcClsIdx() + "].id, " + ((VariableTag)v.tag).getName() + ") ");
            currIndent += tabSize;
            body.write(this, PHASE_WRITE);
            currIndent -= tabSize;
            writeEndComment(s);
        }
        return 0;
    }

    public int write(YYFinally s, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            return s.getBody().write(this, param);
        } else {
            writeBegComment(s);
            int result = s.getBody().write(this, param);
            writeEndComment(s);
            return result;
        }
    }

    private DeclarationTag getDclTag(YYStatement s) {
        while (s.tag == null) {
            s = (YYStatement)s.parent();
        }
        return (DeclarationTag)s.tag;
    }

    private int writeAbrupt(YYStatement s, YYExpression expr, String action,
                            int param, boolean checknotnull) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {
            // store in s.tag the current DeclarationCxt
            s.tag = currentDclTag;
            currentDclTag.setUsesAbrupts();
            if (expr != null) {
                expr.write(this, PHASE_PREPARE | REUSABLE);
            }
        } else {
            DeclarationTag dt = (DeclarationTag)s.tag;
            ExpressionTag baseTag = null;
            writeBegComment(s);
            openWriteContext("do {");
            if (expr != null) {
                baseTag = getTag(expr);
                if (baseTag.needsEvaluation()) {
                    expr.write(this, PHASE_WRITE | EVALUATE_ONLY);
                    write(";");
                }
                if (checknotnull) {
                    cr(); write("_JANET_" + getLocalInfix(dt) + "_ENSURE_NOT_NULL(");
                    write(baseTag.getUse(false));
                    write(", \"\");");
                }
            }
            cr();
            write("_JANET_" + action + "_" + getLocalSuffix(dt) +
                  "(" + (baseTag == null ? "" : baseTag.getUse(false)) + ");");
            closeWriteContext("} while(0);");
            writeEndComment(s);
        }
        return 0;
    }

    public int write(YYThrowStatement s, int param) throws IOException {
        return writeAbrupt(s, s.getExceptionExpression(), "THROW", param, true);
    }

    public int write(YYReturnStatement s, int param) throws IOException {
        return writeAbrupt(s, s.getReturnedExpression(), "RETURN", param, false);
    }

    public int write(YYVariableDeclaratorList s, int param) throws IOException {
        for (Iterator i = s.iterator(); i.hasNext();) {
            ((YYVariableDeclarator)i.next()).write(this, param);
        }
        return 0;
    }

    public int write(YYVariableDeclarator s, int param) throws IOException {
        YYExpression initializer = s.getInitializer();
        if (initializer == null) return 0;
        if ((param & PHASE_PREPARE) != 0) {

            prepareLocalVariable(s, param | MULTIREF);
            initializer.write(this, PHASE_PREPARE | MULTIREF | REUSABLE);

        } else {

            VariableTag vtag = (VariableTag)s.tag;
            writeBegComment(s);

            openWriteContext("(");
            ExpressionTag iniTag = getTag(initializer);
            if (iniTag.needsEvaluation()) {
                initializer.write(this, PHASE_WRITE + MULTIREF + EVALUATE_ONLY);
                write(",");
            }

            cr(); write(vtag.getVariableAssignmentPrefix(true));
            write(iniTag.getUse(true));
            write(vtag.getVariableAssignmentSuffix(true));

            closeWriteContext(");");

            writeEndComment(s);
        }
        return 0;
    }




    public int write(YYEnclosedNativeString e, int param) throws IOException {
        if ((param & PHASE_PREPARE) != 0) {

            initExpressionTag(e, null, param, true);
            currentDclTag.setUsesLocalExceptions();

        } else {

            writeBegComment(e);
            openWriteContext("(");
            cr(); write(getTag(e).getEvaluationPrefix(false) + "JNI_NEW_STRING");
            if (!e.isUnicode()) write("_UTF");
            write("(");
            e.getNativeString().write(this, PHASE_WRITE);
            write(getTag(e).getEvaluationSuffix(false) + "),");
            cr(); write("_JANET_LOCAL_HANDLE_EXCEPTION(),");
            cr(); write(getTag(e).getUse((param & MULTIREF) != 0));

            closeWriteContext(")");
            writeEndComment(e);
        }
        return 0;
    }

    public int write(YYEnclosedNativeExpression e, int param)
            throws IOException {
        if ((param & PHASE_PREPARE) != 0) {

            initExpressionTag(e, null, param, false);
            return e.getNativeCode().write(this, param);

        } else {

            writeBegComment(e);
            openWriteContext("(");
            cr(); write(getTag(e).getEvaluationPrefix(false));
            write("(");
            e.getNativeCode().write(this, PHASE_WRITE);
            write(")");
            write(getTag(e).getEvaluationSuffix(false));
            closeWriteContext(")");
            writeEndComment(e);

        }
        return 0;
    }

    public int write(YYEnclosedNativeStatements e, int param)
            throws IOException {
        return e.getNativeCode().write(this, param);
    }


    interface AuxWriter {
        void write() throws IOException;
    }

    private final int BINOP_MULTIEVAL_LEFT  = 1;
    private final int BINOP_MULTIEVAL_RIGHT = 2;
    private final int BINOP_THROWS          = 4;
    private final int BINOP_DISCARDS_TYPE   = 8;

    private boolean isFP(BinaryOperator op) {
        IClassInfo c = op.getResultType();
        return (c == classMgr.DOUBLE || c == classMgr.FLOAT);
    }

    private int checkBinaryOperationReqs(BinaryOperator op) {
        switch (op.getID()) {
        case BinaryOperator.PLUS:
            if (!op.getResultType().isPrimitive()) {
                // string concatenation
                throw new UnsupportedOperationException();
            }
            return isFP(op) ? BINOP_DISCARDS_TYPE : 0;
        case BinaryOperator.MINUS:
            return isFP(op) ? BINOP_DISCARDS_TYPE : 0;
        case BinaryOperator.MULTIPLY:
            return isFP(op) ? BINOP_DISCARDS_TYPE : 0;
        case BinaryOperator.DIVIDE:
            return isFP(op)
                ? BINOP_DISCARDS_TYPE
                : BINOP_MULTIEVAL_RIGHT | BINOP_THROWS;
        case BinaryOperator.REMAINDER:
            return isFP(op)
                ? BINOP_DISCARDS_TYPE
                : BINOP_MULTIEVAL_RIGHT | BINOP_MULTIEVAL_LEFT | BINOP_THROWS;
        case BinaryOperator.AND:
        case BinaryOperator.OR:
        case BinaryOperator.XOR:
            return 0;
        case BinaryOperator.LSHIFT:
            return 0;
        case BinaryOperator.RSHIFT:
        case BinaryOperator.LOGRSHIFT:
            return BINOP_MULTIEVAL_LEFT | BINOP_MULTIEVAL_RIGHT;
        default:
            throw new RuntimeException();
        }
    }

    private void writeDefault(AuxWriter left, String op,
        AuxWriter right) throws IOException
    {
        write("(");
        left.write();
        write(" " + op + " ");
        right.write();
        write(")");
    }

    private void writeBinaryOperation(BinaryOperator op, //AuxWriter result,
        AuxWriter left, AuxWriter right) throws IOException
    {
        IClassInfo restype = op.getResultType();
        switch (op.getID()) {
        case BinaryOperator.PLUS:
            if (restype == classMgr.String) {
                throw new UnsupportedOperationException();
            }
        case BinaryOperator.MINUS:
        case BinaryOperator.MULTIPLY:
            // todo: do not cast
            writeDefault(left, op.getSymbol(), right);
            break;
        case BinaryOperator.DIVIDE:
            //boolean rne = getTag(r).needsEvaluation();
            if (restype == classMgr.INT || restype == classMgr.LONG) {
                // integer division
                write("_JANET_INTEGER_DIVISION(");
                left.write();
                write(", ");
                right.write();
                write(")");
            } else {
                // fp division; NO JAVA SEMANTICS ENFORCED
                writeDefault(left, "/", right);
            }
            break;
        default:
            throw new UnsupportedOperationException();
        }

    }

    private AuxWriter getAuxWriterForExpr(final YYExpression e, boolean reusable) {
        final ExpressionTag tag = getTag(e);
        if (reusable && tag.needsEvaluation()) {
            return new AuxWriter() {
                public void write() throws IOException {
                    Writer.this.write(tag.getUse());
                }
            };
        } else {
            return new AuxWriter() {
                public void write() throws IOException {
                    // hack to avoid casting FP subexpressions in binops
                    // (they had owntype set to NATIVE)
                    tag.owntype = e.getExpressionType();
                    e.write(Writer.this, PHASE_WRITE);
                }
            };
        }
    }

    public int write(YYBinaryExpression e, int param) throws IOException
    {
        YYExpression l = e.getLeft();
        YYExpression r = e.getRight();
        BinaryOperator op = e.getOperator();
        IClassInfo rtype = op.getResultType();
        int check = checkBinaryOperationReqs(op);
        boolean lreusable = (check & BINOP_MULTIEVAL_LEFT) != 0;
        boolean rreusable = (check & BINOP_MULTIEVAL_RIGHT) != 0;

        if ((param & PHASE_PREPARE) != 0)
        {
            initExpressionTag(e, null, param, false);
            if ((check & BINOP_THROWS) != 0) {
                currentDclTag.setUsesLocalExceptions();
            }
            l.write(this, PHASE_PREPARE + (lreusable ? REUSABLE : 0));
            r.write(this, PHASE_PREPARE + (rreusable ? REUSABLE : 0));

            if ((check & BINOP_DISCARDS_TYPE) != 0) {
                // we defer type cast:
                // it is later performed due to "casttype" in the tag
                // but if it appears as a side in binop, the cast is avoided
                // by re-setting "owntype" again
                getTag(e).owntype = classMgr.NATIVETYPE;
            }
        }
        else
        {
            ExpressionTag tag = getTag(e);
            lreusable &= getTag(l).needsEvaluation();
            rreusable &= getTag(r).needsEvaluation();
            AuxWriter lw = getAuxWriterForExpr(l, lreusable);
            AuxWriter rw = getAuxWriterForExpr(r, rreusable);
            if (lreusable || rreusable) {
                writeBegComment(e);
                openWriteContext("(");
                if (lreusable) {
                    cr(); l.write(this, PHASE_WRITE + EVALUATE_ONLY);
                    write(", ");
                }
                if (rreusable) {
                    cr(); r.write(this, PHASE_WRITE + EVALUATE_ONLY);
                    write(", ");
                }
            }
            write(tag.getEvaluationPrefix(false));
            writeBinaryOperation(op, lw, rw);
            write(tag.getEvaluationSuffix(false));
            if (lreusable || rreusable) {
                closeWriteContext(")");
                writeEndComment(e);
            }
        }
        return 0;
    }




    String makeIndent() {
        return makeIndent(currIndent);
    }

    private static StringBuffer seq_spaces = new StringBuffer(
        /* initially 256 spaces */
        "                                                                " +
        "                                                                " +
        "                                                                " +
        "                                                                ");

    private static String[] string_seqs = new String[256];

    private String makeIndent(int indent) {
        if (indent < 256) {
            if (string_seqs[indent] == null) {
                string_seqs[indent] = seq_spaces.substring(0, indent);
            }
            return string_seqs[indent];
        }
        while (seq_spaces.length() < indent) seq_spaces.append(seq_spaces);
        return seq_spaces.substring(0, indent);
    }

    String getJNITypeInfixName(IClassInfo cls) {
        if (cls.isReference()) return "Object";
        if (cls == classMgr.VOID) return "Void";
        if (cls == classMgr.BOOLEAN) return "Boolean";
        if (cls == classMgr.BYTE) return "Byte";
        if (cls == classMgr.SHORT) return "Short";
        if (cls == classMgr.CHAR) return "Char";
        if (cls == classMgr.INT) return "Int";
        if (cls == classMgr.LONG) return "Long";
        if (cls == classMgr.FLOAT) return "Float";
        if (cls == classMgr.DOUBLE) return "Double";
        throw new RuntimeException();
    }



//    static String writeVariableUse(YYExpression expr) {
//        ((VariableTag)expr.tag).writeUse();
//    }
//
    /**
     * shortcut
     */
    static ExpressionTag getTag(YYExpression e) {
        return (ExpressionTag)e.tag;
    }

    private void writeComment(YYStatement stmt, String prefix, String suffix) throws IOException {
        if (!settings.sourceComments()) return;
        String s = stmt.ibuf().getbuf().substring(stmt.lbeg().charno0(), stmt.lend().charno0());
        int pos1 = s.indexOf('\n');
        int posaux = s.indexOf('\r');
        if (posaux >= 0 && posaux < pos1) pos1 = posaux;
        int pos2 = Math.max(s.lastIndexOf('\n'), s.lastIndexOf('\r'));
        if (pos1 >= 0 && pos2 >= 0) {
            s = s.substring(0, pos1) + " ... " + s.substring(pos2+1, s.length());
        }
        int pos = 0;
        while ((pos = s.indexOf("*/", 0)) > 0) {
            s = s.substring(0, pos) + "* /" + s.substring(pos+2);
        }
        write("\n" + makeIndent() + "/* " + prefix + s + suffix + " */");
    }

    void writeBegComment(YYStatement s) throws IOException {
        writeComment(s, "beg: ", "");
    }

    void writeEndComment(YYStatement s) throws IOException {
        String suffix = "";
        VariableTag vtag = null;
        if (s.tag instanceof ExpressionTag) {
            vtag = ((ExpressionTag)s.tag).getVariableTag();
        } else if (s.tag instanceof VariableTag) {
            vtag = (VariableTag)s.tag;
        }
        if (vtag != null) suffix = ", hold in: " + vtag.getName();
        writeComment(s, "end: ", suffix);
    }

    private void openWriteContext(String s) throws IOException {
        cr(); write(s); currIndent += tabSize;
    }

    private void openWriteContextInline(String s) throws IOException {
        write(s); currIndent += tabSize;
    }

    private void closeWriteContext(String s) throws IOException {
        currIndent -= tabSize; cr(); write(s);
    }

    private void cr() throws IOException {
        write("\n");
        write(makeIndent());
    }

    /*    private void setMultiRef(VariableTag tag) {
        if (tag.setMultiRef()) this.maxMultiRefsUsed++;
    }*/
}