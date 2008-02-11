import org.junit.TestCase;

public class MyTest extends TestCase {
	Uut u = new Uut();

	public void firstCom() {
		u.a();
		u.b();
	}

	public void secondCom() {
		u.a();
		u.b();	
	}
}
