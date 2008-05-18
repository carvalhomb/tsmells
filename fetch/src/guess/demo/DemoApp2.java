package demo;

import com.hp.hpl.guess.Guess;
import com.hp.hpl.guess.storage.StorageFactory;
import com.hp.hpl.guess.ui.StatusBar;
import com.hp.hpl.guess.ui.VisFactory;
import org.python.util.PythonInterpreter;
import javax.swing.JFrame;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.lang.reflect.InvocationTargetException;

/**
 * this is an example that shows you how to use GUESS in your own
 * application.  It will load up the graph "canvas" into a new
 * window and will not create the default GUESS application window.
 *
 * On the command line it expects a gdf/xml file as an argument
 * 
 * @author Eytan Adar
 */
public class DemoApp2 extends JFrame {

    public Rectangle getDefaultFrameBounds() {
	return new Rectangle(100, 100, 800, 600);
    }		

    public DemoApp2(String filename) throws Exception {

	setTitle("DemoApp2");

	// You probably don't need
	// a gridbag, but I copied code out of the 
	// main UI system
	getContentPane().setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.weighty = 1;
	c.weightx = 1;
	c.gridx = 0;
	c.gridy = 0;

	// don't forget to set the size or you'll have
	// problems with PICCOLO
	setBounds(getDefaultFrameBounds());

	// we're going to tell GUESS to use a SQL database
	StorageFactory.useDBServer();
	
	// then load the gdf/xml file
	// you can skip this if you don't want to load
	// any initial data
	StorageFactory.loadFromFile(filename);
	
	// we tell GUESS not to create the ui window
	Guess.enableMainUI(false);

	// start running, if you're not using PICCOLO you can get away
	// with just calling the single init function (instead of
	// initUI and then initRest) but PICCOLO has the feature of
	// getting very confused with the bounds aren't set correctly.
	// So if you're using touchgraph or prefuse you can do:
	// Guess.init(VisFactory.PREFUSE,false,false);
	// otherwise:

	// we tell GUESS to use the PICCOLO interface
	// we disable both interfaces (GUI and console)
	Guess.initUI(VisFactory.PICCOLO,false,false);
	
	// now we can set the Content pane for
	// this frame to the "canvas".  
	getContentPane().add((Component)Guess.getFrame(),c);

	// you'll want to show at this point because 
	// the next part depends on having a window the
	// canvas showing and sized appropriately
	show();

	// finish setting up the environment
	Guess.initRest(VisFactory.PICCOLO,false,false);
	
	// now you can grab the interpreter if you want
	PythonInterpreter pi = 
	    (PythonInterpreter)Guess.getInterpreter();
	
	    		    
	// and start dumping commands to it 
	// e.g. pi.eval(".....");
	// or pi.exec("....");
	System.out.println("2+2 = " + pi.eval("2+2"));
	pi.exec("gemLayout()");

	// you may want/need to run some of these things in the swing
	// thread to avoid race conditions
	// e.g. 
	// try {
	//    javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
	//	    public void run() {
	//		pi.exec("...");
	//	    }
	//	});
	//} catch (Exception e) {
	//     ...
	//  }
    }

    public static void main(String[] args) throws Exception {
	DemoApp2 da2 = new DemoApp2(args[0]);
    }
}
