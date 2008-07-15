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
import messenger.MSN.MSNIncomingNLNPacket;

/**
 * @author benny
 */
public class MSNIncomingNLNPacketTest extends TestCase {
	private String fReceivedPacket = null;
	private MSNIncomingNLNPacket fNLNPacket = null;	
	
	public MSNIncomingNLNPacketTest() {

	}
	
	public void testContent() {
		String content = fNLNPacket.getContent();
		assertTrue(content.equals(fReceivedPacket));		
	}
	
	public void testStatus() {
		String status = fNLNPacket.getStatus();
		assertTrue(status.equals("NLN"));
		
		String awy = "NLN AWY someone@somewhere.com nick";
		MSNIncomingNLNPacket awypacket = new MSNIncomingNLNPacket(awy);
		status = awypacket.getStatus();
		assertTrue(status.equals("AWY"));
				
		String idl = "NLN IDL someone@somewhere.com nick";
		MSNIncomingNLNPacket idlpacket = new MSNIncomingNLNPacket(idl);
		status = idlpacket.getStatus();
		assertTrue(status.equals("IDL"));
				
		String bsy = "NLN BSY someone@somewhere.com nick";
		MSNIncomingNLNPacket bsypacket = new MSNIncomingNLNPacket(bsy);
		status = bsypacket.getStatus();
		assertTrue(status.equals("BSY"));
				
		String brb = "NLN BRB someone@somewhere.com nick";
		MSNIncomingNLNPacket brbpacket = new MSNIncomingNLNPacket(brb);
		status = brbpacket.getStatus();
		assertTrue(status.equals("BRB"));
		
		String phn = "NLN PHN someone@somewhere.com nick";
		MSNIncomingNLNPacket phnpacket = new MSNIncomingNLNPacket(phn);
		status = phnpacket.getStatus();
		assertTrue(status.equals("PHN"));
			
		String lun = "NLN LUN someone@somewhere.com nick";
		MSNIncomingNLNPacket lunpacket = new MSNIncomingNLNPacket(lun);
		status = lunpacket.getStatus();
		assertTrue(status.equals("LUN"));
	}
	
	public void testContact() {
		String status = fNLNPacket.getStatus();
		assertTrue(status.equals("NLN"));
	}
	
	public void testNick() {
		String nick = fNLNPacket.getContactNick();
		assertTrue(nick.equals("nick"));
	}
	
	public static Test suite() {
		return new TestSuite(MSNIncomingNLNPacketTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		fReceivedPacket = "NLN NLN someone@somewhere.com nick";
		fNLNPacket = new MSNIncomingNLNPacket(fReceivedPacket);
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
