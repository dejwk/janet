/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.yytree;

import pl.edu.agh.icsr.janet.*;
import pl.edu.agh.icsr.janet.reflect.*;
import java.util.*;

public class YYNameNode extends YYNode implements IDetailedLocationContext {

    String text;
    YYName detachedName;

    public YYNameNode(ILocationContext cxt, String text) {
        super(cxt);
        this.text = text;
    }

    final YYNameNode nextNameNode() {
        return (YYNameNode)super.nextBrother();
    }

    final YYNameNode prevNameNode() {
        return (YYNameNode)super.prevBrother();
    }

    final YYName getName() { // must be valid even after detaching
        YYName n;
        n = (YYName)parent();
        if (n == null) n = detachedName;
        return n;
    }

    public String get() {
        return this.text;
    }

    public String getQualified() {
        YYNameNode n = prevNameNode();
        return (n != null ? n.getQualified() + "." + this.text : this.text);
    }

    public String toString() { return getQualified(); }

    public ClassManager getClassManager() {
        return getName().getClassManager();
    }

    public YYCompilationUnit getCompilationUnit() {
        return getName().getCompilationUnit();
    }

    public IScope getScope() {
        return getName().getScope();
    }

    // See JLS 6.5 (p. 89)

    public YYPackage reclassifyAsPackage() {
        return new YYPackage(this.expand(getName().firstNameNode()),
                             this.getQualified());
    }

    public YYType reclassifyAsType() {
        YYNameNode nn = prevNameNode();
        if (nn == null) { // simple (or qualified if it's single type import)
            return new YYType(this, null, this.text);
        } else { // qualified
            return new YYType(this, nn.reclassifyAsPackage(), this.text);
        }
    }

    public YYExpression reclassifyAsExpression(VariableStack vs) {
        YYNameNode nn = prevNameNode();
        if (nn == null) {
            // if in scope of local variable, this is that local variable
            // else - not yet known
            YYVariableDeclarator v = vs.find(this.text);
            if (v != null) { // in scope of local variable of that name
                detachMe();
                return new YYLocalVariableAccessExpression(this, v);
            } else { // must be field of current class
                return new YYFieldAccessExpression(this, this.text);
            }
        } else { // qualified
            // this is field access, but head may not be resolved
            // (and then stays as a Name)
            YYExpression e = nn.tryReclassifyAsExpression(vs);
            detachMe();
            if (e != null) {
                return new YYFieldAccessExpression(this, e, this.text);
            } else {
                return new YYFieldAccessExpression(this, getName(), this.text);
            }
        }
    }

    public YYMethodInvocationExpression reclassifyAsMethodInvocation(
            VariableStack vs) {
        YYNameNode nn = prevNameNode();
        if (nn == null) {
            // simple method invocation (must be method of curren class)
            detachMe();
            return new YYMethodInvocationExpression(this, this.text);
        } else {
            YYExpression e = nn.tryReclassifyAsExpression(vs);
            // this is method invocation, but head may not be resolved
            // (and then stays as Name)
            if (e != null) {
                return new YYMethodInvocationExpression(this, e, this.text);
            } else {
                detachMe();
                return new YYMethodInvocationExpression(this, getName(),
                    this.text);
            }
        }
    }

    /**
     * If in scope of local variable, reclassify as subsequent field accesses
     * from that variable. Otherwise, leave as YYNameNode.
     */
    public YYExpression tryReclassifyAsExpression(VariableStack vs) {
        YYNameNode nn = prevNameNode();
        if (nn == null) { // simple name
            // if in scope of local variable, this is that local variable
            // else - not yet known
            YYVariableDeclarator v = vs.find(this.text);
            if (v != null) { // in scope of local variable of that name
                detachMe();
                return new YYLocalVariableAccessExpression(this, v);
            } else {
                return null;
            }
        } else { // qualified
            YYExpression e = nn.tryReclassifyAsExpression(vs);
            // if head is an expression, this is field access
            // else - not yet known
            if (e != null) {
                detachMe();
                return new YYFieldAccessExpression(this, e, this.text);
            } else {
                return null;
            }
        }
    }

    /**
     * When all classes are already parsed and any semantic information is
     * known (JLS 6.5.2)
     */
    public Object reclassify() throws ParseException {
        YYNameNode nn = prevNameNode();
        IClassInfo cls = null;
        ClassManager classMgr = getName().getClassManager();
        YYCompilationUnit compUnit = getCompilationUnit();

        detachMe();

        if (nn == null) { // simple name

            // maybe it is a field of current class or interface
            cls = getScope().getCurrentClass();
            Map<String, ? extends IFieldInfo> fields = cls.getFields(this.text);
            if (!fields.isEmpty()) {
                return new YYFieldAccessExpression(this, this.text);
            }

            // maybe it is a type
            try {
                cls = classMgr.tryResolveAsType("", this.text, compUnit);
            } catch (CompileException e) {
                reportError(e.getMessage());
            }
            if (cls != null) {
                return cls;
            }

            // it is a package - return its name
            return this.text;

        } else { // qualified name
            Object o = nn.reclassify();

            // package -> package or type
            if (o instanceof String) {
                try {
                    cls = classMgr.tryResolveAsType((String)o, this.text,
                        compUnit);
                } catch (CompileException e) {
                    reportError(e.getMessage());
                }
                if (cls != null) {
                    return cls; // type
                }

                return ((String)o) + "." + this.text; // package
            }

            // type -> field
            if (o instanceof IClassInfo) {
                cls = (IClassInfo)o;
                return new YYFieldAccessExpression(this, (IClassInfo)o,
                    this.text);
            }

            // field -> field
            if (o instanceof YYFieldAccessExpression) {
                return new YYFieldAccessExpression(this, (YYExpression)o,
                    this.text);
            }

            throw new RuntimeException();
        }
    }

    public void detachMe() {
        detachedName = (YYName)parent();
        detach();
    }
}

