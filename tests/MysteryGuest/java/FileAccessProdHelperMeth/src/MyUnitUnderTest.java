import java.io.FileReader;
import java.io.BufferedReader;

public class MyUnitUnderTest {
	public boolean myMethod(String configFile) {
		someHelper(configFile);
        return true;
	}

	private void someHelper(String configFile) {
        try {
            FileReader myFile = new FileReader(configFile);
            BufferedReader myReader = new BufferedReader(myFile);
            String myLine = myReader.readLine();
        } catch (Exception e) {}
	}
}
