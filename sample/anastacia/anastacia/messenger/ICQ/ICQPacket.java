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

import messenger.ABytePacket;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/05/25 18:09:41 $
 * 
 * All ICQ packets are inherited from tis abstract class
 * 
 */
public abstract class ICQPacket extends ABytePacket {
	protected final String LOGIN	= "2A01";
	protected final String SNACS = "2A02";
	protected final String ERRORS = "2A03";
	protected final String DISCONNECT = "2A04";
	protected final String PING = "2A05";
	
	protected final String HELLO	= "00000001";
	
	protected final String TLV1 	= "0001";
	protected final String TLV2	= "0002";
	protected final String TLV3	= "0003";
	protected final String TLV4 = "0004";
	protected final String TLV5	= "0005";
	protected final String TLV6	= "0006";
	protected final String TLV7 = "0007";
	protected final String TLV8 = "0008";
	protected final String TLV9	= "0009";
	protected final String TLV10 = "000A";
	protected final String TLV11	= "000B";
	protected final String TLV12 	= "000C";
	protected final String TLV13 = "000D";
	protected final String TLV14 = "000E";
	protected final String TLV15 = "000F";
	protected final String TLV16 = "0010";
	protected final String TLV17 = "0011";
	protected final String TLV18 = "0012";
	protected final String TLV19 = "0013";
	protected final String TLV20 = "0014";
	protected final String TLV21 = "0015";
	protected final String TLV22	= "0016";
	protected final String TLV23 = "0017";
	protected final String TLV24 = "0018";
	protected final String TLV25 = "0019"; 
	protected final String TLV26 = "001A";
	protected final String TLV0131 = "0131";
	protected final String TLV257 = "0101";
	protected final String TLV1281 = "0501";

	protected final String SNAC1_2 = "00010002";
	protected final String SNAC1_6 = "00010006";
	protected final String SNAC1_8 = "00010008";
	protected final String SNAC1_E = "0001000E";
	protected final String SNAC1_23 = "00010017";
	protected final String SNAC1_1E = "0001001E";
	protected final String SNAC2_2 = "00020002";
	protected final String SNAC2_4 = "00020004";
	
	protected final String SNAC3_2 = "00030002";
	protected final String SNAC3_4 = "00030004";
	
	protected final String SNAC4_2 = "00040002";
	protected final String SNAC4_4 = "00040004";
	protected final String SNAC4_6 = "00040006";
	
	protected final String SNAC9_2 = "00090002";
	
	protected final String SNAC13_2 = "00130002";
	protected final String SNAC13_4 = "00130004";
	protected final String SNAC13_8 = "00130008";
	protected final String SNAC13_11 = "00130011";
	protected final String SNAC13_12 = "00130012";
	
	protected final String SNAC15_2 = "00150002";
	
	
	protected static int SEQUENCE = 0;
	
	protected static Utils fU;
	private String fKindOfPacket = "";
	
	/**
	 * @see java.lang.Object#Object()
	 */
	public ICQPacket() {
		fU = new Utils();
	}
	
	/**
	 * Method setKindOfPacket.
	 * @param String name of the packet
	 * 
	 * @pre s != null;
	 */
	protected void setKindOfPacket(String s) { // protected: alleen afgeleide klasse mogen dit zetten of intern, maar nooit de oproepende klasse
		assert s != null;
		fKindOfPacket = s;		
	}
	
	/**
	 * Method getKindOfPacket.
	 * @return String name of the packet
	 * 
	 * @pre fKindOfPacket != null;
	 */
	public String getKindOfPacket() {
		assert fKindOfPacket != null;
		return fKindOfPacket;
	}
	
}
