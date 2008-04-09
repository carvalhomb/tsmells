import org.junit.TestCase;

public class FooTest extends TestCase {

	Bar bar;
	Baz baz;
	Fal fal;
	Foo foo;
	Fuu fuu;



	void helperLvl1() {
		bar.bar();
		foo.foo();
		helperLvl2();
	}

	void helperLvl2() {
		baz.baz();
	}

	public void testFoo() {
		helperLvl1();
		fal.fal();
		fuu.fuu();
	}
}
