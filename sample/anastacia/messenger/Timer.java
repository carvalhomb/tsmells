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
 * @version $Revision: 1.3 $
 * @date $Date: 2003/04/22 18:40:12 $
 * 
 * a Timer waits for an amount of time set by
 * the user. It can be interrupted by the user, or
 * when it keeps running untill the end, it will set a
 * boolean
 */
public class Timer implements Runnable {
	 long delay;
     boolean timedOut = false;
     Thread t;

     public Timer(long delay) {
     		this.delay = delay;
             t  = new Thread(this);
             t.start();
         }

	public void interrupt() {
   		t.interrupt();	
	}


    public synchronized boolean isTimedOut() {
         return timedOut;
     }

     private synchronized void setTimedOut() {
         timedOut = true;
     }

     public void run() {
         try {
         	//System.out.println("going to sleep");
             Thread.currentThread().sleep(delay);
             //Thread.sleep(delay);
              //System.out.println("end of sleep");
         } 
         catch (InterruptedException ie) {}
         setTimedOut();
     }
}
