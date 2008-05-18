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

package messenger.ICQ;
import messenger.AContact;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:04:17 $
 * 
 * This class holds info about a buddy on your contact list
 */
public class ICQContact extends AContact  {
	private String fUin;
	private String fIP;
	private boolean fClientVersion; // can the contact's client accept version-2 messages?
	
	/**
	 * @see java.lang.Object#Object()
	 */
	public ICQContact() {
		fClientVersion = true; // assume yes, change if we get an icbmerror
	}

	/**
	 * Method ICQContact.
	 * @param String UIN
	 * @param String nick
	 * @param String IP
	 *
	 * @pre assert nick != "";
	 * @pre assert IP != "";
	 * @pre assert (IP.length() >= 7)&&(IP.length() <= 15);
	 * @post assert this.getUIN().equals(UIN);
	 * @post assert this.getNick().equals(nick);
	 * @post assert this.getIP().equals(IP);
	 */
	public ICQContact(String UIN, String nick, String IP) {
		assert nick != "";
		assert IP != "";
		assert (IP.length() >= 7)&&(IP.length() <= 15);
		
		this.setLogin(UIN);
		this.setNick(nick);
		fIP = IP;
		fClientVersion = true;
		
		assert this.getUIN() == UIN;
		assert this.getNick().equals(nick);
		assert this.getIP().equals(IP);
	}
	
	/**
	 * Method ICQContact.
	 * @param String UIN
	 * 
	 * @pre assert UIN != "";
	 */
	public ICQContact(String UIN) {
		assert UIN != "";
		setLogin(UIN);
	}
	
	/**
	 * Method setUIN.
	 * @param String uin
	 * @pre assert uin != "";
	 * @post getUIN() != "";
	 */
	public void setUIN(String uin) {
		assert uin != "";
		
		fUin = uin;
		
		assert getUIN() != "";
	}
	
	/**
	 * Method getUIN.
	 * 
	 * @post assert getLogin() != ""
	 * @return String
	 */
	public String getUIN() {
		assert getLogin() != "";
		return getLogin();
	}
	
	/**
	 * Method getIP.
	 * @return String
	 */
	public String getIP() {
		return fIP;
	}
	
	/**
	 * Method setIP.
	 * @param String ip
	 */
	public void setIP(String ip) {
		fIP = ip;	
	}
	
	/**
	 * Method getClientVersion.
	 * @return boolean
	 */
	public boolean getClientVersion() {
		return fClientVersion;
	}
	
	/**
	 * Method setClientVersion.
	 * @param boolean version2
	 */
	public void setClientVersion(boolean version2) {
		fClientVersion = version2;
	}
}
