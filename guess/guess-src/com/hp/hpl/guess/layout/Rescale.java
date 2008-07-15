package com.hp.hpl.guess.layout;

import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import com.hp.hpl.guess.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

/**
 * seperate layout thread
 *
 * @author Eytan Adar
 * @author Joshua Tyler
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
 */
public class Rescale extends AbstractLayout {

    private int width = 1000;
    private int height = 1000;
    private int pad = 20;

    private final boolean SQUARE = true;
    private final boolean RATIO = false;

    private boolean ltype = SQUARE;

    Graph g = null;

    HashMap locations = new HashMap();

    private double xpercent = .5;
    private double ypercent = .5;

    public Rescale(Graph g, int width, int height) {
	super(g);
	this.width = width;
	this.height = height;
	this.g = g;
    }

    public Rescale(Graph g, double xpercent) {
	super(g);
	this.g = g;
	ltype = RATIO;
	this.xpercent = xpercent;
	this.ypercent = xpercent;
    }

    public Rescale(Graph g, double xpercent, double ypercent) {
	super(g);
	this.g = g;
	ltype = RATIO;
	this.xpercent = xpercent;
	this.ypercent = ypercent;
    }

    public void advancePositions() {
	if (done)
	    return;

	if (ltype == SQUARE) {
	    rescalePositions(g.getNodes(),width,height,0);
	} else {
	    rescalePositions(g.getNodes(),xpercent,ypercent,0);
	}
	
	done = true;
    }

    /**
     * Rescales the x and y coordinates of each node so that the network
     * will maximally fill the display. Will result in some distortion.
     * Called internally if rescale is set to true, will rescale
     * smoothly if animateTransitions is true.
     *
     * @param nodes the nodes to rescale.
     */
    public void rescalePositions(Set nodes, int width,
				 int height, int pad) {
	
	int nNodes = nodes.size();
	if (nNodes <= 1) {
	    return;
	}

	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
	Node[] nlist = new Node[nNodes];

	double xMax = Double.MIN_VALUE;
	double yMax = Double.MIN_VALUE;
	double xMin = Double.MAX_VALUE;
	double yMin = Double.MAX_VALUE;
	
	Iterator it = nodes.iterator();
	int i = 0;
	while(it.hasNext()) {
	    nlist[i] = (Node)it.next();
	    xPos[i] = (double)nlist[i].getX();
	    yPos[i] = (double)nlist[i].getY();
	    xMax = Math.max(xMax, xPos[i]);
	    yMax = Math.max(yMax, yPos[i]);
	    xMin = Math.min(xMin, xPos[i]);
	    yMin = Math.min(yMin, yPos[i]);
	    i++;
	}

	//rescale coords of nodes to fit inside frame, move to 
	//position
	for (i = 0; i < nNodes; i++) {
	    xPos[i] = ((xPos[i] - xMin) / (xMax - xMin)) * (width - pad);
	    yPos[i] = ((yPos[i] - yMin) / (yMax - yMin)) * (height - pad);
	    locations.put(nlist[i],
			  new Coordinates(xPos[i],
					  yPos[i]));
	}
    }

    /**
     * Rescales the x and y coordinates of each node by percent.
     *
     * @param nodes the nodes to rescale.
     */
    public void rescalePositions(Set nodes, double xpercent, 
				 int pad) {
	rescalePositions(nodes,xpercent,xpercent,pad);

    }

    /**
     * Rescales the x and y coordinates of each node by percent.
     *
     * @param nodes the nodes to rescale.
     */
    public void rescalePositions(Set nodes, double xpercent, 
				 double ypercent, int pad) {
	
	int nNodes = nodes.size();
	if (nNodes <= 1) {
	    return;
	}

	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
	Node[] nlist = new Node[nNodes];

	double xMax = Double.MIN_VALUE;
	double yMax = Double.MIN_VALUE;
	double xMin = Double.MAX_VALUE;
	double yMin = Double.MAX_VALUE;
	
	Iterator it = nodes.iterator();
	int i = 0;
	while(it.hasNext()) {
	    nlist[i] = (Node)it.next();
	    xPos[i] = (double)nlist[i].getX();
	    yPos[i] = (double)nlist[i].getY();
	    xMax = Math.max(xMax, xPos[i]);
	    yMax = Math.max(yMax, yPos[i]);
	    xMin = Math.min(xMin, xPos[i]);
	    yMin = Math.min(yMin, yPos[i]);
	    i++;
	}

	double width = (xMax - xMin)*xpercent;
	double height = (yMax - yMin)*ypercent;

	if ((width == 0) || (height == 0)) {
	    throw(new Error("can't rescale, width or height = 0"));
	}

	//rescale coords of nodes to fit inside frame, move to 
	//position
	for (i = 0; i < nNodes; i++) {
	    xPos[i] = ((xPos[i] - xMin) / (xMax - xMin)) * (width - pad);
	    yPos[i] = ((yPos[i] - yMin) / (yMax - yMin)) * (height - pad);

	    locations.put(nlist[i],
			  new Coordinates(xPos[i],
					  yPos[i]));
	}
    }

