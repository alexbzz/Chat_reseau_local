package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    private final int port;
    private final ClientRegistry registry;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public ChatServer(int port) {
        this.port = port;
        this.registry = new ClientRegistry();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;

            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("  Serveur de chat démarré !");
            System.out.println("  IP locale  : " + ip);
            System.out.println("  Port       : " + port);
            System.out.println("  Attente de connexions…");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Nouvelle connexion depuis : "
                        + clientSocket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(clientSocket, registry);
                registry.register(handler);
                Thread thread = new Thread(handler);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (IOException e) {
            if (running) System.err.println("[Server] Erreur : " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[Server] Erreur fermeture : " + e.getMessage());
        }
        System.out.println("[Server] Arrêté.");
    }
}