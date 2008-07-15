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

package messenger;

import java.util.Vector;
import messenger.MSN.MSNProtocol;
import messenger.ICQ.ICQProtocol;
import messenger.Yahoo.YahooProtocol;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.4 $
 * @date $Date: 2003/05/04 17:02:16 $
 * 
 * 
 */
public class Protocols {
	private Vector protocolList = null;
	
	/**
	 * @see java.lang.Object#Object()
	 */
	public Protocols() {
		protocolList = new Vector();
	}
	
	/**
	 * Method Protocols.
	 * @param protocols
	 */
	public Protocols(Vector protocols) {
		assert protocols != null;
		
		protocolList = protocols; 
		/*new Vector();
		
		for(int i = 0; i < protocols.size(); i++) {
			protocolList.addElement(protocols.elementAt(i));
		}*/
		
		assert protocols.size() == this.getNumberOfProtocols();
	}
	
	/**
	 * Method addProtocol.
	 * @param mess
	 */
	public void addProtocol(AProtocol mess) {
		assert mess != null;
		int old = this.getNumberOfProtocols();
		
		protocolList.addElement(mess);
		
		assert this.getNumberOfProtocols() == old + 1;
	}
	
	/**
	 * Method getProtocol.
	 * @param i
	 * @return Object
	 */
	public AProtocol getProtocol(int i) {
		return (AProtocol)protocolList.elementAt(i);
	}
	
	public AProtocol getProtocol(String contact) throws UnknownAccountException {
		assert contact != "";
		
		for(int i = 0; i < protocolList.size(); i++) {
			if(((AProtocol)protocolList.elementAt(i)).getLoginName().equals(contact)) {
				return (AProtocol)protocolList.elementAt(i);
			}
		}	
		throw new UnknownAccountException();
	}
	
	public AProtocol getProtocolContainingContact(String contact) throws UnknownContactException {
		assert contact != "";	
		
		for(int i = 0; i < protocolList.size(); i++) {
			for(int j = 0; j < ((AProtocol)protocolList.elementAt(i)).getContactList().size(); j++) {
				AProtocol p = (AProtocol)protocolList.elementAt(i);
				if(((AContact)p.getContactList().elementAt(j)).getLogin().equals(contact)) {
					return p;
				}
			}
		}
		throw new UnknownContactException();	
	}
	
	public Vector getContactList() {
		Vector v = new Vector();
		for(int i = 0; i < protocolList.size(); i++) {
			v.addAll(((AProtocol)protocolList.elementAt(i)).getContactList());
		}
		return v;
	}
		
	public int getNumberOfProtocols() {
		return protocolList.size();
		
	}
	
	/**
	 * Method removeProtocol.
	 * @param protocol
	 */
	public void removeProtocol(String protocol) { 
		// haal messengersystem uit vector
		// protocolList.removeElementAt(i);
		
		assert protocol != "";
		//protocolList.removeElement(i);
	
	}

	public void refreshProtocol(int i) {
		switch (i) {
			case 0: ICQProtocol icq = (ICQProtocol)protocolList.get(i);
						protocolList.removeElementAt(i);
						protocolList.insertElementAt(new ICQProtocol(icq.getControlData(), "login.icq.com", 5190), i);
						break;
			case 1:	MSNProtocol msn = (MSNProtocol)protocolList.get(i);
						protocolList.removeElementAt(i);
						protocolList.insertElementAt(new MSNProtocol(msn.getControlData(), "messenger.hotmail.com", 1863), i);
						break;
			case 2: YahooProtocol yahoo = (YahooProtocol)protocolList.get(i);
						protocolList.removeElementAt(i);
						protocolList.insertElementAt(new YahooProtocol(yahoo.getControlData(), "scsa.yahoo.com", 5050), i);
						break;
		}
	}
	
	public void sendMessage(String contact, String message) throws UnknownContactException, ProtocolException {
		assert contact != "";
		assert message != "";
		
		for(int i = 0; i < protocolList.size(); i++) {
			AProtocol aProtocol = (AProtocol)protocolList.elementAt(i);
			for(int j = 0; j < aProtocol.getContactList().size(); j++) {
				if(((AContact)aProtocol.getContactList().elementAt(i)).getLogin().equals(contact)) {
					aProtocol.sendMessage(contact, message);
				}
			}
		}
		throw new UnknownContactException();
	}
}
