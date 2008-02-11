import org.junit.TestCase;

public class MyTest extends TestCase {
	Uut u = new Uut();

	public void testOne() {
		u.a(); u.c(); u.e(); u.b(); u.d();

		u.a();
		u.c();
		u.e();
		u.b();
		u.d();
	}

}
