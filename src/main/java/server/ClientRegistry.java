package server;

import protocol.Message;
import protocol.MessageSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientRegistry {

    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // Historique par salon : Map<nomSalon, Deque<Message>>
    private final ConcurrentHashMap<String, Deque<Message>> historiques = new ConcurrentHashMap<>();
    private static final int MAX_HISTORIQUE = 50;

    public void register(ClientHandler handler) {
        clients.add(handler);
        System.out.println("[Registry] Client ajouté. Total : " + clients.size());
    }

    public void unregister(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("[Registry] Client retiré. Total : " + clients.size());
    }

    /** Broadcast à tous les clients d'un salon donné */
    public void broadcastToRoom(Message message, String room, ClientHandler exclude) {
        String serialized = MessageSerializer.serialize(message);
        for (ClientHandler client : clients) {
            if (client != exclude && room.equals(client.getRoom())) {
                client.sendRaw(serialized);
            }
        }
    }

    /** Broadcast à tous les clients (toutes rooms) */
    public void broadcast(Message message, ClientHandler exclude) {
        String serialized = MessageSerializer.serialize(message);
        for (ClientHandler client : clients) {
            if (client != exclude) {
                client.sendRaw(serialized);
            }
        }
    }

    /** Ajoute un message à l'historique d'un salon */
    public synchronized void addToHistorique(String room, Message message) {
        historiques.putIfAbsent(room, new ArrayDeque<>());
        Deque<Message> hist = historiques.get(room);
        if (hist.size() >= MAX_HISTORIQUE) hist.pollFirst();
        hist.addLast(message);
    }

    /** Retourne l'historique d'un salon */
    public synchronized List<Message> getHistorique(String room) {
        Deque<Message> hist = historiques.get(room);
        return hist == null ? List.of() : List.copyOf(hist);
    }

    /** Retourne les pseudos connectés dans un salon donné */
    public List<String> getPseudosInRoom(String room) {
        return clients.stream()
                .filter(c -> room.equals(c.getRoom()))
                .map(ClientHandler::getPseudo)
                .filter(p -> p != null && !p.isBlank())
                .toList();
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