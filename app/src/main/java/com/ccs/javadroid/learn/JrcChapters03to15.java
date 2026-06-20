package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Глави 3-15 курсу Java Complete Reference 9th.
 * Кожна глава має кілька детальних двомовних уроків з прикладами для початківців.
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

    // ═══════════════════════════════════════════════════════════════
    //  Глава 3. Класи та об'єкти
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter03(Course c) {
        Chapter ch = new Chapter("Глава 3. Класи та об'єкти", "Chapter 3. Classes and objects");
        ch.add(lessonClasses());
        ch.add(lessonMethods());
        ch.add(lessonConstructors());
        ch.add(lessonThisAndStatic());
        c.add(ch);
    }

    private static Lesson lessonClasses() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Класи та об'єкти"));
        uk.add(LessonBlock.paragraph(
                "Клас — це шаблон (наче креслення), за яким створюються об'єкти. "
                + "Уявіть клас як «рецепт тістечка», а об'єкт — саме тістечко, "
                + "зварене за цим рецептом. Один рецепт — багато тістечок."));
        uk.add(LessonBlock.paragraph(
                "Клас об'єднує стан (поля — «що клас знає») та поведінку "
                + "(методи — «що клас може робити»). Об'єкт створюється оператором new."));
        uk.add(LessonBlock.code(
                "class Person {\n"
                + "    // Поля (стан) — кожен об'єкт має СВОЇ копії\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    // Метод (поведінка) — спільний для всіх об'єктів\n"
                + "    void sayHello() {\n"
                + "        System.out.println(\"Привіт, я \" + name + \", мені \" + age);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Створюємо два різних об'єкти з одного класу\n"
                + "Person ivan = new Person();\n"
                + "ivan.name = \"Іван\";\n"
                + "ivan.age = 25;\n"
                + "\n"
                + "Person olena = new Person();\n"
                + "olena.name = \"Олена\";\n"
                + "olena.age = 30;\n"
                + "\n"
                + "ivan.sayHello();   // Привіт, я Іван, мені 25\n"
                + "olena.sayHello();  // Привіт, я Олена, мені 30"));
        uk.add(LessonBlock.heading("Типи даних у Java"));
        uk.add(LessonBlock.table(
                "Тип\tРозмір\tДіапазон\tПриклад",
                Arrays.asList(
                    "byte\t1 байт\t-128..127\tbyte b = 42;",
                    "short\t2 байти\t-32768..32767\tshort s = 1000;",
                    "int\t4 байти\t~±2 млрд\tint x = 100;",
                    "long\t8 байт\tдуже великий\tlong big = 100000L;",
                    "float\t4 байти\t~7 знаків\tfloat f = 3.14f;",
                    "double\t8 байти\t~15 знаків\tdouble d = 3.14;",
                    "char\t2 байти\t0..65535\tchar c = 'A';",
                    "boolean\t1 bit\ttrue/false\tboolean ok = true;")));
        uk.add(LessonBlock.warning(
                "Локальні змінні посилального типу (String, тощо) за замовчуванням НЕ "
                + "ініціалізуються — компілятор видасть помилку. Поля класу ініціалізуються "
                + "автоматично: 0 для чисел, false для boolean, null для посилань."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Classes and objects"));
        en.add(LessonBlock.paragraph(
                "A class is a template (like a blueprint) from which objects are created. "
                + "Think of a class as a \"cookie cutter\" and objects as the cookies — "
                + "one cutter, many cookies."));
        en.add(LessonBlock.paragraph(
                "A class combines state (fields — \"what the class knows\") and behavior "
                + "(methods — \"what the class can do\"). An object is created with new."));
        en.add(LessonBlock.code(
                "class Person {\n"
                + "    // Fields (state) — each object has its OWN copy\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    // Method (behavior) — shared by all instances\n"
                + "    void sayHello() {\n"
                + "        System.out.println(\"Hi, I'm \" + name + \", I'm \" + age);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Create two different objects from one class\n"
                + "Person john = new Person();\n"
                + "john.name = \"John\";\n"
                + "john.age = 25;\n"
                + "\n"
                + "Person helen = new Person();\n"
                + "helen.name = \"Helen\";\n"
                + "helen.age = 30;\n"
                + "\n"
                + "john.sayHello();   // Hi, I'm John, I'm 25\n"
                + "helen.sayHello();  // Hi, I'm Helen, I'm 30"));
        en.add(LessonBlock.heading("Primitive types in Java"));
        en.add(LessonBlock.table(
                "Type\tSize\tRange\tExample",
                Arrays.asList(
                    "byte\t1 byte\t-128..127\tbyte b = 42;",
                    "short\t2 bytes\t-32768..32767\tshort s = 1000;",
                    "int\t4 bytes\t~±2 billion\tint x = 100;",
                    "long\t8 bytes\tvery large\tlong big = 100000L;",
                    "float\t4 bytes\t~7 digits\tfloat f = 3.14f;",
                    "double\t8 bytes\t~15 digits\tdouble d = 3.14;",
                    "char\t2 bytes\t0..65535\tchar c = 'A';",
                    "boolean\t1 bit\ttrue/false\tboolean ok = true;")));
        en.add(LessonBlock.warning(
                "Local reference variables (String, etc.) are NOT initialized by default — "
                + "the compiler will give an error. Class fields are initialized automatically: "
                + "0 for numbers, false for booleans, null for references."));
        return new Lesson("3.1", "Класи та об'єкти", "Classes and objects", uk, en);
    }

    private static Lesson lessonMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Методи: як класи \"діють\""));
        uk.add(LessonBlock.paragraph(
                "Метод — це блок коду з ім'ям, який виконує певну дію. Може приймати "
                + "параметри (вхідні дані) та повертати результат."));
        uk.add(LessonBlock.code(
                "class Calc {\n"
                + "    // Метод з параметрами й результатом\n"
                + "    int add(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Метод без результату (void)\n"
                + "    void printResult(int a, int b) {\n"
                + "        System.out.println(a + \" + \" + b + \" = \" + add(a, b));\n"
                + "    }\n"
                + "\n"
                + "    // Перевантаження (overloading) — одне ім'я, різні параметри\n"
                + "    double add(double a, double b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Статичний метод — викликається через назву класу\n"
                + "    static int square(int x) { return x * x; }\n"
                + "}\n"
                + "\n"
                + "Calc c = new Calc();\n"
                + "System.out.println(c.add(2, 3));       // 5\n"
                + "System.out.println(c.add(2.5, 3.5));   // 6.0\n"
                + "System.out.println(Calc.square(4));    // 16"));
        uk.add(LessonBlock.heading("Передавання параметрів"));
        uk.add(LessonBlock.paragraph(
                "В Java параметри завжди передаються за ЗНАЧЕННЯМ (pass-by-value). "
                + "Для примітивів — копіюється значення. Для об'єктів — копіюється "
                + "ПОСИЛАННЯ (сам об'єкт НЕ дублюється)."));
        uk.add(LessonBlock.code(
                "class Box { int value; }\n"
                + "\n"
                + "void changePrimitive(int x) {\n"
                + "    x = 999;  // змінює лише локальну копію\n"
                + "}\n"
                + "\n"
                + "void changeObject(Box b) {\n"
                + "    b.value = 999;  // змінює ОБ'ЄКТ, на який вказує посилання\n"
                + "    b = null;       // обнулює лише локальну копію посилання!\n"
                + "}\n"
                + "\n"
                + "int num = 10;\n"
                + "changePrimitive(num);\n"
                + "System.out.println(num);  // 10 (не змінилося!)\n"
                + "\n"
                + "Box box = new Box();\n"
                + "box.value = 42;\n"
                + "changeObject(box);\n"
                + "System.out.println(box.value);  // 999 (змінилося!)"));
        uk.add(LessonBlock.note(
                "Порада: щоб \"повернути\" кілька значень з методу, створіть клас-контейнер "
                + "або використайте масив. Для JDK 8 це найзрозуміліший і найсумісніший варіант."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Methods: how classes \"act\""));
        en.add(LessonBlock.paragraph(
                "A method is a named block of code that performs an action. It can accept "
                + "parameters (input data) and return a result."));
        en.add(LessonBlock.code(
                "class Calc {\n"
                + "    // Method with parameters and return value\n"
                + "    int add(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Void method\n"
                + "    void printResult(int a, int b) {\n"
                + "        System.out.println(a + \" + \" + b + \" = \" + add(a, b));\n"
                + "    }\n"
                + "\n"
                + "    // Overloading — same name, different parameters\n"
                + "    double add(double a, double b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Static method — called via class name\n"
                + "    static int square(int x) { return x * x; }\n"
                + "}\n"
                + "\n"
                + "Calc c = new Calc();\n"
                + "System.out.println(c.add(2, 3));       // 5\n"
                + "System.out.println(c.add(2.5, 3.5));   // 6.0\n"
                + "System.out.println(Calc.square(4));    // 16"));
        en.add(LessonBlock.heading("Passing parameters"));
        en.add(LessonBlock.paragraph(
                "In Java, parameters are ALWAYS passed by value. For primitives — the value "
                + "is copied. For objects — the REFERENCE is copied (the object itself is NOT "
                + "duplicated)."));
        en.add(LessonBlock.code(
                "class Box { int value; }\n"
                + "\n"
                + "void changePrimitive(int x) {\n"
                + "    x = 999;  // changes only local copy\n"
                + "}\n"
                + "\n"
                + "void changeObject(Box b) {\n"
                + "    b.value = 999;  // changes the OBJECT the reference points to\n"
                + "    b = null;       // nulls only the local copy of the reference!\n"
                + "}\n"
                + "\n"
                + "int num = 10;\n"
                + "changePrimitive(num);\n"
                + "System.out.println(num);  // 10 (unchanged!)\n"
                + "\n"
                + "Box box = new Box();\n"
                + "box.value = 42;\n"
                + "changeObject(box);\n"
                + "System.out.println(box.value);  // 999 (changed!)"));
        en.add(LessonBlock.note(
                "Tip: to \"return\" multiple values from a method, create a wrapper class "
                + "or use an array. For JDK 8 this is the clearest and most compatible option."));
        return new Lesson("3.2", "Методи", "Methods", uk, en);
    }

    private static Lesson lessonConstructors() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Конструктори: створення об'єктів"));
        uk.add(LessonBlock.paragraph(
                "Конструктор — це спеціальний метод, який викликається при створенні об'єкта "
                + "(new). Має таке ж ім'я, що й клас, і НЕ повертає значення навіть void."));
        uk.add(LessonBlock.code(
                "class Point {\n"
                + "    int x, y;\n"
                + "\n"
                + "    // Конструктор за замовчуванням\n"
                + "    Point() {\n"
                + "        this(0, 0);   // виклик іншого конструктора через this\n"
                + "    }\n"
                + "\n"
                + "    // Конструктор з параметрами\n"
                + "    Point(int x, int y) {\n"
                + "        this.x = x;   // this розрізняє поле й параметр\n"
                + "        this.y = y;\n"
                + "    }\n"
                + "\n"
                + "    // Копіювальний конструктор\n"
                + "    Point(Point other) {\n"
                + "        this(other.x, other.y);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"(\" + x + \", \" + y + \")\";\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Point a = new Point();      // (0, 0)\n"
                + "Point b = new Point(3, 4);  // (3, 4)\n"
                + "Point c = new Point(b);     // (3, 4) — копія"));
        uk.add(LessonBlock.note(
                "Якщо не оголосити жодного конструктора — компілятор згенерує порожній "
                + "конструктор за замовчуванням. Якщо є хоч один — автоматичний зникає, "
                + "тому треба оголосити порожній явно, якщо він потрібен."));
        uk.add(LessonBlock.heading("Приватні поля + геттери/сеттери (інкапсуляція)"));
        uk.add(LessonBlock.paragraph(
                "Не залишайте поля публічними! Приховуйте їх через private та надавайте "
                + "доступ через геттери та сеттери — це дозволить контролювати дані."));
        uk.add(LessonBlock.code(
                "class BankAccount {\n"
                + "    private double balance;   // приватне поле\n"
                + "    private String owner;\n"
                + "\n"
                + "    BankAccount(String owner, double initial) {\n"
                + "        this.owner = owner;\n"
                + "        this.balance = Math.max(0, initial);  // не дозволимо від'ємний баланс\n"
                + "    }\n"
                + "\n"
                + "    // Геттер — читання\n"
                + "    public double getBalance() { return balance; }\n"
                + "    public String getOwner() { return owner; }\n"
                + "\n"
                + "    // Бізнес-метод з валідацією\n"
                + "    public boolean withdraw(double amount) {\n"
                + "        if (amount <= 0) {\n"
                + "            System.out.println(\"Сума має бути позитивною!\");\n"
                + "            return false;\n"
                + "        }\n"
                + "        if (amount > balance) {\n"
                + "            System.out.println(\"Недостатньо коштів!\");\n"
                + "            return false;\n"
                + "        }\n"
                + "        balance -= amount;\n"
                + "        return true;\n"
                + "    }\n"
                + "\n"
                + "    public void deposit(double amount) {\n"
                + "        if (amount > 0) balance += amount;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "BankAccount acc = new BankAccount(\"Іван\", 1000);\n"
                + "acc.withdraw(300);      // OK, balance = 700\n"
                + "acc.withdraw(5000);     // Недостатньо коштів!\n"
                + "acc.deposit(-100);      // ігнорується\n"
                + "System.out.println(acc.getBalance());  // 700"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Constructors: creating objects"));
        en.add(LessonBlock.paragraph(
                "A constructor is a special method called when creating an object (new). "
                + "It has the same name as the class and returns NOTHING (not even void)."));
        en.add(LessonBlock.code(
                "class Point {\n"
                + "    int x, y;\n"
                + "\n"
                + "    // Default constructor\n"
                + "    Point() {\n"
                + "        this(0, 0);   // call another constructor via this\n"
                + "    }\n"
                + "\n"
                + "    // Parameterized constructor\n"
                + "    Point(int x, int y) {\n"
                + "        this.x = x;   // this distinguishes field from parameter\n"
                + "        this.y = y;\n"
                + "    }\n"
                + "\n"
                + "    // Copy constructor\n"
                + "    Point(Point other) {\n"
                + "        this(other.x, other.y);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"(\" + x + \", \" + y + \")\";\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Point a = new Point();      // (0, 0)\n"
                + "Point b = new Point(3, 4);  // (3, 4)\n"
                + "Point c = new Point(b);     // (3, 4) — copy"));
        en.add(LessonBlock.note(
                "If you declare no constructor, the compiler generates an empty default one. "
                + "If you add any constructor, the default one disappears — so declare an "
                + "empty one explicitly if needed."));
        en.add(LessonBlock.heading("Private fields + getters/setters (encapsulation)"));
        en.add(LessonBlock.paragraph(
                "Don't leave fields public! Hide them with private and provide access through "
                + "getters and setters — this lets you control the data."));
        en.add(LessonBlock.code(
                "class BankAccount {\n"
                + "    private double balance;   // private field\n"
                + "    private String owner;\n"
                + "\n"
                + "    BankAccount(String owner, double initial) {\n"
                + "        this.owner = owner;\n"
                + "        this.balance = Math.max(0, initial);  // no negative balance\n"
                + "    }\n"
                + "\n"
                + "    // Getter — read access\n"
                + "    public double getBalance() { return balance; }\n"
                + "    public String getOwner() { return owner; }\n"
                + "\n"
                + "    // Business method with validation\n"
                + "    public boolean withdraw(double amount) {\n"
                + "        if (amount <= 0) {\n"
                + "            System.out.println(\"Amount must be positive!\");\n"
                + "            return false;\n"
                + "        }\n"
                + "        if (amount > balance) {\n"
                + "            System.out.println(\"Insufficient funds!\");\n"
                + "            return false;\n"
                + "        }\n"
                + "        balance -= amount;\n"
                + "        return true;\n"
                + "    }\n"
                + "\n"
                + "    public void deposit(double amount) {\n"
                + "        if (amount > 0) balance += amount;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "BankAccount acc = new BankAccount(\"John\", 1000);\n"
                + "acc.withdraw(300);      // OK, balance = 700\n"
                + "acc.withdraw(5000);     // Insufficient funds!\n"
                + "acc.deposit(-100);      // ignored\n"
                + "System.out.println(acc.getBalance());  // 700"));
        return new Lesson("3.3", "Конструктори та інкапсуляція", "Constructors & encapsulation", uk, en);
    }

    private static Lesson lessonThisAndStatic() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("this та static"));
        uk.add(LessonBlock.code(
                "class Config {\n"
                + "    private String name;\n"
                + "    private static int count = 0;   // спільний для ВСІХ об'єктів\n"
                + "\n"
                + "    Config(String name) {\n"
                + "        this.name = name;   // this = поточний об'єкт\n"
                + "        count++;             // збільшуємо лічильник\n"
                + "    }\n"
                + "\n"
                + "    static int getCount() { return count; }\n"
                + "    String getName() { return name; }\n"
                + "}\n"
                + "\n"
                + "new Config(\"A\");\n"
                + "new Config(\"B\");\n"
                + "new Config(\"C\");\n"
                + "System.out.println(Config.getCount());  // 3"));
        uk.add(LessonBlock.note(
                "static поле — ОДНЕ на весь клас (не на кожен об'єкт). static метод "
                + "не має доступу до this і може викликати лише static поля/методи. "
                + "Виклик: ClassName.staticMethod() або ClassName.staticField."));
        uk.add(LessonBlock.heading("final — незмінність"));
        uk.add(LessonBlock.code(
                "final int MAX = 100;      // константа (не можна змінити)\n"
                + "// MAX = 200;             // помилка компіляції!\n"
                + "\n"
                + "class ImmutablePoint {\n"
                + "    final int x, y;   // задається тільки в конструкторі\n"
                + "    ImmutablePoint(int x, int y) { this.x = x; this.y = y; }\n"
                + "    // сеттерів немає — об'єкт незмінний\n"
                + "}\n"
                + "\n"
                + "final class MathHelper { }   // final клас не можна успадкувати"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("this and static"));
        en.add(LessonBlock.code(
                "class Config {\n"
                + "    private String name;\n"
                + "    private static int count = 0;   // shared across ALL instances\n"
                + "\n"
                + "    Config(String name) {\n"
                + "        this.name = name;   // this = current object\n"
                + "        count++;             // increment the counter\n"
                + "    }\n"
                + "\n"
                + "    static int getCount() { return count; }\n"
                + "    String getName() { return name; }\n"
                + "}\n"
                + "\n"
                + "new Config(\"A\");\n"
                + "new Config(\"B\");\n"
                + "new Config(\"C\");\n"
                + "System.out.println(Config.getCount());  // 3"));
        en.add(LessonBlock.note(
                "A static field is ONE per class (not per object). A static method "
                + "has no access to this and can only access static fields/methods. "
                + "Call: ClassName.staticMethod() or ClassName.staticField."));
        en.add(LessonBlock.heading("final — immutability"));
        en.add(LessonBlock.code(
                "final int MAX = 100;      // constant (cannot be changed)\n"
                + "// MAX = 200;             // compile error!\n"
                + "\n"
                + "class ImmutablePoint {\n"
                + "    final int x, y;   // set only in constructor\n"
                + "    ImmutablePoint(int x, int y) { this.x = x; this.y = y; }\n"
                + "    // no setters — object is immutable\n"
                + "}\n"
                + "\n"
                + "final class MathHelper { }   // final class cannot be extended"));
        return new Lesson("3.4", "this, static та final", "this, static & final", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 4. Успадкування та поліморфізм
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter04(Course c) {
        Chapter ch = new Chapter("Глава 4. Успадкування та поліморфізм",
                "Chapter 4. Inheritance and polymorphism");
        ch.add(lessonInheritance());
        ch.add(lessonPolymorphism());
        ch.add(lessonAbstractClasses());
        ch.add(lessonObjectMethods());
        c.add(ch);
    }

    private static Lesson lessonInheritance() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Успадкування: extends"));
        uk.add(LessonBlock.paragraph(
                "Успадкування дозволяє створювати новий клас на основі існуючого. "
                + "Дочірній клас (нащадок) отримує ВСІ public/protected поля й методи "
                + "батьківського класу та може додавати свої."));
        uk.add(LessonBlock.code(
                "class Animal {\n"
                + "    String name;\n"
                + "\n"
                + "    Animal(String name) { this.name = name; }\n"
                + "\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" їсть\");\n"
                + "    }\n"
                + "\n"
                + "    void info() {\n"
                + "        System.out.println(\"Тварина: \" + name);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Dog extends Animal {\n"
                + "    private String breed;\n"
                + "\n"
                + "    Dog(String name, String breed) {\n"
                + "        super(name);       // виклик конструктора батька\n"
                + "        this.breed = breed;\n"
                + "    }\n"
                + "\n"
                + "    void bark() {\n"
                + "        System.out.println(name + \" гавкає!\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" жує кістку\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Cat extends Animal {\n"
                + "    Cat(String name) { super(name); }\n"
                + "\n"
                + "    void purr() {\n"
                + "        System.out.println(name + \" мурчить\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" лизькає молоко\");\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.heading("Ключове слово super"));
        uk.add(LessonBlock.list(
                "super() — виклик конструктора батьківського класу (має бути ПЕРШим рядком!)",
                "super.метод() — виклик методу батька (корисно при перевизначенні)",
                "super.поле — доступ до прихованого батьківського поля"));
        uk.add(LessonBlock.warning(
                "super() обов'язково має бути першим рядком конструктора! "
                + "Якщо не викликати super() явно, Java автоматично додасть super() без аргументів. "
                + "Але якщо батьківський клас не має конструктора без параметрів — буде помилка."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Inheritance: extends"));
        en.add(LessonBlock.paragraph(
                "Inheritance lets you create a new class based on an existing one. "
                + "The child class (subclass) gets ALL public/protected fields and methods "
                + "of the parent class and can add its own."));
        en.add(LessonBlock.code(
                "class Animal {\n"
                + "    String name;\n"
                + "\n"
                + "    Animal(String name) { this.name = name; }\n"
                + "\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" is eating\");\n"
                + "    }\n"
                + "\n"
                + "    void info() {\n"
                + "        System.out.println(\"Animal: \" + name);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Dog extends Animal {\n"
                + "    private String breed;\n"
                + "\n"
                + "    Dog(String name, String breed) {\n"
                + "        super(name);       // call parent constructor\n"
                + "        this.breed = breed;\n"
                + "    }\n"
                + "\n"
                + "    void bark() {\n"
                + "        System.out.println(name + \" barks!\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" chews a bone\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Cat extends Animal {\n"
                + "    Cat(String name) { super(name); }\n"
                + "\n"
                + "    void purr() {\n"
                + "        System.out.println(name + \" purrs\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" laps milk\");\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.heading("The super keyword"));
        en.add(LessonBlock.list(
                "super() — call parent constructor (must be the FIRST line!)",
                "super.method() — call parent's method (useful when overriding)",
                "super.field — access hidden parent field"));
        en.add(LessonBlock.warning(
                "super() MUST be the first line of a constructor! If you don't call super() "
                + "explicitly, Java adds super() with no arguments automatically. But if the "
                + "parent has no no-arg constructor — you'll get a compile error."));
        return new Lesson("4.1", "Успадкування", "Inheritance", uk, en);
    }

    private static Lesson lessonPolymorphism() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Поліморфізм: один тип — багато форм"));
        uk.add(LessonBlock.paragraph(
                "Поліморфізм (від грецького «багато форм») — об'єкт може "
                + "використовуватися як тип батьківського класу, але викликатиметься "
                + "ЙОГО власний метод (динамічна диспетчеризація)."));
        uk.add(LessonBlock.code(
                "// Використовуємо класи Animal, Dog, Cat з попереднього прикладу\n"
                + "\n"
                + "Animal a1 = new Dog(\"Рекс\", \"лабрадор\");\n"
                + "Animal a2 = new Cat(\"Мурчик\");\n"
                + "Animal a3 = new Animal(\"Тварина\");\n"
                + "\n"
                + "// Кожен викликає СВОЇЙ eat() — завдяки поліморфізму!\n"
                + "a1.eat();  // Рекс жує кістку  (Dog.eat)\n"
                + "a2.eat();  // Мурчик лизькає молоко (Cat.eat)\n"
                + "a3.eat();  // Тварина їсть (Animal.eat)\n"
                + "\n"
                + "// a1.bark();  // ПОМИЛКА! Animal не знає метод bark()\n"
                + "// Треба приведення типу:\n"
                + "Dog dog = (Dog) a1;\n"
                + "dog.bark();  // OK"));
        uk.add(LessonBlock.heading("instanceof — перевірка типу"));
        uk.add(LessonBlock.code(
                "Animal a = new Dog(\"Рекс\", \"лабрадор\");\n"
                + "\n"
                + "if (a instanceof Dog) {\n"
                + "    Dog d = (Dog) a;      // safe downcast\n"
                + "    d.bark();\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Завжди перевіряйте instanceof перед приведенням типу! Інакше отримаєте "
                + "ClassCastException. Поліморфізм працює лише з методами, НЕ з полями — "
                + "поля визначаються типом змінної (статичний зв'язок)."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Polymorphism: one type, many forms"));
        en.add(LessonBlock.paragraph(
                "Polymorphism (from Greek \"many forms\") — an object can be used as its "
                + "parent type, but ITS OWN method will be called (dynamic dispatch)."));
        en.add(LessonBlock.code(
                "// Using Animal, Dog, Cat from the previous example\n"
                + "\n"
                + "Animal a1 = new Dog(\"Rex\", \"labrador\");\n"
                + "Animal a2 = new Cat(\"Whiskers\");\n"
                + "Animal a3 = new Animal(\"Generic\");\n"
                + "\n"
                + "// Each calls ITS OWN eat() — thanks to polymorphism!\n"
                + "a1.eat();  // Rex chews a bone  (Dog.eat)\n"
                + "a2.eat();  // Whiskers laps milk (Cat.eat)\n"
                + "a3.eat();  // Generic is eating  (Animal.eat)\n"
                + "\n"
                + "// a1.bark();  // ERROR! Animal doesn't know bark()\n"
                + "// Need a cast:\n"
                + "Dog dog = (Dog) a1;\n"
                + "dog.bark();  // OK"));
        en.add(LessonBlock.heading("instanceof — type check"));
        en.add(LessonBlock.code(
                "Animal a = new Dog(\"Rex\", \"labrador\");\n"
                + "\n"
                + "if (a instanceof Dog) {\n"
                + "    Dog d = (Dog) a;      // safe downcast\n"
                + "    d.bark();\n"
                + "}"));
        en.add(LessonBlock.note(
                "Always check instanceof before casting! Otherwise you'll get "
                + "ClassCastException. Polymorphism works with methods only, NOT fields — "
                + "fields are resolved by the variable's type (static binding)."));
        return new Lesson("4.2", "Поліморфізм", "Polymorphism", uk, en);
    }

    private static Lesson lessonAbstractClasses() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Абстрактні класи та інтерфейси"));
        uk.add(LessonBlock.paragraph(
                "Абстрактний клас — клас, який НЕ можна створити напряму (new не працює). "
                + "Він служить загальним шаблоном для нащадків. Містить абстрактні методи "
                + "(без тіла), які нащадки зобов'язані реалізувати."));
        uk.add(LessonBlock.code(
                "abstract class Shape {\n"
                + "    String color;\n"
                + "\n"
                + "    Shape(String color) { this.color = color; }\n"
                + "\n"
                + "    // Абстрактний метод — без тіла, нащадки ЗОБОВ'ЯЗАНІ реалізувати\n"
                + "    abstract double area();\n"
                + "    abstract double perimeter();\n"
                + "\n"
                + "    // Звичайний метод — спільний для всіх\n"
                + "    void printInfo() {\n"
                + "        System.out.println(color + \" фігура: площа=\"\n"
                + "            + area() + \", периметр=\" + perimeter());\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Circle extends Shape {\n"
                + "    double radius;\n"
                + "    Circle(String color, double r) { super(color); this.radius = r; }\n"
                + "\n"
                + "    @Override double area() { return Math.PI * radius * radius; }\n"
                + "    @Override double perimeter() { return 2 * Math.PI * radius; }\n"
                + "}\n"
                + "\n"
                + "class Rectangle extends Shape {\n"
                + "    double width, height;\n"
                + "    Rectangle(String color, double w, double h) {\n"
                + "        super(color); this.width = w; this.height = h;\n"
                + "    }\n"
                + "\n"
                + "    @Override double area() { return width * height; }\n"
                + "    @Override double perimeter() { return 2 * (width + height); }\n"
                + "}\n"
                + "\n"
                + "// Shape s = new Shape(\"чорний\");  // ПОМИЛКА! абстрактний\n"
                + "Circle c = new Circle(\"червоний\", 5);\n"
                + "Rectangle r = new Rectangle(\"синій\", 4, 6);\n"
                + "c.printInfo();   // червоний фігура: площа=78.54, периметр=31.42\n"
                + "r.printInfo();   // синій фігура: площа=24.0, периметр=20.0"));
        uk.add(LessonBlock.note(
                "Абстрактний клас vs інтерфейс: клас може успадкувати лише ОДИН абстрактний "
                + "клас, але реалізувати БАГАТО інтерфейсів. Абстрактний клас може мати "
                + "поля, конструктори; інтерфейс — ні (до Java 8)."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Abstract classes"));
        en.add(LessonBlock.paragraph(
                "An abstract class CANNOT be instantiated (new won't work). It serves as "
                + "a general template for subclasses. It contains abstract methods (no body) "
                + "that subclasses MUST implement."));
        en.add(LessonBlock.code(
                "abstract class Shape {\n"
                + "    String color;\n"
                + "\n"
                + "    Shape(String color) { this.color = color; }\n"
                + "\n"
                + "    // Abstract method — no body, subclasses MUST implement\n"
                + "    abstract double area();\n"
                + "    abstract double perimeter();\n"
                + "\n"
                + "    // Regular method — shared by all\n"
                + "    void printInfo() {\n"
                + "        System.out.println(color + \" shape: area=\"\n"
                + "            + area() + \", perimeter=\" + perimeter());\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Circle extends Shape {\n"
                + "    double radius;\n"
                + "    Circle(String color, double r) { super(color); this.radius = r; }\n"
                + "\n"
                + "    @Override double area() { return Math.PI * radius * radius; }\n"
                + "    @Override double perimeter() { return 2 * Math.PI * radius; }\n"
                + "}\n"
                + "\n"
                + "class Rectangle extends Shape {\n"
                + "    double width, height;\n"
                + "    Rectangle(String color, double w, double h) {\n"
                + "        super(color); this.width = w; this.height = h;\n"
                + "    }\n"
                + "\n"
                + "    @Override double area() { return width * height; }\n"
                + "    @Override double perimeter() { return 2 * (width + height); }\n"
                + "}\n"
                + "\n"
                + "// Shape s = new Shape(\"black\");  // ERROR! abstract\n"
                + "Circle c = new Circle(\"red\", 5);\n"
                + "Rectangle r = new Rectangle(\"blue\", 4, 6);\n"
                + "c.printInfo();   // red shape: area=78.54, perimeter=31.42\n"
                + "r.printInfo();   // blue shape: area=24.0, perimeter=20.0"));
        en.add(LessonBlock.note(
                "Abstract class vs interface: a class can extend only ONE abstract class "
                + "but implement MANY interfaces. An abstract class can have fields and "
                + "constructors; an interface cannot (pre-Java 8)."));
        return new Lesson("4.3", "Абстрактні класи", "Abstract classes", uk, en);
    }

    private static Lesson lessonObjectMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Методи Object: toString, equals, hashCode"));
        uk.add(LessonBlock.paragraph(
                "Кожен клас в Java наслідує java.lang.Object. Тому кожен об'єкт має "
                + "методи toString(), equals(), hashCode(). За замовчуванням вони "
                + "некорисні — їх треба перевизначати."));
        uk.add(LessonBlock.code(
                "class Student {\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    Student(String name, int age) { this.name = name; this.age = age; }\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"Student{name='\" + name + \"', age=\" + age + \"}\";\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public boolean equals(Object o) {\n"
                + "        if (this == o) return true;              // той самий об'єкт\n"
                + "        if (!(o instanceof Student)) return false; // інший тип\n"
                + "        Student s = (Student) o;\n"
                + "        return age == s.age\n"
                + "            && name.equals(s.name);              // порівняння полів\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public int hashCode() {\n"
                + "        return 31 * name.hashCode() + age;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Student a = new Student(\"Іван\", 20);\n"
                + "Student b = new Student(\"Іван\", 20);\n"
                + "\n"
                + "System.out.println(a.toString());  // Student{name='Іван', age=20}\n"
                + "System.out.println(a.equals(b));   // true (однакові дані)\n"
                + "System.out.println(a == b);         // false (різні об'єкти в пам'яті!)"));
        uk.add(LessonBlock.warning(
                "Правило: ЯКЩО ви перевизначили equals(), ОБОВ'ЯЗКОВО перевизначте "
                + "і hashCode()! Інакше колекції (HashMap, HashSet) працюватимуть "
                + "неправильно — два об'єкти з однаковим equals() матимуть різний hashCode()."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Object methods: toString, equals, hashCode"));
        en.add(LessonBlock.paragraph(
                "Every class in Java extends java.lang.Object. So every object has "
                + "toString(), equals(), hashCode(). By default they are not useful — "
                + "you need to override them."));
        en.add(LessonBlock.code(
                "class Student {\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    Student(String name, int age) { this.name = name; this.age = age; }\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"Student{name='\" + name + \"', age=\" + age + \"}\";\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public boolean equals(Object o) {\n"
                + "        if (this == o) return true;              // same object\n"
                + "        if (!(o instanceof Student)) return false; // different type\n"
                + "        Student s = (Student) o;\n"
                + "        return age == s.age\n"
                + "            && name.equals(s.name);              // compare fields\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public int hashCode() {\n"
                + "        return 31 * name.hashCode() + age;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Student a = new Student(\"John\", 20);\n"
                + "Student b = new Student(\"John\", 20);\n"
                + "\n"
                + "System.out.println(a.toString());  // Student{name='John', age=20}\n"
                + "System.out.println(a.equals(b));   // true (same data)\n"
                + "System.out.println(a == b);         // false (different objects in memory!)"));
        en.add(LessonBlock.warning(
                "Rule: IF you override equals(), you MUST override hashCode() too! "
                + "Otherwise collections (HashMap, HashSet) will behave incorrectly — "
                + "two objects with equal equals() will have different hashCode()."));
        return new Lesson("4.4", "Методи Object", "Object methods", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 5. Інтерфейси
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter05(Course c) {
        Chapter ch = new Chapter("Глава 5. Інтерфейси", "Chapter 5. Interfaces");
        ch.add(lessonInterfaces());
        ch.add(lessonDefaultStaticMethods());
        ch.add(lessonFunctionalInterfaces());
        c.add(ch);
    }

    private static Lesson lessonInterfaces() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Інтерфейси: контракт для класів"));
        uk.add(LessonBlock.paragraph(
                "Інтерфейс — це контракт (обіцянка): «хто реалізує мене — зобов'язаний "
                + "мати ці методи». Клас реалізує інтерфейс через implements і може "
                + "реалізувати кілька інтерфейсів одночасно."));
        uk.add(LessonBlock.code(
                "interface Drawable {\n"
                + "    void draw();  // абстрактний метод (як в abstract класі)\n"
                + "}\n"
                + "\n"
                + "interface Resizable {\n"
                + "    void resize(double factor);\n"
                + "    double getScale();\n"
                + "}\n"
                + "\n"
                + "// Клас може реалізувати КІЛЬКА інтерфейсів\n"
                + "class Widget implements Drawable, Resizable {\n"
                + "    private double scale = 1.0;\n"
                + "\n"
                + "    @Override\n"
                + "    public void draw() {\n"
                + "        System.out.println(\"Малюю віджет (масштаб \" + scale + \")\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public void resize(double factor) { scale *= factor; }\n"
                + "\n"
                + "    @Override\n"
                + "    public double getScale() { return scale; }\n"
                + "}\n"
                + "\n"
                + "// Поліморфізм через інтерфейси\n"
                + "Drawable d = new Widget();  // можна використовувати як Drawable\n"
                + "d.draw();\n"
                + "// d.resize(2.0);  // ПОМИЛКА! тип Drawable не знає resize()"));
        uk.add(LessonBlock.heading("Що може містити інтерфейс"));
        uk.add(LessonBlock.table(
                "Елемент\tІнтерфейс\tАбстрактний клас",
                Arrays.asList(
                    "Абстрактні методи\tТак\tТак",
                    "Поля\tТільки public static final\tБудь-які",
                    "Конструктори\tНі\tТак",
                    "Наслідування\textends (багато)\nextends (один)",
                    "Реалізація\timplements (багато)\nextends (один)")));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Interfaces: a contract for classes"));
        en.add(LessonBlock.paragraph(
                "An interface is a contract (promise): \"whoever implements me MUST have "
                + "these methods\". A class implements an interface via implements and can "
                + "implement multiple interfaces."));
        en.add(LessonBlock.code(
                "interface Drawable {\n"
                + "    void draw();  // abstract method (like in abstract class)\n"
                + "}\n"
                + "\n"
                + "interface Resizable {\n"
                + "    void resize(double factor);\n"
                + "    double getScale();\n"
                + "}\n"
                + "\n"
                + "// A class can implement MULTIPLE interfaces\n"
                + "class Widget implements Drawable, Resizable {\n"
                + "    private double scale = 1.0;\n"
                + "\n"
                + "    @Override\n"
                + "    public void draw() {\n"
                + "        System.out.println(\"Drawing widget (scale \" + scale + \")\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public void resize(double factor) { scale *= factor; }\n"
                + "\n"
                + "    @Override\n"
                + "    public double getScale() { return scale; }\n"
                + "}\n"
                + "\n"
                + "// Polymorphism via interfaces\n"
                + "Drawable d = new Widget();  // can use as Drawable\n"
                + "d.draw();\n"
                + "// d.resize(2.0);  // ERROR! Drawable type doesn't know resize()"));
        en.add(LessonBlock.heading("What an interface can contain"));
        en.add(LessonBlock.table(
                "Element\tInterface\tAbstract class",
                Arrays.asList(
                    "Abstract methods\tYes\tYes",
                    "Fields\tOnly public static final\tAny",
                    "Constructors\tNo\tYes",
                    "Inheritance\textends (many)\nextends (one)",
                    "Implementation\timplements (many)\nextends (one)")));
        return new Lesson("5.1", "Інтерфейси", "Interfaces", uk, en);
    }

    private static Lesson lessonDefaultStaticMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Default та static методи (Java 8+)"));
        uk.add(LessonBlock.paragraph(
                "З Java 8 інтерфейси можуть мати default методи (з тілом) та static методи. "
                + "Це дозволило додавати нові методи без злому існуючого коду."));
        uk.add(LessonBlock.code(
                "interface Logger {\n"
                + "    // Звичайний абстрактний метод\n"
                + "    void log(String message);\n"
                + "\n"
                + "    // Default метод — вже має реалізацію\n"
                + "    default void warn(String message) {\n"
                + "        log(\"[WARN] \" + message);\n"
                + "    }\n"
                + "\n"
                + "    // Static метод — викликається через назву інтерфейсу\n"
                + "    static Logger consoleLogger() {\n"
                + "        return msg -> System.out.println(msg);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class FileLogger implements Logger {\n"
                + "    @Override\n"
                + "    public void log(String message) {\n"
                + "        System.out.println(\"FILE: \" + message);\n"
                + "    }\n"
                + "    // warn() не перевизначений — використовується default-версія\n"
                + "}\n"
                + "\n"
                + "FileLogger fl = new FileLogger();\n"
                + "fl.log(\"Помилка\");          // FILE: Помилка\n"
                + "fl.warn(\"Увага!\");           // FILE: [WARN] Увага!\n"
                + "\n"
                + "// Виклик static методу\n"
                + "Logger cl = Logger.consoleLogger();\n"
                + "cl.log(\"Hi\");"));
        uk.add(LessonBlock.heading("Проблема «діаманта»"));
        uk.add(LessonBlock.paragraph(
                "Якщо клас реалізує два інтерфейси з однаковим default-методом — "
                + "компілятор не знає, який обрати. Треба перевизначити метод явно:"));
        uk.add(LessonBlock.code(
                "interface A { default void hello() { System.out.println(\"A\"); } }\n"
                + "interface B { default void hello() { System.out.println(\"B\"); } }\n"
                + "\n"
                + "class C implements A, B {\n"
                + "    @Override\n"
                + "    public void hello() {\n"
                + "        A.super.hello();  // явний вибір: викликати A.hello()\n"
                + "    }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Default and static methods (Java 8+)"));
        en.add(LessonBlock.paragraph(
                "Since Java 8, interfaces can have default methods (with a body) and static "
                + "methods. This allows adding new methods without breaking existing code."));
        en.add(LessonBlock.code(
                "interface Logger {\n"
                + "    // Regular abstract method\n"
                + "    void log(String message);\n"
                + "\n"
                + "    // Default method — already has an implementation\n"
                + "    default void warn(String message) {\n"
                + "        log(\"[WARN] \" + message);\n"
                + "    }\n"
                + "\n"
                + "    // Static method — called via interface name\n"
                + "    static Logger consoleLogger() {\n"
                + "        return msg -> System.out.println(msg);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class FileLogger implements Logger {\n"
                + "    @Override\n"
                + "    public void log(String message) {\n"
                + "        System.out.println(\"FILE: \" + message);\n"
                + "    }\n"
                + "    // warn() not overridden — uses default version\n"
                + "}\n"
                + "\n"
                + "FileLogger fl = new FileLogger();\n"
                + "fl.log(\"Error\");          // FILE: Error\n"
                + "fl.warn(\"Attention!\");     // FILE: [WARN] Attention!"));
        en.add(LessonBlock.heading("The diamond problem"));
        en.add(LessonBlock.paragraph(
                "If a class implements two interfaces with the same default method, "
                + "the compiler doesn't know which to choose. You must override explicitly:"));
        en.add(LessonBlock.code(
                "interface A { default void hello() { System.out.println(\"A\"); } }\n"
                + "interface B { default void hello() { System.out.println(\"B\"); } }\n"
                + "\n"
                + "class C implements A, B {\n"
                + "    @Override\n"
                + "    public void hello() {\n"
                + "        A.super.hello();  // explicit choice: call A.hello()\n"
                + "    }\n"
                + "}"));
        return new Lesson("5.2", "Default та static методи", "Default & static methods", uk, en);
    }

    private static Lesson lessonFunctionalInterfaces() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Функціональні інтерфейси та lambda"));
        uk.add(LessonBlock.paragraph(
                "Функціональний інтерфейс — інтерфейс з ОДНИМ абстрактним методом. "
                + "Позначається анотацією @FunctionalInterface. До нього можна застосувати "
                + "лямбда-вираз замість анонімного класу."));
        uk.add(LessonBlock.code(
                "@FunctionalInterface\n"
                + "interface Transformer {\n"
                + "    String transform(String input);\n"
                + "}\n"
                + "\n"
                + "// До Java 8 — анонімний клас (багато зайвого коду)\n"
                + "Transformer upper = new Transformer() {\n"
                + "    @Override\n"
                + "    public String transform(String input) {\n"
                + "        return input.toUpperCase();\n"
                + "    }\n"
                + "};\n"
                + "\n"
                + "// З Java 8 — lambda (один рядок!)\n"
                + "Transformer lower = input -> input.toLowerCase();\n"
                + "Transformer reverser = input -> new StringBuilder(input).reverse().toString();\n"
                + "\n"
                + "System.out.println(upper.transform(\"hello\"));   // HELLO\n"
                + "System.out.println(lower.transform(\"HELLO\"));   // hello\n"
                + "System.out.println(reverser.transform(\"abc\"));  // cba"));
        uk.add(LessonBlock.note(
                "Java вже має готові функціональні інтерфейси в java.util.function: "
                + "Predicate<T> (boolean test), Function<T,R> (R apply), "
                + "Consumer<T> (void accept), Supplier<T> (T get)."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Functional interfaces and lambda"));
        en.add(LessonBlock.paragraph(
                "A functional interface has ONE abstract method. It's marked with "
                + "@FunctionalInterface. You can apply a lambda expression instead of "
                + "an anonymous class."));
        en.add(LessonBlock.code(
                "@FunctionalInterface\n"
                + "interface Transformer {\n"
                + "    String transform(String input);\n"
                + "}\n"
                + "\n"
                + "// Before Java 8 — anonymous class (lots of boilerplate)\n"
                + "Transformer upper = new Transformer() {\n"
                + "    @Override\n"
                + "    public String transform(String input) {\n"
                + "        return input.toUpperCase();\n"
                + "    }\n"
                + "};\n"
                + "\n"
                + "// Since Java 8 — lambda (one line!)\n"
                + "Transformer lower = input -> input.toLowerCase();\n"
                + "Transformer reverser = input -> new StringBuilder(input).reverse().toString();\n"
                + "\n"
                + "System.out.println(upper.transform(\"hello\"));   // HELLO\n"
                + "System.out.println(lower.transform(\"HELLO\"));   // hello\n"
                + "System.out.println(reverser.transform(\"abc\"));  // cba"));
        en.add(LessonBlock.note(
                "Java already has built-in functional interfaces in java.util.function: "
                + "Predicate<T> (boolean test), Function<T,R> (R apply), "
                + "Consumer<T> (void accept), Supplier<T> (T get)."));
        return new Lesson("5.3", "Функціональні інтерфейси", "Functional interfaces", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 6. Пакети та модифікатори доступу
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter06(Course c) {
        Chapter ch = new Chapter("Глава 6. Пакети", "Chapter 6. Packages");
        ch.add(lessonAccessModifiers());
        ch.add(lessonPackagesAndImports());
        c.add(ch);
    }

    private static Lesson lessonAccessModifiers() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Модифікатори доступу"));
        uk.add(LessonBlock.paragraph(
                "Модифікатори доступу визначають, ХТО може бачити клас, поле чи метод. "
                + "Це як замки на дверях: відкриті для всіх, для своїх, для близьких, "
                + "або тільки для себе."));
        uk.add(LessonBlock.table(
                "Модифікатор\tКлас\tПакет\tНащадки\tСвіт",
                Arrays.asList(
                    "public\tТак\tТак\tТак\tТак",
                    "protected\tТак\tТак\tТак\tНі",
                    "default (без ключового слова)\tТак\tТак\tНі\tНі",
                    "private\tТак\tНі\tНі\tНі")));
        uk.add(LessonBlock.code(
                "package com.example;\n"
                + "\n"
                + "public class User {\n"
                + "    public String name;           // видно всюди\n"
                + "    protected int age;            // пакет + нащадки\n"
                + "    String email;                 // default — тільки пакет\n"
                + "    private String password;      // тільки всередині User\n"
                + "\n"
                + "    public void printPublic() { System.out.println(name); }\n"
                + "    protected void printProtected() { System.out.println(age); }\n"
                + "    void printDefault() { System.out.println(email); }\n"
                + "    private void printPrivate() { System.out.println(password); }\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Порада для початківців: за замовчуванням робіть все private. "
                + "Відкривайте доступ (public/protected) тільки коли це дійсно потрібно. "
                + "Це називається «мінімальні привілеї» (principle of least privilege)."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Access modifiers"));
        en.add(LessonBlock.paragraph(
                "Access modifiers determine WHO can see a class, field or method. "
                + "Think of them as locks on doors: open to all, to family, to neighbors, "
                + "or only to yourself."));
        en.add(LessonBlock.table(
                "Modifier\tClass\tPackage\tSubclasses\tWorld",
                Arrays.asList(
                    "public\tYes\tYes\tYes\tYes",
                    "protected\tYes\tYes\tYes\tNo",
                    "default (no keyword)\tYes\tYes\tNo\tNo",
                    "private\tYes\tNo\tNo\tNo")));
        en.add(LessonBlock.code(
                "package com.example;\n"
                + "\n"
                + "public class User {\n"
                + "    public String name;           // visible everywhere\n"
                + "    protected int age;            // package + subclasses\n"
                + "    String email;                 // default — package only\n"
                + "    private String password;      // only inside User\n"
                + "\n"
                + "    public void printPublic() { System.out.println(name); }\n"
                + "    protected void printProtected() { System.out.println(age); }\n"
                + "    void printDefault() { System.out.println(email); }\n"
                + "    private void printPrivate() { System.out.println(password); }\n"
                + "}"));
        en.add(LessonBlock.note(
                "Tip for beginners: default to private. Only open access (public/protected) "
                + "when truly needed. This is the principle of least privilege."));
        return new Lesson("6.1", "Модифікатори доступу", "Access modifiers", uk, en);
    }

    private static Lesson lessonPackagesAndImports() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Пакети та імпорти"));
        uk.add(LessonBlock.paragraph(
                "Пакет — це папка для класів. Організує код та уникає конфліктів імен. "
                + "Правило: назва пакету відповідає структурі папок (com.example → com/example/)."));
        uk.add(LessonBlock.code(
                "// Оголошення пакету — перший рядок файлу\n"
                + "package com.example.util;\n"
                + "\n"
                + "// Імпорт — щоб не писати повний шлях\n"
                + "import java.util.ArrayList;       // один клас\n"
                + "import java.util.*;                // всі класи з пакету\n"
                + "import static java.lang.Math.PI;   // static поле\n"
                + "import static java.lang.Math.*;    // всі static елементи\n"
                + "\n"
                + "class App {\n"
                + "    void run() {\n"
                + "        ArrayList<String> list = new ArrayList<>();\n"
                + "        System.out.println(PI);      // завдяки import static\n"
                + "        System.out.println(sqrt(16)); // завдяки import static Math.*\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.list(
                "java.lang — імпортується автоматично (String, System, Math)",
                "java.util — колекції, дати, random",
                "java.io — ввід/вивід, файли",
                "java.sql — робота з БД",
                "java.time — Date/Time API (Java 8+)"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Packages and imports"));
        en.add(LessonBlock.paragraph(
                "A package is a folder for classes. It organizes code and avoids name "
                + "conflicts. Rule: package name matches folder structure "
                + "(com.example → com/example/)."));
        en.add(LessonBlock.code(
                "// Package declaration — first line of the file\n"
                + "package com.example.util;\n"
                + "\n"
                + "// Import — so you don't have to write the full path\n"
                + "import java.util.ArrayList;       // one class\n"
                + "import java.util.*;                // all classes from the package\n"
                + "import static java.lang.Math.PI;   // static field\n"
                + "import static java.lang.Math.*;    // all static elements\n"
                + "\n"
                + "class App {\n"
                + "    void run() {\n"
                + "        ArrayList<String> list = new ArrayList<>();\n"
                + "        System.out.println(PI);      // thanks to import static\n"
                + "        System.out.println(sqrt(16)); // thanks to import static Math.*\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.list(
                "java.lang — auto-imported (String, System, Math)",
                "java.util — collections, dates, random",
                "java.io — input/output, files",
                "java.sql — database access",
                "java.time — Date/Time API (Java 8+)"));
        return new Lesson("6.2", "Пакети та імпорти", "Packages & imports", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 7. Обробка винятків
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter07(Course c) {
        Chapter ch = new Chapter("Глава 7. Обробка винятків",
                "Chapter 7. Exception handling");
        ch.add(lessonExceptionHierarchy());
        ch.add(lessonTryCatchFinally());
        ch.add(lessonTryWithResources());
        ch.add(lessonCustomExceptions());
        c.add(ch);
    }

    private static Lesson lessonExceptionHierarchy() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Ієрархія винятків"));
        uk.add(LessonBlock.paragraph(
                "Винятки в Java — це об'єкти, що сигналізують про помилку. Є дві "
                + "основні гілки: Checked (перевіряємі) та Unchecked (неперевіряємі)."));
        uk.add(LessonBlock.code(
                "Throwable\n"
                + "├── Error (системні помилки — OutOfMemoryError, StackOverflow)\n"
                + "│   └── НЕ ловимо, бо це проблема JVM, а не нашого коду\n"
                + "└── Exception\n"
                + "    ├── RuntimeException (UNCHECKED — не вимагає try/catch)\n"
                + "    │   ├── NullPointerException\n"
                + "    │   ├── ArrayIndexOutOfBoundsException\n"
                + "    │   ├── ArithmeticException (ділення на 0)\n"
                + "    │   ├── ClassCastException\n"
                + "    │   ├── IllegalArgumentException\n"
                + "    │   └── NumberFormatException\n"
                + "    └── Checked (вимагає try/catch або throws)\n"
                + "        ├── IOException\n"
                + "        ├── SQLException\n"
                + "        ├── FileNotFoundException\n"
                + "        └── ClassNotFoundException"));
        uk.add(LessonBlock.warning(
                "Checked винятки — компілятор змусить вас їх обробити. "
                + "Unchecked винятки — можна ігнорувати (але не варто!). "
                + "Error — ніколи не ловіть, це проблема платформи."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Exception hierarchy"));
        en.add(LessonBlock.paragraph(
                "Exceptions in Java are objects signaling an error. Two main branches: "
                + "Checked and Unchecked."));
        en.add(LessonBlock.code(
                "Throwable\n"
                + "├── Error (system errors — OutOfMemoryError, StackOverflow)\n"
                + "│   └── Don't catch — this is a JVM problem, not yours\n"
                + "└── Exception\n"
                + "    ├── RuntimeException (UNCHECKED — no try/catch required)\n"
                + "    │   ├── NullPointerException\n"
                + "    │   ├── ArrayIndexOutOfBoundsException\n"
                + "    │   ├── ArithmeticException (division by zero)\n"
                + "    │   ├── ClassCastException\n"
                + "    │   ├── IllegalArgumentException\n"
                + "    │   └── NumberFormatException\n"
                + "    └── Checked (requires try/catch or throws)\n"
                + "        ├── IOException\n"
                + "        ├── SQLException\n"
                + "        ├── FileNotFoundException\n"
                + "        └── ClassNotFoundException"));
        en.add(LessonBlock.warning(
                "Checked exceptions — the compiler forces you to handle them. "
                + "Unchecked exceptions — you can ignore them (but shouldn't!). "
                + "Error — never catch these, it's a platform problem."));
        return new Lesson("7.1", "Ієрархія винятків", "Exception hierarchy", uk, en);
    }

    private static Lesson lessonTryCatchFinally() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("try / catch / finally"));
        uk.add(LessonBlock.code(
                "try {\n"
                + "    int result = 10 / 0;        // ArithmeticException!\n"
                + "    String s = null;\n"
                + "    s.length();                  // NullPointerException!\n"
                + "} catch (ArithmeticException e) {\n"
                + "    System.out.println(\"Ділення на нуль: \" + e.getMessage());\n"
                + "} catch (NullPointerException e) {\n"
                + "    System.out.println(\"Посилання null: \" + e.getMessage());\n"
                + "} catch (Exception e) {\n"
                + "    System.out.println(\"Будь-яка інша помилка: \" + e.getMessage());\n"
                + "} finally {\n"
                + "    System.out.println(\"ЗАВЖДИ виконується!\");\n"
                + "}"));
        uk.add(LessonBlock.heading("Multi-catch (Java 7+)"));
        uk.add(LessonBlock.code(
                "// Якщо обробка однакова для кількох винятків\n"
                + "try {\n"
                + "    String s = \"abc\";\n"
                + "    int n = Integer.parseInt(s);  // NumberFormatException\n"
                + "} catch (NumberFormatException | IllegalArgumentException e) {\n"
                + "    System.out.println(\"Погане значення: \" + e.getMessage());\n"
                + "}"));
        uk.add(LessonBlock.heading("Рекомендації з розробки"));
        uk.add(LessonBlock.list(
                "Ловіть найконкретніший виняток (наприклад, ArithmeticException замість базового Exception)",
                "Не ігноруйте винятки: порожній блок catch (Exception e) {} є антипатерном",
                "Завжди записуйте винятки в лог: catch (Exception e) { logger.error(\"...\", e); }",
                "Не використовуйте механізм винятків для керування логічним потоком програми (це знижує продуктивність)"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("try / catch / finally"));
        en.add(LessonBlock.code(
                "try {\n"
                + "    int result = 10 / 0;        // ArithmeticException!\n"
                + "    String s = null;\n"
                + "    s.length();                  // NullPointerException!\n"
                + "} catch (ArithmeticException e) {\n"
                + "    System.out.println(\"Division by zero: \" + e.getMessage());\n"
                + "} catch (NullPointerException e) {\n"
                + "    System.out.println(\"Null reference: \" + e.getMessage());\n"
                + "} catch (Exception e) {\n"
                + "    System.out.println(\"Other error: \" + e.getMessage());\n"
                + "} finally {\n"
                + "    System.out.println(\"ALWAYS runs!\");\n"
                + "}"));
        en.add(LessonBlock.heading("Multi-catch (Java 7+)"));
        en.add(LessonBlock.code(
                "// If handling is the same for multiple exceptions\n"
                + "try {\n"
                + "    String s = \"abc\";\n"
                + "    int n = Integer.parseInt(s);  // NumberFormatException\n"
                + "} catch (NumberFormatException | IllegalArgumentException e) {\n"
                + "    System.out.println(\"Bad value: \" + e.getMessage());\n"
                + "}"));
        en.add(LessonBlock.heading("Practical recommendations"));
        en.add(LessonBlock.list(
                "Catch the most specific exception (e.g., ArithmeticException instead of generic Exception)",
                "Do not ignore exceptions: an empty catch (Exception e) {} block is an anti-pattern",
                "Always write exceptions to the log: catch (Exception e) { logger.error(\"...\", e); }",
                "Do not use exception handling for control flow (as it degrades performance)"));
        return new Lesson("7.2", "try / catch / finally", "try / catch / finally", uk, en);
    }

    private static Lesson lessonTryWithResources() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("try-with-resources (AutoCloseable)"));
        uk.add(LessonBlock.paragraph(
                "try-with-resources автоматично закриває ресурси (файли, з'єднання, потоки) "
                + "навіть якщо сталася помилка. Клас має реалізовувати AutoCloseable."));
        uk.add(LessonBlock.code(
                "import java.io.*;\n"
                + "\n"
                + "// З Java 7+ — try-with-resources\n"
                + "try (BufferedReader br = new BufferedReader(\n"
                + "        new FileReader(\"file.txt\"))) {\n"
                + "    String line;\n"
                + "    while ((line = br.readLine()) != null) {\n"
                + "        System.out.println(line);\n"
                + "    }\n"
                + "} catch (IOException e) {\n"
                + "    System.out.println(\"Помилка читання: \" + e.getMessage());\n"
                + "}\n"
                + "// br закрито автоматично навіть при винятку!"));
        uk.add(LessonBlock.heading("Кілька ресурсів одночасно"));
        uk.add(LessonBlock.code(
                "try (FileInputStream in = new FileInputStream(\"in.txt\");\n"
                + "     FileOutputStream out = new FileOutputStream(\"out.txt\")) {\n"
                + "    byte[] buffer = new byte[4096];\n"
                + "    int count;\n"
                + "    while ((count = in.read(buffer)) != -1) {\n"
                + "        out.write(buffer, 0, count);\n"
                + "    }\n"
                + "}  // обидва закриються у зворотному порядку"));
        uk.add(LessonBlock.note(
                "У JDK 8 вказуйте тип ресурсу явно: FileInputStream, FileOutputStream, "
                + "BufferedReader тощо. Ресурси закриваються у зворотному порядку створення."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("try-with-resources (AutoCloseable)"));
        en.add(LessonBlock.paragraph(
                "try-with-resources automatically closes resources (files, connections, streams) "
                + "even if an exception occurs. The class must implement AutoCloseable."));
        en.add(LessonBlock.code(
                "import java.io.*;\n"
                + "\n"
                + "// Since Java 7 — try-with-resources\n"
                + "try (BufferedReader br = new BufferedReader(\n"
                + "        new FileReader(\"file.txt\"))) {\n"
                + "    String line;\n"
                + "    while ((line = br.readLine()) != null) {\n"
                + "        System.out.println(line);\n"
                + "    }\n"
                + "} catch (IOException e) {\n"
                + "    System.out.println(\"Read error: \" + e.getMessage());\n"
                + "}\n"
                + "// br closed automatically even on exception!"));
        en.add(LessonBlock.heading("Multiple resources"));
        en.add(LessonBlock.code(
                "try (FileInputStream in = new FileInputStream(\"in.txt\");\n"
                + "     FileOutputStream out = new FileOutputStream(\"out.txt\")) {\n"
                + "    byte[] buffer = new byte[4096];\n"
                + "    int count;\n"
                + "    while ((count = in.read(buffer)) != -1) {\n"
                + "        out.write(buffer, 0, count);\n"
                + "    }\n"
                + "}  // both closed in reverse order"));
        en.add(LessonBlock.note(
                "In JDK 8, write the resource type explicitly: FileInputStream, "
                + "FileOutputStream, BufferedReader, and so on. Resources are closed in reverse order."));
        return new Lesson("7.3", "try-with-resources", "try-with-resources", uk, en);
    }

    private static Lesson lessonCustomExceptions() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Власні винятки"));
        uk.add(LessonBlock.code(
                "// Checked виняток — компілятор змусить обробити\n"
                + "class InsufficientFundsException extends Exception {\n"
                + "    private final double deficit;\n"
                + "\n"
                + "    InsufficientFundsException(double deficit) {\n"
                + "        super(\"Недостатньо коштів. Бракує: \" + deficit);\n"
                + "        this.deficit = deficit;\n"
                + "    }\n"
                + "\n"
                + "    double getDeficit() { return deficit; }\n"
                + "}\n"
                + "\n"
                + "// Unchecked виняток — компілятор НЕ змусить обробити\n"
                + "class InvalidDataException extends RuntimeException {\n"
                + "    InvalidDataException(String msg) { super(msg); }\n"
                + "}\n"
                + "\n"
                + "// Використання\n"
                + "void withdraw(double amount) throws InsufficientFundsException {\n"
                + "    if (amount > balance)\n"
                + "        throw new InsufficientFundsException(amount - balance);\n"
                + "    balance -= amount;\n"
                + "}"));
        uk.add(LessonBlock.list(
                "Checked (extends Exception) — для відновлюваних помилок (нема файлу, нема з'єднання)",
                "Unchecked (extends RuntimeException) — для помилок програміста (null, індекс за межами)",
                "Використовуйте throws у сигнатурі методу для checked-винятків",
                "throw створює новий об'єкт винятку і «кидає» його"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Custom exceptions"));
        en.add(LessonBlock.code(
                "// Checked exception — compiler forces you to handle\n"
                + "class InsufficientFundsException extends Exception {\n"
                + "    private final double deficit;\n"
                + "\n"
                + "    InsufficientFundsException(double deficit) {\n"
                + "        super(\"Insufficient funds. Short by: \" + deficit);\n"
                + "        this.deficit = deficit;\n"
                + "    }\n"
                + "\n"
                + "    double getDeficit() { return deficit; }\n"
                + "}\n"
                + "\n"
                + "// Unchecked exception — compiler won't force handling\n"
                + "class InvalidDataException extends RuntimeException {\n"
                + "    InvalidDataException(String msg) { super(msg); }\n"
                + "}\n"
                + "\n"
                + "// Usage\n"
                + "void withdraw(double amount) throws InsufficientFundsException {\n"
                + "    if (amount > balance)\n"
                + "        throw new InsufficientFundsException(amount - balance);\n"
                + "    balance -= amount;\n"
                + "}"));
        en.add(LessonBlock.list(
                "Checked (extends Exception) — for recoverable errors (no file, no connection)",
                "Unchecked (extends RuntimeException) — for programmer errors (null, out of bounds)",
                "Use throws in method signature for checked exceptions",
                "throw creates a new exception object and \"throws\" it"));
        return new Lesson("7.4", "Власні винятки", "Custom exceptions", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 8. Рядки
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter08(Course c) {
        Chapter ch = new Chapter("Глава 8. Рядки", "Chapter 8. Strings");
        ch.add(lessonStringImmutable());
        ch.add(lessonStringMethods());
        ch.add(lessonStringBuilder());
        c.add(ch);
    }

    private static Lesson lessonStringImmutable() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("String: незмінність (immutability)"));
        uk.add(LessonBlock.paragraph(
                "String в Java — НЕЗМІННИЙ (immutable). Коли ви \"змінюєте\" рядок, "
                + "створюється НОВИЙ об'єкт, а старий залишається в пам'яті. "
                + "Це важливо знати для продуктивності!"));
        uk.add(LessonBlock.code(
                "String s1 = \"Hello\";\n"
                + "String s2 = s1.concat(\" World\");  // створює НОВИЙ рядок\n"
                + "System.out.println(s1);   // Hello (не змінилося!)\n"
                + "System.out.println(s2);   // Hello World\n"
                + "\n"
                + "// === Інтернування рядків (String Pool) ===\n"
                + "String a = \"hello\";\n"
                + "String b = \"hello\";\n"
                + "System.out.println(a == b);  // true — посилаються на той самий об'єкт!\n"
                + "\n"
                + "String c = new String(\"hello\");\n"
                + "System.out.println(a == c);   // false — new завжди створює новий об'єкт\n"
                + "System.out.println(a.equals(c));  // true — порівнює зміст"));
        uk.add(LessonBlock.warning(
                "Порівнюйте рядки через equals(), а НЕ через ==. "
                + "== перевіряє чи це той самий об'єкт у пам'яті, а equals — "
                + "чи однаковий зміст. Для рядків завжди хочете перевірити зміст."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("String: immutability"));
        en.add(LessonBlock.paragraph(
                "String in Java is IMMUTABLE. When you \"change\" a string, a NEW object "
                + "is created while the old one stays in memory. This is important for "
                + "performance!"));
        en.add(LessonBlock.code(
                "String s1 = \"Hello\";\n"
                + "String s2 = s1.concat(\" World\");  // creates a NEW string\n"
                + "System.out.println(s1);   // Hello (unchanged!)\n"
                + "System.out.println(s2);   // Hello World\n"
                + "\n"
                + "// === String Pool interning ===\n"
                + "String a = \"hello\";\n"
                + "String b = \"hello\";\n"
                + "System.out.println(a == b);  // true — same object!\n"
                + "\n"
                + "String c = new String(\"hello\");\n"
                + "System.out.println(a == c);   // false — new always creates a new object\n"
                + "System.out.println(a.equals(c));  // true — compares content"));
        en.add(LessonBlock.warning(
                "Compare strings with equals(), NOT with ==. "
                + "== checks if it's the same object in memory, equals checks if the "
                + "content is the same. For strings you always want to compare content."));
        return new Lesson("8.1", "String та незмінність", "String & immutability", uk, en);
    }

    private static Lesson lessonStringMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Корисні методи String"));
        uk.add(LessonBlock.code(
                "String s = \"  Hello, Java World!  \";\n"
                + "\n"
                + "// Довжина та доступ до символів\n"
                + "s.length();                  // 22 (з пробілами)\n"
                + "s.charAt(2);                 // 'H'\n"
                + "\n"
                + "// Пошук\n"
                + "s.indexOf(\"Java\");          // 9\n"
                + "s.contains(\"Java\");         // true\n"
                + "s.startsWith(\"  Hello\");    // true\n"
                + "s.endsWith(\"!\");            // true\n"
                + "\n"
                + "// Зріз та трансформація\n"
                + "s.trim();                    // \"Hello, Java World!\"\n"
                + "s.substring(9, 13);          // \"Java\"\n"
                + "s.toUpperCase();             // \"  HELLO, JAVA WORLD!  \"\n"
                + "s.toLowerCase();             // \"  hello, java world!  \"\n"
                + "\n"
                + "// Заміна та розділення\n"
                + "\"a-b-c\".replace('-', '_');  // \"a_b_c\"\n"
                + "\"one,two,three\".split(\",\"); // [\"one\", \"two\", \"three\"]\n"
                + "\n"
                + "// Перевірка та конвертація\n"
                + "\"\".isEmpty();                // true\n"
                + "Integer.parseInt(\"42\");      // 42\n"
                + "String.valueOf(3.14);        // \"3.14\"\n"
                + "String.join(\" \", \"a\", \"b\"); // \"a b\""));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Useful String methods"));
        en.add(LessonBlock.code(
                "String s = \"  Hello, Java World!  \";\n"
                + "\n"
                + "// Length and character access\n"
                + "s.length();                  // 22 (with spaces)\n"
                + "s.charAt(2);                 // 'H'\n"
                + "\n"
                + "// Search\n"
                + "s.indexOf(\"Java\");          // 9\n"
                + "s.contains(\"Java\");         // true\n"
                + "s.startsWith(\"  Hello\");    // true\n"
                + "s.endsWith(\"!\");            // true\n"
                + "\n"
                + "// Slicing and transformation\n"
                + "s.trim();                    // \"Hello, Java World!\"\n"
                + "s.substring(9, 13);          // \"Java\"\n"
                + "s.toUpperCase();             // \"  HELLO, JAVA WORLD!  \"\n"
                + "s.toLowerCase();             // \"  hello, java world!  \"\n"
                + "\n"
                + "// Replace and split\n"
                + "\"a-b-c\".replace('-', '_');  // \"a_b_c\"\n"
                + "\"one,two,three\".split(\",\"); // [\"one\", \"two\", \"three\"]\n"
                + "\n"
                + "// Check and convert\n"
                + "\"\".isEmpty();                // true\n"
                + "Integer.parseInt(\"42\");      // 42\n"
                + "String.valueOf(3.14);        // \"3.14\"\n"
                + "String.join(\" \", \"a\", \"b\"); // \"a b\""));
        return new Lesson("8.2", "Методи String", "String methods", uk, en);
    }

    private static Lesson lessonStringBuilder() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("StringBuilder та StringBuffer"));
        uk.add(LessonBlock.paragraph(
                "StringBuilder — змінний (mutable) рядок. Не створює нові об'єкти "
                + "при кожній зміні, тому значно ШВИДШИЙ за конкатенацію String у циклах."));
        uk.add(LessonBlock.code(
                "StringBuilder sb = new StringBuilder(\"Hello\");\n"
                + "sb.append(\" World\");         // Hello World\n"
                + "sb.insert(5, \",\");           // Hello, World\n"
                + "sb.replace(6, 11, \"Java\");   // Hello, Java\n"
                + "sb.delete(5, 6);              // Hello Java\n"
                + "sb.reverse();                 // avaJ olleH\n"
                + "String result = sb.toString(); // avaJ olleH"));
        uk.add(LessonBlock.heading("Порівняння продуктивності"));
        uk.add(LessonBlock.code(
                "// ПОГАНО — створює тисячі проміжних об'єктів String\n"
                + "String bad = \"\";\n"
                + "for (int i = 0; i < 10000; i++) {\n"
                + "    bad += i;  // кожна ітерація = new String!\n"
                + "}\n"
                + "\n"
                + "// ДОБРЕ — один об'єкт StringBuilder\n"
                + "StringBuilder good = new StringBuilder();\n"
                + "for (int i = 0; i < 10000; i++) {\n"
                + "    good.append(i);\n"
                + "}\n"
                + "String result = good.toString();"));
        uk.add(LessonBlock.table(
                "Клас\tЗмінність\tПотокобезпечність\tКоли використовувати",
                Arrays.asList(
                    "String\tНі (immutable)\tТак (немає змін)\tКороткі рядки, константи",
                    "StringBuilder\tТак\tНі\tОднопоточні операції з рядками",
                    "StringBuffer\tТак\tТак (synchronized)\tБагатопоточні операції")));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("StringBuilder and StringBuffer"));
        en.add(LessonBlock.paragraph(
                "StringBuilder is a mutable string. It doesn't create new objects on every "
                + "change, so it's much FASTER than String concatenation in loops."));
        en.add(LessonBlock.code(
                "StringBuilder sb = new StringBuilder(\"Hello\");\n"
                + "sb.append(\" World\");         // Hello World\n"
                + "sb.insert(5, \",\");           // Hello, World\n"
                + "sb.replace(6, 11, \"Java\");   // Hello, Java\n"
                + "sb.delete(5, 6);              // Hello Java\n"
                + "sb.reverse();                 // avaJ olleH\n"
                + "String result = sb.toString(); // avaJ olleH"));
        en.add(LessonBlock.heading("Performance comparison"));
        en.add(LessonBlock.code(
                "// BAD — creates thousands of intermediate String objects\n"
                + "String bad = \"\";\n"
                + "for (int i = 0; i < 10000; i++) {\n"
                + "    bad += i;  // each iteration = new String!\n"
                + "}\n"
                + "\n"
                + "// GOOD — one StringBuilder object\n"
                + "StringBuilder good = new StringBuilder();\n"
                + "for (int i = 0; i < 10000; i++) {\n"
                + "    good.append(i);\n"
                + "}\n"
                + "String result = good.toString();"));
        en.add(LessonBlock.table(
                "Class\tMutability\tThread-safe\tWhen to use",
                Arrays.asList(
                    "String\tImmutable\tYes (no changes)\tShort strings, constants",
                    "StringBuilder\tYes\tNo\tSingle-threaded string ops",
                    "StringBuffer\tYes\tYes (synchronized)\tMulti-threaded string ops")));
        return new Lesson("8.3", "StringBuilder та StringBuffer", "StringBuilder & StringBuffer", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 9. Колекції
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter09(Course c) {
        Chapter ch = new Chapter("Глава 9. Колекції", "Chapter 9. Collections");
        ch.add(lessonList());
        ch.add(lessonSet());
        ch.add(lessonMap());
        ch.add(lessonIterator());
        c.add(ch);
    }

    private static Lesson lessonList() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("List: ArrayList та LinkedList"));
        uk.add(LessonBlock.paragraph(
                "List — впорядкована колекція, що дозволяє дублікати. Дві основні "
                + "реалізації: ArrayList (на основі динамічного масиву) та LinkedList (на основі двозв'язного списку)."));
        uk.add(LessonBlock.code(
                "List<String> names = new ArrayList<>();\n"
                + "\n"
                + "// Додавання\n"
                + "names.add(\"Іван\");\n"
                + "names.add(\"Олена\");\n"
                + "names.add(\"Андрій\");\n"
                + "names.add(1, \"Марія\");  // вставити на позицію 1\n"
                + "System.out.println(names);  // [Іван, Марія, Олена, Андрій]\n"
                + "\n"
                + "// Доступ\n"
                + "names.get(0);              // Іван\n"
                + "names.size();              // 4\n"
                + "names.contains(\"Олена\");  // true\n"
                + "names.indexOf(\"Андрій\"); // 3\n"
                + "\n"
                + "// Видалення та зміна\n"
                + "names.remove(\"Марія\");    // видалити за значенням\n"
                + "names.remove(0);           // видалити за індексом\n"
                + "names.set(0, \"Богдан\");   // замінити елемент\n"
                + "\n"
                + "// Сортування\n"
                + "names.sort(Comparator.naturalOrder());"));
        uk.add(LessonBlock.note(
                "ArrayList — швидкий доступ по індексу O(1), повільне видалення з "
                + "середини O(n). LinkedList — навпаки: швидке вставляння/видалення "
                + "O(1) при наявності ітератора, але повільний доступ O(n). "
                + "На практиці ArrayList кращий у 95% випадків."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("List: ArrayList and LinkedList"));
        en.add(LessonBlock.paragraph(
                "List is an ordered collection with duplicates. Two main implementations: "
                + "ArrayList (array underneath) and LinkedList (linked list)."));
        en.add(LessonBlock.code(
                "List<String> names = new ArrayList<>();\n"
                + "\n"
                + "// Adding\n"
                + "names.add(\"John\");\n"
                + "names.add(\"Helen\");\n"
                + "names.add(\"Andrey\");\n"
                + "names.add(1, \"Maria\");  // insert at position 1\n"
                + "System.out.println(names);  // [John, Maria, Helen, Andrey]\n"
                + "\n"
                + "// Access\n"
                + "names.get(0);              // John\n"
                + "names.size();              // 4\n"
                + "names.contains(\"Helen\");  // true\n"
                + "names.indexOf(\"Andrey\"); // 3\n"
                + "\n"
                + "// Remove and change\n"
                + "names.remove(\"Maria\");    // remove by value\n"
                + "names.remove(0);           // remove by index\n"
                + "names.set(0, \"Bogdan\");   // replace element\n"
                + "\n"
                + "// Sorting\n"
                + "names.sort(Comparator.naturalOrder());"));
        en.add(LessonBlock.note(
                "ArrayList — fast indexed access O(1), slow middle removal O(n). "
                + "LinkedList — opposite: fast insert/remove O(1) with an iterator, "
                + "but slow access O(n). In practice ArrayList is better 95% of the time."));
        return new Lesson("9.1", "List: ArrayList та LinkedList", "List: ArrayList & LinkedList", uk, en);
    }

    private static Lesson lessonSet() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Set: унікальні елементи"));
        uk.add(LessonBlock.code(
                "// HashSet — швидкий, без порядку\n"
                + "Set<String> set = new HashSet<>(Arrays.asList(\"b\", \"a\", \"c\", \"a\"));\n"
                + "System.out.println(set);  // [a, b, c] — дублікат \"a\" відкинутий\n"
                + "\n"
                + "// TreeSet — відсортований (через Comparable)\n"
                + "TreeSet<Integer> sorted = new TreeSet<>(Arrays.asList(5, 1, 3, 1));\n"
                + "System.out.println(sorted);       // [1, 3, 5]\n"
                + "System.out.println(sorted.first());  // 1\n"
                + "System.out.println(sorted.last());   // 5\n"
                + "\n"
                + "// LinkedHashSet — зберігає порядок вставки\n"
                + "LinkedHashSet<String> ordered = new LinkedHashSet<>();\n"
                + "ordered.add(\"c\"); ordered.add(\"a\"); ordered.add(\"b\");\n"
                + "System.out.println(ordered);  // [c, a, b] — порядок збережено"));
        uk.add(LessonBlock.warning(
                "HashSet використовує hashCode() + equals(). Якщо ваш клас НЕ "
                + "перевизначає ці методи — два різних об'єкти з однаковим станом "
                + "будуть вважатися різними елементами Set!"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Set: unique elements"));
        en.add(LessonBlock.code(
                "// HashSet — fast, no ordering\n"
                + "Set<String> set = new HashSet<>(Arrays.asList(\"b\", \"a\", \"c\", \"a\"));\n"
                + "System.out.println(set);  // [a, b, c] — duplicate \"a\" removed\n"
                + "\n"
                + "// TreeSet — sorted (via Comparable)\n"
                + "TreeSet<Integer> sorted = new TreeSet<>(Arrays.asList(5, 1, 3, 1));\n"
                + "System.out.println(sorted);       // [1, 3, 5]\n"
                + "System.out.println(sorted.first());  // 1\n"
                + "System.out.println(sorted.last());   // 5\n"
                + "\n"
                + "// LinkedHashSet — preserves insertion order\n"
                + "LinkedHashSet<String> ordered = new LinkedHashSet<>();\n"
                + "ordered.add(\"c\"); ordered.add(\"a\"); ordered.add(\"b\");\n"
                + "System.out.println(ordered);  // [c, a, b] — order preserved"));
        en.add(LessonBlock.warning(
                "HashSet uses hashCode() + equals(). If your class doesn't override "
                + "these methods, two different objects with the same state will be "
                + "treated as different Set elements!"));
        return new Lesson("9.2", "Set: HashSet, TreeSet", "Set: HashSet, TreeSet", uk, en);
    }

    private static Lesson lessonMap() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Map: ключ-значення"));
        uk.add(LessonBlock.code(
                "Map<String, Integer> ages = new HashMap<>();\n"
                + "\n"
                + "// Додавання та оновлення\n"
                + "ages.put(\"Іван\", 30);\n"
                + "ages.put(\"Олена\", 25);\n"
                + "ages.put(\"Іван\", 31);  // оновлення існуючого ключа\n"
                + "\n"
                + "// Отримання\n"
                + "ages.get(\"Іван\");          // 31\n"
                + "ages.getOrDefault(\"Богдан\", 0);  // 0 (ключа немає)\n"
                + "\n"
                + "// Перевірка\n"
                + "ages.containsKey(\"Олена\");  // true\n"
                + "ages.containsValue(25);      // true\n"
                + "ages.size();                 // 2\n"
                + "\n"
                + "// Безпечне оновлення\n"
                + "ages.putIfAbsent(\"Богдан\", 22);  // додає, якщо ключа немає\n"
                + "ages.merge(\"Іван\", 1, Integer::sum);  // 31+1=32\n"
                + "\n"
                + "// Ітерація\n"
                + "ages.forEach((name, age) ->\n"
                + "    System.out.println(name + \": \" + age));\n"
                + "\n"
                + "// Ключі, значення, записи\n"
                + "ages.keySet();       // Set<String>\n"
                + "ages.values();       // Collection<Integer>\n"
                + "ages.entrySet();     // Set<Map.Entry<String, Integer>>"));
        uk.add(LessonBlock.note(
                "HashMap — швидкий (O(1)), без порядку ключів. TreeMap — ключі "
                + "відсортовані (O(log n)). LinkedHashMap — зберігає порядок вставки. "
                + "Для використання ключів у HashMap обов'язково перевизначте "
                + "hashCode() та equals()."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Map: key-value pairs"));
        en.add(LessonBlock.code(
                "Map<String, Integer> ages = new HashMap<>();\n"
                + "\n"
                + "// Add and update\n"
                + "ages.put(\"John\", 30);\n"
                + "ages.put(\"Helen\", 25);\n"
                + "ages.put(\"John\", 31);  // update existing key\n"
                + "\n"
                + "// Retrieve\n"
                + "ages.get(\"John\");          // 31\n"
                + "ages.getOrDefault(\"Bogdan\", 0);  // 0 (key doesn't exist)\n"
                + "\n"
                + "// Check\n"
                + "ages.containsKey(\"Helen\");  // true\n"
                + "ages.containsValue(25);      // true\n"
                + "ages.size();                 // 2\n"
                + "\n"
                + "// Safe update\n"
                + "ages.putIfAbsent(\"Bogdan\", 22);  // add if key absent\n"
                + "ages.merge(\"John\", 1, Integer::sum);  // 31+1=32\n"
                + "\n"
                + "// Iteration\n"
                + "ages.forEach((name, age) ->\n"
                + "    System.out.println(name + \": \" + age));\n"
                + "\n"
                + "// Keys, values, entries\n"
                + "ages.keySet();       // Set<String>\n"
                + "ages.values();       // Collection<Integer>\n"
                + "ages.entrySet();     // Set<Map.Entry<String, Integer>>"));
        en.add(LessonBlock.note(
                "HashMap — fast (O(1)), no key ordering. TreeMap — sorted keys (O(log n)). "
                + "LinkedHashMap — preserves insertion order. For keys in HashMap you MUST "
                + "override hashCode() and equals()."));
        return new Lesson("9.3", "Map: HashMap, TreeMap", "Map: HashMap, TreeMap", uk, en);
    }

    private static Lesson lessonIterator() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Iterator та for-each"));
        uk.add(LessonBlock.code(
                "List<String> list = Arrays.asList(\"A\", \"B\", \"C\", \"D\");\n"
                + "\n"
                + "// Enhanced for — простий та зрозумілий\n"
                + "for (String s : list) {\n"
                + "    System.out.println(s);\n"
                + "}\n"
                + "\n"
                + "// for з індексом\n"
                + "for (int i = 0; i < list.size(); i++) {\n"
                + "    System.out.println(i + \": \" + list.get(i));\n"
                + "}\n"
                + "\n"
                + "// Iterator — для безпечного видалення під час обходу\n"
                + "Iterator<String> it = list.iterator();\n"
                + "while (it.hasNext()) {\n"
                + "    String s = it.next();\n"
                + "    if (s.equals(\"B\")) {\n"
                + "        it.remove();  // безпечне видалення\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(list);  // [A, C, D]"));
        uk.add(LessonBlock.warning(
                "Не видаляйте елементи під час enhanced-for циклу! "
                + "list.remove(s) усередині for-each спричинить "
                + "ConcurrentModificationException. Використовуйте Iterator "
                + "або removeIf() (Java 8+)."));
        uk.add(LessonBlock.heading("removeIf (Java 8+)"));
        uk.add(LessonBlock.code(
                "list.removeIf(s -> s.equals(\"A\"));  // видалити \"A\"\n"
                + "// Найпростіший спосіб безпечного видалення!"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Iterator and for-each"));
        en.add(LessonBlock.code(
                "List<String> list = Arrays.asList(\"A\", \"B\", \"C\", \"D\");\n"
                + "\n"
                + "// Enhanced for — simple and clear\n"
                + "for (String s : list) {\n"
                + "    System.out.println(s);\n"
                + "}\n"
                + "\n"
                + "// for with index\n"
                + "for (int i = 0; i < list.size(); i++) {\n"
                + "    System.out.println(i + \": \" + list.get(i));\n"
                + "}\n"
                + "\n"
                + "// Iterator — safe removal during traversal\n"
                + "Iterator<String> it = list.iterator();\n"
                + "while (it.hasNext()) {\n"
                + "    String s = it.next();\n"
                + "    if (s.equals(\"B\")) {\n"
                + "        it.remove();  // safe removal\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(list);  // [A, C, D]"));
        en.add(LessonBlock.warning(
                "Don't remove elements during an enhanced-for loop! "
                + "list.remove(s) inside for-each will throw "
                + "ConcurrentModificationException. Use Iterator or removeIf() (Java 8+)."));
        en.add(LessonBlock.heading("removeIf (Java 8+)"));
        en.add(LessonBlock.code(
                "list.removeIf(s -> s.equals(\"A\"));  // remove \"A\"\n"
                + "// The simplest safe removal!"));
        return new Lesson("9.4", "Iterator та for-each", "Iterator & for-each", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 10. Потоки введення-виведення
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter10(Course c) {
        Chapter ch = new Chapter("Глава 10. Потоки введення-виведення",
                "Chapter 10. I/O streams");
        ch.add(lessonFileNio());
        ch.add(lessonByteAndCharStreams());
        c.add(ch);
    }

    private static Lesson lessonFileNio() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Робота з файлами (NIO.2)"));
        uk.add(LessonBlock.paragraph(
                "java.nio.file (NIO.2, Java 7+) — сучасний спосіб роботи з файлами. "
                + "Простіший та безпечніший за старий java.io.File."));
        uk.add(LessonBlock.code(
                "import java.nio.file.*;\n"
                + "import java.nio.charset.StandardCharsets;\n"
                + "import java.io.IOException;\n"
                + "import java.io.BufferedReader;\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "// Створення шляху\n"
                + "Path p = Paths.get(\"data\", \"users.txt\");\n"
                + "Path absolute = Paths.get(\"/home/user/file.txt\");\n"
                + "\n"
                + "// Запис\n"
                + "Files.write(p,\n"
                + "        Arrays.asList(\"Привіт, світ!\", \"Рядок 2\"),\n"
                + "        StandardCharsets.UTF_8);\n"
                + "\n"
                + "// Читання всього файлу\n"
                + "byte[] bytes = Files.readAllBytes(p);\n"
                + "String content = new String(bytes, StandardCharsets.UTF_8);\n"
                + "System.out.println(content);\n"
                + "\n"
                + "// Читання по рядках\n"
                + "List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);\n"
                + "lines.forEach(System.out::println);\n"
                + "\n"
                + "// Перевірка існування\n"
                + "Files.exists(p);          // true\n"
                + "Files.isRegularFile(p);   // true\n"
                + "Files.isDirectory(Paths.get(\"data\"));  // true\n"
                + "\n"
                + "// Копіювання та переміщення\n"
                + "Files.copy(p, Paths.get(\"backup.txt\"), StandardCopyOption.REPLACE_EXISTING);\n"
                + "Files.move(p, Paths.get(\"archive.txt\"));\n"
                + "\n"
                + "// Видалення\n"
                + "Files.delete(p);"));
        uk.add(LessonBlock.heading("try-with-resources для великих файлів"));
        uk.add(LessonBlock.code(
                "// Для великих файлів — читання по рядках (не все в пам'яті)\n"
                + "try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {\n"
                + "    String line;\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        System.out.println(line);\n"
                + "    }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("File operations (NIO.2)"));
        en.add(LessonBlock.paragraph(
                "java.nio.file (NIO.2, Java 7+) is the modern way to work with files. "
                + "Simpler and safer than the old java.io.File."));
        en.add(LessonBlock.code(
                "import java.nio.file.*;\n"
                + "import java.nio.charset.StandardCharsets;\n"
                + "import java.io.IOException;\n"
                + "import java.io.BufferedReader;\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "\n"
                + "// Create a path\n"
                + "Path p = Paths.get(\"data\", \"users.txt\");\n"
                + "Path absolute = Paths.get(\"/home/user/file.txt\");\n"
                + "\n"
                + "// Write\n"
                + "Files.write(p,\n"
                + "        Arrays.asList(\"Hello, world!\", \"Line 2\"),\n"
                + "        StandardCharsets.UTF_8);\n"
                + "\n"
                + "// Read entire file\n"
                + "byte[] bytes = Files.readAllBytes(p);\n"
                + "String content = new String(bytes, StandardCharsets.UTF_8);\n"
                + "System.out.println(content);\n"
                + "\n"
                + "// Read by lines\n"
                + "List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);\n"
                + "lines.forEach(System.out::println);\n"
                + "\n"
                + "// Check existence\n"
                + "Files.exists(p);          // true\n"
                + "Files.isRegularFile(p);   // true\n"
                + "Files.isDirectory(Paths.get(\"data\"));  // true\n"
                + "\n"
                + "// Copy and move\n"
                + "Files.copy(p, Paths.get(\"backup.txt\"), StandardCopyOption.REPLACE_EXISTING);\n"
                + "Files.move(p, Paths.get(\"archive.txt\"));\n"
                + "\n"
                + "// Delete\n"
                + "Files.delete(p);"));
        en.add(LessonBlock.heading("try-with-resources for large files"));
        en.add(LessonBlock.code(
                "// For large files — read line by line (not all in memory)\n"
                + "try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {\n"
                + "    String line;\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        System.out.println(line);\n"
                + "    }\n"
                + "}"));
        return new Lesson("10.1", "Робота з файлами (NIO.2)", "File I/O (NIO.2)", uk, en);
    }

    private static Lesson lessonByteAndCharStreams() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Потоки: байтові та символьні"));
        uk.add(LessonBlock.paragraph(
                "Потік (stream) — послідовність даних. Байтові потоки (InputStream/OutputStream) "
                + "працюють з бінарними даними. Символьні (Reader/Writer) — з текстом."));
        uk.add(LessonBlock.code(
                "import java.io.*;\n"
                + "import java.nio.charset.StandardCharsets;\n"
                + "\n"
                + "// Копіювання файлу через байтові потоки\n"
                + "try (FileInputStream in = new FileInputStream(\"source.jpg\");\n"
                + "     FileOutputStream out = new FileOutputStream(\"copy.jpg\")) {\n"
                + "    byte[] buffer = new byte[8192];\n"
                + "    int bytesRead;\n"
                + "    while ((bytesRead = in.read(buffer)) != -1) {\n"
                + "        out.write(buffer, 0, bytesRead);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Buffered — краща продуктивність\n"
                + "try (BufferedReader br = new BufferedReader(\n"
                + "        new InputStreamReader(\n"
                + "            new FileInputStream(\"data.txt\"), StandardCharsets.UTF_8));\n"
                + "     BufferedWriter bw = new BufferedWriter(\n"
                + "         new OutputStreamWriter(\n"
                + "             new FileOutputStream(\"out.txt\"), StandardCharsets.UTF_8))) {\n"
                + "    String line;\n"
                + "    while ((line = br.readLine()) != null) {\n"
                + "        bw.write(line);\n"
                + "        bw.newLine();\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Зазвичай NIO.2 (Files.readAllLines, Files.write) — "
                + "найпростіший вибір для файлів. Старі потоки (InputStream/OutputStream) "
                + "потрібні для бінарних даних, мережевих з'єднань або великих файлів."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Streams: byte and character"));
        en.add(LessonBlock.paragraph(
                "A stream is a sequence of data. Byte streams (InputStream/OutputStream) "
                + "work with binary data. Character streams (Reader/Writer) — with text."));
        en.add(LessonBlock.code(
                "import java.io.*;\n"
                + "import java.nio.charset.StandardCharsets;\n"
                + "\n"
                + "// Copy a file via byte streams\n"
                + "try (FileInputStream in = new FileInputStream(\"source.jpg\");\n"
                + "     FileOutputStream out = new FileOutputStream(\"copy.jpg\")) {\n"
                + "    byte[] buffer = new byte[8192];\n"
                + "    int bytesRead;\n"
                + "    while ((bytesRead = in.read(buffer)) != -1) {\n"
                + "        out.write(buffer, 0, bytesRead);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Buffered — better performance\n"
                + "try (BufferedReader br = new BufferedReader(\n"
                + "        new InputStreamReader(\n"
                + "            new FileInputStream(\"data.txt\"), StandardCharsets.UTF_8));\n"
                + "     BufferedWriter bw = new BufferedWriter(\n"
                + "         new OutputStreamWriter(\n"
                + "             new FileOutputStream(\"out.txt\"), StandardCharsets.UTF_8))) {\n"
                + "    String line;\n"
                + "    while ((line = br.readLine()) != null) {\n"
                + "        bw.write(line);\n"
                + "        bw.newLine();\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.note(
                "Usually NIO.2 (Files.readAllLines, Files.write) is the simplest "
                + "choice for files. Old streams (InputStream/OutputStream) are needed for "
                + "binary data, network connections or very large files."));
        return new Lesson("10.2", "Потоки введення-виведення", "I/O streams", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 11. Багатопоточність
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter11(Course c) {
        Chapter ch = new Chapter("Глава 11. Багатопоточність",
                "Chapter 11. Multithreading");
        ch.add(lessonThreadsCreation());
        ch.add(lessonSynchronization());
        ch.add(lessonExecutorService());
        c.add(ch);
    }

    private static Lesson lessonThreadsCreation() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Створення потоків"));
        uk.add(LessonBlock.paragraph(
                "Потік (thread) — окремий потік виконання. Java підтримує "
                + "багатопоточність на рівні мови. Є два способи створити потік: "
                + "через Thread та через Runnable."));
        uk.add(LessonBlock.code(
                "// Спосіб 1: lambda + Thread (найпростіший)\n"
                + "Thread t1 = new Thread(() -> {\n"
                + "    for (int i = 0; i < 5; i++) {\n"
                + "        System.out.println(\"Потік 1: \" + i);\n"
                + "    }\n"
                + "});\n"
                + "\n"
                + "// Спосіб 2: Runnable (краще для тестування та переиспользования)\n"
                + "Runnable task = () -> {\n"
                + "    for (int i = 0; i < 5; i++) {\n"
                + "        System.out.println(\"Потік 2: \" + i);\n"
                + "    }\n"
                + "};\n"
                + "Thread t2 = new Thread(task, \"worker-2\");\n"
                + "\n"
                + "t1.start();  // ЗАПУСКАЄ новий потік!\n"
                + "t2.start();  // run() — просто викликає метод у поточному потоці!\n"
                + "\n"
                + "t1.join();  // головний потік ЧЕКАЄ завершення t1\n"
                + "t2.join();  // головний потік ЧЕКАЄ завершення t2\n"
                + "System.out.println(\"Обидва потоки завершились!\");"));
        uk.add(LessonBlock.warning(
                "start() — запускає НОВИЙ потік. run() — просто викликає метод у "
                + "ПОТОЧНОМУ потоці (не створює новий)! Майже завжди використовуйте start()."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Creating threads"));
        en.add(LessonBlock.paragraph(
                "A thread is a separate execution flow. Java supports multithreading "
                + "at the language level. Two ways to create a thread: via Thread "
                + "and via Runnable."));
        en.add(LessonBlock.code(
                "// Way 1: lambda + Thread (simplest)\n"
                + "Thread t1 = new Thread(() -> {\n"
                + "    for (int i = 0; i < 5; i++) {\n"
                + "        System.out.println(\"Thread 1: \" + i);\n"
                + "    }\n"
                + "});\n"
                + "\n"
                + "// Way 2: Runnable (better for testing and reuse)\n"
                + "Runnable task = () -> {\n"
                + "    for (int i = 0; i < 5; i++) {\n"
                + "        System.out.println(\"Thread 2: \" + i);\n"
                + "    }\n"
                + "};\n"
                + "Thread t2 = new Thread(task, \"worker-2\");\n"
                + "\n"
                + "t1.start();  // LAUNCHES a new thread!\n"
                + "t2.start();  // run() just calls the method in the current thread!\n"
                + "\n"
                + "t1.join();  // main thread WAITS for t1 to finish\n"
                + "t2.join();  // main thread WAITS for t2 to finish\n"
                + "System.out.println(\"Both threads finished!\");"));
        en.add(LessonBlock.warning(
                "start() — launches a NEW thread. run() — just calls a method in the "
                + "CURRENT thread (doesn't create a new one)! Almost always use start()."));
        return new Lesson("11.1", "Створення потоків", "Creating threads", uk, en);
    }

    private static Lesson lessonSynchronization() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Синхронізація та race condition"));
        uk.add(LessonBlock.paragraph(
                "Race condition — коли два потоки одночасно змінюють одні дані, "
                + "і результат залежить від порядку виконання. Синхронізація — "
                + "захист від цього."));
        uk.add(LessonBlock.code(
                "// БЕЗ синхронізації — race condition!\n"
                + "class UnsafeCounter {\n"
                + "    private int count = 0;\n"
                + "    void inc() { count++; }  // count++ = read + increment + write\n"
                + "    int get() { return count; }\n"
                + "}\n"
                + "// Результат непередбачуваний: менше 2000!\n"
                + "\n"
                + "// З синхронізацією — правильно\n"
                + "class SafeCounter {\n"
                + "    private int count = 0;\n"
                + "    synchronized void inc() { count++; }\n"
                + "    int get() { return count; }\n"
                + "}\n"
                + "\n"
                + "SafeCounter c = new SafeCounter();\n"
                + "Runnable r = () -> { for (int i = 0; i < 1000; i++) c.inc(); };\n"
                + "Thread t1 = new Thread(r), t2 = new Thread(r);\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(c.get());  // завжди 2000"));
        uk.add(LessonBlock.heading("volatile та AtomicInteger"));
        uk.add(LessonBlock.code(
                "// volatile — гарантує видимість між потоками (але НЕ атомарність)\n"
                + "private volatile boolean running = true;\n"
                + "\n"
                + "// AtomicInteger — атомарні операції без synchronized\n"
                + "import java.util.concurrent.atomic.*;\n"
                + "AtomicInteger counter = new AtomicInteger(0);\n"
                + "counter.incrementAndGet();  // атомарний i++"));
        uk.add(LessonBlock.note(
                "synchronized блокує весь об'єкт — обмежує паралелізм. "
                + "Для простих лічильників AtomicInteger ефективніший. "
                + "Для складних операцій — ReentrantLock (java.util.concurrent)."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Synchronization and race conditions"));
        en.add(LessonBlock.paragraph(
                "Race condition — when two threads simultaneously modify the same data, "
                + "and the result depends on execution order. Synchronization protects "
                + "against this."));
        en.add(LessonBlock.code(
                "// WITHOUT synchronization — race condition!\n"
                + "class UnsafeCounter {\n"
                + "    private int count = 0;\n"
                + "    void inc() { count++; }  // count++ = read + increment + write\n"
                + "    int get() { return count; }\n"
                + "}\n"
                + "// Result is unpredictable: less than 2000!\n"
                + "\n"
                + "// WITH synchronization — correct\n"
                + "class SafeCounter {\n"
                + "    private int count = 0;\n"
                + "    synchronized void inc() { count++; }\n"
                + "    int get() { return count; }\n"
                + "}\n"
                + "\n"
                + "SafeCounter c = new SafeCounter();\n"
                + "Runnable r = () -> { for (int i = 0; i < 1000; i++) c.inc(); };\n"
                + "Thread t1 = new Thread(r), t2 = new Thread(r);\n"
                + "t1.start(); t2.start(); t1.join(); t2.join();\n"
                + "System.out.println(c.get());  // always 2000"));
        en.add(LessonBlock.heading("volatile and AtomicInteger"));
        en.add(LessonBlock.code(
                "// volatile — guarantees visibility across threads (but NOT atomicity)\n"
                + "private volatile boolean running = true;\n"
                + "\n"
                + "// AtomicInteger — atomic operations without synchronized\n"
                + "import java.util.concurrent.atomic.*;\n"
                + "AtomicInteger counter = new AtomicInteger(0);\n"
                + "counter.incrementAndGet();  // atomic i++"));
        en.add(LessonBlock.note(
                "synchronized locks the entire object — limits parallelism. "
                + "For simple counters AtomicInteger is more efficient. "
                + "For complex operations — ReentrantLock (java.util.concurrent)."));
        return new Lesson("11.2", "Синхронізація", "Synchronization", uk, en);
    }

    private static Lesson lessonExecutorService() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("ExecutorService: пул потоків"));
        uk.add(LessonBlock.paragraph(
                "Створювати Thread вручну для кожної задачі — погана практика. "
                + "ExecutorService керує пулом потоків: створює один раз, "
                + "відправляєш задачі — він сам розподіляє."));
        uk.add(LessonBlock.code(
                "import java.util.concurrent.*;\n"
                + "\n"
                + "// Створюємо пул з 4 потоків\n"
                + "ExecutorService pool = Executors.newFixedThreadPool(4);\n"
                + "\n"
                + "// Відправляємо 10 задач\n"
                + "for (int i = 0; i < 10; i++) {\n"
                + "    final int task = i;\n"
                + "    pool.submit(() -> {\n"
                + "        System.out.println(\"Задача \" + task\n"
                + "            + \" на \" + Thread.currentThread().getName());\n"
                + "    });\n"
                + "}\n"
                + "\n"
                + "pool.shutdown();  // завершити після виконання всіх задач\n"
                + "pool.awaitTermination(5, TimeUnit.SECONDS);"));
        uk.add(LessonBlock.heading("Future — результат задачі"));
        uk.add(LessonBlock.code(
                "ExecutorService pool = Executors.newSingleThreadExecutor();\n"
                + "Future<Integer> future = pool.submit(() -> {\n"
                + "    Thread.sleep(1000);\n"
                + "    return 42;\n"
                + "});\n"
                + "\n"
                + "System.out.println(\"Робимо інше...\");\n"
                + "Integer result = future.get();  // БЛОКУЄ до завершення\n"
                + "System.out.println(\"Результат: \" + result);  // 42\n"
                + "pool.shutdown();"));
        uk.add(LessonBlock.warning(
                "Обов'язково викликайте метод shutdown() для завершення роботи пулу потоків, оскільки "
                + "інакше активний пул перешкоджатиме завершенню процесу JVM. Використовуйте try-with-resources або блок finally."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("ExecutorService: thread pool"));
        en.add(LessonBlock.paragraph(
                "Creating a Thread manually for each task is bad practice. "
                + "ExecutorService manages a thread pool: create once, submit tasks, "
                + "it distributes them automatically."));
        en.add(LessonBlock.code(
                "import java.util.concurrent.*;\n"
                + "\n"
                + "// Create a pool with 4 threads\n"
                + "ExecutorService pool = Executors.newFixedThreadPool(4);\n"
                + "\n"
                + "// Submit 10 tasks\n"
                + "for (int i = 0; i < 10; i++) {\n"
                + "    final int task = i;\n"
                + "    pool.submit(() -> {\n"
                + "        System.out.println(\"Task \" + task\n"
                + "            + \" on \" + Thread.currentThread().getName());\n"
                + "    });\n"
                + "}\n"
                + "\n"
                + "pool.shutdown();  // finish after all tasks\n"
                + "pool.awaitTermination(5, TimeUnit.SECONDS);"));
        en.add(LessonBlock.heading("Future — task result"));
        en.add(LessonBlock.code(
                "ExecutorService pool = Executors.newSingleThreadExecutor();\n"
                + "Future<Integer> future = pool.submit(() -> {\n"
                + "    Thread.sleep(1000);\n"
                + "    return 42;\n"
                + "});\n"
                + "\n"
                + "System.out.println(\"Doing other things...\");\n"
                + "Integer result = future.get();  // BLOCKS until done\n"
                + "System.out.println(\"Result: \" + result);  // 42\n"
                + "pool.shutdown();"));
        en.add(LessonBlock.warning(
                "Be sure to call the shutdown() method to close the thread pool; otherwise, "
                + "the active pool will keep the JVM process running. Use try-with-resources or finally block."));
        return new Lesson("11.3", "ExecutorService", "ExecutorService", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 12. Лямбда-вирази та Stream API
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter12(Course c) {
        Chapter ch = new Chapter("Глава 12. Лямбда-вирази та Stream API",
                "Chapter 12. Lambda and Stream API");
        ch.add(lessonLambdaBasics());
        ch.add(lessonStreamPipeline());
        ch.add(lessonCollectors());
        c.add(ch);
    }

    private static Lesson lessonLambdaBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Лямбда-вирази: анонімні функції"));
        uk.add(LessonBlock.paragraph(
                "Лямбда — короткий запис для функціонального інтерфейсу "
                + "(з одним методом). Замінює громіздкі анонімні класи."));
        uk.add(LessonBlock.code(
                "// До Java 8\n"
                + "Runnable r1 = new Runnable() {\n"
                + "    @Override\n"
                + "    public void run() { System.out.println(\"Hello\"); }\n"
                + "};\n"
                + "\n"
                + "// З Java 8 — те саме, один рядок!\n"
                + "Runnable r2 = () -> System.out.println(\"Hello\");\n"
                + "\n"
                + "// З параметрами\n"
                + "Comparator<String> cmp = (a, b) -> a.length() - b.length();\n"
                + "\n"
                + "// Тіло з кількома рядками\n"
                + "Function<String, Integer> parser = s -> {\n"
                + "    s = s.trim();\n"
                + "    return Integer.parseInt(s);\n"
                + "};\n"
                + "\n"
                + "// Method reference (найкоротше)\n"
                + "Function<String, Integer> len = String::length;\n"
                + "Consumer<String> printer = System.out::println;"));
        uk.add(LessonBlock.list(
                "(x, y) -> x + y           — два параметри",
                "x -> x * x                — один параметр без дужок",
                "() -> System.out.println() — без параметрів",
                "x -> { return x * 2; }    — явний return"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Lambda expressions: anonymous functions"));
        en.add(LessonBlock.paragraph(
                "A lambda is a short notation for a functional interface (one method). "
                + "Replaces verbose anonymous classes."));
        en.add(LessonBlock.code(
                "// Before Java 8\n"
                + "Runnable r1 = new Runnable() {\n"
                + "    @Override\n"
                + "    public void run() { System.out.println(\"Hello\"); }\n"
                + "};\n"
                + "\n"
                + "// Since Java 8 — same thing, one line!\n"
                + "Runnable r2 = () -> System.out.println(\"Hello\");\n"
                + "\n"
                + "// With parameters\n"
                + "Comparator<String> cmp = (a, b) -> a.length() - b.length();\n"
                + "\n"
                + "// Multi-line body\n"
                + "Function<String, Integer> parser = s -> {\n"
                + "    s = s.trim();\n"
                + "    return Integer.parseInt(s);\n"
                + "};\n"
                + "\n"
                + "// Method reference (shortest)\n"
                + "Function<String, Integer> len = String::length;\n"
                + "Consumer<String> printer = System.out::println;"));
        en.add(LessonBlock.list(
                "(x, y) -> x + y           — two parameters",
                "x -> x * x                — single parameter without parens",
                "() -> System.out.println() — no parameters",
                "x -> { return x * 2; }    — explicit return"));
        return new Lesson("12.1", "Лямбда-вирази", "Lambda expressions", uk, en);
    }

    private static Lesson lessonStreamPipeline() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Stream API: конвеєр обробки даних"));
        uk.add(LessonBlock.paragraph(
                "Stream — це «конвеєр» для обробки колекцій. Фільтруєте, "
                + "трансформуєте, збираєте результат — ланцюжком операцій."));
        uk.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\n"
                + "    \"Іван\", \"Олена\", \"Андрій\", \"Марія\", \"Богдан\");\n"
                + "\n"
                + "// filter → map → sorted → collect\n"
                + "List<String> result = names.stream()\n"
                + "    .filter(n -> n.length() > 4)          // залишити довгі\n"
                + "    .map(String::toUpperCase)              // у верхній регістр\n"
                + "    .sorted()                              // відсортувати\n"
                + "    .toList();                             // зібрати у List\n"
                + "// [ОЛЕНА, АНДРІЙ, МАРІЯ, БОГДАН]\n"
                + "\n"
                + "// sum — підрахунок\n"
                + "int sum = IntStream.rangeClosed(1, 100)\n"
                + "    .reduce(0, Integer::sum);  // 5050\n"
                + "\n"
                + "// anyMatch — чи є хоч один?\n"
                + "boolean hasLong = names.stream()\n"
                + "    .anyMatch(n -> n.length() > 6);  // true\n"
                + "\n"
                + "// flatMap — розгортання вбудованих колекцій\n"
                + "List<String> words = Arrays.asList(\"hello world\", \"java stream\");\n"
                + "List<String> allWords = words.stream()\n"
                + "    .flatMap(w -> Arrays.stream(w.split(\" \")))\n"
                + "    .toList();\n"
                + "// [hello, world, java, stream]"));
        uk.add(LessonBlock.list(
                "Проміжні: filter, map, flatMap, sorted, distinct, peek, limit, skip",
                "Термінальні: collect, toList, forEach, reduce, count, anyMatch, findFirst"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Stream API: data processing pipeline"));
        en.add(LessonBlock.paragraph(
                "A Stream is a \"pipeline\" for processing collections. Filter, "
                + "transform, collect results — a chain of operations."));
        en.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\n"
                + "    \"John\", \"Helen\", \"Andrey\", \"Maria\", \"Bogdan\");\n"
                + "\n"
                + "// filter → map → sorted → collect\n"
                + "List<String> result = names.stream()\n"
                + "    .filter(n -> n.length() > 4)          // keep long ones\n"
                + "    .map(String::toUpperCase)              // to uppercase\n"
                + "    .sorted()                              // sort\n"
                + "    .toList();                             // collect to List\n"
                + "// [HELEN, ANDREY, MARIA, BOGDAN]\n"
                + "\n"
                + "// sum\n"
                + "int sum = IntStream.rangeClosed(1, 100)\n"
                + "    .reduce(0, Integer::sum);  // 5050\n"
                + "\n"
                + "// anyMatch — is there at least one?\n"
                + "boolean hasLong = names.stream()\n"
                + "    .anyMatch(n -> n.length() > 6);  // true\n"
                + "\n"
                + "// flatMap — flattening nested collections\n"
                + "List<String> words = Arrays.asList(\"hello world\", \"java stream\");\n"
                + "List<String> allWords = words.stream()\n"
                + "    .flatMap(w -> Arrays.stream(w.split(\" \")))\n"
                + "    .toList();\n"
                + "// [hello, world, java, stream]"));
        en.add(LessonBlock.list(
                "Intermediate: filter, map, flatMap, sorted, distinct, peek, limit, skip",
                "Terminal: collect, toList, forEach, reduce, count, anyMatch, findFirst"));
        return new Lesson("12.2", "Stream API", "Stream API", uk, en);
    }

    private static Lesson lessonCollectors() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Collectors: збирання результатів"));
        uk.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\"Іван\", \"Олена\", \"Андрій\", \"Марія\");\n"
                + "\n"
                + "// З'єднання рядків\n"
                + "String csv = names.stream().collect(Collectors.joining(\", \"));\n"
                + "// \"Іван, Олена, Андрій, Марія\"\n"
                + "\n"
                + "// Групування\n"
                + "Map<Integer, List<String>> byLength = names.stream()\n"
                + "    .collect(Collectors.groupingBy(String::length));\n"
                + "// {4=[Іван, Марія], 5=[Олена], 6=[Андрій]}\n"
                + "\n"
                + "// Поділ на дві групи\n"
                + "Map<Boolean, List<String>> parts = names.stream()\n"
                + "    .collect(Collectors.partitioningBy(n -> n.length() > 4));\n"
                + "// {false=[Іван], true=[Олена, Андрій, Марія]}\n"
                + "\n"
                + "// Підрахунок\n"
                + "Map<String, Integer> nameLen = names.stream()\n"
                + "    .collect(Collectors.toMap(n -> n, String::length));\n"
                + "// {Іван=4, Олена=5, Андрій=6, Марія=5}"));
        uk.add(LessonBlock.note(
                "Collectors — потужний інструмент. groupingBy + downstream collector "
                + "дозволяє робити складні агрегації: "
                + "Collectors.groupingBy(String::length, Collectors.counting())."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Collectors: gathering results"));
        en.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\"John\", \"Helen\", \"Andrey\", \"Maria\");\n"
                + "\n"
                + "// Join strings\n"
                + "String csv = names.stream().collect(Collectors.joining(\", \"));\n"
                + "// \"John, Helen, Andrey, Maria\"\n"
                + "\n"
                + "// Grouping\n"
                + "Map<Integer, List<String>> byLength = names.stream()\n"
                + "    .collect(Collectors.groupingBy(String::length));\n"
                + "// {4=[John, Maria], 5=[Helen], 6=[Andrey]}\n"
                + "\n"
                + "// Partition into two groups\n"
                + "Map<Boolean, List<String>> parts = names.stream()\n"
                + "    .collect(Collectors.partitioningBy(n -> n.length() > 4));\n"
                + "// {false=[John], true=[Helen, Andrey, Maria]}\n"
                + "\n"
                + "// Counting\n"
                + "Map<String, Integer> nameLen = names.stream()\n"
                + "    .collect(Collectors.toMap(n -> n, String::length));\n"
                + "// {John=4, Helen=5, Andrey=6, Maria=5}"));
        en.add(LessonBlock.note(
                "Collectors are powerful. groupingBy + downstream collector allows complex "
                + "aggregations: Collectors.groupingBy(String::length, Collectors.counting())."));
        return new Lesson("12.3", "Collectors", "Collectors", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 13. Generics
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter13(Course c) {
        Chapter ch = new Chapter("Глава 13. Узагальнення (Generics)",
                "Chapter 13. Generics");
        ch.add(lessonGenericBasics());
        ch.add(lessonBoundedTypes());
        ch.add(lessonWildcards());
        c.add(ch);
    }

    private static Lesson lessonGenericBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Generics: типобезпечність"));
        uk.add(LessonBlock.paragraph(
                "Generics дозволяють створювати класи/методи з параметрами типу. "
                + "Компілятор перевіряє типи на етапі компіляції — вам НЕ потрібно "
                + "писати приведення типу (cast)."));
        uk.add(LessonBlock.code(
                "// Без generics — ризик ClassCastException\n"
                + "class OldBox {\n"
                + "    Object value;\n"
                + "    void set(Object v) { value = v; }\n"
                + "    Object get() { return value; }\n"
                + "}\n"
                + "OldBox box = new OldBox();\n"
                + "box.set(\"Hello\");\n"
                + "String s = (String) box.get();  // cast — ризик!\n"
                + "Integer n = (Integer) box.get(); // ClassCastException!\n"
                + "\n"
                + "// З generics — безпечніше\n"
                + "class Box<T> {\n"
                + "    private T value;\n"
                + "    void set(T v) { value = v; }\n"
                + "    T get() { return value; }\n"
                + "}\n"
                + "\n"
                + "Box<String> sb = new Box<>();\n"
                + "sb.set(\"Hello\");\n"
                + "String s = sb.get();   // без кастингу!\n"
                + "// sb.set(42);         // ПОМИЛКА компіляції!\n"
                + "\n"
                + "Box<Integer> ib = new Box<>();\n"
                + "ib.set(42);\n"
                + "int n = ib.get();      // автобоксинг: Integer → int"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Generics: type safety"));
        en.add(LessonBlock.paragraph(
                "Generics let you create classes/methods with type parameters. "
                + "The compiler checks types at compile time — you DON'T need "
                + "to write casts."));
        en.add(LessonBlock.code(
                "// Without generics — ClassCastException risk\n"
                + "class OldBox {\n"
                + "    Object value;\n"
                + "    void set(Object v) { value = v; }\n"
                + "    Object get() { return value; }\n"
                + "}\n"
                + "OldBox box = new OldBox();\n"
                + "box.set(\"Hello\");\n"
                + "String s = (String) box.get();  // cast — risky!\n"
                + "Integer n = (Integer) box.get(); // ClassCastException!\n"
                + "\n"
                + "// With generics — safer\n"
                + "class Box<T> {\n"
                + "    private T value;\n"
                + "    void set(T v) { value = v; }\n"
                + "    T get() { return value; }\n"
                + "}\n"
                + "\n"
                + "Box<String> sb = new Box<>();\n"
                + "sb.set(\"Hello\");\n"
                + "String s = sb.get();   // no cast!\n"
                + "// sb.set(42);         // COMPILE ERROR!\n"
                + "\n"
                + "Box<Integer> ib = new Box<>();\n"
                + "ib.set(42);\n"
                + "int n = ib.get();      // autoboxing: Integer → int"));
        return new Lesson("13.1", "Generics основи", "Generics basics", uk, en);
    }

    private static Lesson lessonBoundedTypes() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Обмеження типів (bounds)"));
        uk.add(LessonBlock.code(
                "// T extends Comparable — T має бути Comparable\n"
                + "public static <T extends Comparable<T>> T max(T a, T b) {\n"
                + "    return a.compareTo(b) >= 0 ? a : b;\n"
                + "}\n"
                + "\n"
                + "max(3, 5);          // 5 (Integer implements Comparable)\n"
                + "max(\"a\", \"z\");      // \"z\"\n"
                + "// max(new Object(), new Object());  // помилка компіляції!\n"
                + "\n"
                + "// Множинні обмеження\n"
                + "public static <T extends Comparable<T> & Serializable> void save(T obj) {\n"
                + "    // T одночасно Comparable й Serializable\n"
                + "}"));
        uk.add(LessonBlock.note(
                "extends для generics означує «є підтипом» (а не тільки клас). "
                + "Для інтерфейсів можна вказати кілька через &: <T extends A & B>."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Type bounds"));
        en.add(LessonBlock.code(
                "// T extends Comparable — T must be Comparable\n"
                + "public static <T extends Comparable<T>> T max(T a, T b) {\n"
                + "    return a.compareTo(b) >= 0 ? a : b;\n"
                + "}\n"
                + "\n"
                + "max(3, 5);          // 5 (Integer implements Comparable)\n"
                + "max(\"a\", \"z\");      // \"z\"\n"
                + "// max(new Object(), new Object());  // compile error!\n"
                + "\n"
                + "// Multiple bounds\n"
                + "public static <T extends Comparable<T> & Serializable> void save(T obj) {\n"
                + "    // T is both Comparable and Serializable\n"
                + "}"));
        en.add(LessonBlock.note(
                "extends for generics means \"is a subtype of\" (not just class). "
                + "For interfaces you can specify multiple via &: <T extends A & B>."));
        return new Lesson("13.2", "Обмеження типів", "Type bounds", uk, en);
    }

    private static Lesson lessonWildcards() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Wildcards: ? extends та ? super"));
        uk.add(LessonBlock.code(
                "// ? extends Number — приймає List<Integer>, List<Double>...\n"
                + "double sum(List<? extends Number> list) {\n"
                + "    double total = 0;\n"
                + "    for (Number n : list) total += n.doubleValue();\n"
                + "    return total;\n"
                + "}\n"
                + "sum(Arrays.asList(1, 2, 3));      // 6.0\n"
                + "sum(Arrays.asList(1.5, 2.5));     // 4.0\n"
                + "// sum(Arrays.asList(\"a\"));       // помилка компіляції!\n"
                + "\n"
                + "// ? super Integer — приймає List<Integer>, List<Number>, List<Object>\n"
                + "void addNumbers(List<? super Integer> list) {\n"
                + "    list.add(1); list.add(2); list.add(3);\n"
                + "}\n"
                + "addNumbers(new ArrayList<Number>());  // OK\n"
                + "addNumbers(new ArrayList<Object>());  // OK"));
        uk.add(LessonBlock.heading("PECS: Producer Extends, Consumer Super"));
        uk.add(LessonBlock.paragraph(
                "Правило PECS (Effective Java, Joshua Bloch): "
                + "якщо структура ВИРОБЛЯЄ дані — use extends; "
                + "якщо СПОЖИВАЄ — use super."));
        uk.add(LessonBlock.warning(
                "Після запису через ? super компілятор не дозволить читати крім Object. "
                + "Після читання через ? extends не дозволить запис. Обирайте напрямок!"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Wildcards: ? extends and ? super"));
        en.add(LessonBlock.code(
                "// ? extends Number — accepts List<Integer>, List<Double>...\n"
                + "double sum(List<? extends Number> list) {\n"
                + "    double total = 0;\n"
                + "    for (Number n : list) total += n.doubleValue();\n"
                + "    return total;\n"
                + "}\n"
                + "sum(Arrays.asList(1, 2, 3));      // 6.0\n"
                + "sum(Arrays.asList(1.5, 2.5));     // 4.0\n"
                + "// sum(Arrays.asList(\"a\"));       // compile error!\n"
                + "\n"
                + "// ? super Integer — accepts List<Integer>, List<Number>, List<Object>\n"
                + "void addNumbers(List<? super Integer> list) {\n"
                + "    list.add(1); list.add(2); list.add(3);\n"
                + "}\n"
                + "addNumbers(new ArrayList<Number>());  // OK\n"
                + "addNumbers(new ArrayList<Object>());  // OK"));
        en.add(LessonBlock.heading("PECS: Producer Extends, Consumer Super"));
        en.add(LessonBlock.paragraph(
                "The PECS rule (Effective Java, Joshua Bloch): if a structure PRODUCES data — "
                + "use extends; if it CONSUMES — use super."));
        en.add(LessonBlock.warning(
                "After writing through ? super, the compiler won't allow reading except as Object. "
                + "After reading through ? extends, writing is not allowed. Choose your direction!"));
        return new Lesson("13.3", "Wildcards та PECS", "Wildcards & PECS", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 14. Перерахування (enum)
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter14(Course c) {
        Chapter ch = new Chapter("Глава 14. Перерахування enum",
                "Chapter 14. Enumerations");
        ch.add(lessonEnumBasics());
        ch.add(lessonEnumWithFields());
        c.add(ch);
    }

    private static Lesson lessonEnumBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("enum: обмежений набір значень"));
        uk.add(LessonBlock.paragraph(
                "enum — це клас, що має фіксований набір констант. "
                + "Набагато безпечніший за int-константи (неможливо створити \"випадкове\" "
                + "значення)."));
        uk.add(LessonBlock.code(
                "enum Day {\n"
                + "    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,\n"
                + "    SATURDAY, SUNDAY\n"
                + "}\n"
                + "\n"
                + "Day today = Day.WEDNESDAY;\n"
                + "\n"
                + "// switch — ідеально для enum\n"
                + "switch (today) {\n"
                + "    case SATURDAY: case SUNDAY:\n"
                + "        System.out.println(\"Вихідний!\"); break;\n"
                + "    default:\n"
                + "        System.out.println(\"Робочий день\");\n"
                + "}\n"
                + "\n"
                + "// Корисні методи\n"
                + "today.name();           // \"WEDNESDAY\" (рядок)\n"
                + "today.ordinal();        // 2 (порядковий номер від 0)\n"
                + "Day.valueOf(\"MONDAY\"); // enum з рядка\n"
                + "\n"
                + "// Перебір ВСІХ значень\n"
                + "for (Day d : Day.values()) {\n"
                + "    System.out.println(d.ordinal() + \": \" + d.name());\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("enum: limited set of values"));
        en.add(LessonBlock.paragraph(
                "An enum is a class with a fixed set of constants. "
                + "Much safer than int constants (impossible to create a \"random\" value)."));
        en.add(LessonBlock.code(
                "enum Day {\n"
                + "    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,\n"
                + "    SATURDAY, SUNDAY\n"
                + "}\n"
                + "\n"
                + "Day today = Day.WEDNESDAY;\n"
                + "\n"
                + "// switch — perfect for enums\n"
                + "switch (today) {\n"
                + "    case SATURDAY: case SUNDAY:\n"
                + "        System.out.println(\"Weekend!\"); break;\n"
                + "    default:\n"
                + "        System.out.println(\"Weekday\");\n"
                + "}\n"
                + "\n"
                + "// Useful methods\n"
                + "today.name();           // \"WEDNESDAY\" (string)\n"
                + "today.ordinal();        // 2 (ordinal number from 0)\n"
                + "Day.valueOf(\"MONDAY\"); // enum from string\n"
                + "\n"
                + "// Iterate ALL values\n"
                + "for (Day d : Day.values()) {\n"
                + "    System.out.println(d.ordinal() + \": \" + d.name());\n"
                + "}"));
        return new Lesson("14.1", "enum основи", "enum basics", uk, en);
    }

    private static Lesson lessonEnumWithFields() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("enum з полями та методами"));
        uk.add(LessonBlock.code(
                "enum Season {\n"
                + "    WINTER(\"зима\", -5),\n"
                + "    SPRING(\"весна\", 15),\n"
                + "    SUMMER(\"літо\", 30),\n"
                + "    AUTUMN(\"осінь\", 10);\n"
                + "\n"
                + "    private final String nameUk;\n"
                + "    private final int avgTemp;\n"
                + "\n"
                + "    Season(String nameUk, int avgTemp) {\n"
                + "        this.nameUk = nameUk;\n"
                + "        this.avgTemp = avgTemp;\n"
                + "    }\n"
                + "\n"
                + "    public String getNameUk() { return nameUk; }\n"
                + "    public int getAvgTemp() { return avgTemp; }\n"
                + "\n"
                + "    public boolean isCold() { return avgTemp < 0; }\n"
                + "}\n"
                + "\n"
                + "for (Season s : Season.values()) {\n"
                + "    System.out.println(s.getNameUk() + \": \" + s.getAvgTemp() + \"°C\"\n"
                + "        + (s.isCold() ? \" (холодно!)\" : \"\"));\n"
                + "}\n"
                + "// зима: -5°C (холодно!)\n"
                + "// весна: 15°C\n"
                + "// літо: 30°C\n"
                + "// осінь: 10°C"));
        uk.add(LessonBlock.note(
                "enum може реалізовувати інтерфейси (але не наслідувати класи — "
                + "всі enum наслідують java.lang.Enum). enum не можна створити "
                + "через new — конструктор викликається автоматично."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("enum with fields and methods"));
        en.add(LessonBlock.code(
                "enum Season {\n"
                + "    WINTER(\"winter\", -5),\n"
                + "    SPRING(\"spring\", 15),\n"
                + "    SUMMER(\"summer\", 30),\n"
                + "    AUTUMN(\"autumn\", 10);\n"
                + "\n"
                + "    private final String name;\n"
                + "    private final int avgTemp;\n"
                + "\n"
                + "    Season(String name, int avgTemp) {\n"
                + "        this.name = name;\n"
                + "        this.avgTemp = avgTemp;\n"
                + "    }\n"
                + "\n"
                + "    public String getName() { return name; }\n"
                + "    public int getAvgTemp() { return avgTemp; }\n"
                + "\n"
                + "    public boolean isCold() { return avgTemp < 0; }\n"
                + "}\n"
                + "\n"
                + "for (Season s : Season.values()) {\n"
                + "    System.out.println(s.getName() + \": \" + s.getAvgTemp() + \"°C\"\n"
                + "        + (s.isCold() ? \" (cold!)\" : \"\"));\n"
                + "}\n"
                + "// winter: -5°C (cold!)\n"
                + "// spring: 15°C\n"
                + "// summer: 30°C\n"
                + "// autumn: 10°C"));
        en.add(LessonBlock.note(
                "An enum can implement interfaces (but not extend classes — all enums "
                + "extend java.lang.Enum). An enum cannot be created via new — "
                + "the constructor is called automatically."));
        return new Lesson("14.2", "enum з полями", "enum with fields", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 15. Анотації
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter15(Course c) {
        Chapter ch = new Chapter("Глава 15. Анотації", "Chapter 15. Annotations");
        ch.add(lessonStandardAnnotations());
        ch.add(lessonCustomAnnotations());
        c.add(ch);
    }

    private static Lesson lessonStandardAnnotations() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Стандартні анотації"));
        uk.add(LessonBlock.paragraph(
                "Анотація — метадані над класом/методом/полем. Починаються з @. "
                + "Компілятор або фреймворк читають їх та діють відповідно."));
        uk.add(LessonBlock.code(
                "// @Override — «я перевизначаю метод батьківського класу»\n"
                + "class Animal {\n"
                + "    String sound() { return \"...\"; }\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    @Override\n"
                + "    String sound() { return \"Гав!\"; }  // компілятор перевірить правильність\n"
                + "    // без @Override помилка підписання НЕ помітиться!\n"
                + "}\n"
                + "\n"
                + "// @Deprecated — «цей метод застарів, не використовуйте»\n"
                + "@Deprecated\n"
                + "void oldMethod() { }\n"
                + "\n"
                + "// @SuppressWarnings — «приглушити попередження компілятора»\n"
                + "@SuppressWarnings(\"unchecked\")\n"
                + "List<String> list = (List<String>) rawList;\n"
                + "\n"
                + "// @Override + @Override = помилка (анотацію не можна використати двічі)\n"
                + "// @FunctionalInterface — «цей інтерфейс має бути з одним методом»"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Standard annotations"));
        en.add(LessonBlock.paragraph(
                "An annotation is metadata over a class/method/field. Starts with @. "
                + "The compiler or framework reads it and acts accordingly."));
        en.add(LessonBlock.code(
                "// @Override — \"I'm overriding a parent class method\"\n"
                + "class Animal {\n"
                + "    String sound() { return \"...\"; }\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    @Override\n"
                + "    String sound() { return \"Woof!\"; }  // compiler checks correctness\n"
                + "    // without @Override, signature mistakes won't be caught!\n"
                + "}\n"
                + "\n"
                + "// @Deprecated — \"this method is outdated, don't use it\"\n"
                + "@Deprecated\n"
                + "void oldMethod() { }\n"
                + "\n"
                + "// @SuppressWarnings — \"suppress compiler warnings\"\n"
                + "@SuppressWarnings(\"unchecked\")\n"
                + "List<String> list = (List<String>) rawList;\n"
                + "\n"
                + "// @FunctionalInterface — \"this interface must have one method\""));
        return new Lesson("15.1", "Стандартні анотації", "Standard annotations", uk, en);
    }

    private static Lesson lessonCustomAnnotations() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Власні анотації та рефлексія"));
        uk.add(LessonBlock.code(
                "// Оголошення власної анотації\n"
                + "@Retention(RetentionPolicy.RUNTIME)  // доступна в рантаймі\n"
                + "@Target(ElementType.METHOD)            // тільки для методів\n"
                + "@interface LogExecutionTime { }\n"
                + "\n"
                + "// Використання\n"
                + "class Service {\n"
                + "    @LogExecutionTime\n"
                + "    void processData() {\n"
                + "        // ... довга операція\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Читання анотації через рефлексію\n"
                + "Method m = Service.class.getMethod(\"processData\");\n"
                + "if (m.isAnnotationPresent(LogExecutionTime.class)) {\n"
                + "    long start = System.nanoTime();\n"
                + "    m.invoke(serviceInstance);\n"
                + "    long elapsed = System.nanoTime() - start;\n"
                + "    System.out.println(\"Execution: \" + elapsed + \" ns\");\n"
                + "}"));
        uk.add(LessonBlock.list(
                "@Retention(RUNTIME) — зберігається під час виконання (рефлексія)",
                "@Retention(CLASS) — у .class файлі, але не в рантаймі (за замовчуванням)",
                "@Retention(SOURCE) — тільки під час компіляції (@Override)",
                "@Target(METHOD) — анотація може бути тільки над методом",
                "@Target(TYPE) — над класом/інтерфейсом/enum",
                "@Target(FIELD) — над полем"));
        uk.add(LessonBlock.note(
                "Рефлексія (Class, Method, Field) дозволяє аналізувати код в рантаймі. "
                + "Саме на ній побудовані Spring, Jackson, JUnit та інші фреймворки."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Custom annotations and reflection"));
        en.add(LessonBlock.code(
                "// Declare a custom annotation\n"
                + "@Retention(RetentionPolicy.RUNTIME)  // available at runtime\n"
                + "@Target(ElementType.METHOD)            // only for methods\n"
                + "@interface LogExecutionTime { }\n"
                + "\n"
                + "// Usage\n"
                + "class Service {\n"
                + "    @LogExecutionTime\n"
                + "    void processData() {\n"
                + "        // ... long operation\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Read annotation via reflection\n"
                + "Method m = Service.class.getMethod(\"processData\");\n"
                + "if (m.isAnnotationPresent(LogExecutionTime.class)) {\n"
                + "    long start = System.nanoTime();\n"
                + "    m.invoke(serviceInstance);\n"
                + "    long elapsed = System.nanoTime() - start;\n"
                + "    System.out.println(\"Execution: \" + elapsed + \" ns\");\n"
                + "}"));
        en.add(LessonBlock.list(
                "@Retention(RUNTIME) — kept during execution (reflection)",
                "@Retention(CLASS) — in .class file, but not at runtime (default)",
                "@Retention(SOURCE) — only during compilation (@Override)",
                "@Target(METHOD) — annotation can only be on a method",
                "@Target(TYPE) — on a class/interface/enum",
                "@Target(FIELD) — on a field"));
        en.add(LessonBlock.note(
                "Reflection (Class, Method, Field) allows analyzing code at runtime. "
                + "Spring, Jackson, JUnit and other frameworks are built on it."));
        return new Lesson("15.2", "Власні анотації", "Custom annotations", uk, en);
    }
}
