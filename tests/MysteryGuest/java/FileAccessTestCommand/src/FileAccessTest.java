
import org.junit.TestCase;
import java.io.FileReader;
import java.io.BufferedReader;

public class FileAccessTest extends TestCase {
	public void testSomething() throws Exception {
		FileReader myFile = new FileReader("/path/to/file");
		BufferedReader myReader = new BufferedReader(myFile);
		String myLine = myReader.readLine();
		MyUnitUnderTest myUUT = new MyUnitUnderTest();
		this.assertEquals(myLine, myUUT.myMethod());
	}
}
