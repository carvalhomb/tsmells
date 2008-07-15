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
import messenger.Utils;


/**
 * @author Bart Van Rompaey
 *
 * This class is a wrapper and parser for the OSCAR SNAC(19,6) packet.
 * This packet contains your complete server side contact list. 
 * Sending CLI_REQROSTER with an old time and/or list size 
 * will cause the server to send this SNAC. If the values sent in 
 * CLI_CHECKROSTER are up to date, the server replies with 
 * SRV_REPLYROSTEROK.
 */
public class ICQSRV_REPLYROSTERPacket extends ICQPacket {
	private Vector fContactList = null;
	private Vector fIgnoreList = null;
	
	// true if byte 12 is 00, it means REPLYROSTER is only one packet. 
	// if it is two (or more) packets, this byte will be set to 01 while there
	// are still REPLYROSTER packet(s) following.
	private boolean fMoreRRPacketsComing; 
	
	/**
	 * Method ICQSRV_REPLYROSTERPacket.
	 * @param String packet in hex form
	 */
	public ICQSRV_REPLYROSTERPacket(String s) {
		setContent(Utils.fromHexString(s));	
		
		//System.out.println("**********SEND THIS TO ME***************");
		//System.out.println("REPLYROSTER PACKET:                          ");	
		//System.out.println(fU.printableHexString(s));
		//System.out.println("**********/SEND THIS TO ME***************");
		
		setKindOfPacket("SRV_REPLYROSTER");
		Utils.printableHexString(getContentHex());
		
		analyze();
	}
	
	/**
	 * Method analyze.
	 */
	private void analyze() {
		int number = 0, i = 0;
		String s = getContentHex(), uin = "", nick = "", nickhex = "";
		fContactList = new Vector();
		fIgnoreList = new Vector();
		boolean end = false;

		int length = (((int)s.charAt(8))-48)*4096 + (((int)s.charAt(9))-48)*256 + (((int)s.charAt(10))-48)*16 + ((int)(s.charAt(11))-48);
		
		try {
			if(s.charAt(23) == '1') {
				fMoreRRPacketsComing = true;
			}
			else {
				// normally it should be 0 then, but you never
				// know with clone icq clients
				fMoreRRPacketsComing = false;
			}
							
			while((i < s.length())&&(!((s.charAt(i) == '0')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '0')&&
					((s.charAt(i+3) == '9')||(s.charAt(i+3) == '8')||(s.charAt(i+3) == '7'))&&(s.charAt(i+4) == '3')))) {
					i++;
			} // ready to read first uin
			
			while(i+4 < s.length()) {	
					
					boolean ignore = false;
					int uinlength = (((int)s.charAt(i))-48)*4096 + (((int)s.charAt(i+1))-48)*256 + (((int)s.charAt(i+2))-48)*16 + ((int)(s.charAt(i+3))-48);
					ICQContact aContact = new ICQContact();
					uin = "";
					nick = "";
					for(int j = 0; j < uinlength*2; j += 2) {
						uin += s.charAt(i+5+j);			
					}
	
					i += 3;		
					i = i + uinlength*2;
							
					while((i+4 < s.length())&&(!((s.charAt(i) == '0')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '0')&&
					((s.charAt(i+3) == '9')||(s.charAt(i+3) == '8')||(s.charAt(i+3) == '7'))&&(s.charAt(i+4) == '3')))) {
						
						// contact is nen ignore list person
						if((s.charAt(i) == '0')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '0')&&(s.charAt(i+3) == 'e')) {
							ignore = true;
						}
						
						// wiiii, person heeft een nickname!
						if((s.charAt(i) == '0')&&(s.charAt(i+1) == '1')&&(s.charAt(i+2) == '3')
							&&(s.charAt(i+3) == '1')&&(s.charAt(i+4) == '0')&&(s.charAt(i+5) == '0')) {
							String nicklengthhex = "";
							nicklengthhex += s.charAt(i+6);
							nicklengthhex += s.charAt(i+7);
							int nicklength = Utils.hexToInt(nicklengthhex);
							nickhex = "";
					
							i += 8;
							int start = i;
							while(i < start+nicklength*2) {
								nickhex += s.charAt(i);
								i++;
							}
							
							nick = Utils.HexString2AsciiString(nickhex);
							i--;
						}			
						i++;
					} 
					
					aContact.setLogin(uin);
					if(nick.equals("")) {
						aContact.setNick(uin);
					}
					else {
						aContact.setNick(nick);
					}
					if(!ignore) {	
						fContactList.add(aContact);
						//System.out.println(fContactList.size()+" "+((ICQContact)fContactList.elementAt(fContactList.size()-1)).getLogin()+"  "+((ICQContact)fContactList.elementAt(fContactList.size()-1)).getNick());
					}	
					else {
						//System.out.println("IGNORE "+uin);
						fIgnoreList.add(aContact);
					}
					// ready to read first uin
			}
		}
		catch(StringIndexOutOfBoundsException e) {
			/*System.out.println("s.length(): "+s.length());
			System.out.println("i: "+i);
			System.out.println("uin: "+uin);*/
		}
		
	}
	
	/**
	 * Method getList.
	 * @return Vector vector of contacts
	 */
	public Vector getList() {
		//System.out.println(fContactList.size()+" "+((ICQContact)fContactList.elementAt(fContactList.size()-1)).getLogin()+"  "+((ICQContact)fContactList.elementAt(fContactList.size()-1)).getNick());				
		return fContactList;	
	}
	
	/**
	 * Method getIgnoreList.
	 * @return Vector vector of ignore list numbers
	 */
	public Vector getIgnoreList() {
		return fIgnoreList;	
	}
	
	/**
	 * Method moreRRPacketsComing.
	 * @return boolean are there still ReplyRoster packets coming?
	 */
	public boolean moreRRPacketsComing() {
		return fMoreRRPacketsComing;
	}
}
