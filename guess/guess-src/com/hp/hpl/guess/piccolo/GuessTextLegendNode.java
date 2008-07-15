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

import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.piccolo.GFrame;

public class GuessTextLegendNode extends PText {
    

    private int style = VisFactory.TEXT_ROUNDRECTANGLE;
    
    public int getStyle() {
	return(style);
    }

    private RectangularShape borderShape = null;

    public void setStyle(int s) {

	if ((style == s) && (borderShape != null)) {
	    return;
	}

	if (s == VisFactory.TEXT_RECTANGLE) {
	    borderShape = 
		new Rectangle2D.Double(getX()-1,
				       getY()-1,
				       getWidth()+2,
				       getHeight()+2);
	} else if (s == VisFactory.TEXT_ELLIPSE) {
	    borderShape = 
		new Ellipse2D.Double(getX()-4,
				     getY()-4,
				     getWidth()+8,
				     getHeight()+8);
	} else {
	    borderShape = 
		new RoundRectangle2D.Double(getX()-1,
					    getY()-1,
					    getWidth()+2,
					    getHeight()+2,
					    2,2);
	}
	style = s;
    }

    Paint curcolor = null;

    public GuessTextLegendNode(Node owner) {
	super(owner.getName());
	GuessTextNode gtn = (GuessTextNode)owner.getRep();
	setStyle(gtn.getStyle());
	curcolor = gtn.getPaint();
	setTextPaint(gtn.getTextPaint());
	strokePaint = gtn.strokePaint;
    }


    public boolean setBounds(java.awt.geom.Rectangle2D newBounds) {
	// notify Node that we're resizing
	boolean toRet = super.setBounds(newBounds);
	return(toRet);
    }

    Color strokePaint = Color.black;
    
    public void setColor(Color c)
    {
	setPaint(c);
    }

    public void setShape(Shape shp, int style) {
    }

    public String getLabel()
    {
	return getText();
    }
    
    public void setLabel(String label) {
	setText(label);
    }

    public void setLocation(double x1, double y1, 
			    double width, double height) {

	setBounds(x1,
		  y1,
		  width,
		  height);
    }

    public void setLocation(double x1, double y1) {
	
	double w = super.getWidth();
	double h = super.getHeight();

	//System.out.println("f: " + x1 + " " + y1);

	setBounds(x1,
		  y1,
		  w,
		  h);
    }


    public void setSize(double width, double height) {
	
	double x = super.getX();
	double y = super.getY();

	setBounds(x-width,
		  y-height,
		  width,
		  height);
    }

    public double getDrawWidth() {
	return(getWidth());
    }

    public double getDrawHeight() {
	return(getHeight());
    }

    private boolean highlightMode = false;
    private boolean labelMode = false;

    public void label(boolean state) {
	labelMode = state;
    }

    private static BasicStroke linestroke = new BasicStroke((float).3);
    
    public void paint(PPaintContext apc) {
	Graphics2D g2 = apc.getGraphics();
	if (highlightMode) {
	    g2.setPaint(Color.yellow);
	} else {
	    g2.setPaint(curcolor);
	}

	if (borderShape == null) {
	    // if no one's ever told us what shape to use
	    // go with the default
	    setStyle(style);
	}

	if (style == VisFactory.TEXT_ELLIPSE) {
	    borderShape.setFrame(getX()-4,
				 getY()-4,
				 getWidth() + 8,
				 getHeight() + 8);
	} else {
	    borderShape.setFrame(getX()-1,
				 getY()-1,
				 getWidth() + 2,
				 getHeight() + 2);
	}
	
	g2.fill(borderShape);
	g2.setPaint(strokePaint);
	g2.setStroke(linestroke);
	g2.draw(borderShape);
	super.paint(apc);
    }

    public static int ZOOM_SIZE = 40;
}
