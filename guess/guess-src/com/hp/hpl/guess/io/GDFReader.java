package com.hp.hpl.guess.io;

import java.sql.*;
import java.util.*;
import java.io.*;
import org.python.core.*;

import com.hp.hpl.guess.*;
import com.hp.hpl.guess.storage.*;
import com.hp.hpl.guess.db.*;
import com.hp.hpl.guess.ui.ExceptionWindow;
import com.hp.hpl.guess.ui.StatusBar;
import javax.swing.table.AbstractTableModel;
import edu.uci.ics.jung.exceptions.ConstraintViolationException;
import com.hp.hpl.guess.db.*;

public class GDFReader {

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

    private static String fixString(String init,Hashtable defs) {
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
		System.err.println("\n\nWARNING! field name \"" + subelem[0] + "\" may conflict with a restricted word\n\n");
	    }
	    if (i < foo.length - 1) {
		toRet.append(",");
	    }
	}
	return(toRet.toString());
    }

    private Field[] processNodeDef(Graph g, String s) {
	return(processDef(g,s,g.getNodeSchema()));
    }

    private Field[] processEdgeDef(Graph g, String s) {
	return(processDef(g,s,g.getEdgeSchema()));
    }

    private Field[] processDef(Graph g, String s, Schema ns) {
	//System.out.println(s);
    	String[] foo = s.split(",");
	Field[] toRet = new Field[foo.length];
	for (int i = 0 ; i < foo.length ; i++) {
	    String t = foo[i].trim().toLowerCase();
	    String[] subelem = t.split(" ");
	    String attrName = subelem[0];
	    Field existingF = ns.getField(attrName);
	    Integer attrType =  null;
	    if (existingF != null) {
		// field already exists in the system
		attrType = new Integer(existingF.getSQLType());
	    }

	    String def = null;

	    for (int j = 1 ; j < subelem.length ; j++) {
		// handle not nulls?
		if (subelem[j].equals("default")) {
		    def = subelem[j+1];
		    j++;
		    continue;
		} else {
		    if (attrType == null) {
			attrType = new Integer(getSQLType(subelem[j]));
		    }
		}
	    }

	    if (attrType == null) {
		// trouble, report error
		throw new RuntimeException("");
	    }
	    Field f = ns.getField(attrName);
	    if (f != null) {
		// field already defined
		Object o = convertToType(f.getSQLType(),def);
		if (o != null)
		    f.setDefault(o);
		if (f.getSQLType() != attrType.intValue()) {
		    String error = attrName + 
			", current SQL type: " +
			f.getSQLType() + 
			"requested type: " + 
			attrType;
		    if (ns instanceof NodeSchema) {
			ExceptionWindow.getExceptionWindow(new Exception("Possible type conflict on: Node."+error));
		    } else {
			ExceptionWindow.getExceptionWindow(new Exception("Possible type conflict on: Edge."+error));
		    }
		}
	    } else {
		//System.out.println(attrType);
		if (ns instanceof NodeSchema) {
		    f = g.addNodeField(attrName,attrType.intValue(),
				       convertToType(attrType.intValue(),def));
		} else if (ns instanceof EdgeSchema) {
		    f = g.addEdgeField(attrName,attrType.intValue(),
				       convertToType(attrType.intValue(),def));
		}
	    }
	    //	    System.out.println(f);
	    toRet[i] = f;
	}
	return(toRet);
    }

    private Object convertToType(int attrType,String def) {
	if (def == null) {
	    return(null);
	}
	//	def = def.trim();
	try {
	    if ((attrType == Types.BIT) ||
		(attrType == Types.BOOLEAN)) {
		if ((def.equalsIgnoreCase("true")) ||
		    (def.equalsIgnoreCase("1"))) {
		    return(Boolean.TRUE);
		} else {
		    return(Boolean.FALSE);
		}
	    } else if ((attrType == Types.INTEGER) || 
		       (attrType == Types.TINYINT) ||
		       (attrType == Types.SMALLINT)) {
		return(new Integer(Integer.parseInt(def)));
	    } else if (attrType == Types.DOUBLE) {
		return(new Double(Double.parseDouble(def)));
	    } else if (attrType == Types.FLOAT) {
		return(new Float(Float.parseFloat(def)));
	    } else if (attrType == Types.VARCHAR) {
		return(def);
	    } else {
		System.out.println("Treating unknown type " + attrType + 
				   " as String");
		return(def);
	    }
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
	return(null);
    }

    public GDFReader(Graph g, String file) throws IOException {
	BufferedReader br = new BufferedReader(new FileReader(file));

	String line = null;
	boolean inNodeDef = false;
	boolean inEdgeDef = false;
	
	int nodecount = 0;
	int edgecount = 0;
	
	Random rand = new Random();
	
	Field[] nodeCols = null;
	Field[] edgeCols = null;

	int nNameCol = -1;
	int eNode1Col = -1;
	int eNode2Col = -1;
	int eIDCol = -1;
	int eDirCol = -1;

	boolean randomLayout = true;

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
		nodeCols = processNodeDef(g,def);
		//System.out.println("node cols length: " + nodeCols.length);
		for (int i = 0 ; i < nodeCols.length ; i++) {
		    if (nodeCols[i].getName().equalsIgnoreCase("name")) {
			nNameCol = i;
		    } else if (nodeCols[i].getName().equalsIgnoreCase("x")) {
			randomLayout = false;
		    } else if (nodeCols[i].getName().equalsIgnoreCase("y")) {
			randomLayout = false;
		    }
		}
		if (nNameCol == -1) {
		    throw new RuntimeException("No name column, invalid GDF file");
		}
	    } else if (line.startsWith("edgedef>")) {
		inEdgeDef = true;
		inNodeDef = false;
		String def = line.substring(8);
		edgeCols = processEdgeDef(g,def);
		//System.out.println("edge cols length: " + nodeCols.length);
		for (int i = 0 ; i < edgeCols.length ; i++) {
		    if (edgeCols[i].getName().equalsIgnoreCase("node1")) {
			eNode1Col = i;
		    } else if (edgeCols[i].getName().equalsIgnoreCase("node2")) {
			eNode2Col = i;
		    } else if (edgeCols[i].getName().equalsIgnoreCase("directed")) {
		        eDirCol = i;
		    } else if (edgeCols[i].getName().equalsIgnoreCase("__edgeid")) {
			eIDCol = i;
		    }

		}
		if (eNode1Col == -1) {
		    throw new RuntimeException("No node1 column, invalid GDF file");
		}
		if (eNode2Col == -1) {
		    throw new RuntimeException("No node2 column, invalid GDF file");
		}
	    } else {
		String[] vals = stringSplit(line);
		if (inNodeDef) {
		    Node n = g.addNode(vals[nNameCol]);
		    n.__setattr__("label",vals[nNameCol]);
		    for (int i = 0 ; i < vals.length ; i++) {
			if (i == nNameCol)
			    continue;
			n.__setattr__(nodeCols[i].getName(),
				      convertToType(nodeCols[i].getSQLType(),
						    vals[i]));
		    }
		    if (randomLayout) {
			n.__setattr__("x",new Double((rand.nextDouble()*500)));
			n.__setattr__("y",new Double((rand.nextDouble()*500)));
		    }
		} else if (inEdgeDef) {
		    Node s = g.getNodeByName(vals[eNode1Col]);
		    Node t = g.getNodeByName(vals[eNode2Col]);
		    boolean directed = false;
		    if (eDirCol != -1) {
			directed = 
			    ((Boolean)convertToType(edgeCols[eDirCol].getSQLType(),
						    vals[eDirCol])).booleanValue();
		    }
		    Edge e = null;
		    if (eIDCol == -1) {
			if (directed) {
			    e = g.addDirectedEdge(s,t);
			} else {
			    e = g.addUndirectedEdge(s,t);
			}
		    } else {
			// we have an existing edgeid that 
			// overrides everything else
			int eid = Integer.parseInt(vals[eIDCol]);
			if (directed) {
			    e = g.addDirectedEdgeWID(s,t,eid);
			} else {
			    e = g.addUndirectedEdgeWID(s,t,eid);
			}
		    }
		    for (int i = 0 ; i < vals.length ; i++) {
			if ((i == eNode1Col) || 
			    (i == eNode2Col) || 
			    (i == eIDCol) ||
			    (i == eDirCol)) 
			    continue;
			e.__setattr__(edgeCols[i].getName(),
				      convertToType(edgeCols[i].getSQLType(),
						    vals[i]));
		    }
		} else {
		    throw new RuntimeException("Your database definition file may "+
					   "have a problem in it, not sure what "+
					   "to do with:\n"+line+ " (line: "+
					   lineNum+")");
		}
	    }
	}
	return;	
    }

    private int getSQLType(String s) {
	if (s.equals("array")) {
	    return(Types.ARRAY);
	} else if (s.equals("bigint")) {
	    return(Types.BIGINT);
	}  else if (s.equals("binary")) {
	    return(Types.BINARY);
	}  else if (s.equals("bit")) {
	    return(Types.BIT);
	}  else if (s.equals("boolean")) {
	    return(Types.BOOLEAN);
	}  else if (s.equals("blob")) {
	    return(Types.BLOB);
	}  else if (s.equals("decimal")) {
	    return(Types.DECIMAL);
	}  else if (s.equals("double")) {
	    return(Types.DOUBLE);
	}  else if (s.equals("float")) {
	    return(Types.FLOAT);
	}  else if ((s.equals("integer")) || (s.equals("int"))) {
	    return(Types.INTEGER);
	}  else if (s.equals("java_object")) {
	    return(Types.JAVA_OBJECT);
	}  else if (s.equals("longvarchar")) {
	    return(Types.LONGVARCHAR);
	}  else if (s.equals("null")) {
	    return(Types.NULL);
	}  else if (s.equals("numeric")) {
	    return(Types.NUMERIC);
	}  else if (s.equals("real")) {
	    return(Types.REAL);
	}  else if (s.equals("smallint")) {
	    return(Types.SMALLINT);
	}  else if (s.equals("struct")) {
	    return(Types.STRUCT);
	}  else if (s.equals("time")) {
	    return(Types.TIME);
	}  else if (s.equals("timestamp")) {
	    return(Types.TIMESTAMP);
	}  else if (s.equals("tinyint")) {
	    return(Types.TINYINT);
	}  else if (s.startsWith("varchar")) {
	    return(Types.VARCHAR);
	}  
	throw new RuntimeException("Unsuported Type");
    }
}
