package com.hp.hpl.guess.piccolo.util;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.PFrame;
import edu.umd.cs.piccolox.nodes.P3DRect;
import com.hp.hpl.guess.*;
import com.hp.hpl.guess.ui.*;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.Paint;
import java.awt.BasicStroke;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.handles.PHandle;
import edu.umd.cs.piccolo.activities.*;
import java.util.*;
import javax.swing.*;
import java.io.*;

public class SerEllipse extends PPath implements SerInterface {
    
    private double sx,sy,sw,sh;

    public SerEllipse() {
	super();
    }

    public SerEllipse(double x, 
		   double y,
		   double w,
		   double h) {
	setPathTo(new Ellipse2D.Double(x,y,w,h));
	sx = x;
	sy = y;
	sw = w;
	sh = h;
    }

    public void writeObject(java.io.ObjectOutputStream ois)
	throws IOException {
	ois.writeDouble(sx);
	ois.writeDouble(sy);
	ois.writeDouble(sw);
	ois.writeDouble(sh);
	ois.writeObject(getStrokePaint());
	ois.writeObject(getBounds());
	Util.writeBasicStroke(ois,getStroke());
    }

    public void readObject(java.io.ObjectInputStream ois)
	throws IOException, ClassNotFoundException {
	sx = ois.readDouble();
	sy = ois.readDouble();
	sw = ois.readDouble();
	sh = ois.readDouble();
	setPathTo(new Ellipse2D.Double(sx,sy,sw,sh));
	setPaint(null);
	setStrokePaint((Paint)ois.readObject());
	setBounds((PBounds)ois.readObject());
	setStroke(Util.readBasicStroke(ois));
    }

}
