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

import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:41 $
 * 
 * 
 */
public class ICQSRV_ICBMERRPacket extends ICQPacket {
	private String fReason = "";
	
	public ICQSRV_ICBMERRPacket (String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_ICBMERR");
	
		analyze();
	}
	
	private void analyze() {
		String s = getContentHex();
		
		if(s.charAt(35) == 'e') {
			fReason = "Malformed Packet";
		}
		else if(s.charAt(35) == '9') {
			fReason = "Client does not understand type-2 messages";
		}
		else if(s.charAt(35) == '4') {
			fReason = "User is offline";
		}
		else {
			fReason = "Unknown Reason";
		}
		
	}
	
	public String getReason() {
		assert fReason != "";
		return fReason;	
	}
}
