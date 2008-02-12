import org.junit.TestCase;

public class MyTest extends TestCase {
	private Uut u = new Uut();
	public void testOne() {
		int i = u.go(u.go(1, 2), u.go(2, 3));
		int j = u.so(u.so(1, 2, 3), u.so(2, 3, 4), u.so(3, 4, 5));
	}
	public void testTwo() {
		int i = u.go(u.go(9, 8), u.go(8, 7));
		int j = u.so(u.so(9, 8, 7), u.so(8, 7, 6), u.so(7, 6, 5));
	}
}
