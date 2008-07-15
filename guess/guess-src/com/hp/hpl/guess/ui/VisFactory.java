package com.hp.hpl.guess.ui;

import com.hp.hpl.guess.piccolo.*;
import com.hp.hpl.guess.prefuse.*;
import com.hp.hpl.guess.jung.*;
import com.hp.hpl.guess.tg.*;
import com.hp.hpl.guess.*;
import java.awt.Color;

import org.python.core.PySequence;
import org.python.core.PyInstance;
import java.util.Collection;
import java.util.Vector;

/**
 * @pyobj vf
 */
public abstract class VisFactory {

    private static VisFactory singleton = null;

    public static final int PICCOLO = 1;
    public static final int PREFUSE = 2;
    public static final int TOUCHGRAPH = 3;
    public static final int NOVIS = 4;
    public static final int JUNG = 5;

    private static int uiMode = PICCOLO;
    
    public static final int RECTANGLE = 1;
    public static final int ELLIPSE = 2;
    public static final int ROUNDRECTANGLE = 3;

    public static final int TEXT_RECTANGLE = 4;
    public static final int TEXT_ELLIPSE = 5;
    public static final int TEXT_ROUNDRECTANGLE = 6;

    public static final int IMAGE = 7;

    public static final int RECTANGLE3D = 8;
    public static final int ELLIPSE3D = 9;
    public static final int ROUNDRECTANGLE3D = 10;

    public static final int USER = 4;

    public static void shutdown() {
	singleton = null;
    }

    public static void setFactory(int type) {
	if (singleton != null) {
	    throw new Error("VisFactory.setFactory() called twice");
	}
	if (type == PICCOLO) {
	    singleton = new PFactory();
	} else if (type == TOUCHGRAPH) {
	    singleton = new TGFactory();
	} else if (type == PREFUSE) {
	    singleton = new PrefuseFactory();
	} else if (type == JUNG) {
	    singleton = new JungVisFactory();
	} else {
	    singleton = new DumbFactory();
	}
	uiMode = type;
    }

    public static VisFactory getFactory() {
	if (singleton == null) {
	    setFactory(PICCOLO);
	}
	return(singleton);
    }

    public static int getUIMode() {
	return(uiMode);
    }

    public abstract FrameListener getDisplay();

    public abstract NodeListener generateNode(Node n);

    public abstract NodeListener generateNode(int type, double x, double y,
					      double width, double height,
					      Color clr, Node n);
    
    public abstract EdgeListener generateEdge(Edge n);
 
    public abstract void remove(Node rep);

    public abstract void remove(Edge rep);

    public abstract void add(Node rep);

    public abstract void add(Edge rep);
    
    public void runNow() {
    }
    
    private boolean directed = false;

    /**
     * @pyexport
     */
    public boolean getDirected() {
	return(directed);
    }

    /**
     * @pyexport
     */
    public void setDirected(boolean dir) {
	directed = dir;
    }

    /**
     * @pyexport
     */
    public ConvexHull createConvexHull(PySequence seq, String c) {
	Vector v = new Vector();
	for (int i = 0; i < seq.__len__(); i++)
	    {
		Node element =
		    (Node)((PyInstance)seq.__finditem__(i)).__tojava__(Node.class);
		v.add(element);
	    }
	return(createConvexHull(v,Colors.getColor(c,Color.red)));
    }

  
    public ConvexHull createConvexHull(Collection s, Color c) {
	return(null);
    }

    /**
     * @pyexport
     */
    public void removeConvexHull(ConvexHull c) {
    }

    /**
     * @pyexport
     */
    public void removeConvexHulls(PySequence seq) {
	for (int i = 0; i < seq.__len__(); i++) {
	    removeConvexHull((ConvexHull)((PyInstance)seq.__finditem__(i)).__tojava__(ConvexHull.class));
	}	
    }

    /**
     * @pyexport
     */
    public Collection getConvexHulls() {
	return(new java.util.HashSet());
    }
}

	







