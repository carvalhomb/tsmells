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
import messenger.ICQ.ICQProtocol;
import messenger.MSN.MSNProtocol;
import messenger.Yahoo.YahooProtocol;
import java.awt.event.*;
import java.awt.*;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.8 $
 * @date $Date: 2003/05/25 16:52:21 $
 * 
 * 
 */
public class Preferences extends JFrame implements ActionListener {
	private AProtocol fModel;
	private JTextField textPort, textServer, textLogin; 
	private JPasswordField textPassword;
	private JLabel labelPort, labelServer, labelLogin, labelPassword, labelError;
	private JPanel panePreferences, paneError;
	private JButton buttonOk;
	private boolean fPrefsFilledIn = false;

	/**
	 * Method Preferences.
	 * @param p
	 */
	public Preferences(AProtocol p) {
		
		fModel = p;
		
		new JFrame("Preferences");
		setSize(300, 200);
		setLocation(200,200);
		
		panePreferences = new JPanel();
		paneError = new JPanel();
		
		addPreferencesWidgets();
	}
	
	/**
	 * Method addPreferencesWidgets.
	 */
	private void addPreferencesWidgets() {
		labelServer = new JLabel("Server");
		labelPort = new JLabel("Port");
		labelLogin = new JLabel("Login");
		labelPassword = new JLabel("Password");
		labelError = new JLabel("");
		
		textServer = new JTextField(fModel.getServer());
		textPort = new JTextField(String.valueOf(fModel.getPort()));
		textLogin = new JTextField(fModel.getLoginName());
		textPassword = new JPasswordField();
		
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
	
		panePreferences.add(labelServer);
		panePreferences.add(textServer);
		panePreferences.add(labelPort);
		panePreferences.add(textPort);
		panePreferences.add(labelLogin);
		panePreferences.add(textLogin);
		panePreferences.add(labelPassword);
		panePreferences.add(textPassword);
		panePreferences.add(buttonOk);
		
		paneError.add(labelError);
		
		getContentPane().add(panePreferences, BorderLayout.CENTER);
		getContentPane().add(paneError, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	// was DO_NOTHING_ON_CLOSE
		pack();
		setVisible(false);	
	}
	
	public boolean prefsFilledIn() {
		return fPrefsFilledIn;
	}
	
	public void setPrefsFilledIn(boolean prefsFilledIn) {
		fPrefsFilledIn = prefsFilledIn;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		fPrefsFilledIn = false;
		
		if(e.getActionCommand().equals("ok")) {
			if(textServer.getText().equals("")||textPort.getText().equals("")
				||textLogin.getText().equals("")||textPassword.getPassword().equals("")) {
					labelError.setText("Fill in all the fields!");
					System.out.println("fill in all the fields");
			}
			else {
			
				fModel.setServer((String)textServer.getText());
				fModel.setPort((int)(Double.parseDouble(textPort.getText())));
				fModel.setLoginName((String)textLogin.getText());
				
				if(fModel instanceof ICQProtocol) {
					fModel.setEncPasswd(Utils.ICQEncryptPass(String.valueOf(textPassword.getPassword())));
				}
				else if(fModel instanceof MSNProtocol) {
					fModel.setEncPasswd(String.valueOf(textPassword.getPassword()));
				}
				else if(fModel instanceof YahooProtocol) {
					fModel.setEncPasswd(String.valueOf(textPassword.getPassword()));
				}
				else {
					System.out.println("sorry for the trouble, there occured a system error");
				}
				labelError.setText("");
				fPrefsFilledIn = true;
				setVisible(false);
			}
		}
	}	
}
