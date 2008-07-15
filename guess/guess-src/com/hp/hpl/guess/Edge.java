package com.hp.hpl.guess;

import com.hp.hpl.guess.piccolo.*;
import com.hp.hpl.guess.prefuse.*;
import com.hp.hpl.guess.tg.*;
import com.hp.hpl.guess.ui.*;
import java.awt.geom.*;
import java.awt.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.*;
import java.util.*;
import org.python.core.*;
import edu.uci.ics.jung.graph.impl.*;
import com.hp.hpl.guess.storage.StorageFactory;

/**
 * an implementation of a graph edge
 */
public abstract class Edge extends AbstractSparseEdge implements GraphElement
{
    private EdgeListener rep = null;

    private double weight = 0.0;
    
    private int id;

    private static int maxID = 0;

    private Object _scratch = null;
    
    protected String label = null;

    public static int getMaxID() {
	return(maxID);
    }

    public int getID() {
	return(id);
    }

    public double edgeWeight() {
	return(weight);
    }

    public void updateColumn(String fName,Object o) {
	StorageFactory.getSL().updateColumn(this,
					    getSchema().getField(fName),
					    o);
    }

    public Edge(int id, Node node1, Node node2)
    {
	super(node1, node2);
	
	this.id = id;
	if (id > maxID) {
	    maxID = id;
	}
	rep = VisFactory.getFactory().generateEdge(this);
    }

    public Edge(Node node1, Node node2)
    {
	this(maxID+1,node1,node2);
    }

    public PyObject __getitem__(int key) {
	if (StorageFactory.getSL().containsEdge(this, ""+key)) {
	    return new PyJavaInstance(new StateQuery(this,""+key));
	} else {
	    return(null);
	}	
    }

    public PyObject __getitem__(PyObject key) {
	if (StorageFactory.getSL().containsEdge(this,key.toString())) {
	    return new PyJavaInstance(new StateQuery(this,key.toString()));
	} else {
	    return(null);
	}
    }

    public Object __getattr__(String fieldName) {
	
	fieldName = fieldName.toLowerCase();
	
	if (fieldName.equals("_scratch")) {
	    return(_scratch);
	}
	EdgeSchema schema = getSchema();
	
	//attributes stored in Edge
	if (fieldName.equals("id"))
	    return new Integer(id);
	
	if (fieldName.equals("__edgeid"))
	    return new Integer(id);
	
	if (fieldName.equals("weight"))
	    return new Double(weight);
	
	if (fieldName.equals("betweenness"))
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
	
	//check if rep stores this attribute
	Object value = rep.get(fieldName);
	
	if (value != null)
	    return value;
	
		//must be a user-defined property.
		//check the schema.
	
	Field field = getSchema().getField(fieldName);
	
	if (field == null)
	    throw new Error("Invalid property:  " + fieldName);
	
	//it checks out.  query the database.
	return StorageFactory.getSL().getColumn(this, field);
    }

    public void __setattr__(String name, Object value) {
	name = name.toLowerCase();
	
	if (name.equals("_scratch")) {
	    _scratch = value;
	    return;
	}

	Field field = getSchema().getField(name);
	
	if (field == null)
	    throw new Error("Invalid property:  " + name);
	
	//converting from PyObject
	value = field.toJava(value);
	
	//checking edge fields
	if (name.equals("__edgeid"))
	    throw new Error("Cannot modify __edgeid");
	else if (name.equals("node1"))
	    throw new Error("Cannot modify node1");
	else if (name.equals("node2"))
	    throw new Error("Cannot modify node2");
	else if (name.equals("directed"))
	    throw new Error("Cannot modify directed");
	else if (name.equals("weight")) 
	    weight = ((Double)value).doubleValue();
	else if (rep.get(name) != null)
	    rep.set(name, value);

	if (name.equals("label")) {
	    label = (String)value;
	}

	//may need to show/hide connected edges
	if (name.equals("visible"))
	    {
		if (value == Boolean.TRUE) {
		    // if I'm visible, make the two end points visible
		    Node a = getNode1();
		    Node b = getNode2();
		    if ((Boolean)a.__getattr__("visible") 
			!= Boolean.TRUE) {
			a.__setattr__("visible",Boolean.TRUE);
		    }
		    if (b != a) {
			if ((Boolean)b.__getattr__("visible") 
			    != Boolean.TRUE) {
			    b.__setattr__("visible",Boolean.TRUE);
			}
		    }
		} 
	    }
	
	//need to update database regardless.
	updateColumn(name, value);
    }
    
