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
 * @date $Date: 2003/04/16 17:00:55 $
 * 
 * This is sent when a user goes offline.
 */
public class ICQSRV_USEROFFLINEPacket extends ICQPacket {
	private String fUin;
	
	/**
	 * Method ICQSRV_USEROFFLINEPacket.
	 * @param s
	 */
	public ICQSRV_USEROFFLINEPacket(String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_USEROFFLINE");
		
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
	}
}
