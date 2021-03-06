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
import messenger.ICQ.ICQCLI_IDENTPacket;


import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:02:04 $
 * 
 * 
 */
public class ICQCLI_IDENTPacketTest extends TestCase {
	/**
	 * Constructor for ICQCLI_IDENTPacketTest.
	 * @param arg0
	 */
	public ICQCLI_IDENTPacketTest(String arg0) {
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
		return new TestSuite(ICQCLI_IDENTPacketTest.class);
	}

	public void testICQCLI_IDENTPacket() {
		Vector v = new Vector();
		v.addElement(Utils.UIN("174075367"));
     		v.addElement(Utils.ICQEncryptPass("blahblah"));
     		v.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
		ICQCLI_IDENTPacket p = new ICQCLI_IDENTPacket(v);
		
		Vector w = new Vector();
		w.addElement(Utils.UIN("268912722"));
     		w.addElement(Utils.ICQEncryptPass("28032803"));
     		w.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
		ICQCLI_IDENTPacket q = new ICQCLI_IDENTPacket(w);
	}

	public void testGetKindOfPacket() {
		Vector v = new Vector();
		v.addElement(Utils.UIN("174075367"));
     		v.addElement(Utils.ICQEncryptPass("blahblah"));
     		v.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
		ICQCLI_IDENTPacket p = new ICQCLI_IDENTPacket(v);
		
		assertTrue(p.getKindOfPacket().equals("CLI_IDENT"));
		
		Vector w = new Vector();
		w.addElement(Utils.UIN("268912722"));
     	w.addElement(Utils.ICQEncryptPass("28032803"));
     	w.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
		ICQCLI_IDENTPacket q = new ICQCLI_IDENTPacket(w);
		assertTrue(q.getKindOfPacket().equals("CLI_IDENT"));
	}

	public void testGetContentHex() {
		Vector v = new Vector();
		v.addElement(Utils.UIN("174075367"));
     		v.addElement(Utils.ICQEncryptPass("blahblah"));
     		v.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
		ICQCLI_IDENTPacket p = new ICQCLI_IDENTPacket(v);
		
		assertTrue(p.getContentHex().substring(8).equals("0086000000010001000931373430373533363700020008914ae0ac5beabafa0003003349435120496e632e202d2050726f64756374206f66204943512028544d292e32303031622e352e31372e312e333634322e383500160002010a001700020005001800020011001900020001001a00020e3a0014000400000055000e00027573000f0002656e"));
	
		Vector w = new Vector();
		w.addElement(Utils.UIN("268912722"));
     	w.addElement(Utils.ICQEncryptPass("28032803"));
     	w.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
		ICQCLI_IDENTPacket q = new ICQCLI_IDENTPacket(w);
		assertTrue(q.getContentHex().substring(8).equals("0086000000010001000932363839313237323200020008c11eb1f70bbeeba10003003349435120496e632e202d2050726f64756374206f66204943512028544d292e32303031622e352e31372e312e333634322e383500160002010a001700020005001800020011001900020001001a00020e3a0014000400000055000e00027573000f0002656e"));
	}
	
	public void testGetLength() {
		Vector v = new Vector();
		v.addElement(Utils.UIN("174075367"));
     		v.addElement(Utils.ICQEncryptPass("blahblah"));
     		v.addElement("49435120496E632E202D2050726F6475"
								+ "6374206F66204943512028544D292E32"
								+ "303031622E352E31372E312E33363432"
								+ "2E3835");
		ICQCLI_IDENTPacket p = new ICQCLI_IDENTPacket(v);
		
		assertTrue(p.getLength() == 140);
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
