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

import messenger.AContact;
import messenger.Utils;

/**
 * A class implementing a contact in the MSN contactlist
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:02:29 $
 */ 
public class MSNContact extends AContact {

	/**
	 * Method MSNContact.
	 * @param loginName
	 * @param nick
	 */
	public MSNContact(String loginName, String nick) {
		assert loginName != null;
		assert nick != null;
		assert loginName != "";
		assert nick != "";

		this.setLogin(loginName);
		this.setNick(Utils.URLQuoted2PlainText(nick));

		assert this.getLogin() == loginName;
		assert this.getNick().equals(nick);
	}
	
	/**
	 * Method MSNContact.
	 * @param loginName
	 */
	public MSNContact(String loginName) {
		assert loginName != null;
		assert loginName != "";
		
		this.setLogin(loginName);
		
		assert this.getLogin() == loginName;
	}

}
