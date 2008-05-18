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

package messenger.MSN;

import java.util.Vector;
import messenger.UnknownPacketException;
import messenger.BadArgumentException; 
import messenger.IPacketFactory;
import messenger.IPacket;
import messenger.IncomingNullPacketException;

/**
 * A factory for creating MSN packets
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.2 $
 * @date $Date: 2003/04/05 17:31:27 $
 */
public class MSNPacketFactory implements IPacketFactory {
	
	/**
	 * @see messenger.IPacketFactory#createOutgoingPacket(String, Vector)
	 */
	public IPacket createOutgoingPacket(String packet, Vector arg) throws UnknownPacketException, BadArgumentException {
		
		if(packet.equals("MSN_VER")) {
			return new MSNOutgoingVERPacket(arg);
		}
		else if(packet.equals("MSN_INF")) {
			return new MSNOutgoingINFPacket(arg);
		}
		else if(packet.equals("MSN_USRI")) {
			return new MSNOutgoingUSRIPacket(arg);
		}
		else if(packet.equals("MSN_USRS")) {
			return new MSNOutgoingUSRSPacket(arg);
		}
		else if(packet.equals("MSN_CHG")) {
			return new MSNOutgoingCHGPacket(arg);
		}
		else if(packet.equals("MSN_MSG")) {
			return new MSNOutgoingMSGPacket(arg);
		}
		else if(packet.equals("MSN_XFR")) {
			return new MSNOutgoingXFRPacket(arg);
		}
		else if(packet.equals("MSN_USR")) {
			return new MSNOutgoingUSRPacket(arg);
		}
		else if(packet.equals("MSN_CAL")) {
			return new MSNOutgoingCALPacket(arg);
		}
		else if(packet.equals("MSN_ANS")) {
			return new MSNOutgoingANSPacket(arg);
		}
		else if(packet.equals("MSN_QRY")) {
			return new MSNOutgoingQRYPacket(arg);
		}
		else if(packet.equals("MSN_LST")) {
			return new MSNOutgoingLSTPacket(arg);
		}
		else if(packet.equals("MSN_ADD")) {
			return new MSNOutgoingADDPacket(arg);
		}
		else if(packet.equals("MSN_OUT")) {
			return new MSNOutgoingOUTPacket(arg);
		}
		else if(packet.equals("MSN_BYE")) {
			return new MSNOutgoingBYEPacket(arg);
		}
		else {
	  		 throw new UnknownPacketException("unknown Packet");
   		}
	}
	
