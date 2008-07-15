package com.hp.hpl.guess.util.intervals;

import java.io.Serializable;
import org.python.core.*;
import com.hp.hpl.guess.util.*;

/**
 * The basic node class for the Interval Tree implementation.  It contains
 * all the magic necessary to make the interval tree work.
 * 
 */
public class IntervalNode implements Serializable {

    private Object proxyObj = null;
    
    public final Object getProxy() {
	return(proxyObj);
    }

    public String getAnnotation() {
	if (proxyObj != null) {
	    Object t = proxyObj;
	    if (proxyObj instanceof PyInstance) {
		//System.out.println(t.getClass());
		t = ((Object)(((PyInstance)t).__tojava__(Object.class)));
	    } else if (proxyObj instanceof PyList) {
		return(((PyList)proxyObj).annotation);
	    }

	    if (t instanceof AnnoHashSet) {
		return(((AnnoHashSet)t).annotation);
	    } else if (t instanceof AnnoVector) {
		return(((AnnoVector)t).annotation);
	    }
	}
	return(null);
    }

    /**
     * a basic constructor.  Used to generate a new IntervalNode with 
     * the given interval.  If for some reason high < low,
     * the values are reversed (so that low' = high and high' = low).
     * We don't check that high,low >= 0, nor do we check that 
     * high != low.  Those are, in theory legal, but I haven't
     * tested to make sure they work.
     * @param low the low mark of the interval.
     * @param high the high mark of the interval.
     * @param proxy the object this node is a proxy for
     */
    public IntervalNode(int low, int high, Object proxy) {
	// if the user gave high < low, reverse it
	if (high < low) {
	    int lowtemp = low;
	    low = high;
	    high = lowtemp;
	}

	// set the internal values
	this.low = low;
	this.high = high;
	this.max = high;
	this.min = low;

	// node is "free" floating
	this.right = nullIntervalNode;
	this.left = nullIntervalNode;
	this.p = nullIntervalNode;

	this.proxyObj = proxy;

	//System.out.println(low + " " + high + " " + proxy);
    }
    

    /**
     * a basic constructor.  Used to generate a new IntervalNode with 
     * the given interval.  If for some reason high < low,
     * the values are reversed (so that low' = high and high' = low).
     * We don't check that high,low >= 0, nor do we check that 
     * high != low.  Those are, in theory legal, but I haven't
     * tested to make sure they work.
     * @param low the low mark of the interval.
     * @param high the high mark of the interval.
     */
    public IntervalNode(int low, int high) {
	this(low,high,null);
    }

    /**
     * A constructor for the sentinel (null)IntervalNode.  
     */
    protected IntervalNode() {
	this.low = -1;
	this.high = -1;
	this.max = -1;
	this.min = -1;
	this.right = this;
	this.left = this;
	this.p = this;
    }

    /**
     * A pretty printer for the IntervalNode
     * If the node is black the low val appears in parrends
     * @return the string rep for the object including the low
     * val for the left and right children, the parent, and the
     * max and min val for the current node
     */
    public String toString() {
	//if (this.color == IntervalNode.RED)
	//  return("" + this.low + "\t" + this.high + "\tp:" + p.low +
	//   "\tl:" + this.left.low + "\tr:" +
	//   this.right.low + "\tmx:" + this.max + "\tmn:" + this.min);
	//return("(" + this.low + ")" + "\t" + this.high + "\tp:" + p.low +
	//     "\tl:" + this.left.low + "\tr:" +
	//     this.right.low + "\tm:" + this.max + "\tmn:" + this.min);
	return(low + " " + high + " " + proxyObj);
    }

    /**
     * tests if the IntervalNode contains a given value.  This is if 
     * this.low <= low2 and this.high >= high2
     * @param low2 the low value of the interval
     * @param high2 the high value of the interval
     */
    protected final boolean contains(int low2, int high2) {
	//if (((this.low >= low2) && (this.high <= high2)) ||
	//    ((low2 >= this.low) && (high2 <= this.high))) {
	if ((this.low <= low2) && (this.high >= high2)) {
	    return(true);
	}
	return(false);
    }

    /**
     * tests if the IntervalNode is contained by a given interval.  That is
     * if this.low >= low2 and this.high <= high2
     * @param low2 the low value of the interval
     * @param high2 the high value of the interval
     */
    protected final boolean isContained(int low2, int high2) {
	if ((this.low >= low2) && (this.high <= high2)) {
	    return(true);
	}
	return(false);
    }

    /**
     * tests if the IntervalNode overlaps a given interval.  That is if
     * <i>not<i> ((high2 < this.low) or (low2 > this.high))
     * @param low2 the low value of the interval
     * @param high2 the high value of the interval
     */
    protected final boolean overlaps(int low2, int high2) {
	if ((high2 < this.low) || (low2 > this.high)) { 
	    return(false);
	}
	return(true);
    }

