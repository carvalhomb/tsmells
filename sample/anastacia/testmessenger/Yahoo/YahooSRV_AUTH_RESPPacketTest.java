/*
 * Created on Apr 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package testmessenger.Yahoo;

import junit.framework.*;
import messenger.Yahoo.YahooSRV_AUTH_RESPPacket;

/**
 * @author bartcardi
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class YahooSRV_AUTH_RESPPacketTest extends TestCase {

	/**
	 * Constructor for YahooSRV_AUTH_RESPPacketTest.
	 * @param arg0
	 */
	public YahooSRV_AUTH_RESPPacketTest(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(YahooSRV_AUTH_RESPPacketTest.class);
	}

	public void testGetKindOfPacket() {
		YahooSRV_AUTH_RESPPacket p = new YahooSRV_AUTH_RESPPacket("594d53470000000000240006000000047d2d466435c08062656e6e79766132303032c0803130c0803939390231303531303232373837c080");
	
		assertTrue(p.getKindOfPacket().equals("SRV_AUTH_RESP"));
	}

	public void testGetContentHex() {
		YahooSRV_AUTH_RESPPacket p = new YahooSRV_AUTH_RESPPacket("594d53470000000000240006000000047d2d466435c08062656e6e79766132303032c0803130c0803939390231303531303232373837c080");
	
		assertTrue(p.getContentHex().equals("594d53470000000000240006000000047d2d466435c08062656e6e79766132303032c0803130c0803939390231303531303232373837c080"));
	}

	public void testGetLength() {
		YahooSRV_AUTH_RESPPacket p = new YahooSRV_AUTH_RESPPacket("594d53470000000000240006000000047d2d466435c08062656e6e79766132303032c0803130c0803939390231303531303232373837c080");
	
		}
		
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
