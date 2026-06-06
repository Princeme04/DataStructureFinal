package com.project;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class SocialGraph {
    private final Map<String, User> users;

    /*
     * The adjacency list stores each username and the usernames connected to it.
     * This is efficient for a social network because most users are friends with
     * only a small part of the whole network.
     */
    private final Map<String, Set<String>> adjacencyList;

    public SocialGraph() {
        users = new LinkedHashMap<>();
        adjacencyList = new LinkedHashMap<>();
    }

    public boolean addUser(User user) {
        String username = normalizeUsername(user.getUsername());

        if (username.isEmpty() || users.containsKey(username)) {
            return false;
        }

        users.put(username, user);
        adjacencyList.put(username, new LinkedHashSet<>());
        return true;
    }

    public boolean updateUser(String currentUsername, User updatedUser) {
        String oldUsername = normalizeUsername(currentUsername);
        String newUsername = normalizeUsername(updatedUser.getUsername());

        if (!users.containsKey(oldUsername) || newUsername.isEmpty()) {
            return false;
        }

        if (!oldUsername.equals(newUsername) && users.containsKey(newUsername)) {
            return false;
        }

        Map<String, User> updatedUsers = new LinkedHashMap<>();
        Map<String, Set<String>> updatedAdjacencyList = new LinkedHashMap<>();

        for (Map.Entry<String, User> entry : users.entrySet()) {
            String username = entry.getKey();
            String usernameToStore = username.equals(oldUsername) ? newUsername : username;
            User userToStore = username.equals(oldUsername) ? updatedUser : entry.getValue();

            updatedUsers.put(usernameToStore, userToStore);
        }

        for (Map.Entry<String, Set<String>> entry : adjacencyList.entrySet()) {
            String username = entry.getKey();
            String usernameToStore = username.equals(oldUsername) ? newUsername : username;
            Set<String> friends = new LinkedHashSet<>();

            for (String friendUsername : entry.getValue()) {
                friends.add(friendUsername.equals(oldUsername) ? newUsername : friendUsername);
            }

            updatedAdjacencyList.put(usernameToStore, friends);
        }

        users.clear();
        users.putAll(updatedUsers);
        adjacencyList.clear();
        adjacencyList.putAll(updatedAdjacencyList);
        return true;
    }

    public boolean deleteUser(String username) {
        String normalizedUsername = normalizeUsername(username);

        if (!users.containsKey(normalizedUsername)) {
            return false;
        }

        users.remove(normalizedUsername);
        adjacencyList.remove(normalizedUsername);

        for (Set<String> friends : adjacencyList.values()) {
            friends.remove(normalizedUsername);
        }

        return true;
    }

    public boolean addFriendship(String username1, String username2) {
        String firstUsername = normalizeUsername(username1);
        String secondUsername = normalizeUsername(username2);

        if (firstUsername.equals(secondUsername)) {
            return false;
        }

        if (!users.containsKey(firstUsername) || !users.containsKey(secondUsername)) {
            return false;
        }

        /*
         * Friendship is undirected, so the relationship is stored twice:
         * A -> B and B -> A.
         */
        boolean addedToFirstUser = adjacencyList.get(firstUsername).add(secondUsername);
        boolean addedToSecondUser = adjacencyList.get(secondUsername).add(firstUsername);

        return addedToFirstUser || addedToSecondUser;
    }

    public boolean removeFriendship(String username1, String username2) {
        String firstUsername = normalizeUsername(username1);
        String secondUsername = normalizeUsername(username2);

        if (!areFriends(firstUsername, secondUsername)) {
            return false;
        }

        adjacencyList.get(firstUsername).remove(secondUsername);
        adjacencyList.get(secondUsername).remove(firstUsername);
        return true;
    }

    public boolean containsUser(String username) {
        return users.containsKey(normalizeUsername(username));
    }

    public boolean areFriends(String username1, String username2) {
        String firstUsername = normalizeUsername(username1);
        String secondUsername = normalizeUsername(username2);

        return adjacencyList.containsKey(firstUsername)
                && adjacencyList.get(firstUsername).contains(secondUsername)
                && adjacencyList.containsKey(secondUsername)
                && adjacencyList.get(secondUsername).contains(firstUsername);
    }

    public User getUser(String username) {
        return users.get(normalizeUsername(username));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public List<Friendship> getAllFriendships() {
        List<Friendship> friendships = new ArrayList<>();

        for (String username : adjacencyList.keySet()) {
            for (String friendUsername : adjacencyList.get(username)) {
                if (username.compareTo(friendUsername) < 0) {
                    friendships.add(new Friendship(username, friendUsername));
                }
            }
        }

        return friendships;
    }

    public int getNextUserId() {
        int maxId = 0;

        for (User user : users.values()) {
            maxId = Math.max(maxId, user.getId());
        }

        return maxId + 1;
    }

    public Set<User> getFriendsOfUser(String username) {
        String normalizedUsername = normalizeUsername(username);

        if (!adjacencyList.containsKey(normalizedUsername)) {
            return Collections.emptySet();
        }

        return convertUsernamesToUsers(adjacencyList.get(normalizedUsername));
    }

    public Set<User> getMutualFriends(String username1, String username2) {
        String firstUsername = normalizeUsername(username1);
        String secondUsername = normalizeUsername(username2);

        if (!adjacencyList.containsKey(firstUsername) || !adjacencyList.containsKey(secondUsername)) {
            return Collections.emptySet();
        }

        Set<String> mutualFriendUsernames = new LinkedHashSet<>(adjacencyList.get(firstUsername));
        mutualFriendUsernames.retainAll(adjacencyList.get(secondUsername));

        return convertUsernamesToUsers(mutualFriendUsernames);
    }

    public Set<User> getFriendRecommendations(String username) {
        String normalizedUsername = normalizeUsername(username);

        if (!adjacencyList.containsKey(normalizedUsername)) {
            return Collections.emptySet();
        }

        Set<String> directFriends = adjacencyList.get(normalizedUsername);
        Set<String> recommendedUsernames = new LinkedHashSet<>();

        /*
         * Recommendation logic:
         * look at friends-of-friends, then remove the user and current friends.
         */
        for (String friendUsername : directFriends) {
            for (String friendOfFriendUsername : adjacencyList.get(friendUsername)) {
                boolean isSameUser = friendOfFriendUsername.equals(normalizedUsername);
                boolean isAlreadyFriend = directFriends.contains(friendOfFriendUsername);

                if (!isSameUser && !isAlreadyFriend) {
                    recommendedUsernames.add(friendOfFriendUsername);
                }
            }
        }

        return convertUsernamesToUsers(recommendedUsernames);
    }

    public List<User> findConnectionPath(String startUsername, String targetUsername) {
        return findBfsShortestPath(startUsername, targetUsername);
    }

    public List<User> findBfsShortestPath(String startUsername, String targetUsername) {
        String start = normalizeUsername(startUsername);
        String target = normalizeUsername(targetUsername);

        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(target)) {
            return Collections.emptyList();
        }

        if (start.equals(target)) {
            return List.of(users.get(start));
        }

        /*
         * The graph is unweighted, so BFS gives the shortest connection path by
         * number of friendship edges. DFS would find a path, but not necessarily
         * the shortest one.
         */
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> previousUser = new HashMap<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String currentUsername = queue.poll();

            for (String friendUsername : adjacencyList.get(currentUsername)) {
                if (visited.contains(friendUsername)) {
                    continue;
                }

                visited.add(friendUsername);
                previousUser.put(friendUsername, currentUsername);

                if (friendUsername.equals(target)) {
                    return buildPath(previousUser, start, target);
                }

                queue.add(friendUsername);
            }
        }

        return Collections.emptyList();
    }

    public List<User> findDfsFurthestPath(String startUsername, String targetUsername) {
        String start = normalizeUsername(startUsername);
        String target = normalizeUsername(targetUsername);

        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(target)) {
            return Collections.emptyList();
        }

        if (start.equals(target)) {
            return List.of(users.get(start));
        }

        /*
         * DFS explores one branch deeply before backtracking. Here it checks all
         * simple paths between two users and keeps the longest one found.
         */
        Set<String> visited = new LinkedHashSet<>();
        List<String> currentPath = new ArrayList<>();
        List<String> bestPath = new ArrayList<>();

        findDfsFurthestPath(start, target, visited, currentPath, bestPath);

        return convertUsernamesToUserPath(bestPath);
    }

    public String displayAdjacencyList() {
        StringBuilder builder = new StringBuilder();

        for (String username : adjacencyList.keySet()) {
            builder.append(users.get(username).getName())
                    .append(" -> ");

            Set<User> friends = getFriendsOfUser(username);

            if (friends.isEmpty()) {
                builder.append("No friends");
            } else {
                builder.append(formatUserNames(friends));
            }

            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    public String formatUserPath(List<User> path) {
        List<String> names = new ArrayList<>();

        for (User user : path) {
            names.add(user.getName());
        }

        return String.join(" -> ", names);
    }

    private List<User> buildPath(Map<String, String> previousUser, String start, String target) {
        List<User> path = new ArrayList<>();
        String currentUsername = target;

        while (currentUsername != null) {
            path.add(users.get(currentUsername));

            if (currentUsername.equals(start)) {
                break;
            }

            currentUsername = previousUser.get(currentUsername);
        }

        Collections.reverse(path);
        return path;
    }

    private void findDfsFurthestPath(
            String currentUsername,
            String targetUsername,
            Set<String> visited,
            List<String> currentPath,
            List<String> bestPath
    ) {
        visited.add(currentUsername);
        currentPath.add(currentUsername);

        if (currentUsername.equals(targetUsername)) {
            if (currentPath.size() > bestPath.size()) {
                bestPath.clear();
                bestPath.addAll(currentPath);
            }
        } else {
            for (String friendUsername : adjacencyList.get(currentUsername)) {
                if (!visited.contains(friendUsername)) {
                    findDfsFurthestPath(friendUsername, targetUsername, visited, currentPath, bestPath);
                }
            }
        }

        visited.remove(currentUsername);
        currentPath.remove(currentPath.size() - 1);
    }

    private List<User> convertUsernamesToUserPath(List<String> usernames) {
        List<User> path = new ArrayList<>();

        for (String username : usernames) {
            User user = users.get(username);

            if (user != null) {
                path.add(user);
            }
        }

        return path;
    }

    private Set<User> convertUsernamesToUsers(Set<String> usernames) {
        Set<User> result = new LinkedHashSet<>();

        for (String username : usernames) {
            User user = users.get(username);

            if (user != null) {
                result.add(user);
            }
        }

        return result;
    }

    private String formatUserNames(Set<User> usersToFormat) {
        List<String> names = new ArrayList<>();

        for (User user : usersToFormat) {
            names.add(user.getName());
        }

        return String.join(", ", names);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }

        return username.trim().toLowerCase();
    }
}
