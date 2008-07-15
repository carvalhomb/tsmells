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

import java.util.Vector;
import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.4 $
 * @date $Date: 2003/04/25 22:29:26 $
 * 
 * 
 */
public class YahooSRV_LISTPacket extends YahooPacket {
	private Vector fContactList;
	
	public YahooSRV_LISTPacket (String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_LIST");
		
		fContactList = new Vector();

		analyze();
	}
	
	private void analyze() {
		String s = getContentHex();
		String content = "";
		YahooContact aContact = null;

		int i = 0;
		while(i < s.length()) {
			if((s.charAt(i) == '3')&&(s.charAt(i+1) == '8')&&(s.charAt(i+2) == '3')&&(s.charAt(i+3) == '7')
				&&(s.charAt(i+4) == 'c')&&(s.charAt(i+5) == '0')&&(s.charAt(i+6) == '8')&&(s.charAt(i+7) == '0')) {
				i += 8;
				
				while(!((s.charAt(i) == '3')&&(s.charAt(i+1) == 'a'))) {
					i++;	// skip group name (could be 'friends' or 'all'
				}
				i += 2;
				
				while((i+3 < s.length())&&!((s.charAt(i) == '0')&&(s.charAt(i+1) == 'a'))) {
						content += s.charAt(i);
						//System.out.println(content);
						if((content.length() >= 2) && (content.charAt(content.length()-1) == 'c') 
															&& (content.charAt(content.length()-2) == '2')) {
							//System.out.println(content);	 
							String contact = Utils.HexString2AsciiString(content.substring(0, content.length()-2));
							//System.out.println(contact);
							aContact = new YahooContact(contact);
							aContact.setNick(contact);
							fContactList.add(aContact);
							content = "";
						}
						
						i++;
				}
				//System.out.println(content);	 
				String contact = Utils.HexString2AsciiString(content);
				//System.out.println(contact);
				aContact = new YahooContact(contact);
				aContact.setNick(contact);
				fContactList.add(aContact);
				//System.out.println(content);
				/*System.out.println("contactlist size: "+fContactList.size());
				for(int j = 0; j < fContactList.size(); j++) {
					System.out.println(((YahooContact)fContactList.elementAt(j)).getLogin());
				}*/
				
			}
			/* parses challenge strings, http proxy stuff, Time, path, ... -> not interesting for now
			/*else if((s.charAt(i) == 'c')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '8')&&(s.charAt(i+3) == '0')) {
				i += 4;
				while((i+3 < s.length())&&!((s.charAt(i) == 'c')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '8')&&(s.charAt(i+3) == '0'))) {
						content += s.charAt(i);
						i++;
				}
				System.out.println(Utils.HexString2AsciiString(content));
				content = "";
			}*/
			else {
				i++;
			}
		}
	}
	
	/**
	 * Method getList.
	 * @return Vector vector of contacts
	 */
	public Vector getList() {
		//System.out.println(fContactList.size()+" "+((YahooContact)fContactList.elementAt(fContactList.size()-1)).getLogin()+"  "+((YahooContact)fContactList.elementAt(fContactList.size()-1)).getNick());				
		return fContactList;	
	}
}
