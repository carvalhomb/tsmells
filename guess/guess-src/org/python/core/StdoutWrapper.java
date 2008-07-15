// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import com.hp.hpl.guess.util.intervals.Tracker;

public class StdoutWrapper extends OutputStream
{
    protected String name;

    public StdoutWrapper() {
        name = "stdout";
    }

    protected PyObject getObject(PySystemState ss) {
        return ss.stdout;
    }
    protected void setObject(PySystemState ss, PyObject obj) {
        ss.stdout = obj;
    }

    protected PyObject myFile() {
        PySystemState ss = Py.getSystemState();
        PyObject obj = getObject(ss);
        if (obj == null) {
            throw Py.AttributeError("missing sys."+name);
        }
        if (obj instanceof PyJavaInstance) {
            PyFile f = null;

            Object tmp = obj.__tojava__(OutputStream.class);
            if ((tmp != Py.NoConversion) && (tmp != null)) {
                OutputStream os = (OutputStream)tmp;
                f = new PyFile(os, "<java OutputStream>");
            } else {
                tmp = obj.__tojava__(Writer.class);
                if ((tmp != Py.NoConversion) && (tmp != null)) {
                    Writer w = (Writer)tmp;
                    f = new PyFile(w, "<java Writer>");
                }
            }
            if (f != null) {
                setObject(ss, f);
                return f;
            }
        }
        return obj;
    }

    public void flush() {
        PyObject obj = myFile();
        if (obj instanceof PyFile) {
            ((PyFile)obj).flush();
        } else {
            obj.invoke("flush");
        }
    }

    public void write(String s) {
        PyObject obj = myFile();

        if (obj instanceof PyFile) {
            ((PyFile)obj).write(s);
        } else {
            obj.invoke("write", new PyString(s));
        }
    }


    public void write(int i) {
        write(new String(new char[] {(char)i}));
    }

    public void write(byte[] data, int off, int len) {
        write(new String(data, off, len));
    }


    public void clearSoftspace() {
        PyObject obj = myFile();

        if (obj instanceof PyFile) {
            PyFile file = (PyFile)obj;
            if (file.softspace) {
                file.write("\n");
                file.flush();
            }
            file.softspace = false;
        } else {
            PyObject ss = obj.__findattr__("softspace");
            if (ss != null && ss.__nonzero__()) {
                obj.invoke("write", Py.Newline);
            }
            obj.invoke("flush");
            obj.__setattr__("softspace", Py.Zero);
        }
    }

    public void print(PyObject o, boolean space, boolean newline) {
	//System.out.println(o.getClass());
	//System.out.println("foo");
        PyString string = o.__str__();
        PyObject obj = myFile();

        if (obj instanceof PyFile) {
	    PyFile file = (PyFile)obj;
            String s = string.toString();
            if (newline)
                s = s+"\n";
            if (file.softspace)
                s = " "+s;
            file.write(s);
            file.flush();
            if (space && s.endsWith("\n"))
                space = false;
            file.softspace = space;
        } else {
            PyObject ss = obj.__findattr__("softspace");
            if (ss != null && ss.__nonzero__()) {
                obj.invoke("write", Py.Space);
            }
            obj.invoke("write", string);
            if (newline)
                obj.invoke("write", Py.Newline);
//          obj.invoke("flush");

            if (space && string.toString().endsWith("\n"))
                space = false;
            obj.__setattr__("softspace", space ? Py.One : Py.Zero);
        }

	//System.out.println(Tracker.getLocation());
	if (o instanceof PyCompositeString) {
	    Vector v = ((PyCompositeString)o).getLocations();
	    Collections.sort(v);
	    int whereTo = Tracker.getLocation();
	    int lineloc = Tracker.getLocation() - string.__len__();
	    //System.out.println(lineloc);
	    for (int i = 0 ; i < v.size() ; i++) {
		PyCompositeString.Location loci = 
		    (PyCompositeString.Location)v.elementAt(i);
		Tracker.setLocation(lineloc+loci.start);
		Tracker.addNode(loci.length-1,loci.po);
	    }
	    Tracker.setLocation(whereTo);
	}
	//Tracker.moveToDocEnd();
    }


    public void print(String s) {
        print(new PyString(s), false, false);
    }

    public void println(String s) {
        print(new PyString(s), false, true);
	//Tracker.incrementLocation(4);
    }

    public void print(PyObject o) {
        print(o, false, false);
    }

    public void printComma(PyObject o) {
        print(o, true, false);
    }

    public void println(PyObject o) {
        print(o, false, true);
	//Tracker.incrementLocation(4);
    }

    public void println() {
        PyObject obj = myFile();

        if (obj instanceof PyFile) {
            PyFile file = (PyFile)obj;
            file.write("\n");
            file.flush();
            file.softspace = false;
        }
        else {
            obj.invoke("write", Py.Newline);
            obj.__setattr__("softspace", Py.Zero);
        }
	//Tracker.incrementLocation(4);
    }
}
