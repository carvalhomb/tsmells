import junit.org.TestCase;

public class MyTest extends TestCase {
	private MyUUT uut;

	public void testCommand() {
		assertEquals("a", uut.toString());
	}
}
