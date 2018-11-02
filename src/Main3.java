import java.io.IOException;

public class Main3 {
	public static void main(String[] args) {
		try {
			final Configuration config = ConfigurationReader.read("config3.txt");
			new Server(config);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
