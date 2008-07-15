package edu.uci.ics.jung.algorithms;

import edu.uci.ics.jung.graph.*;
import java.util.*;

public class DFSIterator {

    private Graph g = null;

    private Stack queue = new Stack();

    private HashSet visited = new HashSet();

    public DFSIterator(Vertex start, Graph g) {
	this.g = g;
	visited.add(start);
	queue.push(start);
    }

    public boolean hasNext() {
	if (queue.peek() != null) {
	    return(true);
	} else {
	    return(false);
	}
    }

    public Vertex next() {

	Vertex v = (Vertex)queue.pop();

	if (v == null) {
	    return(null);
	}

	Iterator succs = v.getSuccessors().iterator();
	while(succs.hasNext()) {
	    Vertex su = (Vertex)succs.next();
	    if (!visited.contains(su)) {
		visited.add(su);
		queue.push(su);
	    }
	}
	return(v);
    }
}
