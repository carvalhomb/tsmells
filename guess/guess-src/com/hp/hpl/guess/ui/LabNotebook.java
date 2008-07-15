package com.hp.hpl.guess.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.python.util.PythonInterpreter;

import org.python.core.*;
import com.hp.hpl.guess.*;

public class LabNotebook extends JFrame {

    JTextPane pane = null;
    HTMLDocument doc = null;
    private PythonInterpreter jython = null;
    private static LabNotebook singleton = null;
    private JScrollPane jsp = null;

    public static LabNotebook createNotebook(PythonInterpreter j) {
	if (singleton == null) {
	    singleton = new LabNotebook(j);
	}
	return(singleton);
    }

    public static LabNotebook getNotebook() {
	return(singleton);
    }

    public Element getElementByTag(Element parent, HTML.Tag tag) {
	if (parent == null || tag == null)
	    return null;
	for (int k=0; k<parent.getElementCount(); k++) {
	    Element child = parent.getElement(k);
	    if (child.getAttributes().getAttribute(StyleConstants.NameAttribute).equals(tag))
		return child;
	    Element e = getElementByTag(child, tag);
	    if (e != null)
		return e;
	}
	return null;
    }

    
    private LabNotebook(PythonInterpreter jython) {
	this.jython = jython;
	pane = new JTextPane();
	pane.setEditable(false);
	//pane.setStyledDocument(doc);
	pane.setEditorKit(new HTMLEditorKit());
	pane.setText("<HTML><BODY><H1>Lab Notebook</H1><TABLE><TR ID=1><TD>&nbsp;</TD><TD>&nbsp;</TD></TR><DIV></TABLE></BODY></HTML>");
	doc = (HTMLDocument)pane.getStyledDocument();
	StyleSheet styleSheet = doc.getStyleSheet();
	styleSheet.addRule("body {font-family: Dialog}");
	SimpleLinkListener sll = new SimpleLinkListener(pane);
	pane.addHyperlinkListener(sll);
	jsp = new JScrollPane(pane);
	setContentPane(jsp);
	setSize(400,500);
	setVisible(true);
	//System.out.println(pane.getText());
    }

    private int counter = 1;
    private int lineNum = 0;
    boolean color = false;
    public void addText(String command, Object result) {
	try {
	    Element root = doc.getRootElements()[0];
	    Element body = doc.getElement(""+counter);
	    counter++;
	    String cl = "#FFFFFF";
	    if (!color) {
		cl = "#CCFFFF";
	    }
	    color = !color;
	    lineNum++;
	    StringBuffer toInsert = 
		new StringBuffer("<TR ID="+counter+" BGCOLOR="+cl+"><TD VALIGN=TOP><B>"+
				 lineNum+"</B></TD><TD VALIGN=TOP><PRE>"+command+"</PRE></TD></TR>\n");
	    String res = null;
	    if (result instanceof PyJavaInstance) {
		Object o = 
		    (Object)((PyInstance)result).__tojava__(Object.class);
		if (o instanceof Node) {
		    String name = ((Node)o).getName();
		    res = "<A HREF=\"http://"+name+"\">"+name+"</A>";
		} else if (o instanceof Edge) {
		    String name = ((Edge)o).toString();
		    res = "<A HREF=\"http://"+name+"\">"+name+"</A>";
		}
	    }
	    if (res != null) {
		counter++;
		toInsert.append("<TR ID="+counter+"><TD>&nbsp;<TD BGCOLOR="+cl+">"+res+"</TD></TR>\n");
	    }
	    doc.insertAfterEnd(body, toInsert.toString());
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
	jsp.scrollRectToVisible(bottom);
    }

    static private final Rectangle bottom = 
	new Rectangle( 0, Integer.MAX_VALUE, 0, 0 );

    public void addImage(String url, int width, int height) {
	try {
	    Element root = doc.getRootElements()[0];
	    Element body = doc.getElement(""+counter);
	    double scale = Math.min((double)getHeight()/(double)height,
				    (double)getWidth()/(double)width)*.8;
	    height = (int)(height * scale);
	    width = (int)(width * scale);
	    counter++;
	    url = (new java.io.File(url)).toURL().toString();
	    StringBuffer toInsert = 
		new StringBuffer("<TR ID="+counter+"><TD VALIGN=TOP>&nbsp;"+
				 "</TD><TD VALIGN=TOP>"+
				 "<IMG SRC=\""+url+"\" HEIGHT="+
				 height+" WIDTH="+
				 width+"></TD></TR>\n");
	    doc.insertAfterEnd(body, toInsert.toString());
	} catch (Exception ex) {
	    ExceptionWindow.getExceptionWindow(ex);
	}
	jsp.scrollRectToVisible(bottom);
    }

    class SimpleLinkListener implements HyperlinkListener {
	
	private JEditorPane pane;       // The pane we're using to display HTML
	
	public SimpleLinkListener(JEditorPane jep) {
	    pane = jep;
	}
	
	public void hyperlinkUpdate(HyperlinkEvent he) {
	    HyperlinkEvent.EventType type = he.getEventType();
	    //Element el = he.getSourceElement();
	    java.net.URL testURL = he.getURL();
	    if (testURL == null) {
		return;
	    }
	    String host = testURL.getHost();
	    if (type == HyperlinkEvent.EventType.ENTERED) {
		// Enter event.  Fill in the status bar.
		System.out.println("over: " + host);
	    }
	    else if (type == HyperlinkEvent.EventType.EXITED) {
		// Exit event.  Clear the status bar.
	    }
	    else if (type == HyperlinkEvent.EventType.ACTIVATED) {
		try {
		    jython.exec("center("+host+")");
		} catch (Exception e) {
		}
	    }
	}
    }
    
}

