package com.hp.hpl.guess.ui;

import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.Edge;
import com.hp.hpl.guess.GraphElement;
import java.util.HashSet;
import java.util.Iterator;

public class GraphEvents {

    private static GraphEvents singleton = null;

    public static GraphEvents getGraphEvents() {
	if (singleton == null) {
	    singleton = new GraphEvents();
	} 
	return(singleton);
    }

    private HashSet gmls = new HashSet();

    public void addUIListener(UIListener gml) {
	gmls.add(gml);
    }

    public void removeUIListener(UIListener gml) {
	gmls.remove(gml);
    }

    public void addGraphMouseListener(GraphMouseListener gml) {
	gmls.add(gml);
    }

    public void removeGraphMouseListener(GraphMouseListener gml) {
	gmls.remove(gml);
    }

    public static void click(GraphElement ge) {
	if (ge instanceof Node) {
	    getGraphEvents().clickNodeI((Node)ge);
	} else {
	    getGraphEvents().clickEdgeI((Edge)ge);
	}
    }

    public static void shiftClick(GraphElement ge) {
	if (ge instanceof Node) {
	    getGraphEvents().shiftClickNodeI((Node)ge);
	} else {
	    getGraphEvents().shiftClickEdgeI((Edge)ge);
	}
    }

    public static void mouseEnter(GraphElement ge) {
	if (ge instanceof Node) {
	    getGraphEvents().mouseEnterNodeI((Node)ge);
	} else {
	    getGraphEvents().mouseEnterEdgeI((Edge)ge);
	}
    }

    public static void mouseLeave(GraphElement ge) {
	if (ge instanceof Node) {
	    getGraphEvents().mouseLeaveNodeI((Node)ge);
	} else {
	    getGraphEvents().mouseLeaveEdgeI((Edge)ge);
	}
    }

    public synchronized void shiftClickNodeI(Node n) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		GraphMouseListener gml = (GraphMouseListener)it.next();
		if (gml instanceof UIListener) {
		    ((UIListener)gml).shiftClickNode(n);
		}
	    }
	} catch (Exception ex) {
	}
    }

    public synchronized void shiftClickEdgeI(Edge e) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		GraphMouseListener gml = (GraphMouseListener)it.next();
		if (gml instanceof UIListener) {
		    ((UIListener)gml).shiftClickEdge(e);
		}
	    }
	} catch (Exception ex) {
	}
    }

    public synchronized void clickNodeI(Node n) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		GraphMouseListener gml = (GraphMouseListener)it.next();
		if (gml instanceof UIListener) {
		    ((UIListener)gml).clickNode(n);
		}
	    }
	} catch (Exception ex) {
	}
    }

    public synchronized void clickEdgeI(Edge e) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		GraphMouseListener gml = (GraphMouseListener)it.next();
		if (gml instanceof UIListener) {
		    ((UIListener)gml).clickEdge(e);
		}
	    }
	} catch (Exception ex) {
	}
    }

    public synchronized void mouseEnterNodeI(Node n) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		((GraphMouseListener)it.next()).mouseEnterNode(n);
	    }
	} catch (Exception ex) {
	}
    }

    public synchronized void mouseEnterEdgeI(Edge e) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		((GraphMouseListener)it.next()).mouseEnterEdge(e);
	    }
	} catch (Exception ex) {
	}
    }

    public synchronized void mouseLeaveNodeI(Node n) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		((GraphMouseListener)it.next()).mouseLeaveNode(n);
	    }
	} catch (Exception ex) {
	}
    }

    public synchronized void mouseLeaveEdgeI(Edge e) {
	try {
	    Iterator it = gmls.iterator();
	    while(it.hasNext()) {
		((GraphMouseListener)it.next()).mouseLeaveEdge(e);
	    }
	} catch (Exception ex) {
	}
    }
}
