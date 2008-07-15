package com.hp.hpl.guess.piccolo;

import java.awt.*;
import java.awt.geom.*;
import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.*;
import edu.umd.cs.piccolo.PNode;
import org.python.core.PySequence;
import java.util.Collection;

public class PFactory extends VisFactory implements UIListener {

    private GFrame curFrame = null;

    private static long lastModTime = System.currentTimeMillis();
    
    public static void updateTime() {
	lastModTime = System.currentTimeMillis();
    }

    public static long getLastUpdate() {
	return(lastModTime);
    }

    public PFactory() {
	init();
	GraphEvents.getGraphEvents().addGraphMouseListener(this);
    }

    public GFrame init() {
	//	System.out.println("init called...");
	curFrame = new GFrame();
	return(curFrame);
    }

    public FrameListener getDisplay() {
	//System.out.println("foo foo " + curFrame);
	return(curFrame);
    }

    public NodeListener generateNode(Node n) {
	return(generateNode(1,500,500,10,10,Color.red,n));
    }

    public NodeListener generateNode(int type, double x, double y,
				     double width, double height,
				     Color clr, Node n) {
	//System.out.println("x on init: " + x);
	GuessPNode toRet = null;
	if (type == ELLIPSE) {
	    toRet = 
		new GuessShapeNode(new Ellipse2D.Double(x,y,width,height),
				   n,curFrame,type);
	} else if (type == ROUNDRECTANGLE) {
	    toRet = 
		new GuessShapeNode(new RoundRectangle2D.Double(x,y,
							       width,
							       height,
							       Math.max(2,width/5),Math.max(2,height/5)),
				   n,curFrame,type);
	} else if (type == RECTANGLE3D) {
	    toRet = 
		new GuessShapeNode3D(new Rectangle2D.Double(x,y,width,height),
				     n,curFrame,type);
	} else if (type == ELLIPSE3D) {
	    toRet = 
		new GuessShapeNode3D(new Ellipse2D.Double(x,y,width,height),
				     n,curFrame,type);
	} else if (type == ROUNDRECTANGLE3D) {
	    toRet = 
		new GuessShapeNode3D(new RoundRectangle2D.Double(x,y,
								 width,
								 height,
								 Math.max(2,width/5),Math.max(2,height/5)),
				     n,curFrame,type);
	} else if ((type == TEXT_RECTANGLE) || 
		   (type == TEXT_ROUNDRECTANGLE) || 
		   (type == TEXT_ELLIPSE)) {
	    toRet = 
		new GuessTextNode(n,x,y,curFrame,type);
	} else if (type == IMAGE) {
	    toRet =
		new GuessImageNode(n,curFrame,type,
				   x,y,width,height);
	} else if (type > 100) {
	    // user defined style, lets see if we know about it
	    Shape s = ShapeDB.getShapeDB().getShape(type);
	    if (s == null) {
		// not defined yet
		toRet = 
		    new GuessShapeNode(new Rectangle2D.Double(x,y,
							      width,height),
				       n,curFrame,type);
	    } else {
		toRet = new GuessShapeNode(s,n,curFrame,type);
		toRet.setLocation(x,y,width,height);
	    }
	} else { //(type == RECTANGLE) {
	    // we're just going to make rectangles the default
	    // shape.  If the user asks for a strange type
	    // that we don't know about just give them a rectangle
	    toRet = 
		new GuessShapeNode(new Rectangle2D.Double(x,y,width,height),
				   n,curFrame,type);
	} 

	if (toRet != null) {
	    toRet.setColor(clr);
	    curFrame.getNodes().addChild((PNode)toRet);
	}

	// piccolo seems to move things around for some reason
	// so we need to fudge this a little and tell it to reset
	// the bounds
	((PNode)toRet).setBounds(new Rectangle2D.Double(x,y,width,height));
	//	System.out.println("x on return: " + toRet.getX());
	return(toRet);
    }

    //removes the given node rep from the collection of nodes to draw
    public void remove(Node n)
    {
	PNode rep = (PNode)n.getRep();
	if (rep.getParent() != null)
	    curFrame.getNodes().removeChild(rep);
    }

    //removes the given edge rep from the collection of edges to draw
    public void remove(Edge e)
    {
	PNode rep = (PNode)e.getRep();
	if (rep.getParent() != null)
	    curFrame.getEdges().removeChild(rep);
    }

    public void add(Node n) {
	PNode pn = (PNode)n.getRep();
	if (pn.getParent() == null) 
	    curFrame.getNodes().addChild(pn);
    }

    public void add(Edge e) {
	PNode pn = (PNode)e.getRep();
	if (pn.getParent() == null) {
	    curFrame.getEdges().addChild(pn);
	    if (pn instanceof GuessPEdge)
		((GuessPEdge)pn).readjust();
	}
    }

