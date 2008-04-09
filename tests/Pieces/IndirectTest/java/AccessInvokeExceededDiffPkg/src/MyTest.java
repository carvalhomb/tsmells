import org.junit.TestCase;
import pkg1.*;
import pkg2.*;
import pkg3.*;

// use treshold = 4

public class MyTest extends TestCase {

    public void testCommand() {
        A a = new A();
        B b = new B();
        C c = new C();
        D d = new D();
        E e = new E();
        F f = new F();

        a.a();
        b.b();
        c.c();

        int i;
        i = d.d;
        i = e.e;
        i = f.f;
    }

}
