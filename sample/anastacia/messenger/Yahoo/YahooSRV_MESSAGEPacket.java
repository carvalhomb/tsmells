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

package messenger.Yahoo;

import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.4 $
 * @date $Date: 2003/04/27 21:39:27 $
 * 
 */
public class YahooSRV_MESSAGEPacket extends YahooPacket {
	private String fNick = "";
	private String fMessage = "";
	
	
	public YahooSRV_MESSAGEPacket (String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_MESSAGE");

		analyze();
	}
	
	private void analyze() {
		String s = getContentHex();
		String nick = "", message = "";
		int i = 0;
		
		while(((i+6) < s.length())&&(!((s.charAt(i) == '3')&&(s.charAt(i+1) == '4')
											&&(s.charAt(i+2) == 'c')&&(s.charAt(i+3) == '0')  
											&&(s.charAt(i+4) == '8')&&(s.charAt(i+5) == '0')))) {
			i++;										
		}
		i += 6;
	
		if(!((i+6) < s.length())) {
			
		}
		else {
			// lees nickname in
			while(!((s.charAt(i) == 'c')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '8')&&(s.charAt(i+3) == '0'))) {
				nick += s.charAt(i);
				i++;
			}						
			//System.out.println(nick);
			fNick = Utils.HexString2AsciiString(nick);	
			i +=3;
			
			// spoel gedeelte tussen nick en message en de separators door
			while(((i+7)<s.length())&&(!((s.charAt(i) == '3')&&(s.charAt(i+1) == '1')
												&&(s.charAt(i+2) == '3')&&(s.charAt(i+3) == '4')
												&&(s.charAt(i+4) == 'c')&&(s.charAt(i+5) == '0')
												&&(s.charAt(i+6) == '8')&&(s.charAt(i+7) == '0')))) {
				i++; // brol doorspoelen			
			}
			i +=8;
			
			// lees message content in
			while((i+4 < s.length())&&(!((s.charAt(i) == 'c')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '8')&&(s.charAt(i+3) == '0')))) {
				if(((i%2 == 0)&&(s.charAt(i) == '3')&&(s.charAt(i+1) == 'c'))) { // find a 3c '<', not a x3 cy
					while(!((i%2 == 0)&&(s.charAt(i) == '3')&&(s.charAt(i+1) == 'e')))  {
						i++; // skip font settings
					}
					i+=2;
				}		
				message += s.charAt(i);
				i++;
			}	
			//System.out.println(message);
			
			fMessage = Utils.HexString2AsciiString(message);					
			
			//System.out.println(fNick+" says "+fMessage);
		}
	}
	
	public String getLogin() {
		return fNick;
	}
	
	public String getMessage() {
		return fMessage;
	}
		
}
