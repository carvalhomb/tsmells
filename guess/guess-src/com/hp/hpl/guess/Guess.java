
package com.hp.hpl.guess;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import com.hp.hpl.guess.*;
import org.python.core.*;
import org.python.util.*;
import edu.uci.ics.jung.graph.*;
import com.hp.hpl.guess.storage.*;
import com.hp.hpl.guess.ui.*;
import gnu.getopt.*;
import com.hp.hpl.guess.r.R;
import com.hp.hpl.guess.util.GuessPyStringMap;
import java.applet.AppletContext;
import com.hp.hpl.guess.util.intervals.Tracker;

import com.jgoodies.looks.FontSizeHints;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jidesoft.utils.Lm;


/**
 * the main guess system, it contains a Main that does most of what you
 * want, but you can basically build your own application using the stuff
 * in this class.  Generally, you will want to do:
 * <PRE>
 * StorageFactory.useDBServer(...) // to set up the database you want
 * Guess.configureUI()             // unless you want your own L&F
 * Guess.init(...)
 * </PRE>
 * @pyobj Guess
 * @pyimport from com.hp.hpl.guess import Guess
 */
public class Guess
{
    /**
     * the visualization frame
     */
    private static FrameListener myF = null;

    /**
     * the current working graph, there is only 1
     */
    private static Graph g = null;

    /**
     * a wrapper around the jython interp so that 
     * we can get rid of it when people don't need/want it
     */
    private static InterpreterAbstraction interpSingleton = null;

    /**
     * use the fake interpreter?
     */
    private static boolean fakeInterp = false;

    /**
     * shortcut
     */
    private static final char sep = File.separatorChar;

    /**
     * some times we need to execute a command for loading
     * the database at a later time, so we use this
     */
    private static String doLater = null;

    /**
     * we also can take a list of files to execute
     */
    private static Vector pythonToExec = null;

    /**
     * are we running inside an applet
     */
    private static boolean appletMode = false;

    /**
     * are we running inside a signed applet
     */
    private static boolean signedAppletMode = false;

    /**
     * the applet context
     */
    private static AppletContext myAC = null;

    /**
     * are we running in GPL free mode
     */
    private static boolean gplFree = false;

    /**
     * enable the main ui window or not
     */
    private static boolean enableMainUI = true;

    /**
     * allow multiple edges between nodes?
     */
    private static boolean multiEdge = false;

    /**
     * allow multiple edges
     */
    public static boolean allowMultiEdge() {
	return(multiEdge);
    }

    /**
     * enable the main UI window?
     */
    public static void enableMainUI(boolean state) {
	enableMainUI = state;
    }

    /**
     * set the gplfree mode
     */
    public static void setGPLFreeMode(boolean state) {
	gplFree = state;
    }

    /**
     * get the gplfree mode
     */
    public static boolean getGPLFreeMode() {
	return(gplFree);
    }

    /**
     * running as a signed applet?
     */
    public static void setSignedAppletMode(boolean state) {
	signedAppletMode = state;
    }

    /**
     * running as a signed applet?
     */
    public static boolean getSignedAppletMode() {
	return(signedAppletMode);
    }
    
    /**
     * running inside an applet? true for yes, default is false
     */
    public static void setAppletMode(boolean state, 
				     AppletContext ac) {
	appletMode = state;
	myAC = ac;
    }

    /**
     * are we running inside an applet?
     */
    public static boolean getAppletMode() {
	return(appletMode);
    }

    /**
     * get the applet context
     * @pyexport
     */
    public static AppletContext getAppletContext() {
	return(myAC);
    }

    /**
     * call this first if you want to use the "fake" interpreter.
     * The fake one won't let you execute any commands through
     * jython but may be ok if you're just building a simple
     * applet
     * @param true to enable fake one, false (default) otherwise
     */
    public static void useFakeInterpreter(boolean state) {
	fakeInterp = state;
    }

