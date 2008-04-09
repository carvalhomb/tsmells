package test;

import org.junit.TestCase;
import baz.Baz;

public class BazTest extends TestCase {
	private Baz baz;

	public void setUp() {
		baz = new Baz();
	}

	public void testInt() {
		assertEquals(2, baz.baz);
	}
}
