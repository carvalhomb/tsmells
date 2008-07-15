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
import java.util.Vector;
import messenger.ICQ.ICQCLI_REQICBMPacket;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:02:12 $
 * 
 * 
 */
public class ICQCLI_REQICBMPacketTest extends TestCase {

	/**
	 * Constructor for ICQCLI_REQICBMPacketTest.
	 * @param arg0
	 */
	public ICQCLI_REQICBMPacketTest(String arg0) {
		super(arg0);
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
	
	public static Test suite() {
		return new TestSuite(ICQCLI_REQICBMPacketTest.class);
	}

	public void testICQCLI_REQICBMPacket() {
		Vector v = new Vector();
		
		ICQCLI_REQICBMPacket p = new ICQCLI_REQICBMPacket(v);	
	}

	public void testGetKindOfPacket() {
		Vector v = new Vector();
		
		ICQCLI_REQICBMPacket p = new ICQCLI_REQICBMPacket(v);	
		assertTrue(p.getKindOfPacket().equals("CLI_REQICBM"));
	}

	public void testGetContentHex() {
		Vector v = new Vector();
		
		ICQCLI_REQICBMPacket p = new ICQCLI_REQICBMPacket(v);	
		
		assertTrue(p.getContentHex().substring(8).equals("000a00040004000000000008"));
	}

	public void testGetLength() {
		Vector v = new Vector();
		
		ICQCLI_REQICBMPacket p = new ICQCLI_REQICBMPacket(v);	
		assertTrue(p.getLength() == 16);
	}
	
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
