import org.junit.TestCase;

public class MyTest extends TestCase {
    public void testCommand() {
        int i = 0;
        while ( i < 5 ) {
            assertTrue(i < 5);
            i++;
        }
    }

    public void testMore() {
        assertTrue(this.helper());
    }

    private enum NUM {ONE, TWO, THREE}

    public boolean helper() {
        NUM b = NUM.ONE;
        switch (b) {
        case ONE:
            assertTrue(true);
            break;
        case TWO:
            assertTrue(true);
            break;
        default:
            fail();
        }
        return true;
    }

}
