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
import messenger.MSN.*;
import messenger.UnknownPacketException;
import messenger.IncomingNullPacketException;
import messenger.BadArgumentException;
import messenger.IPacket;
import java.util.Vector;

/**
 * @author benny
 */
public class MSNPacketFactoryTest extends TestCase {
	private String fReceivedPacket = null;
	private MSNPacketFactory fFactory = null;
	
	public MSNPacketFactoryTest() {

	}
	
	public void testOutgoing() {
		try {
			Vector args = new Vector();
			args.add(Long.valueOf("7"));
			args.add("NLN");
			
			MSNOutgoingCHGPacket packet1 = new MSNOutgoingCHGPacket(args);
			IPacket ipacket = fFactory.createOutgoingPacket("MSN_CHG", args);
			
			assertTrue(ipacket instanceof MSNOutgoingCHGPacket);
			
			MSNOutgoingCHGPacket packet2 = (MSNOutgoingCHGPacket)ipacket;
				
			assertTrue(packet1.getContent().equals(packet2.getContent()));
		} catch (BadArgumentException e) {
			assertTrue(false);
		} catch (UnknownPacketException e) {
			assertTrue(false);
		}
	}
	
	public void testIncoming() {
		try {
			fReceivedPacket = "CHL 0 20881396011366812350";
			MSNIncomingCHLPacket packet1 = new MSNIncomingCHLPacket(fReceivedPacket);
			IPacket ipacket = fFactory.createIncomingPacket(fReceivedPacket);
			
			assertTrue(ipacket instanceof MSNIncomingCHLPacket);
			
			MSNIncomingCHLPacket packet2 = (MSNIncomingCHLPacket)ipacket;
			
			assertTrue(packet1.getContent().equals(packet2.getContent()));
		} catch (UnknownPacketException e) {
			assertTrue(false);
		} catch (IncomingNullPacketException e) {
			assertTrue(false);
		}
	}
	
	public void testIncomingErrorCode() {
		try {
			fReceivedPacket = "911 Authentication failed";
			fFactory.createIncomingPacket(fReceivedPacket);
		
			assertTrue(false);
		} catch (UnknownPacketException e) {
			assertTrue(e.getMessage().equals("Authentication failed"));
		} catch (IncomingNullPacketException e) {
			assertTrue(false);
		}
	}
	
	public void testUnknownPacketException() {
		try {
			fReceivedPacket = "CHZ 0 20881396011366812350";
			fFactory.createIncomingPacket(fReceivedPacket);
			
			assertTrue(false);
		} catch (UnknownPacketException e) {
			assertTrue(true);
		} catch (IncomingNullPacketException e) {
			assertTrue(false);
		}
	}
	
	public void testIncomingNullPacketException() {
		try {
			fReceivedPacket = null;
			fFactory.createIncomingPacket(fReceivedPacket);
			
			assertTrue(false);
		} catch (UnknownPacketException e) {
			assertTrue(false);
		} catch (IncomingNullPacketException e) {
			assertTrue(true);
		}
	}
	
	
	public static Test suite() {
		return new TestSuite(MSNPacketFactoryTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		fFactory = new MSNPacketFactory();
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
