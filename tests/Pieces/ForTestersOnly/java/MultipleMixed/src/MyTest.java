import org.junit.TestCase;

public class MyTest extends TestCase {
    private MyUUT uut;

    public void setUp() {
        this.uut = new MyUUT();
    }

    public void testUut() {
        this.uut.anotherFto();
        this.uut.doStuff();
        this.uut.doMore();
        assertTrue(this.uut.ftoMethod());
    }

}
