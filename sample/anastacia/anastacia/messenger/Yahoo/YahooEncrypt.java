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

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class needed for authentication with the Yahoo server
 * 
 * @author Benny Van Aerschot
 * @version  $Revision: 1.2 $
 * @date $Date: 2003/04/07 13:59:01 $
 */ 
public class YahooEncrypt {
	
	// inner class needed for the MAC64 encoding of the Yahoo strings
	
	/* BASE64 encoding encodes 3 bytes into 4 characters.
	   |11111122|22223333|33444444|
	   Each set of 6 bits is encoded according to the
	   toBase64 map. If the number of input bytes is not
	   a multiple of 3, then the last group of 4 characters
	   is padded with one or two = signs. Each output line
	   is at most 76 characters.
	*/
	
	class Base64OutputStream extends FilterOutputStream {
		private int col = 0;
		private int i = 0;
		private int[] inbuf = new int[3];
		
		// MAC64 encoding
	 	private char[] toBase64 =
	 	{  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
	      'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
	      'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
	      'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
	      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
	      'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
	      'w', 'x', 'y', 'z', '0', '1', '2', '3',
	      '4', '5', '6', '7', '8', '9', '.', '_'
		};
		
		public Base64OutputStream(OutputStream out) {
			super(out);
		}
	
	   public void write(int c) throws IOException {
			inbuf[i] = c;
			i++;
			if (i == 3) {
				super.write(toBase64[(inbuf[0] & 0xFC) >> 2]);
				super.write(toBase64[((inbuf[0] & 0x03) << 4) | ((inbuf[1] & 0xF0) >> 4)]);
				super.write(toBase64[((inbuf[1] & 0x0F) << 2) | ((inbuf[2] & 0xC0) >> 6)]);
				super.write(toBase64[inbuf[2] & 0x3F]);
				col += 4;
				i = 0;
				if (col >= 76) {
					super.write('\n');
	            	col = 0;
	            }
			}
		}
	
		public void flush() throws IOException {
			if (i == 1) {
				super.write(toBase64[(inbuf[0] & 0xFC) >> 2]);
				super.write(toBase64[(inbuf[0] & 0x03) << 4]);
				super.write('-');
				super.write('-');
			} else if (i == 2) {
				super.write(toBase64[(inbuf[0] & 0xFC) >> 2]);
	         	super.write(toBase64[((inbuf[0] & 0x03) << 4) | ((inbuf[1] & 0xF0) >> 4)]);
				super.write(toBase64[(inbuf[1] & 0x0F) << 2]);
				super.write('-');
			}
	   }
	
	}

	public String base64Encode2(byte[] s) {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		Base64OutputStream out = new Base64OutputStream(bOut);
		try {
			out.write(s);
			out.flush();
		} catch (IOException exception) {
			assert(false);
			// so be it
		}
		
		return bOut.toString();
	}

    static final char[] b64t = {  '.', '/', '0', '1', '2', '3',
      '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
      'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
      'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
      'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
      'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
      'w', 'x', 'y', 'z'
   };

    static final String md5_salt_prefix = "$1$";
    public String c = null;
    //int buflen = 0;
    //char[] buffer = null;

    public int Min (int a, int b) {
	  if (a < b) {
		return a;
	  } else {
		return b;
	  }
    }

    public int Max (int a, int b) {
	  if (a > b) {
		return a;
	  } else {
		return b;
	  }
    }


    public void b64_from_24bit(byte B2, byte B1, byte B0, int n) {
	  int w = ((B2 & (0xFF)) << 16) | ((B1 & (0xFF)) << 8) | (B0 & (0xFF));
	  while (n-- > 0 /*&& buflen > 0*/) {
		 c += b64t[w & 0x3f];
		 //--buflen;
		 w >>= 6;
	 }
    }

