/**   	Anastacia is a Java ICQ/MSN/Yahoo Instant Messenger
 *   	Copyright (C) 2002,2003 	Benny Van Aerschot, Bart Van Rompaey
 * 	Made as a project in 3th year computer science at the university of Antwerp (UA)
 *
 * 	This file is part of Anastacia.
 *
 *    	Anastacia is free software; you can redistribute it and/or modify
 *    	it under the terms of the GNU General Public License as published by
 *    	the Free Software Foundation; either version 2 of the License, or
 *    	(at your option) any later version.
 *
 *    	Anastacia is distributed in the hope that it will be useful,
 *    	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   	GNU General Public License for more details.
 *
 *    	You should have received a copy of the GNU General Public License
 *    	along with Anastacia; if not, write to the Free Software
 *    	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * 	Contact authors:
 * 		Benny Van Aerschot - bennyva@pi.be
 * 		Bart Van Rompaey - bart@perfectpc.be
 */


package gui;

import javax.swing.table.*;
import java.util.Vector;
import messenger.AContact;
import javax.swing.ImageIcon;
import javax.swing.*;


/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.5 $
 * @date $Date: 2003/05/24 10:11:54 $
 * 
 * 
 */
public class ContactTableModel extends AbstractTableModel {
	protected static int NUM_COLUMNS = 3;
	protected static int START_NUM_ROWS = 5;
	protected int nextEmptyRow = 0;
	protected int numRows = 0;
	private String[] fColumns = { "St.", "Login", "Nick" };
	Vector data = null;
	private ImageIcon[] fIcon;

	/**
	 * @param icon
	 */
	public ContactTableModel(ImageIcon[] icon) {
		fIcon = icon;
		data = new Vector();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
			return numRows;
	}
	
	/**
	returns number of rows in the table
	*/
	public int getColumnCount() {
		return NUM_COLUMNS;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int aColumn) {
        return fColumns[aColumn];
    }
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int columnIndex) {
		if(columnIndex == 0) {
			return Icon.class;
		}
		else {
			return Object.class;
		}	
	}
	
	/**
	 * @param row
	 * @return
	 */
	public AContact getContact(int row) {
		return (AContact)data.elementAt(row);
	}
	
	/**
	deletes contact with given id
	*/
	public boolean deleteContact(String loginname) {
		for(int i = 0; i < getRowCount(); i++) {
			AContact aContact = (AContact)data.elementAt(i);
			if(aContact.getLogin().equals(loginname)) {
				data.removeElementAt(i);
				numRows--;
				fireTableRowsDeleted(i, i);		
				return true;
			}
		}
		return false;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 *
	 * returns the Object at given row and column
	 */
	public Object getValueAt(int row, int column) {
		try {
			AContact aContact = (AContact)data.elementAt(row);
			String nick = null;
			String login = null;
			
			switch (column) {
				case 0:
					if(aContact.getStatus().endsWith("YONLINE")) {
						return fIcon[11];
					}
					else if(aContact.getStatus().endsWith("YOFFLINE")) {
						return fIcon[12];
					}
					else if(aContact.getStatus().equals("AWAY")) {
						return fIcon[1];
					}
					else if(aContact.getStatus().equals("N/A")) {
						return fIcon[2];
					}
					else if(aContact.getStatus().equals("NLN")) {
						return fIcon[4];
					}
					else if(aContact.getStatus().equals("AWY")) {
						return fIcon[5];
					}
					else if(aContact.getStatus().equals("LUN")) {
						return fIcon[5];
					}
					else if(aContact.getStatus().equals("PHN")) {
						return fIcon[5];
					}
					else if(aContact.getStatus().equals("BRB")) {
						return fIcon[5];
					}
					else if(aContact.getStatus().equals("IDL")) {
						return fIcon[5];
					}
					else if(aContact.getStatus().equals("BSY")) {
						return fIcon[6];
					}
					else if(aContact.getStatus().equals("FLN")) {
						return fIcon[7];
					}
					else if(aContact.getStatus().endsWith("DND")) {
						return fIcon[9];
					}
					else if(aContact.getStatus().endsWith("OCCUPIED")) {
						return fIcon[10];
					}
					else if(aContact.getStatus().endsWith("ONLINE")) {
						return fIcon[0];
					}
					else if(aContact.getStatus().endsWith("OFFLINE")) {
						return fIcon[3];
					}
					else {
						System.out.println("Unknown status: " + aContact.getStatus());
						return fIcon[8];
					}
					
					
				case 1:
					login = aContact.getLogin();
					return login;
				case 2:
					nick = aContact.getNick();
					return nick;
			}
		}
		catch (Exception e) {
			
		}
		return "";
	}

	/**
	* updates the table after changes in the data
	*/
	public void updateContact(AContact aContact) {
		String loginname = aContact.getLogin();
		AContact m = null;
		int index = -1;
		boolean found = false;
		boolean addedRow = false;
		
		int i = 0;
		while(!found && (i < nextEmptyRow)) {
			m = (AContact)data.elementAt(i);
			//System.out.println(m.getLogin());
			if(m.getLogin().equals(loginname)) {
				found = true;
				index = i;
			}
			else {
				i++;
			}
		}
		
		if(found) { //update old contact
			data.setElementAt(aContact, index);
		}
		else { // add new
			if(numRows <= nextEmptyRow) {
				// add a row
				numRows++;
				addedRow = true;
			}
			index = nextEmptyRow;
			data.addElement(aContact);
			nextEmptyRow++;
		}

		// Notify listeners that the data changed
		if(addedRow) {
			fireTableRowsInserted(index, index);
		}
		else {
			fireTableRowsUpdated(index, index);
		}
	}

	/**
	clears the table
	*/
	public void clear() {
		int oldNumRows = numRows;
		
		numRows = START_NUM_ROWS;
		data.removeAllElements();
		nextEmptyRow = 0;
		
		if(oldNumRows > START_NUM_ROWS) {
			fireTableRowsUpdated(START_NUM_ROWS, oldNumRows - 1);
		}
		fireTableRowsUpdated(0, START_NUM_ROWS - 1);
	}
}
