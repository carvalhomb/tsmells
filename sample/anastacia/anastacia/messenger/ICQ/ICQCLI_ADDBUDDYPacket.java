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
import java.util.Random;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.3 $
 * @date $Date: 2003/04/16 17:00:43 $
 * 
 * this packet is sent if you want to add a new user to you're server side 
 * contact list.
 */
public class ICQCLI_ADDBUDDYPacket extends ICQOutgoingPacket {
	private String fUin = "";
	private String fNick = "";

	/**
	 * Method ICQCLI_ADDBUDDYPacket.
	 * @param Vector arg
	 * @pre assert (arg.size() == 2)
	 * @post assert (fUin.length() < 10)
	 * @post assert (fNick != "")
	 */
	public ICQCLI_ADDBUDDYPacket(Vector arg) {
		assert (arg.size() == 2);
		
		fUin = (String)arg.elementAt(0);
		fNick = (String)arg.elementAt(1);
		
		/*System.out.println("fUin: "+fUin+" "+fUin.length());
		System.out.println("fNick: "+fNick+" "+fNick.length());
		System.out.println("HexNick: "+fU.HexFromString(fNick)+" "+fU.HexFromString(fNick).length());*/
	
		setKindOfPacket("CLI_ADDBUDDY");
		updatePacket();
		
		assert (fUin.length() < 10); // is er een minimumgrens?	
		assert (fNick != "");
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		int randomnumber = new Random().nextInt(65536);
		
		String body = SNAC13_8
									+"000000000000"
									+Utils.TwoByteInt2FourCharString(Utils.UIN(fUin).length()/2)+Utils.UIN(fUin) // lengte zit al mee ingecalculeerd
									+"4013"
									+Utils.intToHex(randomnumber)
									+"0000"
									+Utils.TwoByteInt2FourCharString(4+Utils.HexFromString(fNick).length()/2)
									+TLV0131
									+Utils.TwoByteInt2FourCharString(Utils.HexFromString(fNick).length()/2)
									+Utils.HexFromString(fNick);
		
		setContent(Utils.fromHexString(SNACS+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
}
