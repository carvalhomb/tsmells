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
 * A class for exchanging information between MSN/ICQ Protocol and Text/Data Listener
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:57 $
 */
public class DataBuffer {
	private boolean fDataForUser;			// true if there is data in fToUser
	private boolean fIsDump;					// true if there is data in fDump
	private String fToUser;						// data for the user
	private String fDump;						// dump info
	

	/**
	 * @see java.lang.Object#Object()
	 */
	public DataBuffer() {
		fDataForUser = false;
		fIsDump = false;
		fToUser = "";
		fDump = "";
	}
	
	
	/**
	 * Method setToUser.
	 * @param s
	 */
	public synchronized void setToUser(String s) {
		assert s != "";
		
		while (fDataForUser == true) {
			try {
				//System.out.println("*** waiting in setToUser ***");
				wait();
				
			} catch (InterruptedException e) {
	        	System.out.println("Exception occurred: " + e.getMessage());
			}
		}
		
		fDataForUser = true;
		fToUser = s;
		//System.out.println("*** setToUser ***");		// debug
		
		//notifyAll();	
	}


	/**
	 * Method getToUser.
	 * @return String
	 */
	public synchronized String getToUser() {
		while (fDataForUser == false) {
			try {
				//System.out.println("*** waiting in getToUser ***");
				wait();
				
			} catch (InterruptedException e) {
	                System.out.println("Exception occurred: " + e.getMessage());
			}
		}
		fDataForUser = false;
		//notifyAll();
	//	System.out.println("*** getToUser ***");		// debug
		return fToUser;
	}

	
	/**
	 * Method setDump.
	 * @param s
	 */
	public synchronized void setDump(String s) {
		assert s != "";
		
		while (fIsDump == true) {
			try {
				//System.out.println("*** waiting in setToDump ***");
				wait();
					
			} catch (InterruptedException e) {
	                System.out.println("Exception occurred: " + e.toString());
			}
		}
		fIsDump = true;
		fDump = s;
	
		notifyAll();	
	}
	
	
	/**
	 * Method getDump.
	 * @return String
	 */
	public synchronized String getDump() {
		
		while (fIsDump == false) {
			try {
				//System.out.println("*** waiting in getDump ***");
				wait();
				
			} catch (InterruptedException e) {
	                System.out.println("Exception occurred: " + e.toString());
			}
		}
		fIsDump = false;
		notifyAll();
		return fDump;
	}
	
	
	/**
	 * Method ExistsDump.
	 * @return boolean
	 */
	public synchronized boolean ExistsDump() {
		//notifyAll();
		return fIsDump;
	}
	
	
	/**
	 * Method ExistsUserData.
	 * @return boolean
	 */
	public synchronized boolean ExistsUserData() {
		//notifyAll();	// is dat hier nodig?
		return fDataForUser;
	}
}
