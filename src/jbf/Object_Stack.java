package jbf;

import java.util.EmptyStackException;

public
class Object_Stack extends Object_Vector {

    public
    Object
    push(Object n)
    {
        addElement(n);
        return n;
    }

    public
    Object
    pop() throws EmptyStackException
    {
        Object n = peek();
        setSize(size()-1);
        return n;
    }

    public
    Object
    peek() throws EmptyStackException
    {
        if(depth() == 0)
            throw new EmptyStackException();
        return elementAt(depth() - 1);
    }

    public
    boolean
    empty()
    {
        return size() == 0;
    }

    public
    int
    search(Object n)
    {
        int i = lastIndexOf(n);
        return (i >= 0)?size()-i:-1;
    }

/**************************************************/

    public
    void
    clear()
    {
        setSize(0);
    }

    public
    void
    popn(int n) throws EmptyStackException
    {
        int len = size();
        if(n < 0 || n > len) throw new EmptyStackException();
        setSize(len - n);
    }

    public
    void
    popto(int n) throws EmptyStackException
    {
        popn(size() - n);
    }

    public
    Object
    ith(int i) throws EmptyStackException
    {
        if(i < 0 || i >= size()) throw new EmptyStackException();
        return elementAt(i);

    }

    public
    int
    depth()
    {
        return size();
    }

    // ith from top (==tth); i <= 0
    // Primarily needed to implement bison's translation of $i references.
    public
    Object
    tth(int i) throws EmptyStackException
    {
        int len = size();
        i = - i;
        if(i < 0 || i >= len) throw new EmptyStackException();
        return elementAt(len - i - 1);
    }

    // ith out of top n
    public
    Object
    ithn(int i, int topn) throws EmptyStackException
    {
        int len = size();
        if(i < 0 || i >= topn || topn > len) throw new EmptyStackException();
        return elementAt(len - topn + i);
    }

    public
    Object
    top() throws EmptyStackException
    {
        if(size() <= 0) throw new EmptyStackException();
        return lastElement();
    }

}

