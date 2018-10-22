import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		try {
			final Configuration config = ConfigurationReader.read("config2.txt");
			new Server(config);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
