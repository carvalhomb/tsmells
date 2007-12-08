import junit.org.TestCase;

public class MyTest extends TestCase {

	private MyUUT uut = null;

	public void setUp() {
		this.uut = new MyUUT();
	}

	public void testCommand() {
		uut.myMethod();
		uut.mySecond();
		helper();
	}

	private void helper() {
	}
}
