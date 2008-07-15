package com.hp.hpl.guess.prefuse;

import com.hp.hpl.guess.ui.FrameListener;
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
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.hp.hpl.guess.ui.DockableAdapter;
import com.hp.hpl.guess.ui.MainUIWindow;

public class ForcePanel extends DockableAdapter {
    
    public int getDirectionPreference() {
	return(MainUIWindow.VERTICAL_DOCK);
    }
    
    PrefuseDisplay pd = null;

    private final JButton run = new JButton("Start/Stop");

    public ForcePanel(PrefuseDisplay pd) {
	this.pd = pd;

	final GraphDistanceFilter filter = pd.filter;
	final Visualization m_vis = pd.m_vis;
	final PrefuseDisplay finalDisp = pd;

        JForcePanel jp = new JForcePanel(pd.fsim);
        
	Box cf = new Box(BoxLayout.X_AXIS);
        
	cf.add(run);
        cf.setBorder(BorderFactory.createTitledBorder(""));
	cf.add(Box.createHorizontalGlue());
        jp.add(cf);

	ActionListener startstop = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    finalDisp.toggleForce();
		}
	    };
	    
	run.addActionListener(startstop);

        //add(opanel);
      
	add(jp);
        add(Box.createVerticalGlue());
    }

    public String getTitle() {
	return("Force Controls");
    }    
}
