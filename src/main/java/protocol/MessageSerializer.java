package protocol;

public class MessageSerializer {

    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_REGEX = "\\|";

    public static String serialize(Message message) {
        return message.getType().name()
                + SEPARATOR + message.getPseudo()
                + SEPARATOR + message.getContenu()
                + SEPARATOR + message.getTimestamp();
    }

    public static Message deserialize(String line) {
        if (line == null || line.isBlank()) return null;

        // Limite à 4 parties : le contenu peut contenir des | lui-même
        String[] parts = line.split(SEPARATOR_REGEX, 4);
        if (parts.length < 4) return null;

        try {
            MessageType type  = MessageType.valueOf(parts[0]);
            String pseudo     = parts[1];
            String contenu    = parts[2];
            long timestamp    = Long.parseLong(parts[3]);
            return new Message(type, pseudo, contenu, timestamp);
        } catch (IllegalArgumentException e) {
            System.err.println("[MessageSerializer] Ligne invalide : " + line);
            return null;
        }
    }
}