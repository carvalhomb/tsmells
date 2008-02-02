import org.junit.TestCase;

// use treshold = 4

public class MyTest extends TestCase {

    private A aa;
    private B bb;
    private C cc;
    private D dd;
    private E ee;
    private F ff;

    public void testCommand() {
        int i;
        i = aa.a;
        i = bb.b;
        i = cc.c;
        i = dd.d;
        i = ee.e;
        i = ff.f;
    }

}
