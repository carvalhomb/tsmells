import org.junit.TestCase;

public class MyTest extends TestCase {
    private MyUUT myUut;

    public void setUp() {
        this.myUut = new MyUUT();
    }

    public void myTestCommand() {
        assertTrue(this.myUut.myFTOMethod());
    }
}
