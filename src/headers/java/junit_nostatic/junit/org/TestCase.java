package junit.framework;

// Made all assertions non-static to play nice with SourceNavigator
public class TestCase {
	public void     assertEquals(boolean expected, boolean actual){}
	public void     assertEquals(byte expected, byte actual){}
	public void     assertEquals(char expected, char actual){}
	public void     assertEquals(double expected, double actual, double delta){}
	public void     assertEquals(float expected, float actual, float delta){}
	public void     assertEquals(int expected, int actual){}
	public void     assertEquals(long expected, long actual){}
	public void     assertEquals(java.lang.Object expected, java.lang.Object actual){}
	public void     assertEquals(short expected, short actual){}
	public void     assertEquals(java.lang.String message, boolean expected, boolean actual){}
	public void     assertEquals(java.lang.String message, byte expected, byte actual){}
	public void     assertEquals(java.lang.String message, char expected, char actual){}
	public void     assertEquals(java.lang.String message, double expected, double actual, double delta){}
	public void     assertEquals(java.lang.String message, float expected, float actual, float delta){}
	public void     assertEquals(java.lang.String message, int expected, int actual){}
	public void     assertEquals(java.lang.String message, long expected, long actual){}
	public void     assertEquals(java.lang.String message, java.lang.Object expected, java.lang.Object actual){}
	public void     assertEquals(java.lang.String message, short expected, short actual){}
	public void     assertEquals(java.lang.String expected, java.lang.String actual){}
	public void     assertEquals(java.lang.String message, java.lang.String expected, java.lang.String actual){}
	public void     assertFalse(boolean condition){}
	public void     assertFalse(java.lang.String message, boolean condition){}
	public void     assertNotNull(java.lang.Object object){}
	public void     assertNotNull(java.lang.String message, java.lang.Object object){}
	public void     assertNotSame(java.lang.Object expected, java.lang.Object actual){}
	public void     assertNotSame(java.lang.String message, java.lang.Object expected, java.lang.Object actual){}
	public void     assertNull(java.lang.Object object){}
	public void     assertNull(java.lang.String message, java.lang.Object object){}
	public void     assertSame(java.lang.Object expected, java.lang.Object actual){}
	public void     assertSame(java.lang.String message, java.lang.Object expected, java.lang.Object actual){}
	public void     assertTrue(boolean condition){}
	public void     assertTrue(java.lang.String message, boolean condition){}
	public void     fail(){}
	public void     fail(java.lang.String message){}
	private void     failNotEquals(java.lang.String message, java.lang.Object expected, java.lang.Object 	actual){}
	private void     failNotSame(java.lang.String message, java.lang.Object expected, java.lang.Object 	actual){}
	private void     failSame(java.lang.String message){}
	static java.lang.String       format(java.lang.String message, java.lang.Object expected, java.lang.Object actual) { return ""; }

}

