import org.junit.TestCase;

public class MyTest extends TestCase {

	private MyUUT uut = null;

	public void setUp() {
		this.uut = new MyUUT();
	}

	public void testCommand() {
		uut.myMethod();
		helper();
	}

	private void helper() {
		assertTrue(true);
	}
}
