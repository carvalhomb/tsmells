package com.hp.hpl.guess.ui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideButton;
import com.jidesoft.utils.SystemInfo;
import com.jidesoft.utils.Lm;
import com.jidesoft.swing.MultilineLabel;

import com.hp.hpl.guess.*;

/**
 * @pyobj DragWindow
 */
public class DragWindow extends JPanel implements Dockable {

    private HashMap panes = new HashMap();

    private HashMap cp2lp = new HashMap();

    public static DragWindow singleton = null;

    /**
     * @pyexport dragwindow
     */
    public static void create() {
	if ((getDragWindow().getWindow() == null) || 
	    (!getDragWindow().getWindow().isVisible())) {
	    Guess.getMainUIWindow().dock(getDragWindow());
	}
    }

    public static DragWindow getDragWindow() {
	if (singleton == null) {
	    singleton = new DragWindow();
	}
	return(singleton);
    }

    public Dimension getPreferredSize() {
	return(new Dimension(250,500));
    }

    private DragWindow() {
        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);
        CollapsiblePanes pane = new CollapsiblePanes();
	pane.setPreferredSize(new Dimension(240,490));
	CollapsiblePane info = new CollapsiblePane("Draggable Menu System");
	//pane.add(info);
	//pane.setMinimumSize(new Dimension(350,600));
        pane.add(createFileFolderTaskPane("Generic"));
	pane.add(createFileFolderTaskPane("Node"));
        pane.add(createFileFolderTaskPane("Graph"));
        pane.add(createFileFolderTaskPane("Field"));
        pane.add(createFileFolderTaskPane("User"));
        pane.addExpansion();
	add(new JScrollPane(pane));
	//setMinimumSize(new Dimension(350,600));
		Guess.getMainUIWindow().dock(this);

	//System.out.println(getMinimumSize());
    }

    private CollapsiblePane createFileFolderTaskPane(String st) {
	if (panes.containsKey(st)) {
	    return((CollapsiblePane)panes.get(st));
	}

        CollapsiblePane pane = new CollapsiblePane(st);
	//pane.setMinimumSize(new Dimension(350,100));
	// uncomment following for a different style of collapsible pane
	//        panel.setStyle(CollapsiblePane.TREE_STYLE);
        JPanel labelPanel = new JPanel();
	//labelPanel.setMinimumSize(new Dimension(350,100));
        labelPanel.setOpaque(false);
        labelPanel.setLayout(new GridLayout(6, 1, 1, 0));
	
	labelPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        pane.setContentPane(labelPanel);
        pane.setEmphasized(true);
	pane.setEnabled(false);
	panes.put(st,pane);
	cp2lp.put(pane,labelPanel);
        return pane;
    }

    public void addButton(String category, DWButton db) {

	CollapsiblePane cp = (CollapsiblePane)panes.get(category);
	if (cp == null)
	    throw(new Error(category + " does not exist"));
	
	cp.setEnabled(true);

	JPanel labelPanel = (JPanel)cp2lp.get(cp);


	//public void receiveDrop(Object o) {
	///    if (o instanceof Collection) {
	//	Iterator it = ((Collection)o).iterator();
	//	while(it.hasNext()) {
	//	    Object ge = it.next();
	//	    if (ge instanceof GraphElement) {
	//		((GraphElement)ge).__setattr__("color",Color.blue);
	//	    }
	//	}
	//    }
	//}
	//  });
				
        labelPanel.add(db);
    }

    public int getDirectionPreference() {
	return(MainUIWindow.VERTICAL_DOCK);
    }

    public void opening(boolean state) {
    }

    public void attaching(boolean state) {
    }

    private GuessJFrame myParent = null;

    public GuessJFrame getWindow() {
	return(myParent);
    }

    public void setWindow(GuessJFrame gjf) {
	myParent = gjf;
    }

    public String getTitle() {
	return("Menu");
    }


    
    //    class DragMouseAdapter extends MouseAdapter {
    //public void mousePressed(MouseEvent e) {
    //    JComponent c = (JComponent)e.getSource();
    //    TransferHandler handler = c.getTransferHandler();
    //    if (handler != null) {
    //	handler.exportAsDrag(c, e, TransferHandler.COPY);
    //    }
    //}
    //}
}

//    JButton test = new JButton("test");

//  public DragWindow() {
//test.setTransferHandler(new TransferHandler("text"));
//test.addMouseListener(new DragMouseAdapter());
//getContentPane().add(test);
//setSize(100,100);
//setVisible(true);
//  }

//  private 
//  }
//}
