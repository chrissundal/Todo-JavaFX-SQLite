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

    public Boolean ConnectDb() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection("jdbc:sqlite:TaskDB.db");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void testLists() {
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

    public void getAddFromHistory(TableView<Task> TableViewTaskDone) {
        Task selectedTask = TableViewTaskDone.getSelectionModel().getSelectedItem();
        if (selectedTask == null) return;

        String description = selectedTask.getDescription();
        String newDate = LocalDate.now().plusDays(7).toString();
        int repeat = selectedTask.getRepeatValue();
        Integer repeatNoTimes = selectedTask.getRepeatTimes();
        String taskId = selectedTask.getId();
        Task task = new Task(taskId, description, newDate, selectedTask.getStreakValue(), newDate, repeat, true, repeatNoTimes, false,taskId);
        deleteTaskFromDatabase(selectedTask);
        createRepeatedTasks(task, repeat, repeatNoTimes, description, newDate);
    }

    public void getAddNewTask(TextField inputDescription, DatePicker inputDate, ChoiceBox<Integer> choiceBox, ChoiceBox<Integer> repeatTimes, Label errorMessage) {
        String description = inputDescription.getText();
        String date = inputDate.getValue().toString();
        Integer repeatNoTimes = repeatTimes.getValue();
        Integer repeat = choiceBox.getValue();
        if (LocalDate.parse(date).isBefore(LocalDate.now())) {
            errorMessage.setText("Dato må være etter dagens dato");
            return;
        }
        String taskId = UUID.randomUUID().toString();
        Task task = new Task(taskId, description, date, 0, date, repeat, true, repeatNoTimes, false, taskId);
        createRepeatedTasks(task, repeat, repeatNoTimes, description, date);
    }
    private void createRepeatedTasks(Task task, int repeat, int repeatNoTimes, String description, String startDate) {
        String newDate = startDate;
        saveTaskToDatabase(task);

        for (int i = 1; i < repeatNoTimes; i++) {
            newDate = LocalDate.parse(newDate).plusDays(repeat).toString();
            String newDescription = description + " (" + i + ")";
            String newId = UUID.randomUUID().toString();
            Task repeatedTask = new Task(newId, newDescription, newDate, 0, newDate, 0, false, 0, false,task.getId());
            saveTaskToDatabase(repeatedTask);
        }
    }
    public void updateStreak(Task selectedTask, Label streakField, VBox streakPopup, VBox vboxAdd, HBox vboxFront) {
        LocalDate dateNow = LocalDate.now();
        String parentId = selectedTask.getParentId();
        Task foundTask = doneTasksList.stream()
                .filter(task -> task.getId().equals(parentId))
                .findFirst()
                .orElse(null);
        if (foundTask != null) {
            String dateLast = foundTask.getDateLast();
            LocalDate lastDate = LocalDate.parse(dateLast);
            if (dateNow.isAfter(lastDate)) {
                foundTask.resetStreak();
                updateTaskInDatabase(foundTask);
                System.out.println("Du var for sent ute");
            } else {
                foundTask.addStreak();
                updateTaskInDatabase(foundTask);
                System.out.println("Oppdatert streak:" + foundTask.getStreakValue());
            }
        } else {
            System.out.println("Fant ikke oppgave");
        }

        Integer streak = foundTask != null ? foundTask.getStreakValue() : null;
        if (streak != null) {
            Set<Integer> streakMilestones = Set.of(3, 5, 10, 15, 20, 25, 30);
            if (streakMilestones.contains(streak)) {
                streakField.setText(streak.toString());
                streakPopup.setVisible(true);
                vboxAdd.setVisible(false);
                vboxFront.setVisible(false);
            }
        }
    }
    public void fetchData() {
        try {
            String query = "SELECT * FROM Task";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            doneTasksList.clear();
            ongoingTasksList.clear();

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
                Task task = new Task(id, description, date, streak, dateLast, repeat, mainTask, repeatTimes, done, parentId);
                if (task.getDone() && task.getMainTask()) {
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
        try {
            String query = "UPDATE Task SET description = ?, date = ?, streak = ?, dateLast = ?, repeat = ?, mainTask = ?, repeatTimes = ?, done = ?, parentId = ? WHERE id = ?";
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
            pstmt.setString(10, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveTaskToDatabase(Task task) {
        try {
            String query = "INSERT INTO Task (id, description, date, streak, dateLast, repeat, mainTask, repeatTimes, done, parentId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTaskFromDatabase(Task task) {
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