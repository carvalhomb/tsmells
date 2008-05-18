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
 * A class representing an MSN USR packet.
 * Send as a reply to a XFR packet, and used to authenticate with the SB server
 * 
 * >>> USR (transaction ID) (your e-mail) (CKI)
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:44 $
 */
public class MSNOutgoingUSRPacket extends MSNOutgoingPacket {
	private String fLogin = "";
	private String fCKI = "";


	/**
	 * Method MSNUSRPacket.
	 * @param arg
	 */
	public MSNOutgoingUSRPacket(Vector arg) {
		assert arg.size() == 3;

		fCmd = "USR";
		fTrID	=	(Long)arg.elementAt(0);
		fLogin	=	(String)arg.elementAt(1);
		fCKI		=	(String)arg.elementAt(2);
		
		setCommand(fCmd);
		setTrID(fTrID);
		addArgument(fLogin);
		addArgument(fCKI);
	}
}
