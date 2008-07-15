package com.hp.hpl.guess.ui;

import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Writer;

import java.util.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.Point;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import javax.swing.text.html.HTMLEditorKit;
import com.hp.hpl.guess.util.intervals.*;
import com.hp.hpl.guess.*;

public class UserMenuItem extends JMenuItem implements ActionListener {

    private Vector menuListeners = new Vector();

    private EditorPopup ep = null;

    public UserMenuItem(String s, EditorPopup ep) {
	super(s);
	addActionListener(this);
	this.ep = ep;
    }

    public void addUIListener(MenuListener al) {
	menuListeners.addElement(al);
    }
    
    public void notifyEvent() {
	for (int i = 0 ; i < menuListeners.size() ; i++) {
	    ((MenuListener)menuListeners.elementAt(i)).menuEvent(ep.selected);
	}
    }

    public void actionPerformed(ActionEvent e) {
	notifyEvent();
    }
}
