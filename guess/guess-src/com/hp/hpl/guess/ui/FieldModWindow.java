package com.hp.hpl.guess.ui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.*;
import com.hp.hpl.guess.*;
import java.sql.Types;

public class FieldModWindow extends JFrame implements TreeSelectionListener {

    final DefaultListModel model = new DefaultListModel();
    JTree attributeTree = new JTree(getTreeRoot());
    JTextField newValue = new JTextField();
    JList applyList = new JList(model);
    JButton okB = new JButton();
    JButton cancelB = new JButton();
    JButton selectB = new JButton();
    Collection nodes = null;
    Collection edges = null;

    boolean showingNodes = false;
    boolean showingEdges = false;

    Field selectedField = null;

    private static FieldModWindow singleton = null;
    
    public static FieldModWindow getFieldModWindow() {
	if (singleton == null) {
	    singleton = new FieldModWindow();
	} else {
	    singleton.setNE(Guess.getGraph().getNodes(),
			    Guess.getGraph().getEdges());
	}
	return(singleton);
    }

    public static FieldModWindow getFieldModWindow(Collection n, 
						   Collection e) {
	if (singleton == null) {
	    singleton = new FieldModWindow(n,e);
	} else {
	    singleton.setNE(n,e);
	}
	return(singleton);
    }

    public void setNE(Collection nodes, Collection edges) {
	this.nodes = nodes;
	this.edges = edges;
	model.removeAllElements();
	if (selectedField != null) {
	    showingNodes = false;
	    showingEdges = false;
	    valueChanged(null);
	}
    }

    private FieldModWindow() {
	this(Guess.getGraph().getNodes(),Guess.getGraph().getEdges());
    }

