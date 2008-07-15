package com.hp.hpl.guess;

import org.python.core.*;
import org.python.util.PythonInterpreter;
import com.hp.hpl.guess.util.GuessPyStringMap;
import java.io.*;
import com.hp.hpl.guess.ui.ExceptionWindow;
import java.util.Date;

public class RealInterpreter extends PythonInterpreter 
    implements InterpreterAbstraction {

    PrintStream log = null;

    private static GuessPyStringMap gpsm = new GuessPyStringMap(); 

    public static GuessPyStringMap getPyStringMap() {
	return(gpsm);
    }

    public RealInterpreter() {
	super(gpsm);
    }
    
    public RealInterpreter(PyObject dict) {
	super(dict);
    }

    public RealInterpreter(PyObject dict, PySystemState systemState) {
	super(dict,systemState);
    }

    public void setImmutable(String name, Object value) {
	setImmutable(name,value,true);
    }

    public void setImmutable(String name, Object value, boolean overwrite) {
	if (!overwrite) {
	    if (gpsm.contains(name)) {
		throw(Py.NameError("Immutable variable " + name + 
				   " already defined in namespace"));
	    }
	} 
	try {
	    gpsm.removeImmutable(name);
	    super.set(name,value);
	    gpsm.setImmutable(name);
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	} catch (NoClassDefFoundError ncdfe) {
	    // this probably means we didn't have javax.media installed
	    // just ignore it
	}
    } 
    
    public void setImmutable(String name, PyObject value) {
	setImmutable(name,value,true);
    }

    public void setImmutable(String name, PyObject value, boolean overwrite) {
	if (!overwrite) {
	    if (gpsm.contains(name)) {
		throw(Py.NameError("Immutable variable " + name + 
				   " already defined in namespace"));
	    }
	}
	gpsm.removeImmutable(name);
	super.set(name,value);
	gpsm.setImmutable(name);
    } 

    public boolean state = false;

    public void freeze(boolean state) {
	this.state = state;
    }

    public void exec(String s) {
	try {
	    super.exec(s);
	} catch (Error e) {
	    throw(e);
	}
	logCommand(s);
    }

    public PyObject eval(String s) {
	PyObject toRet = null;
	try {
	    toRet = super.eval(s);
	} catch (Error e) {
	    throw(e);
	}
	logCommand(s);
	return(toRet);
    }

    public void logCommand(String s) {
	if ((log != null) && 
	    (!s.startsWith("ENV[")) &&
	    (!s.startsWith("if _ != None:")) &&
	    (!s.equals("print _")) &&
	    (s.indexOf("interp.stoplog") < 0) &&
	    (!s.startsWith("interp.log"))) {
	    try {
		if (s.startsWith("apply(_, ())")) {
		    log.println("()");
		    return;
		} else {
		    log.println("");
		}
		log.print(s);
	    } catch(Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}
    }

    public boolean isFrozen() {
	return(state);
    }

    public void log(String filename) {
	try {
	    if (log != null) {
		stoplog();
	    }
	    log = new PrintStream(new FileOutputStream(filename));
	    Date gc = new Date(System.currentTimeMillis());
	    log.println("# Logged on " + gc.toString());
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	    log = null;
	}
    }

    public void stoplog() {
	if (log != null) {
	    try {
		log.println("");
		log.close();
	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}
	log = null;
    }

    public void remove(String name) {
	gpsm.removeImmutable(name);
	gpsm.__delitem__(name);
    }
}
