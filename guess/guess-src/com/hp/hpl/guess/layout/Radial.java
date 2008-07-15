// $Id: Radial.java,v 1.1 2005/10/05 20:19:39 eytanadar Exp $
package com.hp.hpl.guess.layout;

import java.util.*;
import com.hp.hpl.guess.*;
import java.awt.geom.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

/**
 * The layout computePositions method follows the algorithm
 * as given by Eades in his paper "Drawing Free Trees",
 * Bulletin of the Institute for Combinatorics and its Applications,
 * vol. 5, 10-36, 1992.
 *
 * Note: as described by Eades, the algorithm does not allow variable
 * node size.
 *
 * @author Hacked by Eytan Adar for Guess
 */
public class Radial extends AbstractLayout {
    
    protected double layerDistance = 10;

    Node center = null; 
    Graph tree = null; 
    HashSet ve = null;

    HashMap locations = new HashMap();

    /** 
     * Constructor  
     */
    public Radial(Graph tree, Node center, HashSet ve) {
	super(tree);
	this.tree = tree;
	this.center = center;
	this.ve = ve;
	Iterator it = tree.getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    locations.put(n, new Coordinates(n.getX(),
					     n.getY()));
	}
    }

    public Radial(Graph tree, Node center) {
	this(tree,center,null);
    }

    private Graph graph = null;

    Hashtable coords = new Hashtable();
    Hashtable radialWidth = new Hashtable();
    HashSet seen = new HashSet();
    HashSet validEdges = new HashSet();

    // this one takes into account a predefined set of edges
    public Vector getNextLayerEdgesPredef(Node center) {
	Vector frontier = new Vector();
	Iterator it = center.getOutEdges().iterator();
	seen.add(center);
	while(it.hasNext()) {
	    Edge e = (Edge)it.next();
	    if (validEdges.contains(e)) {
		Node n = (Node)e.getOpposite(center);
		if ((n != center) && (!seen.contains(n))) {
		    //e.getRep().set("width",new Double(5));
		    frontier.addElement(n);
		    seen.add(n);
		}
	    }
	}
	return(frontier);
    }

    public Vector getNextLayer(Node center) {
	Vector frontier = new Vector();
	Iterator it = center.getOutEdges().iterator();
	seen.add(center);
	while(it.hasNext()) {
	    Edge e = (Edge)it.next();
	    Node n = (Node)e.getOpposite(center);
	    if ((n != center) && (!seen.contains(n))) {
		validEdges.add(e);
		//e.getRep().set("width",new Double(5));
		frontier.addElement(n);
		seen.add(n);
	    }
	}
	return(frontier);
    }


    public void advancePositions() {

	if (done)
	    return;
	
	boolean predef = false;

	if (ve != null) {
	    predef = true;
	    validEdges = ve;
	}

    
	Vector front = null;
	if (!predef) {
	    front = getNextLayer(center);
	} else {
	    front = getNextLayerEdgesPredef(center);
	}
	while(front.size() > 0) {
	    Vector nextLayer = new Vector();
	    for (int i = 0 ; i < front.size() ; i++) {
		if (!predef) {
		    nextLayer.addAll((Vector)getNextLayer((Node)front.elementAt(i)));
		} else {
		    nextLayer.addAll((Vector)getNextLayerEdgesPredef((Node)front.elementAt(i)));
		}
	    }
	    front = nextLayer;
	}

	seen.clear();

	Iterator it = tree.getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    layerDistance = 
		Math.max(Math.max(layerDistance,n.getWidth()*5),
			 n.getHeight()*5);
	}

	graph = tree;
	double baseX = 0.0; 
	//System.out.println("\tsetting width prop...");
	defineWidthProperty(center,null);
	//System.out.println("\tsetting laying out...");

	double rho = 0.0;
	double alpha1 = 0.0;
	double alpha2 = 2 * Math.PI;
	
	Point2D nodeCoord = polarToCartesian(rho, 
					     (alpha1 + alpha2) / 2, baseX);
	coords.put(center,nodeCoord);
	
	int centerWidth = ((Integer)radialWidth.get(center)).intValue();
	rho += layerDistance;
	Iterator neighbors = ((Node)center).getOutEdges().iterator();
	while (neighbors.hasNext()) {
	    Edge e = (Edge)neighbors.next();
	    if (!validEdges.contains(e)) {
		continue;
	    }
	    Node neighbor = (Node)e.getOpposite(center);
	    int neighborWidth = 
		((Integer)radialWidth.get(neighbor)).intValue();
	    alpha2 = alpha1 + (2* Math.PI * neighborWidth / centerWidth);
	    
	    RadialSubTreeUndirected((Node)center, neighbor, 
				    neighborWidth, rho, alpha1, 
				    alpha2, tree, baseX);
	    alpha1 = alpha2;
	}
	
	it = tree.getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    Point2D loc = (Point2D)coords.get(n);
	    if (loc != null) {
		
		locations.put(n, new Coordinates(loc.getX(),
						 loc.getY()));
	    } else {
		//System.out.println("oops... ");
	    }
	}
	done = true;
    }    
  
    protected void RadialSubTreeUndirected(Node forbiddenNeighbor, 
					   Node node,
					   double width, double rho, 
					   double alpha1, double alpha2,
					   Graph tree, double baseX) {

	Point2D nodeCoord = 
	    polarToCartesian(rho, (alpha1 + alpha2) / 2, baseX);
	coords.put(node,nodeCoord);
	double tau = 2 * Math.acos(rho / (rho + layerDistance));
	double alpha = 0.0;
	double s = 0.0;
	if (tau < (alpha2 - alpha1)) {
	    alpha = (alpha1 + alpha2 - tau) / 2.0;
	    s = tau / width;
	    //System.out.println("1: " + node + " " + s);
	}
	else {
	    alpha = alpha1;
	    s = (alpha2 - alpha1) / width;
	    //System.out.println("2: " + node + " " + s);
	}
	Iterator neighbors = node.getOutEdges().iterator();
	while (neighbors.hasNext()) {
	    Edge e = (Edge)neighbors.next();
	    if (!validEdges.contains(e)) {
		continue;
	    }
	    Node neighbor = (Node)e.getOpposite(node);
	    if (neighbor != forbiddenNeighbor) {
		int neighborWidth = 
		    ((Integer)radialWidth.get(neighbor)).intValue();
		if (neighborWidth == 0) {
		    System.out.println(neighbor);
		}
		RadialSubTreeUndirected(node, neighbor, neighborWidth, 
					rho + layerDistance,
					alpha, alpha += s * neighborWidth, 
					tree, baseX);
	    }
	}
    }
    
    private Point2D polarToCartesian(double rho, double alpha, 
				     double Xtranslation) {
	//System.out.println(rho + " " + alpha + Xtranslation);
	return new Point2D.Double(rho * Math.cos(alpha) + Xtranslation, 
				  rho * Math.sin(alpha));
    }
    
    /**
     * This method is actually called only once with the center of the
     * graph as a parameter.
     */
    private int defineWidthProperty(Node center, Node enteringFrom) {
	//System.out.println("at: " + center);
	if (radialWidth.containsKey(center)) {
	    //  System.out.println(center + " " + radialWidth.get(center));
	    //System.out.println("\treturning...\n");
	    return ((Integer)radialWidth.get(center)).intValue();
	}

	//System.out.println("\trecursing\n");
	int width = 0;
	Iterator edges = ((Node)center).getOutEdges().iterator();
	int validNeighbors = 0;
	while (edges.hasNext()) {
	    Edge edge = (Edge)edges.next();
	    if (!validEdges.contains(edge)) {
		continue;
	    }
	    Node goingTo = (Node)edge.getOpposite(center);
	    if (enteringFrom == goingTo) {
		continue;
	    }
	    validNeighbors++;
	    width += defineWidthProperty(goingTo,center);
	}
	if (validNeighbors != 0) {
	    radialWidth.put(center,new Integer(width));
	    return(width);
	} else {
	    radialWidth.put(center,new Integer(1));
	    return(1);
	}
    }

    public double getX(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getX());
    }

    public double getY(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getY());
    }

    public Coordinates getCoordinates(Node v) {
	return((Coordinates)locations.get(v));
    }

    public boolean done = false;

    public boolean incrementsAreDone() {
	return(done);
    }

    public void initialize_local_vertex(edu.uci.ics.jung.graph.Vertex v) {
    }

    public void initialize_local() {
    }

    public boolean isIncremental() {
	return(false);
    }   
}
