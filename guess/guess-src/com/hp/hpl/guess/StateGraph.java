package com.hp.hpl.guess;

import java.awt.*;
import java.sql.*;
import java.util.*;
import com.hp.hpl.guess.piccolo.*;
import com.hp.hpl.guess.tg.*;
import com.hp.hpl.guess.prefuse.*;
import com.hp.hpl.guess.storage.*;
import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.layout.*;
import com.hp.hpl.guess.util.*;
import com.hp.hpl.guess.io.*;
import javax.swing.JOptionPane;

import org.apache.commons.collections.*;
import edu.uci.ics.jung.algorithms.cluster.*;
import edu.uci.ics.jung.algorithms.importance.*;
import edu.uci.ics.jung.algorithms.transformation.*;
import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.utils.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.contrib.*;
import edu.uci.ics.jung.random.generators.*;
import edu.uci.ics.jung.graph.ArchetypeGraph;
import edu.uci.ics.jung.visualization.Coordinates;

import org.python.core.*;
import org.python.util.*;

import com.hp.hpl.guess.ui.StatusBar;

/**
 *
 */
public class StateGraph extends Graph {

    // just so we can compile
    NodeSchema nodeSchema = null;
    EdgeSchema edgeSchema = null;
    private InterpreterAbstraction interp;
    private FrameListener display;


    /**
     * create a new graph.  Must be tied to a display object
     * right now as well as an interpreter instance.  Will
     * probably relax that in the future
     * @param display something we render to
     * @param interp the interpreter
     */
    public StateGraph(InterpreterAbstraction interp)
    {
	super(null,interp);
    }
    
    /**
     * used to load nodes from the database into the graph
     * @param n the node we're adding
     * @return the added node (possibly a different object)
     */
    public Node addNode(Node n)
    {
	nameToNode.put(n.getName(),n);
	super.lastMod = System.currentTimeMillis() - 1; // invalidate cache
	return (Node)addVertex(n);
    }
    
    /**
     * user's API for adding a new node to the graph and database
     * @param name the new node name
     * @return a new node object
     *
     */
    public Node addNode(String name)
    {
	//create new node with given name and default values
	Node node = new Node(name);
	
	//add node to database
	//StorageFactory.getSL().addNode(node,state);
	
	//add node to graph
	addNode(node);
	
	//fill in default values for this node.
	Iterator fields = nodeSchema.fields();
	while (fields.hasNext())
	    {
		Field field = (Field)fields.next();
		String fieldName = field.getName();
		if (!fieldName.equals("name"))
		    node.__setattr__(fieldName, field.getDefault());
	    }
	return node;
    }
    
    /**
     * used to load edges from the database into the graph 
     *
     * @param e the edge to add 
     * @return the created edge
     */
    public Edge addEdge(Edge e)
    {
	//System.out.println("***adding: " + e);
	//get available ID from database
	if (StorageFactory.getSL().containsEdge(e)) {
	    addEdgeNoCheck(e);
	    return(e);
	} else {
	    //System.out.println("magic");
	    StorageFactory.getSL().addEdge(e);
	    addEdgeNoCheck(e);
	    //fill in default values for this edge.
	    Iterator fields = edgeSchema.fields();
	    while (fields.hasNext())
		{
		    Field field = (Field)fields.next();
		    String name = field.getName();
		    if (!name.equals("__edgeid") && 
			!name.equals("node1") && 
			!name.equals("node2") &&
			!name.equals("directed"))
			e.__setattr__(name, field.getDefault());
		}
	    //System.out.println("magic done");
	}
	return(e);
    }
    

    /**
     * use this if you know the storage object already contains
     * the edge.  Shortcut for speeding things up
     */
    public Edge addEdgeNoCheck(Edge e)
    {
	if (e instanceof DirectedEdge)
	    containsDirected = true;

	//System.out.println(e.getID());
	super.addEdge(e);
	setIdToEdge(e);
	VisFactory.getFactory().add(e);
	lastMod = System.currentTimeMillis() - 1; // invalidate cache
	updateDegrees(e);
	return e;
    }

    private void updateDegrees(Edge e) {
	Node n1 = e.getNode1();
	n1.updateDegrees();
	Node n2 = e.getNode2();
	if (n2 != n1) 
	    n2.updateDegrees();
    }

    /**
     * user's API for adding a new directed edge to the graph and database
     * @param source source node
     * @param dest destinatin node
     * @return a new DirectedEdge between source and dest
     *
     */
    public DirectedEdge addDirectedEdge(Node source, Node dest)
    {
	containsDirected = true;

	//get available ID from database
	int id = StorageFactory.getSL().createDirectedEdge(source, dest);
	
	//create new edge
	DirectedEdge edge = new DirectedEdge(id, source, dest);
	
	//add new edge to graph
	addEdgeNoCheck(edge);
	
	//fill in default values for this edge.
	Iterator fields = edgeSchema.fields();
	while (fields.hasNext())
	    {
		Field field = (Field)fields.next();
		String name = field.getName();
		if (!name.equals("__edgeid") && !name.equals("node1") && !name.equals("node2") &&
		    !name.equals("directed"))
		    edge.__setattr__(name, field.getDefault());
	    }

	return edge;
    }

    private boolean containsDirected = false;

