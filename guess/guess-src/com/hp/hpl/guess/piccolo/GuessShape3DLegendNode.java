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

public class GuessShape3DLegendNode extends PPath {
    

    private int style = VisFactory.RECTANGLE;

    public int getStyle() {
	return(style);
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

    private Paint gradPaint = null;
    private Color flatColor = null;

    public GuessShape3DLegendNode(Node owner) {
	super();
	GuessShapeNode3D gsn = (GuessShapeNode3D)owner.getRep();
	style = gsn.getStyle();
	double width = gsn.getWidth();
	double height = gsn.getHeight();
	if (style == PFactory.RECTANGLE3D) {
	    append(new Rectangle2D.Double(0,0,width,height),false);
	} else if (style == PFactory.ELLIPSE3D) {
	    append(new Ellipse2D.Double(0,0,width,height),false);
	} else if (style == PFactory.ROUNDRECTANGLE3D) {
	    append(new RoundRectangle2D.Double(0,0,
					       width,
					       height,
					       2,2),false);
	}  else if (style > 100) {
	    // user defined style, lets see if we know about it
	    Shape s = ShapeDB.getShapeDB().getShape(style);
	    if (s == null) {
		// not defined yet
		append(new Rectangle2D.Double(0,0,width,height),false);
	    } else {
		append(s,false);
		setLocation(0,0,width,height);
	    }
	}
	gradPaint = gsn.gradPaint;
	flatColor = gsn.flatColor;
	setGradPaint();
	setStrokePaint(gsn.getStrokePaint());
    }


    Color curcolor = null;

    public void setPaint(Color c) {
	Color oldcolor = (Color)getPaint();
	super.setPaint(c);
	curcolor = c;
	if ((oldcolor != null) && 
	    (oldcolor.getAlpha() != curcolor.getAlpha())) {
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
    
    public void setColor(Color c)
    {
	setPaint(c);
    }

    public void setShape(int type) {
    }

    public void setShape(Shape shp, int type) {
	this.style = type;
	setPathTo(shp);
    }

    private String label = "";

    public void setLabel(String str) {
	//System.out.println("label: " + label);
	this.label = str;
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

	setBounds(x,
		  y,
		  width,
		  height);
    }

    private LabelText labelText = null;

    private boolean highlightMode = false;
    private boolean labelMode = false;

    
    private static BasicStroke linestroke = new BasicStroke((float).3);
    
    public void paint(PPaintContext apc) {
	Graphics2D g2 = apc.getGraphics();
	g2.setPaint(GuessShapeNode3D.shadow);
	int shadowOffset = GuessShapeNode3D.shadowOffset;

	if (style == VisFactory.RECTANGLE3D) {
	    g2.fillRect((int)getX()+shadowOffset,
			(int)getY()+shadowOffset,
			(int)getWidth(),(int)getHeight());
	} else if (style == VisFactory.ELLIPSE3D) {
	    g2.fillOval((int)getX()+shadowOffset,
			(int)getY()+shadowOffset,
			(int)getWidth(),(int)getHeight());
	} else if (style == VisFactory.ROUNDRECTANGLE3D) {
	    g2.fillRoundRect((int)getX()+shadowOffset,
			     (int)getY()+shadowOffset,
			     (int)getWidth(),(int)getHeight(),2,2);
	}
	g2.setStroke(linestroke);
	super.paint(apc);
    }

    public double getDrawWidth() {
	return(getWidth());
    }

    public double getDrawHeight() {
	return(getHeight());
    }
}
