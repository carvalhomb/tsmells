package edu.umd.cs.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;

import com.hp.hpl.guess.ui.NodeListener;
import com.hp.hpl.guess.piccolo.*;

public class PHighlightText extends PText {
    
    GuessPNode parent2 = null;

    public void setParent2(GuessPNode parent2) {
	this.parent2 = parent2;
    }
    
    public void paintAfterChildren(PPaintContext p) {
	super.paintAfterChildren(p);
	setThings(p.getCamera());
    }
   
    public void setThings(PCamera cam) {
	System.out.println("xxx");
	float scaling = (float)(1/cam.getViewScale());
	Font f = getFont();
	f = f.deriveFont((float)(12*scaling));
	setFont(f);
	// System.out.println("font size: " + f.getSize());
	setX(parent2.getX() + parent2.getWidth()/2 + 4*scaling);
	setY(parent2.getY() + parent2.getHeight()/2 + 4*scaling);

	PBounds pb = cam.getViewBounds();

	double x1 = getX();
	double x2 = getX() + getWidth();
	double y1 = getY();
	double y2 = getY() + getHeight();
	
	double camX1 = pb.getX();
	double camX2 = pb.getX() + pb.getWidth();
	double camY1 = pb.getY();
	double camY2 = pb.getY() + pb.getHeight();

	if (((x1 < camX1) && (x2 < camX1)) ||
	    ((x2 > camX2) && (x2 > camX2)) ||
	    ((y1 < camY1) && (y2 < camY1)) ||
	    ((y1 > camY2) && (y2 > camY2))) {
	    // node isn't on the screen
	    return;
	}

	// move the label around if the node is still on the screen	
	if ((getX() + getWidth()) >
	    (pb.getX() + pb.getWidth())) {
	    double newX = (getX() + getWidth()) - (pb.getX() + pb.getWidth());
	    setX(getX()-newX);
	}
	if ((getY() + getHeight()) >
	    (pb.getY() + pb.getHeight())) {
	    double newY = (getY() + getHeight()) - (pb.getY() + pb.getHeight()) + getHeight()/2;
	    setY(getY()-newY);
	}
	if (getY() < pb.getY()) {
	    setY(getY() + pb.getY());
	}
	if (getX() < pb.getX()) {
	    setX(getX() + pb.getX());
	}
    }
}
