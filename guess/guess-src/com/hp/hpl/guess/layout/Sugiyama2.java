package com.hp.hpl.guess.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import com.hp.hpl.guess.*;
import java.util.*;
import java.awt.geom.Point2D;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.AbstractSparseGraph;
import edu.uci.ics.jung.graph.impl.SimpleSparseVertex;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.algorithms.transformation.EadesGreedyDAG;
import java.awt.geom.GeneralPath;
import com.hp.hpl.guess.piccolo.*;

public class Sugiyama2 {

    Graph originalGraph;
    List nodeLevels;
    boolean vertical;

    protected Point spacing = new Point(250, 150);
    protected int gridAreaSize = Integer.MIN_VALUE;

    private Rectangle2D getBounds(Vertex v) {
	Node n = (Node)v.getEqualVertex(originalGraph);

	if (n == null)
	    return(null);

	return(new Rectangle2D.Double(n.getX(),
				      n.getY(),
				      n.getWidth(),
				      n.getHeight()));
    }
    
    public Sugiyama2(Graph view) {
	this.originalGraph = view;
	this.vertical = true;
	this.originalGraph = originalGraph;

	DirectedGraph graph = 
	    (DirectedGraph)EadesGreedyDAG.eadesGreedyDAG(originalGraph);

	Vertex[] selectedCellViews = new Node[graph.getVertices().size()];
	Iterator it = graph.getVertices().iterator();
	for (int i = 0 ; i < selectedCellViews.length ; i++) {
	    selectedCellViews[i] = (Vertex)it.next();
	}
	
	Vertex[] dynamic_cells = selectedCellViews;
	Vertex[] static_cells = selectedCellViews;
	
	/*  The Algorithm distributes the nodes on a grid.
	 *  For this grid you can configure the horizontal spacing.
	 *  This field specifies the configured value
	 *
	 */
	
	Rectangle2D maxBounds = new Rectangle2D.Double();
	for (int i = 0; i < selectedCellViews.length; i++) {
	    // Add vertex to list
	    // Fetch Bounds
	    Rectangle2D bounds = getBounds(selectedCellViews[i]);
	    
	    maxBounds.setFrame(0, 0,
			       Math.max(bounds.getWidth(), 
					maxBounds.getWidth()),
			       Math.max(bounds.getHeight(), 
					maxBounds.getHeight()));
	}
	
	if (spacing.x == 0)
	    spacing.x = (int) (maxBounds.getWidth());
	
	/*  The Algorithm distributes the nodes on a grid.
	 *  For this grid you can configure the vertical spacing.
	 *  This field specifies the configured value
	 *
	 */
	
	if (spacing.y == 0)
	    spacing.y = (int) (2*maxBounds.getHeight());
	
	// search all roots
	Vector roots = searchRoots(graph, selectedCellViews);
	
	// return if no root found
	if (roots.size() == 0)
	    return;
	
	HashMap vToWrapper = new HashMap();

	// create levels
	Vector levels = fillLevels(graph, selectedCellViews, 
				   roots, vToWrapper);

	ArrayList newLevels = new ArrayList();
	it = levels.iterator();
	while(it.hasNext()) {
	    Vector lv = (Vector)it.next();
	    ArrayList newlv = new ArrayList();
	    Iterator it2 = lv.iterator();
	    while (it2.hasNext()) {
		Vertex v = (Vertex)it2.next();
		if (v.getEqualVertex(originalGraph) != null) {
		    newlv.add(v.getEqualVertex(originalGraph));
		}
	    }
	    newLevels.add(newlv);
	}

	layout(newLevels,vertical);
    }

    /** Searches all Roots for the current Graph
     *  First the method marks any Vertex as not visited.
     *  Than calls searchRoots(MyGraphCell) for each
     *  not visited Cell.
     *  The Roots are stored in the Vector named roots
     *
     * 	@return returns a Vector with the roots
     *  @see #searchRoots(JGraph, CellView[])
     */
    protected Vector searchRoots(DirectedGraph jgraph, Vertex[] selectedCellViews) {
	
	// get all cells and relations
	Vector vertexViews = new Vector(selectedCellViews.length);
	Vector roots = new Vector();

	// first: mark all as not visited
	// O(allCells&Edges)
	for (int i = 0; i < selectedCellViews.length; i++) {
	    visited.remove(selectedCellViews[i]);
	    vertexViews.add(selectedCellViews[i]);
	}

	// O(graphCells)
	for (int i = 0; i < vertexViews.size(); i++) {
	    Vertex vertexView = (Vertex)vertexViews.get(i);
	    // need to deal with non-DAGS here
	    Set inEdges = vertexView.getInEdges();
	    if (inEdges.size() == 0) {
		roots.add(vertexView);
	    }
	}
	
	// Error Msg if the graph has no roots
	if (roots.size() == 0) {
	    throw new IllegalArgumentException("The Graph is not a DAG. Can't use Sugiyama Algorithm!");
	}
	return roots;
    }
    

