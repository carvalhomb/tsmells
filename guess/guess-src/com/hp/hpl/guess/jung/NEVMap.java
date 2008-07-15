package com.hp.hpl.guess.jung;

import com.hp.hpl.guess.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.ArchetypeEdge;

public class NEVMap implements NumberEdgeValue {

    String fld = null;

    public NEVMap(Field f) {
	//System.out.println("baz monster");
	if (f.getType() != Field.EDGE) {
	    throw(new Error("Field is not an edge field"));
	}
	fld = f.getName();
    }

    public Number getNumber(ArchetypeEdge e) {
	//System.out.println(e + " " + fld + " = " + ((Edge)e).__getattr__(fld));
	if (e instanceof Edge) {
	    return((Number)((Edge)e).__getattr__(fld));
	} else {
	    throw(new Error(e + " not an instance of a GUESS edge"));
	}
    }

    public void setNumber(ArchetypeEdge e, Number n) {
	if (e instanceof Edge) {
	    ((Edge)e).__setattr__(fld,n);
	} else {
	    throw(new Error(e + " not an instance of a GUESS edge"));
	}
    }
}
