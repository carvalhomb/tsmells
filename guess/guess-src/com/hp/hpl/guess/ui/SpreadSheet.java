package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;

import com.jgoodies.looks.*;
import com.hp.hpl.guess.freehep.*;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.storage.StorageFactory;

import java.sql.Types;

/**
 * @pyobj SpreadSheet
 */
public class SpreadSheet extends JPanel implements Dockable {

    private static SpreadSheet n_singleton = null;
    private static SpreadSheet e_singleton = null;

    private AbstractTableModel gtm = null;

    private boolean visible = false;

    public static SpreadSheet getNodeSpreadSheet() {
	if (n_singleton == null) {
	    n_singleton = new SpreadSheet("Nodes",true);
	}
	return(n_singleton);
    }

    public static SpreadSheet getEdgeSpreadSheet() {
	if (e_singleton == null) {
	    e_singleton = new SpreadSheet("Edges",false);
	}
	return(e_singleton);
    }

    /**
     * @pyexport infowindow
     */
    public static void create() {
	//Guess.getMainUIWindow().dock(getSpreadSheet());
    }


    public Dimension getPreferredSize() {
	return(new Dimension(200,600));
    }

    public int getDirectionPreference() {
	return(MainUIWindow.HORIZONTAL_DOCK);
    }

    public void opening(boolean state) {
	visible = state;
    }

    public void attaching(boolean state) {
    }

    private String title = "";

    public String getTitle() {
	return(title);
    }

    private GuessJFrame myParent = null;

    public GuessJFrame getWindow() {
	return(myParent);
    }

    public void setWindow(GuessJFrame gjf) {
	myParent = gjf;
    }

    private SpreadSheet(String title,boolean nOrE) {
	this.title = title;

	if (nOrE) {
	    gtm = StorageFactory.getSL().getNodeTable();
	} else {
	    gtm = StorageFactory.getSL().getEdgeTable();
	}
	JTable table = new JTable(gtm);
	JScrollPane scrollpane = new JScrollPane(table);
	setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	c.weightx = 1;
	c.gridx = 0;
	c.gridy = 0;
	c.weighty = 1;
	c.fill = GridBagConstraints.BOTH;

	add(scrollpane,c);
	setBounds(getDefaultFrameBounds());
    }

    public Rectangle getDefaultFrameBounds() {
	return new Rectangle(50, 50, 300, 600);
    }


}