    /**
     * get the interpreter (fake or real depending on
     * useFakeInterpreter setting)
     * @return a jython interpreter
     */
    public static InterpreterAbstraction getInterpreter() {
	if (interpSingleton == null) {
	    if (fakeInterp) {
		interpSingleton = new FakeInterpreter();
	    } else {
		PySystemState.initialize();
		interpSingleton = new RealInterpreter();
	    }
	}
	return(interpSingleton);
    }

    /**
     * set the frame used for synchro issues
     * @param gf the frame 
     */
    private static void setFrame(FrameListener gf) {
	myF = gf;
    }

    public static FrameListener getFrame() {
	return(myF);
    }

    private static TextPaneJythonConsole tpjc = null;

    /**
     * gets a reference to the existing GUI console object, or
     * null if it doesn't exist
     * @pyexport
     */
    public static TextPaneJythonConsole getJythonConsole() {
	return(tpjc);
    }

    /**
     * gets the current working graph, if the sytem
     * hasn't been inited you get back null
     * @return the working graph or null
     */
    public static Graph getGraph() {
	return(g);
    }

    private static MainUIWindow myWin = null;

    public static boolean nowarn = false;

    public static MainUIWindow getMainUIWindow() {
	return(myWin);
    }

    public static void setCacheDir() {
	Properties prop = System.getProperties();
	String tempdir = System.getProperty("java.io.tmpdir");
	if (tempdir == null) {
	    tempdir = "";
	}
	tempdir = tempdir + File.separatorChar + "cachedir";
	prop.setProperty("python.cachedir",tempdir);
    }

