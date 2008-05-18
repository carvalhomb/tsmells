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

package messenger.Yahoo;

import java.io.*;
import java.net.*;
import java.util.Vector;
import messenger.Yahoo.YahooEncrypt;

import messenger.*;
import messenger.Yahoo.YahooExceptions.*;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.7 $
 * @date $Date: 2003/05/25 10:15:51 $
 * 
 */
public class YahooProtocol extends AProtocol {
	
	private DataBuffer fControlData;
	private Vector fContactList = null;
	private String fLastContact = "";
	private String fLastMessage = "";
	
	/**
	 * Method YahooProtocol.
	 * @param DataBuffer feedback class to gui
	 * @param String login
	 * @param String password
	 * @param String name of Yahoo
	 * @param int port number
	 * 
	 * @pre assert aUin.length() > 0;
	 * @pre assert password != "";
	 * 
	 * @post assert getLoginName() == aUin;
	 * @post assert getEncPasswd() != "";
	 */
	public YahooProtocol(DataBuffer controlData, String aUin, String password, String serverName, int port) {
		super(serverName, port);
	
		assert aUin.length() > 0;
		assert password != "";
		
		this.setLoginName(aUin);
		// nieuw encryptiealgoritme this.setEncPasswd(fU.ICQEncryptPass(password));
		this.setEncPasswd(password);
		
		fContactList = new Vector();
		fControlData = controlData;
		try {
			fFactory = new PacketFactory().getFactory(this);
		}
		catch(UnknownProtocolException e) {
			assert(true);
		}
		
		assert getLoginName() == aUin;
		assert getEncPasswd() != "";
	}
	
	public DataBuffer getControlData() {
		return fControlData;
	}
	
	public YahooProtocol(DataBuffer controlData, String serverName, int port) {
		super(serverName, port);
	
		fContactList = new Vector();
		fControlData = controlData;
		try {
			fFactory = new PacketFactory().getFactory(this);
		}
		catch(UnknownProtocolException e) {
			assert(true);
		}
	}
	
	/**
	 * @see messenger.AProtocol#getNumberOfContacts()
	 * 
	 * @return fContactList.size();
	 * @invariant assert fContactList != null;
	 */
	protected int getNumberOfContacts() {
		assert fContactList != null;
		
		return fContactList.size();
	}
	
	/**
	 * Method addContact.
	 * @param YahooContact the contact you want to add to your list
	 * 
	 * @pre assert ic != null
	 * 
	 * @post assert this.getNumberOfContacts() == old+1;
	 */
	public void addContact(YahooContact ic) {
		assert ic != null;
		int old = this.getNumberOfContacts();
		
		fContactList.addElement(ic);
		
		assert this.getNumberOfContacts() == old + 1;	
	}
	
	/**
	 * @see messenger.AProtocol#getContactList()
	 * 
	 * @return Vector list of contacts
	 */
	public Vector getContactList() {
		return fContactList;	
	}

	/**
	 * Method sendPacket.
	 * @param IPacket packet
	 * @throws IOException
	 * 
	 * @pre assert this.getServer() != null;
	 * @pre assert packet != null;
	 * @pre assert packet.getLength() > 0;
	 */
	protected void sendPacket(IPacket packet) throws IOException {
			assert this.getServer() != null;
			assert packet != null;
			assert packet.getLength() > 0;
			
			DataOutputStream fOutToServer = new DataOutputStream(getSocket().getOutputStream());
			YahooPacket p = (YahooPacket)packet;
			InetAddress ia = getSocket().getInetAddress();
					
       		try {
       			fOutToServer.write(p.getContentByte(), 0, p.getLength());
       		}
       		catch (IOException e) {
       			//e.printStackTrace();
       			System.out.println("IOException while trying to send a Yahoo Packet");
       		}
       		catch (Exception e) {
       			System.out.println("Exception while sending Yahoo Packet");
       		}
       		
       		fControlData.setDump("sending to "+ia.getHostAddress()+" "
       												+p.getKindOfPacket()+" ("
       												+p.getLength()+" bytes): \n"
       												+Utils.printableHexString(p.getContentHex()));  		
	}
	
