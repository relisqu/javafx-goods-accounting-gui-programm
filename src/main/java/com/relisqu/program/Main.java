package com.relisqu.program;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/mainScreen.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Программа для 1с-конкурса");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
        MainScreen controller = loader.getController();
        controller.init();
    }
}
