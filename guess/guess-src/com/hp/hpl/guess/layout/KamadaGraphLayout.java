/*$$
 * packages uchicago.src.*
 * Copyright (c) 1999, Trustees of the University of Chicago
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of the University of Chicago nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Nick Collier
 * nick@src.uchicago.edu
 *
 * packages cern.jet.random.*
 * Copyright (c) 1999 CERN - European Laboratory for Particle
 * Physics. Permission to use, copy, modify, distribute and sell this
 * software and its documentation for any purpose is hereby granted without
 * fee, provided that the above copyright notice appear in all copies
 * and that both that copyright notice and this permission notice appear in
 * supporting documentation. CERN makes no representations about the
 * suitability of this software for any purpose. It is provided "as is"
 * without expressed or implied warranty.
 *
 * Wolfgang Hoschek
 * wolfgang.hoschek@cern.ch
 * @author Hacked by Eytan Adar for Guess classes
 *$$*/
package com.hp.hpl.guess.layout;
import com.hp.hpl.guess.*;

import java.util.*;
import java.lang.Math;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

/**
 * Positions network nodes according to the Kamada-Kawai algorithm.
 * The Kamada-Kawai graph layout class attempts to position nodes on
 * the space so that the geometric (Euclidean) distance between them
 * is as close as possible to the graph-theoretic (path) distance
 * between them.  the x and y coordinates of the nodes will be
 * modified by the layout.<p>
 *
 * The KamadaGraphLayout implements the ActionListener interface to
 * interrupt the layout. This breaks out of the algorithm
 * implementation as soon as possible, but will rescale the display if
 * appropriate. You can have the KamadaGraphLayout listener for RePast
 * toolbar button presses by including something like the following
 * code inside your model class.
 *
 * <code><pre>
 * graphLayout = new KamadaGraphLayout(...);
 * Controller c = (Controller)getController();
 * c.addStopListener(graphLayout);
 * c.addPauseListener(graphLayout);
 * c.addExitListener(graphLayout);
 * </pre></code>
 *
 * This will cause the KamadaGraphLayout graphLayout to interrupt its layout
 * whenever stop, pause, or exit is pressed.<p>
 *
 * <b>Note</b> The KamadaGraphLayout is quite slow. It is not meant
 * as a "true" visualization tool, but rather is intended only to
 * provide the modeler with "sense" of the network. Real analysis and
 * visualization should be done in a tool like Pajek.
 *
 * @version $Revision: 1.1 $ $Date: 2005/10/05 20:19:39 $
 * @author Skye Bender-deMoll e-mail skyebend@santafe.edu
 */
public class KamadaGraphLayout extends AbstractLayout {

    //kamada-kawai algorithm vars
    private double springConst = 1;       //K in KK paper (avg. i,j distance?)
    private double minEpsilon = 1;  //target deltaM goal
    private int maxPasses = 5000;   //maximum number of inner loops
    private boolean circleLayout = false; //start each layout from circle?
    private boolean rescaleLayout = true; //wheter to resize and recenter
    
    private int pad = 20;
    private int updates = 0;            //how often to update the screen during layout
    private boolean stop = false;    //flag to break layout
    private String status ="";      //status string for reporting breaks
    private boolean firstLayout = true;
    private boolean breakOut = false;

    private Set nodeList;
    private int width, height;
    private boolean update = true;

    private HashMap locations = new HashMap();

    public KamadaGraphLayout(Graph g, boolean firstLayout, int width, 
			     int height) {
	super(g);
	this.width = width;
	this.height = height;
	this.nodeList = g.getNodes();
	this.firstLayout = firstLayout;
    }


    /**
     * Sets the number of pixels to shrink the effective window by. Java
     * draws object from top left hand corner and this allows objects
     * drawn on the far right to be visible.
     * 
     * @param p the number of pixels to inset by
     */
    public void setPad(int p) {
	pad = p;
    }

