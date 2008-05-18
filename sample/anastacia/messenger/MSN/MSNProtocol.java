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

package messenger.MSN;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Vector;
import messenger.AProtocol;
import messenger.BadArgumentException;
import messenger.UnknownPacketException;
import messenger.IPacket;
import messenger.PacketFactory;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import messenger.DataBuffer;
import messenger.MSN.MSNException;
import messenger.UnknownProtocolException;
import java.util.StringTokenizer;
import messenger.TimeOutException;
import messenger.IncomingNullPacketException;
import messenger.Utils;

/**
 * A class implementing the MSN messenger protocol
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.10 $
 * @date $Date: 2003/05/25 19:19:07 $
 */
public class MSNProtocol extends AProtocol implements MSNSessionListener {
	private Long fTrID = new Long(0);
	private PrintWriter fOutToServer;
	private BufferedReader fInFromServer;	
	private String fContact = null;
	private HashMap fSessionMap = null;
	private String fMessage = null;
	private Thread fUpdateSession;
	private DataBuffer fControlData;
	private boolean fRetrievedServerContactList = false;

	
	/**
	 * Method MSNProtocol.
	 * @param controlData
	 * @param login
	 * @param password
	 * @param servername
	 * @param port
	 */
	public MSNProtocol(DataBuffer controlData, String login, String password, String servername, int port) {
		super(servername, port);
		
		setLoginName(login);
		setEncPasswd(password);
//		setOEncPasswd(password);
//		setOLoginName(login);
		fContactList = new Vector();
		fControlData = controlData;
		fSessionMap = new HashMap();
		PacketFactory f = new PacketFactory();
		try {
			fFactory = f.getFactory(this);
		}
		catch (UnknownProtocolException e) {
			
		}
	}


	/**
	 * Method MSNProtocol.
	 * @param controlData
	 * @param serverName
	 * @param port
	 */
	public MSNProtocol(DataBuffer controlData,  String serverName, int port) {
		super(serverName, port);
		
		fContactList = new Vector();
		fControlData = controlData;
		fSessionMap = new HashMap();
		PacketFactory f = new PacketFactory();
		try {
			fFactory = new PacketFactory().getFactory(this);
		}
		catch(UnknownProtocolException e) {

		}
	}
	
	public DataBuffer getControlData() {
		return fControlData;
	}
	
	public void login() throws Exception {
		fTrID = new Long(0);

		this.connect();

		fSessionMap = new HashMap();
		fOutToServer =  new PrintWriter(new OutputStreamWriter(getSocket().getOutputStream(), "UTF-8"));
		fInFromServer = new BufferedReader(new InputStreamReader(getSocket().getInputStream(), "UTF-8"));
		
		Vector v = new Vector();
		v.add(fTrID);
		this.increaseTrID();
		IPacket packet = fFactory.createOutgoingPacket("MSN_VER", v);
		this.sendPacket(packet);
		
		//this.setPriority(MIN_PRIORITY);
		this.start();
	}
	
	public void sendByeMessage(String contact) {
		MSNSession msnsession = (MSNSession)fSessionMap.get(contact);
		if (msnsession != null) {
			msnsession.sendOutMessage();
		}
	}
	
	/**
	 * Method sendMessage.
	 * @param message
	 * @param contact
	 */
	public void sendMessage(String contact, String message) throws MSNException {
		fContact = contact;
		fMessage = message;

		MSNSession session = (MSNSession)fSessionMap.get(contact);
		if (session != null) {
			session.sendMessage(contact, message);
		} else {
			Vector v = null;

			try {
				v = new Vector();
				v.addElement(fTrID);
				increaseTrID();
				IPacket packet = fFactory.createOutgoingPacket("MSN_XFR", v);
				sendPacket(packet);
			} catch(BadArgumentException e) {
				throw new MSNException("Trying to send a packet with invalid content");
			} catch (UnknownPacketException e) {
				throw new MSNException("Trying to send an unknown packet");
			}
		}
	}
	
