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

import java.util.Vector;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:23 $
 * 
 * 
 */
public class YahooCLI_MESSAGEPacket extends YahooPacket {
	private String fLogin, fTarget, fMessage;
	
	/**
	 * Method YahooCLI_MESSAGEPacket.
	 * @param arg
	 */
	public YahooCLI_MESSAGEPacket(Vector arg) {
		assert (arg.size() == 3);
		
		fLogin = (String)arg.elementAt(0);
		fTarget = (String)arg.elementAt(1);
		fMessage = (String)arg.elementAt(2);
		
		setKindOfPacket("CLI_AUTH_RESP");
		updatePacket();
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = "31"
							+ARGUMENT_SEPARATOR
							+fLogin
							+ARGUMENT_SEPARATOR
							+"35"
							+ARGUMENT_SEPARATOR
							+fTarget
							+ARGUMENT_SEPARATOR
							+"3134"
							+ARGUMENT_SEPARATOR
							+fMessage
							+ARGUMENT_SEPARATOR
							+"3937"
							+ARGUMENT_SEPARATOR
							+"30"
							+ARGUMENT_SEPARATOR
							+"3633"
							+ARGUMENT_SEPARATOR
							+"3b30"
							+ARGUMENT_SEPARATOR
							+"3634"
							+ARGUMENT_SEPARATOR
							+"30"
							+ARGUMENT_SEPARATOR
							+"31303032"
							+ARGUMENT_SEPARATOR
							+"31"
							+ARGUMENT_SEPARATOR;
		
		this.setHeader("06", body.length());
		this.setBody(body);
		this.setContent();
	}
}
