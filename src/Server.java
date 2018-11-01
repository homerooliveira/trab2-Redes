import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

public class Server {

	public static final String NAO_COPIADO = "naocopiado";
	public static final String TODOS = "todos";
	public static final String OK = "OK";
	public static final String ERRO = "erro";
	public static final double ERROR_PROBABILITIY = 0.3;
	private final Configuration configuration;
	private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());
	private Message errorMessage;

	public Server(Configuration configuration) {
		this.configuration = configuration;

		new Thread(this::lintenClients).start();

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

				final String receivedMessage = new String(receivePacket.getData(), receivePacket.getOffset(),
						receivePacket.getLength());

				if (receivedMessage.equals("1234")) {
					configuration.setHasToken(true);
					System.out.println(configuration.getNickname() + ": Recebi o token.");
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
//						if (Math.random() <= ERROR_PROBABILITIY) {
						message.setErrorControl(ERRO);
						System.out.println("Error control: " + ERRO);
//						} else	 {
//							message.setErrorControl(OK);
//							System.out.println("Error control: " + OK);
//							if (message.getDataType() == 'A') {
//								saveMessage(message);
//							}
//						}
						sendMessageToClient(message.toString());
					}
					// A mensagem é para todos e eu não sou a origem, controle de erro é: naocopiado
					else if (message.getNicknameDestination().equals(TODOS)
							&& !message.getNicknameSource().equals(configuration.getNickname())
							&& message.getErrorControl().equals(NAO_COPIADO)) {
						System.out.println("A mensagem voltou para mim");
						printMessage(message);
						sendMessageToClient(message.toString());
					}
					// A Mensagem não é para mim e eu não sou a origem.
					else if (!message.getNicknameDestination().equals(configuration.getNickname())
							&& !message.getNicknameSource().equals(configuration.getNickname())) {
						System.out.println(configuration.getNickname() + ": Recebi uma mensagem e não é para mim.");
						sendMessageToClient(message.toString());
					}
					// Fui eu que enviei a mensagem e ela voltou para mim.
					else if (message.getNicknameSource().equals(configuration.getNickname())) {
						System.out.println("A mensagem voltou para mim.");
						System.out.println("Error control: " + message.getErrorControl());
						if (message.getErrorControl().equals(ERRO)) {
							System.out.println("Não foi possível entregar a mensagem.");
							if (errorMessage != null && errorMessage.equals(message)) {
								// Mensagem voltou pela segunda vez com erro, mensagem descartada
								errorMessage = null;
								System.out.println("Mensagem descartada!");
							}  else {
								message.setErrorControl(NAO_COPIADO);
								errorMessage = message;
								System.out.println("A mensagem será enviada mais uma vez assim que o token voltar!");

							}
							sendToken();
						}
						sendToken();
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
		System.out.println(configuration.getNickname() + ": Enviando o token.");
		configuration.setHasToken(false);
		sendMessageToClient("1234");
	}

	private void printMessage(Message message) {
		System.out.println("--- Recebi uma mensagem ---");
		System.out.println("Source: " + message.getNicknameSource());
		System.out.println("Content: " + message.getContent());
		System.out.println("---------------------------");
	}

	private void saveMessage(Message message) {
		System.out.println("Salvando em um arquivo...");
		final String uuid = UUID.randomUUID().toString();
		try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(uuid)), true)) {
			printWriter.append(message.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage() {
		if (errorMessage != null) {
			System.out.println(configuration.getNickname() + ": Enviando mensagem.");
			sendMessageToClient(errorMessage.toString());
		} else if (!messages.isEmpty()) {
			System.out.println(configuration.getNickname() + ": Enviando mensagem.");
			sendMessageToClient(messages.remove(0).toString());
		} else {
			System.out.println("Não há mensagens para serem enviadas. Enviando o token para a maquina à direita.");
			sendToken();
		}
	}

	public void sendMessageToClient(String message) {
		try {
			try (DatagramSocket clientSocket = new DatagramSocket()) {
				InetAddress address = InetAddress.getByName(configuration.getIpDestiny());

				byte[] sendData = message.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address,
						configuration.getPortDestiny());
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
