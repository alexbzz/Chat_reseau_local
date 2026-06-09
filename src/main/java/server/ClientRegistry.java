package server;

import protocol.Message;
import protocol.MessageSerializer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientRegistry {

    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // Historique des 50 derniers messages
    private final Deque<Message> historique = new ArrayDeque<>();
    private static final int MAX_HISTORIQUE = 50;

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

    /** Ajoute un message à l'historique */
    public synchronized void addToHistorique(Message message) {
        if (historique.size() >= MAX_HISTORIQUE) {
            historique.pollFirst();
        }
        historique.addLast(message);
    }

    /** Retourne une copie de l'historique */
    public synchronized List<Message> getHistorique() {
        return List.copyOf(historique);
    }

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
    public ClientHandler getClientByPseudo(String pseudo) {
        return clients.stream()
                .filter(c -> pseudo.equalsIgnoreCase(c.getPseudo()))
                .findFirst()
                .orElse(null);
    }
}