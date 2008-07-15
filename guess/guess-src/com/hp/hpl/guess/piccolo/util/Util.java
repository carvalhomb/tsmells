package com.hp.hpl.guess.piccolo.util;

import java.awt.Stroke;
import java.awt.BasicStroke;
import java.io.*;

public abstract class Util {

    private static final float[] dashPattern = { 30, 10, 10, 10 };

    public static void writeBasicStroke(ObjectOutputStream oos, 
					Stroke b) 
	throws IOException {
	BasicStroke bas = (BasicStroke)b;
	float[] dash = bas.getDashArray();
	if (dash != null) {
	    oos.writeInt(1);
	    oos.writeFloat(bas.getLineWidth());
	} else {
	    oos.writeInt(0);
	    oos.writeFloat(bas.getLineWidth());
	}
    }

    public static BasicStroke readBasicStroke(ObjectInputStream ois) 
	throws IOException, ClassNotFoundException {
	int dashLength = ois.readInt();
	float[] dash = null;
	BasicStroke toRet = null;
	if (dashLength != 0) {
	    toRet = new BasicStroke(ois.readFloat(), BasicStroke.CAP_BUTT,
				    BasicStroke.JOIN_MITER, 10,
				    dashPattern, 0);
	} else {
	    toRet = new BasicStroke(ois.readFloat());
	}
	return(toRet);
    }
}
