package com.hp.hpl.guess;

import com.hp.hpl.guess.piccolo.*;
import com.hp.hpl.guess.prefuse.*;
import com.hp.hpl.guess.tg.*;
import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.db.DBServer;
import com.hp.hpl.guess.storage.StorageFactory;

import java.awt.geom.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import edu.uci.ics.jung.algorithms.shortestpath.*;

import edu.uci.ics.jung.graph.impl.*;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.*;

import org.python.core.*;

public class Node extends SparseVertex implements Comparable, GraphElement
{
    private static int nextID = 1;

    private String name;

    private Object _scratch = null;

    private String label = null;

    public String getName() {
	return(name);
    }

    /**
     * a pointer to the visual rep of the object
     */
    private NodeListener rep = null;

    private static final Random r = new Random();

    public Node(String name)
    {
	this(1, 
	     r.nextDouble()*5, 
	     r.nextDouble()*5, 
	     10, 10, name);
    }
    
    public Node(int type, double x,double y,
		double width,
		double height, 
		String name) {
	this.name = name;
	rep = VisFactory.getFactory().generateNode(type,x,y,
						   width,height,Color.red,
						   this);
    }

    public String toString()
    {
	//Thread.dumpStack();
	if (label == null)
	    return name;
	
	if (label.equals(name))
	    return name;
	
	return(label);
    }

    public PyObject __getitem__(int key) {
	if (StorageFactory.getSL().containsNode(this, ""+key)) {
	    return new PyJavaInstance(new StateQuery(this,""+key));
	} else {
	    return(null);
	}
    }

    public PyObject __getitem__(PyObject key) {
	if (StorageFactory.getSL().containsNode(this,key.toString())) {
	    return new PyJavaInstance(new StateQuery(this,key.toString()));
	} else {
	    return(null);
	}
    }

    public int compareTo(Object object)
    {
	return name.compareTo(((Node)object).__getattr__("name").toString());
    }

    public PySequence __anye__(PySequence sequence)
    {
	PyList nodes = new PyList();
	nodes.append(new PyJavaInstance(this));
	return (PySequence)nodes.__anye__(sequence);
    }

    public PySequence __anye__(Node node)
    {
	PyList toRet = new PyList();
	HashSet seen = new HashSet();
	Iterator it = findEdgeSet(node).iterator();
	while (it.hasNext())
	    {
		Object o = it.next();
		if (seen.contains(o))
		    continue;
		toRet.append(new PyJavaInstance(o));
		seen.add(o);
	    }
	it = node.findEdgeSet(this).iterator();
	while (it.hasNext())
	    {
		Object o = it.next();
		if (seen.contains(o))
		    continue;
		toRet.append(new PyJavaInstance(o));
		seen.add(o);
	    }
	return(toRet);
    }


    public PySequence __sub__(PySequence sequence)
    {
	PyList nodes = new PyList();
	nodes.append(new PyJavaInstance(this));
	return (PySequence)nodes.__sub__(sequence);
    }

    public PySequence __sub__(Node node)
    {
	PyList toRet = new PyList();
	boolean found = false;
	Set set = findEdgeSet(node);
	Iterator it = set.iterator();
	while (it.hasNext())
	    {
		Edge edge = (Edge)it.next();
		if (edge instanceof UndirectedEdge) {
		    edge.__lastTouched(node);		    
		    edge.__firstTouched(this);
		    found = true;
		    toRet.append(new PyJavaInstance(edge));
		}
	    }

	return(toRet);
    }

//     public PySequence __sub__(Edge e) {
// 	PyList nodes = new PyList();
// 	Node n1 = null;
// 	Node n2 = null;
// 	if (e.__firstTouched() == null) {
// 	    n1 = e.getNode1();
// 	    n2 = e.getNode2();
// 	} else {
// 	    n1 = e.__firstTouched();
// 	}
// 	//System.out.println(n1 + " " + n2 + " " + e);
// 	Edge e1 = this.__sub__(n1);
// 	Edge e2 = null;
// 	if ((n2 != null) && (n1 != n2)) {
// 	    e2 = this.__sub__(n2);
// 	}
// 	//	System.out.println(e1 + " " + e2);
// 	if (e1 == e2) {
// 	    e2 = null;
// 	}
// 	if ((e1 != null) || (e2 != null)) {
// 	    if (e1 != null) {
// 		nodes.append(new PyJavaInstance(e1));
// 	    } 
// 	    if (e2 != null) {
// 		nodes.append(new PyJavaInstance(e2));
// 	    }
// 	    nodes.append(new PyJavaInstance(e));
// 	    return(nodes);
// 	}
// 	return(null);
//     }

