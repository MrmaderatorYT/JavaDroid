package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssConcurrency1 {
static void add(Course s) {
    Chapter ch = new Chapter(
            "Java Concurrency Essentials (Частина 1)",
            "Java Concurrency Essentials (Part 1)");
    ch.add(materialThreads());
    ch.add(materialSync());
    ch.add(materialVolatile());
    ch.add(materialAtomic());
    s.add(ch);
}

private static Lesson materialThreads() {
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

private static Lesson materialSync() {
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

private static Lesson materialVolatile() {
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

private static Lesson materialAtomic() {
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
