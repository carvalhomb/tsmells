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
public class ICQSRV_RECVMSGV2Packet extends ICQPacket {
	private String fMessage = "";
	private String fUin = "";
	
	public ICQSRV_RECVMSGV2Packet(String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_RECVMSGV2");
		
		analyze();
	}
	
	private void analyze() {
		String s = getContentHex();
		String uin = "";
		
		int length = (((int)s.charAt(8))-48)*4096 + (((int)s.charAt(9))-48)*256 + (((int)s.charAt(10))-48)*16  + ((int)(s.charAt(11))-48);
		if((((int)s.charAt(51))-48) == 2) {
			for(int i = 0; i < (((int)s.charAt(53))-48)*2; i=i+2) {
				uin += s.charAt(55+i);
			}
			fUin = uin;
			/*int j = (((int)s.charAt(53))-48)*2;*/ // we beginnen te zoeken naar de eigenlijke message ergens achter uin 
			// voorkomst in het packet
			boolean found = false;
			int j = 128;
			
			while((j+21 < s.length())&&(!found)) {	
				if((s.charAt(j) == '0')&&(s.charAt(j+1) == '0')&&(s.charAt(j+2) == '0')&&(s.charAt(j+3) == '0')&&
					(s.charAt(j+4) == '0')&&(s.charAt(j+5) == '0')&&(s.charAt(j+6) == '0')&&(s.charAt(j+7) == '0')&&
					(s.charAt(j+8) == '0')&&(s.charAt(j+9) == '0')&&(s.charAt(j+10) == '0')&&(s.charAt(j+11) == '0')
					&&(s.charAt(j+12) == '1')) {
						// message
					j += 12;
					found = true;
					j += 7;
					/*System.out.println(j+" "+s.charAt(j));
					System.out.println(j+1+" "+s.charAt(j+1));*/
					
					int messagelength = (((int)s.charAt(j))-48)*16 + (((int)s.charAt(j+1))-48);
					/*System.out.println(messagelength);*/
					j += 8;
					String message = "";		
					/*System.out.println(j+" "+s.charAt(j+1));*/	
					while(!(s.charAt(j) == '0')) {
						message += s.charAt(j);
						message += s.charAt(j+1);
						/*System.out.println(j+" "+s.charAt(j));
						System.out.println(j+" "+s.charAt(j));*/
						j = j+2;
					}
					/*System.out.println("");
					System.out.println(message);*/
					fMessage = Utils.HexString2AsciiString(message);
					/*System.out.println(fMessage);*/			
				}
				else {
					j++;
				}			
			}			
		}		
	}
	
	public String getUin() {
		return fUin;
	}
	
	public String getMessage() {
		return fMessage;
	}
}
