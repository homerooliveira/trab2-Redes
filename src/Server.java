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
		sendMessage();

		new Thread(this::lintenClients)
				.start();

		new Thread(() -> {
			while (true) {
				final Scanner scanner = new Scanner(System.in);
				final String messageString = scanner.nextLine();
				final Message message = Message.from(messageString);
				if (messages.size() < 10) {
					messages.add(message);
				} else {
					System.out.println("Messagem perdida " + messageString);
				}
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

				if (receivedMessage.equals("1234")) {
					configuration.setHasToken(true);
					sendMessage();
				} else {
					final Message message = Message.from(receivedMessage);
					if (message != null) {
						System.out.println(message.toString());

						if (message.getNicknameDestination().equals(configuration.getNickname())) {
							printMessage(message);
							message.setErrorControl("ok");
							sendMessageToClient(message.toString());
						} else if (message.getNicknameDestination().equals("todos")
								&& !message.getNicknameSource().equals(configuration.getNickname())
								&& message.getErrorControl().equals("naocopiado")) {
							System.out.println("A mensagem voltou para mim");
							printMessage(message);
							sendMessageToClient(message.toString());
						} else if(!message.getNicknameDestination().equals(configuration.getNickname())) {
							System.out.println("Recebi não para mim");
							System.out.println(message.toString());
							sendMessageToClient(message.toString());
						} else if (message.getErrorControl().equals("OK")
								|| message.getErrorControl().equals("naocopiado")) {
							System.out.println("Foi entregue");
							sendToken();
						} else if (message.getErrorControl().equals("erro")){
							if (message.getRetryCount() > 0) {
								System.out.println("Não foi possivel enviar a mensagem");
							} else {
								System.out.println("Não foi possivel enviar a mensagem, voltou para fila.");
								message.setRetryCount(message.getRetryCount() + 1);
								message.setErrorControl("naocopiado");
								messages.add(0, message);
							}
							sendToken();
						}
					}
				}

			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendToken() {
		sendMessageToClient("1234");
	}

	private void printMessage(Message message) {
		System.out.println("Source " + message.getNicknameSource());
		System.out.println(message);
		// TODO: Fazer o probalidade de ocorrer um erro
		if (message.getDataType() == 'A') {
			//TODO: salvar o conteudo em um arquivo
			System.out.println("Salvando em um arquivo");
		}
	}

	private void sendMessage() {
		if (configuration.isHasToken() && !messages.isEmpty()) {
			final Message remove = messages.remove(0);
			sendMessageToClient(remove.toString());
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
