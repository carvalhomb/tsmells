package com.hp.hpl.guess.ui;

import java.util.*;
import java.awt.Color;

/**
 * a dummy class to hold the various colors for use in the display.
 * Intended to elliminate redundancy.
 *
 * @author Eytan Adar
 * Copyright (c) 2003, Hewlett Packard Labs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of the Hewlett Packard nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @pyobj Colors
 *
 */
public abstract class Colors {

    public static Hashtable colors = new Hashtable();

    public static void main(String[] args) {
	ArrayList st = new ArrayList(colors.keySet());
	Collections.sort(st);
	System.out.println("<FONT FACE=ARIAL><TABLE>");
	int cnt = 0;
	Iterator it = st.iterator();
	while(it.hasNext()) {
	    if (cnt == 0) {
		System.out.println("<TR>");
	    }
	    String s = (String)it.next();
	    Color c = (Color)colors.get(s);
	    String t = RGBtoHex(c);
	    System.out.println("<TD BGCOLOR="+t + ">" +
			        "&nbsp;&nbsp;&nbsp;</TD><TD>"+s + " " + 
			       c.getRed() + "," + 
			       c.getGreen() + "," +
			       c.getBlue() + "</TD>");
	    cnt++;
	    if (cnt == 4) {
		cnt = 0;
		System.out.println("</TR>");
	    }
	}
	System.out.println("</TABLE>");
    }


    public static String RGBtoHex(Color color) {

	int r = color.getRed();
	int g = color.getGreen();
	int b = color.getBlue();

	String red,green,blue;
	
	red=r<20?"0"+Integer.toHexString(r):Integer.toHexString(r);
	green=g<20?"0"+Integer.toHexString(g):Integer.toHexString(g);
	blue=b<20?"0"+Integer.toHexString(b):Integer.toHexString(b);
	
	String webcolor="#"+red+green+blue+"";

	return webcolor.toUpperCase();
    }