    /**
     * tests if the IntervalNode exactly matches a given interval.  That
     * is if (low2 == this.low and high2 == this.high).
     * @param low2 the low value of the interval
     * @param high2 the high value of the interval
     */
    protected final boolean exactMatch(int low2, int high2) {
	if ((low2 == this.low) && (high2 == this.high)) { 
	    return(true);
	}
	return(false);
    }

    /**
     * sets the right child of the IntervalNode
     * @param x the right child to add
     */
    protected final void setRight(IntervalNode x) {
	this.right = x;

	// recalc max and min
	setMax();
	setMin();
    }

    /**
     * sets the left child of the IntervalNode
     * @param x the left child to add
     */
    protected final void setLeft(IntervalNode x) {
	this.left = x;

	// recalc max and min
	setMax();
	setMin();
    }

    /**
     * return the right subtree
     * @return the right subtree
     */
    protected final IntervalNode getRight() {
	return(this.right);
    }

    /**
     * return the left subtree
     * @return the left subtree
     */
    protected final IntervalNode getLeft() {
	return(this.left);
    }

    /**
     * return the node's parent
     * @return the node's parent
     */
    protected final IntervalNode getP() {
	return(this.p);
    }

    /**
     * sets the node's parent
     * @param x the new parent
     */
    protected final void setP(IntervalNode x) {
	this.p = x;

	// recalc max and min
	setMax();
	setMin();
    }

    /**
     * gets the max for the node
     * @return the max
     */
    protected final int getMax() {
	return(this.max);
    }

    /**
     * sets the max for the node. You shouldn't call this directly
     * only by using setRight() and setLeft()
     */
    protected final void setMax() {

	int prevmax = this.max;

	int i1 = this.high;
	int i2 = left.max;
	int i3 = right.max;

	if ((i1 >= i2) && (i1 >= i3)) {
	    this.max = i1;
	} else if ((i2 >= i1) && (i2 >= i3)) {
	    this.max = i2;
	} else {
	    this.max = i3;
	}
	
	if ((p != IntervalNode.nullIntervalNode) &&
	    (prevmax != this.max)) {
    	    p.setMax();
	}
    }

    /**
     * gets the min for the node
     * @return the min
     */
    protected final int getMin() {
	return(this.min);
    }

    /**
     * sets the min for the node. 
     */
    protected final void setMin() {
	int prevmin = this.min;

	int i1 = this.low;
	int i2 = left.min;
	int i3 = right.min;

	if (i2 == -1) 
	    i2 = i1;
	if (i3 == -1) 
	    i3 = i1;

	if ((i1 <= i2) && (i1 <= i3)) {
	    this.min = i1;
	} else if ((i2 <= i1) && (i2 <= i3)) {
	    this.min = i2;
	} else {
	    this.min = i3;
	}

	if ((p != IntervalNode.nullIntervalNode) &&
	    (prevmin != this.min)) {
    	    p.setMin();
	}
    }

    /**
     * given a starting point in the tree, return the minimum node
     * (i.e. the leftmost node) in the tree
     * @return the minimum node in the tree
     */
    protected final IntervalNode treeMin() {
	IntervalNode x = this;
	while(x.left != IntervalNode.nullIntervalNode) {
	    x = x.left;
	}
	return(x);
    }

    /**
     * a method to copy the values of another IntervalNode into this
     * @param y the node to copy from
     */
    public final void copyValues(IntervalNode y) {
	this.low = y.low;
	this.high = y.high;
	setMax();
	setMin();
	this.color = y.color;
    }

    /**
     * given a starting point in the tree, return the max node
     * (i.e. the rightmost node) in the tree
     * @return the max node in the tree
     */
    protected final IntervalNode treeMax() {
	IntervalNode x = this;
	while(x.right != IntervalNode.nullIntervalNode) {
	    x = x.right;
	}
	return(x);
    }

    /**
     * returns the high value
     */
    public final int getHigh() {
	return(this.high);
    }


    /**
     * returns the low value
     */
    public final int getLow() {
	return(this.low);
    }

    /**
     * the max as used by the interval tree
     */
    private int max = 0;

    /**
     * the min as used by the interval tree
     */
    private int min = 0;

    /**
     * the high value of this node's interval
     */
    protected int high = 0;

    /**
     * the low value of this node's interval
     */
    protected int low = 0;

    /**
     * the right child
     */
    private IntervalNode right;

    /**
     * the left child
     */
    private IntervalNode left;
 
    /**
     * parent
     */
    protected IntervalNode p;

    // some static variables //

    /**
     * black = true; red = false
     */
    protected boolean color = true;

    /**
     * red
     */
    protected static final boolean RED = false;

    /**
     * black
     */
    protected static final boolean BLACK = true;

    /**
     * a null node... what we use for leaves
     */
    protected static final IntervalNode nullIntervalNode = new IntervalNode();
}
