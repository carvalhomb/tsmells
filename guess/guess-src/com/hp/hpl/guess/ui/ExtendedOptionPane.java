package com.hp.hpl.guess.ui;

import java.awt.*;
import java.beans.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.beans.*;

public class ExtendedOptionPane {

    public static int showOptionDialog(Component parentComponent,
        Object message, String title, int optionType, int messageType,
        Icon icon, Object[] options, Object initialValue)
        throws HeadlessException {
        JOptionPane pane = new JOptionPane(message, messageType,
					   optionType, icon,
					   options, initialValue);
	
        pane.setInitialValue(initialValue);
        pane.setComponentOrientation(((parentComponent == null) ?
	    getRootFrame() : parentComponent).getComponentOrientation());

        //int style = styleFromMessageType(messageType);
        JDialog dialog = createDialog(null,null,title);

        pane.selectInitialValue();
        dialog.show();
        dialog.dispose();

        Object        selectedValue = pane.getValue();

        if(selectedValue == null)
            return -1;
        if(options == null) {
            if(selectedValue instanceof Integer)
                return ((Integer)selectedValue).intValue();
            return -1;
        }
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return counter;
        }
        return -1;
    }

    /**
     * Creates and returns a new <code>JDialog</code> wrapping
     * <code>this</code> centered on the <code>parentComponent</code>
     * in the <code>parentComponent</code>'s frame.
     * <code>title</code> is the title of the returned dialog.
     * The returned <code>JDialog</code> will not be resizable by the
     * user, however programs can invoke <code>setResizable</code> on
     * the <code>JDialog</code> instance to change this property.
     * The returned <code>JDialog</code> will be set up such that
     * once it is closed, or the user clicks on one of the buttons,
     * the optionpane's value property will be set accordingly and
     * the dialog will be closed.  Each time the dialog is made visible,
     * it will reset the option pane's value property to 
     * <code>JOptionPane.UNINITIALIZED_VALUE</code> to ensure the
     * user's subsequent action closes the dialog properly.
     *
     * @param parentComponent determines the frame in which the dialog
     *		is displayed; if the <code>parentComponent</code> has
     *		no <code>Frame</code>, a default <code>Frame</code> is used
     * @param title     the title string for the dialog
     * @return a new <code>JDialog</code> containing this instance
     * @exception HeadlessException if
     *   <code>GraphicsEnvironment.isHeadless</code> returns
     *   <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public static JDialog createDialog(Component t, 
				       Component parentComponent, String title)
        throws HeadlessException {
	
        final JDialog dialog;
	
        Window window = getWindowForComponent(parentComponent);
        if (window instanceof Frame) {
            dialog = new JDialog((Frame)window, title, true);	
        } else {
            dialog = new JDialog((Dialog)window, title, true);
        }
        Container             contentPane = dialog.getContentPane();
	
        contentPane.setLayout(new BorderLayout());
        contentPane.add(t, BorderLayout.CENTER);
        dialog.setResizable(false);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.setUndecorated(true);
                //getRootPane().setWindowDecorationStyle(style);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);
        dialog.addWindowListener(new WindowAdapter() {
            private boolean gotFocus = false;
            public void windowClosing(WindowEvent we) {
                //setValue(null);
            }
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    //selectInitialValue();
                    gotFocus = true;
                }
            }
        });
	dialog.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
	        // reset value to ensure closing works properly
                //setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
	});
        
	//	addPropertyChangeListener(new PropertyChangeListener() {
	//  public void propertyChange(PropertyChangeEvent event) {
                // Let the defaultCloseOperation handle the closing
                // if the user closed the window without selecting a button
                // (newValue = null in that case).  Otherwise, close the dialog.
                //if(dialog.isVisible() && event.getSource() == t &&
		// (event.getPropertyName().equals(VALUE_PROPERTY)) &&
		//  event.getNewValue() != null &&
		//  event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
		//  dialog.setVisible(false);               
                //}
	//  }
        //});
        return dialog;
    }

    public static Frame getRootFrame() throws HeadlessException {
	return(JOptionPane.getRootFrame());
    }

    static Window getWindowForComponent(Component parentComponent)
        throws HeadlessException {
        if (parentComponent == null)
            return getRootFrame();
        if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
            return (Window)parentComponent;
        return getWindowForComponent(parentComponent.getParent());
    }

}
