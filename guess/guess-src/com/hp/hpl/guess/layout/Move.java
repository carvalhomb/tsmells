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
public class Move extends AbstractLayout {

    private double xmove = 0;
    private double ymove = 0;
    private int pad = 0;

    Graph g = null;

    HashMap locations = new HashMap();

    public Move(Graph g, double xmove, double ymove) {
	super(g);
	this.xmove = xmove;
	this.ymove = ymove;
	this.g = g;
    }
    
    public void advancePositions() {
	if (done)
	    return;

	movePositionsInternal(g.getNodes(),xmove,ymove,0);
	
	done = true;
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
     * Moves the x and y coordinates of each node so that the network
     * will maximally fill the display. Will result in some distortion.
     * Called internally if rescale is set to true, will rescale
     * smoothly if animateTransitions is true.
     *
     * @param nodes the nodes to rescale.
     */
    public static void movePositions(Set nodes, int newx,
				     int newy, int pad) {
	
	int nNodes = nodes.size();

	Object[] nlist = nodes.toArray();

	if (nNodes == 1) {
	    Node move1 = (Node)nlist[0];
	    move1.setLocation(newx+pad,newy+pad);
	    return;
	}
	
	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
	double xMin = Double.MAX_VALUE;
	double yMin = Double.MAX_VALUE;	

	for (int i = 0; i<nNodes; i++) {
	    Node workNode = (Node)nlist[i];
	    xPos[i] = (double)workNode.getX();
	    yPos[i] = (double)workNode.getY();
	    xMin = Math.min(xMin, xPos[i]);
	    yMin = Math.min(yMin, yPos[i]);
	}

	//System.out.println(" \t" +xMin + " " + yMin);
	// ok, so now we know the current top left corner of the 
	// encapsulating rectangle... we just move it
	//System.out.println("\t moving to: " +newx + " " + newy);
	double xTrans = newx - xMin + pad;
	double yTrans = newy - yMin + pad;
	//System.out.println("\t trans: " +xTrans + " " + yTrans);
	//rescale coords of nodes to fit inside frame
	for (int i = 0; i < nNodes; i++) {
	    xPos[i] = xPos[i] + xTrans; 
	    yPos[i] = yPos[i] + yTrans;
	    ((Node)nlist[i]).setLocation(xPos[i],yPos[i]);
	}
    }

    /**
     * Moves the x and y coordinates of each node so that the network
     * will maximally fill the display. Will result in some distortion.
     * Called internally if rescale is set to true, will rescale
     * smoothly if animateTransitions is true.
     *
     * @param nodes the nodes to rescale.
     */
    public void movePositionsInternal(Set nodes, double newx,
				      double newy, int pad) {
	
	int nNodes = nodes.size();

	Object[] nlist = nodes.toArray();

	if (nNodes == 1) {
	    Node move1 = (Node)nlist[0];
	    locations.put(move1,
			  new Coordinates(newx+pad,
					  newy+pad));
	    return;
	}
	
	double[] xPos = new double[nNodes];
	double[] yPos = new double[nNodes];
	double xMin = Double.MAX_VALUE;
	double yMin = Double.MAX_VALUE;	

	for (int i = 0; i<nNodes; i++) {
	    Node workNode = (Node)nlist[i];
	    xPos[i] = (double)workNode.getX();
	    yPos[i] = (double)workNode.getY();
	    xMin = Math.min(xMin, xPos[i]);
	    yMin = Math.min(yMin, yPos[i]);
	}

	//System.out.println(" \t" +xMin + " " + yMin);
	// ok, so now we know the current top left corner of the 
	// encapsulating rectangle... we just move it
	//System.out.println("\t moving to: " +newx + " " + newy);
	double xTrans = newx - xMin + pad;
	double yTrans = newy - yMin + pad;
	//System.out.println("\t trans: " +xTrans + " " + yTrans);
	//rescale coords of nodes to fit inside frame
	for (int i = 0; i < nNodes; i++) {
	    xPos[i] = xPos[i] + xTrans; 
	    yPos[i] = yPos[i] + yTrans;
	    locations.put(nlist[i],
			  new Coordinates(xPos[i],
					  yPos[i]));
	}
    }
}
