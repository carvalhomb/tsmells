package com.hp.hpl.guess.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;
import edu.umd.cs.piccolo.nodes.PText;

/*
 * FontDialag.java - created on 21.11.2003 by J-Domain
 * 
 * @author Michaela Behling
 */

public class FontDialog extends StandardDialog implements
      ListSelectionListener, ActionListener {

    private static final int MIN_SIZE = 6;
    private static final int MAX_SIZE = 72;
    private static final int DEFAULT_SIZE = 12;

    public static final Font DEFAULT_FONT = new Font( "SansSerif", Font.PLAIN,
						      12 );
    public static final Font BOLD_FONT = new Font( "SansSerif", Font.BOLD, 12 );
    
    private static final int DIALOG_ICON_SIZE = 48;
    private static final int DIALOG_TEXT_WIDTH = Toolkit.getDefaultToolkit()
	.getScreenSize().width / 4;

    private Font createdFont = DEFAULT_FONT;
    private boolean antialiased = false;
    private JLabel info;
    private JButton applyButton;
    private JPanel fontPanel;
    private JList fontList, styleList;
    private JComboBox sizeDropDown;
    private final JCheckBox[] checkBoxes;
    private JCheckBox antialiasBox = new JCheckBox( "Antialiased" );
    private JTextArea textArea = new JTextArea() {
	    
	    public void paint( Graphics g ) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
				     (antialiased ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
				      : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) );
		super.paint( g2 );
	    }
	};
    
    
    private static FontDialog singleton = null;

    private static PText node = null;

    public static FontDialog getDialog() {
	if (singleton == null) {
	    singleton = new FontDialog();
	}
	return(singleton);
    }

    public static FontDialog getDialog(PText n) {
	if (singleton == null) {
	    singleton = new FontDialog();
	}
	node = n;
	singleton.apply();
	singleton.setModal(true);
	singleton.show();
	return(singleton);
    }
	    

    private FontDialog() {
	super(null,"Font Selection");
	checkBoxes = null;
	createGui();
	update();
    }
    
    
    public final void setAntialiased( boolean aFlag ) {
	if ( antialiased != aFlag ) {
	    antialiased = aFlag;
	    antialiasBox.setSelected( aFlag );
	    invalidate();
	}
    }
    
    public final boolean isAntialiased() {
	return antialiased;
    }
    
    public final Font getFont() {
	if ( getResult() == APPROVE ) {
	    return createdFont;
	}
	return null;
    }
    
    public String getText() {
	return textArea.getText().trim();
    }
    
    public void apply() {
	if (node != null) {
	    node.setText(getText());
	    node.setFont(createdFont);
	    VisFactory.getFactory().getDisplay().repaint();
	}
    }

   protected void approve() {
       super.approve();
       if (node != null) {
	   node.setText(getText());
	   node.setFont(createdFont);
       }
       node = null;
   }

   protected void abort() {
       super.abort();
       node = null;
   }

    protected void cancel() {
	super.cancel();
	if (node != null) {
	    node.removeFromParent();
	}
	node = null;
    }
    
    private void createGui() {
	addRightButton( getApproveButton() );
	getRootPane().setDefaultButton( getApproveButton() );
	addRightButton( getCancelButton() );
	addButtonPanel();
	setUndecorated( false );
	setResizable( true );
	setDefaultBorder();
	//setModal(false);
	
	fontPanel = createFontPanel();
	main.add( fontPanel, BorderLayout.CENTER );
	
	antialiasBox.setSelected( antialiased );
	antialiasBox.addActionListener( this );
	fontList.setSelectedIndex( 0 );
	fontList.addListSelectionListener( this );
	sizeDropDown.setSelectedIndex( DEFAULT_SIZE - MIN_SIZE );
	sizeDropDown.addActionListener( this );
	styleList.setSelectedIndex( 0 );
	styleList.addListSelectionListener( this );
	
	textArea.setText( "Enter your text here..." );
	applyButton = new JButton( "Apply" );
	addLeftButton( applyButton );
	applyButton.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    apply();
		}
	    } );
	pack();
    }
    
    private JLabel createLabel( String text, int top, int leftSpace ) {
	JLabel label = new JLabel( text );
	label.setFont( BOLD_FONT );
	label.setHorizontalAlignment( SwingConstants.LEFT );
	label.setVerticalAlignment( SwingConstants.TOP );
	label.setBorder( new EmptyBorder( top, leftSpace, 0, 10 ) );
	return label;
    }

    private JPanel createFontPanel() {
	JPanel panel = new JPanel( new GridBagLayout() );
	GridBagConstraints gc = new GridBagConstraints();
	gc.gridx = 0;
	gc.gridy = 0;
	gc.anchor = GridBagConstraints.FIRST_LINE_START;
	gc.fill = GridBagConstraints.BOTH;
	gc.insets = new Insets( 2, 2, 2, 2 );

	gc.gridheight = 4;
	// font liste
	gc.weightx = 0.8;
	gc.weighty = 1.0;
	panel.add( createFontList(), gc );

	gc.gridheight = 1;
	gc.gridx++;
	gc.weightx = 0.2;
	gc.weighty = 0;
	JLabel sizeLabel = createLabel( "Size:", 0, 10 );
	panel.add( sizeLabel, gc );
	gc.gridx++;
	panel.add( createSizeComp(), gc );

	gc.gridx = 1;
	gc.gridy++;
	JLabel styleLabel = createLabel( "Style:", 0, 10 );
	panel.add( styleLabel, gc );
	gc.gridx++;
	panel.add( createStyleList(), gc );

	JPanel p = new JPanel( new GridLayout( 0, 1 ) );
	gc.gridx = 2;
	gc.gridy++;
	//p.add( antialiasBox );
	panel.add( p, gc );

	if ( checkBoxes != null ) {
	    final int boxes = checkBoxes.length;
	    for ( int i = 0; i < boxes; i++ ) {
		p.add( checkBoxes[i] );
	    }
	}

	gc.gridy++;
	panel.add( Box.createVerticalGlue(), gc );

	gc.gridx = 0;
	gc.gridy++;
	JLabel textLabel = createLabel( "Text:", 10, 0 );
	panel.add( textLabel, gc );

	// text area
	gc.weightx = 0.5;
	gc.weighty = 0.5;
	gc.gridy++;
	gc.gridwidth = 3;
	panel.add( createTextComp(), gc );

	gc.weightx = 0;
	gc.weighty = 0;
	gc.gridy++;
	info = createLabel( " ", 10, 0 );
	info.setBorder( null );
	info.setFont( DEFAULT_FONT );
	panel.add( info, gc );

	return panel;
    }

    public static Rectangle getStringWidth(
					   String aText,
					   Font f,
					   boolean antiAliased) {
	FontRenderContext rc = new FontRenderContext(null, antiAliased,
						     true);
	Rectangle2D r2d = f.getStringBounds(aText, rc);
	Rectangle r = new Rectangle();
	r.x = (int) Math.round(r2d.getX());
	r.y = (int) Math.round(r2d.getY());
	r.width = (int) Math.round(r2d.getWidth());
	r.height = (int) Math.round(r2d.getHeight());
	return r;
    }

    private JComponent createSizeComp() {
	ArrayList list = new ArrayList();
	for ( int i = MIN_SIZE; i < MAX_SIZE; i++ ) {
	    list.add( new SizeEntry( new Integer( i ) ) );
	}
	SizeEntry[] entries = new SizeEntry[list.size()];
	list.toArray( entries );
	sizeDropDown = new JComboBox( entries );
	sizeDropDown.setEditable( true );
	Rectangle r = getStringWidth("7777",
				     DEFAULT_FONT, true );
	Dimension dim = sizeDropDown.getPreferredSize();
	dim.width = r.width;
	sizeDropDown.setPreferredSize( dim );

	return sizeDropDown;
    }

    private JComponent createTextComp() {
	textArea.setFont( DEFAULT_FONT );
	JScrollPane pane = new JScrollPane( textArea );
	pane.setPreferredSize( new Dimension( 160, 80 ) );

	JPanel p = new JPanel( new BorderLayout() );
	p.add( pane, BorderLayout.CENTER );
	return p;
    }

    private JComponent createFontList() {
	GraphicsEnvironment env = GraphicsEnvironment
            .getLocalGraphicsEnvironment();
	String[] fontNames = env.getAvailableFontFamilyNames();
	final int count = fontNames.length;
	FontEntry[] entries = new FontEntry[count];
	for ( int i = 0; i < count; i++ ) {
	    entries[i] = new FontEntry( fontNames[i] );
	}
	fontList = new JList( entries );

	JScrollPane pane = new JScrollPane( fontList );

	return pane;
    }

    private JComponent createStyleList() {
	ArrayList list = new ArrayList();
	list.add( new StyleEntry( "Plain", Font.PLAIN ) );
	list.add( new StyleEntry( "Bold", Font.BOLD ) );
	list.add( new StyleEntry( "Italic", Font.ITALIC ) );
	list.add( new StyleEntry( "Bold+Italic", Font.ITALIC + Font.BOLD ) );

	StyleEntry[] entries = new StyleEntry[list.size()];
	list.toArray( entries );
	styleList = new JList( entries );
	styleList.setBorder( new SoftBevelBorder( SoftBevelBorder.LOWERED ) );

	return styleList;
    }

    private String getFontString( Font f ) {

	StringBuffer buf = new StringBuffer();
	buf.append( f.getName() );
	buf.append( ", " );
	buf.append( f.getSize() );
	buf.append( ", " );

	StringBuffer style = new StringBuffer();

	style.append( f.isPlain() ? "plain " : "" );
	if ( f.isBold() && f.isItalic() ) {
	    style.append( "bold+italic" );
	}
	else {
	    style.append( f.isBold() ? "bold " : "" );
	    style.append( f.isItalic() ? "italic " : "" );
	}
	buf.append( style.toString() );

	return buf.toString();
    }

    public static int asInt( String s ) {
	return asInt( s, 10 );
    }
    
    public static int asInt( String s, int radix ) {
      return asInt( s, radix, Integer.MIN_VALUE, Integer.MAX_VALUE );
    }
    
    public static int asInt( String s, int radix, int min, int max ) {
	if ( isNumber( s, radix, min, max ) ) {
	    return Integer.parseInt( s, radix );
	}
	return 0;
    }

   public static boolean isNumber( String aNumber, int radix ) {
      return isNumber( aNumber, radix, Integer.MIN_VALUE, Integer.MAX_VALUE );
   }

   public static boolean isNumber( String aNumber, int min, int max ) {
      return isNumber( aNumber, 10, min, max );
   }

   public static boolean isNumber( String aNumber, int radix, int min, int max )
 {
      if ( aNumber == null ) {
         return false;
      }
      String s = aNumber.trim();
      if ( s.length() == 0 ) {
         return false;
      }
      try {
         int i = Integer.parseInt( aNumber, radix );
         return (i >= min) && (i <= max);
      }
      catch ( NumberFormatException e ) {
         return false;
      }
   }

    protected void update() {
	Font f = DEFAULT_FONT;

	antialiased = antialiasBox.isSelected();
	FontEntry font = (FontEntry) fontList.getSelectedValue();
	SizeEntry size = null;
	Object o = sizeDropDown.getSelectedItem();
	if ( o instanceof SizeEntry ) {
	    size = (SizeEntry) o;
	}
	else if ( o instanceof String ) {
	    String s = (String) o;
	    if ( isNumber( s, 1, 300 ) ) {
		size = new SizeEntry( new Integer( asInt( s ) ) );
	    }
	}
	StyleEntry style = (StyleEntry) styleList.getSelectedValue();
	if ( (font != null) && (size != null) && (style != null) ) {
	    try {
		f = new Font( font.toString(), style.style, size.size.intValue() );
	    }
	    catch ( ArithmeticException e ) {

	    }
	}
	createdFont = f;
	textArea.setFont( f );
	info.setText( getFontString( f ) );
	repaint();
    }


    // FontEntry
    private final class FontEntry {
	public final String fontName;

	public FontEntry( String aFont ) {
	    fontName = aFont;
	}

	public String toString() {
	    return fontName;
	}
    }


    //	SizeEntry
    private final class SizeEntry {
	public final Integer size;

	public SizeEntry( Integer aSize ) {
	    size = aSize;
	}

	public String toString() {
	    return String.valueOf( size.intValue() );
	}
    }


    // StyleEntry
    private final class StyleEntry {
	public final int style;
	public final String name;

	public StyleEntry( String aName, int aStyle ) {
	    name = aName;
	    style = aStyle;
	}

	public String toString() {
	    return name;
	}
    }

    public void valueChanged( ListSelectionEvent e ) {
	update();
    }

    public void actionPerformed( ActionEvent e ) {
	update();
    }

    public final void setFontBackground( Color colour ) {
	textArea.setOpaque( colour.getAlpha() == 255 );
	textArea.setBackground( colour );
    }

    public final void setFontForeground( Color colour ) {
	textArea.setForeground( colour );
    }

}
