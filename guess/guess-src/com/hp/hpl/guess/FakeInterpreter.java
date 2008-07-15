package com.hp.hpl.guess;

import org.python.core.PySystemState;
import org.python.core.PyObject;
import org.python.core.Py;
import java.io.*;
import java.util.Properties;
import com.hp.hpl.guess.util.GuessPyStringMap;
import com.hp.hpl.guess.ui.ExceptionWindow;

public class FakeInterpreter implements InterpreterAbstraction {

    private static GuessPyStringMap gpsm = 
	RealInterpreter.getPyStringMap();
    
    public FakeInterpreter() {
    }
    
    public FakeInterpreter(PyObject dict) {
    }

    public FakeInterpreter(PyObject dict, PySystemState systemState) {
    }

    public void cleanup() {
    } 
    
    public PyObject eval(String s) {
	return(null);
    }
    
    public void exec(PyObject code) {
    }
    
    public void exec(String s) {
    }
    
    public void execfile(InputStream s) {
    } 
    
    public void execfile(InputStream s, String name) {
    } 
    
    public void execfile(String s) {
    }
    
    public PyObject get(String name) {
	return(null);
    }
    
    public Object get(String name, Class javaclass) {
	return(null);
    }
    
    public PyObject getLocals() {
	return(null);
    } 
    
    public void set(String name, Object value) {
	gpsm.__setitem__(name.intern(), Py.java2py(value));
    } 
    
    public void set(String name, PyObject value) {
	gpsm.__setitem__(name.intern(), value);
    } 
    
    public void setImmutable(String name, Object value) {
	setImmutable(name,value,true);
    } 
    
    public void setImmutable(String name, PyObject value) {
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
	    gpsm.__setitem__(name.intern(), Py.java2py(value));
	    gpsm.setImmutable(name);
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
    }

    public void setImmutable(String name, PyObject value, boolean overwrite) {
	if (!overwrite) {
	    if (gpsm.contains(name)) {
		throw(Py.NameError("Immutable variable " + name + 
				   " already defined in namespace"));
	    }
	} 
	try {
	    gpsm.removeImmutable(name);
	    gpsm.__setitem__(name.intern(), Py.java2py(value));
	    gpsm.setImmutable(name);
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
    }

    public void setErr(OutputStream outStream) {
    } 
    
    public void setErr(PyObject outStream) {
    } 
    
    public void setErr(Writer outStream) {
    } 
    
    public void setLocals(PyObject d) {
    } 
    
    public void setOut(OutputStream outStream) {
    } 
    
    public void setOut(PyObject outStream) {
    } 
    
    public void setOut(Writer outStream) {
    } 

    public boolean state = false;

    public void freeze(boolean state) {
	this.state = state;
    }

    public boolean isFrozen() {
	return(state);
    }

    public void log(String filename) {
    }

    public void logCommand(String s) {
    }

    public void stoplog() {
    }

    public void remove(String name) {
	gpsm.__delitem__(name);
    }
}
