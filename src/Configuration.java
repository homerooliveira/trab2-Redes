public class Configuration {
    private String ipDestiny;
    private int portDestiny;
    private String nickname;
    private double tokenTime;
    private boolean hasToken;
    private int debugPort;

    public Configuration(String ipDestiny, int portDestiny, String nickname, double tokenTime, boolean hasToken, int debugPort) {
        this.ipDestiny = ipDestiny;
        this.portDestiny = portDestiny;
        this.nickname = nickname;
        this.tokenTime = tokenTime;
        this.hasToken = hasToken;
        this.debugPort = debugPort;
    }

    public String getIpDestiny() {
        return ipDestiny;
    }

    public void setIpDestiny(String ipDestiny) {
        this.ipDestiny = ipDestiny;
    }

    public int getPortDestiny() {
        return portDestiny;
    }

    public void setPortDestiny(int portDestiny) {
        this.portDestiny = portDestiny;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public double getTokenTime() {
        return tokenTime;
    }

    public void setTokenTime(double tokenTime) {
        this.tokenTime = tokenTime;
    }

    public boolean isHasToken() {
        return hasToken;
    }

    public void setHasToken(boolean hasToken) {
        this.hasToken = hasToken;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public boolean isDebug() {
        return debugPort != -1;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

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
