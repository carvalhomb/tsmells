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

package messenger.MSN;

/**
 * A class for an incoming version packet
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.2 $
 * @date $Date: 2003/04/04 19:33:56 $
 * 
 * XFR 2 NS ip:port 0 oldip:oldport
 * XFR 9 SB ip:port CKI 464548644.544534
 */
public class MSNIncomingXFRPacket extends MSNIncomingPacket {
	private String fNF = null;
	private String fNFIpAndPort = null;
	private String fNFIp = null;
	private int fNFPort = 0;

	/**
	 * @see messenger.MSN.MSNIncomingPacket#MSNIncomingPacket(String)
	 */
	public MSNIncomingXFRPacket(String receivedPacket) {
		super(receivedPacket);
		
		fNF					=	(String)tokens.elementAt(2);
		fNFIpAndPort	=	(String)tokens.elementAt(3);
		
		int index = fNFIpAndPort.indexOf(':');
		fNFIp = fNFIpAndPort.substring(0, index);
		fNFPort = (new Integer(fNFIpAndPort.substring(index+1))).intValue();
	}
	
	public String getNF() {
		return fNF;
	}
	
	/**
	 * Method getIp.
	 * @return String
	 */
	public String getIp() {
		return fNFIp;
	}
	
	/**
	 * Method getCKI.
	 * @return String
	 */
	public String getCKI() {
		return (String)tokens.elementAt(5);
	}
}
