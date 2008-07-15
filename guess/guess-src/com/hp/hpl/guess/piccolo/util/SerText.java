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

public class SerText extends PText implements SerInterface {

    public SerText() {
    }

    public SerText(String text) {
	super(text);
    }
}