    public double getX(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getX());
    }

    public double getY(Vertex n) {
	Coordinates d2d = (Coordinates)locations.get(n);
	return(d2d.getY());
    }

    public Coordinates getCoordinates(Node v) {
	return((Coordinates)locations.get(v));
    }

    public boolean done = false;

    public boolean incrementsAreDone() {
	return(done);
    }

    public void initialize_local_vertex(edu.uci.ics.jung.graph.Vertex v) {
    }

    public void initialize_local() {
    }

    public boolean isIncremental() {
	return(false);
    }    

    /**
     * Rescales the x and y coordinates of each node by percent.
     *
     * @param nodes the nodes to rescale.
     */
    public static void rescalePositions(Set nodes, 
					double percent, 
					int pad,
					Map locations) {

	int nNodes = nodes.size();
	if (nNodes <= 1) {
	    return;
	}

	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
	Node[] nlist = new Node[nNodes];

	double xMax = Double.MIN_VALUE;
	double yMax = Double.MIN_VALUE;
	double xMin = Double.MAX_VALUE;
	double yMin = Double.MAX_VALUE;
	
	Iterator it = nodes.iterator();
	int i = 0;
	while(it.hasNext()) {
	    nlist[i] = (Node)it.next();
	    Coordinates c = (Coordinates)locations.get(nlist[i]);
	    xPos[i] = c.getX();
	    yPos[i] = c.getY();
	    xMax = Math.max(xMax, xPos[i]);
	    yMax = Math.max(yMax, yPos[i]);
	    xMin = Math.min(xMin, xPos[i]);
	    yMin = Math.min(yMin, yPos[i]);
	    i++;
	}

	double width = (xMax - xMin)*percent;
	double height = (yMax - yMin)*percent;

	if ((width == 0) || (height == 0)) {
	    throw(new Error("can't rescale, width or height = 0"));
	}

	//rescale coords of nodes to fit inside frame, move to 
	//position
	for (i = 0; i < nNodes; i++) {
	    xPos[i] = ((xPos[i] - xMin) / (xMax - xMin)) * (width - pad);
	    yPos[i] = ((yPos[i] - yMin) / (yMax - yMin)) * (height - pad);

	    locations.put(nlist[i],
			  new Coordinates(xPos[i],
					  yPos[i]));
	}
    }

    public static void rescalePositions(Set nodes, int width,
					int height, Map locations) {
	
	int pad = 0;

	int nNodes = nodes.size();
	if (nNodes <= 1) {
	    return;
	}

	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
	Node[] nlist = new Node[nNodes];

	double xMax = Double.MIN_VALUE;
	double yMax = Double.MIN_VALUE;
	double xMin = Double.MAX_VALUE;
	double yMin = Double.MAX_VALUE;
	
	Iterator it = nodes.iterator();
	int i = 0;
	while(it.hasNext()) {
	    nlist[i] = (Node)it.next();
	    Coordinates c = (Coordinates)locations.get(nlist[i]);
	    xPos[i] = c.getX();
	    yPos[i] = c.getY();
	    xMax = Math.max(xMax, xPos[i]);
	    yMax = Math.max(yMax, yPos[i]);
	    xMin = Math.min(xMin, xPos[i]);
	    yMin = Math.min(yMin, yPos[i]);
	    i++;
	}

	//rescale coords of nodes to fit inside frame, move to 
	//position
	for (i = 0; i < nNodes; i++) {
	    xPos[i] = ((xPos[i] - xMin) / (xMax - xMin)) * (width - pad);
	    yPos[i] = ((yPos[i] - yMin) / (yMax - yMin)) * (height - pad);
	    locations.put(nlist[i],
			  new Coordinates(xPos[i],
					  yPos[i]));
	}
    }
}
