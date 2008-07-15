/*
 * @(#)SugiyamaLayoutAlgorithm.java 1.0 18-MAY-2004
 * 
 * Copyright (c) 2004, Sven Luzar
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * - Neither the name of JGraph nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.hp.hpl.guess.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.*;

import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.ExceptionWindow;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.AbstractSparseGraph;
import edu.uci.ics.jung.graph.impl.SimpleSparseVertex;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.algorithms.transformation.EadesGreedyDAG;
import java.awt.geom.GeneralPath;
import com.hp.hpl.guess.piccolo.*;


/**
 * Arranges the nodes with the Sugiyama Layout Algorithm.<br>
 *
 * <a href="http://plg.uwaterloo.ca/~itbowman/CS746G/Notes/Sugiyama1981_MVU/">
 *  Link to the algorithm</a>
 *
 *<br>
 *<br>
 * @author Sven Luzar<br>
 * modified by Gaudenz Alder
 * @version 1.0 init
 */
public class SugiyamaLayout {
    
    /** Const to add Attributes at the Nodes
     *
     */
    public static final String SUGIYAMA_VISITED = "SugiyamaVisited" /*#Frozen*/;
    
    /** Const to add the Cell Wrapper to the Nodes
     */
    public static final String SUGIYAMA_CELL_WRAPPER =
	"SugiyamaCellWrapper" /*#Frozen*/;
    
    /** represents the size of the grid in horizontal grid elements
     *
     */
    protected int gridAreaSize = Integer.MIN_VALUE;
    
    /** A vector with Integer Objects. The Vector contains the
     *  history of movements per loop
     *  It was needed for the progress dialog
     */
    Vector movements = null;
    /** Represents the movements in the current loop.
     *  It was needed for the progress dialog
     */
    int movementsCurrentLoop = -1;
    /** Represents the maximum of movements in the current loop.
     *  It was needed for the progress dialog
     */
    int movementsMax = Integer.MIN_VALUE;
    /** Represents the current loop number
     *  It was needed for the progress dialog
     */
    int iteration = 0;
    
    protected boolean vertical = true;
    
    protected Point spacing = new Point(250, 150);
    
    /**
     * Returns an new instance of SugiyamaLayoutSettings
     */
    //public JGraphLayoutSettings createSettings() {
    //return new SugiyamaLayoutSettings(this);
    //}

    /**
     * Returns the name of this algorithm in human
     * readable form.
     */
    public String toString() {
	return "Sugiyama";
    }
    
    /**
     * Get a human readable hint for using this layout.
     */
    public String getHint() {
	return "Ignores selection";
    }
    
    private Graph originalGraph = null;

    private Rectangle2D getBounds(Vertex v) {
	Node n = (Node)v.getEqualVertex(originalGraph);

	if (n == null)
	    return(null);

	return(new Rectangle2D.Double(n.getX(),
				      n.getY(),
				      n.getWidth(),
				      n.getHeight()));
    }

    HashMap vToWrapper = new HashMap();

