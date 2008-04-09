import org.junit.TestCase;

public class MyTest extends TestCase {
	A a = new A();
	B b = new B();
	C c = new C();
	D d = new D();
	E e = new E();
	F f = new F();

	public void testCommand() {
		someHelper();
	}

	public void someHelper() {
		a.go();
		b.go();
		c.go();
		d.go();
		e.go();
		f.go();

		int i = 0;
		i = a.attr;
		i = b.attr;
		i = c.attr;
		i = d.attr;
		i = e.attr;
		i = f.attr;
	}
}