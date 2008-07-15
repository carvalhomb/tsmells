package com.hp.hpl.guess.ui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.*;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

import com.jgoodies.looks.*;

public class PickFile extends JPanel implements ActionListener {
   JFormattedTextField m_jformattedtextfield1 = new JFormattedTextField();
   JButton m_jbutton1 = new JButton();
   JButton m_jbutton2 = new JButton();
   JButton m_jbutton3 = new JButton();
   JComboBox m_jcombobox1 = new JComboBox();
   JFormattedTextField m_jformattedtextfield2 = new JFormattedTextField();
   JButton m_jbutton4 = new JButton();
   JFormattedTextField m_jformattedtextfield3 = new JFormattedTextField();

    /**
     * do some initial setup to the UI look and feel
     */
    public static void configureUI() {
	//ClearLookManager.setMode(ClearLookMode.DEBUG);

        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
        Options.setDefaultIconSize(new Dimension(18, 18));

        String lafName =
            LookUtils.IS_OS_WINDOWS_XP
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();

        try {
            UIManager.setLookAndFeel(lafName);
        } catch (Exception e) {
	    ExceptionWindow.getExceptionWindow(e);
            System.err.println("Can't set look & feel:" + e);
        }
    }

    JDialog owner = null;
    
    /**
     * Default constructor
     */
    public PickFile()
    {
	initializePanel();
	this.owner = new JDialog();
    }
    
    /**
     * Main method for panel
     */
    public static void main(String[] args)
    {
	configureUI();
	PickFile pf = new PickFile();
	File f =pf.showDialog();
	if (f == null) {
	    System.out.println("Cancel");
	} else {
	    System.out.println(f);
	    if (pf.isPersistent()) {
		System.out.println(pf.getName());
		System.out.println(pf.getDirectory());
	    } else {
		System.out.println("in memory");
	    }
	}
	System.exit(0);
    }
    
    public File showDialog() {
	//JFrame frame = new JFrame();
	//
	owner.setTitle("Please select a file");
	owner.setResizable(false);
	owner.setLocation(200, 200);
	owner.getContentPane().setLayout(new FlowLayout());
	owner.getContentPane().add(this);
	owner.pack();
	owner.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
	owner.setModal(true);
	owner.show();
	return(getSelectedFile());
    }

    public File getSelectedFile() {
	return(approved);
    }

    public File getDirectory() {
	return(new File(m_jformattedtextfield3.getText()));
    }

    public String getName() {
	return(m_jformattedtextfield2.getText());
    }

    public boolean isPersistent() {
	if (m_jcombobox1.getSelectedIndex() == 0) {
	    return(false);
	} else {
	    return(true);
	}
    }