    static {
	putColor("black",new GuessColor(Color.black));
	putColor("blue",new GuessColor(Color.blue));
	putColor("cyan",new GuessColor(Color.cyan));
	putColor("darkGray",new GuessColor(Color.darkGray));
	putColor("gray",new GuessColor(Color.gray));
	putColor("green",new GuessColor(Color.green));
	putColor("lightGray",new GuessColor(Color.lightGray));
	putColor("magenta",new GuessColor(Color.magenta));
	putColor("orange",new GuessColor(Color.orange));
	putColor("pink",new GuessColor(Color.pink));
	putColor("red",new GuessColor(Color.red));
	putColor("white",new GuessColor(Color.white));
	putColor("yellow",new GuessColor(Color.yellow));
	putColor("GreenYellow",new GuessColor(236,252,151));
	putColor("Yellow",new GuessColor(255,255,0));
	putColor("Goldenrod",new GuessColor(253,241,112));
	putColor("Dandelion",new GuessColor(252,216,112));
	putColor("Apricot",new GuessColor(251,213,184));
	putColor("Peach",new GuessColor(252,186,148));
	putColor("Melon",new GuessColor(252,192,187));
	putColor("YellowOrange",new GuessColor(253,198,7));
	putColor("Orange",new GuessColor(255,200,0));
	putColor("BurntOrange",new GuessColor(253,184,7));
	putColor("Bittersweet",new GuessColor(223,45,2));
	putColor("RedOrange",new GuessColor(252,130,101));
	putColor("Mahogany",new GuessColor(209,0,2));
	putColor("Maroon",new GuessColor(213,0,31));
	putColor("BrickRed",new GuessColor(218,1,2));
	putColor("Red",new GuessColor(255,0,0));
	putColor("OrangeRed",new GuessColor(251,11,187));
	putColor("RubineRed",new GuessColor(250,14,239));
	putColor("WildStrawberry",new GuessColor(251,59,203));
	putColor("Salmon",new GuessColor(252,181,205));
	putColor("CarnationPink",new GuessColor(250,163,253));
	putColor("Magenta",new GuessColor(255,0,255));
	putColor("VioletRed",new GuessColor(250,121,253));
	putColor("Rhodamine",new GuessColor(250,118,253));
	putColor("Mulberry",new GuessColor(204,82,251));
	putColor("RedViolet",new GuessColor(197,13,211));
	putColor("Fuchsia",new GuessColor(174,47,244));
	putColor("Lavender",new GuessColor(251,190,254));
	putColor("Thistle",new GuessColor(237,170,253));
	putColor("Orchid",new GuessColor(210,161,253));
	putColor("DarkOrchid",new GuessColor(199,124,230));
	putColor("Purple",new GuessColor(191,106,253));
	putColor("Plum",new GuessColor(182,18,253));
	putColor("Violet",new GuessColor(119,99,253));
	putColor("RoyalPurple",new GuessColor(130,91,253));
	putColor("BlueViolet",new GuessColor(79,66,249));
	putColor("Periwinkle",new GuessColor(170,178,254));
	putColor("CadetBlue",new GuessColor(161,174,226));
	putColor("CornflowerBlue",new GuessColor(156,238,253));
	putColor("MidnightBlue",new GuessColor(0,176,198));
	putColor("NavyBlue",new GuessColor(57,180,254));
	putColor("RoyalBlue",new GuessColor(0,187,253));
	putColor("Blue",new GuessColor(0,0,255));
	putColor("Cerulean",new GuessColor(61,240,253));
	putColor("Cyan",new GuessColor(0,255,255));
	putColor("ProcessBlue",new GuessColor(46,253,253));
	putColor("SkyBlue",new GuessColor(162,253,240));
	putColor("Turquoise",new GuessColor(104,253,230));
	putColor("TealBlue",new GuessColor(95,250,208));
	putColor("Aquamarine",new GuessColor(115,253,217));
	putColor("BlueGreen",new GuessColor(106,253,212));
	putColor("Emerald",new GuessColor(0,253,187));
	putColor("JungleGreen",new GuessColor(23,253,184));
	putColor("SeaGreen",new GuessColor(150,253,187));
	putColor("Green",new GuessColor(0,255,0));
	putColor("ForestGreen",new GuessColor(39,239,34));
	putColor("PineGreen",new GuessColor(8,222,111));
	putColor("LimeGreen",new GuessColor(188,252,9));
	putColor("YellowGreen",new GuessColor(196,252,139));
	putColor("SpringGreen",new GuessColor(221,252,134));
	putColor("OliveGreen",new GuessColor(13,201,7));
	putColor("RawSienna",new GuessColor(194,1,1));
	putColor("Sepia",new GuessColor(147,0,1));
	putColor("Brown",new GuessColor(168,0,1));
	putColor("Tan",new GuessColor(236,198,176));
	putColor("Gray",new GuessColor(128,128,128));
	putColor("Black",new GuessColor(0,0,0));
	putColor("White",new GuessColor(255,255,255));
	putColor("LightYellow",new GuessColor(252,252,202));
	putColor("LightCyan",new GuessColor(227,253,254));
	putColor("LightMagenta",new GuessColor(251,229,254));
	putColor("LightPurple",new GuessColor(227,229,253));
	putColor("LightGreen",new GuessColor(228,253,216));
	putColor("LightOrange",new GuessColor(252,229,216));
	putColor("Canary",new GuessColor(251,252,187));
	putColor("LFadedGreen",new GuessColor(241,253,230));
	putColor("Pink",new GuessColor(255,175,175));
	putColor("LSkyBlue",new GuessColor(234,247,254));   
    }

    public static void putColor(String name,GuessColor clr) {
	name = name.toLowerCase();
	colors.put(name,clr);
	clr.setName(name);
    }
    
    public static Color getColor(String name, Color def) {
	if (name == null) {
	    //System.out.println("illegal color: " + name);
	    //Thread.dumpStack();
	    return(def);
	}
	name = name.toLowerCase();
	Color nc = (Color)colors.get(name);
	if (nc == null) {
	    StringTokenizer st = new StringTokenizer(name,", ");
	    //System.out.println(st.countTokens();
	   
	    if ((st.countTokens() < 3) || (st.countTokens() > 4)){
		//System.out.println("illegal color");
		nc = def;
	    } else {
		try {
		    int r = Integer.parseInt(st.nextToken());
		    int g = Integer.parseInt(st.nextToken());
		    int b = Integer.parseInt(st.nextToken());
		    if (st.countTokens() == 0) {
			nc = new GuessColor(r,g,b);
		    } else {
			int a = Integer.parseInt(st.nextToken());
			nc = new GuessColor(r,g,b,a);
		    }
		    colors.put(name,nc);
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		    StatusBar.setErrorStatus("Invalid color, using default");
		    nc = def;
		}
	    }
	}
	return(nc);
    }
    
    public static String toString(Color c) {
	if (c instanceof GuessColor) {
	    return(c.toString());
	} else {
	    return(c.getRed()+","+c.getBlue()+","+
		   c.getGreen()+","+c.getAlpha());
	}
    }

    private static Random r = new Random();

    /**
     * @pyexport
     */
    public static String randomColor() {
	return(r.nextInt(255)+","+
	       r.nextInt(255)+","+
	       r.nextInt(255));
    }

    /**
     * @pyexport
     */
    public static String randomColor(int alpha) {
	return(r.nextInt(255)+","+
	       r.nextInt(255)+","+
	       r.nextInt(255)+","+
	       alpha);
    }

