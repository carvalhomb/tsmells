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

import java.util.Vector;

/**
 * @author Benny Van Aerschot
 * @version 10/10/2002
 */
public class Buffer {
	private Vector fUserQueue;
	private Vector fDumpQueue;
	
	public Buffer() {
		fUserQueue = new Vector();
		fDumpQueue = new Vector();
	}
	
	public synchronized void setToUser(String s) {
		fUserQueue.add(s);	
	}
	
	public synchronized String getToUser() {
		String front = "";
		if (fUserQueue.size() > 0) {
			front = (String)fUserQueue.firstElement();
			fUserQueue.removeElementAt(0);
		}
		return front;	
	}
	
	public synchronized void setDump(String s) {
		fDumpQueue.add(s);	
	}
	
	public synchronized String getDump() {
		String front = "";
		if (fDumpQueue.size() > 0) {
			front = (String)fDumpQueue.firstElement();
			fDumpQueue.removeElementAt(0);
		}
		return front;
	}
	
	public synchronized boolean existsUserData() {
		return fUserQueue.size() > 0;
	}
	
	
	public synchronized boolean existsDump() {
		return fDumpQueue.size() > 0; 
	}
}
