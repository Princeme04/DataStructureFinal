package com.project;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SocialGraphDatabase {
    private static final Path DATABASE_PATH = Path.of("social_network_graph.db").toAbsolutePath();
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_PATH.toString().replace("\\", "/");

    public void initializeDatabase() throws SQLException {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        username TEXT NOT NULL UNIQUE,
                        major TEXT NOT NULL,
                        interest TEXT NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS friendships (
                        first_username TEXT NOT NULL,
                        second_username TEXT NOT NULL,
                        PRIMARY KEY (first_username, second_username),
                        FOREIGN KEY (first_username) REFERENCES users(username)
                            ON UPDATE CASCADE ON DELETE CASCADE,
                        FOREIGN KEY (second_username) REFERENCES users(username)
                            ON UPDATE CASCADE ON DELETE CASCADE,
                        CHECK (first_username <> second_username)
                    )
                    """);
        }
    }

    public SocialGraph loadGraph() throws SQLException {
        SocialGraph graph = new SocialGraph();

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet users = statement.executeQuery(
                     "SELECT id, name, username, major, interest FROM users ORDER BY id")) {

            while (users.next()) {
                graph.addUser(new User(
                        users.getInt("id"),
                        users.getString("name"),
                        users.getString("username"),
                        users.getString("major"),
                        users.getString("interest")
                ));
            }
        }

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet friendships = statement.executeQuery(
                     "SELECT first_username, second_username FROM friendships ORDER BY first_username, second_username")) {

            while (friendships.next()) {
                graph.addFriendship(
                        friendships.getString("first_username"),
                        friendships.getString("second_username")
                );
            }
        }

        return graph;
    }

    public void saveGraph(SocialGraph graph) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);

            try {
                deleteExistingData(connection);
                insertUsers(connection, graph);
                insertFriendships(connection, graph);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public String getDatabasePath() {
        return DATABASE_PATH.toString();
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    private void deleteExistingData(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM friendships");
            statement.executeUpdate("DELETE FROM users");
        }
    }

    private void insertUsers(Connection connection, SocialGraph graph) throws SQLException {
        String sql = "INSERT INTO users (id, name, username, major, interest) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (User user : graph.getAllUsers()) {
                statement.setInt(1, user.getId());
                statement.setString(2, user.getName());
                statement.setString(3, user.getUsername());
                statement.setString(4, user.getMajor());
                statement.setString(5, user.getInterest());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void insertFriendships(Connection connection, SocialGraph graph) throws SQLException {
        String sql = "INSERT INTO friendships (first_username, second_username) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Friendship friendship : graph.getAllFriendships()) {
                statement.setString(1, friendship.getFirstUsername());
                statement.setString(2, friendship.getSecondUsername());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
}
