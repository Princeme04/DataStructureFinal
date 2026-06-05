package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Set;

public class SocialGraphController {
    private final SocialGraph socialGraph = new SocialGraph();

    @FXML
    private TextArea outputArea;

    @FXML
    private TextField nameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField majorField;

    @FXML
    private TextField interestField;

    @FXML
    private TextField friendshipUser1Field;

    @FXML
    private TextField friendshipUser2Field;

    @FXML
    private TextField queryUser1Field;

    @FXML
    private TextField queryUser2Field;

    @FXML
    private TextField recommendationUserField;

    private int nextUserId = 8;

    @FXML
    private void initialize() {
        addDummyData();
        handleDisplayAllUsers();
    }

    @FXML
    private void handleAddUser() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String major = majorField.getText().trim();
        String interest = interestField.getText().trim();

        if (name.isEmpty() || username.isEmpty() || major.isEmpty() || interest.isEmpty()) {
            showMessage("Input Error", "All user fields must be filled.");
            return;
        }

        boolean added = socialGraph.addUser(new User(nextUserId, name, username, major, interest));

        if (!added) {
            showMessage("Duplicate User", "Username already exists: " + username);
            return;
        }

        nextUserId++;
        nameField.clear();
        usernameField.clear();
        majorField.clear();
        interestField.clear();
        handleDisplayAllUsers();
    }

    @FXML
    private void handleAddFriendship() {
        String username1 = friendshipUser1Field.getText().trim();
        String username2 = friendshipUser2Field.getText().trim();

        if (username1.isEmpty() || username2.isEmpty()) {
            showMessage("Input Error", "Both friendship usernames must be filled.");
            return;
        }

        boolean added = socialGraph.addFriendship(username1, username2);

        if (!added) {
            showMessage("Friendship Error", "Friendship could not be added. Check usernames or duplicate friendship.");
            return;
        }

        friendshipUser1Field.clear();
        friendshipUser2Field.clear();
        handleDisplayAdjacencyList();
    }

    @FXML
    private void handleDisplayAllUsers() {
        StringBuilder builder = new StringBuilder();
        builder.append("=== All Users ===").append(System.lineSeparator());

        for (User user : socialGraph.getAllUsers()) {
            builder.append("- ").append(user).append(System.lineSeparator());
        }

        outputArea.setText(builder.toString());
    }

    @FXML
    private void handleDisplayAdjacencyList() {
        outputArea.setText("=== Friendship Adjacency List ==="
                + System.lineSeparator()
                + socialGraph.displayAdjacencyList());
    }

    @FXML
    private void handleFindMutualFriends() {
        String username1 = queryUser1Field.getText().trim();
        String username2 = queryUser2Field.getText().trim();

        if (username1.isEmpty() || username2.isEmpty()) {
            showMessage("Input Error", "Fill Username 1 and Username 2 first.");
            return;
        }

        Set<User> mutualFriends = socialGraph.getMutualFriends(username1, username2);
        StringBuilder builder = new StringBuilder();
        builder.append("=== Mutual Friends ===").append(System.lineSeparator());
        builder.append(username1).append(" and ").append(username2).append(System.lineSeparator());
        appendUserList(builder, mutualFriends, "No mutual friends found.");

        outputArea.setText(builder.toString());
    }

    @FXML
    private void handleFriendRecommendations() {
        String username = recommendationUserField.getText().trim();

        if (username.isEmpty()) {
            showMessage("Input Error", "Fill the recommendation username first.");
            return;
        }

        Set<User> recommendations = socialGraph.getFriendRecommendations(username);
        StringBuilder builder = new StringBuilder();
        builder.append("=== Friend Recommendations ===").append(System.lineSeparator());
        builder.append("For: ").append(username).append(System.lineSeparator());
        appendUserList(builder, recommendations, "No recommendations found.");

        outputArea.setText(builder.toString());
    }

    @FXML
    private void handleConnectionPath() {
        String startUsername = queryUser1Field.getText().trim();
        String targetUsername = queryUser2Field.getText().trim();

        if (startUsername.isEmpty() || targetUsername.isEmpty()) {
            showMessage("Input Error", "Fill Username 1 and Username 2 first.");
            return;
        }

        List<User> path = socialGraph.findConnectionPath(startUsername, targetUsername);
        StringBuilder builder = new StringBuilder();
        builder.append("=== Connection Path ===").append(System.lineSeparator());
        builder.append(startUsername).append(" to ").append(targetUsername).append(System.lineSeparator());

        if (path.isEmpty()) {
            builder.append("No connection path found.");
        } else {
            builder.append(socialGraph.formatUserPath(path));
        }

        outputArea.setText(builder.toString());
    }

    private void addDummyData() {
        socialGraph.addUser(new User(1, "Alice", "alice", "Computer Science", "Music"));
        socialGraph.addUser(new User(2, "Bob", "bob", "Information Systems", "Football"));
        socialGraph.addUser(new User(3, "Charlie", "charlie", "Computer Science", "Gaming"));
        socialGraph.addUser(new User(4, "Diana", "diana", "Data Science", "Reading"));
        socialGraph.addUser(new User(5, "Ethan", "ethan", "Software Engineering", "Photography"));
        socialGraph.addUser(new User(6, "Farah", "farah", "Information Systems", "Travel"));
        socialGraph.addUser(new User(7, "Gavin", "gavin", "Data Science", "Basketball"));

        socialGraph.addFriendship("alice", "bob");
        socialGraph.addFriendship("alice", "charlie");
        socialGraph.addFriendship("bob", "diana");
        socialGraph.addFriendship("charlie", "diana");
        socialGraph.addFriendship("diana", "ethan");
        socialGraph.addFriendship("farah", "gavin");
        socialGraph.addFriendship("ethan", "farah");
    }

    private void appendUserList(StringBuilder builder, Set<User> users, String emptyMessage) {
        if (users.isEmpty()) {
            builder.append(emptyMessage).append(System.lineSeparator());
            return;
        }

        for (User user : users) {
            builder.append("- ")
                    .append(user.getName())
                    .append(" (@")
                    .append(user.getUsername())
                    .append(")")
                    .append(System.lineSeparator());
        }
    }

    private void showMessage(String title, String message) {
        outputArea.setText("[" + title + "] " + message);
    }
}