	/**
	 * Method connect.
	 */
	public void login() throws YahooException {
		String s = "";
		DataOutputStream fOutToServer;
		DataInputStream fInFromServer;
		
		try {
			this.connect();
			
			/*System.out.println(this.getLoginName());
			System.out.println(this.getEncPasswd());
			System.out.println(this.getServer());
			System.out.println(this.getPort());*/
			
			fInFromServer = new DataInputStream(getSocket().getInputStream());
			InetAddress ia1 = getSocket().getInetAddress();
			
			Vector v = new Vector();	
     		YahooPacket cli_hello = (YahooPacket)fFactory.createOutgoingPacket("CLI_HELLO", v);
    		sendPacket(cli_hello);
			
			//System.out.println("CLI_HELLO");
			
			s = getServerMessage();
			YahooPacket srv_hello = (YahooPacket)fFactory.createIncomingPacket(s);
			
			/*fControlData.setDump("receiving from "+ia1.getHostAddress()+" "+srv_hello.getKindOfPacket()
														+" ("+srv_hello.getLength()+" bytes):\n"+fU.printableHexString(srv_hello.getContentHex()));
  */
			//System.out.println("INCOMING");
			
			// CLI_AUTH
     		v = new Vector(); 		
     		v.addElement(Utils.HexFromString(getLoginName()));
     		YahooPacket cli_login1 = (YahooPacket)fFactory.createOutgoingPacket("CLI_AUTH", v);
    		sendPacket(cli_login1);
    		
    		//System.out.println("CLI_AUTH");
    		
     		s = getServerMessage();
			YahooPacket srv_auth = (YahooPacket)fFactory.createIncomingPacket(s);
			//System.out.println("hier");
			fControlData.setDump("receiving from "+ia1.getHostAddress()+" "+srv_auth.getKindOfPacket()
														+" ("+srv_auth.getLength()+" bytes):\n"+Utils.printableHexString(srv_auth.getContentHex()));

  			//System.out.println("INCOMING");
  			
  			String[] strings = new YahooEncrypt().encrypt(((YahooSRV_AUTHPacket)srv_auth).getChallenge(), getEncPasswd(), getLoginName());
  			
  			/*System.out.println("string1: "+strings[0]);
  			System.out.println("string2: "+strings[1]);
  			System.out.println("login: "+getLoginName());*/
  			
  			// CLI_AUTH_RESP
			v = new Vector();
			v.addElement(Utils.HexFromString(getLoginName()));
			v.addElement(Utils.HexFromString(strings[0]));
			v.addElement(Utils.HexFromString(strings[1]));		
			YahooPacket cli_auth_resp = (YahooPacket)fFactory.createOutgoingPacket("CLI_AUTH_RESP", v);
			sendPacket(cli_auth_resp);
			
			/*
			// SRV_AUTH_RESP ==> WRONG PASSWORD
			s = getServerMessage();
			//System.out.println(fU.printableHexString(s));
			YahooPacket srv_auth_resp = (YahooPacket)fFactory.createIncomingPacket(s);
			fControlData.setDump("receiving from "+ia1.getHostAddress()+" "+srv_auth_resp.getKindOfPacket()
														+" ("+srv_auth_resp.getLength()+" bytes):\n"+fU.printableHexString(srv_auth_resp.getContentHex()));
  			*/
			
			// SRV_LIST
			
			s = getServerMessage();
			//System.out.println(Utils.printableHexString(s));
			YahooPacket srv_list = (YahooPacket)fFactory.createIncomingPacket(s);
			fControlData.setDump("receiving from "+ia1.getHostAddress()+" "+srv_list.getKindOfPacket()
														+" ("+srv_list.getLength()+" bytes):\n"+Utils.printableHexString(srv_list.getContentHex()));
  			
  			Vector list = ((YahooSRV_LISTPacket)srv_list).getList();
  			for(int j = 0; j < list.size(); j++) {
  				YahooContact c = (YahooContact)list.elementAt(j);
				((YahooContact)list.elementAt(j)).setStatus("YOFFLINE");
				fContactList.addElement((YahooContact)list.elementAt(j));	
  			}
  			
  			// SRV_LOGON
			/*s = getServerMessage();
			YahooPacket srv_logon = (YahooPacket)fFactory.createIncomingPacket(s);
			fControlData.setDump("receiving from "+ia1.getHostAddress()+" "+srv_logon.getKindOfPacket()
														+" ("+srv_logon.getLength()+" bytes):\n"+Utils.printableHexString(srv_logon.getContentHex()));*/
  			
  			// CLI_PASSTHROUGH2
  			v = new Vector();
  			v.addElement(Utils.HexFromString(getLoginName()));
  			YahooPacket cli_pass = (YahooPacket)fFactory.createOutgoingPacket("CLI_PASSTHROUGH2", v);
			sendPacket(cli_pass);
			
			// CLI_0015
  			v = new Vector();
  			YahooPacket cli_0015 = (YahooPacket)fFactory.createOutgoingPacket("CLI_0015", v);
			sendPacket(cli_0015);
  			
  			// CLI_ISBACK
			v = new Vector();	
			YahooPacket cli_isback = (YahooPacket)fFactory.createOutgoingPacket("CLI_ISBACK", v);
			sendPacket(cli_isback);
			
  			
  			this.setLoggedIn(true);
			this.setPriority(MIN_PRIORITY);
			fControlData.setDump("CONTACT");
			this.start();
		}
		catch(IOException e) {
			this.setLoggedIn(false);
			System.out.println("IOException");
			throw new YahooException();
		}
		catch(TimeOutException e) {
			this.setLoggedIn(false);
			System.out.println("TimeOut");
			throw new YahooException();
		}
		catch(BadArgumentException e) {
			this.setLoggedIn(false);
			System.out.println("BadArgument");
			throw new YahooException();
		}
		catch(UnknownPacketException e) {
			this.setLoggedIn(false);
			e.printStackTrace();
			System.out.println("UnknownPacket");
			throw new YahooException();
		}
		catch (IncomingNullPacketException e) {
			this.setLoggedIn(false);
			System.out.println("IncomingNullPacket");
			throw new YahooException();
		}
		catch(Exception e) {
			this.setLoggedIn(false);
			System.out.println("Exception");
			//e.printStackTrace();
			throw new YahooException();
		}
	}
	