    public PySequence __rde__(PySequence sequence)
    {
	PyList nodes = new PyList();
	nodes.append(new PyJavaInstance(this));
	return (PySequence)nodes.__rde__(sequence);
    }

    public PySequence __rde__(Node node)
    {
	PyList toRet = new PyList();
	boolean found = false;

	Set set = findEdgeSet(node);
	Iterator it = set.iterator();
	while (it.hasNext())
	    {
		Edge edge = (Edge)it.next();

		if (edge instanceof DirectedEdge) {
		    edge.__lastTouched(node);		    
		    edge.__firstTouched(this);
		    found = true;
		    toRet.append(new PyJavaInstance(edge));
		}
	    }

	return(toRet);
    }

    public PySequence __lde__(PySequence sequence)
    {
	PyList nodes = new PyList();
	nodes.append(new PyJavaInstance(this));
	return (PySequence)nodes.__lde__(sequence);
    }

    public PySequence __lde__(Node node)
    {
	return node.__rde__(this);
    }

    public PySequence __bde__(PySequence sequence)
    {
	PyList nodes = new PyList();
	nodes.append(new PyJavaInstance(this));
	return (PySequence)nodes.__bde__(sequence);
    }

    public PySequence __bde__(Node node)
    {
	return __sub__(node);
    }

    public void updateDegrees() {
	NodeSchema schema = getSchema();
	Field deg = schema.getField("indegree");
	if (deg != null) {
	    __setattr__("indegree",new Integer(inDegree()));
	} 
	deg = schema.getField("outdegree");
	if (deg != null) {
	    __setattr__("outdegree",new Integer(outDegree()));
	} 
	deg = schema.getField("totaldegree");
	if (deg != null) {
	    __setattr__("totaldegree",new Integer(degree()));
	} 
    }


    public Object __getattr__(String fieldName)
    {
	fieldName = fieldName.toLowerCase();

	if (fieldName.equals("_scratch")) {
	    return(_scratch);
	}

	NodeSchema schema = getSchema();
	
	//attributes stored in Node
	if (fieldName.equals("name"))
	    return name;
	else if (fieldName.equals("size"))
	    {
		double height = ((Double)__getattr__("height")).doubleValue();
		double width = ((Double)__getattr__("width")).doubleValue();
		
		return new Double(Math.sqrt(height * width));
	    }
	else if (fieldName.equals("betweenness"))
	    {
		((Graph)getGraph()).computeBetweennessCentrality();
	    }
	else if (fieldName.equals("pagerank"))
	    {	
		((Graph)getGraph()).computePageRank();
	    }
	else if (fieldName.equals("hits"))
	    {
		((Graph)getGraph()).computeHITS();
	    }
	else if (fieldName.equals("degrank"))
	    {
		((Graph)getGraph()).computeDegreeDistributionRank();
	    }
	else if (fieldName.equals("rwbetweenness"))
	    {
		((Graph)getGraph()).computeRandomWalkBetweenness();
	    }
	else if (fieldName.equals("indegree")) 
	    {
		((Graph)getGraph()).computeDegrees();
	    } 
	else if (fieldName.equals("outdegree")) 
	    {
		((Graph)getGraph()).computeDegrees();
	    } 
	else if (fieldName.equals("totaldegree")) 
	    {
		((Graph)getGraph()).computeDegrees();
	    } 
	else if (fieldName.equals("location"))
	    {
		ArrayList location = new ArrayList();
		
		location.add(rep.get("x"));
		location.add(rep.get("y"));
		
		return location;
	    }
	else if (fieldName.equals("bounds"))
	    {
		ArrayList bounds = new ArrayList();
		
		bounds.add(rep.get("x"));
		bounds.add(rep.get("y"));
		bounds.add(rep.get("width"));
		bounds.add(rep.get("height"));
		
		return bounds;
	    }
	
	//check if rep stores this attribute
	Object value = rep.get(fieldName);
	
	if (value != null)
	    return value;
	
	//must be a user-defined property.
	//check the schema.
	
	Field field = schema.getField(fieldName);
	
	if (field == null)
	    throw new Error("Invalid property:  " + fieldName);
	
	//it checks out.  query the database.
	Object dbValue = StorageFactory.getSL().getColumn(this, field);
	
	//System.out.println("Returning " + dbValue + " of type " + dbValue.getClass());
	
	return dbValue;
    }

