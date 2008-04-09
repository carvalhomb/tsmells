import org.junit.TestCase;
import mypkg.MyUUT;

public class MyTest extends TestCase {
    private MyUUT myUut;

    public void setUp() {
        this.myUut = new MyUUT();
    }

    public void myTestCommand() {
        boolean res = this.myUut.myFTOMethod();
        assertTrue(res);
    }
}
