package com.hp.hpl.guess;

import java.sql.Types;
import java.util.*;

public class ComparisonQuery extends Query
{
    private String op;
    private Field field;
    private Object value;
    private Graph g;

    public ComparisonQuery(Graph graph, String op, 
			   Field field, Object value, 
			   int type)
    {
	super(graph, type);
	
	this.op = op;
	this.field = field;
	this.value = value;
	this.g = graph;
	//System.out.println(toSQLString());
    }

    public Set getStates(Set init) {

	if (init == null) {
	    init = new HashSet();
	}

	if (field.getType() == Field.NODE) {
	    if (field.getState() == null) {
		init.add("nodes");
	    } else {
		init.add("nodes_"+field.getState());
	    }
	} else {
	    if (field.getState() == null) {
		init.add("edges");
	    } else {
		init.add("edges_"+field.getState());
	    }
	}

	if (value instanceof Field) {
	    Field temp = (Field)value;
	    if (temp.getType() == Field.NODE) {
		if (temp.getState() == null) {
		    init.add("nodes");
		} else {
		    init.add("nodes_"+temp.getState());
		}
	    } else {
		if (temp.getState() == null) {
		    init.add("edges");
		} else {
		    init.add("edges_"+temp.getState());
		}
	    }
	}

	return(init);
    }

    public String toSQLString()
    {
	String sqlOp = op;
	if (op.equals(EQ))
	    sqlOp = "=";
	
	if (value instanceof Field) {
	    value = ((Field)value).toSQLString();
	}

	if ((field.getSQLType() == Types.VARCHAR) ||
	    (field.getSQLType() == Types.CHAR) ||
	    (field.getSQLType() == Types.DATE) ||
	    (field.getSQLType() == Types.TIME) ||
	    (field.getSQLType() == Types.TIMESTAMP) ||
	    (field.getSQLType() == Types.LONGVARCHAR)) {
	    return field.toSQLString() + " " + sqlOp + " '" + value + "'";
	} else {
	    return field.toSQLString() + " " + sqlOp + " " + value;
	}
    }

    protected void forceQuery() {
	if (field.needsUpdate(g)) {
	    // fields needs an update so let's
	    // force one node or edge to update
	    if (field.getType() == Field.NODE) {
		Iterator it = g.getNodes().iterator();
		while(it.hasNext()) {
		    Node n = (Node)it.next();
		    n.__getattr__(field.getName());
		    break;
		}
	    } else {
		Iterator it = g.getEdges().iterator();
		while(it.hasNext()) {
		    Edge n = (Edge)it.next();
		    n.__getattr__(field.getName());
		    break;
		}
	    }	    
	}
	annotation = toSQLString();
	super.forceQuery();
    }
    
    public boolean describes(GraphElement element)
    {
	Object fieldValue = element.__getattr__(field.getName());
	
	if (op.equals(EQ) || op.equals(LIKE))
	    return fieldValue.equals(value);
	if (op.equals(NE))
	    return !fieldValue.equals(value);
	
	//must be numbers from here on out.
	double value1 = ((Double)fieldValue).doubleValue();
	double value2;
	
	if (value instanceof Double)
	    value2 = ((Double)value).doubleValue();
	else if (value instanceof Integer)
	    value2 = ((Integer)value).intValue();
	else
	    throw new Error("Invalid number type:  " + value.getClass());
	
	if (op.equals(LT))
	    return value1 < value2;
	if (op.equals(GT))
	    return value1 > value2;
	if (op.equals(LE))
	    return value1 <= value2;
	if (op.equals(GE))
	    return value1 >= value2;
	
	throw new Error("Invalid operation:  " + op);
    }
}