	/**
	 * Method getContact.
	 * @param String login
	 * @return AContact
	 * @throws UnknownContactException
	 */
	public AContact getContact(String login) throws UnknownContactException {
		for(int i = 0; i < fContactList.size(); i++) {
			if(((AContact)fContactList.elementAt(i)).getLogin().equals(login)) {
				return (AContact)fContactList.elementAt(i);
			}
		}	
		throw new UnknownContactException();
	}
	
	/**
	 * @see messenger.AProtocol#addContact(java.lang.String, java.lang.String)
	 */
	public void addContact(String login, String nick) { }
	
	/**
	 * @see messenger.AProtocol#sendMessage(java.lang.String, java.lang.String)
	 */
	public void sendMessage(String target, String message) {
		assert message != "";
		assert message != null;
		
		try {
			DataOutputStream fOutToServer = new DataOutputStream(getSocket().getOutputStream());
		   	Vector v = new Vector();
		   	InetAddress ia = getSocket().getInetAddress();
		   	
		   	v.addElement(Utils.HexFromString(this.getLoginName()));
		   	v.addElement(Utils.HexFromString(target));
		   	v.addElement(Utils.HexFromString(message));
		   	
		 	AContact a = this.getContact(target);

		 	YahooPacket p = (YahooPacket)fFactory.createOutgoingPacket("CLI_MESSAGE", v);
		 
		   	sendPacket(p);
		  				
		  	fControlData.setToUser("You say: "+message);
		}
		catch(BadArgumentException e) {
			e.printStackTrace();
			//throw new ICQBadArgumentException();
		}
		catch(UnknownPacketException e) {
			e.printStackTrace();
				//throw new ICQUnknownPacketException();
		}
		catch(IOException e) {
			e.printStackTrace();
			//throw new ICQIOException();
		}
		catch(StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			//throw new ICQStringIndexOutOfBoundsException();
		}
		catch(Exception e) {
			e.printStackTrace();
			//throw new ICQException();
		}
		
		
	}
	
