package com.hp.hpl.guess.ui;

import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.util.*;
import javax.swing.*;
import java.io.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.Point;
import java.awt.datatransfer.*;
import java.awt.Toolkit;
import javax.swing.text.html.HTMLEditorKit;
import com.hp.hpl.guess.util.intervals.*;
import com.hp.hpl.guess.GraphElement;
import com.hp.hpl.guess.Field;
import java.awt.Dimension;

import com.hp.hpl.guess.Guess;

/**
 * <p>The text pane that implement short-cut and general behaviour from a bash
 * interpreter.</p>
 *
 *
 * <blockquote>
 * <p>Copyright (C) 2003, 2004 Javier Iglesias.</p>
 *
 * <p>This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA</p> </blockquote>
 *
 * @author javier iglesias &lt;javier.iglesias@alawa.ch&gt;
 * @author Eytan Adar, serious revisions for GUESS
 * @version $Id: TextPaneJythonConsole.java,v 1.17 2005/07/07 02:01:49
 * eytan Exp $
 */
public class TextPaneJythonConsole extends JScrollPane implements Dockable {

    private int lineNumber = 1;

    protected static int fontSize = 11;

    public static void setFontSize(int size) {
	fontSize = size;
    }

    public Dimension getPreferredSize() {
	if (docked) {
	    return(super.getPreferredSize());
	}
	return(new Dimension(800,600));
    }

    private InternalTextPane itp = null;

    public TextPaneJythonConsole(PythonInterpreter jython) {
	super();
	itp = new InternalTextPane(jython);
	setViewportView(itp);
    }

    class PyFunctionWrapper {
	//public PyFunction func = null;
	public String name = null;
	
	public PyFunctionWrapper(PyFunction func, String name) {
	    //this.func = func;
	    this.name = name;
	}
    }

    public void shutdown() {
	itp.shutdown();
    }

    class InternalTextPane extends JTextPane implements GuessSelectable {

	public static final String REVISION = "1.0";

	private static final String COMMAND_STYLE = "command";
	private static final String COMMAND_UNDERLINE_STYLE = "ucommand";

	private static final String PROMPT_STYLE = "prompt";

	private static final String ANSWER_STYLE = "answer";
	private static final String ANSWER_UNDERLINE_STYLE = "uanswer";
	
	private static final String ERROR_STYLE = "error";
	private static final String ERROR_UNDERLINE_STYLE = "uerror";
	
	private PythonInterpreter jython;
	private ConsoleDocument document;
	private int currentItem;
	private Vector history;

	private Object currentH = null;
	private HashSet currentHighlights = new HashSet();
	private IntervalNode vin = null;

	private boolean demoMode = false;//true;
	
	public void shutdown() {
	    document.shutdown();
	}

	public Object getGuessSelected() {
	    IntervalNode[] matching =
		Tracker.searchContained(getSelectionStart(),
					getSelectionEnd()-1);
	    
	    HashSet pl = new HashSet();
	    for (int i = 0 ; i < matching.length ; i++) {
		IntervalNode in = matching[i];
		Object o = in.getProxy();
		if (o instanceof PyString) {
		    continue;
		} else if (o instanceof PySequence) {
		    Iterator it = 
			((PySequence)o).findGraphElements().iterator();
		    while(it.hasNext()) {
			pl.add(it.next());
		    }
		} else if (o instanceof PyInstance) {
		    if (((PyInstance)o).isGraphElementProxy()) {
			pl.add((((PyInstance)o).__tojava__(Object.class)));
		    }
		}
	    }
	    return(pl);
	}

	private void underline(IntervalNode nvin) {

	    if ((nvin != null) && (vin == nvin))
		return;

	    if (vin != null) {
		int length1 = 
		    vin.getHigh() - vin.getLow() + 1;
		int start1 = 
		    vin.getLow();
		try {
		    int t = ((TextPaneIntervalNode)vin).getStyle();
		    if (t == TextPaneIntervalNode.COMMAND_STYLE) {
			document.superSetCharacterAttributes(start1,length1,
							     getStyle(COMMAND_STYLE),
							     true);
		    } else if (t == TextPaneIntervalNode.ERROR_STYLE) {
			document.superSetCharacterAttributes(start1,length1,
							     getStyle(ERROR_STYLE),
							     true);
		    } else {
			document.superSetCharacterAttributes(start1,length1,
							     getStyle(ANSWER_STYLE),
							     true);
		    }  
		} catch (Exception ex) {
		    ExceptionWindow.getExceptionWindow(ex);
		}
	    }

	    if (nvin != null) {
		int length1 = 
		    nvin.getHigh() - nvin.getLow() + 1;
		int start1 = 
		    nvin.getLow();
		try {
		    int t = ((TextPaneIntervalNode)nvin).getStyle();
		    if (t == TextPaneIntervalNode.COMMAND_STYLE) {
			document.superSetCharacterAttributes(start1,length1,
							     getStyle(COMMAND_UNDERLINE_STYLE),
							     true);
		    } else if (t == TextPaneIntervalNode.ERROR_STYLE) {
			document.superSetCharacterAttributes(start1,length1,
							     getStyle(ERROR_UNDERLINE_STYLE),
							     true);
		    } else {
			document.superSetCharacterAttributes(start1,length1,
							     getStyle(ANSWER_UNDERLINE_STYLE),
							     true);
		    }  
		} catch (Exception ex) {
		    ExceptionWindow.getExceptionWindow(ex);
		}
	    }
	    vin = nvin;
	}

