package com.todo.todojavafxsqlite;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

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
    @FXML private ProgressBar progressBar;
    @FXML private Label statusMessage;
    @FXML private VBox repeatTaskCheckboxVbox;
    @FXML private HBox repeatTaskHbox;
    @FXML private CheckBox repeatTaskCheckbox;

    private final ManipulateTasks manipulateTasks = new ManipulateTasks();

    @FXML
    void initialize() {
        tableDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                LocalDate.parse(cellData.getValue().dateProperty().get())
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault()))
        ));
        tableDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        tableRepeat.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        tableRepeatDone.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        tableDateDone.setCellValueFactory(cellData -> new SimpleStringProperty(
                LocalDate.parse(cellData.getValue().dateProperty().get())
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault()))
        ));
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
    void repeatTaskCheckbox() {
        repeatTaskHbox.setVisible(true);
        repeatTaskCheckboxVbox.setVisible(false);
    }
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
            setErrorMessageText("Vennligst skriv beskrivelse og velg dato");
            return;
        }
        if(inputDate.getValue() == null) {
            setErrorMessageText("Vennligst velg dato");
            return;
        }
        if (inputDescription.getText().isEmpty()) {
            setErrorMessageText("Vennligst skriv beskrivelse");
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
        selectedTask.setDone(true);
        manipulateTasks.updateTaskInDatabase(selectedTask);
        fetchTasks();
        manipulateTasks.updateStreak(selectedTask, streakField, streakPopup, vboxAdd, vboxFront,this);
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
        repeatTaskHbox.setVisible(false);
        repeatTaskCheckboxVbox.setVisible(true);
        repeatTaskCheckbox.setSelected(false);
    }
    @FXML
    void deleteDone() {
        Task selectedTask = TableViewTaskDone.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;
        if(manipulateTasks.deleteTaskByParentId(selectedTask.getParentId())){
            setErrorMessageText("Oppgave slettet");
        }
        fetchTasks();
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
        Task nextTask = manipulateTasks.getOngoingTasksList().stream()
                .filter(task -> LocalDate.parse(task.getTaskDate()).isAfter(LocalDate.now()))
                .findFirst().orElse(null);
        if (nextTask != null) {
            LocalDate date = LocalDate.parse(nextTask.getTaskDate());
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault());
            String formattedDate = date.format(formatter);
            statusMessage.setText("Neste oppgave: " + nextTask.getDescription() + " - " + " Deadline: " + formattedDate);
        } else {
            statusMessage.setText("Ingen neste oppgave");
        }

        ObservableList<Task> ongoingTasksList = FXCollections.observableArrayList(
                manipulateTasks.getOngoingTasksList().stream()
                        .filter(task -> LocalDate.parse(task.getTaskDate()).isEqual(LocalDate.now()) || LocalDate.parse(task.getTaskDate()).isBefore(LocalDate.now()))
                        .collect(Collectors.toList())
        );
        ongoingTasksList.sort((a,b) -> LocalDate.parse(a.getTaskDate()).compareTo(LocalDate.parse(b.getTaskDate())));
        TableViewTask.setItems(ongoingTasksList);
        ObservableList<Task> doneTasksList = FXCollections.observableArrayList(
            manipulateTasks.getDoneTasksList().stream()
                .filter(task -> task.getMainTask())
                .collect(Collectors.toList())
        );
        doneTasksList.sort((a,b) -> LocalDate.parse(a.getTaskDate()).compareTo(LocalDate.parse(b.getTaskDate())));
        TableViewTaskDone.setItems(doneTasksList);
        int totalTasks = manipulateTasks.getDoneTasksList().size() + manipulateTasks.getOngoingTasksList().size();
        progressBar.setProgress((float) manipulateTasks.getDoneTasksList().size() / (float) totalTasks);
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

    private void setErrorMessageText(String text) {
        errorMessage.setText(text);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    errorMessage.setText("");
                    timer.cancel();
                });
            }
        }, 3000);
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