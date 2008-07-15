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

import messenger.Protocols;
import messenger.AContact;
import messenger.AProtocol;
import messenger.UnknownContactException;
import messenger.ICQ.ICQProtocol;
import messenger.ICQ.ICQExceptions.ICQException;
import messenger.MSN.MSNException;
import messenger.MSN.MSNProtocol;
import messenger.Yahoo.YahooProtocol;
import messenger.Yahoo.YahooExceptions.YahooException;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.lang.String;
import java.io.File;

/**
 * @author Bart Van Rompaey & Benny Van Aerschot
 * @version $Revision: 1.20 $
 * @date $Date: 2003/05/25 19:08:28 $
 * 
 */
public class View implements ActionListener {
	JFrame mess;
	JPanel paneMess;
	JTable contactTable;
	TableSorter sorter;
	JScrollPane scrollPane;
	JMenuBar menuBar;
	JMenu ICQmenu, menuLists, menuHelp, menuQuit, ICQMenuChangeStatus;
	JMenu MSNmenu, MSNMenuChangeStatus;
	JMenu Yahoomenu, YahooMenuChangeStatus;
	
	JMenuItem ICQItemAddUser, ICQItemServerList, ICQItemQuit;
	JMenuItem ICQItemLogin;
	JMenuItem MSNItemAddUser, MSNItemServerList, MSNItemQuit;
	JMenuItem MSNItemLogin;
	JMenuItem YahooItemAddUser, YahooItemServerList, YahooItemQuit;
	JMenuItem YahooItemLogin;
	
	JMenuItem ICQItemOnline, ICQItemAway, ICQItemNA, ICQItemDND;
	JMenuItem ICQItemIgnoreList;
	JMenuItem MSNItemOnline, MSNItemAway, MSNItemPhone, MSNItemLunch, MSNItemBRB, MSNItemBusy;
	JMenuItem MSNItemIgnoreList;
	JMenuItem YahooItemOnline;
	JMenuItem YahooItemIgnoreList;
	JMenuItem itemHelp, itemAbout, itemDumpOn, itemDumpOff;
	
	JMenu menuStatus;
	JMenuItem itemVisible, itemInvisible;
	private Protocols model;
	private ContactTableModel contactModel;
	private Vector boxList;
	private Preferences ICQPrefs, MSNPrefs, YahooPrefs;
	private AddBuddy ICQAddUser, MSNAddUser, YahooAddUser;
	private ImageIcon[] icon = { null, null, null, null, null, null, null, null, null, null, null, null, null };
	private DumpListener fDumpListener;
	
	private Thread fMSNThread;
	private Thread fICQThread;
	private Thread fYahooThread;
	
	/**
	 * Method View.
	 * @param Protocols aManager
	 */
	public View(Protocols aManager) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch (Exception e) { }
		
		icon[0] = new ImageIcon("../multimedia/online.jpg");
		icon[1]	= new ImageIcon("../multimedia/away.jpg");
		icon[2] = new ImageIcon("../multimedia/na.jpg");
		icon[3]	= new ImageIcon("../multimedia/offline.jpg");
		icon[4] = new ImageIcon("../multimedia/msnnln.png");
		icon[5] = new ImageIcon("../multimedia/msnawy.png");
		icon[6] = new ImageIcon("../multimedia/msnbsy.png");
		icon[7] = new ImageIcon("../multimedia/msnfln.png");
		icon[8] = new ImageIcon("../multimedia/unknown.jpg");
		icon[9] = new ImageIcon("../multimedia/dnd.jpg");
		icon[10] = new ImageIcon("../multimedia/occ.jpg");
		icon[11] = new ImageIcon("../multimedia/yahoo.png");
		icon[12] = new ImageIcon("../multimedia/yahoooffline.png");
		
		
		model = aManager;
		mess = new JFrame("Anastacia");
		mess.setIconImage(icon[3].getImage());
		
		//mess.setSize(120, 400);
		mess.setSize(new Dimension(170, 400));
		mess.setLocation(300,300);
		
