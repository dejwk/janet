package jbf;

import java.util.*;

public
class Int_Stack extends Int_Vector {

    public
    int
    push(int n)
    {
        addElement(n);
        return n;
    }

    public
    int
    pop() throws EmptyStackException
    {
        int n = peek();
        setSize(size()-1);
        return n;
    }

    public
    int
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
    search(int n)
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
    int
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
    int
    tth(int i) throws EmptyStackException
    {
        int len = size();
        i = - i;
        if(i < 0 || i >= len) throw new EmptyStackException();
        return elementAt(len - i - 1);
    }

    // ith out of top n
    public
    int
    ithn(int i, int topn) throws EmptyStackException
    {
        int len = size();
        if(i < 0 || i >= topn || topn > len) throw new EmptyStackException();
        return elementAt(len - topn + i);
    }

    public
    int
    top() throws EmptyStackException
    {
        if(size() <= 0) throw new EmptyStackException();
        return lastElement();
    }

}

