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

import java.util.Vector;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:46 $
 * 
 * This packet sends the client's capabilities information to the server.
 */
public class ICQCLI_SETUSERINFOPacket extends ICQOutgoingPacket {
	
	/**
	 * Method ICQCLI_SETUSERINFOPacket.
	 * @param Vector arg
	 * @pre assert arg.size() == 0
	 */
	public ICQCLI_SETUSERINFOPacket(Vector arg) {
		assert (arg.size() == 0);
		
		setKindOfPacket("CLI_SETUSERINFO");
		updatePacket();
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = SNAC2_4
									+"00000000000C"
									+"0005002009461349"
									+"4C7F11D182224445"
									+"5354000009491349"
									+"4C7F11D182224445"
									+"53540000";
														
		setContent(Utils.fromHexString(SNACS+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
}
