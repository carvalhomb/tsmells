package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.P3DRect;
import com.hp.hpl.guess.*;
import java.awt.geom.*;
import java.awt.BasicStroke;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.event.MySelectionHandler;
import edu.umd.cs.piccolox.handles.PHandle;
import edu.umd.cs.piccolo.activities.*;
import java.util.*;

import com.hp.hpl.guess.ui.*;

import javax.swing.*;

import org.python.core.PySequence;
import org.python.core.PyObject;
import org.python.core.PyInstance;

import com.sun.image.codec.jpeg.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;

import com.hp.hpl.guess.freehep.*;

/**
 * The frame for the Piccolo interface.  This provides the high level
 * interface window, canvas, and camera.  It is currently implemented
 * as a singleton
 */
public class Legend extends JPanel implements Dockable {

    DumbCanvas myCanvas = new DumbCanvas();

    private Vector nodes = new Vector();
    private HashMap annotations = new HashMap();
    private HashMap a2n = new HashMap();

    private double maxWidth = 14;
    private double maxHeight = 14;
    private Font font = null;
    private Color fontColor = Color.black;
    private Rectangle2D bounds = null;

    private JPopupMenu jp = new JPopupMenu("Options");
   
    public Legend() {
	this(MainUIWindow.VERTICAL_DOCK);
    }