    /**
     * does this graph contain any directed edges? Shortcut so 
     * we can decide to draw arrows
     * @return true or false
     */
    protected boolean containsDirected() {
	return(containsDirected);
    }

    //user's API for adding a new undirected edge to the graph and database
    /**
     *
     */
    public UndirectedEdge addUndirectedEdge(Node node1, Node node2)
    {
	//System.out.println(node1 + " " + node2 + " created");
	//get available ID from database
	int id = StorageFactory.getSL().createUndirectedEdge(node1, node2);
	
	//create new edge
	UndirectedEdge edge = new UndirectedEdge(id, node1, node2);
	
	//add new edge to graph
	addEdgeNoCheck(edge);
	
	//fill in default values for this edge.
	Iterator fields = edgeSchema.fields();
	while (fields.hasNext())
	    {
		Field field = (Field)fields.next();
		String name = field.getName();
		if (!name.equals("__edgeid") && !name.equals("node1") && !name.equals("node2") &&
		    !name.equals("directed"))
		    edge.__setattr__(name, field.getDefault());
	    }
	//	System.out.println("\t"+edge.__getattr__("visible"));
	return edge;
    }
    
    /**
     * user's API shortcut for adding a new undirected edge.
     *
     */
    public UndirectedEdge addEdge(Node node1, Node node2)
    {
	return addUndirectedEdge(node1, node2);
    }
    
    /**
     * remove a node from the graph
     * @param n the node to remove
     *
     */
    public Set removeNode(Node n)
    {
	//System.out.println("foo: " + n);
	//first, must remove all incident edges, and not rely on Jung
	//to do this.
	Iterator edges = n.getIncidentEdges().iterator();
	while (edges.hasNext())
	    removeEdge((Edge)edges.next());
	
	super.removeVertex(n);
	nameToNode.remove(n.getName());
	StorageFactory.getSL().remove(n);
	VisFactory.getFactory().remove(n);
	lastMod = System.currentTimeMillis() - 1; // invalidate cache
	return(null);
    }

    /**
     * remove an edge from the graph
     * @param e the edge to remove
     *
     */
    public Set removeEdge(Edge e)
    {
	super.removeEdge(e);
	idToEdge.remove(new Integer(e.getID()));
	StorageFactory.getSL().remove(e);
	VisFactory.getFactory().remove(e);
	lastMod = System.currentTimeMillis() - 1; // invalidate cache
	updateDegrees(e);
	return(null);
    }
    
    
    /**
     * remove nodes and edges from the graph
     * @param seq the nodes + edges
     *
     */
    public Set remove(PySequence seq)
    {
	for (int i = 0; i < seq.__len__(); i++)
	    {
		GraphElement element =
		    (GraphElement)((PyInstance)seq.__finditem__(i)).__tojava__(GraphElement.class);
		
		if (element instanceof Node)
		    removeNode((Node)element);
		else if (element instanceof Edge)
		    removeEdge((Edge)element);
		else
		    throw new Error("Invalid graph element type:  " + element.getClass());
	    }
	lastMod = System.currentTimeMillis() - 1; // invalidate cache
	return(null);
    }
    

    /**
     * add nodes and edges from the graph
     * @param seq the nodes + edges
     *
     */
    public void add(PySequence seq)
    {
	for (int i = 0; i < seq.__len__(); i++)
	    {
		GraphElement element =
		    (GraphElement)((PyInstance)seq.__finditem__(i)).__tojava__(GraphElement.class);
		
		if (element instanceof Node)
		    addNode((Node)element);
		else if (element instanceof Edge)
		    addEdge((Edge)element);
		else
		    throw new Error("Invalid graph element type:  " + element.getClass());
	    }
	lastMod = System.currentTimeMillis() - 1; // invalidate cache
    }

    /**
     * @return all the nodes in the graph
     */
    public Set getNodes()
    {
	return getVertices();
    }
    
    /**
     * @return figures out if you have a Color object,
     * Python object, or a string object, automagically converts
     */
    public static Color toColor(Object object)
    {
	if (object instanceof Color)
	    return (Color)object;
	if (object instanceof String)
	    return Colors.getColor((String)object, Color.red);
	if (object instanceof PyJavaInstance)
	    return (Color)((PyInstance)object).__tojava__(Color.class);

	throw new Error("Invalid color class:  " + object.getClass());
    }

    /**
     * @return figures out if you have a Double object,
     * Python object and automatically converts
     */
    public static double toDouble(Object object)
    {
	if (object instanceof PyFloat)
	    return ((PyFloat)object).getValue();
	if (object instanceof PyInteger)
	    return ((PyInteger)object).getValue();

	throw new Error("Invalid double class:  " + object.getClass());
    }
    
   
    /**
     * moves edges so they don't overlap
     *
     */
    public void readjustEdges() {
	expandOverlapping();
    }

    public void compressOverlapping() {
	EdgeAdjustment.compressOverlapping(this);
    }

    public void expandOverlapping() {
	EdgeAdjustment.expandOverlapping(this);
    }

    /**
     * Kamada-Kawai, puts nodes in 1000 x 1000 square
     *
     */
    public void kkLayout()
    {
	kkLayout(1000,1000);
    }
    
    /**
     * Sugiyama
     *
     */
    public void sugiyamaLayout() {
	sugiyamaLayout(false);
    }

