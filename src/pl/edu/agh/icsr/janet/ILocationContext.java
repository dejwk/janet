/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet;

import java.net.URL;
import java.io.File;
import pl.edu.agh.icsr.janet.yytree.*;

public interface ILocationContext {

    public JanetSourceReader ibuf();
    public YYLocation lbeg();
    public YYLocation lend();

    public void reportError(String msg) throws CompileException;
}
