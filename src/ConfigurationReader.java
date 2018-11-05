import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ConfigurationReader {
	// Método utilizado para ler o arquivo de configuração de cada máquina,
	// retornano um objeto configuração.
    public static Configuration read(String filename) throws IOException {
        try (Scanner scanner = new Scanner(Files.newBufferedReader(Paths.get(filename)))) {
            final String[] ipAndPort = scanner.next().split(":");
            final String nickname = scanner.next();
            final double tokenTime = scanner.nextDouble();
            final boolean hasToken = scanner.nextBoolean();
            final int debugPort;
            if (scanner.hasNextInt()) {
                debugPort = scanner.nextInt();
            } else {
                debugPort = -1;
            }


            return new Configuration(
                    ipAndPort[0],
                    Integer.parseInt(ipAndPort[1]),
                    nickname,
                    tokenTime,
                    hasToken,
                    debugPort);
        }
    }
}