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

import java.util.Vector;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import messenger.IPacketFactory;
import messenger.BadArgumentException;
import messenger.UnknownPacketException;
import messenger.IncomingNullPacketException;
import messenger.IPacket;
import messenger.DataBuffer;
//import messenger.Buffer;
import java.util.StringTokenizer;

/**
 * @author Benny Van Aerschot
 * @version  $Revision: 1.7 $
 * @date $Date: 2003/05/24 16:17:34 $
 */
public class MSNSession extends Thread {
	private PrintWriter fOutToServer;
	private BufferedReader fInFromServer;
	private IPacketFactory fFactory;
	private String fLogin;
	private Socket fServer;
	private String fContact;
	private String fCKI;
	private String fSessionID;
	private Long fTrID = new Long(1);
	private String fReceived = null;
	private DataBuffer fControlData = null;
	private MSNSessionListener fSessionListener;


	/**
	 * Method MSNSession.
	 * @param login
	 * @param contact
	 * @param cki
	 * @param controlData
	 */
	public MSNSession(String login, String contact, String cki, DataBuffer controlData, IPacketFactory pf) {
		fLogin = login;
		fContact = contact;
		fCKI = cki;
		fControlData = controlData;
		fFactory = pf;
	}
	

	/**
	 * Method connect.
	 * @param switchboardServer
	 * @param port
	 */
	public void connect(String switchboardServer, int port) {
		try {
			fTrID = new Long(1);
			fServer = new Socket(switchboardServer, port);
			fOutToServer =  new PrintWriter(new OutputStreamWriter(fServer.getOutputStream(), "UTF-8"));
			fInFromServer = new BufferedReader(new InputStreamReader(fServer.getInputStream(), "UTF-8"));
		} catch (IOException e) {
			System.out.println("Exception occurred: " + e.getMessage());
		}
	}

	/**
	 * Method setupOutgoingSession.
	 */
	public void setupOutgoingSession() throws MSNException {
		try {
			Vector v = null;
		
			v = new Vector();
			v.add(fTrID);
			increaseTrID();
			v.add(fLogin);
			v.add(fCKI);
			IPacket packet = fFactory.createOutgoingPacket("MSN_USR", v);
			sendPacket(packet);
			receivePacket();
			fControlData.setDump("msnsession <<< " + fReceived);
				
			v.clear();
			v.add(fTrID);
			increaseTrID();
			v.add(fContact);
			packet = fFactory.createOutgoingPacket("MSN_CAL", v);
			sendPacket(packet);
			receivePacket();			
			packet = fFactory.createIncomingPacket(fReceived);
			fControlData.setDump("msnsession <<< " +  ((MSNPacket)packet).getContent());
			fSessionID = ((MSNIncomingCALPacket)packet).getSessionID();
				
			// receive JOI
			receivePacket();
			fControlData.setDump("msnsession <<< " + fReceived);
		} catch (BadArgumentException e) {
			System.out.println("Exception occurred: " + e.getMessage());
		} catch (UnknownPacketException e) {
			System.out.println("Exception occurred: " + e.getMessage());
		} catch (MSNException e) {
			throw new MSNException(e.getMessage());
		} catch (IncomingNullPacketException e) {
			throw new MSNException(e.getMessage());
		}
	}
	

	/**
	 * Method setupIncomingSession.
	 * @param sessionID
	 * @throws MSNException
	 */
	public void setupIncomingSession(String sessionID) throws MSNException {
		Vector v = new Vector();
		
		try {
			fSessionID = sessionID;
			v.addElement(fTrID);
			increaseTrID();
			v.addElement(fLogin);
			v.addElement(fCKI);
			v.addElement(fSessionID);
			IPacket packet = fFactory.createOutgoingPacket("MSN_ANS", v);
			sendPacket(packet);
			receivePacket();
			fControlData.setDump("msnsession <<< " + fReceived);
			receivePacket();
			fControlData.setDump("msnsession <<< " + fReceived);
		} catch (MSNException e) {
			throw new MSNException(e.getMessage());
		} catch (UnknownPacketException e) {
			System.out.println("Exception occurred: " + e.getMessage());
		}  catch (BadArgumentException e) {
			System.out.println("Exception occurred: " + e.getMessage());
		}
	}