    /**
     * don't use this unless you know what you're doing... it's a hack
     */
    public Object getAttrDB(String fieldName) {

	NodeSchema schema = getSchema();

	fieldName = fieldName.toLowerCase();
	Field field = schema.getField(fieldName);
	
	if (field == null)
	    throw new Error("Invalid property:  " + fieldName);
	

	Object dbValue = StorageFactory.getSL().getColumn(this, field);
	
	return dbValue;
    }

    public void __setattr__(String name, Object value) {

	name = name.toLowerCase();
	
	Field field = null;
	
	if (name.equals("size")) 
	    //size is special, since it doesn't correspond to a real field.
	    value = Field.toJava(value, Types.DOUBLE);
	    //Thread.dumpStack();
	else if (name.equals("location") || name.equals("bounds"))
	    value = Field.toJava(value, Types.ARRAY);
	else if (name.equals("shape")) {
	    rep.set(name,value);
	    return;
	} else if (name.equals("_scratch")) {
	    _scratch = value;
	    return;
	}
	else
	    {
		field = getSchema().getField(name);
		
		if (field == null)
		    throw new Error("Invalid property:  " + name);
		
		//converting from PyObject
		value = field.toJava(value);
	    }
	
	//checking Node fields
	if (name.equals("name"))
	    throw new Error("Cannot modify name");
	else if (name.equals("size"))
	    {
		//this is shorthand for setting height and width.
		__setattr__("height", value);
		__setattr__("width", value);
		//don't update the database with size!
		return;
	    }
	else if (name.equals("location"))
	    {
		double[] values = (double[])value;
		setLocation(values[0], values[1]);
		return;
	    }
	else if (name.equals("bounds"))
	    {
		double[] values = (double[])value;
		setLocation(values[0], values[1], values[2], values[3]);
		return;
	    }
	else if (name.equals("label")) 
	    {
		this.label = (String)value;
		rep.set(name, value);
	    } 
	else if (rep.get(name) != null)
	    {
		//updating in the rep.
		//if (name.equals("label")) 
		//  System.out.println("-----" + name + " to " + value);
		rep.set(name, value);
	    }

	//need to update database regardless.
	updateColumn(name, value);
	
	//may need to show/hide connected edges
	if (name.equals("visible"))
	    {
		Iterator it = getIncidentEdges().iterator();
		while (it.hasNext())
		    {
			// if both end points are visible
			// make the edge visible
			Edge edge = (Edge)it.next();
			Node a = edge.getNode1();
			Node b = edge.getNode2();
			if ((a.__getattr__("visible").equals(Boolean.TRUE)) &&
			    (b.__getattr__("visible").equals(Boolean.TRUE))) {
			    if (edge.__getattr__("visible").equals(Boolean.FALSE)) 
				edge.__setattr__("visible", Boolean.TRUE);
			    
			} else {
			    if (edge.__getattr__("visible").equals(Boolean.TRUE))
				edge.__setattr__("visible", Boolean.FALSE);
			}
		    }
	    }
	
    }

    public void hide()
    {
		__setattr__("visible", Boolean.FALSE);
    }

    public void show()
    {
		__setattr__("visible", Boolean.TRUE);
    }


    /**
     * returns the rep
     */
    public GraphElementListener getRep() {
	return(rep);
    }

    /**
     * sets the rep
     * don't call this unless you know what you're doing, you
     * could separate the nodes from the visualization 
     */
    public void setRep(NodeListener rep) {
	this.rep = rep;
    }

    public void readjustEdges() {
	Iterator i = getIncidentEdges().iterator();
	while (i.hasNext()) {
	    ((Edge)i.next()).readjust();
	}
    }


    /**
     * returns the xlocation of the node
     */
    public double getX() {
	return getDouble("x");
    }

    public void setX(double x)
    {
		__setattr__("x", new Double(x));
    }

    /**
     * returns the ylocation of the node
     */
    public double getY() {
	return getDouble("y");
    }
    
    public void setY(double y)
    {
		__setattr__("y", new Double(y));
    }

