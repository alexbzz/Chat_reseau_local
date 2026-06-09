import server.ChatServer;

public class Main {

    public static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
            new ChatServer(port).start();

        } else if (args.length > 0 && args[0].equalsIgnoreCase("client")) {
            System.out.println("Client console : à venir ");

        } else {
            System.out.println("Interface JavaFX : à venir ");
        }
    }
}