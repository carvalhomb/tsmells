package com.hp.hpl.guess.tg;

import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.*;
import java.awt.Color;

public class TGFactory extends VisFactory {

    public TGFactory() {
	init();
    }

    public void runNow() {
	if (lastCreated != null) {
	    curFrame.getHVScroll().slowScrollToCenter(lastCreated);
	}
    }

    private TGDisplay curFrame = null;

    private TGNode lastCreated = null;

    public TGDisplay init() {
	curFrame = new TGDisplay();
	return(curFrame);
    }

    public FrameListener getDisplay() {
	return(curFrame);
    }

    public NodeListener generateNode(int type, double x, double y,
				     double width, double height,
				     Color clr, Node n) {
	return(generateNode(n));
    }
    
    public NodeListener generateNode(Node n) {
	//m_actionList.setEnabled(false);	
	TGNode pn = new TGNode(n.getName());
	//System.out.println(n.getName());
	try {
	    curFrame.getTGPanel().addNode(pn);
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e);
	}

	//m_actionList.setEnabled(true);	
	lastCreated = pn;
	return(pn);
    }

    public void remove(Node n)
    {
	curFrame.getTGPanel().deleteNode((TGNode)n.getRep());
    }
    
    //removes the given edge rep from the collection of edges to draw
    public void remove(Edge e)
    {
	curFrame.getTGPanel().deleteEdge((TGEdge)e.getRep());
    }

    public void add(Node n) {
    }

    public void add(Edge e) {
    }

    public EdgeListener generateEdge(Edge n) {
	//m_actionList.setEnabled(false);	
	TGEdge pe = new TGEdge((TGNode)n.getNode1().getRep(),
			       (TGNode)n.getNode2().getRep());
	curFrame.getTGPanel().addEdge((TGEdge)pe);
	//m_actionList.setEnabled(true);	
	return(pe);
    }
}
