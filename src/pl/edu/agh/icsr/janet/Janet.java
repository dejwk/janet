/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import java.io.*;
import java.net.*;
import java.util.*;

public class Janet {

    private static final String generatedCodeLicense;

    // load the license for generated code
    static {
        try {
            Reader r = new InputStreamReader(new BufferedInputStream(
                Writer.class.getResourceAsStream("generated_code_license.txt")));
            StringBuffer buf = new StringBuffer(2048);
            int data;
            while ((data = r.read()) >= 0) {
                buf.append((char)data);
            }
            generatedCodeLicense = buf.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getGeneratedCodeLicense() {
        return generatedCodeLicense;
    }

    public static void main(String[] args) {

        Settings s = new Settings();
        PrintWriter ferr = s.getFErr();
        CompilationManager cm = new CompilationManager(s);
        int exitstatus = 0;

        try {
            int i = parseInitialParams(args, s, cm);
            int processedInputFiles = 0;

            // Phase 1: parse files
            while (i<args.length) {
                if (args[i].startsWith("-")) {
                    i = parseThroughoutParam(args, i, s, cm);
                    continue;
                }

                File inFile = new File(args[i]);
                URL url;
                try {
                    url = inFile.toURL();
                } catch (MalformedURLException e) { throw new RuntimeException(); }

                cm.parse(url, inFile, true);
                i++;
                processedInputFiles++;
            }

            if (processedInputFiles <= 0) {
                showUsageInfo();
                exitstatus = 1;
                return;
            }

            // Phase 2: resolve semantic dependences
            cm.resolve();

            if (s.dump_classes) System.out.println(cm.dumpClasses());
            if (s.dump_tree) System.out.println(cm.dumpTree());

            // Phase 3: generate output
            cm.translate();

            System.out.flush();
        }
        catch (JanetException e) {
            ferr.println(e.getMessage());
            exitstatus = 1;
        }
        catch(CompileException e) {
            ferr.println("No output generated");
            exitstatus = 1;
        }
        catch(Exception e) {
            e.printStackTrace(ferr);
            exitstatus = 1;
        }
        finally {
            ferr.flush();
            if (exitstatus > 0) System.exit(exitstatus);
        }
    }

    private Janet() {}
//    Janet(Settings settings) {
//        this.settings = settings;
//        ferr = new PrintWriter(System.err);
//    }
//
    static void showUsageInfo() {
        //System.out.println("Java Native Extensions (JANET), v1.01");
        System.out.println("Usage: janet [<options>] <source files>");
        System.out.println("where possible options include:");
        System.out.println("  -verbose[=level]         Output messages about what Janet is doing");
        System.out.println("  -classpath <path>        Specify where to find user class files");
        System.out.println("  -sourcepath <path>       Specify where to find input source files");
        System.out.println("  -d <directory>           Specify where to place generated files");
        System.out.println("  -encoding <encoding>     Specify character encoding used by source files");
        System.out.println("  -headerdir               Overrides location of Janet header files");
        System.out.println("  -noloadlibrary           Don't generate code to load native libraries");
        System.out.println("  -library <libname>       Specify the name of the library for native code");
        System.out.println("  -dumpclasses             Dumps parsed classes to the screen (debug option)");
        System.out.println("  -dumptree                Dumps parsing trees to the screen (debug option)");
        //System.out.println("  -qnames                  Generate name output files with fully qualified class names");
        System.out.println("  -comments                Put comments in output files");
        System.out.println("  -strict                  Enforce strict access checks for non-public members");
    }

    public static class JanetException extends Exception {
        JanetException(String s) { super(s); }
    }

    static int parseInitialParams(String[] args, Settings settings,
        CompilationManager cm) throws JanetException
    {
        int idx=0;
        while (idx < args.length && args[idx].startsWith("-")) {
            String param = args[idx++];
            if ("-verbose".equals(param)) {
                settings.dbg_level = 1;
            }
            else if (param.startsWith("-verbose=")) {
                String level = param.substring("-verbose=".length());
                settings.dbg_level = Settings.parseInt("verbose", level);
            }
            else if ("-classpath".equals(param) || "-cp".equals(param)) {
                if (idx >= args.length) {
                    throw new JanetException("-classpath: missing path");
                }
                String path = args[idx++];
                URL[] urlpath = path2URLs(path);
                settings.classLoader = new URLClassLoader(urlpath);
            }
            else if ("-sourcepath".equals(param) || "-sp".equals(param)) {
                if (idx >= args.length) {
                    throw new JanetException("-sourcepath: missing path");
                }
                String path = args[idx++];
                URL[] urlpath = path2URLs(path);
                settings.srcLoader = new URLClassLoader(urlpath);
            }
            else if ("-d".equals(param)) {
                if (idx >= args.length) {
                    throw new JanetException("-d: missing directory");
                }
                String dir = args[idx++];
                settings.targetDirectory = new File(dir);
            }
            else if ("-encoding".equals(param)) {
                if (idx >= args.length) {
                    throw new JanetException("-encoding: missing value");
                }
                settings.encoding = args[idx++];
            }
            else if ("-dumpclasses".equals(param)) {
                settings.dump_classes = true;
            }
            else if ("-dumptree".equals(param)) {
                settings.dump_tree = true;
            }
            else if ("-comments".equals(param)) {
                settings.source_comments = true;
            }
            else if ("-strict".equals(param)) {
                settings.strict_access = true;
            }
            else if ("-noloadlibrary".equals(param)) {
                cm.setLoadLibrary(false);
            }
            else {
                int newidx = parseThroughoutParam(args, idx-1, settings, cm);
                if (newidx <= idx-1) {
                    Janet.showUsageInfo();
                    throw new JanetException("Unrecognized option: " + param);
                }
                idx = newidx;
            }
        }
        return idx;
    }

    static int parseThroughoutParam(String[] args, int idx, Settings s,
        CompilationManager cm) throws JanetException
    {
        if ("-library".equals(args[idx])) {
            idx++;
            if (idx >= args.length) {
                throw new JanetException("-library: missing library name");
            }
            cm.setCurrentLibName(args[idx++]);
        } else if ("-headerdir".equals(args[idx])) {
            idx++;
            if (idx >= args.length) {
                throw new JanetException("-headerdir: missing directory name");
            }
            s.setHeaderDir(args[idx++]);
        }
        return idx;
    }

    private static URL[] path2URLs(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, File.pathSeparator);
        Collection<URL> urls = new ArrayList<URL>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            File file = new File(token);
            try {
                URL url = file.toURL();
                urls.add(url);
            }
            catch (java.net.MalformedURLException e) {
                System.err.print("Warning: ");
                e.printStackTrace();
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

    private static URL resolveFile(File f, Settings s) {
        if (f.isAbsolute()) {
            try {
                return f.toURL();
            } catch (MalformedURLException e) { throw new RuntimeException(); }
        }
        // relative
        ClassLoader sourceLoader = s.getSourceLoader();
        String fileNameAsRsrc = f.getPath().replace(File.separatorChar, '/');
        return sourceLoader.getResource(fileNameAsRsrc);
    }

    public static class Settings {
        private File targetDirectory = new File(System.getProperty("user.dir"));
        //boolean qnames = false;
        private int dbg_level;
        private boolean dump_classes;
        private boolean dump_tree;
        private boolean source_comments;
        private boolean strict_access;
        private String headerDir;
        private ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        private ClassLoader srcLoader = new URLClassLoader(path2URLs(
            System.getProperty("user.dir")));
        private String encoding = "ASCII";
        private PrintWriter ferr = new PrintWriter(System.err);

        public File getTargetDirectory() { return targetDirectory; }
        //public boolean getQnames() { return qnames; }
        public boolean sourceComments() { return source_comments; }
        public boolean strictAccess() { return strict_access; }
        public void setHeaderDir(String dir) { this.headerDir = dir; }
        public String getHeaderDir() { return headerDir; }

        public ClassLoader getClassLoader() { return classLoader; }
        public ClassLoader getSourceLoader() { return srcLoader; }
        public String getSrcEncoding() { return encoding; }
        public PrintWriter getFErr() { return ferr; }
        public int getDbgLevel() { return dbg_level; }

        static int parseInt(String name, String value) throws JanetException {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new JanetException("Invalid value for -" +
                    name + " parameter: \"" + value + "\" is not an integer");
            }
        }
    }
}

