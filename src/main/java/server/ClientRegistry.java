package server;

import protocol.Message;
import protocol.MessageSerializer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientRegistry {

    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public void register(ClientHandler handler) {
        clients.add(handler);
        System.out.println("[Registry] Client ajouté. Total : " + clients.size());
    }

    public void unregister(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("[Registry] Client retiré. Total : " + clients.size());
    }

    public void broadcast(Message message, ClientHandler exclude) {
        String serialized = MessageSerializer.serialize(message);
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.sendRaw(serialized);
            }
        }
    }

    /** Vérifie si un pseudo est déjà utilisé par un client connecté */
    public boolean isPseudoTaken(String pseudo) {
        return clients.stream()
                .anyMatch(c -> pseudo.equalsIgnoreCase(c.getPseudo()));
    }

    public List<String> getPseudos() {
        return clients.stream()
                .map(ClientHandler::getPseudo)
                .filter(p -> p != null && !p.isBlank())
                .toList();
    }
}