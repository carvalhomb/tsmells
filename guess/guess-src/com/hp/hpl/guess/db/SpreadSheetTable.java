package com.hp.hpl.guess.db;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;

import com.jgoodies.looks.*;
import com.hp.hpl.guess.freehep.*;
import com.hp.hpl.guess.*;

import java.sql.Types;

public class SpreadSheetTable extends AbstractTableModel {

    public SpreadSheetTable(DBServer db, boolean nOrE) {
	StringBuffer sb = null;
	Schema schema = null;
	if (nOrE == true) {
	    schema = Guess.getGraph().getNodeSchema();
	    sb = new StringBuffer("name");
	    colNames.addElement("name");
	} else {
	    schema = Guess.getGraph().getEdgeSchema();
	    sb = new StringBuffer("node1,node2");
	    colNames.addElement("node1");
	    colNames.addElement("node2");
	}
	Iterator fields = schema.fields();
	while(fields.hasNext()) {
	    Field f = (Field)fields.next();
	    String fName = f.getName();
	    if (nOrE) {
		if (fName.equalsIgnoreCase("name")) {
		    continue;
		}
	    } else {
		if ((fName.equalsIgnoreCase("node1"))
		    || (fName.equalsIgnoreCase("node2"))
		    || (fName.equalsIgnoreCase("__edgeid"))) {
		    continue;
		}
	    }
	    sb.append(",");
	    sb.append(fName);
	    colNames.addElement(fName);
	}
	
	fireTableDataChanged();	
    }

    String[][] data = null;
    Vector elements = new Vector();
    Vector colNames = new Vector();

    public int getColumnCount() { 
	if (data == null) {
	    return(0);
	} else {
	    return(data.length);
	}
    }
    
    public int getRowCount() {
	return(elements.size());
    }

    public Object getValueAt(int row, int col) {
	if (data != null) {
	    return(data[col][row]);
	}
	return(null);
    }
	
    public String getColumnName(int col) {
	return((String)colNames.elementAt(col));
    }

    public boolean isCellEditable(int row, int col) {
	return(false);
    }

    public void setValueAt(Object value, int row, int col) {
// 	String f = null;
// 	Field fld = null;
// 	if (lastSel != null) {
// 	    if (lastSel instanceof Node) {
// 		f = (String)nFields.elementAt(row);
// 		fld = Guess.getGraph().getNodeSchema().getField(f);
// 	    } else {
// 		f = (String)eFields.elementAt(row);
// 		fld = Guess.getGraph().getEdgeSchema().getField(f);
// 	    }
// 	    try {
// 		if ((fld.getSQLType() == Types.INTEGER) ||
// 		    (fld.getSQLType() == Types.TINYINT) ||
// 		    (fld.getSQLType() == Types.SMALLINT) ||
// 		    (fld.getSQLType() == Types.BIGINT)) {
// 		    lastSel.__setattr__(f,new Integer((String)value));
// 		} else if (fld.getSQLType() == Types.BOOLEAN) {
// 		    lastSel.__setattr__(f,new Boolean((String)value));
// 		} else if (fld.isNumeric()) {
// 		    lastSel.__setattr__(f,new Double((String)value));
// 		} else {
// 		    lastSel.__setattr__(f,value);
// 		}
// 	    } catch (Exception e) {
// 		e.printStackTrace();
// 	    }
// 	    fireTableCellUpdated(row, col);
// 	}
    }

}
