package com.todo.todojavafxsqlite;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

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
    private TableColumn<Task, String> tableRepeat;

    @FXML
    private TableColumn<Task, String> tableRepeatDone;

    @FXML
    private Label streakField;

    @FXML
    private VBox streakPopup;

    private ObservableList<Task> doneTasksList = FXCollections.observableArrayList();
    private ObservableList<Task> ongoingTasksList = FXCollections.observableArrayList();

    @FXML
    void closeStreakMessage(ActionEvent event) {
        closeTaskWindow();
    }

    @FXML
    void closeAddTask(ActionEvent event) {
        closeTaskWindow();
    }

    @FXML
    void addFromHistory(ActionEvent event) {
        Task selectedTask = TableViewTaskDone.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;
        TableViewTaskDone.getItems().remove(selectedTask);
        String description = selectedTask.getDescription();
        LocalDate newDate = LocalDate.now().plusDays(7);
        Integer repeat = selectedTask.getRepeatValue();
        Integer repeatNoTimes = selectedTask.getRepeatTimes();
        int taskId = selectedTask.getId();
        Integer streak = selectedTask.getRepeatTimes().equals(0) ? selectedTask.getStreakValue() : 0;
        Task task = new Task(taskId, description, newDate.toString(), streak, newDate, repeat, true, repeatNoTimes);
        Task previousTask = task;

        for (int i = 1; i < repeatNoTimes; i++) {
            newDate = newDate.plusDays(repeat);
            String newDescription = description + " (" + i + ")";
            Task repeatedTask = new Task(taskId, newDescription, newDate.toString(), 0, newDate, repeat, false, repeatNoTimes);
            previousTask.setNextTask(repeatedTask);
            previousTask = repeatedTask;
        }
        ongoingTasksList.add(task);
        TableViewTask.getItems().add(task);
        closeTaskWindow();
        testLists();
    }
    void testLists() {
        System.out.println("Ongoing:");
        Integer num = 1;
        for (Task task : ongoingTasksList) {
            System.out.print(num + ". ");
            task.printInfo();
            num++;
        }
        System.out.println("Done:");
        for (Task task : doneTasksList) {
            System.out.print(num + ". ");
            task.printInfo();
            num++;
        }
    }


    @FXML
    void addNewTask(ActionEvent event) {
        String description = inputDescription.getText();
        LocalDate date = inputDate.getValue();
        Integer repeatNoTimes = repeatTimes.getValue();
        Integer repeat = choiceBox.getValue();
        if (description.isEmpty() || date == null || date.isBefore(LocalDate.now())) {
            errorMessage.setText("Ugyldig input");
            return;
        }
        int taskId = TableViewTask.getItems().size() + 1;
        Task task = new Task(taskId, description, date.toString(), 0, date, repeat, true, repeatNoTimes);
        Task previousTask = task;

        for (int i = 1; i < repeatNoTimes; i++) {
            LocalDate dateAfterAdding = date.plusDays(repeat);
            String newDescription = description + " (" + i + ")";
            Task repeatedTask = new Task(taskId, newDescription, dateAfterAdding.toString(), 0, dateAfterAdding, repeat, false, repeatNoTimes);
            previousTask.setNextTask(repeatedTask);
            previousTask = repeatedTask;
            date = dateAfterAdding;
        }
        ongoingTasksList.add(task);
        TableViewTask.getItems().add(task);
        closeTaskWindow();
        testLists();
    }


    @FXML
    void doneTask(ActionEvent event) {
        Task selectedTask = TableViewTask.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;
        LocalDate dateNow = LocalDate.now();
        TableViewTask.getItems().remove(selectedTask);
        if(selectedTask.getMainTask()) {
            TableViewTaskDone.getItems().add(selectedTask);
            doneTasksList.add(selectedTask);
        }
        Task foundTask = doneTasksList.stream()
                .filter(task -> task.getId() == selectedTask.getId())
                .findFirst()
                .orElse(null);
        if (foundTask != null) {
            if(dateNow.isAfter(selectedTask.getDateLast())){
                foundTask.resetStreak();
                System.out.println("Du var for sent ute");
            } else {
                foundTask.addStreak();
            }
            System.out.println("streaks: " + foundTask.getStreakValue());
        } else {
            System.out.println("Fant ikke oppgave");
        }
        Task nextTask = selectedTask.getNextTask();
        if (nextTask != null) {
            TableViewTask.getItems().add(nextTask);
        }
        Integer streak = foundTask.getStreakValue();
        Set<Integer> streakMilestones = Set.of(3, 5, 10, 15, 20, 25, 30);
        if (streakMilestones.contains(streak)) {
            streakField.setText(streak.toString());
            openStreakMessage();
        }
    }

    @FXML
    void initialize() {
        tableDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        tableDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        tableRepeat.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        TableViewTask.getItems().forEach(task -> System.out.println(task.getDescription() + " - " + task.getTaskDate() + " - " + task.getRepeatValue()));
        tableRepeatDone.setCellValueFactory(cellData -> cellData.getValue().repeatProperty().asString());
        tableDateDone.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        tableDescriptionDone.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        startClock();
        choiceBox.getItems().addAll(0,1,2,7,30);
        repeatTimes.getItems().addAll(0,2,5,10,52,100);
        choiceBox.setValue(0);
        repeatTimes.setValue(0);
    }

    @FXML
    void removeTask(ActionEvent event) {
        Task selectedTask = TableViewTask.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            TableViewTask.getItems().remove(selectedTask);
        }
    }

    @FXML
    void openTask(ActionEvent event) {
        errorMessage.setText("");
        vboxAdd.setVisible(true);
        vboxFront.setVisible(false);
    }

    private void openStreakMessage() {
        vboxAdd.setVisible(false);
        vboxFront.setVisible(false);
        streakPopup.setVisible(true);
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