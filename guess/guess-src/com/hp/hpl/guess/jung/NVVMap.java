package com.hp.hpl.guess.jung;

import com.hp.hpl.guess.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.ArchetypeVertex;

public class NVVMap implements NumberVertexValue {

    String fld = null;

    public NVVMap(Field f) {
	if (f.getType() != Field.NODE) {
	    throw(new Error("Field is not an node field"));
	}
	fld = f.getName();
    }

    public Number getNumber(ArchetypeVertex e) {
	if (e instanceof Node) {
	    return((Number)((Node)e).__getattr__(fld));
	} else {
	    throw(new Error(e + " not an instance of a GUESS node"));
	}
    }

    public void setNumber(ArchetypeVertex e, Number n) {
	if (e instanceof Node) {
	    ((Node)e).__setattr__(fld,n);
	} else {
	    throw(new Error(e + " not an instance of a GUESS node"));
	}
    }
}
