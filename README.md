# Todo JavaFX SQLite


A JavaFX-based Todo List application that uses SQLite as its database.

## Table of Contents


* [Overview](#overview)
* [Technologies Used](#technologies-used)
* [Screenshots](#screenshots)
* [Features](#features)
* [Classes and Files](#classes-and-files)
* [Acknowledgments](#acknowledgments)
* [Getting Started](#getting-started)


## Overview


This application allows users to create, read, update, and delete (CRUD) todo tasks. It features a simple and intuitive interface built with JavaFX, and stores data in a SQLite database.

## Technologies Used


* JavaFX for the graphical user interface
* SQLite for data storage
* Java 8 or later for development

## Screenshots


### Main screen
![Screenshot 1](/src/main/resources/com/todo/todojavafxsqlite/screenshots/screenshot1.png)

### Add task screen
![Screenshot 2](/src/main/resources/com/todo/todojavafxsqlite/screenshots/screenshot2.png)

### Interval choice
![Screenshot 3](/src/main/resources/com/todo/todojavafxsqlite/screenshots/screenshot3.png)

### New repeated task added
![Screenshot 4](/src/main/resources/com/todo/todojavafxsqlite/screenshots/screenshot4.png)

### Task completed, progressbar updates
![Screenshot 5](/src/main/resources/com/todo/todojavafxsqlite/screenshots/screenshot5.png)



## Features


| Feature                | Description                                                                            |
|------------------------|----------------------------------------------------------------------------------------|
| Create Tasks           | Create new todo tasks with descriptions and due dates                                  |
| Create Repeating Tasks | Create new repeating todo tasks, that you can set customizable intervals for repeating |
| Add existing Tasks     | Add existing tasks from history of earlier completed tasks                             |
| Delete Tasks           | Delete tasks                                                                           |
| Streak Tracking        | The application keeps track of consecutive days a task is completed                    |


## Classes and Files


| File                   | Description                                                                          |
|------------------------|--------------------------------------------------------------------------------------|
| `view.fxml`            | Main view that controlles the UI and handles user input                              |
| `App.java`             | Main class that starts the application and stops the DB when exiting                 |
| `Controller.java`      | Handles user input and updates the UI accordingly                                    |
| `Task.java`            | Represents a single todo task with attributes like description, due date, and streak |
| `ManipulateTasks.java` | Provides methods for CRUD operations on tasks                                        |
| `TaskDB.db`            | SQLite database file for storing task data                                           |

## Acknowledgments


I would like to thank the OpenJFX team for their hard work and dedication to creating and maintaining the JavaFX library, which was used extensively in this project. Their efforts have made it possible for me to build a high-quality and visually appealing GUI application.

* [OpenJFX Website](https://openjfx.io/)
* [OpenJFX GitHub Repository](https://github.com/openjdk/jfx)
* [SQLite Website](https://www.sqlite.org/)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)
* [SQLite Xerial JDBC](https://github.com/xerial/sqlite-jdbc)

## Getting Started


### Prerequisites

* Java 8 or later installed on your system
* A compatible IDE (Eclipse, IntelliJ IDEA)

### Running the Application

1. Clone the repository to your local machine.
2. Open the project in your preferred IDE.
3. Run the application using the `App.java` file.