	/**
	 * @see messenger.AProtocol#sendPacket(IPacket)
	 */
	public void sendPacket(IPacket packet) {
		MSNPacket mp = (MSNPacket)packet;
		String s = mp.getContent();
		fOutToServer.print(mp.getContent());
		if (s.substring(0,3).equals("QRY")) {
			// geen CRLF na een QRY packet
		} else {
			fOutToServer.print("\r\n");
		}
		fOutToServer.flush();
		fControlData.setDump("msnprotocol >>> " + mp.getContent());
	}
	
	
	public String receiveMessagePacket(String firstLine) throws IOException {
		StringTokenizer st = new StringTokenizer(firstLine);
		Vector tokens = new Vector();
				
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}
			
		String cmd = (String)tokens.elementAt(0);
		
		Integer len = new Integer((String)tokens.lastElement());
	
		StringBuffer sb = new StringBuffer();
		sb.append(firstLine);
		sb.append("\r\n");
		for (int i = 0; i < len.intValue(); ++i) {
			char c = (char)fInFromServer.read();
			sb.append(c);
		}
					
		return sb.toString();
	}

	/**
	 * Method analyzePacket.
	 * @param s
	 * @throws MSNException
	 */
	public void analyzePacket(String s) throws MSNException {
		String cmd = null;
		Vector v = new Vector();
		IPacket packet = null;
		
		if (s == null) throw new MSNException("Null packet");
		
		try {
			cmd = s.substring(0,3);
			
			if (! cmd.equals("BPR")) {
				packet = fFactory.createIncomingPacket(s);
				fControlData.setDump("msnprotocol <<< " + ((MSNPacket)packet).getContent());
			}
			
			if (cmd.equals("VER")) {
				// >>> INF 4
				v.clear();
				v.addElement(fTrID);
				increaseTrID();
				packet = fFactory.createOutgoingPacket("MSN_INF", v);
				sendPacket(packet);
			}
			
			else if (cmd.equals("INF")) {
				// >>> USR 5 MD5 I loginname@domain.com
				v.clear();
				v.addElement(fTrID);
				increaseTrID();
				v.addElement("MD5");
				v.addElement(getLoginName());
				packet = fFactory.createOutgoingPacket("MSN_USRI", v);
				sendPacket(packet);
			}
			
			else if (cmd.equals("USR")) {
				MSNIncomingUSRPacket usr = (MSNIncomingUSRPacket)packet;
				String auth = usr.getAuth();
				
				if (auth.equals("OK")) {
					this.changeStatus("NLN");					
					this.getServerContactList();
				} else if (auth.equals("MD5")) {
					v.clear();
					v.addElement(fTrID);
					increaseTrID();
					setEncPasswd(Utils.MD5Encrypt(((MSNIncomingUSRPacket)packet).getHash() + getEncPasswd()));
					v.addElement("MD5");
					v.addElement(getEncPasswd());
					packet = fFactory.createOutgoingPacket("MSN_USRS", v);
					sendPacket(packet);
				}
			}


			else if (cmd.equals("CHG")) {
				
			}
					
			else if (cmd.equals("NLN")) {
				MSNIncomingNLNPacket p = (MSNIncomingNLNPacket)packet;
				String contact = p.getContact();
				for (int i = 0; i < fContactList.size(); i++) {
					MSNContact msncontact = (MSNContact)fContactList.elementAt(i);
					if (msncontact.getLogin().equals(contact)) {
						msncontact.setStatus(p.getStatus());
						fContactList.setElementAt(msncontact, i);
					}
				}
				fControlData.setDump("CONTACT");
			}
			
			else if (cmd.equals("FLN")) {
				MSNIncomingFLNPacket p = (MSNIncomingFLNPacket)packet;
				String contact = p.getContact();
				for (int i = 0; i < fContactList.size(); i++) {
					MSNContact msncontact = (MSNContact)fContactList.elementAt(i);
					if (msncontact.getLogin().equals(contact)) {
						msncontact.setStatus("FLN");
						fContactList.setElementAt(msncontact, i);
					}
				}
				fControlData.setDump("CONTACT");
			}
			
			else if (cmd.equals("ILN")) {
				MSNIncomingILNPacket p = (MSNIncomingILNPacket)packet;
				MSNContact contact = new MSNContact(p.getContact(), p.getContactNick());
				contact.setStatus(p.getStatus());
				fContactList.add(contact);
				fControlData.setDump("msnprotocol <<< " + contact.getLogin() + " (" + contact.getNick() + ") -> " + contact.getStatus());
			}
			
			else if (cmd.equals("BPR")) {
				// phone info about your contacts
			}
			
			else if (cmd.equals("ADD")) {
				MSNIncomingADDPacket p = (MSNIncomingADDPacket)packet;
				fControlData.setDump("msnprotocol <<< " + ((MSNPacket)packet).getContent());
			}
			
			else if (cmd.equals("LST")) {
				MSNIncomingLSTPacket p = (MSNIncomingLSTPacket)packet;
				MSNContact contact = new MSNContact(p.getContact(), p.getContactNick());
				boolean contactAlreadyAdded = false;
				 MSNContact msncontact = null;
				
				for (int i = 0; i < fContactList.size(); i++) {
					msncontact = (MSNContact)fContactList.elementAt(i);
					if (msncontact.getLogin().equals(contact.getLogin())) {
						contactAlreadyAdded = true;
					} 
				}
				
				if (! contactAlreadyAdded) {
					contact.setStatus("FLN");
					fContactList.add(contact);
					fControlData.setDump("msnprotocol <<< " + contact.getLogin() + " (" + contact.getNick() + ") -> " + contact.getStatus());
				}
				
				if (p.getNrOfContacts() == p.getTotalNrOfContacts()) {
					fRetrievedServerContactList = true;
				}
			}

			else if (cmd.equals("RNG")) {
				try {	
					MSNIncomingRNGPacket rng = (MSNIncomingRNGPacket)packet;
			
					MSNSession session = new MSNSession(getLoginName(), rng.getContact(), rng.getCKI(), fControlData, fFactory);
					session.connect(rng.getSwitchBoardIp(), getPort());
	
					session.setupIncomingSession(rng.getSessionID());

					fSessionMap.put(rng.getContact(), session);
					((MSNSession)fSessionMap.get(rng.getContact())).setListener(this);
					((MSNSession)fSessionMap.get(rng.getContact())).start();
				} catch (StringIndexOutOfBoundsException e) {
					throw new MSNException("Session setup failed");
				}
			}
	
			else if (cmd.equals("CHL")) {
				 v = new Vector();
				v.addElement(fTrID);
				increaseTrID();
				v.addElement(((MSNIncomingCHLPacket)packet).getChallenge());

				packet = fFactory.createOutgoingPacket("MSN_QRY", v);
				sendPacket(packet);
			}
	
			else if (cmd.equals("XFR")) {
				MSNIncomingXFRPacket xfr = (MSNIncomingXFRPacket)packet;
				
				String nf = xfr.getNF();
				
				if (nf.equals("NS")) {
					String notificationServer = xfr.getIp();
					setServer(notificationServer);
					this.connect();

					fOutToServer =  new PrintWriter(new OutputStreamWriter(getSocket().getOutputStream(), "UTF-8"));
					fInFromServer = new BufferedReader(new InputStreamReader(getSocket().getInputStream(), "UTF-8"));
					
					v = new Vector();
					v.add(fTrID);
					increaseTrID();
					packet = fFactory.createOutgoingPacket("MSN_VER", v);
					sendPacket(packet);
				} else if (nf.equals("SB")) {
				
					String switchboardServer = ((MSNIncomingXFRPacket)packet).getIp();
					String cki = ((MSNIncomingXFRPacket)packet).getCKI();
	
					MSNSession session = new MSNSession(getLoginName(), fContact, cki, fControlData, fFactory);
					session.connect(switchboardServer, getPort());
	
					session.setupOutgoingSession();
	
					fSessionMap.put(fContact, session);
					if (fMessage != null) session.sendMessage(fContact, fMessage);		// rightclick in the gui sets up session
					((MSNSession)fSessionMap.get(fContact)).setListener(this);
					((MSNSession)fSessionMap.get(fContact)).start();
				}
			}
			
			else if (cmd.equals("MSG")) {
				MSNIncomingMSGPacket msgpacket = ((MSNIncomingMSGPacket)packet);
				String type = msgpacket.getType();
				if (type.equals("initialemailnotification")) {
					fControlData.setToUser("INI " + "Unread messages in inbox: " + msgpacket.getNrOfUnreadMsgs());
				} else if (type.equals("emailnotification")) {
					fControlData.setToUser("EMN " + "New email from: " + msgpacket.getFromName());
				} else if (type.equals("activemailnotification")) {
					//fControlData.setToUser(type + " " + msgpacket.getContent());
				}
			}
			
			else if (cmd.equals("QRY")) {
				
			}
			
			else {
				throw new MSNException("Unknown incoming packet received: " + s);
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new MSNException("Corrupt incoming packet: " + s + " stacktrace: ");
		} catch (UnknownPacketException e) {
			throw new MSNException(e.getMessage());
		} catch (BadArgumentException e) {
			throw new MSNException("Corrupt incoming packet: " + s);
		} catch (MSNException e) {
			throw new MSNException(e.getMessage());
		} catch (IOException e) {
			throw new MSNException(e.getMessage());
		}  catch (IncomingNullPacketException e) {
			throw new MSNException(e.getMessage());
		} catch (TimeOutException e) {
			throw new MSNException(e.getMessage());
		}
	}

	public void run() {
		try {
			
			while (true) {
				String r = null;
				if ((r = fInFromServer.readLine()) != null) {
					try {
						String msg = r.substring(0,3);
						if (msg.equals("MSG")) {
							r = this.receiveMessagePacket(r);
						}
						this.analyzePacket(r);
					} catch (IOException e) {
						System.out.println("analysis: " + e.getMessage()); e.printStackTrace();
					}
				}
				
				try {
					sleep(100);
				} catch(InterruptedException e) {
				
				}
			}
		
		} catch (IOException e) {
			//System.out.println("efa" + e.getMessage());
		} catch (Exception e) {
			fControlData.setToUser(/*"MSNLOGOFF " + */e.getMessage());
		}
	}

	
	public void shutdown() {
		try {
			if (this.isConnected()) {
				IPacket packet = fFactory.createOutgoingPacket("MSN_OUT", null);
				sendPacket(packet);
				
				if (! fSessionMap.isEmpty()) {
					Collection c = (Collection)fSessionMap.values();
			
					Iterator iterator = c.iterator();
			
					while (iterator.hasNext()) {
						MSNSession session = (MSNSession)iterator.next();
						session.sendOutMessage();
					}
				}

				fSessionMap = null;
				fContactList = new Vector();
				//fContactList = null;
				this.interrupt();
				disconnect();
			}
		}
		catch(Exception e) {
			//System.out.println("msn " + e.getMessage());
		}
	}
	
	public Vector getContactList() {
		return fContactList;	
	}
	
	public void getServerContactList() throws MSNException {
		try {
			Vector v = new Vector();
			v.add(fTrID);
			increaseTrID();
			IPacket packet = fFactory.createOutgoingPacket("MSN_LST", v);
			sendPacket(packet);
		} catch (BadArgumentException e) {
			System.out.println(e.getMessage());
		} catch (UnknownPacketException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void changeStatus(String status) {		
		try {
			Vector v = new Vector();
			v.add(fTrID);
			increaseTrID();
			v.add(status);
			IPacket packet = fFactory.createOutgoingPacket("MSN_CHG", v);
			sendPacket(packet);
		} catch (UnknownPacketException e) {
			System.out.println(e.getMessage());
		} catch (BadArgumentException e) {
			System.out.println(e.getMessage());
		}
	}
	
	// waarom die nickname?????
	public void addContact(String contact, String nick) {
		try {
			Vector v = new Vector();
			v.add(fTrID);
			increaseTrID();
			v.add("FL");
			v.add(contact);
			IPacket packet = fFactory.createOutgoingPacket("MSN_ADD", v);
			sendPacket(packet);
			
			v = new Vector();
			v.add(fTrID);
			increaseTrID();
			v.add("AL");
			v.add(contact);
			packet = fFactory.createOutgoingPacket("MSN_ADD", v);
			sendPacket(packet);
		} catch (UnknownPacketException e) {
			System.out.println(e.getMessage());
		} catch (BadArgumentException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public void increaseTrID() {
		fTrID = new Long(fTrID.longValue() + 1);
	}

	public int getNumberOfContacts() {
		return fContactList.size();
	}

	public void removeSession(String contact) {
		fSessionMap.remove(contact);
	}
	
	public boolean contactListRetrieved() {
		return fRetrievedServerContactList;
	}

}
