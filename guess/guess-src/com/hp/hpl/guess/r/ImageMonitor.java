package com.hp.hpl.guess.r;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;
import com.hp.hpl.guess.ui.GuessJFrame;
import com.hp.hpl.guess.ui.ExceptionWindow;

public class ImageMonitor extends GuessJFrame {

    private String toMonitor = "";

    private long lastModified = 0;

    private BufferedImage bi = null;

    private ImagePanel ip = null;

    public ImageMonitor(String toMonitor) {
	super("R Image Output");
        this.setSize(500, 500);
	this.toMonitor = toMonitor;
	this.setResizable(false);
	//final String toMonitor = toMonitoritor;

	try {
	    File test = new File(toMonitor);
	    test.delete();
	} catch (Exception e) {}
	ip = new ImagePanel(this);
	getContentPane().add(ip);
	pack();
	validate();

    }
    
    public void repaint() {
	super.repaint();
	try {
	    File test = new File(toMonitor);
	    if ((test.exists()) && (test.length() > 5000)) {
		if (test.lastModified() > lastModified) {
		    this.show();
		    bi = 
			ImageIO.read(new java.io.FileInputStream(test));
		    lastModified = test.lastModified();
		    pack();
		}
	    }
	} catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
	}
    }
	    
    class ImagePanel extends JPanel {
	
	ImageMonitor im = null;

	public ImagePanel(ImageMonitor im) {
	    super();
	    this.im = im;
	}

	public Dimension getMinimumSize() {
	    if (bi == null) {
		return(new Dimension(0,0));
	    } else {
		return(new Dimension(bi.getWidth(),
				     bi.getHeight()));
	    }
	}

	public Dimension getPreferredSize() {
	    return(getMinimumSize());
	}

	public void paint(Graphics g) {
	    try {
		if (bi != null) {
		    setSize(bi.getWidth(),bi.getHeight());
		    g.drawImage(bi, 0,0, null);
		}
	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}
    }
}
