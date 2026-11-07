 
# 🏋️ GymForge – Gym Management System

**GymForge** is a Java-based Gym Management System designed to simplify gym operations through a centralized application. It provides user management, authentication, and database integration to support the management of gym-related information.

The project is developed using Java, JDBC, MySQL, and web technologies, with Apache Tomcat used for server deployment.

## 🚀 Features

- **User Management:** Manage gym user information and account details.
- **User Authentication:** Validate login credentials and control access.
- **Database Connectivity:** Store and retrieve application data using MySQL and JDBC.
- **Input Validation:** Validate user information and handle invalid inputs.
- **Exception Handling:** Custom exception handling for user-related errors.
- **Admin Dashboard:** Provides an administrative interface for managing gym operations.
- **Web Interface:** User-friendly interface built using HTML, CSS, JavaScript, and JSP.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Backend development |
| JDBC | Database connectivity |
| MySQL | Relational database |
| Jakarta Servlets | Server-side request handling |
| JSP | Dynamic web pages |
| HTML5 | Web page structure |
| CSS3 | Styling and layout |
| JavaScript | Client-side interactivity |
| Apache Tomcat 10.1 | Web application server |
| Git & GitHub | Version control |

## 📂 Project Structure

```text
GymForge/
│
├── src/
│   ├── db/
│   │   └── DatabaseConnection.java
│   │
│   ├── exception/
│   │   └── InvalidUserException.java
│   │
│   ├── model/
│   │   └── User.java
│   │
│   ├── service/
│   │   ├── UserOperations.java
│   │   └── UserService.java
│   │
│   ├── servlet/
│   │   ├── LoginServlet.java
│   │   ├── AdminLoginServlet.java
│   │   └── AdminLogoutServlet.java
│   │
│   ├── server/
│   │   └── GymServer.java
│   │
│   └── Main.java
│
├── WebContent/
│   ├── WEB-INF/
│   │   ├── web.xml
│   │   └── lib/
│   │
│   ├── css/
│   ├── js/
│   ├── admin.html
│   ├── admin-login.html
│   └── dashboard.jsp
│
└── README.md
```

*Note: The structure above is a representative overview. Folder and file names may vary in the actual project.*

## ⚙️ Requirements

Before running the application, make sure you have installed:

- Java Development Kit (JDK 21)
- MySQL Server
- Apache Tomcat 10.1
- MySQL Connector/J
- Git
- Visual Studio Code or another Java IDE

## 💻 Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/7vyshnav07-bot/Gym-Management-System.git
```

### 2. Navigate to the Project Directory

```bash
cd Gym-Management-System
```

### 3. Configure the Database

1. Start your MySQL server.
2. Create the database used by the application.
3. Configure the database connection details in `DatabaseConnection.java`.
4. Ensure the required tables are created.

Update the database credentials according to your local MySQL configuration.

### 4. Configure Apache Tomcat

1. Install Apache Tomcat 10.1.
2. Configure the Tomcat server in your development environment.
3. Ensure the MySQL Connector/J library is available in the application's `WEB-INF/lib` directory.
4. Deploy the GymForge web application to Tomcat.

### 5. Run the Application

Start the Apache Tomcat server and open the application in your browser:

```text
http://localhost:8080/GymForge/
```

## 🔐 Security Considerations

- User authentication is handled through server-side Java logic.
- Database operations use JDBC.
- User input should be validated before processing.
- Database credentials should be configured securely and should not be committed to GitHub.

## 🎯 Project Objectives

- Simplify gym management operations.
- Provide an organized approach to managing user information.
- Implement object-oriented programming principles using Java.
- Demonstrate database connectivity using JDBC and MySQL.
- Develop a web-based application using Java server-side technologies.
- Improve application reliability through validation and exception handling.

## 🔮 Future Enhancements

- Membership plan management and renewal notifications.
- Trainer scheduling and assignment.
- Attendance tracking.
- Payment history and invoice generation.
- Reports and analytics dashboard.
- Improved role-based access control.

## 👨‍💻 Developer

**Vyshnav Manoharan**

GitHub: [@7vyshnav07-bot](https://github.com/7vyshnav07-bot)

## 📄 License

This project is developed for academic and educational purposes.
