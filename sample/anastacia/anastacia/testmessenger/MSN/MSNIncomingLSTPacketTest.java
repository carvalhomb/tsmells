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
import messenger.MSN.MSNIncomingLSTPacket;

/**
 * @author benny
 */
public class MSNIncomingLSTPacketTest extends TestCase {
	private String fReceivedPacket = null;
	private MSNIncomingLSTPacket fLSTPacket = null;	
	
	public MSNIncomingLSTPacketTest() {

	}
	
	public void testContent() {
		String content = fLSTPacket.getContent();
		assertTrue(content.equals(fReceivedPacket));
	}
	
	public void testContact() {
		String contact = fLSTPacket.getContact();
		assertTrue(contact.equals("someone@somewhere.com"));
	}
	
	public void testContactNick() {
		String nick = fLSTPacket.getContactNick();
		assertTrue(nick.equals("nick"));
	}
		
	public void testNrOfContacts() {
		int contacts = fLSTPacket.getNrOfContacts();
		assertTrue(contacts == 1);
	}
		
	public void testTotalNrOfContacts() {
		int totalContacts = fLSTPacket.getTotalNrOfContacts();
		assertTrue(totalContacts == 3);
	}
	
	public static Test suite() {
		return new TestSuite(MSNIncomingLSTPacketTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		fReceivedPacket = "LST 10 FL 21 1 3 someone@somewhere.com nick";
		fLSTPacket = new MSNIncomingLSTPacket(fReceivedPacket);
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
