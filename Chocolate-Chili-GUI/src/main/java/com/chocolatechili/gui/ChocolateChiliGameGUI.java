package com.chocolatechili.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChocolateChiliGameGUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/dashboard.fxml"));

        Scene scene =
                new Scene(loader.load(), 1400, 900);

        stage.setTitle(
                "Chocolate Chili AI Dashboard");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
