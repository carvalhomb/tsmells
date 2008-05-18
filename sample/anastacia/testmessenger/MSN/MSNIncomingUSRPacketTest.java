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
import messenger.MSN.MSNIncomingUSRPacket;

/**
 * @author benny
 */
public class MSNIncomingUSRPacketTest extends TestCase {
	private String fReceivedPacket = null;
	private MSNIncomingUSRPacket fUSRPacket = null;

	public MSNIncomingUSRPacketTest() {

	}
	
	public void testContent() {
		String content = fUSRPacket.getContent();
		assertTrue(content.equals(fReceivedPacket));
	}
	
	public void testEncryption() {
		String enc = fUSRPacket.getAuth();
		assertTrue(enc.equals("MD5"));
		
		String hash = fUSRPacket.getHash();
		assertTrue(hash.equals("1013928519.63957190"));
	}
	
	public static Test suite() {
		return new TestSuite(MSNIncomingUSRPacketTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		fReceivedPacket = "USR 5 MD5 S 1013928519.63957190";
		fUSRPacket = new MSNIncomingUSRPacket(fReceivedPacket);
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
