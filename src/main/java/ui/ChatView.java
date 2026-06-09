package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ChatView extends BorderPane {

    private final ListView<String> messagesList;
    private final ListView<String> usersList;
    private final TextField inputField;
    private final Button sendButton;
    private final Label titleLabel;

    public ChatView() {
        messagesList = new ListView<>();
        usersList    = new ListView<>();
        inputField   = new TextField();
        sendButton   = new Button("Envoyer");
        titleLabel   = new Label("Chat Réseau");

        buildLayout();
        applyStyles();
    }

    private void buildLayout() {
        HBox top = new HBox(titleLabel);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(10));
        setTop(top);

        messagesList.setPrefHeight(400);
        VBox center = new VBox(messagesList);
        VBox.setVgrow(messagesList, Priority.ALWAYS);
        center.setPadding(new Insets(10));
        setCenter(center);

        Label usersLabel = new Label("Connectés");
        usersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        usersList.setPrefWidth(150);
        VBox right = new VBox(10, usersLabel, usersList);
        right.setPadding(new Insets(10));
        setRight(right);

        inputField.setPrefWidth(400);
        inputField.setPromptText("Écris ton message…");
        HBox bottom = new HBox(10, inputField, sendButton);
        bottom.setPadding(new Insets(10));
        bottom.setAlignment(Pos.CENTER);
        setBottom(bottom);
    }

    private void applyStyles() {
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        setStyle("-fx-background-color: #1e1e2e;");
        messagesList.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white;");
        usersList.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white;");
        titleLabel.setTextFill(Color.WHITE);
    }

    // Getters pour le controller
    public ListView<String> getMessagesList() { return messagesList; }
    public ListView<String> getUsersList()     { return usersList; }
    public TextField getInputField()           { return inputField; }
    public Button getSendButton()              { return sendButton; }
}