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

import com.hp.hpl.guess.ui.FrameListener;
import com.hp.hpl.guess.ui.Colors;
import com.hp.hpl.guess.ui.StatusBar;
import com.hp.hpl.guess.ui.ExceptionWindow;
import com.hp.hpl.guess.freehep.HEPWriter;

import javax.swing.SwingUtilities;
import javax.swing.JPanel;

import org.python.core.PySequence;
import org.python.core.PyObject;
import org.python.core.PyInstance;

import com.sun.image.codec.jpeg.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;

import java.awt.print.*;
import edu.umd.cs.piccolo.util.PPaintContext;

import org.freehep.graphicsio.ImageGraphics2D;

/**
 * The frame for the Piccolo interface.  This provides the high level
 * interface window, canvas, and camera.  It is currently implemented
 * as a singleton
 * @pyobj v
 */
public class GFrame extends PCanvas implements FrameListener {

    boolean frozen = false;

    JpegImagesToMovie jitm = null;

    Vector frameBuffer = new Vector();

    public int fileCounter = 0;

    private boolean auto = true;

    private boolean movieMode = false;

    public GFrame() {
	super(); 
	//initialize();
	//repaint();
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    //		    System.out.println("run...");
		    initialize();
		    repaint();
		}
	    });
    }


    public void setFrozen(boolean state) {
	//	System.out.println("frozen: " + state);
	this.frozen = state;
	if (state == false) {
	    Iterator it = hulls.getChildrenIterator();
	    while(it.hasNext()) {
		ConvexHullNode chn = (ConvexHullNode)it.next();
		chn.setFrozen(state);
	    }
	    repaint();
	}
    }

    public void paintImmediately() {
	if (frozen) {
	    return;
	}
	
	super.paintImmediately();

    }

    public void paintImmediately(int x, int y, int width, int height) {
	if (frozen) {
	    return;
	}
	
	super.paintImmediately(x,y,width,height);

    }

    public void paintImmediately(Rectangle r) {
	if (frozen) {
	    return;
	}
	
	super.paintImmediately(r);

    }

    /**
     * @pyexport
     */
    public void startMovie(int fps, String filename) {
	startMovie(fps,filename,true);
    }

    /**
     * @pyexport
     */
    public void startMovie(int fps, String filename, boolean auto) {
	if (movieMode = true) {
	    stopMovie();
	}
	this.auto = auto;
	movieMode = true;
	Rectangle b = getBounds();
	jitm = new JpegImagesToMovie((int)b.width,(int)b.height,fps,filename);
	Thread th = new Thread(jitm);
	th.start();
    }


    /**
     * basically dumps the movie to disk
     * @pyexport
     */
    public void stopMovie() {
	if (jitm != null) {
	    System.out.println("Stopping movie...");
	    flushBuffer();
	    jitm.getISS().stopMovie();
	}
	movieMode = false;
	jitm = null;
    }

    /**
     * flushes the movie buffer.  Right now it only gets called
     * when we're done with a move, but maybe the right thing
     * is to do it when the framebuffer gets too big
     */
    public void flushBuffer() {
	for (int i = 0 ; i < frameBuffer.size() ; i++) {
	    byte[] b = (byte[])frameBuffer.elementAt(i);
	    if (jitm != null) {
		jitm.getISS().loadBuffer(b);
	    }
	}
	frameBuffer.clear();
    }

    private synchronized void click() {
	click(false);
    }

    private synchronized void click(boolean pause) {
	try {
	    Rectangle b = getBounds();
	    //setRenderQuality(RENDER_QUALITY_HIGH);
	    BufferedImage image = 
		new BufferedImage((int)b.width,(int)b.height,
				  BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2 = image.createGraphics(); 
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON); 
	    //g.setBackground(Color.black);
	    super.paint(g2);
	    if (pause)
		Thread.sleep(1000);
	    ByteArrayOutputStream fos = 
		new ByteArrayOutputStream();
	    fileCounter++;
	    JPEGImageEncoder encoder = 
		JPEGCodec.createJPEGEncoder(fos);
	    JPEGEncodeParam jpegParams = 
		encoder.getDefaultJPEGEncodeParam(image);
	    jpegParams.setQuality(1.0f, false);
	    encoder.setJPEGEncodeParam(jpegParams);
	    encoder.encode(image);  // Encode image to JPEG and send to browser
	    fos.close();
	    frameBuffer.addElement(fos.toByteArray());
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public void paint(Graphics g) {
	if (frozen) {
	    //Thread.dumpStack();
	    return;
	}

	super.paint(g);

	if (movieMode) {
	    click();
	}
    }

    public static final int BROWSE_MODE = 0;
    public static final int NODE_EDIT_MODE = 1;
    public static final int EDGE_EDIT_MODE = 2;
    public static final int HULL_EDIT_MODE = 3;
    public static final int DRAW_CREATE_MODE = 4;
    public static final int DRAW_EDIT_MODE = 5;

    private int currentMode = BROWSE_MODE;

    public int getMode() {
	return(currentMode);
    }

    public void returnToRegular() {
	unselectAll();
	this.removeInputEventListener(dseh); // remove draw handler
	this.removeInputEventListener(pseh); // remove selection handler

	// let's make sure we don't add things twice 
	// (hack since piccolo doesn't seem to check)
	this.removeInputEventListener(rceh);
	this.removeInputEventListener(mph);

	this.addInputEventListener(rceh); // add the regular handlers
	this.addInputEventListener(mph); // add the regular handlers
	// get rid of the draw window if it's there
	com.hp.hpl.guess.ui.DrawWindow.uncreate();
	currentMode = BROWSE_MODE;
    }

    public void switchHandler(int i) {
	
	returnToRegular(); // reset everything

	if (i == NODE_EDIT_MODE) {
	    this.removeInputEventListener(rceh);
	    this.addInputEventListener(pseh);
	    pseh.switchTo(nodes);
	} else if (i == EDGE_EDIT_MODE) {
	    this.removeInputEventListener(rceh);
	    this.addInputEventListener(pseh);
	    pseh.switchTo(edges,false);
	} else if (i == HULL_EDIT_MODE) {
	    this.removeInputEventListener(rceh);
	    this.addInputEventListener(pseh);
	    pseh.switchTo(hulls,false);
	} else if (i == DRAW_EDIT_MODE) {
	    this.removeInputEventListener(rceh);
	    this.addInputEventListener(pseh);
	    pseh.switchTo(draw);
	    com.hp.hpl.guess.ui.DrawWindow.create();
	} else if (i == DRAW_CREATE_MODE) {
	    this.removeInputEventListener(mph);
	    this.removeInputEventListener(rceh);
	    this.addInputEventListener(dseh);
	    com.hp.hpl.guess.ui.DrawWindow.create();
	} else {
	    return;
	}
	currentMode = i;
    }

    /**
     * the background image layer
     */
    protected PLayer bImage = new GPLayer();

    /**
     * the hull layer
     */
    protected PLayer hulls = new GPLayer();

    /**
     * the node 
     */
    protected PLayer nodes = new GPLayer();

    /**
     * the edge 
     */
    protected PLayer edges = new GPLayer();

    /**
     * the label layer
     */
    protected PLayer labels = new GPLayer();

    /**
     * the extra drawn nodes
     */
    protected PLayer draw = new GPLayer();

    /**
     * extra for other stuff on top (dragging rectangles)
     */
    protected PLayer extra = new GPLayer();

    public PLayer getNodes() {
	return(nodes);
    }

    public PLayer getEdges() {
	return(edges);
    }

    public PLayer getHulls() {
	return(hulls);
    }

    public PLayer getDraw() {
	return(draw);
    }

    /**
     * Returns the camera viewing the canvas in the frame
     * @return the camera for this canvas
     */
    public synchronized PCamera getGCamera() {
	return(this.getCamera());
    }


    RectZoomHandler rceh = null;
    
    MySelectionHandler pseh = null;

    DrawZoomHandler dseh = null;

    MyPanHandler mph = null;

    public void center() {
	center(new Integer(1));
    }

    public void centerFast() {
	center(new Integer(1),100);
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
	    int tcount = 0;
	    if (((Integer)o).intValue() == 1) {
		ListIterator li = 
		    (ListIterator)nodes.getChildrenIterator();
		while (li.hasNext()) {
		    PNode n = (PNode)li.next();
		    if (!n.getVisible()) {
			continue;
		    }
		    tcount++;
		    if (n.getX() < minx) {
			minx = n.getX();
		    }
		    if (n.getY() < miny) {
			miny = n.getY();
		    }
		    if (n.getY() + n.getHeight() > maxy) {
			maxy = n.getY() + ((GuessPNode)n).getDrawHeight();
		    }
		    if (n.getX() + n.getWidth() > maxx) {
			maxx = n.getX() + ((GuessPNode)n).getDrawWidth();
		    }
		}
	    }
	    if (tcount > 0)
		center(minx,miny,maxx,maxy,t);
	    return;
	}

	if (o instanceof Node) {
	    centerOn((GuessPNode)((Node)o).getRep());
	    return;
	} else if (o instanceof Edge) {
	    centerOn((GuessPNode)((Edge)o).getNode1().getRep(),
		     (GuessPNode)((Edge)o).getNode2().getRep());
	    return;
	} 

	if (o instanceof PySequence) {
	    PySequence q = (PySequence)o;
	    int len = q.__len__();
	    for (int i = 0 ; i < len ; i++) {

		// hack, we should really be iterating internaly, but whatever
		if (!(q.__getitem__(i) instanceof PyInstance)) 
		    continue;

		Object elem = 
		    ((PyInstance)q.__getitem__(i)).__tojava__(Object.class);
		if (elem instanceof Node) {
		    GuessPNode n = (GuessPNode)((Node)elem).getRep();
		    if (n.getX() < minx) {
			minx = n.getX();
		    }
		    if (n.getY() < miny) {
			miny = n.getY();
		    }
		    if (n.getY() + n.getHeight() > maxy) {
			maxy = n.getY() + ((GuessPNode)n).getDrawHeight();
		    }
		    if (n.getX() + n.getWidth() > maxx) {
			maxx = n.getX() + ((GuessPNode)n).getDrawWidth();
		    }
		} else if (elem instanceof Edge) {
		    GuessPNode n = 
			(GuessPNode)((Edge)elem).getNode1().getRep();
		   
		    if (n.getX() < minx) {
			minx = n.getX();
		    }
		    if (n.getY() < miny) {
			miny = n.getY();
		    }
		    if (n.getY() + n.getHeight() > maxy) {
			maxy = n.getY() + ((GuessPNode)n).getDrawHeight();
		    }
		    if (n.getX() + n.getWidth() > maxx) {
			maxx = n.getX() + ((GuessPNode)n).getDrawWidth();
		    }
		    
		    n = 
			(GuessPNode)((Edge)elem).getNode2().getRep();

		    if (n.getX() < minx) {
			minx = n.getX();
		    }
		    if (n.getY() < miny) {
			miny = n.getY();
		    }
		    if (n.getY() + n.getHeight() > maxy) {
			maxy = n.getY() + ((GuessPNode)n).getDrawHeight();
		    }
		    if (n.getX() + n.getWidth() > maxx) {
			maxx = n.getX() + ((GuessPNode)n).getDrawWidth();
		    }
		}
	    }
	    center(minx,miny,maxx,maxy,t);
	    return;
	} 

	if (o instanceof Collection) {
	    Iterator it = ((Collection)o).iterator();
	    while(it.hasNext()) {
		Object elem = it.next();
		if (elem instanceof Node) {
		    GuessPNode n = (GuessPNode)((Node)elem).getRep();
		    if (n.getX() < minx) {
			minx = n.getX();
		    }
		    if (n.getY() < miny) {
			miny = n.getY();
		    }
		    if (n.getY() + n.getHeight() > maxy) {
			maxy = n.getY() + ((GuessPNode)n).getDrawHeight();
		    }
		    if (n.getX() + n.getWidth() > maxx) {
			maxx = n.getX() + ((GuessPNode)n).getDrawWidth();
		    }
		} else if (elem instanceof Edge) {
		    GuessPNode n = 
			(GuessPNode)((Edge)elem).getNode1().getRep();
		   
		    if (n.getX() < minx) {
			minx = n.getX();
		    }
		    if (n.getY() < miny) {
			miny = n.getY();
		    }
		    if (n.getY() + n.getHeight() > maxy) {
			maxy = n.getY() + ((GuessPNode)n).getDrawHeight();
		    }
		    if (n.getX() + n.getWidth() > maxx) {
			maxx = n.getX() + ((GuessPNode)n).getDrawWidth();
		    }
		    
		    n = 
			(GuessPNode)((Edge)elem).getNode2().getRep();

		    if (n.getX() < minx) {
			minx = n.getX();
		    }
		    if (n.getY() < miny) {
			miny = n.getY();
		    }
		    if (n.getY() + n.getHeight() > maxy) {
			maxy = n.getY() + ((GuessPNode)n).getDrawHeight();
		    }
		    if (n.getX() + n.getWidth() > maxx) {
			maxx = n.getX() + ((GuessPNode)n).getDrawWidth();
		    }
		}
	    }
	    center(minx,miny,maxx,maxy,t);
	    return;
	}  else {
	    throw(new Error("center() on " + o.getClass().toString() + 
			    " not implemented"));
	}
    }
    
    public void center(double minx, double miny, double maxx, double maxy) {
	center(minx,miny,maxx,maxy,2000);
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
	Rectangle2D r2d = new Rectangle2D.Double(minx,
						 miny,
						 maxx-minx,
						 maxy-miny);
	center(r2d,t);
    }
    
    public void center(Rectangle2D r2, long t) {
	final Rectangle2D r2d = r2;

	final long tm = t;
	
	//System.out.println("1: " + getBounds());

	if (Guess.getSynchronous()) {
	    try {
		//System.out.println("anim 1");
		PTransformActivity pta = 
		    getGCamera().animateViewToCenterBounds(r2d,
							   true,
							   tm);
		
		if (pta == null) {
		    return;
		}
		SimpDelegate sd = new SimpDelegate();
		pta.setDelegate(sd);
		while(sd.running()) {
		    try {
			Thread.sleep(100);
		    } catch (Exception inte) {
			System.out.println(inte);
		    }
		}
	    } catch (Exception ex) {
		ExceptionWindow.getExceptionWindow(ex);
	    }
	} else {
	    javax.swing.SwingUtilities.invokeLater(new Runnable() { 
		    public void run() { 
			//System.out.println("anim 2");
			PTransformActivity pta = 
			    getGCamera().animateViewToCenterBounds(r2d,
								   true,
								   tm);
		    } 
		}); 
	}
    }

    class SimpDelegate implements PActivity.PActivityDelegate {
	
	private boolean runn = false;
	
	public boolean running() {
	    return(runn);
	}
	
	public void activityFinished(PActivity activity) {
	    runn = false;
	}
	
	public void activityStarted(PActivity activity) {
	    runn = true;
	}
            
	public void activityStepped(PActivity activity) {
	    runn = true;
	}
    }

    public void setBackgroundImage(String filename) {
	setBackgroundImage(filename,0,0);
    }

    public void setBackgroundImage(String filename, double x, double y) {
	try {
	    removeBackgroundImage();
	    PImage backgroundNode = new PImage(filename);
	    backgroundNode.setX(x);
	    backgroundNode.setY(y);
	    bImage.addChild(backgroundNode);
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public void removeBackgroundImage() {
	bImage.removeAllChildren();
	//backgroundNode = null;
    }

    /**
     * an init routine... called when the frame is created.
     * currently it only adds the layers to the canvas
     */
    public void initialize() {
	while((hulls == null) || (nodes == null) || 
	    (edges == null) || (labels == null)) {
	    try {
		Thread.sleep(500);
	    } catch (Exception e) {
	    }	       
	}
	this.getLayer().addChild(bImage);
	this.getLayer().addChild(hulls);
	this.getLayer().addChild(edges);
	this.getLayer().addChild(nodes);
	this.getLayer().addChild(labels);
	this.getLayer().addChild(draw);
	this.getLayer().addChild(extra);
	
	rceh = new RectZoomHandler(extra,getGCamera()); 
	this.addInputEventListener(rceh);
	
	pseh = new MySelectionHandler(extra,nodes);

	dseh = new DrawZoomHandler(draw,getGCamera());
	
	KeyBoardManager kbm = new KeyBoardManager(this);
	this.getRoot().getDefaultInputManager().setKeyboardFocus(kbm);

	this.removeInputEventListener(this.getPanEventHandler());
	mph = new MyPanHandler(this);
	this.addInputEventListener(mph);
	this.removeInputEventListener(this.getZoomEventHandler());
	this.addInputEventListener(new MyZoomHandler(this));
	//this.setZoomEventHandler(new OverloadedZoom());
	CursorFactory.init(this);

	PHandle.DEFAULT_HANDLE_SHAPE = new Ellipse2D.Float(0f, 0f, 2,2);

	//System.out.println("initialized...");
    }

    /**
     * returns a buffered image for the full image
     * scaled to 1
     */
    public BufferedImage getFullImage() {
	return(getFullImage(null,1));
    }

    public BufferedImage getFullImage(double scale) {
	return(getFullImage(null,scale));
    }

    public BufferedImage getFullImage(Rectangle2D fis) {
	return(getFullImage(fis,1));
    }

    /**
     * returns a buffered image scaled to fit some coordinates
     */
    public BufferedImage getFullImage(double maxWidth, double maxHeight) {
	return(getFullImage(null,maxWidth,maxHeight));
    }

    public BufferedImage getFullImage(Rectangle2D fis,
				      double maxWidth, 
				      double maxHeight) {
	if (fis == null)
	    fis = getFullImageSize();

	double scaleX = maxWidth/fis.getWidth();
	double scaleY = maxHeight/fis.getHeight();
	return(getFullImage(fis,Math.min(scaleX,scaleY)));
    }
    
    public Dimension getFullImageDimensions() {
	return(getFullImageDimensions(1));
    }

    public Dimension getFullImageDimensions(double scale) {
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;
	double maxW = Double.MIN_VALUE;
	double maxH = Double.MIN_VALUE;
	for (int i = 0 ; i < getGCamera().getLayerCount() ; i++) {
	    PLayer l = getGCamera().getLayer(i);
	    PBounds pb = l.getFullBounds();
	    if (pb.getX() < minX) 
		minX = pb.getX();
	    if (pb.getY() < minY)
		minY = pb.getY();
	    if (pb.getWidth() > maxW)
		maxW = pb.getWidth();
	    if (pb.getHeight() > maxH)
		maxH = pb.getHeight();
	}
	maxW = maxW * scale;
	maxH = maxH * scale;
	return(new Dimension((int)maxW,(int)maxH));
    }

    public Rectangle2D getFullImageSize() {
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;
	double maxW = Double.MIN_VALUE;
	double maxH = Double.MIN_VALUE;
	for (int i = 0 ; i < getGCamera().getLayerCount() ; i++) {
	    PLayer l = getGCamera().getLayer(i);
	    PBounds pb = l.getFullBounds();
	    if (pb.getX() < minX) 
		minX = pb.getX();
	    if (pb.getY() < minY)
		minY = pb.getY();
	    if (pb.getWidth() > maxW)
		maxW = pb.getWidth();
	    if (pb.getHeight() > maxH)
		maxH = pb.getHeight();
	}

	return(new Rectangle2D.Double(minX,minY,maxW,maxH));
    }

    public BufferedImage getFullImage(Rectangle2D trans, double scale) {
	//System.out.println("scale: " + scale);
	if (trans == null) {
	    trans = getFullImageSize();
	}

	double minX = trans.getX() * scale;
	double minY = trans.getY() * scale;
	double maxW = trans.getWidth() * scale;
	double maxH = trans.getHeight() * scale;
	
	// find the size of the image to create
	// and the offsets

	if ((maxW <= Double.MIN_VALUE) || 
	    (maxH <= Double.MIN_VALUE)) {
	    return(null);
	}

	BufferedImage img = new BufferedImage((int)maxW,
					      (int)maxH,
					      BufferedImage.TYPE_INT_BGR);
	
	
	Graphics2D g = img.createGraphics();
	Paint temp = g.getPaint();
	g.setPaint(getDisplayBackground());
	g.fill(new Rectangle(0,0,(int)maxW,(int)maxH));
	g.setPaint(temp);
	for (int i = 0 ; i < getGCamera().getLayerCount() ; i++) {
	    PLayer l = getGCamera().getLayer(i);
	    PBounds pb = l.getFullBounds();
	    Image timage = null;
	    if (scale == 1) { 
		timage = l.toImage();
	    } else {
		timage = l.toImage((int)(pb.getWidth()*scale),
				   (int)(pb.getHeight()*scale),
				   null);
	    }
	    int moveX = (int)(pb.getX() * scale) - (int)minX;
	    int moveY = (int)(pb.getY() * scale) - (int)minY;
	    g.drawImage(timage,moveX,moveY,this);
	}
	return(img);
    }

    public Point2D getLastClickedPosition() {
	return(mph.getLastClickedPosition());
    }

    private static PText pt = new PText();

    public Collection getSelected() {
	return(pseh.getSelection());
    }

    public void unselectAll() {
	pseh.unselectAll();
    }

    public void overlayText(String s) {
	//System.out.println("foo");
	pt.setText(s);
	pt.setTextPaint(Color.yellow);
	pt.setPaint(Color.black);
	pt.setX(0);
	pt.setY(0);
	getGCamera().addChild(pt);
	try {
	    repaint();
	    Thread.sleep(1000);
	} catch (Exception e) {
	}
	getGCamera().removeChild(pt);
    }

    /**
     * sets the background color
     * @param bg the background color
     */
    public void setDisplayBackground(java.awt.Color bg) {
	this.setBackground(bg);
    }

    public void setDisplayBackground(String bg) {
	this.setBackground(Colors.getColor(bg,getDisplayBackground()));
    }

    /**
     * gets the display background color
     * @return the background color
     */
    public java.awt.Color getDisplayBackground() {
	return(this.getBackground());
    }

    private static boolean testMode = true;


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

    public static int ZOOM_SIZE = 40;

    public void centerOn(GuessPNode gpn) {
	float centerX = (float)gpn.getX() + (float)gpn.getWidth()/2;
	float centerY = (float)gpn.getY() + (float)gpn.getHeight()/2;
	float zs = 
	    (float)Math.max(gpn.getWidth(),
			    Math.max(gpn.getHeight(),
				     ZOOM_SIZE));
	if (zs > ZOOM_SIZE)
	    zs += 10;
	
	final Rectangle2D r2d = new Rectangle2D.Float(centerX - zs/2, 
						      centerY - zs/2, 
						      zs,
						      zs);
	
	//frame.getGCamera().addChild(labelText);
	
	//System.out.println("2: " + getBounds());

	javax.swing.SwingUtilities.invokeLater(new Runnable() { 
		public void run() { 
		    //System.out.println("anim 3");
		    PTransformActivity pta = 
			getGCamera().animateViewToCenterBounds(r2d,
							       true,
							       2000);
		} 
	    });
    }

    public void centerOn(GuessPNode n1, GuessPNode n2) {

	if (n1 == n2) {
	    centerOn(n1);
	    return;
	}

	float minX = (float)Math.min(n1.getX(),n2.getX());
	float maxX = (float)Math.max(n1.getX()+n1.getWidth(),
				     n2.getX()+n2.getWidth());
	float minY = (float)Math.min(n1.getY(),n2.getY());
	float maxY = (float)Math.max(n1.getY()+n1.getHeight(),
				     n2.getY()+n2.getHeight());

	float zs = (float)Math.max(maxY-minY,maxX-minX);

	if (zs > ZOOM_SIZE) {
	    zs += 10;
	    minX -= 10;
	    minY -= 10;
	}
	
	final Rectangle2D r2d = new Rectangle2D.Float(minX, 
						      minY, 
						      zs,
						      zs);
	
	//frame.getGCamera().addChild(labelText);
	
	//	System.out.println("3: " + getBounds());

	javax.swing.SwingUtilities.invokeLater(new Runnable() { 
		public void run() { 
		    //System.out.println("anim 4");
		    PTransformActivity pta = 
			getGCamera().animateViewToCenterBounds(r2d,
							       true,
							       2000);
		} 
	    });
    }

    private boolean fs = false;
    
    public void saveState(String filename) {
	try {
	    FileOutputStream fos = new FileOutputStream(filename);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject("_BACKGROUND_DISPLAY_COLOR");
	    oos.writeObject(getDisplayBackground());
	    oos.writeObject("_DRAW_LAYER");
	    dseh.saveState(oos);
	    oos.close();
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public void loadState(String filename) {
	try {
	    FileInputStream fis = new FileInputStream(filename);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    String tag = (String)ois.readObject();
	    if (tag.equals("_BACKGROUND_DISPLAY_COLOR")) {
		setDisplayBackground((Color)ois.readObject());
	    } 
	    tag = (String)ois.readObject();
	    if (tag.equals("_DRAW_LAYER")) {
		dseh.loadState(ois);
	    } 
	    ois.close();
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public void writeFullImage(String name, int itype,
			       Properties props) {
	writeFullImage(name,itype,null,1,props);
    }

    public void writeFullImage(String name, int itype, double scale,
			       Properties props) {
	writeFullImage(name,itype,null,scale,props);
    }

    public void writeFullImage(String name, int itype, Rectangle2D fis,
			       Properties props) {
	writeFullImage(name,itype,fis,1,props);
    }

    /**
     * returns a buffered image scaled to fit some coordinates
     */
    public void writeFullImage(String name, int itype,
			       double maxWidth, 
			       double maxHeight,
			       Properties props) {
	writeFullImage(name,itype,null,maxWidth,maxHeight,props);
    }

    public void writeFullImage(String name,
			       int itype,
			       Rectangle2D fis,
			       double maxWidth, 
			       double maxHeight,
			       Properties props) {
	if (fis == null)
	    fis = getFullImageSize();

	double scaleX = maxWidth/fis.getWidth();
	double scaleY = maxHeight/fis.getHeight();
	writeFullImage(name,itype,fis,Math.min(scaleX,scaleY),props);
    }
    
    public void writeFullImage(String name, int itype,
			       Rectangle2D trans, double scale,
			       Properties props) {
	//System.out.println("scale: " + scale);
	if (trans == null) {
	    trans = getFullImageSize();
	}

	double minX = trans.getX() * scale;
	double minY = trans.getY() * scale;
	double maxW = trans.getWidth() * scale;
	double maxH = trans.getHeight() * scale;
	
	// find the size of the image to create
	// and the offsets

	if ((maxW <= Double.MIN_VALUE) || 
	    (maxH <= Double.MIN_VALUE)) {
	    return;
	}

	PageFormat pf = new PageFormat();
	Paper pg = new Paper();
	pg.setSize(maxW,maxH); // some padding
	pg.setImageableArea(0,0,maxW,maxH);
	pf.setPaper(pg);
	Graphics2D g = HEPWriter.getGraphics2D(name,itype,(int)maxW,(int)maxH);

	//PPaintContext ppc = new PPaintContext(g);
	//ppc.setRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

	if (g instanceof org.freehep.graphics2d.VectorGraphics) {
	    ((org.freehep.graphics2d.VectorGraphics)g).startExport();
	}
	Paint temp = g.getPaint();
	g.setPaint(getDisplayBackground());
	g.fill(new Rectangle(0,0,(int)maxW,(int)maxH));
	g.setPaint(temp);
	for (int i = 0 ; i < getGCamera().getLayerCount() ; i++) {
	    PLayer l = getGCamera().getLayer(i);
	    l.print(g,pf,0);
	    //if (l instanceof GPLayer) {
	    //System.out.println("painting layer...");
	    //((GPLayer)l).paintBackChannel(ppc);
	    //}
	}
	if (g instanceof org.freehep.graphics2d.VectorGraphics) {
	    ((org.freehep.graphics2d.VectorGraphics)g).endExport();
	}
    }

}


