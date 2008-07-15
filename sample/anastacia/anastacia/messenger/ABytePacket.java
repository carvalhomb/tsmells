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

package messenger;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:02:53 $
 * 
 * an abstract class where byte packet based protocols
 * inherit there packets from
 * 
 */
public abstract class ABytePacket implements IPacket { 
	private byte[] fContent;
	private int length;

	/**
	 * Method getContentHex.
	 * @return String
	 */
	public String getContentHex() {
		return Utils.byteToHex(fContent);
	}
	
	/**
	 * Method getContentByte.
	 * @return byte[]
	 */
	public byte[] getContentByte() {
		return fContent;
	}
	
	/**
	 * Sets the fContent.
	 * @param fContent The fContent to set
	 */
	public void setContent(byte[] content) {
		fContent = content;
	}

	/**
	 * @see IPacket#getLength()
	 */
	public int getLength() {
		return fContent.length;
	}

	

}
