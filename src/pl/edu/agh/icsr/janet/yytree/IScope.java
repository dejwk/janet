/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;

public interface IScope extends ILocationContext {

    int ERROR                = -1;

    int COMPILATION_UNIT             = 0x0001;
    int STATIC_CLASS                 = 0x0002;
    int INSTANCE_CLASS               = 0x0004;
    int INTERFACE                    = 0x0008;

    int CONSTRUCTOR                  = 0x0010;
    int INSTANCE_METHOD              = 0x0020;
    int INSTANCE_INITIALIZER         = 0x0040;
    int STATIC_METHOD                = 0x0080;
    int STATIC_INITIALIZER           = 0x0100;

    int STATEMENT                    = 0x0200;
    int BLOCK                        = 0x0400;

    int NATIVE_METHOD_IMPLEMENTATION = 0x0800;

    int CLASS_OR_INTERFACE = STATIC_CLASS | INSTANCE_CLASS | INTERFACE;
    int MEMBER = STATIC_METHOD | STATIC_INITIALIZER | INSTANCE_METHOD |
                     INSTANCE_INITIALIZER | CONSTRUCTOR;

    int INSTANCE_CONTEXT = CONSTRUCTOR | INSTANCE_METHOD | INSTANCE_INITIALIZER;
    int STATIC_CONTEXT =  STATIC_METHOD | STATIC_INITIALIZER;

    int getScopeType();
    IScope getEnclosingScope();
    IScope getCurrentMember();
    YYClass getCurrentClass();

    void resolve() throws ParseException;
}
