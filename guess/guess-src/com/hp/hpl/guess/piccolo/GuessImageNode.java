package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import edu.umd.cs.piccolo.activities.*;

import com.hp.hpl.guess.Guess;
import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.piccolo.GFrame;

public class GuessImageNode extends PImage implements GuessPNode {
    
    private Node owner = null;
    
    private GFrame frame = null;

    private int style = VisFactory.RECTANGLE;

    private String image = "";

    public Node getOwner() {
	return(owner);
    }

    private static Hashtable imageCache = new Hashtable();
    
    public void set(String field, Object o) {
	/*try {*/
	if (field.equals("style")) {
	    setStyle(((Integer)o).intValue());
	} else if (field.equals("x")) {
	    _xCache = ((Double)o).doubleValue();
	    setLocation(((Double)o).doubleValue(),getY());
	} else if (field.equals("y")) {
	    _yCache = ((Double)o).doubleValue();
	    setLocation(getX(),((Double)o).doubleValue());
	} else if (field.equals("width")) {
	    _widthCache = ((Double)o).doubleValue();
	    setLocation(getX(), getY(),((Double)o).doubleValue(), 
			getHeight());
	} else if (field.equals("height")) {
	    _heightCache = ((Double)o).doubleValue();
	    setLocation(getX(), getY(),getWidth(),
			((Double)o).doubleValue()); 
	} else if (field.equals("label")) {
			setLabel((String)o);
	} else if (field.equals("labelvisible")) {
	    setLabelVisible(((Boolean)o).booleanValue());
	} else if (field.equals("visible")) {
	    setVisible(((Boolean)o).booleanValue());
	} else if (field.equals("image")) {
	    if (o instanceof Image) {
		setImage((Image)o);
	    } else {
		setImage((String)o);
	    }
	}
	if (Guess.getMTF()) 
	    moveToFront();
    
	/*} catch (Exception e) {
	    throw new Error("Problem with setting rep attribute: " + 
			    e.toString());
	}*/
    }

    public void setImage(Image img) {
	if (img == null) 
	    return;

	super.setImage(img);
	if (_xCache == -1) {
	    _xCache = getX();
	}
	if (_yCache == -1) {
	    _yCache = getY();
	}
	if (_widthCache == -1) {
	    _widthCache = getWidth();
	}
	if (_heightCache == -1) {
	    _heightCache = getHeight();
	}
	setLocation(_xCache,_yCache,_widthCache,_heightCache);
    }

    public void setImage(String filename) {
	if (filename == null) 
	    return;
	

	//System.out.println("setting image...");
	this.image = filename;
	if (imageCache.containsKey(filename)) {
	    //	    System.out.println("getting from cache...");
	    super.setImage((java.awt.Image)imageCache.get(filename));
	} else {
	    try {
		super.setImage(filename);
		imageCache.put(filename,getImage());
	    } catch (Exception ex) {
		ExceptionWindow.getExceptionWindow(ex);
		StatusBar.setErrorStatus("Error loading image: " + filename);
	    }
	}
	//System.out.println(_xCache + " " + _yCache);
	//System.out.println(_widthCache + " " + _heightCache);
	if (_xCache == -1) {
	    _xCache = getX();
	}
	if (_yCache == -1) {
	    _yCache = getY();
	}
	if (_widthCache == -1) {
	    _widthCache = getWidth();
	}
	if (_heightCache == -1) {
	    _heightCache = getHeight();
	}
	setLocation(_xCache,_yCache,_widthCache,_heightCache);
    }

    public Object get(String field) {
	try {
	    if (field.equals("style")) {
		return new Integer(style);
	    } else if (field.equals("x")) {
		return(new Double(getX()));
	    } else if (field.equals("y")) {
		return(new Double(getY()));
	    } else if (field.equals("width")) {
		return(new Double(getWidth()));
	    } else if (field.equals("height")) {
		return(new Double(getHeight()));
	    } else if (field.equals("label")) {
		return(label);
	    } else if (field.equals("labelvisible")) {
		return(new Boolean(labelMode));
	    } else if (field.equals("visible")) {
		return(new Boolean(getVisible()));
	    } else if (field.equals("image")) {
		return(image);
	    } else {
		return(null);
	    }
	} catch (Exception e) {
	    throw new Error("Problem with getting rep attribute: " + field + 
			    " " + e.toString());
	}
    }

    public int getStyle() {
	return(style);
    }

