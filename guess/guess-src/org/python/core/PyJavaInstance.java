// Copyright (c) Corporation for National Research Initiatives
package org.python.core;
import java.lang.reflect.Modifier;

import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.Edge;
import com.hp.hpl.guess.ui.DWButton;
import com.hp.hpl.guess.Field;
import com.hp.hpl.guess.util.intervals.Tracker;
import com.hp.hpl.guess.ui.Interesting;

/**
 * A wrapper around a java instance.
 */

public class PyJavaInstance
    extends PyInstance
    implements java.io.Externalizable
{

    public PyJavaInstance() {
        super();
        //javaProxies = new Object[1];
    }

    public PyJavaInstance(PyJavaClass iclass) {
        super(iclass, null);
	//	System.out.println(iclass);
    }

    public PyJavaInstance(Object proxy) {
        super(PyJavaClass.lookup(proxy.getClass()), null);
        javaProxy = proxy;
	//System.out.println(proxy);
	if (proxy instanceof Node) {
	    super.nodeType = true;
	} else if (proxy instanceof Edge) {
	    super.edgeType = true;
	} else if (proxy instanceof Interesting) {
	    super.typeOfInterest = true;
	}
		//Thread.dumpStack();
    }

    /**
     * Implementation of the Externalizable interface.
     * @param in the input stream.
     * @exception java.io.IOException
     * @exception ClassNotFoundException
     */
    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException
    {
        Object o = in.readObject();
        javaProxy = o;
        __class__ = PyJavaClass.lookup(o.getClass());
    }

    /**
     * Implementation of the Externalizable interface.
     * @param out the output stream.
     * @exception java.io.IOException
     */
    public void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException
    {
        //System.out.println("writing java instance");
        out.writeObject(javaProxy);
    }


    public void __init__(PyObject[] args, String[] keywords) {
        //javaProxies = new Object[1];

        Class pc = __class__.proxyClass;
        if (pc != null) {
            int mods = pc.getModifiers();
            if (Modifier.isInterface(mods)) {
                throw Py.TypeError("can't instantiate interface ("+
                                   __class__.__name__+")");
            }
            else if (Modifier.isAbstract(mods)) {
                throw Py.TypeError("can't instantiate abstract class ("+
                                   __class__.__name__+")");
            }
        }

        PyReflectedConstructor init = ((PyJavaClass)__class__).__init__;
        if (init == null) {
            throw Py.TypeError("no public constructors for "+
                               __class__.__name__);
        }
        init.__call__(this, args, keywords);
    }

	/* Original method
    protected void noField(String name, PyObject value) {
        throw Py.TypeError("can't set arbitrary attribute in java instance: "+
                           name);
    }*/

	//replaced method - davef/eytan
	protected void noField(String name, PyObject value) {
     PyObject method = __class__.lookup("__setattr__", false);
     if ( method == null ) {
       throw Py.TypeError("can't set arbitrary attribute in java instance: " + name);
     } else {
       method.__call__(this, new PyString(name), value);
     }
   }

    //added  davef/eytan
    protected PyObject ifindfunction(String name) {
	try {
	    //System.out.println(name);
	    PyObject getter = __class__.__getattr__;
	    if (getter == null) {
		getter = __class__.lookup("__getattr__", false);
	    }
	    if ( getter == null ) {
		return null;
	    }
	    
	    try {
		return getter.__call__(this, new PyString(name));
	    } catch (PyException exc) {
		if (Py.matchException(exc, Py.AttributeError)) return null;
		throw exc;
	    } 
	} catch (Throwable e) {
	    //System.out.println("caught");
	    return(null);
	}
    }

    protected void unassignableField(String name, PyObject value) {
        throw Py.TypeError("can't assign to this attribute in java " +
                           "instance: " + name);
    }

    public int hashCode() {
        if (javaProxy != null) {
            return javaProxy.hashCode();
        } else {
            return super.hashCode();
        }
    }

    public PyObject _is(PyObject o) {
        if (o instanceof PyJavaInstance) {
            return javaProxy == ((PyJavaInstance)o).javaProxy
                ? Py.One : Py.Zero;
        }
        return Py.Zero;
    }

    public PyObject _isnot(PyObject o) {
        return _is(o).__not__();
    }

    public int __cmp__(PyObject o) {
        if (!(o instanceof PyJavaInstance))
            return -2;
        PyJavaInstance i = (PyJavaInstance)o;
        if (javaProxy.equals(i.javaProxy))
            return 0;
        return -2;
    }

    public PyString __str__() {
	String toRet = javaProxy.toString();
	if ((javaProxy instanceof Node) || (javaProxy instanceof Edge) || 
	    (javaProxy instanceof Interesting)) { 
	    //if (nodeType || edgeType || typeOfInterest) {
	    //System.out.println("adding...");
	    Tracker.addNode(toRet.length()-1,this);
	}
	Tracker.incrementLocation(toRet.length());
	return new PyString(toRet);
    }

    public PyString __str2__() {
	return new PyString(javaProxy.toString());
    }


    public String toString() {
	return(javaProxy.toString());
    }

    public PyString __repr__() {
        return __str__();
    }

    public void __delattr__(String attr) {
        throw Py.TypeError("can't delete attr from java instance: "+attr);
    }
}
