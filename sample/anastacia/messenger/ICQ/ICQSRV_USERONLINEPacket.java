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
 * @date $Date: 2003/04/16 17:00:53 $
 * 
 * This SNAC is sent whenever a contact in your contact list goes online or changes status.
 */
public class ICQSRV_USERONLINEPacket extends ICQPacket {
	private String fStatus = "";
	private String fStatusHex = "";
	private String fUin = "";
	
	/**
	 * Method ICQSRV_USERONLINEPacket.
	 * @param String packet in hex form
	 */
	public ICQSRV_USERONLINEPacket(String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_USERONLINE");
		
		analyze();
	}
	
	/**
	 * Method getUin.
	 * @return String UIN
	 */
	public String getUin() {
		return fUin;	
	}
	
	/**
	 * Method getStatus.
	 * @return String users' new status
	 */
	public String getStatus() {
		return fStatus;	
	}
	
	/**
	 * Method analyze.
	 */
	private void analyze() {
		String s = getContentHex();
		String uin = "";
		boolean found = false;
		
		int length = (((int)s.charAt(32))-48)*16 + (((int)s.charAt(33))-48);
		
		for(int i = 0; i < length*2; i=i+2) {
			uin += s.charAt(35+i);
		}
		fUin = uin;
		
		int j = 0;
		while((j < s.length())&&(!found)) {	
			if((s.charAt(j) == '0')&&(s.charAt(j+1) == '0')&&(s.charAt(j+2) == '0')&&(s.charAt(j+3) == '6')&&
				(s.charAt(j+4) == '0')&&(s.charAt(j+5) == '0')&&(s.charAt(j+6) == '0')&&(s.charAt(j+7) == '4')) {
				found = true;
				j += 8;	
				String message = "";
				j += 4; // nog 2 bytes warek nie goe van weet wat ze doen
					
				for(int k = 0; k < 4; k++) {						
					fStatusHex += s.charAt(j);
					j++;
				}		
			}
			else {
				j++;
			}	
		}	
		
		if(fStatusHex.equals("0000")) {
			fStatus = "ONLINE";
		}
		else if(fStatusHex.equals("0001")) {
			fStatus = "AWAY";	
		}	
		else if(fStatusHex.equals("0011")) {
			fStatus = "OCCUPIED";
		}
		else if(fStatusHex.equals("0013")) {
			fStatus = "DND";
		}
		else if(fStatusHex.equals("0005")) {
			fStatus = "N/A";
		}
		else if(fStatusHex.equals("0100")) {
			fStatus = "INVISIBLE";	
		}
		else if(fStatusHex.equals("0020")) {
			fStatus = "FREE FOR CHAT";	
		}
		else {
			System.out.println("STATUS: "+fStatusHex);
			fStatus = "UNKNOWN STATUS";
		}
	}
}