    public void setStyle(int s) {
	if (s != style) {
	    NodeListener rep = 
		((PFactory)VisFactory.getFactory()).convertNode(this,s,owner);
	    owner.setRep(rep);
	    owner.readjustEdges();
	}
    }

    public GuessImageNode(Node owner, GFrame frame, int style,
			  double x, double y,
			  double width, double height) {
	super();
	this.owner = owner;
	this.frame = frame;
	this.style = style;
	this.label = owner.getName();
	_xCache = x;
	_yCache = y;
	_widthCache = width;
	_heightCache = height;
    }

    private double _xCache = -1;
    private double _yCache = -1;
    private double _widthCache = -1;
    private double _heightCache = -1;

    public boolean setBounds(java.awt.geom.Rectangle2D newBounds) {
	// notify Node that we're resizing
	boolean toRet = super.setBounds(newBounds);
	owner.readjustEdges();
	return(toRet);
    }
    
    Color curcolor = null;

    public void setColor(Color c)
    {
    }

    public void setShape(int type) {
    }

    public void setShape(Shape shp, int type) {
    }

    private String label = "";
    private String[] multiLineLabel = null;

    public void setLabel(String str) {
	//System.out.println("label: " + label);
	this.label = str;
	if (label != null) {
	    if (label.indexOf("\n") >= 0) {
		multiLineLabel = breakupLines(str);
	    } else {
		multiLineLabel = null;
	    }
	}
    }

    public void setLocation(double x1, double y1, 
			    double width, double height) {

	setBounds(x1,
		  y1,
		  width,
		  height);
	owner.readjustEdges();
	notifyHullListeners();
    }

    public void setLocation(double x1, double y1) {
	
	double w = super.getWidth();
	double h = super.getHeight();

	//System.out.println("f: " + x1 + " " + y1);

	setBounds(x1,
		  y1,
		  w,
		  h);

	owner.readjustEdges();
	notifyHullListeners();
    }


    public void setSize(double width, double height) {
	
	double x = super.getX();
	double y = super.getY();

	setBounds(x,
		  y,
		  width,
		  height);
    }


    public void mouseEntered(PInputEvent aEvent) {
	if (getVisible()) {
	    GraphEvents.mouseEnter(owner);
	}
    }

    public void mouseExited(PInputEvent aEvent) {
	if (getVisible()) {
	    GraphEvents.mouseLeave(owner);
	}
    }
    
    private LabelText labelText = null;

    public void highlight(boolean state) {
	if (!getVisible()) 
	    return;

	if (labelText == null) {
	    labelText = new LabelText(this);
	}

	if (state) {
	    super.setPaint(Color.yellow);
	    labelText.setText(label);
	    labelText.setPaint(new Color(100,100,100,210));
	    labelText.setTextPaint(Color.yellow);
	    float scaling = (float)(1/frame.getGCamera().getViewScale());
	    labelText.setX(getX() + getWidth()+1*scaling);
	    labelText.setY(getY() + getHeight());
	    frame.labels.addChild(labelText);
	    if ((label != null) && (label.equals(owner.getName()))) {
		StatusBar.setStatus(label);
	    } else if (label == null) {
		StatusBar.setStatus(owner.getName());
	    } else {
		StatusBar.setStatus(owner.getName() + " (" + label + ")");
	    }
	} else {
	    super.setPaint(curcolor);
	    labelText.removeFromParent();
	    StatusBar.setStatus("");
	}
	highlightMode = state;
    }

    private boolean highlightMode = false;
    private boolean labelMode = false;

    public void setLabelVisible(boolean state) {
	labelMode = state;
	if (state) {
	    if ((label == null) || (label.equals(""))) {
		label = owner.getName();
	    }
	}
    }
    
    private static BasicStroke linestroke = new BasicStroke((float).3);
    
    public static String[] breakupLines(String text) { 
	String[] toRet = null;
	StringTokenizer st = new StringTokenizer(text,"\n");
	Vector v = new Vector();
	while (st.hasMoreTokens()) {
	    v.addElement(st.nextToken());
	}
	toRet = new String[v.size()];
	for (int i = 0 ; i < v.size() ; i++) {
	    toRet[i] = (String)v.elementAt(i);
	}
	return(toRet);
    }

