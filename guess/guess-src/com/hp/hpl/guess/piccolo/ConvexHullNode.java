package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import edu.umd.cs.piccolo.activities.*;
import java.util.*;

import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.piccolo.GFrame;

public class ConvexHullNode extends PPath implements ConvexHull {

    public static final int REGULAR = 0;
    public static final int ROUNDED_RECTANGLE = 1;
    public static final int RECTANGLE = 2;
    public static final int ELLIPSE = 3;

    ArrayList nodes = null;

    Color myColor = null;

    private boolean dirtyBit = true;

    private boolean frozen = false;

    private float x3Points[] = null;
    private float y3Points[] = null;

    public float[] getX3() {
	return(x3Points);
    }

    public float[] getY3() {
	return(y3Points);
    }

    public void setFrozen(boolean state) {
	frozen = state;
	if (!frozen) {
	    refreshHull();
	}
    }

    public String toString() {
	if (nodes == null) {
	    return(null);
	} else {
	    return("hull:"+nodes.toString());
	}
    }

    public ConvexHullNode(Collection nds, Color c) {
	Iterator it = nds.iterator();
	while(it.hasNext()) {
	    GuessPNode gpn = (GuessPNode)((Node)it.next()).getRep();
	    gpn.addHullListener(this);
	}
	nodes = new ArrayList(nds.size());
	nodes.addAll(nds);
	myColor = c;
	refreshHull();
    }

    /**
     * adds the nodes to the hull
     */
    public void addNodes(Collection s) {
	Iterator it = s.iterator();
	while(it.hasNext()) {
	    GuessPNode gpn = (GuessPNode)((Node)it.next()).getRep();
	    gpn.addHullListener(this);
	}
	nodes.addAll(s);
	refreshHull();
    }

    /**
     * remove nodes from the hull
     */
    public void removeNodes(Collection s) {
	Iterator it = s.iterator();
	while(it.hasNext()) {
	    GuessPNode gpn = (GuessPNode)((Node)it.next()).getRep();
	    gpn.removeHullListener(this);
	}
	nodes.removeAll(s);
	refreshHull();
    }

    /**
     * adds a node from the hull
     */
    public void addNode(Node n) {
	nodes.add(n);
	GuessPNode gpn = (GuessPNode)((Node)n).getRep();
	gpn.addHullListener(this);
	refreshHull();
    }

    /**
     * remove nodes from the hull
     */
    public void removeNode(Node n) {
	nodes.remove(n);
	GuessPNode gpn = (GuessPNode)((Node)n).getRep();
	gpn.removeHullListener(this);
	refreshHull();
    }

    public void destroyHull() {
	removeFromParent();
	Iterator it = nodes.iterator();
	while(it.hasNext()) {
	    GuessPNode gpn = (GuessPNode)((Node)it.next()).getRep();
	    gpn.removeHullListener(this);
	}
	nodes.clear();
    }
    
    /**
     * get the nodes in this hull
     */
    public Collection getNodes() {
	return(nodes);
    }

    public void setColor(Object color) {
	if (color instanceof Color) {
	    myColor = (Color)color;
	} else {
	    myColor = (Colors.getColor((String)color,(Color)getPaint()));
	}
	super.setPaint(myColor);
    }
    

    /**
     * gets the color
     */
    public String getColor() {
	return(Colors.toString(myColor));
    }


    private int style = REGULAR;

    public void setStyle(int style) {
	if (this.style != style) {
	    this.style = style;
	    dirtyBit = true;
	    refreshHull();
	}
    }

    private void refreshHull() {
	if (dirtyBit) {
	    Node[] hull = convexHull(nodes);
	    if ((hull == null) || (hull.length == 0))
		return;
	    //System.out.println("size: " + hull.length);
	    if (style == REGULAR) {
		x3Points = new float[hull.length];
		y3Points = new float[hull.length];
		for (int i = 0 ; i < hull.length ; i++) {
		    Node gn = hull[i];
		    x3Points[i] = (float)getNodeX(gn);
		    y3Points[i] = (float)getNodeY(gn);
		}
		setPathToPolyline(x3Points,y3Points);
	    } else {
		double minx = Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double maxx = Double.MIN_VALUE;
		double maxy = Double.MIN_VALUE;
		for (int i = 0 ; i < hull.length ; i++) {
		    Node gn = hull[i];
		    minx = Math.min(minx,getNodeX(gn));
		    miny = Math.min(miny,getNodeY(gn));
		    maxx = Math.max(maxx,getNodeX(gn));
		    maxy = Math.max(maxy,getNodeY(gn));
		}
		minx -= 10;
		miny -= 10;
		maxx += 10;
		maxy += 10;
		if (style == ROUNDED_RECTANGLE) {
		    setPathTo(new RoundRectangle2D.Double(minx,miny,maxx-minx,maxy-miny,15,15));
		} else if (style == RECTANGLE) {
		    setPathTo(new Rectangle2D.Double(minx,miny,maxx-minx,maxy-miny));
		} else {
		    setPathTo(new Ellipse2D.Double(minx,miny,maxx-minx,maxy-miny));
		}
	    }
	    setPaint(myColor);
	    setStrokePaint(null);
	    //this.hull.setPenWidth(0);
	    //setStroke(null);
	    repaint();
	    dirtyBit = false;
	}
    }

