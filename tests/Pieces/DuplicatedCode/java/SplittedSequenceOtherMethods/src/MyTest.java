import org.junit.TestCase;

public class MyTest extends TestCase {
	Uut u = new Uut();

	public void testOne() {
		u.a();
		u.b();
		u.c();
		u.d();
		u.e();
		u.e();
		u.d();
		u.c();
		u.b();
		u.a();
	}

	public void testTwo() {
		u.a();
		u.b();
		u.c();
		u.d();
		u.e();
	}

	public void testThree() {
		u.e();
		u.d();
		u.c();
		u.b();
		u.a();
	}

}
