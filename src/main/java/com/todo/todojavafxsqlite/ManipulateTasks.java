package com.todo.todojavafxsqlite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("CallToPrintStackTrace")
public class ManipulateTasks {
    private ObservableList<Task> doneTasksList = FXCollections.observableArrayList();
    private ObservableList<Task> ongoingTasksList = FXCollections.observableArrayList();
    private Connection conn;
    private LocalDate dateNow = LocalDate.now();

    public Boolean ConnectDb() {
        // connect to sqlite
        try {
            if (conn == null || conn.isClosed()) {
                // Using the SQLite JDBC driver
                conn = DriverManager.getConnection("jdbc:sqlite:TaskDB.db");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        // close connection
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getAddFromHistory(TableView<Task> TableViewTaskDone) {
        // Add from done tasks
        Task selectedTask = TableViewTaskDone.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;

        String description = selectedTask.getDescription();
        // add 7 days
        String newDate = LocalDate.now().plusDays(7).toString();
        int repeat = selectedTask.getRepeatValue();
        Integer repeatNoTimes = selectedTask.getRepeatTimes();
        String taskId = selectedTask.getId();
        Task task = new Task(taskId, description, newDate, selectedTask.getStreakValue(), newDate, repeat, true, repeatNoTimes, false, taskId, selectedTask.getRepeatable());
        // delete the old task
        deleteTaskByParentId(selectedTask.getParentId());
        // save the task and create repeated tasks if there are any
        createRepeatedTasks(task, repeat, repeatNoTimes, description, newDate);
    }

    public void getAddNewTask(TextField inputDescription, DatePicker inputDate, ChoiceBox<Integer> choiceBox, ChoiceBox<Integer> repeatTimes) {
        // Add new tasks
        String description = inputDescription.getText();
        String date = inputDate.getValue().toString();
        Integer repeatNoTimes = repeatTimes.getValue();
        Integer repeat = choiceBox.getValue();
        // check if task is repeatable
        boolean repeatable = repeatTimes.getValue() > 0;
        // generate a new id
        String taskId = UUID.randomUUID().toString();
        Task task = new Task(taskId, description, date, 0, date, repeat, true, repeatNoTimes, false, taskId, repeatable);
        // save the task and create repeated tasks if there are any
        createRepeatedTasks(task, repeat, repeatNoTimes, description, date);
    }
    private void createRepeatedTasks(Task task, int repeat, int repeatNoTimes, String description, String startDate) {
        // Add repeating tasks
        String newDate = startDate;
        String parentId = task.getId();
        // save the parent to SQLite
        saveTaskToDatabase(task);
        task.printInfo();
        // loop the tasks as many times as inputted
        for (int i = 1; i < repeatNoTimes; i++) {
            // add days between tasks
            newDate = LocalDate.parse(newDate).plusDays(repeat).toString();
            // sets a new description and add a index number after
            String newDescription = description + " (" + i + ")";
            // generate a new id
            String newId = UUID.randomUUID().toString();
            // sets the task object
            Task repeatedTask = new Task(newId, newDescription, newDate, 0, newDate, 0, false, 0, false,parentId,true);
            // save the child elements to SQLite
            saveTaskToDatabase(repeatedTask);
            repeatedTask.printInfo();
        }
    }
    public void updateStreak(Task selectedTask, Label streakField, VBox streakPopup, VBox vboxAdd, HBox vboxFront, Controller controller) {
        // update the streak
        String parentId = selectedTask.getParentId();
        // find the task with the parentId and update streak-counter or if too late it will reset
        Task foundTask = doneTasksList.stream()
                .filter(task -> task.getId().equals(parentId))
                .findFirst()
                .orElse(null);
        if (foundTask != null) {
            // get the last date and convert to LocalDate
            String dateLast = foundTask.getDateLast();
            LocalDate lastDate = LocalDate.parse(dateLast);
            // checks if the date is too late
            if (dateNow.isAfter(lastDate)) {
                // reset the streak
                foundTask.resetStreak();
                // update the database
                updateTaskInDatabase(foundTask);
                System.out.println("Du var for sent ute");
            } else {
                // add to the streak
                foundTask.addStreak();
                // update the database
                updateTaskInDatabase(foundTask);
                System.out.println("Oppdatert streak:" + foundTask.getStreakValue());
            }
        } else {
            System.out.println("Fant ikke oppgave");
        }
        // sets milestones after an amount of streaks
        Integer streak = foundTask != null ? foundTask.getStreakValue() : null;
        if (streak != null) {
            Set<Integer> streakMilestones = Set.of(3, 5, 10, 15, 20, 25, 30);
            if (streakMilestones.contains(streak)) {
                streakField.setText(streak.toString());
                // set next view
                streakPopup.setVisible(true);
                vboxAdd.setVisible(false);
                vboxFront.setVisible(false);
            }
        }
        controller.fetchTasks();
    }
    public void fetchData() {
        // fetch the tasks
        try {
            String query = "SELECT * FROM Task";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            // clear lists of previous data
            doneTasksList.clear();
            ongoingTasksList.clear();
            // while loop to add tasks
            while (rs.next()) {
                String id = rs.getString("id");
                String description = rs.getString("description");
                String date = rs.getString("date");
                int streak = rs.getInt("streak");
                String dateLast = rs.getString("dateLast");
                int repeat = rs.getInt("repeat");
                boolean mainTask = rs.getBoolean("mainTask");
                int repeatTimes = rs.getInt("repeatTimes");
                boolean done = rs.getBoolean("done");
                String parentId = rs.getString("parentId");
                Boolean repeatable = rs.getBoolean("repeatable");
                // gets a task
                Task task = new Task(id, description, date, streak, dateLast, repeat, mainTask, repeatTimes, done, parentId, repeatable);
                // add task and sort it into different lists
                if (task.getDone()) {
                    doneTasksList.add(task);
                } else if (!task.getDone()) {
                    ongoingTasksList.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTaskInDatabase(Task task) {
        // update task in SQLite
        try {
            String query = "UPDATE Task SET description = ?, date = ?, streak = ?, dateLast = ?, repeat = ?, mainTask = ?, repeatTimes = ?, done = ?, parentId = ?, repeatable = ? WHERE id = ?";
            // "?" is placeholder, need to set index below, what type and what property
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, task.getDescription());
            pstmt.setString(2, task.getTaskDate());
            pstmt.setInt(3, task.getStreakValue());
            pstmt.setString(4, task.getDateLast());
            pstmt.setInt(5, task.getRepeatValue());
            pstmt.setBoolean(6, task.getMainTask());
            pstmt.setInt(7, task.getRepeatTimes());
            pstmt.setBoolean(8, task.getDone());
            pstmt.setString(9, task.getParentId());
            pstmt.setBoolean(10, task.getRepeatable());
            pstmt.setString(11, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveTaskToDatabase(Task task) {
        // save a new task to the database, same here: "?" is placeholder
        try {
            String query = "INSERT INTO Task (id, description, date, streak, dateLast, repeat, mainTask, repeatTimes, done, parentId, repeatable) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, task.getId());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getTaskDate());
            pstmt.setInt(4, task.getStreakValue());
            pstmt.setString(5, task.getDateLast());
            pstmt.setInt(6, task.getRepeatValue());
            pstmt.setBoolean(7, task.getMainTask());
            pstmt.setInt(8, task.getRepeatTimes());
            pstmt.setBoolean(9, task.getDone());
            pstmt.setString(10, task.getParentId());
            pstmt.setBoolean(11, task.getRepeatable());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Boolean deleteTaskByParentId(String parentId) {
        // delete task based on parentId, needed to delete all tasks with that parentId like repeating tasks
        try {
            String query = "DELETE FROM Task WHERE parentId = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, parentId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteTaskFromDatabase(Task task) {
        // delete a single task from SQLite from id
        try {
            String query = "DELETE FROM Task WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Task> getOngoingTasksList() {
        return ongoingTasksList;
    }

    public ObservableList<Task> getDoneTasksList() {
        return doneTasksList;
    }
}