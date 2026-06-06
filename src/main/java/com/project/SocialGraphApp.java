package com.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SocialGraphApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(SocialGraphApp.class.getResource("NewBased.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Social Network Graph");
        stage.setScene(scene);
        stage.show();
    }
}
