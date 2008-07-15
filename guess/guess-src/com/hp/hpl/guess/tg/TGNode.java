package com.hp.hpl.guess.tg;

import com.hp.hpl.guess.ui.*;
import java.awt.Color;
import java.awt.geom.Point2D;

public class TGNode extends com.touchgraph.graphlayout.Node 
    implements NodeListener {
    
    public TGNode() {
	super();
    }

    public TGNode(String id) {
	super(id);
    }

    public Object get(String field) {
	if (field.equals("label")) {
	    return(getLabel());
	} else if (field.equals("color")) {
	    return(getBackColor());
	} else if (field.equals("visible")) {
	    return(new Boolean(isVisible()));
	} else if (field.equals("fixed")) {
	    return(new Boolean(getFixed()));
	} else if (field.equals("style")) {
	    return(new Integer(getStyle()));
	} else if (field.equals("x")) {
	    Point2D loc = getLocation();
	    return(new Double(loc.getX()));
	} else if (field.equals("y")) {
	    Point2D loc = getLocation();
	    return(new Double(loc.getY()));
	} else if (field.equals("width")) {
	    return(new Double(getWidth()));
	} else if (field.equals("height")) {
	    return(new Double(getHeight()));
	} 
	return(null);
    }
    
    public void set(String field, Object o) {
	if (field.equals("label")) {
	    setLabel((String)o);
	} else if (field.equals("color")) {
	    if (o instanceof Color) {
		setBackColor((Color)o);
	    } else {
		setBackColor((Colors.getColor((String)o,
					      (Color)getBackColor())));
	    }
	} else if (field.equals("visible")) {
	    setVisible(((Boolean)o).booleanValue());
	} else if (field.equals("fixed")) {
	    setFixed(((Boolean)o).booleanValue());
	} else if (field.equals("style")) {
	    setStyle(((Integer)o).intValue());
	} else if (field.equals("x")) {
	    Point2D loc = getLocation();
	    loc.setLocation(((Double)o).doubleValue(),
			    loc.getY());
	} else if (field.equals("y")) {
	    Point2D loc = getLocation();
	    loc.setLocation(loc.getX(),
			    ((Double)o).doubleValue());
	} 
    }

    public void setBackColor(Color bg) {
	super.setBackColor(bg);
	double lum = 
	    (299 * bg.getRed() + 587 * bg.getGreen() + 114 * bg.getBlue()) 
	    / 1000;
	if (lum > 127) {
	    setTextColor(Color.black);
	} else {
	    setTextColor(Color.white);
	}
    }

    private int getStyle() {
	int t = getType();
	if (t == com.touchgraph.graphlayout.Node.TYPE_CIRCLE) {
	    return(VisFactory.ELLIPSE);
	} else if (t == com.touchgraph.graphlayout.Node.TYPE_RECTANGLE) {
	    return(VisFactory.RECTANGLE);
	} else if (t == com.touchgraph.graphlayout.Node.TYPE_ROUNDRECT) {
	    return(VisFactory.ROUNDRECTANGLE);
	} else {
	    return(VisFactory.ELLIPSE);
	}
    }

    private void setStyle(int guessStyle) {
	if ((guessStyle == VisFactory.RECTANGLE) ||
	    (guessStyle == VisFactory.TEXT_RECTANGLE)) {
	    setType(com.touchgraph.graphlayout.Node.TYPE_RECTANGLE);
	} else if ((guessStyle == VisFactory.ROUNDRECTANGLE) ||
		   (guessStyle == VisFactory.TEXT_ROUNDRECTANGLE))  {
	    setType(com.touchgraph.graphlayout.Node.TYPE_ROUNDRECT);
	} else {
	    setType(com.touchgraph.graphlayout.Node.TYPE_ELLIPSE);
	} 
    }

    public void highlight(boolean state) {
    }
    
    public void setLocation(double x, double y) {
    }
    
    public void setLocation(double x, double y, double width, double height) {
    }
}
