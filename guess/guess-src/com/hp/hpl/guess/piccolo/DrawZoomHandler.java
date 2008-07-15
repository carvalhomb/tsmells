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
import com.hp.hpl.guess.piccolo.util.*;
import com.hp.hpl.guess.ui.*;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.Paint;
import java.awt.BasicStroke;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.handles.PHandle;
import edu.umd.cs.piccolo.activities.*;
import java.util.*;
import javax.swing.*;
import java.io.*;

public class DrawZoomHandler extends PBasicInputEventHandler {
    // The rectangle that is currently getting created.
    protected PPath rectangle;

    protected SerLine myLine;
    
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

    public DrawZoomHandler(PLayer layer,PCamera cam) {
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

	// Initialize the locations.
	pressPoint = e.getPosition();
	dragPoint = pressPoint;				

	// create a new rectangle and add it to the canvas layer so that
	// we can see it.
	if ((DrawWindow.getTool() != DrawWindow.TEXT) &&
	    (DrawWindow.getTool() != DrawWindow.LINE)) {
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
	    
	    updateRectangle();
	} else if (DrawWindow.getTool() == DrawWindow.LINE) {
	    myLine = new SerLine();
	    layer.addChild(myLine);
	}
    }
	
    public void mouseDragged(PInputEvent e) {

	if (!e.isLeftMouseButton()) {
	    return;
	}

	if (rectangle != null) {
	    float origStrokeNum = strokeNum;
	    strokeNum = (strokeNum + 0.5f) % NUM_STROKES;	// Increment by partial steps to slow down animation
	    if ((int)strokeNum != (int)origStrokeNum) {
		rectangle.setStroke(strokes[(int)strokeNum]);
	    }
	    
	    // update the drag point location.
	    dragPoint = e.getPosition();	
	    
	    // update the rectangle shape.
	    updateRectangle();
	} else if (myLine != null) {
	    // update the drag point location.
	    dragPoint = e.getPosition();	
	    updateLine();
	}
    }

    public static final Color transparent = new Color(255,255,255,0);

    public void mouseReleased(PInputEvent e) {
	if (!e.isLeftMouseButton()) {
	    return;
	}

	try {
	    e.popCursor();
	} catch (Exception ex) {
	    e.pushCursor(CursorFactory.getCursor(CursorFactory.STANDARD));
	}


	// update the rectangle shape.
	updateRectangle();
	updateLine();

	// draw the shape.

	PNode nPath = null;
	if (DrawWindow.getTool() == DrawWindow.ELLIPSE) {
	    nPath = new SerEllipse(rectangle.getX(),
				   rectangle.getY(),
				   rectangle.getWidth(),
				   rectangle.getHeight());
	    ((PPath)nPath).setStrokePaint(DrawWindow.getDrawColor());
	    ((PPath)nPath).setStroke(DrawWindow.getStroke());
	    layer.addChild(nPath);
	} else if (DrawWindow.getTool() == DrawWindow.RRECTANGLE) {
	    nPath = new SerRoundRectangle(rectangle.getX(),
					  rectangle.getY(),
					  rectangle.getWidth(),
					  rectangle.getHeight(),
					  rectangle.getWidth()*.1,
					  rectangle.getHeight()*.1);
	    layer.addChild(nPath);
	    ((PPath)nPath).setStrokePaint(DrawWindow.getDrawColor());
	    ((PPath)nPath).setStroke(DrawWindow.getStroke());
	} else if (DrawWindow.getTool() == DrawWindow.TEXT) {
	    nPath = new PText();
	    nPath.setX((float)pressPoint.getX());
	    nPath.setY((float)pressPoint.getY());
	    ((PText)nPath).setTextPaint(DrawWindow.getDrawColor());
	    layer.addChild(nPath);
	    FontDialog.getDialog((PText)nPath);
	    //System.out.println("SDF:" + fd.getText());
	} else if (DrawWindow.getTool() == DrawWindow.LINE) {
	    if (myLine != null) {
		myLine.setStrokePaint(DrawWindow.getDrawColor());
		myLine.setStroke(DrawWindow.getStroke());
	    }
	} else {
	    nPath = new SerRectangle(rectangle.getX(),
				     rectangle.getY(),
				     rectangle.getWidth(),
				     rectangle.getHeight());
	    layer.addChild(nPath);
	    ((PPath)nPath).setStrokePaint(DrawWindow.getDrawColor());
	    ((PPath)nPath).setStroke(DrawWindow.getStroke());
	}

	if (nPath != null)
	    nPath.setPaint(null);

	if (rectangle != null)
	    layer.removeChild(rectangle);

	rectangle = null;
	myLine = null;
    }	
	
