package com.hp.hpl.guess.jfreechart;

import org.jfree.chart.*;
import org.jfree.data.general.*;
import org.jfree.data.category.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.entity.*;
import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;

import javax.swing.*;
import java.util.HashMap;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import org.python.core.*;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.*;

import java.awt.*;
import java.awt.geom.*;
import org.jfree.ui.Drawable;
import org.jfree.chart.ChartUtilities;
import java.util.HashSet;
import java.awt.event.MouseEvent;

public class GChartFrame extends JPanel implements Dockable {

    class SeriesPackage extends XYSeries implements GraphMouseListener {

	protected HashMap idToElem = new HashMap();
	protected HashMap elemToId = new HashMap();
	protected XYPlot plot = null;
	private	HashMap annotations = new HashMap();
	private boolean docking = true;

	public SeriesPackage(String title, XYPlot plot, boolean docking) {
	    super(title);
	    this.plot = plot;
	    this.docking = docking;
	}

	public void add(int x, double y, GraphElement name) {
	    super.add((double)x,(double)y);
	    if (docking) {
		idToElem.put(new Integer(x),name);
		elemToId.put(name,new Integer(x));
	    }
	}

	public void mouseEnterNode(Node n) {
	    annotate(n);
	}
	
	public void mouseEnterEdge(Edge e) {
	    annotate(e);
	    
	}
	
	public void mouseLeaveNode(Node n) {
	    if (annotations.containsKey(n)) {
		plot.removeAnnotation((XYAnnotation)annotations.get(n));
		annotations.remove(n);
	    }
	}
	
	public void mouseLeaveEdge(Edge e) {
	    if (annotations.containsKey(e)) {
		plot.removeAnnotation((XYAnnotation)annotations.get(e));
		annotations.remove(e);
	    }
	}
	
	public void annotate(GraphElement name) {
	    if (annotations.containsKey(name)) {
		return;
	    }
	    
	    int x = 0;
	    double y = 0;
	    if (elemToId.containsKey(name)) {
		x = ((Integer)elemToId.get(name)).intValue();
	    }
	    
	    y = ((Number)getY(x)).doubleValue();
	    
	    XYDrawableAnnotation anno =  
		new XYDrawableAnnotation((double)x,y, 11, 11, cd);
	    annotations.put(name,anno);
	    plot.addAnnotation(anno);
	}

    }

    protected String title = null;
    protected HashMap seriesMap = new HashMap();
    protected XYPlot plot = null;
    
    public void setTitle(String title) {
	if (this.title == null) {
	    this.title = title;
	} else {
	    this.title = this.title + " / " + title;
	}
    }

    private int seriesCounter = 0;

    public void addToChart(String title, GraphElement[] names, 
			   double[] vals, String xlabel) {
	SeriesPackage series1 = new SeriesPackage(title,plot,docking);

	for (int i = 0 ; i < names.length ; i++) {
	    series1.add(i,vals[i],names[i]);
	}

	augmentChart(title,series1);
    }

    private boolean docking = true;

    private boolean legend = true;

    public GChartFrame(String title, PySequence names, 
		       PySequence vals, String xlabel) {
	this(title,names,vals,xlabel,true,true);
    }

    public GChartFrame(String title, PySequence names, 
		       PySequence vals, String xlabel, 
		       boolean docking, boolean legend) {

	this.docking = docking;
	this.legend = legend;
	
	buildChart(title,xlabel);
	addToChart(title,names,vals,xlabel);
    }

    public void addToChart(String title, PySequence names,
			   PySequence vals, String xlabel) {
	SeriesPackage series1 = new SeriesPackage(title,plot,docking);

	for (int i = 0 ; i < names.size() ; i++) {
	    Object pyval = (Object)vals.__finditem__(i);
	    double val = 0;
	    if (pyval instanceof PyInteger) {
		val = (double)(((PyInteger)pyval).getValue());
	    } else if (pyval instanceof PyFloat) {
		val = (double)(((PyFloat)pyval).getValue());
	    } else if (pyval instanceof PyInstance) {
		Double tval = 
		    (Double)((PyInstance)vals.__finditem__(i)).__tojava__(Double.class);
		val = tval.doubleValue();
	    }
	    GraphElement name = 
		((GraphElement)((PyInstance)names.__finditem__(i)).__tojava__(GraphElement.class));
	    //System.out.println(name + " " + name.getClass());
	    series1.add(i,val,name);
	}
	augmentChart(title,series1);
    }


