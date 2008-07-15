package com.hp.hpl.guess.layout;

import com.hp.hpl.guess.*;
import com.hp.hpl.guess.piccolo.*;
import java.util.*;

public class EdgeAdjustment {
    
    public static void expandOverlapping(Graph g) {
	HashMap edgeb = new HashMap();
	
	// first thing is to group all the edges
	// that are overlapping
	Iterator it = g.getEdges().iterator();
	while(it.hasNext()) {
	    Edge e = (Edge)it.next();
	    Node n1 = e.getNode1();
	    Node n2 = e.getNode2();
	    if (n1 == n2)
		continue;

	    String def = null;
	    if (n1.getName().compareTo(n2.getName()) < 0) {
		def = n1.getName() + "-" + n2.getName();
	    } else {
		def = n2.getName() + "-" + n1.getName();
	    }
	    
	    Vector v = (Vector)edgeb.get(def);
	    if (v == null) {
		v = new Vector();
		edgeb.put(def,v);
	    }
	    v.add(e);
	}	

	// now go through and move things
	Iterator eg = edgeb.keySet().iterator();
	while (eg.hasNext()) {
	    String k = (String)eg.next();
	    Vector v = (Vector)edgeb.get(k);
	    //System.out.println(k + " " + v.size());
	    if (v.size() > 1) {
		// more than one overlapping edge
		Edge e1 = (Edge)v.elementAt(0);
		Edge e2 = (Edge)v.elementAt(1);
		if (e1.getRep() instanceof GuessPEdge) {
		    ((GuessPEdge)e1.getRep()).readjustJiggle(true);
		}
		if (e2.getRep() instanceof GuessPEdge) {
		    ((GuessPEdge)e2.getRep()).readjustJiggle(false);
		}
	    }
	}
    }

    public static void compressOverlapping(Graph g) {

	Iterator it = g.getEdges().iterator();
	while(it.hasNext()) {
	    Edge e = (Edge)it.next();
	    e.readjust();
	}	
    }
}
