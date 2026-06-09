package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApp extends Application {

    private ChatController controller;

    public static String PSEUDO = "Anonyme";
    public static String HOST   = "localhost";

    @Override
    public void start(Stage stage) {
        ChatView view = new ChatView();
        controller = new ChatController(view, HOST, PSEUDO);

        Scene scene = new Scene(view, 700, 500);
        stage.setTitle("Chat Réseau - " + PSEUDO);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> {
            controller.disconnect();
            Platform.exit();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}