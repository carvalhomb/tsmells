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
import java.util.*;

import com.hp.hpl.guess.Guess;
import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.piccolo.GFrame;

public class GuessPEdge extends PPath implements EdgeListener {
    
    private Edge owner = null;
    
    private GFrame frame = null;

    public Edge getOwner() {
	return(owner);
    }

    private Color labelColor = null;

    public void set(String field, Object o) {

	try {
	    if (field.equals("width")) {
		setLineWidth(((Double)o).doubleValue());
	    } else if (field.equals("color")) {
		if (o instanceof Color) {
		    setColor((Color)o);
		} else {
		    setColor((Colors.getColor((String)o,(Color)getColor())));
		}
	    } else if (field.equals("visible")) {
		//if (owner.toString().equals("n2-n4")) {
		//  System.out.println("+++" + owner + " visible: " + o);
		//  Thread.dumpStack();
		//}
		setVisible(((Boolean)o).booleanValue());
		if (getVisible())
		    readjust();
		//Thread.dumpStack();
	    }  else if (field.equals("label")) {
		//		Thread.dumpStack();
		setLabel((String)o);
	    } else if (field.equals("labelvisible")) {
		setLabelVisible(((Boolean)o).booleanValue());
	    } else if (field.equals("labelcolor")) {
		if (o instanceof Color) {
		    labelColor = (Color)o;
		} else {
		    labelColor = 
			(Colors.getColor((String)o,(Color)getColor()));
		}
	    }
	    if (Guess.getMTF()) 
		moveToFront();
	} catch (Exception e) {
	    //e.printStackTrace();
	    throw new Error("Problem with setting rep attribute: " + field + 
			    " " + e.toString());
	}
    }

    public Object get(String field) {
	try {
	    if (field.equals("width")) {
		return(new Double(getLineWidth())); 
	    } else if (field.equals("color")) {
		return(Colors.toString((Color)getColor()));
	    } else if (field.equals("labelcolor")) {
		if (labelColor != null)
		    return(Colors.toString(labelColor));
		return(Colors.toString((Color)getColor()));
	    } else if (field.equals("visible")) {
		return(new Boolean(getVisible()));
	    } else if (field.equals("label")) {
		return(label);
	    } else if (field.equals("labelvisible")) {
		return(new Boolean(labelVisible));
	    } else {
		return(null);
	    }
	} catch (Exception e) {
	    throw new Error("Problem with getting rep attribute: " + 
			    e.toString());
	}
    }

    public GuessPEdge(Edge owner, GFrame frame) {
	this.owner = owner;
	this.frame = frame;
	readjust();
    }

    public GuessPEdge(Shape aShape, Edge owner, GFrame frame) {
	super(aShape);
	this.owner = owner;
	this.frame = frame;
	super.setPaint(null);
    }
    
    public void setShape(Shape aShape) {
	//System.out.println("changing shape: " + aShape);
	setPathTo(aShape);
    }

    public void setLineWidth(double width) {
	setStroke(new BasicStroke((float)width));
	readjust();
    }

    Color curcolor = Color.black;

    public void setColor(Color clr) {
	super.setStrokePaint(clr);
	curcolor = clr;
	super.setPaint(null);
    }


    private boolean mouseOver = false;

    public void highlight(boolean state) {
	if (!getVisible()) 
	    return;

	if (state) {
	    mouseOver = true;
	    InfoWindow.details(owner);
	
	    //System.out.println("ev1");
	    //frame.labels.removeAllChildren();
	    setStrokePaint(Color.yellow);
	    String ownerName = null;
	    if (owner instanceof UndirectedEdge) {
		Node n1 = owner.getNode1();
		Node n2 = owner.getNode2();
		if (n1 != n2) {
		    ((GuessPNode)n1.getRep()).highlight(true);
		    ((GuessPNode)n2.getRep()).highlight(true);
		    ownerName = n1 + " - " + n2;
		} else {
		    ((GuessPNode)n2.getRep()).highlight(true);
		    ownerName = n1 + " self loop";
		}
	    } else {
		Node n1 = (Node)((DirectedEdge)owner).getSource();
		Node n2 = (Node)((DirectedEdge)owner).getDest();
		if (n1 != n2) {
		    ((GuessPNode)n1.getRep()).highlight(true);
		    ((GuessPNode)n2.getRep()).highlight(true);
		    ownerName = n1 + " -> " + n2;
		} else {
		    ((GuessPNode)n2.getRep()).highlight(true);
		    ownerName = n1 + " -> self loop";
		}
	    }
	    if ((label != null) && (label.equals(ownerName))) {
		StatusBar.setStatus(label);
	    } else if (label == null) {
		StatusBar.setStatus(ownerName);
	    } else {
		StatusBar.setStatus(ownerName + " (" + label + ")");
	    }
	} else {
	    mouseOver = false;
	    //System.out.println("ev2");
	    setStrokePaint(curcolor);
	    java.util.List l = 
		new java.util.ArrayList(owner.getIncidentVertices());
	    Node n1 = (Node)l.get(0);
	    ((GuessPNode)n1.getRep()).highlight(false);
	    if (l.size() > 1) {
		n1 = (Node)l.get(1);
		((GuessPNode)n1.getRep()).highlight(false);
	    }
	    StatusBar.setStatus("");
	    //highlight(false);
	    // unhighlight
	}
    }

