package com.hp.hpl.guess.piccolo;

import com.hp.hpl.guess.*;
import com.hp.hpl.guess.storage.*;
import com.hp.hpl.guess.ui.*;
import java.util.*;

import edu.umd.cs.piccolo.activities.*;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PRoot;
import java.awt.Color;

/**
 * @pyobj Morpher
 * @pyimport from com.hp.hpl.guess.piccolo import Morpher
 */
public class Morpher {

    private static Color invisiblecolor = null;


    public static Color getInvisible() {
	return(invisiblecolor);
    }

    public static void morph(Graph g, String state, long duration) {
	// 500
	
	Color background = 
	    VisFactory.getFactory().getDisplay().getDisplayBackground();
	invisiblecolor = 
	    new Color(background.getRed(),
		      background.getBlue(),
		      background.getGreen(),
		      0);

	StorageListener sl = StorageFactory.getSL();

	Vector v = sl.getNodesNotInCurrent(g,state);
	for (int i = 0 ; i < v.size() ; i++) {
	    Node n = (Node)v.elementAt(i);
	    // System.out.println(n);
	    n.__setattr__("visible",new Boolean(false));
	    g.addNode(n);
	}
	
	Set nset = g.getNodes();
	GuessNodeActivity[] nActs = new GuessNodeActivity[nset.size()];

	NodeSchema ns = g.getNodeSchema();
	Field[] fields = new Field[]{ns.getField("x"),
				     ns.getField("y"),
				     ns.getField("color"),
				     ns.getField("visible"),
				     ns.getField("labelvisible"),
				     ns.getField("height"),
				     ns.getField("width"),
				     ns.getField("style")};
	
	Iterator it = nset.iterator();

	PActivityScheduler pas = null;
	PRoot pr = null;

	int i = 0;
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    Object[] vals = sl.getColumns(n,fields,state);
	    GuessNodeActivity gna = 
		new GuessNodeActivity(duration,
				      1,
				      (GuessPNode)n.getRep());
	    if (pas == null) {
		pr = ((PNode)n.getRep()).getRoot();
		pas = pr.getActivityScheduler();
	    }
	    //	    gna.setStarts();
	    if (vals == null) {
		// destination is not in graph
		gna.setDestinationGone();
	    } else {
		gna.setDestination(((Double)vals[0]).doubleValue(),
				   ((Double)vals[1]).doubleValue(),
				   Colors.getColor((String)vals[2],
						   null),
				   ((Boolean)vals[3]).booleanValue(),
				   ((Boolean)vals[4]).booleanValue(),
				   ((Double)vals[5]).doubleValue(),
				   ((Double)vals[6]).doubleValue());
	    }
	    nActs[i] = gna;
	    i++;
	}

	v = sl.getEdgesNotInCurrent(g,state);
	for (int j = 0 ; j < v.size() ; j++) {
	    Edge e = (Edge)v.elementAt(j);
	    // System.out.println(n);
	    e.__setattr__("visible",new Boolean(false));
	    g.addEdge(e);
	}

	Set eset = g.getEdges();
	GuessEdgeActivity[] eActs = new GuessEdgeActivity[eset.size()];

	EdgeSchema es = g.getEdgeSchema();
	fields = new Field[]{ns.getField("color"),
			     ns.getField("visible"),
			     ns.getField("width")};
	
	it = eset.iterator();

	i = 0;
	while(it.hasNext()) {
	    Edge e = (Edge)it.next();
	    Object[] vals = sl.getColumns(e,fields,state);
	    GuessEdgeActivity gea = 
		new GuessEdgeActivity(duration,
				      1,
				      (GuessPEdge)e.getRep());
	    if (pas == null) {
		pr = ((PNode)e.getRep()).getRoot();
		pas = pr.getActivityScheduler();
	    }
	    //	    gea.setStarts();
	    if (vals == null) {
		gea.setDestinationGone();
	    } else {
		gea.setDestination(Colors.getColor((String)vals[0],
						   null),
				   ((Boolean)vals[1]).booleanValue(),
				   ((Double)vals[2]).doubleValue());
	    }
	    eActs[i] = gea;
	    i++;
	}

	long start = System.currentTimeMillis() + 100;
	for (i = 0 ; i < nActs.length ; i++) {
	    nActs[i].setStartTime(start);
	    pas.addActivity(nActs[i]);
	}

	for (i = 0 ; i < eActs.length ; i++) {
	    eActs[i].setStartTime(start);
	    pas.addActivity(eActs[i]);
	}
	
	pr.waitForActivities();
	sl.loadState(g,state);
    }

    public static void morph(Graph g, int state, long duration) {
	morph(g,state+"",duration);
    }

    private static float nodeDisBy = 1;

    /**
     * @pyexport
     */
    public static float getNodeDisappearBy() {
	return(nodeDisBy);
    }

    /**
     * @pyexport
     */
    public static void setNodeDisappearBy(float nodeDisBy) {
	if (nodeDisBy > 1) {
	    nodeDisBy = 1;
	} else if (nodeDisBy < 0) {
	    nodeDisBy = 0;
	}
	Morpher.nodeDisBy = nodeDisBy;
    }

    private static float nodeAppearAfter = 0;

    /**
     * @pyexport
     */
    public static float getNodeAppearAfter() {
	return(nodeAppearAfter);
    }

    /**
     * @pyexport
     */
    public static void setNodeAppearAfter(float nodeAppearAfter) {
	if (nodeAppearAfter > 1) {
	    nodeAppearAfter = 1;
	} else if (nodeAppearAfter < 0) {
	    nodeAppearAfter = 0;
	}
	Morpher.nodeAppearAfter = nodeAppearAfter;
    }

    private static float edgeDisBy = 1;

    /**
     * @pyexport
     */
    public static float getEdgeDisappearBy() {
	return(edgeDisBy);
    }

    /**
     * @pyexport
     */
    public static void setEdgeDisappearBy(float edgeDisBy) {
	if (edgeDisBy > 1) {
	    edgeDisBy = 1;
	} else if (edgeDisBy < 0) {
	    edgeDisBy = 0;
	}
	Morpher.edgeDisBy = edgeDisBy;
    }

    private static float edgeAppearAfter = 0;

    /**
     * @pyexport
     */
    public static float getEdgeAppearAfter() {
	return(edgeAppearAfter);
    }

    /**
     * @pyexport
     */
    public static void setEdgeAppearAfter(float edgeAppearAfter) {
	if (edgeAppearAfter > 1) {
	    edgeAppearAfter = 1;
	} else if (edgeAppearAfter < 0) {
	    edgeAppearAfter = 0;
	}
	Morpher.edgeAppearAfter = edgeAppearAfter;
    }

}
