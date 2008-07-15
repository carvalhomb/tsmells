package com.hp.hpl.guess.util;

import org.python.core.*;
import java.util.*;

public class GuessPyStringMap extends PyStringMap {

    HashSet dontChange = new HashSet();

    public GuessPyStringMap() {
	super();
    }

    public synchronized void __setitem__(String key, PyObject value) {
	if (dontChange.contains(key)) {
	    throw Py.TypeError("can't set the value of immutable variable " + 
			       key);
	} else {
	    super.__setitem__(key,value);
	}
    }

    public void setImmutable(String key) {
	dontChange.add(key);
    }

    public void removeImmutable(String key) {
	dontChange.remove(key);
    }

    public boolean contains(String key) {
	return(dontChange.contains(key));
    }
}
