package com.hp.hpl.guess.layout;

import com.hp.hpl.guess.Node;
import java.util.*;
import java.awt.geom.Point2D;

/**
 * @author Hacked by Eytan Adar for Guess
 */
public class SGBin implements Comparable {
    
    protected double x = 0;
    protected double y = 0;
    protected double width = 0;
    protected double height = 0;
    protected Set sg = null;
    protected int nodes = 0;
    protected boolean rotate = false;
    protected double maxNS = 0;
    
    protected boolean bySize = false;

    protected Point2D centroid = null;

    protected SGBin(double w, double h, Set sg) {
	this.width = w;
	this.height = h;
	this.sg = sg;
	this.nodes = sg.size();
    }

    public Point2D calcCentroid() {
	
	double x = 0;
	double y = 0;
	Iterator it = sg.iterator();
	while (it.hasNext()) {
	    Node gn = (Node)it.next();
	    x += gn.getX();
	    y += gn.getY();
	}
	if ((sg != null) && (sg.size() > 0)) {
	    x = x / sg.size();
	    y = y / sg.size();
	    centroid = new Point2D.Double(x,y);
	    return(centroid);
	} else {
	    return(null);
	}
    }
    
    /**
     * reverses the order because we want largest
     * to smallest on the iterator
     */
    public int compareTo(Object o) {
	if (this == o) {
	    return(0);
	} 
	if (!bySize) {
	    if (nodes > ((SGBin)o).nodes) {
		return(-1);
	    } else {
		return(1);
	    } 
	} else {
	    double a1 = width * height;
	    double a2 = ((SGBin)o).width * ((SGBin)o).height;
	    if (a1 < a2) {
		return(1);
	    } else {
		return(-1);
	    }
	}
    }
}
