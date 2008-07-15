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
import java.net.URL;

public class GuessSignedApplet extends Applet
{
    public void init() {
	try {
	    //PySystemState.initializeApplet();
	    Guess.setCacheDir();
	    Guess.setAppletMode(true,getAppletContext());
	    Guess.setSignedAppletMode(true);
	    Guess.configureUI();
	    //Guess.useFakeInterpreter(true);
	    System.out.println("init...");

	    //String applet_db = getParameter("DB");
	    //applet_db = applet_db.replace(';', '\n');
	    URL sample = 
		this.getClass().getResource("/sample.gdf");
	    String applet_db = null;
	    if (sample != null) {
		try {
		    StringBuffer todo = new StringBuffer();
		    BufferedReader br = new BufferedReader(new InputStreamReader(sample.openStream()));
		    String t = br.readLine();
		    while(t != null) {
			todo.append(t + "\n");
			t = br.readLine();
		    }
		    applet_db = todo.toString();
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }
	    System.out.println("applet_db");
	    StorageFactory.useDBServer();
	    StorageFactory.loadFromText(applet_db);
	    //System.out.println(System.getProperties());
	    try {
		Guess.getInterpreter();
		URL mbpy = 
		    this.getClass().getResource("/cachedir/guess.pkc");
		((CachedJarsPackageManager)PySystemState.packageManager).loadResource(mbpy,"guess.jar");
	    } catch (Throwable ig) {
	    }
	    Guess.init(VisFactory.PICCOLO,true,false);
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
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
