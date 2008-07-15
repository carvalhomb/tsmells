/**   	Anastacia is a Java ICQ/MSN/Yahoo Instant Messenger
 *   	Copyright (C) 2002,2003 	Benny Van Aerschot, Bart Van Rompaey
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

package messenger.ICQ;

import java.io.*;
import java.net.*;
import java.util.*;
import messenger.*;
import messenger.ICQ.ICQExceptions.*;


/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.6 $
 * @date $Date: 2003/05/25 18:09:41 $
 * 
 * 
 */
public class ICQProtocol extends AProtocol implements Runnable {

	private DataBuffer fControlData;
	private String fLastContact = "";
	private String fLastMessage = "";
	private boolean retrievedServerList = false;
	private KeepAlive fKeepAlive;
	
	/**
	 * Method ICQProtocol.
	 * @param DataBuffer controlData
	 * @param String aUin
	 * @param String password
	 * @param String serverName
	 * @param int port
	 * 
	 * @pre assert aUin.length() <= 10;
	 * @pre assert password != "";
	 * 
	 * @post assert getLoginName() == aUin;
	 * @post assert getEncPasswd() != "";
	 */
	public ICQProtocol(DataBuffer controlData, String aUin, String password, String serverName, int port) {
		super(serverName, port);
		
		assert aUin.length() <= 10;
		assert password != "";
		
		this.setLoginName(aUin);
		this.setEncPasswd(Utils.ICQEncryptPass(password));
		fContactList = new Vector();
		fControlData = controlData;
		fKeepAlive = new KeepAlive();
		try {
			fFactory = new PacketFactory().getFactory(this);
		}
		catch(UnknownProtocolException e) {
			// blah blah
		}
		
		assert getLoginName() == aUin;
		assert getEncPasswd() != "";
	}
	
	/**
	 * @return DataBuffer
	 */
	public DataBuffer getControlData() {
		return fControlData;
	}
	
	/**
	 * Method ICQProtocol.
	 * @param controlData
	 * @param serverName
	 * @param port
	 */
	public ICQProtocol(DataBuffer controlData,  String serverName, int port) {
		super(serverName, port);
		
		fContactList = new Vector();
		fControlData = controlData;
		fKeepAlive = new KeepAlive();
		try {
			fFactory = new PacketFactory().getFactory(this);
		}
		catch(UnknownProtocolException e) {
			// blah blah
		}
	}
	
	/**
	 * @see messenger.AProtocol#getNumberOfContacts()
	 * 
	 * @pre fContactList != null;
	 */
	public int getNumberOfContacts() {
		assert fContactList != null;
		
		return fContactList.size();
	}
	
	/**
	 * @see messenger.AProtocol#addContact(java.lang.String, java.lang.String)
	 */
	public void addContact(String login, String nick) {
		
		try {
			Vector v = new Vector();
     		v.addElement(login);
     		v.addElement(nick);
     	
     		ICQPacket cli_addbuddy = (ICQPacket)fFactory.createOutgoingPacket("CLI_ADDBUDDY", v);
     		sendPacket(cli_addbuddy);
		}
		catch (BadArgumentException e) {
			e.getMessage();
			assert(true);	
		}
		catch (UnknownPacketException e) {
			e.getMessage();
			assert(true);
		}
		catch (IOException e) {
			e.getMessage();
			assert(true);
		}
	}
	
	/**
	 * Method addContact.
	 * @param ICQContact ic
	 * 
	 * @pre ic != null;
	 * 
	 * @post assert this.getNumberOfContacts() == old + 1;	
	 */
	public void addContact(ICQContact ic) {
		assert ic != null;
		
		int old = this.getNumberOfContacts();
		
		fContactList.addElement(ic);
		
		assert this.getNumberOfContacts() == old + 1;	
	}
	
	/**
	 * @see messenger.AProtocol#getContactList()
	 */
	public Vector getContactList() {
		return fContactList;	
	}
	
