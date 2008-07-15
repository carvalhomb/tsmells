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
import messenger.ABytePacket;

/**
 * @author Bart Van Rompaey & Benny Van Aerschot
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:29 $
 * 
 * 
 */
public class YahooPacket extends ABytePacket {
	private String fHeader = null;
	private String fBody = null;
	private int fLength;
	private String fService;	//???
	private String fStatus;
	private String fSessionId;
	private String fKindOfPacket = "";
	private String fType;														// <- check this out
	private static String fSessionID = "00000000";
	private String UNKNOWN = "00000000"; 				// <- check this out
	private final String CLI_HEADER = "594D5347";					// YMSG
	private final String PROTOCOL_VERSION = "09000000"; // protocol version 9
	private final String TYPE = "57";
	protected final String ARGUMENT_SEPARATOR = "C080";
	
	/**
	 * @see java.lang.Object#Object()
	 */
	public YahooPacket() {
		
	}
	
	/**
	 * Method setHeader.
	 * @param type
	 * @param length
	 */
	public void setHeader(String type, int length) {
		fType = type;
		fLength = length + 20; 	// header (20 bytes) + body (variable)
		
		fHeader = CLI_HEADER
					+ PROTOCOL_VERSION
					+ Utils.TwoByteInt2FourCharString(length/2)
					+ "00"
					+ fType			// bijv L is dan 4C
					+ UNKNOWN
					+ fSessionID; 	// sessionID (initially = "00000000")
	}
	
	/**
	 * Method setUNKNOWN.
	 * @param s
	 */
	public void setUNKNOWN(String s) {
		assert s.length() == 8;
		
		UNKNOWN = s;
	}
	
	/**
	 * Method setBody.
	 * @param body
	 */
	public void setBody(String body) {
		fBody = body;
	}
	
	/**
	 * Method setSessionID.
	 * @param sessionID
	 */
	public void setSessionID(String sessionID) {
		assert sessionID.length() == 8;
		fSessionID = sessionID;
		//System.out.println(sessionID);
	}
	
	/**
	 * Method setContent.
	 */
	public void setContent() {
		if ((fBody == null) || (fBody.equals(""))) {
			this.setContent(Utils.fromHexString(fHeader));
		} else {
			this.setContent(Utils.fromHexString(fHeader + fBody));
		}		
	}
	
	/**
	 * Method setKindOfPacket.
	 * @param s
	 */
	protected void setKindOfPacket(String s) { // protected: alleen afgeleide klasse mogen dit zetten of intern, maar nooit de oproepende klasse
		assert s != null;

		fKindOfPacket = s;		
	}
	
	/**
	 * Method getKindOfPacket.
	 * @return String
	 */
	public String getKindOfPacket() {
		assert fKindOfPacket != null;

		return fKindOfPacket;
	}
}
