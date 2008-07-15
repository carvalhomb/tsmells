package com.hp.hpl.guess;

import java.sql.*;
import org.python.core.*;
import com.hp.hpl.guess.storage.StorageListener;
import com.hp.hpl.guess.storage.StorageFactory;
import com.hp.hpl.guess.ui.Interesting;
import com.hp.hpl.guess.ui.EditorPopup;
import com.hp.hpl.guess.ui.ExceptionWindow;

public final class Field implements Comparable, Interesting {

    private Graph graph;
    private String name;
    private int type;
    private int sqlType;
    private String state = null;
    
    public static final int NODE = 1;
    public static final int EDGE = 2;

    public static boolean nn = false;

    private Object defval = null;

    private long lastMod = System.currentTimeMillis();

    private boolean graphSensitive = false;

    public String getState() {
	return(state);
    }

    public void setState(String state) {
	this.state = state;
    }

    public void setDynamic(boolean dyn) {
	graphSensitive = dyn;
    }

    public void update() {
	lastMod = System.currentTimeMillis();
    }

    public boolean needsUpdate(Graph g) {
	if (!graphSensitive)
	    return false;

	if (g.getLastModTime() <= lastMod) {
	    return(false);
	} else {
	    return(true);
	}
    }

    public Object getDefault() {
	return(defval);
    }

    public void setDefault(Object o) {
	if ((o == null) && (nn)) {
	    throw(Py.AttributeError("Unable to set default to null for this field"));
	}
	this.defval = o;
    }

    public boolean isNotNull() {
	return(nn);
    }

    public void setNotNull(boolean nn) {
	this.nn = nn;
    }

    public PyObject __getitem__(int key) {
	Field f = new Field(graph,name,type,sqlType,defval);
	f.setState(key+"");
	return new PyJavaInstance(f);
    }
    
    public PyObject __getitem__(PyObject key) {
	Field f = new Field(graph,name,type,sqlType,defval);
	f.setState(key.toString());
	return new PyJavaInstance(f);
    }

    public Field(Graph graph, String name, int type, 
		 int sqlType, Object defval)
    {
	this.graph = graph;
	this.name = name.toLowerCase();
	this.type = type;
	this.sqlType = sqlType;
       	this.defval = defval;
    }

    public Field(Graph graph, String name, int type, int sqlType)
    {
	this(graph, name, type, sqlType, null);
	System.err.println("Call to deprecated Field constructor.");
    }
    
    public ComparisonQuery __eq__(Object value)
    {
	return new ComparisonQuery(graph, Query.EQ, this, value, type);
    }
    
    public ComparisonQuery __ne__(Object value)
    {
	return new ComparisonQuery(graph, Query.NE, this, value, type);
    }
    
    public ComparisonQuery __lt__(Object value)
    {
	return new ComparisonQuery(graph, Query.LT, this, value, type);
    }
    
    public ComparisonQuery __gt__(Object value)
    {
	return new ComparisonQuery(graph, Query.GT, this, value, type);
    }
    
    public ComparisonQuery __le__(Object value)
    {
	return new ComparisonQuery(graph, Query.LE, this, value, type);
    }
    
    public ComparisonQuery __ge__(Object value)
    {
	return new ComparisonQuery(graph, Query.GE, this, value, type);
    }
    
    public ComparisonQuery __like__(Object value)
    {
	return new ComparisonQuery(graph, Query.LIKE, this, value, type);
    }
    
    public String toString()
    {
	return name;
    }
    
    public String toSQLString() {
	StringBuffer sb = null;
	if (type == NODE) {
	    sb = new StringBuffer("nodes");
	} else {
	    sb = new StringBuffer("edges");
	}
	if (state != null) {
	    sb.append("_"+state);
	} 
	sb.append("."+name);
	return(sb.toString());
    }

    public String getName()
    {
	return name;
    }
    
    public int getType() {
	// return(type);
	return(this.type);
    }

    public int getSQLType() {
	return(this.sqlType);
    }
    
    //If value is a PyObject, converts it to a java object.
    //Otherwise, just returns the value.
    public Object toJava(Object value)
    {
	return toJava(value, sqlType);
    }
    
    public static Object toJava(Object value, int sqlType)
    {
	//if (value != null)
	//  System.out.print("Converted " + value.getClass()+"\n");
	
	if (value instanceof PyObject)
	    {
		Class javaClass;
		
		if (sqlType == Types.VARCHAR)
		    javaClass = String.class;
		else if (sqlType == Types.DOUBLE)
		    javaClass = Double.class;
		else if (sqlType == Types.ARRAY)
		    {
			double[] x = {0.0};
			
			javaClass = x.getClass();
		    }
		else if (sqlType == Types.TINYINT)
		    javaClass = Integer.class;
		else if (sqlType == Types.INTEGER)
		    javaClass = Integer.class;
		else if (sqlType == Types.BOOLEAN)
		    javaClass = Boolean.class;
		else
		    throw new Error("Unable to convert to SQL type:  " + sqlType);
		
		value = ((PyObject)value).__tojava__(javaClass);
	    }
	else
	    {
		if (value instanceof Integer && sqlType ==Types.DOUBLE)
		    value = new Double(((Integer)value).intValue());
		else if (value instanceof Integer && sqlType == Types.BOOLEAN)
		    value = new Boolean(((Integer)value).intValue() != 0);
		else
		    {
			//no conversion needed.
		    }
	    }
	
	//System.out.println(" to " + value.getClass());
	
	return value;
    }

        public Object __getattr__(String fieldName) {
	    
	    fieldName = fieldName.toLowerCase();

	    if ((fieldName.equals("max")) || 
		(fieldName.equals("min")) || 
		(fieldName.equals("avg")) ||
		(fieldName.equals("sum"))) {
		if (isNumeric(sqlType)) {
		    StorageListener db = StorageFactory.getSL();
		    Object o = db.getStatistic(this,fieldName);
		    return(o);
		} else {
		    throw(new Error(name + " is not a numeric field"));
		}
	    }
	    throw(new Error("Invalid property: " + fieldName));
	}
    
    public boolean isNumeric() {
	return(isNumeric(sqlType));
    }

    public static boolean isNumeric(int type) {
	return (type == Types.DECIMAL || type == Types.DOUBLE 
		|| type ==Types.FLOAT || type == Types.NUMERIC 
		|| type == Types.REAL || type == Types.BIGINT 
		|| type == Types.TINYINT || type == Types.SMALLINT 
		|| type == Types.INTEGER );
    }

    public int compareTo(Object o) {
	return(getName().compareTo(((Field)o).getName()));
    }

    public String getStatusBarString() {
	StringBuffer sb = new StringBuffer();
	if (getType() == Field.NODE) {
	    sb.append("Node Field, ");
	} else {
	    sb.append("Edge Field, ");
	} 
	sb.append(getName() + " ");
	if (isNumeric()) {
	    sb.append(__getattr__("min") + " - " + 
		      __getattr__("max"));
	}
	return sb.toString();
    }

    public EditorPopup getPopup() {
	return(null);
    }
}
