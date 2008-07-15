package com.hp.hpl.guess;

import com.hp.hpl.guess.storage.*;

public class StateQuery {

    private String state = null;

    private GraphElement ge = null;

    public StateQuery(GraphElement ge, String state) {
	this.state = state;
	this.ge = ge;
    }

    public String toString() {
	return(ge.toString()+"['"+state+"']");
    }

    public Object __getattr__(String fieldName)
    {
	StorageListener s = StorageFactory.getSL();
	Field f = null;
	if (ge instanceof Node) {
	    f = Guess.getGraph().getNodeSchema().getField(fieldName);
	} else {
	    f = Guess.getGraph().getEdgeSchema().getField(fieldName);
	}
	if (f == null) {
	    throw new Error("Invalid property:  " + fieldName);
	} else { 
	    try {
		if (ge instanceof Node)
		    return(s.getColumn((Node)ge,f,state));
		else
		    return(s.getColumn((Edge)ge,f,state));
	    } catch (Exception ex) {
		throw new Error("Invalid property:  " + fieldName + 
				" or state " + state);
	    }
	}
    }
}
