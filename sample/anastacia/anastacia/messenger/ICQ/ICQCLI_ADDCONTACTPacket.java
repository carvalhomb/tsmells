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
 * @version $Revision: 1.2 $
 * @date $Date: 2003/04/16 17:00:44 $
 * 
 * This is sent at login and when you add a new user to your contact list. 
 * It contains a list of all the uin's in you're contact list.
 * 
 */
public class ICQCLI_ADDCONTACTPacket extends ICQOutgoingPacket {
	Vector uins;
	
	/**
	 * Method ICQCLI_ADDCONTACTPacket.
	 * @param Vector arg
	 */
	public ICQCLI_ADDCONTACTPacket(Vector arg) {
		//assert (arg.size() == 1);
		//System.out.println("arg: "+arg.size());
		
		uins = new Vector(arg);
		
		setKindOfPacket("CLI_ADDCONTACT");
		updatePacket();
}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = SNAC3_4
									+"000000000000";
		
		for(int i = 0; i < uins.size(); i++) {
			//System.out.println(uins.size());
			ICQContact c = (ICQContact)uins.elementAt(i);
			String Uin = c.getUIN();
			body += Utils.TwoByteInt2FourCharString(Utils.UIN(Uin).length()/2)+Utils.UIN(Uin); // lengte zit al mee ingecalculeerd
		}
								
		setContent(Utils.fromHexString(SNACS+Utils.TwoByteInt2FourCharString(SEQUENCE)+Utils.TwoByteInt2FourCharString(body.length()/2)+body));
	}
}