    /**
     * Sugiyama
     *
     */
    public void sugiyamaLayout(boolean bends) {
	new SugiyamaLayout(this,bends);
	if (display instanceof GFrame) {
	    ((GFrame)display).centerFast();
	} else {
	    display.center();
	}
    }

    /**
     * Sugiyama
     *
     */
    public void sugiyamaLayout2() {
	new Sugiyama2(this);
	if (display instanceof GFrame) {
	    ((GFrame)display).centerFast();
	} else {
	    display.center();
	}
    }

    /**
     * DAG
     *
     */
    public void dagLayout() {
	layout(new DAGLayoutWrapper(this));	
    }


    /**
     * Fruchterman-Rheingold, puts nodes in 1000 x 1000 square
     *
     */
    public void frLayout()
    {
	frLayout(1000,1000);
    }
    
    /**
     * Multidimensional scaling layout based on edge weight
     *
     */
    public void mdsLayout()
    {
	layout(new SMDS(this));
    }
    

    /**
     * moves the nodes to a new origin
     * @param newX
     * @param newY
     *
     */
    public void moveLayout(int newX, int newY)
    {
	layout(new Move(this,(double)newX,(double)newY));
    }

    public void moveLayout(double newX, double newY)
    {
	layout(new Move(this,(double)newX,(double)newY));
    }

    /**
     * moves the nodes to a new origin
     * @param newX
     * @param newY
     *
     */
    public void translateLayout(int newX, int newY)
    {
	layout(new Translate(this,(double)newX,(double)newY));
    }

    public void translateLayout(double newX, double newY)
    {
	layout(new Translate(this,(double)newX,(double)newY));
    }


    /**
     * rescales nodes to fit into width x height
     * @param width the rescale width
     * @param height the rescale height
     *
     */
    public void rescaleLayout(int width, int height)
    {
	layout(new Rescale(this,width,height));
    }

    /**
     * rescales the layout to %percent
     * @param percent the percent to shrink/grow
     *
     */
    public void rescaleLayout(double percent)
    {
	layout(new Rescale(this,percent));
    }

    /**
     * rescales the layout to %xpercent X %ypercent
     * @param xpercent the percent to shrink/grow
     * @param ypercent the percent to shrink/grow
     */
    public void rescaleLayout(double xpercent,double ypercent)
    {
	layout(new Rescale(this,xpercent,ypercent));
    }

    /**
     * places all nodes in a growing radius around center
     * @param center the node to put in the center
     *
     */
    public void radialLayout(Node center)
    {
	layout(new Radial(this,center));
    }

    public void radialLayout(Node center, PySequence seq) {
	
	HashSet hs = new HashSet();
	
	for (int i = 0; i < seq.__len__(); i++)
	    {
		GraphElement element =
		    (GraphElement)((PyInstance)seq.__finditem__(i)).__tojava__(GraphElement.class);
		
		if (element instanceof Edge)
		    hs.add(element);
		else
		    throw new Error("Invalid graph element type:  " + element.getClass());
	    }

	layout(new Radial(this,center,hs));
    }

    /**
     * randomly places nodes in a 1000 x 1000 grid
     *
     */
    public void randomLayout() {
	randomLayout(1000,1000);
    }

    /**
     * randomly places nodes in a width x height grid
     * @param width width
     * @param height height
     *
     */
    public void randomLayout(int width, int height) {
	layout(new RandomGraphLayout(this,width,height));
    }

    /**
     * the JUNG Fructerman-Rheingold Layout
     *
     */
    public void jfrLayout() {
	layout(new FRLayout(this));
    }

    /**
     * the JUNG Kamada-Kawai (regular)
     *
     */
    public void jkkLayout1() {
	layout(new KKLayout(this));
    }


    /**
     * the JUNG Kamada-Kawai (int)
     *
     */
    public void jkkLayout2() {
	layout(new KKLayoutInt(this));
    }

    /**
     * the JUNG Kamada-Kawai (regular)
     *
     */
    public void weightedKKLayout() {
	layout(new WeightedKK(this));
    }


    /**
     * Fruchterman-Rheingold into a width x height grid
     * @param width
     * @param height
     *
     */
    public void frLayout(int width, int height)
    {
	layout(new FruchGraphLayout(this,true,width,height));
    }
    
    /**
     * Kamada-Kawai into a width x height grid
     * @param width
     * @param height
     *
     */
    public void kkLayout(int width, int height)
    {
	layout(new KamadaGraphLayout(this,true,width,height));
    }

    /**
     * physics layout
     * @param gather merges connected nodes first (not very useful)
     * @param max loops max times
     */
    public void physicsLayout(boolean gather, int max)
    {
	layout(new Physics(this,gather),max);
    }

    /**
     * physics layout, asks every 30 seconds if you want to keep running
     *
     */
    public void physicsLayout()
    {
	layout(new Physics(this,false));
    }

    /**
     * run physics layout for max steps
     * @param max the number of steps
     *
     */
    public void physicsLayout(int max)
    {
	physicsLayout(false,max);
    }

    /**
     * the JUNG Spring Layout (seems a little broken).  Asks
     * every 30 seconds if you want to keep running
     *
     */
    public void jSpringLayout()
    {
	layout(new SpringLayout(this));
    }

    /**
     * run the JUNG spring layout for max iterations
     * @param max the number of iterations
     *
     */
    public void jSpringLayout(int max)
    {
	layout(new SpringLayout(this),max);
    }

