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
 * @author Benny Van Aerschot
 * @version  $Revision: 1.2 $
 * @date $Date: 2003/04/04 19:33:56 $
 * 
 * USR 1 OK venky@hotmail.com nick
 * URS 5 MD5 S 454545646578.7458754
 */
public class MSNIncomingUSRPacket extends MSNIncomingPacket {
	private String fHash = null;

	/**
	 * @see messenger.MSN.MSNIncomingPacket#MSNIncomingPacket(String)
	 */
	public MSNIncomingUSRPacket(String receivedPacket) {
		super(receivedPacket);
		
		fHash =	(String)tokens.elementAt(4);
	}
	
	public String getAuth() {
		return (String)tokens.elementAt(2);
	}
	
	/**
	 * Method getHash.
	 * @return String
	 */
	public String getHash() {
		return fHash;
	}
}