	/**
	 * @see messenger.IPacketFactory#createIncomingPacket(String)
	 */
	public IPacket createIncomingPacket(String receivedPacket) throws UnknownPacketException, IncomingNullPacketException {
		String cmd = null;
		
		if (receivedPacket == null) throw new IncomingNullPacketException("Received a null packet");
		
		try {
			cmd = receivedPacket.substring(0,3);
		} catch (StringIndexOutOfBoundsException e) {
			throw new UnknownPacketException("Error, unknown packet: " + receivedPacket);
		}
		
		if (((cmd.charAt(0) > 47) && (cmd.charAt(0) < 58))
				&& ((cmd.charAt(1) > 47) && (cmd.charAt(1) < 58))
				&& ((cmd.charAt(2) > 47) && (cmd.charAt(2) < 58))) {

				if (cmd.equals("200")) {
					throw new UnknownPacketException("Syntax error");
				} else if (cmd.equals("201")) {
					throw new UnknownPacketException("Invalid parameter");
				} else if (cmd.equals("205")) {
					throw new UnknownPacketException("Invalid user");
				} else if (cmd.equals("206")) {
					throw new UnknownPacketException("Domain name missing");
				} else if (cmd.equals("207")) {
					throw new UnknownPacketException("Already logged in");
				} else if (cmd.equals("208")) {
					throw new UnknownPacketException("Invalid username");
				} else if (cmd.equals("209")) {
					throw new UnknownPacketException("Invalid fusername");
				} else if (cmd.equals("210")) {
					throw new UnknownPacketException("User list full");
				} else if (cmd.equals("215")) {
					throw new UnknownPacketException("User already there");
				} else if (cmd.equals("216")) {
					throw new UnknownPacketException("User already on list");
				} else if (cmd.equals("217")) {
					throw new UnknownPacketException("User not online");
				} else if (cmd.equals("218")) {
					throw new UnknownPacketException("Already in mode");
				} else if (cmd.equals("219")) {
					throw new UnknownPacketException("User is in the opposite list");
				} else if (cmd.equals("280")) {
					throw new UnknownPacketException("Switchboard failed");
				} else if (cmd.equals("281")) {
					throw new UnknownPacketException("Transfer to switchboard failed");
				} else if (cmd.equals("300")) {
					throw new UnknownPacketException("Required field missing");
				} else if (cmd.equals("302")) {
					throw new UnknownPacketException("Not logged in");
				} else if (cmd.equals("500")) {
					throw new UnknownPacketException("Internal server error");
				} else if (cmd.equals("501")) {
					throw new UnknownPacketException("Database server error");
				} else if (cmd.equals("510")) {
					throw new UnknownPacketException("File operation failed");
				} else if (cmd.equals("520")) {
					throw new UnknownPacketException("Memory allocation failed");
				} else if (cmd.equals("600")) {
					throw new UnknownPacketException("Server is busy");
				} else if (cmd.equals("601")) {
					throw new UnknownPacketException("Server is unavailable");
				} else if (cmd.equals("602")) {
					throw new UnknownPacketException("Peer nameserver is down");
				} else if (cmd.equals("603")) {
					throw new UnknownPacketException("Database connection failed");
				} else if (cmd.equals("604")) {
					throw new UnknownPacketException("Server is going down");
				} else if (cmd.equals("707")) {
					throw new UnknownPacketException("Could not create connection");
				} else if (cmd.equals("711")) {
					throw new UnknownPacketException("Write is blocking");
				} else if (cmd.equals("712")) {
					throw new UnknownPacketException("Session is overloaded");
				} else if (cmd.equals("713")) {
					throw new UnknownPacketException("Too many active users");
				} else if (cmd.equals("714")) {
					throw new UnknownPacketException("Too many sessions");
				} else if (cmd.equals("715")) {
					throw new UnknownPacketException("Not expected");
				} else if (cmd.equals("717")) {
					throw new UnknownPacketException("Bad friend file");
				} else if (cmd.equals("911")) {
					throw new UnknownPacketException("Authentication failed");
				} else if (cmd.equals("913")) {
					throw new UnknownPacketException("Not allowed when offline");
				} else if (cmd.equals("920")) {
					throw new UnknownPacketException("Not accepting new users");
				} else if (cmd.equals("924")) {
					throw new UnknownPacketException("Passport account not yet verified");
				} else {
					throw new UnknownPacketException("Unkown error from server");
				}
		}
		else if (cmd.equals("VER")){
			return new MSNIncomingVERPacket(receivedPacket);
		}
		else if (cmd.equals("XFR")){
			return new MSNIncomingXFRPacket(receivedPacket);
		}
		else if (cmd.equals("CHG")){
			return new MSNIncomingCHGPacket(receivedPacket);
		}
		else if (cmd.equals("CHL")){
			return new MSNIncomingCHLPacket(receivedPacket);
		}
		else if (cmd.equals("ILN")){
			return new MSNIncomingILNPacket(receivedPacket);
		}
		else if (cmd.equals("LST")){
			return new MSNIncomingLSTPacket(receivedPacket);
		}
		else if (cmd.equals("MSG")){
			return new MSNIncomingMSGPacket(receivedPacket);
		}
		else if (cmd.equals("USR")){
			return new MSNIncomingUSRPacket(receivedPacket);
		}
		else if (cmd.equals("INF")) { 
			return new MSNIncomingINFPacket(receivedPacket);
		}
		else if (cmd.equals("RNG")) { 
			return new MSNIncomingRNGPacket(receivedPacket);
		}
		else if (cmd.equals("QRY")) { 
			return new MSNIncomingQRYPacket(receivedPacket);
		}
		else if (cmd.equals("NLN")) { 
			return new MSNIncomingNLNPacket(receivedPacket);
		}
		else if (cmd.equals("FLN")) { 
			return new MSNIncomingFLNPacket(receivedPacket);
		}
		else if (cmd.equals("ADD")) { 
			return new MSNIncomingADDPacket(receivedPacket);
		}
		else if (cmd.equals("CAL")) { 
			return new MSNIncomingCALPacket(receivedPacket);
		}
		else if (cmd.equals("BPR")) { 
			return new MSNIncomingBPRPacket(receivedPacket);
		}
		else {
			throw new UnknownPacketException("Unknown packet: " + receivedPacket);
		}
	}
		
}
