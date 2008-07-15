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

import java.net.Socket;
import java.io.*;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:04:01 $
 * 
 * TimeOutSocket uses the standard Java Socket 
 * and adds the feature to try connecting for an certain
 * amount of time before giving up. 
 * The standard Java Socket has no such changable
 * parameter but waits for a system dependent amount
 * of time (can be up to 3 minutes!!)
 */
public class TimeOutSocket {
	private Socket s;
	private Timer t;
	private String fServer = "";
	private int fPort = 0;
	private boolean running = false;
	
	public TimeOutSocket(String server, int port) throws TimeOutException {
		fServer = server;
		fPort = port;
		
		t = new Timer(10000);

 		while (!t.isTimedOut() && !isConnected()) {
 			//System.out.println("in de lus");
 			if(running) {
 				//System.out.println("waiting for connection ...");
 			}
 			else {
				try {
					//System.out.println("creating new socket");
					s = new Socket(fServer, fPort);
					//System.out.println("new socket created");
				}
				catch(IllegalArgumentException e) {
					System.out.println(e.getMessage());	
				}
		catch(IOException e) {		
			System.out.println("IOException");
		}
 				running = true;

 			}
 			
 			if(t == null) {
 				System.out.println("t is null!");
 			}
 			else if(this == null) {
 				System.out.println("dit is null??");
 			}
 			//System.out.println("voor yield");
     		//Thread.currentThread().yield();
     		//System.out.println("na yield");
		}
		//System.out.println("na de lus");
		if(t.isTimedOut()) {
			if(t == null) {
 				System.out.println("t is null!");
 			}
			//System.out.println("timeout!");
			t.interrupt();
		
			if(t == null) {
 				System.out.println("t is null!");
 			}
 			//System.out.println("kga throwen");
			throw new TimeOutException();
		}
		else {
			//System.out.println("tis gelukt!");
			t.interrupt();	
		}
	}
	
	private boolean running() {
		return running;	
	}
	
	public void close() {
		try {
			s.close();
		}
		catch(IOException e) {
			
		}
	}
	
	public Socket getServer() {
		assert s != null;
		return s;
	}
	
	public boolean isConnected() {
		if(s == null) {
			return false;
		}
		else {
			return s.isConnected();
		}
	}
}