    public void mouseEntered(PInputEvent aEvent) {
	if (getVisible()) {
	    GraphEvents.mouseEnter(owner);
	}
    }

    public void mouseExited(PInputEvent aEvent) {
	if (getVisible()) {
	    GraphEvents.mouseLeave(owner);
	}
    }

    public Paint getColor() {
	return(curcolor);
    }

    public double getLineWidth() {
	return((double)((BasicStroke)getStroke()).getLineWidth());
    }


    public void mouseClicked(PInputEvent aEvent) {

	if (aEvent.isLeftMouseButton()) {
	    if (frame.getMode() == GFrame.BROWSE_MODE) { 
		if (aEvent.isShiftDown()) {
		    GraphEvents.shiftClick(owner);
		} else {
		    GraphEvents.click(owner);
		}
	    }
	}
    }
    
    public void centerDisplay() {
	java.util.List l = 
	    new java.util.ArrayList(owner.getIncidentVertices());
	Node node1 = (Node)l.get(0);
	Node node2 = node1;
	if (l.size() > 1) {
	    node2 = (Node)l.get(1);
	}
	
	if (node1 == node2) {
	    frame.centerOn((GuessPNode)node1.getRep());
	} else {
	    frame.centerOn((GuessPNode)node1.getRep(),
			   (GuessPNode)node2.getRep());
	}
    }

    private static LabelText labelText = new LabelText(new Point2D.Double(0,0));
    public void highlightOld(boolean state) {
	if (labelText.getParent() == null) {
	    frame.labels.addChild(labelText);
	}

	if (state) {
	    if (owner instanceof UndirectedEdge) {
		labelText.setText(((UndirectedEdge)owner).getNode1()+"-"+
				  ((UndirectedEdge)owner).getNode2());
	    } else {
		labelText.setText(((DirectedEdge)owner).getSource()+"->"+
				  ((DirectedEdge)owner).getDest());
	    }
	    labelText.setPaint(new Color(100,100,100,210));
	    labelText.setTextPaint(Color.yellow);
	    labelText.setVisible(true);
	} else {
	    labelText.setVisible(false);
	}
    }

    private static void findEndPoints(GuessPNode node1, GuessPNode node2, 
				      Point2D pa, Point2D pb) {
	Point2D[] n1 = null;
	Point2D[] n2 = null;
	
	double transform1 = - (node1.getX() + node1.getWidth()/2);
	double transform2 = - (node1.getY() + node1.getHeight()/2);
	double testx = (node2.getX() + node2.getWidth()/2) +
	    transform1;
	double testy = (node2.getY() + node2.getHeight()/2) +
	    transform2;
	double angle = 
	    Math.sqrt(Math.pow(Math.toDegrees(Math.atan2(testy,testx)),
			       2));

	if ((angle < 30) || (angle > 150) || 
	    ((angle > 60) && (angle < 120))) {
	    n1 = node1.getPrefPorts();
	    n2 = node2.getPrefPorts();
	} else {
	    n1 = node1.getAllPorts();
	    n2 = node2.getAllPorts();
	}
	
	double mindist = Double.MAX_VALUE;
	double x1 = 0;
	double y1 = 0;
	double x2 = 0;
	double y2 = 0;
	
	for (int i = 0 ; i < n1.length ; i++) {
	    for (int j = 0 ; j < n1.length ; j++) {
		double dist = Math.sqrt(Math.pow(n1[i].getX()-
						 n2[j].getX(),2)+
					Math.pow(n1[i].getY()-
						 n2[j].getY(),2));
		if (dist < mindist) {
		    x1 = n1[i].getX();
		    x2 = n2[j].getX();
		    y1 = n1[i].getY();
		    y2 = n2[j].getY();
		    mindist = dist;
		}
	    }
	}
	pa.setLocation(x1,y1);
	pb.setLocation(x2,y2);
    }

