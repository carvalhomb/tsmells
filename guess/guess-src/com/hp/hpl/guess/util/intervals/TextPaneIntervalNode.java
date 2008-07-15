package com.hp.hpl.guess.util.intervals;

public class TextPaneIntervalNode extends IntervalNode {

    public final static int ANSWER_STYLE = 0;
    public final static int COMMAND_STYLE = 1;
    public final static int ERROR_STYLE = 2;

    private int style = 0;

    public final int getStyle() {
	return(style);
    }

    public final void setStyle(int style) {
	this.style = style;
    }

    public TextPaneIntervalNode(int low, int high, Object proxy) {
	super(low,high,proxy);
    }

    public TextPaneIntervalNode(int low, int high) {
	super(low,high);
    }

    public TextPaneIntervalNode() {
	super();
    }
}
