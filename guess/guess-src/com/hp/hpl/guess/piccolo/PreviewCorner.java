package com.hp.hpl.guess.piccolo;

/*
 * originally by:
 * David Bismut, david.bismut@gmail.com
 * Intern, SETLabs, Infosys Technologies Ltd. May 2004 - Jul 2004
 * http://www.infosys.com/instepweb
 * Ecole des Mines de Nantes, France
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.PBounds;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

 /**
  * This is a button which is designed to be the corner component of a
  * <code>JScrollPane</code>. It triggers a popup menu which holds a 
  * scaled image of the component contained inside the 
  * <code>JScrollPane</code>.
  */
  
public class PreviewCorner extends JButton implements MouseListener {
    
    private String _corner;
    private PreviewPopup _previewPopup;

    public PreviewCorner(GFrame myF,
			 ImageIcon zoomIcon,
			 boolean doCloseAfterClick,
			 String corner) {
	
	super(zoomIcon);
	this._corner = corner;
	
	_previewPopup = new PreviewPopup(myF, doCloseAfterClick);
	
	setToolTipText("View a miniature of scrollpane content and navigate");
	
	// The action listener is used to trigger the popup menu.
	addMouseListener(this);
	setBorder(null);
    }
	
    public PreviewCorner(GFrame myF,
			 ImageIcon zoomIcon,
			 String corner) {
	
	this(myF, zoomIcon, false, corner);
    }

