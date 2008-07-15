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

import javax.swing.JLabel;
import javax.swing.table.*;
import javax.swing.JTable;
import java.awt.Component;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A class for rendering nicknames with emoticons
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.3 $
 * @date $Date: 2003/04/05 17:31:27 $
 */ 

public class CustomCellRenderer extends JLabel implements TableCellRenderer {
		public CustomCellRenderer(String htmlText, String login) {
			super(htmlText);
			this.setToolTipText(login);
		}
		
		private String insertEmoticons(String text) {
		StringTokenizer st = new StringTokenizer(text);
   		Vector tokens = new Vector();
   		
   		while (st.hasMoreTokens()) {
    		tokens.add(st.nextToken());
   		}
   		
   		for (int i = 0; i < tokens.size(); ++i) {
   			String s = (String)tokens.elementAt(i);
   			
			String emoticon = new EmoticonsAndSounds().getEmoticon(s);

   			if (emoticon != null) {
   				emoticon = "<img src=" + "file:" + emoticon + " align=bottom>";
   				tokens.removeElementAt(i);
   				tokens.insertElementAt(emoticon, i);
   			}
   		}
   		
   		StringBuffer sBuf = new StringBuffer();
   		for (int i = 0; i < tokens.size(); ++i) {
   			sBuf.append((String)tokens.elementAt(i));
   			sBuf.append(" ");
   		}

   		return sBuf.toString();
	}
	
        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
              
            if (isSelected) {
                // cell (and perhaps other cells) are selected
            }
    
            if (hasFocus) {
            	// this cell is the anchor and the table has the focus
            }
            
            String login = (String)table.getValueAt(rowIndex, vColIndex-1);

			// 'value' is value contained in the cell located at
            // (rowIndex, vColIndex)
    		String htmlText = "<html><font color=navy>" + this.insertEmoticons(value.toString()) + "</font></html>";
    		setText(htmlText);
    
            // Since the renderer is a component, return itself
			return new CustomCellRenderer(htmlText, login);
        }
    
        // The following methods override the defaults for performance reasons
        public void validate() {}
        public void revalidate() {}
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }
