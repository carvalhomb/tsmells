package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.GridBagConstraints;

import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.*;
import javax.swing.border.LineBorder;

import com.hp.hpl.guess.piccolo.*;
import com.hp.hpl.guess.storage.*;
import com.hp.hpl.guess.*;

public class StatusBar extends JPanel implements ActionListener {

    protected static JProgressBar progressBar = null;
    protected static JLabel label = null;

    private static StatusBar sb = null;
    private static JComboBox stateList = null;

    private static boolean ignoreChange = false;

    private SimpleButton[] sbList = new SimpleButton[5];

    private static HashSet states = new HashSet();

    public static boolean useButtons = true;

    public static boolean buttonsEnabled() {
	return(useButtons);
    }

    public static void enableButtons(boolean state) {
	if (sb != null) {
	    for (int i = 0 ; i < 5 ;i++) {
		sb.sbList[i].setEnabled(state);
	    }
	}
	useButtons = state;
    }

    public static void setState(String state) {
	// we're going to push the new state to the 
	// bottom
	if (stateList != null) {
	    if (!states.contains(state)) {
		ignoreChange = true;
		stateList.addItem(state);
		states.add(state);
	    }
	    ignoreChange = true;
	    stateList.setSelectedItem(state);
	    ignoreChange = false;
	}
    }

    public static void repaintNow() {
	if (sb != null) {
	    sb.repaint();
	}
    }

    public static void setValue(int max, int nv) {
	if (progressBar != null) {
	    progressBar.setIndeterminate(false);	
	    progressBar.setMaximum(max);
	    progressBar.setValue(nv);
	}
    }

    public static void setStatus(String status) {
	if (label != null) {
	    label.setForeground(Color.black);
	    label.setText("     "+status);
	    label.setToolTipText(null);
	}
    }

    public static void setErrorStatus(String status) {
	if (label != null) {
	    //Thread.dumpStack();
	    label.setForeground(Color.red);
	    label.setText("     "+status);
	    label.setToolTipText(status);
	}
    }

    public static void runProgressBar(boolean state) {
	if (progressBar != null) {
	    if (state) {
		//progressBar.setBackground(Color.red);
	    } else {
		//progressBar.setBackground(Color.green);
	    }
	    progressBar.setIndeterminate(state);	
	}
    }

    public static void setStatus(String status,boolean state) {
	runProgressBar(state);
	setStatus(status);
    }

    public StatusBar() {
	super();
	setPreferredSize(new Dimension(800,25));
	setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	label = new JLabel("     Welcome to GUESS");
	label.setPreferredSize(new Dimension(200,20));
	label.setMaximumSize(new Dimension(200,20));

	c.fill = GridBagConstraints.HORIZONTAL;
	c.weighty = 1;
	c.weightx = 0;
	c.gridx = 0;
	c.gridy = 0;
	c.insets = new Insets(0,5,0,5);

	progressBar = (JProgressBar)new ErrorTolerantProgressBar();
	progressBar.setPreferredSize(new Dimension(100,18));
	progressBar.setMinimumSize(new Dimension(100,18));
	progressBar.setMaximumSize(new Dimension(100,18));
	//progressBar.setBackground(Color.green);

	add(progressBar,c);

	c.fill = GridBagConstraints.HORIZONTAL;
	c.weighty = 1;
	c.gridx = 1;

	String[] stateStrings = { "Select a state" };
	//Create the combo box, select item at index 4.
	//Indices start at 0, so 4 specifies the pig.
	stateList = new JComboBox(stateStrings);
	stateList.setSelectedIndex(0);
	StorageListener sl = StorageFactory.getSL();
	Iterator it = sl.getStates().iterator();
	while(it.hasNext()) {
	    String s = (String)it.next();
	    stateList.addItem(s);
	    states.add(s);
	}
	stateList.addActionListener(this);
	c.insets = new Insets(0,5,0,10);
	add(stateList,c);

	MouseAdapter ma = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {

		    if (!buttonsEnabled()) {
			return;
		    }

		    Component c = e.getComponent();
		    if (c instanceof SimpleButton) {
			for (int i = 0 ; i < sbList.length ; i++) {
			    sbList[i].click(false);
			}
			((SimpleButton)c).click(true);
			int bType = ((SimpleButton)c).bType;
			FrameListener fl = 
			    VisFactory.getFactory().getDisplay();
			if (fl instanceof GFrame) {
			    ((GFrame)fl).switchHandler(bType - 1);
			}
		    }
		}
	    };

	c.fill = GridBagConstraints.NONE;
	c.weightx = 0;
	c.gridx = 2;
	c.insets = new Insets(1,1,1,1);
	SimpleButton sb = new SimpleButton("browse.gif",1,"Browse");
	add(sb,c);
	sbList[0] = sb;
	sb.click(true);
	sb.addMouseListener(ma);

	c.gridx = 3;
	sb = new SimpleButton("nodeed.gif",2,"Manipulate Nodes");
	add(sb,c);
	sbList[1] = sb;
	sb.addMouseListener(ma);

	c.gridx = 4;
	sb = new SimpleButton("edgeed.gif",3,"Manipulate Edges");
	add(sb,c);
	sbList[2] = sb;
	sb.addMouseListener(ma);

	c.gridx = 5;
	sb = new SimpleButton("hulled.gif",4,"Manipulate Hulls");
	add(sb,c);
	sbList[3] = sb;
	sb.addMouseListener(ma);

	c.gridx = 6;
	sb = new SimpleButton("draw.gif",5,"Draw");
	add(sb,c);
	sbList[4] = sb;
	sb.addMouseListener(ma);

	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = .8;
	c.gridx = 7;
	add(label,c);

	if (VisFactory.getUIMode() == VisFactory.PICCOLO) {
	    c.gridx = 8;
	    c.weightx = 0;
	    ImageIcon previewIcon = 
		new ImageIcon(getClass().getResource("/images/previewscroller.png"));
	    add(new PreviewCorner((GFrame)VisFactory.getFactory().getDisplay(),
				  previewIcon,
				  true,
				  "whatever"));
	}
	
	StatusBar.sb = this;
    }

    public void actionPerformed(ActionEvent e) {
	Object newItem = stateList.getSelectedItem();
	//System.out.println(newItem);
	if ("comboBoxChanged".equals(e.getActionCommand())) {
	    if (!((String)newItem).equals("Select a state")) {
		if (ignoreChange) {
		    //System.out.println("ignoring...");
		    ignoreChange = false;
		    return;
		}
		setStatus("ls(g,"+newItem+")");
		StorageFactory.getSL().loadState(Guess.getGraph(),
						 (String)newItem);
	    }
	}
    }

    class ErrorTolerantProgressBar extends JProgressBar {

	public ErrorTolerantProgressBar() {
	    super();
	}
	
	public void paint(Graphics g) {
	    try {
		super.paint(g);
	    } catch (Exception e) {
	    }
	}
    }


}
