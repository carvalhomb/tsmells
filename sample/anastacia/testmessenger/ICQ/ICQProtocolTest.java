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

package testmessenger.ICQ;

import junit.framework.*;


/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:02:13 $
 * 
 * 
 */
public class ICQProtocolTest extends TestCase {

	/**
	 * Constructor for ICQProtocolTest.
	 * @param arg0
	 */
	public ICQProtocolTest(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(ICQProtocolTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSendPacket() {
	}

	/*
	 * Test for void ICQProtocol(DataBuffer)
	 */
	public void testICQProtocolDataBuffer() {
	}

	/*
	 * Test for void ICQProtocol(String, String)
	 */
	public void testICQProtocolStringString() {
	}

	public void testGetUIN() {
	}

	public void testIsConnected() {
	}

	public void testGetNumberOfContacts() {
	}

	/*
	 * Test for void addContact(ICQContact)
	 */
	public void testAddContactICQContact() {
	}

	/*
	 * Test for void login(String, String, String, int)
	 */
	public void testLoginStringStringStringI() {
	}

	/*
	 * Test for void login(String, int)
	 */
	public void testLoginStringI() {
	}

	public void testSendMessage() {
	}

	public void testChangeStatus() {
	}

	public void testGetContactListFromServer() {
	}

	public void testGetContactList() {
	}

	/*
	 * Test for void addContact(String)
	 */
	public void testAddContactString() {
	}

	public void testLogout() {
	}

	public void testShutdown() {
	}

	public void testAnalyzePacket() {
	}

	public void testConnect() {
	}

	public void testDisconnect() {
	}

	public void testGetServer() {
	}
	
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
