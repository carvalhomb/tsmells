package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.jgoodies.looks.*;

import com.hp.hpl.guess.freehep.*;
import com.hp.hpl.guess.*;

public class GuessJFrame extends JFrame {

    private JMenuBar jm = new JMenuBar();

    private Dockable dockable = null;

    public GuessJFrame() {
	super();
	initMenus();
    }

    public GuessJFrame(Dockable d) {
	super(d.getTitle());
	initMenus();
	getContentPane().add((Component)d);
	d.setWindow(this);
	JMenu fileMenu = new JMenu("Window");
	JMenuItem jmi = new JMenuItem("Dock");
	this.dockable = d;
	final Dockable dc = d;

	jmi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("Dock")) {
			getContentPane().removeAll();
			hide();
			Guess.getMainUIWindow().dock(dc);
		    }
		}
	    });
	fileMenu.add(jmi);
	jm.add(fileMenu);
    }

    public GuessJFrame(GraphicsConfiguration gc) {
	super(gc);
	initMenus();
    }

    public GuessJFrame(String title) {
	super(title);
	initMenus();
    }

    public GuessJFrame(String title, GraphicsConfiguration gc) {
	super(title,gc);
	initMenus();
    }

    public void initMenus() {

	jm.putClientProperty(Options.HEADER_STYLE_KEY, Boolean.TRUE);
	JMenu fileMenu = new JMenu("File");
	JMenuItem jmi = new JMenuItem("Export Image");
	jmi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    if (event.getActionCommand().equals("Export Image")) {
			HEPDialog hd = new HEPDialog(null);
			hd.showHEPDialog(null,"Export File",
					 getContentPane(),
					 "output.jpg",false);
		    }
		}
	    });
	fileMenu.add(jmi);
	jm.add(fileMenu);
	setJMenuBar(jm);
	validate();
 	addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e) {
		    if (dockable != null) {
			dockable.opening(false);
		    }
		}
	    });
    }			      

    public void exportGIF(String filename) {
	HEPWriter.export(filename,getContentPane(),HEPWriter.GIF);
    }

    public void exportJPG(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.JPG);
    }

    public void exportPDF(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.PDF);
    }

    public void exportPS(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.PS);
    }

    public void exportEPS(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.EPS);
    }

    public void exportSVG(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.SVG);
    }

    public void exportSWF(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.SWF);
    }

    public void exportJAVA(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.JAVA);
    }

    public void exportCGM(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.CGM);
    }

    public void exportEMF(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.EMF);
    }

    public void exportPNG(String filename){
	HEPWriter.export(filename,getContentPane(),HEPWriter.PNG);
    }

}
