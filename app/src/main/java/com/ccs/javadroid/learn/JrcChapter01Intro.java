package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Глава 1. Вступ до Java (Complete Reference 9th).
 */
final class JrcChapter01Intro {

    static void add(Course c) {
        Chapter ch = new Chapter(
                "Глава 1. Історія та філософія Java",
                "Chapter 1. History and Philosophy of Java");
        ch.add(lessonWhatIsJava());
        ch.add(lessonFeatures());
        ch.add(lessonBytecode());
        ch.add(lessonFirstProgram());
        ch.add(lessonVariables());
        c.add(ch);
    }

    // ── 1.1 Що таке Java ───────────────────────────────────────────────────

    private static Lesson lessonWhatIsJava() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Що таке Java"));
        uk.add(LessonBlock.paragraph(
                "Java — об'єктно-орієнтована, типобезпечна, платформонезалежна мова програмування "
                + "загального призначення, розроблена Sun Microsystems (нині Oracle). Перша публічна "
                + "версія вийшла 1995 року. Головне гасло мови — «Write Once, Run Anywhere» "
                + "(пиши один раз — запускай скрізь): скомпільована Java-програма виконується на "
                + "будь-якій платформі, де встановлено JVM."));
        uk.add(LessonBlock.paragraph(
                "Ключова риса Java — поділ на два етапи: вихідний код (.java) компілюється у "
                + "байткод (.class), а байткод вже виконує віртуальна машина (JVM). Тому програма "
                + "не прив'язана до конкретної архітектури чи ОС."));
        uk.add(LessonBlock.note(
                "JDK (Java Development Kit) — набір для розробки (компілятор, інструменти). "
                + "JRE (Java Runtime Environment) — середовище виконання. "
                + "JVM (Java Virtual Machine) — віртуальна машина, що виконує байткод."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("What is Java"));
        en.add(LessonBlock.paragraph(
                "Java is an object-oriented, type-safe, platform-independent general-purpose "
                + "programming language developed by Sun Microsystems (now Oracle). The first "
                + "public version was released in 1995. The language's main motto is "
                + "\"Write Once, Run Anywhere\": a compiled Java program runs on any platform "
                + "that has a JVM installed."));
        en.add(LessonBlock.paragraph(
                "A defining feature of Java is the two-stage model: source code (.java) is "
                + "compiled into bytecode (.class), and the bytecode is then executed by the "
                + "Java Virtual Machine. Because of this, programs are not tied to a specific "
                + "architecture or operating system."));
        en.add(LessonBlock.note(
                "JDK (Java Development Kit) — the development kit (compiler, tools). "
                + "JRE (Java Runtime Environment) — the runtime environment. "
                + "JVM (Java Virtual Machine) — the virtual machine that executes bytecode."));

        return new Lesson("1.1", "Що таке Java", "What is Java", uk, en);
    }

    // ── 1.2 Основні властивості ────────────────────────────────────────────

