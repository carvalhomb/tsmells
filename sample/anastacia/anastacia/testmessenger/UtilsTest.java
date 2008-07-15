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

package testmessenger;
import junit.framework.*;
import messenger.Utils;
 
/*
 * @author Bart Van Rompaey
 * @author Benny Van Aerschot
 * @date 08/10/02
 */
public class UtilsTest extends TestCase {
	Utils u;
	
	protected void setUp() {
		u = new Utils();
	}
	public static Test suite() {
		return new TestSuite(UtilsTest.class);
	}
	
	public void testCharToNibble() {
	
	}
	
	public void testFromHexString() {
		// sample variables
		int a = 0;
		int b = 1;
		int c = 15;
		int d = 16;
		int e = 17;
		int f = 42;
		int g = 1;
		int h = 87;
		int i = 102;
		int j = 50;
		String s1 = "00";
		String s2 = "01";
		String s3 = "0f";
		String s4 = "10";
		String s5 = "11";
		String s6 = "2A015766";
		String s7 = "32";	
		byte[] br = new byte[50];
		
		br = Utils.fromHexString(s1);
		assertTrue((int)(br[0]) == a);
	
		br = Utils.fromHexString(s2);
		assertTrue((int)(br[0]) == b);
		
		br = Utils.fromHexString(s3);
		assertTrue((int)(br[0]) == c);
		
		br = Utils.fromHexString(s4);
		assertTrue((int)(br[0]) == d);
		
		br = Utils.fromHexString(s5);
		assertTrue((int)(br[0]) == e);
		
		br = Utils.fromHexString(s6);
		assertTrue((int)(br[0]) == f);
		assertTrue((int)(br[1]) == g);
		assertTrue((int)(br[2]) == h);
		assertTrue((int)(br[3]) == i);
		
		br = Utils.fromHexString(s7);
		assertTrue((int)(br[0]) == j);
	
	}
	
	
	public void testHexFromString() {

	}
	
	public void testHexToInt() {
		
	}
	
	public void testHexToByte() {
	
	}
	
	public void testByteToHex() {
	
	}
	
	public void testIntToHex() {
		int a = 0;
		int b = 1;
		int c = 15;
		int d = 16;
		int e = 17;
		int f = 1500;
		int g = -50;
		String s1 = "00";
		String s2 = "01";
		String s3 = "0f";
		String s4 = "10";
		String s5 = "11";
		String s6 = "5dc";
		String s7 = "32";
		
		assertTrue(Utils.intToHex(a).equals(s1));
		assertTrue(Utils.intToHex(b).equals(s2));
		assertTrue(Utils.intToHex(c).equals(s3));
		assertTrue(Utils.intToHex(d).equals(s4));
		assertTrue(Utils.intToHex(e).equals(s5));
		assertTrue(Utils.intToHex(f).equals(s6));	
	}
	
	public void testTwoByteInt2FourCharString() {
		int a = 5;
		int b = 16;
		int c = 256;
		int d = 4369;
		
		int f1 = -50;
		int f2 = 70000;
		
		String s1 = "0005";
		String s2 = "0010";
		String s3 = "0100";
		String s4 = "1111";
		
		assertTrue(Utils.TwoByteInt2FourCharString(a).equals(s1));
		assertTrue(Utils.TwoByteInt2FourCharString(b).equals(s2));
		assertTrue(Utils.TwoByteInt2FourCharString(c).equals(s3));
		assertTrue(Utils.TwoByteInt2FourCharString(d).equals(s4));
		
		this.assertFalse(Utils.TwoByteInt2FourCharString(f1).equals(s4));
		this.assertFalse(Utils.TwoByteInt2FourCharString(f2).equals(s4));
	}
	public void testEncryptPass() {
		
	}
	
	public void testUIN() {
	
	}
	
	public void  testHexString2AsciiString(String s) {
		assertTrue(Utils.HexString2AsciiString("3532343432373739").equals("52442779"));		
	}
	
	public void testURLQuoted2PlainText(String s) {
		assertTrue(Utils.URLQuoted2PlainText("Microsoft%20Sans%20Serif").equals("Microsoft Sans Serif"));
	}
	

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
