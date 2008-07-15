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

public class GuessShapeLegendNode extends PPath {
    

    private int style = VisFactory.RECTANGLE;

    public int getStyle() {
	return(style);
    }

    public GuessShapeLegendNode(Node owner) {
	super();
	GuessShapeNode gsn = (GuessShapeNode)owner.getRep();
	int type = gsn.getStyle();
	double width = gsn.getWidth();
	double height = gsn.getHeight();
	if (type == PFactory.RECTANGLE) {
	    append(new Rectangle2D.Double(0,0,width,height),false);
	} else if (type == PFactory.ELLIPSE) {
	    append(new Ellipse2D.Double(0,0,width,height),false);
	} else if (type == PFactory.ROUNDRECTANGLE) {
	    append(new RoundRectangle2D.Double(0,0,
					       width,
					       height,
					       2,2),false);
	}  else if (type > 100) {
	    // user defined style, lets see if we know about it
	    Shape s = ShapeDB.getShapeDB().getShape(type);
	    if (s == null) {
		// not defined yet
		append(new Rectangle2D.Double(0,0,width,height),false);
	    } else {
		append(s,false);
		setLocation(0,0,width,height);
	    }
	}
	setStrokePaint(gsn.getStrokePaint());
	setPaint(gsn.getPaint());
    }


    public boolean setBounds(java.awt.geom.Rectangle2D newBounds) {
	// notify Node that we're resizing
	boolean toRet = super.setBounds(newBounds);
	return(toRet);
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
	if (labelMode) {
	    float scaling = (float)(1/((GFrame)VisFactory.getFactory().getDisplay()).getGCamera().getViewScale());
	    Font f = g2.getFont();
	    f = f.deriveFont((float)(12*scaling));
	    g2.setFont(f);
	    g2.setPaint(curcolor);
	    g2.drawString(label,(float)(getX() + getWidth()+1),
			  (float)(getY() + getHeight()));
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
