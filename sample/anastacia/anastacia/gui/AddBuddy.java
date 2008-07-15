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

import javax.swing.*;
import messenger.AProtocol;
import java.awt.event.*;
import java.awt.*;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.4 $
 * @date $Date: 2003/05/04 19:37:21 $
 * 
 */
public class AddBuddy extends JFrame implements ActionListener {
	private AProtocol fModel;
	private JTextField textNick, textLogin; 
	private JLabel labelNick, labelLogin, labelError;
	private JPanel panePreferences, paneError;
	private JButton buttonOk; 

	public AddBuddy(AProtocol p) {
		
		fModel = p;
		
		new JFrame("Add Buddy");
		setSize(300, 200);
		setLocation(200,200);
		
		panePreferences = new JPanel();
		paneError = new JPanel();
		
		addBuddyWidgets();
	}
	
	private void addBuddyWidgets() {
		labelLogin = new JLabel("Login");
		labelNick = new JLabel("Nickname");
		labelError = new JLabel("");
		
		textLogin = new JTextField();
		textNick = new JTextField();
		
		buttonOk = new JButton("Ok");
		
		buttonOk.setActionCommand("ok");
		buttonOk.addActionListener(this);
		
		panePreferences.setBorder(BorderFactory.createEmptyBorder(
                                       5,//top
                                       5,//left
                                       5, //bottom
                                       5) //right
                                       );
        panePreferences.setLayout(new GridLayout(0, 2));

		panePreferences.add(labelLogin);
		panePreferences.add(textLogin);
		panePreferences.add(labelNick);
		panePreferences.add(textNick);
		panePreferences.add(buttonOk);
		
		paneError.add(labelError);
		
		getContentPane().add(panePreferences, BorderLayout.CENTER);
		getContentPane().add(paneError, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(false);	
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("ok")) {
			if(textLogin.getText().equals("")||textNick.getText().equals("")) {
					labelError.setText("Fill in all the fields!");
					System.out.println("fill in all the fields");
			}
			else {
				fModel.addContact(textLogin.getText(), textNick.getText());
				labelError.setText("");
				setVisible(false);
			}
		}
	}	
}
