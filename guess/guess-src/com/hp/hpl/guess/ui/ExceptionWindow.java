package com.hp.hpl.guess.ui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import org.python.core.*;
import java.awt.datatransfer.*;
import com.hp.hpl.guess.Version;

public class ExceptionWindow extends JFrame
{
    JTextArea eMain = new JTextArea();
    JLabel eLabel = new JLabel();
    JButton copyButton = new JButton();
    JButton forwardB = new JButton();
    JButton backB = new JButton();
    JScrollPane jscrollpane1 = null;

    private Vector exceptions = new Vector();

    private int location = 0;

    private static ExceptionWindow singleton = null;

    public static ExceptionWindow getExceptionWindow() {
	return(getExceptionWindow(null));
    } 

    public static ExceptionWindow getExceptionWindow(Throwable t) {
	if (singleton == null) {
	    singleton = new ExceptionWindow();
	}
	singleton.newException(t);
	return(singleton);
    }

    public void newException(Throwable t) {
	if (t != null) {
	    exceptions.add(t);
	    setLocation(exceptions.size() - 1);
	    if (t instanceof PyException) {
		StatusBar.setErrorStatus(((PyException)t).userFriendlyString());
	    } else {
		StatusBar.setErrorStatus(t.toString());
	    }
	}
    }
    
    private Clipboard clipboard = null;
    
    public void addToClipboard() {
	if (clipboard == null) {
	    try {
		clipboard = 
		    Toolkit.getDefaultToolkit().getSystemClipboard();
	    } catch (Exception ex) {
		ExceptionWindow.getExceptionWindow(ex);
	    }
	}
	if (clipboard != null) {
	    try {
		StringSelection ss = new StringSelection(eMain.getText());
		clipboard.setContents(ss,ss);
	    } catch (Exception ex) {
		ExceptionWindow.getExceptionWindow(ex);
	    }
	}
    }

    private void setLocation(int loc) {
	
	location = loc;

	if (loc <= 0) {
	    location = 0;
	    backB.setEnabled(false);
	} else {
	    backB.setEnabled(true);
	}

	if (loc >= (exceptions.size() - 1)) {
	    location = exceptions.size() - 1;
	    forwardB.setEnabled(false);
	} else {
	    forwardB.setEnabled(true);
	}
	
	//System.out.println(location + " " + exceptions.elementAt(location));
	displayException((Throwable)exceptions.elementAt(location));
    }

    public void displayException(Throwable t) {
	if (t != null) {
	    eMain.setText(getStackTrace(t));
	    if (t instanceof PyException) {
		eLabel.setText(((PyException)t).userFriendlyString());
	    } else {
		eLabel.setText(t.toString());
	    }
	    eMain.setCaretPosition(0);
	}
    }

    public static String getStackTrace( Throwable aThrowable ) {
	final Writer result = new StringWriter();
	final PrintWriter printWriter = new PrintWriter( result );
	aThrowable.printStackTrace( printWriter );
	printWriter.println("\n\nGUESS version: " + 
			    Version.MAJOR_VERSION + "/"+ 
			    Version.MINOR_VERSION);
	return result.toString();
    }

    /**
     * Default constructor
     */
    private ExceptionWindow()
    {
	super("Error Log");
	setSize(550, 250);
	setLocation(100, 100);
	initializePanel();
	backB.setEnabled(false);
	forwardB.setEnabled(false);
	eMain.setEditable(false);
	addWindowListener( new WindowAdapter()
	    {
		public void windowClosing( WindowEvent evt )
		{
		    setVisible(false);
		}
	    });
	copyButton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
		    addToClipboard();
		}
	    });
	backB.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
		    setLocation(location-1);
		}
	    });
	forwardB.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
		    setLocation(location+1);
		}
	    });
    }
    
    /**
     * Adds fill components to empty cells in the first row and first
     * column of the grid.  This ensures that the grid spacing will be
     * the same as shown in the designer.
     * @param cols an array of column indices in the first row where
     * fill components should be added.
     * @param rows an array of row indices in the first column where
     * fill components should be added.
     */
   void addFillComponents( Container panel, int[] cols, int[] rows )
   {
      Dimension filler = new Dimension(10,10);

      boolean filled_cell_11 = false;
      CellConstraints cc = new CellConstraints();
      if ( cols.length > 0 && rows.length > 0 )
      {
         if ( cols[0] == 1 && rows[0] == 1 )
         {
            /** add a rigid area  */
            panel.add( Box.createRigidArea( filler ), cc.xy(1,1) );
            filled_cell_11 = true;
         }
      }

      for( int index = 0; index < cols.length; index++ )
      {
         if ( cols[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(cols[index],1) );
      }

      for( int index = 0; index < rows.length; index++ )
      {
         if ( rows[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(1,rows[index]) );
      }

   }

   /**
    * Helper method to load an image file from the CLASSPATH
    * @param imageName the package and name of the file to load relative to the CLASSPATH
    * @return an ImageIcon instance with the specified image file
    * @throws IllegalArgumentException if the image resource cannot be loaded.
    */
   public ImageIcon loadImage( String imageName )
   {
      try
      {
         ClassLoader classloader = getClass().getClassLoader();
         java.net.URL url = classloader.getResource( imageName );
         if ( url != null )
         {
            ImageIcon icon = new ImageIcon( url );
            return icon;
         }
      }
      catch( Exception e )
      {
	  getExceptionWindow(e);
      }
      throw new IllegalArgumentException( "Unable to load image: " + imageName );
   }

   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:132PX:NONE,FILL:290PX:NONE,FILL:46PX:NONE,FILL:47PX:NONE,FILL:DEFAULT:NONE","CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      eMain.setName("eMain");
      jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(eMain);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xywh(2,5,4,15));

      eLabel.setForeground(new Color(255,0,0));
      eLabel.setName("eLabel");
      eLabel.setText("No exception reported");
      jpanel1.add(eLabel,cc.xywh(2,2,4,2));

      copyButton.setActionCommand("Copy to clipboard");
      copyButton.setName("copyButton");
      copyButton.setText("Copy to clipboard");
      jpanel1.add(copyButton,cc.xy(2,21));

      forwardB.setActionCommand(">");
      forwardB.setName("forwardB");
      forwardB.setText(">");
      jpanel1.add(forwardB,cc.xy(5,21));

      backB.setActionCommand("<");
      backB.setName("backB");
      backB.setText("<");
      jpanel1.add(backB,cc.xy(4,21));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22 });
      return jpanel1;
   }

    /**
     * Initializer
     */
    protected void initializePanel()
    {
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(createPanel(), BorderLayout.CENTER);
    }


}
