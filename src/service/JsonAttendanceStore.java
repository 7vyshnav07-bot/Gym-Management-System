
package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonAttendanceStore {

    private static final Path FILE =
            Paths.get("data", "attendance.json").toAbsolutePath();

    private JsonAttendanceStore() {}

    private static class Record {
        String username;
        String date;
        String status;

        Record(String username, String date, String status) {
            this.username = username;
            this.date = date;
            this.status = status;
        }
    }

    public static synchronized boolean markAttendance(
            String username, String status) throws IOException {

        List<Record> records = readRecords();
        String today = LocalDate.now().toString();

        for (Record r : records) {
            if (r.username.equalsIgnoreCase(username)
                    && r.date.equals(today)) {
                return false;
            }
        }

        records.add(new Record(username, today, status));
        writeRecords(records);
        return true;
    }

    public static synchronized String getAttendanceJson(
            String username) throws IOException {

        StringBuilder result = new StringBuilder("[");
        boolean first = true;

        for (Record r : readRecords()) {
            if (!r.username.equalsIgnoreCase(username)) {
                continue;
            }

            if (!first) result.append(",");
            first = false;

            result.append("{\"date\":\"")
                  .append(escape(r.date))
                  .append("\",\"status\":\"")
                  .append(escape(r.status))
                  .append("\"}");
        }

        return result.append("]").toString();
    }


    /** Read-only count for the administrator overview; uses the same JSON source as member attendance. */
    public static synchronized int getAllAttendanceCount() throws IOException {
        return readRecords().size();
    }

    private static List<Record> readRecords() throws IOException {
        List<Record> records = new ArrayList<>();

        Files.createDirectories(FILE.getParent());

        if (!Files.exists(FILE)) {
            Files.writeString(FILE, "[]", StandardCharsets.UTF_8);
            return records;
        }

        String json = Files.readString(FILE, StandardCharsets.UTF_8).trim();

        if (json.isEmpty() || json.equals("[]")) {
            return records;
        }

        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IOException("Invalid attendance JSON file: " + FILE);
        }

        Pattern objectPattern = Pattern.compile("\\{([^{}]*)\\}");
        Matcher objects = objectPattern.matcher(json);

        while (objects.find()) {
            String object = objects.group(1);

            String username = getString(object, "username");
            String date = getString(object, "date");
            String status = getString(object, "status");

            if (username == null || date == null || status == null) {
                throw new IOException("Invalid attendance record in " + FILE);
            }

            records.add(new Record(username, date, status));
        }

        return records;
    }

    private static String getString(String object, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(object);

        if (!matcher.find()) return null;

        return unescape(matcher.group(1));
    }

    private static void writeRecords(List<Record> records)
            throws IOException {

        Files.createDirectories(FILE.getParent());

        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < records.size(); i++) {
            Record r = records.get(i);

            json.append("  {\"username\":\"")
                .append(escape(r.username))
                .append("\",\"date\":\"")
                .append(escape(r.date))
                .append("\",\"status\":\"")
                .append(escape(r.status))
                .append("\"}");

            if (i < records.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("]\n");

        Path temp = Files.createTempFile(
                FILE.getParent(), "attendance-", ".tmp");

        try {
            Files.writeString(temp, json.toString(), StandardCharsets.UTF_8);

            try {
                Files.move(temp, FILE,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            if (escaped) {
                switch (c) {
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    case '/': result.append('/'); break;
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    default: result.append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                result.append(c);
            }
        }

        if (escaped) result.append('\\');
        return result.toString();
    }
}
