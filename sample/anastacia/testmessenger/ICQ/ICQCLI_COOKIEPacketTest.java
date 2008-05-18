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
import messenger.ICQ.ICQCLI_COOKIEPacket;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:02:14 $
 * 
 * 
 */
public class ICQCLI_COOKIEPacketTest extends TestCase {

	/**
	 * Constructor for ICQCLI_COOKIEPacketTest.
	 * @param arg0
	 */
	public ICQCLI_COOKIEPacketTest(String arg0) {
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
		return new TestSuite(ICQCLI_COOKIEPacketTest.class);
	}

	public void testICQCLI_COOKIEPacket() {
		Vector v = new Vector();
		
		v.addElement("38acbc02ebe9baa45b9358ef7244a9ce01344c40a6523b8e060dd2d8fc93013c3b9e18f65b9a2b5076086f90a375a7e4e0fab0a259e779a816964c9dec33f572400c423952435334e29ef391617455d907f6b687a274f79180a9606c2055fe1f5e2112a64e6483e7c29ed7c514268a42b1bd334a6efda39a243a55c0e9d576345ffbd18c836fa95c164a2959e56e0d49e88be78f3544da5138668509641fc646af407d972510e7fbc07c7aa4f45e80b0042db0a79b320f761c38ff16083d9b96efe4847d80a2e016757f4c65ef34673c3fbda0b98cf82a5d9f707bac908e171196e38c564adb23006888bb738f992031c69a5cfe31c4edb0d10ca2fc55cb9bd8");
		ICQCLI_COOKIEPacket p = new ICQCLI_COOKIEPacket(v);
	}

	public void testGetKindOfPacket() {
		Vector v = new Vector();
		
		v.addElement("38acbc02ebe9baa45b9358ef7244a9ce01344c40a6523b8e060dd2d8fc93013c3b9e18f65b9a2b5076086f90a375a7e4e0fab0a259e779a816964c9dec33f572400c423952435334e29ef391617455d907f6b687a274f79180a9606c2055fe1f5e2112a64e6483e7c29ed7c514268a42b1bd334a6efda39a243a55c0e9d576345ffbd18c836fa95c164a2959e56e0d49e88be78f3544da5138668509641fc646af407d972510e7fbc07c7aa4f45e80b0042db0a79b320f761c38ff16083d9b96efe4847d80a2e016757f4c65ef34673c3fbda0b98cf82a5d9f707bac908e171196e38c564adb23006888bb738f992031c69a5cfe31c4edb0d10ca2fc55cb9bd8");
	
		ICQCLI_COOKIEPacket p = new ICQCLI_COOKIEPacket(v);
		assertTrue(p.getKindOfPacket().equals("CLI_COOKIE"));
	}

	public void testGetContentHex() {
		Vector v = new Vector();
		
		v.addElement("38acbc02ebe9baa45b9358ef7244a9ce01344c40a6523b8e060dd2d8fc93013c3b9e18f65b9a2b5076086f90a375a7e4e0fab0a259e779a816964c9dec33f572400c423952435334e29ef391617455d907f6b687a274f79180a9606c2055fe1f5e2112a64e6483e7c29ed7c514268a42b1bd334a6efda39a243a55c0e9d576345ffbd18c836fa95c164a2959e56e0d49e88be78f3544da5138668509641fc646af407d972510e7fbc07c7aa4f45e80b0042db0a79b320f761c38ff16083d9b96efe4847d80a2e016757f4c65ef34673c3fbda0b98cf82a5d9f707bac908e171196e38c564adb23006888bb738f992031c69a5cfe31c4edb0d10ca2fc55cb9bd8");
	
		ICQCLI_COOKIEPacket p = new ICQCLI_COOKIEPacket(v);
		assertTrue(p.getContentHex().substring(8).equals("0108000000010006010038acbc02ebe9baa45b9358ef7244a9ce01344c40a6523b8e060dd2d8fc93013c3b9e18f65b9a2b5076086f90a375a7e4e0fab0a259e779a816964c9dec33f572400c423952435334e29ef391617455d907f6b687a274f79180a9606c2055fe1f5e2112a64e6483e7c29ed7c514268a42b1bd334a6efda39a243a55c0e9d576345ffbd18c836fa95c164a2959e56e0d49e88be78f3544da5138668509641fc646af407d972510e7fbc07c7aa4f45e80b0042db0a79b320f761c38ff16083d9b96efe4847d80a2e016757f4c65ef34673c3fbda0b98cf82a5d9f707bac908e171196e38c564adb23006888bb738f992031c69a5cfe31c4edb0d10ca2fc55cb9bd8"));
	
	}

	public void testGetLength() {
		Vector v = new Vector();
		
		v.addElement("38acbc02ebe9baa45b9358ef7244a9ce01344c40a6523b8e060dd2d8fc93013c3b9e18f65b9a2b5076086f90a375a7e4e0fab0a259e779a816964c9dec33f572400c423952435334e29ef391617455d907f6b687a274f79180a9606c2055fe1f5e2112a64e6483e7c29ed7c514268a42b1bd334a6efda39a243a55c0e9d576345ffbd18c836fa95c164a2959e56e0d49e88be78f3544da5138668509641fc646af407d972510e7fbc07c7aa4f45e80b0042db0a79b320f761c38ff16083d9b96efe4847d80a2e016757f4c65ef34673c3fbda0b98cf82a5d9f707bac908e171196e38c564adb23006888bb738f992031c69a5cfe31c4edb0d10ca2fc55cb9bd8");
	
		ICQCLI_COOKIEPacket p = new ICQCLI_COOKIEPacket(v);
		
		assertTrue(p.getLength() == 270);
	}
	
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
