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

import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.piccolo.GFrame;

public class GuessLegendEdge extends PPath {

    double n1x = 0;
    double n1y = 0;
    double n2x = 10;
    double n2y = 10;

    boolean loop = false;

    boolean arrows = false;

    int destination = 0;

    static final int toN1 = 1;
    static final int toN2 = 2;
    static final int toN1N2 = 3;

    public GuessLegendEdge(Edge owner) {
	GuessPEdge gpe = (GuessPEdge)owner.getRep();
	setColor((Color)gpe.getColor());
	setLineWidth(gpe.getLineWidth());
	Node n1 = owner.getNode1();
	Node n2 = owner.getNode2();
	if (n1 == n2) {
	    loop = true;
	}
	arrows = VisFactory.getFactory().getDirected();
	if (arrows) {
	    if (owner instanceof UndirectedEdge) {
		destination = toN1N2;
	    } else {
		if (!loop) {
		    n1 = (Node)((DirectedEdge)owner).getSource();
		    if (n1 == n2) {
			destination = toN2;
		    } else {
			destination = toN1;
		    }
		}
	    }
	}
	readjust();
    }

    
    public void setShape(Shape aShape) {
	setPathTo(aShape);
    }

    public void setLineWidth(double width) {
	setStroke(new BasicStroke((float)width));
    }

    Color curcolor = Color.black;

    public void setColor(Color clr) {
	super.setStrokePaint(clr);
	curcolor = clr;
	super.setPaint(null);
    }

    public Paint getColor() {
	return(curcolor);
    }

    public double getLineWidth() {
	return((double)((BasicStroke)getStroke()).getLineWidth());
    }

    public void setCoords(double x1,double y1, double x2, double y2) {
	n1x = x1;
	n2x = x2;
	n1y = y1;
	n2y = y2;
	readjust();
    }
    
    public double getWidth() {
	return(Math.max(n2x-n1x,n1x-n2x));
    }

    public double getHeight() {
	return(Math.max(n2y-n1y,n1y-n2y));
    }

    public void readjust() {

	if (!loop) {
	    if (arrows) {
		double[] x1s = new double[]{n1x+1/2,
					    n1x+1/2,
					    n1x,
					    n1x+1};
		double[] y1s = new double[]{n1y,
					    n1y+1,
					    n1y+1/2,
					    n1y+1/2};
		
		double[] x2s = new double[]{n2x+1/2,
					    n2x+1/2,
					    n2x,
					    n2x+1};
		
		double[] y2s = new double[]{n2y,
					    n2y+1,
					    n2y+1/2,
					    n2y+1/2};
		
		double mindist = Double.MAX_VALUE;
		double x1 = 0;
		double y1 = 0;
		double x2 = 0;
		double y2 = 0;
		
		for (int i = 0 ; i <= 3 ; i++) {
		    for (int j = 0 ; j <= 3 ; j++) {
			double dist = Math.sqrt(Math.pow(x1s[i]-x2s[j],2)+
						Math.pow(y1s[i]-y2s[j],2));
			if (dist < mindist) {
			    x1 = x1s[i];
			    x2 = x2s[j];
			    y1 = y1s[i];
			    y2 = y2s[j];
			    mindist = dist;
			}
		    }
		}
		setShape(new Line2D.Double(x1,y1,x2,y2));
		if (destination == toN1) {
		    // put the arrow head at point 1
		    arrow_style = ARROW_END;
		    p1 = new Point2D.Double(x1,y1);
		    p2 = new Point2D.Double(x2,y2); 
		} else if (destination == toN2) {
		    // put the arrow head at point 2
		    arrow_style = ARROW_START;
		    p1 = new Point2D.Double(x1,y1);
		    p2 = new Point2D.Double(x2,y2); 
		} else if (destination == toN1N2) {
		    // we want arrow heads at both ends
		    //addArrowHead(ARROW_OPEN,new Point2D.Double(x1,y1),
		    //	 new Point2D.Double(x2,y2));
		    arrow_style = ARROW_BOTH;
		    p1 = new Point2D.Double(x1,y1);
		    p2 = new Point2D.Double(x2,y2);
		}
	    } else {
		setShape(new Line2D.Double(n1x + 
					   1 / 2,
					   n1y + 
					   1 / 2,
					   n2x + 
					   1 / 2,
					   n2y + 
					   1 / 2));
		arrow_style = ARROW_NONE;
		p1 = null;
		p2 = null;
	    }
	} else {
	    setShape(new Arc2D.Double(n1x,
				      n1y,
				      n2x - n1x,
				      n2y - n1y,
				      (double)20,
				      (double)200,
				      Arc2D.OPEN));
	    arrow_style = ARROW_NONE;
	    p1 = null;
	    p2 = null;
	}
    }
    
    private int arrow_style = ARROW_NONE;
    
    private Point2D p1 = null;
    private Point2D p2 = null;
    public void paint(PPaintContext apc) {
	super.paint(apc);
	if (arrow_style == ARROW_NONE) {
	    return;
	}
	if ((p1 == null) || (p2 == null)) {
	    return;
	}
	Graphics2D g2 = apc.getGraphics();
	if (arrow_style == ARROW_BOTH) {
	    Arrow.drawArrow(g2,p1,p2,Arrow.SLEEK,getLineWidth());
	    Arrow.drawArrow(g2,p2,p1,Arrow.SLEEK,getLineWidth());
	} else if (arrow_style == ARROW_START) {
	    Arrow.drawArrow(g2,p2,p1,Arrow.SLEEK,getLineWidth());
	} else if (arrow_style == ARROW_END) {
	    Arrow.drawArrow(g2,p1,p2,Arrow.SLEEK,getLineWidth());
	}
    }

    static public final int   ARROW_NONE = 0;
    static public final int   ARROW_START = 1;
    static public final int   ARROW_END = 2;
    static public final int   ARROW_BOTH = 3;

}
