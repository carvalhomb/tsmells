package com.hp.hpl.guess.util.intervals;

import com.hp.hpl.guess.util.intervals.*;
import com.hp.hpl.guess.*;
import java.util.*;

/**
 * @pyobj DefaultRangeManager
 * @pyimport from com.hp.hpl.guess.util.intervals import DefaultRangeManager
 */
public abstract class DefaultRangeManager {

    private static RangeManager singleton = null;

    private static void checkSingleton() {
	if (singleton == null) {
	    singleton = new RangeManager();
	}
    }

    /**
     * @pyexport
     */
    public static Collection searchOverlap(String range) {
	checkSingleton();
	return(singleton.searchOverlap(range));
    }

    /**
     * @pyexport
     */
    public static Collection searchContains(String range) {
	checkSingleton();
	return(singleton.searchContains(range));
    }

    /**
     * @pyexport
     */
    public static Collection searchContained(String range) {
	checkSingleton();
	return(singleton.searchContained(range));
    }

    /**
     * @pyexport
     */
    public static Collection searchExact(String range) {
	checkSingleton();
	return(singleton.searchExact(range));
    }

    /**
     * @pyexport
     */
    public static void addRange(Object o, String range) {
	checkSingleton();
	singleton.addRange(o,range);
    }
}
