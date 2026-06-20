package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Курс Essentials: поглиблені теми (рівень enterprise).
 * Date/Time API, Concurrency Essentials (ч.1 та ч.2), JVM Memory, RDBMS/SQL,
 * JDBC/DAO, Web Basics, Servlet/JSP.
 */
final class EssentialsCourse {

    static Course create() {
        Course c = new Course(
                "java_essentials",
                "Essentials: від Date/Time до Web",
                "Essentials: from Date/Time to Web",
                "Поглиблені enterprise-теми з прикладами: час, конкурентність, пам'ять JVM, "
                + "бази даних, JDBC, Servlet/JSP.",
                "Advanced enterprise topics with examples: time, concurrency, JVM memory, "
                + "databases, JDBC, Servlet/JSP.");

        EssDateTime.add(c);
        EssConcurrency1.add(c);
        EssConcurrency2.add(c);
        EssJvmMemory.add(c);
        EssRdbms.add(c);
        EssJdbc.add(c);
        EssWeb.add(c);
        EssServletJsp.add(c);
        return c;
    }

    // ── Date/Time ───────────────────────────────────────────────────────────

    static final class EssDateTime {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "Дата/час: Timeline, Local і Zone Time, форматування та парсинг",
                    "Date/Time: Timeline, Local and Zone time, formatting & parsing");
            ch.add(lessonTimeline());
            ch.add(lessonLocal());
            ch.add(lessonZoned());
            ch.add(lessonFormat());
            c.add(ch);
        }

        private static Lesson lessonTimeline() {
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

        private static Lesson lessonLocal() {
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

        private static Lesson lessonZoned() {
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

        private static Lesson lessonFormat() {
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

    // ── Concurrency Essentials Part 1 ───────────────────────────────────────

    static final class EssConcurrency1 {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "Java Concurrency Essentials (Частина 1)",
                    "Java Concurrency Essentials (Part 1)");
            ch.add(lessonThreads());
            ch.add(lessonSync());
            ch.add(lessonVolatile());
            ch.add(lessonAtomic());
            c.add(ch);
        }

        private static Lesson lessonThreads() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Створення потоків"));
            uk.add(LessonBlock.paragraph(
                    "Три способи: extends Thread, implements Runnable, lambda у конструкторі Thread. "
                    + "start() — запускає окремий потік; run() — просто викликає метод у поточному потоці."));
            uk.add(LessonBlock.code(
                "Runnable task = () -> {\n"
                + "    String name = Thread.currentThread().getName();\n"
                + "    System.out.println(\"Виконується в \" + name);\n"
                + "};\n"
                + "Thread t = new Thread(task, \"worker-1\");\n"
                + "t.start();\n"
                + "t.join();   // дочекатися завершення"));
            uk.add(LessonBlock.note(
                "Пріоритет потоку лише рекомендація для планувальника ОС. На практиці краще "
                + "не покладатися на пріоритети для коректності."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Creating threads"));
            en.add(LessonBlock.paragraph(
                "Three ways: extends Thread, implements Runnable, lambda into Thread constructor. "
                + "start() launches a new thread; run() just calls the method in the current thread."));
            en.add(LessonBlock.code(
                "Runnable task = () -> {\n"
                + "    String name = Thread.currentThread().getName();\n"
                + "    System.out.println(\"Running in \" + name);\n"
                + "};\n"
                + "Thread t = new Thread(task, \"worker-1\");\n"
                + "t.start();\n"
                + "t.join();   // wait for completion"));
            en.add(LessonBlock.note(
                "Thread priority is only a hint to the OS scheduler. In practice do not rely "
                + "on priorities for correctness."));
            return new Lesson("c1.1", "Потоки", "Threads", uk, en);
        }

        private static Lesson lessonSync() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Синхронізація: synchronized"));
            uk.add(LessonBlock.paragraph(
                "synchronized гарантує, що лише один потік одночасно виконує блок/метод для "
                + "даного об'єкта-монітора. Вирішує race conditions, але обмежує паралелізм."));
            uk.add(LessonBlock.code(
                "class Counter {\n"
                + "    private int count;\n"
                + "    public synchronized void inc() { count++; }\n"
                + "    public int get() { return count; }\n"
                + "}\n"
                + "\n"
                + "Counter c = new Counter();\n"
                + "Runnable r = () -> { for (int i = 0; i < 1000; i++) c.inc(); };\n"
                + "Thread t1 = new Thread(r), t2 = new Thread(r);\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(c.get());   // 2000 (без synchronized отримали б менше)"));
            uk.add(LessonBlock.warning(
                "synchronized на методі блокує весь об'єкт. Якщо розділяєте незалежні "
                + "структури даних — використовуйте окремі об'єкти-монітори або "
                + "java.util.concurrent.locks."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Synchronization: synchronized"));
            en.add(LessonBlock.paragraph(
                "synchronized guarantees that only one thread at a time executes a block/method "
                + "for a given monitor object. It fixes race conditions but limits parallelism."));
            en.add(LessonBlock.code(
                "class Counter {\n"
                + "    private int count;\n"
                + "    public synchronized void inc() { count++; }\n"
                + "    public int get() { return count; }\n"
                + "}\n"
                + "\n"
                + "Counter c = new Counter();\n"
                + "Runnable r = () -> { for (int i = 0; i < 1000; i++) c.inc(); };\n"
                + "Thread t1 = new Thread(r), t2 = new Thread(r);\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(c.get());   // 2000 (without synchronized you'd get less)"));
            en.add(LessonBlock.warning(
                "A synchronized method locks the entire object. If you split independent data "
                + "structures, use separate monitor objects or java.util.concurrent.locks."));
            return new Lesson("c1.2", "Синхронізація", "Synchronization", uk, en);
        }

        private static Lesson lessonVolatile() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("volatile — видимість між потоками"));
            uk.add(LessonBlock.paragraph(
                "volatile гарантує, що зміна змінної одразу видна іншим потокам (немає "
                + "кешування в регістрах/CPU-кеші). НЕ робить операції атомарними "
                + "(i++ все одно небезпечне), але гарантує happens-before."));
            uk.add(LessonBlock.code(
                "class Flag {\n"
                + "    private volatile boolean running = true;\n"
                + "    void stop() { running = false; }\n"
                + "    void work() {\n"
                + "        while (running) {\n"
                + "            // do something\n"
                + "        }\n"
                + "    }\n"
                + "}"));
            uk.add(LessonBlock.warning(
                "volatile не замінює синхронізацію для складених операцій. Для лічильників "
                + "використовуйте AtomicInteger."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("volatile — visibility across threads"));
            en.add(LessonBlock.paragraph(
                "volatile guarantees that a change to the variable is immediately visible to "
                + "other threads (no caching in registers/CPU cache). It does NOT make "
                + "operations atomic (i++ is still unsafe), but it does establish happens-before."));
            en.add(LessonBlock.code(
                "class Flag {\n"
                + "    private volatile boolean running = true;\n"
                + "    void stop() { running = false; }\n"
                + "    void work() {\n"
                + "        while (running) {\n"
                + "            // do something\n"
                + "        }\n"
                + "    }\n"
                + "}"));
            en.add(LessonBlock.warning(
                "volatile does not replace synchronization for compound operations. For "
                + "counters use AtomicInteger."));
            return new Lesson("c1.3", "volatile", "volatile", uk, en);
        }

        private static Lesson lessonAtomic() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Atomic-класи (lock-free)"));
            uk.add(LessonBlock.paragraph(
                "java.util.concurrent.atomic: AtomicInteger, AtomicLong, AtomicReference тощо. "
                + "Реалізовані через CAS (compare-and-swap) — атомарні операції без блокувань."));
            uk.add(LessonBlock.code(
                "import java.util.concurrent.atomic.*;\n"
                + "\n"
                + "AtomicInteger counter = new AtomicInteger(0);\n"
                + "\n"
                + "Runnable r = () -> {\n"
                + "    for (int i = 0; i < 1000; i++) counter.incrementAndGet();\n"
                + "};\n"
                + "Thread t1 = new Thread(r), t2 = new Thread(r);\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(counter.get());   // 2000"));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Atomic classes (lock-free)"));
            en.add(LessonBlock.paragraph(
                "java.util.concurrent.atomic: AtomicInteger, AtomicLong, AtomicReference etc. "
                + "Implemented via CAS (compare-and-swap) — atomic operations without locks."));
            en.add(LessonBlock.code(
                "import java.util.concurrent.atomic.*;\n"
                + "\n"
                + "AtomicInteger counter = new AtomicInteger(0);\n"
                + "\n"
                + "Runnable r = () -> {\n"
                + "    for (int i = 0; i < 1000; i++) counter.incrementAndGet();\n"
                + "};\n"
                + "Thread t1 = new Thread(r), t2 = new Thread(r);\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(counter.get());   // 2000"));
            return new Lesson("c1.4", "Atomic-класи", "Atomic classes", uk, en);
        }
    }

    // ── Concurrency Essentials Part 2 ───────────────────────────────────────

    static final class EssConcurrency2 {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "Java Concurrency Essentials (Частина 2)",
                    "Java Concurrency Essentials (Part 2)");
            ch.add(lessonExecutor());
            ch.add(lessonFuture());
            ch.add(lessonConcurrentCollections());
            ch.add(lessonCompletableFuture());
            c.add(ch);
        }

        private static Lesson lessonExecutor() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("ExecutorService — пул потоків"));
            uk.add(LessonBlock.paragraph(
                    "Замість створення тисяч Thread вручну — пул: Executors.newFixedThreadPool(n), "
                + "newCachedThreadPool(), newSingleThreadExecutor(). Пул reuse-ить потоки."));
            uk.add(LessonBlock.code(
                "import java.util.concurrent.*;\n"
                + "\n"
                + "ExecutorService pool = Executors.newFixedThreadPool(4);\n"
                + "for (int i = 0; i < 10; i++) {\n"
                + "    final int task = i;\n"
                + "    pool.submit(() -> System.out.println(\"Задача \" + task\n"
                + "        + \" на \" + Thread.currentThread().getName()));\n"
                + "}\n"
                + "pool.shutdown();   // завершити після виконання\n"
                + "pool.awaitTermination(1, TimeUnit.MINUTES);"));
            uk.add(LessonBlock.warning(
                "НИКОЛИ не забувайте shutdown() — інакше пул триматиме JVM живою. "
                + "У JDK 8 використовуйте явний finally { pool.shutdown(); }, якщо після "
                + "submit/execute може статися виняток."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("ExecutorService — thread pool"));
            en.add(LessonBlock.paragraph(
                "Instead of creating thousands of Threads by hand, use a pool: "
                + "Executors.newFixedThreadPool(n), newCachedThreadPool(), newSingleThreadExecutor(). "
                + "The pool reuses threads."));
            en.add(LessonBlock.code(
                "import java.util.concurrent.*;\n"
                + "\n"
                + "ExecutorService pool = Executors.newFixedThreadPool(4);\n"
                + "for (int i = 0; i < 10; i++) {\n"
                + "    final int task = i;\n"
                + "    pool.submit(() -> System.out.println(\"Task \" + task\n"
                + "        + \" on \" + Thread.currentThread().getName()));\n"
                + "}\n"
                + "pool.shutdown();   // shut down after tasks finish\n"
                + "pool.awaitTermination(1, TimeUnit.MINUTES);"));
            en.add(LessonBlock.warning(
                "NEVER forget shutdown() — otherwise the pool keeps the JVM alive. "
                + "In JDK 8, use an explicit finally { pool.shutdown(); } if an exception "
                + "may happen after submit/execute."));
            return new Lesson("c2.1", "ExecutorService", "ExecutorService", uk, en);
        }

        private static Lesson lessonFuture() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Callable та Future"));
            uk.add(LessonBlock.paragraph(
                    "Callable — як Runnable, але повертає значення й може кидати checked-виняток. "
                    + "Future — обіцянка результату: get() блокує до завершення."));
            uk.add(LessonBlock.code(
                "ExecutorService pool = Executors.newSingleThreadExecutor();\n"
                + "Future<Integer> f = pool.submit(() -> {\n"
                + "    Thread.sleep(500);\n"
                + "    return 42;\n"
                + "});\n"
                + "System.out.println(\"Робимо інше...\");\n"
                + "Integer result = f.get();   // блокує, поки не готово\n"
                + "System.out.println(result);  // 42\n"
                + "pool.shutdown();"));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Callable and Future"));
            en.add(LessonBlock.paragraph(
                "Callable is like Runnable but returns a value and may throw checked exceptions. "
                + "Future is a promise of a result: get() blocks until completion."));
            en.add(LessonBlock.code(
                "ExecutorService pool = Executors.newSingleThreadExecutor();\n"
                + "Future<Integer> f = pool.submit(() -> {\n"
                + "    Thread.sleep(500);\n"
                + "    return 42;\n"
                + "});\n"
                + "System.out.println(\"Doing other things...\");\n"
                + "Integer result = f.get();   // blocks until ready\n"
                + "System.out.println(result);  // 42\n"
                + "pool.shutdown();"));
            return new Lesson("c2.2", "Future/Callable", "Future/Callable", uk, en);
        }

        private static Lesson lessonConcurrentCollections() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Concurrent-колекції"));
            uk.add(LessonBlock.paragraph(
                    "Потокобезпечні колекції без явної синхронізації: ConcurrentHashMap, "
                    + "CopyOnWriteArrayList, BlockingQueue."));
            uk.add(LessonBlock.code(
                "ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();\n"
                + "map.put(\"a\", 1);\n"
                + "map.compute(\"a\", (k, v) -> v + 1);   // атомарна зміна\n"
                + "System.out.println(map.get(\"a\"));      // 2\n"
                + "\n"
                + "// Черга «виробник-споживач»\n"
                + "BlockingQueue<String> q = new ArrayBlockingQueue<>(10);\n"
                + "q.put(\"item\");            // блокує, якщо черга повна\n"
                + "String s = q.take();       // блокує, якщо порожня"));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Concurrent collections"));
            en.add(LessonBlock.paragraph(
                "Thread-safe collections without explicit synchronization: ConcurrentHashMap, "
                + "CopyOnWriteArrayList, BlockingQueue."));
            en.add(LessonBlock.code(
                "ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();\n"
                + "map.put(\"a\", 1);\n"
                + "map.compute(\"a\", (k, v) -> v + 1);   // atomic update\n"
                + "System.out.println(map.get(\"a\"));      // 2\n"
                + "\n"
                + "// Producer-consumer queue\n"
                + "BlockingQueue<String> q = new ArrayBlockingQueue<>(10);\n"
                + "q.put(\"item\");            // blocks if full\n"
                + "String s = q.take();       // blocks if empty"));
            return new Lesson("c2.3", "Concurrent-колекції", "Concurrent collections", uk, en);
        }

        private static Lesson lessonCompletableFuture() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("CompletableFuture — асинхронні ланцюжки"));
            uk.add(LessonBlock.paragraph(
                    "CompletableFuture — розширена версія Future, що підтримує композицію "
                    + "через методи thenApply, thenAccept, thenCombine та exceptionally. Є аналогом концепції Promise у JavaScript."));
            uk.add(LessonBlock.code(
                "CompletableFuture.supplyAsync(() -> \"hello\")     // фонова задача\n"
                + "    .thenApply(String::toUpperCase)               // перетворення\n"
                + "    .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + \"!\"))\n"
                + "    .thenAccept(System.out::println)              // побічний ефект\n"
                + "    .exceptionally(e -> { e.printStackTrace(); return null; });\n"
                + "// надрукує: HELLO!"));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("CompletableFuture — async chains"));
            en.add(LessonBlock.paragraph(
                "CompletableFuture is an enhanced implementation of the Future interface that supports composition "
                + "via thenApply, thenAccept, thenCombine, and exceptionally. It is analogous to Promises in JavaScript."));
            en.add(LessonBlock.code(
                "CompletableFuture.supplyAsync(() -> \"hello\")     // background task\n"
                + "    .thenApply(String::toUpperCase)               // transform\n"
                + "    .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + \"!\"))\n"
                + "    .thenAccept(System.out::println)              // side effect\n"
                + "    .exceptionally(e -> { e.printStackTrace(); return null; });\n"
                + "// prints: HELLO!"));
            return new Lesson("c2.4", "CompletableFuture", "CompletableFuture", uk, en);
        }
    }

    // ── JVM Memory Management ───────────────────────────────────────────────

    static final class EssJvmMemory {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "Керування пам'яттю JVM",
                    "JVM Memory Management");
            ch.add(lessonAreas());
            ch.add(lessonGc());
            ch.add(lessonLeaks());
            c.add(ch);
        }

        private static Lesson lessonAreas() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Області пам'яті JVM"));
            uk.add(LessonBlock.paragraph("Пам'ять JVM поділена на області:"));
            uk.add(LessonBlock.table(
                "Область\tПризначення",
                Arrays.asList(
                    "Heap\tОб'єкти та масиви (Young/Old generation)",
                    "Metaspace (PermGen до Java 8)\tМетадані класів",
                    "Stack\tСтек-фрейми потоку (локальні змінні, виклики)",
                    "PC Register\tАдреса поточної інструкції потоку",
                    "Native Stack\tВиклики JNI")));
            uk.add(LessonBlock.note(
                "Heap — загальна для всіх потоків. Stack — окремий на потік (за замовчуванням "
                + "512KB, -Xss). Метасpace росте автоматично з Java 8."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("JVM memory areas"));
            en.add(LessonBlock.paragraph("JVM memory is divided into areas:"));
            en.add(LessonBlock.table(
                "Area\tPurpose",
                Arrays.asList(
                    "Heap\tObjects and arrays (Young/Old generation)",
                    "Metaspace (PermGen before Java 8)\tClass metadata",
                    "Stack\tPer-thread stack frames (locals, calls)",
                    "PC Register\tCurrent instruction address for a thread",
                    "Native Stack\tJNI calls")));
            en.add(LessonBlock.note(
                "Heap is shared by all threads. Stack is per-thread (default 512KB, -Xss). "
                + "Metaspace grows automatically since Java 8."));
            return new Lesson("jvm.1", "Області пам'яті", "Memory areas", uk, en);
        }

        private static Lesson lessonGc() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Garbage Collector"));
            uk.add(LessonBlock.paragraph(
                    "GC автоматично звільняє об'єкти, на які не лишилося сильних посилань. "
                    + "У JDK 8 можна зустріти Serial, Parallel, CMS та G1 GC. Головна ідея "
                    + "для початку: збирач шукає недосяжні об'єкти й повертає пам'ять JVM."));
            uk.add(LessonBlock.list(
                "Young generation (Eden + S0/S1) — нові об'єкти, часта збірка (minor GC).",
                "Old (Tenured) generation — об'єкти, що вижили кілька minor GC.",
                "Meta­space — метадані класів, не чиститься GC у класичному сенсі."));
            uk.add(LessonBlock.paragraph(
                "Явно «попросити» GC: System.gc() — лише рекомендація, не гарантує збірку."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Garbage Collector"));
            en.add(LessonBlock.paragraph(
                "The GC automatically frees objects with no strong references left. In JDK 8 "
                + "you may see Serial, Parallel, CMS, and G1 GC. The beginner-level idea is "
                + "simple: the collector finds unreachable objects and returns memory to the JVM."));
            en.add(LessonBlock.list(
                "Young generation (Eden + S0/S1) — fresh objects, frequent collection (minor GC).",
                "Old (Tenured) generation — objects surviving several minor GCs.",
                "Meta­space — class metadata, not GC'd in the classic sense."));
            en.add(LessonBlock.paragraph(
                "Explicit GC: System.gc() is only a hint, no guarantee of collection."));
            return new Lesson("jvm.2", "Garbage Collector", "Garbage Collector", uk, en);
        }

        private static Lesson lessonLeaks() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Витоки пам'яті"));
            uk.add(LessonBlock.paragraph(
                    "У Java витоки — це утримання посилань на об'єкти, які більше не потрібні: "
                    + "статичні колекції, не-прибрані слухачі, незакриті ресурси, кеші без eviction."));
            uk.add(LessonBlock.code(
                "// Класичний витік: статична Map тримає об'єкти назавжди\n"
                + "public class Cache {\n"
                + "    private static final Map<String, Object> MAP = new HashMap<>();\n"
                + "    public static void put(String k, Object v) { MAP.put(k, v); }\n"
                + "}\n"
                + "// Виправлення — WeakHashMap або явний eviction:"));
            uk.add(LessonBlock.note(
                "Слабкі посилання: WeakReference / WeakHashMap дозволяють GC зібрати ключі. "
                + "Try-with-resources гарантує закриття Closeable."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Memory leaks"));
            en.add(LessonBlock.paragraph(
                "In Java a leak means holding references to objects no longer needed: static "
                + "collections, un-removed listeners, unclosed resources, caches without eviction."));
            en.add(LessonBlock.code(
                "// Classic leak: a static Map keeps objects forever\n"
                + "public class Cache {\n"
                + "    private static final Map<String, Object> MAP = new HashMap<>();\n"
                + "    public static void put(String k, Object v) { MAP.put(k, v); }\n"
                + "}\n"
                + "// Fix — WeakHashMap or explicit eviction:"));
            en.add(LessonBlock.note(
                "Weak references: WeakReference / WeakHashMap let the GC collect keys. "
                + "Try-with-resources ensures Closeable resources are closed."));
            return new Lesson("jvm.3", "Витоки пам'яті", "Memory leaks", uk, en);
        }
    }

    // ── RDBMS & SQL ─────────────────────────────────────────────────────────

    static final class EssRdbms {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "RDBMS та SQL Essentials",
                    "RDBMS and SQL Essentials");
            ch.add(lessonSql());
            ch.add(lessonJoins());
            ch.add(lessonIndex());
            c.add(ch);
        }

        private static Lesson lessonSql() {
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

        private static Lesson lessonJoins() {
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

        private static Lesson lessonIndex() {
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

    // ── JDBC / DAO ──────────────────────────────────────────────────────────

    static final class EssJdbc {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "JDBC та патерн DAO",
                    "JDBC Essentials and the DAO Pattern");
            ch.add(lessonJdbc());
            ch.add(lessonPrepared());
            ch.add(lessonDao());
            c.add(ch);
        }

        private static Lesson lessonJdbc() {
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

        private static Lesson lessonPrepared() {
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

        private static Lesson lessonDao() {
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

    // ── Web Basics ──────────────────────────────────────────────────────────

    static final class EssWeb {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "Web Basics",
                    "Web Basics");
            ch.add(lessonHttp());
            ch.add(lessonUrl());
            c.add(ch);
        }

        private static Lesson lessonHttp() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Основи HTTP"));
            uk.add(LessonBlock.paragraph(
                    "HTTP — клієнт-серверний протокол. Клієнт надсилає запит (request), сервер "
                    + "повертає відповідь (response). Кожен має: рядок статусу, заголовки, тіло."));
            uk.add(LessonBlock.table(
                "Метод\tПризначення",
                Arrays.asList(
                    "GET\tОтримати ресурс (без тіла запиту)",
                    "POST\tСтворити ресурс (тіло з даними)",
                    "PUT\tОновити/замінити ресурс",
                    "DELETE\tВидалити ресурс",
                    "PATCH\tЧасткове оновлення")));
            uk.add(LessonBlock.table(
                "Код\tЗначення",
                Arrays.asList(
                    "200\tOK",
                    "301/302\tRedirect",
                    "400\tBad Request (синтаксис)",
                    "401\tUnauthorized (нема авторизації)",
                    "403\tForbidden (є, але заборонено)",
                    "404\tNot Found",
                    "500\tInternal Server Error")));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("HTTP basics"));
            en.add(LessonBlock.paragraph(
                "HTTP is a client-server protocol. The client sends a request, the server "
                + "returns a response. Each has: a status line, headers, a body."));
            en.add(LessonBlock.table(
                "Method\tPurpose",
                Arrays.asList(
                    "GET\tRetrieve a resource (no request body)",
                    "POST\tCreate a resource (body with data)",
                    "PUT\tUpdate/replace a resource",
                    "DELETE\tDelete a resource",
                    "PATCH\tPartial update")));
            en.add(LessonBlock.table(
                "Code\tMeaning",
                Arrays.asList(
                    "200\tOK",
                    "301/302\tRedirect",
                    "400\tBad Request (syntax)",
                    "401\tUnauthorized (no auth)",
                    "403\tForbidden (auth present but denied)",
                    "404\tNot Found",
                    "500\tInternal Server Error")));
            return new Lesson("web.1", "HTTP", "HTTP", uk, en);
        }

        private static Lesson lessonUrl() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("URL та URI"));
            uk.add(LessonBlock.code(
                "https://example.com:8443/api/users?id=42&active=true#section\n"
                + "  \\_/   \\_________/ \\_/ \\______/ \\_____________/ \\______/\n"
                + " scheme     host    port  path      query         fragment"));
            uk.add(LessonBlock.paragraph(
                    "Java-клас java.net.URI розбирає ці складові. Для JDK 8 використовуйте "
                    + "HttpURLConnection або бібліотеку на кшталт OkHttp, якщо вона підключена до проєкту."));
            uk.add(LessonBlock.code(
                "import java.io.BufferedReader;\n"
                + "import java.io.InputStreamReader;\n"
                + "import java.net.HttpURLConnection;\n"
                + "import java.net.URI;\n"
                + "import java.net.URL;\n"
                + "\n"
                + "URI uri = URI.create(\"https://api.github.com/repos/octocat/Hello-World\");\n"
                + "URL url = uri.toURL();\n"
                + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
                + "conn.setRequestMethod(\"GET\");\n"
                + "conn.setConnectTimeout(10000);\n"
                + "conn.setReadTimeout(10000);\n"
                + "conn.setRequestProperty(\"Accept\", \"application/json\");\n"
                + "\n"
                + "int status = conn.getResponseCode();\n"
                + "System.out.println(status);   // 200\n"
                + "\n"
                + "try (BufferedReader reader = new BufferedReader(\n"
                + "        new InputStreamReader(conn.getInputStream(), \"UTF-8\"))) {\n"
                + "    String line;\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        System.out.println(line);       // JSON\n"
                + "    }\n"
                + "} finally {\n"
                + "    conn.disconnect();\n"
                + "}"));
            uk.add(LessonBlock.note(
                    "На Android мережевий запит не можна виконувати в головному UI-потоці. "
                    + "Додайте permission INTERNET і запускайте запит у фоновому потоці."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("URL and URI"));
            en.add(LessonBlock.code(
                "https://example.com:8443/api/users?id=42&active=true#section\n"
                + "  \\_/   \\_________/ \\_/ \\______/ \\_____________/ \\______/\n"
                + " scheme     host    port  path      query         fragment"));
            en.add(LessonBlock.paragraph(
                "Java's java.net.URI parses those components. For JDK 8 use HttpURLConnection "
                + "or a library such as OkHttp if it is added to the project."));
            en.add(LessonBlock.code(
                "import java.io.BufferedReader;\n"
                + "import java.io.InputStreamReader;\n"
                + "import java.net.HttpURLConnection;\n"
                + "import java.net.URI;\n"
                + "import java.net.URL;\n"
                + "\n"
                + "URI uri = URI.create(\"https://api.github.com/repos/octocat/Hello-World\");\n"
                + "URL url = uri.toURL();\n"
                + "HttpURLConnection conn = (HttpURLConnection) url.openConnection();\n"
                + "conn.setRequestMethod(\"GET\");\n"
                + "conn.setConnectTimeout(10000);\n"
                + "conn.setReadTimeout(10000);\n"
                + "conn.setRequestProperty(\"Accept\", \"application/json\");\n"
                + "\n"
                + "int status = conn.getResponseCode();\n"
                + "System.out.println(status);   // 200\n"
                + "\n"
                + "try (BufferedReader reader = new BufferedReader(\n"
                + "        new InputStreamReader(conn.getInputStream(), \"UTF-8\"))) {\n"
                + "    String line;\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        System.out.println(line);       // JSON\n"
                + "    }\n"
                + "} finally {\n"
                + "    conn.disconnect();\n"
                + "}"));
            en.add(LessonBlock.note(
                    "On Android, do not run network requests on the main UI thread. Add the "
                    + "INTERNET permission and run the request on a background thread."));
            return new Lesson("web.2", "URL/HttpURLConnection", "URL/HttpURLConnection", uk, en);
        }
    }

    // ── Servlet API & JSP ───────────────────────────────────────────────────

    static final class EssServletJsp {
        static void add(Course c) {
            Chapter ch = new Chapter(
                    "Servlet API та JSP",
                    "Servlet API and JSP");
            ch.add(lessonServlet());
            ch.add(lessonJsp());
            c.add(ch);
        }

        private static Lesson lessonServlet() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("Servlet"));
            uk.add(LessonBlock.paragraph(
                    "Servlet — Java-клас, що обробляє HTTP-запити у веб-контейнері (Tomcat, "
                    + "Jetty). HttpServlet має doGet/doPost. Один екземпляр на всі запити — "
                    + "не зберігайте стан у полях!"));
            uk.add(LessonBlock.code(
                "@WebServlet(\"/hello\")\n"
                + "public class HelloServlet extends HttpServlet {\n"
                + "    @Override\n"
                + "    protected void doGet(HttpServletRequest req, HttpServletResponse resp)\n"
                + "            throws IOException {\n"
                + "        String name = req.getParameter(\"name\");\n"
                + "        if (name == null) name = \"світ\";\n"
                + "        resp.setContentType(\"text/html; charset=UTF-8\");\n"
                + "        try (PrintWriter out = resp.getWriter()) {\n"
                + "            out.println(\"<h1>Привіт, \" + name + \"!</h1>\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
            uk.add(LessonBlock.warning(
                "Життєвий цикл: init() → service(doGet/doPost...) → destroy(). Один сервлет "
                + "обслуговує багато потоків одночасно — поля НЕ для стану запиту."));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("Servlet"));
            en.add(LessonBlock.paragraph(
                "A Servlet is a Java class handling HTTP requests in a web container "
                + "(Tomcat, Jetty). HttpServlet has doGet/doPost. One instance serves all "
                + "requests — do not store state in fields!"));
            en.add(LessonBlock.code(
                "@WebServlet(\"/hello\")\n"
                + "public class HelloServlet extends HttpServlet {\n"
                + "    @Override\n"
                + "    protected void doGet(HttpServletRequest req, HttpServletResponse resp)\n"
                + "            throws IOException {\n"
                + "        String name = req.getParameter(\"name\");\n"
                + "        if (name == null) name = \"world\";\n"
                + "        resp.setContentType(\"text/html; charset=UTF-8\");\n"
                + "        try (PrintWriter out = resp.getWriter()) {\n"
                + "            out.println(\"<h1>Hello, \" + name + \"!</h1>\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
            en.add(LessonBlock.warning(
                "Lifecycle: init() → service(doGet/doPost...) → destroy(). One servlet "
                + "serves many threads at once — fields are NOT for per-request state."));
            return new Lesson("srv.1", "Servlet", "Servlet", uk, en);
        }

        private static Lesson lessonJsp() {
            List<LessonBlock> uk = new ArrayList<>();
            uk.add(LessonBlock.heading("JSP"));
            uk.add(LessonBlock.paragraph(
                    "JSP (JavaServer Pages) — HTML із вставками Java <% ... %> та EL ${expr}. "
                    + "Внутрішньо JSP компілюється у сервлет. Сьогодні частіше використовують "
                    + "Thymeleaf/Freemarker, але JSP досі зустрічається в legacy-проектах."));
            uk.add(LessonBlock.code(
                "<%@ page contentType=\"text/html; charset=UTF-8\" %>\n"
                + "<ul>\n"
                + "  <% for (User u : users) { %>\n"
                + "    <li><%= u.getName() %> — <%= u.getEmail() %></li>\n"
                + "  <% } %>\n"
                + "</ul>\n"
                + "\n"
                + "<%-- EL-вираз --%>\n"
                + "<p>Всього користувачів: ${users.size()}</p>"));
            List<LessonBlock> en = new ArrayList<>();
            en.add(LessonBlock.heading("JSP"));
            en.add(LessonBlock.paragraph(
                "JSP (JavaServer Pages) is HTML with embedded Java <% ... %> and EL ${expr}. "
                + "Internally, it compiles into a servlet. Today template engines like Thymeleaf/Freemarker are "
                + "more common, but JSP is still maintained in legacy projects."));
            en.add(LessonBlock.code(
                "<%@ page contentType=\"text/html; charset=UTF-8\" %>\n"
                + "<ul>\n"
                + "  <% for (User u : users) { %>\n"
                + "    <li><%= u.getName() %> — <%= u.getEmail() %></li>\n"
                + "  <% } %>\n"
                + "</ul>\n"
                + "\n"
                + "<%-- EL expression --%>\n"
                + "<p>Total users: ${users.size()}</p>"));
            return new Lesson("srv.2", "JSP", "JSP", uk, en);
        }
    }
}