	/**
	 * Method changeStatus.
	 * @param String status
	 */
	public void changeStatus(String status) {
		assert status != "";
		
		try {
			Vector v = new Vector();
			v.addElement(status);
			//System.out.println("hier");
			YahooPacket cli_setstatus = (YahooPacket)fFactory.createOutgoingPacket("CLI_ISAWAY", v);
			//System.out.println("voor senden");
			sendPacket(cli_setstatus);
		}
		catch(BadArgumentException e) {
			System.out.println("bad arguments");
		}
		catch(UnknownPacketException e) {
			System.out.println("unknown packet changeStatus");
		}
		catch(IOException e) {
			System.out.println("message niet verstuurd");	
		}
		catch(StringIndexOutOfBoundsException e) {
			System.out.println("String out of bounds");
			//System.out.println(e.getMessage());
		}
		catch(Exception e) {
			System.out.println("general exception");
			//System.out.println(e.getMessage());
		}
	}
	
	public void addContact(String uin) {
		
	}
	
	public void logout() {
		
	}
	
	public void shutdown() {
		this.interrupt();
	}
	
	/**
	 * Method analyzePacket.
	 * @param String packet in hex-string form
	 * @return boolean recognized or not?
	 * @throws YahooException
	 */
	public boolean analyzePacket(String s) /*throws YahooException*/ {
		InetAddress ia = getSocket().getInetAddress();
		String uin = "";
		
		//System.out.print("packetcontentANALYZE: "+s);
		try {
			YahooPacket p = (YahooPacket)fFactory.createIncomingPacket(s);
			fControlData.setDump("receiving from "+ia.getHostAddress()+" "+p.getKindOfPacket()
													+" ("+p.getLength()+" bytes):\n"+Utils.printableHexString(p.getContentHex()));
		
			if(p instanceof YahooSRV_MESSAGEPacket) {
				YahooSRV_MESSAGEPacket srv_msg = (YahooSRV_MESSAGEPacket)p;
				if(!srv_msg.getMessage().equals("")) {
					fControlData.setToUser("MSG "+srv_msg.getLogin()+" "+srv_msg.getMessage());	
				}	
				return true;
			}	
			else if(p instanceof YahooSRV_ISBACKACKPacket) {
				YahooSRV_ISBACKACKPacket srv_iba = (YahooSRV_ISBACKACKPacket)p;
				return true;
			}		
			else if(p instanceof YahooSRV_NOTIFYPacket) {
				YahooSRV_NOTIFYPacket srv_not = (YahooSRV_NOTIFYPacket)p;
				return true;
			}		
			else if(p instanceof YahooSRV_LOGONPacket) {
				boolean found = false;
								int i = 0;
				
								YahooSRV_LOGONPacket srv_useronline = (YahooSRV_LOGONPacket)p;
								while((!found)&&(i < fContactList.size())) {
									if(((YahooContact)fContactList.elementAt(i)).getUIN().equals(srv_useronline.getContact())) {
										((YahooContact)fContactList.elementAt(i)).setStatus(srv_useronline.getStatus());
										//System.out.println(((YahooContact)fContactList.elementAt(i)).getLogin()+" gaat nu "+srv_useronline.getStatus());	
										found = true;
									}
									i++;
								}
								//System.out.println();
								//System.out.println("overzicht van contactlist: "+fContactList.size());
								for(i = 0; i < fContactList.size(); i++) {
										AContact a = ((YahooContact)fContactList.elementAt(i));
										//System.out.println(a.getLogin()+" is "+a.getStatus());	
								}
				
								/*System.out.println("useronline: "+fContactList.size());*/
								fControlData.setToUser(srv_useronline.getContact()+" goes "+srv_useronline.getStatus());
								fControlData.setDump("CONTACT"); // om aan te geven dat er contacts zijn veranderd
								return true;		
			}
			else if(p instanceof YahooSRV_LOGONPacket) {
					boolean found = false;
					int i = 0;

					YahooSRV_LOGOFFPacket srv_useroffline = (YahooSRV_LOGOFFPacket)p;
					while((!found)&&(i < fContactList.size())) {
						if(((YahooContact)fContactList.elementAt(i)).getUIN().equals(srv_useroffline.getContact())) {
							((YahooContact)fContactList.elementAt(i)).setStatus(srv_useroffline.getStatus());
							//System.out.println(((YahooContact)fContactList.elementAt(i)).getLogin()+" gaat nu "+srv_useroffline.getStatus());	
							found = true;
						}
						i++;
					}
					//System.out.println();
					//System.out.println("overzicht van contactlist: "+fContactList.size());
					for(i = 0; i < fContactList.size(); i++) {
							AContact a = ((YahooContact)fContactList.elementAt(i));
							//System.out.println(a.getLogin()+" is "+a.getStatus());	
					}

					/*System.out.println("useronline: "+fContactList.size());*/
					fControlData.setToUser(srv_useroffline.getContact()+" goes "+srv_useroffline.getStatus());
					fControlData.setDump("CONTACT"); // om aan te geven dat er contacts zijn veranderd
					return true;		
			}
			else if(p instanceof YahooSRV_AUTH_RESPPacket) {
				return true;
			}
			else {
				return false;
			}
		}
		catch(UnknownPacketException e) {
			System.out.println("Yahoo: unknown packet received");
			//Utils.printableHexString(s);
		}
		catch(IncomingNullPacketException e) {
			e.printStackTrace();
		}
		//catch(Exception e) {
			//throw new YahooException((e.getStackTrace()).toString());
		//}
		
		return false;
	}
	
