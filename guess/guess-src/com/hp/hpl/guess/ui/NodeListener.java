package com.hp.hpl.guess.ui;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;

public interface NodeListener extends GraphElementListener {
    
    public void setLocation(double x, double y);
    
    public void setLocation(double x, double y, double width, double height);
}
