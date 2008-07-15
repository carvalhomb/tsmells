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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.event.*;
import java.awt.Point;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import javax.swing.text.html.HTMLEditorKit;
import com.hp.hpl.guess.util.intervals.*;
import com.hp.hpl.guess.*;

public class ThrowableEditorPopup extends EditorPopup {
    
    private String[ ] menuItems = new String[ ] {"View Exception Log"};

    public static EditorPopup singleton = null;

    public static JMenuItem addItem(String s) {
	EditorPopup ep = getPopup();

	if (!ep.sep) {
	    ep.addSeparator();
	    ep.sep = true;
	}

	JMenuItem jmi = ep.createJMI(s);
	ep.add(jmi);
	return(jmi);
    }

    public String getHeader() {
	return("Exception Options");
    }

    public static EditorPopup getPopup() {
	if (singleton == null) {
	    singleton = new ThrowableEditorPopup(Guess.getInterpreter());
	}
	return(singleton);
    }

    protected ThrowableEditorPopup(InterpreterAbstraction jython) {
	super(jython);
	setLabel("Exception/Error");

	ActionListener al = new ActionListener(  ) {
		public void actionPerformed(ActionEvent event) {
		    String ac = event.getActionCommand();
		    if (ac.equals("View Exception Log")) {
			ExceptionWindow.getExceptionWindow().show();
		    } 
		} 
	    };
	
	for (int i = 0 ; i < menuItems.length ; i++) {
	    JMenuItem jmi1 = add(menuItems[i]);
	    jmi1.addActionListener(al);
	}
    }
}