	/**
	 * Method sendPacket.
	 * @param packet
	 */
	protected void sendPacket(IPacket packet) {
		MSNPacket mp = (MSNPacket)packet;
		String s = mp.getContent();
		fOutToServer.print(s);
		if (s.substring(0,3).equals("MSG")) {

		} else {
			fOutToServer.print("\r\n");
		}
		fOutToServer.flush();
		fControlData.setDump("msnsession >>> " + mp.getContent());
	}
	

	/**
	 * Method sendMessage.
	 * @param message
	 * @param contact
	 */
	public void sendMessage(String contact, String message) {
		try {
			Vector v = new Vector();
			v.add(fTrID);
			increaseTrID();
			v.add(message);
			IPacket packet = fFactory.createOutgoingPacket("MSN_MSG", v);
			this.sendPacket(packet);
		} catch(BadArgumentException e) {
			System.out.println("Exception occurred: " + e.getMessage());
		} catch (UnknownPacketException e){
			System.out.println("Exception occurred: " + e.getMessage());
		}
	}
	
	public void sendOutMessage() {
		try {
			IPacket packet = fFactory.createOutgoingPacket("MSN_OUT", null);
			this.sendPacket(packet);
			this.shutdown();
		} catch(BadArgumentException e) {
			System.out.println("Exception occurred: " + e.getMessage());
		} catch (UnknownPacketException e){
			System.out.println("Exception occurred: " + e.getMessage());
		}
	}
	

	/**
	 * Method receivePacket.
	 * @throws MSNException
	 */
	public void receivePacket() throws MSNException {
		try {
			fReceived = fInFromServer.readLine( );
		} catch (IOException e) { 
			System.out.println("Exception occurred: " + e.getMessage());
		}
	}


	/**
	 * Method getContact.
	 * @return String
	 */
	public String getContact() {
		return fContact;
	}

	/**
	 * Method getSessionID.
	 * @return String
	 */
	public String getSessionID() {
		return fSessionID;
	}
	
	
	/**
	 * Method increaseTrID.
	 */
	public void increaseTrID() {
		fTrID = new Long(fTrID.longValue() + 1);
	}

	/**
	 * Method checkForMessages.
	 */
	public void run() {
		StringBuffer sb = null;
		StringTokenizer st = null;
		Vector tokens = null;
		
		try {
			while (true) {
				try {
					sleep(150);
				}
				catch(InterruptedException e) {
				
				}
		
				String s = null;
				if ((s = fInFromServer.readLine()) != null) {
					st = new StringTokenizer(s);
					tokens = new Vector();
					
					while (st.hasMoreTokens()) {
						tokens.add(st.nextToken());
					}
					
					String cmd = (String)tokens.elementAt(0);
					String sender = (String)tokens.elementAt(1);
					//if (cmd.equals("MSG")) {
					if	(cmd.indexOf("G") != -1) {
						//String nick = (String)tokens.elementAt(2);
						Integer len = new Integer((String)tokens.lastElement());
						
						sb = new StringBuffer();
						sb.append(s);
						sb.append("\r\n");
						for (int i = 0; i < len.intValue(); ++i) {
							char c = (char)fInFromServer.read();
							sb.append(c);
						}
						
						String message = sb.toString();
					
						String CRLF = "\r\n\r\n";
						int index = message.indexOf(CRLF);
						String header = message.substring(0, index);
						String body = message.substring(index + CRLF.length());
					
						if (header.indexOf("Typing") != -1) {
							fControlData.setToUser("TYP " + sender);
						} else {
							fControlData.setToUser("MSG " + sender + " " + body);
						}
					} //else if (cmd.equals("BYE")) {
					else if (cmd.indexOf("E") != -1) {
						fControlData.setToUser("BYE " + sender);
						this.shutdown();
					}
				}
			}
		
		} catch (IOException e) {
			//System.out.println("msnsession " + e.getMessage());
		}
	}
	
	public void shutdown() {
		try {
			fServer.close();
			if (fSessionListener != null) {
				fSessionListener.removeSession(fContact);
			}
		} catch (Exception e) {
			//System.out.println("msnsession " + e.getMessage());
		}
	}
	
	public void setListener(MSNSessionListener sessionListener) {
		fSessionListener = sessionListener;
	}
	
	public MSNSessionListener getListener() {
		return fSessionListener;
	}

}