    public void readjust() {
	java.util.List l = 
	    new java.util.ArrayList(owner.getIncidentVertices());
	GuessPNode node1 = (GuessPNode)((Node)l.get(0)).getRep();
	GuessPNode node2 = node1;
	if (l.size() > 1) {
	    node2 = (GuessPNode)((Node)l.get(1)).getRep();
	}
	if (node1 != node2) {
	    if (VisFactory.getFactory().getDirected()) {
		
		Point2D pa = new Point2D.Double(0,0);
		Point2D pb = new Point2D.Double(0,0);
		findEndPoints(node1,node2,pa,pb);
		double x1 = pa.getX();
		double y1 = pa.getY();
		double x2 = pb.getX();
		double y2 = pb.getY();


		if (VisFactory.getFactory().getDirected()) {
		    // get angle of line from 0 - 360
		    double cx = (x1 + x2)/2;
		    double cy = (y1 + y2)/2;
		    double thetaRadians = 
			Math.atan2(( y1 - y2),
				   (x1 - x2));
		    double buffer = Math.max(2,
					     Arrow.getArrowLength(x1,y1,
								  x2,y2,
								  getLineWidth()) / 2);
		    //System.out.println(" " + buffer);

		    double radius = (Math.sqrt(Math.pow(x1-x2,2)+
					       Math.pow(y1-y2,2)) / 2)-buffer;

		    double tx1 = radius * Math.cos(thetaRadians) + cx;
		    double ty1 = radius * Math.sin(thetaRadians) + cy;

		    thetaRadians += Math.PI;

		    double tx2 = radius * Math.cos(thetaRadians) + cx;
		    double ty2 = radius * Math.sin(thetaRadians) + cy;

		    setShape(new Line2D.Double(tx1,ty1,tx2,ty2));
		} else {
		    setShape(new Line2D.Double(x1,y1,x2,y2));
		}

		if (owner instanceof DirectedEdge) {
		    if (((Node)(((DirectedEdge)owner).getSource())).getRep() == node1) {
			// put the arrow head at point 1
			arrow_style = ARROW_END;
			p1 = new Point2D.Double(x1,y1);
			p2 = new Point2D.Double(x2,y2); 
		    }else {
			// put the arrow head at point 2
			arrow_style = ARROW_START;
			p1 = new Point2D.Double(x1,y1);
			p2 = new Point2D.Double(x2,y2); 
		    }		    
		} else if (owner instanceof UndirectedEdge) {
		    // we want arrow heads at both ends
		    //addArrowHead(ARROW_OPEN,new Point2D.Double(x1,y1),
		    //	 new Point2D.Double(x2,y2));
		    arrow_style = ARROW_BOTH;
		    p1 = new Point2D.Double(x1,y1);
		    p2 = new Point2D.Double(x2,y2);
		}

		if (labelVisible) {
		    // label should be halfway between the nodes
		    // and moved a little bit
		    labelX = (x1 + x2)/2 + 2;
		    
		    if (x1 != x2) {
			double slope = (y1 - y2) / (x1 - x2);
			//System.out.println("slope: " + slope);
			if ((slope >= -.5) && (slope <= 0)) {
			    labelY = (y1 + y2)/2 + 2;
			} else if ((slope > 0) && (slope <= .5)) {
			    labelY = (y1 + y2)/2 - 2;
			} else {
			    labelY = (y1 + y2)/2;
			}
		    } else {
			labelY = (y1 + y2)/2;
		    }
		}

	    } else {
		
		// just a line, lets figure out where to place
		// the ends
		double lx1 = node1.getX() + 
		    node1.getWidth() / 2;
		double ly1 = node1.getY() + 
		    node1.getHeight() / 2;
		double lx2 = node2.getX() + 
		    node2.getWidth() / 2;
		double ly2 =  node2.getY() + 
		    node2.getHeight() / 2;

		setShape(new Line2D.Double(lx1,
					   ly1,
					   lx2,
					   ly2));

		if (labelVisible) {
		    
		    labelX = (lx1 + lx2)/2 + 2;
		    
		    if (lx1 != lx2) {
			double slope = (ly1 - ly2) / (lx1 - lx2);
			//System.out.println("slope: " + slope);
			if ((slope >= -.5) && (slope <= 0)) {
			    labelY = (ly1 + ly2)/2 + 2;
			} else if ((slope > 0) && (slope <= .5)) {
			    labelY = (ly1 + ly2)/2 - 2;
			} else {
			    labelY = (ly1 + ly2)/2;
			}
		    } else {
			labelY = (ly1 + ly2)/2;
		    }

		    //System.out.println(owner.toString() + " ly1: " + 
		    //ly1 + " ly2: " + ly2);
		   

		    //double adjustment = -1;

		    //   if (lx1 != lx2) { 
		    //double slope = (ly1 - ly2) / (lx1 - lx2);
		    //if (slope > 0) {
		    //    adjustment = 1;
		    //}
		    //}

		    //labelX = (lx1 + lx2)/2;
		    //labelY = (ly1 + ly2)/2;

		}

		arrow_style = ARROW_NONE;
		p1 = null;
		p2 = null;
	    }
	} else {
	    double x1 = node1.getX() + node1.getWidth()/2;
	    double y1 = node1.getY() + node1.getHeight()/2;
	    double size = Math.max(node1.getHeight(),
				   node1.getWidth());
	    setShape(new Ellipse2D.Double(x1 - size,
					  y1 - size,
					  size,
					  size));
	    if (labelVisible) {
		labelX = x1 - size/2;
		labelY = y1 - size/2 - 1;
	    }

	    arrow_style = ARROW_NONE;
	    p1 = null;
	    p2 = null;
	}
    }
    
