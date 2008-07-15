package com.hp.hpl.guess.mascopt;

import mascoptLib.graphs.DiGraph;
import mascoptLib.graphs.VertexSet;
import mascoptLib.graphs.ArcSet;
import mascoptLib.graphs.Vertex;
import mascoptLib.graphs.Arc;
import mascoptLib.algos.digraph.KShortestPaths;
import mascoptLib.algos.abstractalgos.KShortestPath;
import mascoptLib.graphs.DiPath;

import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.Edge;
import com.hp.hpl.guess.DirectedEdge;

import java.util.*;

/**
 * this is a simple example of mapping to and from an extra
 * graph library.  In this case we are using Mascopt (a network
 * optimization package)
 */
public class MascoptDiGraphProxy extends DiGraph {

    
    /**
     * maps a guess object to mascopt.  A little bit of 
     * a hack because a guess object (like a bidirected/undirected
     * edge) will become two mascopt objects.  The map 
     * will therefore be:
     * guess object -> mascopt object
     * guess object -> Arc[2] (two arc objects)
     */
    public HashMap g2m = new HashMap();

    /**
     * maps a mascopt object to a guess object
     * mascopt -> guess, possibly multiple
     * mascopt obj to single guess obj
     */
    public HashMap m2g = new HashMap();

    /**
     * creates a proxy for the given graph object
     */
    public static MascoptDiGraphProxy createProxy(com.hp.hpl.guess.Graph g) {

	HashMap g2m = new HashMap();
	HashMap m2g = new HashMap();

	// copy in all the vertex objects, creating if necessary
	VertexSet vs = new VertexSet();

	Iterator nodes = g.getNodes().iterator();
	while (nodes.hasNext()) {
	    Node node = (Node)nodes.next();
	    Vertex v = (Vertex)g2m.get(node);
	    if (v == null) {
		v = new Vertex();
		g2m.put(node,v);
		m2g.put(v,node);
	    }
	    vs.add(v);
	}

	// copy in all the edge objects
	ArcSet as = new ArcSet(vs);

	// default weights
	as.setValue(KShortestPath.WEIGHT,"1");
	KShortestPaths.NAME_OF_VALUE = "poids";

	Iterator edges = g.getEdges().iterator();
	while (edges.hasNext()) {
	    // handle case where we have an undirected edge
	    Edge e = (Edge)edges.next();

	    // copy weight into mascopt field
	    double weight = 
		((Double)e.__getattr__("weight")).doubleValue();

	    // see above documentation for g2m for an explanation
	    if (g2m.containsKey(e)) {
		// object already mapped, just update the weights
		Object o = g2m.get(e);
		if (o instanceof Arc) {
		    ((Arc)o).setDouValue("poids",weight);
		    as.add((Arc)o);
		} else {
		    Arc[] aset = (Arc[])o;
		    Arc a1 = aset[0];
		    Arc a2 = aset[1];
		    a1.setDouValue("poids",weight);
		    as.add(a1);
		    a2.setDouValue("poids",weight);
		    as.add(a2);
		}
	    } else {
		// object doesn't exist
		if (e instanceof DirectedEdge) {
		    // directed, just do a one to one
		    Vertex head = 
			(Vertex)g2m.get(((DirectedEdge)e).getDestination());
		    Vertex tail = 
			(Vertex)g2m.get(((DirectedEdge)e).getSource());
		    Arc a = new Arc(tail,head);
		    a.setDouValue("poids",weight);
		    System.out.println(a + " " + weight);
		    g2m.put(e,a);
		    m2g.put(a,e);
		    as.add(a);
		} else {
		    // undirected, transform into 2 directed
		    Arc[] aset = new Arc[2];
		    Vertex head = 
			(Vertex)g2m.get(((Edge)e).getNode1());
		    Vertex tail = 
			(Vertex)g2m.get(((Edge)e).getNode2());

		    Arc a1 = new Arc(tail,head);
		    a1.setDouValue("poids",weight);
		    aset[0] = a1;
		    Arc a2 = new Arc(head,tail);
		    a2.setDouValue("poids",weight);
		    aset[0] = a2;
		    g2m.put(e,aset);
		    m2g.put(a1,e);
		    m2g.put(a2,e);

		    as.add(a1);
		    as.add(a2);
		}
	    }
	}

	MascoptDiGraphProxy toRet = new MascoptDiGraphProxy(vs,as);
	//as.setDouValue("poids",toRet,1.0);	
	toRet.m2g = m2g;
	toRet.g2m = g2m;
	return(toRet);
    }    

    /**
     * dummy overloaded constructor
     */
    public MascoptDiGraphProxy(VertexSet vs, ArcSet as) {
	super(vs,as);
    }

    /**
     * returns an ordered vector of k shortest paths from s to t
     * @param k the (max) number of shortest paths
     * @param s the source
     * @param t the target
     * @return a vector of vectors of edges
     */
    public Vector kShortestPaths(int k, Node s, Node t) {
	return(kShortestPaths(k,s,t,null));
    }

    /**
     * returns an ordered vector of k shortest paths from s to t,
     * inserts the weights as a Double into the weights hashmap
     * if not null
     * @param k the (max) number of shortest paths
     * @param s the source
     * @param t the target
     * @param weights if not null, inserts the Double weight
     * of each path
     * @return a vector of vectors of edges
     */
    public Vector kShortestPaths(int k, Node s, Node t, HashMap weights) {
	Vector paths = new Vector();
	KShortestPaths ksp = new KShortestPaths(this,k);
	ksp.run((Vertex)g2m.get(s),(Vertex)g2m.get(t));
	for (int i = 0 ; i < ksp.numberOfComputedPaths() ; i++) {
	    ArcSet as = ksp.getShortestPath(i).getArcSet();
	    Vector path = new Vector();

	    Iterator it = as.iterator();
	    while(it.hasNext()) {
		Arc a = (Arc)it.next();
		path.add(m2g.get(a));
	    }
	    paths.add(path);
	    if (weights != null) {
		weights.put(path,new Double(ksp.getWeight(i)));
	    }
	}
	return(paths);
    }
}
