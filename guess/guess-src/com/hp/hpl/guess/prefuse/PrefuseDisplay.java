package com.hp.hpl.guess.prefuse;

import com.hp.hpl.guess.ui.FrameListener;
import com.hp.hpl.guess.ui.Colors;
import com.hp.hpl.guess.freehep.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.force.ForceSimulator;
import prefuse.util.io.IOLib;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import java.util.Iterator;
import java.util.Collection;

import org.python.core.PySequence;
import org.python.core.PyObject;
import org.python.core.PyInstance;

public class PrefuseDisplay extends Display implements FrameListener {

    private static final String graph = "graph";
    private static final String nodes = "graph.nodes";
    private static final String edges = "graph.edges";
      
    protected Visualization m_vis = null;;
    protected ForceSimulator fsim = null;
    protected GraphDistanceFilter filter = null;

    private Graph m_graph = null;
    protected int hops = 30;


    public PrefuseDisplay(Graph m_graph) {
        // create a new, empty visualization for our data
        m_vis = new Visualization();
	this.m_graph = m_graph;
        setVisualization(m_vis);
        setSize(700,700);
        pan(350, 350);
        setForeground(Color.GRAY);
        setBackground(Color.WHITE);
        
        // main display controls
        addControlListener(new FocusControl(1));
        addControlListener(new DragControl());
        addControlListener(new PanControl());
        addControlListener(new ZoomControl());
        addControlListener(new WheelZoomControl());
        addControlListener(new ZoomToFitControl());
        addControlListener(new NeighborHighlightControl());

        setForeground(Color.GRAY);
        setBackground(Color.WHITE);

    }

    ForceDirectedLayout fdl = null;

    public void toggleForce() {
	fdl.setEnabled(!fdl.isEnabled());
    }

    public void preRun() {
        LabelRenderer tr = new LabelRenderer();
        tr.setRoundedCorner(8, 8);
        m_vis.setRendererFactory(new DefaultRendererFactory(tr));

        // --------------------------------------------------------------------
        // register the data with a visualization
 

        // adds graph to visualization and sets renderer label field
        setGraph(m_graph, "label");

	ActionList draw = new ActionList();

	draw.add(new ColorAction(nodes, 
				 VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)));


	ActionList animate = new ActionList(Activity.INFINITY);
	fdl = new ForceDirectedLayout(graph);
	fdl.setEnabled(false);
        animate.add(fdl);

        animate.add(new RepaintAction());
        
        // finally, we register our ActionList with the Visualization.
        // we can later execute our Actions by invoking a method on our
        // Visualization, using the name we've chosen below.
        m_vis.putAction("draw", draw);
        m_vis.putAction("layout", animate);

        m_vis.runAfter("draw", "layout");

        fsim = ((ForceDirectedLayout)animate.get(0)).getForceSimulator();



