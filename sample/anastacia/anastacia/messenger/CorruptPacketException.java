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
 * @author Benny Van Aerschot
 * @version 10/10/2002
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CorruptPacketException extends Exception {

	/**
	 * Constructor for CorruptPacketException.
	 */
	public CorruptPacketException() {
		super();
	}

	/**
	 * Constructor for CorruptPacketException.
	 * @param message
	 */
	public CorruptPacketException(String message) {
		super(message);
	}

	/**
	 * Constructor for CorruptPacketException.
	 * @param message
	 * @param cause
	 */
	public CorruptPacketException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for CorruptPacketException.
	 * @param cause
	 */
	public CorruptPacketException(Throwable cause) {
		super(cause);
	}

}
