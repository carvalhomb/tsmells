package com.hp.hpl.guess.util;

import java.util.*;
import com.hp.hpl.guess.Field;

public class SortableHashSet extends AnnoHashSet implements Comparable {
    
    public Comparable key = null;
    
    public SortableHashSet(Comparable key) {
	super(key.toString());
	this.key = key;
    }

    public SortableHashSet(String f, Comparable key) {
	super(f + " = " + key.toString());
	this.key = key;
    }

    public SortableHashSet(Field f, Comparable key) {
	this(f.getName(),key);
    }
    
    public int compareTo(Object o) {
	return(this.key.compareTo(((SortableHashSet)o).key));
    }
}
