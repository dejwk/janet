/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import pl.edu.agh.icsr.janet.yytree.YYCompilationUnit;

public class Writer {

    BufferedWriter javaFileWriter, cFileWriter;
    CompilationManager compMgr;
    Janet.Settings settings;
    String jFilename;
    NativeWriter nativeWriter;
    Substituter subst;

    public Writer(CompilationManager compMgr, Janet.Settings settings) {
        this.compMgr = compMgr;
        this.settings = settings;
        this.subst = new Substituter();
        this.nativeWriter = new NativeWriter(this.subst, settings,
                this.compMgr.getClassManager());
    }

    public NativeWriter getNativeWriter() {
        return nativeWriter;
    }

    public Substituter getSubstituter() {
        return subst;
    }

    public void write() {
        for (YYCompilationUnit cunit : compMgr.compUnits.values()) {
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

    public class Substituter {
        HashMap<String, String> substs = new HashMap<String, String>();

        public String setSubst(String tag, String value) {
            return substs.put(tag, value);
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
                String subst = substs.get(varname);
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

