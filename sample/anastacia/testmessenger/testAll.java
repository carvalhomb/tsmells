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

package testmessenger;

import junit.framework.*;
import testmessenger.MSN.*;
import testmessenger.ICQ.*;
import testmessenger.Yahoo.*;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.6 $
 * @date $Date: 2003/05/18 11:01:54 $
 * 
 * 
 * TestSuite that runs all the sample tests
 *
 */
public class testAll {

	public static void main (String[] args) {
		junit.textui.TestRunner.run (suite());
	}
	public static Test suite ( ) {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(UtilsTest.suite());
		suite.addTest(testMSNMessage.suite());
		suite.addTest(ICQCLI_CONTACTTest.suite());
		suite.addTest(ICQProtocolTest.suite());
		suite.addTest(ICQCLI_ACKRATESPacketTest.suite());
		//suite.addTest(ICQCLI_ADDBUDDYPacketTest.suite());
		suite.addTest(ICQCLI_ADDCONTACTPacketTest.suite());
		suite.addTest(ICQCLI_COOKIEPacketTest.suite());
		suite.addTest(ICQCLI_FAMILIESPacketTest.suite());
		suite.addTest(ICQCLI_IDENTPacketTest.suite());
		suite.addTest(ICQCLI_REQINFOPacketTest.suite());
		suite.addTest(ICQCLI_REQLOCATIONPacketTest.suite());
		suite.addTest(ICQCLI_RATESREQUESTPacketTest.suite());
		suite.addTest(ICQCLI_READYPacketTest.suite());
		suite.addTest(ICQCLI_REQBOSPacketTest.suite());
		//suite.addTest(ICQCLI_REQBUDDYPacketTest.suite());
		suite.addTest(ICQCLI_REQICBMPacketTest.suite());
		suite.addTest(ICQCLI_REQINFOPacketTest.suite());
		suite.addTest(ICQCLI_REQLISTSPacketTest.suite());
		suite.addTest(ICQCLI_REQLOCATIONPacketTest.suite());
		suite.addTest(ICQCLI_SENDMSGPacketTest.suite());
		suite.addTest(ICQCLI_SENDMSGV1PacketTest.suite());
		suite.addTest(ICQCLI_SETICBMPacketTest.suite());
		suite.addTest(ICQCLI_SETSTATUSPacketTest.suite());
		
		suite.addTest(ICQSRV_FAMILIES2PacketTest.suite());
		suite.addTest(ICQSRV_FAMILIESPacketTest.suite());
		suite.addTest(ICQSRV_COOKIEPacketTest.suite());
		suite.addTest(ICQSRV_REPLYROSTERPacketTest.suite());
		suite.addTest(ICQSRV_RATESPacketTest.suite());
		
		suite.addTest(MSNContactTest.suite());
		suite.addTest(MSNIncomingADDPacketTest.suite());
		suite.addTest(MSNIncomingCHGPacketTest.suite());
		suite.addTest(MSNIncomingCHLPacketTest.suite());
		suite.addTest(MSNIncomingFLNPacketTest.suite());
		suite.addTest(MSNIncomingILNPacketTest.suite());
		suite.addTest(MSNIncomingINFPacketTest.suite());
		suite.addTest(MSNIncomingLSTPacketTest.suite());
		suite.addTest(MSNIncomingMSGPacketTest.suite());
		suite.addTest(MSNIncomingNLNPacketTest.suite());
		suite.addTest(MSNIncomingPacketTest.suite());
		suite.addTest(MSNIncomingQRYPacketTest.suite());
		suite.addTest(MSNIncomingRNGPacketTest.suite());
		suite.addTest(MSNIncomingUSRPacketTest.suite());
		suite.addTest(MSNIncomingVERPacketTest.suite());
		suite.addTest(MSNIncomingXFRPacketTest.suite());
		suite.addTest(MSNOutgoingADDPacketTest.suite());
		suite.addTest(MSNOutgoingANSPacketTest.suite());
		suite.addTest(MSNOutgoingCALPacketTest.suite());
		suite.addTest(MSNOutgoingCHGPacketTest.suite());
		suite.addTest(MSNOutgoingINFPacketTest.suite());
		suite.addTest(MSNOutgoingLSTPacketTest.suite());
		suite.addTest(MSNOutgoingMSGPacketTest.suite());
		suite.addTest(MSNOutgoingPacketTest.suite());
		suite.addTest(MSNOutgoingQRYPacketTest.suite());
		suite.addTest(MSNOutgoingUSRIPacketTest.suite());
		suite.addTest(MSNOutgoingUSRPacketTest.suite());
		suite.addTest(MSNOutgoingUSRSPacketTest.suite());
		suite.addTest(MSNOutgoingVERPacketTest.suite());
		suite.addTest(MSNOutgoingXFRPacketTest.suite());
		suite.addTest(MSNPacketFactoryTest.suite());
		suite.addTest(MSNPacketTest.suite());
		suite.addTest(MSNSessionTest.suite());
		
		suite.addTest(YahooSRV_AUTHPacketTest.suite());
		suite.addTest(YahooSRV_LISTPacketTest.suite());
		suite.addTest(YahooSRV_LOGONPacketTest.suite());
		suite.addTest(YahooSRV_LOGOFFPacketTest.suite());
		suite.addTest(YahooSRV_AUTH_RESPPacketTest.suite());
		suite.addTest(YahooSRV_MESSAGEPacketTest.suite());
		suite.addTest(YahooContactTest.suite());
		return suite;
	}
}