    HashSet visited = new HashSet();
    HashMap wrappers = new HashMap();

    /** Method fills the levels and stores them in the member levels.
     *  Each level was represended by a Vector with Cell Wrapper objects.
     *  These Vectors are the elements in the <code>levels</code> Vector.
     *
     */
    protected Vector fillLevels(DirectedGraph jgraph,
				Vertex[] selectedCellViews,
				Vector rootNodes, HashMap vToL) {
	Vector levels = new Vector();
	
	// mark as not visited
	// O(allCells)
	for (int i = 0; i < selectedCellViews.length; i++) {
	    Vertex cellView = selectedCellViews[i];
	    
	    // more stabile
	    if (cellView == null)
		continue;

	    visited.remove(cellView);
	}

	Enumeration enumRoots = rootNodes.elements();
	while (enumRoots.hasMoreElements()) {
	    Vertex vertexView = (Vertex)enumRoots.nextElement();
	    fillLevels(jgraph, levels, 0, vertexView,vToL);
	}

	return levels;

    }

    /** Fills the Vector for the specified level with a wrapper
     *  for the MyGraphCell. After that the method called for
     *  each neighbor graph cell.
     *
     *  @param level        The level for the graphCell
     *  @param graphCell    The Graph Cell
     */
    protected void fillLevels(DirectedGraph jgraph,
			      Vector levels,
			      int level,
			      Vertex vertexView,
			      HashMap vToL) {
	// precondition control
	if (vertexView == null)
	    return;

	// be sure that a Vector container exists for the current level
	if (levels.size() == level)
	    levels.insertElementAt(new Vector(), level);

	// if the cell already visited return
	if (visited.contains(vertexView)) {
	    return;
	}

	// mark as visited for cycle tests
	visited.add(vertexView);

	// put the current node into the current level
	// get the Level Vector
	Vector vecForTheCurrentLevel = (Vector) levels.get(level);
	
	// Create a wrapper for the node
	int numberForTheEntry = vecForTheCurrentLevel.size();
	
	//	CellWrapper wrapper =
	//  new CellWrapper(level, numberForTheEntry, vertexView);

	//vToL.put(vertexView,wrapper);

	// put the Wrapper in the LevelVector
	vecForTheCurrentLevel.add(vertexView);

	// if the Cell has no Ports we can return, there are no relations
	Iterator it = vertexView.getOutEdges().iterator();
	while(it.hasNext()) {
	    DirectedEdge e = (DirectedEdge)it.next();
	    Vertex n1 = e.getSource();
	    Vertex n2 = e.getDest();
	    if (n1 == n2) {
		continue;
	    }
	    Vertex targetVertex = null;
	    if (vertexView == n1) {
		targetVertex = n2;
	    } else {
		targetVertex = n1;
	    }
	    fillLevels(jgraph, levels, (level + 1), targetVertex,vToL);
	}
	
	if (vecForTheCurrentLevel.size() > gridAreaSize) {
	    gridAreaSize = vecForTheCurrentLevel.size();
	}

    }

    /**
     * @param levels This is a List of Lists of Nodes, each list shows 
     *               the nodes that  are on the same level.  It is 
     *               assumed that the levels will be in order.
     */
    public void setNodeLevels(List levels) {
	this.nodeLevels = levels;
    }
    
    public void layout(List levels, boolean vertical) {
	////////////////////// debug code
	if (levels != null)
	    {
		//Let's see what's going on
		Iterator outer = levels.iterator();
		int i = 0;
		int j = 0;
		while (outer.hasNext())
		    {
			List list = (List)outer.next();
			Iterator inner = list.iterator();
			j = 0;
			while (inner.hasNext())
			    {
				Node node = (Node) inner.next();
				System.err.println ("In list #" + i + ", inner Node #" + j + " is named: " + node);
				j++;
			    }
			i++;
		    }
		///////////////////// end debug code
		this.nodeLevels = levels;
		this.vertical = vertical;
		layout();
	    }
	else
	    {
		System.err.println ("In Sugiyama, list of list of nodes == null");
	    }
    }
    
    
    public void layout() {
	
	Iterator level_iterator = nodeLevels.iterator();
	
	double major = 0; // Major refers to the axis that corresponds to 
	// i.e. the Y-axis if a horizonatal layout is chosen.
	
	int numLevels = nodeLevels.size();
	List thisTier, nextTier;
	for (int i = 0; i < numLevels; i++)
	    {
		if (i == numLevels - 1)
		    {
			thisTier = (List) nodeLevels.get (i);
			nextTier = null;
		    }
		else
		    {
			thisTier = (List)nodeLevels.get (i);
			nextTier = (List)nodeLevels.get (i + 1);
		    }
		System.err.println ("tier = " + i);
		layoutLevel (thisTier, nextTier, major);
		major += 200;
	    }
	
	// now go through and put the bends in the right place.
	level_iterator = nodeLevels.iterator();
	while ( level_iterator.hasNext() ) {
	    layoutBends( ( List )level_iterator.next() );
	}
    }
    
