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
 * @date $Date: 2003/04/16 17:00:39 $
 * 
 * This command, like CLI_CHECKROSTER, requests the server side contact list. 
 * The difference between CLI_REQROSTER and CLI_CHECKROSTER is that 
 * CLI_REQROSTER has no parameters, and always causes SRV_REPLYROSTER 
 * (rather than SRV_REPLYROSTEROK). My guess is that CLI_REQROSTER is sent 
 * instead of CLI_CHECKROSTER when the client does not have a cached copy of the 
 * contact list; ie, the first time a user logs in with a particular client. 
 * 
 */
public class ICQCLI_REQROSTERPacket extends ICQOutgoingPacket {
	
	/**
	 * Method ICQCLI_REQROSTERPacket.
	 * @param Vector arg
	 * 
	 * @pre assert arg.size() == 0
	 */
	public ICQCLI_REQROSTERPacket(Vector arg) {
		assert (arg.size() == 0);
		
		setKindOfPacket("CLI_REQROSTER");
		updatePacket();
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		Utils u = new Utils();
		String body = SNAC13_4
									+"000000000000";
														
		setContent(Utils.fromHexString(SNACS+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
}