	private void centerHighlights() {
	    if (currentHighlights.size() > 0) {
		VisFactory.getFactory().getDisplay().center(currentHighlights.clone());
	    }
	}

	private PyDictionary func_dict = null;

	private void highlight(Object o) {

	    if ((o != null)  && (currentH == o)) 
		return;

	    StatusBar.setStatus("");

	    Iterator it = currentHighlights.iterator();
	   
	    while(it.hasNext()) {
		GraphElement el = (GraphElement)it.next();
		GraphEvents.mouseLeave(el);
	    }

	    currentHighlights.clear();
	    currentH = null;

	    if (o instanceof PySequence) {
		it = ((PySequence)o).findGraphElements().iterator();
		int test = 0;
		while(it.hasNext()) {
		    test++;
		    GraphElement ge = (GraphElement)it.next();
		    currentHighlights.add(ge);
		    GraphEvents.mouseEnter(ge);
		}
	    } else if (o instanceof PyInstance) {
		if (((PyInstance)o).isGraphElementProxy()) {
		    try {
			GraphElement element =
			    ((GraphElement)(((PyInstance)o).__tojava__(GraphElement.class)));
			currentHighlights.add(element);
			GraphEvents.mouseEnter(element);
		    } catch (Exception ex) {
			//continue;
		    }
		} else if (((PyInstance)o).isTypeOfInterest()) {
		    StatusBar.setStatus(Tracker.generateStatusString(o));
		    // do something
		}
	    } else if (o instanceof PyFunctionWrapper) {
		StatusBar.setStatus("Function: " + 
				    ((PyFunctionWrapper)o).name);
	    }

	    currentH = o;
	}

	public EditorPopup getMenu(Object o) {
	    if (o instanceof PyInstance) {
		if (((PyInstance)o).isNodeProxy()) {
		    return(NodeEditorPopup.getPopup());
		}
		if (((PyInstance)o).isEdgeProxy()) {
		    return(EdgeEditorPopup.getPopup());
		}
	    } else if (o instanceof PyString) {
		return(null);
	    } else if (o instanceof PySequence) {
		return(GraphElementEditorPopup.getPopup());
	    } else if (o instanceof Throwable) {
		return(ThrowableEditorPopup.getPopup());
	    }
	    return(null);
	}

	/**
	 *
	 * @since 1.0
	 */
	public InternalTextPane(PythonInterpreter jython) {
	    try {
		//setEditorKit(new HTMLEditorKit());
		this.jython = jython;
		setTransferHandler(new GuessTransferHandler());
		setDragEnabled(true);

		jython = prepareInterpreter();
		history = prepareHistory();
		document = prepareDocument();
		setStyledDocument(document);
		prepareKeymap(getKeymap());
		document.insertString(0, getEnvironment("PS1"),
				      getStyle(PROMPT_STYLE));
		
		final InternalTextPane thisitp = this;

		MouseAdapter test = new MouseAdapter() {

			private void selectionHandler(MouseEvent e) {
			    IntervalNode[] matching =
				Tracker.searchContained(getSelectionStart(),
							getSelectionEnd()-1);
			    
			    if (matching.length <= 0) {
				setSelectionStart(getSelectionStart());
				setSelectionEnd(getSelectionStart());
				return;
			    }
			    
			    int min = Integer.MAX_VALUE;
			    int max = Integer.MIN_VALUE;
			    HashSet pl = new HashSet();
			    for (int i = 0 ; i < matching.length ; i++) {
				IntervalNode in = matching[i];
				min = (int)Math.min(in.getLow(),min);
				max = (int)Math.max(in.getHigh(),max);
				Object o = in.getProxy();
				if (o instanceof PyString) {
				    continue;
				} else if (o instanceof PySequence) {
				    Iterator it = 
					((PySequence)o).findGraphElements().iterator();
				    while(it.hasNext()) {
					pl.add(it.next());
				    }
				} else if (o instanceof PyInstance) {
				    if (((PyInstance)o).isGraphElementProxy()) {
					pl.add((((PyInstance)o).__tojava__(Object.class)));
				    }
				}
			    }
			    
			    setSelectionStart(min);
			    setSelectionEnd(max+1);
			    if (pl.size() > 0) {
				JEditorPane editor = 
				    (JEditorPane) e.getSource();
				GraphElementEditorPopup.getPopup().show(editor,
									e.getX(),
									e.getY(),
									pl,
									pl);
			    }
			}

			public void mouseClicked(MouseEvent e) {

			    if (getSelectedText() != null) {
				// do something else here
				if (e.getButton() == MouseEvent.BUTTON3) {
				    // figure out which things are contained
				    // in this selection
				    selectionHandler(e);
				}
				return;
			    }

			    // if we have nothing selected, do...

			    // if we left click on something that we
			    // can center on, center on it
			    if (e.getButton() == MouseEvent.BUTTON1) {
				//if (e.getClickCount() == 2) {
				centerHighlights();
				//}
			    }

			    // if we right click figure out what menu
			    // to display
			    if (e.getButton() == MouseEvent.BUTTON3) {
				JEditorPane editor = 
				    (JEditorPane) e.getSource();
				EditorPopup mpup = getMenu(currentH);
				if (mpup != null) {
				    mpup.show(editor,e.getX(),e.getY(),
					      (HashSet)currentHighlights.clone(),
					      currentH);
				}
			    }
			}

			public void mouseExited(MouseEvent e) {
			    highlight(null);
			    underline(null);
			}
		    };
		addMouseListener(test);


		MouseMotionAdapter test2 = new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {

			    if (getSelectedText() != null) {
				// do something else here
				highlight(null);
				underline(null);
				return;
			    }

			    Point pt = new Point(e.getX(), e.getY());
			    int pos = viewToModel(pt);

			    IntervalNode min = findBestIN(pos);

			    if (min == null) {
				highlight(null);
				underline(null);
				return;
			    }
			   
			    if (min != null) {
				highlight(min.getProxy());
			    }
			    underline(min);
			}
		    };

		addMouseMotionListener(test2);

		ToolTipManager.sharedInstance().registerComponent(this);
		
		//setMenuOptions();

	    } catch (Exception e) {
		ExceptionWindow.getExceptionWindow(e);
	    }
	}



