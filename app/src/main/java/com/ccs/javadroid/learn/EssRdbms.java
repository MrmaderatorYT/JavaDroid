package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssRdbms {
static void add(Course s) {
    Chapter ch = new Chapter(
            "RDBMS та SQL Essentials",
            "RDBMS and SQL Essentials");
    ch.add(materialSql());
    ch.add(materialJoins());
    ch.add(materialIndex());
    s.add(ch);
}

private static Lesson materialSql() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Основи SQL: DDL та DML"));
    uk.add(LessonBlock.code(
        "-- DDL: створення таблиць\n"
        + "CREATE TABLE users (\n"
        + "    id    INTEGER PRIMARY KEY AUTOINCREMENT,\n"
        + "    name  VARCHAR(100) NOT NULL,\n"
        + "    email VARCHAR(200) UNIQUE,\n"
        + "    age   INTEGER CHECK (age >= 0)\n"
        + ");\n"
        + "\n"
        + "-- DML\n"
        + "INSERT INTO users (name, email, age) VALUES ('Іван', 'iv@example.com', 30);\n"
        + "UPDATE users SET age = 31 WHERE name = 'Іван';\n"
        + "DELETE FROM users WHERE age < 18;\n"
        + "\n"
        + "SELECT name, age FROM users WHERE age >= 18 ORDER BY age DESC;"));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("SQL basics: DDL and DML"));
    en.add(LessonBlock.code(
        "-- DDL: creating tables\n"
        + "CREATE TABLE users (\n"
        + "    id    INTEGER PRIMARY KEY AUTOINCREMENT,\n"
        + "    name  VARCHAR(100) NOT NULL,\n"
        + "    email VARCHAR(200) UNIQUE,\n"
        + "    age   INTEGER CHECK (age >= 0)\n"
        + ");\n"
        + "\n"
        + "-- DML\n"
        + "INSERT INTO users (name, email, age) VALUES ('John', 'john@example.com', 30);\n"
        + "UPDATE users SET age = 31 WHERE name = 'John';\n"
        + "DELETE FROM users WHERE age < 18;\n"
        + "\n"
        + "SELECT name, age FROM users WHERE age >= 18 ORDER BY age DESC;"));
    return new Lesson("sql.1", "SQL основи", "SQL basics", uk, en);
}

private static Lesson materialJoins() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("JOIN — з'єднання таблиць"));
    uk.add(LessonBlock.code(
        "-- INNER: лише рядки зі збігом з обох таблиць\n"
        + "SELECT u.name, o.total\n"
        + "FROM users u\n"
        + "INNER JOIN orders o ON o.user_id = u.id;\n"
        + "\n"
        + "-- LEFT: усі рядки лівої + збіги правої (інакше NULL)\n"
        + "SELECT u.name, o.total\n"
        + "FROM users u\n"
        + "LEFT JOIN orders o ON o.user_id = u.id;\n"
        + "\n"
        + "-- GROUP BY + агрегатні функції\n"
        + "SELECT u.name, COUNT(o.id) AS orders_count, SUM(o.total) AS spent\n"
        + "FROM users u\n"
        + "LEFT JOIN orders o ON o.user_id = u.id\n"
        + "GROUP BY u.id, u.name\n"
        + "HAVING spent > 100;"));
    uk.add(LessonBlock.note(
        "GROUP BY групує рядки; HAVING фільтрує групи (на відміну від WHERE, що "
        + "фільтрує рядки до групування)."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("JOIN — combining tables"));
    en.add(LessonBlock.code(
        "-- INNER: only rows matching in both tables\n"
        + "SELECT u.name, o.total\n"
        + "FROM users u\n"
        + "INNER JOIN orders o ON o.user_id = u.id;\n"
        + "\n"
        + "-- LEFT: all left rows + right matches (else NULL)\n"
        + "SELECT u.name, o.total\n"
        + "FROM users u\n"
        + "LEFT JOIN orders o ON o.user_id = u.id;\n"
        + "\n"
        + "-- GROUP BY + aggregate functions\n"
        + "SELECT u.name, COUNT(o.id) AS orders_count, SUM(o.total) AS spent\n"
        + "FROM users u\n"
        + "LEFT JOIN orders o ON o.user_id = u.id\n"
        + "GROUP BY u.id, u.name\n"
        + "HAVING spent > 100;"));
    en.add(LessonBlock.note(
        "GROUP BY groups rows; HAVING filters groups (unlike WHERE which filters "
        + "rows before grouping)."));
    return new Lesson("sql.2", "JOIN", "JOIN", uk, en);
}

private static Lesson materialIndex() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Індекси та планування"));
    uk.add(LessonBlock.paragraph(
            "Індекс — окрема структура (зазвичай B-tree), що пришвидшує пошук за "
            + "колонкою. Проте сповільнює INSERT/UPDATE/DELETE і займає місце."));
    uk.add(LessonBlock.code(
        "CREATE INDEX idx_users_email ON users(email);\n"
        + "CREATE UNIQUE INDEX idx_users_name_age ON users(name, age);  -- складений\n"
        + "DROP INDEX idx_users_email;\n"
        + "\n"
        + "-- Подивитися план запиту\n"
        + "EXPLAIN QUERY PLAN SELECT * FROM users WHERE email = 'iv@example.com';"));
    uk.add(LessonBlock.warning(
        "Індексуйте колонки з WHERE/JOIN/ORDER BY, але не всі підряд. Зайвий індекс "
        + "= повільніші вставки й марна трата місця."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("Indexes and planning"));
    en.add(LessonBlock.paragraph(
        "An index is a separate structure (usually a B-tree) that speeds up lookup on "
        + "a column. However, it slows down INSERT/UPDATE/DELETE and takes space."));
    en.add(LessonBlock.code(
        "CREATE INDEX idx_users_email ON users(email);\n"
        + "CREATE UNIQUE INDEX idx_users_name_age ON users(name, age);  -- composite\n"
        + "DROP INDEX idx_users_email;\n"
        + "\n"
        + "-- Inspect the query plan\n"
        + "EXPLAIN QUERY PLAN SELECT * FROM users WHERE email = 'john@example.com';"));
    en.add(LessonBlock.warning(
        "Index columns in WHERE/JOIN/ORDER BY, but not every column. A useless index "
        + "= slower inserts and wasted space."));
    return new Lesson("sql.3", "Індекси", "Indexes", uk, en);
}
}
