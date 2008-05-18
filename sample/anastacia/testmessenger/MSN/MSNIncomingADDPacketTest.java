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

package testmessenger.MSN;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import messenger.MSN.MSNIncomingADDPacket;

/**
 * @author benny
 */
public class MSNIncomingADDPacketTest extends TestCase {
	private String fReceivedPacket = null;
	private MSNIncomingADDPacket fPacket = null;

	public MSNIncomingADDPacketTest() {
		fReceivedPacket = "ADD 10 AL 101 someone@somewhere.com someone@somewhere.com";
	}
	
	public void testCmd() {
		assertTrue(fPacket.getCommand().equals("ADD"));
	}
	
	public void testContent() {
		assertTrue(fPacket.getContent().equals(fReceivedPacket));
	}
	
	public static Test suite() {
		return new TestSuite(MSNIncomingADDPacketTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fPacket = new MSNIncomingADDPacket(fReceivedPacket);
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
