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

package pl.edu.agh.icsr.janet;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.yytree.*;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class NativeWriter {

    static final String janetHeader =
        "/**\n" +
        " * file:      %__CFILENAME__%\n" +
        " * basefile:  %BASEFILE%\n" +
        " * generated: %__DATE__%\n" +
        " */\n" +
        "\n" +
        "#include <janet_base.h>\n" +
        "\n";

    BufferedWriter fileWriter;
    Writer.Substituter subst;
    ClassManager classMgr;
    Janet.Settings settings;
    Hashtable nwriters;
    String filename;

    public NativeWriter(Writer.Substituter subst, Janet.Settings settings,
                        ClassManager classMgr) {
        this.subst = subst;
        this.settings = settings;
        this.classMgr = classMgr;
        this.nwriters = new Hashtable();
    }

    public void classWriteInit(YYClass cls) throws IOException {
        filename = ClassManager.mangle(
            //settings.getQnames() ? cls.getFullName() : cls.getSimpleName()) +
            cls.getSimpleName()) +
            ".c";

        File dir = Writer.getOutDirForInput(cls.ibuf(), settings);
        File cOutput = new File(dir, filename);
        fileWriter = new BufferedWriter(new FileWriter(cOutput));
        subst.setSubst("__CFILENAME__", filename);

        fileWriter.write(Janet.getGeneratedCodeLicense());
        fileWriter.write(subst.substitute(janetHeader));
        writeRefClasses(cls);
        writeRefFields(cls);
        writeRefMethods(cls);
        writeRefStringLiterals(cls);
        writeInitMethods(cls);
    }

    void writeRefClasses(YYClass cls) throws IOException {
        Vector refClasses = cls.getReferencedClasses();
        if (refClasses.size() == 0) {
            fileWriter.write("#define _janet_depclasses ((void*)0)\n\n");
        } else {
            fileWriter.write("static _janet_cls _janet_depclasses[] = {\n");
            for (int i=0, len = refClasses.size(); i<len; i++) {
                IClassInfo refcls = (IClassInfo)refClasses.get(i);
                fileWriter.write("   { 0, " + (i==0 ? "1" : "0") + ", \"");
                fileWriter.write(refcls.getJNIName());
                fileWriter.write("\" },\n");
            }
            fileWriter.write("};\n\n");
        }
    }

    void writeRefFields(YYClass cls) throws IOException {
        Vector refFields = cls.getReferencedFields();
        Vector refFieldsClsIdxs = cls.getRefFieldClsIdxs();
        if (refFields.size() == 0) {
            fileWriter.write("#define _janet_depfields ((void*)0)\n\n");
            return;
        }
        fileWriter.write("static _janet_fld _janet_depfields[] = {\n");
        for (int i=0, len = refFields.size(); i<len; i++) {
            IFieldInfo reffld = (IFieldInfo)refFields.get(i);
            int clsidx = ((Integer)refFieldsClsIdxs.get(i)).intValue();
            try {
                fileWriter.write("   { 0, " +
                    "&_janet_depclasses[" + clsidx + "], " +
                    (Modifier.isStatic(reffld.getModifiers()) ? "1" : "0") +
                    ", \"" + reffld.getName() + "\"" +
                    ", \"" + reffld.getType().getSignature() + "\" },\n");
            } catch (ParseException e) {
                throw new RuntimeException();
            }
        }
        fileWriter.write("};\n\n");
    }

    void writeRefMethods(YYClass cls) throws IOException {
        Vector refMethods = cls.getReferencedMethods();
        Vector refMethodsClsIdxs = cls.getRefMethodClsIdxs();
        if (refMethods.size() == 0) {
            fileWriter.write("#define _janet_depmethods ((void*)0)\n\n");
            return;
        }
        fileWriter.write("static _janet_mth _janet_depmethods[] = {\n");
        for (int i=0, len = refMethods.size(); i<len; i++) {
            IMethodInfo refmth = (IMethodInfo)refMethods.get(i);
            int clsidx = ((Integer)refMethodsClsIdxs.get(i)).intValue();
            try {
                fileWriter.write("   { 0, " +
                    "&_janet_depclasses[" + clsidx + "], " +
                    (Modifier.isStatic(refmth.getModifiers()) ? "1" : "0") +
                    ", \"" +
                    (refmth.isConstructor() ? "<init>" : refmth.getName()) +
                    "\"" +
                    ", \"" + refmth.getJNISignature() + "\" },\n");
            } catch (ParseException e) {
                throw new RuntimeException();
            }
        }
        fileWriter.write("};\n\n");
    }

    void writeRefStringLiterals(YYClass cls) throws IOException {
        Vector refStrings = cls.getRefStringLiterals();
        //Vector refFieldsClsIdxs = cls.getRefFieldClsIdxs();
        if (refStrings.size() == 0) {
            fileWriter.write("#define _janet_depstrings ((void*)0)\n\n");
            return;
        }
        fileWriter.write("static _janet_str _janet_depstrings[] = {\n");
        for (int i=0, len = refStrings.size(); i<len; i++) {
            String utf = classMgr.utf2cstring(classMgr.unicode2UTF(
                (String)refStrings.get(i)));
            fileWriter.write("   { 0, " + "\"" + utf + "\" },\n");
        }
        fileWriter.write("};\n\n");
    }

/*    void writeLinkCode(YYClass cls) throws IOException {
        fileWriter.write("void _");*/

    void writeInitMethods(YYClass cls) throws IOException {
        fileWriter.write(getNativeMethodHeader(cls, true,
            cls.getClassManager().VOID, "janetClassInit$", false, null));
        fileWriter.write(
            "{\n" +
            "    _JANET_INIT();\n" +
            "    _JANET_LINK(" +
            "_janet_depclasses, " + cls.getReferencedClasses().size() + ", " +
            "_janet_depfields, " + cls.getReferencedFields().size() + ", " +
            "_janet_depmethods, " + cls.getReferencedMethods().size() + ", " +
            "_janet_depstrings, " + cls.getRefStringLiterals().size() + ");\n" +
            "}\n\n");

        fileWriter.write(getNativeMethodHeader(cls, true,
            cls.getClassManager().VOID, "janetClassFinalize$", false, null));
        fileWriter.write(
            "{\n" +
            "    _JANET_UNLINK(" +
            "_janet_depclasses, " + cls.getReferencedClasses().size() + ");\n" +
            "    _JANET_FINALIZE();\n" +
            "}\n\n");
    }

    public void classWriteFinalize() throws IOException {
        try {
            subst.unsetSubst("__CFILENAME__");
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
        } catch (IOException e) {
            throw new IOException("can't write to file " + filename +
                ": " + e.getMessage());
        }
    }

    public void write(String s) throws IOException {
        write(s, false);
    }

    public void write(String s, boolean substitute) throws IOException {
        if (substitute) {
            s = subst.substitute(s);
        }
        fileWriter.write(s);
    }

    IWriter nwload(String nlang_name) throws Janet.JanetException {
        IWriter nlang_writer = (IWriter)nwriters.get(nlang_name);
        if (nlang_writer == null) { // not yet loaded
            String clname = "pl.edu.agh.icsr.janet.natives." + nlang_name +
                            ".Writer";
            String errstr = "Unable to load writer for native language \"" +
                nlang_name + "\": class " + clname + " ";
            try {
                Class cls = Class.forName(clname);
                nlang_writer = (IWriter)cls.newInstance();
                nlang_writer.init(settings, subst, classMgr);
            } catch (ClassNotFoundException e) {
                throw new Janet.JanetException(errstr + "not found");
            } catch (IllegalAccessException e) {
                throw new Janet.JanetException(errstr + "is not public");
            } catch (InstantiationException e) {
                throw new Janet.JanetException(errstr + "can't be instantiated " +
                    "(it is abstract class or interface)");
            }
            nwriters.put(nlang_name, nlang_writer);
        }
        return nlang_writer;
    }

    public void writeNativeMethod(INativeMethodInfo mth) throws IOException {
        YYClass cls = (YYClass)mth.getDeclaringClass();
        Vector classes = cls.getReferencedClasses();
        Vector fields = cls.getReferencedFields();
        Vector fldclsidxs = cls.getRefFieldClsIdxs();
        Vector methods = cls.getReferencedMethods();
        Vector mthclsidxs = cls.getRefMethodClsIdxs();
        Vector strings = cls.getRefStringLiterals();

        YYNativeMethodImplementation nimpl = mth.getImplementation();

        write(getImplementationFunctionHeader(mth, true));
        write(";\n\n");

        write(getNativeMethodHeader(mth));
        write("{\n");

        // write data structures for primitive type arrays if required
        if (nimpl.usesPrimitiveTypeArrays()) {
            int initsize = nimpl.radkeNumbers[nimpl.getInitialRadkeIdx()];
            write("    _janet_arr _janet_arrhtdata[" + initsize +
                    "] = { { 0, 0, 0, 0, 0, 0, 0, 0, 0 } };\n");

            write("    _janet_arrHashTable _janet_arrhtable = { " +
                    nimpl.getInitialRadkeIdx() + ", 0, " +
                    (int)(initsize * 0.75) + ", 0 };\n\n");
        }
        int nsync = nimpl.getSynchronizedStatementsNum();
        if (nsync > 0) {
            write("    jobject _janet_monitors[" + nsync + "] = { 0 };\n");
        }

        String suffix = "_V";
        try {
            if (mth.getReturnType() != classMgr.VOID) {
                suffix = "_N";
                write("    " + mth.getReturnType().getJNIType() + " " +
                    "_janet_result;\n\n");
            }
        } catch (ParseException e) {
            throw new RuntimeException();
        }

        if (nimpl.usesPrimitiveTypeArrays()) {
            write("    _janet_arrhtable.data = _janet_arrhtdata;\n\n");
        }

        // linking classes
        for (Iterator i = mth.getUsedClassIdxs().iterator(); i.hasNext();) {
            int clsidx = ((Integer)i.next()).intValue();
            IClassInfo rcls = (IClassInfo)classes.get(clsidx);
            write("    _JANET_LOAD_CLASS" + suffix + "(" + clsidx + ");");
            write(settings.sourceComments()
                ? " /* " + rcls.getJNIName() + " */\n"
                : "\n");
        }

        // linking fields
        try {
            for (Iterator i = mth.getUsedFieldsIdxs().iterator(); i.hasNext();) {
                int fldidx = ((Integer)i.next()).intValue();
                IFieldInfo rfld = (IFieldInfo)fields.get(fldidx);
                int clsidx = ((Integer)fldclsidxs.get(fldidx)).intValue();
                IClassInfo rcls = (IClassInfo)classes.get(clsidx);
                write("    _JANET_LOAD_FIELD" + suffix + "(" + fldidx + ");");
                write(settings.sourceComments()
                    ? " /* " + rcls.getJNIName() + "/" + rfld.getName() + " " +
                          rfld.getType().getSignature() +
                          " */\n"
                    : "\n");
            }
        } catch (ParseException e) {
            throw new RuntimeException();
        }

        // linking methods
        try {
            for (Iterator i = mth.getUsedMethodsIdxs().iterator(); i.hasNext();) {
                int mthidx = ((Integer)i.next()).intValue();
                IMethodInfo rmth = (IMethodInfo)methods.get(mthidx);
                int clsidx = ((Integer)mthclsidxs.get(mthidx)).intValue();
                IClassInfo rcls = (IClassInfo)classes.get(clsidx);
                write("    _JANET_LOAD_METHOD" + suffix + "(" + mthidx + ");");
                write(settings.sourceComments()
                    ? " /* " + rcls.getJNIName() + "/" +
                        (rmth.isConstructor() ? "<init>" : rmth.getName()) +
                        rmth.getJNISignature() + " */\n"
                    : "\n");
            }
        } catch (ParseException e) {
            throw new RuntimeException();
        }

        // loading string literals
        for (Iterator i = mth.getUsedStringsIdxs().iterator(); i.hasNext();) {
            int stridx = ((Integer)i.next()).intValue();
            String s = (String)strings.get(stridx);
            write("    _JANET_LOAD_STRING" + suffix + "(" + stridx + ");");
            write(settings.sourceComments()
                ? " /* " + s + " */\n"
                : "\n");
        }

        // invoking implementation function
        try {
            if (mth.getReturnType() != classMgr.VOID) {
                write("    _janet_result = ");
            } else {
                write("    ");
            }

            write(getImplementationFunctionHeader(mth, false));

            write(";\n\n");

            if (nimpl.usesPrimitiveTypeArrays()) {
                write("    _jh3_releaseHashTable(_janet_jnienv, &_janet_arrhtable);\n\n");
            }

            if (nsync > 0) {
                write("    _jm1_releaseMonitors(_janet_jnienv, _janet_monitors, " +
                      nsync + ");\n\n");
            }

            if (mth.getReturnType() != classMgr.VOID) {
                write("    return _janet_result;\n");
            }
        } catch (ParseException e) {
            throw new RuntimeException();
        }

        //finished
        write("}\n\n");

        // now get the native writer and write implementation body
        try {
            String nlang_name = CompilationManager.getCanonicLanguageName(
                mth.getNativeLanguage());

            IWriter w = nwload(nlang_name);
            w.write(mth);
        } catch (Janet.JanetException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void writeStaticNativeStatement(YYStaticNativeStatement stmt)
            throws IOException {
        try {
            String nlang_name = CompilationManager.getCanonicLanguageName(
                stmt.getNativeLanguage());

            IWriter w = nwload(nlang_name);
            w.write(stmt);
        } catch (Janet.JanetException e) {
            throw new IOException(e.getMessage());
        }
    }


    static String getNativeMethodHeader(INativeMethodInfo mth) {
        Iterator i;
        try {
            return getNativeMethodHeader(mth.getDeclaringClass(),
                    (mth.getModifiers() & Modifier.STATIC) != 0,
                     mth.getReturnType(), mth.getName(),
                     isNativeMethodOverridden(mth), mth.getParameters());
        } catch (ParseException e) {
            throw new RuntimeException();
        }

    }

    static String getNativeMethodHeader(IClassInfo cls, boolean isStatic,
            IClassInfo rettype, String methodName, boolean overridden,
            YYVariableDeclarator[] parameters) {
        try {
            YYVariableDeclarator v;
            String result =
                "JNIEXPORT " + rettype.getJNIType() + " JNICALL\n" +
                "Java_" +
                ClassManager.mangle(cls.getFullName()) +
                "_" + ClassManager.mangle(methodName);

            if (overridden) {
                result += "__";
                if (parameters != null) {
                    for (int i=0; i<parameters.length; i++) {
                        result += ClassManager.mangle(parameters[i].getType().
                            getSignature());
                    }
                }
            }
            result += "(\n";
            result += Writer.makeIndent(8) + "JNIEnv* _janet_jnienv,\n" +
                Writer.makeIndent(8) +
                (isStatic ? "jclass _janet_jthisclass"
                          : "jobject _janet_jthis");

            if (parameters != null) {
                for (int i=0; i<parameters.length; i++) {
                    result += ",\n" + Writer.makeIndent(8) +
                        parameters[i].getType().getJNIType() + " " +
                        getNativeArgName(parameters[i].getName());
                }
            }
            result += ")\n";
            return result;
        } catch (ParseException e) {
            throw new IllegalStateException();
        }
    }

    public static String getNativeArgName(String name) {
        return "_janet_arg_" + ClassManager.mangle(name);
    }

    public final static boolean isNativeMethodOverridden(
            INativeMethodInfo mth) {
        Iterator i;
        try {
            i = mth.getDeclaringClass().getMethods(mth.getName()).values().
                iterator();
            int no = 0;
            while (i.hasNext()) {
                IMethodInfo m = (IMethodInfo)i.next();
                if (Modifier.isNative(mth.getModifiers())) {
                    no++;
                    if (no>1) return true;
                }
            }
            return false;
        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    public static String getImplementationFunctionHeader(
            INativeMethodInfo mth, boolean isDeclaration) {
        String s = "";
        IClassInfo cls = mth.getDeclaringClass();
        YYNativeMethodImplementation nimpl = mth.getImplementation();
        try {
            YYVariableDeclarator[] parameters = mth.getParameters();

            if (isDeclaration) {
                s += mth.getReturnType().getJNIType() + " ";
            }

            s += "Janet_" + ClassManager.mangle(cls.getFullName()) +
                    "_" + ClassManager.mangle(mth.getName());

            if (NativeWriter.isNativeMethodOverridden(mth)) {
                s += "__";
                if (parameters != null) {
                    for (int i=0; i<parameters.length; i++) {
                        s += ClassManager.mangle(parameters[i].getType().
                            getSignature());
                    }
                }
            }

            s += "(\n        ";

            if (isDeclaration) {
                s += "JNIEnv*";
            } else {
                s += "_janet_jnienv";
            }

            if (!(mth.getUsedClassIdxs().isEmpty() &&
                        mth.getUsedFieldsIdxs().isEmpty() &&
                        mth.getUsedMethodsIdxs().isEmpty())) {
                s += ",\n        ";
                if (isDeclaration) {
                    s += "_janet_cls*";
                } else {
                    s += "_janet_depclasses";
                }
            }

            if (!mth.getUsedFieldsIdxs().isEmpty()) {
                s += ",\n        ";
                if (isDeclaration) {
                    s += "_janet_fld*";
                } else {
                    s += "_janet_depfields";
                }
            }

            if (!mth.getUsedMethodsIdxs().isEmpty()) {
                s += ",\n        ";
                if (isDeclaration) {
                    s += "_janet_mth*";
                } else {
                    s += "_janet_depmethods";
                }
            }

            if(!mth.getUsedStringsIdxs().isEmpty()) {
                s += ",\n        ";
                if (isDeclaration) {
                    s += "_janet_str*";
                } else {
                    s += "_janet_depstrings";
                }
            }

            if (nimpl.usesPrimitiveTypeArrays()) {
                s += ",\n        ";
                if (isDeclaration) {
                    s += "_janet_arrHashTable*";
                } else {
                    s += "&_janet_arrhtable";
                }
            }

            if (nimpl.getSynchronizedStatementsNum() > 0) {
                s += ",\n        ";
                if (isDeclaration) {
                    s += "jobject*";
                } else {
                    s += "_janet_monitors";
                }
            }

            s += ",\n        ";
            if (isDeclaration) {
                s += (Modifier.isStatic(mth.getModifiers())
                          ? "jclass"
                          : "jobject");
            } else {
                s += (Modifier.isStatic(mth.getModifiers())
                          ? "_janet_jthisclass"
                          : "_janet_jthis");
            }

            for (int i=0; i<parameters.length; i++) {
                s += ",\n        ";
                if (isDeclaration) {
                    s += parameters[i].getType().getJNIType();
                } else {
                    s += getNativeArgName(parameters[i].getName());
                }
            }

            s += (")");
            return s;

        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

}

