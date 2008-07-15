package com.hp.hpl.guess.ui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideButton;
import com.jidesoft.utils.SystemInfo;
import com.jidesoft.utils.Lm;

import com.hp.hpl.guess.*;

public class DWButton extends JideButton 
    implements GuessDropListener, Interesting {
    
    private HashSet listeners = new HashSet();

    private String nm = null;

    public void addGuessDropListener(GuessDropListener gdl) {
	listeners.add(gdl);
    }
    
    public void removeGuessDropListener(GuessDropListener gdl) {
	listeners.remove(gdl);
    }
    
    public DWButton(String category, String name) {
	super(name);
	this.nm = category + " / " + name;
	setButtonStyle(JideButton.HYPERLINK_STYLE);
	
	setOpaque(false);
	setPreferredSize(new Dimension(0, 20));
	setHorizontalAlignment(SwingConstants.LEADING);
	
	setRequestFocusEnabled(true);
	setFocusable(true);
	
	setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	setTransferHandler(new GuessTransferHandler());
	DragWindow.create();
	DragWindow.getDragWindow().addButton(category, this);
    }
    
    public void receiveDrop(Object o) {
	Iterator it = listeners.iterator();
	//System.out.println(o);
	while(it.hasNext()) {
	    //System.out.println("calling...");
	    ((GuessDropListener)it.next()).receiveDrop(o);
	}
    }

    public String toString() {
	//Thread.dumpStack();
	return(nm);
    }

    public String getStatusBarString() {
	return(nm);
    }

    public EditorPopup getPopup() {
	return(null);
    }
}