    public void layoutBends ( List nodes ) {
	Iterator member_iterator = nodes.iterator();
	Iterator neighbors;
	Edge edge_view;
	Node from, to;
	double from_width, from_height, to_width, to_height;
	
	while ( member_iterator.hasNext() ) {
	    from = (Node)member_iterator.next();
	    //System.err.println ("from = " + from);
	    neighbors = from.getNeighbors().iterator();
	    while ( neighbors.hasNext() ) {
		to = (Node)neighbors.next();
		//System.err.println ("\tto = " + to);
		if ( nodes.contains( to ) ) {
		    // these nodes are on the same level
		    // do nothing
		    continue;
		}
		
		double from_x = from.getX();
		double from_y = from.getY();
		double to_x = to.getX();
		double to_y = to.getY();
		from_width = from.getWidth();
		from_height = from.getHeight();
		to_width = to.getWidth();
		to_height = to.getHeight();
		
		// Matt Wyatt changed to addition from subtraction
		double mid_x = Math.abs( from_x + to_x ) / 2;
		double mid_y = Math.abs( from_y + to_y ) / 2;

		Iterator eit = from.getOutEdges().iterator();
		GeneralPath gp = new GeneralPath();
		while (eit.hasNext()) {
		    edge_view = (Edge)eit.next();
		    //Bend bend = edge_view.getBend();
		    Point2D from_point, to_point;
		 
		    if ( !vertical ) {
			if (  to_y > from_y ) {
			    from_point = new Point2D.Double( mid_x + from_x + from_width/2, mid_y + from_height/2);
			    to_point = new Point2D.Double( mid_x + to_width/2, mid_y + to_height/2);
			} else {
			    from_point = new Point2D.Double( mid_x + from_x + from_width/2, mid_y + from_height/2);
			    to_point = new Point2D.Double( mid_x + to_width/2, mid_y + to_height/2);
			}
		    } else {
			if (  to_x > from_x ) {
			    from_point = new Point2D.Double( mid_x + from_x + from_width/2, mid_y + from_height/2);
			    to_point = new Point2D.Double( mid_x + to_width/2, mid_y + to_height/2);
			} else {
			    from_point = new Point2D.Double( mid_x + from_x + from_width/2, mid_y + from_height/2);
			    to_point = new Point2D.Double( mid_x + to_width/2, mid_y + to_height/2);
			}
		    }

		    System.out.println(edge_view + " " + to_point);

		    gp.moveTo((float)(from.getX() + from.getWidth()/2),
			      (float)(from.getY() + from.getHeight()/2));
		    // gp.lineTo((float)to_point.getX(),
		    //     (float)to_point.getY());
		    gp.lineTo((float)(to.getX() + to.getWidth()/2),
			      (float)(to.getY() + to.getHeight()/2));
		    //((GuessPEdge)edge_view.getRep()).setShape(gp);
		}
	    }
	}
    }

