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

public abstract class EditorPopup extends JPopupMenu {
    
    InterpreterAbstraction jython = null;
    protected Collection selected = null;
    protected Object currentH = null;

    public abstract String getHeader();

    protected boolean sep = false;

    public static EditorPopup getPopup() {
	return(null);
    }

    protected EditorPopup(InterpreterAbstraction jython) {
	super("Options");
	this.jython = jython;
	setBackground(Color.white);
	JMenuItem header = new JMenuItem("<HTML><B>"+getHeader()+"</B></HTML>");
	header.setForeground(Color.white);
	header.setBackground(Color.black);
	add(header);
    }

    public void show(Component inv, int x, int y, 
		     Collection selected, Object currentH) {
	this.selected = selected;
	this.currentH = currentH;
	show(inv,x,y);
    }

    public JMenuItem createJMI(String s) {
	return(new UserMenuItem(s,this));
    }
    
    public void cacheContent(Collection selected, Object currentH) {
	this.selected = selected;
	this.currentH = currentH;
    }
}