    /**
     * the main loop, this gets used when you're not using 
     * the applet version
     * @param argv arguments
     */
    public static void main(String[] argv)
	throws Exception {

	Lm.verifyLicense("GUESS", "GUESS",
			 "kaiS04IaJ.QjUq.ZLB0OWobuNMddGb41");

	try {
	    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    //UIManager.getSystemLookAndFeelClassName());
	    configureUI();
	} catch (Exception lnfe) { 
	}
	
	LongOpt[] longopts = new LongOpt[11];
	longopts[0] = new LongOpt("prefuse", LongOpt.NO_ARGUMENT, null, 'p');
	longopts[1] = new LongOpt("touchgraph", 
				  LongOpt.NO_ARGUMENT, null, 't'); 
	longopts[2] = new LongOpt("console", LongOpt.NO_ARGUMENT, null, 'c'); 
	longopts[3] = new LongOpt("persistent", 
				  LongOpt.REQUIRED_ARGUMENT, null, 'o'); 
	longopts[4] = new LongOpt("gplfree", LongOpt.NO_ARGUMENT, null, 'f'); 
	longopts[5] = new LongOpt("nowarn", LongOpt.NO_ARGUMENT, null, 'n'); 
	longopts[6] = new LongOpt("novis", LongOpt.NO_ARGUMENT, null, 'v'); 
	longopts[7] = new LongOpt("multiedge", 
				  LongOpt.NO_ARGUMENT, null, 'm'); 
	longopts[8] = new LongOpt("fontsize", 
				  LongOpt.REQUIRED_ARGUMENT, null, 's'); 
	longopts[9] = new LongOpt("jung", LongOpt.NO_ARGUMENT, null, 'j');
	longopts[10] = new LongOpt("consolelog", 
				  LongOpt.NO_ARGUMENT, null, 'l');

	Getopt go = new Getopt("Guess", argv, ":ptcvmofnmsl", longopts);
	go.setOpterr(false);
	int c;

	boolean guiMode = true;

	int uiMode = VisFactory.PICCOLO;

	String persistent = null;

	while ((c = go.getopt()) != -1)
	    {
		switch(c)
		    {
		    case 'p':
			uiMode = VisFactory.PREFUSE;
			break;
		    case 'j':
			uiMode = VisFactory.JUNG;
			break;
		    case 't':
			uiMode = VisFactory.TOUCHGRAPH;
			break;
		    case 'v':
			uiMode = VisFactory.NOVIS;
			break;
		    case 'c':
			guiMode = false;
			break;
		    case 'n':
			nowarn = true;
			break;
		    case 'f':
			System.out.println("****Running in GPL Free Mode****");
			gplFree = true;
			break;
		    case 'o':
			if (go.getOptarg() == null) {
			    System.out.println("Please enter a database directory to use -o/--persistent");
			    System.exit(0);
			} else {
			    persistent = go.getOptarg();
			}
			break;
		    case 's':
			if (go.getOptarg() == null) {
			    System.out.println("Please specify a numerical font size with the -s/--fontsize option");
			    System.exit(0);
			} else {
			    int size = 10;
			    try {
				size = Integer.parseInt(go.getOptarg());
				TextPaneJythonConsole.setFontSize(size);
			    } catch (Exception ne) {
				System.out.println("Please specify a numerical font size with the -s/--fontsize option");
				System.exit(0);
			    }
			}
			break;
		    case 'm':
			System.out.println("allowing multiple edges");
			multiEdge = true;
			break;
		    case 'l':
			System.out.println("STDOUT/STDERR logged to console");
			handleOver = false;
			break;
		    case ':':
			System.out.print("unknown option: " + (char)c + "\n");
			break;
		    case '?':
			System.out.print("unknown option: " + (char)c + "\n");
			break;
		    default:
			System.out.print("unknown option: " + (char)c + "\n");
			System.exit(0);
		    }
	    }
	
	
	System.out.println("GUESS Version: " + Version.MAJOR_VERSION + " (" + Version.MINOR_VERSION + ")");

	setCacheDir();

	//System.out.println(uiMode);

	String database = null;
	for (int i = go.getOptind(); i < argv.length ; i++) {
	    if ((argv[i].endsWith(".py")) ||
		(argv[i].endsWith(".Py")) ||
		(argv[i].endsWith(".PY"))) {
		if (pythonToExec == null) {
		    pythonToExec = new Vector();
		}
		pythonToExec.addElement(argv[i]);
	    } else {
		database = argv[i];
	    }
	}

	if (database == null) {
	    getDataBase();
	} else {

	    File f = new File(database);

	    String fileExtension = "";

	    if (f.exists()) {
		SunFileFilter filter = 
		    new SunFileFilter();
		fileExtension = filter.getExtension(f);
	    }

	    if (database.equals("null")) {
		// make "null" a special kind of database
		// for people who want to work with a dummy database
		StorageFactory.useDBServer();
		StorageFactory.createEmpty();
	    } else if (fileExtension.equalsIgnoreCase("gdf")) {
		if (persistent != null) {
		    StorageFactory.useDBServer(persistent);
		} else {
		    StorageFactory.useDBServer();
		}
		StorageFactory.loadFromFile(database);
	    } else if ((fileExtension.equalsIgnoreCase("xml")) ||
		       (fileExtension.equalsIgnoreCase("graphml"))) {
		if (persistent != null) {
		    StorageFactory.useDBServer(persistent);
		} else {
		    StorageFactory.useDBServer();
		}
		StorageFactory.createEmpty();
		
		doLater = "g.makeFromGML(\""+
		    database.replace('\\','/')+
		    "\")";
	    } else if ((fileExtension.equalsIgnoreCase("net")) ||
		       (fileExtension.equalsIgnoreCase("paj")) ||
		       (fileExtension.equalsIgnoreCase("pajek"))) {
		if (persistent != null) {
		    StorageFactory.useDBServer(persistent);
		} else {
		    StorageFactory.useDBServer();
		}
		StorageFactory.createEmpty();
		
		doLater = "g.makeFromPajek(\""+
		    database.replace('\\','/')+
		    "\")";
	    } else if (fileExtension.equalsIgnoreCase("dl")) {
		// added for Patrick
		if (persistent != null) {
		    StorageFactory.useDBServer(persistent);
		} else {
		    StorageFactory.useDBServer();
		}
		StorageFactory.createEmpty();
		
		doLater = "g.makeFromDL(\""+
		    database.replace('\\','/')+
		    "\")";
	    } else {
		System.out.println(database + " not found as file, trying to load database");
		//System.out.println("using database: " + database);
		StorageFactory.useDBServer(database);
	    }
	}

	// some extra little set up things to the interp
	InterpreterAbstraction interp = getInterpreter();
	interp.exec("from java.sql import Types");
	interp.exec("from com.hp.hpl.guess.ui import VisFactory");
	interp.exec("from com.hp.hpl.guess.ui import Colors");
	interp.exec("from com.hp.hpl.guess import Subgraph");
	interp.exec("from com.hp.hpl.guess.piccolo import Legend");
	interp.exec("from com.hp.hpl.guess.piccolo import GradientLegend");
	interp.exec("from com.hp.hpl.guess.ui import InfoWindow");
	interp.exec("from com.hp.hpl.guess.ui import DragWindow");
	interp.exec("from com.hp.hpl.guess.ui import DWButton");
	interp.exec("from com.hp.hpl.guess.ui import ExceptionWindow");
	interp.exec("from com.hp.hpl.guess.ui import DrawWindow");
	interp.exec("from com.hp.hpl.guess.util.intervals import Tracker");
	interp.exec("from com.hp.hpl.guess.ui import GraphElementEditorPopup");
	interp.exec("from com.hp.hpl.guess.ui import NodeEditorPopup");
	interp.exec("from com.hp.hpl.guess.ui import EdgeEditorPopup");

	//	((CachedJarsPackageManager)PySystemState.packageManager).addJarToPackages(new java.net.URL("http://www.hpl.hp.com/research/idl/projects/graphs/guess/lib/guess.jar"));

	// final step
	init(uiMode,guiMode,(!guiMode));


    }

