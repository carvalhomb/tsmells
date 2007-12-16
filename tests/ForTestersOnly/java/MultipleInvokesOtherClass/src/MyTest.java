import org.junit.TestCase;

public class MyTest extends TestCase {
    MyUUT uut;

    public void setUp() {
        this.uut = new MyUUT();
    }

    public void testUut() {
        assertTrue(this.uut.doStuff());
    }
}
