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
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:23 $
 * 
 */
public class YahooSRV_AUTHPacket extends YahooPacket {
	private String fChallenge;
	
	/**
	 * Method YahooSRV_AUTHPacket.
	 * @param s
	 */
	public YahooSRV_AUTHPacket (String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_AUTH");

		analyze();
	}
	
	/**
	 * Method analyze.
	 */
	private void analyze() {
		String s = getContentHex();
		String challenge = "";
		int i = 0;	
		String sessionid = "";
		
		/* read sessionid */
		for(int k = 32; k < 40; k++) {
			//System.out.print(s.charAt(k));
			sessionid += s.charAt(k);
		}
		//System.out.println();
		
		setSessionID(sessionid);

		/* read challenge strings */
		for(int j = 0; j < 3; ++j) {
			while(((i+4)<s.length())&&(!((s.charAt(i) == 'c')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '8')&&(s.charAt(i+3) == '0')))) {
				//System.out.print(s.charAt(i));	
				i++; // brol doorspoelen			
			}
			//System.out.print(s.charAt(i));
			i++;
		}
		/*System.out.print(s.charAt(i));
		System.out.print(s.charAt(i+1));
		System.out.print(s.charAt(i+2));*/
		i+= 3;
		int start = i;
		while(i < start+48) {
			challenge += s.charAt(i);
			i++;
		}
							
		fChallenge = Utils.HexString2AsciiString(challenge);					
		/*System.out.println();
		System.out.println();
		System.out.println(fChallenge);*/
	}
	
	/**
	 * Method getChallenge.
	 * @return String
	 */
	public String getChallenge() {
		assert fChallenge.length() == 24;
		
		return fChallenge;
	}
}
