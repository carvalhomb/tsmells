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
import messenger.ProtocolException;
import messenger.ICQ.ICQExceptions.ICQException;
import messenger.MSN.MSNProtocol;
import java.io.File;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.3 $
 * @date $Date: 2003/05/24 10:11:54 $
 * 
 * 
 */
public class MessageBox extends JFrame implements ActionListener {
	private AProtocol fModel;
	private String fNick;
	private String fLogin;
	private JTextArea boxMessage;	// was JTextField
	private EditorPane areaLog;
	private JPanel paneMessBox;
	private JButton buttonSend, buttonClose, buttonEmoticon;
	private JScrollPane areaScrollPane1;
	private EmoticonSelector es = null;
	
	/**
	 * Method MessageBox.
	 * @param p
	 * @param login
	 */
	public MessageBox(AProtocol p, String login, String nick) {
		super(nick);
		
		fModel = p;
		fNick = nick;
		fLogin = login;
		
		new JFrame(fNick);
		setSize(300, 200);
		setLocation(300,200);
		
		paneMessBox = new JPanel();
		
		addMessageBoxWidgets();
	}	
		
	/**
	 * Method addMessageBoxWidgets.
	 */
	private void addMessageBoxWidgets() {
		
		paneMessBox.setBorder(BorderFactory.createEmptyBorder(
                                       2,//top
                                       2,//left
                                       2, //bottom
                                       2) //right
                                       );
		paneMessBox.setLayout(new BoxLayout(paneMessBox, BoxLayout.Y_AXIS));
		//paneMess.setLayout(new BoxLayout(paneMess, BoxLayout.Y_AXIS));	
		
		areaLog = new EditorPane(fNick);
		areaLog.setEditable(false);
		//areaLog.setAutoscrolls(true);
		areaScrollPane1 = new JScrollPane(areaLog);
        areaScrollPane1.setPreferredSize(new Dimension(400, 250));
        areaScrollPane1.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Messaging Window"),
                                BorderFactory.createEmptyBorder(5,5,5,5)),
							   areaScrollPane1.getBorder()));
		
		boxMessage = new JTextArea();
		boxMessage.setLineWrap(true);
		boxMessage.setRows(4);
		boxMessage.setWrapStyleWord(true);
		JScrollPane boxScrollPane1 = new JScrollPane(boxMessage);
        boxScrollPane1.setPreferredSize(new Dimension(100, 100));
        boxScrollPane1.setBorder(
            BorderFactory.createCompoundBorder(                               
                                BorderFactory.createEmptyBorder(5,5,5,5),
							   boxScrollPane1.getBorder()));
		boxMessage.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(boxMessage.getText().equals("")) {
					// niks doen, nie verzenden
				}
				else {
					areaLog.append("You say", (String)boxMessage.getText());

					try {
						fModel.sendMessage(fLogin, (String)boxMessage.getText());
					} 
					catch(ICQException ie) {
						ie.printStackTrace();
					}
					catch (ProtocolException err) {
						System.out.println(err.getMessage());
					}
					catch (Exception ex) {
						ex.printStackTrace();	
					}

					boxMessage.setText("");
				}
			}
		},KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),JComponent.WHEN_FOCUSED);
		
		
							   
		buttonSend = new JButton("Send");
		buttonClose = new JButton("Close");
		buttonEmoticon = new JButton();
		buttonEmoticon.setIcon(new ImageIcon(".." + File.separator + "multimedia" + File.separator + "emoticons" + File.separator + "ok.gif"));

		buttonSend.setActionCommand("send");
		buttonClose.setActionCommand("close");
		buttonEmoticon.setActionCommand("e");
		buttonSend.addActionListener(this);
		buttonClose.addActionListener(this);
		buttonEmoticon.addActionListener(this);
		
		paneMessBox.add(areaScrollPane1);
		paneMessBox.add(boxScrollPane1);
		JPanel pane2 = new JPanel(new GridLayout(1,3));
		//paneMessBox.add(buttonSend);
		//paneMessBox.add(buttonClose);
		//paneMessBox.add(buttonEmoticon);
		pane2.add(buttonSend);
		pane2.add(buttonClose);
		pane2.add(buttonEmoticon);

		paneMessBox.add(pane2);
		getContentPane().add(paneMessBox);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
       		public void windowClosing(java.awt.event.WindowEvent evt) {
            	exitForm(evt);
        	}
     	});
	}
	
	/**
	 * Method showMessage.
	 * @param msg
	 */
	public void showMessage(String msg) {
		areaScrollPane1.getVerticalScrollBar().setValue(areaScrollPane1.getVerticalScrollBar().getMaximum());
		areaLog.append(fNick + " says", msg);
		areaScrollPane1.getVerticalScrollBar().setValue(areaScrollPane1.getVerticalScrollBar().getMaximum());

		//System.out.println("areaLog.getHeight()-2" + areaLog.getHeight());
		this.setTitle(fNick);
	}
	
	public void showByeMessage(String contact) {
		this.setTitle(fNick + " [LEFT CONVERSATION]");
	}
	
	public void showTypingMessage(String contact) {
		this.setTitle(fNick + " [TYPING]");
	}
	
	/**
	 * Method getLogin.
	 * @return String
	 */
	public String getLogin() {
		return fLogin;
	}
	
	private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        setVisible(false);
		if (fModel instanceof MSNProtocol) {
			MSNProtocol msn = (MSNProtocol)fModel;
			msn.sendByeMessage(fLogin);
		}
		areaLog.close();
    }

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("e")) {
			Point p = buttonEmoticon.getLocation();
			if (es == null) {
				es = new EmoticonSelector((int)p.getX(), (int)p.getY(), boxMessage);
			} else {
				es.setVisible(true);
			}
		}
		if(e.getActionCommand().equals("close")) {
			setVisible(false);
			if (fModel instanceof MSNProtocol) {
				MSNProtocol msn = (MSNProtocol)fModel;
				msn.sendByeMessage(fLogin);
			}
			areaLog.close();
		}
		if(e.getActionCommand().equals("send")) {
			if(boxMessage.getText().equals("")) {
				// niks doen, nie verzenden
			}
			else {
				areaLog.append("You say", (String)boxMessage.getText());

				try {
					fModel.sendMessage(fLogin, (String)boxMessage.getText());
				} 
				catch(ICQException ie) {
					JOptionPane.showMessageDialog(this, ie.getMessage());
				}
				catch (ProtocolException err) {
					System.out.println(err.getMessage());
				}
				boxMessage.setText("");
			}
		}
	}	
}
