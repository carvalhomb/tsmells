package com.hp.hpl.guess.jung;

import com.hp.hpl.guess.ui.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import edu.uci.ics.jung.graph.impl.*;
import java.util.*;

public class JungVisNode extends SparseVertex implements NodeListener {
    

    private HashMap data = new HashMap();

    public Object get(String field) {
	if (field.equals("label")) {
	    return(data.get("label"));
	} else if (field.equals("color")) {
	    return(data.get("color"));
	} else if (field.equals("visible")) {
	    return(data.get("visible"));
	} else if (field.equals("fixed")) {
	    return(data.get("fixed"));
	} else if (field.equals("style")) {
	    return(data.get("style"));
	} else if (field.equals("x")) {
	    return(data.get("x"));
	} else if (field.equals("y")) {
	    return(data.get("y"));
	} else if (field.equals("width")) {
	    return(data.get("width"));
	} else if (field.equals("height")) {
	    return(data.get("height"));
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
						 Color.blue));
	    }
	} else if (field.equals("visible")) {
	    data.put("visible",(Boolean)o);
	} else if (field.equals("fixed")) {
	    data.put("fixed",(Boolean)o);
	} else if (field.equals("style")) {
	    data.put("style",(Integer)o);
	} else if (field.equals("x")) {
	    data.put("x",(Double)o);
	} else if (field.equals("y")) {
	    data.put("y",(Double)o);
	} else if (field.equals("width")) {
	    data.put("width",(Double)o);
	} else if (field.equals("height")) {
	    data.put("height",(Double)o);
	} 
    }

    public void highlight(boolean state) {
    }
    
    public void setLocation(double x, double y) {
	data.put("x",new Double(x));
	data.put("y",new Double(y));
    }
    
    public void setLocation(double x, double y, double width, double height) {
	data.put("x",new Double(x));
	data.put("y",new Double(y));
	data.put("width",new Double(width));
	data.put("height",new Double(height));
    }
}
