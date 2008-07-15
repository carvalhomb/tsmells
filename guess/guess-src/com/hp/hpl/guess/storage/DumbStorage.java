package com.hp.hpl.guess.storage;

import com.hp.hpl.guess.*;
import java.util.*;
import java.sql.Types;
import org.python.core.*;
import javax.swing.table.AbstractTableModel;

public class DumbStorage implements StorageListener {

    public DumbStorage() {
    }

    public void findMatchingNodes(Query q) {
	Iterator it = q.getGraph().getVertices().iterator();
	while (it.hasNext())
	    {
		GraphElement element = (GraphElement)it.next();
		
		if (q.describes(element))
		    q.append(new PyJavaInstance(element));
	    }
    }

    public void findMatchingEdges(Query q) {
	Iterator it = q.getGraph().getEdges().iterator();
	while (it.hasNext())
	    {
		GraphElement element = (GraphElement)it.next();
		
		if (q.describes(element))
		    q.append(new PyJavaInstance(element));
	    }
    }

    public void fillSchema(NodeSchema ns,Graph graph) {
	ns.addField(new Field(graph, "label", 
			      Field.NODE,Types.VARCHAR,null));
	ns.addField(new Field(graph, "name", 
			      Field.NODE,Types.VARCHAR,null));
	ns.addField(new Field(graph, "color", 
			      Field.NODE,Types.VARCHAR,null));
	ns.addField(new Field(graph, "height", 
			      Field.NODE,Types.DOUBLE,null));
	ns.addField(new Field(graph, "width", 
			      Field.NODE,Types.DOUBLE,null));
	ns.addField(new Field(graph, "visible", 
			      Field.NODE,Types.BOOLEAN,null));
	ns.addField(new Field(graph, "fixed", 
			      Field.NODE,Types.BOOLEAN,null));
	ns.addField(new Field(graph, "x", 
			      Field.NODE,Types.DOUBLE,null));
	ns.addField(new Field(graph, "y", 
			      Field.NODE,Types.DOUBLE,null));
	ns.addField(new Field(graph, "labelvisible", Field.NODE,
			      Types.BOOLEAN,null));
	ns.addField(new Field(graph, "style", 
			      Field.NODE,Types.TINYINT,null));
    }

    public void fillSchema(EdgeSchema ns,Graph graph) {
	ns.addField(new Field(graph, "color", 
			   Field.EDGE,Types.VARCHAR,null));
	ns.addField(new Field(graph, "width", 
			   Field.EDGE,Types.DOUBLE,null));
	ns.addField(new Field(graph, "weight", 
			   Field.EDGE,Types.DOUBLE,null));
	ns.addField(new Field(graph, "visible", 
			   Field.EDGE,Types.BOOLEAN,null));
	ns.addField(new Field(graph, "rank", 
			      Field.EDGE,Types.DOUBLE,null));
    }

    public void updateColumn(Node n, Field f, Object value) {
    }

    public void updateColumn(Edge e, Field f, Object value) {
    }

    public Object getColumn(Node n, Field f) {
	throw new Error("no such field: " + f.getName());
    }

    public Object getColumn(Edge e, Field f) {
	throw new Error("no such field: " + f.getName());
    }

    public Object getColumn(Node n, Field f, String s) {
	throw new Error("no such field: " + f.getName());
    }

    public Object getColumn(Edge e, Field f, String s) {
	throw new Error("no such field: " + f.getName());
    }

    public Object getColumn(Node n, Field f, int s) {
	throw new Error("no such field: " + f.getName());
    }

    public Object getColumn(Edge e, Field f, int s) {
	throw new Error("no such field: " + f.getName());
    }

    public Object[] getColumns(Node n, Field[] f) {
	throw new Error("no such fields");
    }

    public Object[] getColumns(Edge e, Field[] f) {
	throw new Error("no such fields");
    }

    public Object[] getColumns(Node n, Field[] f, String s) {
	throw new Error("no such fields");
    }

    public Object[] getColumns(Edge e, Field[] f, String s) {
	throw new Error("no such fields");
    }

    public Object[] getColumns(Node n, Field[] f, int s) {
	throw new Error("no such fields");
    }

    public Object[] getColumns(Edge e, Field[] f, int s) {
	throw new Error("no such fields");
    }

    public void refresh(Graph g) {
    }
    
    public void shutdown() {
    }

    public void addField(Field f) {
    }

	public int createDirectedEdge(Node source, Node dest)
	{
		return -1;
	}

	public int createUndirectedEdge(Node source, Node dest)
	{
		return -1;
	}

	public int createDirectedEdge(Node source, Node dest, int id)
	{
		return -1;
	}

	public int createUndirectedEdge(Node source, Node dest, int id)
	{
		return -1;
	}

	public void addNode(Node node)
	{
		//ignore
	}

	public void remove(Edge edge)
	{
		//ignore
	}
	
	public void remove(Node node)
	{
		//ignore
	}

	public void removeComplete(Edge edge)
	{
		//ignore
	}
	
	public void removeComplete(Node node)
	{
		//ignore
	}

    public void undelete(Node node) {
    }

    public void undelete(Edge edge) {
    }

    public void loadState(Graph g, int state) {
	throw(new Error("Unsupported function"));
    }

    public void loadState(Graph g, String state) {
	throw(new Error("Unsupported function"));
    }

    public void loadState(int state) {
	throw(new Error("Unsupported function"));
    }

    public void loadState(String state) {
	throw(new Error("Unsupported function"));
    }

    public void saveState(int state) {
	throw(new Error("Unsupported function"));
    }
    
    public void saveState(String state) {
	throw(new Error("Unsupported function"));
    }

    public Vector getEdgesNotInCurrent(Graph g, int statenum)  {
	throw(new Error("Unsupported function"));
    }

    public Vector getEdgesNotInCurrent(Graph g, String statenum) {
	throw(new Error("Unsupported function"));
    }

    public Vector getNodesNotInCurrent(Graph g, int statenum) {
	throw(new Error("Unsupported function"));
    }

    public Vector getNodesNotInCurrent(Graph g, String statenum) {
	throw(new Error("Unsupported function"));
    }


    public AbstractTableModel getNodeTable() {
	throw(new Error("Unsupported function"));
    }

    public AbstractTableModel getEdgeTable() {
	throw(new Error("Unsupported function"));
    }

    public Object getStatistic(Field f, String statType) {
	return(null);
    }

    public void loadFromText(String text) {}
 
    public void loadFromFile(String file) {}

    public void createEmpty() {}

    public Set getStates() { 
	return(null);
    }

    public boolean containsEdge(Edge e) {
	return(false);
    }
    public boolean containsEdge(Edge e, String st) {
	return(false);
    }

    public boolean containsNode(Node n, String st) {
	return(false);
    }

    public boolean containsNode(Node n) {
	return(false);
    }

    public void addEdge(Edge e) {
    }

    public void exportGDF(String filename) {
    }
}


