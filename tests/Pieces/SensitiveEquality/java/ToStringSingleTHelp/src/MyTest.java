import org.junit.TestCase;

public class MyTest extends TestCase {
	private MyUUT uut;

	public void testCommand() {
        this.helper();
	}

    public void helper() {
        assertEquals("a", uut.toString());
    }
}