    /**
     * returns the width of the node
     */
    public double getWidth() {
	return getDouble("width");
    }

    public void setWidth(double width)
    {
		__setattr__("width", new Double(width));
    }

    public void updateColumn(String fName,Object o) {
	try {
	    StorageFactory.getSL().updateColumn(this,
						getSchema().getField(fName),
						o);
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    /**
     * returns the height of the node
     */
    public double getHeight() {
	return getDouble("height");
    }

    public void setHeight(double height)
    {
		__setattr__("height", new Double(height));
    }
    
    /*public Color getColor()
    {
	return Color.red;
	//throw new Error("getColor not yet implemented");
    }*/
    
    /*public void setColor(Color color)
    {
	rep.setPaint(color);
	updateColumn("color",Colors.toString(color));
    }*/

    public void setLocation(double x1, double y1, 
			    double width, double height)
	{
		rep.setLocation(x1, y1, width, height);
		updateColumn("x", new Double(x1));
		updateColumn("y", new Double(y1));
		updateColumn("width", new Double(width));
		updateColumn("height", new Double(height));
    }
    
    public void setLocation(double x1, double y1)
    {
	rep.setLocation(x1, y1);
	updateColumn("x", new Double(x1));
	updateColumn("y", new Double(y1));
    }
    
    public void startMove(double x1, double y1) {
	rep.setLocation(x1,y1);
    }

    public void move(double x1, double y1) {
	rep.setLocation(x1,y1);
    }

    public void endMove(double x1, double y1) {
	// do the regular thing
	setLocation(x1,y1);
    }
    
    /*public void setSize(double width,double height) {
	rep.setSize(width,height);
	readjustEdges();
	updateColumn("width",new Double(width));
	updateColumn("height",new Double(height));
    }*/

    /*public void setVisible(boolean state) {
	rep.setVisible(state);
	edgeVisibility(state);
	updateColumn("visible",new Boolean(state));
    }*/

    /*public boolean getVisible()
    {
	return(rep.getVisible());
    }*/

    /*public void setLabel(String label) {
	this.label = label;
	this.rep.setLabel(label);
	updateColumn("label",new String(label));
    }*/

    /*public String getLabel() {
	return(label);
    }*/


    /*public void setLabelVisible(boolean state) {
	rep.label(state);
	updateColumn("labelvisible",new Boolean(state));
    }*/

    /*public boolean getLabelVisible()
    {
	throw new Error("getLabelVisible not yet implemented");
    }*/
    
    /*public boolean getFixed()
    {
	throw new Error("getFixed not yet implemented");
    }*/
    
    /*public void setFixed(boolean fixed)
    {
	//throw new Error("setFixed not yet implemented");
	updateColumn("fixed",new Boolean(fixed));
    }*/

    /*public double getSize()
    {
	double height = getHeight();
	double width = getWidth();
	
	if (height == width)
	    return height;
	
	throw new Error("ambiguous size:  height = " + height + ", width = " + width);
    }*/
    
    /*public void setSize(double size)
    {
	setHeight(size);
	setWidth(size);
    }*/

    /*public void setStyle(int style) {
	rep = PFactory.convertNode(rep,style,this);
	readjustEdges();
	updateColumn("style",new Integer(style));
    }*/
    
    /*public int getStyle() {
	return(rep.getStyle());
    }*/
    
    public java.util.List unweightedShortestPath(Node dest)
    {
	ShortestPath alg = new UnweightedShortestPath((Graph)getGraph());
	return ShortestPathUtils.getPath(alg, this, dest);
    }
    
    public java.util.List dijkstraShortestPath(Node dest)
    {
	ShortestPath alg = 
	    new DijkstraShortestPath((Graph)getGraph(), (Graph)getGraph());
	return ShortestPathUtils.getPath(alg, this, dest);
    }
    
    private NodeSchema getSchema()
    {
	if (getGraph() != null) {
	    return ((Graph)getGraph()).getNodeSchema();
	} else {
	    return(Guess.getGraph().getNodeSchema());
	}
    }
    
	private double getDouble(String name)
	{
		return ((Double)__getattr__(name)).doubleValue();
	}

	public Map values()
	{
		Map values = new HashMap();

		Iterator fields = getSchema().fields();
		while (fields.hasNext())
		{
			Field field = (Field)fields.next();
			String name = field.getName();
			values.put(name, __getattr__(name));
		}

		return values;
	}

}
