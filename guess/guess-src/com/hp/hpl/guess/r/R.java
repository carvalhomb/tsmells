package com.hp.hpl.guess.r;

import java.io.*;
import org.rosuda.JRclient.*;
import org.python.core.*;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.ExceptionWindow;
import com.hp.hpl.guess.ui.StatusBar;

/**
 * @pyobj r
 */
public class R {

    private static Rconnection c = null;

    private boolean trackImage = false;

    private String server = "127.0.0.1";

    private String imageMLoc = "/_output.jpg";

    private ImageMonitor myMon = null;

    private GraphMap gm = null;

    /**
     * @pyexport
     */
    public GraphMap getGraphMap() {
	return(gm);
    }

    /**
     * @pyexport
     */
    public void initConnection(String server) {
	if (Guess.getGPLFreeMode()) {
	    throw(new Error("Running in GPL Free Mode, you will not be able to use this class"));
	}
	this.server = server;
	initConnection();
    }

    /**
     * @pyexport
     */
    public void initConnection() {
	try {
	    myMon = new ImageMonitor(imageMLoc);
	    // make new connecton
	    c = new Rconnection(server);
	    StatusBar.setStatus("R: Server vesion: "+c.getServerVersion());
	    if (c.needLogin()) { // if server requires authentication, send one
		//System.out.println("authentication required.");
		c.login("guest","guest");
	    }
	} catch(RSrvException rse) {
	    c = null;
	    throw(new Error(rse.getMessage()));
	} catch(Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	    c = null;
	    throw(new Error(e.getMessage()));
	}
    }

    //double[] t = { 1.5, 3, 4.5, 2, 0, 0 };
    //c.assign("m", t);
    //

    /**
     * @pyexport isRConnected
     */
    public boolean isConnected() {
	if (c != null) {
	    return(true);
	} else {
	    return(false);
	}
    }


    public Object __call__(Object o) {
	//System.out.println(o.getClass());
	return(null);
    }

    private Object unwrap(REXP rx) {
	if (rx == null) 
	    return null;
	
	int Xt = rx.getType();
	if (Xt == REXP.XT_STR) {
	    return(rx.asString());
	} else if (Xt == REXP.XT_INT) {
	    return(new Integer(rx.asInt()));
	} else if (Xt == REXP.XT_DOUBLE) {
	    return(new Double(rx.asDouble()));
	} else if (Xt == REXP.XT_VECTOR) {
	    return(rx.asVector());
	} else if (Xt == REXP.XT_FACTOR) {
	    return(rx.asFactor());
	} else if (Xt == REXP.XT_LIST) {
	    return(rx.asList());
	} else if (Xt == REXP.XT_BOOL) {
	    return(rx.asBool());
	} else if (Xt == REXP.XT_ARRAY_DOUBLE) {
	    double[] t = rx.asDoubleArray();
	    if (t.length == 1) {
		return(new Double(t[0]));
	    }
	    return(t);
	} else if (Xt == REXP.XT_ARRAY_INT) {
	    int[] t = rx.asIntArray();
	    if (t.length == 1) {
		return(new Integer(t[0]));
	    }
	    return(t);
	} else {
	    Object matr = rx.asMatrix();
	    if (matr != null) {
		return(matr);
	    } else {
		return(rx);
	    }
	}
    }

