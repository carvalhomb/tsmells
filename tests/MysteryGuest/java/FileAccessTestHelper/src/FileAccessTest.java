import org.junit.TestCase;
import java.io.FileReader;
import java.io.BufferedReader;

public class FileAccessTest extends TestCase {
	public void testSomething() throws Exception {
		MyUnitUnderTest myUUT = new MyUnitUnderTest();
		String myLine = readFile("/path/to/file");
		this.assertEquals(myLine, myUUT.myMethod());
	}

	public String readFile(String path) throws Exception {
		FileReader myFile = new FileReader(path);
		BufferedReader myReader = new BufferedReader(myFile);
		String myLine = myReader.readLine();
		return myLine;
	}
}