    public void show()
    {
	__setattr__("visible", Boolean.TRUE);
    }
    
    public void hide()
    {
	__setattr__("visible", Boolean.FALSE);
    }
    
    public GraphElementListener getRep() {
	return(rep);
    }

    public void readjust() {
	rep.readjust();
    }

    /*public void setVisible(boolean state) {
	rep.setVisible(state);
	updateColumn("state",new Boolean(state));
    }*/

    /*public Color getColor()
    {
	return((Color)rep.getColor());
    }*/
    
    /*public double getWidth()
    {
	return(rep.getLineWidth());
    }*/

    /*public double getWeight()
    {
	return weight;
    }*/
    
    /*public boolean getVisible()
    {
	return(rep.getVisible());
    }*/
    
    /*public void setColor(Color c)
    {
	rep.setColor(c);
	updateColumn("color",Colors.toString(c));
    }*/
    
    /*public void setWidth(double width)
    {
	rep.setLineWidth(width);
	updateColumn("width",new Double(width));
    }*/
    
    /*public void setWeight(double weight)
    {
	this.weight = weight;
	updateColumn("weight",new Double(weight));
    }*/
    
    public Collection getNodes() {
	HashSet hs = new HashSet();
	hs.add(getNode1());
	hs.add(getNode2());
	return(hs);
    }

    public abstract Node getNode1();
    
    public abstract Node getNode2();

	private EdgeSchema getSchema()
	{
	    if (getGraph() != null) {
		return ((Graph)getGraph()).getEdgeSchema();
	    } else {
		return(Guess.getGraph().getEdgeSchema());
	    }
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

    
 //    public PySequence __sub__(Node n) {
// 	PyList nodes = new PyList();
// 	Node n1 = null;
// 	Node n2 = null;
// 	if (__lastT == null) {
// 	    n1 = getNode1();
// 	    n2 = getNode2();
// 	} else {
// 	    n1 = __lastT;
// 	}
// 	//System.out.println(n1 + " " + n2 + " " + e);
// 	Edge e1 = n1.__sub__(n);
// 	Edge e2 = null;
// 	if ((n2 != null) && (n1 != n2)) {
// 	    e2 = n2.__sub__(n);
// 	}
// 	//	System.out.println(e1 + " " + e2);
// 	if (e1 == e2) {
// 	    e2 = null;
// 	}
// 	if ((e1 != null) || (e2 != null)) {
// 	    nodes.append(new PyJavaInstance(this));
// 	    if (e1 != null) {
// 		nodes.append(new PyJavaInstance(e1));
// 	    } 
// 	    if (e2 != null) {
// 		nodes.append(new PyJavaInstance(e2));
// 	    }
// 	    return(nodes);
// 	}
// 	return(null);
//     }
    
    private Node __lastT = null;
    public Node __lastTouched() {
	return(__lastT);
    }
    protected void __lastTouched(Node n) {
	this.__lastT = n;
    }

    private Node __firstT = null;
    public Node __firstTouched() {
	return(__firstT);
    }
    protected void __firstTouched(Node n) {
	this.__firstT = n;
    }

    public int compareTo(Object object)
    {
	Integer thisI = new Integer(id);
	Integer otherI = new Integer(((Edge)object).getID());
	return thisI.compareTo(otherI);
    }
}