    public Object __getattr__(String fieldName)
    {
	if (!isConnected()) {
	    initConnection();
	}
	if (!fieldName.startsWith("__")) {
	    try {
		Object rx = evalString(fieldName);
		if (rx == null) {
		    return(this);
		}
		return(rx);
	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	    return(this);
	}
	return(this);
    }

    public void __setattr__(String fieldName, Object value)
    {
	if (!isConnected()) {
	    initConnection();
	}

	if (!fieldName.startsWith("__")) {
	    try {
		if (value instanceof Double) {
		    evalString(fieldName + "<-" + value);
		} else if (value instanceof Integer) {
		    evalString(fieldName + "<-" + value);
		} else if (value instanceof double[][]) {
		    double[][] mdim = (double[][])value;
		    int m = mdim.length;
		    int n = mdim[0].length;
		    double[] newA = new double[m*n];
		    int loop = 0;
		    for (int i = 0 ; i < m ; i++) {
			for (int j = 0 ; j < n ; j++) {
			    newA[loop] = mdim[i][j];
			}
		    }
		    c.assign(fieldName,newA);
		    c.voidEval(fieldName+"<-matrix("+
			       fieldName+","+m+","+n+")");
		} else if (value instanceof double[]) {
		    c.assign(fieldName,(double[])value);
		} else if (value instanceof int[]) {
		    c.assign(fieldName,(int[])value);
		} else if (value instanceof int[][]) {
		    int[][] mdim = (int[][])value;
		    int m = mdim.length;
		    int n = mdim[0].length;
		    int[] newA = new int[m*n];
		    int loop = 0;
		    for (int i = 0 ; i < m ; i++) {
			for (int j = 0 ; j < n ; j++) {
			    newA[loop] = mdim[i][j];
			}
		    }
		    c.assign(fieldName,newA);
		    c.voidEval(fieldName+"<-matrix("+
			       fieldName+","+m+","+n+")");
		} else if (value instanceof Graph) {
		    // so graphs are going to be wierd
		    
		    // first thing, take the nodes, and put them
		    // into an array... we need to ensure that
		    // the indexing is consistent
		    
		    gm = new GraphMap((Graph)value);
		    double[] conn = gm.getConn();
		    c.assign(fieldName,conn);
		    c.voidEval(fieldName+"<-matrix("+fieldName+","+
			       gm.getNodeCount() + "," +
			       gm.getNodeCount() + ")");
		} else if (value instanceof String) {
		    evalString(fieldName + "<-\"" + value + "\"");
		} else if (value instanceof PySequence) {
		    // we don't support multi dim arrays right now
		    boolean str = false;
		    double[] vals = new double[((PySequence)value).__len__()];
		    for (int i = 0 ; i < ((PySequence)value).__len__() ; i++) {
			Object o = ((PySequence)value).__finditem__(i);
			if (!(o instanceof PyInteger) &&
			    !(o instanceof PyLong) &&
			    !(o instanceof PyFloat)) {
			    throw(new Error("Invalid type," + o.getClass() + 
					    " can't be sent to R as part of "+
					    "an array"));
			} else {
			    if (o instanceof PyLong) {
				Long d = 
				    (Long)((PyLong)o).__tojava__(Long.class);
				vals[i] = (double)(d.longValue());
			    } else if (o instanceof PyInteger) {
				Integer d = 
				    (Integer)((PyInteger)o).__tojava__(Integer.class);
				vals[i] = (double)(d.intValue());
			    } else if (o instanceof PyFloat) {
				Float d = 
				    (Float)((PyFloat)o).__tojava__(Float.class);
				vals[i] = (double)(d.floatValue());
			    }
			}
		    }
		    c.assign(fieldName,vals);
		} else {
		    throw(new Error("Invalid R type"));
		}
	    } catch (Exception e) {
		throw (new Error(e.toString()));
	    }
	}
    }

    public void rmode(BufferedReader reader) {
	if (!isConnected())
	    initConnection();

	String toEval = readLine(reader);
	while(!toEval.equals(".")) {
	    toEval = 
		"paste(capture.output(print("+toEval+")),collapse=\"\\n\")";
	    System.out.println(evalString(toEval));
	    toEval = readLine(reader);
	}
    }

    private static String readLine(BufferedReader reader) {
	System.out.print("R> ");
	
	try
	    {
		String s = reader.readLine();
		return(s);
	    }
	catch (IOException e)
	    {
		throw new Error(e);
	    }
    }
	
    /**
     * @pyexport reval
     */
    public Object evalString(String s) {
	s = "try("+s+")";
	REXP rx = null;
	try {
	    // this is just a quick hack to test assignments
	    // if the user types: #symbol value 
	    // then symbol<-value is performed
	    // if no value is specified then an array of integers is assigned
	    if (s.length()>1 && s.charAt(0)=='#') {
		s=s.substring(1);
		int i=s.indexOf(' ');
		if (i<1) {
		    int ti[]=new int[16];
		    int j=0; while (j<16) { ti[j]=(j==0)?1:ti[j-1]*j; j++; };
		    REXP r=new REXP(REXP.XT_ARRAY_INT,ti);
		    //double ti[]=new double[16];
		    //int j=0; while (j<16) { ti[j]=((double)j)/2; j++; };
		    //REXP r=new REXP(REXP.XT_ARRAY_DOUBLE,ti);
		    c.assign(s,r);
		    System.out.println("assign(\""+s+"\","+r+") OK");	
		} else {
		    c.assign(s.substring(0,i),s.substring(i+1));
		    System.out.println("assign(\""+s+"\") OK");		    
		}
	    } else {
		c.eval("try(jpeg(\""+imageMLoc+"\",quality=100))");
		rx = c.eval(s);
		c.eval("try(dev.off())");
		//System.out.println("exp: "+rx.toString());
		myMon.repaint();
		return(unwrap(rx));
	    }
	} catch(RSrvException rse) {
	    System.out.println("Rserve exception: "+rse.getMessage());
	} catch(Exception e) {
	    System.out.println("Something went wrong, but it's not the Rserve: "+e.getMessage());
	    ExceptionWindow.getExceptionWindow(e);
	}
	return(null);
    }

    /**
     * @pyexport rshutdown
     */
    public void shutdown() throws Exception {
	c.close();
    }

    public static void main(String[] arg) throws Exception {

	R myR = new R();
	myR.initConnection();

	BufferedReader ir = 
	    new BufferedReader(new InputStreamReader(System.in));
	
	System.out.print("> ");
	String s = null;
	while ((s=ir.readLine()).length()>0) {
	    myR.evalString(s);
	    System.out.print("> ");
	}
    }
}

