import org.junit.TestCase;

public class MySecondTest extends TestCase {
	private MyUUT uut;

	public void testStuff() {
		assertEquals("a", uut.toString());
		assertEquals("b", uut.toString());
	}
}
