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
 * A class for an incoming online notification packet
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:34 $
 */
public class MSNIncomingNLNPacket extends MSNIncomingPacket {
	private String fContact = null;
	private String fContactNick = null;
	private String fStatus = null;

	/**
	 * @see messenger.MSN.MSNIncomingPacket#MSNIncomingPacket(String)
	 */
	public MSNIncomingNLNPacket(String receivedPacket) {
		super(receivedPacket);
		
		fStatus = (String)tokens.elementAt(1);
		fContact = (String)tokens.elementAt(2);
		fContactNick = (String)tokens.elementAt(3);
	}
	
	/**
	 * Method getContact.
	 * @return String
	 */
	public String getContact() {
		return fContact;
	}
	
	/**
	 * Method getStatus.
	 * @return String
	 */
	public String getStatus() {
		if (fStatus.equals("NLN")) {
			fStatus = "NLN";	
		}
		else if (fStatus.equals("PHN")) {
			fStatus = "PHN";
		}
		else if (fStatus.equals("LUN")) {
			fStatus = "LUN";
		}
		else if (fStatus.equals("FLN")) {
			fStatus = "FLN";
		}
		else if (fStatus.equals("BSY")) {
			fStatus = "BSY";
		}
		else if (fStatus.equals("BRB")) {
			fStatus = "BRB";
		}
		else if (fStatus.equals("AWY")) {
			fStatus = "AWY";
		}
		else if (fStatus.equals("IDL")) {
			fStatus = "IDL";
		}
		else {
			fStatus = "Unknown status " + fStatus;
		}
		
		return fStatus;
	}
	
	/**
	 * Method getContactNick.
	 * @return String
	 */
	public String getContactNick() {
		return fContactNick;
	}
}
