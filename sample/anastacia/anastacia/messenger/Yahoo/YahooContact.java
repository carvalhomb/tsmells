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

import messenger.AContact;


/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:08 $
 * 
 * 
 */
public class YahooContact extends AContact {
	private String fLogin;
	private String fIP;
	
	/**
	 * Method ICQContact.
	 * @param UIN
	 * @param nick
	 * @param IP
	 *
	 * @pre assert nick != "";
	 * @pre assert IP != "";
	 * @pre assert (IP.length() >= 7)&&(IP.length() <= 15);
	 * @post assert this.getUIN().equals(UIN);
	 * @post assert this.getNick().equals(nick);
	 * @post assert this.getIP().equals(IP);
	 */
	public YahooContact(String login, String nick, String IP) {
		assert nick != "";
		assert IP != "";
		assert (IP.length() >= 7)&&(IP.length() <= 15);
		
		this.setLogin(login);
		this.setNick(nick);
		fIP = IP;
		
		assert this.getUIN() == login;
		assert this.getNick().equals(nick);
		assert this.getIP().equals(IP);
	}
	
	/**
	 * Method YahooContact.
	 * @param login
	 */
	public YahooContact(String login) {
		assert login != "";
		setLogin(login);
	}
	
	/**
	 * @see messenger.AContact#setLogin(java.lang.String)
	 */
	public void setLogin(String login) {
		assert login != "";
		
		super.setLogin(login);
	}
	
	/**
	 * Method getUIN.
	 * @return String
	 */
	public String getUIN() {
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
	 * @param ip
	 */
	public void setIP(String ip) {
		fIP = ip;	
	}
	
	
}
