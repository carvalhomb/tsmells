package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.activities.PTransformActivity;

public class GPCamera extends PCamera {

    public void fullPaint(PPaintContext paintContext) {
	//System.out.println("cfp");
	super.fullPaint(paintContext);
    }
	
    public  PTransformActivity animateViewToCenterBounds(java.awt.geom.Rectangle2D centerBounds, boolean shouldScaleToFit, long duration) {
	System.out.println("centering: " + centerBounds);
	return(super.animateViewToCenterBounds(centerBounds,shouldScaleToFit,duration));
    }
}
