package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.GridBagConstraints;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.*;

public class GoneIn30 extends Thread {

    JProgressBar jpb = new JProgressBar(0,30);

    private boolean running = false;

    private static GoneIn30 singleton = null;

    private JFrame jf = new JFrame("Timer...");

    public static GoneIn30 getWindow() {
	return(new GoneIn30());
    }

    private GoneIn30() {
	jf.getContentPane().add(jpb);
	jf.pack();
	jf.setResizable(false);
	jf.setBounds(200,200,200,50);
    }

    public void run() {

	if (running)
	    return;

	jf.setBounds(200,200,200,50);
	jf.show();

	running = true;

	for (int i = 0 ; i < 30 ; i++) {

	    if (running == false) 
		break;

	    try {
		Thread.sleep(1000);
	    } catch (Exception e) {
	    }
	    jpb.setValue(i);
	    //System.out.println(i+"");
	}

	jf.hide();
	jpb.setValue(0);
	running = false;
    }

    public void stopTimer() {
	running = false;
	jf.hide();
    }
}
