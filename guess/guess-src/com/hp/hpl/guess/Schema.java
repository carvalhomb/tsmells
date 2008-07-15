package com.hp.hpl.guess;

import java.util.*;
import com.hp.hpl.guess.storage.StorageFactory;
import java.math.BigInteger;
import org.python.core.*;
import com.hp.hpl.guess.util.intervals.*;

public abstract class Schema {

    private static HashSet dynamicFields = new HashSet();
    private static String[] dynamicF = new String[]{"betweenness",
						    "pagerank",
						    "hits",
						    "degrank",
						    "rwbetweenness"};
    static {
	for (int i = 0 ; i < dynamicF.length ; i++) {
	    dynamicFields.add(dynamicF[i]);
	}
    }

    public Collection fieldNames() {
	return(fields.keySet());
    }

    public Collection allFields() {
	return(fields.values());
    }

    public Iterator fields() { 
	return fields.values().iterator();
    }

    private Hashtable fields = new Hashtable();

    public void addField(Field f) {
	fields.put(f.getName(),f);
	if (dynamicFields.contains(f.getName())) {
	    f.setDynamic(true);
	}
    }

    public Field getField(String fieldName) {
	return((Field)fields.get(fieldName));
    }

    public Object __getattr__(String fieldName)
    {
	return((Field)fields.get(fieldName));
    }

    public void addFieldToSL(Field f) {
	StorageFactory.getSL().addField(f);
	addField(f);
    }

    public Enumeration getFields() {
	return(fields.elements());
    }

    public int fieldCount() {
	return(fields.size());
    }

    public abstract void createIntegerField(String fieldName,int def);

    public abstract void createDoubleField(String fieldName,double def);

    public abstract void createBooleanField(String fieldName,boolean def);

    public abstract void createStringField(String fieldName,String def);

    public void createField(String fieldName, Object def) {
	if (def instanceof Double) 
	    createDoubleField(fieldName,((Double)def).doubleValue());
	else if (def instanceof Integer) 
	    createDoubleField(fieldName,((Integer)def).intValue());
	else if (def instanceof String) 
	    createStringField(fieldName,(String)def);
	else if (def instanceof BigInteger) 
	    createIntegerField(fieldName,((BigInteger)def).intValue());
	else
	    throw(new Error("Unknown default object type: " + def.getClass()));
    }

    private IntervalTree rangeIndex = null;

    private Field myRangeField = null;

    public void setRangeField(Field rf) {
	this.myRangeField = rf;
	rangeIndex = null;
    }

    public void rebuildRangeIndex() {
	Field rf = getRangeField();
	if (rf == null) {
	    throw(new Error("No field found for range values"));
	}
	rebuildRange(rf);
    }

    public List parseRange(String range) {
	ArrayList toRet = new ArrayList();
	String[] ranges = range.split(",");
	for (int i = 0 ; i < ranges.length ; i++ ) {
	    try {
		String temp = ranges[i].trim();
		String[] hilo = temp.split("\\s*-\\s*");
		int hi = 0;
		int lo = 0;
		if (hilo.length == 1) {
		    lo = Integer.parseInt(hilo[0]);
		    hi = Integer.parseInt(hilo[0]);
		    toRet.add(new SimplePair(lo,hi));
		} else if (hilo.length == 2) {
		    lo = Integer.parseInt(hilo[0]);
		    hi = Integer.parseInt(hilo[1]);
		    toRet.add(new SimplePair(lo,hi));
		} else {
		    throw(new Error("Invalid range: " + temp + "in '" + 
				    range + "'"));
		}
	    } catch (Exception ex) {
		throw(new Error("Invalid range '" + range + "' " + ex.toString()));
	    }
	}
	return(toRet);
    }

