import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {

    public void testCommand() {
        A a = new A();
        B b = new B();
        C c = new C();
        Integer i;

        a.a();
        i = b.b;
        c.c();
        assertEquals(1, 1);
    }

}
