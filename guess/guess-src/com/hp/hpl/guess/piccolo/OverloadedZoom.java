package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.event.PZoomEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import java.awt.event.*;

public class OverloadedZoom extends PZoomEventHandler {

    public void processEvent(PInputEvent event, int type) {
	System.out.println(event.isControlDown() + " " + event.isAltDown());
	if ((type == MouseEvent.MOUSE_DRAGGED) && (event.isAltDown())) {
	    System.out.println("returning...");
	    return;
	} else {
	    System.out.println("doing...");
	    super.processEvent(event,type);
	}
    }
}
