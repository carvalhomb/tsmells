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

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/05/25 18:09:41 $
 * 
 * this is an abstract class where different protocols
 * can inherit their specific contact class from. A contact 
 * class represents a buddy who's on your contact list
 * 
 */
public abstract class AContact {
	private String fLogin, fNick = null;
	private String fStatus = null;
	
	/**
	 * @param String login string on a certain protocol
	 * 
	 * @pre login != null;
	 * @pre login != "";
	 * 
	 * @post fLogin == getLogin();
	 */
	public void setLogin(String login) {
		assert login != null;
		assert login != "";
		
		fLogin = login;
		
		assert(fLogin == getLogin());
	}
	
	/**
	 * @return String login
	 * 
	 * @pre fLogin != null;
	 * @pre fLogin != "";
	 */
	public String getLogin() {
		assert fLogin != null;
		assert fLogin != "";
		
		return fLogin;
	}
	
	/**
	 * @param String nick name
	 * 
	 * @pre nick != null;
	 * @pre nick != "";
	 */
	public void setNick(String nick) {
		assert nick != null;
		assert nick != "";
		
		fNick = nick;	
	}
	
	/**
	 * @return String nick name
	 * 
	 * @pre fNick != null;
	 * @pre fNick != "";
	 */
	public String getNick() {
		assert fNick != null;
		assert fNick != "";
		
		return fNick;
	}
	
	/**
	 * @param String a new status
	 * 
	 * @pre status != null;
	 * @pre status != "";
	 */
	public void setStatus(String status) {
		assert status != null;
		assert status != "";
		
		fStatus = status;	
	}
	
	/**
	 * @return String status
	 * 
	 * @pre fStatus != null;
	 * @pre fStatus != "";
	 */
	public String getStatus() {
		assert fStatus != null;
		assert fStatus != "";
		
		return fStatus;	
	}
}
