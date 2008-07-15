package com.hp.hpl.guess.ui;

import com.hp.hpl.guess.*;
import java.awt.Color;

public class DumbFactory extends VisFactory {

    private DumbFrameListener curFrame = null;

    public DumbFactory() {
	init();
    }

    private DumbFrameListener init() {
	curFrame = new DumbFrameListener();
	return(curFrame);
    }

    public FrameListener getDisplay() {
	return(curFrame);
    }

    public void runNow() {
    }

    public NodeListener generateNode(int type, double x, double y,
				     double width, double height,
				     Color clr, Node n) {
	DumbNodeListener dnl = new DumbNodeListener(n);
	dnl.setLocation(x,y,width,height);
	return(dnl);
    }

    public NodeListener generateNode(Node n) {
	DumbNodeListener dnl = new DumbNodeListener(n);
	return(dnl);
    }

    public void remove(Node n)
    {
	
    }
    
    //removes the given edge rep from the collection of edges to draw
    public void remove(Edge e)
    {
    }

    public void add(Node n) {
    }

    public void add(Edge e) {
    }

    public EdgeListener generateEdge(Edge n) {
	DumbEdgeListener del = new DumbEdgeListener(n);
	return(del);
    }
        
}