    /**
     * this is a special method that "twists" the edge
     * around a central control point so that
     * @param upd we can twist it up or down
     */
    public void readjustJiggle(boolean upd) {
	java.util.List l = 
	    new java.util.ArrayList(owner.getIncidentVertices());
	GuessPNode node1 = (GuessPNode)((Node)l.get(0)).getRep();
	GuessPNode node2 = node1;
	if (l.size() > 1) {
	    node2 = (GuessPNode)((Node)l.get(1)).getRep();
	}
	if (node1 != node2) {
	    if (VisFactory.getFactory().getDirected()) {

		Point2D pa = new Point2D.Double(0,0);
		Point2D pb = new Point2D.Double(0,0);
		findEndPoints(node1,node2,pa,pb);
		double x1 = pa.getX();
		double y1 = pa.getY();
		double x2 = pb.getX();
		double y2 = pb.getY();

		setShapeJiggle(x1,y1,x2,y2,upd);
		if (owner instanceof DirectedEdge) {
		    if (((Node)(((DirectedEdge)owner).getSource())).getRep() == node1) {
			// put the arrow head at point 1
			arrow_style = ARROW_END;
			p1 = new Point2D.Double(x1,y1);
			p2 = new Point2D.Double(x2,y2); 
		    }else {
			// put the arrow head at point 2
			arrow_style = ARROW_START;
			p1 = new Point2D.Double(x1,y1);
			p2 = new Point2D.Double(x2,y2); 
		    }		    
		} else if (owner instanceof UndirectedEdge) {
		    // we want arrow heads at both ends
		    //addArrowHead(ARROW_OPEN,new Point2D.Double(x1,y1),
		    //	 new Point2D.Double(x2,y2));
		    arrow_style = ARROW_BOTH;
		    p1 = new Point2D.Double(x1,y1);
		    p2 = new Point2D.Double(x2,y2);
		}
	    } else {
		setShapeJiggle(node1.getX() + 
			       node1.getWidth() / 2,
			       node1.getY() + 
			       node1.getHeight() / 2,
			       node2.getX() + 
			       node2.getWidth() / 2,
			       node2.getY() + 
			       node2.getHeight() / 2,
			       upd);
		arrow_style = ARROW_NONE;
		p1 = null;
		p2 = null;
	    }
	}
    }

