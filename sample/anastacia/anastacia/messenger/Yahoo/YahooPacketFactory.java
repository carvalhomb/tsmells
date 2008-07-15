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

package messenger.Yahoo;

import messenger.IPacketFactory;
import messenger.IPacket;
import messenger.UnknownPacketException;
import messenger.BadArgumentException;
import messenger.Utils;
import java.util.Vector;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.4 $
 * @date $Date: 2003/05/25 10:15:50 $
 * 
 * 
 */
public class YahooPacketFactory implements IPacketFactory {
	
	/**
	 * @see messenger.IPacketFactory#createOutgoingPacket(java.lang.String, java.util.Vector)
	 */
	public IPacket createOutgoingPacket(String packet, Vector arg) throws UnknownPacketException, BadArgumentException {
		if(packet.equals("CLI_HELLO")) {
			return new YahooCLI_HELLOPacket(arg);
		}
		else if(packet.equals("CLI_AUTH")) {
			return new YahooCLI_AUTHPacket(arg);
		}
		else if(packet.equals("CLI_AUTH_RESP")) {
			return new YahooCLI_AUTH_RESPPacket(arg);	
		}
		else if(packet.equals("CLI_PASSTHROUGH2")) {
			return new YahooCLI_PASSTHROUGH2Packet(arg);	
		}
		else if(packet.equals("CLI_0015")) {
			return new YahooCLI_0015Packet(arg);	
		}
		else if(packet.equals("CLI_ISBACK")) {
			return new YahooCLI_ISBACKPacket(arg);	
		}
		else if(packet.equals("CLI_ISAWAY")) {
			return new YahooCLI_ISAWAYPacket(arg);	
		}
		else if(packet.equals("CLI_MESSAGE")) {
			return new YahooCLI_MESSAGEPacket(arg);	
		}
		else {
	  		 throw new UnknownPacketException();
   		}
	}
	
	/**
	 * @see messenger.IPacketFactory#createIncomingPacket(java.lang.String)
	 */
	public IPacket createIncomingPacket(String s) throws UnknownPacketException {
		//System.out.println("creaeteincomingpacket");
		if(s.length() < 23) {
			//System.out.println("short message:");
			//System.out.println(Utils.printableHexString(s));	
			throw new UnknownPacketException();
		}
		else if((s.charAt(22) == '0') && (s.charAt(23) == '1')) {
			return new YahooSRV_LOGONPacket(s);	
		}
		else if((s.charAt(22) == '0')&&(s.charAt(23) == '2')) { 
			return new YahooSRV_LOGOFFPacket(s);
		}		
		else if((s.charAt(22) == '0')&&(s.charAt(23) == '4')) { 
			return new YahooSRV_ISBACKACKPacket(s);
		}		
		else if((s.charAt(22) == '0')&&(s.charAt(23) == '6')) { 
			return new YahooSRV_MESSAGEPacket(s);
		}		
		else if((s.charAt(22) == '4')&&(s.charAt(23) == 'b')) { 
			return new YahooSRV_NOTIFYPacket(s);
		}	
		else if((s.charAt(22) == '4')&&(s.charAt(23) == 'c')) { 
			System.out.println("server - hello");
			return new YahooSRV_HELLOPacket(s);
		}	
		else if((s.charAt(22) == '5') && (s.charAt(23) == '4')) {
			return new YahooSRV_AUTH_RESPPacket(s);	
		}
		else if((s.charAt(22) == '5') && (s.charAt(23) == '5')) {
			return new YahooSRV_LISTPacket(s);	
		}
		else if((s.charAt(22) == '5')&&(s.charAt(23) == '7')) {
			return new YahooSRV_AUTHPacket(s);
		}
	
		
		else {
			System.out.println("creaeteincomingpacket - unknown: "+s.charAt(22)+" "+s.charAt(23));
	  		 throw new UnknownPacketException();
   		}
	}

}
