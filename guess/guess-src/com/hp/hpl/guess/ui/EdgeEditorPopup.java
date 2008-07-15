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

public class EdgeEditorPopup extends GraphElementEditorPopup {

    public static EditorPopup singleton = null;

    public String getHeader() {
	return("Edge Options");
    }

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

    public static EditorPopup getPopup() {
	if (singleton == null) {
	    singleton = new EdgeEditorPopup(Guess.getInterpreter());
	}
	return(singleton);
    }

    protected EdgeEditorPopup(InterpreterAbstraction jython) {
	super(jython);
	setLabel("Edge Menu");
	
	// add functions here

    }
}
