package com.hp.hpl.guess.layout;

import com.hp.hpl.guess.*;
import java.util.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

/**
 * initial attempts at binpacking... do not distribute
 * @author Hacked by Eytan Adar
 */
public class BinPack extends AbstractLayout {

    HashMap locations = new HashMap();

    boolean done = false;

    Graph g = null;

    private boolean rescale = true;

    public BinPack(Graph g, boolean rescale) {

	super(g);
	this.g = g;
	Iterator it = g.getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    locations.put(n, new Coordinates(n.getX(),
					     n.getY()));
	}

	this.rescale = rescale;
    }

    public void advancePositions() {
	
	if (done) 
	    return;

	double sumHeight = 0;
	double sumWidth = 0;

	ArrayList toFit = new ArrayList();

	// fill in with SizeCompRects
	Set clusters = g.weakComponentClusters();

	if (clusters.size() <= 1) {
	    // just switch to done and return
	    return;
	}

	Iterator it = clusters.iterator();
	double maxD = Double.MIN_VALUE;
	while (it.hasNext()) {
	    Set n2 = (Set)it.next();
	    SizeCompRect ntf = getBoundingBox(n2);
	    toFit.add(ntf);
	    double test = 
		((ntf.getWidth() - 30)*(ntf.getHeight() - 30)) 
		/ n2.size();
	    if (test > maxD)
		maxD = test;
	    sumHeight += ntf.getHeight();
	    sumWidth += ntf.getWidth();
	}

	Collections.sort(toFit);

	//System.out.println("desired sq pixes/node: " + maxD);
	
	if (rescale) {
	    sumHeight = 0;
	    sumWidth = 0;
	    HashMap seenResizes = new HashMap();
	    for (int i = toFit.size() - 1 ; i >= 0 ; i--) {
		SizeCompRect tf = (SizeCompRect)toFit.get(i);
		double test2 = 
		    ((tf.getWidth() - 30)*(tf.getHeight() - 30)) 
		    / tf.nodes.size();
		double curSize = (tf.getWidth() * tf.getHeight());
		double test = (maxD * tf.nodes.size()) / curSize;
		if ((tf.nodes.size() > 1) &&
		    (test != 1) &&
		    (test != Double.POSITIVE_INFINITY) &&
		    (test != Double.NEGATIVE_INFINITY)) {
		    
		    Double alreadyDone = 
			(Double)seenResizes.get(new Integer(tf.nodes.size()));
		    if (alreadyDone != null) {
			rescaleRect(tf,alreadyDone.doubleValue());
		    } else {
			seenResizes.put(new Integer(tf.nodes.size()),
					new Double(1+Math.log(test)));
			rescaleRect(tf,1+Math.log(test));
		    }
		}
		sumHeight += tf.getHeight();
		sumWidth += tf.getWidth();
		//System.out.println(sumHeight + " " + sumWidth);
	    }

	    //System.out.println(toFit.size() + " " + sumHeight + " " + sumWidth);
	    Collections.sort(toFit);
	}


	ArrayList available = new ArrayList();
	available.add(new SizeCompRect(0,0,sumWidth,sumHeight,null));

	double xcent = 0;
	double ycent = 0;

	for (int i = toFit.size() - 1 ; i >= 0 ; i--) {
	    //for (int i = 0 ; i < toFit.size() ; i++) {
	    SizeCompRect tf = (SizeCompRect)toFit.get(i);
	    int locBF = findBestFit2(available,tf,xcent,ycent);
	    //int locBF = findBestFit(available,tf);
	    //System.out.println(i + " " + available.size());
	    SizeCompRect bf = (SizeCompRect)available.get(locBF);
	    Collection newRects = moveAndSplit(bf,tf);
	    if (i == toFit.size() - 1) {
		xcent = tf.getWidth() / 2;
		ycent = tf.getHeight() / 2;
	    }
	    available.remove(locBF);
	    available.addAll(newRects);
	    Collections.sort(available);
	}

	// move stuff
	for (int i = 0 ; i < toFit.size() ; i++) {
	    SizeCompRect scr = (SizeCompRect)toFit.get(i);
	    double deltaX = scr.getX() - scr.originalX;
	    double deltaY = scr.getY() - scr.originalY;
	    it = scr.nodes.iterator();
	    while (it.hasNext()) {
		Node n = (Node)it.next();
		Coordinates c = (Coordinates)locations.get(n);
		c.setX(c.getX() + deltaX);
		c.setY(c.getY() + deltaY);
	    }
	}
	done = true;
    }

    public void rescaleRect(SizeCompRect scr, double percent) {
	//System.out.println(scr.nodes.size() + " " + percent);

	double w = scr.getWidth();
	double h = scr.getHeight();
	scr.setRect(scr.getX(),
		    scr.getY(),
		    w*percent,
		    h*percent);

	Iterator it = scr.nodes.iterator();
	while(it.hasNext()) {
	      Node n = (Node)it.next();
	      Coordinates c = (Coordinates)locations.get(n);
	      c.setX(scr.getX() + (c.getX() - scr.getX()) * percent);
	      c.setY(scr.getY() + (c.getY() - scr.getY()) * percent);
	}
    }

    public SizeCompRect getBoundingBox(Set nodes) {
	
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;
	double maxX = Double.MIN_VALUE;
	double maxY = Double.MIN_VALUE;
	
	Iterator it = nodes.iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    if (n.getX() < minX) {
		minX = n.getX();
	    } 

	    if (n.getY() < minY) {
		minY = n.getY();
	    }
	    
	    if (n.getX() + n.getWidth() > maxX) {
		maxX = n.getX() + n.getWidth();
	    }
	    
	    if (n.getY() + n.getHeight() > maxY) {
		maxY = n.getY() + n.getHeight();
	    }
	}
	minX -= 15;
	minY -= 15;
	maxX += 15;
	maxY += 15;
	return (new SizeCompRect(minX,minY,
				 maxX-minX,
				 maxY-minY,
				 nodes));
    }

    // don't use this one
    private int findBestFit(ArrayList avail, SizeCompRect tf) {

	double tfS = tf.size;

	for (int i = 0 ; i < avail.size() ; i++) {
	    //for (int i = avail.size() - 1 ; i >= 0 ; i--) {
	    SizeCompRect test = (SizeCompRect)avail.get(i);
	    if (test.size >= tf.size) {
		if ((tf.getWidth() <= test.getWidth()) &&
		    (tf.getHeight() <= test.getHeight())) {
		    return(i);
		}
	    }
	}
	return(-1);
    }

    private int findBestFit2(ArrayList avail, 
			    SizeCompRect tf,
			    double xcent,
			    double ycent) {

	double minDist = Double.MAX_VALUE;
	int minIndex = -1;

	//System.out.println(tf.getWidth() + " " + tf.getHeight());

	//for (int i = 0 ; i < avail.size() ; i++) {
	int counter = 0;
	for (int i = avail.size() - 1 ; i >= 0 ; i--) {
	    SizeCompRect test = (SizeCompRect)avail.get(i);
	    //System.out.println(tf.getWidth() + " " + test.getWidth());
	    //System.out.println(tf.getHeight() + " " + test.getHeight());
	    if ((tf.getWidth() <= test.getWidth()) &&
		(tf.getHeight() <= test.getHeight())) {
		//System.out.println("***");
		double dist = Point2D.distance(xcent,ycent,
					       test.getX() + tf.getWidth()/2,
					       test.getY() + tf.getHeight()/2);
		if (dist < minDist) {
		    minDist = dist;
		    minIndex = i;
		    counter++;
		    //if (counter == 3)
		    //break;
		}
	    }
	}
	//System.out.println(minIndex  + " " + counter + " " + xcent + " " + ycent);
	return(minIndex);
    }

    private Collection moveAndSplit(SizeCompRect bf, SizeCompRect tf) {

	tf.setRect(bf.getX(),
		   bf.getY(),
		   tf.getWidth(),
		   tf.getHeight());

	ArrayList al = new ArrayList(2);

	double xn1 = bf.getX() + tf.getWidth();
	double yn1 = bf.getY();
	double wn1 = bf.getWidth() - tf.getWidth();
	double hn1 = tf.getHeight();

	double xn2 = bf.getX();
	double yn2 = bf.getY() + tf.getHeight();
	double wn2 = bf.getWidth();
	double hn2 = bf.getHeight() - tf.getHeight();

	al.add(new SizeCompRect(xn1,yn1,wn1,hn1,null));
	al.add(new SizeCompRect(xn2,yn2,wn2,hn2,null));
	
	return(al);
    }

    class SizeCompRect extends Rectangle2D.Double implements Comparable {
	
	public double size = 0;

	public Set nodes = null;

	public double originalX = 0;

	public double originalY = 0;

	public SizeCompRect(double x,
			    double y,
			    double width,
			    double height,
			    Set nodes) {
	    super(x,y,width,height);
	    this.size = width * height;
	    this.nodes = nodes;
	    this.originalX = x;
	    this.originalY = y;
	}

	public int compareTo(Object o) {
	    if (((SizeCompRect)o).size < size) {
		return(1);
	    } else {
		return(-1);
	    }
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
