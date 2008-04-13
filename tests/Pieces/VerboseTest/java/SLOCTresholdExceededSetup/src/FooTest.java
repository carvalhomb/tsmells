import org.junit.TestCase;

public class FooTest extends TestCase {
	private UUT uut;

	public void setUp() {
		uut.bar();
		uut.baz();
		uut.ful();
		uut.fuz();
		uut.git();

		uut.bar();
		uut.baz();
		uut.ful();
		uut.fuz();
		uut.git();

		uut.bar();
		uut.baz();
		uut.ful();
		uut.fuz();
		uut.git();

		uut.bar();
		uut.baz();
		uut.ful();
		uut.fuz();
		uut.git();
	}
}
