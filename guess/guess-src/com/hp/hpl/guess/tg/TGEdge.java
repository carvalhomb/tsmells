package com.hp.hpl.guess.tg;

import com.hp.hpl.guess.ui.EdgeListener;
import com.hp.hpl.guess.ui.Colors;
import java.awt.Color;

public class TGEdge extends com.touchgraph.graphlayout.Edge 
    implements EdgeListener {
    
    public TGEdge(TGNode n1, TGNode n2) {
	super((com.touchgraph.graphlayout.Node)n1,
	      (com.touchgraph.graphlayout.Node)n2);
    }

    public Object get(String field) {
	//return(getAttribute(field));
	if (field.equals("color")) {
	    return(getColor());
	}
	return(null);
    }
    
    public void set(String field, Object o) {
	//setAttribute(field,value.toString());
	if (field.equals("color")) {
	    if (o instanceof Color) {
		setColor((Color)o);
	    } else {
		setColor((Colors.getColor((String)o,(Color)getColor())));
	    }
	} 
    }

    public void highlight(boolean state) {
    }

    public void readjust() {
    }

    public void readjust(boolean moveToF) {
    }
}
