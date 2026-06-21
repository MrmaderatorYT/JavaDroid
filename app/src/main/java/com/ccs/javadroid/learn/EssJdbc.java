package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssJdbc {
static void add(Course s) {
    Chapter ch = new Chapter(
            "JDBC та патерн DAO",
            "JDBC Essentials and the DAO Pattern");
    ch.add(materialJdbc());
    ch.add(materialPrepared());
    ch.add(materialDao());
    s.add(ch);
}

private static Lesson materialJdbc() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Підключення через JDBC"));
    uk.add(LessonBlock.paragraph(
            "JDBC — стандартний API Java для роботи з реляційними БД. Потік: "
            + "DriverManager → Connection → Statement → ResultSet."));
    uk.add(LessonBlock.code(
        "import java.sql.*;\n"
        + "\n"
        + "try (Connection con = DriverManager.getConnection(\n"
        + "        \"jdbc:sqlite:app.db\")) {\n"
        + "    Statement st = con.createStatement();\n"
        + "    ResultSet rs = st.executeQuery(\"SELECT id, name FROM users\");\n"
        + "    while (rs.next()) {\n"
        + "        int id = rs.getInt(\"id\");\n"
        + "        String name = rs.getString(\"name\");\n"
        + "        System.out.println(id + \": \" + name);\n"
        + "    }\n"
        + "}   // try-with-resources автоматично закриває Connection"));
    uk.add(LessonBlock.warning(
        "Уникайте конкатенації значень безпосередньо у рядок SQL-запиту, оскільки це створює критичну вразливість перед SQL-ін'єкціями. "
        + "Завжди використовуйте PreparedStatement із параметризованими запитами."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("Connecting via JDBC"));
    en.add(LessonBlock.paragraph(
        "JDBC is the standard Java API for relational databases. Flow: "
        + "DriverManager → Connection → Statement → ResultSet."));
    en.add(LessonBlock.code(
        "import java.sql.*;\n"
        + "\n"
        + "try (Connection con = DriverManager.getConnection(\n"
        + "        \"jdbc:sqlite:app.db\")) {\n"
        + "    Statement st = con.createStatement();\n"
        + "    ResultSet rs = st.executeQuery(\"SELECT id, name FROM users\");\n"
        + "    while (rs.next()) {\n"
        + "        int id = rs.getInt(\"id\");\n"
        + "        String name = rs.getString(\"name\");\n"
        + "        System.out.println(id + \": \" + name);\n"
        + "    }\n"
        + "}   // try-with-resources auto-closes the Connection"));
    en.add(LessonBlock.warning(
        "Do not concatenate parameters directly into the SQL query string to prevent SQL injection vulnerabilities. "
        + "Always use PreparedStatement with query parameters."));
    return new Lesson("jdbc.1", "JDBC основи", "JDBC basics", uk, en);
}

private static Lesson materialPrepared() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("PreparedStatement — безпечно та швидко"));
    uk.add(LessonBlock.code(
        "try (Connection con = DriverManager.getConnection(\"jdbc:sqlite:app.db\");\n"
        + "     PreparedStatement ps = con.prepareStatement(\n"
        + "         \"INSERT INTO users(name, email, age) VALUES(?, ?, ?)\")) {\n"
        + "    ps.setString(1, \"Олена\");\n"
        + "    ps.setString(2, \"ol@example.com\");\n"
        + "    ps.setInt(3, 28);\n"
        + "    int rows = ps.executeUpdate();   // кількість змінених рядків\n"
        + "    System.out.println(\"Inserted \" + rows);\n"
        + "}"));
    uk.add(LessonBlock.note(
        "PreparedStatement: (1) захищає від ін'єкцій, (2) кешується драйвером — "
        + "швидше при повторних викликах, (3) правильно серіалізує дати й бінар."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("PreparedStatement — safe and fast"));
    en.add(LessonBlock.code(
        "try (Connection con = DriverManager.getConnection(\"jdbc:sqlite:app.db\");\n"
        + "     PreparedStatement ps = con.prepareStatement(\n"
        + "         \"INSERT INTO users(name, email, age) VALUES(?, ?, ?)\")) {\n"
        + "    ps.setString(1, \"Helen\");\n"
        + "    ps.setString(2, \"helen@example.com\");\n"
        + "    ps.setInt(3, 28);\n"
        + "    int rows = ps.executeUpdate();   // number of affected rows\n"
        + "    System.out.println(\"Inserted \" + rows);\n"
        + "}"));
    en.add(LessonBlock.note(
        "PreparedStatement: (1) protects from injection, (2) cached by the driver — "
        + "faster on repeated calls, (3) serializes dates and binaries correctly."));
    return new Lesson("jdbc.2", "PreparedStatement", "PreparedStatement", uk, en);
}

