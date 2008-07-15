package com.hp.hpl.guess.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/*
 * StandardDialog.java - created on 17.11.2003
 * 
 * @author Michaela Behling
 */

public class StandardDialog extends JDialog implements KeyEventDispatcher,
      WindowFocusListener {

   public static final int APPROVE = 1;
   public static final int CANCEL = 0;
   public static final int ABORT = 2;

   private JButton approveButton = null;
   private final ArrayList buttonList = new ArrayList();
   private final JPanel buttons = new JPanel( new BorderLayout( 0, 0 ) );
   private JButton cancelButton = null;
   private JButton abortButton = null;
   private boolean disposeOnClose = true;

   private JComponent firstFocusComponent = null;
   private final boolean ignoreEscapeKey;

   private boolean ignoreKeys = true;

   private final JPanel leftButtons = new JPanel( new FlowLayout(
         FlowLayout.LEFT, 0, 0 ) );
   public final JPanel main = new JPanel( new BorderLayout( 0,
         0 ) );
   private int result = CANCEL;
   private final JPanel rightButtons = new JPanel( new FlowLayout(
         FlowLayout.RIGHT, 0, 0 ) );

   public StandardDialog( JFrame parent, String title, boolean ignoreESCKey ) {
      super(parent,title );

      ignoreEscapeKey = ignoreESCKey;
      //setModal( false );
      setUndecorated( true );
      setContentPane( main );

      buttons.add( leftButtons, BorderLayout.WEST );
      buttons.add( rightButtons, BorderLayout.EAST );
      buttons.setBorder( new EmptyBorder( 15, 0, 0, 0 ) );
   }

   public StandardDialog( JDialog parent, String title, boolean ignoreESCKey ) {
      super(parent,title );

      ignoreEscapeKey = ignoreESCKey;
      //setModal( false );
      setUndecorated( true );
      setContentPane( main );

      buttons.add( leftButtons, BorderLayout.WEST );
      buttons.add( rightButtons, BorderLayout.EAST );
      buttons.setBorder( new EmptyBorder( 15, 0, 0, 0 ) );
   }

   public JPanel getButtonPanel() {
      return buttons;
   }

   public JPanel getLeftButtonPanel() {
      return leftButtons;
   }

   public JPanel getRightButtonPanel() {
      return rightButtons;
   }

   public StandardDialog( JFrame parent, String title ) {
      this( parent, title, false );
   }

   public final void addButtonPanel() {
      main.add( buttons, BorderLayout.SOUTH );
   }

   public final void addLeftButton( JButton button ) {
      buttonList.add( button );
      leftButtons.add( button );
      leftButtons.add( Box.createHorizontalStrut( 4 ) );
   }

   public final void addRightButton( JButton button ) {
      buttonList.add( button );
      rightButtons.add( Box.createHorizontalStrut( 4 ) );
      rightButtons.add( button );
   }

   protected void approve() {
      result = APPROVE;
      close();
   }

   protected void abort() {
      result = ABORT;
      close();
   }

   protected void cancel() {
      result = CANCEL;
      close();
   }

   public final void center() {
      Dimension screen;
      Point p;
      if ( hasValidParent() ) {
         screen = getParent().getSize();
         p = getParent().getLocation();
      }
      else {
         screen = Toolkit.getDefaultToolkit().getScreenSize();
         p = new Point( 0, 0 );
      }
      Dimension dim = getPreferredSize();
      p.x = Math.max( 0, p.x + (screen.width / 2) - (dim.width / 2) );
      p.y = Math.max( 0, p.y + (screen.height / 2) - (dim.height / 2) );
      setLocation( p.x, p.y );
   }

   public final void close() {
      setVisible( false );
      if ( disposeOnClose ) {
         dispose();
      }
   }

   public final boolean dispatchKeyEvent( KeyEvent e ) {
      if ( !isActive() ) {
         return false;
      }
      if ( e.getID() == KeyEvent.KEY_TYPED ) {
         if ( (e.getKeyChar() == 0x1b) && (e.getModifiers() == 0) ) { // ESC
            if ( !ignoreEscapeKey ) {
               cancel();
               return true;
            }
         }
      }
      return handleKey( e );
   }

   public JButton getApproveButton() {
      if ( approveButton == null ) {
         approveButton = new JButton( "OK" );
         approveButton.setMnemonic( 'o' );
         approveButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               approve();
            }
         } );
      }
      return approveButton;
   }

   public JButton getCancelButton() {
      if ( cancelButton == null ) {
         cancelButton = new JButton( "Cancel" );
         cancelButton.setMnemonic( 'c' );
         cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               cancel();
            }
         } );
      }
      return cancelButton;
   }

   public JButton getAbortButton() {
      if ( abortButton == null ) {
         abortButton = new JButton( "Abort" );
         abortButton.setMnemonic( 'a' );
         abortButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               abort();
            }
         } );
      }
      return abortButton;
   }

   public final int getResult() {
      return result;
   }

   protected final void setResult( int result ) {
      this.result = result;
   }

   protected boolean handleKey( KeyEvent e ) {
      return false;
   }

   private final boolean hasValidParent() {
      Window parent = (Window) getParent();
      boolean valid = (parent != null) && parent.isVisible();
      if ( parent instanceof Frame ) {
         return valid && ((((Frame) parent).getState() & Frame.ICONIFIED) == 0);
      }
      return valid;
   }

   public final void open() {
      if ( buttonList.size() > 0 ) {
         resizeButtons();
         buttons.add( Box.createHorizontalStrut( 20 ), BorderLayout.CENTER );
      }
      pack();
      center();
      setVisible( true );
   }

   private void resizeButtons() {
      int maxHeight = 0;
      int h;
      Iterator it = buttonList.iterator();
      while ( it.hasNext() ) {
         h = ((JButton) it.next()).getPreferredSize().height;
         if ( h > maxHeight ) {
            maxHeight = h;
         }
      }
      it = buttonList.iterator();
      JButton b;
      Dimension dim;
      while ( it.hasNext() ) {
         b = (JButton) it.next();
         dim = b.getPreferredSize();
         dim.height = maxHeight;
         b.setPreferredSize( dim );
      }
   }

   public final void setDefaultBorder() {
      main.setBorder( new CompoundBorder( new EtchedBorder(), new EmptyBorder(
            10, 10, 10, 10 ) ) );
   }

   public void setDisposeOnClose( boolean flag ) {
      disposeOnClose = flag;
   }

   protected final void setFirstFocusComponent( JComponent c ) {
      firstFocusComponent = c;
   }

   private final void setIgnoreKeys( boolean b ) {
      ignoreKeys = b;
   }

   public void windowGainedFocus( WindowEvent e ) {
      setIgnoreKeys( false );
   }

   public void windowLostFocus( WindowEvent e ) {
      setIgnoreKeys( true );
   }

}
