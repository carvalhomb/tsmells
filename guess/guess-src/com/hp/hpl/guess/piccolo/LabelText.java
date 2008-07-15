package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import edu.umd.cs.piccolo.activities.*;
import com.hp.hpl.guess.ui.VisFactory;

public class LabelText extends PText {

    private GuessPNode owner = null;

    public LabelText(GuessPNode owner) {
	this.owner = owner;
    }

    private Point2D location = null;

    public LabelText(Point2D location) {
	this.location = location;
    }

    public void setLocation(Point2D location) {
	this.location = location;
	setX(location.getX());
	setY(location.getY()-20);
    }

    private float prevScale = -1;

    public void paint(PPaintContext apc) {
	//	Thread.dumpStack();
	float scaling = (float)(1/((GFrame)VisFactory.getFactory().getDisplay()).getGCamera().getViewScale());

	//System.out.println(scaling + " " + prevScale);	
	//System.out.println(getText());

	//	System.out.println("xxx");
	if (scaling != prevScale) {
	    Font f = getFont();
	    f = f.deriveFont((float)(12*scaling));
	    prevScale = scaling;
	    if (scaling <= .3) {
		scaling = (float).3;
	    }
	    //System.out.println(scaling);
	    if (owner != null) {
		setX(owner.getX() + owner.getWidth()+1*scaling);
		setY(owner.getY() + owner.getHeight());
	    }
	    setFont(f);
	} 
	//repaint();
	super.paint(apc);
    }
}
