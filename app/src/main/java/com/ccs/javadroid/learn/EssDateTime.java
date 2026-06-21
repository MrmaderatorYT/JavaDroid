package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssDateTime {
static void add(Course s) {
    Chapter ch = new Chapter(
            "Дата/час: Timeline, Local і Zone Time, форматування та парсинг",
            "Date/Time: Timeline, Local and Zone time, formatting & parsing");
    ch.add(materialTimeline());
    ch.add(materialLocal());
    ch.add(materialZoned());
    ch.add(materialFormat());
    s.add(ch);
}

private static Lesson materialTimeline() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Timeline: Instant"));
    uk.add(LessonBlock.paragraph(
            "java.time.Instant — точка на всесвітній шкалі часу (UTC). Містить секунди "
            + "та наносекунди від Unix-епохи (1970-01-01T00:00:00Z). Основа для "
            + "машинної обробки часу й збереження в БД."));
    uk.add(LessonBlock.code(
            "import java.time.*;\n"
            + "\n"
            + "Instant now = Instant.now();          // поточний момент\n"
            + "Instant epoch = Instant.ofEpochSecond(0);\n"
            + "Instant later = now.plusSeconds(60);\n"
            + "Duration d = Duration.between(now, later);\n"
            + "System.out.println(d.getSeconds());    // 60"));
    uk.add(LessonBlock.note(
            "Instant не залежить від часового поясу — це абсолютна точка в часі. "
            + "Ідеально для timestamp-ів, логів і метрик."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("Timeline: Instant"));
    en.add(LessonBlock.paragraph(
            "java.time.Instant is a point on the universal time scale (UTC). It holds "
            + "seconds and nanoseconds since the Unix epoch (1970-01-01T00:00:00Z). "
            + "The foundation for machine time and DB storage."));
    en.add(LessonBlock.code(
        "import java.time.*;\n"
        + "\n"
        + "Instant now = Instant.now();          // current moment\n"
        + "Instant epoch = Instant.ofEpochSecond(0);\n"
        + "Instant later = now.plusSeconds(60);\n"
        + "Duration d = Duration.between(now, later);\n"
        + "System.out.println(d.getSeconds());    // 60"));
    en.add(LessonBlock.note(
        "Instant is independent of any time zone — it's an absolute point in time. "
        + "Ideal for timestamps, logs and metrics."));
    return new Lesson("dt.1", "Timeline (Instant)", "Timeline (Instant)", uk, en);
}

private static Lesson materialLocal() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("LocalDate / LocalTime / LocalDateTime"));
    uk.add(LessonBlock.paragraph(
            "Local-типи НЕ мають інформації про часовий пояс. Це людське подання: "
            + "«день народження», «обід о 13:00». Зручні для бізнес-логіки."));
    uk.add(LessonBlock.code(
        "LocalDate date = LocalDate.of(2026, 6, 15);\n"
        + "LocalTime time = LocalTime.of(14, 30);\n"
        + "LocalDateTime dt = LocalDateTime.of(date, time);\n"
        + "\n"
        + "LocalDate tomorrow = date.plusDays(1);\n"
        + "boolean leap = date.isLeapYear();          // false для 2026\n"
        + "DayOfWeek dow = date.getDayOfWeek();        // MONDAY"));
    uk.add(LessonBlock.warning(
        "Local-типи не можна змішувати з Instant напряму — потрібен часовий пояс: "
        + "localDateTime.atZone(ZoneId.of(\"Europe/Kyiv\")).toInstant()."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("LocalDate / LocalTime / LocalDateTime"));
    en.add(LessonBlock.paragraph(
            "Local types carry NO time zone information. They represent human-readable "
            + "values: \"birthday\", \"lunch at 13:00\". Convenient for business logic."));
    en.add(LessonBlock.code(
        "LocalDate date = LocalDate.of(2026, 6, 15);\n"
        + "LocalTime time = LocalTime.of(14, 30);\n"
        + "LocalDateTime dt = LocalDateTime.of(date, time);\n"
        + "\n"
        + "LocalDate tomorrow = date.plusDays(1);\n"
        + "boolean leap = date.isLeapYear();          // false for 2026\n"
        + "DayOfWeek dow = date.getDayOfWeek();        // MONDAY"));
    en.add(LessonBlock.warning(
        "Local types cannot be mixed with Instant directly — you need a time zone: "
        + "localDateTime.atZone(ZoneId.of(\"Europe/Kyiv\")).toInstant()."));
    return new Lesson("dt.2", "Local date/time", "Local date/time", uk, en);
}

