package jbf;

import java.util.*;

public
class Object_Vector {

    protected static final int defaultsize = 16;

    protected int _length;
    protected Object _contents[];
    protected int _delta;

    //////////////////////////////////////////////////

    public Object_Vector(int initsize) {this(initsize,0);}
    public Object_Vector() {this(defaultsize);}

    public
    Object_Vector(int initsize, int delta)
    {
        super();
        _contents = new Object[initsize];
        _delta = delta;
    }

    public
    Object_Vector(Object_Vector v)
    {
        this(v.size());
        System.arraycopy(v.contents(),0,_contents,0,v.size());
    }

    //////////////////////////////////////////////////

    public int capacity() {return _contents.length;}
    public int size() {return _length;}
    public int length() {return _length;}
    public boolean isEmpty() {return _length == 0;}

    //////////////////////////////////////////////////

    public Object[] contents() { return _contents;}
    public void contents(Object v[]) { _contents = v; }
    public void setLength(int newlen) {setSize(newlen);}
    public boolean contains(Object e) {return indexOf(e, 0) >= 0;}
    public int indexOf(Object e) {return indexOf(e, 0);}
    public int lastIndexOf(Object e) {return lastIndexOf(e, _length);}
    public Object firstElement() {return elementAt(0);}
    public Object lastElement() {return elementAt(_length-1);}
    public void copy(Object_Vector v) {fill(v.contents(),v.size());}
    public void fill(Object v[]) {fill(v,v.length);}
    public void fill(Object v[], int count) {fill(v,0,count);}
    public void removeAllElements() {removeElementRange(0,_length);}
    public void removeLastElement() {removeElementAt(_length-1);}
    public void addVector(Object_Vector v) {insertVectorAt(v,_length);}

    //////////////////////////////////////////////////

    protected
    void
    extendto(int sz)
    {
        int oldsz = _contents.length;
        if(sz > oldsz) {
            Object newbuf[];
            if(oldsz == 0) oldsz = defaultsize;
            int newsz = oldsz;
            if(_delta > 0) {
                while(newsz < sz) newsz += _delta;
            } else {
                while(newsz < sz) newsz *= 2;
            }
            newbuf = new Object[newsz];
            if(oldsz > 0) System.arraycopy(_contents, 0, newbuf, 0, _length);
            _contents = newbuf;
        }
    }

    public
    void
    setSize(int newsz)
    {
        if(newsz > _length) {
            extendto(newsz);
// the following is suppressed on the assumption
// that the elementype is a scalar
//	} else {
//	    int i;
//	    for(i=newsz;i < _length;i++) {
//		_contents[i] = null;
//	    }
        }
        _length = newsz;
    }

    public
    int
    indexOf(Object e, int index)
    {
        int i;
        for(i=index;i < _length;i++) {
            if(e == _contents[i]) return i;
        }
        return -1;
    }

    public
    int
    lastIndexOf(Object e, int index)
    {
        int i;
        for(i=index-1;i >= 0;i--) {
            if(e == _contents[i]) return i;
        }
        return -1;
    }

    public
    Object
    elementAt(int index)
    {
        if(index < 0 || index >= _length) {
            throw new ArrayIndexOutOfBoundsException(""+index);
        }
        return _contents[index];
    }

    public
    void
    setElementAt(Object e, int index)
    {
        if(index < 0 || index >= _length) {
            throw new ArrayIndexOutOfBoundsException(index + "");
        }
        _contents[index] = e;
    }

    public
    void
    removeElementAt(int index)
    {
        if(index < 0 || index >= _length) {
            throw new ArrayIndexOutOfBoundsException(index + "");
        }
        int i = _length - index - 1;
        if(i > 0) {
            System.arraycopy(_contents, index+1, _contents, index, i);
        }
        _length--;
// again, assume elemtype is scalar
//	_contents[_length] = null;
    }

    public
    void
    storeElementAt(Object e, int index)
    {
        if(index < 0 || index >= _length) {
            throw new ArrayIndexOutOfBoundsException(index + "");
        }
        _contents[index] = e;
    }

    public
    void
    insertVectorAt(Object_Vector v, int index)
    {
        int l = v.size();
        if(index < 0 || index > _length ) {
            throw new ArrayIndexOutOfBoundsException(index + "");
        }
        extendto(_length+l);
        System.arraycopy(_contents,index,_contents,index+l,_length - index);
        System.arraycopy(v.contents(),0,_contents,index,l);
        _length += l;
    }

    public
    void
    addElement(Object e)
    {
        extendto(_length+1);
        _contents[_length++] = e;
    }

    public
    boolean
    removeElement(Object e)
    {
        int i = indexOf(e);
        if(i >= 0) {
            removeElementAt(i);
            return true;
        }
        return false;
    }

    public
    void
    removeElementRange(int first, int count)
    {
        if(count < 0 || first < 0 || first+count > _length) {
            throw new ArrayIndexOutOfBoundsException("range "+first+"/"+count);
        }
        int mv = (_length - count);
        if(count > 0 && mv > 0) {
            System.arraycopy(_contents,first+count,_contents,0,mv);
        }
        _length -= count;
// again, assume elemtype is scalar
//	for(int i=0;i<count;i++) {_contents[_length+i] = null;}
    }

    public
    Object
    clone() throws CloneNotSupportedException
    {
        Object_Vector v = (Object_Vector)super.clone();
        Object newbuf[] = new Object[_length];
        System.arraycopy(_contents, 0, newbuf, 0, _length);
        v.contents(newbuf);
        return v;
    }

    public
    void
    fill(Object v[], int offset, int count)
    {
        setSize(count);
        System.arraycopy(v, offset, _contents, 0, count);
    }

    public
    String
    toString()
    {
        int i;
        int len = size();
        StringBuffer b = new StringBuffer();

        b.append("[");
        for(i=0;i < len;i++) {
            if(i > 0) b.append(", ");
            b.append(_contents[i]);
        }
        b.append("]");
        return b.toString();
    }


    public
    void
    insertElementAt(Object e, int index)
    {
        if(index < 0 || index >= _length + 1) {
            throw new ArrayIndexOutOfBoundsException(index + "");
        }
        extendto(_length+1);
        System.arraycopy(_contents,index,_contents,index+1,_length - index);
        _contents[index] = e;
        _length++;
    }

}
