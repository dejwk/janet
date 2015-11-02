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

import java.util.EmptyStackException;

public
class YYLocationStack {

    protected static final int defaultsize = 16;
    protected static final int FACTOR = 4;

    protected int _length;
    protected int _contents[];

    public YYLocationStack() {
        _contents = new int[defaultsize * FACTOR];
    }

    protected final void extendto(int sz)
    {
	int oldsz = _contents.length;
        if (sz > oldsz) {
	    int newbuf[];
	    if(oldsz == 0) oldsz = defaultsize * FACTOR;
	    int newsz = oldsz;
            while(newsz < sz) newsz *= 2;
	    newbuf = new int[newsz];
	    if(oldsz > 0) {
                System.arraycopy(_contents, 0, newbuf, 0, _length * FACTOR);
            }
	    _contents = newbuf;
	}
    }

    public final int size() {
        return _length;
    }

    public final void setSize(int newsz) {
	if(newsz > _length) {
	    extendto(newsz * FACTOR);
	}
	_length = newsz;
    }

    public final YYLocation elementAt(int index) {
	if(index < 0 || index >= _length) {
	    throw new ArrayIndexOutOfBoundsException(""+index);
	}
        int pos = index * FACTOR;
        return new YYLocation(_contents[pos++], _contents[pos++],
              _contents[pos++], _contents[pos++]);
    }

    public final YYLocation elementAt(int index, YYLocation l) {
	if(index < 0 || index >= _length) {
	    throw new ArrayIndexOutOfBoundsException(""+index);
	}
        int pos = index * FACTOR;
        l.set(_contents[pos++], _contents[pos++],
              _contents[pos++], _contents[pos++]);
        return l;
    }

    public final void push(YYLocation l) {
        int pos = _length * FACTOR;
        extendto(pos + FACTOR);
        _length++;
        _contents[pos++] = l.lineno;
        _contents[pos++] = l.charno;
        _contents[pos++] = l.charno0;
        _contents[pos++] = l.line_beg;
    }

    public final YYLocation pop() throws EmptyStackException {
	YYLocation l = peek();
	setSize(size()-1);
	return l;
    }

    public final YYLocation peek() throws EmptyStackException {
	if (size() == 0)
	    throw new EmptyStackException();
	return elementAt(size() - 1);
    }

    public final boolean empty() {
	return size() == 0;
    }

    public void clear() {
	setSize(0);
    }

    public final void popn(int n) throws EmptyStackException
    {
	int len = size();
	if(n < 0 || n > len) throw new EmptyStackException();
	setSize(len - n);
    }

    public final void popto(int n) throws EmptyStackException {
	popn(size() - n);
    }

    public final YYLocation ith(int i) throws EmptyStackException {
	if (i < 0 || i >= size()) throw new EmptyStackException();
	return elementAt(i);
    }

    public final YYLocation ith(int i, YYLocation buf)
            throws EmptyStackException {
	if (i < 0 || i >= size()) throw new EmptyStackException();
	return elementAt(i, buf);
    }

    public final int depth() {
        return size();
    }

    public final YYLocation tth(int i) throws EmptyStackException {
	int len = size();
	i = - i;
	if(i < 0 || i >= len) throw new EmptyStackException();
	return elementAt(len - i - 1);
    }

    public final YYLocation tth(int i, YYLocation buf)
            throws EmptyStackException {
	int len = size();
	i = - i;
	if(i < 0 || i >= len) throw new EmptyStackException();
	return elementAt(len - i - 1, buf);
    }

    public final YYLocation top() throws EmptyStackException {
	return tth(0);
    }

}