    public void augmentChart(String title, SeriesPackage pac) {
	setTitle(title);
	dataset.addSeries(pac);
	if (docking)
	    GraphEvents.getGraphEvents().addGraphMouseListener(pac);
	seriesMap.put(new Integer(seriesCounter),pac);
	seriesCounter++;
    }

    private XYSeriesCollection dataset = new XYSeriesCollection();

    private JFreeChart chart = null;
    
    private ChartPanel cp = null;

    public void saveJPEG(String filename) throws IOException {
	File f = new File(filename);
	saveChartAsJPEG(new File(filename),(float).9,
			(int)cp.getPreferredSize().getWidth(),
			(int)cp.getPreferredSize().getHeight());
    }

    public void saveJPEG(String filename, float quality) 
	throws IOException {
	saveChartAsJPEG(new File(filename),quality,
			(int)cp.getPreferredSize().getWidth(),
			(int)cp.getPreferredSize().getHeight());
    }

    public void saveJPEG(String filename, float quality, 
			 int width, int height) throws IOException {
	saveChartAsJPEG(new File(filename),quality,width,height);
    }

    private void saveChartAsJPEG(File fname, float quality, 
				 int width, int height) throws IOException {
	ChartUtilities.saveChartAsJPEG(fname,quality,chart,width,height);
    }

    public void buildChart(String title, 
			   String xlabel) {

	setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weighty = 1;
	c.weightx = 1;
	c.gridy = 1;
	c.gridx = 1;

	dataset = new XYSeriesCollection();

	chart = 
	    ChartFactory.createXYLineChart(title,
					   xlabel,
					   title,
					   dataset,
					   PlotOrientation.VERTICAL,
					   legend,
					   true,
					   false);
	
	//CategoryItemRenderer xir = 
	//   ((CategoryPlot)chart.getPlot()).getRenderer();
	//xir.setToolTipGenerator(new InternalToolTip());
	plot = (XYPlot)chart.getPlot();
	XYLineAndShapeRenderer renderer = 
	    (XYLineAndShapeRenderer)plot.getRenderer();
	renderer.setShapesVisible(true);
	renderer.setShapesFilled(true);

	cp = new ChartPanel(chart);
	if (docking)
	    cp.addChartMouseListener(new InternalMouseListener(this));
	cp.setPreferredSize(new Dimension(600,250));
	add(cp,c);
	if (docking)
	    Guess.getMainUIWindow().dock(this);
    }	

    private static CircleDrawer cd = 
	new CircleDrawer(Color.blue, new BasicStroke(1.0f), null);


    public int getDirectionPreference() {
	return(MainUIWindow.HORIZONTAL_DOCK);
    }

    public void opening(boolean state) {
    }

    public void attaching(boolean state) {
    }

    private GuessJFrame myParent = null;

    public GuessJFrame getWindow() {
	return(myParent);
    }

    public void setWindow(GuessJFrame gjf) {
	myParent = gjf;
    }

    public String getTitle() {
	if (title == null) 
	    return("Charts");

	return(title);
    }

    //public Dimension getPreferredSize() {
    //return(new Dimension(600,300));
    //}
  
    class InternalMouseListener implements ChartMouseListener {


	private GChartFrame owner = null;

	private GraphElement prev = null;

	private JPopupMenu jpm = null;

	public InternalMouseListener(GChartFrame own) {
	    this.owner = own;
	}