    /**
     * Sets how frequently the layout will be redrawn during algorithm
     * convergence.  If updateEveryN is greater than 0,the layout will
     * update the display after every Nth pass through the algorithm.
     * Additional updates make the layout take much longer, especially
     * if there are a large number of nodes to draw.
     *
     * @param updateEveryN the N for determining wether to update on the
     * Nth pass
     */
    public void setUpdateEveryN(int updateEveryN) {
	updates = updateEveryN;
    }

    /**
     * Sets the minimum "spring" energy which the layout attempts to
     * achieve.  Small values mean greater accuracy, and an unknown (but
     * large) amount of additional run time. The algorithm will start
     * with an initially high epsilon value, and keep decreasing it
     * until the layout drops below epsilon, the layout stops improving,
     * or maxPasses is exceeded.  Default is 1, so setting to a higher
     * value will speed up layouts.
     * 
     * @param energy the value for the minimum epsilon
     */
    public void setMinEpsilon(double energy) {
	minEpsilon = energy;
    }

    /**
     * Sets the "springiness" of the imaginary springs connecting the
     * nodes.  Impact on layout is not well understood, seems to control
     * how far nodes are moved each time.  Default is 1.
     *
     * @param spring the value for the spring constant in the algorithm
     */
    public void setSpringConst(double spring) {
	springConst = spring;
    }


    /**
     * Sets the maximum number of passes the inner loop of the KK
     * algorithm will execute.  Lower values mean that the layout is
     * more likely to end before arriving at a minima, but it will break
     * more quickly when stuck in a cycle.  The number of loops needed
     * to a achieve a layout is roughly proportional to the number of
     * nodes (but not in all cases!). Default is 5000
     *
     * @param passes the maximum number of time the inner loop will
     * execute.
     */
    public void setMaxPasses(int passes) {
	maxPasses = passes;
    }

    /**
     * Sets whether circleLayout will be called to arrange nodes before
     * starting each layout.  Should be called before first layout to
     * insure repeatability.  May make layouts take slightly longer.
     * Default is false, but will still circle the first layout unless
     * explicitly set to false.
     *
     * @param eachTime true = always call circleLayout
     */
    public void setCircleLayout(boolean eachTime) {
	circleLayout = eachTime;
	firstLayout = false;
    }

    /**
     * Sets whether the completed layout will be resized to exactly fill
     * the display window.  Setting rescale to false may mean that
     * individual nodes or the entire network may drift off the screen,
     * but it will insure maximum visual continuity between layouts, and
     * minimum layout time.  default is true.
     *
     * @param rescale whether to rescale the layout
     */
    public void setRescaleLayout(boolean rescale) {
	rescaleLayout = rescale;
    }

    /**
     * Positions nodes on layout in a circle for repeatability.  Can be
     * called internally before each layout by setting circleLayout to
     * true.  Useful to insure that nodes have starting coordinates.
     */
    public void circleLayout() {
	int nNodes = nodeList.size();
	// calculate the radius of the circle
	int originX = (int)(width / 2);
	int originY = (int)(height / 2);
	int radius;
	//calc radius
	if (height > width)	radius = (width / 2) - (pad * 2);
	else radius = (height / 2) - (pad * 2);
      
	Iterator it = nodeList.iterator();
	int i = 0;
	while(it.hasNext()) {
	    Node node = (Node)it.next();
	    Coordinates c = (Coordinates)locations.get(node);
	    c.setX(radius * Math.cos(2 * Math.PI * i / nNodes)+ originX);
	    c.setY(radius * Math.sin(2 * Math.PI * i / nNodes)+ originY);
	    i++;
	}
    }

