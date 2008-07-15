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

package messenger.ICQ;

import java.util.Vector;
import messenger.BadArgumentException;
import messenger.UnknownPacketException;
import messenger.IPacketFactory;
import messenger.IPacket;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:56 $
 * 
 * this is a factory creating ICQ Packets
 * 
 */
public class ICQPacketFactory implements IPacketFactory {
	
	/**
	 * @see messenger.IPacketFactory#createOutgoingPacket(java.lang.String, java.util.Vector)
	 */
	public IPacket createOutgoingPacket(String packet, Vector arg) throws UnknownPacketException, BadArgumentException {
		if(packet.equals("CLI_IDENT")) {
			return new ICQCLI_IDENTPacket(arg);
		}
		else if(packet.equals("CLI_GOODBYE")) {
			return new ICQCLI_GOODBYEPacket(arg);	
		}
		else if(packet.equals("CLI_COOKIE")) {
			return new ICQCLI_COOKIEPacket(arg);
		}
		else if(packet.equals("CLI_FAMILIES")) {
			return new ICQCLI_FAMILIESPacket(arg);
		}
		else if(packet.equals("CLI_RATESREQUEST")) {
			return new ICQCLI_RATESREQUESTPacket(arg);
		}
		else if(packet.equals("CLI_REQINFO")) {
			return new ICQCLI_REQINFOPacket(arg);
		} 
		else if(packet.equals("CLI_REQLOCATION")) {
			return new ICQCLI_REQLOCATIONPacket(arg);
		}
		else if(packet.equals("CLI_REQBUDDY")) {
			return new ICQCLI_REQBUDDYPacket(arg);
		}
		else if(packet.equals("CLI_REQLISTS")) {
			return new ICQCLI_REQLISTSPacket(arg);
		}
		else if(packet.equals("CLI_REQICBM")) {
			return new ICQCLI_REQICBMPacket(arg);
		}
		else if(packet.equals("CLI_REQBOS")) {
			return new ICQCLI_REQBOSPacket(arg);
		} 
		else if(packet.equals("CLI_ACKRATES")) {
			return new ICQCLI_ACKRATESPacket(arg);
		}
		else if(packet.equals("CLI_SETICBM")) {
			return new ICQCLI_SETICBMPacket(arg);
		}
		else if(packet.equals("CLI_SETUSERINFO")) {
			return new ICQCLI_SETUSERINFOPacket(arg);
		}
		else if(packet.equals("CLI_SETSTATUS")) {
			try {
				return new ICQCLI_SETSTATUSPacket(arg);
			}
			catch(BadArgumentException e) {
				throw new BadArgumentException();
			}		
		}
		else if(packet.equals("CLI_REQROSTER")) {
			return new ICQCLI_REQROSTERPacket(arg);
		}
		else if(packet.equals("CLI_READY")) {
			return new ICQCLI_READYPacket(arg);
		}
		else if(packet.equals("CLI_TOICQSRV")) {
			return new ICQCLI_TOICQSERVERPacket(arg);
		}
		else if(packet.equals("CLI_SENDMSG")) {
			return new ICQCLI_SENDMSGPacket(arg);
		}
		else if(packet.equals("CLI_SENDMSGV1")) {
			return new ICQCLI_SENDMSGV1Packet(arg);	
		}
		else if(packet.equals("CLI_ADDBUDDY")) {
			return new ICQCLI_ADDBUDDYPacket(arg);
		}
		else if(packet.equals("CLI_ADDCONTACT")) {
			return new ICQCLI_ADDCONTACTPacket(arg);
		}
		else if(packet.equals("CLI_KEEPALIVE")) {
			return new ICQCLI_KEEPALIVEPacket(arg);	
		}
		else {
	  		 throw new UnknownPacketException();
   		}
	}
	
