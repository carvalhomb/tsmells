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
 * @date $Date: 2003/04/16 17:00:43 $
 * 
 * 
 */
public class ICQSRV_COOKIEPacket extends ICQPacket {
	private String fCookie = "";
	private String fIp = "";
	private int fPort = 0;
	
	public ICQSRV_COOKIEPacket(String s) {
		assert s != null;
		assert s != "";
		
		setContent(Utils.fromHexString(s));	
		setKindOfPacket("SRV_COOKIE");	
		
		analyze();
	}
	
	private void analyze() {
		String s = getContentHex();
		String serverIp2 = "", cookie = "", cookieLength = "", ipAndPortLength = "";
		int port2 = 0, cookielen = 0, i = 0;
		
		// nu halek de nieve ip en poort voor de nieuwe server
       	while(((i+3)<s.length())&&(!((s.charAt(i) == '0')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '0')&&(s.charAt(i+3) == '5')))) {
			i++; // brol doorspoelen
		}
		i += 3; // en nog wa verder
		
		for(int x = 1; x < 5; x++) {
			ipAndPortLength += s.charAt(i+x);
		}
		int iplen = Utils.hexToInt(ipAndPortLength);
		
		boolean getport = false;
		
		i += 5;
		int point = 0;
		for(int j = i; j < i+(2*iplen); j+=2) {
			if(s.charAt(j) == '3') { // hexadecimaal is de weergave van een cijfer 3x met x de eigenlijke waarde
				if(s.charAt(j+1) == 'a') { // 3a staat voor een :
					getport = true;
				}
				else if(!(getport)) {  // we zijn het ip nog aan het inlezen
					serverIp2 += s.charAt(j+1);
				}
				else {
					port2 = port2*10+(int)(s.charAt(j+1))-48;	// port aan het inlezen			
				}
			}
			else if((s.charAt(j) == '2')&&(s.charAt(j+1) == 'e')) { // we lezen een . in
				serverIp2 += ".";
			}	
		}
		
		fPort = port2;
		fIp = serverIp2;
		
		// nu halek het cookie uit de SRV_COOKIE
       	while(((i+3)<s.length())&&(!((s.charAt(i) == '0')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '0')&&(s.charAt(i+3) == '6')))) {
			i++; // brol doorspoelen
		}
		i += 3; // en nog wa verder
		
		cookieLength = "";
		for(int y = 1; y < 5; y++) {
			cookieLength += s.charAt(i+y);
		}
		cookielen = Utils.hexToInt(cookieLength);
			
		i += 5;
		for(int j = i; j < i+(2*cookielen); j+=1) {
			cookie += s.charAt(j);
		}
		fCookie = cookie;				
	}	
	
	public String getCookie() {
		assert fCookie != "";
		return fCookie;
	}
	
	public String getIp() {
		assert fIp.length() >= 8; // ex. 0.0.0.0 => lengte is 8
		assert fIp.length() <= 15; // ex. 255.255.255.255 => lengte is 15
		return fIp;
	}
	
	public int getPort() {
		assert fPort < 65536;
		assert fPort > 0;
		return fPort;
	}
}
