package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import com.hp.hpl.guess.*;

public class ShapeDB {

    public static ShapeDB singleton = null;

    public static ShapeDB getShapeDB() {
	if (singleton == null) {
	    singleton = new ShapeDB();
	}
	return(singleton);
    }

    public HashMap idToShape = new HashMap();

    public ShapeDB() {
	GeneralPath gp = new GeneralPath();
	gp.moveTo(5,0);
	gp.lineTo(10,5);
	gp.lineTo(5,10);
	gp.lineTo(0,5);
	gp.lineTo(5,0);
	addShape(101,gp);
    }

    public void addShape(int styleid, Shape shape) {

	idToShape.put(new Integer(styleid),shape);

	Iterator it = Guess.getGraph().getNodes().iterator();
	while(it.hasNext()) {
	    Node n = (Node)it.next();
	    Integer style = (Integer)n.__getattr__("style");
	    if (style == null) {
		continue;
	    }
	    if (style.intValue() != styleid) {
		continue;
	    }
	    // reset the value to force an update
	    n.__setattr__("style",style);  
	}
    }

    public Shape getShape(int styleid) {
	return((Shape)idToShape.get(new Integer(styleid)));
    }
}
