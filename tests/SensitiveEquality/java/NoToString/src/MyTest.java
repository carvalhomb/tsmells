import org.junit.TestCase;

public class MyTest extends TestCase {
	public void testCommand() {
		MyUUT uut = new MyUUT();
		assertEquals(uut.getAttribute(), new Integer(5));
	}
}