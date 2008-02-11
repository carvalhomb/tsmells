import org.junit.TestCase;

public class MyTest extends TestCase {
	Uut u = new Uut();

	public void testOne() {
		u.a();
		u.b();
		u.c();
		u.d();
		u.e();
		u.a();
		u.b();
		u.c();
		u.d();
		u.e();
	}
}
