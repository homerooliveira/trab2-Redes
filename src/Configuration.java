public class Configuration {
	// IP da máquina a direita na rede.
    private String ipDestiny;
    // Porta que a mensagem sera enviada para a outra máquina.
    private int portDestiny;
    // Apelido da máquina.
    private String nickname;
    private double tokenTime;
    // Utilizado para verificar se a máquina possuí o TOKEN e pode enviar mensagens.
    private boolean hasToken;
    // Porta utilizada para realizar debug em uma máquina apenas.
    private int debugPort;

    public Configuration(String ipDestiny, int portDestiny, String nickname, double tokenTime, boolean hasToken, int debugPort) {
        this.ipDestiny = ipDestiny;
        this.portDestiny = portDestiny;
        this.nickname = nickname;
        this.tokenTime = tokenTime;
        this.hasToken = hasToken;
        this.debugPort = debugPort;
    }

    // Retorna o IP da máquina a direita no anel da rede.
    public String getIpDestiny() {
        return ipDestiny;
    }

    // Retorna a porta que a mensagem sera enviada para a máquina destino.
    public int getPortDestiny() {
        return portDestiny;
    }

    // Retorna o apelido da máquina.
    public String getNickname() {
        return nickname;
    }
    
    public double getTokenTime() {
        return tokenTime;
    }

    // Retorna se a máquinda possui o TOKEN e pode enviar mensagens.
    public boolean isHasToken() {
        return hasToken;
    }

    // Atualiza se a máquina possui ou não o TOKEN.
    public void setHasToken(boolean hasToken) {
        this.hasToken = hasToken;
    }

    // Retorna a porta utilizada para debug em uma máquina.
    public int getDebugPort() {
        return debugPort;
    }

    // Verifica se está no modo de debug em uma máquina.
    public boolean isDebug() {
        return debugPort != -1;
    }

    // Imprime a configuração da rede.
    @Override
    public String toString() {
        return "Configuration{" +
                "ipDestiny='" + ipDestiny + '\'' +
                ", portDestiny='" + portDestiny + '\'' +
                ", nickname='" + nickname + '\'' +
                ", tokenTime=" + tokenTime +
                ", hasToken=" + hasToken +
                '}';
    }
}
