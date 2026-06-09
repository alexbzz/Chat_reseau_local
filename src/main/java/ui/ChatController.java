package ui;

import client.ChatClient;
import client.MessageReceiver;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import protocol.Message;
import protocol.MessageType;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ChatController {

    private final ChatView view;
    private ChatClient client;
    private MessageReceiver receiver;

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public ChatController(ChatView view, String host, String pseudo) {
        this.view = view;
        this.client = new ChatClient(host, 5000, pseudo);

        connectToServer();
        bindEvents();
    }

    private void connectToServer() {
        try {
            client.connect();

            // Ajouter son propre pseudo dans la liste dès la connexion
            Platform.runLater(() -> {
                if (!view.getUsersList().getItems().contains(client.getPseudo())) {
                    view.getUsersList().getItems().add(client.getPseudo());
                }
            });

            receiver = new MessageReceiver(client.getReader(), this::handleMessage);
            Thread t = new Thread(receiver);
            t.setDaemon(true);
            t.start();

        } catch (IOException e) {
            view.getMessagesList().getItems().add("Impossible de se connecter : " + e.getMessage());
        }
    }

    private void handleMessage(Message message) {
        // Cas spécial : pseudo déjà pris
        if (message.getType() == MessageType.SERVER_INFO
                && message.getContenu().equals("PSEUDO_TAKEN")) {
            Platform.runLater(() ->
                    view.getMessagesList().getItems().add("[ERREUR] Ce pseudo est déjà pris !")
            );
            return;
        }

        String time = LocalTime.ofInstant(
                Instant.ofEpochMilli(message.getTimestamp()),
                ZoneId.systemDefault()
        ).format(TIME_FMT);

        String formatted = switch (message.getType()) {
            case TEXT        -> "[" + time + "] " + message.getPseudo() + " : " + message.getContenu();
            case SERVER_INFO -> "[INFO] " + message.getContenu();
            case CONNECT     -> "[+] " + message.getPseudo() + " a rejoint le chat";
            case DISCONNECT  -> "[-] " + message.getPseudo() + " a quitté le chat";
        };

        Platform.runLater(() -> {
            view.getMessagesList().getItems().add(formatted);
            view.getMessagesList().scrollTo(
                    view.getMessagesList().getItems().size() - 1
            );

            if (message.getType() == MessageType.SERVER_INFO) {
                updateUsersList(message.getContenu());
            }
        });
    }

    private void updateUsersList(String info) {
        if (info.contains("a rejoint")) {
            String pseudo = info.replace(" a rejoint le chat", "").trim();
            if (!view.getUsersList().getItems().contains(pseudo)) {
                view.getUsersList().getItems().add(pseudo);
            }
        } else if (info.contains("a quitté")) {
            String pseudo = info.replace(" a quitté le chat", "").trim();
            view.getUsersList().getItems().remove(pseudo);
        }
    }

    private void bindEvents() {
        view.getSendButton().setOnAction(e -> sendMessage());

        view.getInputField().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) sendMessage();
        });
    }

    private void sendMessage() {
        String text = view.getInputField().getText().trim();
        if (text.isEmpty()) return;

        if (text.startsWith("/msg ")) {
            String[] parts = text.split(" ", 3);
            if (parts.length < 3) {
                Platform.runLater(() ->
                        view.getMessagesList().getItems().add("[ERREUR] Format : /msg <pseudo> <message>")
                );
                view.getInputField().clear();
                return;
            }
            String destinataire = parts[1];
            String contenu = parts[2];
            client.sendPrivate(destinataire, contenu);
            String time = LocalTime.now().format(TIME_FMT);
            Platform.runLater(() ->
                    view.getMessagesList().getItems().add("[" + time + "] [PRIVE -> " + destinataire + "] " + contenu)
            );

        } else if (text.startsWith("/join ")) {
            // Changer de salon
            client.sendMessage(text);
            String[] parts = text.split(" ", 2);
            String salon = parts.length > 1 ? parts[1].trim() : "general";
            Platform.runLater(() ->
                    view.getMessagesList().getItems().add("[INFO] Vous avez rejoint le salon #" + salon)
            );

        } else {
            client.sendMessage(text);
        }

        view.getInputField().clear();
    }

    public void disconnect() {
        if (receiver != null) receiver.stop();
        if (client != null) client.disconnect();
    }
}