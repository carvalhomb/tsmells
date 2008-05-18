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
import messenger.MSN.MSNIncomingRNGPacket;

/**
 * @author benny
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSNIncomingRNGPacketTest extends TestCase {
	private String fReceivedPacket = null;
	private MSNIncomingRNGPacket fRNGPacket = null;
	
	public MSNIncomingRNGPacketTest() {

	}
	
	public void testContent() {
		String content = fRNGPacket.getContent();
		assertTrue(content.equals(fReceivedPacket));
	}
	
	public void testSessionID() {
		String sessionID = fRNGPacket.getSessionID();
		assertTrue(sessionID.equals("11752099"));
	}
	
	public void testSwitchBoardIP() {
		String sbIP = fRNGPacket.getSwitchBoardIp();
		assertTrue(! sbIP.equals("64.4.12.193:1863"));
		assertTrue(sbIP.equals("64.4.12.193"));
	}
	
	public void testCKI() {
		String cki = fRNGPacket.getCKI();
		assertTrue(cki.equals("849102291.520491932"));
	}
	
	public void testContact() {
		String contact = fRNGPacket.getContact();
		assertTrue(contact.equals("someone@somewhere.com"));
	}
	
	public void testNick() {
		String nick = fRNGPacket.getContactNick();
		assertTrue(nick.equals("nick"));
	}
	
	public static Test suite() {
		return new TestSuite(MSNIncomingRNGPacketTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		fReceivedPacket = "RNG 11752099 64.4.12.193:1863 CKI 849102291.520491932 someone@somewhere.com nick";
		fRNGPacket = new MSNIncomingRNGPacket(fReceivedPacket);
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
