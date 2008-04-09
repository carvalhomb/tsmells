import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {

    public void testCommand() {
        A a = new A();
        B b = new B();
        int i;
        i = a.a;
        b.b();
    }

}
