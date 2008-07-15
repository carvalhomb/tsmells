package edu.umd.cs.piccolo.event;

import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolo.PNode;

import java.awt.*;

import com.hp.hpl.guess.piccolo.*;
import com.hp.hpl.guess.ui.*;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;

public class MyZoomHandler extends PZoomEventHandler {
 
    private GFrame owner = null;

    public MyZoomHandler(GFrame owner) {
	this.owner = owner;
    }

    private boolean isNodeOrEdge(PInputEvent e) {
	PPickPath pp = e.getPath();
	PNode t = pp.getPickedNode();
	if (t instanceof GuessPNode) {
	    return(true);
	}
	if (t instanceof GuessPEdge) {
	    return(true);
	}
	return(false);
    }

    public void mousePressed(PInputEvent e) {
	if (e.isRightMouseButton()) { 

	    if (isNodeOrEdge(e))
		return;

	    e.pushCursor(CursorFactory.getCursor(CursorFactory.ZOOM));
	    super.mousePressed(e);
	}	    
    }

    public void mouseReleased(PInputEvent e) {
	super.mouseReleased(e);
	try {

	    if (isNodeOrEdge(e))
		return;

	    e.popCursor();
	} catch (Exception ex) {
	    e.pushCursor(CursorFactory.getCursor(CursorFactory.STANDARD));
	}
	//CursorFactory.setCursor(CursorFactory.STANDARD);
    }

    private Point2D lastClicked = null;
    
    public Point2D getLastClickedPosition() {
	return(lastClicked);
    }

    public void mouseClicked(PInputEvent e) {
	if (e.isRightMouseButton()) {
	    lastClicked = e.getPosition();
	    PPickPath pp = e.getPath();
	    PNode t = pp.getPickedNode();
	    GFrame frame = (GFrame)VisFactory.getFactory().getDisplay();
	    Collection c = frame.getSelected();
	    if ((c == null) || (c.size() <= 0)) {
		if (t instanceof GuessPNode) {
		    ((GuessPNode)t).mouseClicked(e);
		    c = new HashSet();
		    c.add(((GuessPNode)t).getOwner());
		    Point2D p2d = 
			e.getPositionRelativeTo(frame.getCamera());
		    NodeEditorPopup.getPopup().show((Component)frame,
						    (int)p2d.getX(),
						    (int)p2d.getY(),
						    c,((GuessPNode)t).getOwner());
		} else if (t instanceof GuessPEdge) {
		    ((GuessPEdge)t).mouseClicked(e);
		    c = new HashSet();
		    c.add(((GuessPEdge)t).getOwner());
		    Point2D p2d = 
			e.getPositionRelativeTo(frame.getCamera());
		    EdgeEditorPopup.getPopup().show((Component)frame,
						    (int)p2d.getX(),
						    (int)p2d.getY(),
						    c,((GuessPEdge)t).getOwner());
		}
	    } else {
		boolean nodes = false;
		boolean edges = false;

		if (c.size() <= 0) {
		    return;
		} else {
		    HashSet newhs = new HashSet();
		    Iterator it = c.iterator();
		    while(it.hasNext()) {
			Object o = it.next();
			if (o instanceof GuessPEdge) {
			    newhs.add(((GuessPEdge)o).getOwner());
			    edges = true;
			} else if (o instanceof GuessPNode) {
			    newhs.add(((GuessPNode)o).getOwner());
			    nodes = true;
			}
		    }
		    c = newhs;
		}
		Point2D p2d = 
		    e.getPositionRelativeTo(frame.getCamera());
		if (nodes && edges) {
		    GraphElementEditorPopup.getPopup().show((Component)frame,
							    (int)p2d.getX(),
							    (int)p2d.getY(),
							    c,c);
		} else if (nodes) {
		    NodeEditorPopup.getPopup().show((Component)frame,
						    (int)p2d.getX(),
						    (int)p2d.getY(),
						    c,c);
		} else if (edges) {
		    EdgeEditorPopup.getPopup().show((Component)frame,
						    (int)p2d.getX(),
						    (int)p2d.getY(),
						    c,c);
		}
	    }
	    super.mouseClicked(e);
	}
    }
}