    public String yahooCrypt(String key, String salt) {
	  byte[] alt_result;
	  int salt_len;
	  int key_len;
	  int cnt;

	  if (salt.substring(0,3).equals(md5_salt_prefix)) {
		salt = salt.substring(3);
	  }

	  salt_len = Min(salt.indexOf('$'), 8);	// salt.indexOf('$') is 8
	  key_len = key.length();
	  
	  try {
		MessageDigest ctx = MessageDigest.getInstance("MD5");
		ctx.reset(); // empty the buffer
		ctx.update(key.getBytes(), 0, key_len);
		ctx.update(md5_salt_prefix.getBytes(), 0, md5_salt_prefix.length());
		ctx.update(salt.getBytes(), 0, salt_len);

		MessageDigest alt_ctx = MessageDigest.getInstance("MD5");
		alt_ctx.reset(); // empty the buffer
		alt_ctx.update(key.getBytes(), 0, key_len);
		alt_ctx.update(salt.getBytes(), 0, salt_len);
		alt_ctx.update(key.getBytes(), 0, key_len);
		
		alt_result = alt_ctx.digest();		// length of alt_result is 16

		/* Add for any character in the key one byte of the alternate sum.  */
		for (cnt = key_len; cnt > 16; cnt -= 16)
			ctx.update(alt_result, 0, 16);
	     ctx.update(alt_result, 0, cnt);

		alt_result[0] = '\0';

		/* The original implementation now does something weird: for every 1
	   bit in the key the first 0 is added to the buffer, for every 0
	   bit the first character of the key.  This does not seem to be
	   what was intended but we have to follow this to be compatible.  */
		for (cnt = key_len; cnt > 0; cnt >>= 1) {
		   ctx.update((cnt & 1) != 0 ? alt_result : key.getBytes(), 0, 1);
		}
		

		alt_result = ctx.digest();


		/* Now comes another insane loop.  In fear of password crackers here
	   comes a quite long loop which just processes the output of the
	   previous round again.  We cannot ignore this here.  */
		for (cnt = 0; cnt < 1000; ++cnt) {
		    /* New context.  */
		    ctx.reset(); // empty the buffer
    
		    /* Add key or last result.  */
		    if ((cnt & 1) != 0)
			    ctx.update(key.getBytes(), 0, key_len);
		    else
			    ctx.update(alt_result, 0, 16);
    
		    /* Add salt for numbers not divisible by 3.  */
		    if (cnt % 3 != 0)
			    ctx.update(salt.getBytes(), 0, salt_len);
    
		    /* Add key for numbers not divisible by 7.  */
		    if (cnt % 7 != 0)
			    ctx.update(key.getBytes(), 0, key_len);
    
		    /* Add key or last result.  */
		    if ((cnt & 1) != 0)
			    ctx.update(alt_result, 0, 16);
		    else
			    ctx.update(key.getBytes(), 0, key_len);
    
		    /* Create intermediate result.  */
		    alt_result = ctx.digest();
	    }


	    c = md5_salt_prefix;
	    c += salt;
	    //buflen = 30;

	    b64_from_24bit (alt_result[0], alt_result[6], alt_result[12], 4);
	    b64_from_24bit (alt_result[1], alt_result[7], alt_result[13], 4);
	    b64_from_24bit (alt_result[2], alt_result[8], alt_result[14], 4);
	    b64_from_24bit (alt_result[3], alt_result[9], alt_result[15], 4);
	    b64_from_24bit (alt_result[4], alt_result[10], alt_result[5], 4);
	    b64_from_24bit ((byte) 0, (byte) 0, alt_result[11], 2);

	    return c;

	  } catch (NoSuchAlgorithmException e) {
		assert(false);
		return "";
	  }
    }


    public String[] encrypt(String receivedAscii, String pass, String login) {     
	char checksum;
      int sv;
      String passwordHash = null;
      String passwordCryptHash = null;
      String resultAscii1 = null;
	String resultAscii2 = null;
      byte[] res = null;
      byte[] res3 = null;

      sv = receivedAscii.charAt(15);
	sv = sv % 8;

	try {
		MessageDigest md1 = MessageDigest.getInstance("MD5");
		md1.update(pass.getBytes(), 0, pass.length());
		res = md1.digest();
		passwordHash = base64Encode2(res);

		MessageDigest md2 = MessageDigest.getInstance("MD5");
		pass = yahooCrypt(pass, "$1$_2S43d5f$");
		md2.update(pass.getBytes(), 0, pass.length());
		res = md2.digest();
		passwordCryptHash = base64Encode2(res);
	

	switch (sv) {
	    case 1:
	    case 6:
		 checksum = receivedAscii.charAt(receivedAscii.charAt(9) % 16);
		 resultAscii1 = checksum + login + receivedAscii + passwordHash;
		 resultAscii2 = checksum + login + receivedAscii + passwordCryptHash;
		 break;
	    case 2:
	    case 7:
		 checksum = receivedAscii.charAt(receivedAscii.charAt(15) % 16);
		 resultAscii1 = checksum + receivedAscii + passwordHash + login;
		 resultAscii2 = checksum + receivedAscii + passwordCryptHash + login;
		 break;
	    case 3:
		 checksum = receivedAscii.charAt(receivedAscii.charAt(1) % 16);
		 resultAscii1 = checksum + login + passwordHash + receivedAscii;
		 resultAscii2 = checksum + login + passwordCryptHash + receivedAscii;    
		 break;
	    case 4:
		 checksum = receivedAscii.charAt(receivedAscii.charAt(3) % 16);
		 resultAscii1 = checksum + passwordHash + receivedAscii + login;
		 resultAscii2 = checksum + passwordCryptHash + receivedAscii + login;
		 break;
	    case 0:
	    case 5:
		 checksum = receivedAscii.charAt(receivedAscii.charAt(7) % 16);
		 resultAscii1 = checksum + passwordHash + login + receivedAscii;
		 resultAscii2 = checksum + passwordCryptHash + login + receivedAscii;
		 break;
	}  
	  

	  MessageDigest md3 = MessageDigest.getInstance("MD5");
	  md3.reset();
	  md3.update(resultAscii1.getBytes(), 0, resultAscii1.length()); 
	  res3 = md3.digest();
	  String result1 = base64Encode2(res3);
	  
	  md3.reset();
	  md3.update(resultAscii2.getBytes(), 0, resultAscii2.length()); 
	  res3 = md3.digest();
	  String result2 = base64Encode2(res3);

	  String[] resultStrings = { result1, result2 };

	  return resultStrings;

      } catch (NoSuchAlgorithmException e) {
      	assert(false);
		return null;
      }

    }
}
    
 