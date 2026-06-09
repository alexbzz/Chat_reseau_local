package client;

import protocol.Message;
import protocol.MessageSerializer;
import protocol.MessageType;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private final String host;
    private final int port;
    private final String pseudo;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean connected = false;

    public ChatClient(String host, int port, String pseudo) {
        this.host = host;
        this.port = port;
        this.pseudo = pseudo;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;

        Message connectMsg = new Message(MessageType.CONNECT, pseudo, "");
        writer.println(MessageSerializer.serialize(connectMsg));
        System.out.println("[Client] Connecté au serveur en tant que " + pseudo);
    }

    public void sendMessage(String contenu) {
        if (!connected) return;
        Message msg = new Message(MessageType.TEXT, pseudo, contenu);
        writer.println(MessageSerializer.serialize(msg));
    }

    public void disconnect() {
        if (!connected) return;
        Message msg = new Message(MessageType.DISCONNECT, pseudo, "");
        writer.println(MessageSerializer.serialize(msg));
        connected = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[Client] Erreur fermeture : " + e.getMessage());
        }
        System.out.println("[Client] Déconnecté.");
    }

    public BufferedReader getReader() { return reader; }
    public String getPseudo()         { return pseudo; }
    public boolean isConnected()      { return connected; }
}