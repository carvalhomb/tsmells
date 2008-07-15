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
import messenger.ICQ.ICQSRV_COOKIEPacket;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:02:04 $
 * 
 * 
 */
public class ICQSRV_COOKIEPacketTest extends TestCase {
	private String fCookiePacketHexString = "2a04b17e012600010009313734303" +
		"735333637000500113230352e3138382e312e38313a35313930000601002312" +
		"fd05c4d87b7d4a3fc9a8dc7c57dcdbb250b7d7a28267dbf66717c5284c4da1294" +
		"5ed5f4120aff343f5605731dc6128f5e99f54a9dc9dea863329d5e84bd871987652" +
		"a6a484cca587d88a157d0531eadb163deeab9aa4607f8a85aa50703a1b4492b11" +
		"9b78ba3ccebcafb29e62caab57e7370645feacd7a3bacd2aa90893bd0089c3936c" +
		"72e6ecc1d90497ea978a909b7e8452ce2c4bc2237924781312d5b9df80889f3ba07" +
		"a757df1e31baee77e1fcb81a8d90cee11e69f0b2b2ebb1c45f2956f91b405a9b5f2c92" +
		"c9f501431fb72d449a00b3ba23dabb13914e0fe51a8444dde1868fc32efae74db62925" +
		"5014fd390e606054105d4ae6c2432248b84a76";
	
	/**
	 * Constructor for ICQSRV_COOKIEPacketTest.
	 * @param arg0
	 */
	public ICQSRV_COOKIEPacketTest(String arg0) {
		super(arg0);
	}
	
	public void testICQSRV_FAMILIES2Packet() {
		String cookie = "2312fd05c4d87b7d4a3fc9a8dc7c57dcd" +
			"bb250b7d7a28267dbf66717c5284c4da12945ed5f4120" +
			"aff343f5605731dc6128f5e99f54a9dc9dea863329d5e84b" +
			"d871987652a6a484cca587d88a157d0531eadb163deea" +
			"b9aa4607f8a85aa50703a1b4492b119b78ba3ccebcafb29" +
			"e62caab57e7370645feacd7a3bacd2aa90893bd0089c393" +
			"6c72e6ecc1d90497ea978a909b7e8452ce2c4bc22379247" +
			"81312d5b9df80889f3ba07a757df1e31baee77e1fcb81a8d90" +
			"cee11e69f0b2b2ebb1c45f2956f91b405a9b5f2c92c9f501431" +
			"fb72d449a00b3ba23dabb13914e0fe51a8444dde1868fc32ef" +
			"ae74db629255014fd390e606054105d4ae6c2432248b84a76";
		
		ICQSRV_COOKIEPacket p = new ICQSRV_COOKIEPacket(fCookiePacketHexString);
		
		assertTrue(p.getCookie().equals(cookie));
		assertTrue(p.getIp().equals("205.188.1.81"));
		assertTrue(p.getPort() == 5190);
	}
	
	public static Test suite() {
		return new TestSuite(ICQSRV_COOKIEPacketTest.class);
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
	
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
