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

import java.util.Vector;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:52 $
 * 
 * The packet sent upon establishing a connection. If the client wants to login to login.icq.com, 
 * it sends all TLVs (CLI_IDENT) except TLV(6), which is for login to the redirected server (CLI_COOKIE). 
 * To request a new UIN, no TLV is sent (CLI_HELLO).
 */
public class ICQCLI_COOKIEPacket extends ICQOutgoingPacket {
	private String fCookie = "";

	/**
	 * Method ICQCLI_COOKIEPacket.
	 * @param Vector arg
	 * @pre assert arg.size() == 1
	 * @invariant assert fCookie != ""
	 * 
	 */
	public ICQCLI_COOKIEPacket(Vector arg) {
		assert (arg.size() == 1);
		
		fCookie = (String)arg.elementAt(0);
		
		assert fCookie != "";
		
		setKindOfPacket("CLI_COOKIE");
		updatePacket();
		
		assert fCookie != "";
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = HELLO
									+TLV6+Utils.TwoByteInt2FourCharString(fCookie.length()/2)+fCookie;
									
		setContent(Utils.fromHexString(LOGIN+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
	
}
