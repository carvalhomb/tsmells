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

import junit.framework.*;
import messenger.Yahoo.YahooSRV_MESSAGEPacket;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/25 22:29:23 $
 * 
 * 
 */
public class YahooSRV_MESSAGEPacketTest extends TestCase {

	/**
	 * Constructor for YahooSRV_MESSAGEPacketTest.
	 * @param arg0
	 */
	public YahooSRV_MESSAGEPacketTest(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(YahooSRV_MESSAGEPacketTest.class);
	}

	public void testYahooSRV_MESSAGEPacket() {
		YahooSRV_MESSAGEPacket p = new YahooSRV_MESSAGEPacket("594d5347000000000067000600000005623909103331c08036c0803332c08036c08035c0806261636172646962617274c08034c08062656e6e79766132303032c0803135c08031303531303237313536c0803134c0803c666f6e7420666163653d2268656c766574696361222073697a653d223130223e3a44c080");
	
		p = new YahooSRV_MESSAGEPacket("594d5347000000000024000600000004cf235e2535c08062656e6e79766132303032c0803130c0803939390231303531303239343339c080");
	}

	public void testGetLogin() {
		YahooSRV_MESSAGEPacket p = new YahooSRV_MESSAGEPacket("594d5347000000000067000600000005623909103331c08036c0803332c08036c08035c0806261636172646962617274c08034c08062656e6e79766132303032c0803135c08031303531303237313536c0803134c0803c666f6e7420666163653d2268656c766574696361222073697a653d223130223e3a44c080");
		
		assertTrue(p.getLogin().equals("bennyva2002"));
		
		p = new YahooSRV_MESSAGEPacket("594d5347000000000024000600000004cf235e2535c08062656e6e79766132303032c0803130c0803939390231303531303239343339c080");
	
		assertTrue(p.getLogin().equals(""));
	}

	public void testGetMessage() {
		YahooSRV_MESSAGEPacket p = new YahooSRV_MESSAGEPacket("594d5347000000000067000600000005623909103331c08036c0803332c08036c08035c0806261636172646962617274c08034c08062656e6e79766132303032c0803135c08031303531303237313536c0803134c0803c666f6e7420666163653d2268656c766574696361222073697a653d223130223e3a44c080");
	
		assertTrue(p.getMessage().equals(":D"));
		
		p = new YahooSRV_MESSAGEPacket("594d5347000000000024000600000004cf235e2535c08062656e6e79766132303032c0803130c0803939390231303531303239343339c080");
	
		assertTrue(p.getMessage().equals(""));
	}

	public void testGetKindOfPacket() {
		YahooSRV_MESSAGEPacket p = new YahooSRV_MESSAGEPacket("594d5347000000000067000600000005623909103331c08036c0803332c08036c08035c0806261636172646962617274c08034c08062656e6e79766132303032c0803135c08031303531303237313536c0803134c0803c666f6e7420666163653d2268656c766574696361222073697a653d223130223e3a44c080");
	
		assertTrue(p.getKindOfPacket().equals("SRV_MESSAGE"));
		
		p = new YahooSRV_MESSAGEPacket("594d5347000000000024000600000004cf235e2535c08062656e6e79766132303032c0803130c0803939390231303531303239343339c080");
	
		assertTrue(p.getKindOfPacket().equals("SRV_MESSAGE"));
	}

	public void testGetContentHex() {
		YahooSRV_MESSAGEPacket p = new YahooSRV_MESSAGEPacket("594d5347000000000067000600000005623909103331c08036c0803332c08036c08035c0806261636172646962617274c08034c08062656e6e79766132303032c0803135c08031303531303237313536c0803134c0803c666f6e7420666163653d2268656c766574696361222073697a653d223130223e3a44c080");
	
		assertTrue(p.getContentHex().equals("594d5347000000000067000600000005623909103331c08036c0803332c08036c08035c0806261636172646962617274c08034c08062656e6e79766132303032c0803135c08031303531303237313536c0803134c0803c666f6e7420666163653d2268656c766574696361222073697a653d223130223e3a44c080"));
	
		p = new YahooSRV_MESSAGEPacket("594d5347000000000024000600000004cf235e2535c08062656e6e79766132303032c0803130c0803939390231303531303239343339c080");
	
		assertTrue(p.getContentHex().equals("594d5347000000000024000600000004cf235e2535c08062656e6e79766132303032c0803130c0803939390231303531303239343339c080"));
	}
	
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