    public Legend(int direction) {
	this.direction = direction;

	Guess.getMainUIWindow().dock(this);
	//myCanvas = new DumbCanvas();
	//myCanvas.setBounds(getDefaultFrameBounds());

	setLayout(new GridBagLayout());

	GridBagConstraints c = new GridBagConstraints();

	c.fill = GridBagConstraints.BOTH;
	c.weighty = 1;
	c.weightx = 1;
	c.gridx = 0;
	c.gridy = 0;
	add(myCanvas,c);

	addComponentListener( new ComponentAdapter()
	    {
		public void componentResized(ComponentEvent e)
		{
		    if (e.getComponent().getWidth() > 0) {
			recenter();
		    }
		}
	    });
	//pack();
	JMenuItem jmi = jp.add("Export Image");
	
	ActionListener printListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("Export Image")) {
			HEPDialog hd = new HEPDialog(null);
			hd.showHEPDialog(null,"Export File",
					 myCanvas,
					 "output.jpg",false);
		    }
		}
	    };

	jmi.addActionListener(printListener);

	MouseAdapter ma = new MouseAdapter(){
		public void mousePressed(MouseEvent e) {
		    if ((e.getButton() == MouseEvent.BUTTON2) ||
			(e.getButton() == MouseEvent.BUTTON3)) {
			//System.out.println(tabbedPane.getSelectedIndex());
			//System.out.println("popup");
			jp.show(e.getComponent(),
				e.getX(),
				e.getY());
		    }
		}
	    };
	myCanvas.addMouseListener(ma);
    }

    public void relayoutNodes() {
	int nnodes = nodes.size();
	double cellHeight = maxHeight + 8;
	double cellWidth = maxWidth + 8;
	int cellLoc = 0;
	Font newFont = null;
	if (font != null) {
	    newFont = font.deriveFont((float)maxHeight);
	}
	double maxX = 0;
	double maxY = 0;

	for (int i = 0 ; i < nodes.size() ; i++) {
	    PNode n = (PNode)nodes.elementAt(i);

	    if (n instanceof GuessLegendEdge) {
		((GuessLegendEdge)n).setCoords(4,
					       i*cellHeight+4,
					       cellWidth - 4,
					       i*cellHeight + cellHeight - 4);
	    } else {
		double nwidth = n.getWidth();
		double nheight = n.getHeight();
		double hoffset = cellHeight * i + (cellHeight/2 - nheight/2);
		double woffset = cellWidth/2 - nwidth/2;
		n.setX(woffset);
		n.setY(hoffset);
	    }
	    PText anno = (PText)annotations.get(n);
	    if (anno != null) {
		double hoffset = cellHeight * i + 4;
		double woffset = maxWidth + 20;
		anno.setFont(newFont);
		anno.setX(woffset);
		anno.setY(hoffset);
		anno.setTextPaint(fontColor);
		if ((anno.getX() + anno.getWidth()) > maxX) {
		    maxX = anno.getX() + anno.getWidth();
		}
		if ((anno.getY() + anno.getHeight()) > maxY) {
		    maxY = anno.getY() + anno.getHeight();
		}
	    }
	    if ((n.getX() + n.getWidth()) > maxX) {
		maxX = n.getX() + n.getWidth();
	    }
	    if ((n.getY() + n.getHeight()) > maxY) {
		maxY = n.getY() + n.getHeight();
	    }
	}

	bounds = new Rectangle2D.Float(-10, 
				       -10, 
				       (float)maxX + 20,
				       (float)maxY + 20);
	recenter();
    }

    public Rectangle2D getB() {
	return(bounds);
    }

    public void recenter() {

	if (bounds == null) 
	    return;

	//frame.getGCamera().addChild(labelText);
	
	javax.swing.SwingUtilities.invokeLater(new Runnable() { 
		public void run() { 
		    PTransformActivity pta = 
			myCanvas.getCamera().animateViewToCenterBounds(getB(),
								       true,
								       100);
		} 
	    });
    }

    public void remove(String s) {
	remove(s,(PNode)a2n.get(s));
	relayoutNodes();	
    }

    private void remove(String s, PNode pn) {
	if (pn != null) {
	    nodes.remove(pn);
	    pn.removeFromParent();
	    PNode p2 = (PNode)annotations.get(pn);
	    annotations.remove(pn);
	    if (p2 != null) {
		p2.removeFromParent();
	    }
	    a2n.remove(s);
	}
    }

    public void removeAll() {
	ArrayList al = new ArrayList(a2n.keySet());
	Iterator it = al.iterator();
	while(it.hasNext()) {
	    remove((String)it.next());
	}
	relayoutNodes();
    }

    public void setDisplayBackground(java.awt.Color bg) {
	myCanvas.setBackground(bg);
	double lum = 
	    (299 * bg.getRed() + 587 * bg.getGreen() + 114 * bg.getBlue()) 
	    / 1000;
	if (lum > 127) {
	    fontColor = Color.black;
	} else {
	    fontColor = Color.white;
	}
	relayoutNodes();
    }

    public void setDisplayBackground(String bg) {
	setDisplayBackground(Colors.getColor(bg,getDisplayBackground()));
    }

    public Color getDisplayBackground() {
	return(myCanvas.getBackground());
    }

    public void add(Node n, String annotation) {
	if (a2n.containsKey(annotation)) {
	    throw(new Error(annotation + " already exists in legend"));
	}

	PNode pn = (PNode)n.getRep();
	PNode toRet = null;
	
	if (pn instanceof GuessTextNode) {
	    toRet = new GuessTextLegendNode(n);
	} else if (pn instanceof GuessShapeNode3D) {
	    toRet = new GuessShape3DLegendNode(n);
	} else if (pn instanceof GuessShapeNode) {
	    toRet = new GuessShapeLegendNode(n);
	} else if (pn instanceof GuessImageNode) {
	    toRet = new GuessImageLegendNode(n);
	} 

	//	System.out.println(n + " " + annotation + " " + toRet);

	if (toRet != null) {
	    if (toRet.getWidth() > maxWidth) {
		maxWidth = toRet.getWidth();
	    }
	    
	    if (toRet.getHeight() > maxHeight) {
		maxHeight = toRet.getHeight();
	    }
	    myCanvas.dLayer.addChild(toRet);
	    nodes.add(toRet);
	    PText anno = new PText(annotation);
	    anno.setX(toRet.getWidth() + 10);
	    anno.setY(toRet.getY());
	    annotations.put(toRet,anno);
	    myCanvas.dLayer.addChild(anno);
	    font = anno.getFont();
	    a2n.put(annotation,toRet);
	}
	relayoutNodes();
    }


    public void add(Edge e, String annotation) {
	if (a2n.containsKey(annotation)) {
	    throw(new Error(annotation + " already exists in legend"));
	}
	PNode toRet = (PNode)(new GuessLegendEdge(e));
	myCanvas.dLayer.addChild(toRet);
	nodes.add(toRet);
	PText anno = new PText(annotation);
	anno.setX(toRet.getWidth() + 10);
	anno.setY(toRet.getY());
	annotations.put(toRet,anno);
	myCanvas.dLayer.addChild(anno);
	font = anno.getFont();
	a2n.put(annotation,toRet);
	relayoutNodes();
    }

    public void add(ConvexHullNode c, String annotation) {
	if (a2n.containsKey(annotation)) {
	    throw(new Error(annotation + " already exists in legend"));
	}
	PNode toRet = (PNode)(new ConvexHullLegendNode(c));
	myCanvas.dLayer.addChild(toRet);
	nodes.add(toRet);
	PText anno = new PText(annotation);
	anno.setX(toRet.getWidth() + 10);
	anno.setY(toRet.getY());
	annotations.put(toRet,anno);
	myCanvas.dLayer.addChild(anno);
	font = anno.getFont();
	a2n.put(annotation,toRet);
	relayoutNodes();
    }

    public Rectangle getDefaultFrameBounds() {
	return new Rectangle(100, 100, 200, 600);
    }		

    class DumbCanvas extends PCanvas {
	
	/**
	 * the edge layer (drawn first)
	 */
	public PLayer dLayer = new PLayer();

	public DumbCanvas() {
	    super();
	    if (dLayer == null) {
		try {
		    Thread.sleep(500);
		} catch (Exception e) {
		}	       
	    }
	    this.getLayer().addChild(dLayer);
	    this.removeInputEventListener(this.getPanEventHandler());
	    this.removeInputEventListener(this.getZoomEventHandler());
	}
    }

    private int direction = MainUIWindow.VERTICAL_DOCK;
    public int getDirectionPreference() {
	return(direction);
    }

    public Dimension getPreferredSize() {
	if (direction == MainUIWindow.VERTICAL_DOCK) 
	    return(new Dimension(200,600));
	else 
	    return(new Dimension(600,200));
    }

    public String toString() {
	return("Legend [Items: "+nodes.size()+"]");
    }

    public void opening(boolean state) {
    }

    public void attaching(boolean state) {
    }

    public String getTitle() {
	return("Legend");
    }

    private GuessJFrame myParent = null;

    public GuessJFrame getWindow() {
	return(myParent);
    }

    public void setWindow(GuessJFrame gjf) {
	myParent = gjf;
    }
}
