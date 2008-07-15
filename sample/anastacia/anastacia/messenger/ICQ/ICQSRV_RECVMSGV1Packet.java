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
 * @date $Date: 2003/04/16 17:00:34 $
 * 
 * 
 */
public class ICQSRV_RECVMSGV1Packet extends ICQPacket {
	private String fMessage = "";
	private String fUin = "";
	
	public ICQSRV_RECVMSGV1Packet(String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_RECVMSGV1");
		
		analyze();
	}
	
	/**
	 * Method analyze.
	 */
	private void analyze() {
		String s = getContentHex();
		String uin = "";
		
		int length = (((int)s.charAt(8))-48)*4096 + (((int)s.charAt(9))-48)*256 + (((int)s.charAt(10))-48)*16 + ((int)(s.charAt(11))-48);
		
		
		if((((int)s.charAt(51))-48) == 1) {
			for(int i = 0; i < (((int)s.charAt(53))-48)*2; i=i+2) {
				/*System.out.println(s.charAt(55+i));*/
				uin += s.charAt(55+i);
			}
			fUin = uin;
			
			boolean found = false;
			
			int j = 80; // ervoor komt het zeker nie
			while((j < s.length())&&(!found)) {	
				if((s.charAt(j) == '0')&&(s.charAt(j+1) == '1')&&(s.charAt(j+2) == '0')&&((s.charAt(j+3) == '2')||(s.charAt(j+3) == '1'))&&
					(s.charAt(j+4) == '0')&&(s.charAt(j+5) == '1')&&(s.charAt(j+6) == '0')&&(s.charAt(j+7) == '1')) {
						// message
					/*System.out.println(s.charAt(j-2));
					System.out.println(s.charAt(j-1));*/
					j += 7;
					j += 3;
					int messagelength = (((int)s.charAt(j-2))-48)*16 + (((int)s.charAt(j-1))-48);	
					found = true;
					j += 8;	
					String message = "";
					
					j += 2;
					while(j < s.length()) {
						message += s.charAt(j);
						j++;
					}
					
					fMessage = Utils.HexString2AsciiString(message);
								
				}
				else {
					j++;
				}
			}			
		}		
	}
	
	/**
	 * Method getUin.
	 * @return String
	 */
	public String getUin() {
		return fUin;
	}
	
	/**
	 * Method getMessage.
	 * @return String
	 */
	public String getMessage() {
		return fMessage;
	}
}