	/**
	 * @see messenger.IPacketFactory#createIncomingPacket(java.lang.String)
	 */
	public IPacket createIncomingPacket(String s) throws UnknownPacketException {
		if((s.length() == 20)&&(s.charAt(12) == '0')&&(s.charAt(13) == '0')
									&&(s.charAt(14) == '0')&&(s.charAt(15) == '0')
									&&(s.charAt(16) == '0')&&(s.charAt(17) == '0')
									&&(s.charAt(18) == '0')&&(s.charAt(19) == '1')) {
			return new ICQSRV_HELLOPacket(s);
		}
		else if((s.charAt(3) == '4')&&(s.charAt(15) == '1')) { 
			return new ICQSRV_COOKIEPacket(s);
		}	
		else if((s.charAt(15) == '1')&&(s.charAt(19) == '3')) { 
			return new ICQSRV_FAMILIESPacket(s);
		}	
		else if((s.charAt(15) == '1')&&(s.charAt(18) == '1')&&(s.charAt(19) == '8')) { 
			return new ICQSRV_FAMILIES2Packet(s);
		}
		else if((s.charAt(15) == '1')&&(s.charAt(19) == '7')) { 
			return new ICQSRV_RATESPacket(s);
		}
		else if((s.charAt(15) == '1')&&((s.charAt(19) == 'F')||(s.charAt(19) == 'f'))) { 
			return new ICQSRV_REPLYINFOPacket(s);
		}
		else if((s.charAt(15) == '2')&&(s.charAt(19) == '3')) { 
			return new ICQSRV_REPLYLOCATIONPacket(s);
		}
		else if((s.charAt(15) == '3')&&(s.charAt(19) == '1')) { 
			return new ICQSRV_CONTACTERRPacket(s);
		}
		else if((s.charAt(15) == '3')&&(s.charAt(19) == '3')) { 
			return new ICQSRV_REPLYBUDDYPacket(s);
		}
		else if((s.charAt(15) == '3')&&((s.charAt(19) == 'A')||(s.charAt(19) == 'a'))) { 
			return new ICQSRV_REFUSEDPacket(s);
		}
		else if((s.charAt(15) == '3')&&((s.charAt(19) == 'B')||(s.charAt(19) == 'b'))) { 
			return new ICQSRV_USERONLINEPacket(s);
		}
		else if((s.charAt(15) == '3')&&((s.charAt(19) == 'C')||(s.charAt(19) == 'c'))) { 
			return new ICQSRV_USEROFFLINEPacket(s);
		}
		else if((s.charAt(15) == '4')&&(s.charAt(19) == '1')) { 
			return new ICQSRV_ICBMERRPacket(s);
		}
		else if((s.charAt(15) == '4')&&(s.charAt(19) == '5')) { 
			return new ICQSRV_REPLYICBMPacket(s);
		}
		else if((s.charAt(15) == '4')&&((s.charAt(19) == 'B')||(s.charAt(19) == 'b'))) { 
			return new ICQSRV_ACKMSGPacket(s);
		}
		else if((s.charAt(15) == '4')&&((s.charAt(19) == 'C')||(s.charAt(19) == 'c'))) { 
			return new ICQSRV_SRVACKMSGPacket(s);
		}
		else if((s.charAt(15) == '9')&&(s.charAt(19) == '3')) { 
			return new ICQSRV_REPLYBOSPacket(s);
		}
		else if((s.charAt(14) == '1')&&(s.charAt(15) == '3')&&(s.charAt(19) == '3')) { 
			return new ICQSRV_REPLYLISTSPacket(s);
		}
		else if((s.charAt(14) == '1')&&(s.charAt(15) == '3')&&(s.charAt(19) == '6')) { 
			return new ICQSRV_REPLYROSTERPacket(s);
		}
		else if((s.charAt(14) == '1')&&(s.charAt(15) == '3')&&(s.charAt(19) == 'e')) { 
			return new ICQSRV_UPDATEACKPacket(s);
		}
		else if((s.charAt(14) == '1')&&(s.charAt(15) == '5')&&(s.charAt(19) == '3')) { 
			return new ICQSRV_REPLYLISTSPacket(s);
		}
		else if(s.length() < 51) {	
			System.out.println("Unknown:"+Utils.printableHexString(s));
			throw new UnknownPacketException();
		}
		else if((s.charAt(15) == '4')&&(s.charAt(19) == '7')&&(s.charAt(48) == '0')&&(s.charAt(49) == '0')
				&&(s.charAt(50) == '0')&&(s.charAt(51) == '1')) { // packet is SRV_RECVMSG
			return new ICQSRV_RECVMSGV1Packet(s);
		}
		else if((s.charAt(15) == '4')&&(s.charAt(19) == '7')&&(s.charAt(48) == '0')&&(s.charAt(49) == '0')
				&&(s.charAt(50) == '0')&&(s.charAt(51) == '2')) { // packet is SRV_RECVMSG
			return new ICQSRV_RECVMSGV2Packet(s);
		}
		else {
			System.out.println("Unknown:"+Utils.printableHexString(s));
			throw new UnknownPacketException();	
		}
	}
}
