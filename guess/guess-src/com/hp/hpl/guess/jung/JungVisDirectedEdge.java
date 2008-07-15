package com.hp.hpl.guess.jung;

import java.util.*;
import edu.uci.ics.jung.graph.Vertex;

public class JungVisDirectedEdge extends JungVisEdge 
    implements edu.uci.ics.jung.graph.DirectedEdge
{
    
    public JungVisDirectedEdge(JungVisNode source, JungVisNode dest) {
	super(source, dest);
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
    
    public JungVisNode getJungVisNode1()
    {
	return (JungVisNode)getSource();
    }
    
    public JungVisNode getJungVisNode2()
    {
	return (JungVisNode)getDest();
    }
}
