package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolox.nodes.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import edu.umd.cs.piccolo.activities.*;
import java.util.*;

import com.hp.hpl.guess.ui.*;
import com.hp.hpl.guess.Node;
import com.hp.hpl.guess.piccolo.GFrame;

public class ConvexHullLegendNode extends PPath {

    public ConvexHullLegendNode(ConvexHullNode chn) {
	float[] x3Points = chn.getX3();
	float[] y3Points = chn.getY3();
	if ((x3Points != null) && (x3Points.length > 0)) {
	    setPathToPolyline(x3Points,y3Points);
	    setPaint(chn.getPaint());
	    setStrokePaint(null);
	}
	setBounds(0,0,15,15);
    }
}
