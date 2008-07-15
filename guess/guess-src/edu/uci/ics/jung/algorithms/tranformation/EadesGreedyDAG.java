package edu.uci.ics.jung.algorithms.transformation;

import java.util.*;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.Graph;

public abstract class EadesGreedyDAG {

    public static void removeLoopsAndTwoCycles(DirectedGraph graph) {

	// remove self loops
	HashSet toRemove = new HashSet();
	Iterator it = graph.getEdges().iterator();
	while(it.hasNext()) {
	    DirectedEdge de = (DirectedEdge)it.next();
	    if (de.getSource() == de.getDest()) {
		toRemove.add(de);
	    }
	}

	// remove two-cycles
	it = graph.getVertices().iterator();
	while(it.hasNext()) {
	    Vertex v1 = (Vertex)it.next();
	    Iterator it2 = v1.getSuccessors().iterator();
	    while(it2.hasNext()) {
		Vertex v2 = (Vertex)it2.next();
		if (v1.isSuccessorOf(v2) &&
		    v1.isPredecessorOf(v2)) {
		    // bad, get rid of edges
		    toRemove.addAll(v1.findEdgeSet(v2));
		    toRemove.addAll(v2.findEdgeSet(v1));
		}
	    }
	}
	
	it = toRemove.iterator();
	while(it.hasNext()) {
	    DirectedEdge de = (DirectedEdge)it.next();
	    //System.out.println("removing: " + de);
	    graph.removeEdge(de);
	}
    }

    public static Vertex findSink(DirectedGraph graph) {
	Iterator it = graph.getVertices().iterator();
	while(it.hasNext()) {
	    // while G contains a sink
	    Vertex v = (Vertex)it.next();
	    if (v.outDegree() == 0) {
		// sink
		return(v);
	    }
	}
	return(null);
    }

    public static Vertex findSource(DirectedGraph graph) {
	Iterator it = graph.getVertices().iterator();
	while(it.hasNext()) {
	    // while G contains a sink
	    Vertex v = (Vertex)it.next();
	    if (v.inDegree() == 0) {
		// sink
		return(v);
	    }
	}
	return(null);
    }

    public static Graph eadesGreedyDAG(Graph graphO) {
	return(eadesGreedyDAG(graphO,true));
    }

    public static Graph eadesGreedyDAG(Graph graphO, boolean copy) {

	// make sure it's directed
	graphO = DirectionTransformer.toDirected(graphO,copy);

	LinkedList s1 = new LinkedList();
	LinkedList s2 = new LinkedList();

	// going to work off a copy since
	// we have to remove nodes and edges
	DirectedGraph graph = 
	    DirectionTransformer.toDirected(graphO);

	// dumb hack because we're going to want to go back later
	// and the loop is destructive to the getEqualVertex
	// stuff
	HashMap oToN = new HashMap();
	Iterator it = graphO.getVertices().iterator();
	while(it.hasNext()) {
	    Vertex v = (Vertex)it.next();
	    oToN.put(v,v.getEqualVertex(graph));
	}

	while(graph.getVertices().size() > 0) { // while G not empty
	    //System.out.println(graph.getVertices().size());
	    Vertex v = findSink(graph);
	    while(v != null) { // while G contains a sink
		s2.addFirst(v);
		graph.removeVertex(v);
		v = findSink(graph);
	    }
	    v = findSource(graph);
	    while(v != null) { // while G contains a source
		s1.addLast(v);
		graph.removeVertex(v);
		v = findSource(graph);
	    }
	    // choose a vertex for which out - in is max
	    int max = Integer.MIN_VALUE;
	    Vertex mVert = null;
	    it = graph.getVertices().iterator();
	    while(it.hasNext()) {
		v = (Vertex)it.next();
		if ((v.outDegree() - v.inDegree()) > max) {
		    max = (v.outDegree() - v.inDegree());
		    mVert = v;
		}
	    }
	    if (mVert != null) {
		s1.addLast(mVert);
		graph.removeVertex(mVert);
	    }
	}

	// make a graph ordering
	s1.addAll(s2);
       
	// want to be able to tell quickly if we have "left" 
	// edges so we'll keep a map between vertex and location
	// in the list
	HashMap hm = new HashMap();
	it = s1.iterator();
	int i = 0;
	while(it.hasNext()) {
	    Vertex v = (Vertex)it.next();
	    hm.put(v,new Integer(i));
	    i++;
	}

	// go through the list, reverse left edges
	it = s1.iterator();
	i = 0;
	while(it.hasNext()) {
	    Vertex vN = (Vertex)it.next();
	    Vertex vO = (Vertex)vN.getEqualVertex(graphO);
	    Iterator oe = vO.getOutEdges().iterator();
	    while(oe.hasNext()) {
		DirectedEdge e = (DirectedEdge)oe.next();
		Vertex succO = e.getDest();
		Vertex succN = (Vertex)oToN.get(succO);
		int loc = ((Integer)hm.get(succN)).intValue();
		if (loc < i) {
		    // we have a left edge, delete it
		    graphO.removeEdge(e);
		    // make it into a right edge if it
		    // doesn't exist
		    if (!succO.isPredecessorOf(vO)) {
			graphO.addEdge(new DirectedSparseEdge(succO,vO));
		    }
		}
	    }
	    i++;
	}
	return(graphO);
    }
}
