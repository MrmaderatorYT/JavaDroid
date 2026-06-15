package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Глави 3-15 курсу Java Complete Reference 9th.
 * Кожна глава має щонайменше один детальний двомовний урок;
 * інші уроки додаються у JrcChapterNN builders окремо.
 */
final class JrcChapters03to15 {

    static void add(Course c) {
        addChapter03(c);
        addChapter04(c);
        addChapter05(c);
        addChapter06(c);
        addChapter07(c);
        addChapter08(c);
        addChapter09(c);
        addChapter10(c);
        addChapter11(c);
        addChapter12(c);
        addChapter13(c);
        addChapter14(c);
        addChapter15(c);
    }

    // ── Глава 3. Керування потоком (деталі) — фактично у Ch.02, тут класи/методи ─
    private static void addChapter03(Course c) {
        Chapter ch = new Chapter("Глава 3. Класи та об'єкти", "Chapter 3. Classes and objects");
        ch.add(lessonClasses());
        ch.add(lessonMethods());
        ch.add(lessonConstructors());
        c.add(ch);
    }

    private static Lesson lessonClasses() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Класи та об'єкти"));
        uk.add(LessonBlock.paragraph(
                "Клас — шаблон, за яким створюються об'єкти. Клас об'єднує стан (поля) та "
                + "поведінку (методи). Об'єкт створюється оператором new."));
        uk.add(LessonBlock.code(
                "class Person {\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    void sayHello() {\n"
                + "        System.out.println(\"Привіт, я \" + name);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Person p = new Person();\n"
                + "p.name = \"Іван\";\n"
                + "p.age = 25;\n"
                + "p.sayHello();   // Привіт, я Іван"));
        uk.add(LessonBlock.note(
                "Локальні змінні посилального типу за замовчуванням не ініціалізуються. "
                + "Поля класу ініціалізуються: 0 для чисел, false для boolean, null для посилань."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Classes and objects"));
        en.add(LessonBlock.paragraph(
                "A class is a template from which objects are created. A class combines state "
                + "(fields) and behavior (methods). An object is created with the new operator."));
        en.add(LessonBlock.code(
                "class Person {\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    void sayHello() {\n"
                + "        System.out.println(\"Hi, I'm \" + name);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Person p = new Person();\n"
                + "p.name = \"John\";\n"
                + "p.age = 25;\n"
                + "p.sayHello();   // Hi, I'm John"));
        en.add(LessonBlock.note(
                "Local reference variables are not initialized by default. Class fields are "
                + "initialized: 0 for numbers, false for booleans, null for references."));
        return new Lesson("3.1", "Класи та об'єкти", "Classes and objects", uk, en);
    }

    private static Lesson lessonMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Методи"));
        uk.add(LessonBlock.code(
                "class Calc {\n"
                + "    // Метод із параметрами й результатом\n"
                + "    int add(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Перевантаження (overloading) — одне ім'я, різні параметри\n"
                + "    double add(double a, double b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Статичний метод\n"
                + "    static int square(int x) { return x * x; }\n"
                + "}\n"
                + "\n"
                + "Calc c = new Calc();\n"
                + "System.out.println(c.add(2, 3));       // 5\n"
                + "System.out.println(c.add(2.5, 3.5));   // 6.0\n"
                + "System.out.println(Calc.square(4));    // 16"));
        uk.add(LessonBlock.paragraph(
                "Параметри в Java передаються за значенням (pass-by-value). Для примітивів — "
                + "копіюється значення, для об'єктів — копіюється посилання (об'єкт не дублюється)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Methods"));
        en.add(LessonBlock.code(
                "class Calc {\n"
                + "    // Method with parameters and a result\n"
                + "    int add(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Overloading — same name, different parameters\n"
                + "    double add(double a, double b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Static method\n"
                + "    static int square(int x) { return x * x; }\n"
                + "}\n"
                + "\n"
                + "Calc c = new Calc();\n"
                + "System.out.println(c.add(2, 3));       // 5\n"
                + "System.out.println(c.add(2.5, 3.5));   // 6.0\n"
                + "System.out.println(Calc.square(4));    // 16"));
        en.add(LessonBlock.paragraph(
                "Parameters in Java are pass-by-value. For primitives the value is copied, for "
                + "objects the reference is copied (the object itself is not duplicated)."));
        return new Lesson("3.2", "Методи", "Methods", uk, en);
    }

