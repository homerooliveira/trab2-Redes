
public class Message {
    public final int id = 2345;
    private String errorControl;
    private String nicknameSource;
    private String nicknameDestination;
    private char dataType;
    private String content;
    private int retryCount = 0;

    private Message(String errorControl, String nicknameSource, String nicknameDestination, char dataType, String content) {
        this.errorControl = errorControl;
        this.nicknameSource = nicknameSource;
        this.nicknameDestination = nicknameDestination;
        this.dataType = dataType;
        this.content = content;
    }

    public static Message from(String rawValue) {
        final String[] values = rawValue.split(";");

        if (values.length != 2) {
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

    public String getErrorControl() {
        return errorControl;
    }

    public void setErrorControl(String errorControl) {
        this.errorControl = errorControl;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getNicknameSource() {
        return nicknameSource;
    }

    public String getNicknameDestination() {
        return nicknameDestination;
    }

    public char getDataType() {
        return dataType;
    }

    public String getContent() {
        return content;
    }

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
