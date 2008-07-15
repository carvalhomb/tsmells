package com.hp.hpl.guess.prefuse;

import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.*;
import java.awt.Color;
import prefuse.data.Table;
import prefuse.data.Schema;
import prefuse.visual.VisualItem;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import prefuse.Visualization;

public class PrefuseFactory extends VisFactory {

    private PrefuseDisplay curFrame = null;
    
    protected prefuse.data.Graph m_graph = null;
    protected static Visualization m_vis = null;

    public PrefuseFactory() {
	init();
    }

    private PrefuseDisplay init() {
	m_graph = new prefuse.data.Graph(false);
	prefuse.data.Node n1 = m_graph.addNode();
	m_graph.getNodeTable().addColumn("label",String.class);

	m_graph.getNodeTable().addColumn(VisualItem.X,double.class);
	m_graph.getNodeTable().addColumn(VisualItem.Y,double.class);

	m_graph.getNodeTable().addColumn(VisualItem.STROKECOLOR,int.class);
	m_graph.getEdgeTable().addColumn(VisualItem.FILLCOLOR,int.class);
	m_graph.getEdgeTable().addColumn(VisualItem.STROKECOLOR,int.class);
	curFrame = new PrefuseDisplay(m_graph);
	try {
	    curFrame.preRun();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	m_graph.removeNode(n1);
	m_vis = curFrame.m_vis;
	return(curFrame);
    }

    public FrameListener getDisplay() {
	return(curFrame);
    }

    private ForcePanel fp = null;

    public synchronized void runNow() {
	curFrame.runNow();
	fp = new ForcePanel(curFrame);
	JMenu pMenu = new JMenu("Prefuse");
	JMenuItem fpan = new JMenuItem("Force controls...");
	ActionListener displayListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("Force controls...")) {
			Guess.getMainUIWindow().undock(fp);
		    }
		}
	    };
	fpan.addActionListener(displayListener);
	pMenu.add(fpan);
	Guess.getMainUIWindow().getGMenuBar().add(pMenu);
	Guess.getMainUIWindow().undock(fp);
    }

    public NodeListener generateNode(int type, double x, double y,
				     double width, double height,
				     Color clr, Node n) {
	NodeListener node = generateNode(n);
	node.set("x",new Double(x));
	node.set("y",new Double(y));
	return(node);
    }

    public NodeListener generateNode(Node n) {
	//m_actionList.setEnabled(false);	
	PrefuseNode pn = new PrefuseNode(m_graph);
	pn.set("label",n.getName());
	//System.out.println(n.getName());
	//pn.setAttribute("label",n.getName());
	return(pn);
    }

    public void remove(Node n)
    {
	//curFrame.g.removeNode((PrefuseNode)n.getRep());
    }
    
    //removes the given edge rep from the collection of edges to draw
    public void remove(Edge e)
    {
	//curFrame.g.removeEdge((PrefuseEdge)e.getRep());
    }

    public void add(Node n) {
    }

    public void add(Edge e) {
    }

    public EdgeListener generateEdge(Edge n) {
	//m_actionList.setEnabled(false);	
	PrefuseEdge pe = new PrefuseEdge(m_graph,
					 (PrefuseNode)n.getNode1().getRep(),
					 (PrefuseNode)n.getNode2().getRep());
	//pe.setAttribute("WEIGHT",n.__getattr__("weight").toString());
	//m_actionList.setEnabled(true);	
	return(pe);
    }
        
}
