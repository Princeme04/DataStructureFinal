package com.project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main extends Application {

    static HashMap<String, HashSet<String>> graph = new HashMap<>();

    private TextArea outputArea;
    private TextField usernameField;
    private TextField user1Field;
    private TextField user2Field;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        addDummyData();

        Label titleLabel = new Label("Social Network Graph");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(300);

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        Button addUserButton = new Button("Add User");
        addUserButton.setOnAction(e -> handleAddUser());

        HBox addUserBox = new HBox(10, usernameField, addUserButton);

        user1Field = new TextField();
        user1Field.setPromptText("User 1");

        user2Field = new TextField();
        user2Field.setPromptText("User 2");

        Button addFriendButton = new Button("Add Friendship");
        addFriendButton.setOnAction(e -> handleAddFriendship());

        HBox friendshipBox = new HBox(10, user1Field, user2Field, addFriendButton);

        Button refreshButton = new Button("Refresh Graph");
        refreshButton.setOnAction(e -> displayGraph());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                titleLabel,
                addUserBox,
                friendshipBox,
                refreshButton,
                outputArea
        );

        Scene scene = new Scene(root, 600, 450);

        stage.setTitle("Social Network Graph");
        stage.setScene(scene);
        stage.show();

        displayGraph();
    }

    static void addUser(String username) {
        if (!graph.containsKey(username)) {
            graph.put(username, new HashSet<>());
        }
    }

    static void addFriendship(String user1, String user2) {
        if (!graph.containsKey(user1)) {
            addUser(user1);
        }

        if (!graph.containsKey(user2)) {
            addUser(user2);
        }

        graph.get(user1).add(user2);
        graph.get(user2).add(user1);
    }

    static void addDummyData() {
        addUser("Ovan");
        addUser("Amalia");
        addUser("Budi");
        addUser("Sinta");
        addUser("Raka");
        addUser("Dina");
        addUser("Fajar");

        addFriendship("Ovan", "Amalia");
        addFriendship("Ovan", "Budi");
        addFriendship("Ovan", "Sinta");
        addFriendship("Budi", "Raka");
        addFriendship("Sinta", "Dina");
        addFriendship("Raka", "Fajar");
        addFriendship("Dina", "Fajar");
    }

    private void handleAddUser() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            showAlert("Input Error", "Username cannot be empty.");
            return;
        }

        if (graph.containsKey(username)) {
            showAlert("Duplicate User", "User already exists: " + username);
            return;
        }

        addUser(username);
        usernameField.clear();
        displayGraph();
    }

    private void handleAddFriendship() {
        String user1 = user1Field.getText().trim();
        String user2 = user2Field.getText().trim();

        if (user1.isEmpty() || user2.isEmpty()) {
            showAlert("Input Error", "Both users must be filled.");
            return;
        }

        if (user1.equals(user2)) {
            showAlert("Input Error", "A user cannot be friends with themselves.");
            return;
        }

        addFriendship(user1, user2);

        user1Field.clear();
        user2Field.clear();

        displayGraph();
    }

    private void displayGraph() {
        StringBuilder builder = new StringBuilder();

        builder.append("=== Social Network Graph ===\n\n");

        for (Map.Entry<String, HashSet<String>> entry : graph.entrySet()) {
            builder.append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue())
                    .append("\n");
        }

        outputArea.setText(builder.toString());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}