package com.hp.hpl.guess.prefuse;

import com.hp.hpl.guess.ui.EdgeListener;
import prefuse.data.Node;
import prefuse.data.Edge;
import prefuse.data.Graph;
import com.hp.hpl.guess.ui.NodeListener;
import prefuse.data.Node;
import prefuse.visual.NodeItem;
import prefuse.data.Graph;
import prefuse.util.ColorLib;
import com.hp.hpl.guess.ui.Colors;
import java.awt.Color;
import prefuse.visual.VisualItem;

public class PrefuseEdge implements EdgeListener {

    protected prefuse.data.Edge iEdge = null;

    public PrefuseEdge(Graph g, PrefuseNode n1, PrefuseNode n2) {
	iEdge = g.addEdge(n1.iNode,n2.iNode);
    }

    public Object get(String field) {
	//return(getAttribute(field));
	VisualItem vi = PrefuseFactory.m_vis.getVisualItem("graph.edges",
							   iEdge);
	if (field.equals("color")) {
	    return(ColorLib.getColor(vi.getStrokeColor()));
	}  else if (field.equals("width")) {
	    return(new Double(width));
	}
	return(null);
    }
    
    private double width = -1;

    public void set(String field, Object value) {
	//setAttribute(field,value.toString());
	VisualItem vi = PrefuseFactory.m_vis.getVisualItem("graph.edges",
							   iEdge);
	if (field.equals("color")) {
	    Color temp = null;
	    if (value instanceof Color) {
		temp = (Color)value;
	    } else {
		temp = (Colors.getColor((String)value,
					Color.blue));
	    }
	    vi.setFillColor(ColorLib.color(temp));
	    vi.setStrokeColor(ColorLib.color(temp));
	} else if (field.equals("width")) {
	    double tempWidth = ((Double)value).doubleValue();
	    if (tempWidth != width) {
		vi.setStroke(new java.awt.BasicStroke((float)tempWidth));
	    }
	    width = tempWidth;
	}
    }

    public void highlight(boolean state) {
    }

    public void readjust() {
    }

    public void readjust(boolean moveToF) {
    }
}