    //set up matrix of spring forces between pairs using K/(d[i][j]^2)
    private DenseDoubleMatrix2D calcKMatrix(DenseDoubleMatrix2D distMatrix,
					    double spring)
    {
	int nNodes = distMatrix.rows();
	DenseDoubleMatrix2D kMatrix = new DenseDoubleMatrix2D(nNodes,nNodes);
	for (int i = 0; i < nNodes; i++) {
	    for (int j = 0; j < nNodes; j++) {
		double distMVal = distMatrix.getQuick(i, j);
		kMatrix.setQuick(i, j, (spring/(distMVal * distMVal)));
	    }
	}
	return kMatrix;
    }

    //set up matrix of desired edge lengths using L*d[i][j]
    private DenseDoubleMatrix2D calcLMatrix(DenseDoubleMatrix2D distMatrix,
					    double optDist)
    {
	int nNodes = distMatrix.rows();
	DenseDoubleMatrix2D lMatrix = new DenseDoubleMatrix2D(nNodes,nNodes);
	for (int i = 0; i < nNodes; i++) {
	    for (int j = 0; j < nNodes; j++) {
		lMatrix.setQuick(i, j,(optDist * distMatrix.getQuick(i,j)));
	    }
	}
	return lMatrix;
    }

    //calculate the diameter of the graph (longest shortest path)
    //requires that path lengths are calc'd first
    private int getDiam(DenseDoubleMatrix2D distMatrix) {
	int nNodes = distMatrix.rows();
	double graphDiam = 0;
	for(int i = 0;  i < nNodes; i++) {
	    for(int j = 0; j < nNodes; j++) {
		graphDiam = Math.max(graphDiam, distMatrix.getQuick(i,j));
	    }
	}
	return (int)graphDiam;
    }


    /**
     * Positions the nodes on the layout according to the results of
     * numerous iterations of the Kamada-Kawai spring-embedding
     * algorithm.  Essentially, the network is modeled as a collection
     * of nodes connected by springs with resting lengths proportional
     * to the length of the shortest path distance between each node
     * pair.  Nodes are normally positioned in a circle, and then each
     * node in sequence is repositioned until the "energy" of all of its
     * springs are minimized to a parameter value epsilon.  The location
     * of the local minima for each node is estimated with iterations of
     * a Newtown-Raphson steepest descent method.  Repositioning ceases
     * when all nodes have energy below epsilon.  In this
     * implementation, epsilon is initialized at a high value, and than
     * decreased as in simulated annealing.  the layout SHOULD stop when
     * a low value (epsilon < 1) is reached or when energies of nodes
     * can now longer be decreased.<p>
     *
     * Note: In the current implementation the layout may not always
     * converge!  however, the maxPasses parameter can be set lower to
     * interrupt cycling layouts.  Also has not been tested/ implemented
     * on weighted graphs. The Kamada-Kawai algorithm was not intended
     * to run on disconnected graphs (graphs with multiple components.
     * The kludgy solution implemented here is to run the algorithm
     * independently on each of the components (of size > 1).  This is
     * somewhat unsatisfactory as the components will often overlap.<p>
     *
     * The KK algorithm is relatively slow, especially on the first
     * round.  However, it often discovers layouts of regularly
     * structured graphs which are "better" and more repeatable than the
     * Fruchmen-Reingold technique.  Implementation of the numerics of
     * the Newton-Raphson method follows Shawn Lorae Stutzman, Auburn
     * University, 12/12/96 <A
     * href="http://mathcs.mta.ca/research/rosebrugh/gdct/javasource.htm">
     * http://mathcs.mta.ca/research/rosebrugh/gdct/javasource.htm</A>
     * <p> Kamada, Tomihisa and Satoru Kawai (1989) "An Algorithm for
     * Drawing Undirected Graphs" <CITE> Information Processing
     * Letters</CITE> 31:7-15
     */
    public void advancePositions() {
	if (done) {
	    return;
	}

	//check that layout should be drawn
	if (update) {

	    Iterator it = nodeList.iterator();
	    while(it.hasNext()) {
		Node workNode = (Node)it.next();
		locations.put(workNode,new Coordinates(workNode.getX(),
						       workNode.getY()));
	    }

	    stop = false;
	    if (circleLayout) {
		//give nodes circular initial coord to begin with
		circleLayout();
	    }

	    if (firstLayout) {
		firstLayout = false;
		circleLayout();
	    }
	
	    //runs kk algorithm on each component individualy
	    Set components = NetUtilities.getComponents(nodeList);
	    Iterator compIter = components.iterator();
      
	    while (compIter.hasNext() && !stop) {
		Set comp = (Set)compIter.next();
		if (comp.size() > 1) runKamadaOn(comp);
	      
	    }
      
	    //rescale node positions to fit in window
	    if (rescaleLayout) 
		Rescale.rescalePositions(nodeList,width,height,locations);
	}
	done = true;
    }

