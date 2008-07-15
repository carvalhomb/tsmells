package com.hp.hpl.guess.db;

import java.sql.*;
import java.util.*;
import java.io.*;
import org.python.core.*;

import com.hp.hpl.guess.*;
import com.hp.hpl.guess.storage.*;
import com.hp.hpl.guess.ui.ExceptionWindow;
import com.hp.hpl.guess.ui.StatusBar;
import javax.swing.table.AbstractTableModel;
import edu.uci.ics.jung.exceptions.ConstraintViolationException;

/**
 * @pyobj db
 */
public class DBServer implements StorageListener {
    
    private Connection conn;  

    private static DBServer singleton = null;

    public static final boolean NODE = true;
    public static final boolean EDGE = false;

    private static HashSet tableList = new HashSet();

    public Statement getStatement() throws SQLException {
       	if (conn != null) {
	    return(conn.createStatement());
	} else {
	    return(null);
	}
    }

    public Set getStates() {
	HashSet hs = new HashSet(); 
	try {
	    DatabaseMetaData dmb = conn.getMetaData();
	    ResultSet rs = dmb.getTables(null,null,null,null);
	    while(rs.next()) {
		String name = rs.getString(3);
		if (name.startsWith("EDGES_")) {
		    String foo = name.substring(6);
		    if (foo.equalsIgnoreCase("_deleted"))
			continue;
		    if (!foo.equalsIgnoreCase("DEF"))
			hs.add(foo);
		}
	    }
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	}
	return(hs);
    }

    /**
     * @param nodeOrEdge true for node, false for edge
     */
    private boolean containsTable(boolean nodeOrEdge, String st) {

	if (st == null)
	    return(true);

	//System.out.println("calling...");
	if (tableList.size() == 0) {
	    // our cache was invalidated, let's rebuild the
	    // list of states
	    //System.out.println("rebuilding...");
	    try {
		DatabaseMetaData dmb = conn.getMetaData();
		ResultSet rs = dmb.getTables(null,null,null,null);
		while(rs.next()) {
		    String name = rs.getString(3);
		    tableList.add(name.toLowerCase());
		}
	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}

	String toTest = "NODES_"+st;
	if (!nodeOrEdge)
	    toTest = "EDGES_"+st;

	return(tableList.contains(toTest.toLowerCase()));
    }

    private Set getAllStates() {
	HashSet hs = new HashSet(); 
	try {
	    DatabaseMetaData dmb = conn.getMetaData();
	    ResultSet rs = dmb.getTables(null,null,null,null);
	    while(rs.next()) {
		String name = rs.getString(3);
		if (name.startsWith("EDGES")) {
		    String foo = name.substring(5);
		    if (!foo.equalsIgnoreCase("_DEF"))
			hs.add(foo);
		}
	    }
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
	return(hs);
    }

    private Hashtable preparedStatements = new Hashtable();

    private boolean commitState = false;
    
    public void setCommitState(boolean state) {
	this.commitState = state;
    }

    public boolean getCommitState() {
	return(this.commitState);
    }

    public Object getColumn(Node n, Field f) {
	if (n.getGraph() == null) {
	    return(getColumn(n,f,"_deleted"));
	} else {
	    return(getColumn(n,f,null));
	}
    }

    public Object getColumn(Edge e, Field f) {
	if (e.getGraph() == null) {
	    return(getColumn(e,f,"_deleted"));
	} else {
	    return(getColumn(e,f,null));
	}
    }

    public Object getColumn(Node n, Field f, String s) {
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    st = conn.createStatement();   
	    if (s == null) {
		rs = st.executeQuery("SELECT " + f.getName() + 
				     " from nodes where name = '" + 
				     n.getName() + "'");
	    } else {
		rs = st.executeQuery("SELECT " + f.getName() + 
				     " from nodes_"+s+" where name = '" + 
				     n.getName() + "'");
	    }
	    while(rs.next()) {
		Object o = rs.getObject(1);
		st.close();
		return(o);
	    }
	    return(null); // or should we throw an error?
	} catch (Exception e) {
	    throw new Error(e.toString());
	}
    }

    public Object getStatistic(Field f, String s) {
	return(getStatistic(f,s,null));
    }

    public Object getStatistic(Field f, String s, String state) {
	try {
	    Statement st = null;
	    ResultSet rs = null;

	    if (state != null) {
		state = "_"+state;
	    } else {
		state = "";
	    }

	    st = conn.createStatement();   
	    if (f.getType() == Field.NODE) {
		rs = st.executeQuery("SELECT " + s + "("+f.getName() + 
				     ") from nodes"+state);
	    } else {
		rs = st.executeQuery("SELECT " + s + "("+f.getName() + 
				     ") from edges"+state);
	    }
	    while(rs.next()) {
		Object o = rs.getObject(1);
		st.close();
		return(o);
	    }
	    return(null); // or should we throw an error?
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    public Object getColumn(Edge e, Field f, String s) {
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    st = conn.createStatement();  
	    if (s == null) {
		rs = st.executeQuery("SELECT " + f.getName() + 
				     " from edges where __EDGEID = " + 
				     e.getID());
	    } else {
		rs = st.executeQuery("SELECT " + f.getName() + 
				     " from edges_"+s+" where __EDGEID = " + 
				     e.getID());
	    }
	    while(rs.next()) {
		Object o = rs.getObject(1);
		st.close();
		return(o);
	    }
	    return(null); // or should we throw an error?
	} catch (Exception ex) {
	    throw new Error(ex.toString());
	}
    }

    public Object getColumn(Node n, Field f, int s) {
	return(getColumn(n,f,""+s));
    }

    public Object getColumn(Edge e, Field f, int s) {
	return(getColumn(e,f,""+s));
    }

    public Object[] getColumns(Node n, Field[] f) {
	return(getColumns(n,f,null));
    }

    public Object[] getColumns(Edge e, Field[] f) {
	return(getColumns(e,f,null));
    }

    public Object[] getColumns(Edge e, Field[] f, String s) {
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    st = conn.createStatement(); 
	    StringBuffer toSelect = new StringBuffer();
	    for (int i = 0 ; i < f.length ; i++) {
		if (i == 0) {
		    toSelect.append(f[i].getName());
		} else {
		    toSelect.append(","+f[i].getName());
		}
	    }
	    if (s == null) {
		// value from the current state
		rs = st.executeQuery("SELECT " + toSelect.toString() + 
				     " from edges where __EDGEID = " + 
				     e.getID());
	    } else {
		// value from another state
		rs = st.executeQuery("SELECT " + toSelect.toString() + 
				     " from edges_"+s+" where __EDGEID = " + 
				     e.getID());
	    }
	    Object[] toRet = new Object[f.length];
	    while(rs.next()) {
		for (int i = 0 ; i < f.length ; i++) {
		    toRet[i] = rs.getObject(i+1);
		}
		st.close();
		return(toRet);
	    }
	    return(null); // or should we throw an error?
	} catch (Exception ex) {
	    throw new Error(ex.toString());
	}
    }

    public Object[] getColumns(Node n, Field[] f, String s) {
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    st = conn.createStatement(); 
	    StringBuffer toSelect = new StringBuffer();
	    for (int i = 0 ; i < f.length ; i++) {
		if (i == 0) {
		    toSelect.append(f[i].getName());
		} else {
		    toSelect.append(","+f[i].getName());
		}
	    }
	    //System.out.println(toSelect.toString());
	    if (s == null) {
		rs = st.executeQuery("SELECT " + toSelect.toString() + 
				     " from nodes where name = '" + 
				     n.getName() + "'");
	    } else {
		rs = st.executeQuery("SELECT " + toSelect.toString() + 
				     " from nodes_"+s+" where name = '" + 
				     n.getName() + "'");
		//System.out.println("SELECT " + toSelect.toString() + 
		//	     " from nodes_"+s+" where name = '" + 
		//	     n.getName() + "'");
	    
	    }
	    Object[] toRet = new Object[f.length];
	    while(rs.next()) {
		for (int i = 0 ; i < f.length ; i++) {
		    toRet[i] = rs.getObject(i+1);
		}
		st.close();
		return(toRet);
	    }
	    return(null); // or should we throw an error?
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	    throw new Error(ex.toString());
	}
    }

    public Object[] getColumns(Node n, Field[] f, int s) {
	return(getColumns(n,f,""+s));
    }

    public Object[] getColumns(Edge e, Field[] f, int s) {
	return(getColumns(e,f,""+s));
    }

    public PreparedStatement getStatement(String prep) throws SQLException {

	// let's see if we've cached the statement
	PreparedStatement st = (PreparedStatement)preparedStatements.get(prep);

	if (st != null) {
	    return(st);
	}

	if (conn != null) {
	    st = conn.prepareStatement(prep);
	    preparedStatements.put(prep,st);
	    return(st);
	} else {
	    throw new Error("no connection to the database");
	}
    }

    public void closeStatements() {
	Iterator it = preparedStatements.values().iterator();
	while(it.hasNext()) {
	    try {
		PreparedStatement ps = (PreparedStatement)it.next();
		ps.close();
	    } catch (Exception e) {
		
		ExceptionWindow.getExceptionWindow(e);
		continue;
	    }
	}
    }
    
    public void fillSchema(NodeSchema schema, Graph g) {
	fillSchemaInternal(schema,g);
    }

    public void fillSchema(EdgeSchema schema, Graph g) {
	fillSchemaInternal(schema,g);
    }

