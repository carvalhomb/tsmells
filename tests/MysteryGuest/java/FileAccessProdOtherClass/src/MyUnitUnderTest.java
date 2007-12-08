package mine.uut;

import java.io.FileReader;
import java.io.BufferReader;

public class MyUnitUnderTest {
	public void myMethod(String configFile) {
		MyOtherProductionClass mopc = new MyOtherProductionClass();
		mopc.myOtherMethod(configFile);
	}
}
