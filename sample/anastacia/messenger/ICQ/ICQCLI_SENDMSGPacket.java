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
 * Send a message.
 */
public class ICQCLI_SENDMSGPacket extends ICQOutgoingPacket {
	private String fUin = "";
	private String fMessage = "";

	/**
	 * Method ICQCLI_SENDMSGPacket.
	 * @param Vector arg
	 * 
	 * @pre assert arg.size() == 2
	 * @post assert fUin.length()/2 < 10
	 * @post assert fMessage != ""
	 */
	public ICQCLI_SENDMSGPacket(Vector arg) {
		assert (arg.size() == 2);
		
		fUin = (String)arg.elementAt(0);
		fMessage = (String)arg.elementAt(1);
		
		setKindOfPacket("CLI_SENDMSG");
		updatePacket();
		
		assert (fUin.length()/2 < 10);
		assert (fMessage != "");
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String TLV2Content = TLV1281+"000101"+TLV257+Utils.TwoByteInt2FourCharString(fMessage.length()/2+4)
												+"00000000"+fMessage;
		
		// versie 1
		/*String body = SNAC4_6
									+"000000000000"
									+"0000000000000000"
									+"0001"
									+fU.intToHex(fUin.length()/2)+fUin // deze lengte wel zo want maar 2 bytes
									+TLV2+fU.TwoByteInt2FourCharString(TLV2Content.length()/2)
									+TLV1281+"000101"
									+TLV257+fU.TwoByteInt2FourCharString(fMessage.length()/2+4)
									+"00000000"
									+fMessage
									//+"0D0A"
									+TLV6+"0000";*/
									
		// versie 2					
		/*String messagesnac = "05010001"
								+"010101"
								+fU.TwoByteInt2FourCharString(fMessage.length()/2+4)
								+"00000000"
								//+fU.HexFromString(fMessage)
								+fMessage;
		
		String body = SNAC4_6
								+"000000000000"
								+"0000000000000000"
								+"0000000000000001"
								+fU.intToHex(fUin.length()/2)+fUin
								+"0002"
								+fU.TwoByteInt2FourCharString(messagesnac.length()/2)
								+messagesnac
								+"00060000";*/
								
		//versie 3 WERKT voor 4letterwoorden
		/*String snac0005 = "00006A14" 
								+"0500322900000946" 
								+"13494C7F11D18222" 
								+"444553540000000A" 
								+"00020001000F0000"
								+"271100421B000800" 
								+"0000000000000000" 
								+"0000000000000000"
								+"00000300000000FE" 
								+"FF0E00FEFF000000" 
								+"0000000000000000"
								+"000100010021"
								+fU.TwoByteInt2FourCharString(fMessage.length()/2+1)
								+"00"
								+fMessage
								+"0000000000FFFFFF000";
	
		String body = SNAC4_6
								+"0000000A" 
								+"00066A1405003229"
								+"00000002"
								+fU.intToHex(fUin.length()/2)+fUin
								+"0005"
								+fU.TwoByteInt2FourCharString(snac0005.length()/2)
								+snac0005
								+"0030000";*/
								
		String snac2711 = "1B000800" 
								+"0000000000000000" 
								+"0000000000000000"
								+"00000300000000FE" 
								+"FF0E00FEFF000000" 
								+"0000000000000000"
								+"000100010021"
								+Utils.TwoByteInt2FourCharString(fMessage.length()/2+1)
								+"00"
								+fMessage
								+"0000000000FFFFFF000";
		
		
		String snac0005 = "00006A14" 
								+"0500322900000946" 
								+"13494C7F11D18222" 
								+"444553540000000A" 
								+"00020001000F0000"
								+"2711"
								+Utils.TwoByteInt2FourCharString(snac2711.length()/2)
								+snac2711;
	
		String body = SNAC4_6
								+"0000000A" 
								+"00066A1405003229"
								+"00000002"
								+Utils.intToHex(fUin.length()/2)+fUin
								+"0005"
								+Utils.TwoByteInt2FourCharString(snac0005.length()/2)
								+snac0005
								+"0030000";
								
		setContent(Utils.fromHexString(SNACS+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
}
