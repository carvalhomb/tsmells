package edu.uci.ics.jung.algorithms;

import edu.uci.ics.jung.graph.*;
import java.util.*;

public class BFSIterator {

    private Graph g = null;

    private Vector queue = new Stack();

    private HashSet visited = new HashSet();

    private int location = -1;

    public BFSIterator(Vertex start, Graph g) {
	this.g = g;
	visited.add(start);
	queue.add(start);
    }

    public boolean hasNext() {
	if (queue.elementAt(location + 1) != null) {
	    return(true);
	} else {
	    return(false);
	}
    }

    public Vertex next() {

	location++;
	Vertex v = (Vertex)queue.elementAt(location);

	if (v == null) {
	    return(null);
	}

	Iterator succs = v.getSuccessors().iterator();
	while(succs.hasNext()) {
	    Vertex su = (Vertex)succs.next();
	    if (!visited.contains(su)) {
		visited.add(su);
		queue.add(su);
	    }
	}
	return(v);
    }
}
