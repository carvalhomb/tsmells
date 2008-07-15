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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Set;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.ImageIcon;
import java.util.Collection;
import java.awt.event.MouseAdapter;

/**
 * @author benny
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class EmoticonSelector extends JFrame implements MouseListener {
	private EmoticonsAndSounds fEmoticonsAndSounds;
	private JPanel fJPanel;
	private JTextArea fMessageBox;
	private Collection fC;
	private Set fS;
	private Object[] oa;
	private Object[] oa2;
	private int i = 0;

	public EmoticonSelector(int x, int y, JTextArea ja) {
		new JFrame("Emoticons");
		fJPanel = new JPanel();
		fJPanel.setLayout(new GridLayout(7,9));
		fJPanel.setBackground(Color.WHITE);
		fMessageBox = ja;
		//this.setSize(100, 100);
		this.setLocation(0,0);
		fEmoticonsAndSounds = new EmoticonsAndSounds();
		this.addEmoticons();
		this.getContentPane().add(fJPanel);
		this.pack();
		this.setVisible(true);
	}

	public void addEmoticons() {
		fC = fEmoticonsAndSounds.getAllEmoticons();
		oa = fC.toArray();
		fS =  fEmoticonsAndSounds.getAllEmoticonKeys();
		oa2 = fS.toArray();
		
		for (i = 0; i < oa.length; ++i) {
			ImageIcon imgIcon = new ImageIcon((oa[i]).toString());
			JLabel jl = new JLabel(imgIcon);
			jl.setPreferredSize(new Dimension(30,22));
			
			MouseListener ml = new MouseAdapter() {
				int j = i;
				
				public void mouseClicked(MouseEvent e) {
					fMessageBox.append(" " + (oa2[j]).toString().toUpperCase() + " ");
    			}
			};
			jl.addMouseListener(ml);
			fJPanel.add(jl);
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

}
