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

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:04:02 $
 * 
 * All ICQ packets are inherited from tis abstract class
 * 
 */
public abstract class ICQOutgoingPacket extends ICQPacket {
	
	/**
	 * @see java.lang.Object#Object()
	 */
	public ICQOutgoingPacket() {
		if(SEQUENCE == 65535) {
			SEQUENCE = 0;
		}
		else {
			++SEQUENCE;
		}
	}
	
	/**
	 * Method resetSequence.
	 */
	public void resetSequence() {
		SEQUENCE = 0;
	}
	
	/**
	 * Method getSequence.
	 * @return int sequence number
	 */
	public int getSequence() {
		return SEQUENCE;
	}
}
