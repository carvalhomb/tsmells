package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolox.nodes.P3DRect;
import com.hp.hpl.guess.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.BasicStroke;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;
import com.hp.hpl.guess.ui.DrawWindow;
import com.hp.hpl.guess.ui.VisFactory;

public class KeyBoardManager extends PBasicInputEventHandler {

    GFrame frame = null;

    public KeyBoardManager(GFrame f) {
	this.frame = f;
    }

    public void keyReleased(PInputEvent event) {
	if (event.getKeyCode() == 69) {
	    //frame.setMode(GFrame.NODE_EDIT);
	} else if (event.getKeyCode() == 68) {
	    // frame.setDrawMode(mode);
	} else if ((event.getKeyCode() == 48) || (event.getKeyCode() == 36)) {
	    // home key
	    frame.center();
	} else if (event.getKeyCode() == 70) {
	    // f key
	    //if (!frame.getFullScreenMode()) {
	    //frame.setFullScreenMode(true);
	    //} else {
	    //frame.setFullScreenMode(false);
	    //}
	    // frame.removeEscapeFullScreenModeListener();
	} else if (event.getKeyCode() == KeyEvent.VK_DELETE) {
	    //System.out.println("delete");
	    Collection c = frame.getSelected();
	    Iterator it = c.iterator();
	    while(it.hasNext()) {
		PNode pn  = (PNode)it.next();
		if (pn instanceof GuessPNode) {
		    Node owner = ((GuessPNode)pn).getOwner();
		    if (owner != null) {
			Guess.getGraph().removeNode(owner);
		    }
		} else if (pn instanceof GuessPEdge) {
		    Edge owner = ((GuessPEdge)pn).getOwner();
		    if (owner != null) {
			Guess.getGraph().removeEdge(owner);
		    }
		} else if (pn instanceof ConvexHullNode) {
		    VisFactory.getFactory().removeConvexHull((ConvexHullNode)pn);
		} else {
		    pn.removeFromParent();
		}
	    }
	    frame.unselectAll();
	}	
    }

    public void keyPressed(PInputEvent event) {

       	PCamera cam = frame.getGCamera();
	double scale = cam.getScale();

	
	//System.out.println(event.getKeyCode());

	if (event.getKeyCode() == 90) {
	    AffineTransform af = new AffineTransform();
	    af.scale(scale*.8,scale*.8);
	    cam.animateToTransform(af,200);
	    //cam.setScale(scale*.8);
	} else if (event.getKeyCode() == 88) {
	    AffineTransform af = new AffineTransform();
	    af.scale(scale*1.2,scale*1.2);
	    cam.animateToTransform(af,200);
	} else if (event.getKeyCode() == 40) {
	    cam.translateView(0,(1/scale)*(-20));
	} else if (event.getKeyCode() == 38) {
	    cam.translateView(0,(1/scale)*(20));
	} else if (event.getKeyCode() == 39) {
	    cam.translateView((1/scale)*(-20),0);
	} else if (event.getKeyCode() == 37) {
	    cam.translateView((1/scale)*(20),0);
	}
    }

}