    private static Lesson lessonFeatures() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Ключові властивості мови"));
        uk.add(LessonBlock.list(
                "Простота — синтаксис наближений до C/C++, але без вказівників і ручного керування пам'яттю.",
                "Об'єктно-орієнтованість — усе в Java є об'єктом (окрім примітивів).",
                "Розподілена вбудована підтримка мережі (java.net).",
                "Надійність — сувора типізація, перевірки під час компіляції та виконання.",
                "Безпека — пісочниця, перевірка байткоду, менеджер безпеки.",
                "Незалежність від архітектури — байткод + JVM.",
                "Багатопоточність — вбудована підтримка потоків на рівні мови.",
                "Динамічність — завантаження класів під час виконання, рефлексія."));
        uk.add(LessonBlock.paragraph(
                "Java автоматично керує пам'яттю через збиральник сміття (Garbage Collector): "
                + "об'єкти, на які не лишилося посилань, знищуються автоматично. Це усуває цілий "
                + "клас помилок, пов'язаних із витоками пам'яті та «висячими» вказівниками."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Key language features"));
        en.add(LessonBlock.list(
                "Simple — the syntax is close to C/C++, but without pointers and manual memory management.",
                "Object-oriented — everything in Java is an object (except primitives).",
                "Distributed — built-in networking support (java.net).",
                "Robust — strong typing, compile-time and runtime checks.",
                "Secure — sandbox, bytecode verification, security manager.",
                "Architecture-neutral — bytecode + JVM.",
                "Multithreaded — built-in language-level thread support.",
                "Dynamic — runtime class loading, reflection."));
        en.add(LessonBlock.paragraph(
                "Java manages memory automatically through a Garbage Collector: objects with no "
                + "remaining references are destroyed automatically. This eliminates a whole class "
                + "of errors related to memory leaks and dangling pointers."));

        return new Lesson("1.2", "Ключові властивості", "Key features", uk, en);
    }

    // ── 1.3 Байткод ────────────────────────────────────────────────────────

    private static Lesson lessonBytecode() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Байткод і JVM"));
        uk.add(LessonBlock.paragraph(
                "Вихідний код компілюється компілятором javac у проміжне подання — байткод. "
                + "Це набір інструкцій, оптимізований для JVM. Кожен .class-файл починається з "
                + "магічного числа 0xCAFEBABE і містить константний пул, опис полів, методів та "
                + "інструкцій."));
        uk.add(LessonBlock.paragraph(
                "JVM завантажує класи через ClassLoader, перевіряє байткод (верифікатор) і "
                + "виконує його. Сучасні JVM використовують JIT-компіляцію (Just-In-Time): "
                + "гарячі ділянки байткоду компілюються у машинний код прямо під час виконання, "
                + "що дає продуктивність, порівнянну з C/C++."));
        uk.add(LessonBlock.note(
                "Переглянути байткод можна утилітою javap: javap -c -p MyClass.class"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Bytecode and the JVM"));
        en.add(LessonBlock.paragraph(
                "Source code is compiled by the javac compiler into an intermediate representation "
                + "called bytecode. This is a set of instructions optimized for the JVM. Each "
                + ".class file starts with the magic number 0xCAFEBABE and contains a constant "
                + "pool, a description of fields, methods and instructions."));
        en.add(LessonBlock.paragraph(
                "The JVM loads classes via a ClassLoader, verifies the bytecode (verifier) and "
                + "executes it. Modern JVMs use Just-In-Time compilation: hot regions of bytecode "
                + "are compiled into native code at runtime, yielding performance comparable to C/C++."));
        en.add(LessonBlock.note(
                "Inspect the bytecode with the javap tool: javap -c -p MyClass.class"));

        return new Lesson("1.3", "Байткод і JVM", "Bytecode and the JVM", uk, en);
    }

    // ── 1.4 Перша програма ─────────────────────────────────────────────────

    private static Lesson lessonFirstProgram() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Перша програма"));
        uk.add(LessonBlock.paragraph(
                "Традиційна перша програма — «Hello, World!». У Java кожна програма складається "
                + "щонайменше з одного класу з методом main:"));
        uk.add(LessonBlock.code(
                "public class HelloWorld {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Привіт, світ!\");\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph("Розбір:"));
        uk.add(LessonBlock.list(
                "public class HelloWorld — оголошення публічного класу; ім'я файлу має збігатися з іменем класу (HelloWorld.java).",
                "public static void main(String[] args) — точка входу; JVM викликає саме цей метод.",
                "System.out.println(...) — друк рядка в стандартний потік виведення."));
        uk.add(LessonBlock.paragraph(
                "Компіляція й запуск з командного рядка:"));
        uk.add(LessonBlock.code(
                "javac HelloWorld.java   # створить HelloWorld.class\n"
                + "java HelloWorld         # виконає програму\n"
                + "# Виведе: Привіт, світ!"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Your first program"));
        en.add(LessonBlock.paragraph(
                "The traditional first program is \"Hello, World!\". In Java every program "
                + "consists of at least one class with a main method:"));
        en.add(LessonBlock.code(
                "public class HelloWorld {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello, World!\");\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.paragraph("Breakdown:"));
        en.add(LessonBlock.list(
                "public class HelloWorld — declares a public class; the file name must match the class name (HelloWorld.java).",
                "public static void main(String[] args) — the entry point; the JVM calls exactly this method.",
                "System.out.println(...) — prints a line to the standard output stream."));
        en.add(LessonBlock.paragraph("Compile and run from the command line:"));
        en.add(LessonBlock.code(
                "javac HelloWorld.java   # produces HelloWorld.class\n"
                + "java HelloWorld         # runs the program\n"
                + "# Prints: Hello, World!"));

        return new Lesson("1.4", "Перша програма", "First program", uk, en);
    }

    // ── 1.5 Змінні ─────────────────────────────────────────────────────────

    private static Lesson lessonVariables() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Змінні та типи"));
        uk.add(LessonBlock.paragraph(
                "Java — мова зі суворою статичною типізацією: тип кожної змінної вказується "
                + "під час оголошення. Примітивні типи:"));
        uk.add(LessonBlock.table(
                "Тип\tРозмір\tДіапазон",
                java.util.Arrays.asList(
                        "byte\t1 байт\t-128..127",
                        "short\t2 байти\t-32768..32767",
                        "int\t4 байти\t±2·10⁹",
                        "long\t8 байтів\t±9·10¹⁸",
                        "float\t4 байти\t±3.4·10³⁸ (31 біт мантиса)",
                        "double\t8 байтів\t±1.7·10³⁰⁸ (52 біти мантиса)",
                        "char\t2 байти\tсимвол UTF-16 (0..65535)",
                        "boolean\t—\ttrue / false")));
        uk.add(LessonBlock.code(
                "public class Vars {\n"
                + "    public static void main(String[] args) {\n"
                + "        int age = 30;\n"
                + "        double price = 19.99;\n"
                + "        boolean active = true;\n"
                + "        char grade = 'A';\n"
                + "        String name = \"Олена\";\n"
                + "        System.out.println(name + \", вік \" + age);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Літерали типу float треба писати з суфіксом f (3.14f), інакше 3.14 трактується як double. "
                + "Літерали long мають суфікс L: 1_000_000L."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Variables and types"));
        en.add(LessonBlock.paragraph(
                "Java is a strongly, statically typed language: every variable's type is declared "
                + "up front. Primitive types:"));
        en.add(LessonBlock.table(
                "Type\tSize\tRange",
                java.util.Arrays.asList(
                        "byte\t1 byte\t-128..127",
                        "short\t2 bytes\t-32768..32767",
                        "int\t4 bytes\t±2·10⁹",
                        "long\t8 bytes\t±9·10¹⁸",
                        "float\t4 bytes\t±3.4·10³⁸ (31 bits mantissa)",
                        "double\t8 bytes\t±1.7·10³⁰⁸ (52 bits mantissa)",
                        "char\t2 bytes\tUTF-16 code unit (0..65535)",
                        "boolean\t—\ttrue / false")));
        en.add(LessonBlock.code(
                "public class Vars {\n"
                + "    public static void main(String[] args) {\n"
                + "        int age = 30;\n"
                + "        double price = 19.99;\n"
                + "        boolean active = true;\n"
                + "        char grade = 'A';\n"
                + "        String name = \"Helen\";\n"
                + "        System.out.println(name + \", age \" + age);\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.warning(
                "float literals must have the f suffix (3.14f), otherwise 3.14 is treated as a double. "
                + "long literals use the L suffix: 1_000_000L."));

        return new Lesson("1.5", "Змінні та типи", "Variables and types", uk, en);
    }
}
