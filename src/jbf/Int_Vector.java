package jbf;


import java.util.*;

public
class Int_Vector {

    protected static final int defaultsize = 16;

    protected int _length;
    protected int _contents[];
    protected int _delta;

    //////////////////////////////////////////////////

    public Int_Vector(int initsize) {this(initsize,0);}
    public Int_Vector() {this(defaultsize);}

    public
    Int_Vector(int initsize, int delta)
    {
        super();
        _contents = new int[initsize];
        _delta = delta;
    }

    public
    Int_Vector(Int_Vector v)
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

    public int[] contents() { return _contents;}
    public void contents(int v[]) { _contents = v; }
    public void setLength(int newlen) {setSize(newlen);}
    public boolean contains(int e) {return indexOf(e, 0) >= 0;}
    public int indexOf(int e) {return indexOf(e, 0);}
    public int lastIndexOf(int e) {return lastIndexOf(e, _length);}
    public int firstElement() {return elementAt(0);}
    public int lastElement() {return elementAt(_length-1);}
    public void copy(Int_Vector v) {fill(v.contents(),v.size());}
    public void fill(int v[]) {fill(v,v.length);}
    public void fill(int v[], int count) {fill(v,0,count);}
    public void removeAllElements() {removeElementRange(0,_length);}
    public void removeLastElement() {removeElementAt(_length-1);}
    public void addVector(Int_Vector v) {insertVectorAt(v,_length);}

    //////////////////////////////////////////////////

    protected
    void
    extendto(int sz)
    {
        int oldsz = _contents.length;
        if(sz > oldsz) {
            int newbuf[];
            if(oldsz == 0) oldsz = defaultsize;
            int newsz = oldsz;
            if(_delta > 0) {
                while(newsz < sz) newsz += _delta;
            } else {
                while(newsz < sz) newsz *= 2;
            }
            newbuf = new int[newsz];
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
//		_contents[i] = 0;
//	    }
        }
        _length = newsz;
    }

    public
    int
    indexOf(int e, int index)
    {
        int i;
        for(i=index;i < _length;i++) {
            if(e == _contents[i]) return i;
        }
        return -1;
    }

    public
    int
    lastIndexOf(int e, int index)
    {
        int i;
        for(i=index-1;i >= 0;i--) {
            if(e == _contents[i]) return i;
        }
        return -1;
    }

    public
    int
    elementAt(int index)
    {
        if(index < 0 || index >= _length) {
            throw new ArrayIndexOutOfBoundsException(""+index);
        }
        return _contents[index];
    }

    public
    void
    setElementAt(int e, int index)
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
//	_contents[_length] = 0;
    }

    public
    void
    storeElementAt(int e, int index)
    {
        if(index < 0 || index >= _length) {
            throw new ArrayIndexOutOfBoundsException(index + "");
        }
        _contents[index] = e;
    }

    public
    void
    insertVectorAt(Int_Vector v, int index)
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
    addElement(int e)
    {
        extendto(_length+1);
        _contents[_length++] = e;
    }

    public
    boolean
    removeElement(int e)
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
//	for(int i=0;i<count;i++) {_contents[_length+i] = 0;}
    }

    public
    Object
    clone() throws CloneNotSupportedException
    {
        Int_Vector v = (Int_Vector)super.clone();
        int newbuf[] = new int[_length];
        System.arraycopy(_contents, 0, newbuf, 0, _length);
        v.contents(newbuf);
        return v;
    }

    public
    void
    fill(int v[], int offset, int count)
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
    insertElementAt(int e, int index)
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
