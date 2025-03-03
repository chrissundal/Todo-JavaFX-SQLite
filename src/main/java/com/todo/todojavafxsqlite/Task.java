package com.todo.todojavafxsqlite;
import javafx.beans.property.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Date;

public class Task {
    private IntegerProperty id;
    private StringProperty description;
    private StringProperty date;
    private LocalDate dateLast;
    private Integer streak;
    private IntegerProperty repeat;
    private BooleanProperty mainTask;
    private IntegerProperty repeatTimes;
    private Task nextTask;

    public Task(int id, String description, String date, Integer streak, LocalDate dateLast, Integer repeat, Boolean mainTask, Integer repeatTimes) {
        this.id = new SimpleIntegerProperty(id);
        this.description = new SimpleStringProperty(description);
        this.date = new SimpleStringProperty(date);
        this.streak = streak;
        this.dateLast = dateLast;
        this.repeat = new SimpleIntegerProperty(repeat);
        this.mainTask = new SimpleBooleanProperty(mainTask);
        this.repeatTimes = new SimpleIntegerProperty(repeatTimes);
    }

    public IntegerProperty idProperty() {
        return id;
    }
    public StringProperty descriptionProperty() {
        return description;
    }
    public StringProperty dateProperty() {
        return date;
    }
    public IntegerProperty repeatProperty() {
        return repeat;
    }
    public void addStreak() {
        LocalDate dateNow = LocalDate.now();
        if (streak == 0) {
            streak = 1;
        } else {
            streak += 1;
        }
        dateLast = dateNow;
    }

    public void printInfo() {
        System.out.println("id: " + id + " Beskrivelse: " + description.get() + " Deadline: " + date.get() + " Antall streaks: " + streak + " Dato sist ferdig: " + dateLast + " Antall ganger: " + repeat.get() + " Hovedtask: " + mainTask.get());
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
    public Task getNextTask() {
        return nextTask;
    }

    public void setNextTask(Task nextTask) {
        this.nextTask = nextTask;
    }

    public Integer getStreakValue() {
        return streak;
    }

    public int getId() {
        return id.get();
    }

    public ChronoLocalDate getDateLast() {
        return dateLast;
    }

    public void resetStreak() {
        streak = 0;
    }
}
