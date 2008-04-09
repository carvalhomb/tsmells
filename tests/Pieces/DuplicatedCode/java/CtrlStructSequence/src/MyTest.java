import org.junit.TestCase;

public class MyTest extends TestCase {
	private Uut u = new Uut();

	public void testOne() {
		u.a();
		for (int i=0; i<5; i++) {
			u.b();
			u.c();
		}
		u.d();
		u.e();
	}

	public void testTwo() {
		u.a();
		u.b();
		u.c();
		u.d();
		u.e();
	}
}