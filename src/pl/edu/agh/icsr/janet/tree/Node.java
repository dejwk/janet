/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package pl.edu.agh.icsr.janet.tree;

import java.util.*;

/**
 * The class representing node of a tree.
 * The node can have at most one parent node and any number of
 * (bidirectionally linked) sons.
 */
public class Node implements Iterable<Node> {
    private Node parent;
    private Node first_son;
    private Node last_son;
    private Node next;
    private Node prev;

    /**
     * Construct single, separate node.
     */
    protected Node() {
        this.parent = null;
        this.first_son = null;
        this.last_son = null;
        this.next = null;
        this.prev = null;
     }

    /**
     * Makes the given node to be a rightmost son of this node.
     * @param    node       the node to be added as a rightmost son
     * @return   the given node
     */
    protected final Node addSon(Node son) {
        son.detach();
        son.attach(this, this.last_son);
        return this;
    }

    /**
     * Makes the given node to be a leftmost son of this node.
     * @param    node       the node to be added as a leftmost son
     * @return   the given node
     */
    protected Node insertSon(Node son) {
        son.detach();
        son.attach(this, null);
        return this;
    }

    /**
     * Returns the parent node of this node.
     */
    public final Node parent() {
        return parent;
    }

    /**
     * Returns the depth of this node.
     * Depth is equal to 0 for root, and to parent().depth() + 1 for any other
     * node (complexity O(depth)).
     */
    protected final int depth() {
        Node n = this;
        int i = 0;
        while (n.parent != null) { n = n.parent; i++; }
        return i;
    }

    /**
     * Returns the leftmost son of this node or null if the node has no sons.
     */
    public final Node firstSon() {
        return first_son;
    }

    /**
     * Returns the rightmost son of this node or null if the node has no sons.
     */
    public final Node lastSon() {
        return last_son;
    }

    /**
     * Returns the next brother node (a node to the right) or null, if this
     * node is at the end of the list.
     */
    public final Node nextBrother() {
        return next;
    }

    /**
     * Returns the previous brother node (a node to the left) or null, if this
     * node is at the beginning of the list.
     */
    public final Node prevBrother() {
        return prev;
    }

    /**
     * Returns the leftmost leaf of the tree, assuming that current node is the
     * root.
     */
    public final Node firstLeaf() {
        Node n = this;
        while (n.first_son != null) n = n.first_son;
        return n;
    }

    /**
     * Returns the next leaf of the tree, or null, if there is no more leafs.
     * This function allows for iterating through the leafs of the tree from
     * left to right.
     */
    public final Node nextLeaf() {
        if (next != null) {
            return next.firstLeaf();
        } else if (parent != null) {
            return parent.nextLeaf();
        } else {
            return null;
        }
    }

    /**
     * Returns the next node. If this node has any sons, the leftmost son is
     * returned. Else, if this node has any brothers to the right, the right
     * brother (a node to the right) is returned. Else, the parent node is
     * searched for right brothers. If it has none, its parent is searched for
     * right brothers and so on. If ancestor with right brother is found,
     * its right brother is returned. Else, the return is null.
     * This function allows for iterating through all nodes of the tree in
     * order: first the node, then its sons (from left to right).
     * node is searched for brothers
     */
    public final Node nextNode() {
        if (first_son != null) {
            return first_son;
        } else {
            Node n = this;
            do {
                if (n.next != null) return n.next;
                n = n.parent;
            } while (n != null);
            return null;
        }
    }

    /**
     * Gets the root of the whole tree. Root is the node which has no parent.
     * The complexity is O(n).
     */
    public final Node getRoot() {
        Node n = this;
        while (n.parent != null) n = n.parent;
        return n;
    }

    /**
     * Returns the number of sons this node has. The complexity is O(n).
     */
    public final int countSons() {
        int sons_no = 0;
        Node n = first_son;
        while (n != null) {
            sons_no++;
            n = n.next;
        }
        return sons_no;
    }
/*
    public final boolean isPredecessorOf(Node n) {
        for (; n != null; n = n.parent()) {
            if (n == this) return true;
        }
        return false;
    }

    public final boolean isSuccessorOf(Node n) {
        return (n != null && n.isPredecessorOf(this));
    }
*/
    /**
     * Deletes all sons of this node.
     */
    protected final synchronized void killSons() {
        first_son = null;
        last_son = null;
    }

    /**
     * Attaches given node to the tree at specified position.
     * @param    parent      The node which is to be parent of the node beeing
     *                       inserted.
     * @param    brother     The node which is to be the left-side brother of
     *                       the node beeing inserted or null if the node is
     *                       to be the leftmost son.
     * @return   the attached node (this node)
     */
    protected final synchronized Node attach(Node parent, Node brother) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null");
        }
        if (brother != null && brother.parent != parent) {
            throw new IllegalArgumentException("Parent and brother must " +
                                               "belong to the same tree");
        }
        synchronized(parent) {
            this.parent = parent;
            if (brother != null) {          // not at the beginning
                this.next = brother.next;
                this.prev = brother;
                brother.next = this;
                if (this.next != null) {        // not at the end
                    this.next.prev = this;
                } else {                        // at the end
                    parent.last_son = this;
                }
            } else {                        // at the beginning
                this.next = parent.first_son;
                this.prev = null;
                parent.first_son = this;
                if (this.next != null) {         // not at the end
                    this.next.prev = this;
                } else {                         // at the end
                    parent.last_son = this;
                }
            }
            return this;
        }
    }

    /**
     * Removes given node (and all its descendants) from the tree.
     * @return   the node which were the left-side brother of the detached one
     *           or null if the detached one was the leftmost brother
     */
    protected final synchronized Node detach() {
        if (this.parent == null) {
            return null;
        }
        synchronized (this.parent) {
            if (this.prev != null) {          // has left brother
                this.prev.next = this.next;
            } else {                          // was first
                parent.first_son = this.next;
            }
            if (this.next != null) {          // has right brother
                this.next.prev = this.prev;
            } else {                          // was last
                parent.last_son = this.prev;
            }

            Node n = this.prev;
            this.parent = this.prev = this.next = null;
            return n;
        }
    }

    /**
     * Returns the string representation of whole subtree, beginning at
     * this node as the root.
     */
    public static String toString(Node n) {
        StringBuffer b = new StringBuffer();
        for (int i=n.depth(); i>0; i--) b.append("    ");
        b.append(n.toString()).append('\n');
        for (Node son = n.firstSon(); son != null; son = son.nextBrother()) {
            b.append(toString(son));
        }
        return b.toString();
    }

    public Iterator<Node> iterator() {
        return new Itr();
    }

    class Itr implements Iterator<Node> {
        Node nthis = null;
        Node nnext = first_son;

        public boolean hasNext() {
            return nnext != null;
        }

        public Node next() {
            if (nnext == null) {
                throw new NoSuchElementException();
            }
            nthis = nnext;
            nnext = nnext.next;
            return nthis;
        }

        public void remove() {
            nthis.detach();
            if (nnext != null) next();
        }
    }

}

