import java.io.FileReader;
import java.io.BufferedReader;

public class MyUnitUnderTest {
	public boolean myMethod(String configFile) {
		MyOtherProductionClass mopc = new MyOtherProductionClass();
		mopc.myOtherMethod(configFile);
        return true;
	}
}
