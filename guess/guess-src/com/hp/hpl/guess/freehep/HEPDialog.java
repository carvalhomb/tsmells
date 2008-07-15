package com.hp.hpl.guess.freehep;

import com.hp.hpl.guess.piccolo.GFrame;
import com.hp.hpl.guess.Guess;

import org.freehep.util.export.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.geom.*;
import javax.swing.event.*;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;

import org.freehep.util.UserProperties;

import org.freehep.graphicsio.gif.GIFExportFileType;
import org.freehep.graphicsio.jpg.JPGExportFileType;
import org.freehep.graphicsio.png.PNGExportFileType;
import org.freehep.graphicsio.ppm.PPMExportFileType;
import org.freehep.graphicsio.raw.RawExportFileType;
import org.freehep.graphicsio.cgm.CGMExportFileType;
import org.freehep.graphicsio.emf.EMFExportFileType;
import org.freehep.graphicsio.java.JAVAExportFileType;
import org.freehep.graphicsio.pdf.PDFExportFileType;
import org.freehep.graphicsio.ps.EPSExportFileType;
import org.freehep.graphicsio.ps.PSExportFileType;
import org.freehep.graphicsio.svg.SVGExportFileType;
import org.freehep.graphicsio.swf.SWFExportFileType;

/**
 * An "Export" dialog for saving components as graphic files.
 *
 * @author tonyj, modified by eytan adar for GUESS
 * @version $Id: HEPDialog.java,v 1.4 2006/05/31 07:12:15 eytanadar Exp $
 */
public class HEPDialog extends JOptionPane
{
    private static final String rootKey = HEPDialog.class.getName();
    private static final String SAVE_AS_TYPE = rootKey +".SaveAsType";
    private static final String SAVE_AS_FILE = rootKey +".SaveAsFile";
    private static Vector list = new Vector();
    private static HashMap efts = new HashMap();

    static {
       addAllExportFileTypes();
    }

   /**
    * Set the Properties object to be used for storing/restoring
    * user preferences. If not called user preferences will not be
    * saved.
    * @param props The Properties to use for user preferences
    */
   public void setUserProperties(Properties properties)
   {
      props = properties;
   }

   /**
    * Register an export file type.
    */
   private static void addExportFileType(ExportFileType fileType, int tp)
   {
      list.addElement(fileType);
      efts.put(fileType,new Integer(tp));
   }

   private static void addAllExportFileTypes()
   {
       //List exportFileTypes = ExportFileType.getExportFileTypes();
       //Collections.sort(exportFileTypes);
       //Iterator iterator = exportFileTypes.iterator();
       //while(iterator.hasNext()) {
       //   ExportFileType type = (ExportFileType)iterator.next();
       //   addExportFileType(type);
       //}
       addExportFileType(new JPGExportFileType(),HEPWriter.JPG);
       addExportFileType(new PNGExportFileType(),HEPWriter.PNG);
       addExportFileType(new PPMExportFileType(),HEPWriter.PPM);
       addExportFileType(new RawExportFileType(),HEPWriter.RAW);
       addExportFileType(new CGMExportFileType(),HEPWriter.CGM);
       addExportFileType(new EMFExportFileType(),HEPWriter.EMF);
       addExportFileType(new PDFExportFileType(),HEPWriter.PDF);
       addExportFileType(new EPSExportFileType(),HEPWriter.EPS);
       addExportFileType(new PSExportFileType(),HEPWriter.PS);
       addExportFileType(new SVGExportFileType(),HEPWriter.SVG);
       addExportFileType(new SWFExportFileType(),HEPWriter.SWF);
       addExportFileType(new GIFExportFileType(),HEPWriter.GIF);
       addExportFileType(new JAVAExportFileType(),HEPWriter.JAVA);
    }

   /**
    * Creates a new instance of ExportDialog with all the standard
    * export filetypes.
    */
   public HEPDialog()
   {
      this(null);
   }

    public void updateSize() {
	double rescale = 1.0;
	try {
	    rescale = Double.parseDouble(scale.getText());
	} catch (Exception ex) {
	}

	//System.out.println("os: " + originalSize);

	if (originalSize != null) {
	    imageSize.setText("Output image size = " + 
			      (int)(originalSize.getWidth() * rescale) + 
			      " x " + 
			      (int)(originalSize.getHeight() * rescale) + 
			      " px");
	}
    }

   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:4DLU:NONE,FILL:80PX:NONE,FILL:4DLU:NONE,FILL:55PX:NONE,FILL:4DLU:NONE,FILL:130PX:NONE,FILL:4DLU:NONE,FILL:93PX:NONE,FILL:4DLU:NONE","CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("Rescale image:");
      jpanel1.add(jlabel1,cc.xy(2,7));

