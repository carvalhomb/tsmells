package foo;

import org.junit.TestCase;

public class FooTest extends TestCase {

	public void testAdd() {
		Foo f = new Foo();
		assertEquals("failed to add 2 ints", 2, f.add(1,1));
	}

	public void testMult() {
		Foo f = new Foo();
		assertEquals("failed to mult 2 ints", 1, f.mult(1,1));
	}

}
