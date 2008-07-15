package edu.umd.cs.piccolo.activities;

import java.awt.Color;
import com.hp.hpl.guess.piccolo.*;
import com.hp.hpl.guess.ui.*;
import edu.umd.cs.piccolo.*;

public class GuessEdgeActivity extends PInterpolatingActivity {
	
    private Color sourceColor;
    private Color[] destinationColor;
    
    private boolean sourceVisible;
    private boolean[] destinationVisible;

    private double sourceWidth;
    private double[] destinationWidth;
    
    private GuessPEdge target;
    
    private int loopC = 0;

    /**
     * Create a new GuessEdgeActivity.
     * <P>
     * @param duration the length of one loop of the activity
     * @param stepRate the amount of time between steps of the activity
     * @param loopCount number of times the activity should reschedule itself
     * @param mode defines how the activity interpolates between states
     * @param aTarget the object that the activity will be applied to and where
     * the source state will be taken from.
     * @param aDestination the destination color state
     */
    public GuessEdgeActivity(long duration, 
			     long stepRate, 
			     GuessPEdge aTarget) {
	super(duration, stepRate, 1, 
	      PInterpolatingActivity.SOURCE_TO_DESTINATION);
	target = aTarget;
	setStarts();
    }	

    protected boolean isAnimation() {
	return true;
    }

    public void setStarts() {
	this.sourceWidth = target.getLineWidth();

	this.sourceColor = (Color)target.getColor();

	if (sourceColor == null) {
	    sourceColor = 
		VisFactory.getFactory().getDisplay().getDisplayBackground();
	}

	this.sourceVisible = ((PNode)target).getVisible();

    }


    public void setDestinationGone() {
	setDestination(null,false,sourceWidth);
    }

    public void setDestination(Color dC, boolean dV,
			       double dWidth) {

	this.destinationColor = new Color[]{dC};

	this.destinationWidth = new double[]{dWidth};

	this.destinationVisible = new boolean[]{dV};
	
	if (dV) {
	    // we're transitioning to visible
	    if (!sourceVisible) {
		// node is currently invisible, make it the
		// background color, set to visible
		target.setColor(Morpher.getInvisible());
		((PNode)target).setVisible(true);
		this.sourceColor = Morpher.getInvisible();
		appearLate = true;
	    }
	    this.destinationColor = new Color[]{dC};
	} else {
	    // we went to fade to invisible
	    disappearEarly = true;
	    this.destinationColor =
		new Color[]{Morpher.getInvisible()};
	}

    }
    
    public void setDestinations(Color[] dC, boolean[] dV,
				double[] dWidth) {

	this.destinationWidth = dWidth;
	this.destinationVisible = dV;
	this.destinationColor = dC;
    }

    protected void activityStarted() {
	super.activityStarted();
    }
    
    private boolean disappearEarly = false;
    private boolean appearLate = false;

    public void setRelativeTargetValue(float zeroToOne) {
	super.setRelativeTargetValue(zeroToOne);
	
	if (disappearEarly) {
	    zeroToOne = zeroToOne/Morpher.getEdgeDisappearBy();
	    if (zeroToOne > 1) 
		zeroToOne = 1;
	} else if (appearLate) {
	    zeroToOne = (zeroToOne - Morpher.getEdgeAppearAfter())/
		(1 - Morpher.getEdgeAppearAfter());
	    if (zeroToOne < 0)
		return;
	}

	//	System.out.println(sourceColor + " " + destinationColor[loopC]);

	float red = (float) (sourceColor.getRed() + (zeroToOne * (destinationColor[loopC].getRed() - sourceColor.getRed())));
	float green = (float) (sourceColor.getGreen() + (zeroToOne * (destinationColor[loopC].getGreen() - sourceColor.getGreen())));
	float blue = (float) (sourceColor.getBlue() + (zeroToOne * (destinationColor[loopC].getBlue() - sourceColor.getBlue())));
	
	float alpha = (float) (sourceColor.getAlpha() + (zeroToOne * (destinationColor[loopC].getAlpha() - sourceColor.getAlpha())));
	target.setColor(new Color(red/255, green/255, blue/255, alpha/255));

	double newWidth = (sourceWidth + (zeroToOne * (destinationWidth[loopC] - sourceWidth)));

	//System.out.println(sourceWidth + " " + destinationWidth[loopC] + " " +
	//	   newWidth);

	target.setLineWidth(newWidth);
    }

    protected void activityFinished() {
	PActivityScheduler scheduler = getActivityScheduler();
	
	super.activityFinished();

	loopC++;
	if (loopC <= destinationColor.length - 1) {
	    loopC++;
	    setStarts();
	    setStartTime(getStartTime()+getDuration());
	    scheduler.addActivity(this);
	} 
    }
}