	private IntervalNode findBestIN(int pos) {
	    IntervalNode[] matching =
		Tracker.searchContains(pos,pos);
	    
	    if (matching.length <= 0) 
		return(null);
	    
	    IntervalNode min = null;
	    int minSize = Integer.MAX_VALUE;
	    for (int m = 0; m < matching.length ; m++) {
		if ((matching[m].getHigh() - 
		     matching[m].getLow()) < minSize) {
		    minSize = matching[m].getHigh() - 
			matching[m].getLow();
		    min = matching[m];
		}
	    }
	    return(min);
	}
	

	private void addToIntervals(String text, int commandstart) {
	    int locstart = commandstart;
	    char[] temp = new char[text.length()];
	    text.getChars(0,temp.length,temp,0);
	    StringBuffer toAdd = null;
	    boolean inQuotes = false;
	    for (int i = 0 ; i < temp.length ; i++) {
		if (temp[i] == '\\') {
		    i++;
		    continue;
		}

		if (temp[i] == '\'') {
		    for (int j = i+1 ; j < temp.length ; j++) {
			i++;
			if (temp[j] == '\'') {
			    break;
			}
		    }
		    continue;
		}

		if (temp[i] == '\"') {
		    for (int j = i+1 ; j < temp.length ; j++) {
			i++;
			if (temp[j] == '\"') {
			    break;
			}
		    }
		    continue;
		}
		
		if (Character.isLetterOrDigit(temp[i])) {
		    if (toAdd == null) {
			toAdd = new StringBuffer();
			locstart = commandstart + i;
		    }
		    toAdd.append(temp[i]);
		    //System.out.println(toAdd);
		} else if (toAdd != null) {
		    Object value = jython.get(toAdd.toString());
		    if (value != null) {
			//System.out.println(toAdd + " " + value);
			
			TextPaneIntervalNode tin = null;
			if (value instanceof PyFunction) {
			    tin = 
				(TextPaneIntervalNode)Tracker.addNode(locstart-1,toAdd.length()-1,new PyFunctionWrapper((PyFunction)value,toAdd.toString()));
			} else {
			    tin = 
				(TextPaneIntervalNode)Tracker.addNode(locstart-1,toAdd.length()-1,value);
			}
			if (tin != null) {
			    tin.setStyle(TextPaneIntervalNode.COMMAND_STYLE);
			}
		    }
		    toAdd = null;
		}
	    }
	    if (toAdd != null) {
		Object value = jython.get(toAdd.toString());
		if (value != null) {
		    TextPaneIntervalNode tin = 
			(TextPaneIntervalNode)Tracker.addNode(locstart-1,toAdd.length()-1,value);
		    if (tin != null) {
			tin.setStyle(TextPaneIntervalNode.COMMAND_STYLE);
		    }
		}
	    }
	}

	public String getToolTipText(MouseEvent e) {

	    if (getSelectedText() != null) {
		return(null);
	    }

	    int pos = viewToModel(new Point(e.getX(),e.getY()));
	    try {
		IntervalNode match = findBestIN(pos);
		if (match == null)
		    return(null);
		
		Object value = match.getProxy();
		if (value != null) {
		    if (value instanceof PySequence) {
			StringBuffer toRet = new StringBuffer("<html>");
			if (value instanceof PyList) {
			    String anno = ((PyList)value).annotation;
			    if (anno != null)
				toRet.append("<B>"+anno+"</B><BR>");
			}
			int size = ((PySequence)value).__len__();
			int max = Math.min(size,15);
			int left = size;
			for (int i = 0 ; i < max ; i++) {
			    String test = 
				((PySequence)value).__finditem__(i).toString(); 
			    if (test.length() > 80) {
				test = test.substring(0,80) + "...";
			    }
			    toRet.append(test);
			    if (i < max - 1) 
				toRet.append("<BR>");
			    left--;
			}
			if (size > 15) {
			    toRet.append("<BR>...("+left+" more)");
			}
			toRet.append("</html>");
			return(toRet.toString());
			//return(null);
			//} //else if (value instanceof Throwable) {
		    //final Writer result = new StringWriter();
		    //final PrintWriter printWriter = new PrintWriter( result );
		    //((Throwable)value).printStackTrace( printWriter );
			//return("<HTML><PRE>"+result.toString()+"</PRE></HTML>");
		    } else if (value instanceof PyFunctionWrapper) {
			StatusBar.setStatus("Function: " + 
					    ((PyFunctionWrapper)value).name);
			if (func_dict == null) {
			    func_dict = 
				(PyDictionary)jython.get("__FUNCTION_DICTIONARY");
			}
			if (func_dict != null) {
			    PyObject foo = 
				func_dict.__finditem__(new PyString(((PyFunctionWrapper)value).name));
			    if (foo != null) {
				return("<HTML><PRE>"+foo.toString()+"</PRE></HTML>");
			    } else {
				return(null);
			    }
			}
		    } else {
			return("<html>"+value.toString()+"</html>");
		    }
		}
	    } catch (Exception ex) {
		return(null);
	    }
	    return(null);
	}

