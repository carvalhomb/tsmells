import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {

    private A a;
    private B b;
    private C c;
    private D d;
    private E e;
    private F f;

    public void testCommand() {
        int i;
        i = a.a;
        A a = this.b.b;
        i = c.c;
        i = d.d;
        i = e.e;
        i = f.f;
    }

}
