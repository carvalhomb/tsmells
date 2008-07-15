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

package messenger;

import java.util.Vector;
import java.lang.Thread;
import java.net.*;


/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.5 $
 * @date $Date: 2003/05/25 18:09:41 $
 * 
 * A class representing an abstract class for
 * implementing a protocol
 */
public abstract class AProtocol extends Thread {
	private TimeOutSocket fSocket;
	//protected static Utils fU = new Utils();
	// de verschillende Systems kunnen dit makkelijk gebruiken
	protected IPacketFactory fFactory;
	private String fLoginName;
	private String fEncPassword;
	private boolean fLoggedIn = false;
	private String fServer;
	private int fPort;
	protected Vector fContactList = null;

	/**
	 * @see java.lang.Object#Object()
	 */
	public AProtocol() {
	}

	/**
	 * Method AProtocol.
	 * @param String server
	 * @param int port
	 * 
	 * @pre port > 0 && port < 65536;
	 * @pre !server.equals("");
	 */
	public AProtocol(String server, int port) {
		assert port > 0 && port < 65536;
		assert !server.equals("");
		
		fServer = server;
		fPort = port;
	}

	/**
	 * Method connect.
	 * @throws TimeOutException
	 */
	public void connect() throws TimeOutException {
		try {
			System.out.println(fServer + ":" + fPort + " awaiting connection...");
			fSocket = new TimeOutSocket(fServer, fPort);
			System.out.println(fServer + ":" + fPort + " connection established!");
		} catch (TimeOutException e) {
			throw new TimeOutException(fServer + ":" + fPort + " connection timed out!");
		}
	}

	/**
	 * Method disconnect.
	 */
	public void disconnect() {
		//this.interrupt();
		fSocket.close();
	}

	/**
	 * Method getSocket.
	 * @return Socket
	 * 
	 * @pre fSocket != null;
	 */
	public Socket getSocket() {
		assert fSocket != null;
		return fSocket.getServer();
	}

	/**
	 * Method setLoggedIn.
	 * @param boolean marks the protocols as logged in or not
	 */
	public void setLoggedIn(boolean loggedin) {
		fLoggedIn = loggedin;
	}

	/**
	 * Method setLoginName.
	 * @param String login string
	 */
	public void setLoginName(String login) {
		fLoginName = login;
	}

	/**
	 * Method setEncPasswd.
	 * @param encpasswd
	 */
	public void setEncPasswd(String encpasswd) {
		fEncPassword = encpasswd;
	}

	/**
	 * Method getLoggedIn.
	 * @return boolean
	 */
	public boolean getLoggedIn() {
		return fLoggedIn;
	}

	/**
	 * Method getLoginName.
	 * @return String
	 */
	public String getLoginName() {
		return fLoginName;
	}

	/**
	 * Method getEncPasswd.
	 * @return String
	 */
	public String getEncPasswd() {
		return fEncPassword;
	}

	/**
	 * Method isConnected.
	 * @return boolean
	 */
	public boolean isConnected() {
		return fSocket.isConnected();
	}

	/**
	 * Method setServer.
	 * @param server
	 */
	public void setServer(String server) {
		assert(!server.equals(""));

		fServer = server;

		assert fServer.equals(server);
	}

	/**
	 * Method getServer.
	 * @return String
	 */
	public String getServer() {
		return fServer;
	}

	/**
	 * Method setPort.
	 * @param int port to use
	 * @pre assert port >= 0
	 * @pre assert port <= 65535
	 * 
	 * @post getPort() == port
	 */
	public void setPort(int port) {
		assert port >= 0;
		assert port <= 65535;

		fPort = port;

		assert getPort() == port;
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
	 * Method getPort.
	 * @return int port
	 */
	public int getPort() {
		return fPort;
	}

	/**
	 * Method getNumberOfContacts.
	 * @return int
	 */
	protected abstract int getNumberOfContacts();
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	//public abstract void run();

	/**
	 * Method sendMessage.
	 * @param String contact
	 * @param String message
	 */
	public abstract void sendMessage(String contact, String message) throws ProtocolException;
	
	/**
	 * Method getContactList.
	 * @return Vector
	 */
	public abstract Vector getContactList();
	
	/**
	 * Method addContact.
	 * @param String login
	 * @param String nick
	 */
	public abstract void addContact(String login, String nick);
}