private static Lesson materialZoned() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("ZonedDateTime"));
    uk.add(LessonBlock.paragraph(
            "ZonedDateTime поєднує LocalDateTime із ZoneId — повне подання моменту в "
            + "певному часовому поясі. Врахує літній/зимовий час, високосні секунди."));
    uk.add(LessonBlock.code(
        "ZoneId kyiv = ZoneId.of(\"Europe/Kyiv\");\n"
        + "ZoneId ny = ZoneId.of(\"America/New_York\");\n"
        + "ZonedDateTime meeting = ZonedDateTime.of(2026, 6, 15, 14, 0, 0, 0, kyiv);\n"
        + "ZonedDateTime meetingNy = meeting.withZoneSameInstant(ny);\n"
        + "System.out.println(meetingNy);   // 2026-06-15T07:00-04:00[America/New_York]"));
    uk.add(LessonBlock.note(
        "Список усіх ZoneId — ZoneId.getAvailableZoneIds(). Використовуйте імена з "
        + "бази IANA (\"Europe/Kyiv\"), а не скорочення (\"EET\")."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("ZonedDateTime"));
    en.add(LessonBlock.paragraph(
            "ZonedDateTime combines LocalDateTime with a ZoneId — a full representation "
            + "of a moment in a specific time zone. It accounts for DST and leap seconds."));
    en.add(LessonBlock.code(
        "ZoneId kyiv = ZoneId.of(\"Europe/Kyiv\");\n"
        + "ZoneId ny = ZoneId.of(\"America/New_York\");\n"
        + "ZonedDateTime meeting = ZonedDateTime.of(2026, 6, 15, 14, 0, 0, 0, kyiv);\n"
        + "ZonedDateTime meetingNy = meeting.withZoneSameInstant(ny);\n"
        + "System.out.println(meetingNy);   // 2026-06-15T07:00-04:00[America/New_York]"));
    en.add(LessonBlock.note(
        "All ZoneIds — ZoneId.getAvailableZoneIds(). Use IANA names "
        + "(\"Europe/Kyiv\"), not abbreviations (\"EET\")."));
    return new Lesson("dt.3", "ZonedDateTime", "ZonedDateTime", uk, en);
}

private static Lesson materialFormat() {
    List<LessonBlock> uk = new ArrayList<>();
    uk.add(LessonBlock.heading("Форматування та парсинг"));
    uk.add(LessonBlock.paragraph(
            "DateTimeFormatter — потокобезпечний форматер. Шаблони: y (рік), M (місяць), "
            + "d (день), H (година), m (хвилина), s (секунда."));
    uk.add(LessonBlock.code(
        "DateTimeFormatter fmt = DateTimeFormatter.ofPattern(\"dd.MM.yyyy HH:mm\");\n"
        + "LocalDateTime dt = LocalDateTime.of(2026, 6, 15, 14, 30);\n"
        + "\n"
        + "// Форматування у рядок\n"
        + "String s = dt.format(fmt);                  // 15.06.2026 14:30\n"
        + "\n"
        + "// Парсинг із рядка\n"
        + "LocalDateTime parsed = LocalDateTime.parse(\"01.01.2025 09:00\", fmt);\n"
        + "System.out.println(parsed.getYear());       // 2025"));
    uk.add(LessonBlock.warning(
        "LocalDateTime.parse за замовчуванням вимагає ISO-8601 (\"2026-06-15T14:30\"). "
        + "Для іншого формату обов'язково передайте DateTimeFormatter."));
    List<LessonBlock> en = new ArrayList<>();
    en.add(LessonBlock.heading("Formatting & parsing"));
    en.add(LessonBlock.paragraph(
        "DateTimeFormatter is a thread-safe formatter. Patterns: y (year), M (month), "
        + "d (day), H (hour), m (minute), s (second)."));
    en.add(LessonBlock.code(
        "DateTimeFormatter fmt = DateTimeFormatter.ofPattern(\"dd.MM.yyyy HH:mm\");\n"
        + "LocalDateTime dt = LocalDateTime.of(2026, 6, 15, 14, 30);\n"
        + "\n"
        + "// Formatting to string\n"
        + "String s = dt.format(fmt);                  // 15.06.2026 14:30\n"
        + "\n"
        + "// Parsing from string\n"
        + "LocalDateTime parsed = LocalDateTime.parse(\"01.01.2025 09:00\", fmt);\n"
        + "System.out.println(parsed.getYear());       // 2025"));
    en.add(LessonBlock.warning(
        "LocalDateTime.parse by default requires ISO-8601 (\"2026-06-15T14:30\"). "
        + "For any other format always pass a DateTimeFormatter."));
    return new Lesson("dt.4", "Форматування й парсинг", "Formatting & parsing", uk, en);
}
}
