package com.hp.hpl.guess;

import java.util.*;
import edu.uci.ics.jung.graph.Vertex;

/**
 * an implementation of a graph edge
 */
public class DirectedEdge extends Edge 
    implements edu.uci.ics.jung.graph.DirectedEdge
{
    public DirectedEdge(int id, Node source, Node dest)
    {
	super(id, source, dest);
    }
    
    public DirectedEdge(Node source, Node dest) {
	super(source, dest);
    }
    
    public String toString()
    {
	String possi = getSource() + "->" + getDest();

	if (label == null)
	    return(possi);
		
	return possi + " ("+label+")";
    }
    
    public Vertex getSource()
    {
        return (Vertex)getEndpoints().getFirst();
    }

    public Vertex getDest()
    {
	return (Vertex)getEndpoints().getSecond();
    }
    
    public Vertex getDestination()
    {
	return(getDest());
    }
    
    public Node getNode1()
    {
	return (Node)getSource();
    }
    
    public Node getNode2()
    {
	return (Node)getDest();
    }
}
