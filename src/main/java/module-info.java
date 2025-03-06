module com.todo.todojavafxsqlite {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.todo.todojavafxsqlite to javafx.fxml;
    exports com.todo.todojavafxsqlite;
}