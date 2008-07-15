package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.hp.hpl.guess.Guess;
import com.hp.hpl.guess.Version;
import com.hp.hpl.guess.Node;
import java.io.*;
import com.hp.hpl.guess.freehep.HEPDialog;
import com.hp.hpl.guess.piccolo.GFrame;

import com.jgoodies.looks.*;


public class GMenuBar extends JMenuBar {

    String[ ] fileItems = 
	new String[ ] { "Export Image...",
			"Export Screenshot...",
			"Run Script...",
			"Save GDF..."};

    char[ ] fileShortcuts = { 'I','S','R'};
    
    String[] layoutItems = 
	new String[] {"Bin Pack","GEM","Circular",
		      "Physics","Kamada-Kawai",
		      "Fruchterman-Rheingold","Spring",
		      "MDS","Random","Radial"};

    JCheckBoxMenuItem logItem = new JCheckBoxMenuItem("Log...");

    HEPDialog hd = new HEPDialog(null);

    public String getInputFromUser(Object question, String title,
				   Object def) {
	// doesn't make use of title at present
	String toRet = JOptionPane.showInputDialog(question,def);
	return(toRet);
    }

    protected JMenu fileMenu = new JMenu("File");
    protected JMenu editMenu = new JMenu("Edit");
    protected JMenu displayMenu = new JMenu("Display");
    protected JMenu layoutMenu = new JMenu("Layout");
    protected JMenu helpMenu = new JMenu("Help");

    public boolean displayProtected() {
	//System.out.println(Guess.getAppletMode() + " " + Guess.getSignedAppletMode());
	if ((Guess.getAppletMode() == true) &&
	    (Guess.getSignedAppletMode() == false)) {
	    return(false);
	} else {
	    return(true);
	}
    }

