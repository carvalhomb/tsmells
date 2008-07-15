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
 * A class for an incoming message packet
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.4 $
 * @date $Date: 2003/05/18 11:01:54 $
 */
public class MSNIncomingMSGPacket extends MSNIncomingPacket {
	private String fHash = null;
	private String fContact = null;
	private String fContactNick = null;
	private String fNick = null;
	private String fType = null;
	private String fFromName = null;
	private String fNrOfUnreadMsgs = null;
	private int fLength;

	/**
	 * @see messenger.MSN.MSNIncomingPacket#MSNIncomingPacket(String)
	 */
	public MSNIncomingMSGPacket(String receivedPacket) {
		super(receivedPacket);
		
		fContact = (String)tokens.elementAt(1);
		fContactNick = (String)tokens.elementAt(2);
		fLength = new Integer((String)tokens.elementAt(3)).intValue();	
		this.parse();
	}
	
	private void parse() {
		int index = tokens.lastIndexOf("Content-Type:");
		String type = "";
		
		if (index != -1) {
			type = (String)tokens.elementAt(index + 1);
			if (type.endsWith(";")) {
				type = type.substring(0, type.length() - 1);
			}
		}
		
		if (type.equals("text/x-msmsgsprofile")) {
			fType = "profile";
		} else if (type.equals("text/x-msmsgscontrol")) {
			fType = "control";
		} else if (type.equals("text/x-msmsgsinitialemailnotification")) {
			fType = "initialemailnotification";
			
			fNrOfUnreadMsgs = this.extractInfo("Inbox-Unread:");
		
			
		} else if (type.equals("text/x-msmsgsemailnotification")) {
			fType = "emailnotification";

			fFromName = this.extractInfo("From:");
			
		} else if (type.equals("text/x-msmsgsactivemailnotification")) {
			fType = "activemailnotification";
		}
	}
	
	private String extractInfo(String info) {
		int index = tokens.lastIndexOf(info);
		String s = "";
	
		if (index != -1) {
			s = (String)tokens.elementAt(index + 1);
			if (s.endsWith(";")) {
				s = s.substring(0, s.length() - 1);
			}
			
			return s;
		}
		
		return null;
	}
	
	public String getFromName() {
		return fFromName;
	}
	
	public String getNrOfUnreadMsgs() {
		return fNrOfUnreadMsgs;
	}
	
	public String getType() {
		return fType;
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
	
	/**
	 * @see messenger.IPacket#getLength()
	 */
	public int getLength() {
		return fLength;
	}
}
