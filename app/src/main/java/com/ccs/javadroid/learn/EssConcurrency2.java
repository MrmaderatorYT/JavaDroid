package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssConcurrency2 {
static void add(Course s) {
    Chapter ch = new Chapter(
            "Java Concurrency Essentials (Частина 2)",
            "Java Concurrency Essentials (Part 2)");
    ch.add(materialExecutor());
    ch.add(materialFuture());
    ch.add(materialConcurrentCollections());
    ch.add(materialCompletableFuture());
    s.add(ch);
}

private static Lesson materialExecutor() {
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

private static Lesson materialFuture() {
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

private static Lesson materialConcurrentCollections() {
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

private static Lesson materialCompletableFuture() {
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
