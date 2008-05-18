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
 * @date $Date: 2003/04/16 17:00:43 $
 * 
 * The first packet sent by the client after connecting and receiving the 
 * SRV_HELLO packet from the server. The packet basiclly identifies 
 * what kind and version of client is connecting along with the user's UIN 
 * and password.
 * 
 */
public class ICQCLI_IDENTPacket extends ICQOutgoingPacket { 
	private String fPassword = "";
	private String fUin = "";
	private String fVersion = "";

	/**
	 * Method ICQCLI_IDENTPacket.
	 * @param Vector arg
	 * @pre assert arg.size() == 3
	 * @post assert fUin.length()/2 < 10
	 * @post assert fPassword != ""
	 */
	public ICQCLI_IDENTPacket(Vector arg) {
		assert (arg.size() == 3);
		
		fUin = (String)arg.elementAt(0);
		fPassword = (String)arg.elementAt(1);
		fVersion = (String)arg.elementAt(2);
		
		/*System.out.println("CLI_IDENT UIN: "+fUin+" lengte: "+fUin.length());
		System.out.println("CLI_IDENT: "+fPassword+" lengte: "+fPassword.length());*/
		
		setKindOfPacket("CLI_IDENT");
		updatePacket();
		
		assert (fUin.length()/2 < 10); // is er een minimumgrens?	
		assert (fPassword != "");
}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = HELLO
									+TLV1+Utils.TwoByteInt2FourCharString(fUin.length()/2)+fUin // lengte zit al mee ingecalculeerd
									+TLV2+Utils.TwoByteInt2FourCharString(fPassword.length()/2)+fPassword
									+TLV3+Utils.TwoByteInt2FourCharString(fVersion.length()/2)+fVersion
									+TLV22+"0002010A"
									+TLV23+"00020005"
									+TLV24+"00020011"
									+TLV25+"00020001"
									+TLV26+"00020E3A"
									+TLV20+"000400000055"
									+TLV14+"00027573"
									+TLV15+"0002656E"; 
		
		setContent(Utils.fromHexString(LOGIN+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
}
