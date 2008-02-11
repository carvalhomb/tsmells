import org.junit.TestCase;

public class MyTest extends TestCase {
	Uut1 u1 = new Uut1();
	Uut2 u2 = new Uut2();
	Uut3 u3 = new Uut3();
	Uut4 u4 = new Uut4();
	Uut5 u5 = new Uut5();

	public void testOne() {
		u1.go();
		u2.go();
		u3.go();
		u4.go();
		u5.go();

		u1.go();
		u2.go();
		u3.go();
		u4.go();
		u5.go();
	}
}
