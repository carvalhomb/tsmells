package edu.umd.cs.piccolo;

import com.hp.hpl.guess.piccolo.PFactory;
import edu.umd.cs.piccolo.util.PPaintContext;

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

    public void paintBackChannel(PPaintContext ppc) {
	super.paint(ppc);
    }
}
