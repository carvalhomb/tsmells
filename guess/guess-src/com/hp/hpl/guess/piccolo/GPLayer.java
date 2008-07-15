package com.hp.hpl.guess.piccolo;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;

public class GPLayer extends PLayer {

    public void setChildPaintInvalid(boolean cp) {
	if (cp)
	    PFactory.updateTime();
	super.setChildPaintInvalid(cp);
    }

    public PNode removeChild(PNode child) {
	PFactory.updateTime();
	return(super.removeChild(child));
    }
}