    public GMenuBar() {
	this.putClientProperty(Options.HEADER_STYLE_KEY, Boolean.TRUE);
	

	ActionListener displayListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("Background Color")) {
			FrameListener fl = 
			    VisFactory.getFactory().getDisplay();
			Color c = 
			    JColorChooser.showDialog(null,
						     "Please pick a color",
						     fl.getDisplayBackground());
			if (c != null) {
			    fl.setDisplayBackground(c);
			    StatusBar.setStatus("v.setDisplayBackground(\""+
						c.getRed()+","+
						c.getGreen()+","+
						c.getBlue()+"\")");
			}
		    } else if (event.getActionCommand().equals("Information Window")) {
			InfoWindow.create();
		    } else if (event.getActionCommand().equals("Center")) {
			VisFactory.getFactory().getDisplay().center();
		    } else if (event.getActionCommand().equals("Toggle Arrows")) {
			VisFactory.getFactory().setDirected(!VisFactory.getFactory().getDirected());
		    }
		}
	    };


	JMenuItem bgcolor = new JMenuItem("Center");
	bgcolor.addActionListener(displayListener);
	displayMenu.add(bgcolor);
	bgcolor = new JMenuItem("Background Color");
	bgcolor.addActionListener(displayListener);
	displayMenu.add(bgcolor);
	bgcolor = new JMenuItem("Information Window");
	bgcolor.addActionListener(displayListener);
	displayMenu.add(bgcolor);
	bgcolor = new JMenuItem("Toggle Arrows");
	bgcolor.addActionListener(displayListener);
	displayMenu.add(bgcolor);

	ActionListener printListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("Exit")) {
			Guess.shutdown();
		    } else if (event.getActionCommand().equals("Export Image...")) {
			if (VisFactory.getUIMode() == VisFactory.PICCOLO) {
			    hd.showHEPDialog(null,"Export Image",
					     (GFrame)VisFactory.getFactory().getDisplay(),
					     "output.jpg",true);
			} else {
			    StatusBar.setErrorStatus("This method is only supported in piccolo mode right now");
			}
		    } else if (event.getActionCommand().equals("Export Screenshot...")) {
			hd.showHEPDialog(null,"Export Screenshot",
					 (GFrame)VisFactory.getFactory().getDisplay(),
					 "output.jpg",false);
		    } else if (event.getActionCommand().equals("Run Script...")) {
			runScript();
		    } else if (event.getActionCommand().equals("Save GDF...")) {
			saveGDF();
		    } else if (event.getActionCommand().equals("Log...")) {
			logToggle();
		    }
		}
	    };

	ActionListener loadListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("GDF")) {
			loadFromFile("GDF");
		    } else if (event.getActionCommand().equals("XML/GML")) {
			loadFromFile("XML");
		    } else if (event.getActionCommand().equals("Pajek")) {
			loadFromFile("Pajek");
		    } 
		}
	    };

	JMenu load = new JMenu("Load from file");
	JMenuItem l1 = new JMenuItem("GDF");
	l1.addActionListener(loadListener);
	load.add(l1);
	l1 = new JMenuItem("XML/GML");
	l1.addActionListener(loadListener);
	load.add(l1);
	l1 = new JMenuItem("Pajek");
	l1.addActionListener(loadListener);
	load.add(l1);

	fileMenu.add(load);
	if (!displayProtected()) {
	    load.setEnabled(false);
	}

	for (int i=0; i < fileItems.length; i++) {
	    JMenuItem item = null;
	    if (i < fileShortcuts.length) {
		item = new JMenuItem(fileItems[i], fileShortcuts[i]);
	    item.setAccelerator(KeyStroke.getKeyStroke(fileShortcuts[i],
						       Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

	    } else {
		item = new JMenuItem(fileItems[i]);
	    }
	    
	    item.addActionListener(printListener);
	    fileMenu.add(item);
	    if (!displayProtected()) {
		item.setEnabled(false);
	    }
	}
	


	// added the log button sep. since we need to 
	// access it later
	logItem.addActionListener(printListener);
	fileMenu.add(logItem);
	if (!displayProtected()) {
	    logItem.setEnabled(false);
	}

	// we want exit to always be last
	JMenuItem item2 = new JMenuItem("Exit");
	item2.addActionListener(printListener);
	fileMenu.add(item2);


	ActionListener layoutListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    String command = event.getActionCommand();
		    StatusBar.runProgressBar(true);
		    try {
			if (command.equals("GEM")) {
			    Guess.getGraph().gemLayout();
			} else if (command.equals("Radial")) {
			    String centerN = 
				getInputFromUser("Please enter a node to "+
						 "use a the center",
						 "User input","");
			    if (centerN != null) {
				Node x = 
				    Guess.getGraph().getNodeByName(centerN);
				if (x != null) {
				    Guess.getGraph().radialLayout(x);
				    StatusBar.setStatus("radialLayout("+
							centerN+")");
				} else {
				    StatusBar.setErrorStatus("Can't find node named " + centerN);
				}
			    }
			} else if (command.equals("Circular")) {
			    Guess.getGraph().circleLayout();
			    StatusBar.setStatus("circleLayout()");
			} else if (command.equals("Physics")) {
			    Guess.getGraph().physicsLayout();
			    StatusBar.setStatus("physicsLayout()");
			} else if (command.equals("Kamada-Kawai")) {
			    Guess.getGraph().jkkLayout1();
			    StatusBar.setStatus("jkkLayout1()");
			} else if (command.equals("Fruchterman-Rheingold")) {
			    Guess.getGraph().frLayout();
			    StatusBar.setStatus("frLayout()");
			} else if (command.equals("Spring")) {
			    Guess.getGraph().springLayout();
			    StatusBar.setStatus("springLayout()");
			} else if (command.equals("MDS")) {
			    Guess.getGraph().mdsLayout();
			    StatusBar.setStatus("mdsLayout()");
			} else if (command.equals("Random")) {
			    Guess.getGraph().randomLayout();
			    StatusBar.setStatus("randomLayout()");
			} else if (command.equals("Bin Pack")) {
			    Guess.getGraph().binPackLayout();
			    StatusBar.setStatus("binPackLayout()");
			}
		    } catch (Exception e) {
			StatusBar.setErrorStatus(e.toString());
		    }
		    StatusBar.runProgressBar(false);
		}
	    };

	for (int i=0; i < layoutItems.length; i++) {
	    JMenuItem item = new JMenuItem(layoutItems[i]);
	    item.addActionListener(layoutListener);
	    layoutMenu.add(item);
	}

	
	ActionListener helpListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("About GUESS")) {
			JOptionPane.showMessageDialog(null,
						      "Major version: " + Version.MAJOR_VERSION + "\nMinor version: " + Version.MINOR_VERSION);
		    } else if (event.getActionCommand().equals("Error Log")) {
			ExceptionWindow.getExceptionWindow(null).setVisible(true);
		    }
		}
	    };
	
	JMenuItem ver = new JMenuItem("Error Log");
	ver.addActionListener(helpListener);
	helpMenu.add(ver);

	ver = new JMenuItem("About GUESS");
	ver.addActionListener(helpListener);
	helpMenu.add(ver);

	ActionListener editListener = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("Modify Field...")) {
			FieldModWindow.getFieldModWindow();
		    }
		}
	    };
	ver = new JMenuItem("Modify Field...");
	ver.addActionListener(editListener);
	editMenu.add(ver);

	super.add(fileMenu);
	super.add(editMenu);
	super.add(displayMenu);
	super.add(layoutMenu);
	super.add(helpMenu);
    }

    public JMenu add(JMenu c) {
	JMenu temp = getMenu(getMenuCount() - 1);
	if (temp == helpMenu) {
	    remove(getMenuCount() - 1);
	}
	super.add(c);
	super.add(helpMenu);
	return(c);
    }

    File prevRun = null;
    File prevLog = null;
    File prevGDF = null;
    File prevLoad = null;
    
    public void runScript() {
	SunFileFilter filter = new SunFileFilter();
	try {
	    if (prevRun == null) {
		prevRun = new File(".");
	    }
	    JFileChooser chooser = 
		new JFileChooser(prevRun.getCanonicalPath());
	    filter.addExtension("py");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		String fileName = 
		    chooser.getSelectedFile().getAbsolutePath();
		Guess.getInterpreter().execfile(fileName);
		prevRun = new File(fileName);
	    }
	} catch (IOException e) {
	    ExceptionWindow.getExceptionWindow(e);
	    JOptionPane.showMessageDialog(null,
					  "Error loading file " + e,
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	}
    }

    public void saveGDF() {
	SunFileFilter filter = new SunFileFilter();
	try {
	    if (prevGDF == null) {
		prevGDF = new File(".");
	    }
	    JFileChooser chooser = 
		new JFileChooser(prevGDF.getCanonicalPath());
	    filter.addExtension("gdf");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		String fileName = 
		    chooser.getSelectedFile().getAbsolutePath();
		Guess.getGraph().exportGDF(fileName);
		prevGDF = new File(fileName);
	    }
	} catch (IOException e) {
	    ExceptionWindow.getExceptionWindow(e);
	    JOptionPane.showMessageDialog(null,
					  "Error saving file " + e,
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	}
    }

    public void loadFromFile(String type) {
	SunFileFilter filter = new SunFileFilter();
	try {
	    if (prevLoad == null) {
		prevLoad = new File(".");
	    }
	    JFileChooser chooser = 
		new JFileChooser(prevLoad.getCanonicalPath());
	    if (type.equals("GDF")) {
		filter.addExtension("gdf");
	    } else if (type.equals("XML")) {
		filter.addExtension("xml");
		filter.addExtension("gml");
		filter.addExtension("graphml");
	    } else if (type.equals("Pajek")) {
		filter.addExtension("net");
	    }
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		String fileName = 
		    chooser.getSelectedFile().getAbsolutePath();
		if (type.equals("GDF")) {
		    Guess.getGraph().makeFromGDF(fileName);
		} else if (type.equals("XML")) {
		    Guess.getGraph().makeFromGML(fileName);
		} else if (type.equals("Pajek")) {
		    Guess.getGraph().makeFromPajek(fileName);
		}
		prevLoad = new File(fileName);
		VisFactory.getFactory().getDisplay().center();
	    }
	} catch (IOException e) {
	    ExceptionWindow.getExceptionWindow(e);
	    JOptionPane.showMessageDialog(null,
					  "Error loading file " + e,
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	}
    }

    public void logToggle() {
	SunFileFilter filter = new SunFileFilter();
	if (logItem.isSelected() == false) {
	    Guess.getInterpreter().stoplog();
	    logItem.setSelected(false);
	    StatusBar.setStatus("Logging stopped...");
	    return;
	}

	try {
	    if (prevLog == null) {
		prevLog = new File(".");
	    }
	    JFileChooser chooser = 
		new JFileChooser(prevLog.getCanonicalPath());
	    filter.addExtension("py");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File f = chooser.getSelectedFile();
		String fileExtension = filter.getExtension(f);
		String fileName = f.getAbsolutePath();
		if (fileExtension == null) {
		    fileName = fileName + ".py";
		    f = new File(fileName);
		}		
		if (f.exists()) {
		    int yn = 
			JOptionPane.showConfirmDialog(null, 
						      "File " + fileName + " exists, overwrite?","Exists",
						      JOptionPane.YES_NO_OPTION);
		    if (yn == JOptionPane.NO_OPTION) {
			return;
		    }
		}

		Guess.getInterpreter().log(fileName);
		prevLog = new File(fileName);
		logItem.setSelected(true);
		StatusBar.setStatus("Logging started...");
	    }
	} catch (IOException e) {
	    ExceptionWindow.getExceptionWindow(e);
	    JOptionPane.showMessageDialog(null,
					  "Error loading file " + e,
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	}
    }
}
