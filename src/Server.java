import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Server {

	private final Configuration configuration;
	private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

	public Server(Configuration configuration) {
		this.configuration = configuration;
		new Thread(this::lintenClients)
				.start();

		new Thread(() -> {
			while (true) {
				final Scanner scanner = new Scanner(System.in);
				final String message = scanner.nextLine();
				sendMessageToClient(message);
			}
		}).start();
	}

	public void lintenClients() {
		DatagramSocket serverSocket;
		try {
			serverSocket = new DatagramSocket(configuration.getDebugPort());
			final byte[] receiveData = new byte[1024];

			while (true) {
				final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);

				final String receivedMessage = new String(
						receivePacket.getData(),
						receivePacket.getOffset(),
						receivePacket.getLength());

				final Message message = new Message(receivedMessage);
				System.out.println(message.toString());
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void sendMessageToClient(String messageString) {
		try {
			try(DatagramSocket clientSocket = new DatagramSocket()) {
				final Message message = new Message(messageString);
				InetAddress address = InetAddress.getByName(configuration.getIpDestiny());

				byte[] sendData = message.toString().getBytes();
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
