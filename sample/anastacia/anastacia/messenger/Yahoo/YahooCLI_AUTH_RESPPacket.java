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
 * @date $Date: 2003/04/04 18:03:08 $
 * 
 */
public class YahooCLI_AUTH_RESPPacket extends YahooPacket {
	private String fString1 = "", fString2 = "", fUin = "";
	
	/**
	 * Method YahooCLI_AUTH_RESPPacket.
	 * @param arg
	 */
	public YahooCLI_AUTH_RESPPacket(Vector arg) {
		assert (arg.size() == 3);
		
		fUin = (String)arg.elementAt(0);
		fString1 = (String)arg.elementAt(1);
		fString2 = (String)arg.elementAt(2);
		
		/*System.out.println("String1: "+fString1);
		System.out.println("String2: "+fString2);*/
		
		setKindOfPacket("CLI_AUTH_RESP");
		updatePacket();
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = "30"
							+ARGUMENT_SEPARATOR
							+fUin
							+ARGUMENT_SEPARATOR
							+"36"
							+ARGUMENT_SEPARATOR
							+fString1
							+ARGUMENT_SEPARATOR
							+"3936"
							+ARGUMENT_SEPARATOR
							+fString2
							+ARGUMENT_SEPARATOR
							+"32"
							+ARGUMENT_SEPARATOR
							+"31"
							+ARGUMENT_SEPARATOR
							+"31"
							+ARGUMENT_SEPARATOR
							+fUin
							+ARGUMENT_SEPARATOR;
		
		this.setHeader("54", body.length());
		this.setBody(body);
		this.setContent();
	}
}