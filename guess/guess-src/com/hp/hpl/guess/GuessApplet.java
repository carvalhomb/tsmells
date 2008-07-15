package com.hp.hpl.guess;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import com.hp.hpl.guess.*;
import org.python.core.*;
import org.python.util.*;
import edu.uci.ics.jung.graph.*;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import com.hp.hpl.guess.db.*;
import com.hp.hpl.guess.storage.*;
import com.hp.hpl.guess.ui.*;
import java.applet.*;
import java.lang.reflect.Constructor;
import java.net.*;

public class GuessApplet extends Applet
{
    public void init() {
	try {
	    PySystemState.initializeApplet();
	    Guess.setAppletMode(true,getAppletContext());
	    Guess.configureUI();
	    Guess.useFakeInterpreter(true);
	    System.out.println("init...");
	    String db_url = getParameter("URL");
	    if ((db_url != null) && (!db_url.equals(""))) {			       
		//		System.out.println("found URL: " + db_url);
		try { 
		    URLConnection conn = null;
		    DataInputStream data = null;
		    String line;
		    StringBuffer buf = new StringBuffer();
		    URL theURL = new URL(db_url); 
		    //		    System.out.println("URL OK: " + theURL);
		    conn = theURL.openConnection();
		    conn.connect();
		    data = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
		    while ((line = data.readLine()) != null) {
			buf.append(line + "\n");
		    }
		    data.close();
		    //dbfcontent = dbfcontent.replace(';', '\n');
		    StorageFactory.useDBServer();
		    StorageFactory.loadFromText(buf.toString());
		} catch ( MalformedURLException e) {
		    System.out.println("Bad URL: " + db_url);
		}
		catch (IOException e) {
		    System.out.println("IO Error:" + e.getMessage());
		}		
	    } else {
		String applet_db = getParameter("DB");
		applet_db = applet_db.replace(';', '\n');
		StorageFactory.useDBServer();
		StorageFactory.loadFromText(applet_db);
	    }
	    String mode = getParameter("VISMODE");
	    if (mode == null) {
		mode = "PICCOLO";
	    }
	    if (mode.equalsIgnoreCase("PICCOLO")) {
		Guess.init(VisFactory.PICCOLO,false,false);
	    } else if (mode.equalsIgnoreCase("PREFUSE")) {
		Guess.init(VisFactory.PREFUSE,false,false);
	    } else if (mode.equalsIgnoreCase("TOUCHGRAPH")) {
		Guess.init(VisFactory.TOUCHGRAPH,false,false);
	    }

	    String toolbar = getParameter("TOOLBAR");
	    //System.out.println("tb: " + toolbar);
	    if ((toolbar != null) && (!toolbar.equals(""))) {
		Class tb = Class.forName(toolbar);
		Constructor tcon = tb.getConstructor((Class[])null);
		Object o = tcon.newInstance((Object[])null);
		//System.out.println(o.getClass());
		//Guess.getMainUIWindow().dock((DockableAdapter)o);
	    }
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	    e.printStackTrace();
	}
    }

    public void stop() {
	//System.out.println("stop...");
	destory();
	super.stop();
    }

    public void destory() {
	//System.out.println("destroy...");
	Guess.shutdown();
	super.destroy();
    }
}
