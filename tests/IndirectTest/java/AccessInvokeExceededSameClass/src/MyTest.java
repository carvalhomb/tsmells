import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {

    public void testCommand() {
        A a = new A();

        a.a();
        a.b();
        a.c();
        a.d();
        a.e();
        a.f();

        Integer i;
        i = a.a;
        i = a.b;
        i = a.c;
        i = a.d;
        i = a.e;
        i = a.f;
    }

}
