package com.hp.hpl.guess.layout;

import com.hp.hpl.guess.*;
import java.util.*;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;


/**
 * @author Hacked by Eytan Adar for Guess classes
 */
public class RandomGraphLayout extends AbstractLayout {

    // number of pixels to shrink radius by. Java draws object from top
    // left hand corner and this allows objects drawn on the far right to
    // be visible.
    private int pad = 4;
    
    private Set nodeList;
    private int width, height;
    private boolean update = true;
    private HashMap locations = new HashMap();

    public RandomGraphLayout(Graph g, int width, int height) {
	super(g);
	this.width = width;
	this.height = height;
	this.nodeList = g.getNodes();
    }
    
    /**
     * Sets the number of pixels to shrink radius by. Java draws
     * object from top left hand corner and this allows objects drawn
     * on the far right to be visible.
     */
    public void setPad(int p) {
	pad = p;
    }
    
    public void advancePositions() {
	if (done)
	    return;

	Random r = new Random();
	if (update) {
	    Iterator it = nodeList.iterator();
	    while(it.hasNext()) {
		Node node = (Node)it.next();
		locations.put(node,
			      new Coordinates((double)r.nextInt(width - pad),
					      (double)r.nextInt(height)));
	    }
	}
	
	done = true;
    }
  
    /**
     * Gets the height of the area on which to layout the graph.
     */
    public int getHeight() {
	return height;
    }
    
    /**
     * Gets the width of the area on which to layout the graph.
     */
    public int getWidth() {
	return width;
    }
    
    public void setUpdate(boolean doUpdate) {
	update = doUpdate;
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
}
