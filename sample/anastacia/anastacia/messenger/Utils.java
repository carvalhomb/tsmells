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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Bart Van Rompaey & Benny Van Aerschot
 * @version $Revision: 1.4 $
 * @date $Date: 2003/04/16 17:02:53 $
 * 
 */
public class Utils {
	private byte[] message;
	private byte[] message2;
	private byte[] header;
	
	/**
	 * convert a single char to corresponding nibble.
	 *
	 * @param c char to convert. must be 0-9 a-f A-F, no
	 * spaces, plus or minus signs.
	 *
	 * @return corresponding integer
	*/
	private static int charToNibble ( char c )
	{
	if ( '0' <= c && c <= '9' )
	{
	return c - '0' ;
	}
	else if ( 'a' <= c && c <= 'f' )
	{
	return c - 'a' + 0xa ;
	}
	else if ( 'A' <= c && c <= 'F' )
	{
	return c - 'A' + 0xa ;
	}
	else
	{
	throw new IllegalArgumentException ( "Invalid hex character: " + c ) ;
	}
	}

	/**
	 * code from: http://mindprod.com/jglosshex.html
	 * Last updated 2003-02-19 by Roedy Green ©1996-2003 Canadian Mind Products 
	 * Convert a hex string to a byte array.
	 * Permits upper or lower case hex.
	 *
	 * @param s String must have even number of characters.
	 * and be formed only of digits 0-9 A-F or
	 * a-f. No spaces, minus or plus signs.
	 * @return corresponding byte array.
	 */

	public static byte[] fromHexString ( String s ) {
		assert s.length()%2 == 0; // hexadecimal string always has even number of chars
		assert s != null;
		
		
		int stringLength = s.length() ;
		
		//System.out.println("fromHexString: "+s+" lengte: "+s.length());
	
		if ( (stringLength & 0x1) != 0 ) {
			throw new IllegalArgumentException ( "fromHexString requires an even number of hex characters" );
		}
	
		byte[] b = new byte[ stringLength / 2 ];
	
		for ( int i=0 ,j= 0; i< stringLength; i+= 2,j ++ )
		{
			int high= charToNibble(s.charAt ( i ));
			int low = charToNibble( s.charAt ( i+1 ) );
			b[ j ] = (byte ) ( ( high << 4 ) | low );
		}
		
		return b;
	} 
	
	public static String HexFromString(String message) {	
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		String blah = "";
		
		//System.out.println("HexFromString: "+message+" lengte: "+message.length());
		
		for(int i = 0; i < message.length(); i++) {
			
			Integer v = new Integer((int)(message.charAt(i)));
			//System.out.println(message.charAt(i)+" "+v+" "+v.toHexString(v.intValue())+" ");
			if(v.intValue() < 16) {
				blah += "0";
				blah += Integer.toHexString(v.intValue());
			}
			else {
				blah += Integer.toHexString(v.intValue());
			}			
		}
		/*System.out.println();
		System.out.println();*/
		
		/* each character is one byte, one byte is represented by 
		 * two hex characters (0-9A-F)
		 */
		assert blah.length() == 2*message.length();
		return blah;
	}
	
	public static int hexToInt ( String value ) {
	    value = value.toUpperCase();
	    int res = 0;
	
	    for ( int i=0; i<value.length(); i++ ) {
	         int n = (int) value.charAt ( i );
	         n = n - 48;
	         if ( n > 9 ) {
	              n = n - 7;
	         }
	              
	         res = ( res * 16 ) + n;
	    }
	    return res;
	}

	public static byte hexToByte ( String value ) {
	    value = value.toUpperCase();
	    int res = 0;
	
	    for ( int i=0; i<value.length(); i++ ) {
	         int n = (int) value.charAt ( i );
	         n = n - 48;
	         if ( n > 9 ) {
	              n = n - 7;
	         }
	              
	         res = ( res * 16 ) + n;
	    }
	    return (byte)res;
	}	
	
	/**
	* Convenience method to convert a byte array to a hex string.
	*
	* @param data the byte[] to convert
	* @return String the converted byte[]
	*/
	public static String byteToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for ( int i = 0; i < data.length; i++ ) {
			buf.append( byteToHex(data[i]) );
		}
		return(buf.toString());
	}
	
	 /**
	* Convenience method to convert a byte to a hex string.
	*
	* @param data the byte to convert
	* @return String the converted byte
	*/
	public static String byteToHex(byte data)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data>>>4)&0x0F));
		buf.append(toHexChar(data&0x0F));
		return buf.toString();
	}
	
	
	 /**
	* Convenience method to convert an int to a hex char.
	*
	* @param i the int to convert
	* @return char the converted char
	*/
	public static char toHexChar(int i)
	{
		if ((0 <= i) && (i <= 9 ))
			return (char)('0' + i);
		else
			return (char)('a' + (i-10));
	}
