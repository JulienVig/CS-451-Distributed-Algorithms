package cs451;

public class OperationLog {
    private final Operation type;
    private final String content;

    public OperationLog(Operation type, String content) {
        this.type = type;
        this.content = content;
    }
    public OperationLog(Operation type, int content) {
        this(type, String.valueOf(content));
    }

    public Operation getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public int getIntContent(){
        return Integer.valueOf(content);
    }
}