    private void setShapeJiggle(double x1, double y1, 
				double x2, double y2, 
				boolean upd) {
	
	double s = 0;
	if (y2 == y1) {
	    if (upd) {
		s = 1 / (1 / (x2 - x1));
	    } else {
		s = 1 / (-1 / (x2 - x1));
	    }
	} else {
	    s = 1 / ((y2 - y1) / (x2 - x1));
	}
	
	double x3 = (x1 + x2)/2;
	double y3 = (y1 + y2)/2;
	
	double dist = Math.min(4,Point2D.distance(x1,y1,x2,y2) / 8);
	
	double a = s * s + 1;
	double b = -(2 + 2 * s * s) * x3;
	double c = x3 * x3 + s * s * x3 * x3 - dist * dist;
	double x41 = (-b + Math.sqrt(b*b - 4 * a * c)) / (2 * a);
	double x42 = (-b - Math.sqrt(b*b - 4 * a * c)) / (2 * a);
	
	double y41 = y3 + s * (x3 - x41);
	double y42 = y3 + s * (x3 - x42);
	
	if (upd) {
	    setShape(new QuadCurve2D.Double(x1,y1,
					    x41,y41,
					    x2,y2));
	} else {
	    setShape(new QuadCurve2D.Double(x1,y1,
					    x42,y42,
					    x2,y2));
	}
    }

    private int arrow_style = ARROW_NONE;
    
    private Point2D p1 = null;
    private Point2D p2 = null;

    private boolean labelVisible = false;
    private String label = "";

    private String[] multiLineLabel = null;

    private double labelX = 0;
    private double labelY = 0;

    public void setLabel(String str) {
	//System.out.println("label: " + label);
	if (str == null)
	    str = "";
	this.label = str;
	if (label.indexOf("\n") >= 0) {
	    multiLineLabel = breakupLines(str);
	} else {
	    multiLineLabel = null;
	}
    }

    public void setLabelVisible(boolean state) {
	labelVisible = state;
	if (state) {
	    if ((label == null) || (label.equals(""))) {
		label = ""+owner.edgeWeight();
	    }
	}
	readjust();
    }
    
    public static String[] breakupLines(String text) { 
	String[] toRet = null;
	StringTokenizer st = new StringTokenizer(text,"\n");
	Vector v = new Vector();
	while (st.hasMoreTokens()) {
	    v.addElement(st.nextToken());
	}
	toRet = new String[v.size()];
	for (int i = 0 ; i < v.size() ; i++) {
	    toRet[i] = (String)v.elementAt(i);
	}
	return(toRet);
    }

    public void paintLabel(Graphics2D g, 
			   float labelX, 
			   float labelY,
			   Font font) { 

	Color cur = g.getColor();
	if (labelColor != null)
	    g.setColor(labelColor);

	if (multiLineLabel == null) {
	    g.drawString(label,(float)labelX,(float)labelY);
	    g.setColor(cur);
	    return;
	}
	
	FontMetrics fontMetrics = 
	    Toolkit.getDefaultToolkit().getFontMetrics(font); 
	
	int fontHeight = fontMetrics.getHeight(); 
	int fontAscent = fontMetrics.getAscent(); 
	
	int num_lines = multiLineLabel.length; 
	float height; 
	int i; 
	
	for (i=0, height=labelY; i<num_lines; i++, height+=fontHeight) { 
	    g.drawString(multiLineLabel[i], labelX, height); 
	} 
	g.setColor(cur);
    }


    public void paint(PPaintContext apc) {
	super.paint(apc);

	Graphics2D g2 = apc.getGraphics();
	if (labelVisible) {
	    if (mouseOver) {
		g2.setPaint(Color.yellow);
	    } else {
		g2.setPaint(curcolor);
	    }
	    float scaling = (float)(1/frame.getGCamera().getViewScale());
	    Font f = g2.getFont();
	    f = f.deriveFont((float)(12*scaling));
	    g2.setFont(f);
	    paintLabel(g2,(float)labelX,(float)labelY,f);
	}

	if (arrow_style == ARROW_NONE) {
	    return;
	}

	if ((p1 == null) || (p2 == null)) {
	    return;
	}

	if (arrow_style == ARROW_BOTH) {
	    Arrow.drawArrow(g2,p1,p2,Arrow.SLEEK,getLineWidth());
	    Arrow.drawArrow(g2,p2,p1,Arrow.SLEEK,getLineWidth());
	} else if (arrow_style == ARROW_START) {
	    Arrow.drawArrow(g2,p2,p1,Arrow.SLEEK,getLineWidth());
	} else if (arrow_style == ARROW_END) {
	    Arrow.drawArrow(g2,p1,p2,Arrow.SLEEK,getLineWidth());
	}


    }

    static public final int ARROW_NONE = 0;
    static public final int ARROW_START = 1;
    static public final int ARROW_END = 2;
    static public final int ARROW_BOTH = 3;

}
