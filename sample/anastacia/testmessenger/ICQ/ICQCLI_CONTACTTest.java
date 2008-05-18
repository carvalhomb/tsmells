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

package testmessenger.ICQ;
import junit.framework.*;


/**
 * @author Bart Van Rompaey
 * @version $Revision: 1.1 $
 * @date $Date: 2003/04/04 18:02:14 $
 * 
 * 
 */
public class ICQCLI_CONTACTTest extends TestCase {
	protected String fMess1;
	protected String fMess2;

	protected void setUp() {
		fMess1 = "ahoi!";
		fMess2 = "hello!";
	}
	public static Test suite() {

		/*
		 * the type safe way
		 */
		/*TestSuite suite= new TestSuite();
		
		suite.addTest(
			new testICQMessage("message") {
				 protected void runTest() { testMessage(); }
			}
		);*/
	/*
		suite.addTest(
			new SimpleTest("testDivideByZero") {
				 protected void runTest() { testDivideByZero(); }
			}
		);*/
		/*return suite;*/
		

		/*
		 * the dynamic way
		 */
		return new TestSuite(ICQCLI_CONTACTTest.class);
	}
	public void testGetUIN() {
		
	}
	
	public void testGetNick() {
		
	}
	
	public void testGetIP() {
		
	}

	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
}