	/**
	 * Method sendPacket.
	 * @param IPacket packet to send
	 * @throws IOException
	 * 
	 * @pre assert this.getServer() != null
	 * @pre assert packet != null
	 * @pre assert packet.getLength() > 0
	 */
	private void sendPacket(IPacket packet) throws IOException {
			assert this.getServer() != null;
			assert packet != null;
			assert packet.getLength() > 0;
			
			DataOutputStream fOutToServer = new DataOutputStream(getSocket().getOutputStream());
			ICQPacket p = (ICQPacket)packet;
			InetAddress ia = getSocket().getInetAddress();
					
       		try {
       			fOutToServer.write(p.getContentByte(), 0, p.getLength());
       		}
       		catch (IOException e) {
       			// mja als ge het nie verstuurt krijgt das dan meestal omda ge offline gesukkeld zijt zeker??
       		}
       		catch (Exception e) {
       			//System.out.println("wie belandt nu hier??");
       		}
       		
       		fControlData.setDump("sending to "+ia.getHostAddress()+" "
       												+p.getKindOfPacket()+" ("
       												+p.getLength()+" bytes): \n"
       												+Utils.printableHexString(p.getContentHex()));  		
	}
	
	/**
	 * Method login.
	 */
	public void login() throws ICQException {
		ICQSRV_COOKIEPacket cookiepacket = null;
		DataOutputStream fOutToServer;
		DataInputStream fInFromServer;
		byte[] b = new byte[256];
		
		String sentence;
       	String modifiedSentence;
		String s = "";
		
		int i = 0;
		
		/* performance testing */
		/*Date dt= new Date(System.currentTimeMillis());
   		System.out.println(dt);*/
 		/* end performance testing */
 		
		try {
			/***************************** 
			 CONNECTEN NAAR 1STE SERVER
			*****************************/
			this.connect();
			
			fInFromServer = new DataInputStream(getSocket().getInputStream());
			InetAddress ia1 = getSocket().getInetAddress();
			
			// SRV_HELLO
			s = getServerMessage();
			ICQPacket srv_hello = (ICQPacket)fFactory.createIncomingPacket(s);
			fControlData.setDump("receiving from "+ia1.getHostAddress()+" "+srv_hello.getKindOfPacket()
														+" ("+srv_hello.getLength()+" bytes):\n"+Utils.printableHexString(srv_hello.getContentHex()));
     		
     		// CLI_IDENT
     		Vector v = new Vector();		
     		v.addElement(Utils.UIN(getLoginName()));
     		//System.out.println("getEncPasswd "+getEncPasswd());
     		v.addElement(getEncPasswd());
     		v.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
     		ICQPacket cli_ident = (ICQPacket)fFactory.createOutgoingPacket("CLI_IDENT", v);
     		sendPacket(cli_ident);
     		
     		// SRV_COOKIE
	  		s = getServerMessage();
	  		cookiepacket = (ICQSRV_COOKIEPacket)fFactory.createIncomingPacket(s);
    		fControlData.setDump("receiving from "+ia1.getHostAddress()+" "+cookiepacket.getKindOfPacket()
														+" ("+cookiepacket.getLength()+" bytes):\n"+Utils.printableHexString(cookiepacket.getContentHex()));
														
	   		// CLI_GOODBYE
			v = new Vector();
	   		ICQPacket cli_goodbye = (ICQPacket)fFactory.createOutgoingPacket("CLI_GOODBYE", v);
	   		sendPacket(cli_goodbye);
					
			getSocket().close();
		
		/***************************** 
		 CONNECTEN NAAR 2DE SERVER
		*****************************/
			this.setServer(cookiepacket.getIp());
			this.setPort(cookiepacket.getPort());
			this.connect();
			
			fInFromServer = new DataInputStream(getSocket().getInputStream());
			InetAddress ia2 = getSocket().getInetAddress();
			
			// SRV_HELLO
			s = getServerMessage();	
			ICQPacket srv_hello2 = (ICQPacket)fFactory.createIncomingPacket(s);
			fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_hello2.getKindOfPacket()
														+" ("+srv_hello2.getLength()+" bytes):\n"+Utils.printableHexString(srv_hello2.getContentHex()));
     		
     		// CLI_COOKIE
     		v = new Vector();
     		v.addElement(cookiepacket.getCookie());
	   		ICQPacket cli_cookie = (ICQPacket)fFactory.createOutgoingPacket("CLI_COOKIE", v);	
	   		sendPacket(cli_cookie);
			
			// SRV_FAMILIES
	  		s = getServerMessage();
	  		ICQPacket srv_families = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_families.getKindOfPacket()
													+" ("+srv_families.getLength()+" bytes):\n"+Utils.printableHexString(srv_families.getContentHex()));
			
