package server;

import protocol.Message;
import protocol.MessageSerializer;
import protocol.MessageType;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ClientRegistry registry;

    private BufferedReader reader;
    private PrintWriter writer;
    private String pseudo = "inconnu";

    public ClientHandler(Socket socket, ClientRegistry registry) {
        this.socket = socket;
        this.registry = registry;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String line;
            while ((line = reader.readLine()) != null) {
                Message message = MessageSerializer.deserialize(line);
                if (message == null) continue;

                switch (message.getType()) {
                    case CONNECT    -> handleConnect(message);
                    case TEXT       -> handleText(message);
                    case DISCONNECT -> { handleDisconnect(message); return; }
                    default -> System.err.println("[Handler] Type non géré : " + message.getType());
                }
            }
        } catch (IOException e) {
            System.out.println("[Handler] Connexion perdue pour " + pseudo + " : " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleConnect(Message message) {
        String demandedPseudo = message.getPseudo();

        if (registry.isPseudoTaken(demandedPseudo)) {
            Message refus = new Message(MessageType.SERVER_INFO, "", "PSEUDO_TAKEN");
            sendRaw(MessageSerializer.serialize(refus));
            System.out.println("[Server] Pseudo déjà pris : " + demandedPseudo);
            cleanup();
            return;
        }

        this.pseudo = demandedPseudo;
        System.out.println("[Server] " + pseudo + " a rejoint le chat.");

        // Envoyer l'historique au nouveau client
        for (Message msg : registry.getHistorique()) {
            sendRaw(MessageSerializer.serialize(msg));
        }

        // Envoyer la liste des déjà connectés
        for (String p : registry.getPseudos()) {
            if (!p.equals(pseudo)) {
                Message info = new Message(MessageType.SERVER_INFO, "", p + " a rejoint le chat");
                sendRaw(MessageSerializer.serialize(info));
            }
        }

        // Informer tous les autres
        Message info = new Message(MessageType.SERVER_INFO, "", pseudo + " a rejoint le chat");
        registry.broadcast(info, this);
    }

    private void handleText(Message message) {
        System.out.println("[Server] " + pseudo + " : " + message.getContenu());
        registry.addToHistorique(message); // Sauvegarder dans l'historique
        registry.broadcast(message, null);
    }

    private void handleDisconnect(Message message) {
        System.out.println("[Server] " + pseudo + " s'est déconnecté proprement.");
        cleanup();
    }

    private void cleanup() {
        registry.unregister(this);
        if (pseudo != null && !pseudo.isBlank() && !pseudo.equals("inconnu")) {
            Message info = new Message(MessageType.SERVER_INFO, "", pseudo + " a quitté le chat");
            registry.broadcast(info, this);
        }
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[Handler] Erreur fermeture socket : " + e.getMessage());
        }
    }

    public synchronized void sendRaw(String line) {
        if (writer != null) {
            writer.println(line);
        }
    }

    public String getPseudo() { return pseudo; }
}