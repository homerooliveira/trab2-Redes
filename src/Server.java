import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Server {

	private final Configuration configuration;

	public Server(Configuration configuration) {
		this.configuration = configuration;
		new Thread(this::lintenClients)
				.start();

		new Thread(() -> {
			while (true) {
				final Scanner scanner = new Scanner(System.in);
				final String message = scanner.next();
				sendMessageToClient(message);
			}
		}).start();
	}

	public void lintenClients() {
		DatagramSocket serverSocket;
		try {
			serverSocket = new DatagramSocket(configuration.getDebugPort());
			byte[] receiveData = new byte[1024];

			while (true) {
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
				System.out.println(receivedMessage);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void sendMessageToClient(String message) {
		try {
			try(DatagramSocket clientSocket = new DatagramSocket()) {
				InetAddress address = InetAddress.getByName(configuration.getIpDestiny());

				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(
						sendData,
						sendData.length,
						address,
						configuration.getPortDestiny());

				clientSocket.send(sendPacket);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
