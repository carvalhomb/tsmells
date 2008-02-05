import org.junit.TestCase;

public class MyTest extends TestCase {

	private MyUUT uut = null;

	public void setUp() {
		this.uut = new MyUUT();
	}

	public void testCommand() {
		assertTrue(true);
		assertTrue("true", true);
		this.assertEquals("description", "", "");
		this.assertNull(null);
		this.assertFalse("description", false);
		assertSame(new Integer(1), new Integer(1));
		this.assertNotNull("description", new Integer(1));
		this.assertEquals(1, 2);

	}

	public void testMore() {
		this.assertEquals(1,1);
		this.assertEquals(2,2);
		this.assertEquals(3,3);
		this.assertEquals(4,4);
	}

}
