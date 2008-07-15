package com.hp.hpl.guess.util.intervals;

/**
 * an implementatation of a Red-Black tree so that we can keep our interval
 * tree balanced.  Nothing too exciting here... It's directly out of
 * of CLR.
 * 
 */
public class RBTree extends Tree {

    /**
     * Simple constructor
     * @return a new RBTree
     */
    public RBTree() {
	super();
    }

    /**
     * performs the leftRotation step starting at a given node
     * @param x the node at which rotation needs to occur
     */
    private final void leftRotate(IntervalNode x) {
	if (x.getRight() == IntervalNode.nullIntervalNode)
	    return;

	// set y
	IntervalNode y = x.getRight();

	// turn y's left subtree into x's right subtree
	x.setRight(y.getLeft());

	if (y.getLeft() != IntervalNode.nullIntervalNode)
	    y.getLeft().setP(x);

	y.setP(x.getP());

	// link x's parent to y
	if (x.getP() == IntervalNode.nullIntervalNode) {
	    super.root = y;
	} else {
	    if (x == x.getP().getLeft()) {
		x.getP().setLeft(y);
	    } else {
		x.getP().setRight(y);
	    }
	}
	
	// put x on y's left
	y.setLeft(x);
	x.setP(y);
    }

    /**
     * performs the rightRotation step starting at a given node
     * @param y the node at which rotation needs to occur
     */	
    private final void rightRotate(IntervalNode x) {

	//System.out.println("right " + x);

	if (x.getLeft() == IntervalNode.nullIntervalNode)
	    return;


	IntervalNode y = x.getLeft();

	// turn x's left subtree into y's right subtree
	x.setLeft(y.getRight());

	if (y.getRight() != IntervalNode.nullIntervalNode) 
	    y.getRight().setP(x);
	
	y.setP(x.getP());

	if (x.getP() == IntervalNode.nullIntervalNode) {
	    super.root = y;
	} else {
	    if (x == x.getP().getRight()) {
		x.getP().setRight(y);
	    } else {
		x.getP().setLeft(y);
	    }
	}

	y.setRight(x);
	x.setP(y);
    }

    /**
     * inserts a node into the tree and then performs the operations
     * necessary to balance the tree
     * @param x the new node to insert
     */
    public void insert(IntervalNode x) {
	// tree insert
	super.insert(x);
	
	IntervalNode y;
	x.color = IntervalNode.RED;
	
	while ((x != super.root) && (x.getP().color == IntervalNode.RED)) {
	    if (x.getP() == x.getP().getP().getLeft()) {
		y = x.getP().getP().getRight();
		if (y.color == IntervalNode.RED) {
		    x.getP().color = IntervalNode.BLACK;
		    y.color = IntervalNode.BLACK;
		    x.getP().getP().color = IntervalNode.RED;
		    x = x.getP().getP();
		} else {
		    if (x == x.getP().getRight()) {
			x = x.getP();
			leftRotate(x);
		    }
		    x.getP().color = IntervalNode.BLACK;
		    x.getP().getP().color = IntervalNode.RED;
		    rightRotate(x.getP().getP());
		}
	    } else {
		//System.out.println("1");
		y = x.getP().getP().getLeft();
		if (y.color == IntervalNode.RED) {
			    //System.out.println("2");
		  x.getP().color = IntervalNode.BLACK;
		  y.color = IntervalNode.BLACK;
		  x.getP().getP().color = IntervalNode.RED;
		  x = x.getP().getP();
		} else {
		    //System.out.println("3");
		    if (x == x.getP().getLeft()) {
			//System.out.println("4");
			x = x.getP();
			rightRotate(x);
		    }
		    x.getP().color = IntervalNode.BLACK;
		    x.getP().getP().color = IntervalNode.RED;
		    leftRotate(x.getP().getP());
		}
	    }
	}
	super.root.color = IntervalNode.BLACK;
    }

    /**
     * deletes the given node z from the tree and does the 
     * balancing necessary to maintain tree height
     * @param z the node to delete
     */
    public IntervalNode delete(IntervalNode z) {
	IntervalNode y;
	IntervalNode x;

	if ((z.getLeft() == IntervalNode.nullIntervalNode) || 
	    (z.getRight() == IntervalNode.nullIntervalNode)) {
	    y = z;
	} else {
	    y = super.successor(z);
	}
	
	if (y.getLeft() != IntervalNode.nullIntervalNode) {
	    x = y.getLeft();
	} else {
	    x = y.getRight();
	}

	x.setP(y.getP());

	if (y.getP() == IntervalNode.nullIntervalNode) {
	    super.root = x;
	} else {
	    if (y == y.getP().getLeft()) {
		y.getP().setLeft(x);
	    } else {
		y.getP().setRight(x);
	    }
	}

	if (y != z) {
	    z.low = y.low;
	}
	
	if (y.color = IntervalNode.BLACK) 
	    deleteFixup(x);

	return y;
    }
    
    /**
     * a continuation of the delete operation.  We need to fix up
     * random things to maintain balance
     * @param x the node where the fixup should start
     */
    private final void deleteFixup(IntervalNode x) {
	IntervalNode w;

	while ((x != IntervalNode.nullIntervalNode) && (x.color == IntervalNode.BLACK)) {
	    if (x == x.getP().getLeft()) {
		w = x.getP().getRight();
		if (w.color == IntervalNode.RED) {
		    w.color = IntervalNode.BLACK;
		    x.getP().color = IntervalNode.RED;
		    leftRotate(x.getP());
		}
		if ((w.getLeft().color == IntervalNode.BLACK) && 
		    (w.getRight().color == IntervalNode.BLACK)) {
		    w.color = IntervalNode.RED;
		    x = x.getP();
		} else {
		    if (w.getRight().color == IntervalNode.BLACK) {
			w.getLeft().color = IntervalNode.BLACK;
			w.color = IntervalNode.RED;
			rightRotate(w);
			w = x.getP().getRight();
		    }
		    w.color = x.getP().color;
		    x.getP().color = IntervalNode.BLACK;
		    w.getRight().color = IntervalNode.BLACK;
		    leftRotate(x.getP());
		    x = super.root;
		}
	    } else {
		// same as above, right and left exchanged
		w = x.getP().getLeft();
		if (w.color == IntervalNode.RED) {
		    w.color = IntervalNode.BLACK;
		    x.getP().color = IntervalNode.RED;
		    leftRotate(x.getP());
		}
		if ((w.getRight().color == IntervalNode.BLACK) && 
		    (w.getLeft().color == IntervalNode.BLACK)) {
		    w.color = IntervalNode.RED;
		    x = x.getP();
		} else {
		    if (w.getLeft().color == IntervalNode.BLACK) {
			w.getRight().color = IntervalNode.BLACK;
			w.color = IntervalNode.RED;
			rightRotate(w);
			w = x.getP().getLeft();
		    }
		    w.color = x.getP().color;
		    x.getP().color = IntervalNode.BLACK;
		    w.getLeft().color = IntervalNode.BLACK;
		    leftRotate(x.getP());
		    x = super.root;
		}
	    }
	}
	x.color = IntervalNode.BLACK;
    }

}