    /**
     * @pyexport
     */
    public static String averageColor(String c1,
				      String c2) {
	return(averageColor(getColor(c1,Color.black),
			    getColor(c2,Color.white)));
    }

    public static String averageColor(Color c1,
				      Color c2) {

	double avgR = c1.getRed() + c2.getRed();
	double avgB = c1.getBlue() + c2.getBlue();
	double avgG = c1.getGreen() + c2.getGreen();
	double avgA = c1.getAlpha() + c2.getAlpha();
	return((int)(avgR/2)+","+
	       (int)(avgG/2)+","+
	       (int)(avgB/2)+","+
	       (int)(avgA/2));
    }

    /**
     * @pyexport
     */
    public static ArrayList generateColors(String startC, 
					   String endC, 
					   int inBetween) {
	return(generateColors(getColor(startC,Color.red),
			      getColor(endC,Color.blue),
			      inBetween));
    }
    
    public static ArrayList generateColors(Color startColor,
					   Color endColor,
					   int inBetween) {

	ArrayList al = new ArrayList(inBetween);

	double red = 0;
	double green = 0;
	double blue = 0;
	double alpha = 0;

	for (int i = 0 ; i <inBetween ; i++) {
	    double percent = (double)i / (double)(inBetween - 1);
	    red = startColor.getRed() + 
		(endColor.getRed() - startColor.getRed()) * percent;
	    green = startColor.getGreen() + 
		(endColor.getGreen() - startColor.getGreen()) 
		* percent;
	    blue = startColor.getBlue() + 
		(endColor.getBlue() - startColor.getBlue()) * percent;
	    alpha = startColor.getAlpha() + 
		(endColor.getAlpha() - startColor.getAlpha()) 
		* percent;
	    red = Math.max(0,Math.min(red,255));
	    green = Math.max(0,Math.min(green,255));
	    blue = Math.max(0,Math.min(blue,255));
	    alpha = Math.max(0,Math.min(alpha,255));		    
	    al.add((int)red+","+(int)green+","+
		   (int)blue+","+(int)alpha);
	}
	return(al);
    }

    /**
     * @pyexport
     */
    public static ArrayList generateColors(String startC, 
					   String middleC,
					   String endC,
					   int inBetween) {
	return(generateColors(getColor(startC,Color.red),
			      getColor(middleC,Color.green),
			      getColor(endC,Color.blue),
			      inBetween));
    }

    public static ArrayList generateColors(Color sColor,
					   Color mColor,
					   Color eColor,
					   int inBetween) {

	ArrayList al = new ArrayList(inBetween);

	double red = 0;
	double green = 0;
	double blue = 0;
	double alpha = 0;

	int middle = (int)(inBetween / 2);

	Color startColor = sColor;
	Color endColor = mColor;

	for (int i = 0 ; i < middle ; i++) {
	    double percent = (double)i / (double)(middle - 1);
	    red = startColor.getRed() + 
		(endColor.getRed() - startColor.getRed()) * percent;
	    green = startColor.getGreen() + 
		(endColor.getGreen() - startColor.getGreen()) 
		* percent;
	    blue = startColor.getBlue() + 
		(endColor.getBlue() - startColor.getBlue()) * percent;
	    alpha = startColor.getAlpha() + 
		(endColor.getAlpha() - startColor.getAlpha()) 
		* percent;
	    red = Math.max(0,Math.min(red,255));
	    green = Math.max(0,Math.min(green,255));
	    blue = Math.max(0,Math.min(blue,255));
	    alpha = Math.max(0,Math.min(alpha,255));		    
	    al.add((int)red+","+(int)green+","+
		   (int)blue+","+(int)alpha);
	}

	startColor = mColor;
	endColor = eColor;

	for (int i = middle ; i <inBetween ; i++) {
	    double percent = (double)i / (double)(inBetween - 1);
	    red = startColor.getRed() + 
		(endColor.getRed() - startColor.getRed()) * percent;
	    green = startColor.getGreen() + 
		(endColor.getGreen() - startColor.getGreen()) 
		* percent;
	    blue = startColor.getBlue() + 
		(endColor.getBlue() - startColor.getBlue()) * percent;
	    alpha = startColor.getAlpha() + 
		(endColor.getAlpha() - startColor.getAlpha()) 
		* percent;
	    red = Math.max(0,Math.min(red,255));
	    green = Math.max(0,Math.min(green,255));
	    blue = Math.max(0,Math.min(blue,255));
	    alpha = Math.max(0,Math.min(alpha,255));		    
	    al.add((int)red+","+(int)green+","+
		   (int)blue+","+(int)alpha);
	}

	return(al);
    }
}
