package com.hp.hpl.guess.storage;

import com.hp.hpl.guess.*;
import com.hp.hpl.guess.db.*;
import java.io.StringReader;
import java.io.BufferedReader;
import com.hp.hpl.guess.ui.*;

/**
 * @pyobj db
 */
public class StorageFactory {

    public static StorageListener singleton = null;

    public static StorageListener getSL() {
	if (singleton == null) {
	    StatusBar.setStatus("Using basic storage engine (no DB)");
	    useDumbServer();
	}
	return(singleton);
    }

    public static StorageListener useDBServer(String dbName) {
	// assumed persistent
	if (singleton != null) {
	    return(singleton);
	}
	try {
	    singleton = (StorageListener)DBServer.init(dbName);
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
	return(singleton);
    }

    public static StorageListener useDBServer() {
	// assume in memory with some initial state
	if (singleton != null) {
	    return(singleton);
	}
	try {
	    singleton = (StorageListener)DBServer.initInMemory();
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
	return(singleton);
    }

    public static void createEmpty() {
	try {
	    if (singleton == null) {
		// default behavior is to create a singleton for
		// an in memory dbserver
		useDBServer();
	    }
	    singleton.createEmpty();
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public static void loadFromFile(String filename) {
	try {
	    if (singleton == null) {
		// default behavior is to create a singleton for
		// an in memory dbserver
		useDBServer();
	    }
	    singleton.loadFromFile(filename);
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public static void loadFromText(String text) {
	try {
	    if (singleton == null) {
		// default behavior is to create a singleton for
		// an in memory dbserver
		useDBServer();
	    }
	    singleton.loadFromText(text);
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }

    public static StorageListener useDumbServer() {
	if (singleton != null) {
	    return(singleton);
	}
	singleton = new DumbStorage();
	return(singleton);
    }

    public static void shutdown() {
	if (singleton != null) {
	    singleton.shutdown();
	}
	singleton = null;
    }
}
