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

package pl.edu.agh.icsr.janet.natives;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.yytree.*;
import java.io.IOException;

public interface IWriter {

    //public final static int PHASE_PREPARE = 1;
    //public final static int PHASE_WRITE = 2;

    void init(Janet.Settings settings, Writer.Substituter subst,
              ClassManager classMgr);
    //void setCurrentClass(YYClass cls);
    void write(String s) throws IOException;
    void write(INativeMethodInfo mth) throws IOException;
    void write(YYStaticNativeStatement stm) throws IOException;

    int write(YYNode s, int param) throws IOException;
    int write(YYStatement s, int param) throws IOException;

    //int write(YYVariableDeclarator v, int param) throws IOException;

    int write(YYExpression e, int param) throws IOException;
    int write(YYAssignmentExpression e, int param) throws IOException;
    int write(YYArrayAccessExpression e, int param) throws IOException;
    int write(YYArrayCreationExpression e, int param) throws IOException;
    int write(YYClassInstanceCreationExpression e, int param) throws IOException;
    int write(YYMethodInvocationExpression e, int param) throws IOException;
    int write(YYNativeMethodImplementation e, int param) throws IOException;
    int write(YYFieldAccessExpression e, int param) throws IOException;
    int write(YYInstanceOfExpression e, int param) throws IOException;
    int write(YYRelationalExpression e, int param) throws IOException;
    int write(YYIntegerLiteral e, int param) throws IOException;
    int write(YYLongLiteral e, int param) throws IOException;
    int write(YYCharacterLiteral e, int param) throws IOException;
    int write(YYFloatLiteral e, int param) throws IOException;
    int write(YYDoubleLiteral e, int param) throws IOException;
    int write(YYBooleanLiteral e, int param) throws IOException;
    int write(YYNullLiteral e, int param) throws IOException;
    int write(YYStringLiteral e, int param) throws IOException;
    int write(YYThis e, int param) throws IOException;
    int write(YYLocalVariableAccessExpression e, int param) throws IOException;
    int write(YYPtrFetchExpression e, int param) throws IOException;
    int write(YYCastExpression e, int param) throws IOException;
    int write(YYBinaryExpression s, int param) throws IOException;

    int write(YYExpressionStatement s, int param) throws IOException;
    int write(YYReturnStatement s, int param) throws IOException;
    int write(YYTryStatement s, int param) throws IOException;
    int write(YYCatchClause s, int param) throws IOException;
    int write(YYFinally s, int param) throws IOException;
    int write(YYThrowStatement s, int param) throws IOException;
    int write(YYVariableDeclaratorList s, int param) throws IOException;
    int write(YYVariableDeclarator s, int param) throws IOException;
    int write(YYSynchronizedStatement s, int param) throws IOException;

    int write(YYEnclosedNativeString e, int param) throws IOException;
    int write(YYEnclosedNativeExpression e, int param) throws IOException;
    int write(YYEnclosedNativeStatements e, int param) throws IOException;




}