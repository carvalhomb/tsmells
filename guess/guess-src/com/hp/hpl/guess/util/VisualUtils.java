package com.hp.hpl.guess.util;

import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Color;

public abstract class VisualUtils {

    private static Random r = new Random();

    public static void colorize(Graph g, Field column) {

	// get a collection of collections grouped by column
	Collection c = g.groupBy(column);

	Iterator it = c.iterator();
	while(it.hasNext()) {

	    // get every sub-collection
	    Collection c2 = (Collection)it.next();
	    Iterator it2 = c2.iterator();

	    // pick a random color
	    String col = Colors.randomColor();

	    // for each graphelement set the color
	    while(it2.hasNext()) {
		GraphElement ge = (GraphElement)it2.next();
		ge.__setattr__("color",col);
	    }
	}
    }

    public static void resizeRandom(Graph g, Field column, 
				    double min, double max) {

	// get a collection of collections grouped by column
	Collection c = g.groupBy(column);

	Iterator it = c.iterator();
	while(it.hasNext()) {

	    // get every sub-collection
	    Collection c2 = (Collection)it.next();
	    Iterator it2 = c2.iterator();

	    // pick a random color
	    double size = min + r.nextDouble() * (max - min);

	    // for each graphelement set the color
	    while(it2.hasNext()) {
		GraphElement ge = (GraphElement)it2.next();
		if (column.getType() == Field.NODE) {
		    ge.__setattr__("size",new Double(size));
		} else if (column.getType() == Field.EDGE) {
		    ge.__setattr__("width",new Double(size));
		}
	    }
	}
    }

    public static void resizeLinear(Graph g, Field column, 
				    double min, double max) {

	// get a collection of collections grouped by column
	Collection c = g.groupAndSortBy(column);

	double increment = (max - min) / c.size();

	Iterator it = c.iterator();
	int i = 0;
	while(it.hasNext()) {

	    // get every sub-collection
	    Collection c2 = (Collection)it.next();
	    Iterator it2 = c2.iterator();

	    // increment the size based on loop location
	    double size = min + increment * i;
	    i++;

	    // for each graphelement set the color
	    while(it2.hasNext()) {
		GraphElement ge = (GraphElement)it2.next();
		if (column.getType() == Field.NODE) {
		    ge.__setattr__("size",new Double(size));
		} else if (column.getType() == Field.EDGE) {
		    ge.__setattr__("width",new Double(size));
		}
	    }
	}
    }

    public static void colorize(Graph g, Field column, 
				Color start, Color end) {
	
	// get a collection of collections grouped by column
	Collection c = g.groupAndSortBy(column);
	
	if (c.size() <= 0) {
	    System.out.println("No groupings");
	    return;
	}

	ArrayList al = Colors.generateColors(start,end,c.size());

	Iterator it = c.iterator();

	int i = 0;

	while(it.hasNext()) {
	    
	    // get every sub-collection
	    Collection c2 = (Collection)it.next();
	    Iterator it2 = c2.iterator();
	    
	    // pick a random color
	    String col = (String)al.get(i);
	    i++;

	    // for each graphelement set the color
	    while(it2.hasNext()) {
		GraphElement ge = (GraphElement)it2.next();
		ge.__setattr__("color",col);
	    }
	}
    }

    public static void colorize(Graph g, Field column, 
				Color start, Color middle, 
				Color end) {
	
	// get a collection of collections grouped by column
	Collection c = g.groupAndSortBy(column);
	
	if (c.size() <= 0) {
	    System.out.println("No groupings");
	    return;
	}

	ArrayList al = Colors.generateColors(start,middle,end,c.size());

	Iterator it = c.iterator();

	int i = 0;

	while(it.hasNext()) {
	    
	    // get every sub-collection
	    Collection c2 = (Collection)it.next();
	    Iterator it2 = c2.iterator();
	    
	    // pick a random color
	    String col = (String)al.get(i);
	    i++;

	    // for each graphelement set the color
	    while(it2.hasNext()) {
		GraphElement ge = (GraphElement)it2.next();
		ge.__setattr__("color",col);
	    }
	}
    }
}
