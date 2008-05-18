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
import messenger.MSN.MSNIncomingMSGPacket;

/**
 * @author benny
 */
public class MSNIncomingMSGPacketTest extends TestCase {
	private String fReceivedPacket = null;
	private MSNIncomingMSGPacket fMSGPacket = null;	
	
	public MSNIncomingMSGPacketTest() {

	}
	
	public void testContent() {
		String content = fMSGPacket.getContent();
		assertTrue(content.equals(fReceivedPacket));
	}
	
	public void testContact() {
		assertTrue(fMSGPacket.getContact().equals("Hotmail"));
	}
	
	public void testContactNick() {
		assertTrue(fMSGPacket.getContactNick().equals("Hotmail"));
	}
	
	public void getNrOfUnreadMsgs() {
		assertTrue(fMSGPacket.getNrOfUnreadMsgs().equals("73"));
	}
	
	public void testType() {
		assertTrue(fMSGPacket.getType().equals("initialemailnotification"));
	}
	
	public static Test suite() {
		return new TestSuite(MSNIncomingMSGPacketTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
			
		fReceivedPacket = "MSG Hotmail Hotmail 223" +
		" MIME-Version:" +
		" 1.0 Content-Type: text/x-msmsgsinitialemailnotification; charset=UTF-8 " +
		"Inbox-Unread: 73 " +
		"Folders-Unread: 28 " +
		"Inbox-URL: /cgi-bin/HoTMaiL " +
		"Folders-URL: /cgi-bin/folders " +
		"Post-URL: http://www.hotmail.com";

		fMSGPacket = new MSNIncomingMSGPacket(fReceivedPacket);
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
