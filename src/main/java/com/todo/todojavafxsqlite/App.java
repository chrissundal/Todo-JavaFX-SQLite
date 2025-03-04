package com.todo.todojavafxsqlite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private Controller controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("To Do!");
        stage.setScene(scene);
        stage.show();
        controller = fxmlLoader.getController();
    }
    @Override
    public void stop() throws Exception {
        if (controller != null) {
            controller.setCloseConnection();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}