    private static Lesson lessonConstructors() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Конструктори"));
        uk.add(LessonBlock.code(
                "class Point {\n"
                + "    int x, y;\n"
                + "\n"
                + "    // Конструктор за замовчуванням\n"
                + "    Point() {\n"
                + "        this(0, 0);   // виклик іншого конструктора\n"
                + "    }\n"
                + "\n"
                + "    // Конструктор із параметрами\n"
                + "    Point(int x, int y) {\n"
                + "        this.x = x;\n"
                + "        this.y = y;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Point a = new Point();      // (0,0)\n"
                + "Point b = new Point(3, 4);  // (3,4)"));
        uk.add(LessonBlock.note(
                "Якщо не оголосити жодного конструктора — компілятор згенерує порожній "
                + "конструктор за замовчуванням. Якщо є хоч один — автоматичний зникає."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Constructors"));
        en.add(LessonBlock.code(
                "class Point {\n"
                + "    int x, y;\n"
                + "\n"
                + "    // Default constructor\n"
                + "    Point() {\n"
                + "        this(0, 0);   // call another constructor\n"
                + "    }\n"
                + "\n"
                + "    // Parameterized constructor\n"
                + "    Point(int x, int y) {\n"
                + "        this.x = x;\n"
                + "        this.y = y;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Point a = new Point();      // (0,0)\n"
                + "Point b = new Point(3, 4);  // (3,4)"));
        en.add(LessonBlock.note(
                "If you declare no constructor, the compiler generates an empty default one. "
                + "If you add any constructor, the default one is no longer generated."));
        return new Lesson("3.3", "Конструктори", "Constructors", uk, en);
    }

    // ── Глави 4-15: одна главa = один репрезентативний урок (каркас + приклад) ─
    private static void addChapter04(Course c) {
        Chapter ch = new Chapter("Глава 4. Успадкування та поліморфізм",
                "Chapter 4. Inheritance and polymorphism");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Успадкування"));
        uk.add(LessonBlock.paragraph(
                "Ключове слово extends означує успадкування. Java підтримує лише одиночне "
                + "успадкування класів (але множинне — інтерфейсів)."));
        uk.add(LessonBlock.code(
                "class Animal {\n"
                + "    void breathe() { System.out.println(\"Дихаю\"); }\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    void bark() { System.out.println(\"Гав!\"); }\n"
                + "    @Override void breathe() { System.out.println(\"Собака дихає\"); }\n"
                + "}\n"
                + "Animal a = new Dog();   // поліморфізм\n"
                + "a.breathe();            // Собака дихає (динамічна диспетчеризація)"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Inheritance"));
        en.add(LessonBlock.paragraph(
                "The extends keyword denotes inheritance. Java supports only single class "
                + "inheritance (but multiple inheritance of interfaces)."));
        en.add(LessonBlock.code(
                "class Animal {\n"
                + "    void breathe() { System.out.println(\"Breathing\"); }\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    void bark() { System.out.println(\"Woof!\"); }\n"
                + "    @Override void breathe() { System.out.println(\"Dog breathing\"); }\n"
                + "}\n"
                + "Animal a = new Dog();   // polymorphism\n"
                + "a.breathe();            // Dog breathing (dynamic dispatch)"));
        ch.add(new Lesson("4.1", "Успадкування", "Inheritance", uk, en));
        c.add(ch);
    }

    private static void addChapter05(Course c) {
        Chapter ch = new Chapter("Глава 5. Інтерфейси", "Chapter 5. Interfaces");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Інтерфейси"));
        uk.add(LessonBlock.paragraph(
                "Інтерфейс описує контракт: набір методів без реалізації (до Java 8). "
                + "Клас реалізує інтерфейс через implements і може реалізувати кілька інтерфейсів."));
        uk.add(LessonBlock.code(
                "interface Drawable {\n"
                + "    void draw();\n"
                + "    default void drawTwice() { draw(); draw(); }  // Java 8+\n"
                + "}\n"
                + "class Circle implements Drawable {\n"
                + "    @Override public void draw() { System.out.println(\"○\"); }\n"
                + "}\n"
                + "new Circle().drawTwice();   // ○ ○"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Interfaces"));
        en.add(LessonBlock.paragraph(
                "An interface describes a contract: a set of methods without implementation "
                + "(pre-Java 8). A class implements an interface via implements and may "
                + "implement multiple interfaces."));
        en.add(LessonBlock.code(
                "interface Drawable {\n"
                + "    void draw();\n"
                + "    default void drawTwice() { draw(); draw(); }  // Java 8+\n"
                + "}\n"
                + "class Circle implements Drawable {\n"
                + "    @Override public void draw() { System.out.println(\"○\"); }\n"
                + "}\n"
                + "new Circle().drawTwice();   // ○ ○"));
        ch.add(new Lesson("5.1", "Інтерфейси", "Interfaces", uk, en));
        c.add(ch);
    }

    private static void addChapter06(Course c) {
        Chapter ch = new Chapter("Глава 6. Пакети", "Chapter 6. Packages");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Пакети та модифікатори доступу"));
        uk.add(LessonBlock.paragraph(
                "Пакет (package) — простір імен для класів. Доступ: public (всюди), protected "
                + "(пакет + нащадки), default/package-private (лише пакет), private (лише клас)."));
        uk.add(LessonBlock.code(
                "package com.example.util;\n"
                + "public class StringHelper {\n"
                + "    private String value;        // лише всередині класу\n"
                + "    public StringHelper(String v){ value = v; }\n"
                + "    public String reverse() { return new StringBuilder(value).reverse().toString(); }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Packages and access modifiers"));
        en.add(LessonBlock.paragraph(
                "A package is a namespace for classes. Access: public (everywhere), protected "
                + "(package + subclasses), default/package-private (package only), private "
                + "(class only)."));
        en.add(LessonBlock.code(
                "package com.example.util;\n"
                + "public class StringHelper {\n"
                + "    private String value;        // class only\n"
                + "    public StringHelper(String v){ value = v; }\n"
                + "    public String reverse() { return new StringBuilder(value).reverse().toString(); }\n"
                + "}"));
        ch.add(new Lesson("6.1", "Пакети", "Packages", uk, en));
        c.add(ch);
    }

    private static void addChapter07(Course c) {
        Chapter ch = new Chapter("Глава 7. Обробка винятків", "Chapter 7. Exception handling");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Винятки"));
        uk.add(LessonBlock.paragraph(
                "Винятки в Java — об'єкти, що сигналізують про помилку. Перевіряємі (checked) "
                + "винятки вимагають try/catch або throws у сигнатурі; неперевіряємі "
                + "(RuntimeException) — ні."));
        uk.add(LessonBlock.code(
                "try {\n"
                + "    int x = 10 / 0;\n"
                + "} catch (ArithmeticException e) {\n"
                + "    System.out.println(\"Ділення на нуль: \" + e.getMessage());\n"
                + "} finally {\n"
                + "    System.out.println(\"Завжди виконується\");\n"
                + "}\n"
                + "\n"
                + "// Створення власного винятку\n"
                + "class InvalidAgeException extends Exception {\n"
                + "    public InvalidAgeException(String msg) { super(msg); }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Exceptions"));
        en.add(LessonBlock.paragraph(
                "Exceptions in Java are objects signaling an error. Checked exceptions require "
                + "try/catch or a throws clause; unchecked (RuntimeException) do not."));
        en.add(LessonBlock.code(
                "try {\n"
                + "    int x = 10 / 0;\n"
                + "} catch (ArithmeticException e) {\n"
                + "    System.out.println(\"Division by zero: \" + e.getMessage());\n"
                + "} finally {\n"
                + "    System.out.println(\"Always runs\");\n"
                + "}\n"
                + "\n"
                + "// Custom exception\n"
                + "class InvalidAgeException extends Exception {\n"
                + "    public InvalidAgeException(String msg) { super(msg); }\n"
                + "}"));
        ch.add(new Lesson("7.1", "Винятки", "Exceptions", uk, en));
        c.add(ch);
    }

    private static void addChapter08(Course c) {
        Chapter ch = new Chapter("Глава 8. Рядки", "Chapter 8. Strings");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Рядки: String, StringBuilder, StringBuffer"));
        uk.add(LessonBlock.paragraph(
                "String — незмінний (immutable). StringBuilder — змінний, несинхронізований "
                + "(швидкий). StringBuffer — синхронізований (потокобезпечний)."));
        uk.add(LessonBlock.code(
                "String s = \"hello\";\n"
                + "String upper = s.toUpperCase();   // HELLO (s не змінюється)\n"
                + "String joined = String.join(\", \", \"a\", \"b\", \"c\");  // a, b, c\n"
                + "\n"
                + "StringBuilder sb = new StringBuilder();\n"
                + "for (int i = 0; i < 100; i++) sb.append(i).append(' ');\n"
                + "String result = sb.toString();"));
        uk.add(LessonBlock.warning(
                "Конкатенація String через + у циклі створює багато проміжних об'єктів — "
                + "у циклі завжди StringBuilder."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Strings: String, StringBuilder, StringBuffer"));
        en.add(LessonBlock.paragraph(
                "String is immutable. StringBuilder is mutable and unsynchronized (fast). "
                + "StringBuffer is synchronized (thread-safe)."));
        en.add(LessonBlock.code(
                "String s = \"hello\";\n"
                + "String upper = s.toUpperCase();   // HELLO (s unchanged)\n"
                + "String joined = String.join(\", \", \"a\", \"b\", \"c\");  // a, b, c\n"
                + "\n"
                + "StringBuilder sb = new StringBuilder();\n"
                + "for (int i = 0; i < 100; i++) sb.append(i).append(' ');\n"
                + "String result = sb.toString();"));
        en.add(LessonBlock.warning(
                "Concatenating String with + in a loop creates many intermediate objects — "
                + "always use StringBuilder in loops."));
        ch.add(new Lesson("8.1", "Рядки", "Strings", uk, en));
        c.add(ch);
    }

    private static void addChapter09(Course c) {
        Chapter ch = new Chapter("Глава 9. Колекції", "Chapter 9. Collections");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Колекції"));
        uk.add(LessonBlock.paragraph(
                "java.util: List (ArrayList, LinkedList), Set (HashSet, TreeSet), "
                + "Map (HashMap, TreeMap), Queue (ArrayDeque, PriorityQueue)."));
        uk.add(LessonBlock.code(
                "List<String> list = new ArrayList<>();\n"
                + "list.add(\"a\"); list.add(\"b\"); list.add(\"a\");\n"
                + "System.out.println(list);           // [a, b, a]\n"
                + "\n"
                + "Set<String> set = new HashSet<>(list);\n"
                + "System.out.println(set);             // [a, b] (унікальні)\n"
                + "\n"
                + "Map<String, Integer> ages = new HashMap<>();\n"
                + "ages.put(\"Іван\", 30);\n"
                + "ages.forEach((k, v) -> System.out.println(k + \"=\" + v));"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Collections"));
        en.add(LessonBlock.paragraph(
                "java.util: List (ArrayList, LinkedList), Set (HashSet, TreeSet), "
                + "Map (HashMap, TreeMap), Queue (ArrayDeque, PriorityQueue)."));
        en.add(LessonBlock.code(
                "List<String> list = new ArrayList<>();\n"
                + "list.add(\"a\"); list.add(\"b\"); list.add(\"a\");\n"
                + "System.out.println(list);           // [a, b, a]\n"
                + "\n"
                + "Set<String> set = new HashSet<>(list);\n"
                + "System.out.println(set);             // [a, b] (unique)\n"
                + "\n"
                + "Map<String, Integer> ages = new HashMap<>();\n"
                + "ages.put(\"John\", 30);\n"
                + "ages.forEach((k, v) -> System.out.println(k + \"=\" + v));"));
        ch.add(new Lesson("9.1", "Колекції", "Collections", uk, en));
        c.add(ch);
    }

    private static void addChapter10(Course c) {
        Chapter ch = new Chapter("Глава 10. Потоки введення-виведення", "Chapter 10. I/O streams");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Робота з файлами (NIO.2)"));
        uk.add(LessonBlock.code(
                "import java.nio.file.*;\n"
                + "\n"
                + "Path p = Paths.get(\"test.txt\");\n"
                + "Files.write(p, \"Привіт\\n\".getBytes());\n"
                + "List<String> lines = Files.readAllLines(p);\n"
                + "lines.forEach(System.out::println);"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("File I/O (NIO.2)"));
        en.add(LessonBlock.code(
                "import java.nio.file.*;\n"
                + "\n"
                + "Path p = Paths.get(\"test.txt\");\n"
                + "Files.write(p, \"Hello\\n\".getBytes());\n"
                + "List<String> lines = Files.readAllLines(p);\n"
                + "lines.forEach(System.out::println);"));
        ch.add(new Lesson("10.1", "Робота з файлами", "File I/O", uk, en));
        c.add(ch);
    }

    private static void addChapter11(Course c) {
        Chapter ch = new Chapter("Глава 11. Багатопоточність", "Chapter 11. Multithreading");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Потоки"));
        uk.add(LessonBlock.code(
                "Thread t = new Thread(() -> {\n"
                + "    for (int i = 0; i < 5; i++) System.out.println(i);\n"
                + "});\n"
                + "t.start();\n"
                + "t.join();   // дочекатися завершення"));
        uk.add(LessonBlock.paragraph(
                "Синхронізація: synchronized блокує об'єкт/метод; java.util.concurrent.atomic — "
                + "атомарні операції без блокувань; ReentrantLock — гнучке блокування."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Threads"));
        en.add(LessonBlock.code(
                "Thread t = new Thread(() -> {\n"
                + "    for (int i = 0; i < 5; i++) System.out.println(i);\n"
                + "});\n"
                + "t.start();\n"
                + "t.join();   // wait for completion"));
        en.add(LessonBlock.paragraph(
                "Synchronization: synchronized locks an object/method; java.util.concurrent.atomic "
                + "— lock-free atomic operations; ReentrantLock — flexible locking."));
        ch.add(new Lesson("11.1", "Потоки", "Threads", uk, en));
        c.add(ch);
    }

    private static void addChapter12(Course c) {
        Chapter ch = new Chapter("Глава 12. Лямбда-вирази та Stream API",
                "Chapter 12. Lambda and Stream API");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Лямбда-вирази та Stream API"));
        uk.add(LessonBlock.code(
                "List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6);\n"
                + "\n"
                + "// Сума парних через Stream API\n"
                + "int sum = nums.stream()\n"
                + "    .filter(n -> n % 2 == 0)\n"
                + "    .mapToInt(Integer::intValue)\n"
                + "    .sum();\n"
                + "System.out.println(sum);   // 12"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Lambda expressions and Stream API"));
        en.add(LessonBlock.code(
                "List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6);\n"
                + "\n"
                + "// Sum of evens via Stream API\n"
                + "int sum = nums.stream()\n"
                + "    .filter(n -> n % 2 == 0)\n"
                + "    .mapToInt(Integer::intValue)\n"
                + "    .sum();\n"
                + "System.out.println(sum);   // 12"));
        ch.add(new Lesson("12.1", "Лямбди та Stream", "Lambda & Stream", uk, en));
        c.add(ch);
    }

    private static void addChapter13(Course c) {
        Chapter ch = new Chapter("Глава 13. Узагальнення (Generics)",
                "Chapter 13. Generics");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Generics"));
        uk.add(LessonBlock.code(
                "class Box<T> {\n"
                + "    private T value;\n"
                + "    public void set(T v) { value = v; }\n"
                + "    public T get() { return value; }\n"
                + "}\n"
                + "\n"
                + "Box<String> sb = new Box<>();\n"
                + "sb.set(\"hi\");\n"
                + "String s = sb.get();   // без приведення типу"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Generics"));
        en.add(LessonBlock.code(
                "class Box<T> {\n"
                + "    private T value;\n"
                + "    public void set(T v) { value = v; }\n"
                + "    public T get() { return value; }\n"
                + "}\n"
                + "\n"
                + "Box<String> sb = new Box<>();\n"
                + "sb.set(\"hi\");\n"
                + "String s = sb.get();   // no cast needed"));
        ch.add(new Lesson("13.1", "Generics", "Generics", uk, en));
        c.add(ch);
    }

    private static void addChapter14(Course c) {
        Chapter ch = new Chapter("Глава 14. Перерахування enum", "Chapter 14. Enumerations");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("enum"));
        uk.add(LessonBlock.code(
                "enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }\n"
                + "\n"
                + "Day today = Day.WED;\n"
                + "switch (today) {\n"
                + "    case SAT: case SUN: System.out.println(\"Вихідний\"); break;\n"
                + "    default: System.out.println(\"Робочий день\");\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("enum"));
        en.add(LessonBlock.code(
                "enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }\n"
                + "\n"
                + "Day today = Day.WED;\n"
                + "switch (today) {\n"
                + "    case SAT: case SUN: System.out.println(\"Weekend\"); break;\n"
                + "    default: System.out.println(\"Weekday\");\n"
                + "}"));
        ch.add(new Lesson("14.1", "enum", "enum", uk, en));
        c.add(ch);
    }

    private static void addChapter15(Course c) {
        Chapter ch = new Chapter("Глава 15. Анотації", "Chapter 15. Annotations");
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Анотації"));
        uk.add(LessonBlock.paragraph(
                "Анотація — метадані над класом/методом/полем. Стандартні: @Override, "
                + "@Deprecated, @SuppressWarnings. Власні через @interface."));
        uk.add(LessonBlock.code(
                "@interface Author { String value(); }\n"
                + "\n"
                + "@Author(\"Іван\")\n"
                + "class MyClass { }"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Annotations"));
        en.add(LessonBlock.paragraph(
                "An annotation is metadata over a class/method/field. Standard ones: @Override, "
                + "@Deprecated, @SuppressWarnings. Custom via @interface."));
        en.add(LessonBlock.code(
                "@interface Author { String value(); }\n"
                + "\n"
                + "@Author(\"John\")\n"
                + "class MyClass { }"));
        ch.add(new Lesson("15.1", "Анотації", "Annotations", uk, en));
        c.add(ch);
    }
}
