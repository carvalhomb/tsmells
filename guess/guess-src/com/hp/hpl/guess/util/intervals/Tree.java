package com.hp.hpl.guess.util.intervals;

import java.util.Vector;

/**
 * an implementatation of a simple binary tree.
 * Nothing too exciting here... It's directly out of of CLR.
 * 
 * <P>
 */
public class Tree {
    
    /**
     * the constructor... doesn't do much
     * @return the new Tree
     */
    public Tree() {
    }
    
    /**
     * search the binary tree
     * @param x the node to start at
     * @param k the node key to find
     */
    public final IntervalNode search(IntervalNode x, int k, int k2) {
	while ((x != IntervalNode.nullIntervalNode) && (k != x.low)) {
	    if (k < x.low) {
		x = x.getLeft();
	    } else if (k > x.low) {
		x = x.getRight();
	    } else {
		if (k2 < x.high) {
		    x = x.getLeft();
		} else {
		    x = x.getRight();
		}
	    }
	}
	return(x);
    }
    
    
    /**
     * deletes a given IntervalNode z out of the tree
     * @param z the node to delete
     * @return the node the we just deleted. to "recycle" if
     * desired
     */
    public IntervalNode delete(IntervalNode z) {
	IntervalNode x,y;
	
	if ((z.getLeft() == IntervalNode.nullIntervalNode) || 
	    (z.getRight() == IntervalNode.nullIntervalNode)) {
	    y = z;
	} else {
	    y = successor(z);
	}
	
	if (y.getLeft() == IntervalNode.nullIntervalNode) {
	    x = y.getLeft();
	} else {
	    x = y.getRight();
	}
	
	if (x != IntervalNode.nullIntervalNode) 
	    x.setP(y.getP());
	
	if (y.getP() == IntervalNode.nullIntervalNode) {
	    this.root = x;
	} else {
	    if (y == y.getP().getLeft()) {
		y.getP().setLeft(x);
	    } else {
		y.getP().setRight(x);
	    }
	}

	if (y != z) {
	    z.copyValues(y);
	}
	return(y);
    }
   
    /**
     * insert the node z into the binary tree
     * @param z the node to insert
     */
    public void insert(IntervalNode z) {

	IntervalNode y = IntervalNode.nullIntervalNode;

	IntervalNode x = this.root;

	while (x != IntervalNode.nullIntervalNode) {
	    y = x;
	    if (z.low < x.low) {
	      // less than key
	      x = x.getLeft();
	    } else if (z.low > x.low) {
	      // greater than key
	      x = x.getRight();
	    } else {
	      // equal keys
	      if (z.high < x.high) {
		x = x.getLeft();
	      } else {
		x = x.getRight();
	      }
	    }
	}

	z.setP(y);
	
	if (y == IntervalNode.nullIntervalNode) {
	    this.root = z;
	} else {
	    if (z.low < y.low) {
		y.setLeft(z);
	    } else if (z.low > y.low) {
		y.setRight(z);
	    } else {
	      // equal keys
	      if (z.high < y.high) {
		y.setLeft(z);
	      } else {
		y.setRight(z);
	      }
	    }
	}
    }

    /**
     * given a starting point in the tree, return the minimum node
     * (i.e. the leftmost node) in the tree
     * @param x the node to start from
     * @return the minimum node in the tree
     */
    protected final IntervalNode treeMin(IntervalNode x) {
	return(x.treeMin());
    }

    /**
     * given a starting point in the tree, return the max node
     * (i.e. the rightmost node) in the tree
     * @param x the node to start from
     * @return the max node in the tree
     */
    protected final IntervalNode treeMax(IntervalNode x) {
	return(x.treeMax());
    }

    /**
     * walk the tree starting at node x and output the 
     * in order sequence of nodes in the tree.
     * @param x the start node
     * @param buf a prefix for the printed output
     */
    public final void inOrderWalk(IntervalNode x, String buf) {
	if (x != IntervalNode.nullIntervalNode) {
	    inOrderWalk(x.getLeft(), buf + "l");
	    System.out.println(buf + " " + x.low + " " + x.high + " " + x.getMin() + " " + x.getMax());
	    inOrderWalk(x.getRight(), buf + "r");
	}
    }

    /**
     * return the successor for a node (i.e. the smallest key
     * greater than x
     * @param x the node to compare against
     * @return the successor to x
     */
    public IntervalNode successor(IntervalNode x) {
	if (x.getRight() != IntervalNode.nullIntervalNode) {
	    return(x.getRight().treeMin());
	}
	IntervalNode y = x.getP();
	
	while ((y != IntervalNode.nullIntervalNode) && (x == y.getRight())) {
	    x = y;
	    y = y.getP();
	}

	return y;
    }

    /**
     * the root of the tree
     */
    public IntervalNode root = IntervalNode.nullIntervalNode;

}
