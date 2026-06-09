import client.ChatClient;
import client.MessageReceiver;
import protocol.Message;
import server.ChatServer;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
            new ChatServer(port).start();

        } else if (args.length > 0 && args[0].equalsIgnoreCase("client")) {
            String host   = args.length > 1 ? args[1] : "localhost";
            String pseudo = args.length > 2 ? args[2] : "Anonyme";
            lancerClientConsole(host, pseudo);

        } else {
            System.out.println("Interface JavaFX : à venir (étape 3)");
        }
    }

    private static void lancerClientConsole(String host, String pseudo) {
        ChatClient client = new ChatClient(host, DEFAULT_PORT, pseudo);
        try {
            client.connect();

            MessageReceiver receiver = new MessageReceiver(
                    client.getReader(),
                    msg -> System.out.println("[" + msg.getPseudo() + "] " + msg.getContenu())
            );
            Thread receiverThread = new Thread(receiver);
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Boucle de saisie dans le terminal
            Scanner scanner = new Scanner(System.in);
            System.out.println("Tape tes messages (ou 'quit' pour quitter) :");
            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();
                if (ligne.equalsIgnoreCase("quit")) break;
                client.sendMessage(ligne);
            }

            client.disconnect();
            receiver.stop();

        } catch (IOException e) {
            System.err.println("[Main] Impossible de se connecter : " + e.getMessage());
        }
    }
}