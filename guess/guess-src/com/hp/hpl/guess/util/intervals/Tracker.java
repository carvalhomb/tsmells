package com.hp.hpl.guess.util.intervals;

import java.util.HashSet;
import java.util.Iterator;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.*;
import org.python.core.*;
import javax.swing.text.DefaultStyledDocument;

public abstract class Tracker {

    private static IntervalTree itree = new IntervalTree();

    private static int location = 0;

    public static boolean enabled = true;

    private static DefaultStyledDocument doc = null;
    
    public static void setDocument(DefaultStyledDocument d) {
	doc = d;
    }

    public static void moveToDocEnd() {
	if (doc != null)
	    setLocation(doc.getLength());
    }

    public EditorPopup getMenu(Object o) {
	if (o instanceof PyInstance) {
	    if (((PyInstance)o).isNodeProxy()) {
		return(NodeEditorPopup.getPopup());
	    }
	    if (((PyInstance)o).isEdgeProxy()) {
		return(EdgeEditorPopup.getPopup());
	    }
	    if (((PyInstance)o).isTypeOfInterest()) {
		Interesting inte = 
		    (Interesting)(((PyInstance)o).__tojava__(Interesting.class));
		return(inte.getPopup());
	    }
	} else if (o instanceof PyString) {
	    return(null);
	} else if (o instanceof PySequence) {
	    Iterator it = ((PySequence)o).findGraphElements().iterator();
	    boolean nd = false;
	    boolean ed = false;
	    while (it.hasNext()) {
		Object o2 = it.next();
		if (o2 instanceof Node) 
		    nd = true;
		else if (o2 instanceof Edge)
		    ed = true;
	    }
	    if (nd && ed)
		return(GraphElementEditorPopup.getPopup());
	    else if (nd)
		return(NodeEditorPopup.getPopup());
	    else if (ed)
		return(EdgeEditorPopup.getPopup());
	    else
		return(null);
	} else if (o instanceof Throwable) {
	    return(ThrowableEditorPopup.getPopup());
	}
	return(null);
    }

    public static String generateStatusString(Object o) {
	if (o instanceof PyInstance)
	    o = (((PyInstance)o).__tojava__(Object.class));
	
	if (o instanceof Interesting) {
	    return(((Interesting)o).getStatusBarString());
	}
	return("");
    }

    public static void disableTracker() {
	enabled = false;
	itree = null;
	hs.clear();
    }

    public static void setLocation(int newloc) {
	location = newloc;
    }

    public static void incrementLocation(int incr) {
	location += incr;
    }


    public static IntervalNode[] searchOverlap(int low, int high) {
	if (!enabled)
	    return(new IntervalNode[0]);

	return(itree.searchOverlap(low,high));
    }

    public static IntervalNode[] searchContains(int low, int high) {
	if (!enabled)
	    return(new IntervalNode[0]);

	return(itree.searchContains(low,high));
    }
    
    public static IntervalNode[] searchContained(int low, int high) {
	if (!enabled)
	    return(new IntervalNode[0]);

	return(itree.searchContained(low,high));
    }

    public static IntervalNode[] searchExact(int low, int high) {
	if (!enabled)
	    return(new IntervalNode[0]);

	return(itree.searchExact(low,high));
    }

    private static HashSet hs = new HashSet();

    public static HashSet getRecentNodes() {
	return(hs);
    }

    public static TextPaneIntervalNode addNode(int length, Object o) {
	if (!enabled)
	    return(null);

	TextPaneIntervalNode inode = new TextPaneIntervalNode(location,location+length,o);
	hs.add(inode);
        itree.insert(inode);
	return(inode);
    }

    //    private static int nds = 0;

    public static TextPaneIntervalNode addNode(int start, int length, Object o) {
	if (!enabled)
	    return(null);

	TextPaneIntervalNode inode = new TextPaneIntervalNode(start,start+length,o);
	hs.add(inode);
        itree.insert(inode);
	return(inode);
	//nds++;
    }

    public static int getLocation() {
	return(location);
    }
    
    public static IntervalTree getIntervalTree() {
	return(itree);
    }

    public static void testWalk() {
	if (!enabled)
	    return;

	itree.inOrderWalk(itree.root,"");
	System.out.println(itree.root.getMin() + " " + itree.root.getMax());
	IntervalNode test = itree.treeMin(itree.root);
	System.out.println(test.getMin());
	test = itree.treeMax(itree.root);
	System.out.println(test.getMax());
    }
}
