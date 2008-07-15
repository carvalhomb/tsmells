package com.hp.hpl.guess.jung;

import java.util.*;

/**
 * an implementation of a graph edge
 */
public class JungVisUndirectedEdge extends JungVisEdge 
    implements edu.uci.ics.jung.graph.UndirectedEdge {

    public JungVisUndirectedEdge(JungVisNode node1, JungVisNode node2)
    {
	super(node1, node2);
    }
    
    public JungVisNode getJungVisNode1()
    {
	return (JungVisNode)getEndpoints().getFirst();
    }
    
    public JungVisNode getJungVisNode2()
    {
	return (JungVisNode)getEndpoints().getSecond();
    }
}
