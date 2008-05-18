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
import messenger.Utils;

/**
 * A class representing an MSN QRY packet.
 * Send as a reply to a CHL packet.
 * 
 * >>> QRY (transaction ID) msmsgs@msnmsgr.com 32 (MD5 hash of the number send with the CHL)
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.3 $
 * @date $Date: 2003/05/18 11:01:54 $
 */
public class MSNOutgoingQRYPacket extends MSNOutgoingPacket {
	private String fHash = "";
	
	/**
	 * Method MSNQRYPacket.
	 * @param arg
	 */
	public MSNOutgoingQRYPacket(Vector arg) {
		assert arg.size() == 2;
		
		fCmd = "QRY";
		fTrID = (Long)arg.elementAt(0);
		fHash = (String)arg.elementAt(1);
		fHash = Utils.MD5Encrypt(fHash + "Q1P7W2E4J9R8U3S5");

		setCommand(fCmd);
		setTrID(fTrID);
		addArgument("msmsgs@msnmsgr.com");
		addArgument("32");
		addArgument("\r\n");
		addArgumentNoSpace(fHash);
	}
}
