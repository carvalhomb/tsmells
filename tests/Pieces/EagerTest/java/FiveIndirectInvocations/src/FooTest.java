import org.junit.TestCase;

public class FooTest extends TestCase {

	Bar bar;
	Baz baz;
	Fal fal;
	Foo foo;
	Fuu fuu;

	public void someHelper() {
		bar.bar();
		baz.baz();
		fal.fal();
		foo.foo();
		fuu.fuu();
	}

	public void testFoo() {
		someHelper();
	}
}
