package com.todo.todojavafxsqlite;
import javafx.beans.property.*;
import java.time.LocalDate;

public class Task {
    private final String id;
    private final StringProperty description;
    private final StringProperty date;
    private String dateLast;
    private Integer streak;
    private final IntegerProperty repeat;
    private final BooleanProperty mainTask;
    private final IntegerProperty repeatTimes;
    private Boolean done;
    private final String parentId;
    private final BooleanProperty repeatable;

    public Task(String id, String description, String date, Integer streak, String dateLast, Integer repeat, Boolean mainTask, Integer repeatTimes, Boolean done, String parentId, Boolean repeatable) {
        this.id = id;
        this.description = new SimpleStringProperty(description);
        this.date = new SimpleStringProperty(date);
        this.streak = streak;
        this.dateLast = dateLast;
        this.repeat = new SimpleIntegerProperty(repeat);
        this.mainTask = new SimpleBooleanProperty(mainTask);
        this.repeatTimes = new SimpleIntegerProperty(repeatTimes);
        this.done = done;
        this.parentId = parentId;
        this.repeatable = new SimpleBooleanProperty(repeatable);
    }

    public String getParentId() {
        return parentId;
    }
    public StringProperty descriptionProperty() {
        return description;
    }
    public StringProperty dateProperty() {
        return date;
    }
    public BooleanProperty repeatProperty() {
        return repeatable;
    }

    public void addStreak() {
        streak += 1;
        dateLast = LocalDate.now().toString();
    }

    public void printInfo() {
        System.out.println("id: " + id + " Beskrivelse: " + description.get() + " Deadline: " + date.get() + " Antall streaks: " + streak + " Dato sist ferdig: " + dateLast + " Antall ganger: " + repeat.get() + " Hovedtask: " + mainTask.get() + " Repeterbar: " + repeatable.get());
    }

    public int getRepeatValue() {
        return repeat.get();
    }

    public boolean getMainTask() {
        return mainTask.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getTaskDate() {
        return date.get();
    }
    public Integer getRepeatTimes() {
        return repeatTimes.get();
    }

    public Integer getStreakValue() {
        return streak;
    }

    public String getId() {
        return id;
    }

    public String getDateLast() {
        return dateLast;
    }

    public void resetStreak() {
        dateLast = LocalDate.now().toString();
        streak = 0;
    }

    public boolean getDone() {
        return done;
    }

    public void setDone(boolean bool) {
        done = bool;
    }

    public boolean getRepeatable() {
        return repeatable.get();
    }
}