		paneMess = new JPanel();
		boxList = new Vector();
		ICQPrefs = new Preferences(aManager.getProtocol(0));
		MSNPrefs = new Preferences(aManager.getProtocol(1));
		YahooPrefs = new Preferences(aManager.getProtocol(2));
		ICQAddUser = new AddBuddy(aManager.getProtocol(0));
		MSNAddUser = new AddBuddy(aManager.getProtocol(1));
		YahooAddUser = new AddBuddy(aManager.getProtocol(2));
		
		mess.setSize(new Dimension(200, 400));
		addMessWidgets();
	}
	
	public void addMessWidgets() {
		menuBar = new JMenuBar();
	
		// Create the main-menus
		MSNmenu = new JMenu("MSN");
		menuBar.add(MSNmenu);
		ICQmenu = new JMenu("ICQ");
		menuBar.add(ICQmenu);
		Yahoomenu = new JMenu("Yahoo");
		menuBar.add(Yahoomenu);
		menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);
		menuQuit = new JMenu("Quit");
		JMenuItem quit = new JMenuItem("Quit");
		quit.setActionCommand("Quit");
		quit.addActionListener(this);
		menuQuit.add(quit);
		menuBar.add(menuQuit);
		ICQMenuChangeStatus = new JMenu("Change Status");
		MSNMenuChangeStatus = new JMenu("Change Status");
		YahooMenuChangeStatus = new JMenu("Change Status");
		
		// Create the menu items
		ICQItemAddUser = new JMenuItem("Add User ...");
		ICQItemLogin = new JMenuItem("Login");
		ICQItemQuit = new JMenuItem("Logout");
	
		ICQItemOnline = new JMenuItem("Online");
		ICQItemAway = new JMenuItem("Away");
		ICQItemNA = new JMenuItem("N/A");
		ICQItemDND = new JMenuItem("DND");

		MSNItemAddUser = new JMenuItem("Add User ...");
		MSNItemLogin = new JMenuItem("Login");
		MSNItemQuit = new JMenuItem("Logout");
	
		MSNItemOnline = new JMenuItem("Online");
		MSNItemAway = new JMenuItem("Away");
		MSNItemBusy = new JMenuItem("Busy");
		MSNItemBRB = new JMenuItem("Be right back");
		MSNItemPhone = new JMenuItem("On the phone");
		MSNItemLunch = new JMenuItem("Out to lunch");
		
		YahooItemAddUser = new JMenuItem("Add User ...");
		YahooItemLogin = new JMenuItem("Login");
		YahooItemQuit = new JMenuItem("Logout");
	
		YahooItemOnline = new JMenuItem("Online");

		// Add the sub-menus
		ICQmenu.add(ICQMenuChangeStatus);
		ICQmenu.add(ICQItemAddUser);
		ICQmenu.add(ICQItemLogin);
		ICQmenu.add(ICQItemQuit);
		
		ICQMenuChangeStatus.add(ICQItemOnline);
		ICQMenuChangeStatus.add(ICQItemAway);
		ICQMenuChangeStatus.add(ICQItemNA);
		ICQMenuChangeStatus.add(ICQItemDND);
		
		MSNmenu.add(MSNMenuChangeStatus);
		MSNmenu.add(MSNItemAddUser);
		MSNmenu.add(MSNItemLogin);
		MSNmenu.add(MSNItemQuit);
		
		MSNMenuChangeStatus.add(MSNItemOnline);
		MSNMenuChangeStatus.add(MSNItemAway);
		MSNMenuChangeStatus.add(MSNItemBusy);
		MSNMenuChangeStatus.add(MSNItemBRB);
		MSNMenuChangeStatus.add(MSNItemPhone);
		MSNMenuChangeStatus.add(MSNItemLunch);
		
		Yahoomenu.add(YahooMenuChangeStatus);
		Yahoomenu.add(YahooItemAddUser);
		Yahoomenu.add(YahooItemLogin);
		Yahoomenu.add(YahooItemQuit);
		
		YahooMenuChangeStatus.add(YahooItemOnline);

		//itemHelp = new JMenuItem("Help");
		itemAbout = new JMenuItem("About");
		itemDumpOn = new JMenuItem("Dump On");
		itemDumpOff = new JMenuItem("Dump Off");		
		//menuHelp.add(itemHelp);
		menuHelp.add(itemAbout);
		menuHelp.add(itemDumpOn);
		menuHelp.add(itemDumpOff);
		
		ICQItemOnline.setActionCommand("ICQstatusonline");
		ICQItemAway.setActionCommand("ICQstatusaway");
		ICQItemNA.setActionCommand("ICQstatusna");
		ICQItemDND.setActionCommand("ICQstatusdnd");
		ICQItemAddUser.setActionCommand("ICQadduser");
		ICQItemLogin.setActionCommand("ICQlogin");
		ICQItemQuit.setActionCommand("ICQquit");
		
		ICQItemOnline.addActionListener(this);
		ICQItemAway.addActionListener(this);
		ICQItemNA.addActionListener(this);
		ICQItemDND.addActionListener(this);
		ICQItemAddUser.addActionListener(this);
		ICQItemLogin.addActionListener(this);
		ICQItemQuit.addActionListener(this);
		
		MSNItemAddUser.setActionCommand("MSNadduser");
		MSNItemOnline.setActionCommand("MSNstatusonline");
		MSNItemAway.setActionCommand("MSNstatusaway");
		MSNItemBRB.setActionCommand("MSNstatusbrb");
		MSNItemBusy.setActionCommand("MSNstatusbusy");
		MSNItemPhone.setActionCommand("MSNstatusphone");
		MSNItemLunch.setActionCommand("MSNstatuslunch");
		MSNItemLogin.setActionCommand("MSNlogin");
		MSNItemQuit.setActionCommand("MSNquit");
		
		MSNItemAddUser.addActionListener(this);
		MSNItemOnline.addActionListener(this);
		MSNItemAway.addActionListener(this);
		MSNItemLunch.addActionListener(this);
		MSNItemBusy.addActionListener(this);
		MSNItemBRB.addActionListener(this);
		MSNItemPhone.addActionListener(this);
		MSNItemLogin.addActionListener(this);
		MSNItemQuit.addActionListener(this);
		
		YahooItemAddUser.setActionCommand("Yahooadduser");
		YahooItemOnline.setActionCommand("Yahoostatusonline");
		YahooItemLogin.setActionCommand("Yahoologin");
		YahooItemQuit.setActionCommand("Yahooquit");
		
		YahooItemAddUser.addActionListener(this);
		YahooItemOnline.addActionListener(this);
		YahooItemLogin.addActionListener(this);
		YahooItemQuit.addActionListener(this);
		
		//itemHelp.setActionCommand("Help");
		//itemHelp.addActionListener(this);
		itemAbout.setActionCommand("About");
		itemAbout.addActionListener(this);
		itemDumpOn.setActionCommand("Dump On");
		itemDumpOff.setActionCommand("Dump Off");
		itemDumpOn.addActionListener(this);
		itemDumpOff.addActionListener(this);
		
		paneMess.setLayout(new BorderLayout());
		// Set this instance as the application's menu bar
		mess.setJMenuBar( menuBar );	
		
		// add the panel to the frame
		mess.getContentPane().add(paneMess);

		contactModel = new ContactTableModel(icon);
		//sorter = new TableSorter(contactModel);
		contactTable = new JTable(/*sorter*/contactModel);
		//sorter.addMouseListenerToHeaderInTable(contactTable);
		
		//System.out.println(contactTable.getRowHeight());
		contactTable.setRowHeight(25);
		contactTable.setShowHorizontalLines(false);
		contactTable.setShowVerticalLines(false);
		contactTable.setSelectionBackground(contactTable.getBackground());
		contactTable.setSelectionForeground(contactTable.getForeground());

		//System.out.println("Nr of icons in use: " + icon.length);
		for(int i = 0; i < icon.length; i++) {
			icon[i].setImageObserver(contactTable);
		}
		
		TableColumnModel columnModel = contactTable.getColumnModel();
		TableColumn column = columnModel.getColumn(0);
		// fix some of the columns width
		column.setWidth(35);
		column.setMaxWidth(35);
		column.setMinWidth(35);
		column.setResizable(false);
		
		column = columnModel.getColumn(1);
		//column.setWidth(0);
		column.setMaxWidth(-1);
		column.setMinWidth(-1);
		column.setPreferredWidth(-1);
		column.setResizable(false);

		column = columnModel.getColumn(2);
		TableColumnModel tcm = contactTable.getColumnModel();
		column = tcm.getColumn (2);
		TableCellRenderer renderer = new CustomCellRenderer("", "");
		column.setCellRenderer (renderer);

		column.setWidth(90);
		
		//sorter = new TableSorter(contactModel);
		//contactTable = new JTable(sorter);
		//sorter.addMouseListenerToHeaderInTable(contactTable);
        
		scrollPane = new JScrollPane(contactTable);
		paneMess.add(scrollPane, BorderLayout.CENTER);
		
		MouseListener ml = new MouseAdapter() {
    		public void mouseClicked(MouseEvent e) {		// oorspronkelijk mouseReleased

      			//if (e.isMetaDown()) {									// dit opzetten -> rechtermuis klikken zorgt voor messagebox
        		
        			int row = contactTable.rowAtPoint(e.getPoint());
        			
        			try {
        				//MessageBox mb = getMessageBox((String)sorter.getValueAt(row, 1));
						MessageBox mb = getMessageBox((String)contactModel.getValueAt(row, 1));
						if(mb != null) {
							mb.setVisible(true);
						}
						else {
							//mb = new MessageBox(model.getProtocolContainingContact((String)sorter.getValueAt(row, 1)), (String)sorter.getValueAt(row, 1));				
							AProtocol ap = model.getProtocolContainingContact((String)contactModel.getValueAt(row, 1));
							if (ap instanceof MSNProtocol) {		// rechterklik zorgt al voor opzetten van een session
								try {
									ap.sendMessage( (String)contactModel.getValueAt(row, 1), null);
								} catch (Exception err) {
									//System.out.println(err.getMessage());
								}
							}
							mb = new MessageBox(model.getProtocolContainingContact((String)contactModel.getValueAt(row, 1)), (String)contactModel.getValueAt(row, 1), (String)contactModel.getValueAt(row, 2));				
						
							boxList.add(mb);
						}
        			}
        			catch(UnknownContactException ex) {
        				//	
        			}
      			//}
    		}
    	};
    	
    	contactTable.addMouseListener(ml);

		// exit when the window is closed
		mess.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Show the messenger

		mess.validate();
		mess.setVisible(true);
	}
	
	private MessageBox getMessageBox(String uin) {
		//System.out.print(uin);
		for(int i = 0; i < boxList.size(); i++) {
			if(((MessageBox)boxList.elementAt(i)).getLogin().equals(uin)) {
				return (MessageBox)boxList.elementAt(i);
			}
		}
		//System.out.println(" ... nie gevonden!");
		return null;
	}
	
	  // act on JTable row right-click
	public void updateContacts() {
		//System.out.println("Nr of contacts on the contactlist: "+model.getContactList().size());
		
		for(int i = 0; i < model.getContactList().size(); i++) {
			AContact contact = (AContact)model.getContactList().elementAt(i);
			//System.out.println(contact.getLogin()+" "+contact.getStatus());
			contactModel.updateContact(contact);
		}
	}
	
	public void showMSNByeMessage(String sender) {
		MessageBox mb = getMessageBox(sender);
		mb.showByeMessage(sender);
	}
	
	// check this
	public void showMSNTypingMessage(String sender) {
		MessageBox mb = getMessageBox(sender);
		if(mb != null) {
			mb.setVisible(true);
		}
		else {
			try {
				AProtocol ap = model.getProtocolContainingContact(sender);
				AContact a = ap.getContact(sender);
				mb = new MessageBox(ap, sender, a.getNick());
				boxList.add(mb);
				mb.showTypingMessage(sender);
			}
			catch(UnknownContactException e) {
				//System.out.println("unknown contact");	
			}
		}
		
		mb.showTypingMessage(sender);	
	}
	
	public void showMSNMessage(String s) {
		String msg = s;
		
		int index = msg.indexOf(' ');
		String sender = msg.substring(0, index);
		msg = msg.substring(index + 1);
		
		MessageBox mb = getMessageBox(sender);
		
		try {
			if(mb != null) {
				mb.setVisible(true);
			}
			else {
				
					AProtocol ap = model.getProtocolContainingContact(sender);
					AContact a = ap.getContact(sender);
					mb = new MessageBox(ap, sender, a.getNick());
					boxList.add(mb);		
			}
			mb.showMessage(msg);
		}
		catch(UnknownContactException e) {
			//System.out.println("unknown contact");	
		}		
	}
	
	public void showDialog(String msg) {		
		JLabel jl = new JLabel("<html>" + msg + "</html>", new ImageIcon(".." + File.separator + "multimedia" + File.separator + "info.gif"), JLabel.CENTER);
		JPanel jp = new JPanel();
		jp.add(jl);
		JFrame frame = new JFrame("Notification");
	    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    frame.getContentPane().add(jp);
	    frame.setSize(250, 50);
	    frame.show();
	}
	
	public void showMessage(String s) {
		String msg = "";
		String login = "";
		int i = 0;
		
		while(s.charAt(i) != ' ') {
			login += s.charAt(i);
			i++;
		}
		
		msg = s.substring(i+1);
		//System.out.println(login+" says: "+msg);
		
		try {
			MessageBox mb = getMessageBox(login);
			if(mb != null) {	
				mb.setVisible(true);
			}
			else {
				AProtocol ap = model.getProtocolContainingContact(login);
				AContact a = ap.getContact(login);
				mb = new MessageBox(ap, login, a.getNick());
				boxList.add(mb);		
			}		
			mb.showMessage(msg);
		}
		catch(UnknownContactException e) {
			//System.out.println("unknown contact");	
		}
	}
	
	public void msnLogOff() {
		ICQProtocol icqprotocol = (ICQProtocol)model.getProtocol(0);
		MSNProtocol msnprotocol = (MSNProtocol)model.getProtocol(1);
		YahooProtocol yprotocol = (YahooProtocol)model.getProtocol(2);
		Vector icqcontacts = icqprotocol.getContactList();
		Vector ycontacts = yprotocol.getContactList();
		contactModel.clear();
			
			
		if (icqcontacts != null) {
			for (int i = 0; i < icqcontacts.size(); ++i) {
				contactModel.updateContact((AContact)icqcontacts.elementAt(i));
			}
		}
			
		if (ycontacts != null) {
			for (int i = 0; i < ycontacts.size(); ++i) {
				contactModel.updateContact((AContact)ycontacts.elementAt(i));
			}
		}

		msnprotocol.shutdown();
		if (fMSNThread != null) { 
			fMSNThread.interrupt();
			model.refreshProtocol(1);
			mess.validate();
		}
		MSNPrefs.setPrefsFilledIn(false);
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 *
	 * Implementation of ActionListener interface.
	 */
    public void actionPerformed(ActionEvent e) {
    	if(e.getActionCommand().equals("ICQquit")) {
    		// nog geen logout en shutdown in AProtocol?
    		/*for(int i = 0; i < model.getNumberOfProtocols(); i++) {
    			((AProtocol)model.getProtocol(i)).logout();
    			((AProtocol)model.getProtocol(i)).shutdown();
    		}*/
		
		//	System.exit(0);
			
			ICQProtocol icqprotocol = (ICQProtocol)model.getProtocol(0);
			MSNProtocol msnprotocol = (MSNProtocol)model.getProtocol(1);
			YahooProtocol yprotocol = (YahooProtocol)model.getProtocol(2);
			Vector msncontacts = msnprotocol.getContactList();
			Vector ycontacts = yprotocol.getContactList();
			contactModel.clear();
			
			
			if (msncontacts != null) {
				for (int i = 0; i < msncontacts.size(); ++i) {
					contactModel.updateContact((AContact)msncontacts.elementAt(i));
				}
			}
			
			if (ycontacts != null) {
				for (int i = 0; i < ycontacts.size(); ++i) {
					contactModel.updateContact((AContact)ycontacts.elementAt(i));
				}
			}

			icqprotocol.shutdown();
			if (fICQThread != null) {
				fICQThread.interrupt();
				model.refreshProtocol(0);
				mess.validate();
			}
		}
		else if(e.getActionCommand().equals("ICQlogin")) {
			ICQPrefs = new Preferences((ICQProtocol)model.getProtocol(0));
			ICQPrefs.setVisible(true);			
			
			fICQThread = new Thread() {
				public void run() {
					try {
						while (! ICQPrefs.prefsFilledIn()) {
							sleep(100);
						}
						
   						((ICQProtocol)model.getProtocol(0)).login();
					}
					catch(ICQException e) {
						JOptionPane.showMessageDialog(mess, e.getMessage());
					}
					catch (InterruptedException e) {
					
					}
				}
			};
				
				fICQThread.start();
		}
		else if(e.getActionCommand().equals("ICQstatusonline")) {
			try {
				((ICQProtocol)model.getProtocol(0)).changeStatus("ONLINE");	
			}
			catch(ICQException ie) {
				JOptionPane.showMessageDialog(mess, ie.getMessage());
			}
			// doesn't seem to work
			mess.setIconImage(icon[0].getImage());
		}
		else if(e.getActionCommand().equals("ICQstatusaway")) {
			try {
				((ICQProtocol)model.getProtocol(0)).changeStatus("AWAY");
			}
			catch(ICQException ie) {
				JOptionPane.showMessageDialog(mess, ie.getMessage());
			}
			
			// doesn't seem to work
			mess.setIconImage(icon[1].getImage());	
		}
		else if(e.getActionCommand().equals("ICQstatusNA")) {
			try {
				((ICQProtocol)model.getProtocol(0)).changeStatus("NA");
			}
			catch(ICQException ie) {
				JOptionPane.showMessageDialog(mess, ie.getMessage());
			}
			
			// doesn't seem to work
			mess.setIconImage(icon[2].getImage());		
		}
		else if(e.getActionCommand().equals("ICQstatusDND")) {
			try {
				((ICQProtocol)model.getProtocol(0)).changeStatus("DND");	
			}
			catch(ICQException ie) {
				JOptionPane.showMessageDialog(mess, ie.getMessage());
			}
		}
		else if(e.getActionCommand().equals("ICQserverlist")) {
			((ICQProtocol)model.getProtocol(0)).getContactListFromServer();
		}
		else if(e.getActionCommand().equals("ICQshowlist")) {
			try {
				((ICQProtocol)model.getProtocol(0)).retrieveContactList();
			}
			catch(ICQException ie) {
				JOptionPane.showMessageDialog(mess, ie.getMessage());
			}
		}
		else if(e.getActionCommand().equals("ICQadduser")) {
			ICQAddUser.setVisible(true);
		}
		else if(e.getActionCommand().equals("MSNquit")) {
			//System.exit(0);
			this.msnLogOff();
		}
		else if(e.getActionCommand().equals("MSNlogin")) {
			MSNPrefs = new Preferences((MSNProtocol)model.getProtocol(1));
			MSNPrefs.setVisible(true);
			
			fMSNThread = new Thread() {
   					public void run() {
   						try {
							while (! MSNPrefs.prefsFilledIn()) {
								sleep(100);
							}
   							
       						((MSNProtocol)model.getProtocol(1)).login();
							//((MSNProtocol)model.getProtocol(1)).getServerContactList();
							while (! ((MSNProtocol)model.getProtocol(1)).contactListRetrieved()) {
								sleep(100);
							}
							
							// lijkt toch fijn te werken
							((MSNProtocol)model.getProtocol(1)).getContactList();
							updateContacts();
							MSNPrefs.setPrefsFilledIn(false);
   						}
   						catch(MSNException e) {
							JOptionPane.showMessageDialog(mess, e.getMessage());
   						}
   						catch (Exception e) {
   							//JOptionPane.showMessageDialog(mess, e.getMessage());
   						}
   					}
				};
				
			fMSNThread.start();
		}
		else if(e.getActionCommand().equals("MSNadduser")) {
			MSNAddUser.setVisible(true);
			//((MSNProtocol)model.getProtocol(1)).addContact("mikoyan82@hotmail.com", "dumbo");
			updateContacts();
		}
	//	else if(e.getActionCommand().equals("MSNshowlist")) {
	//		//((MSNProtocol)model.getProtocol(1)).getServerContactList();	// fetch it again so that adduser will be noticed
	//		((MSNProtocol)model.getProtocol(1)).getContactList();
	//		updateContacts();
	//	}
		else if(e.getActionCommand().equals("MSNstatusonline")) {
			if (((MSNProtocol)model.getProtocol(1)).contactListRetrieved()) {
				((MSNProtocol)model.getProtocol(1)).changeStatus("NLN");
			}
		}
		else if(e.getActionCommand().equals("MSNstatusaway")) {
			if (((MSNProtocol)model.getProtocol(1)).contactListRetrieved()) {
				((MSNProtocol)model.getProtocol(1)).changeStatus("AWY");
			}
		}
		else if(e.getActionCommand().equals("MSNstatusbusy")) {
			if (((MSNProtocol)model.getProtocol(1)).contactListRetrieved()) {
				((MSNProtocol)model.getProtocol(1)).changeStatus("BSY");
			}
		}
		else if(e.getActionCommand().equals("MSNstatusbrb")) {
			if (((MSNProtocol)model.getProtocol(1)).contactListRetrieved()) {
				((MSNProtocol)model.getProtocol(1)).changeStatus("BRB");
			}
		}
		else if(e.getActionCommand().equals("MSNstatusphone")) {
			if (((MSNProtocol)model.getProtocol(1)).contactListRetrieved()) {
				((MSNProtocol)model.getProtocol(1)).changeStatus("PHN");
			}
		}
		else if(e.getActionCommand().equals("MSNstatuslunch")) {
			if (((MSNProtocol)model.getProtocol(1)).contactListRetrieved()) {
				((MSNProtocol)model.getProtocol(1)).changeStatus("LUN");
			}
		}		
		//else if(e.getActionCommand().equals("MSNpreferences")) {
		//	MSNPrefs.setVisible(true);	
		//}
		else if(e.getActionCommand().equals("Yahooquit")) {
			//System.exit(0);
			ICQProtocol icqprotocol = (ICQProtocol)model.getProtocol(0);
			MSNProtocol msnprotocol = (MSNProtocol)model.getProtocol(1);
			YahooProtocol yprotocol = (YahooProtocol)model.getProtocol(2);
			Vector msncontacts = msnprotocol.getContactList();
			Vector icqcontacts = icqprotocol.getContactList();
			contactModel.clear();
			
			
			if (msncontacts != null) {
				for (int i = 0; i < msncontacts.size(); ++i) {
					contactModel.updateContact((AContact)msncontacts.elementAt(i));
				}
			}
			
			if (icqcontacts != null) {
				for (int i = 0; i < icqcontacts.size(); ++i) {
					contactModel.updateContact((AContact)icqcontacts.elementAt(i));
				}
			}

			yprotocol.shutdown();
			if (fYahooThread != null) {
				fYahooThread.interrupt();
				model.refreshProtocol(2);
				mess.validate();
			}
		}
		else if(e.getActionCommand().equals("Yahoologin")) {
			YahooPrefs = new Preferences((YahooProtocol)model.getProtocol(2));
			YahooPrefs.setVisible(true);		
			
			fYahooThread = new Thread() {
   					public void run() {
   						try {
							while (! YahooPrefs.prefsFilledIn()) {
								sleep(100);
							}
   							
       						((YahooProtocol)model.getProtocol(2)).login();
   						}
   						catch(YahooException e) {
   							JOptionPane.showMessageDialog(mess, e.getMessage());
   						} catch (InterruptedException e) {
   						
   						}
   					}
				};
				
			fYahooThread.start();
		}
		else if(e.getActionCommand().equals("Yahooadduser")) {
	
		}
		else if(e.getActionCommand().equals("Yahoostatusonline")) {
			
		}
		else if(e.getActionCommand().equals("MSNstatusaway")) {
			
		}
		else if(e.getActionCommand().equals("About")) {
			JOptionPane.showMessageDialog(mess, "Anastacia\n\n"+
										"by Benny Van Aerschot & Bart Van Rompaey");
		}
		else if(e.getActionCommand().equals("Dump On")) {
			if (fDumpListener != null) {
				System.out.println("dump on");
				fDumpListener.dumpTriggered();
			}	
		}
		else if(e.getActionCommand().equals("Dump Off")) {
			if (fDumpListener != null) {
				System.out.println("dump off");
				fDumpListener.dumpUnTriggered();
			}	
		}
		else if(e.getActionCommand().equals("Quit")) {
			System.exit(0);	
		}
	}
	
	public void removeDumpListener() {
		fDumpListener = null;
	}
	
	public void setDumpListener(DumpListener dumpListener) {
		fDumpListener = dumpListener;
	}
	
	public DumpListener getListener() {
		return fDumpListener;
	}
	
}
