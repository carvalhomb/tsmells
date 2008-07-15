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

/**
 * A class representing an MSN USR - I packet.
 * Send to the server to ask if the encryption used is MD5.
 * 
 * >>> USR <transaction ID> MD5 I <your e-mail>
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:25 $
 */
public class MSNOutgoingUSRIPacket extends MSNOutgoingPacket {
	private String fEncryption = "MD5";
	private String fUserName = "";


	/**
	 * Constructor for MSNUSRIPacket.
	 * @param arg
	 */
	public MSNOutgoingUSRIPacket(Vector arg) {
		assert arg.size() == 3;
		
		fCmd = "USR";
		fTrID = (Long)arg.elementAt(0);
		fEncryption = (String)arg.elementAt(1);
		fUserName = (String)arg.elementAt(2);

		setCommand(fCmd);
		setTrID(fTrID);
		addArgument(fEncryption);
		addArgument("I");
		addArgument(fUserName);
	}
}
