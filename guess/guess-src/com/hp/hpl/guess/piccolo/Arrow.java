package com.hp.hpl.guess.piccolo;

import java.awt.*;
import java.awt.geom.*;
import java.util.HashMap;
import com.hp.hpl.guess.ui.VisFactory;

/**
 * This pluggable utility paints either a "classic" or a "sleek" filled arrow 
 * on a given edge. To use, create an instance of the Arrow object
 * with your preferred thickness, and then call 
 * arrow.drawArrow( graphics, source_x1, source_y1, dest_x, dest_y2 ) for the edge.
 * 
 * Note that the arrow simply uses the color currently set in the graphics context.
 * 
 * @author Jon Froehlich
 * @pyobj Arrow
 * @pyimport from com.hp.hpl.guess.piccolo import Arrow
 */
public class Arrow {

    public static final int CLASSIC = 1;
    public static final int SLEEK   = 2;
    
    protected static int m_arrowLength = -1;
    protected static int m_arrowWidth  = -1;
       
    /**
     * @pyexport
     */
    public static void overrideArrowLength(int length) {
	m_arrowLength = length;
	VisFactory.getFactory().getDisplay().repaint();
    }

    /**
     * @pyexport
     */
    public static void overrideArrowWidth(int width) {
	m_arrowWidth = width;
	VisFactory.getFactory().getDisplay().repaint();
    }

    public static double distance(Point2D p1, Point2D p2) {
	return Math.sqrt(Math.pow(p1.getX() - p2.getX(),2) +
			 Math.pow(p1.getY() - p2.getY(),2));
    }

    public static double getArrowLength(double x1, double y1,
					double x2, double y2,
					double width) {
	int m_arrowLength = Arrow.m_arrowLength;
	if (m_arrowLength < 0) 
	    m_arrowLength = (int)Math.max(4,(width * 2));		

	double dist = Math.sqrt(Math.pow(x1 - x2,2) +
				Math.pow(y1 - y2,2));
	if (m_arrowLength > (dist / 2)) {
	    m_arrowLength = (int)(dist / 2);
	}
	return(m_arrowLength);
    }

    public static void drawArrow(Graphics2D g2d, 
				 Point2D point1, 
				 Point2D point2,
				 int type,
				 double width) {
	
	int m_arrowWidth = Arrow.m_arrowWidth;
	if (m_arrowWidth < 0) 
	    m_arrowWidth = (int)Math.max(2,(width * 4));

	int m_arrowLength = Arrow.m_arrowLength;
	if (m_arrowLength < 0) 
	    m_arrowLength = (int)Math.max(4,(width * 2));		

	double dist = distance(point1,point2);
	if (m_arrowLength > (dist / 2)) {
	    m_arrowLength = (int)(dist / 2);
	}

	// get angle of line from 0 - 360
	double thetaRadians = 
	    Math.atan2(( point1.getY() - point2.getY()),
		       (point1.getX() - point2.getX()))+
	    Math.PI;
	
	AffineTransform at = new AffineTransform();
	at.translate(point2.getX(),point2.getY());
	at.rotate(thetaRadians);
	Shape arrow = null;
	if (type == SLEEK) {
	    arrow = at.createTransformedShape(getSleekArrow(m_arrowWidth,
							    m_arrowLength));
	} else {
	    arrow = at.createTransformedShape(classic);
	}
	g2d.fill(arrow);
    }
	
    static HashMap arrowCache = new HashMap();

    static GeneralPath classic = getClassicArrow();
    
    protected static GeneralPath getSleekArrow(int m_arrowWidth,
					       int m_arrowLength) {
	Dimension d = new Dimension(m_arrowWidth,m_arrowLength);
	if (arrowCache.containsKey(d)) {
	    return((GeneralPath)arrowCache.get(d));
	}
	GeneralPath arrowhead = new GeneralPath();
	float distance = 0;
	//		(float) point1.distance(point2)-vertexDiam/2.0f;
	// create arrowed line general path
	int width = (int) (m_arrowWidth/2.0f);
	arrowhead.moveTo( 0, 0);
	arrowhead.lineTo( (- m_arrowLength), width);
	arrowhead.lineTo( (- m_arrowLength) , -width);
	arrowhead.lineTo( 0, 0 );
	arrowCache.put(d,arrowhead);
	return arrowhead;
    }
    
    protected static GeneralPath getClassicArrow(){
	GeneralPath arrowhead = new GeneralPath();
	//		float distance = (float) point1.distance(point2)-vertexDiam/2.0f;
	float distance = 0;
	// create arrowed line general path
	int width = (int) (m_arrowWidth/2.0f);
	arrowhead.moveTo( distance , 0);
	arrowhead.lineTo( (distance - m_arrowLength), width);
	arrowhead.lineTo( (distance - m_arrowLength) , -width);
	arrowhead.lineTo( distance , 0 );
	return arrowhead;
    }
}