   /**
    * Adds fill components to empty cells in the first row and first column of the grid.
    * This ensures that the grid spacing will be the same as shown in the designer.
    * @param cols an array of column indices in the first row where fill components should be added.
    * @param rows an array of row indices in the first column where fill components should be added.
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
	  ExceptionWindow.getExceptionWindow(e);
      }
      throw new IllegalArgumentException( "Unable to load image: " + imageName );
   }

    File approved = null;

    private JLabel jlabel2 = null;
    private JLabel jlabel3 = null;
    private JLabel jlabel4 = null;

    private static final char sep = File.separatorChar;

    public JPanel createPanel()
    {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:8DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:59PX:NONE,FILL:66PX:NONE,FILL:55PX:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:8DLU:NONE","CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("Please enter a file name (.gdf/.graphml)");
      jpanel1.add(jlabel1,cc.xywh(2,2,6,1));

      jpanel1.add(m_jformattedtextfield1,cc.xywh(2,4,6,1));
      
      m_jbutton1.setActionCommand("JButton");
      m_jbutton1.setText("...");
      jpanel1.add(m_jbutton1,cc.xy(9,4));
      m_jbutton1.addMouseListener(new MouseAdapter(){
		public void mouseClicked(MouseEvent e) {
		    try {
			String toLoad = ".";
			try {
			    toLoad = System.getProperty("gHome");
			    if (toLoad == null) { 
				toLoad = ".";
			    } else {
				File testF = new File(toLoad);
				if (!(testF.exists()) || !(testF.isDirectory())) {
				    toLoad = ".";
				}
			    }
			} catch (Exception hde) {
			    toLoad = ".";
			}
			//			System.out.println(toLoad);
			JFileChooser chooser = 
			    new JFileChooser(new File(toLoad).getCanonicalPath());
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    m_jformattedtextfield1.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		    } catch (IOException ex) {
			ExceptionWindow.getExceptionWindow(ex);
		    }
		}
	    });
    
	m_jbutton2.setActionCommand("Cancel");
	m_jbutton2.setText("Cancel");
	jpanel1.add(m_jbutton2,cc.xy(9,6));
	m_jbutton2.addMouseListener(new MouseAdapter(){
		public void mouseClicked(MouseEvent e) {
		    owner.dispose();
		}
	    });
	
	m_jbutton3.setActionCommand("OK");
	m_jbutton3.setText("OK");
	m_jbutton3.addMouseListener(new MouseAdapter(){	
		public void mouseClicked(MouseEvent e) {
		    try {
			File f = new File(m_jformattedtextfield1.getText());
			if (f.exists()) {
			    approved = f;
			    //owner.dispose();
			} else {
			    JOptionPane.showMessageDialog(null,
							  "File does not exist",
							  "Error",
							  JOptionPane.ERROR_MESSAGE);
			    return;
			}
			if (m_jcombobox1.getSelectedIndex() == 1) {
			    f = new File(m_jformattedtextfield3.getText());
			    if ((!f.exists()) ||
				(!f.isDirectory())) {
				JOptionPane.showMessageDialog(null,
							      "Directory does not exist",
							      "Error",
							      JOptionPane.ERROR_MESSAGE);
				return;
			    }
			    if ((m_jformattedtextfield2.getText() == null) ||
				(m_jformattedtextfield2.getText().equals(""))) {
				JOptionPane.showMessageDialog(null,
							      "You must declare a database name",
							      "Error",
							      JOptionPane.ERROR_MESSAGE);
				return;
			    }
			    f = new File(m_jformattedtextfield3.getText() + 
					 sep +
					 m_jformattedtextfield2.getText() + 
					 ".properties");
			    if (f.exists()) {
				int yn = 
				    JOptionPane.showConfirmDialog(null, 
								  "Database exists, overwrite?","Exists",JOptionPane.YES_NO_OPTION);
 				if (yn == JOptionPane.NO_OPTION) {
 				    return;
 				}
			    }
			}
			owner.dispose();
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(null,
						      ex.toString(),
						      "Error",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	jpanel1.add(m_jbutton3,cc.xy(7,6));

	jpanel1.add(m_jcombobox1,cc.xywh(2,6,4,1));
	m_jcombobox1.addItem("In memory");
	m_jcombobox1.addItem("Persistent");
	m_jcombobox1.addActionListener(this);
	
	jlabel2 = new JLabel();
	jlabel2.setText("Database Configuration");
	jpanel1.add(jlabel2,cc.xywh(2,8,6,1));
	
	jpanel1.add(m_jformattedtextfield2,cc.xywh(5,10,3,1));
	
	m_jbutton4.setActionCommand("JButton");
	m_jbutton4.setText("...");
	jpanel1.add(m_jbutton4,cc.xy(9,12));
	m_jbutton4.addMouseListener(new MouseAdapter(){
		public void mouseClicked(MouseEvent e) {
		    try {
			JFileChooser chooser = 
			    new JFileChooser(new File(".").getCanonicalPath());
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Please select a directory");
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    m_jformattedtextfield3.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		    } catch (IOException ex) {
			ExceptionWindow.getExceptionWindow(ex);
		    }
		}
	    });
      
	jlabel3 = new JLabel();
	jlabel3.setText("Name");
	jpanel1.add(jlabel3,cc.xy(3,10));
	
	jlabel4 = new JLabel();
	jlabel4.setText("Directory");
	jpanel1.add(jlabel4,cc.xy(3,12));
	
	jpanel1.add(m_jformattedtextfield3,cc.xywh(5,12,3,1));
	
	addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9,10 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13 });
	persistentEnable(false);
	return jpanel1;
    }

    public void actionPerformed(ActionEvent e) {
	if (m_jcombobox1.getSelectedIndex() == 0) {
	    persistentEnable(false);
	} else {
	    persistentEnable(true);
	}
    }

    public void persistentEnable(boolean state) {
	jlabel2.setEnabled(state);
	m_jformattedtextfield2.setEnabled(state);
	m_jbutton4.setEnabled(state);
	jlabel3.setEnabled(state);
	m_jformattedtextfield3.setEnabled(state);
	jlabel4.setEnabled(state);
	if (state) {
	    //System.out.println(m_jformattedtextfield2.getText());
	    if ((m_jformattedtextfield2.getText() == null) ||
		(m_jformattedtextfield2.getText().equals(""))) {
		String simp = m_jformattedtextfield1.getText();
		if ((simp != null)  &&
		    (!simp.equals(""))) {
		    try {
			SunFileFilter filter = 
			    new SunFileFilter();
			
			String fileExtension = 
			    filter.getExtension(new File(simp));
			
			simp = 
			    simp.substring(0,
					   simp.length() - 1 
					   - fileExtension.length());

			int seploc = simp.lastIndexOf(sep);
			seploc++;
			//	System.out.println(seploc);
			simp = simp.substring(seploc);
			m_jformattedtextfield2.setText(simp);
		    } catch (Exception ex) {
			//ExceptionWindow.getExceptionWindow(ex);
		    }
		}
	    }
	    if ((m_jformattedtextfield3.getText() == null) ||
		(m_jformattedtextfield3.getText().equals(""))) {
		try {
		    m_jformattedtextfield3.setText(new File(".").getCanonicalPath());
		} catch (Exception ex) {
		    //ExceptionWindow.getExceptionWindow(ex);
		}
	    }
	}
    }

    /**
     * Initializer
    */
    protected void initializePanel()
    {
	setLayout(new BorderLayout());
	add(createPanel(), BorderLayout.CENTER);
    }
}