    public NodeListener convertNode(GuessPNode rep,
				    int style, 
				    Node n) {

	//System.out.println("foo");
	if ((style <= 100) && (rep.getStyle() == style)) {
	    return(rep);
	}

	boolean vis = ((PNode)rep).getVisible();

	String image = "";
	Object tCol = rep.get("color");
	//if (tCol == null) {
	//  System.out.println(n.__getattr__("color"));
	//}
	
	Color newCol = Color.blue;
	if (tCol != null) {
	    newCol = Colors.getColor(tCol.toString(),
				     Color.blue);
	}

	if (style == IMAGE) {
	    // we're going to want to get the image
	    image = (String)n.__getattr__("image");
	} 

	if (rep.getStyle() == IMAGE) {
	    // we need to recover the paint color from
	    // the Node because image nodes don't keep track
	    newCol = 
		Colors.getColor((String)n.__getattr__("color"),
				Color.blue);
	}

	NodeListener toRet = generateNode(style,rep.getX(),rep.getY(),
					  rep.getWidth(),rep.getHeight(),
					  newCol,n);
	if (toRet == null) {
	    return(rep);
	} 
	
	((PNode)toRet).setVisible(vis);
	
	try {
	    ((PNode)rep).removeFromParent();
	} catch (Exception e) {}

	if (style == IMAGE)
	    toRet.set("image",image);

	// we have to recover some properties like label
	// to fix defaults
	// should be a better way to do this (maybe push all the values
	// from the owner back down?)
	if (rep.get("label") != null) 
	    toRet.set("label",rep.get("label"));
	
	toRet.set("labelvisible",rep.get("labelvisible"));

	// need to do the same thing to recover size, but that has to come 
	// from the Node
	if ((rep.getStyle() == TEXT_RECTANGLE) || 
	    (rep.getStyle() == TEXT_ROUNDRECTANGLE) || 
	    (rep.getStyle() == TEXT_ELLIPSE)) {
	    if ((style == RECTANGLE) || 
		(style == ROUNDRECTANGLE) ||
		(style == ELLIPSE) ||
		(style == RECTANGLE3D) || 
		(style == ROUNDRECTANGLE3D) ||
		(style == ELLIPSE3D) ||
		(style == IMAGE)) {
		toRet.set("width",rep.getOwner().getAttrDB("width"));
		toRet.set("height",rep.getOwner().getAttrDB("height"));
	    }
	}

	toRet.set("strokecolor",rep.get("strokecolor"));

	return(toRet);
    }
    
    public NodeListener generateNode(Shape sp, Node n) {
	return(new GuessShapeNode(sp,n,curFrame,USER));
    }

    public EdgeListener generateEdge(Edge n) {
	GuessPEdge toRet =  new GuessPEdge(n,curFrame);
	curFrame.getEdges().addChild((PNode)toRet);
	return(toRet);
    }
    
    public EdgeListener generateEdge(Shape aShape, Edge n) {
	return(new GuessPEdge(aShape,n,curFrame));
    }
    
    public EdgeListener generateEdge(double x1, double y1, Color clr,
					    float width, double size,
					    Edge n) {
	GuessPEdge toRet =
	    new GuessPEdge(new Ellipse2D.Double(x1 - size,
						y1 - size,
						size,
						size),n,curFrame);
	toRet.setPaint(clr);
	toRet.setStroke(new BasicStroke(width));
	curFrame.getEdges().addChild((PNode)toRet);
	return(toRet);
    }
    
    public void updateEdge(GuessPEdge toRet,
			   double x1, double y1,
			   double x2, double y2) {
	toRet.setShape(new Line2D.Double(x1,y1,x2,y2));
    }
    
    public void updateEdge(GuessPEdge toRet,
			   Shape aShape) {
	toRet.setShape(aShape);
    }
    
    public void updateEdge(GuessPEdge toRet,
			   double x1, double y1, Color clr,
			   float width, double size) {
	toRet.setShape(new Ellipse2D.Double(x1 - size,
					    y1 - size,
					    size,
					    size));
    }

    public void setDirected(boolean dir) {
	boolean readjust = false;

	if (dir != getDirected())
	    readjust = true;

	super.setDirected(dir);

	if (readjust) {
	    curFrame.setFrozen(true);
	    try {
		for (int i = curFrame.getEdges().getChildrenCount() - 1;
		     i >= 0 ; i--) {
		    EdgeListener gpe = 
			(EdgeListener)curFrame.getEdges().getChild(i);
		    gpe.readjust();
		}
	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	    curFrame.setFrozen(false);
	}
    } 

    java.util.HashSet hulls = new java.util.HashSet();

    public ConvexHull createConvexHull(Collection s, Color c) {
	ConvexHull t = (ConvexHull)(new ConvexHullNode(s,c));
	curFrame.getHulls().addChild((PNode)t);
	hulls.add(t);
	return(t);
    }

    public void removeConvexHull(ConvexHull c) {
	hulls.remove(c);
	((ConvexHullNode)c).destroyHull();
    }

    public Collection getConvexHulls() {
	return(hulls);
    }

    public void shiftClickNode(Node n) {
    }

    public void shiftClickEdge(Edge e) {
    }

    public void clickNode(Node n) {
	if (defnzoom)
	    ((GuessPNode)n.getRep()).centerDisplay();
    }

    public void clickEdge(Edge e) {
	if (defezoom)
	    ((GuessPEdge)e.getRep()).centerDisplay();
    }

    public void mouseEnterNode(Node n) {
	if (defnhighlight)
	    n.getRep().highlight(true);
    }

    public void mouseEnterEdge(Edge e) {
	if (defehighlight)
	    e.getRep().highlight(true);
    }

    public void mouseLeaveNode(Node n) {
	if (defnhighlight)
	    n.getRep().highlight(false);
    }

    public void mouseLeaveEdge(Edge e) {
	if (defehighlight)
	    e.getRep().highlight(false);
    }

    private boolean defnhighlight = true;
    
    private boolean defehighlight = true;

    private boolean defnzoom = true;

    private boolean defezoom = true;

    public void defaultNodeHighlights(boolean state) {
	defnhighlight = state;
    }

    public void defaultEdgeHighlights(boolean state) {
	defehighlight = state;
    }

    public void defaultNodeZooming(boolean state) {
	defnzoom = state;
    }

    public void defaultEdgeZooming(boolean state) {
	defezoom = state;
    }
}
