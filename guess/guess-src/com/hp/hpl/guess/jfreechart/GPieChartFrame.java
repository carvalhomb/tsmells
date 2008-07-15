package com.hp.hpl.guess.jfreechart;

import org.jfree.chart.plot.PiePlot;
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
import org.jfree.chart.entity.CategoryItemEntity;


import javax.swing.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.awt.Dimension;
import org.python.core.*;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.*;

import java.awt.*;
import java.awt.geom.*;
import org.jfree.ui.Drawable;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

public class GPieChartFrame extends JPanel 
    implements Dockable, GraphMouseListener {

    protected HashMap idToGroup = new HashMap();

    protected HashMap elemToSection = new HashMap();

    private boolean docking = true;

    private boolean legend = false;

    public GPieChartFrame(String title, PySequence groups, 
			  PySequence vals, PySequence groupNames,
			  PySequence colors) {
	this(title,groups,vals,groupNames,colors,true,false);
    }

    public GPieChartFrame(String title, PySequence groups, 
			  PySequence vals, PySequence groupNames,
			  PySequence colors, boolean docking, 
			  boolean legend) {
	
	this.docking = docking;
	this.legend = legend;

	//CategoryTableXYDataset dset = new CategoryTableXYDataset();
	DefaultPieDataset dset = new DefaultPieDataset();

	Color[] clrs = null;
	if (colors != null) {
	    clrs = new Color[groups.size()];
	}

	for (int i = 0 ; i < groups.size() ; i++) {
	    Object group = groups.__finditem__(i);

	    Collection grp = ((PySequence)group).findGraphElements();

	    if (docking) {
		Iterator it = grp.iterator();
		while(it.hasNext()) {
		    GraphElement ge = (GraphElement)it.next();
		    elemToSection.put(ge,new Integer(i));
		}
		
		idToGroup.put(new Integer(i),grp);
	    }

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
	    String name = groupNames.__finditem__(i).toString();
	    dset.setValue(name,val);
	    if (colors != null) {
		String clr = colors.__finditem__(i).toString();
		clrs[i] = Colors.getColor(clr,Color.blue);
	    }
	}
	buildChart(title,dset,clrs);
    }

    String title = null;

    ChartPanel cp = null;

    PiePlot plot = null;

    private JFreeChart chart = null;
    
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
			   DefaultPieDataset dataset,
			   Color[] clr) {

	setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weighty = 1;
	c.weightx = 1;
	c.gridy = 1;
	c.gridx = 1;

	this.title = title;

	chart = 
	    ChartFactory.createPieChart(title,
					dataset,
					legend,
					true,
					false);
	
	plot = (PiePlot)chart.getPlot();
	if (clr != null) {
	    for (int i = 0 ; i < clr.length ; i++) {
		plot.setSectionPaint(i,clr[i]);
	    }
	    //plot.setExplodePercent(1,.3);
	}

	cp = new ChartPanel(chart);
	if (docking)
	    cp.setPreferredSize(new Dimension(600,250));
	cp.addChartMouseListener(new InternalMouseListener(this));
	add(cp,c);
	if (docking) {
	    Guess.getMainUIWindow().dock(this);
	    GraphEvents.getGraphEvents().addGraphMouseListener(this);
	}
    }

    public int getDirectionPreference() {
	return(MainUIWindow.HORIZONTAL_DOCK);
    }

    public void opening(boolean state) {
	//if (state) 
	//  cp.setSize(new Dimension(getWidth(),getHeight()-40));
    }

    public void attaching(boolean state) {
	//	cp.setPreferredSize(new Dimension(getWidth(),getHeight()));
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


	private GPieChartFrame owner = null;

	private Collection prev = null;

	private JPopupMenu jpm = null;

	public InternalMouseListener(GPieChartFrame own) {
	    this.owner = own;
	}

	public void chartMouseClicked(ChartMouseEvent event) {
	    MouseEvent ev = event.getTrigger();
	    if (ev.getButton() != MouseEvent.BUTTON1)
		return;

	    ChartEntity ce = event.getEntity();

	    if (ce == null)
		return;

	    if (ce instanceof PieSectionEntity) {
		int ser = ((PieSectionEntity)ce).getSectionIndex();
		prev = (Collection)owner.idToGroup.get(new Integer(ser));
		if (prev != null) {
		    VisFactory.getFactory().getDisplay().center(prev);
		}
	    }
	}

	public void chartMouseMoved(ChartMouseEvent event) {
	    if (jpm == null)
		jpm = owner.cp.getPopupMenu();

	    ChartEntity ce = event.getEntity();

	    if (prev != null) {
		Iterator it = prev.iterator();
		while (it.hasNext()) {
		    GraphElement ge = (GraphElement)it.next();
		    GraphEvents.mouseLeave(ge);
		}
		prev = null;
		owner.cp.setPopupMenu(jpm);
	    }
	    
	    if (ce == null) {
		return;
	    }


	    if (ce instanceof PieSectionEntity) {
		int ser = ((PieSectionEntity)ce).getSectionIndex();
		prev = (Collection)owner.idToGroup.get(new Integer(ser));
		if (prev != null) {
		    Iterator it = prev.iterator();
		    boolean nodes = false;
		    boolean edges = false;
		    while (it.hasNext()) {
			GraphElement ge = (GraphElement)it.next();
			if (ge instanceof Node)
			    nodes = true;
			else if (ge instanceof Edge)
			    edges = true;
			GraphEvents.mouseEnter(ge);
		    }
		    EditorPopup ep = null;
		    if (nodes && edges) {
			ep = GraphElementEditorPopup.getPopup();
		    } else if (nodes) {
			ep = NodeEditorPopup.getPopup();
		    } else if (edges) {
			ep = EdgeEditorPopup.getPopup();
		    }
		    if (ep != null) {
			ep.cacheContent(prev,prev);
			owner.cp.setPopupMenu(ep);
		    }
		}
	    }
	}
    }

    private int exploded = -1;
    
    public synchronized void mouseEnterNode(Node n) {
	annotate(n);
    }
    
    public synchronized void mouseEnterEdge(Edge e) {
	annotate(e);
    }
    
    public synchronized void mouseLeaveNode(Node n) {
	if (elemToSection.containsKey(n)) {
	    if (exploded != -1)
		plot.setExplodePercent(exploded,0);
	    exploded = -1;
	}
    }
	
    public synchronized void mouseLeaveEdge(Edge e) {
	if (elemToSection.containsKey(e)) {
	    if (exploded != -1)
		plot.setExplodePercent(exploded,0);
	    exploded = -1;
	}
    }
    
    public synchronized void annotate(GraphElement ge) {
	Integer i = (Integer)elemToSection.get(ge);
	if (i != null) {
	    if (exploded != -1)
		plot.setExplodePercent(exploded,0);

	    exploded = i.intValue();
	    plot.setExplodePercent(exploded,.3);
	}
    }
}
