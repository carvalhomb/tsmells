import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {


    public void testCommand() {
        int i;

        A aa = new A();
        B bb = new B();
        C cc = new C();
        D dd = new D();

        i = aa.a;
        i = bb.b;
        i = cc.c;
        i = dd.d;
    }

}
