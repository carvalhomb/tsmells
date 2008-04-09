import org.junit.TestCase;

public class MyTest extends TestCase {

	private MyUUT uut = null;

	public void setUp() {
		this.uut = new MyUUT();
	}

	public void testCommand() {
		helper();
	}

	public void helper() {
		this.assertEquals("", uut.myMethod());
		this.assertEquals("", uut.myMethod());
		this.assertEquals("", uut.myMethod());
	}
}