    //RENORM COORDS TO 0-1 range before running ?
    private void runKamadaOn(Set componentNodes) {
	int nNodes = componentNodes.size();
	//sets up the matrix of path distances
	DenseDoubleMatrix2D distMatrix =
	    NetUtilities.getAllShortPathMatrix(componentNodes);
	//sets up kmatrix of forces
	DenseDoubleMatrix2D kMatrix = calcKMatrix(distMatrix, springConst);
	//calc desired distance between nodes
	double optDist = Math.min(width, height) /
	    Math.max(getDiam(distMatrix), 1);
	//sets up lMatrix of distance between nodes pairs
	DenseDoubleMatrix2D lMatrix = calcLMatrix(distMatrix, optDist);
	//arrays for quick acess to node coords
	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
 
	int numEdges = 0;

	Node[] nList = new Node[nNodes];
	Iterator it = nodeList.iterator();
	int w = 0;
	while(it.hasNext()) {
	    Node workNode = (Node)it.next();
	    Coordinates c = (Coordinates)locations.get(workNode);
	    xPos[w] = c.getX();
	    yPos[w] = c.getY();
	    nList[w] = workNode;
	    numEdges += workNode.getOutEdges().size();
	    w++;
	}

	//calc value to start minimization from (should be based on previous?)
	//epsilon = (nNodes * numEdges)/2;
	//figure out the initial stat to compare to at the end
	double initialEnergy = getEnergy(lMatrix, kMatrix, xPos, yPos);
	double epsilon = initialEnergy / nNodes;
	//figure out which node to start moving first
	double deltaM;
	int maxDeltaMIndex = 0;
	double maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
	for (int i = 1; i < nNodes; i++) {
	    deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
	    if (deltaM > maxDeltaM) {
		maxDeltaM = deltaM;
		maxDeltaMIndex = i;
	    }
	}
    
	int passes = 0;
	int subPasses = 0;
	//epsilon minimizing loop
	while ((epsilon > minEpsilon) && !stop) {
	    double previousMaxDeltaM = maxDeltaM + 1;
	    // KAMADA-KAWAI LOOP: while the deltaM of the node with
	    // the largest deltaM  > epsilon..
	    while ((maxDeltaM > epsilon) && ((previousMaxDeltaM - maxDeltaM) > 0.1)
		   && !stop)
		{
	  
		    System.out.print(".");
		    double[] deltas;
		    double moveNodeDeltaM = maxDeltaM;
		    double previousDeltaM = moveNodeDeltaM +1;

		    //KK INNER LOOP while the node with the largest energy > epsilon...
		    while ((moveNodeDeltaM > epsilon) && !stop) {
	    
			//get the deltas which will move node towards the local minima
			deltas = getDeltas(maxDeltaMIndex, lMatrix, kMatrix,
					   xPos, yPos);
			//set coords of node to old coords + changes
			xPos[maxDeltaMIndex] += deltas[0];
			yPos[maxDeltaMIndex] += deltas[1];
			previousDeltaM = moveNodeDeltaM;
			//recalculate the deltaM of the node w/ new vals
			moveNodeDeltaM = getDeltaM(maxDeltaMIndex, lMatrix, kMatrix,
						   xPos, yPos);
			subPasses++;
			if (subPasses > maxPasses) stop = true;
		    }
		    previousDeltaM = maxDeltaM;
		    //recalculate deltaMs and find node with max
		    maxDeltaMIndex = 0;
		    maxDeltaM = getDeltaM(0, lMatrix, kMatrix, xPos, yPos);
		    for (int i = 1; i < nNodes; i++) {
			deltaM = getDeltaM(i, lMatrix, kMatrix, xPos, yPos);
			if (deltaM > maxDeltaM) {
			    maxDeltaM = deltaM;
			    maxDeltaMIndex = i;
			}
		    }
		}
      
	    epsilon -= epsilon / 4;
	}
    
	System.out.print("\n");
	for (int i = 0; i < nNodes; i++) {
	    Node node =
		(Node)nList[i];
	    Coordinates c = (Coordinates)locations.get(node);
	    c.setX(xPos[i]);
	    c.setY(yPos[i]);
	}
    }