	VisualItem f = (VisualItem)vg.getNode(0);
	m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
	f.setFixed(false);

    }


    public void runNow() {	
	System.err.println("running...");
	m_vis.run("draw");
    }
    
    VisualGraph vg = null;

    public void setGraph(Graph g, String label) {
        // update labeling
        DefaultRendererFactory drf = (DefaultRendererFactory)
	    m_vis.getRendererFactory();
        ((LabelRenderer)drf.getDefaultRenderer()).setTextField(label);
        
        // update graph
        m_vis.removeGroup(graph);
        vg = m_vis.addGraph(graph, g);
        m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);

    }
    
    public void center() {
	Rectangle2D bounds = m_vis.getBounds(Visualization.ALL_ITEMS);
	GraphicsLib.expand(bounds, 50 + (int)(1/this.getScale()));
	DisplayLib.fitViewToBounds(this, bounds, 2000);
    }

    public void center(Object o) {
	center(o,2000);
    }

    public void center(Object o, long t) {

	if (o == null) {
	    return;
	}

	double minx = Double.MAX_VALUE;
	double miny = Double.MAX_VALUE;
	double maxx = Double.MIN_VALUE;
	double maxy = Double.MIN_VALUE;

	if (o instanceof Integer) {
	    center();
	    return;
	}

	if (o instanceof com.hp.hpl.guess.Node) {

	    Rectangle2D bounds = 
		((PrefuseNode)((com.hp.hpl.guess.Node)o).getRep()).getBounds();

	    GraphicsLib.expand(bounds, 50 + (int)(1/this.getScale()));
	    DisplayLib.fitViewToBounds(this, bounds, 2000);

	    return;
	} else if (o instanceof com.hp.hpl.guess.Edge) {
	    Rectangle2D bounds1 = 
		((PrefuseNode)((com.hp.hpl.guess.Edge)o).getNode1().getRep()).getBounds();
	    Rectangle2D bounds2 = 
		((PrefuseNode)((com.hp.hpl.guess.Edge)o).getNode2().getRep()).getBounds();
	    
	    Rectangle2D bounds = bounds1.createIntersection(bounds2);

	    GraphicsLib.expand(bounds, 50 + (int)(1/this.getScale()));
	    DisplayLib.fitViewToBounds(this, bounds, 2000);

	    return;
	} 
	
	if (o instanceof PySequence) {
	    PySequence q = (PySequence)o;
	    int len = q.__len__();
	    Rectangle2D bounds = null;
	    for (int i = 0 ; i < len ; i++) {

		// hack, we should really be iterating internaly, but whatever
		if (!(q.__getitem__(i) instanceof PyInstance)) 
		    continue;

		Object elem = 
		    ((PyInstance)q.__getitem__(i)).__tojava__(Object.class);

		if (elem instanceof com.hp.hpl.guess.Node) {
		    Rectangle2D bounds1 = 
			((PrefuseNode)((com.hp.hpl.guess.Node)elem).getRep()).getBounds();
		    if (bounds == null) {
			bounds = bounds1;
		    } else {
			bounds = bounds.createIntersection(bounds1);
		    }

		} else if (elem instanceof com.hp.hpl.guess.Edge) {
		    Rectangle2D bounds1 = 
			((PrefuseNode)((com.hp.hpl.guess.Edge)elem).getNode1().getRep()).getBounds();
		    Rectangle2D bounds2 = 
			((PrefuseNode)((com.hp.hpl.guess.Edge)elem).getNode2().getRep()).getBounds();
		    if (bounds == null) {
			bounds = bounds1;
		    } else {
			bounds = bounds.createIntersection(bounds1);
		    } 
		    bounds = bounds.createIntersection(bounds2);
		}
	    }
	    if (bounds != null) {
		GraphicsLib.expand(bounds, 50 + (int)(1/this.getScale()));
		DisplayLib.fitViewToBounds(this, bounds, 2000);
	    }
	} else if (o instanceof Collection) {
	    Iterator it = ((Collection)o).iterator();
	    Rectangle2D bounds = null;
	    while(it.hasNext()) {
		Object elem = it.next();
		if (elem instanceof com.hp.hpl.guess.Node) {
		    Rectangle2D bounds1 = 
			((PrefuseNode)((com.hp.hpl.guess.Node)elem).getRep()).getBounds();
		    if (bounds == null) {
			bounds = bounds1;
		    } else {
			bounds = bounds.createIntersection(bounds1);
		    }

		} else if (elem instanceof com.hp.hpl.guess.Edge) {
		    Rectangle2D bounds1 = 
			((PrefuseNode)((com.hp.hpl.guess.Edge)elem).getNode1().getRep()).getBounds();
		    Rectangle2D bounds2 = 
			((PrefuseNode)((com.hp.hpl.guess.Edge)elem).getNode2().getRep()).getBounds();
		    if (bounds == null) {
			bounds = bounds1;
		    } else {
			bounds = bounds.createIntersection(bounds1);
		    } 
		    bounds = bounds.createIntersection(bounds2);
		}
	    }
	    if (bounds != null) {
		GraphicsLib.expand(bounds, 50 + (int)(1/this.getScale()));
		DisplayLib.fitViewToBounds(this, bounds, 2000);
	    }
	} else {
	    throw(new Error("center() on " + o.getClass().toString() + 
			    " not implemented"));
	}
    }

    public void center(double minx, double miny, double maxx, 
		       double maxy, long t) {
	
	if ((minx == Double.MAX_VALUE) ||
	    (maxx == Double.MIN_VALUE) ||
	    (miny == Double.MAX_VALUE) ||
	    (maxy == Double.MIN_VALUE)) {
	    return;
	}

	minx -= 10;
	miny -= 10;
	maxx += 10;
	maxy += 10;
	
	//frame.getGCamera().addChild(labelText);
	Rectangle2D bounds = new Rectangle2D.Double(minx,
						    miny,
						    maxx-minx,
						    maxy-miny);
	DisplayLib.fitViewToBounds(this, bounds, t);
    }

    public void setFrozen(boolean state){}

    public void exportGIF(String filename) {
	HEPWriter.export(filename,this,HEPWriter.GIF);
    }

    public void exportJPG(String filename){
	HEPWriter.export(filename,this,HEPWriter.JPG);
    }

    public void exportPDF(String filename){
	HEPWriter.export(filename,this,HEPWriter.PDF);
    }

    public void exportPS(String filename){
	HEPWriter.export(filename,this,HEPWriter.PS);
    }

    public void exportEPS(String filename){
	HEPWriter.export(filename,this,HEPWriter.EPS);
    }

    public void exportSVG(String filename){
	HEPWriter.export(filename,this,HEPWriter.SVG);
    }

    public void exportSWF(String filename){
	HEPWriter.export(filename,this,HEPWriter.SWF);
    }

    public void exportJAVA(String filename){
	HEPWriter.export(filename,this,HEPWriter.JAVA);
    }

    public void exportCGM(String filename){
	HEPWriter.export(filename,this,HEPWriter.CGM);
    }

    public void exportEMF(String filename){
	HEPWriter.export(filename,this,HEPWriter.EMF);
    }

    public void exportPNG(String filename){
	HEPWriter.export(filename,this,HEPWriter.PNG);
    }

    public Color getDisplayBackground() {
	return(null);
    }
    
    public void setDisplayBackground(java.awt.Color bg) {
	this.setBackground(bg);
    }

    public void setDisplayBackground(String bg) {
	this.setBackground(Colors.getColor(bg,getDisplayBackground()));
    }

    public void setBackgroundImage(String filename) {
    }

    public void setBackgroundImage(String filename, double x, double y) {
    }

    public void removeBackgroundImage() {
    }

    public BufferedImage getFullImage() {
	return(null);
    }
}
