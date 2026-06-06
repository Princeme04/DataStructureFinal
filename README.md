# Social Network Graph

## Overview

Social Network Graph is a JavaFX project that simulates friendship relationships using a graph data structure.

Each user is represented as a graph node. Each friendship is represented as an undirected edge, which means if Alice is friends with Bob, then Bob is also friends with Alice.

## Core Data Structure

The project uses an adjacency list:

```text
Alice -> Bob, Charlie
Bob -> Alice, Diana
Charlie -> Alice, Diana
```

An adjacency list is suitable for a social network because each user usually connects to only some users, not every user in the system.

## Features

- Add user
- Edit selected user
- Delete selected user and all connected friendships
- Add undirected friendship relationship
- Delete friendship relationship
- Display all users
- Display all friendships as an adjacency list
- Find mutual friends between two users
- Generate friend recommendations from friends-of-friends
- Show the shortest connection path between two users using BFS
- Show the furthest simple connection path between two users using DFS
- Save and load data with SQLite database persistence
- Display an interactive relationship graph with selectable and draggable nodes

## Code Structure

- `User.java` stores user data such as ID, name, username, major, and interest.
- `SocialGraph.java` stores the adjacency list and contains graph operations.
- `Friendship.java` stores one undirected friendship pair for database saving.
- `SocialGraphDatabase.java` creates the SQLite tables and saves/loads users and friendships.
- `NewBasedController.java` connects the JavaFX screen to the graph and database.
- `Main.java` starts the JavaFX app through `SocialGraphApp`.
- `lib/charts-master` contains the vendored JavaFX chart library used by the graph visualization. Maven compiles it automatically.

## JavaFX UI

The JavaFX screen covers the required features:

- Add user
- Select a user from the table for editing
- Save edited user data
- Delete selected user
- Add friendship
- Delete friendship
- Display all users
- Display adjacency list
- Find mutual friends
- Generate friend recommendations
- Show connection path
- Generate BFS shortest path and DFS furthest path with one button
- View and interact with the graph visualization

The graph logic is still kept in `SocialGraph.java`, so the UI only calls reusable methods such as `getAllUsers()`, `getMutualFriends()`, `getFriendRecommendations()`, `findBfsShortestPath()`, and `findDfsFurthestPath()`.

## Database

The app uses SQLite through the `sqlite-jdbc` Maven dependency. No separate database server is needed.

When the app starts, it creates this file in the project folder if it does not already exist:

```text
social_network_graph.db
```

The database setup is handled in `SocialGraphDatabase.java` with these tables:

```sql
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    major TEXT NOT NULL,
    interest TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS friendships (
    first_username TEXT NOT NULL,
    second_username TEXT NOT NULL,
    PRIMARY KEY (first_username, second_username),
    FOREIGN KEY (first_username) REFERENCES users(username)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (second_username) REFERENCES users(username)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (first_username <> second_username)
);
```

If the database is empty, the app inserts the sample users and friendships. After that, add, edit, delete, and relation changes are saved back to SQLite.

## Graph Paths

The friendship graph is unweighted, so BFS is the correct shortest-path algorithm. BFS checks users level by level, so the first time it reaches the target user, that path has the fewest friendship edges.

```text
Alice -> Bob -> Diana -> Ethan -> Farah -> Gavin
```

The Analysis tab uses one `Generate Paths` button to show both results together. DFS furthest path explores each branch deeply and backtracks to check simple paths between two users. It keeps the longest path found and does not repeat users in the same path, so cycles do not cause infinite recursion.

## Run

Use the Maven wrapper:

```powershell
.\mvnw.cmd javafx:run
```

Do not run the app with only `java com.project.Main`, because JavaFX is not included inside the JDK. The Maven command above adds the required JavaFX runtime automatically.
