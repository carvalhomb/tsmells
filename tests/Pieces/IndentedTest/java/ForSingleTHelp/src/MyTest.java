import org.junit.TestCase;

public class MyTest extends TestCase {
    public void testCommand() {
        assertTrue(this.helper());
    }

    private boolean helper() {
        for (int i=0; i<2; i++) {}
        return true;
    }
}
