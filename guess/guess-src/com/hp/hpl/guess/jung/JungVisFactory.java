package com.hp.hpl.guess.jung;

import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.*;
import java.awt.*;
import java.awt.geom.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.graph.decorators.*;

public class JungVisFactory extends VisFactory {

    private JungVisFrame curFrame = null;

    private SparseGraph g = new SparseGraph();

    protected VisualizationViewer vv = null;
    
    public JungVisFactory() {
	init();
    }

    public void runNow() {
	if (curFrame != null)
	    vv.restart();
    }

    private JungVisNode lastCreated = null;

    public JungVisFrame init() {
	Layout l = new PassThroughLayout(g);
	PluggableRenderer pr = new PluggableRenderer();
        pr.setVertexPaintFunction(new VPaintFunction());
        pr.setVertexShapeFunction(new EllipseVertexShapeFunction());
        
        pr.setEdgePaintFunction(new EPaintFunction());
        pr.setEdgeShapeFunction(new EdgeShape.Line());
	pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction((float)1.0));

	vv = new VisualizationViewer(l,pr);
	curFrame = new JungVisFrame(vv);
	return(curFrame);
    }

    public FrameListener getDisplay() {
	return(curFrame);
    }

    public NodeListener generateNode(int type, double x, double y,
				     double width, double height,
				     Color clr, Node n) {
	JungVisNode nl = new JungVisNode();
	g.addVertex(nl);
	nl.set("x",new Double(x));
	nl.set("y",new Double(y));
	nl.set("width",new Double(width));
	nl.set("height",new Double(height));
	nl.set("color",clr);
	nl.set("label",n.getName());
	nl.set("labelvisible",new Boolean(false));
	nl.set("style",new Integer(type));
	nl.set("fixed",new Boolean(false));
	return(nl);
    }

    public NodeListener generateNode(Node n) {
	return(generateNode(0,0,0,1,1,Color.blue,n));
    }

    public void remove(Node n)
    {
	//g.deleteNode((JungVisNode)n.getRep());
    }
    
    //removes the given edge rep from the collection of edges to draw
    public void remove(Edge e)
    {
	//g.deleteEdge((JungVisEdge)e.getRep());
    }

    public void add(Node n) {
    }

    public void add(Edge e) {
    }

    public EdgeListener generateEdge(Edge n) {
	JungVisEdge pe = null;
	if (n instanceof DirectedEdge) {
	    pe = 
		new JungVisDirectedEdge((JungVisNode)n.getNode1().getRep(),
					(JungVisNode)n.getNode2().getRep());
	} else {
	    pe = 
		new JungVisUndirectedEdge((JungVisNode)n.getNode1().getRep(),
					  (JungVisNode)n.getNode2().getRep());
	}
	pe.set("width",new Double(1));
	pe.set("color",Color.black);
	pe.set("label","");
	pe.set("labelvisible",new Boolean(false));
	g.addEdge(pe);
	return(pe);
    }

    class PassThroughLayout extends AbstractLayout {

	public PassThroughLayout(edu.uci.ics.jung.graph.Graph g) {
	    super(g);
	}
	    
	public void advancePositions() {
	}

	public Point2D getLocation(JungVisNode v) {
	    return(new Point2D.Double(((Double)(v.get("x"))).doubleValue(),
				      ((Double)(v.get("y"))).doubleValue()));
	}
	
	public double getX(JungVisNode v) {
	    return(((Double)(v.get("x"))).doubleValue());
	}
	
	public double getY(JungVisNode v) {
	    return(((Double)(v.get("x"))).doubleValue());
	}
	
	public Coordinates getCoordinates(JungVisNode v) {
	    return(new Coordinates(((Double)(v.get("x"))).doubleValue(),
				   ((Double)(v.get("y"))).doubleValue()));
	}
	
	public boolean incrementsAreDone() {
	    return(true);
	}
	
	public void initialize_local_vertex(edu.uci.ics.jung.graph.Vertex v) {
	}
	
	public void initialize_local() {
	}

	public boolean isIncremental() {
	    return(false);
	}
    }

    class VPaintFunction implements VertexPaintFunction {

	public VPaintFunction() {
	}

	public Paint getDrawPaint(edu.uci.ics.jung.graph.Vertex v) {
	    return(Color.black);
	}
	
	public Paint getFillPaint(edu.uci.ics.jung.graph.Vertex v) {
	    return((Paint)((JungVisNode)v).get("color"));
	}
    }
    
    class EPaintFunction implements EdgePaintFunction {

	public EPaintFunction() {
	}

	public Paint getDrawPaint(edu.uci.ics.jung.graph.Edge v) {
	    return((Paint)((JungVisEdge)v).get("color"));
	}
	
	public Paint getFillPaint(edu.uci.ics.jung.graph.Edge v) {
	    return((Paint)((JungVisEdge)v).get("color"));
	}
    }

    class VShapeFunction implements VertexShapeFunction {

	public VShapeFunction() {
	}

	public Shape getShape(edu.uci.ics.jung.graph.Vertex vs) {
	    JungVisNode v = (JungVisNode)vs;
	    double x = ((Double)v.get("x")).doubleValue();
	    double y = ((Double)v.get("y")).doubleValue();
	    double width = ((Double)v.get("width")).doubleValue();
	    double height = ((Double)v.get("height")).doubleValue();
	    Shape toRet = new Ellipse2D.Double(x,y,width,height);
	    return(toRet);
	}
    }
}
