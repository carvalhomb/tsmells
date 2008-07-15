package com.hp.hpl.guess.util.intervals;

import com.hp.hpl.guess.*;
import java.util.*;

public class RangeManager {

    private IntervalTree itree = new IntervalTree();

    class HiLow {
	
	public int hi = 0;
	
	public int low = 0;
	
	public HiLow(int low, int hi) {
	    this.hi = hi;
	    this.low = low;
	}
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
		    toRet.add(new HiLow(lo,hi));
		} else if (hilo.length == 2) {
		    lo = Integer.parseInt(hilo[0]);
		    hi = Integer.parseInt(hilo[1]);
		    toRet.add(new HiLow(lo,hi));
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

    public Collection searchOverlap(String range) {
	List t = parseRange(range);
	Iterator it = t.iterator();
	HashSet toRet = new HashSet();
	while(it.hasNext()) {
	    HiLow chi = (HiLow)it.next();
	    IntervalNode[] res = itree.searchOverlap(chi.low,chi.hi);
	    for (int i = 0 ; i < res.length ; i++) {
		if (res[i].getProxy() != null) {
		    toRet.add(res[i].getProxy());
		}
	    }
	}
	return(toRet);
    }

    public Collection searchContains(String range) {
	List t = parseRange(range);
	Iterator it = t.iterator();
	HashSet toRet = new HashSet();
	while(it.hasNext()) {
	    HiLow chi = (HiLow)it.next();
	    IntervalNode[] res = itree.searchContains(chi.low,chi.hi);
	    for (int i = 0 ; i < res.length ; i++) {
		if (res[i].getProxy() != null) {
		    toRet.add(res[i].getProxy());
		}
	    }
	}
	return(toRet);
    }
    
    public Collection searchContained(String range) {
	List t = parseRange(range);
	Iterator it = t.iterator();
	HashSet toRet = new HashSet();
	while(it.hasNext()) {
	    HiLow chi = (HiLow)it.next();
	    IntervalNode[] res = itree.searchContained(chi.low,chi.hi);
	    for (int i = 0 ; i < res.length ; i++) {
		if (res[i].getProxy() != null) {
		    toRet.add(res[i].getProxy());
		}
	    }
	}
	return(toRet);
    }

    public Collection searchExact(String range) {
	List t = parseRange(range);
	Iterator it = t.iterator();
	HashSet toRet = new HashSet();
	while(it.hasNext()) {
	    HiLow chi = (HiLow)it.next();
	    IntervalNode[] res = itree.searchExact(chi.low,chi.hi);
	    for (int i = 0 ; i < res.length ; i++) {
		if (res[i].getProxy() != null) {
		    toRet.add(res[i].getProxy());
		}
	    }
	}
	return(toRet);
    }

    public void addRange(Object o, String range) {
	List t = parseRange(range);
	Iterator it = t.iterator();
	while(it.hasNext()) {
	    HiLow chi = (HiLow)it.next();
	    IntervalNode inode = 
		new IntervalNode(chi.low,chi.hi,o);
	    itree.insert(inode);
	}
    }
}