	private String getServerMessage() {
		String s = "";

		try {
			int empty = 0;
			DataInputStream fInFromServer = new DataInputStream(getSocket().getInputStream());
			empty = fInFromServer.read();
			
			if (empty == -1) {
				return "";
			} else {
				//System.out.print("packet lezen: ");
				if(empty == 0) {
					s += Utils.intToHex((int) fInFromServer.read());	
				}
				else {	
					s += Utils.intToHex(empty);
				}
				
				// YMSG
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				//System.out.println(s);
				//System.out.print(".");
				// PROTOCOL VERSION
				s += Utils.intToHex((int) fInFromServer.read());
				
				// 00 00 00
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				//s += fU.intToHex((int) fInFromServer.read());
				//System.out.println(s);
				// LENGTH
				StringBuffer sb = new StringBuffer();
				int l1 = ((int) fInFromServer.read());
				s += Utils.intToHex(l1);
				sb.append(Utils.intToHex(l1));
				//System.out.print(".");
				int l2 = ((int) fInFromServer.read());
				s += Utils.intToHex(l2);
				sb.append(Utils.intToHex(l2));
				//System.out.println("sb: "+sb);
				//System.out.println("s: "+s);
				int length = Utils.hexToInt(sb.toString());
				//System.out.println(length);
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				//System.out.print(".");
				for (int i = 0; i < length; ++i) {
					s += Utils.intToHex((int) fInFromServer.read());
				}
				//System.out.println(". done");
			}
		}
		catch (Exception e) {
			System.out.println("exception");
			//System.out.println(e.getMessage());
		}
		
		//System.out.println("packet gelezen:"+ Utils.printableHexString(s));
		return s;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 *@throws YahooException
	 * 
	 */
	public void run() {
		String s = "";

		while(true) {
			s = getServerMessage();
			//System.out.print("packetcontentRUN: "+s);
			if(s != "") {
				/*try {*/
					if(analyzePacket(s)) {
						//System.out.println("analyzed");
					}
					else { 
						System.out.println("unknown packet"); 
						//Utils.printableHexString(s);
					}
				/*}*/
				/*catch(YahooException e) {*/
					//System.out.println(e.getStackTrace());
					//==> richting gui sturen
					//e.getStackTrace()).toString();	
				/*}		*/		
			}	
			//System.out.println("yahooprotocol");
			try {
				sleep(300);
			}
			catch(InterruptedException e) {
			}
		}
		
	}	

}
