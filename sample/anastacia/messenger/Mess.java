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
import java.io.*;
import messenger.MSN.*;
import messenger.MSN.MSNProtocol;
import messenger.ICQ.ICQProtocol;
import messenger.ICQ.ICQExceptions.ICQException;
import messenger.Yahoo.YahooProtocol;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.6 $
 * @date $Date: 2003/05/25 11:26:51 $
 * 
 * The main class for the text-based messenger
 */
public class Mess extends Thread {

	private Vector v = null;
	private ICQProtocol icq = null;
	private MSNProtocol msn = null;
	private YahooProtocol yahoo = null;
	private Protocols mess = null;
	private int fDumpFactor = 0;
	//private Buffer fMSNData;
	private DataBuffer fMSNData;
	private DataBuffer fICQData;
	private DataBuffer fYahooData;
	private UpdateUser fUpdate;
	private boolean fDump = false;
	
	public Mess() {
		fICQData = new DataBuffer();
		//fMSNData = new Buffer();
		fMSNData = new DataBuffer();
		fYahooData = new DataBuffer();
		v  = new Vector();
		
		// dus nieuwe systems, den buffer wordt meegegeven in de constructor
		icq = new ICQProtocol(fICQData, "login.icq.com", 5190); // nieuw icq systeem
		msn = new MSNProtocol(fMSNData, "ace_ventura_2@hotmail.com", "trac_test" ,"messenger.hotmail.com", 1863); // nieuw msn systeem
		yahoo = new YahooProtocol(fYahooData, "scsa.yahoo.com", 5050); 
		// scsa.yahoo.com
		// cs.yahoo.com
		// scs.yahoo.com
		// protocollen aan vector meegeven
		v.addElement(icq);
		v.addElement(msn);
		v.addElement(yahoo);
		
		// nieuwe messmessenger, met 2 protocollen
		mess = new Protocols(v);
		fUpdate = new UpdateUser(fICQData, fMSNData, fYahooData);
		fUpdate.start();
		this.start();
	}
	
