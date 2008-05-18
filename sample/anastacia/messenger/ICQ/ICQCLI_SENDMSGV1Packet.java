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
 * Send a message with the older protocol. Many Open Source clients still 
 * use this kind of messages
 */
public class ICQCLI_SENDMSGV1Packet extends ICQOutgoingPacket {
	private String fUin = "";
	private String fMessage = "";

	/**
	 * Method ICQCLI_SENDMSGV1Packet.
	 * @param Vector arg
	 * @pre assert arg.size() == 2
	 * @post assert fUin.length()/2 < 10
	 * @post assert fMessage != ""
	 */
	public ICQCLI_SENDMSGV1Packet(Vector arg) {
		assert (arg.size() == 2);
		
		fUin = (String)arg.elementAt(0);
		fMessage = (String)arg.elementAt(1);
		
		setKindOfPacket("CLI_SENDMSGV1");
		updatePacket();
		
		assert (fUin.length()/2 < 10);
		assert (fMessage != "");
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		
		String tlv257 = TLV257
									+Utils.TwoByteInt2FourCharString((fMessage.length()/2)+4)
									+"00000000"
									+fMessage;
									
		String tlv0002 = TLV1281
									+"0001"
									+"01"
									+tlv257;
												
		String body = SNAC4_6
								+"00000000000A" 
								+"0000000000000000"
								+"0001"
								+Utils.intToHex(fUin.length()/2)+fUin
								+"0002"
								+Utils.TwoByteInt2FourCharString(tlv0002.length()/2)
								+tlv0002
								+"00060000";

		setContent(Utils.fromHexString(SNACS+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}									
}
