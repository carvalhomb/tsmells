package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.PLine;
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
public class GradientLegend extends JPanel implements Dockable {

    DumbCanvas myCanvas = new DumbCanvas();

    private double majorTicks = 5;

    private double min = 0;

    private double max = 10;

    private JPopupMenu jp = new JPopupMenu("Options");
   
    private Vector tickMarks = null;

    private Vector textMarks = null;

    private PNode gradNode = null;

    private Color foregroundColor = Color.black;

    private Color startColor = null;

    private Color endColor = null;

    private Font anno = null;

    private Rectangle2D bounds = null;

    private void generateTickMarks() {
	if (tickMarks == null) {
	    tickMarks = new Vector();
	    textMarks = new Vector();
	    for (double val = min ; val <= max ; val += majorTicks) {
		PPath tickMark = new PPath();
		tickMarks.add(tickMark);
		PText txt = new PText(""+val);
		if (anno == null) {
		    anno = txt.getFont();
		    if (this.direction == MainUIWindow.VERTICAL_DOCK) {
			anno = anno.deriveFont((float)Math.min(20,600/majorTicks));
			//System.out.println(anno.getSize());
		    } else {
			anno = anno.deriveFont(20);
		    }
		}
		txt.setFont(anno);
		textMarks.add(txt);
		myCanvas.dLayer.addChild(tickMark);
		myCanvas.dLayer.addChild(txt);

	    }
	}
	
	double maxX = 0;
	double maxY = 0;

	if (direction == MainUIWindow.VERTICAL_DOCK) {
	    double startY = 0;
	    double increment = 600 * majorTicks / (max - min);
	    maxY = 600;
	    for (int i = 0 ; i < tickMarks.size() ; i++) {
		PPath ln = (PPath)tickMarks.elementAt(i);
		PText txt = (PText)textMarks.elementAt(i);
		ln.setStrokePaint(foregroundColor);
		txt.setTextPaint(foregroundColor);
		ln.setPathTo(new Line2D.Double(33,startY,60,startY));
		txt.setX(65);
		txt.setY(startY-anno.getSize()/2);
		startY += increment;
		maxX = Math.max(maxX,txt.getBounds().getX() + 
				txt.getBounds().getWidth());
		maxY = Math.max(maxY,txt.getBounds().getY() + 
				txt.getBounds().getHeight());
	    }
	}

	bounds = new Rectangle2D.Float(-10, 
				       -10, 
				       (float)maxX + 20,
				       (float)maxY + 20);
	recenter();
    }

    private void generateGradNode() {
	if (gradNode == null) {
	    gradNode = new PPath(new Rectangle2D.Double(10,10,100,500));
	    myCanvas.dLayer.addChild(gradNode);
	}

	if (this.direction == MainUIWindow.VERTICAL_DOCK) {
	    gradNode.setBounds(0,-1,30,602);
	} else {
	    gradNode.setBounds(0,0,600,30);
	}

	((PPath)gradNode).setStrokePaint(foregroundColor);
	double x1,x2,y1,y2;
	if (this.direction == MainUIWindow.VERTICAL_DOCK) {
	    x1 = gradNode.getX() + gradNode.getWidth()/2;
	    x2 = x1;
	    y1 = gradNode.getY();
	    y2 = gradNode.getY() + gradNode.getHeight();
	} else {
	    x1 = gradNode.getX();
	    x2 = gradNode.getX() + gradNode.getWidth();
	    y1 = gradNode.getY() + gradNode.getHeight()/2;
	    y2 = y1;
	}
	gradNode.setPaint(new GradientPaint((float)x1,(float)y1,startColor,
					    (float)x2,(float)y2,
					    endColor,false));
    }

    public GradientLegend(Color startC, Color endC, 
			  double min, double max, double ticks) {
	this(startC,endC,min,max,ticks,MainUIWindow.VERTICAL_DOCK);
    }

    public GradientLegend(String startC, String endC, 
			  double min, double max, double ticks) {
	this(Colors.getColor(startC,Color.red),
	     Colors.getColor(endC,Color.blue),
	     min,max,ticks,MainUIWindow.VERTICAL_DOCK);
    }

    public GradientLegend(String startC, String endC, double min,
			  double max, double ticks, int direction) {
	this(Colors.getColor(startC,Color.red),
	     Colors.getColor(endC,Color.blue),
	     min,max,ticks,direction);
    }

    public GradientLegend(Color startC, Color endC, double min,
			  double max, double ticks, int direction) {
	this.direction = direction;
	this.startColor = startC;
	this.endColor = endC;
	this.min = min;
	this.max = max;
	this.majorTicks = ticks;

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
	relayoutNodes();
    }

    public void relayoutNodes() {
	generateTickMarks();
	generateGradNode();
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


    public void setDisplayBackground() {
	setDisplayBackground(getDisplayBackground());
    }
    
    public void setDisplayBackground(Color bg) {
	myCanvas.setBackground(bg);
	double lum = 
	    (299 * bg.getRed() + 587 * bg.getGreen() + 114 * bg.getBlue()) 
	    / 1000;
	if (lum > 127) {
	    foregroundColor = Color.black;
	} else {
	    foregroundColor = Color.white;
	}
	relayoutNodes();
    }

    public void setDisplayBackground(String bg) {
	setDisplayBackground(Colors.getColor(bg,getDisplayBackground()));
    }

    public Color getDisplayBackground() {
	return(myCanvas.getBackground());
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
	return("GradientLegend");
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
