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

package gui;

import messenger.Protocols; 
import messenger.ICQ.ICQProtocol;
import messenger.MSN.MSNProtocol;
import messenger.Yahoo.YahooProtocol;
import messenger.DataBuffer;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.6 $
 * @date $Date: 2003/05/24 14:43:21 $
 * 
 */
public class MessGui extends Thread implements DumpListener {
	private DumpListener fListener;
	private View theView;
	private Protocols fManager = null;
	private int fDumpFactor = 0;
	private DataBuffer fICQData;
	private DataBuffer fYahooData;
	//private Buffer fMSNData;
	private DataBuffer fMSNData;
	private UpdateUser fUpdate;
	private boolean fDump = false;
		
	/**
	 * @see java.lang.Object#Object()
	 */
	public MessGui() {
		fICQData = new DataBuffer();
		fYahooData = new DataBuffer();
		//fMSNData = new Buffer();
		fMSNData = new DataBuffer();
		
		// dus nieuwe systems, den buffer wordt meegegeven in de constructor
		fManager = new Protocols();

		ICQProtocol icq = new ICQProtocol(fICQData, "login.icq.com", 5190);
		fManager.addProtocol(icq);
		
		MSNProtocol msn = new MSNProtocol(fMSNData,"messenger.hotmail.com", 1863); // nieuw msn systeem
		fManager.addProtocol(msn);
		
		YahooProtocol yahoo = new YahooProtocol(fYahooData, "scsa.yahoo.com", 5050 ); // nieuw yahoo systeem
		fManager.addProtocol(yahoo);
		
		theView = new View(fManager);
		theView.setDumpListener(this);
		
		fDump = false; //FF opgezet :)
		
		fUpdate = new UpdateUser(fICQData, fMSNData);
		fUpdate.start();
		this.setPriority(MIN_PRIORITY);
		this.start();
	}
	
	
	public void dumpTriggered() {
		fDump = true;
	}
	
	public void dumpUnTriggered() {
		fDump = false;
	}
	
	/**
		 * @author Bart Van Rompaey
		 * @version $Revision: 1.6 $
		 * @date $Date: 2003/05/24 14:43:21 $
		 * 
		 * 
		 */
	class UpdateUser extends Thread {
		private DataBuffer fICQData;
		//private Buffer fMSNData;
		private DataBuffer fMSNData;
		
		/**
		 * Method UpdateUser.
		 * @param icq
		 */
		public UpdateUser(DataBuffer icq, /*Buffer*/DataBuffer msn) {
			fICQData = icq;
			fMSNData = msn;
		}
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			while(true) {
				/*System.out.println("UPDATEGUI");	*/		
				if(fICQData.ExistsUserData()) {
					String output = "";
					//System.out.print("ICQ: ");
					output = fICQData.getToUser();
					if((output.charAt(0) == 'M')&&(output.charAt(1) == 'S')&&
						(output.charAt(2) == 'G')&&(output.charAt(3) == ' ')) {
						String s = output.substring(4);
						theView.showMessage(s);
					}
				}
				if(fICQData.ExistsDump()) {
					String output = fICQData.getDump();
					if(output.equals("CONTACT")) {
						theView.updateContacts();
					}
					if(fDump) {
						System.out.println(output);
					}
				}
				
				if (fMSNData.ExistsUserData()) {
					String s = fMSNData.getToUser();
					if (! s.equals("")) {
						if (s.substring(0,3).equals("MSG")) {
							theView.showMSNMessage(s.substring(4));
						} else if (s.substring(0,3).equals("BYE")) {
							theView.showMSNByeMessage(s.substring(4));
						} else if (s.substring(0,3).equals("TYP")) {
							theView.showMSNTypingMessage(s.substring(4));
						} else if (s.substring(0,3).equals("INI")) {		// initialmailnotification
							theView.showDialog(s.substring(4));
						} else if (s.substring(0,3).equals("EMN")) {	// emailnotification
							theView.showDialog(s.substring(4));
						} else {
							theView.showDialog(s);
						}
					}
				}
				
				if (fMSNData.ExistsDump()) {
					String s = fMSNData.getDump();
					if(s.equals("CONTACT")) {
						theView.updateContacts();
					}
					if (fDump) {
						if (! s.equals("")) {
							System.out.println(s);
						}
					}
				}
				if(fYahooData.ExistsUserData()) {
					String output = "";
					//System.out.print("Yahoo: ");
					output = fYahooData.getToUser();
					if((output.charAt(0) == 'M')&&(output.charAt(1) == 'S')&&
						(output.charAt(2) == 'G')&&(output.charAt(3) == ' ')) {
						String s = output.substring(4);
						theView.showMessage(s);
					}
				}
				if(fYahooData.ExistsDump()) {
					
					String output = fYahooData.getDump();
					if(output.equals("CONTACT")) {
						//System.out.println("CONTACT LIST CHANGED!");
						theView.updateContacts();
					}
					if(fDump) {
						System.out.println(output);
					}
				}
				try {
					sleep(200);
				}
				catch(InterruptedException e) {
			
				}
				//this.yield();
			}
		}
	}
	
	public void setListener(DumpListener dumpListener) {
		fListener = dumpListener;
	}
	
	public DumpListener getListener() {
		return fListener;
	}

	/**
	 * Method main.
	 * @param args
	 */
	public static void main(String[] args) {
		MessGui c = new MessGui();
	}	
}
