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
import messenger.BadArgumentException;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:48 $
 * 
 * This sets the clients online status code and some other direct client to client information as well.
 */
public class ICQCLI_SETSTATUSPacket extends ICQOutgoingPacket {
	private String status = "";
	
	/**
	 * Method ICQCLI_SETSTATUSPacket.
	 * @param arg
	 * @throws BadArgumentException
	 * 
	 * @pre assert arg.size() == 1
	 * @post assert status.length() == 8
	 */
	public ICQCLI_SETSTATUSPacket(Vector arg) throws BadArgumentException {
		assert (arg.size() == 1);
		
		String s = (String)arg.elementAt(0);
		if(s.equals("ONLINE")) { status = "00000000"; }
		else if(s.equals("AWAY")) { status = "00000001"; }
		else if(s.equals("DND")) { status = "00000013"; }
		else if(s.equals("NA")) { status = "00000005"; }
		else if(s.equals("OCCUPIED")) { status = "00000011"; }
		else if(s.equals("INVISIBLE")) { status = "00000100"; }
		else if(s.equals("FREEFORCHAT")) { status = "00000020"; }
		else { throw new BadArgumentException(); }
		
		setKindOfPacket("CLI_SETSTATUS");
		updatePacket();
		assert (status.length() == 8);
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = SNAC1_1E
									+"00000000000D"
									+TLV6+"0004"+status	// lengte en status
									+TLV8+"0002"+"0000"
									+TLV12+"0025" // lengte
												+"C0A800E3" // internal ip
												+"000084D0" // TCP port
												+"01" // firewall
												+"0008" // protocol 8
												+"899FCDA0" // direct connect cookie
												+"0000" // unknown
												+"0050" // unknown
												+"0000" // unknown
												+"0003" // count: 3
												+"3BA8DBAF"
												+"3BEB5373"
												+"3BEB5262"
												+"0000";
																	
		setContent(Utils.fromHexString(SNACS+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
}
