import org.junit.TestCase;

public class BarTest extends TestCase {

	Bar bar = new Bar();

	public void setUp() {
		bar = new Bar();
	}

	public void tearDown() {
	}
	
	public void helper() {
	}

	public void testCompute() {
		assertTrue(bar.compute());
	}

}
