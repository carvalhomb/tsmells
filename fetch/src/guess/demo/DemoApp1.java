package demo;

import com.hp.hpl.guess.Guess;
import com.hp.hpl.guess.storage.StorageFactory;
import com.hp.hpl.guess.ui.StatusBar;
import com.hp.hpl.guess.ui.VisFactory;
import org.python.util.PythonInterpreter;

/**
 * this is an example that shows you how to use GUESS in your own
 * application.  It simply starts up a GUESS window and shows 
 * you how grab hold of the interpreter
 *
 * On the command line it expects a gdf/xml file as an argument
 * 
 * @author Eytan Adar
 */
public class DemoApp1 {

    public static void main(String[] args) {
	try {
	    // we do this to load up the look and feel
	    Guess.configureUI();

	    // we're going to tell guess to use a SQL database
	    StorageFactory.useDBServer();

	    // then load the gdf file
	    // you can skip this if you don't want to load
	    // any initial data
	    StorageFactory.loadFromFile(args[0]);

	    // start running
	    // we tell GUESS to use the PICCOLO interface
	    // first true is for gui mode to be enabled
	    // and we want to disable to console
	    Guess.init(VisFactory.PICCOLO,true,false);

	    // now you can grab the interpreter if you want
	    PythonInterpreter pi = (PythonInterpreter)Guess.getInterpreter();

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

	} catch (Exception e) {
	    StatusBar.setErrorStatus(e.toString());
	    e.printStackTrace();
	}
    }
}
