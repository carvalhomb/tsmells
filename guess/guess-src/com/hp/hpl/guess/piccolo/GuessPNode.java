package com.hp.hpl.guess.piccolo;

import java.awt.*;

import edu.umd.cs.piccolo.event.PInputEvent;

import com.hp.hpl.guess.ui.NodeListener;
import com.hp.hpl.guess.Node;
import java.awt.geom.Point2D;

public interface GuessPNode extends NodeListener {

    public Node getOwner();

    public void moveDone(double x, double y);
    
    public void inTransition(double x, double y);

    public void mouseEntered(PInputEvent e);

    public void mouseExited(PInputEvent e);

    public void mouseClicked(PInputEvent e);
    
    public double getDrawWidth();
    
    public double getDrawHeight();
    
    public double getX();
    
    public double getY();
    
    public double getWidth();
    
    public double getHeight();
    
    public void setColor(Color color);
    
    public void setStyle(int style);
    
    public int getStyle();

    public void setShape(Shape shape, int style);
    
    public Paint getPaint();

    public void addHullListener(ConvexHullNode chn);

    public void removeHullListener(ConvexHullNode chn);

    public Point2D[] getPrefPorts();

    public Point2D[] getAllPorts();

    public void centerDisplay();
}