			// CLI_FAMILIES
     		v = new Vector();
	   		ICQPacket cli_families = (ICQPacket)fFactory.createOutgoingPacket("CLI_FAMILIES", v);	
	   		sendPacket(cli_families);
	   	
			// CLI_RATESREQUEST
     		v = new Vector();
	   		ICQPacket cli_rrequest = (ICQPacket)fFactory.createOutgoingPacket("CLI_RATESREQUEST", v);	
	   		sendPacket(cli_rrequest);
	   		
	   		// CLI_REQINFO
	   		v = new Vector();
	   		ICQPacket cli_reqinfo = (ICQPacket)fFactory.createOutgoingPacket("CLI_REQINFO", v);	
	   		sendPacket(cli_reqinfo);
	   		
	   		// SRV_FAMILIES2	
	   	
	  		s = getServerMessage();

	  		ICQPacket srv_families2 = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_families2.getKindOfPacket()
													+" ("+srv_families2.getLength()+" bytes):\n"+Utils.printableHexString(srv_families2.getContentHex()));
	   		
	   		// CLI_REQLOCATION
	   		v = new Vector();
	   		ICQPacket cli_reqlocation = (ICQPacket)fFactory.createOutgoingPacket("CLI_REQLOCATION", v);	
	   		sendPacket(cli_reqlocation);
	   		
	   		// CLI_REQBUDDY
	   		v = new Vector();
	   		ICQPacket cli_reqbuddy = (ICQPacket)fFactory.createOutgoingPacket("CLI_REQBUDDY", v);
	   		sendPacket(cli_reqbuddy);
	   		
	   		// CLI_REQLISTS
	   		v = new Vector();
	   		ICQPacket cli_reqlists = (ICQPacket)fFactory.createOutgoingPacket("CLI_REQLISTS", v);	
	   		sendPacket(cli_reqlists);
	   		
	   		// CLI_REQICBM
	   		v = new Vector();
	   		ICQPacket cli_reqicbm = (ICQPacket)fFactory.createOutgoingPacket("CLI_REQICBM", v);	
	   		sendPacket(cli_reqicbm);
	   		
	   		// CLI_REQBOS
	   		v = new Vector();
	   		ICQPacket cli_reqbos = (ICQPacket)fFactory.createOutgoingPacket("CLI_REQBOS", v);	
	   		sendPacket(cli_reqbos);
	   			
	   		// SRV_RATES
	  		s = getServerMessage();
	  		ICQPacket srv_rates = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_rates.getKindOfPacket()
													+" ("+srv_rates.getLength()+" bytes):\n"+Utils.printableHexString(srv_rates.getContentHex()));
			
	   		// CLI_ACKRATES
	   		v = new Vector();
	   		ICQPacket cli_ackrates = (ICQPacket)fFactory.createOutgoingPacket("CLI_ACKRATES", v);	
	   		sendPacket(cli_ackrates);
	   		
	   		// SRV_REPLYINFO
	  		s = getServerMessage();
	  		ICQPacket srv_replyinfo = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_replyinfo.getKindOfPacket()
													+" ("+srv_replyinfo.getLength()+" bytes):\n"+Utils.printableHexString(srv_replyinfo.getContentHex()));
	   		
	   		// SRV_REPLYLOCATION
	  		s = getServerMessage();
	  		ICQPacket srv_replylocation = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_replylocation.getKindOfPacket()
													+" ("+srv_replylocation.getLength()+" bytes):\n"+Utils.printableHexString(srv_replylocation.getContentHex()));
	   		// SRV_REPLYBUDDY
	  		s = getServerMessage();
	  		ICQPacket srv_replybuddy = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_replybuddy.getKindOfPacket()
													+" ("+srv_replybuddy.getLength()+" bytes):\n"+Utils.printableHexString(srv_replybuddy.getContentHex()));
	   		// SRV_REPLYICBM
	  		s = getServerMessage();
	  		ICQPacket srv_replyicbm = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_replyicbm.getKindOfPacket()
													+" ("+srv_replyicbm.getLength()+" bytes):\n"+Utils.printableHexString(srv_replyicbm.getContentHex()));
	   		
	   		// SRV_REPLYBOS
	  		s = getServerMessage();
	  		ICQPacket srv_replybos = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_replybos.getKindOfPacket()
													+" ("+srv_replybos.getLength()+" bytes):\n"+Utils.printableHexString(srv_replybos.getContentHex()));
	   		
	   		// SRV_REPLYLISTS
	  		s = getServerMessage();
	  		ICQPacket srv_replylists = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_replylists.getKindOfPacket()
													+" ("+srv_replylists.getLength()+" bytes):\n"+Utils.printableHexString(srv_replylists.getContentHex()));

			// CLI_SETICBM
	   		v = new Vector();
	   		ICQPacket cli_seticbm = (ICQPacket)fFactory.createOutgoingPacket("CLI_SETICBM", v);	
	   		sendPacket(cli_seticbm);
	   		
	   		// CLI_SETUSERINFO
	   		v = new Vector();
	   		ICQPacket cli_setuserinfo = (ICQPacket)fFactory.createOutgoingPacket("CLI_SETUSERINFO", v);	
	   		sendPacket(cli_setuserinfo);
	   		
	   		// CLI_SETSTATUS
	   		v = new Vector();
	   		v.addElement("ONLINE");
	   		ICQPacket cli_setstatus = (ICQPacket)fFactory.createOutgoingPacket("CLI_SETSTATUS", v);	
	   		sendPacket(cli_setstatus);
	   		
	   		// CLI_READY
	   		v = new Vector();
	   		ICQPacket cli_ready = (ICQPacket)fFactory.createOutgoingPacket("CLI_READY", v);	
	   		sendPacket(cli_ready);
	   		
	   		// CLI_TOICQSRV
	   		v = new Vector();
	   		ICQPacket cli_toicqsrv = (ICQPacket)fFactory.createOutgoingPacket("CLI_TOICQSRV", v);	
	   		sendPacket(cli_toicqsrv);
	   				
			s = getServerMessage();
			ICQPacket srv_toicqsrv = (ICQPacket)fFactory.createIncomingPacket(s);
       		fControlData.setDump("receiving from "+ia2.getHostAddress()+" "+srv_toicqsrv.getKindOfPacket()
													+" ("+srv_toicqsrv.getLength()+" bytes):\n"+Utils.printableHexString(srv_toicqsrv.getContentHex()));
			
			this.setLoggedIn(true);
			this.setPriority(MIN_PRIORITY);
			fKeepAlive.start();
			this.start(); // na de inlogprocedure mag deze thread gestart worden, hij checkt geregeld op
			// nieuwe packetten die binnenkomen
		
			/* performance testing */
			/*dt= new Date(System.currentTimeMillis());
   			System.out.println("login done"+dt);*/
 			/* end performance testing */
			
			getContactListFromServer();
				
			while(!retrievedServerList) {
				sleep(1000);
			}
			
			/* performance testing */
			/*dt= new Date(System.currentTimeMillis());
   			System.out.println("contact list retrieved"+dt);*/
 			/* end performance testing */
 			
			retrieveContactList();
			
			/* performance testing */
			/*dt= new Date(System.currentTimeMillis());
   			System.out.println("contact status retrieved"+dt);*/
 			/* end performance testing */
			
		}
		catch(BadArgumentException e) {
			this.setLoggedIn(false);
			throw new ICQBadArgumentException();
		}
		catch(UnknownPacketException e) {
			this.setLoggedIn(true);
			throw new ICQUnknownPacketException();
			//System.out.println("not connected due to "+e.getMessage());
		}
		catch(IOException e) {
			this.setLoggedIn(false);
			throw new ICQIOException();
		}
		catch(StringIndexOutOfBoundsException e) {
			this.setLoggedIn(false);
			throw new ICQStringIndexOutOfBoundsException();
		}
		catch(TimeOutException e) {
			this.setLoggedIn(false);
			System.out.println("TIMEOUT!");
			throw new ICQTimeOutException();
		}
		catch(Exception e) {
			e.printStackTrace();
			this.setLoggedIn(false);
			throw new ICQException();
		}
	}
	
	/**
	 * @see messenger.AProtocol#sendMessage(java.lang.String, java.lang.String)
	 * 
	 * @pre uin.length() <= 10;
	 * @pre message != "";
	 * @pre message != null;
	 */
	public void sendMessage(String uin, String message) throws ICQException {
		assert uin.length() <= 10;
		assert message != "";
		assert message != null;
		
		try {
			DataOutputStream fOutToServer = new DataOutputStream(getSocket().getOutputStream());
		   	Vector v = new Vector();
		   	InetAddress ia = getSocket().getInetAddress();
		   	
		   	fLastContact = uin;   // voor als de contact geen type-2 messages kan ontvangen
		   	fLastMessage = message; // dan zal hij met dit nen type-1 sturen
		   	
		   	v.addElement(Utils.UIN(uin));
		   	v.addElement(Utils.HexFromString(message));
		   	
		 	AContact a = this.getContact(uin);
		 	ICQPacket p = null;
		 	if(((ICQContact)a).getClientVersion()) {
		 		//System.out.println("type 1 message");
		 		p = (ICQPacket)fFactory.createOutgoingPacket("CLI_SENDMSG", v);
		 	}
		 	else {
		 		//System.out.println("type 2 message");
		 		p = (ICQPacket)fFactory.createOutgoingPacket("CLI_SENDMSGV1", v);
		 	}
		   	sendPacket(p);
		  				
		  	fControlData.setToUser("You say: "+message);
		}
		catch(BadArgumentException e) {
			throw new ICQBadArgumentException();
		}
		catch(UnknownPacketException e) {
				throw new ICQUnknownPacketException();
		}
		catch(IOException e) {
			throw new ICQIOException();
		}
		catch(StringIndexOutOfBoundsException e) {
			throw new ICQStringIndexOutOfBoundsException();
		}
		catch(Exception e) {
			throw new ICQException();
		}
	}
	
	/**
	 * Method changeStatus.
	 * @param String status to which you want to switch
	 * 
	 * @pre status != null;
	 * @pre status != "";
	 */
	public void changeStatus(String status) throws ICQException {
		assert status != null;
		assert status != "";
		
		try {
			Vector v = new Vector();
			v.addElement(status);
			ICQPacket cli_setstatus = (ICQPacket)fFactory.createOutgoingPacket("CLI_SETSTATUS", v);
			sendPacket(cli_setstatus);
		}
		catch(BadArgumentException e) {
			throw new ICQBadArgumentException();
		}
		catch(UnknownPacketException e) {
			throw new ICQUnknownPacketException();
		}
		catch(IOException e) {
			throw new ICQIOException();
		}
		catch(StringIndexOutOfBoundsException e) {
			throw new ICQStringIndexOutOfBoundsException();
		}
		catch(Exception e) {
			throw new ICQException();
		}
	}
	
	/**
	 * Method getContactListFromServer.
	 */
	public void getContactListFromServer() {
		try {
			Vector v = new Vector();
			ICQPacket cli_reqroster = (ICQPacket)fFactory.createOutgoingPacket("CLI_REQROSTER", v);
			sendPacket(cli_reqroster);
		}	
		catch(BadArgumentException e) {
			//System.out.println("bad arguments");
		}
		catch(UnknownPacketException e) {
			//System.out.println("unknown packet");
		}
		catch(IOException e) {
			//System.out.println("message niet verstuurd");	
		}
		catch(StringIndexOutOfBoundsException e) {
			//System.out.println("String out of bounds");
		}
		catch(Exception e) {
			//System.out.println(e.getMessage());
		}	
	}
	
	
	/**
	 * Method retrieveContactList.
	 * 
	 * Send your contactlist to the server, the server responds with the status of 
	 * contacts who are currently online
	 */
	public void retrieveContactList() throws ICQException {
		try {
			/*System.out.println("retrievecontactlist");
			System.out.println(fContactList.size());
			System.out.println(((ICQContact)fContactList.elementAt(0)).getLogin());*/
	   		ICQPacket cli_addcontact = (ICQPacket)fFactory.createOutgoingPacket("CLI_ADDCONTACT", fContactList);
	   		sendPacket(cli_addcontact);
	   		//System.out.println("done retrieving");
	   	}	
		catch(BadArgumentException e) {
			throw new ICQBadArgumentException();
			//System.out.println("bad arguments");
		}
		catch(UnknownPacketException e) {
			throw new ICQUnknownPacketException();
			//System.out.println("unknown packet getContactListFromServer");
		}
		catch(IOException e) {
			throw new ICQIOException();
			//System.out.println("message niet verstuurd");	
		}
	}
	
	/**
	 * Method addContact.
	 * @param String uin
	 *
	 */
	public void addContact(String uin) {
		try {
			Vector v = new Vector();
			v.addElement(uin);
			ICQPacket cli_addcontact = (ICQPacket)fFactory.createOutgoingPacket("CLI_ADDCONTACT", v);
			sendPacket(cli_addcontact);
		}
		catch(BadArgumentException e) {
			//System.out.println("bad arguments");
		}
		catch(UnknownPacketException e) {
			//System.out.println("unknown packet addContact");
		}
		catch(IOException e) {
			//System.out.println("message niet verstuurd");	
		}
		catch(StringIndexOutOfBoundsException e) {
			//System.out.println("ai ai String out of bounds");
		}
		catch(Exception e) {
			//System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Method logout.
	 */
	public void logout() {
		if(this.isConnected()) {
			try {
				fKeepAlive.interrupt(); // stop sending keepalives
				
				// SRV_GOODBYE
				DataOutputStream fOutToServer = new DataOutputStream(getSocket().getOutputStream());
				Vector v = new Vector();
	   			ICQPacket cli_goodbye = (ICQPacket)fFactory.createOutgoingPacket("CLI_GOODBYE", v);
	   			sendPacket(cli_goodbye);
	   			
				disconnect();
			}
			catch(BadArgumentException e) {
				//assert(false);
			}
			catch(UnknownPacketException e) {
				//assert(false);
				// goodbye onbekend?	
			}
			catch(IOException e) {
				// mja, ne messagebox voor de gebruiker zeker?
			}
			catch(StringIndexOutOfBoundsException e) {
			//System.out.println("ai ai String out of bounds");
		}
		catch(Exception e) {
			//System.out.println(e.getMessage());
		}
			assert this.isConnected() != true;
		}
	}
	
	/**
	 * Method shutdown.
	 */
	public void shutdown() {
		this.interrupt();
	}
	
	/**
	 * Method analyzePacket.
	 * @param String packet in hex format
	 * @return boolean
	 * 
	 * this method analyzes incoming packets
	 * and decides how to react on them. returns false if 
	 * incoming packet is unknown.
	 */
	public boolean analyzePacket(String s) {
		InetAddress ia = getSocket().getInetAddress();
		String uin = "";
		
		try {
			ICQPacket p = (ICQPacket)fFactory.createIncomingPacket(s);
			fControlData.setDump("receiving from "+ia.getHostAddress()+" "+p.getKindOfPacket()
													+" ("+p.getLength()+" bytes):\n"+Utils.printableHexString(p.getContentHex()));
		
			if(p instanceof ICQSRV_RECVMSGV1Packet) {
				ICQSRV_RECVMSGV1Packet srv_recvmsg = (ICQSRV_RECVMSGV1Packet)p;
				fControlData.setToUser("MSG "+srv_recvmsg.getUin()+" "+srv_recvmsg.getMessage());	
				return true;
			}	
			if(p instanceof ICQSRV_RECVMSGV2Packet) {
				ICQSRV_RECVMSGV2Packet srv_recvmsg = (ICQSRV_RECVMSGV2Packet)p;
				if(srv_recvmsg.getMessage() != "") {
					fControlData.setToUser("MSG "+srv_recvmsg.getUin()+" "+srv_recvmsg.getMessage());	
				}
				return true;
			}	
			else if(p instanceof ICQSRV_USERONLINEPacket) {
				boolean found = false;
				int i = 0;
				
				ICQSRV_USERONLINEPacket srv_useronline = (ICQSRV_USERONLINEPacket)p;
				while((!found)&&(i < fContactList.size())) {
					if(((ICQContact)fContactList.elementAt(i)).getUIN().equals(srv_useronline.getUin())) {
						if(((ICQContact)fContactList.elementAt(i)).getStatus().equals(srv_useronline.getStatus())) {
							// new status and old status are the same => no need to update
						/*	System.out.println("new and old status: (should be same)");
														System.out.println(((ICQContact)fContactList.elementAt(i)).getStatus());
														System.out.println(srv_useronline.getStatus());*/
						}
						else {
						/*	System.out.println("new and old status:");
							System.out.println(((ICQContact)fContactList.elementAt(i)).getStatus());
							System.out.println(srv_useronline.getStatus());*/
							((ICQContact)fContactList.elementAt(i)).setStatus(srv_useronline.getStatus());	
							fControlData.setToUser(srv_useronline.getUin()+" goes "+srv_useronline.getStatus());
							fControlData.setDump("CONTACT"); // status of a contact has been changed
						}		
						found = true;
					}
					i++;
				}
				
				/*System.out.println("useronline: "+fContactList.size());*/
				return true;
			}
			else if(p instanceof ICQSRV_USEROFFLINEPacket) {
				boolean found = false;
				int i = 0;
				//System.out.println("hier");
				ICQSRV_USEROFFLINEPacket srv_useroffline = (ICQSRV_USEROFFLINEPacket)p;
				//System.out.println(srv_useroffline.getUin()+" goes OFFLINE");
				//System.out.println(fContactList.size());
				while((!found)&&(i < fContactList.size())) {
					//System.out.println(((AContact)fContactList.elementAt(i)).getLogin());
					//System.out.println(srv_useroffline.getUin());
					if(((AContact)fContactList.elementAt(i)).getLogin().equals(srv_useroffline.getUin())) {
						((ICQContact)fContactList.elementAt(i)).setStatus("OFFLINE");	
						found = true;
					}
					i++;
				}
				
				fControlData.setToUser(srv_useroffline.getUin()+" goes OFFLINE");
				fControlData.setDump("CONTACT"); // om aan te geven dat er contacts zijn veranderd
				return true;
			}
			else if(p instanceof ICQSRV_REPLYROSTERPacket) {
				ICQSRV_REPLYROSTERPacket srv_replyroster = (ICQSRV_REPLYROSTERPacket)p;
				Vector v = srv_replyroster.getList();
				//System.out.println("v: "+v.size());
				if(srv_replyroster.moreRRPacketsComing()) {
					retrievedServerList = false;
				}
				else {
					retrievedServerList = true;
				}
				
				for(int i = 0; i < v.size(); i++) {
					ICQContact c = (ICQContact)v.elementAt(i);
					((ICQContact)v.elementAt(i)).setStatus("OFFLINE");
					fContactList.addElement((ICQContact)v.elementAt(i));
					/*System.out.println(fContactList.size());
					System.out.println(((ICQContact)fContactList.elementAt(i)).getLogin());*/
				}
				fControlData.setDump("CONTACT"); // om aan te geven dat er contacts zijn veranderd
				return true;
			}
			else if(p instanceof ICQSRV_CONTACTERRPacket) {
				ICQSRV_CONTACTERRPacket srv_icbmerr = (ICQSRV_CONTACTERRPacket)p;
				return true;
			}
			else if(p instanceof ICQSRV_REPLYLISTSPacket) {
				ICQSRV_REPLYLISTSPacket srv_replylists = (ICQSRV_REPLYLISTSPacket)p;
				return true;
			}
			else if(p instanceof ICQSRV_ICBMERRPacket) {
				ICQSRV_ICBMERRPacket srv_icbmerr = (ICQSRV_ICBMERRPacket)p;
				fControlData.setDump(srv_icbmerr.getReason());
				
				// dit zou toch naar sendMessage moeten verplaatsworden met extra paramter
				// voor welk soort message
				if(srv_icbmerr.getReason().equals("Client does not understand type-2 messages")) {
					try {
						((ICQContact)getContact(uin)).setClientVersion(false);
		   			}
					catch(StringIndexOutOfBoundsException e) {
						//System.out.println("ai ai String out of bounds");
					}
					catch(UnknownContactException e) {
						//System.out.println("deze contact bestaat niet!");
					}
					catch(Exception e) {
						//System.out.println(e.getMessage());
					}
				}
				return true;
			}
			else if(p instanceof ICQSRV_REPLYINFOPacket) {
				return true;	
			}
			else if(p instanceof ICQSRV_SRVACKMSGPacket) {
				return true;
			}
			else if(p instanceof ICQSRV_ACKMSGPacket) {
				return true;
			}
			else if(p instanceof ICQSRV_UPDATEACKPacket) {
				ICQSRV_UPDATEACKPacket srv_updateack = (ICQSRV_UPDATEACKPacket)p;
				//System.out.println(srv_updateack.getComment());
				return true;
			}
			else if(p instanceof ICQSRV_REFUSEDPacket) {
				return true;
			}
			else {
				return false;
			}
		}
		catch(UnknownPacketException e) {
			fControlData.setDump("receiving from "+ia.getHostAddress()+" "+"UNKNOWN"
													+" ("+s.length()+" bytes):\n"+Utils.printableHexString(s));
			return false;
		}
		catch(StringIndexOutOfBoundsException e) {
			//System.out.println("out of bounds");
			return false;
		}
		catch (IncomingNullPacketException e) {
			System.out.println("Null packet received. Possibly the connection with the ICQ server was lost");
			return false;
		}
		catch(Exception e) {
			//System.out.println("general exception");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Method getServerMessage.
	 * @return String
	 */
	private String getServerMessage() {
		String s = "";

		try {
			int empty = 0;
			DataInputStream fInFromServer = new DataInputStream(getSocket().getInputStream());
			empty = fInFromServer.read();

			if (empty == -1) {
				return "";
				// ja niks eh :)
				// tis getest en hij geeft idd -1 terug alser geen packet is
			} else {

				// 2A
				s += Utils.intToHex(empty);

				// CHANNEL
				s += Utils.intToHex((int) fInFromServer.read());

				// SEQUENCE NUMBER
				s += Utils.intToHex((int) fInFromServer.read());
				s += Utils.intToHex((int) fInFromServer.read());
				
				// LENGTH
				StringBuffer sb = new StringBuffer();
				int l1 = ((int) fInFromServer.read());
				s += Utils.intToHex(l1);
				sb.append(Utils.intToHex(l1));

				int l2 = ((int) fInFromServer.read());
				s += Utils.intToHex(l2);
				sb.append(Utils.intToHex(l2));

				int length = Utils.hexToInt(sb.toString());

				for (int i = 0; i < length; ++i) {
					s += Utils.intToHex((int) fInFromServer.read());
				}
			}
		} catch (Exception e) {
			//System.out.println(e.getMessage());
		}
		return s;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		String s = "";

		while(true) {
			s = getServerMessage();
			
			if(s != "") {
				if(analyzePacket(s)) {
				}
				else { 
					System.out.println("unknown packet"); 
				}				
			}
			try {
			sleep(200);
			}
			catch(InterruptedException e) {
			
			}
			//this.yield();
		}
	}	
	
	/**
		 * @author Bart Van Rompaey
		 * @version $Revision: 1.6 $
		 * @date $Date: 2003/05/25 18:09:41 $
		 * 
		 * 
		 */
	class KeepAlive extends Thread {
		public void run() {
			while(isConnected()) {
				try {
					sleep(60000); // sleep for a minute
					Vector v = new Vector();
					ICQPacket keepalive = (ICQPacket)fFactory.createOutgoingPacket("CLI_KEEPALIVE", v);	
					/* nice notation: ICQProtocol.this.sendPacket(keepalive); :)
					 	in case you have a method in your inner class that has the same
						signature than de method in the surrounding class */
					sendPacket(keepalive);				
				}
				catch(BadArgumentException e) {
					
				}
				catch(UnknownPacketException e) {
					
				}
				catch(IOException e) {
					
				}
				catch(InterruptedException e) {
					
				}
			}
		}
	}
}
