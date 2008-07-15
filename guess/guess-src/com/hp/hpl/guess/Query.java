package com.hp.hpl.guess;

import java.util.*;
import org.python.core.*;
import com.hp.hpl.guess.storage.*;

public abstract class Query extends PyList
{
    public static final String EQ = "==";
    public static final String NE = "!=";
    public static final String LT = "<";
    public static final String GT = ">";
    public static final String LE = "<=";
    public static final String GE = ">=";
    public static final String LIKE = "like";
    public static final String AND = "&";
    public static final String OR = "|";
    
    private Graph graph;
    private int type;
    boolean forced = false;

    public abstract Set getStates(Set init);

    public Query(Graph graph, int type)
    {
	this.graph = graph;
	this.type = type;
    }
    
    public Graph getGraph() {
	return(this.graph);
    }
    
    public PyObject __and__(PyObject other)
    {
	if (other instanceof PyList) {
	    return super.__and__(other);
	} else {
	    return new CompoundQuery(graph, AND, this, (Query)other, type);
	}
    }
    
    public PyObject __or__(PyObject other)
    {
	if (other instanceof PyList) {
	    return super.__or__(other);
	} else {
	    return new CompoundQuery(graph, OR, this, (Query)other, type);
	}
    }
    
    protected void forceQuery()
    {
	if (forced)
	    return;

	forced = true;
	
	StorageListener db = StorageFactory.getSL();
	
	if (type == Field.NODE)
	    db.findMatchingNodes(this);
	else if (type == Field.EDGE)
	    db.findMatchingEdges(this);
	else
	    throw new Error("Invalid field type:  " + type);
    }
    
    public void __setattr__(String name, PyObject value)
    {
	forceQuery();
	super.__setattr__(name, value);
    }
    
    public PyObject get(int i)
    {
	forceQuery();
	return super.get(i);
    }
    
    public int __len__()
    {
	forceQuery();
	return super.__len__();
    }
    
    public abstract boolean describes(GraphElement element);
    
    public PyString __str__() {
	forceQuery();
	return(super.__str__());
    }

    public String toString()
    {
	forceQuery();
	return super.toString();
    }
    
    public abstract String toSQLString();
}