	/**
	 *
	 * @since 1.0
	 */
	private Vector prepareHistory() {
	    Vector answer = new Vector();
	    answer.add(""); // top sentinelle
	    answer.add(""); // bottom sentinelle
	    currentItem = 0;

	    return answer;
	}

	/**
	 *
	 * @since 1.0
	 */
	private PythonInterpreter prepareInterpreter() {
	    jython.exec("ENV = {}");
	    setEnvironment("PS1", ">>> ");
	    setEnvironment("PS2", "... ");
	    setEnvironment("PATH_SEPARATOR", System.getProperty("path.separator"));
	    setEnvironment("COLS", "80");

	    return jython;
	}

	/**
	 *
	 * @since 1.0
	 */
	public String getEnvironment(String key) {
	    try {
		return jython.eval("ENV['" + key + "']").toString();
	    } catch (Exception e) {
		return "";
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	public void setEnvironment(String key, String value) {
	    jython.exec(new StringBuffer("ENV['").append(key).append("'] = '")
			.append(value).append("'")
			.toString());
	}

	/**
	 *
	 * @since 1.0
	 */
	private ConsoleDocument prepareDocument() {
	    ConsoleDocument answer = new ConsoleDocument();
	    return answer;
	}

	/**
	 *
	 * @since 1.0
	 */
	private void prepareKeymap(Keymap map) {
	    // special characters
	    // replace actions that move caret
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				      new PreviousHistoryItemAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				      new NextHistoryItemAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
				      new MoveLeftAction());

	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
				      new MoveStartAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
				      new MoveRightAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
				      new TabulationAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_A,
							     KeyEvent.CTRL_MASK), new MoveStartAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B,
							     KeyEvent.CTRL_MASK), new MoveLeftAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_E,
							     KeyEvent.CTRL_MASK), new MoveEndAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F,
							     KeyEvent.CTRL_MASK), new MoveRightAction());

	    // replace CTRL+... shortcut actions
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C,
							     KeyEvent.CTRL_MASK), new CancelAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_D,
							     KeyEvent.CTRL_MASK), new DeleteAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_K,
							     KeyEvent.CTRL_MASK), new KillAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_L,
							     KeyEvent.CTRL_MASK), new ClearAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_N,
							     KeyEvent.CTRL_MASK), new NextHistoryItemAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_P,
							     KeyEvent.CTRL_MASK), new PreviousHistoryItemAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_R,
							     KeyEvent.CTRL_MASK), new SearchHistoryItemAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_T,
							     KeyEvent.CTRL_MASK), new SwapAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_U,
							     KeyEvent.CTRL_MASK), new YankAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_W,
							     KeyEvent.CTRL_MASK), new YankWordAction());
	    map.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
							     KeyEvent.CTRL_MASK), new PasteAction());
	}

	/**
	 *
	 * @since 1.0
	 */
	public void move(int offset) {
	    if (document.isOffsetOnCommandLine(offset)) {
		setCaretPosition(offset);
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	public void moveStart() {
	    move(document.getCommandLineStartOffset());
	}

	/**
	 *
	 * @since 1.0
	 */
	public void moveEnd() {
	    move(document.getCommandLineEndOffset());
	}

	/**
	 *
	 * @since 1.0
	 */
	public void moveLeft() {
	    move(getCaretPosition() - 1);
	}

	/**
	 *
	 * @since 1.0
	 */
	public void moveRight() {
	    move(getCaretPosition() + 1);
	}

	/**
	 *
	 * @since 1.0
	 */
	private class ConsoleDocument extends DefaultStyledDocument {
	    private String yank = "";
	    private int multilining = 0; // level of multilining
	    private StringBuffer multiline;
	    private boolean CLEARING = false;
	    private Hashtable styles;

	    /**
	     *
	     * @since 1.0
	     */
	    public ConsoleDocument() {
		initializeStyles();
		addHistoryFromFile();
		try {
		} catch (Exception e) {
		    ;
		}
		Tracker.setDocument(this);
		setMenuOptions();
	    }

	    private void setMenuOptions() {
		JMenu editMenu = 
		    Guess.getMainUIWindow().getGMenuBar().editMenu;
		JMenuItem copy = new JMenuItem("Copy from console");
		JMenuItem paste = new JMenuItem("Paste to console");
		ActionListener cpListener = new ActionListener(  ) {
			public void actionPerformed(ActionEvent event) {
			    if (event.getActionCommand().equals("Copy from console")) {
				if (getSelectedText() != null) {
				    yank = getSelectedText();
				    addToClipboard(yank);
				}
			    } else if (event.getActionCommand().equals("Paste to console")) {
				pasteBuffer();
			    }  
			}
		    };
		copy.addActionListener(cpListener);
		paste.addActionListener(cpListener);
		editMenu.add(copy);
		editMenu.add(paste);
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    protected void initializeStyles() {
		Style def = 
		    StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		def = addStyle(null, def);
		//StyleConstants.setFontFamily(def, "Monospaced");

		Style style = addStyle(PROMPT_STYLE, def);
		StyleConstants.setBold(style, true);
		StyleConstants.setFontSize(style, fontSize);

		style = addStyle(COMMAND_STYLE, def);
		StyleConstants.setBold(style, true);
		StyleConstants.setFontSize(style, fontSize);
		
		style = addStyle(COMMAND_UNDERLINE_STYLE, def);
		StyleConstants.setBold(style, true);
		StyleConstants.setFontSize(style, fontSize);
		StyleConstants.setForeground(style, Color.blue);
		StyleConstants.setUnderline(style,true);

		style = addStyle(ANSWER_STYLE, def);
		StyleConstants.setForeground(style, Color.darkGray);
		StyleConstants.setFontSize(style, fontSize);

		style = addStyle(ANSWER_UNDERLINE_STYLE, def);
		StyleConstants.setForeground(style, Color.blue);
		StyleConstants.setFontSize(style, fontSize);
		StyleConstants.setUnderline(style,true);

		style = addStyle(ERROR_STYLE, def);
		StyleConstants.setForeground(style, Color.red);
		StyleConstants.setFontSize(style, fontSize);

		style = addStyle(ERROR_UNDERLINE_STYLE, def);
		StyleConstants.setForeground(style, Color.red);
		StyleConstants.setFontSize(style, fontSize);
		StyleConstants.setUnderline(style,true);
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    protected void addHistoryItem(String command) {

		if (demoMode) {
		    System.out.println("...");
		    return;
		}

		if ((command.indexOf('\n') == -1) && // FIXME : find a way to include multiline commands in history
                    !history.get(history.size() - 2).equals(command)) {
		    history.insertElementAt(command, history.size() - 1);
		}

		
		currentItem = history.size() - 1;
	    }
	
	    public void shutdown() {
		saveHistoryToFile();
	    }

	    protected void saveHistoryToFile() {
		if ((Guess.getAppletMode() == true) &&
		    (Guess.getSignedAppletMode() == false)) {
		    return;
		}

		try {
		    String toLoad = System.getProperty("gHome");
		    if (toLoad == null) { 
			toLoad = ".";
		    } 
		    File f = new File(toLoad + 
				      File.separatorChar + 
				      ".guess_history");
		    PrintStream out = new PrintStream(new FileOutputStream(f));
		    int len = history.size();
		    int start = history.size() - 100;
		    if (start < 0) {
			start = 0;
		    }
		    for (int i = start ; i < history.size() ; i++) {
			out.println(history.elementAt(i));
		    }
		    out.close();
		} catch (Exception ex) {
		}
	    }

	    protected void addHistoryFromFile() {

		if ((Guess.getAppletMode() == true) &&
		    (Guess.getSignedAppletMode() == false)) {
		    return;
		}

		try {
		    String toLoad = System.getProperty("gHome");
		    if (toLoad == null) { 
			toLoad = ".";
		    } 
		    File f = new File(toLoad + 
				      File.separatorChar + 
				      ".guess_history");
		    if (!f.exists()) {
			return;
		    }
		    BufferedReader in
			= new BufferedReader(new FileReader(f));
		    while(in.ready()) {
			String command = in.readLine();
			if ((command.indexOf('\n') == -1) && // FIXME : find a way to include multiline commands in history
			    !history.get(history.size() - 2).equals(command)) {
			    history.insertElementAt(command, history.size() - 1);
			    
			}
			
			currentItem = history.size() - 1;
			//addHistoryItem(in.readLine());
		    }
		    in.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void selectPreviousHistoryItem() {
		currentItem = Math.max(1, --currentItem);
		setCommandLine((String) history.get(currentItem));
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void selectNextHistoryItem() {
		currentItem = Math.min(history.size() - 1, ++currentItem);
		setCommandLine((String) history.get(currentItem));
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void setCommandLine(String command) {
		clearCommandLine();

		try {
		    insertString(getCommandLineStartOffset(), command,
				 getStyle(COMMAND_STYLE));
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void swapCharacters() {
		int start = getCommandLineStartOffset(getCaretPosition());
		int current = getCaretPosition();
		int end = getLength();

		try {
		    // don't swap if at head of line
		    if (current == start) {
			return;
		    }

		    String swap = getText(current - 1, 1);
		    remove(current - 1, 1);

		    if (current == end) {
			// swap to last characters if at end of line
			superInsertString(getLength() - 1, swap,
					  getStyle(COMMAND_STYLE));
			move(getLength());
		    } else {
			// swap the two previous characters if in the middle of a string
			superInsertString(current, swap, getStyle(COMMAND_STYLE));
			move(current + 1);
		    }
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    private Clipboard clipboard = null;

	    public void addToClipboard(String s) {
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
			StringSelection ss = new StringSelection(s);
			clipboard.setContents(ss,ss);
		    } catch (Exception ex) {
			ExceptionWindow.getExceptionWindow(ex);
		    }
		}
	    }

	    private final DataFlavor stringFlavor = DataFlavor.stringFlavor;

	    public void getFromClipboard() {
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
			Transferable tf = clipboard.getContents(this);
			if (tf.isDataFlavorSupported(stringFlavor)) {
			    yank = (String)tf.getTransferData(stringFlavor);
			}
		    } catch (Exception ex) {
			ExceptionWindow.getExceptionWindow(ex);
		    }
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void yankWord() {
		try {
		    int end = getCaretPosition();
		    int start = getWordStartOffset(end);
		    yank = getText(start, end - start);
		    addToClipboard(yank);
		    remove(start, end - start);
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void yankCommandLine() {
		try {
		    int start = getCommandLineStartOffset(getCaretPosition());
		    int end = getCaretPosition();
		    yank = getText(start, end - start);
		    addToClipboard(yank);
		    remove(start, end - start);
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void killCommandLine() {
		try {
		    int start = getCaretPosition();
		    int end = getCommandLineEndOffset();
		    yank = getText(start, end - start);
		    addToClipboard(yank);
		    remove(start, end - start);
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void completeCommandLine() {
		try {
		    int start = getCommandLineStartOffset();
		    int end = getCaretPosition();
		    String command = getText(start, end - start);

		    // is it a request for completion ?
		    if (command.trim().startsWith("./")) {
			FilenameFilter filter = new CompletionFilenameFilter(command.substring(command.indexOf(
													       "./") + 2));
			StringTokenizer tokens = new StringTokenizer(getEnvironment(
										    "PATH"), getEnvironment("PATH_SEPARATOR"));
			Vector candidates = new Vector();
			int longestCandidate = -1;

			// find all
			if (tokens.countTokens() > 0) {
			    yank = command;
			    addToClipboard(yank);
			    while (tokens.hasMoreElements()) {
				try {
				    File dir = new File(tokens.nextToken());

				    if (dir.isDirectory()) {
					String[] files = dir.list(filter);

					for (int index = 0; index < files.length;
					     index++) {
					    candidates.addElement(files[index]);

					    if (files[index].length() > longestCandidate) {
						longestCandidate = files[index].length();
					    }
					}
				    }
				} catch (Exception e) {
				    ;
				}
			    }

			    if (candidates.size() == 0) {
				// sorry : nothing to complete...
				return;
			    } else if (candidates.size() == 1) {
				// complete the command line
				yankCommandLine();
				insertString(getCaretPosition(),
					     command.substring(0, command.indexOf("./") + 2),
					     getStyle(COMMAND_STYLE));
				insertString(getCaretPosition(),
					     (String) candidates.elementAt(0),
					     getStyle(COMMAND_STYLE));
			    } else {
				// there are several possible completions, list them
				superInsertString(getCaretPosition(), "\n",
						  getStyle(COMMAND_STYLE));

				Iterator list = candidates.iterator();
				StringBuffer line = new StringBuffer();
				String each = null;
				longestCandidate += 2;

				int columns = 1;

				try {
				    columns = Integer.parseInt(getEnvironment(
									      "COLS"));
				} catch (Exception e) {
				    ;
				}

				// print all scripts in columns
				while (list.hasNext()) {
				    each = (String) list.next();

				    if (((line.length() + each.length()) > columns) &&
                                        (line.length() > 0)) {
					superInsertString(getLength(),
							  line.append("\n").toString(),
							  getStyle(ANSWER_STYLE));
					line = new StringBuffer();
				    }

				    line.append(each);

				    for (int count = longestCandidate -
					     each.length(); count > 0; count--) {
					line.append(" ");
				    }
				}

				if (line.length() > 0) {
				    superInsertString(getLength(),
						      line.append("\n").toString(),
						      getStyle(ANSWER_STYLE));
				}

				// reprint the prompt and command line
				superInsertString(getLength(),
						  (multiline != null) ? getEnvironment("PS2")
						  : getEnvironment("PS1"),
						  getStyle(PROMPT_STYLE));

				getFromClipboard();
				superInsertString(getLength(), yank,
						  getStyle(COMMAND_STYLE));
			    }
			}
		    } else {
			insertString(getCaretPosition(), "\t",
				     getStyle(COMMAND_STYLE));
		    }
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     */
	    public void cancel() {
		try {
		    multilining = 0;
		    multiline = null;

		    superInsertString(getLength(), "\n", getStyle(COMMAND_STYLE));
		    superInsertString(getLength(), getEnvironment("PS1"),
				      getStyle(PROMPT_STYLE));
		    moveEnd();
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void delete() {
		try {
		    remove(getCaretPosition(), 1);
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void clear() {
		try {
		    clearCommandLine();
		    CLEARING = true;
		    replace(0, getLength(),
			    (multiline != null) ? getEnvironment("PS2")
			    : getEnvironment("PS1"),
			    getStyle(PROMPT_STYLE));
		    CLEARING = false;
		    pasteBuffer();
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void clearCommandLine() {
		int start = getCommandLineStartOffset();
		int end = getCommandLineEndOffset();

		try {
		    yank = getText(start, end - start);
		    addToClipboard(yank);
		    remove(start, end - start);
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void pasteBuffer() {
		try {
		    getFromClipboard();
		    insertString(getCaretPosition(), yank, getStyle(COMMAND_STYLE));
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    private boolean isOffsetOnCommandLine(int offset) {
		int start = getCommandLineStartOffset();

		return ((offset >= start) && (offset <= getLength()));
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    protected int getCommandLineEndOffset() {
		return getCommandLineEndOffset(getLength()) - 1;
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    protected int getCommandLineEndOffset(int offset) {
		return getParagraphElement(offset).getEndOffset();
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    protected int getCommandLineStartOffset() {
		return getCommandLineStartOffset(getLength());
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    protected int getCommandLineStartOffset(int offset) {
		return getParagraphElement(offset).getStartOffset() +
		    getEnvironment("PS1").length();
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    protected int getWordStartOffset(int offset)
		throws BadLocationException {
		int start = getCommandLineStartOffset(offset);
		String text = getText(start, offset - start).trim();

		return start + Math.max(0, text.lastIndexOf(" ") + 1);
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    private int getOffsetOnCommandLine(int offset) {
		if (!isOffsetOnCommandLine(offset)) {
		    offset = getLength();
		    move(offset);
		}

		return offset;
	    }


	    private void printOutErr() {
		if (Guess.outHandle != null) {
		    try {
			while(Guess.outHandle.ready()) {
			    superInsertString(getLength(), 
					      Guess.outHandle.readLine()+"\n",
					      getStyle(ANSWER_STYLE));
			}
		    } catch (Exception ex) {
			ExceptionWindow.getExceptionWindow(ex);
		    }
		}

		if (Guess.errHandle != null) {
		    try {
			while(Guess.errHandle.ready()) {
			    if (demoMode)
				continue;
			    superInsertString(getLength(), 
					      Guess.errHandle.readLine()+"\n",
					      getStyle(ERROR_STYLE));
			}
		    } catch (Exception ex) {
			ExceptionWindow.getExceptionWindow(ex);
		    }
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void insertString(int offset, String text, AttributeSet a)
		throws BadLocationException {

		printOutErr();

		// only insertions on the last line are allowed
		offset = getOffsetOnCommandLine(offset);

		// is it a command to execute ?
		int end;

		String command = null;
		int aftercommand = 0;

		if ((end = text.indexOf('\n')) > -1) {
		    moveEnd();
		    superInsertString(getCommandLineEndOffset(), "\n",
				      getStyle(COMMAND_STYLE));

		    // delimit the command to execute
		    int start = getCommandLineStartOffset(offset);
		    int length = getCommandLineEndOffset() - start - 1; // "-1" to get ride of final '\n'
		    command = getText(start, length);

		    // this is a request for external file execution
		    if (command.trim().startsWith("./")) {
			String name = command.trim();
			name = name.substring(name.indexOf("./") + 2);

			String path = null;

			// find the script in the path
			StringTokenizer tokens = new StringTokenizer(getEnvironment(
										    "PATH"), getEnvironment("PATH_SEPARATOR"));
			FilenameFilter filter = new CompletionFilenameFilter(name);

			// search the path for the script
			while ((path == null) && tokens.hasMoreElements()) {
			    try {
				File dir = new File(tokens.nextToken());

				if (dir.isDirectory()) {
				    String[] files = dir.list(filter);

				    for (int index = 0; index < files.length;
					 index++) {
					if (files[index].equals(name)) {
					    // got it !
					    path = new File(dir, name).getPath();

					    break;
					}
				    }
				}
			    } catch (Exception e) {
				;
			    }
			}

			// if the script wasn't found, just put its name
			if (path == null) {
			    path = name;
			}

			// keep the original beginning of the command
			String original = command.substring(0, command.indexOf("./"));

			// redefine the command to be python interpretable
			command = new StringBuffer(original).append("execfile('")
			    .append(path)
			    .append("')").toString();
		    }

		    // this is a multiline command start
		    if (command.trim().endsWith(":")) {
			multilining++;

			if (multiline == null) {
			    multiline = new StringBuffer(command).append('\n');
			    if (command.length() > 0)
				addHistoryItem(command);
			} else {
			    // 'else' statements fall here
			    if ((command.indexOf("else") > -1) ||
                                (command.indexOf("elif") > -1) ||
                                (command.indexOf("except") > -1) ||
                                (command.indexOf("finally") > -1)) {
				multilining--;
			    }
			    if (command.length() > 0)
				addHistoryItem(command);
			    multiline.append(command).append('\n');
			}

			superInsertString(getLength(), getEnvironment("PS2"),
					  getStyle(PROMPT_STYLE));

			printOutErr();
			return;
		    }

		    // this expression is part of a multiline command
		    if (multilining > 0) {
			multiline.append(command).append("\n");
			if (command.length() > 0)
			    addHistoryItem(command);
			// check if a level has been closed
			if (command.trim().equals("pass") ||
                            !command.startsWith("\t") ||
                            (command.length() == 0)) {
			    multilining--;

			    // are there more line to read ?
			    if (multilining > 0) {
				superInsertString(getLength(),
						  getEnvironment("PS2"), getStyle(PROMPT_STYLE));
				printOutErr();
				return;
			    }

			    // retrieve the complete command
			    command = multiline.toString();
			    multiline = null;
			} else {
			    superInsertString(getLength(), getEnvironment("PS2"),
					      getStyle(PROMPT_STYLE));

			    printOutErr();
			    return;
			}
		    }

		    // let Jython execute the command if necessary
		    if (command.trim().length() > 0) {
			String value;
			Style style;

			try {
			    StatusBar.setStatus("");
			    if (command.equals("quit")) {
				com.hp.hpl.guess.Guess.shutdown();
			    }
			    // keep command in history
			    // System.out.println(command);
			    addHistoryItem(command);

			    aftercommand = getLength();

			    // do command execution
			    Tracker.setLocation(getLength());
			    DocumentWriter buffer = 
				new DocumentWriter(this,
						   getStyle(ANSWER_STYLE));
			    jython.setOut(buffer);
			    //jython.setErr((Writer)null);
			    try {
				Object oval = jython.eval(command);
				
				jython.set("_", oval);
				
				if (oval instanceof PyFunction) {
				    jython.set("_", 
					       jython.eval("apply(_, ())"));
				}
				
				if (LabNotebook.getNotebook() != null)
				    LabNotebook.getNotebook().addText(command,
								      oval);
				jython.exec("if _ != None: print _");
				//System.out.println(oval.getClass());
			    } catch(PySyntaxError e)
				{
				    try
					{
					    jython.exec(command);
					    if (LabNotebook.getNotebook() != null)
						LabNotebook.getNotebook().addText(command,
										  null);
					}
				    catch(Throwable e2)
					{
					    if (demoMode) {
					    } else if (e2 instanceof PyException) {
						String es = 
						    ((PyException)e2).userFriendlyString();
						ExceptionWindow.getExceptionWindow(e2);
				
						TextPaneIntervalNode tin = 
						    (TextPaneIntervalNode)Tracker.addNode(getLength(),es.length(),e2);
						if (tin != null) {
						    tin.setStyle(TextPaneIntervalNode.ERROR_STYLE);
						}

						superInsertString(getLength(), 
								  es+"\n",
								  getStyle(ERROR_STYLE));
					    } else {
						String es = 
						    e2.toString();

						ExceptionWindow.getExceptionWindow(e2);

						TextPaneIntervalNode tin = 
						    (TextPaneIntervalNode)Tracker.addNode(getLength(),es.length(),e2);
						if (tin != null) {
						    tin.setStyle(TextPaneIntervalNode.ERROR_STYLE);
						}
						
						superInsertString(getLength(), 
								  es+"\n",
								  getStyle(ERROR_STYLE));


						StatusBar.setErrorStatus("Use Help->Error Log for more details");
					    }
					}
				    
				}
			    //System.out.println("xx:" + command);
			    //jython.exec(command);
			    buffer.close();
			} catch (Exception e) {
			    if (demoMode) {
			    } else { 
				String es = 
				    e.toString();
				
				ExceptionWindow.getExceptionWindow(e);
				
				TextPaneIntervalNode tin = 
				    (TextPaneIntervalNode)Tracker.addNode(getLength(),es.length(),e);
				if (tin != null) {
				    tin.setStyle(TextPaneIntervalNode.ERROR_STYLE);
				}
				
				superInsertString(getLength(), 
						  es+"\n",
						  getStyle(ERROR_STYLE));
				
				StatusBar.setErrorStatus("Use Help->Error Log for more details");
			    }
			}
		    }

		    // append the prompt
		    superInsertString(getLength(), getEnvironment("PS1"),
				      getStyle(PROMPT_STYLE));
		} else {
		    // nothing special : simply write data down
		    superInsertString(offset, text, getStyle(COMMAND_STYLE));
		}
		if (command != null) {
		    addToIntervals(command,aftercommand - 
				   command.length());
		}
		Tracker.getRecentNodes().clear();
		printOutErr();
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    private void superInsertString(int offset, String text, AttributeSet a)
		throws BadLocationException {
		super.insertString(offset, text, a);
	    }

	    public void superReplace(int offset,
				     int length,
				     String text,
				     AttributeSet attrs)
		throws BadLocationException {
		super.remove(offset,length);
		super.insertString(offset,text,attrs);
	    }

	    public void superSetCharacterAttributes(int offset,
						    int length,
						    AttributeSet attrs,
						    boolean replace)
		throws BadLocationException {
		super.setCharacterAttributes(offset,length,attrs,replace);
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void remove(int offset, int length) throws BadLocationException {
		// refuse to remove anything that is not on the latest line
		if (isOffsetOnCommandLine(offset) || CLEARING) {
		    super.remove(offset, length);
		}
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class DocumentWriter extends Writer {
	    private ConsoleDocument doc = null;
	    private Style style = null;

	    /**
	     *
	     * @since 1.0
	     */
	    public DocumentWriter(ConsoleDocument doc, Style style) {
		this.doc = doc;
		this.style = style;
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void write(char[] cbuf, int off, int len) {
		//Thread.dumpStack();
		//System.out.println(new String(cbuf,off,len));
		write(cbuf, off, len);
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void write(String text) {
		try {
		    //Thread.dumpStack();
		    //text = "<a HREF=\"asdff\">"+text+"</A>";
		    doc.superInsertString(doc.getLength(), text, style);
		    //   System.out.println(text);
		} catch (Exception e) {
		    ExceptionWindow.getExceptionWindow(e);
		}
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void flush() {
		//System.out.println("flush");
	    }

	    /**
	     *
	     * @since 1.0
	     */
	    public void close() {
		//System.out.println("close");
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class SearchHistoryItemAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		// FIXME: implement that
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class PreviousHistoryItemAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.selectPreviousHistoryItem();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class NextHistoryItemAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.selectNextHistoryItem();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class MoveStartAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		moveStart();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class MoveEndAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		moveEnd();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class MoveLeftAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		moveLeft();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class MoveRightAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		moveRight();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class ClearAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.clear();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class SwapAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.swapCharacters();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class YankWordAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.yankWord();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class YankAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.yankCommandLine();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class KillAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.killCommandLine();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class PasteAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.pasteBuffer();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class TabulationAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.completeCommandLine();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class CancelAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.cancel();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class DeleteAction extends AbstractAction {
	    public void actionPerformed(ActionEvent ae) {
		document.delete();
	    }
	}

	/**
	 *
	 * @since 1.0
	 */
	private class CompletionFilenameFilter extends Object
	    implements FilenameFilter {
	    private String begin = "";

	    public CompletionFilenameFilter(String begin) {
		this.begin = begin;
	    }

	    public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith(".py") &&
		    name.startsWith(begin);
	    }
	}
    }
    
    public int getDirectionPreference() {
	return(MainUIWindow.HORIZONTAL_DOCK);
    }

    public String getTitle() {
	return("Interpreter");
    }

    public String toString() {
	return(getTitle());
    }

    private boolean docked = true;

    public void opening(boolean state) {
    }

    public void attaching(boolean state) {
	docked = state;
	if ((state == true) && (myParent != null))
	    myParent.hide();
    }

    private GuessJFrame myParent = null;

    public GuessJFrame getWindow() {
	return(myParent);
    }

    public void setWindow(GuessJFrame gjf) {
	myParent = gjf;
    }

}