    //the bulk of the KK inner loop, estimates location of local minima
    private double[] getDeltas(int i,DenseDoubleMatrix2D lMatrix,
			       DenseDoubleMatrix2D kMatrix, double[] xPos,
			       double[] yPos)
    {
	//solve deltaM partial eqns to figure out new position for node of index i
	// where deltaM is close to 0 (or less then epsilon)
	int nNodes = lMatrix.rows();
	double[] deltas = new double[2];  //holds x and y coords to return
	double dx, dy, dd;
	double deltaX, deltaY;
	double xPartial = 0;
	double yPartial = 0;
	double xxPartial = 0;
	double xyPartial = 0;
	double yxPartial = 0;
	double yyPartial = 0;
	for (int j = 0; j < nNodes; j++) {
	    if (i != j) {
		dx = xPos[i] - xPos[j];
		dy = yPos[i] - yPos[j];
		dd = Math.sqrt(dx*dx+dy*dy);

		double kMatrixVal = kMatrix.getQuick(i, j);
		double lMatrixVal = lMatrix.getQuick(i, j);
		double ddCubed = dd * dd * dd;

	    
		xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
		yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
		xxPartial += kMatrixVal * (1 - lMatrixVal * dy * dy / ddCubed);
		xyPartial += kMatrixVal * (lMatrixVal * dx * dy/ ddCubed);
		yxPartial += kMatrixVal * (lMatrixVal * dy * dx/ ddCubed);
		yyPartial += kMatrixVal * (1 - lMatrixVal *dx * dx / ddCubed);
	    }
	}
    
	//calculate x and y position difference using partials
	deltas[0] = ((-xPartial) * yyPartial - xyPartial * (-yPartial)) /
	    (xxPartial * yyPartial - xyPartial * yxPartial);
	deltas[1] = (xxPartial * (-yPartial) - (-xPartial) * yxPartial) /
	    (xxPartial * yyPartial - xyPartial * yxPartial);
    
	return deltas;
    }

    //returns the energy of i (looping over all other nodes)
    private double getDeltaM(int i, DenseDoubleMatrix2D lMatrix,
			     DenseDoubleMatrix2D kMatrix, double[] xPos,
			     double[] yPos)
    {
	int nNodes = lMatrix.rows();
	double deltaM = 0;
	double xPartial = 0;
	double yPartial = 0;
	double dx, dy, dd;
	for (int j = 0; j < nNodes; j++) {
	    if (i != j) {
		dx = xPos[i] - xPos[j];
		dy = yPos[i] - yPos[j];
		dd = Math.sqrt(dx*dx+dy*dy);
		double kMatrixVal = kMatrix.getQuick(i, j);
		double lMatrixVal = lMatrix.getQuick(i, j);
		xPartial += kMatrixVal * (dx - lMatrixVal * dx / dd);
		yPartial += kMatrixVal * (dy - lMatrixVal * dy / dd);
	    }
	}
	//deltaM = sqrt(xPartial^2+yPartial^2)
	deltaM = Math.sqrt(xPartial * xPartial + yPartial * yPartial);
	return deltaM;
    }

