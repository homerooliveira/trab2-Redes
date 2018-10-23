
public class Message {
    public final int id = 2345;
    private String errorControl;
    private String nicknameSource;
    private String nicknameDestination;
    private char dataType;
    private String content;

    public Message(String rawValue) {
        final String[] values = rawValue.split(";");

        if (values.length != 2) {
            return;
        }

        final String[] newValues = values[1].split(":");

        if (newValues.length != 5) {
            return;
        }

        final String errorControl = newValues[0];
        final String nicknameSource = newValues[1];
        final String nicknameDestination = newValues[2];
        final char dataType = newValues[3].charAt(0);
        final String content = newValues[4];

        this.errorControl = errorControl;
        this.nicknameSource = nicknameSource;
        this.nicknameDestination = nicknameDestination;
        this.dataType = dataType;
        this.content = content;
    }

    public String getErrorControl() {
        return errorControl;
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
