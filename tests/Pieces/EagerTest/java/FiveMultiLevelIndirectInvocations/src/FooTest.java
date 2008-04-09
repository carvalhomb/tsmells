import org.junit.TestCase;

public class FooTest extends TestCase {

	Bar bar;
	Baz baz;
	Fal fal;
	Foo foo;
	Fuu fuu;

	void helperLvl1() {
		helperLvl2();
	}

	void helperLvl2() {
		helperLvl3();
	}

	void helperLvl3() {
		bar.bar();
		baz.baz();
		fal.fal();
		foo.foo();
		fuu.fuu();
	}

	public void testFoo() {
		helperLvl3();
	}
}