    /**
     * Rescales the x and y coordinates of each node so that the network
     * will maximally fill the display. Will result in some distortion.
     * Called internally if rescale is set to true, will rescale
     * smoothly if animateTransitions is true.
     *
     * @param nodes the nodes to rescale.
     */
    private void rescalePositions(Set nodes) {
    
	int nNodes = nodes.size();
	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
	Node[] nList = new Node[nNodes];
    
	Iterator it = nodes.iterator();
	int i = 0;
	while(it.hasNext()) {
	    Node workNode = (Node)it.next();
	    xPos[i] = (double)workNode.getX();
	    yPos[i] = (double)workNode.getY();
	    nList[i] = workNode;
	    i++;
	}
	//find largest coords
	double xMax = xPos[0];
	double yMax = yPos[0];
	double xMin = xPos[0];
	double yMin = yPos[0];
	for (i = 1; i < nNodes; i++) {
	    xMax = Math.max(xMax, xPos[i]);
	    yMax = Math.max(yMax, yPos[i]);
	    xMin = Math.min(xMin, xPos[i]);
	    yMin = Math.min(yMin, yPos[i]);
	}
	//rescale coords of nodes to fit inside frame
	for (i = 0; i < nNodes; i++) {
	    xPos[i] = ((xPos[i] - xMin) / (xMax - xMin)) * (width - pad);
	    yPos[i] = ((yPos[i] - yMin) / (yMax - yMin)) * (height - pad);
	}
	//animate the transiton
	int numSteps = 5;
	//set positions of nodes to coord array vals
	for (i = 0; i < nNodes; i++) {
	    Node node = (Node)nList[i];
	    Coordinates c = (Coordinates)locations.get(node);
	    c.setX(xPos[i]);
	    c.setY(yPos[i]);
	}
    }


    private double getEnergy(DenseDoubleMatrix2D lMatrix,
			     DenseDoubleMatrix2D kMatrix, double[] xPos,
			     double[] yPos)
    {
	int nNodes = lMatrix.rows();
	double energy = 0;
	double dx, dy,lij;
	int limit =  nNodes - 1;
	//for all pairs..
	for(int i = 0; i < limit; i++) {
	    for(int j = i+1; j<nNodes; j++) {
		dx = xPos[i] - xPos[j];
		dy = yPos[i] - yPos[j];
		lij = lMatrix.getQuick(i,j);
		energy += 0.5 * kMatrix.getQuick(i,j) * (dx * dx + dy * dy +
							 lij* lij - 2 * lij*
							 Math.sqrt(dx*dx+dy*dy));
	    }
	}
	return energy;
    }
  
    /**
     * Implements the ActionListener interface. Whenever this is called the
     * layout will be interrupted as soon as possible.
     */
    public void actionPerformed(ActionEvent evt) {
	stop = true;
    } 
    
    /**
     * Gets the height of the area on which to layout the graph.
     */
    public int getHeight() {
	return height;
    }
    
    /**
     * Gets the width of the area on which to layout the graph.
     */
    public int getWidth() {
	return width;
    }
    
    public void setUpdate(boolean doUpdate) {
	update = doUpdate;
    }
    
    public double getX(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getX());
    }

    public double getY(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getY());
    }

    public Coordinates getCoordinates(Node v) {
	return((Coordinates)locations.get(v));
    }

    public boolean done = false;

    public boolean incrementsAreDone() {
	return(done);
    }

    public void initialize_local_vertex(edu.uci.ics.jung.graph.Vertex v) {
    }

    public void initialize_local() {
    }

    public boolean isIncremental() {
	return(false);
    }    
}