    public void layoutLevel (List nodes, List children, double major) {
	Iterator member_iterator = nodes.iterator();
	Node node_view = null;
	double minor = 0;
	double loop = 1.0;
	double prevWidth = 0;
	int compWidth = 200; //originalGraph.getComponent().getWidth();
	System.err.println ("compWidth = " + compWidth);
	// TODO: we need to lay this out so that nodes that have a large number of children get more space
	// in their tier
	if (children != null)
	    {
		//TODO: fix this to check for children, not just neighbors
		System.err.println ("children != null");
		int childCount;
		// first, find out if there are nodes in this tier that don't have children
		boolean allHaveChildren = true;
		int numChildlessNodes = 0;
		while (member_iterator.hasNext())
		    {
			Node thisNode = (Node) member_iterator.next();
			System.err.println ("currently checking Node " + thisNode + " for children");
			Iterator childIterator = children.iterator();
			boolean hasChild = false;
			while (childIterator.hasNext())
			    {
				Node child = (Node) childIterator.next();
				System.err.println ("\tchecking node " + child);
				if (thisNode.findEdge(child) != null) {
				    {
					System.err.println ("\t" + thisNode + " is " + child + "'s parent");
					hasChild = true;
				    }
				}
			    }
			if (!hasChild)
			    {
				allHaveChildren = false;
				numChildlessNodes++;
			    }
		    }
		if (allHaveChildren)
		    {
			System.err.println ("allHaveChildren == true");
			int numChildren = children.size();
			member_iterator = nodes.iterator();
			while (member_iterator.hasNext())
			    {
				Iterator childrenIterator = children.iterator();
				Node parent = (Node) member_iterator.next();
				childCount = 0;
				while (childrenIterator.hasNext())
				    {
					Node child = (Node)childrenIterator.next();
					if (parent.findEdge(child) != null)
					    {
						childCount++;
					    }
				    }
				
				// now place the node, based on the number of children/total number of nodes in nextTier
				System.err.println ("Node " + parent + " has " + childCount + " children");
				double widthFactor = (double)childCount/(double)numChildren;
				double nodeSpaceWidth = widthFactor * (double)compWidth;
				double midpoint = (nodeSpaceWidth)/(double)2;
				minor = prevWidth + midpoint;
				System.err.println ("widthFactor = " + widthFactor + "\tnodeSpaceWidth = " + nodeSpaceWidth);
				System.err.println ("midpoint = " + midpoint + "\tminor = " + minor);
				node_view = parent;
				if (!vertical)
				    {
					node_view.setX(major);
					node_view.setY(minor);

					//node_view.setOffset(major, minor);
				    }
				else
				    {
					double nodeWidth = node_view.getWidth();
					node_view.setX(minor - (nodeWidth/(double)2));
					node_view.setY(major);
					//node_view.setOffset (minor - (nodeWidth/(double)2), major);
				    }
				prevWidth += nodeSpaceWidth;
				System.err.println ("new prevWidth = " + prevWidth);
				System.err.println ("--------------------------------");
			    }
		    }
		else // allHaveChildren == false
		    {
			System.err.println ("allHaveChildren == false");
			Iterator iterator = nodes.iterator();
			while (iterator.hasNext())
			    {
				Node parent = (Node)iterator.next();
				// with no children, consider the number of nodes in this row plus the next row.
				int numChildren = children.size();
				double numNodes = (double)numChildlessNodes + (double)numChildren;
				double widthFactor = (double)1/numNodes;
				System.err.println ("***there are " + children.size() + " children, and " + numChildlessNodes + " childless nodes");
				System.err.println ("***numNodes = " + numNodes);
				double nodeSpaceWidth = widthFactor * (double)compWidth;
				double midpoint = nodeSpaceWidth/(double)2;
				minor = prevWidth + midpoint;
				System.err.println ("widthFactor = " + widthFactor + "\tnodeSpaceWidth = " + nodeSpaceWidth);
				System.err.println ("midpoint = " + midpoint + "\tminor = " + minor);
				
				node_view = parent;
				if (!vertical)
				    {
					node_view.setX(major);
					node_view.setY(minor);
					//node_view.setOffset (major, minor);
				    }
				else
				    {
					double nodeWidth = node_view.getWidth();
					node_view.setX(minor - (nodeWidth/(double)2));
					node_view.setY(major);
					//node_view.setOffset (minor - (nodeWidth/(double)2), major);
				    }
				prevWidth += nodeSpaceWidth;
				System.err.println ("new prevWidth = " + prevWidth);
				System.err.println ("--------------------------------");
			    }
		    }
	    }
	else // children == null
	    {
		System.err.println ("Children == null");
		int numNodes = nodes.size();
		while (member_iterator.hasNext())
		    {
			Node n = (Node) member_iterator.next();
			//System.out.println ("TEST: " + n.getRootGraphIndex());
			node_view = n;//originalGraph.getNode (n.getRootGraphIndex());

			minor = loop / (double) (numNodes + 1) * (double) compWidth;
			System.err.println ("numNodes = " + numNodes + "\twidth = " + compWidth + "\tminor = " + minor);
			// changed from if (vertical) by Matt Wyatt, 11/7/03
			if (!vertical)
			    {
				node_view.setX(major);
				node_view.setY(minor);
				//node_view.setOffset (major, minor);
			    }
			else
			    {
				node_view.setX(minor);
				node_view.setY(major);
				//node_view.setOffset (minor, major);
			    }
			loop += 1.0;
			//minor += 200;
		    }
	    }
    }

}
