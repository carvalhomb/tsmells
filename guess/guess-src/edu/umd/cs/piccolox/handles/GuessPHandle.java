package edu.umd.cs.piccolox.handles;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingConstants;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.util.PBoundsLocator;

public class GuessPHandle extends PBoundsHandle {
    
    public static void addBoundsHandlesTo(PNode aNode) {
	aNode.addChild(new GuessPHandle(PBoundsLocator.createEastLocator(aNode))); 
	aNode.addChild(new GuessPHandle(PBoundsLocator.createWestLocator(aNode))); 
	aNode.addChild(new GuessPHandle(PBoundsLocator.createNorthLocator(aNode))); 
	aNode.addChild(new GuessPHandle(PBoundsLocator.createSouthLocator(aNode)));
	aNode.addChild(new GuessPHandle(PBoundsLocator.createNorthEastLocator(aNode))); 
	aNode.addChild(new GuessPHandle(PBoundsLocator.createNorthWestLocator(aNode))); 
	aNode.addChild(new GuessPHandle(PBoundsLocator.createSouthEastLocator(aNode))); 
	aNode.addChild(new GuessPHandle(PBoundsLocator.createSouthWestLocator(aNode))); 	
    }
    
    public static void addStickyBoundsHandlesTo(PNode aNode, PCamera camera) {
	camera.addChild(new GuessPHandle(PBoundsLocator.createEastLocator(aNode)));
	camera.addChild(new GuessPHandle(PBoundsLocator.createWestLocator(aNode)));
	camera.addChild(new GuessPHandle(PBoundsLocator.createNorthLocator(aNode)));
	camera.addChild(new GuessPHandle(PBoundsLocator.createSouthLocator(aNode)));
	camera.addChild(new GuessPHandle(PBoundsLocator.createNorthEastLocator(aNode)));
	camera.addChild(new GuessPHandle(PBoundsLocator.createNorthWestLocator(aNode)));
	camera.addChild(new GuessPHandle(PBoundsLocator.createSouthEastLocator(aNode)));
	camera.addChild(new GuessPHandle(PBoundsLocator.createSouthWestLocator(aNode)));
    }

    private static boolean resizeable = true;

    public static void setResizeable(boolean state) {
	resizeable = state;
    }
						    
    public GuessPHandle(PBoundsLocator aLocator) {
	super(aLocator);
    }
    
    public void startHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
	if (resizeable)
	    super.startHandleDrag(aLocalPoint,aEvent);
    }
    
    public void dragHandle(PDimension aLocalDimension, PInputEvent aEvent) {
	if (resizeable)
	    super.dragHandle(aLocalDimension,aEvent);
    }
	
    public void endHandleDrag(Point2D aLocalPoint, PInputEvent aEvent) {
	if (resizeable)
	    super.endHandleDrag(aLocalPoint,aEvent);
    }	

}
