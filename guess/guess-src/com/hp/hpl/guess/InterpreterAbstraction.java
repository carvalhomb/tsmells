package com.hp.hpl.guess;

import org.python.core.PyObject;
import java.io.*;
import java.util.Properties;

/**
 * @pyobj interp
 */
public interface InterpreterAbstraction {
    
    public void cleanup(); 
            
    public PyObject eval(String s);
    
    public void exec(PyObject code);
    
    public void exec(String s);

    public void execfile(InputStream s); 
            
    public void execfile(InputStream s, String name); 
   
    /**
     * @pyexport
     */
    public void execfile(String s);

    public PyObject get(String name);
    
    public Object get(String name, Class javaclass);
    
    public PyObject getLocals(); 
            
    public void set(String name, Object value); 
          
    public void set(String name, PyObject value); 
          
    public void setErr(OutputStream outStream); 
    
    public void setErr(PyObject outStream); 
    
    public void setErr(Writer outStream); 
    
    public void setLocals(PyObject d); 
            
    public void setOut(OutputStream outStream); 
          
    public void setOut(PyObject outStream); 
    
    public void setOut(Writer outStream); 

    public void setImmutable(String name, Object value);

    public void setImmutable(String name, PyObject value);

    public void setImmutable(String name, Object value, boolean overwrite);

    public void setImmutable(String name, PyObject value, boolean overwrite);

    public void freeze(boolean state);

    public void logCommand(String s);

    public boolean isFrozen();

    /**
     * @pyexport
     */
    public void log(String filename);

    /**
     * @pyexport
     */
    public void stoplog();

    public void remove(String name);
}