    /**
     * Implementation.
     *
     * First of all the Algorithm searches the roots from the
     * Graph. Starting from this roots the Algorithm creates
     * levels and stores them in the member <code>levels</code>.
     * The Member levels contains Vector Objects and the Vector per level
     * contains Cell Wrapper Objects. After that the Algorithm
     * tries to solve the edge crosses from level to level and
     * goes top down and bottom up. After minimization of the
     * edge crosses the algorithm moves each node to its
     * bary center. Last but not Least the method draws the Graph.
     *
     * @see LayoutAlgorithm
     *
     * @param graph JGraph instance
     */
    public SugiyamaLayout(Graph originalGraph, boolean bends) {

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
	
	// create levels
	Vector levels = fillLevels(graph, selectedCellViews, 
				   roots, vToWrapper);

	HashMap edgeToFunkyPath = new HashMap();

	if (bends) {
	    // ok, let's add some fake nodes
	    int lev = 0;
	    it = levels.iterator();
	    Vector toAdd = new Vector();
	    while(it.hasNext()) {
		Vector curLev = (Vector)it.next();
		//System.out.println("looking at level " + lev);
		Iterator it2 = curLev.iterator();
		while(it2.hasNext()) {
		    CellWrapper cw = (CellWrapper)it2.next();
		    Vertex topV = cw.getNode();
		    //System.out.println("\tconsidering cell: " + topV);
		    HashSet tempSet = new HashSet();
		    tempSet.addAll(topV.getOutEdges());
		    Iterator it3 = tempSet.iterator();
		    while(it3.hasNext()) {
			DirectedEdge e = (DirectedEdge)it3.next();
			//System.out.println("\t\tHas edge: " + e);
			Vertex botV = e.getDest();
			////System.out.println(botV);
			if (!vToWrapper.containsKey(botV))
			    continue;
			int botLev = ((CellWrapper)vToWrapper.get(botV)).level;
			//System.out.println("\t\tdest at level: " + botLev);
			if (botLev > lev + 1) {
			    //System.out.println("\t\t\tadding dummy");
			    Vector funky = new Vector();
			    graph.removeEdge(e);
			    Vertex prev = topV;
			    for (int i = lev + 1 ; i < botLev ; i++) {
				//System.out.println("\t\t\tat level: " + i);
				Vertex dummy = 
				    new SimpleSparseVertex();
				Vector levToMod =
				    (Vector)levels.elementAt(i);
				int vSi = levToMod.size();
				CellWrapper newCW = 
				    new CellWrapper(i,vSi,dummy,true);
				vToWrapper.put(dummy,newCW);
				levToMod.add(newCW);
				toAdd.insertElementAt(newCW,0);
				graph.addVertex(dummy);
				DirectedSparseEdge dse = 
				    new DirectedSparseEdge(prev,dummy); 
				graph.addEdge(dse);
				prev = dummy;
				funky.addElement(dse);
			    }
			    DirectedSparseEdge dse = 
				new DirectedSparseEdge(prev,botV);
			    graph.addEdge(dse);
			    funky.addElement(dse);
			    edgeToFunkyPath.put(e,funky);
			    //System.out.println(e + " " + funky);
			}
		    }
		}
		lev++;
	    }
	}


	// solves the edge crosses
	solveEdgeCrosses(graph, levels);
	
	// move all nodes into the barycenter
	moveToBarycenter(graph, selectedCellViews, levels);
	
	Point min = findMinimumAndSpacing(selectedCellViews, spacing);
	
	// draw the graph in the window
	drawGraph(graph, levels, min, spacing);
	
	shiftLayers(levels);

	// clean temp values from the nodes / cells
	// the clean up was made in drawGraph
	//cleanUp(selectedCellViews);
	it = edgeToFunkyPath.keySet().iterator();
	while(it.hasNext()) {
	    DirectedSparseEdge dse = (DirectedSparseEdge)it.next();
	    Vector path = (Vector)edgeToFunkyPath.get(dse);
	    // we've basically lost our mapping from new to original
	    // (need to figure out why), but we can recover by looking
	    // at the first and last elements in the path
	    DirectedSparseEdge first = 
		(DirectedSparseEdge)path.elementAt(0);
	    DirectedSparseEdge last = 
		(DirectedSparseEdge)path.elementAt(path.size() - 1);
	    Node source = 
		(Node)((Vertex)first.getSource()).getEqualVertex(originalGraph);
	    Node dest = 
		(Node)((Vertex)last.getDest()).getEqualVertex(originalGraph);
	    Edge originalEdge = (Edge)source.findEdge(dest);
	    if (originalEdge == null) {
		// reversed edge
		originalEdge = (Edge)dest.findEdge(source);
	    }
	    GeneralPath gp = new GeneralPath();
	    gp.moveTo((float)(source.getX() + source.getWidth()/2),
		      (float)(source.getY() + source.getHeight()/2));
	    //System.out.println((source.getX() + source.getWidth()/2) + " " +
	    //	       (source.getY() + source.getHeight()/2));
	    Iterator pathIt = path.iterator();
	    while(pathIt.hasNext()) {
		DirectedEdge de = (DirectedEdge)pathIt.next();
		Vertex nxt = (Vertex)de.getDest();
		CellWrapper cw = (CellWrapper)vToWrapper.get(nxt);
		if (cw == null) {
		    ////System.out.println("ummmm...." + nxt);
		    break;
		}
		gp.lineTo((float)cw.x,
			  (float)cw.y);
		//System.out.println(cw.x + " " + cw.y);
	    }
	    gp.lineTo((float)(dest.getX() + dest.getWidth()/2),
		      (float)(dest.getY() + dest.getHeight()/2));
	    ((GuessPEdge)originalEdge.getRep()).setShape(gp);
	    //System.out.println((dest.getX() + dest.getWidth()/2) + " " +
	    //		       (dest.getY() + dest.getHeight()/2));
	    ////System.out.println(dse + " " + originalEdge + " " + path);
	}
    }

