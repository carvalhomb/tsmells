package com.hp.hpl.guess.jung;

import com.hp.hpl.guess.ui.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import edu.uci.ics.jung.graph.impl.*;
import java.util.*;

public abstract class JungVisEdge extends AbstractSparseEdge 
    implements EdgeListener {
    
    public JungVisEdge(JungVisNode n1, JungVisNode n2) {
	super(n1,n2);
    }

    private Hashtable data = new Hashtable();

    public void readjust() {
    }

    public Object get(String field) {
	if (field.equals("label")) {
	    return(data.get("label"));
	} else if (field.equals("color")) {
	    return(data.get("color"));
	} else if (field.equals("visible")) {
	    return(data.get("visible"));
	} else if (field.equals("fixed")) {
	    return(data.get("fixed"));
	} else if (field.equals("width")) {
	    return(data.get("width"));
	}
	return(null);
    }
    
    public void set(String field, Object o) {
	if (field.equals("label")) {
	    data.put("label",o);
	} else if (field.equals("color")) {
	    if (o instanceof Color) {
		data.put("color",o);
	    } else {
		data.put("color",Colors.getColor((String)o,
						 Color.green));
	    }
	} else if (field.equals("visible")) {
	    data.put("visible",(Boolean)o);
	} else if (field.equals("width")) {
	    data.put("visible",(Double)o);
	}
    }

    public void highlight(boolean state) {
    }
}