    /**
     * places all nodes in a circle
     *
     */
    public void circleLayout()
    {
	layout(new CircleLayout(this));
    }

    /**
     * packs all nodes
     *
     */
    public void binPackLayout()
    {
	binPackLayout(true);
    }

    /**
     * packs all nodes
     *
     */
    public void binPackLayout(boolean rescale)
    {
	layout(new BinPack(this,rescale));
    }

    /**
     * places all nodes in a circle with node c in the middle
     * and its neighbors around that center with a radius
     * proportional to field f (all nodes that are not neighbors
     * of c will be left as is)
     * @param f the field to use to calc the radius
     * @param c the node to put in the center
     *
     */
    public void circleLayout(Field f, 
			     Node c)
    {
	layout(new CircularConstrained(this,c,f));
    }

    /**
     * places all nodes in a circle with node c in the middle
     * and its neighbors around that center with a radius
     * proportional to field f (all nodes that are not neighbors
     * of c will be left as is)
     * @param f the field to use to calc the radius
     * @param c the node to put in the center
     * @param xorigin the x location for the center node
     * @param yorigin the y location for the center node
     *
     */
    public void circleLayout(Field f, 
			     Node c,
			     double xorigin,
			     double yorigin)
    {
	layout(new CircularConstrained(this,c,f,xorigin,yorigin));
    }

    /**
     * spring layout, runs for 30 seconds and asks if you want
     * to continue
     *
     */
    public void springLayout() {
	layout(new Spring(this));
    }

    /**
     * spring layout for max iterations
     * @param max number of iterations
     *
     */
    public void springLayout(int max) {
	layout(new Spring(this), max);
    }

    /**
     * spring layout, runs for 30 seconds and asks if you want
     * to continue
     *
     */
    public void weightedSpringLayout(int min, int max) {
	layout(new WeightedSpring(this,min,max));
    }

    /**
     * spring layout for max iterations
     * @param max number of iterations
     *
     */
    public void weightedSpringLayout(int minL, int maxL, int max) {
	layout(new WeightedSpring(this,minL,maxL), max);
    }

    /**
     * GEM Layout
     *
     */
    public void gemLayout()
    {
	layout(new GEM(this));
    }

    /**
     * ISOM Layout, asks every 30 seconds if you want to continue
     *
     */
    public void isomLayout() {
	layout(new ISOMLayout(this));
    }

    /**
     * ISOM Layout for max steps
     * @param max the number of iterations
     *
     */
    public void isomLayout(int max) {
	layout(new ISOMLayout(this),max);
    }

    /**
     * basic layout command, runs until completes or 30 seconds
     * depending on if the layout is iterative
     * @param lay the layout to run
     */
    public void layout(Layout lay)
    {
	layout(lay,Integer.MAX_VALUE);
    }

    private boolean sync = false;

    /**
     * should layouts run in their own threads?
     *
     */
    public void setSynchronous(boolean state) {
	this.sync = state;
    }

    private boolean centerAfter = true;

    /**
     * should we center after doing a layout
     * disable this to control it yourself
     *
     */
    public void centerAfterLayout(boolean state) {
	this.centerAfter = state;
    }

    /**
     * runs the layout for some number of iterations (mi).  If 
     * mi == Integer.MAX_VALUE this will run until the layout
     * completes or if it is iterative it will ask every 30 seconds
     * if you want to continue
     * @param lay the layout to run
     * @param mi the maximum iterations
     */
    public void layout(Layout lay, int mi)
    {
	final Graph gt = this;
	final Layout layout = lay;
	StatusBar.runProgressBar(true);
	final int maxIters = mi;

	final boolean ca = centerAfter;

	Thread thrd = new Thread(new Runnable() {
		public void run() {
		    interp.freeze(true);

		    GoneIn30 g30 = null;
		    try {

			layout.initialize(new Dimension(1000, 1000));
			if (layout.isIncremental()) {
			    int incCounter = 0;
			    long curTime = System.currentTimeMillis();
			    while (!layout.incrementsAreDone()) {
				incCounter++;
				//System.out.println(layout.getStatus());
				layout.advancePositions();
				// prevent runaway layouts
				long test = System.currentTimeMillis()-
				    curTime;
				if (maxIters == Integer.MAX_VALUE) {
				    // user launched this on a
				    // "infinite" run, so we're
				    // going to ask them every
				    // 30 seconds if they want
				    // to keep going
				    if (incCounter == 1) {
					g30 = GoneIn30.getWindow();
					g30.start();
				    }
				    if ((System.currentTimeMillis() -
					 curTime) > 30000) {
					update();
					int yn = 
					    JOptionPane.showConfirmDialog(null, 
									  "Ran " +
									  incCounter + " loops, Continue?","Continue?",JOptionPane.YES_NO_OPTION);
					if (yn == JOptionPane.NO_OPTION) {
					    if (g30 != null) {
						g30.stopTimer();
					    }
					    break;
					} else {
					    g30 = 
						GoneIn30.getWindow();
					    g30.start();
					}
					curTime = System.currentTimeMillis();
				    }
				} else {
				    if (incCounter >= maxIters) {
					break;
				    }
				    if ((System.currentTimeMillis() -
					 curTime) > 30000) {
					StatusBar.setStatus("Ran " + incCounter + " loops");
					System.out.println("Ran " + incCounter + " loops");
					curTime = System.currentTimeMillis();
				    }
				}
			    }
			} else {
				layout.advancePositions();
			}
			
			update();
		    } catch (Exception e) {
			ExceptionWindow.getExceptionWindow(e);
		    }
		    if (g30 != null) {
			g30.stopTimer();
		    }
		    StatusBar.runProgressBar(false);
		    interp.freeze(false);
		}

		public void update() {
		    try {
			Iterator nodes = getNodes().iterator();
			while (nodes.hasNext())
			    {
				Node node = (Node)nodes.next();
				if (!((Boolean)node.__getattr__("fixed")).booleanValue())
				    {
					
					node.__setattr__("x", 
							 new Double(layout.getX((Node)node)));
					node.__setattr__("y", 
							 new Double(layout.getY((Node)node)));
				    } 
			    }
		    } catch (Exception ex) {
			ExceptionWindow.getExceptionWindow(ex);
		    }
		    if (ca) {
			if (display instanceof GFrame) {
			    ((GFrame)display).centerFast();
			} else {
			    display.center();
			}
		    }
		}
	    });
	if (!sync) {
	    thrd.start();
	} else {
	    thrd.run();
	}
    }

