package com.todo.todojavafxsqlite;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Date;

public class Controller {

    @FXML private DatePicker inputDate;
    @FXML private TextField inputDescription;
    @FXML private VBox vboxAdd;
    @FXML private HBox vboxFront;
    @FXML private TableView<Task> TableViewTask;
    @FXML private TableColumn<Task, String> tableDate;
    @FXML private TableColumn<Task, String> tableDescription;
    @FXML private TableColumn<Task, String> tableDateDone;
    @FXML private TableColumn<Task, String> tableDescriptionDone;
    @FXML private TableView<Task> TableViewTaskDone;
    @FXML private Label clockLabel;
    @FXML private Label errorMessage;
    @FXML private ChoiceBox<Integer> choiceBox;
    @FXML private ChoiceBox<Integer> repeatTimes;
    @FXML private TableColumn<Task, String> tableRepeat;
    @FXML private TableColumn<Task, String> tableRepeatDone;
    @FXML private Label streakField;
    @FXML private VBox streakPopup;

    private final ManipulateTasks manipulateTasks = new ManipulateTasks();

    @FXML
    void closeStreakMessage() {
        closeTaskWindow();
    }

    @FXML
    void closeAddTask() {
        closeTaskWindow();
    }

    @FXML
    void addFromHistory() {
        manipulateTasks.getAddFromHistory(TableViewTaskDone);
        closeTaskWindow();
        fetchTasks();
    }

    @FXML
    void addNewTask() {
        if(inputDate.getValue() == null && inputDescription.getText().isEmpty()) {
            errorMessage.setText("Vennligst skriv beskrivelse og velg dato");
            return;
        }
        if(inputDate.getValue() == null) {
            errorMessage.setText("Vennligst velg dato");
            return;
        }
        if (inputDescription.getText().isEmpty()) {
            errorMessage.setText("Vennligst skriv beskrivelse");
            return;
        }
        manipulateTasks.getAddNewTask(inputDescription, inputDate, choiceBox, repeatTimes, errorMessage);
        closeTaskWindow();
        fetchTasks();
    }

    @FXML
    void doneTask() {
        Task selectedTask = TableViewTask.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;
        if(selectedTask.getMainTask()) {
            selectedTask.setDone(true);
            manipulateTasks.updateTaskInDatabase(selectedTask);
            fetchTasks();
            manipulateTasks.updateStreak(selectedTask, streakField, streakPopup, vboxAdd, vboxFront);
        } else {
            manipulateTasks.updateStreak(selectedTask, streakField, streakPopup, vboxAdd, vboxFront);
            manipulateTasks.deleteTaskFromDatabase(selectedTask);
        }
        fetchTasks();
    }

    @FXML
    void initialize() {
        tableDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        tableDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        tableRepeat.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        tableRepeatDone.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        tableDateDone.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        tableDescriptionDone.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        choiceBox.getItems().addAll(0,1,2,7,30);
        repeatTimes.getItems().addAll(0,2,5,10,52,100);
        choiceBox.setValue(0);
        repeatTimes.setValue(0);
        startClock();
        connectToDatabase();
        fetchTasks();
    }

    @FXML
    void removeTask() {
        Task selectedTask = TableViewTask.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            TableViewTask.getItems().remove(selectedTask);
            manipulateTasks.deleteTaskFromDatabase(selectedTask);
        }
    }

    @FXML
    void openTask() {
        errorMessage.setText("");
        vboxAdd.setVisible(true);
        vboxFront.setVisible(false);
    }
    private void connectToDatabase() {
        if(manipulateTasks.ConnectDb()){
            System.out.println("Database connected");
        } else {
            System.out.println("Database connection failed");
        }
    }
    public void fetchTasks() {
        TableViewTask.getItems().clear();
        TableViewTaskDone.getItems().clear();
        manipulateTasks.fetchData();
        manipulateTasks.testLists();
        TableViewTask.setItems(manipulateTasks.getOngoingTasksList());
        TableViewTaskDone.setItems(manipulateTasks.getDoneTasksList());
    }
    void setCloseConnection() {
        manipulateTasks.closeConnection();
    }
    private void closeTaskWindow() {
        vboxAdd.setVisible(false);
        vboxFront.setVisible(true);
        streakPopup.setVisible(false);
        choiceBox.setValue(0);
        repeatTimes.setValue(0);
        inputDate.setValue(null);
        inputDescription.setText("");
    }

    private void startClock() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                clockLabel.setText(new Date().toLocaleString());
            }
        };
        timer.start();
    }
}