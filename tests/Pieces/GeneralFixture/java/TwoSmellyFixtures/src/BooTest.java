import org.junit.TestCase;

public class BooTest extends TestCase {

    Bar bar;
    Baz baz;
    Boo boo;
    Fal fal;
    Foo foo;
    Fuu fuu;

    public void setUp() {
        bar = new Bar();
        baz = new Baz();
        boo = new Boo();
        fal = new Fal();
        foo = new Foo();
        fuu = new Fuu();
    }

    public void testBoo() {
        assertTrue(foo.foo());
    }

}