    /**
     * do Bi-Component Clustering
     * @return a set of sets
     *
     */
    public Set biComponentClusters()
    {
	//need to convert to an undirected graph.
	UndirectedGraph tempGraph = DirectionTransformer.toUndirected(this);

	Set tempClusters = clusters(tempGraph, new BicomponentClusterer());

	Set clusters = new HashSet();

	Iterator it = tempClusters.iterator();
	while (it.hasNext())
	    {
		Set tempCluster = (Set)it.next();

		Set cluster = new HashSet();

		Iterator tempNodes = tempCluster.iterator();
		while (tempNodes.hasNext())
		    {
			Node tempNode = (Node)tempNodes.next();
			cluster.add(tempNode.getEqualVertex(this));
		    }

		clusters.add(cluster);
	    }

	return clusters;
    }

    /**
     * do Edge betweenness clustering
     * @param numEdgesToRemove edges to remove
     * @return a set of sets
     *
     */
    public Set edgeBetweennessClusters(int numEdgesToRemove)
    {
	return clusters(this, new EdgeBetweennessClusterer(numEdgesToRemove));
    }

    /**
     * do Voltage clustering
     * @param num_candidates number of candidate clusters to create
     * @param rank_iterations number of iterations to run voltageranker
     * @param rank_convergence 
     * @param cluster_iterations number of iterations to run kmeans
     * @param cluster_convergence the convergence value for the KMeans
     */
    //    public Set voltageClusters(int num_candidates,
    //		       int rank_iterations,
    //		       double rank_convergence,
    //		       int cluster_iterations,
    //		       double cluster_convergence) {
    //	Voltage
    //return clusters(this, new VoltageClusterer(num_candidates,rank_iterations,rank_convergence,cluster_iterations,cluster_convergence));
    //    }

    /**
     * do weak component clustering
     * @return a set of sets
     *
     */
    public Set weakComponentClusters()
    {
	return clusters(this, new WeakComponentClusterer());
    }
    
    /**
     * doesn't do anything right now
     */
    public Set exactFlowCommunity(int cohesion) {
	return(null);
    }
    
    /**
     * meta clustering algorithm, takes a clustering algorithm and
     * a graph and returns a set of sets
     * @param graph the graph to analyze
     * @param clusterer the clustering algorithm to use
     * @return a set of sets
     */
    public static Set clusters(edu.uci.ics.jung.graph.Graph graph, 
			       GraphClusterer clusterer)
    {
	ClusterSet clusterSet = clusterer.extract(graph);
	
	Set clusters = new HashSet();
	
	Iterator it = clusterSet.iterator();
	while (it.hasNext())
	    clusters.add(it.next());
	
	return clusters;
    }
    
    /**
     * needed for NumberEdgeValue interface
     */
    public void setNumber(ArchetypeEdge edge, Number number)
    {
	throw new Error("setNumber not yet supported");
    }

    /**
     * for NumberEdgeValue interface, returns the weight of the given edge.
     */
    public Number getNumber(ArchetypeEdge e)
    {
	return (Number)((Edge)e).__getattr__("weight");
    }
    
    /**
     * computes the ranking for nodes or edges and deposits the results
     * based on guessKey
     * @param centrality the ranker
     * @param guessKey the place to put the results
     */
    public void computeAbstractRanker(AbstractRanker centrality,
				      String guessKey) {
	//first, add node and edge fields for betweenness if necessary.
	if (edgeSchema.getField(guessKey) == null)
	    addEdgeField(guessKey, Types.DOUBLE, new Double(0.0));

	if (nodeSchema.getField(guessKey) == null)
	    addNodeField(guessKey, Types.DOUBLE, new Double(0.0));
	
	
	centrality.setRemoveRankScoresOnFinalize(false);
	centrality.evaluate();
	
	String key = centrality.getRankScoreKey();
	
	Iterator nodes = getNodes().iterator();
	while (nodes.hasNext())
	    {
		Node node = (Node)nodes.next();
		double value = 
		    ((MutableDouble)node.getUserDatum(key)).doubleValue();
		node.__setattr__(guessKey, new Double(value));
		
		//System.out.println("Alg has computed a value of: " + value);
	    }
	
	Iterator edges = getEdges().iterator();
	while (edges.hasNext())
	    {
		Edge edge = (Edge)edges.next();
		double value = 
		    ((MutableDouble)edge.getUserDatum(key)).doubleValue();
		edge.__setattr__(guessKey, new Double(value));
	    }	
    }
    