    /**
     * when a database is unspecified you get walked through 
     * a few steps, this should only be run once before the init
     * process (see the main loop for an example)
     */
    public static void getDataBase() {
	Object[] options = {"Existing Database",
			    "Load GDF/GraphML",
			    "Empty"};
	int n = 
	    JOptionPane.showOptionDialog(null,
					 (Object)"Would you like to open an existing database, load a graph definition file, or start with a blank space?",
					 "Welcome to GUESS",
					 JOptionPane.YES_NO_OPTION,
					 JOptionPane.QUESTION_MESSAGE,
					 null,
					 options,
					 options[0]);
	if (n == 0) {
	    // user wants an existing database, let them
	    // pick one and then return
	    if (existingChooser()) {
		return;
	    }
	} else if (n == 1) {
	    // user wants to pick to load
	    if (newChooser()) {
		return;
	    }
	} else {
	    if (emptyChooser()) {
		return;
	    }
	}
	getDataBase();
    }


    /**
     * user seems to want to select from an existing database 
     * @return true if succeeded, false otherwise
     */
    private static boolean existingChooser() {
	try {
	    String toLoad = ".";
	    try {
		toLoad = System.getProperty("gHome");
		if (toLoad == null) { 
		    toLoad = ".";
		} else {
		    File testF = new File(toLoad);
		    if (!(testF.exists()) || !(testF.isDirectory())) {
			toLoad = ".";
		    }
		}
	    } catch (Exception hde) {
		toLoad = ".";
	    }
	    JFileChooser chooser = 
		new JFileChooser(new File(toLoad).getCanonicalPath());
	    SunFileFilter filter = new SunFileFilter();
	    filter.addExtension("properties");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		String fileName = 
		    chooser.getSelectedFile().getAbsolutePath();
		fileName = fileName.substring(0,fileName.length()-11);
		//System.out.println("using database: " + fileName);
		StorageFactory.useDBServer(fileName);
		return(true);
	    }
	} catch (Exception e) {
	    exceptionHandle(e);
	}
	return(false);
    }

    /**
     * user seems to want to select from new file
     * @return true if succeeded, false otherwise
     */
    private static boolean newChooser() {
	try {

	    // PICK THE FILE TO LOAD
	    
	    PickFile chooser = new PickFile();

	    File returnVal = chooser.showDialog();
	    
	    if(returnVal != null) {
		
		String fileName = 
		    returnVal.getAbsolutePath();
		SunFileFilter filter = 
		    new SunFileFilter();

		String fileExtension = filter.getExtension(returnVal);
		
		if (chooser.isPersistent()) {
		    String directory = 
			chooser.getDirectory().getCanonicalPath();
		    String dbName = chooser.getName();
		    if (fileExtension.equalsIgnoreCase("dl")) {
			// added for Patrick
			StorageFactory.useDBServer(directory + 
						   sep + 
						   dbName);
			StorageFactory.createEmpty();
			
			doLater = "g.makeFromDL(\""+
			    fileName.replace('\\','/')+
			    "\")";
		    }  else if ((fileExtension.equalsIgnoreCase("xml")) ||
				(fileExtension.equalsIgnoreCase("graphml"))) {
			StorageFactory.useDBServer(directory + 
						   sep + 
						   dbName);
			StorageFactory.createEmpty();
			
			doLater = "g.makeFromGML(\""+
			    fileName.replace('\\','/')+
			    "\")";
		    } else if ((fileExtension.equalsIgnoreCase("net")) ||
			       (fileExtension.equalsIgnoreCase("paj")) ||
			       (fileExtension.equalsIgnoreCase("pajek"))) {
			StorageFactory.useDBServer(directory + 
						   sep + 
						   dbName);
			StorageFactory.createEmpty();
			
			doLater = "g.makeFromPajek(\""+
			    fileName.replace('\\','/')+
			    "\")";
		    } else {
			StorageFactory.useDBServer(directory + 
						   sep + 
						   dbName);
			StorageFactory.loadFromFile(fileName);
		    }
		    return(true);
		} else {
 		    //System.out.println("using in memory database");
		    if ((fileExtension.equalsIgnoreCase("dl"))) {
			// added for Patrick
 			StorageFactory.useDBServer();
 			StorageFactory.createEmpty();

 			doLater = "g.makeFromDL(\""+
 			    fileName.replace('\\','/')+
 			    "\")";
 		    } else if ((fileExtension.equalsIgnoreCase("xml")) ||
 			(fileExtension.equalsIgnoreCase("graphml"))) {

 			StorageFactory.useDBServer();
 			StorageFactory.createEmpty();

 			doLater = "g.makeFromGML(\""+
 			    fileName.replace('\\','/')+
 			    "\")";
 		    } else if ((fileExtension.equalsIgnoreCase("net")) ||
			       (fileExtension.equalsIgnoreCase("paj")) ||
			       (fileExtension.equalsIgnoreCase("pajek"))) {

 			StorageFactory.useDBServer();
 			StorageFactory.createEmpty();

 			doLater = "g.makeFromPajek(\""+
 			    fileName.replace('\\','/')+
 			    "\")";
 		    } else { 
 			StorageFactory.useDBServer();
			StorageFactory.loadFromFile(fileName);
		    }
 		    return(true);
		}
	    } else {
		return(false);
	    }
	} catch (Exception e) {
	    exceptionHandle(e);
	}
	return(false);
    }

    /**
     * user seems to want to start with a "blank" database
     * @return true if succeeded, false otherwise
     */
    private static boolean emptyChooser() {
	try {
	    //System.out.println("using in memory database");
	    StorageFactory.useDBServer();
	    StorageFactory.createEmpty();
	    return(true);
	} catch (Exception e) {
	    exceptionHandle(e);
	}
	return(false);
    }

    /**
     * do some initial setup to the UI look and feel
     */
    public static void configureUI() {
	//ClearLookManager.setMode(ClearLookMode.DEBUG);

        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
        Options.setDefaultIconSize(new Dimension(18, 18));

        String lafName =
            LookUtils.IS_OS_WINDOWS_XP
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();


	//	System.out.println(lafName + " " + LookUtils.IS_OS_WINDOWS_XP);

        try {
            UIManager.setLookAndFeel(lafName);
        } catch (Exception e) {
            System.err.println("Can't set look & feel");
	    exceptionHandle(e);
        }
    }

    static BufferedReader reader = null;

    public static void exceptionHandle(Throwable e2) {
	if (e2 instanceof PyException) {
	    e2.printStackTrace();
	} else {
	    System.out.println(e2.toString() + 
			       "\n\t(Use Help->Error Log for more details)");
	}
	ExceptionWindow.getExceptionWindow(e2);
    }

    /**
     * the main loop, this will get us running.  You'll usually want
     * either the guiMode or textMode enabled but you can 
     * do both or neither.
     * @param uiMode which ui to use
     * (currently VisFactory.PICCOLO,VisFactory.TOUCGRAPH, 
     * VisFactory.PREFUSE, or VisFactory.NOVIS)
     * @param guiMode run the interpeter as a seperate console
     * @param textMode run the intepreter in the original console
     */
    public static void init(int uiMode, boolean guiMode, boolean textMode) 
	throws Exception {

	initUI(uiMode,guiMode,textMode);
	initRest(uiMode,guiMode,textMode);
    }

    public static BufferedReader outHandle = null;

    public static BufferedReader errHandle = null;

    public static boolean handleOver = true;

    public static void initHandles() throws Exception {

	// override stderr/stdout handles to force them to console

	if (handleOver)
	    return;

	if (outHandle == null) {
	    PipedInputStream pis = new PipedInputStream();
	    PipedOutputStream pos = new PipedOutputStream(pis);
	    System.setOut(new PrintStream(pos));
	    outHandle = new BufferedReader(new InputStreamReader(pis)); 
	}
	if (errHandle == null) {
	    PipedInputStream pis = new PipedInputStream();
	    PipedOutputStream pos = new PipedOutputStream(pis);
	    System.setErr(new PrintStream(pos));
	    errHandle = new BufferedReader(new InputStreamReader(pis)); 
	}
    }
    
    private static boolean sync = false;

    /**
     * should layouts run in their own threads?
     * @pyexport
     */
    public static void setSynchronous(boolean state) {
	sync = state;
    }

    /**
     * thread management
     * @pyexport
     */
    public static boolean getSynchronous() {
	return(sync);
    }

    private static boolean mtf = false;

    /**
     * should objects in the visualization be moved to the front
     * when they change
     * @pyexport
     */
    public static void setMTF(boolean state) {
	mtf = state;
    }

    /**
     * Are objects being moved to the front when they change
     * @pyexport
     */
    public static boolean getMTF() {
	return(mtf);
    }

    public static void initUI(int uiMode, boolean guiMode, boolean textMode) 
	throws Exception {
	if ((textMode) && (guiMode)) {
	    guiMode = true;
	    textMode = false;
	}

	//System.out.println("before");

	final InterpreterAbstraction interp = getInterpreter();

	//System.out.println("after");
	final int uiMode2 = uiMode;

	try {
	    javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
		    public void run() {
			VisFactory.setFactory(uiMode2);
		    }
		});
	} catch (Exception e) {
	    exceptionHandle(e);
	}
	  

	final FrameListener myFrame = VisFactory.getFactory().getDisplay();
	myF = myFrame;
	myF.setDisplayBackground(Color.black);

	try {
	    g = new Graph(myFrame, interp, multiEdge);
	    StorageFactory.getSL().refresh(g);
	} catch (Exception e) {
	    exceptionHandle(e);
	}
	
	if (enableMainUI)
	    myWin = new MainUIWindow((java.awt.Component)myFrame);
	
	VisFactory.getFactory().runNow();
	
	/*
	These get out-of-date when g is modified.  Do we still want these?
	interp.exec("nodes = g.vertices");
	interp.exec("edges = g.edges");
	*/
	Iterator nodes = g.getVertices().iterator();
	while (nodes.hasNext())
	    {
		Node node = (Node)nodes.next();
		interp.setImmutable(node.getName(), node);
	    }
	Enumeration en = Colors.colors.keys();
	while(en.hasMoreElements()) {
	    String key = (String)en.nextElement();
	    Color val = (Color)Colors.colors.get(key);
	    interp.setImmutable(key,val.toString());
	}

	if (g.containsDirected()) {
	    VisFactory.getFactory().setDirected(true);
	}
    }

    public static void initRest(int uiMode, boolean guiMode, boolean textMode) 
	throws Exception {

	if (guiMode) {
	    initHandles();
	}

	final FrameListener myFrame = VisFactory.getFactory().getDisplay();
	final InterpreterAbstraction interp = getInterpreter();

	interp.setImmutable("v", myFrame);
	interp.setImmutable("true",new Integer(1));
	interp.setImmutable("false",new Integer(0));
	
	interp.setImmutable("Node", g.getNodeSchema());
	interp.setImmutable("Edge", g.getEdgeSchema());
	interp.setImmutable("g", g);
	interp.setImmutable("db", StorageFactory.getSL());
	R myR = new R();
	interp.setImmutable("r",myR);
	interp.setImmutable("interp",interp);
	interp.setImmutable("vf",VisFactory.getFactory());
	interp.setImmutable("graphevents",GraphEvents.getGraphEvents());
	interp.setImmutable("shapeDB",ShapeDB.getShapeDB());

	if (enableMainUI) {
	    interp.setImmutable("ui",myWin);
	    
	    try {
		java.net.URL mbpy = 
		    interp.getClass().getResource("/scripts/MenuBar.py");
		//System.out.println("menu: " + mbpy);
		if (mbpy != null) {
		    interp.execfile(mbpy.openStream());
		}
	    } catch (Throwable fe) {
		exceptionHandle(fe);
	    }
	    
	    myWin.validate();
	}

	try {
	    java.net.URL mbpy = null;
	    mbpy = 
		interp.getClass().getResource("/scripts/Main.py");
	    if (mbpy != null) {
		interp.execfile(mbpy.openStream());
	    }
	} catch (Throwable fe) {
	    exceptionHandle(fe);
	    try {
		if (appletMode) {
		    java.net.URL mbpy = null;
		    mbpy = 
			interp.getClass().getResource("/scripts/Main-applet.py");
		    if (mbpy != null) {
			interp.execfile(mbpy.openStream());
		    }
		}
	    } catch (Throwable fe2) {
		exceptionHandle(fe2);
	    }
	}

	//CommandStack cs = new CommandStack(interp,"current.cs");
	//interp.set("cs",cs);
	//cs.setVisible(true);

	Iterator fields = g.getEdgeSchema().fields();
	while (fields.hasNext())
	{
		Field field = (Field)fields.next();
		interp.setImmutable(field.getName(), field);
	}

	fields = g.getNodeSchema().fields();
	while (fields.hasNext())
	{
		Field field = (Field)fields.next();
		interp.setImmutable(field.getName(), field);
	}

	if (guiMode) {	
	    if (gplFree) {
		System.out.println("running in GPL free mode, unable to use UI console, reverting to text...");
		guiMode = false;
		textMode = true;
	    } else {
		if (enableMainUI) {
		    tpjc = new TextPaneJythonConsole((PythonInterpreter)interp);
		    myWin.dock(tpjc);
		}
	    }
	    //LabNotebook.createNotebook((PythonInterpreter)interp);
	}
	
	myFrame.repaint();

	if (doLater != null) {
	    final InterpreterAbstraction ia = interp;
	    final String doLater2 = doLater;
	    try {
		javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
			public void run() {
			    ia.exec(doLater2);
			}
		    });
	    } catch (Exception e) {
		exceptionHandle(e);
	    }

	} 
	
	if (pythonToExec != null) {
	    Iterator it = pythonToExec.iterator();
	    while(it.hasNext()) {
		final String fl = (String)it.next();
		try {
		    javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
			    public void run() {
				interp.execfile(fl);
				StatusBar.setStatus("executed: " + fl);
			    }
			});
		} catch (Exception e) {
		    exceptionHandle(e);
		}
	    }
	}

	myFrame.center();

	//System.out.println(myFrame);

	//new DragWindow();

	if (textMode) {
	    reader = new BufferedReader(new InputStreamReader(System.in));
	    String s = readLine();

	    Tracker.disableTracker(); // we don't care about tracking

	    while (!s.equals("quit")) {
		if (s.equals("rmode")) {
		    myR.rmode(reader);  // let R do everything
		    s = readLine();
		    continue;
		}
		final String s2 = s;
		// because certain things aren't thread safe we're going
		// to run commands inside an invokeLater thread
		// 
		// this kind of sucks because we have to make a new thread
		// for every command, but it's the only way to make things
		// thread safe.  
		//
		// could potentially add commands to a queue in a
		// a swing thread?
		//
		try {
		    StatusBar.setStatus("");
		    javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
			    public void run() { 
				try {
				    Object value = interp.eval(s2);
				    
				    checkFrozen(interp);
				    interp.set("_", value);
				    
				    if (value instanceof PyFunction) {
					checkFrozen(interp);
					interp.set("_", 
						   interp.eval("apply(_, ())"));
				    }
				    
				    checkFrozen(interp);
				    interp.exec("if _ != None: print _");
				    
				} catch(PySyntaxError e)
				    {
					try
					    {
						checkFrozen(interp);
						interp.exec(s2);
					    }
					catch(Throwable e2) {
					    exceptionHandle(e2);	    
					}
					StatusBar.runProgressBar(false);
				    } catch(Throwable e3) {
					exceptionHandle(e3);
					StatusBar.runProgressBar(false);
				    }
				myFrame.repaint();
			    } 
			}); 
		} catch (InterruptedException e) {
		    StatusBar.runProgressBar(false);
		    exceptionHandle(e);
		}
		checkFrozen(interp);
		s = readLine();
	    }
	    shutdown();
	}
    }
    
    public static void checkFrozen(InterpreterAbstraction interp) {
	while(interp.isFrozen()) {
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {
		interp.freeze(false);
	    }
	}
    }

    /**
     * shutdown and exit
     */
    public static void shutdown() {
	if (interpSingleton != null) 
	    interpSingleton.stoplog();

	StorageFactory.shutdown();
	myF = null;
	g = null;
	interpSingleton = null;
	VisFactory.shutdown();
	
	if (tpjc != null) {
	    tpjc.shutdown();
	}

	if (myWin != null)
	    myWin.dispose();

	myWin = null;

	if (!appletMode)
	    System.exit(0);
    }

    /** 
     * reads the next line from the display (blocking)
     */
    private static String readLine() {
	System.out.print("> ");
	
	try
	    {
		String s = reader.readLine();
		if (s.endsWith(":")) {
		    System.out.print(". ");
		    String t = reader.readLine();
		    while(!t.equals("")) {
			s = s + "\n" + t;
			System.out.print(". ");
			t = reader.readLine();
		    }
		}
		return(s);
	    }
	catch (IOException e)
	    {
		throw new Error(e);
	    }
    }
}
