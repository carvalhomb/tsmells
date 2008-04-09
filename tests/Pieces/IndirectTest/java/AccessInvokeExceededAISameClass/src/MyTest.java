import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {
    public void testCommand() {
        A a = new A();
        B b = new B();
        C c = new C();

        Integer i;
        i = a.a;
        a.a();
        i = b.b;
        b.b();
        i = c.c;
        c.c();
    }
}
