package org.python.core;

import java.util.Vector;
import java.util.Enumeration;

public class PyCompositeString extends PyString {

    protected Vector locations = new Vector();

    public Vector getLocations() {
	return(locations);
    }

    public PyCompositeString() {
	super();
    }

    public PyCompositeString(String string) {
	super(string);
    }

    public PyCompositeString(char c) {
	super(c);
    }

    public PyCompositeString(String string, PyCompositeString base) {
	this(string);
	this.locations = base.locations;
    }

    public PyCompositeString(String string, Location loc) {
	this(string);
	locations.addElement(loc);
    }

    public PyCompositeString(String string, PyCompositeString base, 
			     Location newloc) {
	this(string,base);
	locations.addElement(newloc);
    }

    public void shift(int amt) {
	Enumeration en = locations.elements();
	while(en.hasMoreElements()) {
	    Location loc = (Location)en.nextElement();
	    loc.start = loc.start + amt;
	}
    }

    public PyObject __add__(PyObject generic_other) {
        if (generic_other instanceof PyCompositeString) {
	    ((PyCompositeString)generic_other).shift(string.length());
            PyString other = (PyString)generic_other;
	    return(new PyCompositeString(string.concat(other.string),
					 (PyCompositeString)generic_other));
	} else if (generic_other instanceof PyString) {
            PyString other = (PyString)generic_other;
            return new PyCompositeString(string.concat(other.string),
					 (PyCompositeString)this);
        } else if (generic_other instanceof PyInstance) {
	    if (((PyInstance)generic_other).isTypeOfInterest()) {
		String othr = generic_other.__str2__().string;
		Location loc = new Location(string.length(),
					    othr.length(),
					    generic_other);
		return(new PyCompositeString(string.concat(othr),this,loc));
	    } else {
		return null;
	    }
	}
        else return null;
    }

    public static class Location implements Comparable {

	public int start = 0;
	public int length = 0;
	public PyObject po = null;
	
	public Location(int start, int length, PyObject po) {
	    this.start = start;
	    this.length = length;
	    this.po = po;
	    //System.out.println(start + " " + length);
	}
	
	public int compareTo(Object o) {
	    if (o instanceof Location) {
		int other = ((Location)o).start;
		if (start < other) {
		    return(-1);
		} else if (start > other) {
		    return(1);
		} 
	    }
	    return(0);
	}
    }	
}
