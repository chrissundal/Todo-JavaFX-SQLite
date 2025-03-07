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
// JavaFX import from view
    // TextFields
    @FXML private DatePicker inputDate;
    @FXML private TextField inputDescription;
    // Lists to store tasks in tables
    @FXML private TableView<Task> TableViewTask;
    @FXML private TableView<Task> TableViewTaskDone;
    // Table columns
    @FXML private TableColumn<Task, String> tableDate;
    @FXML private TableColumn<Task, String> tableRepeat;
    @FXML private TableColumn<Task, String> tableDescription;
    @FXML private TableColumn<Task, String> tableDateDone;
    @FXML private TableColumn<Task, String> tableRepeatDone;
    @FXML private TableColumn<Task, String> tableDescriptionDone;
    // labels
    @FXML private Label clockLabel;
    @FXML private Label errorMessage;
    @FXML private Label streakField;
    @FXML private Label statusMessage;
    // ChoiceBoxes
    @FXML private ChoiceBox<Integer> choiceBox;
    @FXML private ChoiceBox<Integer> repeatTimes;
    // progress bar
    @FXML private ProgressBar progressBar;
    // disable / enable views
    @FXML private VBox streakPopup;
    @FXML private VBox repeatTaskCheckboxVbox;
    @FXML private HBox repeatTaskHbox;
    @FXML private VBox vboxAdd;
    @FXML private HBox vboxFront;
    // checkboxes
    @FXML private CheckBox repeatTaskCheckbox;

    // new instance of class
    private final ManipulateTasks manipulateTasks = new ManipulateTasks();

    @FXML
    void initialize() {
    // fills cells in tables with data
        // convert the date to Locale date
        tableDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                LocalDate.parse(cellData.getValue().dateProperty().get())
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault()))
        ));
        tableDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        tableRepeat.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        tableRepeatDone.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        // convert the date to Locale date
        tableDateDone.setCellValueFactory(cellData -> new SimpleStringProperty(
                LocalDate.parse(cellData.getValue().dateProperty().get())
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault()))
        ));
        tableDescriptionDone.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        // sets the value of the choicebox
        choiceBox.getItems().addAll(0,1,2,7,30);
        repeatTimes.getItems().addAll(0,2,5,10,52,100);
        // sets default value
        choiceBox.setValue(0);
        repeatTimes.setValue(0);
        // starts clock and database
        startClock();
        connectToDatabase();
        fetchTasks();
    }

    @FXML
    void repeatTaskCheckbox() {
        // checkbox to set the repeated tasks
        repeatTaskHbox.setVisible(true);
        repeatTaskCheckboxVbox.setVisible(false);
    }
    @FXML
    void closeStreakMessage() {
        closeTaskWindow();
    }

    @FXML
    void closeAddTask() {closeTaskWindow();}

    @FXML
    void addFromHistory() {
        // adds a selected task from the done tasks, and moves it to ongoing
        manipulateTasks.getAddFromHistory(TableViewTaskDone);
        closeTaskWindow();
        fetchTasks();
    }

    @FXML
    void addNewTask() {
        // adds a new task and checks for errors
        if (inputDate.getValue().isBefore(LocalDate.now())) {
            setErrorMessageText("Dato må være etter dagens dato");
            return;
        }
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
        manipulateTasks.getAddNewTask(inputDescription, inputDate, choiceBox, repeatTimes);
        closeTaskWindow();
        fetchTasks();
    }

    @FXML
    void doneTask() {
        // finds the selected task
        Task selectedTask = TableViewTask.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;
        // sets the task as done
        selectedTask.setDone(true);
        // update the value of the task in the database
        manipulateTasks.updateTaskInDatabase(selectedTask);
        fetchTasks();
        // updates the streak
        manipulateTasks.updateStreak(selectedTask, streakField, streakPopup, vboxAdd, vboxFront,this);
    }

    @FXML
    void removeTask() {
        // finds the selected task
        Task selectedTask = TableViewTask.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            // deletes selected task from ongoing
            TableViewTask.getItems().remove(selectedTask);
            manipulateTasks.deleteTaskFromDatabase(selectedTask);
        }
    }

    @FXML
    void openTask() {
        // open up the add task page
        errorMessage.setText("");
        vboxAdd.setVisible(true);
        vboxFront.setVisible(false);
        repeatTaskHbox.setVisible(false);
        repeatTaskCheckboxVbox.setVisible(true);
        repeatTaskCheckbox.setSelected(false);
    }
    @FXML
    void deleteDone() {
        // finds the selected task
        Task selectedTask = TableViewTaskDone.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;
        // delete the selected task inside done tasks by parentId
        if(manipulateTasks.deleteTaskByParentId(selectedTask.getParentId())){
            setErrorMessageText("Oppgave slettet");
        }
        fetchTasks();
    }
    private void connectToDatabase() {
        // connects to the database
        if(manipulateTasks.ConnectDb()){
            System.out.println("Database connected");
        } else {
            System.out.println("Database connection failed");
        }
    }
    public void fetchTasks() {
        // clears the tables
        TableViewTask.getItems().clear();
        TableViewTaskDone.getItems().clear();
        // fetches the tasks
        manipulateTasks.fetchData();
        // filter the ongoingTasks to update the next task
        Task nextTask = manipulateTasks.getOngoingTasksList().stream()
                .filter(task -> LocalDate.parse(task.getTaskDate()).isAfter(LocalDate.now()))
                .findFirst().orElse(null);
        if (nextTask != null) {
            // formats the date
            LocalDate date = LocalDate.parse(nextTask.getTaskDate());
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault());
            String formattedDate = date.format(formatter);
            // sets the text
            statusMessage.setText("Neste oppgave: " + nextTask.getDescription() + " - " + " Deadline: " + formattedDate);
        } else {
            statusMessage.setText("Ingen neste oppgave");
        }
        // filter the ongoing tasks by earlier or today's date
        ObservableList<Task> ongoingTasksList = FXCollections.observableArrayList(
                manipulateTasks.getOngoingTasksList().stream()
                        .filter(task -> LocalDate.parse(task.getTaskDate()).isEqual(LocalDate.now()) || LocalDate.parse(task.getTaskDate()).isBefore(LocalDate.now()))
                        .collect(Collectors.toList())
        );
        // sort the tasks by date
        ongoingTasksList.sort((a,b) -> LocalDate.parse(a.getTaskDate()).compareTo(LocalDate.parse(b.getTaskDate())));
        // adds the tasks to the table
        TableViewTask.setItems(ongoingTasksList);
        // filter the done dates and adds it if it's a main task
        ObservableList<Task> doneTasksList = FXCollections.observableArrayList(
            manipulateTasks.getDoneTasksList().stream()
                .filter(task -> task.getMainTask())
                .collect(Collectors.toList())
        );
        // sort the tasks by date
        doneTasksList.sort((a,b) -> LocalDate.parse(a.getTaskDate()).compareTo(LocalDate.parse(b.getTaskDate())));
        // adds the tasks to the table
        TableViewTaskDone.setItems(doneTasksList);
        // set the progressbar value
        int totalTasks = manipulateTasks.getDoneTasksList().size() + manipulateTasks.getOngoingTasksList().size();
        progressBar.setProgress((float) manipulateTasks.getDoneTasksList().size() / (float) totalTasks);
    }
    void setCloseConnection() {
        // Closes the connection in Manipulate tasks class
        manipulateTasks.closeConnection();
    }
    private void closeTaskWindow() {
        // Closes the task window, resets input fields, and sets the visibility of add task and front VBoxes
        vboxAdd.setVisible(false);
        vboxFront.setVisible(true);
        streakPopup.setVisible(false);
        choiceBox.setValue(0);
        repeatTimes.setValue(0);
        inputDate.setValue(null);
        inputDescription.setText("");
    }

    private void setErrorMessageText(String text) {
        // sets the error message
        errorMessage.setText(text);
        Timer timer = new Timer();
        // removes the error message after 3 seconds
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
        // starts the clock
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                clockLabel.setText(new Date().toLocaleString());
            }
        };
        timer.start();
    }
}