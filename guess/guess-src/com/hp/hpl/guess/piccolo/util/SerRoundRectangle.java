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

public class SerRoundRectangle extends PPath implements SerInterface {
    
    private double sx,sy,sw,sh,saw,sah;

    public SerRoundRectangle() {
	super();
    }
    
    public SerRoundRectangle(double x, 
			     double y,
			     double w,
			     double h,
			     double aw,
			     double ah) {
	setPathTo(new RoundRectangle2D.Double(x,y,w,h,aw,ah));
	sx = x;
	sy = y;
	sw = w;
	sh = h;
	saw = aw;
	sah = ah;
    }

    public void writeObject(java.io.ObjectOutputStream ois)
	throws IOException {
	ois.writeDouble(sx);
	ois.writeDouble(sy);
	ois.writeDouble(sw);
	ois.writeDouble(sh);
	ois.writeDouble(saw);
	ois.writeDouble(sah);
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
	saw = ois.readDouble();
	sah = ois.readDouble();
	setPathTo(new RoundRectangle2D.Double(sx,sy,sw,sh,saw,sah));
	setPaint(null);
	setStrokePaint((Paint)ois.readObject());
	setBounds((PBounds)ois.readObject());
	setStroke(Util.readBasicStroke(ois));
    }

}
