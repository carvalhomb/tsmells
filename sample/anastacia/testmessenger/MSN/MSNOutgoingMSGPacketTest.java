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
import messenger.MSN.MSNOutgoingMSGPacket;
import java.util.Vector;

/**
 * @author benny
 */
public class MSNOutgoingMSGPacketTest extends TestCase {
	private MSNOutgoingMSGPacket fMSGPacket;
	
	public MSNOutgoingMSGPacketTest() {

	}
	
	public void testContent() {
		String expectedContent = "MSG 3 U 130\r\n"
											+ "MIME-Version: 1.0\r\n"
											+ "Content-Type: text/plain; charset=UTF-8\r\n"
											+ "X-MMS-IM-Format: FN=ArialEF=; CO=0; CS=0; PF=22\r\n"
											+ "\r\n"
											+ "Hello! How are you?";

		assertTrue(fMSGPacket.getContent().equals(expectedContent));
	}
	
	public static Test suite() {
		return new TestSuite(MSNOutgoingMSGPacketTest.class);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		Vector args = new Vector();
		args.add(Long.valueOf("3"));
		args.add("Hello! How are you?");
		fMSGPacket = new MSNOutgoingMSGPacket(args);
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
