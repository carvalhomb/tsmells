package mine.tests;

import org.junit.TestCase;
import java.io.FileReader;
import java.io.BufferReader;
import mine.uut.MyUnitUnderTest;

public class FileAccessTest extends TestCase {
	public void testSomething() {
		MyUnitUnderTest myUUT = new myUUT();
		String myLine = readFile("/path/to/file");
		this.assertEquals(myLine, myUUT.myMethod());
	}

	public String readFile(String path) {
		FileReader myFile = new FileReader(path);
		BufferedReader myReader = new BufferedReader(myFile);
		String myLine = myReader.readLine();
		
	}
}