    public void computeBetweennessCentrality()
    {
	Field f1 = nodeSchema.getField("betweenness");
	Field f2 = edgeSchema.getField("betweenness");
	if ((f1 == null) || (f2 == null) || 
	    (f1.needsUpdate(this)) ||
	    (f2.needsUpdate(this))) {
	    BetweennessCentrality centrality = new BetweennessCentrality(this);
	    computeAbstractRanker(centrality,"betweenness");
	    nodeSchema.getField("betweenness").update();
	    edgeSchema.getField("betweenness").update();
	}
    }
    
    
    public void computePageRank(double bias)
    {
	Field f1 = nodeSchema.getField("pagerank");
	Field f2 = edgeSchema.getField("pagerank");
	if ((f1 == null) || (f2 == null) || 
	    (f1.needsUpdate(this)) ||
	    (f2.needsUpdate(this))) {
	    DirectedGraph tempGraph = DirectionTransformer.toDirected(this);
	    PageRank centrality = new PageRank(tempGraph,bias);
	    computeAbstractRanker(centrality,"pagerank");
	    nodeSchema.getField("pagerank").update();
	    edgeSchema.getField("pagerank").update();
	}
    }

    public void computeDegreeDistributionRank() {
	Field f1 = nodeSchema.getField("degrank");
	Field f2 = edgeSchema.getField("degrank");
	if ((f1 == null) || (f2 == null) || 
	    (f1.needsUpdate(this)) ||
	    (f2.needsUpdate(this))) {
	    DegreeDistributionRanker centrality = 
		new DegreeDistributionRanker(this);
	    computeAbstractRanker(centrality,"degrank");
	    nodeSchema.getField("degrank").update();
	    edgeSchema.getField("degrank").update();
	}
    }

    public void computeHITS() {
	Field f1 = nodeSchema.getField("hits");
	Field f2 = edgeSchema.getField("hits");
	if ((f1 == null) || (f2 == null) || 
	    (f1.needsUpdate(this)) ||
	    (f2.needsUpdate(this))) {
	    HITS centrality = new HITS(this);
	    computeAbstractRanker(centrality,"hits");
	    nodeSchema.getField("hits").update();
	    edgeSchema.getField("hits").update();
	}
    }

    public void computeRandomWalkBetweenness() {
	Field f1 = nodeSchema.getField("rwbetweenness");
	Field f2 = edgeSchema.getField("rwbetweenness");
	if ((f1 == null) || (f2 == null) || 
	    (f1.needsUpdate(this)) ||
	    (f2.needsUpdate(this))) {
	    UndirectedGraph tempGraph = 
		DirectionTransformer.toUndirected(this);
	    RandomWalkBetweenness centrality = 
		new RandomWalkBetweenness(tempGraph);
	    computeAbstractRanker(centrality,"rwbetweenness");
	    nodeSchema.getField("rwbetweenness").update();
	    edgeSchema.getField("rwbetweenness").update();
	}
    }

    public void computeDegrees() {
	Field in = nodeSchema.getField("indegree");
	Field out = nodeSchema.getField("outdegree");
	Field deg = nodeSchema.getField("totaldegree");
	if ((in == null) ||
	    (out == null) ||
	    (deg == null) ||
	    (in.needsUpdate(this)) ||
	    (out.needsUpdate(this)) ||
	    (deg.needsUpdate(this))) {
	    
	    if (nodeSchema.getField("indegree") == null)
		addNodeField("indegree", Types.INTEGER, new Integer(0));
	    
	    if (nodeSchema.getField("outdegree") == null)
		addNodeField("outdegree", Types.INTEGER, new Integer(0));

	    if (nodeSchema.getField("totaldegree") == null)
		addNodeField("totaldegree", Types.INTEGER, new Integer(0));

	    Iterator nodes = getNodes().iterator();
	    while(nodes.hasNext()) {
		Node n = (Node)nodes.next();
		n.updateDegrees();
	    }
	}

    }

    /**
     *
     */
    public Field addNodeField(String fieldName, int sqlType, 
			     Object defaultValue)
    {
	Field field = new Field(this, fieldName, Field.NODE, 
				sqlType, defaultValue);
	nodeSchema.addFieldToSL(field);
	
	interp.setImmutable(fieldName, field);
	return(field);
    }

    /**
     *
     */
    public Field addEdgeField(String fieldName, int sqlType, 
			     Object defaultValue)
    {
	Field field = new Field(this, fieldName, Field.EDGE,
				sqlType, defaultValue);
	edgeSchema.addFieldToSL(field);
	
	interp.setImmutable(fieldName, field);
	return(field);
    }

    /**
     *
     */
    public NodeSchema getNodeSchema()
    {
	return nodeSchema;
    }

    /**
     *
     */
    public EdgeSchema getEdgeSchema()
    {
	return edgeSchema;
    }

