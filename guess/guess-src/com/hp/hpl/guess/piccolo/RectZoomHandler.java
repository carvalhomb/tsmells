package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolox.nodes.P3DRect;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.*;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.BasicStroke;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.handles.PHandle;
import edu.umd.cs.piccolo.activities.*;
import java.util.*;

import javax.swing.SwingUtilities;

public class RectZoomHandler extends PBasicInputEventHandler {
    // The rectangle that is currently getting created.
    protected PPath rectangle;
	
    // The mouse press location for the current pressed, drag, release sequence.
    protected Point2D pressPoint;
	
    // The current drag location.
    protected Point2D dragPoint;
	
    private PLayer layer = null;

    private PCamera camera = null;

    final static int DASH_WIDTH = 5;
    final static int NUM_STROKES = 10;

    private float strokeNum = 0;
    private BasicStroke[] strokes = null;

    FrameListener fl = null;

    public RectZoomHandler(PLayer layer,PCamera cam) {
	this.layer = layer;
	this.camera = cam;
	float[] dash = { DASH_WIDTH, DASH_WIDTH };
	strokes = new BasicStroke[NUM_STROKES];
	for (int i = 0; i < NUM_STROKES; i++) {
	    strokes[i] = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, i);
	}
	fl = (FrameListener)VisFactory.getFactory().getDisplay();
    }

    public void mousePressed(PInputEvent e) {
 
	if (!e.isLeftMouseButton()) {
	    return;
	}

	if (!e.isShiftDown()) {
	    return;
	}
	    
	super.mousePressed(e);			

	// Initialize the locations.
	pressPoint = e.getPosition();
	dragPoint = pressPoint;				

	// create a new rectangle and add it to the canvas layer so that
	// we can see it.
	rectangle = new PPath();
	rectangle.setStroke(new BasicStroke((float)(1/ e.getCamera().getViewScale())));

	// set the color dep. on background
	Color c = fl.getDisplayBackground();

	double lum = 
	    (299 * c.getRed() + 587 * c.getGreen() + 114 * c.getBlue()) / 1000;
	if (lum < 127) {
	    rectangle.setStrokePaint(Color.white);
	} else {
	    rectangle.setStrokePaint(Color.black);
	}


	layer.addChild(rectangle);

	e.pushCursor(CursorFactory.getCursor(CursorFactory.ZOOM));
	    
	// update the rectangle shape.
	updateRectangle();
    }
	
    public void mouseDragged(PInputEvent e) {

	if (!e.isLeftMouseButton()) {
	    return;
	}

	if (!e.isShiftDown()) {
	    return;
	}

	super.mouseDragged(e);

	float origStrokeNum = strokeNum;
	strokeNum = (strokeNum + 0.5f) % NUM_STROKES;	// Increment by partial steps to slow down animation
	if ((int)strokeNum != (int)origStrokeNum) {
	    rectangle.setStroke(strokes[(int)strokeNum]);
	}

	// update the drag point location.
	dragPoint = e.getPosition();	
	    
	// update the rectangle shape.
	updateRectangle();
    }
	
    public void mouseReleased(PInputEvent e) {
	if (!e.isLeftMouseButton()) {
	    return;
	}

	try {
	    e.popCursor();
	} catch (Exception ex) {
	    e.pushCursor(CursorFactory.getCursor(CursorFactory.STANDARD));
	}


	if (rectangle == null) {
	    return;
	}

	super.mouseReleased(e);
	// update the rectangle shape.
	updateRectangle();
	layer.removeChild(rectangle);

	if ((rectangle.getWidth() < 1) ||
	    (rectangle.getHeight() < 1)) {
	    rectangle = null;
	    return;
	}

	final Rectangle2D r2d = 
	    new Rectangle2D.Double(rectangle.getX(),
				   rectangle.getY(),
				   rectangle.getWidth(),
				   rectangle.getHeight());

	//frame.getGCamera().addChild(labelText);

	 
	javax.swing.SwingUtilities.invokeLater(new Runnable() { 
		public void run() { 
		    camera.animateViewToCenterBounds(r2d,
						     true,
						     2000);
		} 
	    }); 
	rectangle = null;
    }	
	
    public void updateRectangle() {
	// create a new bounds that contains both the press and current
	// drag point.
	PBounds b = new PBounds();
	b.add(pressPoint);
	b.add(dragPoint);
	    
	// Set the rectangles bounds.
	rectangle.setPathTo(b);
    }
}
