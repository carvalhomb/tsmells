package mine.tests;

import org.junit.TestCase;
import java.io.FileReader;
import java.io.BufferReader;
import mine.uut.MyUnitUnderTest;

public class FileAccessTest extends TestCase {
	public void testSomething() {
		FileReader myFile = new FileReader("/path/to/file");
		BufferedReader myReader = new BufferedReader(myFile);
		String myLine = myReader.readLine();
		MyUnitUnderTest myUUT = new myUUT();
		this.assertEquals(myLine, myUUT.myMethod());
	}
}
