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
 * A class for an incoming message session request packet
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:33 $
 */
public class MSNIncomingRNGPacket extends MSNIncomingPacket {
	private String fSessionID = null;
	private String fSwitchBoardIp = null;
	private String fCKI = null;
	private String fContact = null;
	private String fContactNick = null;

	/**
	 * @see messenger.MSN.MSNIncomingPacket#MSNIncomingPacket(String)
	 */
	public MSNIncomingRNGPacket(String receivedPacket) {
		super(receivedPacket);

		fSessionID = (String)tokens.elementAt(1);
		
		String SBIpAndPort = (String)tokens.elementAt(2);
		int index = SBIpAndPort.indexOf(':');
		fSwitchBoardIp = SBIpAndPort.substring(0, index);
		
		fCKI = (String)tokens.elementAt(4);
		fContact = (String)tokens.elementAt(5);
		fContactNick = (String)tokens.elementAt(6);
	}
	
	/**
	 * Method getSessionID.
	 * @return String
	 */
	public String getSessionID() {
		return fSessionID;
	}
	
	/**
	 * Method getSwitchBoardIp.
	 * @return String
	 */
	public String getSwitchBoardIp() {
		return fSwitchBoardIp;
	}
	
	/**
	 * Method getCKI.
	 * @return String
	 */
	public String getCKI() {
		return fCKI;
	}
	
	/**
	 * Method getContact.
	 * @return String
	 */
	public String getContact() {
		return fContact;
	}
	
	/**
	 * Method getContactNick.
	 * @return String
	 */
	public String getContactNick() {
		return fContactNick;	
	}
}
