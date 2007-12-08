package mine.uut;

import java.io.FileReader;
import java.io.BufferReader;

public class MyUnitUnderTest {
	public void myMethod(String configFile) {
		someHelper(configFile);
	}

	private void someHelper(String configFile) {
		FileReader myFile = new FileReader(configFile);
		BufferedReader myReader = new BufferedReader(myFile);
		String myLine = myReader.readLine();
	}
}