    public void nodeMoved(PNode moved) {
	dirtyBit = true;
	if (!frozen) {
	    refreshHull();
	}
    }

    public static class HullNode {
	Node n;
	double angle = 0;
    }

    static class HullComp implements Comparator {
	
	public int compare(Object o1, Object o2) {
	    if (((HullNode)o1).angle < ((HullNode)o2).angle) {
		return(1);
	    } else if (((HullNode)o1).angle > ((HullNode)o2).angle) {
		return(-1);
	    } else {
		return(0);
	    }
	}
	public boolean equals(Object obj) {
	    return(true);
	}
    }

    static HullComp hc = new HullComp();

    private static double getNodeY(Node n) {
	return(n.getY() + n.getHeight()/2); 
    }

    private static double getNodeX(Node n) {
	return(n.getX() + n.getWidth()/2); 
    }

    public static Node[] convexHull(java.util.List nodes) {
	if (nodes.size() <= 2) {
	    return(null);
	}

	Node min = (Node)nodes.get(0);
	Vector v = new Vector();

	// find the min y-coordinate
	for (int i = 1 ; i < nodes.size() ; i++) {
	    Node g = (Node)nodes.get(i);
	    if (((PNode)g.getRep()).getVisible()) {
		if (getNodeY(g) < getNodeY(min)) {
		    min = g;
		}
	    }
	}

	//System.err.println("min: " + min.x + " " + min.y);

	// calculate angles
	for (int i = 0 ; i < nodes.size() ; i++) {
	    Node gi = (Node)nodes.get(i);
	    if (!((PNode)gi.getRep()).getVisible()) {
		continue;
	    }

	    if (min == gi) {
		continue;
	    }
	    double angle = 
		Math.atan2(getNodeX(gi)-getNodeX(min),
			   getNodeY(gi)-getNodeY(min));
	    HullNode hn = new HullNode();
	    hn.n = gi;
	    hn.angle = angle;
	    v.addElement(hn);
	}

	// sort
	Collections.sort(v,hc);

	for (int i = 0 ; i < v.size() ; i++) {
	   HullNode hn = (HullNode)v.elementAt(i);
	   // System.out.println(hn.n.x + " " + hn.n.y + " " + hn.angle);
	}

	Stack s = new Stack();
	HullNode minhn = new HullNode();
	minhn.n = min;
	minhn.angle = 0;

	s.push(minhn);
	if (v.size() < 2) {
	    return(null);
	}
	s.push(v.elementAt(0));
	s.push(v.elementAt(1));
	
	// main loop
	for (int i = 2 ; i < v.size() ;  i++) {
	    // System.out.println(i);
	    HullNode pi = (HullNode)v.elementAt(i);
	    HullNode top = null;
	    HullNode nexttotop = null;
	    if (!s.isEmpty()) {
		top = (HullNode)s.pop();
	    }
	    if (!s.isEmpty()) {
		nexttotop = (HullNode)s.peek();
	    }
	    if ((top != null)&& (nexttotop != null)) {
		s.push(top);
	    } else {
		break;
	    }
	    while(isLeft(pi,top,nexttotop)) {
		s.pop();

		if (!s.isEmpty()) {
		    top = (HullNode)s.pop();
		} else {
		    break;
		}
		if (!s.isEmpty()) {
		    nexttotop = (HullNode)s.peek();
		} else {
		    s.push(top);
		    break;
		}
		s.push(top);
	    }
	    //System.out.println("adding " + pi.n.x + " " + pi.n.y);
	    s.push(pi);
	}
	
	// put everything back in
	Node[] toRet = new Node[s.size()];
	int i = 0;
	while (!s.isEmpty()) {
	    HullNode hn = (HullNode)s.pop();
	    //System.err.println(i + " " + hn.n.x + " " + hn.n.y);
	    toRet[i] = hn.n;
	    i++;
	}
	return(toRet);
    }

    public static boolean isLeft(HullNode p0, HullNode p1, HullNode p2) {
	double x = 
	    (getNodeX(p1.n) - getNodeX(p0.n)) * 
	    (getNodeY(p2.n) - getNodeY(p0.n)) -
	    (getNodeX(p2.n) - getNodeX(p0.n)) * 
	    (getNodeY(p1.n) - getNodeY(p0.n));
	if (x > 0) {
	    return(true);
	} else {
	    return(false);
	}
    }
}
