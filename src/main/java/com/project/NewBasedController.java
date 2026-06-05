package com.project;

import eu.hansolo.fx.charts.forcedirectedgraph.GraphEdge;
import eu.hansolo.fx.charts.forcedirectedgraph.GraphNode;
import eu.hansolo.fx.charts.forcedirectedgraph.GraphPanel;
import eu.hansolo.fx.charts.forcedirectedgraph.NodeEdgeModel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewBasedController {
    private final SocialGraphDatabase database = new SocialGraphDatabase();
    private SocialGraph socialGraph = new SocialGraph();

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField majorField;

    @FXML
    private TextField interestField;

    @FXML
    private Button saveUserButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Number> idColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> majorColumn;

    @FXML
    private TableColumn<User, String> interestColumn;

    @FXML
    private TextField relationUserOneField;

    @FXML
    private TextField relationUserTwoField;

    @FXML
    private Label relationStatusLabel;

    @FXML
    private TextArea adjacencyTextArea;

    @FXML
    private TextField analysisUserOneField;

    @FXML
    private TextField analysisUserTwoField;

    @FXML
    private TextField recommendationUserField;

    @FXML
    private TextArea analysisOutputArea;

    @FXML
    private Pane graphPane;

    private int nextUserId = 1;
    private boolean persistenceAvailable = true;
    private User selectedUser;
    private GraphPanel graphPanel;

    @FXML
    private void initialize() {
        configureUserTable();
        configureUserSelection();
        loadData();
        configureUsernameSuggestions();
        configureGraphPane();
        clearUserForm();
        refreshView();
    }

    @FXML
    private void handleSaveUser() {
        User formUser = buildUserFromForm(selectedUser == null ? nextUserId : selectedUser.getId());

        if (formUser == null) {
            return;
        }

        if (selectedUser == null) {
            addUser(formUser);
        } else {
            updateSelectedUser(formUser);
        }
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            showStatus("Select a user from the table before deleting.", false);
            return;
        }

        String username = selectedUser.getUsername();

        if (!socialGraph.deleteUser(username)) {
            showStatus("User could not be deleted: @" + username, false);
            return;
        }

        clearUserForm();
        persistAndRefresh("User deleted. Connected relations were removed.");
    }

    @FXML
    private void handleClearUserForm() {
        clearUserForm();
        showStatus("User form cleared.", true);
    }

    @FXML
    private void handleAddRelation() {
        String firstUsername = relationUserOneField.getText().trim();
        String secondUsername = relationUserTwoField.getText().trim();

        if (!validateRelationInput(firstUsername, secondUsername)) {
            return;
        }

        if (socialGraph.areFriends(firstUsername, secondUsername)) {
            showStatus("Relation already exists.", false);
            return;
        }

        socialGraph.addFriendship(firstUsername, secondUsername);
        clearRelationFields();
        persistAndRefresh("Relation added successfully.");
    }

    @FXML
    private void handleDeleteRelation() {
        String firstUsername = relationUserOneField.getText().trim();
        String secondUsername = relationUserTwoField.getText().trim();

        if (!validateRelationInput(firstUsername, secondUsername)) {
            return;
        }

        if (!socialGraph.areFriends(firstUsername, secondUsername)) {
            showStatus("Relation does not exist.", false);
            return;
        }

        socialGraph.removeFriendship(firstUsername, secondUsername);
        clearRelationFields();
        persistAndRefresh("Relation deleted successfully.");
    }

    @FXML
    private void handleDisplayAdjacencyList() {
        refreshAdjacencyList();
        showStatus("Adjacency list displayed.", true);
    }

    @FXML
    private void handleFindMutualFriends() {
        String firstUsername = analysisUserOneField.getText().trim();
        String secondUsername = analysisUserTwoField.getText().trim();

        if (!validateAnalysisUsers(firstUsername, secondUsername)) {
            return;
        }

        Set<User> mutualFriends = socialGraph.getMutualFriends(firstUsername, secondUsername);
        StringBuilder builder = new StringBuilder();
        builder.append("=== Mutual Friends ===").append(System.lineSeparator());
        builder.append(firstUsername).append(" and ").append(secondUsername).append(System.lineSeparator());
        appendUserList(builder, mutualFriends, "No mutual friends found.");

        analysisOutputArea.setText(builder.toString());
    }

    @FXML
    private void handleConnectionPath() {
        handleGeneratePaths();
    }

    @FXML
    private void handleBfsShortestPath() {
        handleGeneratePaths();
    }

    @FXML
    private void handleDfsFurthestPath() {
        handleGeneratePaths();
    }

    @FXML
    private void handleGeneratePaths() {
        String firstUsername = analysisUserOneField.getText().trim();
        String secondUsername = analysisUserTwoField.getText().trim();

        if (!validateAnalysisUsers(firstUsername, secondUsername)) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("=== Generated Paths ===").append(System.lineSeparator());
        builder.append(firstUsername).append(" to ").append(secondUsername).append(System.lineSeparator()).append(System.lineSeparator());
        appendPathResult(
                builder,
                "BFS Shortest Path",
                socialGraph.findBfsShortestPath(firstUsername, secondUsername),
                "BFS checks users level by level, so this is the first path that reaches the target with the fewest friendship edges."
        );
        builder.append(System.lineSeparator()).append(System.lineSeparator());
        appendPathResult(
                builder,
                "DFS Furthest Path",
                socialGraph.findDfsFurthestPath(firstUsername, secondUsername),
                "DFS explores each branch deeply and backtracks, so this is the longest simple path found without repeating users."
        );

        analysisOutputArea.setText(builder.toString());
    }

    private void appendPathResult(StringBuilder builder, String title, List<User> path, String explanation) {
        builder.append(title).append(System.lineSeparator());

        if (path.isEmpty()) {
            builder.append("No connection path found.");
        } else {
            builder.append(socialGraph.formatUserPath(path)).append(System.lineSeparator());
            builder.append("Distance: ").append(path.size() - 1).append(" friendship edge(s)").append(System.lineSeparator());
            builder.append("Why chosen: ").append(explanation);
        }
    }

    @FXML
    private void handleFriendRecommendations() {
        String username = recommendationUserField.getText().trim();

        if (username.isEmpty()) {
            analysisOutputArea.setText("Fill the recommendation username first.");
            return;
        }

        if (!socialGraph.containsUser(username)) {
            analysisOutputArea.setText("Username does not exist: " + username);
            return;
        }

        Set<User> recommendations = socialGraph.getFriendRecommendations(username);
        StringBuilder builder = new StringBuilder();
        builder.append("=== Friend Recommendations ===").append(System.lineSeparator());
        builder.append("For: ").append(username).append(System.lineSeparator());
        appendUserList(builder, recommendations, "No recommendations found.");

        analysisOutputArea.setText(builder.toString());
    }

    private void configureUserTable() {
        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        fullNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        usernameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getUsername()));
        majorColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getMajor()));
        interestColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getInterest()));
    }

    private void configureUserSelection() {
        userTable.getSelectionModel().selectedItemProperty().addListener((observable, oldUser, newUser) -> {
            if (newUser == null) {
                return;
            }

            selectedUser = newUser;
            fullNameField.setText(newUser.getName());
            usernameField.setText(newUser.getUsername());
            majorField.setText(newUser.getMajor());
            interestField.setText(newUser.getInterest());
            updateUserFormMode();
        });
    }

    private void configureUsernameSuggestions() {
        configureUsernameSuggestion(relationUserOneField);
        configureUsernameSuggestion(relationUserTwoField);
        configureUsernameSuggestion(analysisUserOneField);
        configureUsernameSuggestion(analysisUserTwoField);
        configureUsernameSuggestion(recommendationUserField);
    }

    private void configureUsernameSuggestion(TextField textField) {
        ContextMenu suggestionMenu = new ContextMenu();
        suggestionMenu.setAutoHide(true);

        textField.textProperty().addListener((observable, oldValue, newValue) ->
                updateUsernameSuggestions(textField, suggestionMenu, newValue));

        textField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (isFocused) {
                updateUsernameSuggestions(textField, suggestionMenu, textField.getText());
            } else {
                suggestionMenu.hide();
            }
        });
    }

    private void updateUsernameSuggestions(TextField textField, ContextMenu suggestionMenu, String typedText) {
        String query = typedText == null ? "" : typedText.trim().toLowerCase();

        if (query.isEmpty() || !textField.isFocused() || textField.getScene() == null) {
            suggestionMenu.hide();
            return;
        }

        suggestionMenu.getItems().clear();

        for (User user : socialGraph.getAllUsers()) {
            String username = user.getUsername();
            String name = user.getName().toLowerCase();

            if (!username.startsWith(query) && !name.startsWith(query)) {
                continue;
            }

            Label suggestionLabel = new Label(username);
            suggestionLabel.setMinWidth(Math.max(140, textField.getWidth() - 20));
            suggestionLabel.setStyle("-fx-padding: 6 10 6 10; -fx-text-fill: #222222;");

            CustomMenuItem suggestionItem = new CustomMenuItem(suggestionLabel, true);
            suggestionItem.setOnAction(event -> {
                textField.setText(username);
                textField.positionCaret(username.length());
                suggestionMenu.hide();
            });

            suggestionMenu.getItems().add(suggestionItem);

            if (suggestionMenu.getItems().size() == 6) {
                break;
            }
        }

        if (suggestionMenu.getItems().isEmpty()) {
            suggestionMenu.hide();
        } else if (!suggestionMenu.isShowing()) {
            suggestionMenu.show(textField, Side.BOTTOM, 0, 0);
        }
    }

    private void addUser(User user) {
        if (!socialGraph.addUser(user)) {
            showStatus("Username already exists: " + user.getUsername(), false);
            return;
        }

        nextUserId = socialGraph.getNextUserId();
        clearUserForm();
        persistAndRefresh("User saved successfully.");
    }

    private void updateSelectedUser(User updatedUser) {
        String oldUsername = selectedUser.getUsername();

        if (!socialGraph.updateUser(oldUsername, updatedUser)) {
            showStatus("Username already exists or selected user no longer exists.", false);
            return;
        }

        clearUserForm();
        persistAndRefresh("User updated successfully.");
    }

    private User buildUserFromForm(int id) {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String major = majorField.getText().trim();
        String interest = interestField.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || major.isEmpty() || interest.isEmpty()) {
            showStatus("All user fields must be filled.", false);
            return null;
        }

        if (username.matches(".*\\s+.*")) {
            showStatus("Username cannot contain spaces.", false);
            return null;
        }

        return new User(id, fullName, username, major, interest);
    }

    private boolean validateRelationInput(String firstUsername, String secondUsername) {
        if (firstUsername.isEmpty() || secondUsername.isEmpty()) {
            showStatus("Both usernames must be filled.", false);
            return false;
        }

        if (firstUsername.equalsIgnoreCase(secondUsername)) {
            showStatus("A user cannot have a relation with themself.", false);
            return false;
        }

        if (!socialGraph.containsUser(firstUsername) || !socialGraph.containsUser(secondUsername)) {
            showStatus("One or both usernames do not exist.", false);
            return false;
        }

        return true;
    }

    private boolean validateAnalysisUsers(String firstUsername, String secondUsername) {
        if (firstUsername.isEmpty() || secondUsername.isEmpty()) {
            analysisOutputArea.setText("Fill Username 1 and Username 2 first.");
            return false;
        }

        if (!socialGraph.containsUser(firstUsername) || !socialGraph.containsUser(secondUsername)) {
            analysisOutputArea.setText("One or both usernames do not exist.");
            return false;
        }

        return true;
    }

    private void loadData() {
        try {
            database.initializeDatabase();
            socialGraph = database.loadGraph();

            if (socialGraph.getAllUsers().isEmpty()) {
                addDummyData();
                database.saveGraph(socialGraph);
            }

            nextUserId = socialGraph.getNextUserId();
            showStatus("Database loaded: " + database.getDatabasePath(), true);
        } catch (SQLException exception) {
            persistenceAvailable = false;
            socialGraph = new SocialGraph();
            addDummyData();
            nextUserId = socialGraph.getNextUserId();
            showStatus("Database unavailable. Using temporary data only.", false);
        }
    }

    private boolean saveData() {
        if (!persistenceAvailable) {
            showStatus("Change kept in memory only. Database is unavailable.", false);
            return false;
        }

        try {
            database.saveGraph(socialGraph);
            return true;
        } catch (SQLException exception) {
            showStatus("Database save failed: " + exception.getMessage(), false);
            return false;
        }
    }

    private void persistAndRefresh(String successMessage) {
        boolean saved = saveData();

        refreshView();

        if (saved) {
            showStatus(successMessage, true);
        }
    }

    private void refreshView() {
        refreshUserTable();
        refreshAdjacencyList();
        refreshGraph();
    }

    private void refreshUserTable() {
        userTable.setItems(FXCollections.observableArrayList(socialGraph.getAllUsers()));
    }

    private void refreshAdjacencyList() {
        adjacencyTextArea.setText(socialGraph.displayAdjacencyList());
    }

    private void configureGraphPane() {
        graphPane.getChildren().clear();

        graphPanel = new GraphPanel(buildGraphModel());
        graphPanel.prefWidthProperty().bind(graphPane.widthProperty());
        graphPanel.prefHeightProperty().bind(graphPane.heightProperty());
        graphPanel.setEdgeColor(Color.web("#B8C7D9"));
        graphPanel.setNodeBorderWidth(2);
        graphPanel.setNodeSizeFactor(2.4);
        graphPanel.setSelectedNodeFillColor(Color.web("#2F3338"));
        graphPanel.setSelectedNodeBorderColor(Color.web("#0B45FF"));

        graphPane.getChildren().setAll(graphPanel);
    }

    private void refreshGraph() {
        if (graphPanel == null) {
            return;
        }

        graphPanel.setNodeEdgeModel(buildGraphModel());
        graphPanel.setEdgeColor(Color.web("#B8C7D9"));
        graphPanel.setNodeBorderWidth(2);
        graphPanel.setNodeSizeFactor(2.4);
        graphPanel.setSelectedNodeFillColor(Color.web("#2F3338"));
        graphPanel.setSelectedNodeBorderColor(Color.web("#0B45FF"));
        graphPanel.restart();
    }

    private NodeEdgeModel buildGraphModel() {
        List<GraphNode> graphNodes = new ArrayList<>();
        List<GraphEdge> graphEdges = new ArrayList<>();
        Map<String, GraphNode> nodesByUsername = new HashMap<>();

        for (User user : socialGraph.getAllUsers()) {
            Map<String, String> groups = new HashMap<>();
            groups.put(NodeEdgeModel.DEFAULT, "Users");
            groups.put("Username", user.getUsername());

            GraphNode node = new GraphNode(user.getName(), new HashMap<>(), groups);
            node.setValue(1);
            node.setFill(Color.web("#0B45FF"));
            node.setStroke(Color.WHITE);

            graphNodes.add(node);
            nodesByUsername.put(user.getUsername(), node);
        }

        for (Friendship friendship : socialGraph.getAllFriendships()) {
            GraphNode source = nodesByUsername.get(friendship.getFirstUsername());
            GraphNode target = nodesByUsername.get(friendship.getSecondUsername());

            if (source != null && target != null) {
                graphEdges.add(new GraphEdge(source, target));
            }
        }

        return new NodeEdgeModel(graphNodes, graphEdges);
    }

    private void clearUserForm() {
        selectedUser = null;
        userTable.getSelectionModel().clearSelection();
        fullNameField.clear();
        usernameField.clear();
        majorField.clear();
        interestField.clear();
        updateUserFormMode();
    }

    private void updateUserFormMode() {
        if (saveUserButton != null) {
            saveUserButton.setText(selectedUser == null ? "Save User" : "Save Changes");
        }

        if (deleteUserButton != null) {
            deleteUserButton.setDisable(selectedUser == null);
        }
    }

    private void clearRelationFields() {
        relationUserOneField.clear();
        relationUserTwoField.clear();
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

    private void showStatus(String message, boolean success) {
        relationStatusLabel.setText(message);
        relationStatusLabel.setTextFill(Color.web(success ? "#2E8B57" : "#B3261E"));
    }

    private void appendUserList(StringBuilder builder, Set<User> users, String emptyMessage) {
        if (users.isEmpty()) {
            builder.append(emptyMessage);
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
}
