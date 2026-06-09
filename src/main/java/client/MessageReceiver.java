package client;

import protocol.Message;
import protocol.MessageSerializer;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageReceiver implements Runnable {

    private final BufferedReader reader;
    private final java.util.function.Consumer<Message> callback;
    private volatile boolean running = true;

    public MessageReceiver(BufferedReader reader, java.util.function.Consumer<Message> callback) {
        this.reader = reader;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                Message message = MessageSerializer.deserialize(line);
                if (message != null) {
                    callback.accept(message);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[Receiver] Connexion perdue : " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
    }
}