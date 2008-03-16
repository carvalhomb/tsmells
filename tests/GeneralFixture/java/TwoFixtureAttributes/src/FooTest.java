import org.junit.TestCase;

public class FooTest extends TestCase {

    Foo foo;
    Fuu fuu;

    public void setUp() {
        foo = new Foo();
        fuu = new Fuu();
    }

    public void testFoo() {
        assertTrue(foo.foo());
    }
}
