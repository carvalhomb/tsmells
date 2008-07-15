package com.hp.hpl.guess.layout;

import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;

import com.hp.hpl.guess.*;

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
public class Spring extends AbstractLayout {

    public static double SPRING_NATURAL_LENGTH = 30;
    public static double SPRING_STIFFNESS = 150;
    public static double GRAVITY_REPULSION = 2;

    private int width = 10000;
    private int height = 10000;
    private int loops = 10000;
    private int pad = 20;
    
    private Graph g = null;

    private int iters = 0;

    private HashMap locations = new HashMap();

    private HashMap dxdys = new HashMap();

    public Spring(Graph g) {
	super(g);
	this.g = g;
	Iterator it = g.getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    locations.put(n,new Coordinates(n.getX(),n.getY()));
	    dxdys.put(n,new Coordinates(0,0));
	}
    }

    public void advancePositions() {

	if (done)
	    return;

	if (iters >= loops) {
	    done = true;
	    return;
	}
	
	//Viz.log("starting 'relax'");
	long start = System.currentTimeMillis();

	Iterator edges = g.getEdges().iterator();

	Node node, node2;
	Edge e;

	//Viz.getViz().setStatus("computing edge distances");
	while (edges.hasNext()) {
	    e = (Edge)edges.next();
	    
	    node = e.getNode1();
	    node2 = e.getNode2();
	    
	    if (node == node2)
		continue;

	    // get distance
	    Coordinates c1 = (Coordinates)locations.get(node);
	    Coordinates c2 = (Coordinates)locations.get(node2);

	    double len = Point2D.distance(c1.getX(), c1.getY(),
					  c2.getX(), c2.getY());

	    double vx = c2.getX() - c1.getX();
	    double vy = c2.getY() - c1.getY();
	    
	    
	    // figure out how much to move
	    double f = (len - SPRING_NATURAL_LENGTH) / SPRING_STIFFNESS;
	    //Viz.log("F:" + f);
	    double dx = f * vx;
	    double dy = f * vy;
	    
	    // set the dx/dys
	    Coordinates dxdy1 = (Coordinates)dxdys.get(node);
	    dxdy1.setX(dxdy1.getX() + dx);
	    dxdy1.setY(dxdy1.getY() + dy);

	    dxdy1 = (Coordinates)dxdys.get(node2);
	    dxdy1.setX(dxdy1.getX() - dx);
	    dxdy1.setY(dxdy1.getY() - dy);
	}

	Iterator nodes = g.getNodes().iterator();

	while (nodes.hasNext()) {
	    node = (Node)nodes.next();

	    double dx = 0;
	    double dy = 0;

	    Coordinates l1 = (Coordinates)locations.get(node);
	    
	    Iterator nodes2 = g.getNodes().iterator();
		
	    while (nodes2.hasNext()) {

		node2 = (Node)nodes2.next();

		if (node == node2)
		    continue;
		
		// two nodes to compare

		Coordinates l2 = (Coordinates)locations.get(node2);
		double vx = l1.getX() - l2.getX();
		double vy = l1.getY() - l2.getY();

		double len_sq = vx * vx + vy * vy;
		
		if (len_sq == 0) {
		    // "jiggle" if they're on top of each other
		    dx += Math.random();
		    dy += Math.random();
		} else if (len_sq < 100*100) {
		    // too far away, move it together
		    dx += GRAVITY_REPULSION * vx / len_sq;
		    dy += GRAVITY_REPULSION * vy / len_sq;
		}
	    }
	

	    // also should repel from the walls a little
	    
	    if (l1.getY() > 0)
		dy += GRAVITY_REPULSION / l1.getY();
	    else 
		dy += GRAVITY_REPULSION;
	    if (l1.getY() < height)
		dy += - GRAVITY_REPULSION / (height - l1.getY());
	    else
		dy += - GRAVITY_REPULSION;
	    if (l1.getX() > 0)
		dx += GRAVITY_REPULSION / l1.getX();
	    else 
		dx += GRAVITY_REPULSION;
	    if (l1.getX() < width)
		dx += - GRAVITY_REPULSION / (width - l1.getX());
	    else 
		dx += - GRAVITY_REPULSION;
		
	    double dlen_sq = dx * dx + dy * dy;
	    if (dlen_sq > 0) {
		double dlen = Math.sqrt(dlen_sq) / 10;
		Coordinates dxdy1 = (Coordinates)dxdys.get(node);
		dxdy1.setX(dxdy1.getX() + dx / dlen);
		dxdy1.setY(dxdy1.getY() + dy / dlen);
	    }		
	}
	    
	// now, we're going to update points
	    
	nodes = g.getNodes().iterator();

	while (nodes.hasNext()) {
	    node = (Node)nodes.next();

	    Coordinates l1 = (Coordinates)locations.get(node);
	    Coordinates dxdy1 = (Coordinates)dxdys.get(node);
	    
	    if (dxdy1.getX() != 0 || dxdy1.getY() != 0) {
		double newX = l1.getX() + 
		    Math.max(-5, Math.min(5,dxdy1.getX())) + 1;
		double newY = l1.getY() + 
		    Math.max(-5, Math.min(5,dxdy1.getY())) + 1;
		//  	if (newX < 0) {
		//  		    newX = 0;
		//  		}
		//  		if (newX > width) {
		//  		    newX = width;
		//  		} 
		//  		if (newY < 0) {
		//  		    newY = 0;
		//  		} 
		//  		if (newY > height) {
		//  		    newY = height;
		//  		}
		l1.setX(newX);
		l1.setY(newY);
		//node.move(newX,newY);
	    }
	    dxdy1.setX(dxdy1.getX() / 2);
	    dxdy1.setY(dxdy1.getY() / 2);
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
	return(true);
    }    
}