    private void fillSchemaInternal(Schema schema,Graph g) {
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = null;
	    int type = Field.NODE;
	    if (schema instanceof NodeSchema) {
		rs = stmt.executeQuery("SELECT * from nodes");
	    } else {
		rs = stmt.executeQuery("SELECT * from edges");
		type = Field.EDGE;
	    }
	    
	    // first lets get the working columns for the table
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int colmax = rsmd.getColumnCount();
	    int i;
	    Object o = null;
	    
	    String[] names = new String[colmax];
	    int[] types = new int[colmax];
	    
	    StringBuffer sb = new StringBuffer();
	    for (i = 0; i < colmax; ++i) {
		names[i] = rsmd.getColumnName(i+1).toLowerCase();
		if (i >= 1)
		    sb.append(",");
		sb.append(names[i]);
		types[i] = rsmd.getColumnType(i+1);
	    }
	    stmt.close();

	    stmt = conn.createStatement();    
	    if (type == Field.NODE) {
		rs = stmt.executeQuery("SELECT " +
				     sb.toString() + " FROM nodes_def");
	    } else {
		rs = stmt.executeQuery("SELECT " +
				     sb.toString() + " FROM edges_def");
	    }
	    
	    while(rs.next()) {
		for (i = 0 ; i < colmax; ++i) {
		    schema.addField(new Field(g,names[i],
					      type,types[i],
					      rs.getObject(i+1)));
		}
	    }
	    stmt.close();

	    // next lets get the default values

	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    public void updateColumn(Node n, Field f, Object value) {
	if (!commitState)
	    return;

	String extra = "";
	if (n.getGraph() == null) {
	    extra = "__deleted";
	}

	try {
	    PreparedStatement st = 
		getStatement("UPDATE nodes"+extra+" SET " + f.getName() + 
			     " = ? WHERE name = ?");
	    updateColumn(st,f,value,1);
	    st.setString(2,n.getName());
	    st.executeUpdate();
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    public void updateColumn(Edge n, Field f, Object value) {
	if (!commitState)
	    return;

	String extra = "";
	if (n.getGraph() == null) {
	    extra = "__deleted";
	}

	try {
	    PreparedStatement st = 
		getStatement("UPDATE edges"+extra+" SET " + f.getName() + 
			     " = ? WHERE __EDGEID = ?");
	    updateColumn(st,f,value,1);
	    st.setInt(2,n.getID());
	    st.executeUpdate();
	} catch (Exception e) {
	    throw new Error(e.toString());
	}
    }


    public void addField(Field f) {
	try {
	    if (Helper.isBadName(f.getName())) {
		System.out.println("\n\nWARNING! field name \"" + f.getName() + "\" may conflict with a restricted word\n\n");
	    }
	    if (conn != null) {
		StringBuffer sb = new StringBuffer("");
		StringBuffer sb2 = new StringBuffer("ALTER TABLE ");
		if (f.getType() == Field.NODE) { 
		    //sb.append("nodes ");
		    sb2.append("nodes_def ");
		} else if (f.getType() == Field.EDGE) {
		    //sb.append("edges ");
		    sb2.append("edges_def ");
		} else {
		    throw new Error("Unsuported field type");
		}

		sb.append("ADD COLUMN " + f.getName() + " " + 
			  getTypeString(f.getSQLType()) + " ");
		sb2.append("ADD COLUMN " + f.getName() + " " + 
			  getTypeString(f.getSQLType()) + " ");
		
		if (f.getDefault() != null) {
		    if ((f.getSQLType() == Types.VARCHAR) ||
			(f.getSQLType() == Types.CHAR) ||
			(f.getSQLType() == Types.DATE) ||
			(f.getSQLType() == Types.TIME) ||
			(f.getSQLType() == Types.TIMESTAMP) ||
			(f.getSQLType() == Types.LONGVARCHAR)) {
			sb.append("DEFAULT '"+f.getDefault()+"\'");
			sb2.append("DEFAULT '"+f.getDefault()+"\'");
		    } else {
			sb.append("DEFAULT "+f.getDefault());
			sb2.append("DEFAULT "+f.getDefault());
		    }
		    //  System.out.println(sb.toString());
		} 
		//	System.out.println(sb.toString());

		// iterate on getAllStates
		Iterator it = getAllStates().iterator();
		while (it.hasNext()) {
		    // insert the new field into ever state
		    // we need to be able to move between states
		    // without crashing
		    String st = (String)it.next();
		    //System.out.println("state: " + st);
		    if (f.getType() == Field.NODE) { 
			update("ALTER TABLE nodes" + st + " " + sb.toString());
			//sb.append("nodes ");
			//sb2.append("nodes_def ");
		    } else if (f.getType() == Field.EDGE) {
			update("ALTER TABLE edges" + st + " " + sb.toString());
			//sb.append("edges ");
			//sb2.append("edges_def ");
		    } else {
			throw new Error("Unsuported field type");
		    }
		}

		// update the default table
		update(sb2.toString());
	    } else {
		throw new Error("no connection to the database");
	    }
	} catch (Exception e) {
	    throw new Error(e.toString());
	}
    }
    
    private void updateColumn(PreparedStatement st,
			      Field f, Object value, int column) {

	if (!commitState)
	    return;

	try {
	    if (value == null) {
		st.setNull(column,java.sql.Types.NULL);
		return;
	    }
	    switch (f.getSQLType()) {
	    case java.sql.Types.ARRAY:
	        st.setArray(column,(Array)value);
	        break;
	    case java.sql.Types.BIGINT:
	        st.setInt(column,((Integer)value).intValue());
	        break;
	    case java.sql.Types.BINARY:
	        st.setBoolean(column,((Boolean)value).booleanValue());
	        break;
	    case java.sql.Types.BIT:
	        st.setBoolean(column,((Boolean)value).booleanValue());
	        break;
	    case java.sql.Types.BOOLEAN:
	        st.setBoolean(column,((Boolean)value).booleanValue());
	        break;
	    case java.sql.Types.BLOB:
	        st.setBlob(column,(Blob)value);
	        break;
	    case java.sql.Types.CHAR:
	        st.setString(column,value.toString());
	        break;
	    case java.sql.Types.CLOB:
	        st.setClob(column,(Clob)value);
	        break;
	    case java.sql.Types.DATE:
	        st.setDate(column,(java.sql.Date)value);
	        break;
	    case java.sql.Types.DECIMAL:
	        st.setBigDecimal(column,(java.math.BigDecimal)value);
	        break;
	    case java.sql.Types.DISTINCT:
                throw new SQLException("Unsuported Type");
	    case java.sql.Types.DOUBLE:
	        st.setDouble(column,((Double)value).doubleValue());
	        break;
	    case java.sql.Types.FLOAT:
	        st.setDouble(column,((Double)value).doubleValue());
	        break;
	    case java.sql.Types.INTEGER:
	        st.setInt(column,((Integer)value).intValue());
	        break;
	    case java.sql.Types.JAVA_OBJECT:
	        st.setObject(column,value);
	        break;
  	    case java.sql.Types.LONGVARBINARY:
                throw new SQLException("Unsuported Type");
  	    case java.sql.Types.LONGVARCHAR:
	        st.setString(column,value.toString());
	        break;
  	    case java.sql.Types.NULL:
	        st.setNull(column,java.sql.Types.NULL);
	        break;
  	    case java.sql.Types.NUMERIC:
	        st.setBigDecimal(column,(java.math.BigDecimal)value);
	        break;
  	    case java.sql.Types.OTHER:
                throw new SQLException("Unsuported Type");
  	    case java.sql.Types.REAL:
	        st.setDouble(column,((Double)value).doubleValue());
	        break;
  	    case java.sql.Types.REF:
                throw new SQLException("Unsuported Type");
  	    case java.sql.Types.SMALLINT:
	        st.setInt(column,((Integer)value).intValue());
	        break;
  	    case java.sql.Types.STRUCT:
	        st.setArray(column,(Array)value);
	        break;
  	    case java.sql.Types.TIME:
	        st.setTime(column,(Time)value);
		break;
  	    case java.sql.Types.TIMESTAMP:
		st.setTimestamp(column,(Timestamp)value);
		break;
  	    case java.sql.Types.TINYINT:
	        st.setInt(column,((Integer)value).intValue());
	        break;
  	    case java.sql.Types.VARBINARY:
                throw new SQLException("Unsuported Type");
	    case java.sql.Types.VARCHAR:
	        st.setString(column,value.toString());
	        break;
	    default:
                throw new SQLException("Unsuported Type");
	    }   
	} catch (Exception e) {
	    
	    System.out.println("error: " + st.toString());
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    public static String getTypeString(int type) {
	switch (type) {
	case java.sql.Types.ARRAY:
	    return("ARRAY");
	case java.sql.Types.BIGINT:
	    return("BIGINT");
	case java.sql.Types.BINARY:
	    return("BINARY");
	case java.sql.Types.BIT:
	    return("BIT");
	case java.sql.Types.BOOLEAN:
	    return("BOOLEAN");
	case java.sql.Types.BLOB:
	    return("BLOB");
	case java.sql.Types.CHAR:
	    return("CHAR");
	case java.sql.Types.CLOB:
	    return("CLOB");
	case java.sql.Types.DATE:
	    return("DATE");
	case java.sql.Types.DECIMAL:
	    return("DECIMAL");
	case java.sql.Types.DISTINCT:
	    throw new Error("Unsuported Type");
	case java.sql.Types.DOUBLE:
	    return("DOUBLE");
	case java.sql.Types.FLOAT:
	    return("FLOAT");
	case java.sql.Types.INTEGER:
	    return("INTEGER");
	case java.sql.Types.JAVA_OBJECT:
	    return("JAVA_OBJECT");
	case java.sql.Types.LONGVARBINARY:
	    throw new Error("Unsuported Type");
	case java.sql.Types.LONGVARCHAR:
	    return("LONGVARCHAR");
	case java.sql.Types.NULL:
	    return("NULL");
	case java.sql.Types.NUMERIC:
	    return("NUMERIC");
	case java.sql.Types.OTHER:
	    throw new Error("Unsuported Type");
	case java.sql.Types.REAL:
	    return("REAL");
	case java.sql.Types.REF:
	    throw new Error("Unsuported Type");
	case java.sql.Types.SMALLINT:
	    return("SMALLINT");
	case java.sql.Types.STRUCT:
	    return("STRUCT");
	case java.sql.Types.TIME:
	    return("TIME");
	case java.sql.Types.TIMESTAMP:
	    return("TIMESTAMP");
	case java.sql.Types.TINYINT:
	    return("TINYINT");
	case java.sql.Types.VARBINARY:
	    throw new Error("Unsuported Type");
	case java.sql.Types.VARCHAR:
	    return("VARCHAR");
	default:
	    throw new Error("Unsuported Type");
	}
    }

    public DBServer(String db_file_name_prefix) throws Exception    
    {
	
        Class.forName("org.hsqldb.jdbcDriver");

	if (db_file_name_prefix.equals(".")) {
	    conn = DriverManager.getConnection("jdbc:hsqldb:mem:aname",
					       "sa",                 
					       "");    
	} else {
	    conn = DriverManager.getConnection("jdbc:hsqldb:file:"
					       + db_file_name_prefix,
					       "sa",                 
					       "");    
	}
    }

    public void shutdownConn() throws SQLException {

        conn.close();   
    }

    /**
     * @pyexport sql
     */
    public synchronized void q(String expression) throws SQLException {
	
        Statement st = null;
        ResultSet rs = null;

        st = conn.createStatement();
	if ((expression.startsWith("SELECT")) || 
	    (expression.startsWith("select"))) {
	    rs = st.executeQuery(expression); 
	    try {
		dump(rs);
	    } catch (Exception e) {
		
		ExceptionWindow.getExceptionWindow(e);
	    }
	} else {
	    try {
		st.executeUpdate(expression);
	    } catch (Exception e) {
		
		ExceptionWindow.getExceptionWindow(e);
	    }
	}
        st.close();
    }

    public synchronized void saveCSV(String filename, String expression) 
	throws Exception {
	BufferedWriter bw = new BufferedWriter(new FileWriter(filename)); 
        Statement st = null;
        ResultSet rs = null;
	
        st = conn.createStatement();
        rs = st.executeQuery(expression);       // run the query

        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o       = null;

	for (i = 0; i < colmax; ++i) {
	    String colName = meta.getColumnName(i+1);
	    if (i == colmax - 1) {
		bw.write(colName);
	    } else {
		bw.write(colName + ",");
	    }
	}
	bw.write("\n");
        for (;rs.next();) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);    // Is SQL the first column
                                            // is indexed with 1 not 0
		String toStr = "NULL";
		if (o != null)
		    toStr = o.toString();
		if (toStr.indexOf(",") != -1) {
		    toStr = "\""+toStr+"\"";
		}
		if (i == colmax - 1) {
		    bw.write(toStr);
		} else {
		    bw.write(toStr+ ",");
		}
            }
	    bw.write("\n");
        }
        st.close();
	bw.close();
    }

    /**
     * executes the query specified by expression
     * @param expression
     */
    public synchronized void query(String expression) throws SQLException {
	
	//System.out.println(expression);

        Statement st = null;
        ResultSet rs = null;
 
        st = conn.createStatement();
        rs = st.executeQuery(expression); 
        st.close();
    }

    public synchronized int identity() throws SQLException {
	
        Statement st = null;
        ResultSet rs = null;

        st = conn.createStatement();
        rs = st.executeQuery("CALL IDENTITY()"); 
	int toRet = -1;
	while(rs.next()) {
	    try {
		toRet = rs.getInt(1);
	    } catch (SQLException e1) {
	    }
	}
        st.close();
	return(toRet);
    }


    public synchronized void update(String expression, Statement st) 
	throws SQLException {
	//System.out.println(expression);
        int i = st.executeUpdate(expression);       // run the query

        if (i == -1) {
            System.out.println("db error : " + expression);
        }


    }

    public synchronized void update(String expression) throws SQLException {
	
	Statement st = conn.createStatement();
	update(expression,st);
        st.close();
    }   


    public synchronized String prettyPrintResult(String query) {
	try {
	    
	    //System.out.println(query);
	    Statement st = null;
	    ResultSet rs = null;
	    
	    st = conn.createStatement(); 
	    rs = st.executeQuery(query);
	    
	    StringBuffer toRet = new StringBuffer();
	    
	    ResultSetMetaData meta   = rs.getMetaData();
	    int               colmax = meta.getColumnCount();
	    int               i;
	    Object            o       = null;
	    
	    for (;rs.next();) {
		for (i = 0; i < colmax; ++i) {
		    o = rs.getObject(i + 1); 
		    String colName = meta.getColumnName(i+1);
		    if (o != null)
			toRet.append(colName + "\t" + o.toString() + "\n");
		}
		toRet.append("\n");
	    }
	    st.close();
	    return(toRet.toString());
	} catch (SQLException e) {
	    //ExceptionWindow.getExceptionWindow(e)
	    return("unable to find match");
	}
    }

    public static void dump(ResultSet rs) throws SQLException {

	if (rs == null) {
	    return;
	}

        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o       = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop 
        for (;rs.next();) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);    // Is SQL the first column
                                            // is indexed with 1 not 0
		if (o != null) 
		    System.out.print(o.toString() + " ");
            }

            System.out.println(" ");
        }
    }                                       //void dump( ResultSet rs
					    //)

    public void getNodeColumn(Hashtable ht,
			      String column,
			      String limit) throws Exception {
	Statement st = null;
	ResultSet rs = null;
	
	st = conn.createStatement();    
	String where = "";
	if (limit != null) {
	    where = " WHERE " + limit;
	}
	rs = st.executeQuery("SELECT name,"+column+" from nodes" + where);   
	while(rs.next()) {
	    String name = rs.getString("name");
	    Object o = rs.getObject(2);
	    //	    Node gn = (Node)nodeHash.get(name);
	    //if (gn != null) 
	    ht.put(name,o);
	}
	st.close(); 
    }

    private String getStateString(Set s) {
	StringBuffer sb = new StringBuffer();
	Iterator it = s.iterator();
	while(it.hasNext()) {
	    String st = (String)it.next();
	    sb.append(st);
	    if (it.hasNext())
		sb.append(",");
	}
	return(sb.toString());
    }

    private String getDisambigString(Set s, String col) {
	StringBuffer sb = new StringBuffer();
	Iterator it = s.iterator();
	String prev = null;
	while(it.hasNext()) {
	    String st = (String)it.next();
	    if (prev != null) {
		sb.append(" AND ("+prev+"."+col+"="+st + "."+col+")");
	    }
	    prev = st;
	}
	return(sb.toString());
    }

    public void findMatchingNodes(Query q) {
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    HashSet hs = new HashSet();
	    
	    st = conn.createStatement();    

	    Set s = q.getStates(null);
	    //s.add("nodes");
	    String tables = getStateString(s);
	    
	    Iterator it = s.iterator();
	    String first = (String)it.next();

	    String disamb = "";
	    if (s.size() > 1) {
		disamb = getDisambigString(s,"name");
	    }

	    //System.out.println("SELECT "+first+"."+
	    //	       "name FROM "+tables+" WHERE " + 
	    //	       q.toSQLString());

	    //System.out.println("SELECT DISTINCT "+first+"."+
	    //	       "name FROM "+tables+" WHERE " + 
	    //	       q.toSQLString()+disamb);

	    rs = st.executeQuery("SELECT DISTINCT "+first+"."+
				 "name FROM "+tables+" WHERE " + 
				 q.toSQLString()+disamb);

	    while(rs.next()) {
		String name = rs.getString("name");
		Node gn = q.getGraph().getNodeByName(name);
		if (gn == null) {
		    gn = (Node)unusedNodes.get(name);
		}
		//if (gn != null) 
		//System.out.println(gn);
		q.append(new PyJavaInstance(gn));
	    }
	    st.close();
	} catch (Exception e) {
	    throw new Error(e.toString());
	}
    }

    public void findMatchingEdges(Query q) {
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    HashSet hs = new HashSet();
	    
	    st = conn.createStatement();    
	    Set s = q.getStates(null);
	    //s.add("nodes");
	    String tables = getStateString(s);

	    String disamb = "";
	    if (s.size() > 1) {
		disamb = getDisambigString(s,"__EDGEID");
	    }

	    Iterator it = s.iterator();
	    String first = (String)it.next();

	    //System.out.println("SELECT "+first+"."+
	    //	       "__EDGEID FROM "+tables+" WHERE " + 
	    //	       q.toSQLString() + disamb);

	    rs = st.executeQuery("SELECT "+first+"."+
			       "__EDGEID FROM "+tables+" WHERE " + 
			       q.toSQLString() + disamb);

	    while(rs.next()) {
		int name = rs.getInt("__EDGEID");
		Edge gn = q.getGraph().getEdgeByID(new Integer(name));
		if (gn == null) {
		    gn = (Edge)unusedEdges.get(new Integer(name));
		}
		//System.out.println(q + " " + gn + " " + name);
		q.append(new PyJavaInstance(gn));
	    }
	    st.close();
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    public void refresh(Graph g) {

	//Thread.dumpStack();

	setCommitState(false);
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    st = conn.createStatement();
	    rs = st.executeQuery("SELECT name,label,x,y,visible,"+
				 "color,fixed,style,width,height,"+
				 "labelvisible,labelcolor,strokecolor,"+
				 "image from nodes"); 
	    
	    Hashtable map = new Hashtable();
	    while(rs.next()) {
		String name = rs.getString("name");
		double x = rs.getDouble("x");
		double y = rs.getDouble("y");
		String color = rs.getString("color");
		String labelcolor = rs.getString("labelcolor");
		String strokecolor = rs.getString("strokecolor");
		boolean vis = rs.getBoolean("visible");
		String label = rs.getString("label");
		boolean labelvis = rs.getBoolean("labelvisible");
		boolean fixed = rs.getBoolean("fixed");
		int style = rs.getInt("style");
		double width = rs.getDouble("width");
		double height = rs.getDouble("height");
		String image = rs.getString("image");

		// is it in the database
		Node n = g.getNodeByName(name);

		if (n == null) {
		    // is it around but unused?
		    n = (Node)unusedNodes.get(name);
		    if (n != null) {
			unusedNodes.remove(name);
		    }
		}
		if (n == null) {
		    // no? just make a new one
		    //System.out.println("making node: " + name);
		    n = new Node(style,x,y,width,height,name);
		    g.addNode(n);
		} else {
		    // ok, it's around, let's just fix it up
		    if (n.getGraph() == null) 
			g.addNode(n);

		    n.__setattr__("style",new Integer(style));
		    n.__setattr__("x",new Double(x));
		    n.__setattr__("y",new Double(y));
		    n.__setattr__("width",new Double(width));
		    n.__setattr__("height",new Double(height));
		}
		
		//System.out.println("label: *" + label + "*");
		if (label != null) {
		    n.__setattr__("label",label);
		} else {
		    n.__setattr__("label",name);
		}

		n.__setattr__("color",color);
		n.__setattr__("strokecolor",strokecolor);
		n.__setattr__("fixed",new Boolean(fixed));
		n.__setattr__("visible",new Boolean(vis));
		n.__setattr__("labelvisible",new Boolean(labelvis));
		n.__setattr__("image",image);
	    }

	    st.close(); 
	    
	    //int max = 4;
	    st = conn.createStatement(); 
	    rs = st.executeQuery("SELECT __EDGEID,node1,node2,visible,color,"+
				 "width,weight,directed,label,labelvisible,"+
				 "labelcolor from edges");
	    while(rs.next()) {
		String src = rs.getString("node1");
		String dest = rs.getString("node2");
		boolean vis = rs.getBoolean("visible");
		String color = rs.getString("color");
		String labelcolor = rs.getString("labelcolor");
		double width = rs.getDouble("width");
		boolean directed = rs.getBoolean("directed");
		double weight = rs.getDouble("weight");
		String label = rs.getString("label");
		boolean labelvis = rs.getBoolean("labelvisible");
		int id = rs.getInt("__EDGEID");

		//System.out.println("figuring out: " + src + " " + dest);
		Edge e = g.getEdgeByID(new Integer(id));
		if (e == null) {
		    //  System.out.println("\tnot in graph");
		    e = (Edge)unusedEdges.get(new Integer(id));
		    if (e != null) {
			//System.out.println("\tin unused edges");
			unusedEdges.remove(new Integer(id));
		    }
		} 
		if (e == null) {
		    //	    System.out.println("\tcreating new");
		    Node s = (Node)g.getNodeByName(src);
		    Node d = (Node)g.getNodeByName(dest);
		    if ((s == null) || (s.getGraph() == null)) {
			System.err.println("\tedge contains undefined node \"" + 
					   src + "\"");
			continue;
		    } else if ((d == null) || (d.getGraph() == null)) {
			System.err.println("\tedge contains undefined node \"" + 
					   dest + "\"");
			continue;
		    }
		    if (!directed) {
			//System.out.println("\tnew undirected edge");
			e = new UndirectedEdge(id, s, d);
			g.addEdgeNoCheck((Edge)e);
		    } else {
			e = new DirectedEdge(id, s, d);
			g.addEdgeNoCheck((Edge)e);
		    }
		} else {
		    //System.out.println("\tfound it somewhere");
		    // not in the graph
		    Node s = (Node)g.getNodeByName(src);
		    Node d = (Node)g.getNodeByName(dest);
		    if ((s == null) || (s.getGraph() == null)) {
			System.err.println("\tedge contains undefined node " + 
					   src);
			if (e.getGraph() != null) {
			    ((Graph)e.getGraph()).removeEdge(e);
			}
			unusedEdges.put(new Integer(id),e);
			continue;
		    } else if ((d == null) || (d.getGraph() == null)) {
			System.err.println("\tedge contains undefined node " + 
					   dest);
			if (e.getGraph() != null) {
			    ((Graph)e.getGraph()).removeEdge(e);
			}
			unusedEdges.put(new Integer(id),e);
			continue;
		    }
		}

		if (e.getGraph() == null)
		    g.addEdgeNoCheck((Edge)e);

		if (label != null) {
		    e.__setattr__("label",label);
		} else {
		    e.__setattr__("label",""+weight);
		}

		e.__setattr__("width",new Double(width));
		e.__setattr__("weight",new Double(weight));
		e.__setattr__("visible",new Boolean(vis));
		e.__setattr__("color",color);

		if (labelcolor != null)
		    e.__setattr__("labelcolor",labelcolor);

		e.__setattr__("labelvisible",new Boolean(labelvis));

		//e = g.getEdgeByID(new Integer(id));
	    }
	    st.close();
	} catch (ConstraintViolationException e) {
	    System.out.println("\n*** You likely have a duplicate edge, try using the -m option\n");
	    ExceptionWindow.getExceptionWindow(e);
	    setCommitState(true);
	    g.resetLastMod();
	    throw new Error(e);
	} catch (Throwable e2) {
	    ExceptionWindow.getExceptionWindow(e2);
	    setCommitState(true);
	    g.resetLastMod();
	    throw new Error(e2);
	}
	setCommitState(true);
	g.resetLastMod();
    }

    public static DBServer init(String dbname) throws Exception {
	//org.hsqldb.Trace.DOASSERT = false;

	if (singleton != null) {
	    return(singleton);
	}
	
	singleton = new DBServer(dbname);
	try {
	    singleton.query("SET WRITE_DELAY TRUE");
	} catch (SQLException ex2) {
	    ExceptionWindow.getExceptionWindow(ex2);
	}
	return(singleton);
    }

    private boolean inmem = false;

    public static DBServer getDBServer() {
	try {
	    if (singleton == null) {
		initInMemory();
	    }
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	}
	return(singleton);
    }

    public static DBServer initInMemory() throws Exception {

	if (singleton != null) {
	    return(singleton);
	}
	
	singleton = new DBServer(".");
	singleton.inmem = true;
	return(singleton);
    }

    public static void resetSingleton() {
	singleton = null;
    }

    public void shutdown() {
	resetSingleton();

	StatusBar.setStatus("Shutting down database");
	StatusBar.runProgressBar(true);

	closeStatements();
	try {
	    update("SHUTDOWN");
	} catch (Exception ex4) {
	    ExceptionWindow.getExceptionWindow(ex4);
	}
	try {
	    shutdownConn();
	} catch (Exception ex3) {
	    ExceptionWindow.getExceptionWindow(ex3);
	}
	StatusBar.runProgressBar(false);
    }

    public void alter(String columnname, String query) {
	try {
	    update(query);
	    //System.out.println(query);
	} catch (SQLException e) {
	    //e.printStackTrace();
	    String t = e.toString();
	    if (t.indexOf("Column already exists") != -1) {
		//System.out.println("Column " + columnname + " exists");
	    } else {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}
    }



    public static Hashtable nodedefs = new Hashtable();

    static {
	nodedefs.put("name","NAME VARCHAR(32) PRIMARY KEY");
	nodedefs.put("x","X DOUBLE DEFAULT 500");
	nodedefs.put("y","Y DOUBLE DEFAULT 500");
	nodedefs.put("visible","VISIBLE BOOLEAN DEFAULT true");
	nodedefs.put("color","COLOR VARCHAR(32) DEFAULT 'cornflowerblue'");
	nodedefs.put("strokecolor",
		     "STROKECOLOR VARCHAR(32) DEFAULT 'cadetblue'");
	nodedefs.put("labelcolor","LABELCOLOR VARCHAR(32) DEFAULT NULL");
	nodedefs.put("fixed","FIXED BOOLEAN DEFAULT false");
	nodedefs.put("style","STYLE TINYINT DEFAULT 2");
	nodedefs.put("width","WIDTH DOUBLE DEFAULT 10");
	nodedefs.put("height","HEIGHT DOUBLE DEFAULT 10");
	nodedefs.put("label","LABEL VARCHAR(256) DEFAULT NULL");
	nodedefs.put("image","IMAGE VARCHAR(256) DEFAULT NULL");
	nodedefs.put("labelvisible","LABELVISIBLE BOOLEAN DEFAULT FALSE");
    }

    public static Hashtable edgedefs = new Hashtable();

    static {
	edgedefs.put("color","COLOR VARCHAR(32) DEFAULT 'dandelion'");
	edgedefs.put("labelcolor","LABELCOLOR VARCHAR(32) DEFAULT NULL");
	edgedefs.put("visible","VISIBLE BOOLEAN DEFAULT true");
	edgedefs.put("__edgeid","__EDGEID INT IDENTITY PRIMARY KEY");
	edgedefs.put("width","width DOUBLE DEFAULT 2");
	edgedefs.put("weight","weight DOUBLE DEFAULT 1");
	edgedefs.put("directed","directed BOOLEAN DEFAULT 0");
	edgedefs.put("node1","node1 VARCHAR(32) DEFAULT '' NOT NULL");
	edgedefs.put("node2","node2 VARCHAR(32) DEFAULT '' NOT NULL");
	edgedefs.put("label","LABEL VARCHAR(256) DEFAULT NULL");
	edgedefs.put("labelvisible","LABELVISIBLE BOOLEAN DEFAULT FALSE");
    }

    public static String fixString(String init,Hashtable defs) {
	String s = init.trim();
	StringBuffer toRet = new StringBuffer();
	
	String[] foo = s.split(",");
	for (int i = 0 ; i < foo.length ; i++) {
	    String t = foo[i].trim().toLowerCase();
	    String[] subelem = t.split(" ");
	    if (defs.containsKey(subelem[0])) {
		toRet.append(defs.get(subelem[0]));
	    } else {
		toRet.append(foo[i]);
	    }
	    if (Helper.isBadName(subelem[0])) {
		System.out.println("\n\nWARNING! field name \"" + subelem[0] + "\" may conflict with a restricted word\n\n");
	    }
	    if (i < foo.length - 1) {
		toRet.append(",");
	    }
	}
	return(toRet.toString());
    }

    private static void processNodeDef(DBServer db, String def) 
	throws Exception {
	String fixed = fixString(def,nodedefs);
	//System.out.println(fixed);
	db.update("CREATE CACHED TABLE nodes"+"("+fixed+")");

	tableList.clear();

	// make sure we have the necessary columns

	db.alter("constraining names",
	      "ALTER TABLE nodes"+
	      " ADD UNIQUE (NAME)");

	db.alter("x","ALTER TABLE NODES"+
		 " ADD COLUMN X DOUBLE DEFAULT 500");
	db.alter("y",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN Y DOUBLE DEFAULT 500");
	db.alter("visible",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN visible BOOLEAN default true");
	db.alter("color",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN color VARCHAR(32) default 'cornflowerblue'");
	db.alter("strokecolor",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN strokecolor VARCHAR(32) default 'cadetblue'");
	db.alter("labelcolor",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN labelcolor VARCHAR(32) default NULL");
	db.alter("fixed",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN fixed BOOLEAN default false");
	db.alter("style",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN style TINYINT default 2");
	db.alter("width",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN width DOUBLE default 10");
	db.alter("height",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN height DOUBLE default 10");
	db.alter("name",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN name VARCHAR(32) default ''");
	db.alter("label",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN label VARCHAR(32) default NULL");
	db.alter("image",
	      "ALTER TABLE nodes"+
	      " ADD COLUMN image VARCHAR(32) default NULL");
	db.alter("labelvisible",
		 "ALTER TABLE nodes"+
		 " ADD COLUMN labelvisible BOOLEAN DEFAULT false");

	// we need to create a default nodes table
	// it's only going to hold around one fake node with 
	// default values
	db.update("CREATE CACHED TABLE nodes_def"+"("+fixed+")");
	tableList.clear();

	db.alter("constraining names",
	      "ALTER TABLE nodes_def"+
	      " ADD UNIQUE (NAME)");
	db.alter("x",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN X DOUBLE DEFAULT 500");
	db.alter("y",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN Y DOUBLE DEFAULT 500");
	db.alter("visible",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN visible BOOLEAN default true");
	db.alter("color",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN color VARCHAR(32) default 'cornflowerblue'");
	db.alter("strokecolor",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN strokecolor VARCHAR(32) default 'cadetblue'");
	db.alter("labelcolor",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN labelcolor VARCHAR(32) default NULL");
	db.alter("fixed",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN fixed BOOLEAN default false");
	db.alter("style",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN style TINYINT default 2");
	db.alter("width",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN width DOUBLE default 10");
	db.alter("height",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN height DOUBLE default 10");
	db.alter("name",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN name VARCHAR(32) default ''");
	db.alter("label",
	      "ALTER TABLE nodes_def"+
	      " ADD COLUMN label VARCHAR(32) default NULL");
	db.alter("image",
		 "ALTER TABLE nodes_def"+
		 " ADD COLUMN image VARCHAR(32) default NULL");
	db.alter("labelvisible",
		 "ALTER TABLE nodes_def"+
		 " ADD COLUMN labelvisible BOOLEAN DEFAULT false");

	db.query("INSERT INTO nodes_def(name) values('default')");
    }

    private static void processEdgeDef(DBServer db, String def) 
	throws Exception {
	
	String fixed = fixString(def,edgedefs);
	if (fixed.indexOf("__EDGEID") == -1) {
	    fixed = fixed + ",__EDGEID INT IDENTITY PRIMARY KEY";
	}
	db.update("CREATE CACHED TABLE edges"+"("+fixed+")");
	tableList.clear();

	// make sure we have the columns we need
	db.alter("visible",
	      "ALTER TABLE edges"+
	      " ADD COLUMN visible BOOLEAN default true");
	db.alter("color",
	      "ALTER TABLE edges"+
	      " ADD COLUMN color VARCHAR(32) default 'dandelion'");
	db.alter("labelcolor",
	      "ALTER TABLE edges"+
	      " ADD COLUMN labelcolor VARCHAR(32) default NULL");
	db.alter("width",
	      "ALTER TABLE edges"+
	      " ADD COLUMN width DOUBLE default 2");
	db.alter("weight",
	      "ALTER TABLE edges"+
	      " ADD COLUMN weight DOUBLE default 1");
	db.alter("directed",
	      "ALTER TABLE edges"+
	      " ADD COLUMN directed BOOLEAN default 0");
	db.alter("node1",
	      "ALTER TABLE edges"+
	      " ADD COLUMN node1 VARCHAR default ''");
	db.alter("node2",
	      "ALTER TABLE edges"+
	      " ADD COLUMN node2 VARCHAR default ''");
	db.alter("label",
	      "ALTER TABLE edges"+
	      " ADD COLUMN label VARCHAR(32) default NULL");
	db.alter("labelvisible",
		 "ALTER TABLE edges"+
		 " ADD COLUMN labelvisible BOOLEAN DEFAULT false");

	db.update("CREATE CACHED TABLE edges_def"+"("+fixed+")");
	tableList.clear();

	// make sure we have the columns we need
	db.alter("visible",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN visible BOOLEAN default true");
	db.alter("color",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN color VARCHAR(32) default 'dandelion'");
	db.alter("labelcolor",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN labelcolor VARCHAR(32) default NULL");
	db.alter("width",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN width DOUBLE default 2");
	db.alter("weight",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN weight DOUBLE default 1");
	db.alter("directed",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN directed BOOLEAN default 0");
	db.alter("node1",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN node1 VARCHAR default ''");
	db.alter("node2",
	      "ALTER TABLE edges_def"+
	      " ADD COLUMN node2 VARCHAR default ''");
	db.alter("label",
		 "ALTER TABLE edges_def"+
		 " ADD COLUMN label VARCHAR(32) default NULL");
	db.alter("labelvisible",
		 "ALTER TABLE edges_def"+
		 " ADD COLUMN labelvisible BOOLEAN DEFAULT false");

	db.query("INSERT INTO edges_def(node1,node2) values('default','default')");
    }

    public void loadFromFile(String text) {
	try {
	    // check to see if this is a file that exists
	    File f = new File(text);
	    if (f.exists()) {
		BufferedReader br = new BufferedReader(new FileReader(text)); 
		loadFromFile(br);
		br.close();
	    }
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	}
    }
    
    public void loadFromText(String text) {
	try {
	    BufferedReader br = new BufferedReader(new StringReader(text));
	    loadFromFile(br);
	    br.close();
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public void createEmpty() {  
	try {
	    String s = "nodedef> name\nedgedef> node1,node2\n";
	    loadFromFile(new BufferedReader(new StringReader(s)));
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public static String[] stringSplit(String line) {
	// not very optimized, but we shouldn't do this that often
	if ((line.indexOf("'") >= 0) ||
	    (line.indexOf("\"") >= 0) ||
	    (line.indexOf("\\") >= 0)) {
	    char[] chars = new char[line.length()];
	    line.getChars(0,chars.length,chars,0);
	    boolean inQuote = false;
	    char quoteChar = '\'';
	    char slashChar = '\\';
	    Vector toReturn = new Vector();
	    StringBuffer curString = null;
	    for (int i = 0 ; i < chars.length ; i++) {
		//System.out.println(chars[i]);
		if (chars[i] == slashChar) {
		    if (chars[i+1] == 'n') {
			curString.append("\n");
			i++;
			continue;
		    } else if (chars[i+1] == 't') {
			curString.append("\t");
			i++;
			continue;
		    }
		}
		if (inQuote) {
		    if (chars[i] == quoteChar) {
			inQuote = false;
			//System.out.println(curString);
			if (curString != null)
			    toReturn.addElement(curString.toString());
			curString = null;
			continue;
		    } 
		} else {
		    if (chars[i] == ',') {
			//System.out.println(curString);
			if (curString != null) {
			    toReturn.addElement(curString.toString());
			} else if ((i > 0) && 
				   (chars[i-1] == ',')) {
			    toReturn.addElement("");
			}
			curString = null;
			continue;
		    } else if ((chars[i] == '\'') ||
			       (chars[i] == '\"')) {
			inQuote = true;
			quoteChar = chars[i];
			continue;
		    }
		}
		if (curString == null)
		    curString = new StringBuffer();
		curString.append(chars[i]);
	    }
	    if ((curString != null) ||
		(line.charAt(line.length() - 1) == ',')) {
		toReturn.addElement(curString.toString());
		//System.out.println(curString);
	    }
	    String[] toR = new String[toReturn.size()];
	    toReturn.copyInto(toR);
	    return(toR);
	} else {	
	    return(line.split(","));
	}
    }

    private void loadFromFile(BufferedReader br) 
	throws Exception {

	DBServer db = this;

	HashSet seenEdge = new HashSet();

	try {
            db.query("SET AUTOCOMMIT TRUE");
        } catch (SQLException ex2) {
	    ExceptionWindow.getExceptionWindow(ex2);
	}
	
	
        try {
            db.query("DROP TABLE nodes");
        } catch (SQLException ex2) {
	}
	
        try {
            db.query("DROP TABLE edges");
        } catch (SQLException ex2) {
	}

        try {
            db.query("DROP TABLE nodes_def");
        } catch (SQLException ex2) {
	}
	
        try {
            db.query("DROP TABLE edges_def");
        } catch (SQLException ex2) {
	}
	
	try {
	    db.query("SET WRITE_DELAY TRUE");
	} catch (SQLException ex2) {
	    ExceptionWindow.getExceptionWindow(ex2);
	}
	
	String line = null;
	boolean inNodeDef = false;
	boolean inEdgeDef = false;
	
	String[] nnames = null;
	int[] ntypes = null;
	String[] enames = null;
	int[] etypes = null;
	
	int nodecount = 0;
	int edgecount = 0;
	
	Random rand = new Random();
	
	boolean lookupID = false;
	int node1Column = 0;
	int node2Column = 0;
	int directedColumn = -1;
	int nameColumn = 0;

	int lineNum = 0;

	while ((line = br.readLine()) != null) {
	    line = line.trim();
	    lineNum++;
	    if ((line.startsWith("#")) || (line.equals(""))) {
		continue;
	    }
	    if (line.startsWith("nodedef>")) {
		inEdgeDef = false;
		inNodeDef = true;
		String def = line.substring(8);
		try {
//		    System.out.println("Found node definitions: " + def);
		    processNodeDef(db,def);

		    // let's deposit a fake graph node in so we 
		    // can do some tests
		    Statement stmt = db.conn.createStatement();
		    ResultSet rs = 
			stmt.executeQuery("SELECT * from nodes");
		    ResultSetMetaData rsmd = rs.getMetaData();
		    int colmax = rsmd.getColumnCount();
		    int i;
		    Object o = null;
		   
		    //System.out.println(colmax);
		    // keep track of the column names/types so we 
		    // can quote things correctly
		    nnames = new String[colmax];
		    ntypes = new int[colmax];
		    for (i = 0; i < colmax; ++i) {
			nnames[i] = rsmd.getColumnName(i+1);
			ntypes[i] = rsmd.getColumnType(i+1);
			if (nnames[i].equalsIgnoreCase("name")) {
			    nameColumn = i;
			}
		    }
		    stmt.close();
		} catch (SQLException ex2) {
		    //ex2.printStackTrace();
		    ExceptionWindow.getExceptionWindow(ex2);
		    System.out.println("*** failed to create db correctly, make sure you have defined the required columns (line: "+lineNum+")");
		    //shutdown();
		    return;
		}
	    } else if (line.startsWith("edgedef>")) {
		try {
		    inEdgeDef = true;
		    inNodeDef = false;
		    String def = line.substring(8);
//		    System.out.println("Found edge definitions: " + def);
		    processEdgeDef(db,def);
		    
		    // lets do a little test
		    Statement stmt = db.conn.createStatement();
		    ResultSet rs = 
			stmt.executeQuery("SELECT * from edges");
		    ResultSetMetaData rsmd = rs.getMetaData();
		    int colmax = rsmd.getColumnCount();
		    int i;
		    Object o = null;
		    enames = new String[colmax];
		    etypes = new int[colmax];
		    for (i = 0; i < colmax; ++i) {
			enames[i] = rsmd.getColumnName(i+1);
			if (enames[i].equalsIgnoreCase("node1")) {
			    node1Column = i;
			} else if (enames[i].equalsIgnoreCase("node2")) {
			    node2Column = i;
			} else if (enames[i].equalsIgnoreCase("directed")) {
			    directedColumn = i;
			} 
			etypes[i] = rsmd.getColumnType(i+1);
		    }
		    stmt.close();
		} catch (SQLException ex2) {
		    ExceptionWindow.getExceptionWindow(ex2);
		    System.out.println("*** failed to create db correctly, make sure you have defined the required columns (line: "+lineNum+")");
		    //shutdown();
		    return;
		}
	    } else {
		if (inNodeDef) {
		    try {
			StringBuffer into = new StringBuffer();
			StringBuffer values = new StringBuffer();
			//		System.out.println(line);
			//Pattern pat = Pattern.compile(",");
			
			String[] vals = stringSplit(line);
			//StringTokenizer st = new StringTokenizer(line,
			//",");
			//System.out.println(st.countTokens());
			int i = -1;
			boolean xl = false;
			boolean yl = false;
			String node1 = null;
			for (int z = 0 ; z < vals.length ; z++) {
			    //while(st.hasMoreTokens()) {
			    i++;
			    String val = vals[z];
			    //System.out.println(i + " " + val);
			    if (val.equals("")) {
				//if (val.equalsIgnoreCase("NULL")) {
				continue;
			    } else {
				if (i == nameColumn) {
				    node1 = val;
				}
				into.append(","+nnames[i]);
				if ((ntypes[i] == Types.VARCHAR) ||
				    (ntypes[i] == Types.CHAR) ||
				    (ntypes[i] == Types.TIMESTAMP) ||
				    (ntypes[i] == Types.DATE) ||
				    (ntypes[i] == Types.TIME) ||
				    (ntypes[i] == Types.LONGVARCHAR)) {
				    values.append(",");
				    if (!val.startsWith("'")) 
					values.append("'");
				    values.append(val);
				    if (!val.endsWith("'")) 
					values.append("'");
				} else {
				    values.append(","+val);
				}
				//System.out.println(nnames[i]);
				if (nnames[i].equalsIgnoreCase("X")) {
				    xl = true;
				} else if (nnames[i].equalsIgnoreCase("Y")) {
				    yl = true;
				} 
			    }
			    //System.out.println(into + " " + values);
			}
			if (!xl) {
			    // x undefined, let's make a random one
			    into.append(",X");
			    values.append(","+(rand.nextDouble()*500));
			}
			if (!yl) {
			    // y undefined, let's make a random one
			    into.append(",Y");
			    values.append(","+(rand.nextDouble()*500));
			}
			if (Helper.isBadName(node1)) {
			    System.out.println("\n\nWARNING! node name \"" + node1 + "\" may conflict with a restricted\n word or character  (line: "+lineNum+")\n\n");
			}
			String a = into.toString();
			String b = values.toString();
			a = a.substring(1);
			b = b.substring(1);
			db.query("INSERT INTO nodes"+"("+a+") VALUES(" + 
				 b + ")");
			//System.out.println(a + "\n" + b);
			nodecount++;
		    } catch (Exception e3) {
			System.out.println("problem with: " + line + " (line: "+lineNum+")");
			ExceptionWindow.getExceptionWindow(e3);
			continue;
		    }
		} else if (inEdgeDef) {
		    try {
			StringBuffer into = new StringBuffer();
			StringBuffer values = new StringBuffer();
			//StringTokenizer st = new StringTokenizer(line,",");
			String[] vals = stringSplit(line);
			int i = -1;
			String node1 = null;
			String node2 = null;
			String directed = null;
			for (int z = 0 ; z < vals.length ; z++) {
			    //while(st.hasMoreTokens()) {
			    i++;
			    String val = vals[z];
			    if (val.equals("")) {
				//) || 
				//(val.equalsIgnoreCase("NULL"))) {
				continue;
			    } else {
				into.append(","+enames[i]);
				
				if (i == node1Column) {
				    node1 = val;
				} else if (i == node2Column) {
				    node2 = val;
				} else if (i == directedColumn) {
				    directed = val;
				}
					
				if ((etypes[i] == Types.VARCHAR) ||
				    (etypes[i] == Types.CHAR) ||
				    (etypes[i] == Types.TIMESTAMP) ||
				    (etypes[i] == Types.DATE) ||
				    (etypes[i] == Types.TIME) ||
				    (etypes[i] == Types.LONGVARCHAR)) {
				    values.append(",");
				    if (!val.startsWith("'")) 
					values.append("'");
				    values.append(val);
				    if (!val.endsWith("'")) 
					values.append("'");

				} else {
				    values.append(","+val);
				}
			    }
			}
			
			String sedge1 = null;
			String sedge2 = null;

			if (directed == null) {
			    sedge1 = node1 + "-" + node2;
			    if (!node1.equals(node2))
				sedge2 = node2 + "-" + node1;
			} else {
			    if ((directed.equalsIgnoreCase("false")) ||
				(directed.equals("0"))) {
				sedge1 = node1 + "-" + node2;
				if (!node1.equals(node2))
				    sedge2 = node2 + "-" + node1;
			    } else {
				sedge1 = node1 + "->" + node2;
			    }
			}

			if (sedge1 != null) {
			    sedge1 = sedge1.toLowerCase();
			    if ((seenEdge.contains(sedge1) && 
				 (!Guess.allowMultiEdge()))) {
				System.out.println("\nWARNING! Duplicate edge " + sedge1 + " ignored (line: "+lineNum+")\nConsider using the -m option to enable multiple edges.");
				continue;
			    }
			    seenEdge.add(sedge1);
			} 
			if (sedge2 != null) {
			    sedge2 = sedge2.toLowerCase();
			    if (seenEdge.contains(sedge2) && 
				(!Guess.allowMultiEdge())) {
				System.out.println("\nWARNING! Duplicate edge " + sedge2 + " ignored (line: "+lineNum+")\nConsider using the -m option to enable multiple edges.");
				continue;
			    }
			    seenEdge.add(sedge2);
			}

			String a = into.toString();
			String b = values.toString();
			//System.out.println(a + " " + b);
			a = a.substring(1);
			b = b.substring(1);
			db.query("INSERT INTO edges"+"("+a+") VALUES(" + 
				 b + ")");
			edgecount++;
			//int id = db.identity();
			//System.out.println(id);
		    } catch (Exception e3) {
			System.out.println("problem with: " + line + "  (line: "+lineNum+")");
			ExceptionWindow.getExceptionWindow(e3);
			continue;
		    }
		} else {
		    System.out.println("Your database definition file may "+
				       "have a problem in it, not sure what "+
				       "to do with:\n"+line+ " (line: "+lineNum+")");
		}
	    }
	}
	System.out.println("\nLoaded " + nodecount + 
			   " nodes and " + edgecount + " edges");
	return;
    }

    public void saveState(int statenum) {
	saveState(""+statenum);
    }

    public void saveState(String statenum) {
	
	tableList.clear();

        try {
            query("DROP TABLE nodes_"+statenum);
        } catch (SQLException ex2) {
	}

        try {
            query("DROP TABLE edges_"+statenum);
        } catch (SQLException ex2) {
	}

	try {
	    //System.out.println("\tSaving nodes...");
	    
	    //String create = "SELECT nodes.name, nodes.x, nodes.y, "+
	    //  "nodes.vis, nodes.color, nodes.size, nodes.shape, "+
	    //  "nodes.fixed INTO nodes_"+statenum+
	    //  " FROM nodes";
	    
	    String create = "SELECT nodes.* INTO nodes_"+statenum+
		" FROM nodes";
	    
	    query(create);
	    
	    //	    System.out.println("\tSaving edges...");
	    
	    //create = "SELECT edges.edgeid, edges.node1, edges.node2, "+
	    //"edges.color, edges.vis, edges.width, edges.weight, "+
	    //  "edges.directed INTO edges_"+statenum+" FROM edges";
	    
	    create = "SELECT edges.* INTO edges_"+statenum+" FROM edges";
	    query(create);
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	}
	StatusBar.setState(statenum);
    }

    Hashtable unusedEdges = new Hashtable();
    Hashtable unusedNodes = new Hashtable();

    public Collection getRemovedNodes() {
	return(unusedNodes.values());
    }

    public Collection getRemovedEdges() {
	return(unusedEdges.values());
    }

    public Vector getNodesNotInCurrent(Graph g, int statenum) {
	return(getNodesNotInCurrent(g,""+statenum));
    }

    public Vector getNodesNotInCurrent(Graph g, String statenum) {
	Vector toReturn = new Vector();
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    HashSet hs = new HashSet();
	    
	    st = conn.createStatement();    
	    rs = st.executeQuery("SELECT name from nodes_"+
				 statenum +
				 " minus select name from nodes");
	    
	    while(rs.next()) {
		String name = rs.getString("name");
		Node gn = (Node)unusedNodes.get(name);
		if (gn == null) {
		    System.out.println("I can't find an edge in the cache, did you load all the states before running a morph?");
		}
		toReturn.addElement(gn);
		//System.out.println("node not in current: " + 
		//	   name);
	    }
	    st.close();
	} catch (Exception e) {
	    throw new Error(e.toString());
	}
	return(toReturn);
    }

    public Vector getEdgesNotInCurrent(Graph g, int statenum) {
	return(getEdgesNotInCurrent(g,""+statenum));
    }

    public Vector getEdgesNotInCurrent(Graph g, String statenum) {
	Vector toReturn = new Vector();
	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    HashSet hs = new HashSet();
	    
	    st = conn.createStatement();    
	    rs = st.executeQuery("SELECT __EDGEID from edges_"+
				 statenum +
				 " minus select __EDGEID from edges");
	    
	    while(rs.next()) {
		int eid = rs.getInt("__EDGEID");
		Edge ge = (Edge)unusedEdges.get(new Integer(eid));
		if (ge == null) {
		    System.out.println("I can't find an edge in the cache, did you load all the states before running a morph?");
		}
		toReturn.addElement(ge);
		//System.out.println("node not in current: " + 
		//	   name);
	    }
	    st.close();
	} catch (Exception e) {
	    throw new Error(e.toString());
	}
	return(toReturn);
    }

    public void loadState(int state) {
	loadState(Guess.getGraph(),state);
    }

    public void loadState(String state) {
	loadState(Guess.getGraph(),state);
    }

    public void loadState(Graph g, int statenum) {
	loadState(g,""+statenum);
    }

    public void loadState(Graph g, String statenum) {


	// lets deal with edges

	// step one, find/remove the edges that are in the state we're 
	// switching to but not the previous one

	setCommitState(false);

	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    HashSet hs = new HashSet();
	    
	    st = conn.createStatement();    
	    rs = st.executeQuery("SELECT __EDGEID from edges"+
				 " minus select __EDGEID from edges_"+
				 statenum);
	    
	    while(rs.next()) {
		int name = rs.getInt("__EDGEID");
		Edge ge = g.getEdgeByID(new Integer(name));
		unusedEdges.put(new Integer(name),ge);
		//System.out.println("removing edge: " + name);
		ge.__setattr__("visible", Boolean.FALSE);
		g.removeEdge(ge);
	    }
	    st.close();
	} catch (Exception e) {
	    setCommitState(true);
	    throw new Error(e.toString());
	}

	// step two, find/remove the nodes that are in the state we're 
	// switching to but not the previous one

	try {
	    Statement st = null;
	    ResultSet rs = null;
	    
	    HashSet hs = new HashSet();
	    
	    st = conn.createStatement();    
	    rs = st.executeQuery("SELECT name from nodes"+
				 " minus select name from nodes_"+
				 statenum);
	    
	    while(rs.next()) {
		String name = rs.getString("name");
		Node gn = g.getNodeByName(name);
		gn.__setattr__("visible", Boolean.FALSE);
		unusedNodes.put(name,gn);
		g.removeNode(gn);
		//System.out.println("removing node: " + name);
	    }
	    st.close();
	} catch (Exception e) {
	    setCommitState(true);
	    throw new Error(e.toString());
	}
	
	setCommitState(true);

	// step 3, overwrite current table with new state
        try {
            query("DROP TABLE nodes");
        } catch (SQLException ex2) {
	}

        try {
            query("DROP TABLE edges");
        } catch (SQLException ex2) {
	}
	
	//System.out.println("\tSaving nodes...");
	
	String create = "SELECT nodes_"+statenum+".* INTO nodes"+
	    " FROM nodes_"+statenum;

	try {
	    query(create);
	} catch (SQLException ex2) {
	    ExceptionWindow.getExceptionWindow(ex2);
	}

	//System.out.println("\tSaving edges...");

	create = "SELECT edges_"+statenum+".* INTO edges"+
	    " FROM edges_"+statenum;

	try {
	    query(create);
	} catch (SQLException ex2) {
	    ExceptionWindow.getExceptionWindow(ex2);
	}


	// step 4, read contraints
	NodeSchema ns = g.getNodeSchema();
	Enumeration en = ns.getFields();
	while(en.hasMoreElements()) {
	    try {
		Field f = (Field)en.nextElement();
		//System.out.println("f: " + f.getName() + " " + f.getDefault());
		if (f.getDefault() != null) {
		    if ((f.getSQLType() == Types.VARCHAR) ||
			(f.getSQLType() == Types.CHAR) ||
			(f.getSQLType() == Types.DATE) ||
			(f.getSQLType() == Types.TIME) ||
			(f.getSQLType() == Types.TIMESTAMP) ||
			(f.getSQLType() == Types.LONGVARCHAR)) {
			update("ALTER TABLE NODES ALTER COLUMN " + 
			       f.getName() + " SET DEFAULT '"+
			       f.getDefault()+"\'");
		    } else {
			update("ALTER TABLE NODES ALTER COLUMN " + 
			       f.getName() + " SET DEFAULT '"+
			       f.getDefault()+"\'");
		    }
		    //  System.out.println(sb.toString());
		} 
	    } catch (Exception e) {
		
		ExceptionWindow.getExceptionWindow(e);
		continue;
	    }
	}

	// do the same for edges
	EdgeSchema es = g.getEdgeSchema();
	en = es.getFields();
	while(en.hasMoreElements()) {
	    try {
		Field f = (Field)en.nextElement();
		if (f.getDefault() != null) {
		    if ((f.getSQLType() == Types.VARCHAR) ||
			(f.getSQLType() == Types.CHAR) ||
			(f.getSQLType() == Types.DATE) ||
			(f.getSQLType() == Types.TIME) ||
			(f.getSQLType() == Types.TIMESTAMP) ||
			(f.getSQLType() == Types.LONGVARCHAR)) {
			update("ALTER TABLE EDGES ALTER COLUMN " + 
			       f.getName() + " SET DEFAULT '"+
			       f.getDefault()+"\'");
		    } else {
			update("ALTER TABLE EDGES ALTER COLUMN " + 
			       f.getName() + " SET DEFAULT '"+
			       f.getDefault()+"\'");
		    }
		    //  System.out.println(sb.toString());
		} 
	    } catch (Exception e) {
		
		ExceptionWindow.getExceptionWindow(e);
		continue;
	    }
	}

	// reload the nodes/edges into memory

	refresh(g);

	StatusBar.setState(statenum);
    }

    public static void main(String[] args) throws Exception {

	if (args.length == 2) {
	    DBServer db = init(args[0]);
	    db.loadFromFile(args[1]);
	    if (db != null)
		db.shutdown();
	    System.exit(0);
	}
    }

    public boolean containsEdge(Edge e) {
	return(containsEdge(e,null));
    }

    public boolean containsEdge(Edge e, String state) {
	try {

	    if (!containsTable(EDGE,state))
		return(false);

	    Statement st = null;
	    ResultSet rs = null;
	    
	    if (state == null) {
		state = "";
	    } else {
		state = "_"+state;
	    }

	    st = conn.createStatement();  
	    rs = st.executeQuery("SELECT * from edges"+
				 state +" where __EDGEID = " + 
				 e.getID());
	    while(rs.next()) {
		return(true);
	    }
	    return(false);
	} catch (Exception ex) {
	    throw new Error(ex.toString());
	}
    }

    public boolean containsNode(Node n) {
	return(containsNode(n,null));
    }

    public boolean containsNode(Node n, String state) {
	try {

	    if (!containsTable(NODE,state))
		return(false);

	    Statement st = null;
	    ResultSet rs = null;
	    
	    if (state == null) {
		state = "";
	    } else {
		state = "_"+state;
	    }

	    st = conn.createStatement();  
	    rs = st.executeQuery("SELECT * from nodes"+state+ 
				 " where name = '" + 
				 n.getName() + "'");
	    while(rs.next()) {
		return(true);
	    }
	    return(false);
	} catch (Exception ex) {
	    throw new Error(ex.toString());
	}
    }

    public void addEdge(Edge e) {
	//System.out.println("in add edge");
	try {
	    
	    if (e instanceof UndirectedEdge) {
		query("INSERT INTO edges(__edgeid,node1,node2,directed) "
		      + "VALUES(" + e.getID() + "," + 
		      "'"+e.getNode1().getName()+"',"+
		      "'"+e.getNode2().getName()+"',false)");
	    } else {
		query("INSERT INTO edges(__edgeid,node1,node2,directed) "
		      + "VALUES(" + e.getID() + "," + 
		      "'"+e.getNode1().getName()+"',"+
		      "'"+e.getNode2().getName()+"',true)");
	    }
	} catch (Exception ex) {
	    throw(new Error(ex.toString() + " " + e));
	}
	//System.out.println("done adding edge");
    }

    public int createDirectedEdge(Node source, Node dest)
    {
	try {
	    query("INSERT INTO edges(node1,node2,directed) VALUES(" + 
		     "'"+source.getName()+"',"+
		     "'"+dest.getName()+"',true)");
	    return identity();
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    public int createDirectedEdge(Node source, Node dest, int id)
    {
	try {
	    query("INSERT INTO edges(__edgeid,node1,node2,directed) VALUES(" +
		  id + "," +
		  "'"+source.getName()+"',"+
		  "'"+dest.getName()+"',true)");
	    return id;
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    public int createUndirectedEdge(Node source, Node dest, int id)
    {
	try {
	    query("INSERT INTO edges(__edgeid,node1,node2,directed) VALUES(" +
		  id + "," +
		  "'"+source.getName()+"',"+
		  "'"+dest.getName()+"',false)");
	    return id;
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }
    
    public int createUndirectedEdge(Node source, Node dest)
    {
	try {
	    query("INSERT INTO edges(node1,node2,directed) VALUES(" + 
		     "'"+source.getName()+"',"+
		     "'"+dest.getName()+"',false)");
	    return identity();
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }
    
    public void undelete(Edge e) {
	try {
	    unusedEdges.remove(new Integer(e.getID()));
	    
	    if (containsTable(EDGE,"_deleted")) {
		if (containsEdge(e,"_deleted")) {
		    query("DELETE FROM edges WHERE __edgeid = " + 
			  e.getID() +"");
		    
		    String create = "INSERT INTO edges select * "+
			"from edges__deleted where __edgeid = " + 
			e.getID()
			+ "";
		    
		    query(create);
		    
		    query("DELETE FROM edges__deleted WHERE __edgeid = " + 
			  e.getID() +"");
		    
		    return;
		}
	    }
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	    throw new Error(ex.toString());
	}
    }

    public void undelete(Node node) {
	try {
	    unusedNodes.remove(node.getName());
	    
	    if (containsTable(NODE,"_deleted")) {
		if (containsNode(node,"_deleted")) {
		    query("DELETE FROM nodes WHERE name = '" + 
			  node.getName() +"'");
		    
		    String create = "INSERT INTO nodes select * "+
			"from nodes__deleted where name = '" + 
			node.getName() 
			+ "'";
		    
		    query(create);
		    
		    query("DELETE FROM nodes__deleted WHERE name = '" + 
			  node.getName() +"'");
		    
		    return;
		}
	    }
	} catch (Exception e) {
	    
	    ExceptionWindow.getExceptionWindow(e);
	    throw new Error(e.toString());
	}
    }

    //Adds the given node to the database.
    public void addNode(Node node)
    {
	if (Helper.isBadName(node.getName())) {
	    System.out.println("\n\nWARNING! node name \"" + 
			       node.getName() + 
			       "\" may conflict with a restricted\n"+
			       " word or character.\n\n");
	}
	try
	    {
		query("INSERT INTO nodes(name) VALUES(" + 
		      "'"+node.getName()+"')");
	    }
	catch (Exception e)
	    {
		
		ExceptionWindow.getExceptionWindow(e);
		throw new Error(e.toString());
	    }
    }

    public void remove(Edge edge)
    {
	try
	    {
		String create = null;
		// does the deleted table exist?
		// TODO? ADD SOME BULLETPROOFING
		if (containsTable(EDGE,"_deleted")) {
		    // create it, this is where we put 
		    // edges that aren't connected to any graph
		    // we want to be able to recover those back
		    create = "DELETE FROM edges__deleted where __edgeid = " + 
			edge.getID();
		    query(create);
		    create = "INSERT INTO edges__deleted select * "+
			"from edges where __edgeid = " + edge.getID() + "";
		    query(create);
		} else {
		    create = "SELECT edges.* INTO edges__deleted " +
			" FROM edges where __edgeid = " + edge.getID() + "";
		    query(create);
		    alter("constraining edges",
			  "ALTER TABLE edges__deleted"+
			  " ADD UNIQUE (__EDGEID)");
		    tableList.clear();
		}
		query("DELETE FROM edges WHERE __edgeid = " + edge.getID());
		unusedEdges.put(new Integer(edge.getID()),edge);
	    }
	catch (Exception e)
	    {
		
		ExceptionWindow.getExceptionWindow(e);
		throw new Error(e.toString());
	    }
    }
    
    public void removeComplete(Edge edge) {
	Set s = getStates();

	if (containsTable(EDGE,"_deleted"))
	    s.add("_deleted");

	try {
	    query("DELETE FROM edges WHERE __EDGEID = " + edge.getID());
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
	Iterator it = s.iterator();
	while(it.hasNext()) {
	    try {
		query("DELETE FROM edges_"+it.next()+
		      " WHERE __EDGEID = " + edge.getID());
	    } catch (Exception ex) {
		ExceptionWindow.getExceptionWindow(ex);
		continue;
	    }
	}
    }

    public void removeComplete(Node node) {
	Set s = getStates();

	if (containsTable(NODE,"_deleted"))
	    s.add("_deleted");

	try {
	    query("DELETE FROM nodes WHERE name = '" + node.getName() +"'");
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
	Iterator it = s.iterator();
	while(it.hasNext()) {
	    try {
		query("DELETE FROM nodes_"+it.next()+
		      " WHERE name = '" + node.getName() + "'");
	    } catch (Exception ex) {
		ExceptionWindow.getExceptionWindow(ex);
		continue;
	    }
	}
    }

    public void remove(Node node) {
	try
	    {
		String create = null;
		// does the deleted table exist?
		// TODO? ADD SOME BULLETPROOFING
		if (containsTable(NODE,"_deleted")) {
		    // create it, this is where we put 
		    // nodes that aren't connected to any graph
		    // we want to be able to recover those back
		    create = "DELETE FROM nodes__deleted where name = '" + 
			node.getName() + "'";
		    query(create);
		    create = "INSERT INTO nodes__deleted select * "+
			"from nodes where name = '" + node.getName() + "'";
		    query(create);
		} else {
		    create = "SELECT nodes.* INTO nodes__deleted " +
			" FROM nodes where name = '" + node.getName() + "'";
		    query(create);
		    alter("constraining names",
			  "ALTER TABLE nodes__deleted"+
			  " ADD UNIQUE (NAME)");
		    tableList.clear();
		}

		query("DELETE FROM nodes WHERE name = '" + 
		      node.getName() + "'");

		unusedNodes.put(node.getName(),node);
	    }
	catch (Exception e)
	    {
		
		ExceptionWindow.getExceptionWindow(e);
		throw new Error(e.toString());
	    }
    }

    public AbstractTableModel getNodeTable() {
	return(new SpreadSheetTable(this,true));
    }

    public AbstractTableModel getEdgeTable() {
	return(new SpreadSheetTable(this,false));
    }

    public void exportGDF(String filename) {
	try {
	    PrintStream gdf = 
		new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)));
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * from nodes");
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int colmax = rsmd.getColumnCount();
	    int i;
	    Object o = null;
	    
	    String name = null;
	    String type = null;
	    
	    gdf.print("nodedef>");
	    for (i = 0; i < colmax; ++i) {
		name = rsmd.getColumnName(i+1).toLowerCase();
		type = getTypeString(rsmd.getColumnType(i+1));
		if (i >= 1)
		    gdf.print(",");
		gdf.print(name + " " + type);
	    }
	    gdf.print("\n");

	    for (;rs.next();) {
		for (i = 0; i < colmax; ++i) {
		    o = rs.getObject(i + 1);
		    if (i >= 1)
			gdf.print(",");
		    if ((o != null) && (!o.toString().equals(""))) {
			String test = o.toString();
			if (test.indexOf(",") >= 0) {
			    gdf.print("'");
			    gdf.print(o.toString());
			    gdf.print("'");
			} else {
			    gdf.print(o.toString());
			}
		    } else {
			//gdf.print("null");
		    }
		}
		gdf.print("\n");
	    }
	    stmt.close();


	    stmt = conn.createStatement();
	    rs = stmt.executeQuery("SELECT * from edges");
	    rsmd = rs.getMetaData();
	    colmax = rsmd.getColumnCount();
	    o = null;
	    
	    name = null;
	    type = null;
	    
	    gdf.print("edgedef>");

	    for (i = 0; i < colmax; ++i) {
		name = rsmd.getColumnName(i+1).toLowerCase();
		type = getTypeString(rsmd.getColumnType(i+1));
		if (i >= 1)
		    gdf.print(",");
		gdf.print(name + " " + type);
	    }
	    gdf.print("\n");

	    for (;rs.next();) {
		for (i = 0; i < colmax; ++i) {
		    o = rs.getObject(i + 1);
		    if (i >= 1)
			gdf.print(",");
		    if ((o != null) && (!o.toString().equals(""))) {
			String test = o.toString();
			if (test.indexOf(",") >= 0) {
			    gdf.print("'");
			    gdf.print(o.toString());
			    gdf.print("'");
			} else {
			    gdf.print(o.toString());
			}
		    } else {
			//gdf.print("null");
		    }
		}
		gdf.print("\n");
	    }
	    stmt.close();
	    gdf.close();
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
    }

}
