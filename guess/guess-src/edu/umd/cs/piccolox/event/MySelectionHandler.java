package edu.umd.cs.piccolox.event;

import com.hp.hpl.guess.piccolo.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.PInputEvent;
import java.util.*;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolox.handles.GuessPHandle;

public class MySelectionHandler extends GuessSelectionEventHandler {

    public MySelectionHandler(PLayer layer1,PLayer layer2) {
	super(layer1,layer2);
    }

    private double xclick = 0;
    private double yclick = 0;

    private boolean moved = false;

    private boolean moveAllowed = true;

    public void switchTo(PLayer layer2) {
	switchTo(layer2,true);
    }

    public void switchTo(PLayer layer2, boolean moveAllowed) {
	super.switchTo(layer2);
	this.moveAllowed = moveAllowed;
	GuessPHandle.setResizeable(moveAllowed);
    }

    protected void startDrag(PInputEvent e) {
	if (e.isLeftMouseButton()) {
	    PNode pressNode = e.getPath().getPickedNode();
	    if ((pressNode instanceof PCamera) &&
		(!e.isShiftDown())) {
		unselectAll();
		return;
	    }
	    xclick = e.getPosition().getX();
	    yclick = e.getPosition().getY();
	    super.startDrag(e);
	}
    }

    protected void drag(PInputEvent e) {
	if (e.isLeftMouseButton()) {
	    super.drag(e);
	}
    }

    protected void endDrag(PInputEvent e) {
	if (e.isLeftMouseButton()) {
	    super.endDrag(e);
	}
	if (moved == true) {
	    double deltaX = e.getPosition().getX() - xclick;
	    double deltaY = e.getPosition().getY() - yclick;
	    xclick = e.getPosition().getX();
	    yclick = e.getPosition().getY();
	    
	    Iterator selectionEn = getSelectionReference().iterator();
	    while (selectionEn.hasNext()) {
		PNode node = (PNode) selectionEn.next();
		if (node instanceof GuessPNode) {
		    ((GuessPNode)node).moveDone(node.getX() + deltaX,
						node.getY() + deltaY);
		} else if (moveAllowed) {
		    //System.out.println(node.getClass());
		    node.setBounds(node.getX() + deltaX,
				   node.getY() + deltaY,
				   node.getWidth(),
				   node.getHeight());
		}
	    }	    
	    moved = false;
	}
    }
    
    protected void dragStandardSelection(PInputEvent e) {
	// There was a press node, so drag selection
	double deltaX = e.getPosition().getX() - xclick;
	double deltaY = e.getPosition().getY() - yclick;
	xclick = e.getPosition().getX();
	yclick = e.getPosition().getY();

	Iterator selectionEn = getSelectionReference().iterator();
	
	if (moved == false) {
	     while (selectionEn.hasNext()) {
		 PNode node = (PNode) selectionEn.next();
		 node.moveToFront();
		 if (node instanceof GuessPNode) {
		     ((GuessPNode)node).inTransition(node.getX() + deltaX,
						     node.getY() + deltaY);
		 } else if (moveAllowed) {
		    node.setBounds(node.getX() + deltaX,
				   node.getY() + deltaY,
				   node.getWidth(),
				   node.getHeight());
		 }
	     }
	     moved = true;
	} else {
	    while (selectionEn.hasNext()) {
		PNode node = (PNode) selectionEn.next();
		if (node instanceof GuessPNode) {
		    ((GuessPNode)node).inTransition(node.getX() + deltaX,
						    node.getY() + deltaY);
		} else if (moveAllowed) {
		    node.setBounds(node.getX() + deltaX,
				   node.getY() + deltaY,
				   node.getWidth(),
				   node.getHeight());
		}
	    }
	}

    }
}
