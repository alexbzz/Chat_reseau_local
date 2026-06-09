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
    private String room = "general"; // salon par défaut

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

        // Envoyer l'historique du salon general au nouveau client
        for (Message msg : registry.getHistorique("general")) {
            sendRaw(MessageSerializer.serialize(msg));
        }

        // Envoyer la liste des connectés dans le même salon
        for (String p : registry.getPseudosInRoom("general")) {
            if (!p.equals(pseudo)) {
                Message info = new Message(MessageType.SERVER_INFO, "", p + " a rejoint le chat");
                sendRaw(MessageSerializer.serialize(info));
            }
        }

        // Informer les autres du salon
        Message info = new Message(MessageType.SERVER_INFO, "", pseudo + " a rejoint le chat");
        registry.broadcastToRoom(info, "general", this);
    }

    private void handleText(Message message) {
        String contenu = message.getContenu();

        if (contenu.startsWith("/msg ")) {
            // Message privé
            String[] parts = contenu.split(" ", 3);
            if (parts.length < 3) return;
            String destinataire = parts[1];
            String texte = parts[2];
            ClientHandler dest = registry.getClientByPseudo(destinataire);
            if (dest == null) {
                Message erreur = new Message(MessageType.SERVER_INFO, "",
                        "Utilisateur introuvable : " + destinataire);
                sendRaw(MessageSerializer.serialize(erreur));
                return;
            }
            Message prive = new Message(MessageType.TEXT, message.getPseudo(), "[PRIVE] " + texte);
            dest.sendRaw(MessageSerializer.serialize(prive));

        } else if (contenu.startsWith("/join ")) {
            // Changer de salon
            String[] parts = contenu.split(" ", 2);
            if (parts.length < 2) return;
            String newRoom = parts[1].trim();
            handleJoin(newRoom);

        } else {
            // Message normal dans le salon courant
            System.out.println("[Server][" + room + "] " + pseudo + " : " + contenu);
            registry.addToHistorique(room, message);
            registry.broadcastToRoom(message, room, null);
        }
    }

    private void handleJoin(String newRoom) {
        // Quitter l'ancien salon
        Message left = new Message(MessageType.SERVER_INFO, "", pseudo + " a quitté le salon #" + room);
        registry.broadcastToRoom(left, room, this);

        // Rejoindre le nouveau salon
        String oldRoom = room;
        this.room = newRoom;

        Message joined = new Message(MessageType.SERVER_INFO, "", pseudo + " a rejoint le salon #" + newRoom);
        registry.broadcastToRoom(joined, newRoom, this);

        // Confirmer au client
        Message confirm = new Message(MessageType.SERVER_INFO, "", "Vous avez rejoint le salon #" + newRoom);
        sendRaw(MessageSerializer.serialize(confirm));

        // Envoyer l'historique du nouveau salon
        for (Message msg : registry.getHistorique(newRoom)) {
            sendRaw(MessageSerializer.serialize(msg));
        }

        System.out.println("[Server] " + pseudo + " : #" + oldRoom + " -> #" + newRoom);
    }

    private void handleDisconnect(Message message) {
        System.out.println("[Server] " + pseudo + " s'est déconnecté proprement.");
        cleanup();
    }

    private void cleanup() {
        registry.unregister(this);
        if (pseudo != null && !pseudo.isBlank() && !pseudo.equals("inconnu")) {
            Message info = new Message(MessageType.SERVER_INFO, "", pseudo + " a quitté le chat");
            registry.broadcastToRoom(info, room, this);
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
    public String getRoom()   { return room; }
}