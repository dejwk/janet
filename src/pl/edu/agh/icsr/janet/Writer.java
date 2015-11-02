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
import pl.edu.agh.icsr.janet.yytree.YYCompilationUnit;
import pl.edu.agh.icsr.janet.natives.IWriter;

public class Writer {

    BufferedWriter javaFileWriter, cFileWriter;
    CompilationManager compMgr;
    Janet.Settings settings;
    Hashtable writers;
    String jFilename;
    NativeWriter nativeWriter;
    Substituter subst;

    public Writer(CompilationManager compMgr, Janet.Settings settings) {
        this.compMgr = compMgr;
        this.settings = settings;
        this.subst = new Substituter();
        this.nativeWriter = new NativeWriter(this.subst, settings,
            this.compMgr.getClassManager());
        //this.writers = new Hashtable();
    }

    public NativeWriter getNativeWriter() {
        return nativeWriter;
    }

    public Substituter getSubstituter() {
        return subst;
    }

    public void write() {
        Iterator cunits = compMgr.compUnits.values().iterator();
        while (cunits.hasNext()) {
            YYCompilationUnit cunit = (YYCompilationUnit)cunits.next();
            if (!cunit.markedForProcessing()) {
                // skip this file
                continue;
            }

            jFilename = cunit.ibuf().getOriginFile().getName();

            // apply appropriate file type suffix (".java")
            jFilename = toJavaFilename(jFilename);

            try {
                File dir = getOutDirForInput(cunit.ibuf(), settings);
                File jOutput = new File(dir, jFilename);
                javaFileWriter = new BufferedWriter(new FileWriter(jOutput));
                subst.setSubst("__JAVAFILENAME__", jFilename);
                subst.setSubst("__DATE__", new Date().toString());
                cunit.write(this);
                javaFileWriter.flush();
                javaFileWriter.close();
                javaFileWriter = null;
            } catch(IOException e) {
                reportError(e.getMessage());
            } finally {
                subst.unsetSubst("__DATE__");
                subst.unsetSubst("__FILENAME__");
            }
        }
    }

    public static File getOutDirForInput(JanetSourceReader rdr, Janet.Settings settings)
        throws IOException
    {
        File srcFile = rdr.getOriginFile();
        File dir = settings.getTargetDirectory();

        if (!dir.exists()) {
            throw new IOException("Output directory: " + dir + " does not exist");
        }

        if (!srcFile.isAbsolute()) {
            String parent = srcFile.getParent();
            if (parent != null) {
                dir = new File(dir, parent);
            }
        }

        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException(dir.toString() + " is not a directory");
            }
            return dir;
        }
        // not exists
        if (!dir.mkdirs()) {
            throw new IOException("cannot create directory: " + dir);
        }
        return dir;
    }


    public void write(String s) throws IOException {
        write(s, false);
    }

    public void write(String s, boolean substitute) throws IOException {
        if (substitute) {
            s = subst.substitute(s);
        }
        try {
            javaFileWriter.write(s);
        } catch (IOException e) {
            throw new IOException("unable to write to file " + jFilename +
                ": " + e.getMessage());
        }
    }

    public static String toJavaFilename(String filename) {
        if (filename.endsWith(".janet")) {
            return filename.substring(0, filename.length()-6) + ".java";
        } else if (filename.endsWith(".java")) {
            return filename;
        } else {
            return filename + ".java";
        }
    }

    public static String makeIndent(int size) {
        String s = "";
        for (int i=0; i<size; i++) s+=" ";
        return s;
    }

    public static void reportError(String msg) {
        System.err.println(msg);
    }


    IWriter nwload(String nlang_name) throws Janet.JanetException {
        IWriter nlang_writer = (IWriter)writers.get(nlang_name);
        if (nlang_writer == null) { // not yet loaded
            String clname = "pl.edu.agh.icsr.janet.natives." + nlang_name +
                            ".Writer";
            String errstr = "Unable to load writer for native language \"" +
                nlang_name + "\": class " + clname + " ";
            try {
                Class cls = Class.forName(clname);
                nlang_writer = (IWriter)cls.newInstance();
                nlang_writer.init(settings, subst, compMgr.getClassManager());
            } catch (ClassNotFoundException e) {
                throw new Janet.JanetException(errstr + "not found");
            } catch (IllegalAccessException e) {
                throw new Janet.JanetException(errstr + "is not public");
            } catch (InstantiationException e) {
                throw new Janet.JanetException(errstr + "can't be instantiated " +
                    "(it is abstract class or interface)");
            }
            writers.put(nlang_name, nlang_writer);
        }
        return nlang_writer;
    }

    public class Substituter {
        HashMap substs = new HashMap();

        public String setSubst(String tag, String value) {
            return (String)substs.put(tag, value);
        }

        public void unsetSubst(String tag) {
            substs.remove(tag);
        }

        public String substitute(String s) {
            StringBuffer b = new StringBuffer(s.length() + 256);
            int beg = 0, pos;
            while ((pos = s.indexOf('%', beg)) >= 0) {
                b.append(s.substring(beg, pos));
                int varend = s.indexOf('%', pos+1);
                if (varend < 0) {
                    throw new RuntimeException("Mismatched '%'");
                }
                String varname = s.substring(pos+1, varend);
                String subst = (String)substs.get(varname);
                if (subst == null) {
                    throw new RuntimeException("Undefined variable " + varname);
                }
                b.append(subst);
                beg = varend+1;
            }
            b.append(s.substring(beg));
            return b.toString();
        }

    }

}

