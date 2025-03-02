package com.todo.todojavafxsqlite;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Controller {

    @FXML
    private DatePicker inputDate;

    @FXML
    private TextField inputDescription;

    @FXML
    private VBox vboxAdd;

    @FXML
    private HBox vboxFront;

    @FXML
    private TableView<Task> TableViewTask;

    @FXML
    private TableColumn<Task, String> tableDate;

    @FXML
    private TableColumn<Task, String> tableDescription;

    @FXML
    private TableColumn<Task, String> tableStreak;

    @FXML
    private TableColumn<Task, String> tableDateDone;

    @FXML
    private TableColumn<Task, String> tableDescriptionDone;

    @FXML
    private TableView<Task> TableViewTaskDone;

    @FXML
    private Label clockLabel;

    @FXML
    private Label errorMessage;

    @FXML
    private ChoiceBox<Integer> choiceBox;

    @FXML
    private ChoiceBox<Integer> repeatTimes;

    @FXML
    void closeAddTask(ActionEvent event) {
        closeTaskWindow();
    }

    @FXML
    void addFromHistory(ActionEvent event) {

    }

    @FXML
    void doneTask(ActionEvent event) {

    }

    @FXML
    void initialize() {

    }

    @FXML
    void removeTask(ActionEvent event) {

    }

    @FXML
    void addNewTask(ActionEvent event) {

    }

    @FXML
    void openTask(ActionEvent event) {
        vboxAdd.setVisible(true);
        vboxFront.setVisible(false);
    }

    private void closeTaskWindow() {
        vboxAdd.setVisible(false);
        vboxFront.setVisible(true);
    }
}