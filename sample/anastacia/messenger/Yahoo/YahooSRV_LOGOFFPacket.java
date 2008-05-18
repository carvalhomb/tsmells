package messenger.Yahoo;

import messenger.Utils;

/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.2 $
 * @date $Date: 2003/05/04 18:24:08 $
 * 
 * packet sent by server when a buddy goes offline
 */
public class YahooSRV_LOGOFFPacket extends YahooPacket {
	private String fContact = "";
	private String fStatus = "";
		
	public YahooSRV_LOGOFFPacket (String s) {
		setContent(Utils.fromHexString(s));		
		setKindOfPacket("SRV_LOGOFF");
	
		analyze();
	
	}
	
	/**
	 * Method getContact.
	 * @return String contact
	 */
	public String getContact() {
		return fContact;	
	}

	/**
	 * Method getStatus.
	 * @return String users' new status
	 */
	public String getStatus() {
		return fStatus;	
	}
	
	
	private void analyze() {
		int i = 0;
		String s = getContentHex(), content = "";
	
		while(((i+7) < s.length())&&!((s.charAt(i) == '3')&&(s.charAt(i+1) == '7')&&(s.charAt(i+2) == 'c')&&
					(s.charAt(i+3) == '0')&&(s.charAt(i+4) == '8')&&(s.charAt(i+5) == '0'))) {		
			i++;	
		}
		i+=6;
	
		while((i+3 < s.length())&&!((s.charAt(i) == 'c')&&(s.charAt(i+1) == '0')&&(s.charAt(i+2) == '8')&&(s.charAt(i+3) == '0'))) {
				content += s.charAt(i);	
				i++;
		}
		fContact = Utils.HexString2AsciiString(content);
		fStatus = "YOFFLINE";
	
		//System.out.println(fContact+" goes "+fStatus);
	}
}
