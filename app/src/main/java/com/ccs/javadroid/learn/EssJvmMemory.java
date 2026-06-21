package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EssJvmMemory {
static void add(Course s) {
    Chapter ch = new Chapter(
            "Керування пам'яттю JVM",
            "JVM Memory Management");
    ch.add(materialAreas());
    ch.add(materialGc());
    ch.add(materialLeaks());
    s.add(ch);
}

private static Lesson materialAreas() {
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

private static Lesson materialGc() {
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

private static Lesson materialLeaks() {
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
