package com.hp.hpl.guess.ui;

import java.awt.Color;

public class GuessColor extends Color {

    public GuessColor(int r, int g, int b, int a) {
	super(r,g,b,a);
    }
    
    public GuessColor(int r, int g, int b) {
	super(r,g,b);
    }

    public GuessColor(Color c) {
	super(c.getRed(),
	      c.getGreen(),
	      c.getBlue(),
	      c.getAlpha());
    }

    public String toString() {
	if (name != null) {
	    return(name);
	} else {
	    return(getRed()+","+getGreen()+","+
		   getBlue()+","+getAlpha());
	}
    }

    private String name = null;
    
    public void setName(String name) {
	this.name = name;
    }				    
}
