import junit.org.TestCase;

public class MyTest extends TestCase {

	private MyUUT uut = null;

	public void setUp() {
		this.uut = new MyUUT();
	}

	public void testMyCommand() {
		this.assertEquals("", uut.myMethod());
		this.assertEquals(new Integer(0), uut.mySecond());
		this.assertEquals("", uut.myMethod());
		this.assertEquals(new Integer(0), uut.mySecond());
		this.assertEquals("", uut.myMethod());
		this.assertEquals(new Integer(0), uut.mySecond());
	}

}
