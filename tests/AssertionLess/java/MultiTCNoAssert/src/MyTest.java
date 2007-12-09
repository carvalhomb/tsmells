import org.junit.TestCase;

public class MyTest extends TestCase {

	private MyUUT uut = null;

	public void setUp() {
		this.uut = new MyUUT();
	}

	public void testCommand() {
		uut.myMethod();
		uut.mySecond();
	}

	public void testMore() {
		uut.myMethod();
	}
}
