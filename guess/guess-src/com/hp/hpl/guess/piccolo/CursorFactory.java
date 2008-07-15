package com.hp.hpl.guess.piccolo;

import java.awt.*;
import java.net.URL;

public class CursorFactory {

    private CursorFactory() {
    }

    private static GFrame frame = null;

    public static void init(GFrame f) {
	frame =f;
    }

    public final static int STANDARD = 1;
    public final static int ZOOM = 2;
    public final static int MOVE = 3;

    public static void setCursor(int ctype) {
	if (frame != null) {
	    if (ctype == ZOOM) {
		frame.setCursor(zoomC);
	    } else if (ctype == MOVE) {
		frame.setCursor(moveC);
	    } else {
		frame.setCursor(normalC);
	    }
	}
    }

    public static Cursor getCursor(int id) {
	if (id == STANDARD) {
	    return(normalC);
	} else if (id == ZOOM) {
	    return(zoomC);
	} else if (id == MOVE) {
	    return(moveC);
	} 
	return(null);
    }

    private static Cursor normalC = new Cursor(Cursor.DEFAULT_CURSOR);
    
    private static Cursor moveC = new Cursor(Cursor.MOVE_CURSOR);

    private static Cursor zoomC = new Cursor(Cursor.MOVE_CURSOR);

    static {
	Toolkit tk = Toolkit.getDefaultToolkit();
 	Dimension d = tk.getBestCursorSize(32, 32);
	int colors = tk.getMaximumCursorColors();
 	if (!d.equals(new Dimension(0, 0)) && (colors != 0)) {
	    CursorFactory c = new CursorFactory();
	    URL uImage = c.getClass().getResource("/images/zoom.gif");
 	    Image image = tk.getImage(uImage);
 	    if (image != null) { 
		try { 
		    // 		    System.out.println("loaded");
		    zoomC = tk.createCustomCursor(image, new Point(16, 16),
 						  "crosshair cursor");
 		} catch (Exception e) {}
 	    }
	    uImage = c.getClass().getResource("/images/move.gif");
	    image = tk.getImage(uImage);
 	    if (image != null) { 
		try { 
		    // 		    System.out.println("loaded");
		    moveC = tk.createCustomCursor(image, new Point(16, 16),
 						  "crosshair cursor");
 		} catch (Exception e) {}
 	    }
 	}
    }

}