    protected void rebuildRange(Field rangeField) {
	if (rangeField != null) {
	    rangeIndex = new IntervalTree();
	    Iterator elements = null;
	    String fname = rangeField.getName();
	    if (rangeField.getType() == Field.NODE) {
		elements = Guess.getGraph().getNodes().iterator();
	    } else {
		elements = Guess.getGraph().getEdges().iterator();
	    }
	    while(elements.hasNext()) {
		GraphElement ge = (GraphElement)elements.next();
		PyJavaInstance pge = new PyJavaInstance(ge);
		String range = (String)ge.__getattr__(fname);
		if (range != null) {
		    Iterator ranges = parseRange(range).iterator();
		    while(ranges.hasNext()) {
			SimplePair t = (SimplePair)ranges.next();
			IntervalNode toAdd = 
			    new IntervalNode(t.lo,t.hi,pge);
			rangeIndex.insert(toAdd);
		    }
		}
	    }
	}
    }

    public Field getRangeField() {
	if (myRangeField == null) {
	    Field def = getField("range");
	    if (def != null) {
		myRangeField = def;
	    }
	}
	return(myRangeField);
    }

    public void rangeIndexCheck() {
	if (rangeIndex != null) {
	    return;
	} else {
	    Field rangeField = getRangeField();
	    if (rangeField == null) {
		throw(new Error("No field found for range values"));
	    } else {
		rebuildRange(rangeField);
	    }
	}
    }

    class SimplePair {

	SimplePair(int lo, int hi) {
	    this.lo = lo;
	    this.hi = hi;
	}

	public int lo = 0;
	public int hi = 0;
    }

    public SimplePair parseLH(PyObject other) {
	int lo = 0;
	int hi = 0;

	if (other instanceof PyString) {
	    StringTokenizer st = new StringTokenizer(other.toString(),"-");
	    lo = Integer.parseInt(st.nextToken());
	    hi = lo;
	    if (st.hasMoreTokens()) {
		hi = Integer.parseInt(st.nextToken());
	    }
	} else if (other instanceof PySequence) {
	    int length = ((PySequence)other).__len__();
	    if ((length < 1) || (length > 2)) {
		throw(new Error("Ranges must be 1 or 2 numbers in length"));
	    }
	    Object s = ((PySequence)other).__finditem__(0);
	    Object e = s;
	    if (length == 2) {
		e = ((PySequence)other).__finditem__(0);
	    }

	    if (s instanceof PyInteger) {
		lo = ((PyInteger)s).getValue();
	    } else if (s instanceof PyString) {
		lo = Integer.parseInt(s.toString());
	    }

	    if (e instanceof PyInteger) {
		hi = ((PyInteger)e).getValue();
	    } else if (s instanceof PyString) {
		hi = Integer.parseInt(e.toString());
	    }

	} else if (other instanceof PyInteger) {
	    lo = ((PyInteger)other).getValue();
	    hi = lo;
	} else {
	    throw(new Error("Invalid argument to range operation"));
	}
	return(new SimplePair(lo,hi));
    }

    private PyObject bundle(IntervalNode[] output) {
	PyList toRet = new PyList();
	for (int i = 0 ; i < output.length ; i++) {
	    Object proxy = output[i].getProxy();
	    if (proxy != null) {
		if (proxy instanceof PyObject) {
		    toRet.append((PyObject)proxy);
		} else {
		    toRet.append(new PyJavaInstance(proxy));
		}
	    }
	}
	return(toRet);
    }

    public PyObject __rcontains__(PyObject other) {
	rangeIndexCheck();

	SimplePair sp = parseLH(other);
	IntervalNode[] output = rangeIndex.searchContains(sp.lo,sp.hi);
	return(bundle(output));
    }

    public PyObject __rexact__(PyObject other) {
	rangeIndexCheck();

	SimplePair sp = parseLH(other);
	IntervalNode[] output = rangeIndex.searchExact(sp.lo,sp.hi);
	return(bundle(output));
    }

    public PyObject __rcontained__(PyObject other) {
	rangeIndexCheck();

	SimplePair sp = parseLH(other);
	IntervalNode[] output = rangeIndex.searchContained(sp.lo,sp.hi);
	return(bundle(output));

    }

    public PyObject __roverlaps__(PyObject other) {
	rangeIndexCheck();

	SimplePair sp = parseLH(other);
	IntervalNode[] output = rangeIndex.searchOverlap(sp.lo,sp.hi);
	return(bundle(output));
    }
}