    public void updateRectangle() {
	// create a new bounds that contains both the press and current
	// drag point.
	if (rectangle != null) {
	    PBounds b = new PBounds();
	    b.add(pressPoint);
	    b.add(dragPoint);
	    
	    // Set the rectangles bounds.
	    rectangle.setPathTo(b);
	} 
    }

    public void updateLine() {
	// create a new bounds that contains both the press and current
	// drag point.
	if (myLine != null) {
	    myLine.setPathTo(pressPoint.getX(),
			     pressPoint.getY(),
			     dragPoint.getX(),
			     dragPoint.getY());
	}
    }

    public static final int RECT = 0;
    public static final int LINE = 1;
    public static final int ELL = 2;
    public static final int RRECT = 3;
    public static final int TEXT = 4;

    public void saveState(ObjectOutputStream oos) {
	try {
	    Collection col = layer.getAllNodes();
	    oos.writeInt(col.size());
	    Iterator it = col.iterator();
	    while(it.hasNext()) {
		Object o = it.next();
		if (o instanceof SerLine) {
		    oos.writeInt(LINE);
		    ((SerLine)o).writeObject(oos);
		} else if (o instanceof SerEllipse) {
		    oos.writeInt(ELL);
		    ((SerEllipse)o).writeObject(oos);
		} else if (o instanceof SerRectangle) {
		    oos.writeInt(RECT);
		    ((SerRectangle)o).writeObject(oos);
		} else if (o instanceof SerRoundRectangle) {
		    oos.writeInt(RRECT);
		    ((SerRoundRectangle)o).writeObject(oos);
		} else if (o instanceof PText) {
		    oos.writeInt(TEXT);
		    oos.writeObject(((PText)o).getText());
		    oos.writeObject(((PText)o).getFont());
		    oos.writeObject(((PText)o).getTextPaint());
		    oos.writeObject(((PText)o).getBounds());
		} else {
		    oos.writeInt(-1);
		}
	    }
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public void loadState(ObjectInputStream ois) {
	try {
	    int toRead = ois.readInt();
	    for (int i = 0 ; i < toRead ; i++) {
		int type = ois.readInt();
		if (type == TEXT) {
		    PText newText = new PText();
		    newText.setText((String)ois.readObject());
		    newText.setFont((java.awt.Font)ois.readObject());
		    newText.setTextPaint((Paint)ois.readObject());
		    newText.setBounds((PBounds)ois.readObject());
		    newText.setPaint(null);
		    layer.addChild(newText);
		} else if (type == LINE) {
		    SerLine o = new SerLine();
		    o.readObject(ois);
		    layer.addChild(o);
		} else if (type == ELL) {
		    SerEllipse o = new SerEllipse();
		    o.readObject(ois);
		    layer.addChild(o);
		} else if (type == RECT) {
		    SerRectangle o = new SerRectangle();
		    o.readObject(ois);
		    layer.addChild(o);
		} else if (type == RRECT) {
		    SerRoundRectangle o = new SerRoundRectangle();
		    o.readObject(ois);
		    layer.addChild(o);
		}
			
	    }
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }	
}
