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
 * @date $Date: 2003/04/16 17:00:48 $
 * 
 * This command is sent as what is perhaps an acknowledgement reply 
 * to at least SNAC(13,8), SNAC(13,9), and SNAC(13,a).
 * 
 */
public class ICQSRV_UPDATEACKPacket extends ICQPacket {
	private String fComment = "";
	
	/**
	 * Method ICQSRV_UPDATEACKPacket.
	 * @param String packet in hex form
	 */
	public ICQSRV_UPDATEACKPacket(String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_UPDATEACK");
		
		analyze();
	}
	
	/**
	 * Method analyze.
	 */
	private void analyze() {
		String s = getContentHex();
		
		if(s.charAt(35) == '0') {
			fComment = "Success";	
		}
		else if(s.charAt(35) == '3') {
			fComment = "Entry Already Exists";
		}
		else if(s.charAt(35) == 'a') {
			fComment = "Failed";
		}
		else if(s.charAt(35) == 'e') {
			fComment = "Failed, Authorization required to add buddy";	
		}
		else {
			fComment = "Unknown Reason";
		}	
	}
	
	public String getComment() {
		return fComment;	
	}
}