    public void paintLabel(Graphics2D g, 
			   float labelX, 
			   float labelY,
			   Font font) { 

	if (multiLineLabel == null) {
	    g.drawString(label,(float)labelX,(float)labelY);
	    return;
	}
	
	FontMetrics fontMetrics = 
	    Toolkit.getDefaultToolkit().getFontMetrics(font); 
	
	int fontHeight = fontMetrics.getHeight(); 
	int fontAscent = fontMetrics.getAscent(); 
	
	int num_lines = multiLineLabel.length; 
	float height; 
	int i; 
	
	for (i=0, height=labelY; i<num_lines; i++, height+=fontHeight) { 
	    g.drawString(multiLineLabel[i], labelX, height); 
	} 
    }

    public void paint(PPaintContext apc) {
	Graphics2D g2 = apc.getGraphics();
	if (labelMode) {
	    float scaling = (float)(1/frame.getGCamera().getViewScale());
	    Font f = g2.getFont();
	    f = f.deriveFont((float)(12*scaling));
	    g2.setFont(f);
	    g2.setPaint(curcolor);
	    paintLabel(g2,(float)(getX() + getWidth()+2),
		       (float)(getY() + getHeight()),f);
	}
	g2.setStroke(linestroke);
	super.paint(apc);
    }

    public double getDrawWidth() {
	return(getWidth());
    }

    public double getDrawHeight() {
	return(getHeight());
    }

    public static int ZOOM_SIZE = 40;


    public void moveDone(double x, double y) {
	owner.endMove(x,y);
	notifyHullListeners();
    }
    
    public void inTransition(double x, double y) {
	owner.move(x,y);
	hideHullListeners();
    }

    public void keyTyped(PInputEvent aEvent) {
	System.out.println(""+aEvent.getKeyChar());
    }

    public void mouseClicked(PInputEvent aEvent) {
	//Thread.dumpStack();
	if (aEvent.isLeftMouseButton()) {
	    if (frame.getMode() == GFrame.BROWSE_MODE) {
		if (aEvent.isShiftDown()) {
		    GraphEvents.shiftClick(owner);
		} else {
		    GraphEvents.click(owner);
		}
	    }
	}
    }
    
    public void centerDisplay() {
	frame.centerOn(this);
    }

    public HashSet hulls = null;

    public void addHullListener(ConvexHullNode chn) {
	if (hulls == null) {
	    hulls = new HashSet();
	}
	hulls.add(chn);
    }
    
    public void removeHullListener(ConvexHullNode chn) {
	if (hulls != null) {
	    hulls.remove(chn);
	}
    }

    private void notifyHullListeners() {
	if (hulls != null) {
	    Iterator it = hulls.iterator();
	    while(it.hasNext()) {
		ConvexHullNode chn = (ConvexHullNode)it.next();
		chn.setVisible(true);
		chn.nodeMoved(this);
	    }
	}
    }

    private void hideHullListeners() {
	if (hulls != null) {
	    Iterator it = hulls.iterator();
	    while(it.hasNext()) {
		ConvexHullNode chn = (ConvexHullNode)it.next();
		chn.setVisible(false);
	    }
	}
    }

    public Point2D[] getAllPorts() {
	Point2D[] toRet = new Point2D[8];
	toRet[0] = new Point2D.Double(getX()+getWidth()/2,
				      getY());
	toRet[1] = new Point2D.Double(getX()+getWidth()/2,
				      getY()+getHeight());
	toRet[2] = new Point2D.Double(getX(),
				      getY()+getHeight()/2);
	toRet[3] = new Point2D.Double(getX()+getWidth(),
				      getY()+getHeight()/2);
	toRet[4] = new Point2D.Double(getX(),getY());
	toRet[5] = new Point2D.Double(getX()+getWidth(),getY());
	toRet[6] = new Point2D.Double(getX(),getY()+getHeight());
	toRet[7] = new Point2D.Double(getX()+getWidth(),
				      getY()+getHeight());
	return(toRet);
    }

    public Point2D[] getPrefPorts() {
	Point2D[] toRet = new Point2D[4];
	toRet[0] = new Point2D.Double(getX()+getWidth()/2,
				      getY());
	toRet[1] = new Point2D.Double(getX()+getWidth()/2,
				      getY()+getHeight());
	toRet[2] = new Point2D.Double(getX(),
				      getY()+getHeight()/2);
	toRet[3] = new Point2D.Double(getX()+getWidth(),
				      getY()+getHeight()/2);
	return(toRet);
    }

}
