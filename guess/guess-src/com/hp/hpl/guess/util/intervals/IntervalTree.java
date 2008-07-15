package com.hp.hpl.guess.util.intervals;

import java.util.Vector;

/**
 * The magic interval tree we're going to store all our nodes in...
 */
//public class IntervalTree extends RBTree {
public class IntervalTree extends RBTree {

    /**
     * main is very unexciting, but you can run it to test that
     * the interval tree is working
     * @param args ignored
     */
    public static void main(String[] args) {
	IntervalTree foo = new IntervalTree();
	foo.insert(new IntervalNode(7,11));
	foo.insert(new IntervalNode(12,13));
	foo.insert(new IntervalNode(5,10));
	foo.insert(new IntervalNode(10,14));
	foo.insert(new IntervalNode(4,10));
	foo.insert(new IntervalNode(6,14));
	foo.insert(new IntervalNode(3,8));
	foo.insert(new IntervalNode(3,8));
	IntervalNode test = new IntervalNode(1,11);
	foo.insert(test);
	foo.insert(new IntervalNode(2,13));
	foo.insert(new IntervalNode(8,9));

	foo.inOrderWalk(foo.root,"");

	IntervalNode[] retr = foo.searchOverlap(2,6);
	System.out.println("\noverlap 2-6 found:");
	for (int i = 0 ; i < retr.length ; i++) {
	    System.out.println(retr[i]);
	}

	retr = foo.searchContains(2,10);
	System.out.println("\ncontains 2-10 found:");
	for (int i = 0 ; i < retr.length ; i++) {
	    System.out.println(retr[i]);
	}

	retr = foo.searchContained(8,11);
	System.out.println("\ncontained in 8-11 found:");
	for (int i = 0 ; i < retr.length ; i++) {
	    System.out.println(retr[i]);
	}

	retr = foo.searchExact(6,14);
	System.out.println("\nexact match for 6-14 found:");
	for (int i = 0 ; i < retr.length ; i++) {
	    System.out.println(retr[i]);
	}

	foo.delete(test);
	System.out.println("");
	foo.inOrderWalk(foo.root,"");
    }

    /**
     * the constructor for an IntervalTree... doesn't do anything
     * on it's own
     */
    public IntervalTree() {
	super();
    }


    /**
     * for all nodes i in the interval tree, return i where
     * <i>not<i> ((high2 < i.low) or (low2 > i.high)).  That
     * is, all nodes that overlap the interval (low,high)
     * @param low the low value of the interval
     * @param high the high value of the interval
     */
    public IntervalNode[] searchOverlap(int low, int high) {
	IntervalNode x = super.root;
	Vector toReturn = new Vector();
	searchOverlap(low,high,super.root,toReturn);
	IntervalNode[] returnIntervalNodes = new IntervalNode[toReturn.size()];
	toReturn.copyInto(returnIntervalNodes);
	return(returnIntervalNodes);
    }

    /**
     * the recursive step of the procedure.  It's the
     * thing that gets back <i> all the matches rather
     * than just one
     * @param low the low value of the interval
     * @param high the high value of the interval
     * @param x the node at which we start testing for matches
     * @param v the vector that holds the matches
     */
    private void searchOverlap(int low, int high, IntervalNode x, Vector v) {
	if (x != IntervalNode.nullIntervalNode) {
	    if (x.overlaps(low,high)) {
		v.addElement(x);
	    }
	    if ((x.getLeft() != IntervalNode.nullIntervalNode) &&
		(x.getLeft().getMax() >= low)) {
		//x = x.getLeft();
		searchOverlap(low,high,x.getLeft(),v);
	    }
	    if ((x.getRight() != IntervalNode.nullIntervalNode) &&
		(x.getRight().getMin() <= high)) {
		//x = x.getLeft();
		searchOverlap(low,high,x.getRight(),v);
	    } 
	}
    }

    /**
     * for all nodes i in the interval tree, return i where
     * i.low <= low and i.high >= high.  That
     * is, all nodes that this interval contains
     * @param low the low value of the interval
     * @param high the high value of the interval
     */
    public IntervalNode[] searchContains(int low, int high) {
	IntervalNode x = super.root;
	Vector toReturn = new Vector();
	searchContains(low,high,super.root,toReturn);
	if (toReturn.size() == 0) {
	    return(new IntervalNode[0]);
	}
	IntervalNode[] returnIntervalNodes = new IntervalNode[toReturn.size()];
	toReturn.copyInto(returnIntervalNodes);
	return(returnIntervalNodes);
	//if (x.contains(low,high));
    }