    public void shiftLayers(Vector levels) {
	for (int i = 0 ; i < levels.size() ; i++) {
	    //for (int i = 0 ; i <= 0 ; i++) {
	    Vector curLayer = (Vector)levels.elementAt(i);
	    double prev = averageTension(curLayer);
	    shiftLayer(curLayer,5);
	    double amtShifted = 5;
	    double right = averageTension(curLayer);
	    shiftLayer(curLayer,-10);
	    amtShifted += -10;
	    double left = averageTension(curLayer);
	    //System.out.println(prev + " " + left + " " + right);
	    double sign = 1;
	    if (left < right) {
		sign = sign * -1;
	    } 	
	    double cur = prev;
	    while(true) {
		shiftLayer(curLayer,sign*5);
		amtShifted += sign * 5;
		cur = averageTension(curLayer);
		if (cur > prev) {
		    shiftLayer(curLayer,sign*-1*5);
		    amtShifted += -1 * sign * 5;
		    break;
		}
		////System.out.println(cur);
		prev = cur;
	    }
	    for (int j = i - 1 ; j >= 0 ; j--) {
		curLayer = (Vector)levels.elementAt(j);
		shiftLayer(curLayer,amtShifted);
	    }
	}
	for (int i = levels.size() - 1; i > 0 ; i--) {
	    //for (int i = 0 ; i <= 0 ; i++) {
	    Vector curLayer = (Vector)levels.elementAt(i);
	    double prev = averageTension(curLayer);
	    shiftLayer(curLayer,5);
	    double amtShifted = 5;
	    double right = averageTension(curLayer);
	    shiftLayer(curLayer,-10);
	    amtShifted += -10;
	    double left = averageTension(curLayer);
	    //System.out.println(prev + " " + left + " " + right);
	    double sign = 1;
	    if (left < right) {
		sign = sign * -1;
	    } 	
	    double cur = prev;
	    while(true) {
		shiftLayer(curLayer,sign*5);
		amtShifted += sign * 5;
		cur = averageTension(curLayer);
		if (cur > prev) {
		    shiftLayer(curLayer,sign*-1*5);
		    amtShifted += -1 * sign * 5;
		    break;
		}
		////System.out.println(cur);
		prev = cur;
	    }
	    for (int j = i + 1 ; j < levels.size() ; j++) {
		curLayer = (Vector)levels.elementAt(j);
		shiftLayer(curLayer,amtShifted);
	    }
	}
	Iterator it = vToWrapper.values().iterator();
	while(it.hasNext()) {
	    CellWrapper cw = (CellWrapper)it.next();
	    Vertex v = (Vertex)cw.getNode().getEqualVertex(originalGraph);
	    if (v != null) {
		((Node)v).setX(cw.x);
		((Node)v).setY(cw.y);
	    }
	}
    }

    public void shiftLayer(Vector curLayer, double inc) {
	if (curLayer == null)
	    return;
	
	for (int n = 0 ; n < curLayer.size() ; n++) {
	    CellWrapper nh = (CellWrapper)curLayer.elementAt(n);
	    nh.x += inc;
	}
    }

