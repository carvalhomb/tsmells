import org.junit.TestCase;

public class MyTest extends TestCase {

    public void testCommand() {
        if (true) {
            assertTrue(true);
        }

        if (false) {
            assertTrue(false);
        }

        int j = 0;
        while ( j < 2 ) {
            assertTrue(true);
            j++;
        }
        for (int i=0; i < 2; i++) {
            assertTrue(true);
        }
    }

}