    /**
     * Default constructor
     */
    private FieldModWindow(Collection nodes, Collection edges)
    {
	super("Field Editor");
	setNE(nodes,edges);
	setSize(440, 220);
	setLocation(100, 100);
	initializePanel();
	//frame.getContentPane().add(new FieldModWindow());
	setVisible(true);
	getRootPane().setDefaultButton(cancelB);
	okB.setEnabled(false);

	attributeTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	attributeTree.addTreeSelectionListener(this);

	addWindowListener( new WindowAdapter()
	    {
		public void windowClosing( WindowEvent evt )
		{
		    singleton = null;
		    dispose();
		}
	    });
	applyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

	okB.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
		    applyToList();
		}
	    });

	cancelB.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
		    singleton = null;
		    dispose();
		}
	    });

	selectB.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent ev) {
		    applyList.getSelectionModel().setSelectionInterval(0,
								       model.getSize());    
		    applyList.repaint();
		}
	    });
    }
    
    public Object newValueData() {
	if ((selectedField.getSQLType() == Types.INTEGER) ||
	    (selectedField.getSQLType() == Types.TINYINT) ||
	    (selectedField.getSQLType() == Types.SMALLINT) ||
	    (selectedField.getSQLType() == Types.BIGINT)) {
	    return(new Integer((String)newValue.getText()));
	} else if (selectedField.getSQLType() == Types.BOOLEAN) {
	    return(new Boolean((String)newValue.getText()));
	} else if (selectedField.isNumeric()) {
	    return(new Double((String)newValue.getText()));
	} else {
	    return(newValue.getText());
	}
    }

    public void applyToList() {

	int start = -1;
	int end = -1;
	try {
	    start = applyList.getSelectionModel().getMinSelectionIndex();
	    end = applyList.getSelectionModel().getMaxSelectionIndex();
	    if (end >= model.getSize()) {
		end = model.getSize() - 1;
	    }
	    if ((start == -1) || (end == -1)) {
		return;
	    }
	    for (int i = start ; i <= end ; i++) {
		if (applyList.isSelectedIndex(i)) {
		    GraphElement o = (GraphElement)model.getElementAt(i);
		    o.__setattr__(selectedField.getName(),newValueData());
		    //System.out.println(o);
		}
	    }
	} catch (Throwable e) {
	    JOptionPane.showMessageDialog(this,
					  "Error setting value " + 
					  e.toString() + " range: ("+start + 
					  "-" + end + ")",
					  "Error",
					  JOptionPane.ERROR_MESSAGE);

	    ExceptionWindow.getExceptionWindow(e);
	}
    }

   /**
    * Main method for panel
    */
   public static void main(String[] args)
   {
      // JFrame frame = new JFrame();
//       frame.setSize(600, 400);
//       frame.setLocation(100, 100);
//       frame.getContentPane().add(new FieldModWindow());
//       frame.setVisible(true);

//       frame.addWindowListener( new WindowAdapter()
//       {
//          public void windowClosing( WindowEvent evt )
//          {
//             System.exit(0);
//          }
//       });
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
    * @param imageName the package and name of the file to load
    * relative to the CLASSPATH
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
         e.printStackTrace();
      }
      throw new IllegalArgumentException( "Unable to load image: " + imageName );
   }

    public void valueChanged(TreeSelectionEvent e) {
	DefaultMutableTreeNode node =
	    (DefaultMutableTreeNode)attributeTree.getLastSelectedPathComponent();

	selectedField = null;
	okB.setEnabled(false);

	if (node == null)
	    return;

	Object o = node.getUserObject();
	if (o instanceof Field) {
	    if (((Field)o).getType() == Field.NODE) {
		if (!showingNodes) {
		    loadData(nodes);
		}
		showingNodes = true;
		showingEdges = false;
	    } else {
		if (!showingEdges) {
		    loadData(edges);
		}
		showingNodes = false;
		showingEdges = true;
	    }
	    selectedField = (Field)o;
	    okB.setEnabled(true);
	}
    }
    
    public void loadData(Collection c) {
	model.removeAllElements();
	Iterator it = c.iterator();
	int last = 0;
	while(it.hasNext()) {
	    model.addElement(it.next());
	    last++;
	}
	applyList.getSelectionModel().setSelectionInterval(0,
							   model.getSize());
	applyList.repaint();
    }

    public DefaultMutableTreeNode getTreeRoot() {
	DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode("Fields");
	DefaultMutableTreeNode nodeB = new DefaultMutableTreeNode("Node");

	ArrayList al = new ArrayList();
	al.addAll(Guess.getGraph().getNodeSchema().allFields());
	Collections.sort(al);
	Iterator it = al.iterator();
	while(it.hasNext()) {
	    Field f = (Field)it.next();
	    DefaultMutableTreeNode t = 
		new DefaultMutableTreeNode(f.getName());
	    t.setUserObject(f);
	    nodeB.add(t);
	}

	DefaultMutableTreeNode edgeB = new DefaultMutableTreeNode("Edge");
	Collection nEs = Guess.getGraph().getEdgeSchema().fieldNames();
	al = new ArrayList();
	al.addAll(Guess.getGraph().getEdgeSchema().allFields());
	Collections.sort(al);
	it = al.iterator();
	while(it.hasNext()) {
	    Field f = (Field)it.next();
	    DefaultMutableTreeNode t = 
		new DefaultMutableTreeNode(f.getName());
	    t.setUserObject(f);
	    edgeB.add(t);
	}

	dmtn.add(nodeB);
	dmtn.add(edgeB);
	return(dmtn);
    }

   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:71PX:NONE,FILL:4DLU:NONE,FILL:153PX:NONE,FILL:4DLU:NONE,FILL:69PX:NONE,FILL:4DLU:NONE,FILL:88PX:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:88PX:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      attributeTree.setName("attributeTree");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(attributeTree);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xywh(2,3,3,3));

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("Select Field...");
      jpanel1.add(jlabel1,cc.xy(2,2));

      JLabel jlabel2 = new JLabel();
      jlabel2.setText("New value:");
      jlabel2.setHorizontalAlignment(JLabel.RIGHT);
      jpanel1.add(jlabel2,cc.xy(2,8));

      newValue.setName("newValue");
      jpanel1.add(newValue,cc.xy(4,8));

      JLabel jlabel3 = new JLabel();
      jlabel3.setText("Apply to...");
      jpanel1.add(jlabel3,cc.xy(6,2));

      applyList.setName("applyList");
      JScrollPane jscrollpane2 = new JScrollPane();
      jscrollpane2.setViewportView(applyList);
      jscrollpane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane2,cc.xywh(6,3,3,1));

      okB.setActionCommand("Apply");
      okB.setName("okB");
      okB.setText("Apply");
      jpanel1.add(okB,cc.xy(6,8));

      cancelB.setActionCommand("Done");
      cancelB.setName("cancelB");
      cancelB.setText("Done");
      jpanel1.add(cancelB,cc.xy(8,8));

      selectB.setActionCommand("Select All");
      selectB.setText("Select All");
      jpanel1.add(selectB,cc.xywh(6,5,3,1));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9 },new int[]{ 1,2,3,4,5,6,7,8,9 });
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
