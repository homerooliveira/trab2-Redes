import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Server {

	public static final String NAO_COPIADO = "naocopiado";
	public static final String TODOS = "todos";
	public static final String OK = "OK";
	public static final String ERRO = "erro";
	private final Configuration configuration;
	private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());
	private Message errorMessage;

	public Server(Configuration configuration) {
		this.configuration = configuration;

		new Thread(this::lintenClients)
				.start();

		new Thread(() -> {
			while (true) {
				final Scanner scanner = new Scanner(System.in);
				final String messageString = scanner.nextLine();
				final Message message = Message.from(messageString);
				if (messages.size() < 10) {
					messages.add(message);
					sendMessage();
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
					if (message == null) {
						System.out.println("Mensagem invalida " + receivedMessage);
						continue;
					}


					// A mensagem é para mim
					if (message.getNicknameDestination().equals(configuration.getNickname())) {
						printMessage(message);
						message.setErrorControl(OK);
						// TODO: Fazer o probalidade de ocorrer um erro
						sendMessageToClient(message.toString());
					}
					// A mensagem é para todos e não fui eu que enviei e codigo é naocopiado
					else if (message.getNicknameDestination().equals(TODOS)
							&& !message.getNicknameSource().equals(configuration.getNickname())
							&& message.getErrorControl().equals(NAO_COPIADO)) {
						System.out.println("A mensagem voltou para mim");
						printMessage(message);
						sendMessageToClient(message.toString());
					}
					// Mensagem não é para mim e o origem não sou eu
					else if(!message.getNicknameDestination().equals(configuration.getNickname())
							&& !message.getNicknameSource().equals(configuration.getNickname())) {
						System.out.println(configuration.getNickname()
								+ " Recebi não é para mim"
								+ message.toString());
						sendMessageToClient(message.toString());
					}
					// Fui eu que enviei a mensagem e estou recendo de volta
					else if(message.getNicknameSource().equals(configuration.getNickname())) {
						System.out.println("é para mim " + message.toString());
						if (message.getErrorControl().equals(OK)
								|| message.getErrorControl().equals(NAO_COPIADO)) {
							System.out.println("Foi entregue " + message.toString());
							sendToken();
						} else if (message.getErrorControl().equals(ERRO)){
							 if(errorMessage == message){ // Mensagem voltou pela segunda vez com erro, mensagem descartada
								errorMessage = null;
							 	System.out.println("Não foi possivel enviar a mensagem " + message.toString());
							 } else {
								System.out.println("Não foi possivel enviar a mensagem, sera enviada mais uma vez assim que o token voltar. "
										+ message.toString());
								message.setErrorControl(NAO_COPIADO);
								errorMessage = message;
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
		configuration.setHasToken(false);
		sendMessageToClient("1234");
	}

	private void printMessage(Message message) {
		System.out.println("Source " + message.getNicknameSource());
		System.out.println("Print "+ message);

		if (message.getDataType() == 'A') {
			//TODO: salvar o conteudo em um arquivo
			System.out.println("Salvando em um arquivo");
		}
	}

	private void sendMessage() {
		if (configuration.isHasToken() && !messages.isEmpty()) {
			final Message remove;
			if (errorMessage != null) {
				remove = errorMessage;
			} else {
				remove = messages.remove(0);
			}
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
				System.out.println("Enviando " + configuration.getNickname());
				Thread.sleep((long) (1000 * configuration.getTokenTime()));
				clientSocket.send(sendPacket);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
