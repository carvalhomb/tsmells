package com.hp.hpl.guess.layout;

import java.util.*;
import com.hp.hpl.guess.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

public class CircularConstrained extends AbstractLayout {
    
    // number of pixels to shrink radius by. Java draws object from top
    // left hand corner and this allows objects drawn on the far right to
    // be visible.
    private int pad = 4;
    protected int maxradius = 500;
    protected Node center = null;
    protected int minradius = 30;
    protected boolean sorted = true;
    protected String column = null;
    protected ArrayList nodeList = null;
    protected double originx = 500;
    protected double originy = 500;
    protected double maxangle = 2 * Math.PI;
    protected boolean disSim = false;

    private Graph g = null;
    private Field field = null;

    private HashMap locations = new HashMap();

    public CircularConstrained(Graph g, Node center, 
			       Field f, double xo,
			       double yo) {
	this(g,center,f);
	originx = xo;
	originy = yo;
    }

    public CircularConstrained(Graph g, Node center, Field f) {
	super(g);
	this.g = g;
	this.center = center;
	this.field = f;
    }

    public CircularConstrained(Graph g, Node center, Field f, 
			       int maxrad, int minrad, boolean sorted,
			       boolean disSim, int maxangle) {
	this(g,center,f);
	this.maxradius = maxrad;
	this.minradius = minrad;
	this.sorted = sorted;
	this.disSim = disSim;
	this.maxangle = maxangle;
    }    

    public double getAsDouble(Object o) {
	if (o instanceof Integer) {
	    return((double)(((Integer)o).intValue()));
	} else if (o instanceof Long) {
	    return((double)(((Long)o).longValue()));
	} else if (o instanceof Double) {
	    return(((Double)o).doubleValue());
	} else if (o instanceof Integer) {
	    return((double)(((Float)o).floatValue()));
	} else {
	    return(0);
	}
    }

    class SortableNode implements Comparable {
	
	public Comparable key = null;
	
	public Node node = null;

	public SortableNode(Node n, Comparable key) {
	    this.key = key;
	    this.node = n;
	}
	
	public int compareTo(Object o) {
	    return(this.key.compareTo(((SortableNode)o).key));
	}
    }

    public void advancePositions() {
	if (done)
	    return;

	//	System.out.println("h.1");
	Iterator it = g.getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    locations.put(n,new Coordinates(n.getX(),n.getY()));
	}

	locations.put(center,new Coordinates(originx,originy));
	
	//System.out.println("h.2");
	// find the min,max + values for each thing we're
	// interested in
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;


	int count = 0;
	
	ArrayList al = new ArrayList();
	Hashtable quickLookup = new Hashtable();
	
	//System.out.println("h.3");
	if (field.getType() == Field.NODE) {
	    Set s = center.getNeighbors();
	    count = s.size();

	    if (s.contains(center)) 
		count--;

	    it = s.iterator();
	    while(it.hasNext()) {
		Node n = (Node)it.next();
		Object o = n.__getattr__(field.getName());
		double dval = 1;
		if (field.isNumeric()) {
		    dval = getAsDouble(o);
		    min = Math.min(min,dval);
		    max = Math.max(max,dval);
		}
		al.add(new SortableNode(n,new Double(dval)));
		quickLookup.put(n,new Double(dval));
	    }
	} else {
	    it = center.getIncidentEdges().iterator();
	    while(it.hasNext()) {
		Edge e = (Edge)it.next();
		Node n1 = e.getNode1();
		Node n2 = e.getNode2();
		Object o = e.__getattr__(field.getName());
		double dval = 1;
		if (n1 == n2)
		    continue;
		
		if (field.isNumeric()) {
		    dval = getAsDouble(o);
		    min = Math.min(min,dval);
		    max = Math.max(max,dval);
		}
		count++;
		if (center == n1) {
		    al.add(new SortableNode(n2,new Double(dval)));
		    quickLookup.put(n2,new Double(dval));
		} else {
		    al.add(new SortableNode(n1,new Double(dval)));
		    quickLookup.put(n1,new Double(dval));
		}
	    }
	}

	//	System.out.println("h.4");
	if (count == 0) {
	    System.out.println("No neighbors");
	    done = true;
	    return;
	}

	if (!field.isNumeric()) {
	    // we don't have a numeric field, so 
	    // we're just going to put things
	    // at a fixed distance
	    min = (maxradius - minradius) / 2;
	    max = min;
	}

	Collections.sort(al);

	double angledelta = maxangle / count;	    

	it = al.iterator();
	int i = 0;

	//System.out.println("h.5");
	while(it.hasNext()) {
	    SortableNode sn = (SortableNode)it.next();
	    Node gn = sn.node;
	    double val = ((Double)quickLookup.get(gn)).doubleValue();
	    if (disSim) {
		val = max - val;
	    }


	    double radius = 300;

	    if (max != min) {
		radius = 
			minradius + ((maxradius-minradius)*val / (max)); 
	    } else {
		if (radius != 0)
		    radius = max;
	    }

	    double angle = i * angledelta;

	    double newx = originx + radius * Math.cos(angle);
	    double newy = originy + radius * Math.sin(angle);
	    locations.put(gn,new Coordinates(newx,newy));
	    i++;
	}
	//	System.out.println("h.6");
	done = true;
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
