import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

public class Server {

	public static final String NAO_COPIADO = "naocopiado";
	public static final String TODOS = "TODOS";
	public static final String OK = "OK";
	public static final String ERRO = "erro";
	public static final double ERROR_PROBABILITIY = 0.3;
	public static final String TOKEN = "1234";
	public static final int DEFAULT_PORT = 6000;
	private final Configuration configuration;
	private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());
	private Message errorMessage;

	// Inicializa um objeto server a partir de um objeto Configuration lido de um arquivo de configuração.
	public Server(Configuration configuration) {
		this.configuration = configuration;

		// Começa a ouvir mensagens na rede..
		new Thread(this::lintenClients).start();

		// Começa a ouvir o teclado esperando mensagens a serem enviadas.
		new Thread(() -> {
			System.out.println(configuration.toString());
			sendToken();
			while (true) {
				System.out.println("Dígite uma mensagem. Exemplo: 2345;naocopiado:nome_da_origem:nome_do_destino:M:conteudo");
				final Scanner scanner = new Scanner(System.in);
				final String messageString = scanner.nextLine();
				final Message message = Message.from(messageString);
				if (messages.size() < 10 && message != null) {
					messages.add(message);
					sendMessage();
				} else {
					System.out.println("Messagem perdida ou inválida" + messageString);
				}
			}
		}).start();

	}

	// Escuta mensagens na rede e realiza o tratamento das mesmas,
	// passando elas para frente até que cheguem de volta ao remetente.
	public void lintenClients() {
		DatagramSocket serverSocket;
		try {
			if (configuration.isDebug()) {
				serverSocket = new DatagramSocket(configuration.getDebugPort());
			} else {
				serverSocket = new DatagramSocket(DEFAULT_PORT);
			}

			final byte[] receiveData = new byte[1024];

			while (true) {
				final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);

				final String receivedMessage = new String(receivePacket.getData(), receivePacket.getOffset(),
						receivePacket.getLength());

				// Caso a mensagem recebida seja um TOKEN, a maquina passa a possuir o TOKEN e tenta enviar uma mensagem.
				// Caso a máquina não possua mensagens para enviar, o toquem é passado a diante.
				if (receivedMessage.equals(TOKEN)) {
					configuration.setHasToken(true);
					System.out.println(configuration.getNickname() + ": Recebi o token.");
					sendMessage();
				} else {
					// Caso seja uma mensagem
					final Message message = Message.from(receivedMessage);
					if (message == null) {
						System.out.println("Mensagem invalida " + receivedMessage);
						continue;
					}
					// Caso a mensagem seja para essa máquina.
					// Informa o contúdo da mensagem e o apelido da máquina que enviou a mesma.
					// Realiza um sortei sobre uma probabilidade, decidindo se ocorreu um erro ou não.
					// Caso ocorra um erro, o erro é notificado.
					// Caso não ocorra um erro, é informado que a mensagem chegou corretamente e
					// a mensagem é salva se necessário.
					if (message.getNicknameDestination().equals(configuration.getNickname())) {
						printMessage(message);
						if (Math.random() <= ERROR_PROBABILITIY) {
							message.setErrorControl(ERRO);
							System.out.println("Error control: " + ERRO);
						} else {
							message.setErrorControl(OK);
							System.out.println("Error control: " + OK);
							// Salva o conteúdo em um arquivo qualquer.
							if (message.getDataType() == 'A') {
								saveMessage(message);
							}
						}
						sendMessageToClient(message.toString());
					}
					// A mensagem é para todas as maquinas na rede.
					// A mensagem é impressa, salva se necessário e enviada a diante.
					else if (message.getNicknameDestination().equals(TODOS)
							&& !message.getNicknameSource().equals(configuration.getNickname())
							&& message.getErrorControl().equals(NAO_COPIADO)) {
						printMessage(message);
						// Salva o conteúdo em um arquivo qualquer.
						if (message.getDataType() == 'A') {
							saveMessage(message);
						}
						sendMessageToClient(message.toString());
					}
					// A Mensagem não é para mim e eu não sou a origem.
					// É informado que uma mensagem foi recebida, porém não é destinada a esta máquina
					// (para facilitar o comprendimento do programa).
					// A mensagem é passada a diante.
					else if (!message.getNicknameDestination().equals(configuration.getNickname())
							&& !message.getNicknameSource().equals(configuration.getNickname())) {
						System.out.println(configuration.getNickname() + ": Recebi uma mensagem e não é para mim.");
						sendMessageToClient(message.toString());
					}
					// A máquina que enviou a mensagem recebe ela novamente.
					// O TOKEN é passado a para a máquina a direita na rede.
					// Caso a tenha ocorrido um erro com a mensagem, é informado que não foi possível entrega-la
					// e será reenviada na próxima vez que a máquina possuir o TOKEN,
					// Caso a mensagem volte com o estado NAOCOPIADO e o destino não é todas as maquinas,
					// então a máquina destino não esta na rede, assim é informado que a mensagem foi descartada.
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
						}  else if (!message.getNicknameDestination().equals(TODOS)
								&& message.getErrorControl().equals(NAO_COPIADO)){
							System.out.println("Destino não encontrado e a mensagem foi descartada");
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

	// Envia o TOKEN para a máquina a direita no anel de rede.
	private void sendToken() {
		if (configuration.isHasToken()) {
			System.out.println(configuration.getNickname() + ": Enviando o token.");
			configuration.setHasToken(false);
			sendMessageToClient(TOKEN);
		}
	}

	// Imprime a mensagem.
	private void printMessage(Message message) {
		System.out.println("--- Recebi uma mensagem ---");
		System.out.println("Source: " + message.getNicknameSource());
		System.out.println("Content: " + message.getContent());
		System.out.println("---------------------------");
	}

	// Salva a mensagem em um arquivo com nome aleatório.
	private void saveMessage(Message message) {
		System.out.println("Salvando em um arquivo...");
		final String uuid = UUID.randomUUID().toString();
		try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(uuid)), true)) {
			printWriter.append(message.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Envia uma mensagem.
	// Caso exista uma mensagem com erro esperando para ser reenviada, esta é a escolhida para ser enviada.
	// Caso não exista uma mensagem com erro e existem mensagens para serem enviadas, a primeira mensagem a entrar na fila é enviada.
	// Caso não exista uma mensagem com erro e não existam mensagens na fila, então o TOKEN é passado a diante.
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

	// Envia uma mensagem para a máquina a direita no anel da rede.
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