    public double averageTension(Vector curLayer) {
	int segments = 0;
	double totallength = 0;
	for (int n = 0 ; n < curLayer.size() ; n++) {
	    CellWrapper nh = (CellWrapper)curLayer.elementAt(n);
	    Vertex v = (Vertex)nh.getNode();
	    Iterator it = v.getNeighbors().iterator();
	    while(it.hasNext()) {
		CellWrapper neig = (CellWrapper)vToWrapper.get(it.next());
		if (neig != null) {
		    double length = Math.sqrt((nh.x - neig.x)*(nh.x - neig.x) + 
					      (nh.y - neig.y)*(nh.y - neig.y));
		    totallength += length;
		    segments++;
		}
	    }
	}
	if (segments == 0) {
	    return(0);
	} else {
	    return(totallength/segments);
	}
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
	
	CellWrapper wrapper =
	    new CellWrapper(level, numberForTheEntry, vertexView);

	vToL.put(vertexView,wrapper);

	// put the Wrapper in the LevelVector
	vecForTheCurrentLevel.add(wrapper);

	// concat the wrapper to the cell for an easy access
	wrappers.put(vertexView,wrapper);

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

    /** calculates the minimum for the paint area.
     *
     */
    protected Point findMinimumAndSpacing(Vertex[] graphCellViews,
					  Point spacing) {
	try {

	    // variables
	    /* represents the minimum x value for the paint area
	     */
	    int min_x = 1000000;

	    /* represents the minimum y value for the paint area
	     */
	    int min_y = 1000000;

	    // find the maximum & minimum coordinates

	    for (int i = 0; i < graphCellViews.length; i++) {

		// the cellView and their bounds
		Vertex cellView = graphCellViews[i];

		if (cellView == null)
		    continue;

		Rectangle2D cellViewBounds = getBounds(cellView);
		
		// checking min area
		try {
		    if ((int)cellViewBounds.getX() < min_x)
			min_x = (int)cellViewBounds.getX();
		    if ((int)cellViewBounds.getY() < min_y)
			min_y = (int)cellViewBounds.getY();
		    /*
		      if (cellViewBounds.width > spacing.x)
		      spacing.x = cellViewBounds.width;
		      if (cellViewBounds.height > spacing.y)
		      spacing.y = cellViewBounds.height;
		    */

		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }
	    // if the cell sice is bigger than the userspacing
	    // dublicate the spacingfactor
	    return new Point(min_x, min_y);

	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
	return null;
    }

    /** Updates the progress based on the movements count
     *
     */
    protected void updateProgress4Movements() {
	// adds the current loop count
	movements.add(new Integer(movementsCurrentLoop));
	iteration++;

	// if the current loop count is higher than the max movements count
	// memorize the new max
	if (movementsCurrentLoop > movementsMax) {
	    movementsMax = movementsCurrentLoop;
	}
    }

    protected void solveEdgeCrosses(DirectedGraph jgraph, Vector levels) {
	movements = new Vector(100);
	movementsCurrentLoop = -1;
	movementsMax = Integer.MIN_VALUE;
	iteration = 0;

	while (movementsCurrentLoop != 0) {

	    // reset the movements per loop count
	    movementsCurrentLoop = 0;

	    // top down
	    for (int i = 0; i < levels.size() - 1; i++) {
		movementsCurrentLoop
		    += solveEdgeCrosses(jgraph, true, levels, i);
	    }

	    // bottom up
	    for (int i = levels.size() - 1; i >= 1; i--) {
		movementsCurrentLoop
		    += solveEdgeCrosses(jgraph, false, levels, i);
	    }

	    updateProgress4Movements();
	}
    }

    /**
     *  @return movements
     */
    protected int solveEdgeCrosses(DirectedGraph jgraph,
				   boolean down,
				   Vector levels,
				   int levelIndex) {
	// Get the current level
	Vector currentLevel = (Vector) levels.get(levelIndex);
	int movements = 0;

	// restore the old sort
	Object[] levelSortBefore = currentLevel.toArray();

	// new sort
	Collections.sort(currentLevel);

	// test for movements
	for (int j = 0; j < levelSortBefore.length; j++) {
	    if (((CellWrapper) levelSortBefore[j]).getEdgeCrossesIndicator()
		!= ((CellWrapper) currentLevel.get(j))
		.getEdgeCrossesIndicator()) {
		movements++;

	    }
	}

	// Collecations Sort sorts the highest value to the first value
	for (int j = currentLevel.size() - 1; j >= 0; j--) {
	    CellWrapper sourceWrapper = (CellWrapper) currentLevel.get(j);
	    
	    Vertex sourceView = sourceWrapper.getNode();

	    Iterator it = sourceView.getOutEdges().iterator();
	    while(it.hasNext()) {
		DirectedEdge e = (DirectedEdge)it.next();
		Vertex n1 = e.getSource();
		Vertex n2 = e.getDest();
		if (n1 == n2) {
		    continue;
		}
		Vertex targetVertex = null;
		if (sourceView == n1) {
		    targetVertex = n2;
		} else {
		    targetVertex = n1;
		}
		CellWrapper targetWrapper =
		    (CellWrapper)wrappers.get(targetVertex);

		// do it only if the edge is a forward edge to a deeper level
		if (down
		    && targetWrapper != null
		    && targetWrapper.getLevel() > levelIndex) {
		    targetWrapper.addToEdgeCrossesIndicator(sourceWrapper.getEdgeCrossesIndicator());
		}
		if (!down
		    && targetWrapper != null
		    && targetWrapper.getLevel() < levelIndex) {
		    targetWrapper.addToEdgeCrossesIndicator(sourceWrapper.getEdgeCrossesIndicator());
		}
	    }
	}
	return movements;
    }
    
    protected void moveToBarycenter(DirectedGraph jgraph,
				    Vertex[] allSelectedViews,
				    Vector levels) {
	
	//================================================================
	// iterate any ReViewNodePort
	for (int i = 0; i < allSelectedViews.length; i++) {
	    Vertex vertexView = allSelectedViews[i];
	    
	    CellWrapper currentwrapper =
		(CellWrapper)wrappers.get(vertexView);

	    Iterator it = vertexView.getOutEdges().iterator();
	    while(it.hasNext()) {
		DirectedEdge e = (DirectedEdge)it.next();
		Vertex n1 = e.getSource();
		Vertex n2 = e.getDest();
		if (n1 == n2) {
		    continue;
		}
		Vertex neighborVertex = null;
		if (vertexView == n1) {
		    neighborVertex = n2;
		} else {
		    neighborVertex = n1;
		}
		
		if (neighborVertex == null
		    || neighborVertex == vertexView)
		    continue;
		
		CellWrapper neighborWrapper =
		    (CellWrapper)wrappers.get(neighborVertex);
		
		if (currentwrapper == null
		    || neighborWrapper == null
		    || currentwrapper.level == neighborWrapper.level)
		    continue;

		currentwrapper.priority++;
		
	    }
	}
	
	//================================================================
	for (int j = 0; j < levels.size(); j++) {
	    Vector level = (Vector) levels.get(j);
	    for (int i = 0; i < level.size(); i++) {
		// calculate the initial Grid Positions 1, 2, 3, .... per Level
		CellWrapper wrapper = (CellWrapper) level.get(i);
		wrapper.setGridPosition(i);
	    }
	}

	movements = new Vector(100);
	movementsCurrentLoop = -1;
	movementsMax = Integer.MIN_VALUE;
	iteration = 0;

	//int movements = 1;

	while (movementsCurrentLoop != 0) {
	    
	    // reset movements
	    movementsCurrentLoop = 0;

	    // top down
	    for (int i = 1; i < levels.size(); i++) {
		movementsCurrentLoop += moveToBarycenter(jgraph, levels, i);
	    }

	    // bottom up
	    for (int i = levels.size() - 1; i >= 0; i--) {
		movementsCurrentLoop += moveToBarycenter(jgraph, levels, i);
	    }

	    this.updateProgress4Movements();
	}

    }

    protected int moveToBarycenter(DirectedGraph jgraph,
				   Vector levels,
				   int levelIndex) {

	// Counter for the movements
	int movements = 0;

	// Get the current level
	Vector currentLevel = (Vector) levels.get(levelIndex);

	for (int currentIndexInTheLevel = 0;
	     currentIndexInTheLevel < currentLevel.size();
	     currentIndexInTheLevel++) {

	    CellWrapper sourceWrapper =
		(CellWrapper) currentLevel.get(currentIndexInTheLevel);

	    float gridPositionsSum = 0;
	    float countNodes = 0;

	    Vertex vertexView = sourceWrapper.getNode();
	    Iterator it = vertexView.getOutEdges().iterator();
	    while(it.hasNext()) {
		DirectedEdge e = (DirectedEdge)it.next();
		Vertex n1 = e.getSource();
		Vertex n2 = e.getDest();
		if (n1 == n2) {
		    continue;
		}
		Vertex neighborVertex = null;
		if (vertexView == n1) {
		    neighborVertex = n2;
		} else {
		    neighborVertex = n1;
		}
					
		if (neighborVertex == null)
		    continue;
		
		CellWrapper targetWrapper =
		    (CellWrapper)wrappers.get(neighborVertex);
		
		if (targetWrapper == sourceWrapper)
		    continue;
		if (targetWrapper == null
		    || targetWrapper.getLevel() == levelIndex)
		    continue;
		
		gridPositionsSum += targetWrapper.getGridPosition();
		countNodes++;
	    }
	    

	    //----------------------------------------------------------
	    // move node to new x coord
	    //----------------------------------------------------------
	    
	    if (countNodes > 0) {
		float tmp = (gridPositionsSum / countNodes);
		int newGridPosition = Math.round(tmp);
		boolean toRight =
		    (newGridPosition > sourceWrapper.getGridPosition());

		boolean moved = true;
		
		while (newGridPosition != sourceWrapper.getGridPosition()
		       && moved) {
		    int tmpGridPos = sourceWrapper.getGridPosition();

		    moved =
			move(
			     toRight,
			     currentLevel,
			     currentIndexInTheLevel,
			     sourceWrapper.getPriority());

		    if (moved)
			movements++;

		}
	    }
	}
	return movements;
    }

    /**@param  toRight <tt>true</tt> = try to move the currentWrapper to right; <tt>false</tt> = try to move the currentWrapper to left;
     * @param  currentLevel Vector which contains the CellWrappers for the current level
     * @param  currentIndexInTheLevel
     * @param  currentPriority
     * @param  currentWrapper The Wrapper
     *
     * @return The free GridPosition or -1 is position is not free.
     */
    protected boolean move(boolean toRight,
			   Vector currentLevel,
			   int currentIndexInTheLevel,
			   int currentPriority) {

	CellWrapper currentWrapper =
	    (CellWrapper) currentLevel.get(currentIndexInTheLevel);
	
	boolean moved = false;
	int neighborIndexInTheLevel =
	    currentIndexInTheLevel + (toRight ? 1 : -1);
	int newGridPosition =
	    currentWrapper.getGridPosition() + (toRight ? 1 : -1);

	// is the grid position possible?

	if (0 > newGridPosition || newGridPosition >= gridAreaSize) {
	    return false;
	}

	// if the node is the first or the last we can move
	if (toRight
	    && currentIndexInTheLevel == currentLevel.size() - 1
	    || !toRight
	    && currentIndexInTheLevel == 0) {

	    moved = true;

	} else {
	    // else get the neighbor and ask his gridposition
	    // if he has the requested new grid position
	    // check the priority

	    CellWrapper neighborWrapper =
		(CellWrapper) currentLevel.get(neighborIndexInTheLevel);

	    int neighborPriority = neighborWrapper.getPriority();

	    if (neighborWrapper.getGridPosition() == newGridPosition) {
		if (neighborPriority >= currentPriority) {
		    return false;
		} else {
		    moved =
			move(
			     toRight,
			     currentLevel,
			     neighborIndexInTheLevel,
			     currentPriority);
		}
	    } else {
		moved = true;
	    }
	}

	if (moved) {
	    currentWrapper.setGridPosition(newGridPosition);
	}
	return moved;
    }

    /** This Method draws the graph. For the horizontal position
     *  we are using the grid position from each graphcell.
     *  For the vertical position we are using the level position.
     *
     */
    protected HashMap drawGraph(DirectedGraph jgraph,
				Vector levels,
				Point min,
				Point spacing) {
	// paint the graph
	
	Map viewMap = new Hashtable();

	HashMap dummyLocs = new HashMap();

	for (int rowCellCount = 0;
	     rowCellCount < levels.size();
	     rowCellCount++) {
	    Vector level = (Vector) levels.get(rowCellCount);

	    for (int colCellCount = 0;
		 colCellCount < level.size();
		 colCellCount++) {
		CellWrapper wrapper = (CellWrapper)level.get(colCellCount);
		Vertex view = wrapper.vertexView;

		// remove the temp objects
		/* While the Algorithm is running we are putting some
		 *  attributeNames to the MyGraphCells. This method
		 *  cleans this objects from the MyGraphCells.
		 *
		 */

		// get the bounds from the cellView
		if (view == null)
		    continue;
		
		Rectangle2D rect = getBounds(view);
		if (rect == null) {
		    rect = new Rectangle2D.Double(0,0,1,1);
		}

		Rectangle bounds =  new Rectangle((int) rect.getX(), 
						  (int) rect.getY(), 
						  (int) rect.getWidth(), 
						  (int) rect.getHeight());
		//(Rectangle) view.getBounds().clone();
		
		// adjust
		bounds.x = min.x + spacing.x * ((vertical) ? wrapper.getGridPosition() : rowCellCount);
		bounds.y = min.y + spacing.y * ((vertical) ? rowCellCount :  wrapper.getGridPosition());

		wrapper.x = bounds.x;
		wrapper.y = bounds.y;
		Vertex vx = (Vertex)view.getEqualVertex(originalGraph);
		if (vx != null) {
		    ((Node)vx).setX(bounds.x);
		    ((Node)vx).setY(bounds.y);
		}
		//System.out.println(vx);
	    }
	}
	return(dummyLocs);
    }

    /** cell wrapper contains all values
     *  for one node
     */
    class CellWrapper implements Comparable {

	/** sum value for edge Crosses
	 */
	private double edgeCrossesIndicator = 0;
	/** counter for additions to the edgeCrossesIndicator
	 */
	private int additions = 0;
	/** the vertical level where the cell wrapper is inserted
	 */
	int level = 0;
	/** current position in the grid
	 */
	int gridPosition = 0;
	/** priority for movements to the barycenter
	 */
	int priority = 0;
	/** reference to the wrapped cell
	 */
	Vertex vertexView = null;

	boolean dummy = false;

	public double x = 0;
	public double y = 0;

	CellWrapper(int level,
		    double edgeCrossesIndicator,
		    Vertex vertexView) {
	    this(level,edgeCrossesIndicator,vertexView,false);
	}

	/** creates an instance and memorizes the parameters
	 *
	 */
	CellWrapper(int level,
		    double edgeCrossesIndicator,
		    Vertex vertexView,
		    boolean dummy) {
	    this.level = level;
	    this.edgeCrossesIndicator = edgeCrossesIndicator;
	    this.vertexView = vertexView;
	    additions++;
	    this.dummy = dummy;
	}

	/** returns the wrapped cell
	 */
	Vertex getNode() {
	    return vertexView;
	}

	/** resets the indicator for edge crosses to 0
	 */
	void resetEdgeCrossesIndicator() {
	    edgeCrossesIndicator = 0;
	    additions = 0;
	}

	/** retruns the average value for the edge crosses indicator
	 *
	 *  for the wrapped cell
	 *
	 */

	double getEdgeCrossesIndicator() {
	    if (additions == 0)
		return 0;
	    return edgeCrossesIndicator / additions;
	}

	/** Addes a value to the edge crosses indicator
	 *  for the wrapped cell
	 *
	 */
	void addToEdgeCrossesIndicator(double addValue) {
	    edgeCrossesIndicator += addValue;
	    additions++;
	}
	/** gets the level of the wrapped cell
	 */
	int getLevel() {
	    return level;
	}

	/** gets the grid position for the wrapped cell
	 */
	int getGridPosition() {
	    return gridPosition;
	}

	/** Sets the grid position for the wrapped cell
	 */
	void setGridPosition(int pos) {
	    this.gridPosition = pos;
	}

	/** increments the the priority of this cell wrapper.
	 *
	 *  The priority was used by moving the cell to its
	 *  barycenter.
	 *
	 */

	void incrementPriority() {
	    priority++;
	}

	/** returns the priority of this cell wrapper.
	 *
	 *  The priority was used by moving the cell to its
	 *  barycenter.
	 */
	int getPriority() {
	    return priority;
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object compare) {
	    if (((CellWrapper) compare).getEdgeCrossesIndicator()
		== this.getEdgeCrossesIndicator())
		return 0;

	    double compareValue =
		(((CellWrapper) compare).getEdgeCrossesIndicator()
		 - this.getEdgeCrossesIndicator());

	    return (int) (compareValue * 1000);

	}
    }
    /**
     * @return Returns the spacing.
     */
    public Point getSpacing() {
	return spacing;
    }
    /**
     * @param spacing The spacing to set.
     */
    public void setSpacing(Point spacing) {
	this.spacing = spacing;
    }
    public boolean isVertical() {
	return vertical;
    }
    public void setVertical(boolean vertical) {
	this.vertical = vertical;
    }
}