//--------------------------------------------end just geadd ------------------------------	


	/*
	public static String byteToHex (byte b) {
		return intToHex(new Byte(b).intValue());
	}
	
	public static String byteToHex(byte b[]) {
		String s = "";
		for(int i = 0; i < b.length; i++) {
			s += byteToHex(b[i]);
		}
		return s;
	}*/

	public static String intToHex (int value) {
		Integer i = new Integer(value);
		if(value < 16) {
			return ("0"+ Integer.toHexString(value));
		}
		else {
			return Integer.toHexString(value);
		}	
	}
	
	// F3,26,81,C4,39,86,DB,92,71,A3,B9,E6,53,7A,95,7C
	public static String ICQEncryptPass(String pass) {
		StringBuffer sb = new StringBuffer();
		short[] c = {0xF3,0x26,0x81,0xC4,0x39,0x86,0xDB,0x92,0x71,0xA3,0xB9,0xE6,0x53,0x7A,0x95,0x7C};
		char[] temp = pass.toCharArray();
		//System.out.println("pass: "+pass+" lengte: "+pass.length());
		for (int i = 0; i < temp.length; i++) {
			temp[i] ^= c[i];
			sb.append(temp[i]);			
		}
		//System.out.println("ICQEncryptPass: "+sb+" lengte: "+sb.length());
		return HexFromString(sb.toString());
	}
	
	public static String MD5Encrypt(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset(); // empty the buffer
			md.update(s.getBytes());
			byte[] digest = md.digest();

			return byteToHex(digest);
		} catch (NoSuchAlgorithmException e) {
			assert(false);
			//System.out.println(e.getMessage());
		}
		
		return "";
	}
	
	
	public static String TwoByteInt2FourCharString(int i) {

		// soms negatieve getallen?? needs research 
		// assert (i  >=  0)&&(i < 65536);
		String s = "";
		
		if(i < 256) {
			s = "00"+intToHex(i);
		}
		else if(i < 4096) {
			s = "0"+intToHex(i);
		}
		else {
			s = intToHex(i);
		}
		
		//assert s.length() == 4;
		return s;			
	}
	
	public static int HexChar2Int(char ch) throws IllegalConversionException {
		if(ch == '0') { return 0; }
		else if(ch == '1') { return 1; }
		else if(ch == '2') { return 2; }
		else if(ch == '3') { return 3; }
		else if(ch == '4') { return 4; }
		else if(ch == '5') { return 5; }
		else if(ch == '6') { return 6; }
		else if(ch == '7') { return 7; }
		else if(ch == '8') { return 8; }
		else if(ch == '9') { return 9; }
		else if((ch == 'A')||(ch == 'a')) { return 10; }
		else if((ch == 'B')||(ch == 'b')) { return 11; }
		else if((ch == 'C')||(ch == 'c')) { return 12; }
		else if((ch == 'D')||(ch == 'd')) { return 13; }
		else if((ch == 'E')||(ch == 'e')) { return 14; }
		else if((ch == 'F')||(ch == 'f')) { return 15; }
		else {
			throw new IllegalConversionException("illegal hexchar");
		}		
	}
	
	public static String HexString2AsciiString(String s) {
		String result = "";
		for(int i = 0; i < s.length()-1; i+=2) { // een letter per 2 hexchars
			int value = 0;
			for(int j = 0; j < 2; j++) {
				if(j == 0) {
					try {
						value = 16*HexChar2Int(s.charAt(i));
					}
					catch(IllegalConversionException e) {
						// blah
					}			
				}
				else {
					try {
						value += HexChar2Int(s.charAt(i+1));
					}
					catch(IllegalConversionException e) {
						// blah
					}			
				} 
			}	
			result += (char)value;
		}
		return result;
	}
	
	public static String UIN(String uin) {
		assert uin != null;
		
		String result = "" ;
		int i = 0;
		while (i < uin.length()) {
			result += "3" + uin.charAt(i);
			++i;
		}
		
		assert 2*uin.length() == result.length();
		return result;
	} 

	public static String URLQuoted2PlainText(String s) {
		String res = s;
		int index = res.indexOf("%");
		
		while (index != -1) {
			String i = res.substring(index+1, index+3);
			char ascii = (char)hexToInt(i);
			res = res.substring(0, index) + ascii + res.substring(index+3);
			index = res.indexOf("%");
		} 

		return res;
	}
	
	public static String AsciiString2HexString(String s) {
		String result = "";
		for(int i = 0; i < s.length(); i++) {
			int j = (int)s.charAt(i);
			result += intToHex(j);
			/*System.out.print(s.charAt(i)+" "+j+" "+intToHex(j));*/
		}
	
		return result;
	}
	
	// maakt de HexString op tot een "printable" string
	public static String printableHexString(String hex) {
		String s = "";
		int i = 1;
		int h = 1;
		while(i < hex.length()) {
			s += hex.charAt(i-1);
			s += hex.charAt(i);
			s += " ";
			if(h%16 == 0) {
				s += ("\n");
			}
			else if(h%8 == 0) {
				s += (" ");
			}
			i = i + 2;
			h++;
		}
		s += ("\n");
		return s;
	}
	
}
