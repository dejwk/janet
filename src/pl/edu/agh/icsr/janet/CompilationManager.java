/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import pl.edu.agh.icsr.janet.reflect.ClassManager;
import pl.edu.agh.icsr.janet.yytree.YYCompilationUnit;

public class CompilationManager {

    Map<URL, YYCompilationUnit> compUnits;
    ClassManager classMgr;
    Janet.Settings settings;

    // Whether to generate the code to load native libraries.
    boolean loadLibrary = true;

    // Current (during parsing) library name as specified by the -library flag
    String currentLibName;

    public CompilationManager(Janet.Settings settings) {
        compUnits = new HashMap<URL, YYCompilationUnit>();
        this.settings = settings;
        classMgr = new ClassManager(this, settings);
    }

    /**
     * Phase 1: parse files
     * @param input
     * @param markForTranslation
     */
    public void parse(URL originURL, File originFile,
                      boolean markForTranslation)
        throws ParseException, IOException
    {
        if (compUnits.containsKey(originURL)) {
            // was parsed already
            return;
        }
        JanetSourceReader jreader = new JanetSourceReader(originURL, originFile,
            1024, settings.getSrcEncoding());
        Preprocessor jp = new Preprocessor(jreader);
        Lexer jl = new Lexer(jp, settings.getFErr(), settings.getDbgLevel());
        Parser parser = new Parser(jl, settings.getFErr());

        parser.setdebug(settings.getDbgLevel());
        parser.yyerrthrow = true;
        parser.yyparse(this, markForTranslation);
    }

    public void setLoadLibrary(boolean loadLibrary) {
        this.loadLibrary = loadLibrary;
    }

    public boolean getLoadLibrary() {
        return loadLibrary;
    }

    public void setCurrentLibName(String libName) {
        this.currentLibName = libName;
    }

    public String getCurrentLibName() {
        return currentLibName;
    }

    /**
     * Phase 2: resolve semantic dependences
     * @param fileName
     * @param isInput
     */
    public void resolve() throws ParseException {
        for (YYCompilationUnit unit : compUnits.values()) {
            unit.resolve();
        }
    }

    /**
     * Phase 3: write output
     * @param fileName
     * @param isInput
     */
    void translate() {
        Writer w = new Writer(this, settings);
        w.write();
    }




    public void addCompilationUnit(YYCompilationUnit unit) {
        compUnits.put(unit.ibuf().getOriginURL(), unit);
    }

    public String toString() {
        String s = "";
        for (YYCompilationUnit unit : compUnits.values()) {
            s += unit.dump() + "\n\n";
        }

        s += classMgr.toString();
        return s;
    }

    public String dumpTree() {
        String s = "";
        for (YYCompilationUnit unit : compUnits.values()) {
            s += unit.dump() + "\n\n";
        }

        return s;
    }

    public String dumpClasses() {
        return classMgr.toString();
    }
    public ClassManager getClassManager() {
        return classMgr;
    }

    public static String getCanonicLanguageName(String language) {
        String s = language.toLowerCase();
        s = replace(s, "+", "plus");
        s = replace(s, "-", "minus");
        return s;
    }

    /**
     * Helper method
     * @param s
     * @param what
     * @param for_what
     * @return
     */
    private static String replace(String s, String what, String for_what) {
        StringBuffer b = new StringBuffer(s);
        int pos;
        while (true) {
            pos = b.toString().indexOf(what);
            if (pos < 0) break;
            b.replace(pos, pos+1, for_what);
        }
        return b.toString();
    }
}