	public void run() {
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		String r = "";
		boolean quit = false;

		while(!quit) {
			try {
				System.out.println("enter request type: ");
				r = console.readLine(); // we wachten dus op gebruikersinvoer
				// dus checken of 'MSN' of 'ICQ' voorkomt
						
				if(r.equals("ICQLOGON")) { // gebruiker wilt inloggen met ICQ
					String passwd = "";
					String uin = "";			
					int port = 0;
					
					System.out.println("enter port number (default 5190) :");
					port = (new Integer((String)console.readLine())).intValue();
					icq.setPort(port);
					
					System.out.println("enter your ICQ uin:");
					uin = console.readLine();
					icq.setLoginName(uin);
					
					
					System.out.println("enter your ICQ password (carefull):");
					passwd = console.readLine();
					icq.setEncPasswd(Utils.ICQEncryptPass(passwd));
					
					try {
						icq.login();
					}
					catch(ICQException e) {
						System.out.println(e.getMessage());
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}		
								
			else if(r.equals("ICQMSG")) {
					String message = "";
					String contact = "";
					
					System.out.println("Enter ICQ uin number to send message to: ");
					try {
						contact = console.readLine();
					}
					catch(IOException e) {
						//
					}
					System.out.println("Enter ICQ message: ");
					try {
						message = console.readLine();
					}
					catch(IOException e) {
						//
					}
					
					try {
						icq.sendMessage(contact, message);
					}
					catch(ICQException e) {
						System.out.println(e.getMessage());
					}
				
				} else if(r.equals("ICQSTATUS")) {
					String status = "";
					
					System.out.println("Enter new ICQ Status");
					status = console.readLine();
				try {
					icq.changeStatus(status);
				}
				catch(ICQException e) {
					System.out.println(e.getMessage());
				}
				// methode oproepen om icq bericht te sturen
			}
			else if(r.equals("ICQSERVERCONTACTLIST")) {
				String status = "";

				icq.getContactListFromServer();
				// methode oproepen om icq bericht te sturen
			}
			else if(r.equals("ICQSHOWCONTACTLIST")) {
				String status = "";
				try {
					icq.retrieveContactList();
				}
				catch(ICQException e) {
					System.out.println(e.getMessage());
				}
				
				// methode oproepen om icq bericht te sturen
			}
			 else if(r.equals("ICQCONTACTLIST")) {
					icq.getContactList();
				
				} else if(r.equals("ICQADDCONTACT")) {
					String uin = "";
					String nick = "";			
					
					System.out.println("enter the uin you want to add:");
					uin = console.readLine();
				
					icq.addContact(uin);											
			}
			else if(r.equals("YAHOOLOGON")) { // gebruiker wilt inloggen met Yahoo
					String passwd = "";
					String loginname = "";	
					int port;		
					
					System.out.println("enter port number (default 5050) :");
					port = (new Integer((String)console.readLine())).intValue();
					yahoo.setPort(port);
					
					System.out.println("enter your Yahoo login:");
					loginname = console.readLine();
					yahoo.setLoginName(loginname);
					
					
					System.out.println("enter your Yahoo password (carefull):");
					passwd = console.readLine();
					yahoo.setEncPasswd(passwd);
				
					yahoo.login();
				}

			else if(r.equals("YLOGON")) {
				String user = "";
				String paswoord = "";
				
				user = "blabla";
				paswoord = "blabla";
				
				yahoo.setLoginName(user);
				yahoo.setEncPasswd(paswoord);
				yahoo.login();
			}
			else if(r.equals("YAHOOSTATUS")) {
					String status = "";
					
					System.out.println("Enter new Yahoo Status");
					status = console.readLine();

					yahoo.changeStatus(status);
			}

			else if(r.equals("MSNLOGON")) {
					String user = "";
					String paswoord = "";
					int port = 0; 

					System.out.println("enter port number (default 1863) :");
					port = (new Integer((String)console.readLine())).intValue();

					System.out.println("enter your MSN loginname:");

					user = console.readLine();
					
					System.out.println("enter your MSN password (carefull):");
					paswoord = console.readLine();
					
					msn.login();
					
				} else if(r.equals("MSNSTATUS")) {
					String status = "";
					
					System.out.println("Enter status: ");
					status = console.readLine();

					msn.changeStatus(status);
	
				} else if(r.equals("MSNMSG")) {
					String message = "";
	
					String contact = "";
					String sessionID = "";
		
					System.out.println("Enter MSN user to send message to: ");
					contact = console.readLine();
					
					System.out.println("Enter MSN message: ");
					message = console.readLine();

					msn.sendMessage(contact, message);

				} else if (r.equals("MSNCONTACTLIST")) {
					msn.getServerContactList();
					
				} else if(r.equals("DUMP")) {
					fDump = true;
					System.out.println("dump on");
					
				} else if(r.equals("NODUMP")) {
					fDump = false;
					System.out.println("dump off");
					
				} 
			else if(r.equals("DUMP")) {
				fDump = true;
				System.out.println("dump on");
			}
			else if(r.equals("NODUMP")) {
				fDump = false;
				System.out.println("dump off");
			}	
			else if(r.equals("HELP")) {
				System.out.println("COMMANDS SUPPORTED:");
				System.out.println(" - ICQLOGON");
				System.out.println(" - ICQMSG");
				System.out.println(" - ICQSERVERCONTACTLIST");
				System.out.println(" - ICQSHOWCONTACTLIST");
				System.out.println(" - MSNLOGON");
				System.out.println(" - MSNLOG-ON");
				System.out.println(" - MSNMSG");
				System.out.println(" - MSNMSGBENNY");
				System.out.println(" - MSNCONTACTLIST");
				System.out.println(" - MSNSTATUS");
				System.out.println(" - QUIT");
			}
			else if(r.equals("QUIT")) {
				//icq.logout();
				//icq.shutdown();
				//msn.shutdown();
				//fUpdate.interrupt();
				//this.interrupt();
				quit = true;

			} 
			}
			catch(IOException e) {
				System.out.println(e.getMessage());	
			} catch (MSNException e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			//System.out.println("user");
			try {
				//System.out.println("slaap");
				sleep(300);
			}
			catch(InterruptedException e) {
			
			}
		}
		System.exit(0);
	}
	
	/**
		 * @author Bart Van Rompaey
		 * @version $Revision: 1.6 $
		 * @date $Date: 2003/05/25 11:26:51 $
		 * 
		 * class that talks to the Gui and passes
		 * information about changes in the model
		 */
	class UpdateUser extends Thread {
		private DataBuffer fICQData;
		//private Buffer fMSNData;
		private DataBuffer fMSNData;
		private DataBuffer fYahooData;
		
		public UpdateUser(DataBuffer icq, /*Buffer*/DataBuffer msn, DataBuffer yahoo) {
			fICQData = icq;
			fMSNData = msn;
			fYahooData = yahoo;
		}
		
		public void run() {
			while(true) {				
				if(fICQData.ExistsUserData()) {
					System.out.print("ICQ: ");
					System.out.println(fICQData.getToUser());
				}
				if(fICQData.ExistsDump()) {
					String output = fICQData.getDump();
					if(fDump) {
						System.out.println(output);
					}
				}
				if(fYahooData.ExistsUserData()) {
					System.out.print("YAHOO: ");
					System.out.println(fYahooData.getToUser());
				}
				if(fYahooData.ExistsDump()) {
					String output = fYahooData.getDump();
					if(fDump) {
						System.out.println(output);
					}
				}
				if (fMSNData.ExistsUserData()) {
					String s = fMSNData.getToUser();
					if (! s.equals("")) {
						System.out.println("MSN: " + s);
					}
				}
				if (fMSNData.ExistsDump()) {
					String s = fMSNData.getDump();
					if (fDump) {
						if (! s.equals("")) {
							System.out.println(s);
						}
					}
				}
				try {
					sleep(300);
				}
				catch(InterruptedException e) {
				
				}
			}	
		}
	}
	
	
	/**
	 * Method main.
	 * @param args
	 */
	public static void main(String[] args) {
		Mess m = new Mess();
	}	
}
