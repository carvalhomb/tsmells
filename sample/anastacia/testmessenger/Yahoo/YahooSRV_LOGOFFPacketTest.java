/*
 * Created on Apr 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package testmessenger.Yahoo;

import junit.framework.*;
import messenger.Yahoo.YahooSRV_LOGOFFPacket;

/**
 * @author bartcardi
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class YahooSRV_LOGOFFPacketTest extends TestCase {

	/**
	 * Constructor for YahooSRV_LOGOFFPacketTest.
	 * @param arg0
	 */
	public YahooSRV_LOGOFFPacketTest(String arg0) {
		super(arg0);
	}
	
	public static Test suite() {
		return new TestSuite(YahooSRV_LOGOFFPacketTest.class);
	}

	public void testYahooSRV_LOGOFFPacket() {
	}

	public void testGetContact() {
		YahooSRV_LOGOFFPacket p = new YahooSRV_LOGOFFPacket("594d5347000000000033000200000001c9355ee937c08062656e6e79766132303032c0803130c08030c0803131c0804246324337434438c0803137c08030c0803133c08030c080");
		assertTrue(p.getContact().equals("bennyva2002"));
	}

	public void testGetStatus() {
		YahooSRV_LOGOFFPacket p = new YahooSRV_LOGOFFPacket("594d5347000000000033000200000001c9355ee937c08062656e6e79766132303032c0803130c08030c0803131c0804246324337434438c0803137c08030c0803133c08030c080");
		assertTrue(p.getStatus().equals("YOFFLINE"));
	}

	public void testGetKindOfPacket() {
		YahooSRV_LOGOFFPacket p = new YahooSRV_LOGOFFPacket("594d5347000000000033000200000001c9355ee937c08062656e6e79766132303032c0803130c08030c0803131c0804246324337434438c0803137c08030c0803133c08030c080");
		assertTrue(p.getKindOfPacket().equals("SRV_LOGOFF"));
	}

	public void testGetContentHex() {
		YahooSRV_LOGOFFPacket p = new YahooSRV_LOGOFFPacket("594d5347000000000033000200000001c9355ee937c08062656e6e79766132303032c0803130c08030c0803131c0804246324337434438c0803137c08030c0803133c08030c080");
		assertTrue(p.getContentHex().equals("594d5347000000000033000200000001c9355ee937c08062656e6e79766132303032c0803130c08030c0803131c0804246324337434438c0803137c08030c0803133c08030c080"));
	}
	
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}

}
