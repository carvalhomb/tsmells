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
import messenger.BadArgumentException;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:03:13 $
 * 
 * 
 */
public class YahooCLI_ISAWAYPacket extends YahooPacket {
	private String fStatus = "";
	
	/**
	 * Method YahooCLI_ISAWAYPacket.
	 * @param arg
	 * @throws BadArgumentException
	 */
	public YahooCLI_ISAWAYPacket(Vector arg) throws BadArgumentException {
		assert (arg.size() == 1);
		
		setKindOfPacket("CLI_ISAWAY");
		
		String s = (String)arg.elementAt(0);
		if(s.equals("ONLINE")) { fStatus = "3030"; }
		else if(s.equals("BRB")) { fStatus = "3031"; }
		else if(s.equals("BUSY")) { fStatus = "3032"; }
		else if(s.equals("NOTATHOME")) { fStatus = "3033"; }
		else if(s.equals("NOTATDESK")) { fStatus = "3034"; }
		else if(s.equals("NOTINOFFICE")) { fStatus = "3035"; }
		else if(s.equals("ONPHONE")) { fStatus = "3036"; }
		else if(s.equals("INVISIBLE")) { fStatus = "3132"; }
		else if(s.equals("OFFLINE")) { fStatus = "005A55AA56"; }
		else { throw new BadArgumentException(); }
		
		updatePacket();
	}
	
	/**
	 * Method updatePacket.
	 */
	private void updatePacket() {
		String body = "3130"+
								ARGUMENT_SEPARATOR+
								fStatus+
								ARGUMENT_SEPARATOR;
														
		this.setHeader("03", body.length());
		this.setBody(body);
		this.setContent();
	}
}
