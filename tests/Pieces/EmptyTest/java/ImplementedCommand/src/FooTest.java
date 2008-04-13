import org.junit.TestCase;

public class FooTest extends TestCase {
	UUT uut = new UUT();
	public void testFoo() {
		uut.foo();
		uut.bar(); 
		uut.baz();
	}
}