      scale = new JTextField();
      scale.setName("scale");
      scale.setText(""+scaling);
      jpanel1.add(scale,cc.xy(4,7));
      scale.addCaretListener(new CaretListener() {
	      public void caretUpdate(CaretEvent e) {
		  updateSize();
	      }
	  });
			      
      imageSize = new JLabel();
      imageSize.setText("Output image size = ");
      jpanel1.add(imageSize,cc.xywh(6,7,3,1));

      file = new JTextField();
      file.setName("file");
      jpanel1.add(file,cc.xywh(2,2,5,1));

      type = new JComboBox(list);
      type.setName("type");
      jpanel1.add(type,cc.xywh(2,4,5,1));

      browse = new JButton();
      browse.setActionCommand("Browse...");
      browse.setName("browse");
      browse.setText("Browse...");
      jpanel1.add(browse,cc.xy(8,2));

      advanced = new JButton();
      advanced.setActionCommand("Options...");
      advanced.setName("advanced");
      advanced.setText("Options...");
      jpanel1.add(advanced,cc.xy(8,4));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9 },new int[]{ 1,2,3,4,5,6,7,8 });
      return jpanel1;
   }

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
    * Creates a new instance of ExportDialog with all the standard export filetypes.
    * @param creator The "creator" to be written into the header of the file (may be null)
    */
   public HEPDialog(String creator)
   {

      super(null,JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
      this.creator = creator;

      try
      {
	  if (baseDir == null)
	      baseDir = System.getProperty("user.home");
      }
      catch (SecurityException x) { trusted = false; }

      ButtonListener bl = new ButtonListener();


      JPanel panel = new JPanel();

      panel.setLayout(new BorderLayout());
      panel.add(createPanel(),BorderLayout.CENTER);
      type.setMaximumRowCount(16);      // rather than 8

      browse.addActionListener(bl);
      advanced.addActionListener(bl);
      type.setRenderer(new SaveAsRenderer());
      type.addActionListener(bl);
      setMessage(panel);
      //if (addAllExportFileTypes) addAllExportFileTypes();
   }

    Rectangle2D originalSize = null;
    
    boolean fullImage = true;

    public void showHEPDialog(Component parent, 
			      String title, 
			      Component target, 
			      String defFile) {
	showHEPDialog(parent,title,target,defFile,false);
    }

    /**
     * Show the dialog.
     * @param parent The parent for the dialog
     * @param title The title for the dialog
     * @param target The component to be saved.
     * @param defFile The default file name to use.
     */
    public void showHEPDialog(Component parent, 
			      String title, 
			      Component target, 
			      String defFile,
			      boolean fullImage) {
	if (target instanceof GFrame) {
	    this.gframe = (GFrame)target;
	    originalSize = gframe.getFullImageSize();
	} else {
	    this.component = target;
	}
	this.fullImage = fullImage;

	if (fullImage) {
	    scale.setEnabled(true);
	} else {
	    scale.setEnabled(false);
	}

	updateSize();
	
	if (list.size() > 0) type.setSelectedIndex(0);
	String dType = props.getProperty(SAVE_AS_TYPE);
	if (dType != null)
	    {
		for (int i=0; i<list.size(); i++)
		    {
			ExportFileType saveAs = (ExportFileType) list.elementAt(i);
			if (saveAs.getFileFilter().getDescription().equals(dType))
			    {
				type.setSelectedItem(saveAs);
				break;
			    }
		    }
	    }
	advanced.setEnabled(currentType() != null && 
			    currentType().hasOptionPanel());
	if (trusted)
	    {
		//String saveFile = props.getProperty(SAVE_AS_FILE);
		//if (saveFile != null) {
		//baseDir = new File(saveFile).getParent();
		//defFile = saveFile;
		//} else {
		defFile = baseDir+File.separator+defFile;
		//}
		File f = new File(defFile);
		if (currentType() != null) f = currentType().adjustFilename(f, props);
		file.setText(f.toString());
	    }
	else
	    {
		file.setEnabled(false);
		browse.setEnabled(false);
	    }
	
	JDialog dlg = createDialog(parent,title);
	dlg.pack();
	dlg.show();
    }


   private ExportFileType currentType()
   {
      return (ExportFileType) type.getSelectedItem();
   }
   /**
    * Called to open a "file browser". Override this method to provide
    * special handling (e.g. in a WebStart app)
    * @return The full name of the selected file, or null if no file selected
    */
   protected String selectFile()
   {
      JFileChooser dlg = new JFileChooser();
      String f = file.getText();
      if (f != null) dlg.setSelectedFile(new File(f));
      dlg.setFileFilter(currentType().getFileFilter());
      if (dlg.showDialog(this, "Select") == dlg.APPROVE_OPTION) {
        return dlg.getSelectedFile().getAbsolutePath();
      } else {
        return null;
      }
   }

   /**
    * Called to acually write out the file.
    * Override this method to provide special handling (e.g. in a WebStart app)
    * @return true if the file was written, or false to cancel operation
    */
    protected boolean writeFile(ExportFileType t) throws IOException
    {
	File f = new File(file.getText());
	if (f.exists())
	    {
		int ok = 
		    JOptionPane.showConfirmDialog(this,
						  "Replace existing file?");
		if (ok != JOptionPane.OK_OPTION) return false;
	    }

	if (gframe != null) {
	    if (fullImage) {
		gframe.writeFullImage(file.getText(),getEFType(t),scaling,props);
	    } else {
		HEPWriter.export(file.getText(),gframe,getEFType(t),props);
	    }
	} else {
	    HEPWriter.export(file.getText(),component,getEFType(t),props);
	}

	//t.exportToFile(f,component,this,props,creator);
	props.put(SAVE_AS_FILE,file.getText());
	props.put(SAVE_AS_TYPE,currentType().getFileFilter().getDescription());
	baseDir = f.getParent();
	return true;
    }

    private int getEFType(ExportFileType t) {
	Integer i = (Integer)efts.get(t);
	if (i == null) {
	    return(HEPWriter.PNG);
	} else {
	    return(i.intValue());
	}
    }

   public void setValue(Object value)
   {
      if (value instanceof Integer && ((Integer) value).intValue() == OK_OPTION)
      {
         try
         {
            if (!writeFile(currentType())) return;
         }
         catch (IOException x)
         {
            JOptionPane.showMessageDialog(this,x,"Error...",
					  JOptionPane.ERROR_MESSAGE);
            return;
         }
      }
      super.setValue(value);
   }
    
    private String creator;
    private JButton browse = null;
    private JButton advanced = null;
    private JTextField file = null;
    private JComboBox type = null;
    private GFrame gframe = null;
    private Component component = null;
    private static double scaling = 1;
    private boolean trusted = true;
    private Properties props = new Properties();
    private static String baseDir = null;
    private JTextField scale = null;
    private JLabel imageSize = null;

   private class ButtonListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         Object source = e.getSource();
         if (source == browse)
         {
            String fileName = selectFile();
            if (fileName != null) {
                if (currentType() != null) {
                    File f = currentType().adjustFilename(new File(fileName),
							  props);
                    file.setText(f.getPath());
                } else {
                    file.setText(fileName);
                }
            }
         }
         else if (source == advanced)
         {
            JPanel panel = currentType().createOptionPanel(props);
            int rc = 
		JOptionPane.showConfirmDialog(HEPDialog.this,panel,
					      "Options for "+
					      currentType().getDescription(),
					      JOptionPane.OK_CANCEL_OPTION,
					      JOptionPane.PLAIN_MESSAGE);
            if (rc == JOptionPane.OK_OPTION) {
                currentType().applyChangedOptions(panel, props);
                File f1 = new File(file.getText());
                File f2 = currentType().adjustFilename(f1, props);
                if (!f1.equals(f2) && file.isEnabled()) 
		    file.setText(f2.toString());
            }
         }
         else if (source == type)
         {
            advanced.setEnabled(currentType().hasOptionPanel());
            File f1 = new File(file.getText());
            File f2 = currentType().adjustFilename(f1, props);
            if (!f1.equals(f2) && file.isEnabled()) 
		file.setText(f2.toString());
         }
      }
   }

   private static class SaveAsRenderer extends DefaultListCellRenderer
   {
      public Component getListCellRendererComponent(JList list,
                                              Object value,
                                              int index,
                                              boolean isSelected,
                                              boolean cellHasFocus)
      {
         super.getListCellRendererComponent(list,value,index,
					    isSelected,cellHasFocus);
         if (value instanceof ExportFileType)
         {
            this.setText(((ExportFileType) value).getFileFilter().getDescription());
         }
         return this;
      }
   }
}



