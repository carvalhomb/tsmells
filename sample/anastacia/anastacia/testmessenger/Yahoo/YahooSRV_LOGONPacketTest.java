/*
 * Created on Apr 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package testmessenger.Yahoo;

import junit.framework.*;
import messenger.Yahoo.YahooSRV_LOGONPacket;

/**
 * @author bartcardi
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class YahooSRV_LOGONPacketTest extends TestCase {

	/**
	 * Constructor for YahooSRV_LOGONPacketTest.
	 * @param arg0
	 */
	public YahooSRV_LOGONPacketTest(String arg0) {
		super(arg0);
		//System.out.println("HIER!!!!");
	}
	
	public static Test suite() {
			return new TestSuite(YahooSRV_LOGONPacketTest.class);
		}

	public void testGetContact() {
	}

	public void testGetStatus() {
		YahooSRV_LOGONPacket p = new YahooSRV_LOGONPacket("594d5347000000020053000100000000a82eee8630c0806261636172646962617274c08031c0806261636172646962617274c08038c08031c08037c08062656e6e79766132303032c0803130c08030c0803131c08030c0803137c08030c0803133c08031c08000");
		//System.out.println("HIER!!!!");
		assertTrue(p.getStatus().equals("YONLINE"));
	}

	public void testGetKindOfPacket() {
		YahooSRV_LOGONPacket p = new YahooSRV_LOGONPacket("594d5347000000020053000100000000a82eee8630c0806261636172646962617274c08031c0806261636172646962617274c08038c08031c08037c08062656e6e79766132303032c0803130c08030c0803131c08030c0803137c08030c0803133c08031c08000");
	
		assertTrue(p.getKindOfPacket().equals("SRV_LOGON"));
	}

	public void testGetContentHex() {
		YahooSRV_LOGONPacket p = new YahooSRV_LOGONPacket("594d5347000000020053000100000000a82eee8630c0806261636172646962617274c08031c0806261636172646962617274c08038c08031c08037c08062656e6e79766132303032c0803130c08030c0803131c08030c0803137c08030c0803133c08031c08000");
	
		assertTrue(p.getContentHex().equals("594d5347000000020053000100000000a82eee8630c0806261636172646962617274c08031c0806261636172646962617274c08038c08031c08037c08062656e6e79766132303032c0803130c08030c0803131c08030c0803137c08030c0803133c08031c08000"));
	}

	public void testGetLength() {
		YahooSRV_LOGONPacket p = new YahooSRV_LOGONPacket("594d5347000000020053000100000000a82eee8630c0806261636172646962617274c08031c0806261636172646962617274c08038c08031c08037c08062656e6e79766132303032c0803130c08030c0803131c08030c0803137c08030c0803133c08031c08000");
	
		assertTrue(p.getLength() == 103);
	}
	
	public static void main (String[] args) {
			junit.textui.TestRunner.run(suite());
		}

}
