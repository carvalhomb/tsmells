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

package testmessenger.Yahoo;

import messenger.Yahoo.YahooSRV_AUTHPacket;
import junit.framework.*;


/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:02:17 $
 * 
 * 
 */
public class YahooSRV_AUTHPacketTest extends TestCase {

	/**
	 * Constructor for YahooSRV_AUTHPacketTest.
	 * @param arg0
	 */
	public YahooSRV_AUTHPacketTest(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(YahooSRV_AUTHPacketTest.class);
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

	public void testYahooSRV_AUTHPacket() {
		YahooSRV_AUTHPacket srv_auth = new YahooSRV_AUTHPacket("594d5347000000000035005700000001ce3979d431c08062656e6e79766132303032c0803934c0803457384669627662706b5934576873414477665338772d2dc0803133c08030c080");
	}

	public void testGetKindOfPacket() {
		YahooSRV_AUTHPacket srv_auth = new YahooSRV_AUTHPacket("594d5347000000000035005700000001ce3979d431c08062656e6e79766132303032c0803934c0803457384669627662706b5934576873414477665338772d2dc0803133c08030c080");
		assertTrue(srv_auth.getKindOfPacket().equals("SRV_AUTH"));
	}
	
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