    /**
     * 
     */
    public Collection sortBy(PySequence seq, Field field) {
	ArrayList list = new ArrayList();
	for (int i = 0; i < seq.__len__(); i++) {
	    GraphElement ge =
		(GraphElement)((PyInstance)seq.__finditem__(i)).__tojava__(GraphElement.class);
	    list.add(new SortableGraphElement(ge,
					      (Comparable)ge.__getattr__(field.getName())));
	}
	Collections.sort(list);
	Vector hs = new Vector();
	Iterator it = list.iterator();
	while(it.hasNext()) {
	    hs.add(((SortableGraphElement)it.next()).el);
	}
	return(hs);
    }

    /**
     *
     */
    public Collection sortBy(Field field) {

	Iterator it = null;

	if (field.getType() == Field.NODE) {
	    it = getNodes().iterator();
	} else if (field.getType() == Field.EDGE) {
	    it = getEdges().iterator();
	} else {
	    throw new Error("invalid Field type");
	}
	ArrayList list = new ArrayList();
	while(it.hasNext()) {
	    GraphElement ge = (GraphElement)it.next();
	    list.add(new SortableGraphElement(ge,
					      (Comparable)ge.__getattr__(field.getName())));
	}
	Collections.sort(list);
	Vector hs = new Vector();
	it = list.iterator();
	while(it.hasNext()) {
	    hs.add(((SortableGraphElement)it.next()).el);
	}
	return(hs);
    }

    class SortableGraphElement implements Comparable {
	
	public Comparable key = null;
	
	public GraphElement el = null;

	public SortableGraphElement(GraphElement el, Comparable key) {
	    this.key = key;
	    this.el = el;
	}
	
	public int compareTo(Object o) {
	    return(this.key.compareTo(((SortableGraphElement)o).key));
	}
    }

    /**
     *
     */
    public Collection groupBy(PySequence seq, Field field) {
	Hashtable map = new Hashtable();
	for (int i = 0; i < seq.__len__(); i++) {
	    GraphElement ge =
		(GraphElement)((PyInstance)seq.__finditem__(i)).__tojava__(GraphElement.class);
	    Object attrib = ge.__getattr__(field.getName());
	    if (!map.containsKey(attrib)) {
		map.put(attrib,new SortableHashSet((Comparable)attrib));
	    }
	    ((HashSet)map.get(attrib)).add(ge);
	}
	return(map.values());
    }

    /**
     *
     */
    public Collection groupBy(Field field) {
	if (field.getType() == Field.NODE) {
	    return(groupNodesBy(field.getName()));
	} else if (field.getType() == Field.EDGE) {
	    return(groupEdgesBy(field.getName()));
	} else {
	    throw new Error("invalid Field type");
	}
    }

    /**
     *
     */
    public Collection groupAndSortBy(Field field) {
	if (field.getType() == Field.NODE) {
	    return(groupAndSortNodesBy(field.getName()));
	} else if (field.getType() == Field.EDGE) {
	    return(groupAndSortEdgesBy(field.getName()));
	} else {
	    throw new Error("invalid Field type");
	}
    }
    
    /**
     *
     */
    public Collection groupAndSortBy(PySequence seq, Field field) {
	ArrayList al = new ArrayList();
	al.addAll(groupBy(seq,field));
	Collections.sort(al);
	return(al);
    }

    public Collection groupAndSortNodesBy(String field) {
	ArrayList al = new ArrayList();
	al.addAll(groupNodesBy(field));
	Collections.sort(al);
	return(al);
    }

