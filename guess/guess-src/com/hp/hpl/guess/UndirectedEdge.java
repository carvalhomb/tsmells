package com.hp.hpl.guess;

import java.util.*;

/**
 * an implementation of a graph edge
 */
public class UndirectedEdge extends Edge implements edu.uci.ics.jung.graph.UndirectedEdge
{
	public UndirectedEdge(int id, Node node1, Node node2)
	{
		super(id, node1, node2);
	}

	public UndirectedEdge(Node node1, Node node2)
	{
		super(node1, node2);
	}

	public String toString()
	{
	    String possi = getNode1() + "-" + getNode2();
	    if (label == null)
		return possi;
	    
	    return possi + " ("+label+")";
	}

	public Node getNode1()
	{
		return (Node)getEndpoints().getFirst();
	}

	public Node getNode2()
	{
		return (Node)getEndpoints().getSecond();
	}
}
