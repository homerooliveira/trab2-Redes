
public class Message {
	// Identificador. Todas as mensagens possuem o mesmo identificador.
    public final int id = 2345;
    // Controle de erro. Verificar e tratar erro no envio das mensagens.
    private String errorControl;
    // Apelido da máquina que enviou a mensagem.
    private String nicknameSource;
    // Apelido da máquina destino.
    private String nicknameDestination;
    // Tipo de mensagem. M = mensagem simples; A = mensagem deve ser salva em um arquivo.
    private char dataType;
    // Conteúdo da mensagem.
    private String content;

    private Message(String errorControl, String nicknameSource, String nicknameDestination, char dataType, String content) {
        this.errorControl = errorControl;
        this.nicknameSource = nicknameSource;
        this.nicknameDestination = nicknameDestination;
        this.dataType = dataType;
        this.content = content;
    }

    // Converte uma mensagem formatada em String, para um objeto mensagem.
    public static Message from(String rawValue) {
        final String[] values = rawValue.split(";");

        if (values.length != 2) {
            return null;
        }

        if (!values[0].equals("2345")) {
            return null;
        }

        final String[] newValues = values[1].split(":");

        if (newValues.length != 5) {
            return null;
        }

        final String errorControl = newValues[0];
        final String nicknameSource = newValues[1];
        final String nicknameDestination = newValues[2];
        final char dataType = newValues[3].charAt(0);
        final String content = newValues[4];

        return new Message(errorControl, nicknameSource, nicknameDestination, dataType, content);
    }

    // Utilizado para verificar se ocorreu um erro no envio da mensagem. OK = Mensagem entregue;
    // NAOCOPIADO = A mensagem foi entregue a todas as máquinas na rede, ou a máquina não foi encontrada;
    // ERRO = Ocorreu um erro.
    public String getErrorControl() {
        return errorControl;
    }

    // Utilizado para atualizar o estado de uma mensagem. OK = Mensagem entregue;
    // NAOCOPIADO = A mensagem foi entregue a todas as máquinas na rede, ou a máquina não foi encontrada;
    // ERRO = Ocorreu um erro.
    public void setErrorControl(String errorControl) {
        this.errorControl = errorControl;
    }

    // Retorna o apelido da máquina que enviou a mensagem.
    public String getNicknameSource() {
        return nicknameSource;
    }

    // Retorna o apelido da máquina que deve receber a mensagem.
    public String getNicknameDestination() {
        return nicknameDestination;
    }

    // Retorna o tipo da mensagem. M = mensagem simples; A = mensagem deve ser salva em um arquivo.
    public char getDataType() {
        return dataType;
    }

    // Retorna o texto contido na mensagem.
    public String getContent() {
        return content;
    }
    
    // Utilizamos este método para garantir que quando ocorrer erro no envio de uma mensagem,
    // ela seja reenviada apenas uma vez. Duas mensagens são iguais se as seguintes propriedades
    // são iguais:
    // - O apelido da maquina que enviou a mensagem;
    // - O apelido da máquina que deve receber a mensagem;
    // - O tipo da mensagem;
    // - O conteúdo da mensagem.
    // Se esses atributos forem iguais, então as mensagem é a mesma.
    // Foi ignorado o atributo para controle de erro, pois no primeiro momento que a mensagem
    // volta com erro, o estado da mesma é trocado para "naocopiado, assim se ela voltar novamente
    // com erro, elas não seriam iguais.
    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof Message) {
    		Message message = (Message) obj;
    		if (message.nicknameSource.equals(nicknameSource) &&
    				message.nicknameDestination.equals(nicknameDestination) &&
    				message.dataType == dataType &&
    				message.content.equals(content)) {
    			return true;
    		}
    	}
    	return false;
    }

    // Formata a mensagem para String, para poder ser enviada na rede.
    @Override
    public String toString() {
        return String.format("%d;%s:%s:%s:%c:%s",
                id,
                errorControl,
                nicknameSource,
                nicknameDestination,
                dataType,
                content);
    }
}