private static Lesson materialDao() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Патерн DAO (Data Access Object)"));
    uk.add(LessonBlock.paragraph(
            "DAO ізолює доступ до даних від бізнес-логіки. Інтерфейс описує операції, "
            + "реалізація ховає JDBC/SQL. Заміна БД не зачіпає решту коду."));
    uk.add(LessonBlock.code(
        "interface UserDao {\n"
        + "    User findById(int id);\n"
        + "    List<User> findAll();\n"
        + "    void save(User u);\n"
        + "    void delete(int id);\n"
        + "}\n"
        + "\n"
        + "class JdbcUserDao implements UserDao {\n"
        + "    private final Connection con;\n"
        + "    JdbcUserDao(Connection con) { this.con = con; }\n"
        + "\n"
        + "    @Override\n"
        + "    public User findById(int id) throws SQLException {\n"
        + "        try (PreparedStatement ps = con.prepareStatement(\n"
        + "                \"SELECT id, name, email FROM users WHERE id = ?\")) {\n"
        + "            ps.setInt(1, id);\n"
        + "            try (ResultSet rs = ps.executeQuery()) {\n"
        + "                return rs.next() ? map(rs) : null;\n"
        + "            }\n"
        + "        }\n"
        + "    }\n"
        + "\n"
        + "    private User map(ResultSet rs) throws SQLException {\n"
        + "        return new User(rs.getInt(\"id\"), rs.getString(\"name\"), rs.getString(\"email\"));\n"
        + "    }\n"
        + "    // ... інші методи\n"
        + "}"));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("DAO (Data Access Object) pattern"));
    en.add(LessonBlock.paragraph(
        "DAO isolates data access from business logic. The interface describes "
        + "operations, the implementation hides JDBC/SQL. Swapping the DB does not "
        + "touch the rest of the code."));
    en.add(LessonBlock.code(
        "interface UserDao {\n"
        + "    User findById(int id);\n"
        + "    List<User> findAll();\n"
        + "    void save(User u);\n"
        + "    void delete(int id);\n"
        + "}\n"
        + "\n"
        + "class JdbcUserDao implements UserDao {\n"
        + "    private final Connection con;\n"
        + "    JdbcUserDao(Connection con) { this.con = con; }\n"
        + "\n"
        + "    @Override\n"
        + "    public User findById(int id) throws SQLException {\n"
        + "        try (PreparedStatement ps = con.prepareStatement(\n"
        + "                \"SELECT id, name, email FROM users WHERE id = ?\")) {\n"
        + "            ps.setInt(1, id);\n"
        + "            try (ResultSet rs = ps.executeQuery()) {\n"
        + "                return rs.next() ? map(rs) : null;\n"
        + "            }\n"
        + "        }\n"
        + "    }\n"
        + "\n"
        + "    private User map(ResultSet rs) throws SQLException {\n"
        + "        return new User(rs.getInt(\"id\"), rs.getString(\"name\"), rs.getString(\"email\"));\n"
        + "    }\n"
        + "    // ... other methods\n"
        + "}"));
    return new Lesson("jdbc.3", "DAO", "DAO", uk, en);
}
}
