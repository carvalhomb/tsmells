package mine.uut;

import java.io.FileReader;
import java.io.BufferReader;

public class MyOtherProductionClass {
	public void myOtherMethod(String configFile) {
		FileReader myFile = new FileReader(configFile);
		BufferedReader myReader = new BufferedReader(myFile);
		String myLine = myReader.readLine();
	}
}