	public void chartMouseClicked(ChartMouseEvent event) {
	    MouseEvent ev = event.getTrigger();
	    if (ev.getButton() != MouseEvent.BUTTON1)
		return;

	    ChartEntity ce = event.getEntity();

	    if (ce == null)
		return;

	    if (ce instanceof XYItemEntity) {
		int ser = ((XYItemEntity)ce).getSeriesIndex();
		SeriesPackage sp = 
		   (SeriesPackage) owner.seriesMap.get(new Integer(ser));

		if (sp != null) {
		    int item = ((XYItemEntity)ce).getItem();
		    Object cat = sp.idToElem.get(new Integer(item));
		    if (cat != null)
			VisFactory.getFactory().getDisplay().center(cat);
		}
	    }
	}

	public void chartMouseMoved(ChartMouseEvent event) {
	    if (jpm == null)
		jpm = owner.cp.getPopupMenu();

	    ChartEntity ce = event.getEntity();

	    if (prev != null) {
		GraphEvents.mouseLeave(prev);
		prev = null;
	    }
	    
	    if (ce == null) {
		return;
	    }


	    if (ce instanceof XYItemEntity) {
		int ser = ((XYItemEntity)ce).getSeriesIndex();
		SeriesPackage sp = 
		   (SeriesPackage) owner.seriesMap.get(new Integer(ser));

		if (sp != null) {
		    int item = ((XYItemEntity)ce).getItem();
		    Object cat = sp.idToElem.get(new Integer(item));
		    boolean nodes = false;
		    boolean edges = false;
		    if (cat instanceof Node)
			nodes = true;
		    else if (cat instanceof Edge)
			edges = true;
		    if (cat instanceof GraphElement) {
			prev = (GraphElement)cat;
			GraphEvents.mouseEnter(prev);
		    }
		    EditorPopup ep = null;
		    if (nodes && edges) {
			ep = GraphElementEditorPopup.getPopup();
		    } else if (nodes) {
			ep = NodeEditorPopup.getPopup();
		    } else if (edges) {
			ep = EdgeEditorPopup.getPopup();
		    }
		    HashSet hs = new HashSet();
		    hs.add(prev);
		    if (ep != null) {
			ep.cacheContent(hs,hs);
			owner.cp.setPopupMenu(ep);
		    }       
		}
	    }
	}
    }
    
    static class CircleDrawer implements Drawable {
	
	/** The outline paint. */
	private Paint outlinePaint;
	
	/** The outline stroke. */
	private Stroke outlineStroke;
	
	/** The fill paint. */
	private Paint fillPaint;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param outlinePaint  the outline paint.
	 * @param outlineStroke  the outline stroke.
	 * @param fillPaint  the fill paint.
	 */
	public CircleDrawer(Paint outlinePaint, 
			    Stroke outlineStroke, 
			    Paint fillPaint) {
	    this.outlinePaint = outlinePaint;
	    this.outlineStroke = outlineStroke;
	    this.fillPaint = fillPaint;
	}
	
	/**
	 * Draws the circle.
	 * 
	 * @param g2  the graphics device.
	 * @param area  the area in which to draw.
	 */
	public void draw(Graphics2D g2, Rectangle2D area) {
	    Ellipse2D ellipse = 
		new Ellipse2D.Double(area.getX(), area.getY(),
				     area.getWidth(), area.getHeight());
	    if (this.fillPaint != null) {
		g2.setPaint(this.fillPaint);
		g2.fill(ellipse);
	    }
	    if (this.outlinePaint != null && this.outlineStroke != null) {
		g2.setPaint(this.outlinePaint);
		g2.setStroke(this.outlineStroke);
		g2.draw(ellipse);
	    }
	    
	    g2.setPaint(Color.black);
	    g2.setStroke(new BasicStroke(1.0f));
	    Line2D line1 = 
		new Line2D.Double(area.getCenterX(), area.getMinY(),
				  area.getCenterX(), area.getMaxY());
	    Line2D line2 = 
		new Line2D.Double(area.getMinX(), area.getCenterY(),
				  area.getMaxX(), area.getCenterY());
	    g2.draw(line1);
	    g2.draw(line2);
	}
    }
}
