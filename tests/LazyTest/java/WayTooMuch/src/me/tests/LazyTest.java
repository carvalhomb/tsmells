package me.tests;

import junit.framework.TestCase;
import me.lazy.MyUnitUnderTest;


/**
 * @author Manuel Breugelmans
 * @what Archetype lazy test
 */
public class LazyTest extends TestCase {

	MyUnitUnderTest myUUT;
	
	protected void setUp() throws Exception {
		myUUT = new MyUnitUnderTest();
	}
	
	protected void tearDown() throws Exception {
	}

	public void testStuff() {
		myUUT.stuff(1);
		//this.assertTrue(myUUT.stuff(1));
		this.assertTrue(true);
	}
	
	public void testStuffAgain() {
		assertTrue(myUUT.stuff(2));
	}
	
	public void testStuffAndAgain() {
		assertTrue(myUUT.stuff(3));
	}
	
	public void testMoreStuff() {
		assertTrue(myUUT.stuff(4));
	}
	
	public void testStuffOnceMore() {
		assertTrue(myUUT.stuff(5));
	}

	static public int aap() {
		int i = 0;
		return i;
	}
}