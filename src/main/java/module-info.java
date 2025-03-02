module com.todo.todojavafxsqlite {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.todo.todojavafxsqlite to javafx.fxml;
    exports com.todo.todojavafxsqlite;
}