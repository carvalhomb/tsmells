package com.hp.hpl.guess;

import java.util.*;
import edu.uci.ics.jung.algorithms.shortestpath.*;
import edu.uci.ics.jung.graph.impl.*;
import com.hp.hpl.guess.ui.GraphElementListener;

public class PreviousNode extends SparseVertex 
    implements Comparable, GraphElement {
    
    private String name = "";

    private String state = "";

    public void __setattr__(String name, Object value) {
    }
    
    public Object __getattr__(String name) {
	return null;
    }
 
    public void show() {
    }
    
    public void hide() {
    }
    
    public Map values() {
	return null;
    }

    public int compareTo(Object o) {
	return(0);
    }

    public GraphElementListener getRep() {
	return(null);
    }
}
