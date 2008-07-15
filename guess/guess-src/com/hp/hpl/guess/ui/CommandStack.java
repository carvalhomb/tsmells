package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.python.core.*;
import org.python.util.*;

/** 
 * Create a JList whose KeyListener adds/removes elements from
 * the lists DefaultListModel.
 */

public class CommandStack extends JPanel {
    
    PythonInterpreter interp = null;	
    DefaultListModel model = null;
    JList list = null;
    Hashtable userFriendly = new Hashtable();
    String filename = null;

    class myML extends MouseAdapter {
	PythonInterpreter interp = null;	
	JList list = null;
	Hashtable userFriendly = null;

	public myML(PythonInterpreter interp, JList list, Hashtable uf) {
	    this.interp = interp;
	    this.list = list;
	    this.userFriendly = uf;
	}
	
	public void mouseClicked(MouseEvent e) {
	    String selected = (String)list.getSelectedValue(); 
	    if (userFriendly.containsKey(selected)) {
		interp.exec(""+userFriendly.get(selected));
	    } else {
		interp.exec(""+selected);
	    }
	}
    }

    public CommandStack(PythonInterpreter interp) {
	this(interp,null);
    }

    public CommandStack(PythonInterpreter interp, String filename) {
	this.interp = interp;
	this.model = new DefaultListModel();
	this.list = new JList(model);
	//list.setSelectionModel(ListSelectionModel.SINGLE_SELECTION);

	MouseListener mListener = new myML(interp,list,userFriendly);

	list.addMouseListener(mListener);

	JScrollPane scrollPane = new JScrollPane(list);
	add(scrollPane, BorderLayout.CENTER);	
	
	if (filename != null) {
	    this.filename = filename;
	    load(filename);
	}
	//setTitle("previous commands");
    }
    
    public void addCommand(String command) {
	model.addElement(command);
	if (!isVisible())
	    setVisible(true);
    }

    public void addCommand(String command, String uf) {
	model.addElement(uf);
	userFriendly.put(uf,command);
	if (!isVisible())
	    setVisible(true);
    }

    public void shutdown() {
	save(this.filename);
    }

    public void save(String filename) {
	if (filename != null) {
	    try {
		BufferedWriter bw = 
		    new BufferedWriter(new FileWriter(filename));
		Enumeration en = model.elements();
		while(en.hasMoreElements()) {
		    String elem = (String)en.nextElement();
		    bw.write(elem + "|");
		    if (userFriendly.containsKey(elem)) {
			bw.write((String)userFriendly.get(elem));
		    } 
		    bw.write("\n");
		}
		bw.close();
	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}
    }

    public void load(String filename) {
	if (filename != null) {
	    try {
		BufferedReader br = 
		    new BufferedReader(new FileReader(filename));
		String line = null;
		while ((line = br.readLine()) != null) {
		    line = line.trim();
		    int brk = line.indexOf("|");
		    String c1 = line.substring(0,brk);
		    String c2 = null;
		    try {
			c2 = line.substring(brk + 1);
		    } catch (Exception er) {}
		    if ((c2 == null) || (c2.equals(""))) {
			addCommand(c1);
		    } else {
			addCommand(c2,c1);
		    }
		    setVisible(true);
		}
		br.close();
	    } catch (FileNotFoundException e) {
		ExceptionWindow.getExceptionWindow(e);
	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}
    }
}
