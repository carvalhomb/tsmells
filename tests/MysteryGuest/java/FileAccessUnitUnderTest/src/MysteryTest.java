import org.junit.TestCase;

public class MysteryTest extends TestCase {
    MyUnitUnderTest myUUT = null;

	public void testSomething() {
		assertTrue(myUUT.myMethod("myConfig.cfg"));
	}
}
