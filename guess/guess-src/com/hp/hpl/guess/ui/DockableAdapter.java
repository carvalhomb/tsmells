package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.jgoodies.looks.*;
import com.hp.hpl.guess.freehep.*;
import com.hp.hpl.guess.*;

public abstract class DockableAdapter extends JPanel implements Dockable {
    
    public int getDirectionPreference() {
	return(MainUIWindow.HORIZONTAL_DOCK);
    }

    public void opening(boolean state) {
	//System.out.println("opening: " + state);
    }

    public void attaching(boolean state) {
	//System.out.println("attaching: " + state);
	if ((state == true) && (myParent != null))
	    myParent.hide();	
    }

    public String getTitle() {
	return("");
    }

    public String toString() {
	return(getTitle());
    }

    private GuessJFrame myParent = null;

    public GuessJFrame getWindow() {
	return(myParent);
    }

    public void setWindow(GuessJFrame gjf) {
	myParent = gjf;
    }
}
