/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.natives;
import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import pl.edu.agh.icsr.janet.yytree.*;
import java.io.IOException;

public interface IWriter {

    //public final static int PHASE_PREPARE = 1;
    //public final static int PHASE_WRITE = 2;

    void init(Janet.Settings settings, Writer.Substituter subst,
            ClassManager classMgr, String nlangName);
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