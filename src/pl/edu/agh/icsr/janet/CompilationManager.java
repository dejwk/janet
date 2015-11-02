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

import java.util.*;
import java.net.URL;
import java.io.*;
import pl.edu.agh.icsr.janet.yytree.*;
import pl.edu.agh.icsr.janet.reflect.*;

public class CompilationManager {

    Map compUnits;
    ClassManager classMgr;
    Janet.Settings settings;

    // Current (during parsing) library name as specified by the -library flag
    String currentLibName;

    public CompilationManager(Janet.Settings settings) {
        compUnits = new HashMap();
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
        Iterator i = compUnits.values().iterator();
        while (i.hasNext()) {
            ((YYCompilationUnit)i.next()).resolve();
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
        Iterator i;
        for (i = compUnits.values().iterator(); i.hasNext(); ) {
            s += ((YYCompilationUnit)i.next()).dump() + "\n\n";
        }

        s += classMgr.toString();
        return s;
    }

    public String dumpTree() {
        String s = "";
        Iterator i;
        for (i = compUnits.values().iterator(); i.hasNext(); ) {
            s += ((YYCompilationUnit)i.next()).dump() + "\n\n";
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
