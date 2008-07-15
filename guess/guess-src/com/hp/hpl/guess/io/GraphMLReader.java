package com.hp.hpl.guess.io;

import java.io.*;
import java.util.*;

import com.hp.hpl.guess.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.sql.Types;
import com.hp.hpl.guess.ui.ExceptionWindow;

public class GraphMLReader extends DefaultHandler {

    private static final int GRAPH = 1;
    private static final int NODE = 2;
    private static final int EDGE = 3;
    private static final int DEFAULT = 4;
    private static final int DATA = 5;
    private static final int KEY = 6;

    private static final int OTHER = 10;

    Graph g = null;
    NodeSchema ns = null;
    EdgeSchema es = null;

    private boolean directed = false;

    public GraphMLReader(Graph g, String fileName) {
	this.g = g;
	try {
	    BufferedInputStream bis = 
		new BufferedInputStream(new FileInputStream(fileName));
	    load(g,bis);
	    bis.close();
	} catch (Exception e) {
	    throw(new Error(e.toString()));
	}
    }

    public GraphMLReader(Graph g, InputStream r) {
	this.g = g;
	load(g,r);
    }

    public void load(Graph g, InputStream r) {

	ns = g.getNodeSchema();
	es = g.getEdgeSchema();

	SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            // Parse the input
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(r,(DefaultHandler)this);

	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
           throw new Error("Error loading graphml file: " +  e);
        }
    }

    private Map curAttrs = null;
    private Stack seen = new Stack();
    private String curString = "";
    private String dataName = "";

    public void startElement(String namespaceURI,
			     String lName,
			     String qName,
			     Attributes attrs) throws SAXException {

	qName = qName.toLowerCase();

	//System.out.println("start element: " + qName);

	if (qName.equals("graph")) {
            createGraph(getAttributeMap(attrs));
        } else if (qName.equals("default")) {
	    //seen.push(new Integer(DEFAULT));
	} else if (qName.equals("data")) {
	    dataName = getKeyName(attrs);
	} else {
	    curAttrs = getAttributeMap(attrs);
	}
    }

    public void endElement(String namespaceURI,
			   String lName,
			   String qName) throws SAXException {

	qName = qName.toLowerCase();
	
	//System.out.println("end element: " + qName);

        if (qName.equals("node")) {
            createNode(curAttrs);
        } else if (qName.equals("edge")) {
            createEdge(curAttrs);
        } else if (qName.equals("key")) {
	    createKey(curAttrs);
	} else if (qName.equals("default")) {
	    curString = curString.trim();
	    curAttrs.put("default",curString);
	    //System.out.println("reset\n");
	    curString = "";
	} else if (qName.equals("data")) {
	    curString = curString.trim();
	    curAttrs.put(dataName,curString);
	    //System.out.println("reset\n");
	    curString = "";
	    dataName = "";
	}
    }

    private HashMap nameMap = new HashMap();

    public void createKey(Map attributeMap) {
	String attrName = (String)attributeMap.get("attr.name");
	String attrType = (String)attributeMap.get("attr.type");
	String id = (String)attributeMap.get("id");
	String forT = (String)attributeMap.get("for");
	String def = (String)attributeMap.get("default");
	if ((attrName == null) || (attrType == null) ||
	    (id == null) || (forT == null)) {
	    System.out.println("illegal key definition " + id);
	    return;
	}
	attrName = attrName.toLowerCase();
	attrType = attrType.toLowerCase();
	id = id.toLowerCase();
	forT = forT.toLowerCase();
	
	boolean addNF = false;
	boolean addEF = false;
	if (forT.equals("node")) {
	    addNF = true;
	} else if (forT.equals("edge")) {
	    addEF = true;
	} else if (forT.equals("all")) {
	    addNF = true;
	    addEF = true;
	} else if (forT.equals("graph")) {
	    System.out.println("graph keys not supported");
	} 
	if (addNF) {
	    // making a new node key
	    Field f = ns.getField(attrName);
	    if (f != null) {
		// field already defined
		System.out.println("node."+attrName + " already exists " + f.getSQLType() + " " + def);
		Object o = convertToType(f.getSQLType(),def);
		if (o != null)
		    f.setDefault(o);
	    } else {
		//System.out.println(attrType);
		int t = getSQLType(attrType);
		g.addNodeField(attrName,
			       getSQLType(attrType),
			       convertToType(t,def));
	    }
	    f = ns.getField(attrName);
	}
	if (addEF) {
	    // making a new node key
	    Field f = es.getField(attrName);
	    if (f != null) {
		// field already defined
		System.out.println("edge."+attrName + " already exists " + f.getSQLType() + " " + def);
		Object o = convertToType(f.getSQLType(),def);
		if (o != null)
		    f.setDefault(o);
	    } else {
		int t = getSQLType(attrType);
		g.addEdgeField(attrName,
			       getSQLType(attrType),
			       convertToType(t,def));
	    }
	    f = es.getField(attrName);
	}
	nameMap.put(id,attrName);
    }

    private int getSQLType(String attrType) {
	if (attrType.equals("boolean")) {
	    return(Types.BIT);
	} else if (attrType.equals("int")) {
	    return(Types.INTEGER);
	} else if (attrType.equals("long")) {
	    return(Types.DOUBLE);
	} else if (attrType.equals("float")) {
	    return(Types.FLOAT);
	} else if (attrType.equals("double")) {
	    return(Types.DOUBLE);
	} else if (attrType.equals("string")) {
	    return(Types.VARCHAR);
	} else {
	    System.out.println("Unsupported type: " + attrType + 
			       ", treating as String");
	    return(Types.VARCHAR);
	}
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

    public void characters(char[] ch, int start, int length) {
	curString = curString + new String(ch,start,length);
	//System.out.println("charactersa: " + curString);
    }

    public void createNode(Map attributeMap) {
	try {
	    String idString = (String)attributeMap.remove("id");
	    Node n = g.addNode(idString);
	    //System.out.println(Thread.currentThread().getName() + " setting label: " + idString);
	    n.__setattr__("label",idString); // give it a reasonable display
	    Iterator it = attributeMap.keySet().iterator();
	    while(it.hasNext()) {
		String key = (String)it.next();
		String val = (String)attributeMap.get(key);
		//System.out.println("extra
		Field f = ns.getField(key);
		if (f != null) {
		    //System.out.println("\t and " + key + " " + val);
		    n.__setattr__(f.getName(),convertToType(f.getSQLType(),val));
		}
	    }
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
    }

    protected void createGraph(Map attributeMap) {
	String edgeDefaultType =
	    (String) attributeMap.remove("edgedefault");
	if ((edgeDefaultType != null) && 
	    (edgeDefaultType.equalsIgnoreCase("directed"))) {
	    directed = true;
	} 
    }

    public void createEdge(Map attributeMap) {
	try {
	    String source = (String)attributeMap.remove("source");
	    String target = (String)attributeMap.remove("target");
	    Node s = g.getNodeByName(source);
	    if (s == null) {
		s = g.addNode(source);
	    }
	    Node t = g.getNodeByName(target);
	    if (t == null) {
		t = g.addNode(target);
	    }
	    
	    Edge e = null;

	    // override directionality?
	    String dirOver = (String)attributeMap.remove("directed");
	    boolean d = directed;
	    if ((dirOver != null)) {
		if (dirOver.equalsIgnoreCase("true")) {
		    d = true;
		} else {
		    d = false;
		}
	    }

	    //String test = s.toString()+"-"+t.toString();
	    //if (test.equals("n2-n4")) {
	    //	System.out.println("creating n2-n4");
	    //}

	    if (d) {
		//System.out.println("creating directed");
		e = g.addDirectedEdge(s,t);
	    } else {
		//System.out.println("creating undirected");
		e = g.addUndirectedEdge(s,t);
	    }
	    
	    //	    if (test.equals("n2-n4")) {
	    //System.out.println("\t"+e + " " + e.__getattr__("visible"));
	    //}

	    Iterator it = attributeMap.keySet().iterator();
	    while(it.hasNext()) {
		String key = (String)it.next();
		String val = (String)attributeMap.get(key);
		Field f = es.getField(key);
		if (f != null) {
		    //		    if (test.equals("n2-n4")) {
		    //	System.out.println("\t+" + e + " " + key + " " + val);
		    //}
		    e.__setattr__(f.getName(),convertToType(f.getSQLType(),val));
		}
	    }
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
    }

    private Map getAttributeMap(Attributes attrs) {
        Map map = new HashMap();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                map.put(attrs.getQName(i).toLowerCase(), 
			attrs.getValue(i));
            }
        }
        return map;
    }

    private String getKeyName(Attributes attrs) {
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
		if (attrs.getQName(i).equalsIgnoreCase("key")) {
		    return(remapKey((String)attrs.getValue(i)));
		}
            }
        }
	return(null);
    }

    private String remapKey(String keyName) {
	return((String)nameMap.get(keyName));
    }
}
