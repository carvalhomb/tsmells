package edu.umd.cs.piccolo.activities;

import java.awt.Color;
import com.hp.hpl.guess.piccolo.*;
import edu.umd.cs.piccolo.*;
import com.hp.hpl.guess.ui.*;
import edu.umd.cs.piccolo.nodes.*;

public class GuessNodeActivity extends PInterpolatingActivity {
	
    private Color sourceColor;
    private Color[] destinationColor;
    
    private boolean sourceVisible;
    private boolean[] destinationVisible;

    private boolean sourceLabelVisible;
    private boolean[] destinationLabelVisible;

    private double sourceX;
    private double[] destinationX;
    private double sourceY;
    private double[] destinationY;

    private double sourceWidth;
    private double sourceHeight;
    private double[] destinationWidth;
    private double[] destinationHeight;

    private GuessPNode target;
    
    private int loopC = 0;

    /**
     * Create a new GuessNodeActivity.
     * <P>
     * @param duration the length of one loop of the activity
     * @param stepRate the amount of time between steps of the activity
     * @param loopCount number of times the activity should reschedule itself
     * @param mode defines how the activity interpolates between states
     * @param aTarget the object that the activity will be applied to and where
     * the source state will be taken from.
     * @param aDestination the destination color state
     */
    public GuessNodeActivity(long duration, 
			     long stepRate, 
			     GuessPNode aTarget) {
	super(duration, stepRate, 1, 
	      PInterpolatingActivity.SOURCE_TO_DESTINATION);
	target = aTarget;
	setStarts();
    }	

    protected boolean isAnimation() {
	return true;
    }

    public void setStarts() {
	this.sourceX = target.getX();
	this.sourceY = target.getY();
	
	this.sourceHeight = target.getHeight();
	this.sourceWidth = target.getWidth();

	this.sourceColor = (Color)target.getPaint();
	
	this.sourceVisible = ((PNode)target).getVisible();
	this.sourceLabelVisible = 
	    ((Boolean)target.get("labelvisible")).booleanValue();

    }

    public void setDestinationGone() {
	setDestination(sourceX,sourceY,null,
		       false,false,sourceWidth,sourceHeight);
    }

    private boolean disappearEarly = false;
    private boolean appearLate = false;

    public void setDestination(double dX, double dY,
			       Color dC, boolean dV,
			       boolean dLV, double dWidth,
			       double dHeight) {
	this.destinationX = new double[]{dX};
	this.destinationY = new double[]{dY};

	this.destinationWidth = new double[]{dWidth};
	this.destinationHeight = new double[]{dHeight};

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
	    this.destinationColor = new Color[]{Morpher.getInvisible()};
	}

	this.destinationLabelVisible = new boolean[]{dLV};
    }
    
    public void setDestinations(double[] dX, double[] dY,
				Color[] dC, boolean[] dV,
				boolean[] dLV,
				double[] dWidth,
				double[] dHeight) {
	this.destinationX = dX;
	this.destinationY = dY;
	this.destinationWidth = dWidth;
	this.destinationHeight = dHeight;

	this.destinationVisible = dV;
	this.destinationColor = dC;
	
	this.destinationLabelVisible = dLV;
    }

    protected void activityStarted() {
	super.activityStarted();
    }
    
    public void setRelativeTargetValue(float zeroToOne) {
	super.setRelativeTargetValue(zeroToOne);

	if (disappearEarly) {
	    zeroToOne = zeroToOne/Morpher.getNodeDisappearBy();
	    if (zeroToOne > 1) 
		zeroToOne = 1;
	} else if (appearLate) {
	    zeroToOne = (zeroToOne - Morpher.getNodeAppearAfter())/
		(1 - Morpher.getNodeAppearAfter());
	    if (zeroToOne < 0)
		return;
	}

	float red = (float) (sourceColor.getRed() + (zeroToOne * (destinationColor[loopC].getRed() - sourceColor.getRed())));
	float green = (float) (sourceColor.getGreen() + (zeroToOne * (destinationColor[loopC].getGreen() - sourceColor.getGreen())));
	float blue = (float) (sourceColor.getBlue() + (zeroToOne * (destinationColor[loopC].getBlue() - sourceColor.getBlue())));
	
	float alpha = (float) (sourceColor.getAlpha() + (zeroToOne * (destinationColor[loopC].getAlpha() - sourceColor.getAlpha())));
	target.setColor(new Color(red/255, green/255, blue/255, alpha/255));

	double newX = (sourceX + (zeroToOne * (destinationX[loopC] - sourceX)));
	double newY = (sourceY + (zeroToOne * (destinationY[loopC] - sourceY)));
	double newWidth = (sourceWidth + (zeroToOne * (destinationWidth[loopC] - sourceWidth)));
	double newHeight = (sourceHeight + (zeroToOne * (destinationHeight[loopC] - sourceHeight)));
	target.setLocation(newX,newY,newWidth,newHeight);
    }

    protected void activityFinished() {
	PActivityScheduler scheduler = getActivityScheduler();
	
	super.activityFinished();

	loopC++;
	if (loopC <= destinationX.length - 1) {
	    loopC++;
	    setStarts();
	    setStartTime(getStartTime()+getDuration());
	    scheduler.addActivity(this);
	} 
    }
}
