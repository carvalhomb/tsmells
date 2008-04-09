import org.junit.TestCase;

public class MyTest extends TestCase {
	Uut u = new Uut();

	public void testOne() {
		assertEquals(1, 1);
		assertEquals(2, 2);
		assertEquals(3, 3);
		assertEquals(4, 4);
		assertEquals(5, 5);
		assertEquals(6, 6);
		assertEquals(7, 7);
	}

	public void testTwo() {
		assertEquals(7, 7);
		assertEquals(6, 6);
		assertEquals(5, 5);
		assertEquals(4, 4);
		assertEquals(3, 3);
		assertEquals(2, 1);
		assertEquals(1, 1);
	}

}
