package protocol;

public class Message {

    private final MessageType type;
    private final String pseudo;
    private final String contenu;
    private final long timestamp;

    public Message(MessageType type, String pseudo, String contenu, long timestamp) {
        this.type = type;
        this.pseudo = pseudo != null ? pseudo : "";
        this.contenu = contenu != null ? contenu : "";
        this.timestamp = timestamp;
    }

    public Message(MessageType type, String pseudo, String contenu) {
        this(type, pseudo, contenu, System.currentTimeMillis());
    }

    public MessageType getType()  { return type; }
    public String getPseudo()     { return pseudo; }
    public String getContenu()    { return contenu; }
    public long getTimestamp()    { return timestamp; }

    @Override
    public String toString() {
        return "[" + type + "] " + pseudo + " : " + contenu;
    }
}