import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {

    public void testCommand() {
        A a = new A();
        B b = new B();
        C c = new C();
        Integer i;

        a.testa();
        i = b.b;
        c.testc();
        assertEquals(1, 1);
    }

}