    public Collection groupNodesBy(String field) {
	Hashtable map = new Hashtable();
	Iterator it = getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    Object attrib = n.__getattr__(field);
	    if (!map.containsKey(attrib)) {
		map.put(attrib,new SortableHashSet((Comparable)attrib));
	    }
	    ((HashSet)map.get(attrib)).add(n);
	}
	return(map.values());
    }
    
    public Collection groupAndSortEdgesBy(String field) {
	ArrayList al = new ArrayList();
	al.addAll(groupEdgesBy(field));
	Collections.sort(al);
	return(al);
    }

    public Collection groupEdgesBy(String field) {
	Hashtable map = new Hashtable();
	Iterator it = getEdges().iterator();
	while(it.hasNext()) {
	    Edge n = (Edge)it.next();
	    Object attrib = n.__getattr__(field);
	    if (!map.containsKey(attrib)) {
		map.put(attrib,new SortableHashSet((Comparable)attrib));
	    }
	    ((HashSet)map.get(attrib)).add(n);
	}
	return(map.values());
    }

    /**
     *
     */
    public void makeBarabasiAlbertRandom(int vert, int edges, int evolve) {
	BarabasiAlbertGenerator barg = 
	    new BarabasiAlbertGenerator(vert, edges);
	barg.evolveGraph(evolve);
	makeFromGenerator(barg);
    }

    /**
     *
     */
    public void makeSimpleRandom(int nodes, int edges) {
	SimpleRandomGenerator srg = new SimpleRandomGenerator(nodes,edges);
	makeFromGenerator(srg);
    }

    /**
     *
     */
    public void makeSimpleDirectedRandom(int nodes, int edges) {
	SimpleRandomDirectedGenerator srg = 
	    new SimpleRandomDirectedGenerator(nodes,edges);
	makeFromGenerator(srg);
    }

    /**
     *
     */
    public void makeEppsteinRandom(int nodes, int edges, int r) {
	EppsteinPowerLawGenerator srg = 
	    new EppsteinPowerLawGenerator(nodes,edges,r);
	makeFromGenerator(srg);
    }

    /**
     *
     */
    public void makeErdosRenyiRandom(int nodes, double p) {
	ErdosRenyiGenerator srg = 
	    new ErdosRenyiGenerator(nodes,p);
	makeFromGenerator(srg);
    }

    /**
     *
     */
    public void makeLattice1DRandom(int nodes, boolean tor) {
	Lattice1DGenerator srg = 
	    new Lattice1DGenerator(nodes,tor);
	makeFromGenerator(srg);
    }

    /**
     *
     */
    public void makeLattice2DRandom(int nodes, boolean tor) {
	Lattice2DGenerator srg = 
	    new Lattice2DGenerator(nodes,tor);
	makeFromGenerator(srg);
    }

    /**
     *
     */
    public void makeKleinbergRandom(int nodes, double clust) {
	KleinbergSmallWorldGenerator srg = 
	    new KleinbergSmallWorldGenerator(nodes,clust);
	makeFromGenerator(srg);
    }

    public void makeFromGenerator(GraphGenerator gg) {
	ArchetypeGraph ag = gg.generateGraph();
	Iterator it = ag.getVertices().iterator();
	int i = 0;
	Hashtable map = new Hashtable();
	while(it.hasNext()) {
	    Node n = addNode("nd"+i);
	    map.put(it.next(),n);
	    i++;
	}
	it = ag.getEdges().iterator();
	while(it.hasNext()) {
	    edu.uci.ics.jung.graph.Edge e = 
		(edu.uci.ics.jung.graph.Edge)it.next();
	    Pair p = e.getEndpoints();
	    Node n1 = (Node)map.get(p.getFirst());
	    Node n2 = (Node)map.get(p.getSecond());
	    if (e instanceof DirectedSparseEdge) {
		//System.out.println("dir");
		addDirectedEdge(n1,n2);
	    } else {
		addUndirectedEdge(n1,n2);
	    }
	}
	randomLayout();
    }

    /**
     *
     */
    public void makeFromGML(String filename) {
	GraphMLReader gmr = new GraphMLReader(this,filename);
	boolean tmp = centerAfter;
	boolean tmp2 = sync;
	sync = true;
	centerAfter = false;
	randomLayout();
	sync = tmp2;
	centerAfter = tmp;
    }

    /**
     *
     */
    public void makeFromDL(String filename) {
	// added for Patrick, fill in the blanks
	//GraphMLReader gmr = new GraphMLReader(this,filename);
	//randomLayout();
    }

    /**
     *
     */
    public void morph(String state, long duration) {
	if (VisFactory.getUIMode() == VisFactory.PICCOLO) {
	    Morpher.morph(this,state,duration);
	}
    }

    public void morph(int state, long duration) {
	morph(""+state,duration);
    }

    /**
     *
     */
    public void colorize(Field f) {
	VisualUtils.colorize(this,f);
    }

    /**
     *
     */
    public void colorize(Field f, Color start, Color end) {
	VisualUtils.colorize(this,f,start,end);
    }

    public void colorize(Field f, String start, String end) {
	VisualUtils.colorize(this,f,
			  Colors.getColor(start,Color.red),
			  Colors.getColor(end,Color.blue));
    }

    /**
     *
     */
    public void resizeRandom(Field f, double start, double end) {
	VisualUtils.resizeRandom(this,f,start,end);
    }

    /**
     *
     */
    public void resizeLinear(Field f, double start, double end) {
	VisualUtils.resizeLinear(this,f,start,end);
    }

    /**
     *
     */
    public double density() {
	return(NetUtilities.calcDensity(getNodes()));
    }

    /**
     *
     */
    public Set removeSelfLoops() {
	Iterator it = getEdges().iterator();
	HashSet toRemove = new HashSet();
	while(it.hasNext()) {
	    Edge e = (Edge)it.next();
	    if (e.getNode1() == e.getNode2()) {
		toRemove.add(e);
	    }
	}
	it = toRemove.iterator();
	while(it.hasNext()) {
	    removeEdge((Edge)it.next());
	}
	return(null);
    }

    /**
     *
     */
    public Set removeDisconnected() {
	Iterator it = getNodes().iterator();
	HashSet toRemove = new HashSet();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    boolean todo = true;
	    Iterator edges = n.getIncidentEdges().iterator();
	    while (edges.hasNext()) {
		Edge e = (Edge)edges.next();
		Node t1 = e.getNode1();
		Node t2 = e.getNode2();
		if ((n != t1) || (n != t2)) {
		    todo = false;
		    break;
		}
	    }
	    if (todo) {
		toRemove.add(n);
	    }
	}
	it = toRemove.iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    removeNode(n);
	}
	return(null);
    }

    /**
     *
     */
    public void hideDisconnected() {
	Iterator it = getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    boolean todo = true;
	    Iterator edges = n.getIncidentEdges().iterator();
	    while (edges.hasNext()) {
		Edge e = (Edge)edges.next();
		Node t1 = e.getNode1();
		Node t2 = e.getNode2();
		if ((n != t1) || (n != t2)) {
		    todo = false;
		    break;
		}
	    }
	    if (todo) {
		n.__setattr__("visible",new Boolean(false));
	    }
	}
    }
}

