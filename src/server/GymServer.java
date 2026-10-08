package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import db.DatabaseConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GymServer {

    private static final int PORT = 8080;

    private static final Path WEB_FOLDER =
            Paths.get("src", "web")
                    .toAbsolutePath()
                    .normalize();

    public static void main(String[] args) throws Exception {

        HttpServer server =
                HttpServer.create(
                        new InetSocketAddress(PORT),
                        0
                );

        // =====================================
        // API ENDPOINTS
        // =====================================

        server.createContext(
                "/register",
                GymServer::handleRegister
        );

        server.createContext(
                "/login",
                GymServer::handleLogin
        );

        server.createContext(
                "/get-trainers",
                GymServer::handleGetTrainers
        );

        server.createContext(
                "/request-trainer",
                GymServer::handleRequestTrainer
        );

        server.createContext(
                "/mark-attendance",
                GymServer::handleMarkAttendance
        );

        server.createContext(
                "/get-attendance",
                GymServer::handleGetAttendance
        );

        // NEW: UPDATE PROFILE
        server.createContext(
                "/update-profile",
                GymServer::handleUpdateProfile
        );

        // =====================================
        // WEBSITE
        // =====================================

        server.createContext(
                "/",
                GymServer::handleWebsite
        );

        server.setExecutor(null);
        server.start();

        System.out.println("=================================");
        System.out.println("       GYMFORGE JAVA SERVER");
        System.out.println("=================================");
        System.out.println("Server started on port " + PORT);
        System.out.println("Website: http://localhost:" + PORT);
        System.out.println("Web folder: " + WEB_FOLDER);
        System.out.println("Waiting for requests...");
    }


    // =====================================
    // REGISTRATION
    // =====================================

    private static void handleRegister(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJsonResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String requestBody =
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        String fullName =
                getValue(requestBody, "fullName");

        String username =
                getValue(requestBody, "username");

        String email =
                getValue(requestBody, "email");

        String phone =
                getValue(requestBody, "phone");

        String dateOfBirth =
                getValue(requestBody, "dateOfBirth");

        String country =
                getValue(requestBody, "country");

        String gender =
                getValue(requestBody, "gender");

        String password =
                getValue(requestBody, "password");

        if (fullName.isBlank()
                || username.isBlank()
                || email.isBlank()
                || phone.isBlank()
                || password.isBlank()) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Please fill all required fields\"}"
            );

            return;
        }

        String sql = """
                INSERT INTO users
                (
                    full_name,
                    username,
                    email,
                    phone,
                    date_of_birth,
                    country,
                    gender,
                    password
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, fullName);
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setString(5, dateOfBirth);
            statement.setString(6, country);
            statement.setString(7, gender);
            statement.setString(8, password);

            statement.executeUpdate();

            System.out.println(
                    "New user registered: " + username
            );

            sendJsonResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Registration successful\"}"
            );

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {

                sendJsonResponse(
                        exchange,
                        409,
                        "{\"success\":false,\"message\":\"Username already exists\"}"
                );

            } else {

                System.out.println(
                        "Registration error: " + e.getMessage()
                );

                sendJsonResponse(
                        exchange,
                        500,
                        "{\"success\":false,\"message\":\"Registration failed\"}"
                );
            }
        }
    }


    // =====================================
    // LOGIN
    // =====================================

    private static void handleLogin(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJsonResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String requestBody =
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        String username =
                getValue(requestBody, "username");

        String password =
                getValue(requestBody, "password");

        if (username.isBlank()
                || password.isBlank()) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Username and password are required\"}"
            );

            return;
        }

        String sql =
                "SELECT full_name, username, email, phone, "
                        + "date_of_birth, country, gender "
                        + "FROM users "
                        + "WHERE username = ? AND password = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet =
                    statement.executeQuery();

            if (resultSet.next()) {

                String fullName =
                        resultSet.getString("full_name");

                String userUsername =
                        resultSet.getString("username");

                String email =
                        resultSet.getString("email");

                String phone =
                        resultSet.getString("phone");

                String dateOfBirth =
                        resultSet.getString("date_of_birth");

                String country =
                        resultSet.getString("country");

                String gender =
                        resultSet.getString("gender");

                String jsonResponse =
                        "{"
                                + "\"success\":true,"
                                + "\"message\":\"Login successful\","
                                + "\"fullName\":\""
                                + escapeJson(fullName)
                                + "\","
                                + "\"username\":\""
                                + escapeJson(userUsername)
                                + "\","
                                + "\"email\":\""
                                + escapeJson(email)
                                + "\","
                                + "\"phone\":\""
                                + escapeJson(phone)
                                + "\","
                                + "\"dateOfBirth\":\""
                                + escapeJson(dateOfBirth)
                                + "\","
                                + "\"country\":\""
                                + escapeJson(country)
                                + "\","
                                + "\"gender\":\""
                                + escapeJson(gender)
                                + "\""
                                + "}";

                System.out.println(
                        "Login successful: " + username
                );

                sendJsonResponse(
                        exchange,
                        200,
                        jsonResponse
                );

            } else {

                System.out.println(
                        "Login failed: " + username
                );

                sendJsonResponse(
                        exchange,
                        401,
                        "{\"success\":false,\"message\":\"Invalid username or password\"}"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Login error: " + e.getMessage()
            );

            sendJsonResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Login failed\"}"
            );
        }
    }


    // =====================================
    // GET ALL TRAINERS
    // =====================================

    private static void handleGetTrainers(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJsonResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"GET method required\"}"
            );

            return;
        }

        String sql = """
                SELECT
                    id,
                    trainer_name,
                    specialization,
                    experience_years,
                    phone,
                    availability,
                    description
                FROM trainers
                ORDER BY id
                """;

        StringBuilder json =
                new StringBuilder("[");

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            boolean firstTrainer = true;

            while (resultSet.next()) {

                if (!firstTrainer) {
                    json.append(",");
                }

                firstTrainer = false;

                int id =
                        resultSet.getInt("id");

                String trainerName =
                        resultSet.getString("trainer_name");

                String specialization =
                        resultSet.getString("specialization");

                int experienceYears =
                        resultSet.getInt("experience_years");

                String phone =
                        resultSet.getString("phone");

                String availability =
                        resultSet.getString("availability");

                String description =
                        resultSet.getString("description");

                json.append("{")
                        .append("\"id\":")
                        .append(id)
                        .append(",")

                        .append("\"trainerName\":\"")
                        .append(escapeJson(trainerName))
                        .append("\",")

                        .append("\"specialization\":\"")
                        .append(escapeJson(specialization))
                        .append("\",")

                        .append("\"experienceYears\":")
                        .append(experienceYears)
                        .append(",")

                        .append("\"phone\":\"")
                        .append(escapeJson(phone))
                        .append("\",")

                        .append("\"availability\":\"")
                        .append(escapeJson(availability))
                        .append("\",")

                        .append("\"description\":\"")
                        .append(escapeJson(description))
                        .append("\"")

                        .append("}");
            }

            json.append("]");

            sendJsonResponse(
                    exchange,
                    200,
                    json.toString()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            sendJsonResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Unable to load trainers\"}"
            );
        }
    }


    // =====================================
    // REQUEST TRAINER
    // =====================================

    private static void handleRequestTrainer(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJsonResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String requestBody =
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        String username =
                getValue(requestBody, "username");

        String trainerIdText =
                getValue(requestBody, "trainerId");

        System.out.println(
                "Trainer request body: " + requestBody
        );

        System.out.println(
                "Username received: " + username
        );

        System.out.println(
                "Trainer ID received: " + trainerIdText
        );

        if (username.isBlank()
                || trainerIdText.isBlank()) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"User and trainer are required\"}"
            );

            return;
        }

        int trainerId;

        try {

            trainerId =
                    Integer.parseInt(trainerIdText);

        } catch (NumberFormatException e) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Invalid trainer ID\"}"
            );

            return;
        }

        String checkSql = """
                SELECT id
                FROM trainer_requests
                WHERE username = ?
                AND trainer_id = ?
                AND status = 'Pending'
                """;

        String insertSql = """
                INSERT INTO trainer_requests
                (
                    username,
                    trainer_id,
                    request_date,
                    status
                )
                VALUES (?, ?, CURDATE(), 'Pending')
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            try (
                    PreparedStatement checkStatement =
                            connection.prepareStatement(checkSql)
            ) {

                checkStatement.setString(1, username);
                checkStatement.setInt(2, trainerId);

                ResultSet resultSet =
                        checkStatement.executeQuery();

                if (resultSet.next()) {

                    sendJsonResponse(
                            exchange,
                            409,
                            "{\"success\":false,\"message\":\"You already have a pending request for this trainer\"}"
                    );

                    return;
                }
            }

            try (
                    PreparedStatement insertStatement =
                            connection.prepareStatement(insertSql)
            ) {

                insertStatement.setString(1, username);
                insertStatement.setInt(2, trainerId);

                insertStatement.executeUpdate();

                System.out.println(
                        "Trainer request created for "
                                + username
                );

                sendJsonResponse(
                        exchange,
                        200,
                        "{\"success\":true,\"message\":\"Trainer request sent successfully\"}"
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            sendJsonResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Unable to send trainer request\"}"
            );
        }
    }


    // =====================================
    // MARK ATTENDANCE
    // =====================================

    private static void handleMarkAttendance(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJsonResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        String requestBody =
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        String username =
                getValue(requestBody, "username");

        String status =
                getValue(requestBody, "status");

        System.out.println(
                "Attendance request body: " + requestBody
        );

        if (username.isBlank()) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Username is required\"}"
            );

            return;
        }

        if (status.isBlank()) {
            status = "Present";
        }

        String sql = """
                INSERT INTO attendance
                (
                    username,
                    attendance_date,
                    status
                )
                VALUES (?, CURDATE(), ?)
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);
            statement.setString(2, status);

            statement.executeUpdate();

            System.out.println(
                    "Attendance marked for: " + username
            );

            sendJsonResponse(
                    exchange,
                    200,
                    "{\"success\":true,\"message\":\"Attendance marked successfully\"}"
            );

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {

                sendJsonResponse(
                        exchange,
                        409,
                        "{\"success\":false,\"message\":\"Attendance already marked for today\"}"
                );

            } else {

                e.printStackTrace();

                sendJsonResponse(
                        exchange,
                        500,
                        "{\"success\":false,\"message\":\"Unable to mark attendance\"}"
                );
            }
        }
    }


    // =====================================
    // GET ATTENDANCE HISTORY
    // =====================================

    private static void handleGetAttendance(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJsonResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"GET method required\"}"
            );

            return;
        }

        String query =
                exchange.getRequestURI()
                        .getQuery();

        String username = "";

        if (query != null) {

            String[] parameters =
                    query.split("&");

            for (String parameter : parameters) {

                if (parameter.startsWith("username=")) {

                    username =
                            URLDecoder.decode(
                                    parameter.substring(9),
                                    StandardCharsets.UTF_8
                            );

                    break;
                }
            }
        }

        if (username.isBlank()) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Username is required\"}"
            );

            return;
        }

        String sql = """
                SELECT
                    attendance_date,
                    status
                FROM attendance
                WHERE username = ?
                ORDER BY attendance_date DESC
                """;

        StringBuilder json =
                new StringBuilder("[");

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            ResultSet resultSet =
                    statement.executeQuery();

            boolean firstRecord = true;

            while (resultSet.next()) {

                if (!firstRecord) {
                    json.append(",");
                }

                firstRecord = false;

                String date =
                        resultSet.getString(
                                "attendance_date"
                        );

                String status =
                        resultSet.getString(
                                "status"
                        );

                json.append("{")
                        .append("\"date\":\"")
                        .append(escapeJson(date))
                        .append("\",")

                        .append("\"status\":\"")
                        .append(escapeJson(status))
                        .append("\"")

                        .append("}");
            }

            json.append("]");

            sendJsonResponse(
                    exchange,
                    200,
                    json.toString()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            sendJsonResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Unable to load attendance\"}"
            );
        }
    }


    // =====================================
    // UPDATE MEMBER PROFILE
    // =====================================

    private static void handleUpdateProfile(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            sendJsonResponse(
                    exchange,
                    405,
                    "{\"success\":false,\"message\":\"POST method required\"}"
            );

            return;
        }

        // -------------------------------------
        // READ REQUEST BODY
        // -------------------------------------

        String requestBody =
                new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        System.out.println(
                "Profile update request: " + requestBody
        );


        // -------------------------------------
        // GET VALUES
        // -------------------------------------

        String username =
                getValue(requestBody, "username");

        String fullName =
                getValue(requestBody, "fullName");

        String email =
                getValue(requestBody, "email");

        String phone =
                getValue(requestBody, "phone");

        String dateOfBirth =
                getValue(requestBody, "dateOfBirth");

        String country =
                getValue(requestBody, "country");

        String gender =
                getValue(requestBody, "gender");

        String password =
                getValue(requestBody, "password");


        // -------------------------------------
        // VALIDATION
        // -------------------------------------

        if (username.isBlank()) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Username is required\"}"
            );

            return;
        }

        if (fullName.isBlank()
                || email.isBlank()
                || phone.isBlank()) {

            sendJsonResponse(
                    exchange,
                    400,
                    "{\"success\":false,\"message\":\"Name, email and phone are required\"}"
            );

            return;
        }


        // -------------------------------------
        // SQL UPDATE
        // -------------------------------------

        String sql;

        if (password.isBlank()) {

            sql = """
                    UPDATE users
                    SET
                        full_name = ?,
                        email = ?,
                        phone = ?,
                        date_of_birth = ?,
                        country = ?,
                        gender = ?
                    WHERE username = ?
                    """;

        } else {

            sql = """
                    UPDATE users
                    SET
                        full_name = ?,
                        email = ?,
                        phone = ?,
                        date_of_birth = ?,
                        country = ?,
                        gender = ?,
                        password = ?
                    WHERE username = ?
                    """;
        }


        // -------------------------------------
        // DATABASE UPDATE
        // -------------------------------------

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, dateOfBirth);
            statement.setString(5, country);
            statement.setString(6, gender);

            if (password.isBlank()) {

                statement.setString(7, username);

            } else {

                statement.setString(7, password);
                statement.setString(8, username);
            }


            int rowsUpdated =
                    statement.executeUpdate();


            // -------------------------------------
            // CHECK UPDATE RESULT
            // -------------------------------------

            if (rowsUpdated > 0) {

                System.out.println(
                        "Profile updated successfully: "
                                + username
                );

                sendJsonResponse(
                        exchange,
                        200,
                        "{\"success\":true,\"message\":\"Profile updated successfully\"}"
                );

            } else {

                System.out.println(
                        "No user found for profile update: "
                                + username
                );

                sendJsonResponse(
                        exchange,
                        404,
                        "{\"success\":false,\"message\":\"User not found\"}"
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();

            sendJsonResponse(
                    exchange,
                    500,
                    "{\"success\":false,\"message\":\"Unable to update profile\"}"
            );
        }
    }


    // =====================================
    // WEBSITE HANDLER
    // =====================================

    private static void handleWebsite(
            HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        String requestPath =
                exchange.getRequestURI()
                        .getPath();

        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        String relativePath =
                requestPath.substring(1);

        relativePath =
                URLDecoder.decode(
                        relativePath,
                        StandardCharsets.UTF_8
                );

        Path requestedFile =
                WEB_FOLDER
                        .resolve(relativePath)
                        .normalize();

        if (!requestedFile.startsWith(WEB_FOLDER)) {

            sendResponse(
                    exchange,
                    403,
                    "Access denied."
            );

            return;
        }

        if (!Files.exists(requestedFile)
                || !Files.isRegularFile(requestedFile)) {

            sendResponse(
                    exchange,
                    404,
                    "File not found."
            );

            return;
        }

        try {

            byte[] fileBytes =
                    Files.readAllBytes(requestedFile);

            String contentType =
                    getContentType(requestedFile);

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    contentType
            );

            exchange.sendResponseHeaders(
                    200,
                    fileBytes.length
            );

            try (
                    OutputStream output =
                            exchange.getResponseBody()
            ) {

                output.write(fileBytes);
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "Unable to load file."
            );
        }
    }


    // =====================================
    // CONTENT TYPE
    // =====================================

    private static String getContentType(
            Path file) {

        String fileName =
                file.getFileName()
                        .toString()
                        .toLowerCase();

        if (fileName.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (fileName.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (fileName.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        if (fileName.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }

        if (fileName.endsWith(".png")) {
            return "image/png";
        }

        if (fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")) {

            return "image/jpeg";
        }

        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }

        if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        }

        if (fileName.endsWith(".ico")) {
            return "image/x-icon";
        }

        return "application/octet-stream";
    }


    // =====================================
    // GET VALUE FROM JSON
    // Supports strings and numbers
    // =====================================

    private static String getValue(
            String json,
            String key) {

        String searchKey =
                "\"" + key + "\"";

        int keyIndex =
                json.indexOf(searchKey);

        if (keyIndex == -1) {
            return "";
        }

        int colonIndex =
                json.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return "";
        }

        int start =
                colonIndex + 1;

        while (start < json.length()
                && Character.isWhitespace(
                        json.charAt(start))) {

            start++;
        }

        if (start >= json.length()) {
            return "";
        }

        // Quoted string value
        if (json.charAt(start) == '"') {

            start++;

            StringBuilder value =
                    new StringBuilder();

            boolean escaped = false;

            for (int i = start;
                 i < json.length();
                 i++) {

                char character =
                        json.charAt(i);

                if (escaped) {

                    value.append(character);
                    escaped = false;

                } else if (character == '\\') {

                    escaped = true;

                } else if (character == '"') {

                    break;

                } else {

                    value.append(character);
                }
            }

            return value.toString();
        }

        // Numeric or unquoted value
        int end =
                json.indexOf(",", start);

        if (end == -1) {

            end =
                    json.indexOf("}", start);
        }

        if (end == -1) {
            end = json.length();
        }

        return json.substring(start, end)
                .trim()
                .replace("\"", "");
    }


    // =====================================
    // ESCAPE JSON
    // =====================================

    private static String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    // =====================================
    // SEND JSON RESPONSE
    // =====================================

    private static void sendJsonResponse(
            HttpExchange exchange,
            int statusCode,
            String json)
            throws IOException {

        addCorsHeaders(exchange);

        byte[] response =
                json.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                response.length
        );

        try (
                OutputStream output =
                        exchange.getResponseBody()
        ) {

            output.write(response);
        }
    }


    // =====================================
    // SEND NORMAL RESPONSE
    // =====================================

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String message)
            throws IOException {

        byte[] response =
                message.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                response.length
        );

        try (
                OutputStream output =
                        exchange.getResponseBody()
        ) {

            output.write(response);
        }
    }


    // =====================================
    // CORS HEADERS
    // =====================================

    private static void addCorsHeaders(
            HttpExchange exchange) {

        Headers headers =
                exchange.getResponseHeaders();

        headers.set(
                "Access-Control-Allow-Origin",
                "*"
        );

        headers.set(
                "Access-Control-Allow-Methods",
                "GET, POST, OPTIONS"
        );

        headers.set(
                "Access-Control-Allow-Headers",
                "Content-Type"
        );
    }
}