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

import java.util.Vector;

/**
 * A class representing an MSN MSG packet.
 * 
 * >>> MSG (transaction ID) (acknowledge) (length) (MIME-header) (MIME-body)
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.2 $
 * @date $Date: 2003/05/18 11:01:54 $
 */
public class MSNOutgoingMSGPacket extends MSNOutgoingPacket {
	private String fMessage;
	private String fHeader;
	private String fNewline = "\r\n";
	private String fAcknowledge = "U";
	private String fCharSet = "UTF-8";
	private String fColor = "0";
	private int fLength = 0;


	/**
	 * Method MSNMSGPacket.
	 * @param arg
	 */
	public MSNOutgoingMSGPacket(Vector arg) {
		assert arg.size() == 2;
		
		fCmd		=	"MSG";
		fHeader	=	 fNewline
							+ "MIME-Version: 1.0" + fNewline
							+ "Content-Type: text/plain; charset=" + fCharSet + fNewline
							+ "X-MMS-IM-Format: " + "FN=" +"Arial" + "EF=" + "; " + "CO=" + fColor + "; " + "CS=0; " + "PF=22"
							+ fNewline
							+ fNewline;
		fTrID		=	(Long)arg.elementAt(0);
		fMessage	=	(String)arg.elementAt(1);
		fLength		=	 fHeader.length() + fMessage.length() - 2;
		
		setCommand(fCmd);
		setTrID(fTrID);
		addArgument(fAcknowledge);
		addArgument(new Integer(fLength).toString());
		addArgumentNoSpace(fHeader);
		addArgumentNoSpace(fMessage);
	}
	
	/**
	 * Method getMessage.
	 * @return String
	 */
	public String getMessage() {
		return fMessage;
	}
}