    public void mouseClicked(MouseEvent e) {
	_previewPopup.showUpInCorner(this, _corner,
				     e.getX(),
				     e.getY());
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

}

class PreviewPopup
    extends JPopupMenu
    implements MouseListener, MouseMotionListener {

    //    private static Robot robot = null;
    
    private PCamera _camera;
    
    private JLabel _zoomWindow; // the JLabel containing the scaled image
    
    private JLabel _cursorLabel; // the JLabel mimicking the fake rectangle cursor
    
    // This component will hold both JLabels _zoomWindow and _cursorLabel, 
    // the latter on top of the other.
    private JLayeredPane _layeredPane;
    
    private int _iconWidth;
    private int _iconHeight;
    
    private boolean _doCloseAfterClick;

    private Rectangle2D _origin = null;

    double _ratio;
    
    // DELTA is the space between the scroll pane and the preview popup menu.
    private static int DELTA = 5;
    
    // SCALEFACTOR is the scale factor between the previewed component 
    // and the viewport.
    private static double SCALEFACTOR = 4;
    
    private GFrame _gf = null;

    public PreviewPopup(GFrame gf, boolean doCloseAfterClick) {
	_camera = gf.getGCamera();
	_gf = gf;
	this.setBorder(BorderFactory.createEtchedBorder());
	
	_doCloseAfterClick = doCloseAfterClick;
	
	//_scrollPane = scrollPane;
	//_viewPort = _scrollPane.getViewport();
	
	_zoomWindow = new JLabel();
	_cursorLabel = createCursor();
	
	_layeredPane = new JLayeredPane();
	
	_layeredPane.add(_zoomWindow, new Integer(0));
	_layeredPane.add(_cursorLabel, new Integer(1));
	
	
	// Creates a blank transparent cursor to be used as the cursor of
	// the popup menu.
	BufferedImage bim =
	    new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	setCursor(
		  getToolkit().createCustomCursor(bim, (new Point(0, 0)), "HiddenM"));
	
	this.add(_layeredPane);
	
	// Adds the mouse input listeners to the _layeredPane to scroll the
	// viewport and to move the fake cursor (_cursorLabel).
	_layeredPane.addMouseListener(this);
	_layeredPane.addMouseMotionListener(this);

	//try {
	//   robot = new Robot();
	//} catch (Exception ex) {};

    }


    private Image componentImage = null;
    
    private long lastUpdate = System.currentTimeMillis();
    
    /**
     * By default, the right corner of a popup menu is positionned at the right
     * of a mouse click. What we want is to have the preview popup menu 
     * positionned <i>inside</i> the scroll pane, near the corner component. 
     * The purpose of this method is to display the scaled image of
     * the component of the scroll pane, and to calculate the correct position
     * of the preview popup menu.
     */
    
    public void showUpInCorner(Component c, String corner,
			       int mouseX, int mouseY) {

	Image original = null;
	if ((componentImage == null) || 
	    (lastUpdate < PFactory.getLastUpdate())) {
	    lastUpdate = System.currentTimeMillis();
	    _origin = _gf.getFullImageSize();
	    original = _gf.getFullImage(_origin,200,200);
	    //System.out.println("image size: " + original.getWidth(this) + " " + original.getHeight(this));
	    SCALEFACTOR = (1/Math.min(200/_origin.getWidth(),
				      200/_origin.getHeight()));
	    //System.out.println("scalefactor: " + SCALEFACTOR);
	    if (original == null)
		return;
	}

	_ratio = 1 / SCALEFACTOR;

	if (original != null) {
	    componentImage = original;
	}

	// Converts the Image to an ImageIcon to be used with a JLabel.
	ImageIcon componentIcon = new ImageIcon(componentImage);
	
	_iconWidth = componentIcon.getIconWidth();
	_iconHeight = componentIcon.getIconHeight();
	
	_zoomWindow.setIcon(componentIcon);
	
	_zoomWindow.setBounds(0, 0, _iconWidth, _iconHeight);
	
	PBounds pb = _camera.getViewBounds();

	double cursorWidth = pb.getWidth() * _ratio;
	double cursorHeight = pb.getHeight() * _ratio;

	//System.out.println("image size: " + _iconWidth + " " + _iconHeight);
	//System.out.println("SCALEFACTOR: " + SCALEFACTOR);
	//System.out.println("ratio: " + _ratio);
	//System.out.println("cursor width: " + cursorWidth + " " + cursorHeight);
	_cursorLabel.setBounds(0, 0, (int)cursorWidth, (int)cursorHeight);
	//_cursorLabel.setBounds(0, 0, 50,50);
	
	_layeredPane.setPreferredSize(new Dimension(_iconWidth, _iconHeight));

	//System.out.println("click at: " + mouseX + " " + mouseY);

	//double startX = Math.max(0,(pb.getX() - _origin.getX()) * _ratio);
	//double startY = Math.max(0,(pb.getY() - _origin.getY()) * _ratio);
	//System.out.println("cursor should be at: " + startX + " " + startY);

	int dx = componentIcon.getIconWidth() + DELTA;
	int dy = componentIcon.getIconHeight() + DELTA;
	
	// Shows the popup menu at the right place.
	this.show(c, dx, dy);
	
	//int origx = c.getX() + c.getWidth() + DELTA;
	//int origy = c.getY() + c.getHeight() + DELTA;
	//System.out.println(origx + " " + origy); 
	//robot.mouseMove(origx + 10, origy + 10);
    }
    
    public JLabel createCursor() {
	JLabel label = new JLabel();
	label.setBorder(BorderFactory.createLineBorder(Color.black));
	label.setVisible(false);
	return label;
    }
    
    public void mouseClicked(MouseEvent e) {
    }
    
    public void mouseEntered(MouseEvent e) {
	// When the mouse enters the preview popup menu, set the visibility
	// of the fake cursor to true.
	_cursorLabel.setVisible(true);
    }
    
    public void mouseExited(MouseEvent e) {
	// When the mouse exits the preview popup menu, set the visibility
	// of the fake cursor to false.
	_cursorLabel.setVisible(false);
    }
    
    public void mousePressed(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
	// When the mouse is released, set the visibility of the preview
	// popup menu to false only if doCloseAfterClick is set to true.
	if(_doCloseAfterClick) {
	    this.setVisible(false);
	    _cursorLabel.setVisible(false);
	}
    }
    
    public void mouseDragged(MouseEvent e) {
	moveCursor(e.getX(), e.getY());
	scrollViewPort();
    }
    
    public void mouseMoved(MouseEvent e) {
	moveCursor(e.getX(), e.getY());
	scrollViewPort();
    }
    
    /**
     * Centers the fake cursor (_cursorLabel) position on the coordinates
     * specified in the parameters. 
     */
    private void moveCursor(int x, int y) {
	int dx = x - _cursorLabel.getWidth() / 2;
	int dy = y - _cursorLabel.getHeight() / 2;
	_cursorLabel.setLocation(dx, dy);
    }
    
    /**
     * Scrolls the viewport according to the fake cursor position in the
     * preview popup menu.
     */
    private void scrollViewPort() {
	Point cursorLocation = _cursorLabel.getLocation();
	double dx = cursorLocation.getX();
	double dy = cursorLocation.getY();

	dx = dx / _ratio + _origin.getX();
	dy = dy / _ratio + _origin.getY();

	PBounds pb = _camera.getViewBounds();
	Rectangle2D r2d = new Rectangle2D.Double(dx,dy,
					       pb.getWidth(),
					       pb.getHeight());

	//System.out.println(r2d);

	_camera.animateViewToPanToBounds(r2d,0);
	//System.out.println(dx + " " + dy);
	//	((JComponent) _gf).scrollRectToVisible(
	//							     new Rectangle(dx, dy, (int)_camera.getWidth(), (int)_camera.getHeight()));
    }
}
