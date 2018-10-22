import java.io.IOException;
import java.net.*;

public class Server {

	private final Configuration configuration;

	public Server(Configuration configuration) {
		this.configuration = configuration;
		System.out.println(configuration.getNickname());
		sendMessageToClient("Olá");
		System.out.println("Enviou");
		lintenClients();
		System.out.println("Recebeu");
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
