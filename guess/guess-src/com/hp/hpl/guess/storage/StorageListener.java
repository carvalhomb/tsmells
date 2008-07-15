package com.hp.hpl.guess.storage;

import com.hp.hpl.guess.*;
import java.util.Set;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 * basic interface to databases and other things that store
 * nodes and edges for us
 * @pyobj db
 */
public interface StorageListener {

    /**
     * find all matching nodes for query q, q will be modified
     * to hold the results
     * @param q the query to evaluate
     */
    public void findMatchingNodes(Query q);

    /**
     * find all matching edges for query q, q will be modified
     * to hold the results
     * @param q the query to evaluate
     */
    public void findMatchingEdges(Query q);

    /**
     * This usually gets called once to add the 
     * fields alrady present for nodes that the database
     * knows about.  Once this is done new Fields (e.g. columns)
     * should not be added to the underlying datastore
     * without informing the schema.
     * @see com.hp.hpl.Schema#addFieldToSL(Field)
     * @see #addFieldToSL(Field);
     */
    public void fillSchema(NodeSchema ns,Graph g);

    /**
     * This usually gets called once to add the 
     * fields alrady present for nodes that the database
     * knows about.  Once this is done new Fields (e.g. columns)
     * should not be added to the underlying datastore
     * without informing the schema.
     * @see com.hp.hpl.Schema#addFieldToSL(Field)
     * @see #addFieldToSL(Field);
     */
    public void fillSchema(EdgeSchema ns,Graph g);

    /**
     * call this to update the value for field f for Node n.
     * @param n the node that is being modified
     * @param f the field that you want changed (e.g. column)
     * @param value the new value
     */
    public void updateColumn(Node n, Field f, Object value);

    /**
     * call this to update the value for field f for Edge n.
     * @param e the edge that is being modified
     * @param f the field that you want changed (e.g. column)
     * @param value the new value
     */
    public void updateColumn(Edge e, Field f, Object value);

    /**
     * call this to load up all the nodes/edges from the database
     * into the graph.
     * @param g the graph to load the results into
     */
    public void refresh(Graph g);

    /**
     * add a new field to the database
     * @param f the field to add
     */
    public void addField(Field f);

    /**
     * shutdown operator, once you call this don't assume
     * you can do anything with the StorageListener
     */
    public void shutdown();

    /**
     * returns the value of Field f in for Node n
     * @param n the node you want the result for
     * @param f the field 
     */
    public Object getColumn(Node n, Field f);

    /**
     * returns the value of Field f in for Edge n
     * @param n the edge you want the result for
     * @param f the field 
     */
    public Object getColumn(Edge e, Field f);
    
    /**
     * returns the value of Field f in for Node n
     * @param n the node you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object getColumn(Node n, Field f, String state);

    /**
     * returns the value of Field f in for Edge n
     * @param n the edge you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object getColumn(Edge e, Field f, String state);

    /**
     * returns the value of Field f in for Node n
     * @param n the node you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object getColumn(Node n, Field f, int state);

    /**
     * returns the value of Field f in for Edge n
     * @param n the edge you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object getColumn(Edge e, Field f, int state);

    /**
     * returns the value of Field[] f in for Node n
     * @param n the node you want the result for
     * @param f the field 
     */
    public Object[] getColumns(Node n, Field[] f);

    /**
     * returns the value of Field[] f in for Edge n
     * @param n the edge you want the result for
     * @param f the field 
     */
    public Object[] getColumns(Edge e, Field[] f);
    
    /**
     * returns the value of Field[] f in for Node n
     * @param n the node you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object[] getColumns(Node n, Field[] f, String state);

    /**
     * returns the value of Field[] f in for Edge n
     * @param n the edge you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object[] getColumns(Edge e, Field[] f, String state);

    /**
     * returns the value of Field[] f in for Node n
     * @param n the node you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object[] getColumns(Node n, Field[] f, int state);

    /**
     * returns the value of Field[] f in for Edge n
     * @param n the edge you want the result for
     * @param f the field 
     * @param state the state to retrieve from
     */
    public Object[] getColumns(Edge e, Field[] f, int state);
    
    /**
     * adds a new edge to the database and return its ID.
     */
    public int createDirectedEdge(Node source, Node dest);
    
    /**
     * adds a new edge to the database and return its ID.
     */
    public int createUndirectedEdge(Node source, Node dest);

    /**
     * adds a new edge to the database and return its ID.
     * Forces specific __edgeid
     */
    public int createDirectedEdge(Node source, Node dest, int id);
    
    /**
     * adds a new edge to the database and return its ID.
     * Forces specific __edgeid
     */
    public int createUndirectedEdge(Node source, Node dest, int id);

    
    //adds the given node to the database.
    public void addNode(Node node);

    //adds the given node to the database.
    public void undelete(Node node);

    //adds the given node to the database.
    public void undelete(Edge edge);
    
    //deletes the given node from the database.
    public void remove(Node node);
    
    //deletes the given edge from the database.
    public void remove(Edge edge);

    //deletes the given node from the database.
    public void removeComplete(Node node);
    
    //deletes the given edge from the database.
    public void removeComplete(Edge edge);

    /**
     * @pyexport ls
     */
    public void loadState(Graph g, int state);

    public void loadState(Graph g, String state);

    /**
     * @pyexport ls
     */
    public void loadState(String state);

    public void loadState(int state);

    /**
     * @pyexport ss
     */
    public void saveState(int state);
    
    public void saveState(String state);

    public Object getStatistic(Field f, String statType);

    public void loadFromText(String text);
    
    public void loadFromFile(String file);

    public void createEmpty();

    /**
     * @pyexport
     */
    public Set getStates();

    public Vector getEdgesNotInCurrent(Graph g, int statenum);

    public Vector getEdgesNotInCurrent(Graph g, String statenum);

    public Vector getNodesNotInCurrent(Graph g, int statenum);

    public Vector getNodesNotInCurrent(Graph g, String statenum);

    public AbstractTableModel getNodeTable();

    public AbstractTableModel getEdgeTable();

    public boolean containsEdge(Edge e, String st);
    public boolean containsEdge(Edge e);

    public boolean containsNode(Node n, String st);
    public boolean containsNode(Node n);

    public void addEdge(Edge e);

    public void exportGDF(String filename);
}


