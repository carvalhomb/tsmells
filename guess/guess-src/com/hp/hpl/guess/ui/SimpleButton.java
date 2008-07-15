package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.GridBagConstraints;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.*;
import javax.swing.border.LineBorder;

public class SimpleButton extends JButton {

    private static final LineBorder unclicked = new LineBorder(Color.gray,1);
    private static final LineBorder clicked = new LineBorder(Color.blue,1);

    Dimension size = new Dimension(20,20);
    
    public int bType = 0;
    
    public SimpleButton(String s, int bType) {
	this(s,bType,null);
    }

    public SimpleButton(String s, int bType, String tt) {
	super();
	setIcon(new ImageIcon(getClass().getResource("/images/"+s)));
	this.bType = bType;
	if (tt != null)
	    setToolTipText(tt);
	setBorder(unclicked);
    }
    
    public void click(boolean state) {
	if(state) {
	    setBorder(clicked);
	} else {
	    setBorder(unclicked);
	}
    }

    public Dimension getMinimumSize() {
	return(size);
    }
    public Dimension getMaximumSize() {
	return(size);
    }
    
    public Dimension getPreferredSize() {
	return(size);
    }
}
