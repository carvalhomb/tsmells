import org.junit.TestCase;

public class FooTest extends TestCase {

    Bar bar;
    Baz baz;
    Fal fal;
    Foo foo;
    Fuu fuu;

    public void setUp() {
        bar = new Bar();
        baz = new Baz();
        fal = new Fal();
        foo = new Foo();
        fuu = new Fuu();
    }

    public void testFoo() {
        assertTrue(foo.foo());
    }
}
