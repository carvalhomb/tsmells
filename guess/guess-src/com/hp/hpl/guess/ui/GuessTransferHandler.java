package com.hp.hpl.guess.ui;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;

public class GuessTransferHandler extends TransferHandler {
    DataFlavor guessFlavor;
    String guessType = DataFlavor.javaJVMLocalObjectMimeType +
	";class=java.util.Collection";
    Object source = null;
   
    public GuessTransferHandler() {
        try {
            guessFlavor = new DataFlavor(guessType);
        } catch (ClassNotFoundException e) {
            System.out.println(
             "GuessTransferHandler: unable to create data flavor");
        }
    }

    public boolean importData(JComponent c, Transferable t) {
        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }
        try {
            //target = (JList)c;
            if (hasGuessFlavor(t.getTransferDataFlavors())) {
		Object o = t.getTransferData(guessFlavor);
		if (c instanceof GuessDropListener) {
		    ((GuessDropListener)c).receiveDrop(o);
		}
	    }
	    //} else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
	    //  alist = (ArrayList)t.getTransferData(serialArrayListFlavor);
            //} else {
	    //  return false;
	    // }
	    //} catch (UnsupportedFlavorException ufe) {
            ///System.out.println("importData: unsupported data flavor");
            //return false;
	    //} catch (IOException ioe) {
            //System.out.println("importData: I/O exception");
            //return false;
        } catch (Exception ex) {
	    ex.printStackTrace();
	}
        return true;
    }

    protected void exportDone(JComponent c, Transferable data, int action) {

    }
    
    private boolean hasGuessFlavor(DataFlavor[] flavors) {
	if (guessFlavor == null) {
	    return false;
	}
	
	for (int i = 0; i < flavors.length; i++) {
	    if (flavors[i].equals(guessFlavor)) {
		return true;
	    }
	}
	return false;
    }

    //    private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {
    //  if (serialArrayListFlavor == null) {
    //      return false;
    //  }
    //
    //  for (int i = 0; i < flavors.length; i++) {
    //      if (flavors[i].equals(serialArrayListFlavor)) {
    //          return true;
    //      }
    //  }
    //  return false;
    //}

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        if (hasGuessFlavor(flavors))  { return true; }
	return false;
    }

    protected Transferable createTransferable(JComponent c) {
	if (c instanceof GuessSelectable) {
	    return new GuessTransferable(((GuessSelectable)c).getGuessSelected());
	} else {
	    return(null);
	}
    }

    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    public class GuessTransferable implements Transferable {
        Object data;

        public GuessTransferable(Object data) {
            this.data = data;
        }

        public Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {guessFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (guessFlavor.equals(flavor)) {
                return true;
            }
            return false;
        }
    }
}
