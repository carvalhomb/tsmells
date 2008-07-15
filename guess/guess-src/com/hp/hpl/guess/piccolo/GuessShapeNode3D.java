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
import java.util.*;
import edu.umd.cs.piccolo.activities.*;

import com.hp.hpl.guess.Guess;
import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.piccolo.GFrame;

public class GuessShapeNode3D extends GuessShapeNode {

    public static int shadowOffset = 2;

    public GuessShapeNode3D(Shape aShape, Node owner, 
			    GFrame frame, int style) {
	super(aShape,owner,frame,style);
    }


    
    protected Color flatColor = null;
    protected Paint gradPaint = null;

    public void set(String field, Object o) {
	/*try {*/
	if (field.equals("color")) {
	    if (o instanceof Color) {
		setPaint((Color)o);
	    } else {
		setPaint((Color)(Colors.getColor((String)o,flatColor)));
	    }
	} else if (field.equals("strokecolor")) {
	    super.set("strokecolor",flatColor);
	} else {
	    super.set(field,o);
	}
    }

    public Object get(String field) {
	if (field.equals("color")) {
	    return(flatColor);
	} else {
	    return(super.get(field));
	}
    }

    public void highlight(boolean state) {
	
	//System.out.println(owner.getName() + " " + state);
	//Thread.dumpStack();

	if (!getVisible()) 
	    return;

	if (labelText == null) {
	    labelText = new LabelText(this);
	}

	if (state) {
	    //System.out.println("**** " + label);
	    labelText.setText(label);
	    labelText.setPaint(new Color(100,100,100,210));
	    labelText.setTextPaint(Color.yellow);
	    float scaling = (float)(1/frame.getGCamera().getViewScale());
	    labelText.setX(getX() + getWidth()+1*scaling);
	    labelText.setY(getY() + getHeight());
	    frame.labels.addChild(labelText);
	    if ((label != null) && (label.equals(owner.getName()))) {
		StatusBar.setStatus(label);
	    } else if (label == null) {
		StatusBar.setStatus(owner.getName());
	    } else {
		StatusBar.setStatus(owner.getName() + " (" + label + ")");
	    }
	} else {
	    labelText.removeFromParent();
	    StatusBar.setStatus("");
	}
	highlightMode = state;
    }
    
    private void setGradPaint() {
	if (flatColor == null)
	    return;

	float p1x = (float)(getX() + getWidth() / 8);
	float p1y = (float)(getY() + getHeight() / 8);
	float p2x = (float)(getX() + getWidth());
	float p2y = (float)(getY() + getHeight());
	gradPaint = new GradientPaint(p1x,p1y,Color.white,
				      p2x,p2y,flatColor);
	setPaint(gradPaint);
    }

    public boolean setBounds(double x1, double y1, 
			    double width, double height) {
	boolean newB = super.setBounds(x1,y1,width,height);
	setGradPaint();
	return(newB);
    }

    public boolean setBounds(java.awt.geom.Rectangle2D newBounds) {
	// notify Node that we're resizing
	boolean  newB = super.setBounds(newBounds);
	setGradPaint();
	return(newB);
    }
    
    public void setPaint(Color c) {
	Color oldcolor = flatColor;
	flatColor = c;
	setGradPaint();
	if ((oldcolor != null) && 
	    (oldcolor.getAlpha() != flatColor.getAlpha())) {
	    // change in transparency, we should change
	    // the color of the line we use
	    Color oldSP = (Color)getStrokePaint();
	    setStrokePaint(Colors.getColor(oldSP.getRed()+","+
					   oldSP.getGreen()+","+
					   oldSP.getBlue()+","+
					   curcolor.getAlpha(),
					   oldSP));
	}
    }

    protected static final Color shadow = new Color(125,125,125,125);

    public void paintOverload(PPaintContext apc) {
	Graphics2D g2 = apc.getGraphics();
	g2.setPaint((Color)shadow);
	if (super.getStyle() == VisFactory.RECTANGLE3D) {
	    g2.fillRect((int)getX()+shadowOffset,
			(int)getY()+shadowOffset,
			(int)getWidth(),(int)getHeight());
	} else if (super.getStyle() == VisFactory.ELLIPSE3D) {
	    g2.fillOval((int)getX()+shadowOffset,
			(int)getY()+shadowOffset,
			(int)getWidth(),(int)getHeight());
	} else if (super.getStyle() == VisFactory.ROUNDRECTANGLE3D) {
	    g2.fillRoundRect((int)getX()+shadowOffset,
			     (int)getY()+shadowOffset,
			     (int)getWidth(),(int)getHeight(),2,2);
	}
	if (labelMode) {
	    float scaling = (float)(1/frame.getGCamera().getViewScale());
	    Font f = g2.getFont();
	    f = f.deriveFont((float)(12*scaling));
	    g2.setFont(f);
	    g2.setPaint(curcolor);
	    paintLabel(g2,(float)(getX() + getWidth()+2),
		       (float)(getY() + getHeight()),f);
	} 
	g2.setStroke(linestroke);
    }
}
    