    /**
     * the recursive step of the procedure.  It's the
     * thing that gets back <i> all the matches rather
     * than just one
     * @param low the low value of the interval
     * @param high the high value of the interval
     * @param x the node at which we start testing for matches
     * @param v the vector that holds the matches
     */
    private void searchContains(int low, int high, IntervalNode x, Vector v) {
	if (x != IntervalNode.nullIntervalNode) {
	    // System.out.println(x + " " + x.low + " " + x.high + " " + x.contains(low,high));
	    if (x.contains(low,high)) {
		//System.out.println("adding: " + x);
		v.addElement(x);
	    }
	    //System.out.println(x.getLeft().getMax());
	    if ((x.getLeft() != IntervalNode.nullIntervalNode) &&
		(x.getLeft().getMax() >= low)) {
		//	    if (x.getLeft() != IntervalNode.nullIntervalNode) {
		//x = x.getLeft();
		searchContains(low,high,x.getLeft(),v);
	    }
	    
	    //System.out.println(x.getRight().getMin());
	    if ((x.getRight() != IntervalNode.nullIntervalNode) &&
		(x.getRight().getMin() <= high)) {
	    //x = x.getLeft();
		//if (x.getRight() != IntervalNode.nullIntervalNode) {
		searchContains(low,high,x.getRight(),v);
	    } 
	}
    }

    /**
     * for all nodes i in the interval tree, return i where
     * i.low >= low and i.high <= high.  That
     * is, all nodes that are contained by this interval
     * @param low the low value of the interval
     * @param high the high value of the interval
     */
    public IntervalNode[] searchContained(int low, int high) {
	IntervalNode x = super.root;
	Vector toReturn = new Vector();
	searchContained(low,high,super.root,toReturn);
	IntervalNode[] returnIntervalNodes = new IntervalNode[toReturn.size()];
	toReturn.copyInto(returnIntervalNodes);
	return(returnIntervalNodes);
    }

    /**
     * the recursive step of the procedure.  It's the
     * thing that gets back <i> all the matches rather
     * than just one
     * @param low the low value of the interval
     * @param high the high value of the interval
     * @param x the node at which we start testing for matches
     * @param v the vector that holds the matches
     */
    private void searchContained(int low, int high, IntervalNode x, Vector v) {
	if (x != IntervalNode.nullIntervalNode) {
	    if (x.isContained(low,high)) {
		v.addElement(x);
	    }
	    if ((x.getLeft() != IntervalNode.nullIntervalNode) &&
		(x.getLeft().getMax() >= low)) {
		//x = x.getLeft();
		searchContained(low,high,x.getLeft(),v);
	    }
	    if ((x.getRight() != IntervalNode.nullIntervalNode) &&
		(x.getRight().getMin() <= high)) {
		//x = x.getLeft();
		searchContained(low,high,x.getRight(),v);
	    } 
	}
    }

    /**
     * for all nodes i in the interval tree, return i where
     * if (low == i.low and high == i.high).  That is, all 
     * nodes that have the exact same low and high values.
     * @param low the low value of the interval
     * @param high the high value of the interval
     */
    public IntervalNode[] searchExact(int low, int high) {
	IntervalNode x = super.root;
	Vector toReturn = new Vector();
	searchExact(low,high,super.root,toReturn);
	IntervalNode[] returnIntervalNodes = new IntervalNode[toReturn.size()];
	toReturn.copyInto(returnIntervalNodes);
	return(returnIntervalNodes);
    }

    /**
     * the recursive step of the procedure.  It's the
     * thing that gets back <i> all the matches rather
     * than just one
     * @param low the low value of the interval
     * @param high the high value of the interval
     * @param x the node at which we start testing for matches
     * @param v the vector that holds the matches
     */
    private void searchExact(int low, int high, IntervalNode x, Vector v) {
	if (x != IntervalNode.nullIntervalNode) {
	    if (x.exactMatch(low,high)) {
		v.addElement(x);
	    }
	    if ((x.getLeft() != IntervalNode.nullIntervalNode) &&
		(x.getLeft().getMax() >= low)) {
		//x = x.getLeft();
		searchExact(low,high,x.getLeft(),v);
	    }
	    if ((x.getRight() != IntervalNode.nullIntervalNode) &&
		(x.getRight().getMin() <= high)) {
		//x = x.getLeft();
		searchExact(low,high,x.getRight(),v);
	    } 
	}
    }
}
