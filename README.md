# Beekeeping Management System

A Java-based application for managing beekeeping operations including hives, tasks, and employee reporting.

## Features

- User authentication (Admin and Employee roles)
- Hive management (view and edit hive status)
- Task management (create, assign, and complete tasks)
- Reporting system (employees can submit reports with related hives and tasks)
- Parallel processing for improved performance with large datasets

## Getting Started

### Prerequisites

- Java 8 or higher
- Any Java IDE or command line tools

### Running the Application

1. Compile all Java files:
   ```
   javac src/*.java
   ```

2. Run the main application:
   ```
   java -cp src Main
   ```

### Login Credentials

- Admin User:
  - Email: admin@example.com
  - Password: admin123

- Employee User:
  - Email: employee@example.com
  - Password: emp123

## System Architecture

The application uses a menu-driven console interface with the following key components:

- **DataManager**: Central data management with thread-safe operations
- **AdminManager**: Administrative functions (user management, task creation, etc.)
- **EmployeeManager**: Employee functions (task completion, report submission)
- **Parallel Processing**: Multi-threaded processing for performance optimization

## License

This project is proprietary software developed for educational purposes.