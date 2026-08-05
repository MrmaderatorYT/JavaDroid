package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Глави 3-15 курсу Java Complete Reference 9th.
 * Кожна глава має кілька детальних двомовних уроків з прикладами для початківців.
 */
final class JrcChapters03to15 {

    static void add(Course s) {
        addChapter03(s);
        addChapter04(s);
        addChapter05(s);
        addChapter06(s);
        addChapter07(s);
        addChapter08(s);
        addChapter09(s);
        addChapter10(s);
        addChapter11(s);
        addChapter12(s);
        addChapter13(s);
        addChapter14(s);
        addChapter15(s);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава 3. Класи та об'єкти
    // ═══════════════════════════════════════════════════════════════

    private static void addChapter03(Course s) {
        Chapter ch = new Chapter("Глава 3. Класи та об'єкти", "Chapter 3. Classes and objects");
        ch.add(materialClasses());
        ch.add(materialMethods());
        ch.add(materialConstructors());
        ch.add(materialThisAndStatic());
        s.add(ch);
    }

    private static Lesson materialClasses() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Класи та об'єкти"));
        uk.add(LessonBlock.paragraph(
                "Клас — це шаблон (наче креслення), за яким створюються об'єкти. "
                + "Уявіть клас як архітектурний план будинку, а об'єкт — це конкретний будинок, "
                + "побудований за цим планом. Один план (клас) дозволяє побудувати "
                + "цілу вулицю однакових будинків (об'єктів), але кожен з них може бути "
                + "пофарбований у свій колір і мати своїх мешканців."));
        uk.add(LessonBlock.paragraph(
                "Клас об'єднує стан (поля — «що клас знає про себе») та поведінку "
                + "(методи — «що клас може робити»). Об'єкт (або екземпляр класу) "
                + "створюється за допомогою ключового слова new, яке виділяє пам'ять для нового об'єкта."));
        uk.add(LessonBlock.code(
                "class Person {\n"
                + "    // Поля (стан) — кожен об'єкт має СВОЇ власні копії цих змінних\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    // Метод (поведінка) — інструкція, спільна для всіх об'єктів,\n"
                + "    // але працює з даними конкретного об'єкта, який її викликав\n"
                + "    void sayHello() {\n"
                + "        System.out.println(\"Привіт, я \" + name + \", мені \" + age);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Створюємо два різних об'єкти з одного класу\n"
                + "Person ivan = new Person();\n"
                + "ivan.name = \"Іван\"; // Задаємо стан першого об'єкта\n"
                + "ivan.age = 25;\n"
                + "\n"
                + "Person olena = new Person();\n"
                + "olena.name = \"Олена\"; // Задаємо стан другого об'єкта\n"
                + "olena.age = 30;\n"
                + "\n"
                + "ivan.sayHello();   // Виведе: Привіт, я Іван, мені 25\n"
                + "olena.sayHello();  // Виведе: Привіт, я Олена, мені 30"));
        uk.add(LessonBlock.paragraph(
                "У наведеному коді ми створили клас Person. Коли ми пишемо new Person(), "
                + "в пам'яті виділяється місце для зберігання імені та віку. Змінна ivan "
                + "просто вказує (посилається) на це місце в пам'яті. Змінна olena вказує на "
                + "інше місце. Тому їхні дані не перетинаються."));
        uk.add(LessonBlock.heading("Типи даних у Java"));
        uk.add(LessonBlock.paragraph(
                "Java є строго типізованою мовою. Це означає, що кожна змінна повинна мати "
                + "заздалегідь визначений тип. Типи поділяються на примітивні (базові числа, "
                + "символи) та посилальні (об'єкти, масиви)."));
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
                "Локальні змінні посилального типу (наприклад, String всередині методу) "
                + "за замовчуванням НЕ ініціалізуються — компілятор видасть помилку, якщо "
                + "спробувати їх використати. Натомість, поля класу ініціалізуються "
                + "автоматично: 0 для чисел, false для boolean, null для посилань."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть клас Car, який має два поля: марка (brand типу String) та "
                + "рік випуску (year типу int). Додайте метод startEngine(), який виводить "
                + "на екран повідомлення виду: 'Двигун авто Toyota (2020) запущено'. "
                + "Потім створіть два різні об'єкти цього класу і викличте для них цей метод."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Car {\n"
                + "    String brand;\n"
                + "    int year;\n"
                + "\n"
                + "    void startEngine() {\n"
                + "        System.out.println(\"Двигун авто \" + brand + \" (\" + year + \") запущено\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// В іншому методі (наприклад, main):\n"
                + "Car myCar = new Car();\n"
                + "myCar.brand = \"Toyota\";\n"
                + "myCar.year = 2020;\n"
                + "\n"
                + "Car yourCar = new Car();\n"
                + "yourCar.brand = \"BMW\";\n"
                + "yourCar.year = 2022;\n"
                + "\n"
                + "myCar.startEngine();\n"
                + "yourCar.startEngine();"));
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

    private static Lesson materialMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Методи: як класи \"діють\""));
        uk.add(LessonBlock.paragraph(
                "Метод — це іменований блок коду, який виконує певну дію. Він допомагає уникнути "
                + "дублювання коду, групуючи інструкції. Метод може приймати "
                + "параметри (вхідні дані), обробляти їх та повертати результат. "
                + "Якщо метод не повертає нічого, використовується ключове слово void."));
        uk.add(LessonBlock.code(
                "class Calc {\n"
                + "    // Метод з параметрами й результатом\n"
                + "    int add(int a, int b) {\n"
                + "        return a + b; // Ключове слово return повертає результат\n"
                + "    }\n"
                + "\n"
                + "    // Метод без результату (void)\n"
                + "    void printResult(int a, int b) {\n"
                + "        // Викликаємо інший метод add всередині printResult\n"
                + "        System.out.println(a + \" + \" + b + \" = \" + add(a, b));\n"
                + "    }\n"
                + "\n"
                + "    // Перевантаження (overloading) — одне ім'я, різні параметри\n"
                + "    // Java розрізняє їх за типами переданих аргументів\n"
                + "    double add(double a, double b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    // Статичний метод — викликається через назву класу, не потребує об'єкта\n"
                + "    static int square(int x) { return x * x; }\n"
                + "}\n"
                + "\n"
                + "Calc c = new Calc();\n"
                + "System.out.println(c.add(2, 3));       // Виведе: 5 (викличеться int-версія)\n"
                + "System.out.println(c.add(2.5, 3.5));   // Виведе: 6.0 (викличеться double-версія)\n"
                + "System.out.println(Calc.square(4));    // Виведе: 16"));
        uk.add(LessonBlock.paragraph(
                "Як ви помітили, метод add зустрічається двічі. Це називається перевантаженням "
                + "(overloading). Компілятор автоматично визначає, який метод викликати, "
                + "залежно від того, які типи даних ви туди передаєте."));
        uk.add(LessonBlock.heading("Передавання параметрів"));
        uk.add(LessonBlock.paragraph(
                "У Java параметри завжди передаються за ЗНАЧЕННЯМ (pass-by-value). "
                + "Для примітивних типів (int, boolean тощо) створюється повна копія значення. "
                + "Для об'єктів — копіюється ПОСИЛАННЯ. Тобто, якщо ви зміните сам об'єкт "
                + "всередині методу, ці зміни будуть видимі ззовні, але ви не зможете "
                + "змусити оригінальне посилання вказувати на інший об'єкт."));
        uk.add(LessonBlock.code(
                "class Box { int value; }\n"
                + "\n"
                + "void changePrimitive(int x) {\n"
                + "    x = 999;  // Змінює лише ЛОКАЛЬНУ копію, оригінал не постраждає\n"
                + "}\n"
                + "\n"
                + "void changeObject(Box b) {\n"
                + "    b.value = 999;  // Змінює ДАНІ ОБ'ЄКТА за посиланням\n"
                + "    b = null;       // Обнулює лише локальну копію посилання!\n"
                + "}\n"
                + "\n"
                + "int num = 10;\n"
                + "changePrimitive(num);\n"
                + "System.out.println(num);  // 10 (не змінилося!)\n"
                + "\n"
                + "Box box = new Box();\n"
                + "box.value = 42;\n"
                + "changeObject(box);\n"
                + "System.out.println(box.value);  // 999 (змінилося, бо це той самий об'єкт!)"));
        uk.add(LessonBlock.note(
                "Порада: щоб \"повернути\" кілька значень з методу, створіть клас-контейнер "
                + "або використайте масив. Для JDK 8 це найзрозуміліший і найсумісніший варіант."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Напишіть клас Rectangle з полями width та height. Додайте два методи: "
                + "1) getArea(), який повертає площу (ширина * висота), та 2) isSquare(), "
                + "який повертає true, якщо ширина дорівнює висоті, і false інакше."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Rectangle {\n"
                + "    int width;\n"
                + "    int height;\n"
                + "\n"
                + "    int getArea() {\n"
                + "        return width * height;\n"
                + "    }\n"
                + "\n"
                + "    boolean isSquare() {\n"
                + "        return width == height;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "Rectangle rect = new Rectangle();\n"
                + "rect.width = 10;\n"
                + "rect.height = 10;\n"
                + "System.out.println(\"Площа: \" + rect.getArea()); // 100\n"
                + "System.out.println(\"Квадрат? \" + rect.isSquare()); // true"));
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
                + "System.out.println(s.add(2, 3));       // 5\n"
                + "System.out.println(s.add(2.5, 3.5));   // 6.0\n"
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

    private static Lesson materialConstructors() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Конструктори: створення об'єктів"));
        uk.add(LessonBlock.paragraph(
                "Конструктор — це спеціальний метод, який викликається автоматично "
                + "під час створення об'єкта за допомогою ключового слова new. "
                + "Він має таке ж ім'я, що й сам клас, і, на відміну від звичайних методів, "
                + "НЕ повертає жодного значення (навіть void)."));
        uk.add(LessonBlock.paragraph(
                "Конструктори використовуються для ініціалізації об'єкта, тобто для "
                + "надання початкових значень його полям перед тим, як об'єкт почне використовуватися."));
        uk.add(LessonBlock.code(
                "class Point {\n"
                + "    int x, y;\n"
                + "\n"
                + "    // Конструктор за замовчуванням (без параметрів)\n"
                + "    Point() {\n"
                + "        this(0, 0);   // Виклик іншого конструктора через this\n"
                + "    }\n"
                + "\n"
                + "    // Конструктор з параметрами\n"
                + "    Point(int x, int y) {\n"
                + "        this.x = x;   // this допомагає розрізнити поле класу і параметр методу\n"
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
                + "Point a = new Point();      // Викличе конструктор без параметрів, результат: (0, 0)\n"
                + "Point b = new Point(3, 4);  // Викличе конструктор з параметрами, результат: (3, 4)\n"
                + "Point c = new Point(b);     // Створить копію об'єкта b, результат: (3, 4)"));
        uk.add(LessonBlock.note(
                "Важливе правило: Якщо ви не оголосили ЖОДНОГО конструктора у вашому класі, "
                + "компілятор автоматично згенерує порожній \"конструктор за замовчуванням\". "
                + "Але якщо ви додали хоча б один власний конструктор (наприклад, з параметрами), "
                + "автоматичний конструктор зникає. Тому, якщо він вам потрібен, доведеться "
                + "оголосити його явно."));
        uk.add(LessonBlock.heading("Приватні поля + геттери/сеттери (інкапсуляція)"));
        uk.add(LessonBlock.paragraph(
                "Інкапсуляція — це приховування внутрішнього стану об'єкта від прямого втручання. "
                + "Замість того, щоб залишати поля публічними (public), ми робимо їх приватними (private), "
                + "а доступ до них надаємо через спеціальні методи — геттери (для читання) "
                + "та сеттери (для запису). Це дозволяє нам контролювати, які дані записуються в об'єкт."));
        uk.add(LessonBlock.code(
                "class BankAccount {\n"
                + "    private double balance;   // Приховане поле (доступне тільки всередині класу)\n"
                + "    private String owner;\n"
                + "\n"
                + "    BankAccount(String owner, double initial) {\n"
                + "        this.owner = owner;\n"
                + "        // Не дозволяємо створити рахунок з від'ємним балансом\n"
                + "        this.balance = Math.max(0, initial);\n"
                + "    }\n"
                + "\n"
                + "    // Геттери — для безпечного читання\n"
                + "    public double getBalance() { return balance; }\n"
                + "    public String getOwner() { return owner; }\n"
                + "\n"
                + "    // Бізнес-метод з валідацією (замість прямого сеттера)\n"
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
                + "acc.withdraw(300);      // Успішно, balance стає 700\n"
                + "acc.withdraw(5000);     // Помилка: Недостатньо коштів!\n"
                + "acc.deposit(-100);      // Ігнорується, баланс не змінюється\n"
                + "System.out.println(acc.getBalance());  // 700"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть клас Book з приватними полями title (назва), author (автор) "
                + "та pages (кількість сторінок). Додайте конструктор для ініціалізації "
                + "всіх трьох полів. Додайте геттери для всіх полів. Напишіть метод "
                + "printInfo(), який виводить інформацію про книгу. "
                + "У разі спроби передати в конструктор від'ємну кількість сторінок, встановлюйте 1."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Book {\n"
                + "    private String title;\n"
                + "    private String author;\n"
                + "    private int pages;\n"
                + "\n"
                + "    Book(String title, String author, int pages) {\n"
                + "        this.title = title;\n"
                + "        this.author = author;\n"
                + "        if (pages > 0) {\n"
                + "            this.pages = pages;\n"
                + "        } else {\n"
                + "            this.pages = 1; // Захист від некоректних даних\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public String getTitle() { return title; }\n"
                + "    public String getAuthor() { return author; }\n"
                + "    public int getPages() { return pages; }\n"
                + "\n"
                + "    public void printInfo() {\n"
                + "        System.out.println(\"Книга: \" + title + \", Автор: \" + author + \", Сторінок: \" + pages);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Book b = new Book(\"Java\", \"Schildt\", -50);\n"
                + "b.printInfo(); // Виведе кількість сторінок: 1"));
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

    private static Lesson materialThisAndStatic() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("this та static"));
        uk.add(LessonBlock.paragraph(
                "У Java є два важливих ключових слова, які визначають, до чого належить змінна чи метод. "
                + "Ключове слово this завжди вказує на поточний екземпляр об'єкта (на «себе»). "
                + "Ключове слово static вказує, що поле або метод належить самому КЛАСУ, а не його "
                + "окремим об'єктам. Статичні елементи є спільними для всіх об'єктів цього класу."));
        uk.add(LessonBlock.code(
                "class Config {\n"
                + "    private String name;            // Звичайне поле (у кожного об'єкта своє)\n"
                + "    private static int count = 0;   // Статичне поле (ОДНЕ спільне на всіх)\n"
                + "\n"
                + "    Config(String name) {\n"
                + "        this.name = name;   // this.name — поле об'єкта, name — параметр методу\n"
                + "        count++;            // Збільшуємо спільний лічильник при кожному створенні\n"
                + "    }\n"
                + "\n"
                + "    // Статичний метод. Не має доступу до this, бо він викликається від імені класу\n"
                + "    static int getCount() { return count; }\n"
                + "    \n"
                + "    String getName() { return name; }\n"
                + "}\n"
                + "\n"
                + "new Config(\"A\");\n"
                + "new Config(\"B\");\n"
                + "new Config(\"C\");\n"
                + "// Звертаємося до статичного методу через назву класу, а не через об'єкт\n"
                + "System.out.println(Config.getCount());  // Виведе: 3"));
        uk.add(LessonBlock.note(
                "Пам'ятайте: static метод не може використовувати this або звертатися "
                + "до нестатичних полів напряму, оскільки він не знає, до якого саме "
                + "екземпляра об'єкта вони належать. Він може працювати лише зі static даними."));
        uk.add(LessonBlock.heading("final — незмінність"));
        uk.add(LessonBlock.paragraph(
                "Ключове слово final використовується для створення констант, або сутностей, "
                + "які не можуть бути змінені після ініціалізації. Якщо це змінна — її значення "
                + "фіксується. Якщо це клас — від нього не можна успадковуватися. "
                + "Якщо метод — його не можна перевизначити (override)."));
        uk.add(LessonBlock.code(
                "final int MAX = 100;      // константа (заведено писати великими літерами)\n"
                + "// MAX = 200;             // помилка компіляції!\n"
                + "\n"
                + "class ImmutablePoint {\n"
                + "    final int x, y;   // Задається тільки один раз у конструкторі\n"
                + "    \n"
                + "    ImmutablePoint(int x, int y) { \n"
                + "        this.x = x; \n"
                + "        this.y = y; \n"
                + "    }\n"
                + "    // Оскільки поля final і немає сеттерів, об'єкт є повністю незмінним (immutable)\n"
                + "}\n"
                + "\n"
                + "final class MathHelper { }   // final клас не можна успадкувати (extends)"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть клас Counter. Додайте в нього статичне поле globalCount і "
                + "нестатичне поле localCount (обидва типу int, початкове значення 0). "
                + "В конструкторі класу збільшуйте обидва поля на 1. "
                + "Створіть 3 об'єкти класу Counter. Після цього виведіть значення "
                + "localCount одного з об'єктів, та значення globalCount."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Counter {\n"
                + "    static int globalCount = 0;\n"
                + "    int localCount = 0;\n"
                + "\n"
                + "    Counter() {\n"
                + "        globalCount++;\n"
                + "        localCount++;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "Counter c1 = new Counter();\n"
                + "Counter c2 = new Counter();\n"
                + "Counter c3 = new Counter();\n"
                + "\n"
                + "System.out.println(\"Local: \" + c3.localCount);   // Виведе: 1 (свій лічильник)\n"
                + "System.out.println(\"Global: \" + Counter.globalCount); // Виведе: 3 (спільний)"));
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

    private static void addChapter04(Course s) {
        Chapter ch = new Chapter("Глава 4. Успадкування та поліморфізм",
                "Chapter 4. Inheritance and polymorphism");
        ch.add(materialInheritance());
        ch.add(materialPolymorphism());
        ch.add(materialAbstractClasses());
        ch.add(materialObjectMethods());
        s.add(ch);
    }

    private static Lesson materialInheritance() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Успадкування: extends"));
        uk.add(LessonBlock.paragraph(
                "Успадкування — це механізм, який дозволяє створювати новий клас на основі вже існуючого. "
                + "Новий клас (нащадок або дочірній) автоматично отримує всі публічні та захищені (protected) "
                + "поля і методи свого батька (суперкласу). Це дозволяє уникнути дублювання коду."));
        uk.add(LessonBlock.paragraph(
                "Наприклад, якщо у нас є загальний клас Animal, ми можемо створити класи Dog та Cat, "
                + "які успадкують його характеристики, але додадуть свої специфічні особливості."));
        uk.add(LessonBlock.code(
                "class Animal {\n"
                + "    String name;\n"
                + "\n"
                + "    Animal(String name) { \n"
                + "        this.name = name; \n"
                + "    }\n"
                + "\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" їсть\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Dog extends Animal {\n"
                + "    private String breed;\n"
                + "\n"
                + "    Dog(String name, String breed) {\n"
                + "        super(name);       // Виклик конструктора батьківського класу\n"
                + "        this.breed = breed;\n"
                + "    }\n"
                + "\n"
                + "    void bark() {\n"
                + "        System.out.println(name + \" гавкає!\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    void eat() {\n"
                + "        System.out.println(name + \" гризе кістку\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Dog myDog = new Dog(\"Рекс\", \"Вівчарка\");\n"
                + "myDog.eat();  // Викличе перевизначений метод (гризе кістку)\n"
                + "myDog.bark(); // Викличе власний метод"));
        uk.add(LessonBlock.heading("Ключове слово super"));
        uk.add(LessonBlock.paragraph(
                "Слово super використовується для звернення до елементів батьківського класу зсередини нащадка. "
                + "Воно має три основні застосування:"));
        uk.add(LessonBlock.list(
                "super() — виклик конструктора батька. Це має бути ПЕРШИЙ рядок у конструкторі нащадка.",
                "super.метод() — виклик методу батька. Дуже корисно, коли ви перевизначаєте метод, але хочете зберегти стару логіку.",
                "super.поле — доступ до прихованого поля батька (якщо нащадок оголосив поле з таким самим ім'ям)."));
        uk.add(LessonBlock.warning(
                "Важливе правило: Якщо ви не викликаєте super() явно, Java автоматично вставляє виклик super() "
                + "без аргументів. Але якщо батьківський клас має лише конструктори з параметрами і не має "
                + "конструктора за замовчуванням, програма не скомпілюється!"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть базовий клас Vehicle з полем brand (марка) та методом start(), який виводить "
                + "назву марки та слово \"заводиться\". Створіть клас-нащадок Car, який додає поле "
                + "doors (кількість дверей). У класі Car перевизначте метод start() так, щоб він спочатку "
                + "викликав батьківський метод, а потім виводив \"У машині X дверей\"."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Vehicle {\n"
                + "    String brand;\n"
                + "    Vehicle(String brand) {\n"
                + "        this.brand = brand;\n"
                + "    }\n"
                + "    void start() {\n"
                + "        System.out.println(brand + \" заводиться.\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Car extends Vehicle {\n"
                + "    int doors;\n"
                + "    Car(String brand, int doors) {\n"
                + "        super(brand); // Виклик конструктора Vehicle\n"
                + "        this.doors = doors;\n"
                + "    }\n"
                + "    @Override\n"
                + "    void start() {\n"
                + "        super.start(); // Виклик методу Vehicle.start()\n"
                + "        System.out.println(\"У машині \" + doors + \" дверей.\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Car c = new Car(\"Toyota\", 4);\n"
                + "c.start();"));
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

    private static Lesson materialPolymorphism() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Поліморфізм: один тип — багато форм"));
        uk.add(LessonBlock.paragraph(
                "Поліморфізм (від грецького «багато форм») — це здатність об'єкта поводитись "
                + "як об'єкт свого батьківського типу, але при цьому зберігати власну реалізацію методів. "
                + "Це називається динамічною диспетчеризацією."));
        uk.add(LessonBlock.paragraph(
                "Завдяки поліморфізму ми можемо створити масив типу Animal, додати туди "
                + "котів і собак, і в циклі викликати метод eat() для кожного. Кожна тварина "
                + "їстиме по-своєму, хоча тип змінної у нас загальний."));
        uk.add(LessonBlock.code(
                "// Використовуємо класи Animal, Dog, Cat\n"
                + "Animal a1 = new Dog(\"Рекс\", \"лабрадор\");\n"
                + "Animal a2 = new Cat(\"Мурчик\");\n"
                + "\n"
                + "// Кожен викликає СВІЙ метод eat()\n"
                + "a1.eat();  // Рекс гризе кістку (Dog.eat)\n"
                + "a2.eat();  // Мурчик лизькає молоко (Cat.eat)\n"
                + "\n"
                + "// a1.bark();  // ПОМИЛКА! Animal не знає методу bark()\n"
                + "// Щоб викликати bark, треба привести тип назад до Dog:\n"
                + "Dog dog = (Dog) a1;\n"
                + "dog.bark();  // Працює успішно"));
        uk.add(LessonBlock.heading("instanceof — безпечне приведення типів"));
        uk.add(LessonBlock.paragraph(
                "Коли ми працюємо зі змінною батьківського типу, ми можемо перевірити, "
                + "яким саме об'єктом вона є насправді, використовуючи оператор instanceof."));
        uk.add(LessonBlock.code(
                "Animal a = new Dog(\"Рекс\", \"лабрадор\");\n"
                + "\n"
                + "if (a instanceof Dog) {\n"
                + "    Dog d = (Dog) a;      // безпечне приведення типу\n"
                + "    d.bark();\n"
                + "}\n"
                + "\n"
                + "// З Java 16 можна писати коротше (Pattern Matching):\n"
                + "if (a instanceof Dog d) {\n"
                + "    d.bark();\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Пам'ятайте: поліморфізм працює лише для методів. Він НЕ працює для полів. "
                + "Якщо ви звертаєтеся до поля a.name, то буде використано поле класу Animal, "
                + "а не Dog. Тому поля завжди краще робити private."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть масив типу Vehicle з двох об'єктів: Car (створений раніше) та Motorcycle. "
                + "Клас Motorcycle повинен наслідувати Vehicle та перевизначати метод start(), "
                + "щоб він виводив \"Мотоцикл заводиться швидко\". Пройдіться по масиву "
                + "циклом і викличте start() для кожного елемента."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Motorcycle extends Vehicle {\n"
                + "    Motorcycle(String brand) {\n"
                + "        super(brand);\n"
                + "    }\n"
                + "    @Override\n"
                + "    void start() {\n"
                + "        System.out.println(brand + \" заводиться швидко.\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Vehicle[] fleet = new Vehicle[2];\n"
                + "fleet[0] = new Car(\"Toyota\", 4);\n"
                + "fleet[1] = new Motorcycle(\"Yamaha\");\n"
                + "\n"
                + "for (Vehicle v : fleet) {\n"
                + "    v.start(); // Для машини викличеться Car.start(), для мотоцикла Motorcycle.start()\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Polymorphism: one type, many forms"));
        en.add(LessonBlock.paragraph(
                "Polymorphism (from Greek \"many forms\") — an object can be used as its "
                + "parent type, but ITS OWN method will be called (dynamic dispatch)."));
        en.add(LessonBlock.code(
                "Animal a1 = new Dog(\"Rex\", \"labrador\");\n"
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

    private static Lesson materialAbstractClasses() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Абстрактні класи"));
        uk.add(LessonBlock.paragraph(
                "Іноді батьківський клас служить лише як концепція, базовий шаблон, "
                + "а не як конкретна сутність, яку варто створювати. Для цього існують "
                + "абстрактні класи. Їх не можна створити напряму (за допомогою new)."));
        uk.add(LessonBlock.paragraph(
                "Абстрактний клас може містити абстрактні методи — методи без тіла (лише сигнатура). "
                + "Всі класи-нащадки ЗОБОВ'ЯЗАНІ реалізувати ці методи (або самі стати абстрактними). "
                + "Абстрактний клас також може мати звичайні методи та поля."));
        uk.add(LessonBlock.code(
                "abstract class Shape {\n"
                + "    String color;\n"
                + "\n"
                + "    Shape(String color) { this.color = color; }\n"
                + "\n"
                + "    // Абстрактні методи (лише оголошення)\n"
                + "    abstract double area();\n"
                + "    abstract double perimeter();\n"
                + "\n"
                + "    // Звичайний метод з реалізацією\n"
                + "    void printInfo() {\n"
                + "        System.out.println(\"Колір фігури: \" + color);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Circle extends Shape {\n"
                + "    double radius;\n"
                + "    Circle(String color, double r) {\n"
                + "        super(color);\n"
                + "        this.radius = r;\n"
                + "    }\n"
                + "\n"
                + "    // Ми зобов'язані реалізувати ці методи:\n"
                + "    @Override \n"
                + "    double area() { return Math.PI * radius * radius; }\n"
                + "    \n"
                + "    @Override \n"
                + "    double perimeter() { return 2 * Math.PI * radius; }\n"
                + "}\n"
                + "\n"
                + "// Shape s = new Shape(\"чорний\"); // ПОМИЛКА: не можна створити екземпляр абстрактного класу\n"
                + "Shape c = new Circle(\"червоний\", 5);\n"
                + "c.printInfo();"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть абстрактний клас Employee з полем name та абстрактним методом calculateSalary(). "
                + "Створіть два класи-нащадки: FullTimeEmployee, який має поле monthlySalary (його і повертає), "
                + "та ContractEmployee, який має поля hourlyRate та hoursWorked (повертає їх добуток). "
                + "Створіть масив Employee та виведіть зарплату кожного."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "abstract class Employee {\n"
                + "    String name;\n"
                + "    Employee(String name) { this.name = name; }\n"
                + "    abstract double calculateSalary();\n"
                + "}\n"
                + "\n"
                + "class FullTimeEmployee extends Employee {\n"
                + "    double monthlySalary;\n"
                + "    FullTimeEmployee(String name, double salary) {\n"
                + "        super(name);\n"
                + "        this.monthlySalary = salary;\n"
                + "    }\n"
                + "    @Override\n"
                + "    double calculateSalary() { return monthlySalary; }\n"
                + "}\n"
                + "\n"
                + "class ContractEmployee extends Employee {\n"
                + "    double hourlyRate;\n"
                + "    int hoursWorked;\n"
                + "    ContractEmployee(String name, double rate, int hours) {\n"
                + "        super(name);\n"
                + "        this.hourlyRate = rate;\n"
                + "        this.hoursWorked = hours;\n"
                + "    }\n"
                + "    @Override\n"
                + "    double calculateSalary() { return hourlyRate * hoursWorked; }\n"
                + "}\n"
                + "\n"
                + "Employee[] staff = {\n"
                + "    new FullTimeEmployee(\"Марія\", 3000),\n"
                + "    new ContractEmployee(\"Олег\", 15, 100)\n"
                + "};\n"
                + "for (Employee e : staff) {\n"
                + "    System.out.println(e.name + \": \" + e.calculateSalary());\n"
                + "}"));
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

    private static Lesson materialObjectMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Методи класу Object"));
        uk.add(LessonBlock.paragraph(
                "Всі класи в Java неявно (автоматично) успадковуються від класу java.lang.Object. "
                + "Це означає, що кожен об'єкт має набір стандартних методів, найважливіші з яких: "
                + "toString(), equals() та hashCode()."));
        uk.add(LessonBlock.paragraph(
                "Метод toString() викликається, коли ви намагаєтесь вивести об'єкт на екран. "
                + "За замовчуванням він виводить ім'я класу і якусь незрозумілу адресу в пам'яті. "
                + "Тому його завжди варто перевизначати."));
        uk.add(LessonBlock.paragraph(
                "Метод equals() порівнює об'єкти. За замовчуванням він використовує оператор == "
                + "і перевіряє, чи це один і той самий об'єкт у пам'яті. Щоб порівнювати об'єкти за їх даними, "
                + "його також треба перевизначити."));
        uk.add(LessonBlock.code(
                "class Student {\n"
                + "    String name;\n"
                + "    int age;\n"
                + "\n"
                + "    Student(String name, int age) { \n"
                + "        this.name = name; \n"
                + "        this.age = age; \n"
                + "    }\n"
                + "\n"
                + "    // Робимо вивід зрозумілим\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"Студент [\" + name + \", \" + age + \" років]\";\n"
                + "    }\n"
                + "\n"
                + "    // Порівнюємо студентів за даними\n"
                + "    @Override\n"
                + "    public boolean equals(Object o) {\n"
                + "        if (this == o) return true;                  // Це один і той самий об'єкт\n"
                + "        if (!(o instanceof Student)) return false;   // Це взагалі не студент\n"
                + "        Student s = (Student) o;                     // Безпечно приводимо тип\n"
                + "        return this.age == s.age && this.name.equals(s.name);\n"
                + "    }\n"
                + "\n"
                + "    // Хеш-код — обов'язкова пара для equals\n"
                + "    @Override\n"
                + "    public int hashCode() {\n"
                + "        return 31 * name.hashCode() + age;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Student a = new Student(\"Іван\", 20);\n"
                + "Student b = new Student(\"Іван\", 20);\n"
                + "\n"
                + "System.out.println(a.toString()); // Студент [Іван, 20 років]\n"
                + "System.out.println(a.equals(b));  // true (дані однакові)\n"
                + "System.out.println(a == b);       // false (в пам'яті це різні об'єкти)"));
        uk.add(LessonBlock.warning(
                "Золоте правило Java: Якщо ви перевизначили метод equals(), ви ЗОБОВ'ЯЗАНІ "
                + "перевизначити і метод hashCode()! Якщо цього не зробити, колекції на зразок "
                + "HashMap та HashSet не зможуть правильно знаходити ваші об'єкти."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть клас Point з полями x та y (типу int). Перевизначте метод toString(), "
                + "щоб він повертав рядок у форматі \"(x, y)\". Перевизначте equals(), "
                + "щоб дві точки вважалися рівними, якщо їхні координати збігаються. "
                + "Створіть дві однакові точки і перевірте їх порівняння."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Point {\n"
                + "    int x, y;\n"
                + "    Point(int x, int y) {\n"
                + "        this.x = x;\n"
                + "        this.y = y;\n"
                + "    }\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"(\" + x + \", \" + y + \")\";\n"
                + "    }\n"
                + "    @Override\n"
                + "    public boolean equals(Object o) {\n"
                + "        if (this == o) return true;\n"
                + "        if (!(o instanceof Point)) return false;\n"
                + "        Point p = (Point) o;\n"
                + "        return this.x == p.x && this.y == p.y;\n"
                + "    }\n"
                + "    @Override\n"
                + "    public int hashCode() {\n"
                + "        return 31 * x + y;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Point p1 = new Point(5, 10);\n"
                + "Point p2 = new Point(5, 10);\n"
                + "System.out.println(p1.equals(p2)); // true"));
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

    private static void addChapter05(Course s) {
        Chapter ch = new Chapter("Глава 5. Інтерфейси", "Chapter 5. Interfaces");
        ch.add(materialInterfaces());
        ch.add(materialDefaultStaticMethods());
        ch.add(materialFunctionalInterfaces());
        s.add(ch);
    }

    private static Lesson materialInterfaces() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Інтерфейси: контракт для класів"));
        uk.add(LessonBlock.paragraph(
                "Інтерфейс — це суворий контракт або набір правил. Він каже: «Будь-який клас, "
                + "який мене реалізує, зобов'язаний мати ці методи». На відміну від абстрактних "
                + "класів, один клас може реалізувати (implements) одразу багато інтерфейсів."));
        uk.add(LessonBlock.code(
                "interface Drawable {\n"
                + "    void draw();  // Метод без тіла. За замовчуванням він public abstract\n"
                + "}\n"
                + "\n"
                + "interface Resizable {\n"
                + "    void resize(double factor);\n"
                + "}\n"
                + "\n"
                + "// Клас реалізує одразу ДВА інтерфейси\n"
                + "class Widget implements Drawable, Resizable {\n"
                + "    private double scale = 1.0;\n"
                + "\n"
                + "    @Override\n"
                + "    public void draw() {\n"
                + "        System.out.println(\"Малюю віджет (масштаб \" + scale + \")\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public void resize(double factor) { \n"
                + "        scale *= factor; \n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використовуємо поліморфізм\n"
                + "Drawable d = new Widget();\n"
                + "d.draw();\n"
                + "// d.resize(2.0); // ПОМИЛКА: Тип Drawable знає тільки про draw()\n"
                + "\n"
                + "Resizable r = (Resizable) d; // Приводимо тип\n"
                + "r.resize(2.0);"));
        uk.add(LessonBlock.heading("Що може містити інтерфейс"));
        uk.add(LessonBlock.table(
                "Елемент\tІнтерфейс\tАбстрактний клас",
                Arrays.asList(
                    "Абстрактні методи\tТак (за замовчуванням)\tТак",
                    "Звичайні методи\tТак (через ключове слово default)\tТак",
                    "Поля\tТільки константи (public static final)\tБудь-які",
                    "Конструктори\tНі\tТак",
                    "Множинність\tМожна імплементувати багато\tМожна успадкувати лише один")));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть інтерфейс Playable з методом play(). Створіть класи MusicPlayer та VideoPlayer, "
                + "які реалізують цей інтерфейс (кожен по-своєму). Створіть масив типу Playable, "
                + "покладіть туди обидва плеєри і викличте play() для кожного."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "interface Playable {\n"
                + "    void play();\n"
                + "}\n"
                + "\n"
                + "class MusicPlayer implements Playable {\n"
                + "    @Override\n"
                + "    public void play() {\n"
                + "        System.out.println(\"Грає музика...\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class VideoPlayer implements Playable {\n"
                + "    @Override\n"
                + "    public void play() {\n"
                + "        System.out.println(\"Відтворюється відео...\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "Playable[] devices = { new MusicPlayer(), new VideoPlayer() };\n"
                + "for (Playable device : devices) {\n"
                + "    device.play();\n"
                + "}"));
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

    private static Lesson materialDefaultStaticMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Default та static методи (Java 8+)"));
        uk.add(LessonBlock.paragraph(
                "У старих версіях Java додавання нового методу до інтерфейсу ламало всі класи, "
                + "які його реалізували, бо вони мали одразу реалізувати цей новий метод. "
                + "Тому у Java 8 додали можливість писати методи з тілом прямо в інтерфейсі (default-методи)."));
        uk.add(LessonBlock.paragraph(
                "Також додали можливість створювати static методи. Вони належать самому інтерфейсу "
                + "і використовуються як допоміжні функції."));
        uk.add(LessonBlock.code(
                "interface Logger {\n"
                + "    // Обов'язковий метод для реалізації\n"
                + "    void log(String message);\n"
                + "\n"
                + "    // Default метод — вже має готове тіло\n"
                + "    default void warn(String message) {\n"
                + "        log(\"[УВАГА] \" + message);\n"
                + "    }\n"
                + "\n"
                + "    // Static метод — викликається через Logger.consoleLogger()\n"
                + "    static Logger consoleLogger() {\n"
                + "        // Використовуємо лямбду для швидкої реалізації\n"
                + "        return msg -> System.out.println(\"Консоль: \" + msg);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class FileLogger implements Logger {\n"
                + "    @Override\n"
                + "    public void log(String message) {\n"
                + "        System.out.println(\"ФАЙЛ: \" + message);\n"
                + "    }\n"
                + "    // Метод warn() ми не реалізовуємо, він береться з інтерфейсу\n"
                + "}\n"
                + "\n"
                + "FileLogger fl = new FileLogger();\n"
                + "fl.log(\"Система стартує\");  // ФАЙЛ: Система стартує\n"
                + "fl.warn(\"Мало пам'яті\");    // ФАЙЛ: [УВАГА] Мало пам'яті"));
        uk.add(LessonBlock.heading("Проблема «діаманта»"));
        uk.add(LessonBlock.paragraph(
                "Оскільки клас може реалізувати багато інтерфейсів, що станеться, якщо два з них "
                + "мають default-метод з однаковим ім'ям? Компілятор видасть помилку. "
                + "Вам доведеться вручну перевизначити цей метод і вказати, чий саме код викликати."));
        uk.add(LessonBlock.code(
                "interface A { default void hello() { System.out.println(\"A\"); } }\n"
                + "interface B { default void hello() { System.out.println(\"B\"); } }\n"
                + "\n"
                + "class C implements A, B {\n"
                + "    @Override\n"
                + "    public void hello() {\n"
                + "        A.super.hello();  // Явно вказуємо, що хочемо логіку з інтерфейсу A\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть інтерфейс Greeter з абстрактним методом greet(String name) та default методом "
                + "greetGuest(), який викликає greet(\"Гість\"). Реалізуйте цей інтерфейс у класі FriendlyGreeter, "
                + "перевизначивши лише метод greet(). Створіть об'єкт і викличте обидва методи."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "interface Greeter {\n"
                + "    void greet(String name);\n"
                + "    default void greetGuest() {\n"
                + "        greet(\"Гість\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class FriendlyGreeter implements Greeter {\n"
                + "    @Override\n"
                + "    public void greet(String name) {\n"
                + "        System.out.println(\"Привіт, \" + name + \"! Раді бачити!\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "FriendlyGreeter fg = new FriendlyGreeter();\n"
                + "fg.greet(\"Олена\"); // Привіт, Олена! Раді бачити!\n"
                + "fg.greetGuest();     // Привіт, Гість! Раді бачити!"));
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

    private static Lesson materialFunctionalInterfaces() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Функціональні інтерфейси та Лямбда-вирази"));
        uk.add(LessonBlock.paragraph(
                "Функціональний інтерфейс — це інтерфейс, який має РІВНО ОДИН абстрактний метод. "
                + "Їх спеціально позначають анотацією @FunctionalInterface (хоча це не обов'язково). "
                + "Саме такі інтерфейси можна реалізовувати за допомогою лямбда-виразів — "
                + "короткого синтаксису, який замінює громіздкі анонімні класи."));
        uk.add(LessonBlock.code(
                "@FunctionalInterface\n"
                + "interface MathOperation {\n"
                + "    int operate(int a, int b);\n"
                + "}\n"
                + "\n"
                + "// Старий спосіб: Анонімний клас\n"
                + "MathOperation addition = new MathOperation() {\n"
                + "    @Override\n"
                + "    public int operate(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "};\n"
                + "\n"
                + "// Новий спосіб: Лямбда-вираз\n"
                + "MathOperation subtraction = (a, b) -> a - b;\n"
                + "MathOperation multiplication = (a, b) -> a * b;\n"
                + "\n"
                + "System.out.println(addition.operate(10, 5));       // 15\n"
                + "System.out.println(subtraction.operate(10, 5));    // 5\n"
                + "System.out.println(multiplication.operate(10, 5)); // 50"));
        uk.add(LessonBlock.note(
                "Вам не потрібно створювати функціональні інтерфейси на кожен випадок. Java вже має "
                + "великий набір готових у пакеті java.util.function: Predicate (перевірка умови), "
                + "Function (перетворення даних), Consumer (споживання даних), Supplier (постачання даних)."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть функціональний інтерфейс StringValidator з одним абстрактним методом "
                + "boolean isValid(String s). Створіть змінну типу StringValidator, присвойте їй "
                + "лямбда-вираз, який перевіряє, чи довжина рядка більше 5. Протестуйте її."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@FunctionalInterface\n"
                + "interface StringValidator {\n"
                + "    boolean isValid(String s);\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "StringValidator lengthCheck = s -> s != null && s.length() > 5;\n"
                + "\n"
                + "System.out.println(lengthCheck.isValid(\"Java\"));       // false\n"
                + "System.out.println(lengthCheck.isValid(\"Інтерфейс\"));  // true"));
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

    private static void addChapter06(Course s) {
        Chapter ch = new Chapter("Глава 6. Пакети", "Chapter 6. Packages");
        ch.add(materialAccessModifiers());
        ch.add(materialPackagesAndImports());
        s.add(ch);
    }

    private static Lesson materialAccessModifiers() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Модифікатори доступу"));
        uk.add(LessonBlock.paragraph(
                "Модифікатори доступу визначають, ХТО може бачити клас, поле чи метод. "
                + "Це як рівні секретності для даних у вашій програмі. "
                + "Правильно налаштовані модифікатори допомагають приховати внутрішню реалізацію "
                + "(інкапсуляція) та уникнути випадкових змін."));
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
                + "    public String name;           // Видно всім\n"
                + "    protected int age;            // Видно в пакеті та спадкоємцям\n"
                + "    String email;                 // Видно ТІЛЬКИ в пакеті com.example\n"
                + "    private String password;      // Видно ТІЛЬКИ всередині класу User\n"
                + "\n"
                + "    public void printPublic() { System.out.println(name); }\n"
                + "    protected void printProtected() { System.out.println(age); }\n"
                + "    void printDefault() { System.out.println(email); }\n"
                + "    private void printPrivate() { System.out.println(password); }\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Порада для початківців: за замовчуванням робіть усі поля private, а методи — "
                + "public (тільки ті, що дійсно потрібні іншим). "
                + "Це називається «принципом мінімальних привілеїв» (principle of least privilege)."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть клас BankAccount. Зробіть поле balance типу double приватним (private). "
                + "Створіть публічний метод deposit(double amount), який збільшує баланс, "
                + "і публічний метод getBalance(), який повертає його значення. "
                + "Спробуйте з іншого класу змінити balance напряму. Що станеться?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class BankAccount {\n"
                + "    private double balance = 0.0;\n"
                + "\n"
                + "    public void deposit(double amount) {\n"
                + "        if (amount > 0) {\n"
                + "            balance += amount;\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public double getBalance() {\n"
                + "        return balance;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "BankAccount acc = new BankAccount();\n"
                + "acc.deposit(100);\n"
                + "System.out.println(\"Баланс: \" + acc.getBalance()); // 100.0\n"
                + "// acc.balance = 5000; // ПОМИЛКА КОМПІЛЯЦІЇ: поле balance є private"));
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

    private static Lesson materialPackagesAndImports() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Пакети та імпорти"));
        uk.add(LessonBlock.paragraph(
                "Пакет — це просто папка на диску, в якій лежать ваші класи. "
                + "Вони потрібні для логічного групування коду та уникнення конфліктів імен "
                + "(може бути два класи User, якщо вони в різних пакетах). "
                + "Назва пакету обов'язково повинна відповідати структурі папок (наприклад, "
                + "пакет com.example знаходиться у папці com/example/)."));
        uk.add(LessonBlock.code(
                "// Оголошення пакету — завжди перший рядок коду у файлі\n"
                + "package com.example.util;\n"
                + "\n"
                + "// Імпорт потрібен, щоб не писати довге повне ім'я класу кожного разу\n"
                + "import java.util.ArrayList;       // Імпорт одного конкретного класу\n"
                + "import java.util.*;               // Імпорт всіх класів з пакету java.util\n"
                + "import static java.lang.Math.PI;  // Імпорт стат. поля (Java 5+)\n"
                + "import static java.lang.Math.*;   // Імпорт всіх стат. елементів\n"
                + "\n"
                + "class App {\n"
                + "    void run() {\n"
                + "        ArrayList<String> list = new ArrayList<>(); // Без імпорту було б java.util.ArrayList\n"
                + "        System.out.println(PI);                     // завдяки import static\n"
                + "        System.out.println(sqrt(16));               // завдяки import static Math.*\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.list(
                "Основні пакети Java:",
                "java.lang — імпортується автоматично (String, System, Math)",
                "java.util — колекції (List, Map), дати, random",
                "java.io / java.nio — робота з вводом-виводом та файлами",
                "java.time — сучасне API для дат і часу (Java 8+)",
                "java.net — мережеві операції"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть клас, імпортуйте клас Scanner з пакету java.util та "
                + "клас Date з пакету java.util. Напишіть код, який створює об'єкт Date "
                + "і виводить його на екран. Зверніть увагу на те, як імпорти роблять код чистішим."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "import java.util.Scanner;\n"
                + "import java.util.Date;\n"
                + "\n"
                + "public class Main {\n"
                + "    public static void main(String[] args) {\n"
                + "        // Без імпорту довелося б писати java.util.Date date = new java.util.Date();\n"
                + "        Date currentDate = new Date();\n"
                + "        System.out.println(\"Поточна дата: \" + currentDate);\n"
                + "        \n"
                + "        // Scanner scan = new Scanner(System.in);\n"
                + "    }\n"
                + "}"));
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

    private static void addChapter07(Course s) {
        Chapter ch = new Chapter("Глава 7. Обробка винятків",
                "Chapter 7. Exception handling");
        ch.add(materialExceptionHierarchy());
        ch.add(materialTryCatchFinally());
        ch.add(materialTryWithResources());
        ch.add(materialCustomExceptions());
        s.add(ch);
    }

    private static Lesson materialExceptionHierarchy() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Ієрархія винятків"));
        uk.add(LessonBlock.paragraph(
                "Винятки (Exceptions) в Java — це об'єкти, що сигналізують про помилку під час "
                + "виконання програми. Всі вони походять від класу Throwable. "
                + "Є дві основні гілки винятків: Checked (перевіряємі компілятором) та "
                + "Unchecked (неперевіряємі)."));
        uk.add(LessonBlock.code(
                "Throwable\n"
                + "├── Error (системні помилки — OutOfMemoryError, StackOverflow)\n"
                + "│   └── НЕ ловимо, бо це проблема JVM, програма не зможе відновитись\n"
                + "└── Exception\n"
                + "    ├── RuntimeException (UNCHECKED — компілятор не вимагає try/catch)\n"
                + "    │   ├── NullPointerException (виклик методу у null)\n"
                + "    │   ├── ArrayIndexOutOfBoundsException (неіснуючий індекс масиву)\n"
                + "    │   ├── ArithmeticException (ділення на 0)\n"
                + "    │   └── IllegalArgumentException (неправильний аргумент)\n"
                + "    └── Checked Exceptions (CHECKED — вимагають try/catch або throws)\n"
                + "        ├── IOException (проблеми з файлами або мережею)\n"
                + "        └── SQLException (проблеми з базою даних)"));
        uk.add(LessonBlock.warning(
                "Checked винятки — це ті ситуації, які ви можете і повинні передбачити (наприклад, "
                + "файлу не існує). Unchecked — це помилки у логіці коду (наприклад, звернення до null)."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Подумайте і дайте відповідь: якщо ви пишете метод, який читає дані з файлу, "
                + "який тип винятку він найімовірніше згенерує, і чи потрібно його обов'язково "
                + "обробляти (Checked чи Unchecked)?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.paragraph(
                "Метод читання з файлу може згенерувати FileNotFoundException або IOException. "
                + "Це Checked винятки, тому компілятор змусить вас обробити їх за допомогою "
                + "блоку try-catch, або додати ключове слово throws до сигнатури методу."));
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

    private static Lesson materialTryCatchFinally() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Блоки try, catch та finally"));
        uk.add(LessonBlock.paragraph(
                "Щоб програма не «падала» при помилках, використовують конструкцію try-catch. "
                + "У блоці try ми пишемо небезпечний код. Якщо стається помилка, виконання переходить "
                + "до блоку catch. Блок finally виконується ЗАВЖДИ, незалежно від того, була помилка чи ні."));
        uk.add(LessonBlock.code(
                "try {\n"
                + "    int result = 10 / 0;             // Викличе ArithmeticException\n"
                + "    System.out.println(\"Цей рядок не виконається\");\n"
                + "} catch (ArithmeticException e) {\n"
                + "    System.out.println(\"Помилка: Ділення на нуль!\");\n"
                + "} catch (Exception e) {\n"
                + "    System.out.println(\"Обробка будь-яких інших помилок\");\n"
                + "} finally {\n"
                + "    System.out.println(\"finally: Я виконаюся в будь-якому випадку!\");\n"
                + "}"));
        uk.add(LessonBlock.heading("Multi-catch (Java 7+)"));
        uk.add(LessonBlock.paragraph(
                "Якщо кілька різних винятків обробляються абсолютно однаково, їх можна "
                + "об'єднати в одному блоці catch через символ | (або)."));
        uk.add(LessonBlock.code(
                "try {\n"
                + "    String s = \"abc\";\n"
                + "    int n = Integer.parseInt(s);  // NumberFormatException\n"
                + "} catch (NumberFormatException | NullPointerException e) {\n"
                + "    System.out.println(\"Помилка формату або null: \" + e.getMessage());\n"
                + "}"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Напишіть метод, який приймає масив цілих чисел і виводить елемент під індексом 5. "
                + "Обгорніть цей код у try-catch, який ловить ArrayIndexOutOfBoundsException "
                + "і виводить повідомлення «Індекс за межами масиву»."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "public void printFifthElement(int[] arr) {\n"
                + "    try {\n"
                + "        System.out.println(\"Елемент: \" + arr[5]);\n"
                + "    } catch (ArrayIndexOutOfBoundsException e) {\n"
                + "        System.out.println(\"Індекс за межами масиву\");\n"
                + "    }\n"
                + "}"));
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

    private static Lesson materialTryWithResources() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("try-with-resources"));
        uk.add(LessonBlock.paragraph(
                "Робота з файлами чи базою даних вимагає обов'язкового закриття ресурсу "
                + "(метод close()). Якщо забути, станеться витік пам'яті. "
                + "try-with-resources (починаючи з Java 7) вирішує цю проблему: він сам закриває "
                + "ресурси, що реалізують інтерфейс AutoCloseable."));
        uk.add(LessonBlock.code(
                "// Замість того, щоб писати finally { br.close(); }\n"
                + "try (BufferedReader br = new BufferedReader(new FileReader(\"test.txt\"))) {\n"
                + "    String line;\n"
                + "    while ((line = br.readLine()) != null) {\n"
                + "        System.out.println(line);\n"
                + "    }\n"
                + "} catch (IOException e) {\n"
                + "    System.out.println(\"Помилка читання файлу\");\n"
                + "}\n"
                + "// Файл гарантовано закрито!"));
        uk.add(LessonBlock.paragraph(
                "Можна відкривати одразу кілька ресурсів, розділяючи їх крапкою з комою (;). "
                + "Вони будуть закриті у зворотному порядку (від останнього до першого)."));
        uk.add(LessonBlock.code(
                "try (FileInputStream in = new FileInputStream(\"in.txt\");\n"
                + "     FileOutputStream out = new FileOutputStream(\"out.txt\")) {\n"
                + "    \n"
                + "    int data = in.read();\n"
                + "    out.write(data);\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть власний клас MyResource, який реалізує інтерфейс AutoCloseable. "
                + "У методі close() виведіть \"Ресурс закрито!\". "
                + "Використайте цей клас у блоці try-with-resources."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class MyResource implements AutoCloseable {\n"
                + "    public void doWork() {\n"
                + "        System.out.println(\"Працюємо з ресурсом\");\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public void close() {\n"
                + "        System.out.println(\"Ресурс закрито!\");\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "try (MyResource res = new MyResource()) {\n"
                + "    res.doWork();\n"
                + "} \n"
                + "// Вивід буде:\n"
                + "// Працюємо з ресурсом\n"
                + "// Ресурс закрито!"));
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

    private static Lesson materialCustomExceptions() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Власні винятки (Custom Exceptions)"));
        uk.add(LessonBlock.paragraph(
                "Іноді стандартних винятків Java недостатньо для бізнес-логіки. "
                + "Наприклад, ви хочете обробити ситуацію «Недостатньо коштів на рахунку». "
                + "Для цього ви можете створити свій клас, успадкувавши його від Exception "
                + "(буде Checked) або від RuntimeException (буде Unchecked)."));
        uk.add(LessonBlock.code(
                "// Створюємо Checked виняток\n"
                + "class InsufficientFundsException extends Exception {\n"
                + "    private double deficit;\n"
                + "\n"
                + "    public InsufficientFundsException(double deficit) {\n"
                + "        super(\"Недостатньо коштів. Бракує: \" + deficit);\n"
                + "        this.deficit = deficit;\n"
                + "    }\n"
                + "    public double getDeficit() { return deficit; }\n"
                + "}\n"
                + "\n"
                + "class BankAccount {\n"
                + "    private double balance = 100.0;\n"
                + "\n"
                + "    // Ключове слово throws вказує, що метод МОЖЕ кинути цей виняток\n"
                + "    public void withdraw(double amount) throws InsufficientFundsException {\n"
                + "        if (amount > balance) {\n"
                + "            // Ключове слово throw ВИКИДАЄ сам об'єкт винятку\n"
                + "            throw new InsufficientFundsException(amount - balance);\n"
                + "        }\n"
                + "        balance -= amount;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Різниця між throw і throws: throw використовується ВЕРЕДИНІ методу, "
                + "щоб фактично згенерувати помилку. throws пишеться в ОГОЛОШЕННІ методу, "
                + "попереджаючи інших, що метод небезпечний."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть свій Unchecked виняток InvalidAgeException (успадкуйте від RuntimeException). "
                + "Напишіть метод setAge(int age). Якщо age < 0 або age > 150, "
                + "метод повинен кинути цей виняток через throw."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class InvalidAgeException extends RuntimeException {\n"
                + "    public InvalidAgeException(String message) {\n"
                + "        super(message);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class Person {\n"
                + "    public void setAge(int age) {\n"
                + "        if (age < 0 || age > 150) {\n"
                + "            throw new InvalidAgeException(\"Некоректний вік: \" + age);\n"
                + "        }\n"
                + "        System.out.println(\"Вік встановлено: \" + age);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Виклик Person.setAge(-5) призведе до завершення програми з вашою помилкою."));
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

    private static void addChapter08(Course s) {
        Chapter ch = new Chapter("Глава 8. Рядки", "Chapter 8. Strings");
        ch.add(materialStringImmutable());
        ch.add(materialStringMethods());
        ch.add(materialStringBuilder());
        s.add(ch);
    }

    private static Lesson materialStringImmutable() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("String: незмінність (immutability)"));
        uk.add(LessonBlock.paragraph(
                "Клас String в Java є НЕЗМІННИМ (immutable). Це означає, що після "
                + "створення об'єкта String його зміст неможливо змінити. Коли ви викликаєте "
                + "методи, які нібито «змінюють» рядок (наприклад, конкатенація чи заміна), "
                + "насправді створюється НОВИЙ об'єкт String, а старий залишається в пам'яті."));
        uk.add(LessonBlock.code(
                "String s1 = \"Hello\";\n"
                + "String s2 = s1.concat(\" World\");  // створює НОВИЙ рядок\n"
                + "System.out.println(s1);           // Hello (залишився без змін!)\n"
                + "System.out.println(s2);           // Hello World\n"
                + "\n"
                + "// === String Pool (Пул рядків) ===\n"
                + "// Для економії пам'яті Java зберігає літерали в пулі рядків.\n"
                + "String a = \"hello\";\n"
                + "String b = \"hello\";\n"
                + "System.out.println(a == b);       // true — посилаються на той самий об'єкт у пулі!\n"
                + "\n"
                + "String c = new String(\"hello\");\n"
                + "System.out.println(a == c);       // false — ключове слово new ЗАВЖДИ створює новий об'єкт\n"
                + "System.out.println(a.equals(c));  // true — метод equals порівнює ЗМІСТ рядків"));
        uk.add(LessonBlock.warning(
                "ЗАВЖДИ порівнюйте рядки через метод equals(), а НЕ через оператор ==. "
                + "Оператор == перевіряє, чи це одне й те саме посилання у пам'яті, "
                + "а метод equals() перевіряє, чи однакові символи всередині."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть рядок String word = \"Java\". "
                + "Викличте метод word.toUpperCase();. "
                + "Потім виведіть змінну word на екран. Який буде результат і чому?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.paragraph(
                "Результат буде \"Java\" (а не \"JAVA\"). Оскільки String є незмінним, метод "
                + "toUpperCase() повертає новий рядок \"JAVA\", але ми не зберегли його в жодну змінну. "
                + "Щоб зберегти результат, потрібно написати: word = word.toUpperCase();"));
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
                + "System.out.println(a.equals(s));  // true — compares content"));
        en.add(LessonBlock.warning(
                "Compare strings with equals(), NOT with ==. "
                + "== checks if it's the same object in memory, equals checks if the "
                + "content is the same. For strings you always want to compare content."));
        return new Lesson("8.1", "String та незмінність", "String & immutability", uk, en);
    }

    private static Lesson materialStringMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Корисні методи класу String"));
        uk.add(LessonBlock.paragraph(
                "Клас String має величезну кількість вбудованих методів для "
                + "маніпуляції текстом. Ось найпопулярніші з них:"));
        uk.add(LessonBlock.code(
                "String s = \"  Hello, Java World!  \";\n"
                + "\n"
                + "// 1. Довжина та доступ до символів\n"
                + "int len = s.length();             // 22 (включно з пробілами)\n"
                + "char ch = s.charAt(2);            // 'H' (індексація починається з 0)\n"
                + "\n"
                + "// 2. Пошук підрядка\n"
                + "int index = s.indexOf(\"Java\");    // 9 (повертає індекс початку, або -1 якщо не знайдено)\n"
                + "boolean has = s.contains(\"Java\"); // true\n"
                + "boolean start = s.startsWith(\"  Hello\"); // true\n"
                + "boolean end = s.endsWith(\"!\");    // false (закінчується пробілами)\n"
                + "\n"
                + "// 3. Зріз (підрядок) та трансформація\n"
                + "String trimmed = s.trim();        // \"Hello, Java World!\" (видаляє пробіли по краях)\n"
                + "String sub = s.substring(9, 13);  // \"Java\" (від 9 індексу включно до 13 виключно)\n"
                + "String upper = s.toUpperCase();   // \"  HELLO, JAVA WORLD!  \"\n"
                + "String lower = s.toLowerCase();   // \"  hello, java world!  \"\n"
                + "\n"
                + "// 4. Заміна та розділення\n"
                + "String replaced = \"a-b-c\".replace('-', '_'); // \"a_b_c\"\n"
                + "String[] parts = \"one,two,three\".split(\",\"); // [\"one\", \"two\", \"three\"]\n"
                + "\n"
                + "// 5. Перевірка та конвертація\n"
                + "boolean empty = \"\".isEmpty();     // true\n"
                + "int num = Integer.parseInt(\"42\"); // 42 (перетворення рядка в число)\n"
                + "String strNum = String.valueOf(3.14); // \"3.14\" (перетворення числа в рядок)"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Є рядок String text = \" apple, banana , orange \". "
                + "Напишіть код, який видалить пробіли по краях, розіб'є рядок по комі, "
                + "і виведе назву другого фрукта (banana) у верхньому регістрі (BANANA)."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "String text = \" apple, banana , orange \";\n"
                + "String cleanText = text.trim();\n"
                + "String[] fruits = cleanText.split(\",\");\n"
                + "\n"
                + "// Звертаємось до другого елемента (індекс 1), видаляємо його зайві пробіли і робимо великими літерами\n"
                + "String secondFruit = fruits[1].trim().toUpperCase();\n"
                + "System.out.println(secondFruit); // BANANA"));
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

    private static Lesson materialStringBuilder() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("StringBuilder та StringBuffer"));
        uk.add(LessonBlock.paragraph(
                "Оскільки String є незмінним (immutable), операція конкатенації "
                + "(з'єднання рядків через +) у циклі дуже неефективна, адже щоразу створюється "
                + "новий об'єкт. Для частої зміни тексту використовують класи "
                + "StringBuilder (для одного потоку) або StringBuffer (для багатопоточності)."));
        uk.add(LessonBlock.code(
                "StringBuilder sb = new StringBuilder(\"Hello\");\n"
                + "sb.append(\" World\");           // Додає в кінець (Hello World)\n"
                + "sb.insert(5, \",\");             // Вставляє на позицію 5 (Hello, World)\n"
                + "sb.replace(7, 12, \"Java\");     // Замінює частину (Hello, Java)\n"
                + "sb.deleteCharAt(5);            // Видаляє кому (Hello Java)\n"
                + "sb.reverse();                  // Перевертає рядок (avaJ olleH)\n"
                + "\n"
                + "// Коли закінчили формувати текст, перетворюємо назад у String\n"
                + "String result = sb.toString(); // avaJ olleH"));
        uk.add(LessonBlock.heading("Порівняння продуктивності"));
        uk.add(LessonBlock.code(
                "// ❌ ПОГАНО — створює тисячі проміжних об'єктів String у пам'яті\n"
                + "String bad = \"\";\n"
                + "for (int i = 0; i < 10000; i++) {\n"
                + "    bad += i;  // кожна ітерація = new String!\n"
                + "}\n"
                + "\n"
                + "// ✅ ДОБРЕ — один об'єкт StringBuilder, який просто розширює свій масив\n"
                + "StringBuilder good = new StringBuilder();\n"
                + "for (int i = 0; i < 10000; i++) {\n"
                + "    good.append(i);\n"
                + "}\n"
                + "String result = good.toString();"));
        uk.add(LessonBlock.table(
                "Клас\tЗмінність\tПотокобезпечність\tКоли використовувати",
                Arrays.asList(
                    "String\tНі (immutable)\tТак (немає змін)\tКороткі рядки, незмінні тексти",
                    "StringBuilder\tТак\tНі\tФормування складного тексту (1 потік)",
                    "StringBuffer\tТак\tТак (synchronized)\tФормування тексту (кілька потоків)")));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть метод, який приймає масив слів (наприклад, {\"I\", \"love\", \"Java\"}) "
                + "і повертає єдиний рядок, де слова розділені пробілом. Використайте StringBuilder."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "public String joinWords(String[] words) {\n"
                + "    StringBuilder sb = new StringBuilder();\n"
                + "    for (int i = 0; i < words.length; i++) {\n"
                + "        sb.append(words[i]);\n"
                + "        // Додаємо пробіл після всіх слів, крім останнього\n"
                + "        if (i < words.length - 1) {\n"
                + "            sb.append(\" \");\n"
                + "        }\n"
                + "    }\n"
                + "    return sb.toString();\n"
                + "}"));
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

    private static void addChapter09(Course s) {
        Chapter ch = new Chapter("Глава 9. Колекції", "Chapter 9. Collections");
        ch.add(materialList());
        ch.add(materialSet());
        ch.add(materialMap());
        ch.add(materialIterator());
        s.add(ch);
    }

    private static Lesson materialList() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("List: Інтерфейс списку"));
        uk.add(LessonBlock.paragraph(
                "List (список) — це впорядкована колекція елементів, яка дозволяє зберігати "
                + "дублікати. Кожен елемент має свій порядковий номер (індекс), починаючи з 0. "
                + "Основними реалізаціями є ArrayList (на основі масиву, що збільшується) та "
                + "LinkedList (на основі двонаправленого списку зв'язаних вузлів)."));
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
                + "String first = names.get(0); // Іван\n"
                + "int size = names.size();     // 4\n"
                + "boolean has = names.contains(\"Олена\"); // true\n"
                + "int pos = names.indexOf(\"Андрій\"); // 3\n"
                + "\n"
                + "// Видалення та зміна\n"
                + "names.remove(\"Марія\");    // видалити за значенням\n"
                + "names.remove(0);           // видалити за індексом\n"
                + "names.set(0, \"Богдан\");   // замінити елемент\n"
                + "\n"
                + "// Сортування (Java 8+)\n"
                + "names.sort(Comparator.naturalOrder());"));
        uk.add(LessonBlock.note(
                "ArrayList — швидкий доступ за індексом O(1), але повільне додавання/видалення з "
                + "середини списку O(n). LinkedList — навпаки: швидке вставляння/видалення "
                + "O(1) (якщо ви вже на правильній позиції), але повільний доступ за індексом O(n). "
                + "На практиці ArrayList використовується у 95% випадків."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть ArrayList з цілих чисел (тип Integer). Додайте числа 10, 20, 30. "
                + "Потім замініть число 20 на 25 за допомогою методу set(). "
                + "Як вивести оновлений список на екран?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "List<Integer> numbers = new ArrayList<>();\n"
                + "numbers.add(10);\n"
                + "numbers.add(20);\n"
                + "numbers.add(30);\n"
                + "\n"
                + "// Замінюємо елемент за індексом 1 (це число 20)\n"
                + "numbers.set(1, 25);\n"
                + "\n"
                + "System.out.println(numbers); // [10, 25, 30]"));
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

    private static Lesson materialSet() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Set: Множина унікальних елементів"));
        uk.add(LessonBlock.paragraph(
                "Set (множина) — це колекція, яка НЕ дозволяє зберігати дублікати. "
                + "Якщо ви спробуєте додати існуючий елемент, колекція просто проігнорує його. "
                + "Тут немає індексів (не можна зробити get(0))."));
        uk.add(LessonBlock.code(
                "// HashSet — швидкий, але не гарантує жодного порядку елементів\n"
                + "Set<String> set = new HashSet<>(Arrays.asList(\"b\", \"a\", \"c\", \"a\"));\n"
                + "System.out.println(set);  // [a, b, c] — дублікат \"a\" відкинутий (порядок може бути будь-який)\n"
                + "\n"
                + "// TreeSet — елементи автоматично сортуються за зростанням (потребує Comparable)\n"
                + "TreeSet<Integer> sorted = new TreeSet<>(Arrays.asList(5, 1, 3, 1));\n"
                + "System.out.println(sorted);          // [1, 3, 5]\n"
                + "System.out.println(sorted.first());  // 1\n"
                + "System.out.println(sorted.last());   // 5\n"
                + "\n"
                + "// LinkedHashSet — зберігає порядок додавання елементів\n"
                + "LinkedHashSet<String> ordered = new LinkedHashSet<>();\n"
                + "ordered.add(\"c\"); ordered.add(\"a\"); ordered.add(\"b\");\n"
                + "System.out.println(ordered);  // [c, a, b] — порядок додавання збережено"));
        uk.add(LessonBlock.warning(
                "Увага! HashSet та LinkedHashSet використовують методи hashCode() та equals() об'єктів "
                + "для визначення унікальності. Якщо ви створюєте свій клас (наприклад, User) і "
                + "хочете зберігати його в Set, ви ОБОВ'ЯЗКОВО повинні перевизначити ці методи, "
                + "інакше Set не зможе правильно розпізнавати однакові об'єкти."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "У вас є список імен з дублікатами: List<String> names = Arrays.asList(\"Анна\", \"Олег\", \"Анна\"); "
                + "Як найшвидше отримати колекцію тільки унікальних імен?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\"Анна\", \"Олег\", \"Анна\");\n"
                + "\n"
                + "// Найшвидший спосіб — просто передати список у конструктор HashSet\n"
                + "Set<String> uniqueNames = new HashSet<>(names);\n"
                + "\n"
                + "System.out.println(uniqueNames); // [Анна, Олег]"));
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

    private static Lesson materialMap() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Map: Словник (Ключ-Значення)"));
        uk.add(LessonBlock.paragraph(
                "Map (карта, словник) — це структура даних, що зберігає пари «Ключ - Значення». "
                + "Кожен ключ має бути унікальним. Наприклад, ви можете використовувати номер "
                + "телефону як ключ, а ім'я власника — як значення. Map НЕ успадковується від Collection."));
        uk.add(LessonBlock.code(
                "Map<String, Integer> ages = new HashMap<>();\n"
                + "\n"
                + "// Додавання та оновлення (метод put)\n"
                + "ages.put(\"Іван\", 30);\n"
                + "ages.put(\"Олена\", 25);\n"
                + "ages.put(\"Іван\", 31);  // Ключ \"Іван\" вже існує, тому старе значення заміниться на 31\n"
                + "\n"
                + "// Отримання значень за ключем (метод get)\n"
                + "System.out.println(ages.get(\"Іван\"));          // 31\n"
                + "System.out.println(ages.get(\"Богдан\"));        // null (такого ключа немає)\n"
                + "System.out.println(ages.getOrDefault(\"Богдан\", 0)); // 0 (безпечне отримання зі значенням за замовчуванням)\n"
                + "\n"
                + "// Перевірки\n"
                + "boolean hasKey = ages.containsKey(\"Олена\");  // true\n"
                + "boolean hasVal = ages.containsValue(25);      // true\n"
                + "int size = ages.size();                       // 2\n"
                + "\n"
                + "// Ітерація (Java 8+)\n"
                + "ages.forEach((name, age) ->\n"
                + "    System.out.println(name + \": \" + age));\n"
                + "\n"
                + "// Робота з множинами ключів та значень\n"
                + "Set<String> keys = ages.keySet();             // Отримати всі ключі\n"
                + "Collection<Integer> values = ages.values();   // Отримати всі значення"));
        uk.add(LessonBlock.note(
                "Як і з Set, існують три основні реалізації: HashMap (швидкий, без порядку), "
                + "TreeMap (ключі автоматично відсортовані) та LinkedHashMap (зберігає порядок додавання "
                + "пар). Для ключів у HashMap обов'язково потрібні методи hashCode() та equals()."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть HashMap з ключем типу String (назва товару) та значенням типу Double (ціна). "
                + "Додайте товар \"Молоко\" з ціною 35.5. Потім перевірте, якщо ціна на \"Молоко\" існує, "
                + "виведіть її."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "Map<String, Double> prices = new HashMap<>();\n"
                + "prices.put(\"Молоко\", 35.5);\n"
                + "\n"
                + "if (prices.containsKey(\"Молоко\")) {\n"
                + "    System.out.println(\"Ціна молока: \" + prices.get(\"Молоко\"));\n"
                + "}"));
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

    private static Lesson materialIterator() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Ітератор (Iterator) та цикли для колекцій"));
        uk.add(LessonBlock.paragraph(
                "Обходити колекції можна різними способами. Найпопулярніший — цикл for-each. "
                + "Проте, якщо вам потрібно ВИДАЛИТИ елемент прямо під час обходу, звичайний цикл "
                + "спричинить помилку. Для цього потрібен Iterator або метод removeIf()."));
        uk.add(LessonBlock.code(
                "List<String> list = new ArrayList<>(Arrays.asList(\"A\", \"B\", \"C\", \"D\"));\n"
                + "\n"
                + "// 1. Enhanced for (for-each) — простий та зрозумілий, підходить для читання\n"
                + "for (String s : list) {\n"
                + "    System.out.print(s + \" \"); // Виведе A B C D\n"
                + "}\n"
                + "\n"
                + "// 2. Iterator — для БЕЗПЕЧНОГО видалення під час обходу колекції\n"
                + "Iterator<String> it = list.iterator();\n"
                + "while (it.hasNext()) {\n"
                + "    String s = it.next();  // Отримуємо наступний елемент\n"
                + "    if (s.equals(\"B\")) {\n"
                + "        it.remove();       // БЕЗПЕЧНО видаляємо елемент \"B\"\n"
                + "    }\n"
                + "}\n"
                + "System.out.println(list);  // [A, C, D]"));
        uk.add(LessonBlock.warning(
                "Ніколи не використовуйте метод list.remove(s) всередині циклу for-each! "
                + "Це призведе до помилки ConcurrentModificationException. Колекція «зрозуміє», "
                + "що її модифікували ззовні циклу, і перерве роботу."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Використовуючи метод removeIf() з Java 8, який приймає лямбда-вираз, видаліть "
                + "усі слова з колекції List<String> words, які починаються на букву \"X\"."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "List<String> words = new ArrayList<>(Arrays.asList(\"Apple\", \"X-ray\", \"Banana\", \"Xenon\"));\n"
                + "\n"
                + "// s -> s.startsWith(\"X\") є умовою видалення.\n"
                + "words.removeIf(s -> s.startsWith(\"X\"));\n"
                + "\n"
                + "System.out.println(words); // [Apple, Banana]"));
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

    private static void addChapter10(Course s) {
        Chapter ch = new Chapter("Глава 10. Потоки введення-виведення",
                "Chapter 10. I/O streams");
        ch.add(materialFileNio());
        ch.add(materialByteAndCharStreams());
        s.add(ch);
    }

    private static Lesson materialFileNio() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Робота з файлами (NIO.2)"));
        uk.add(LessonBlock.paragraph(
                "Пакет java.nio.file (NIO.2, доступний з Java 7) — це сучасний, швидкий та "
                + "безпечний спосіб роботи з файловою системою. Він замінює старий і не завжди "
                + "зручний клас java.io.File. Основні класи тут: Path (шлях до файлу/папки) та "
                + "Files (утиліта для виконання операцій)."));
        uk.add(LessonBlock.code(
                "import java.nio.file.*;\n"
                + "import java.nio.charset.StandardCharsets;\n"
                + "import java.io.IOException;\n"
                + "import java.util.Arrays;\n"
                + "import java.util.List;\n"
                + "import java.util.Collections;\n"
                + "\n"
                + "// 1. Створення шляху (Path)\n"
                + "Path p = Paths.get(\"data\", \"users.txt\"); // Відносний шлях: data/users.txt\n"
                + "Path absolute = Paths.get(\"C:\\\\data\\\\file.txt\"); // Абсолютний шлях (Windows)\n"
                + "\n"
                + "// 2. Запис у файл (з перетиранням старого вмісту)\n"
                + "try {\n"
                + "    Files.write(p,\n"
                + "            Arrays.asList(\"Привіт, світ!\", \"Рядок 2\"),\n"
                + "            StandardCharsets.UTF_8);\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}\n"
                + "\n"
                + "// 3. Читання всього файлу (якщо файл не надто великий)\n"
                + "try {\n"
                + "    // Читання відразу у список рядків\n"
                + "    List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);\n"
                + "    lines.forEach(System.out::println);\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}\n"
                + "\n"
                + "// 4. Інші корисні операції\n"
                + "boolean exists = Files.exists(p);          // Чи існує файл?\n"
                + "boolean isFile = Files.isRegularFile(p);   // Це файл чи папка?\n"
                + "Files.deleteIfExists(p);                   // Видалити, якщо існує"));
        uk.add(LessonBlock.note(
                "Методи Files.readAllLines() та Files.readAllBytes() завантажують "
                + "ВЕСЬ файл в оперативну пам'ять (RAM). Якщо файл має розмір кілька гігабайт, "
                + "програма впаде з помилкою OutOfMemoryError. Для великих файлів використовуйте BufferedReader."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "У вас є шлях до файлу: Path file = Paths.get(\"hello.txt\"); "
                + "Як за допомогою класу Files записати туди один рядок \"Java 2026\"?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "Path file = Paths.get(\"hello.txt\");\n"
                + "try {\n"
                + "    // Collections.singletonList створює список з одного елемента\n"
                + "    Files.write(file, Collections.singletonList(\"Java 2026\"), StandardCharsets.UTF_8);\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
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

    private static Lesson materialByteAndCharStreams() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Потоки: байтові та символьні"));
        uk.add(LessonBlock.paragraph(
                "Потік (stream) — це послідовність даних. У Java I/O є два основних типи потоків:\n"
                + "1. Байтові (InputStream / OutputStream) — читають/пишуть сирі байти (картинки, відео, аудіо).\n"
                + "2. Символьні (Reader / Writer) — працюють із текстом (String, char), враховуючи кодування (UTF-8)."));
        uk.add(LessonBlock.code(
                "import java.io.*;\n"
                + "import java.nio.charset.StandardCharsets;\n"
                + "\n"
                + "// 1. Байтові потоки: копіювання зображення\n"
                + "try (FileInputStream in = new FileInputStream(\"source.jpg\");\n"
                + "     FileOutputStream out = new FileOutputStream(\"copy.jpg\")) {\n"
                + "    byte[] buffer = new byte[8192]; // Читаємо по 8 КБ за раз\n"
                + "    int bytesRead;\n"
                + "    while ((bytesRead = in.read(buffer)) != -1) {\n"
                + "        out.write(buffer, 0, bytesRead);\n"
                + "    }\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}\n"
                + "\n"
                + "// 2. Символьні потоки з буферизацією (читання великого тексту по рядках)\n"
                + "try (BufferedReader br = new BufferedReader(new FileReader(\"data.txt\", StandardCharsets.UTF_8));\n"
                + "     BufferedWriter bw = new BufferedWriter(new FileWriter(\"out.txt\", StandardCharsets.UTF_8))) {\n"
                + "    String line;\n"
                + "    while ((line = br.readLine()) != null) {\n"
                + "        bw.write(line);\n"
                + "        bw.newLine(); // Додає перехід на новий рядок\n"
                + "    }\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Блок try-with-resources (круглі дужки після слова try) АВТОМАТИЧНО закриває "
                + "потоки після використання (викликає метод close()). Завжди використовуйте його, "
                + "щоб уникнути витоку пам'яті (memory leak) та блокування файлів в ОС!"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Як правильно відкрити файл \"log.txt\" для ДОПИСУВАННЯ (append), а не перетирання "
                + "за допомогою FileWriter?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "// Передаємо параметр true (append = true) у конструктор FileWriter\n"
                + "try (FileWriter fw = new FileWriter(\"log.txt\", StandardCharsets.UTF_8, true);\n"
                + "     BufferedWriter bw = new BufferedWriter(fw)) {\n"
                + "    \n"
                + "    bw.write(\"Новий запис у лог\");\n"
                + "    bw.newLine();\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}"));
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

    private static void addChapter11(Course s) {
        Chapter ch = new Chapter("Глава 11. Багатопоточність",
                "Chapter 11. Multithreading");
        ch.add(materialThreadsCreation());
        ch.add(materialSynchronization());
        ch.add(materialExecutorService());
        s.add(ch);
    }

    private static Lesson materialThreadsCreation() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Створення потоків (Threads)"));
        uk.add(LessonBlock.paragraph(
                "Багатопоточність дозволяє вашій програмі виконувати кілька завдань ОДНОЧАСНО. "
                + "Наприклад, поки один потік завантажує файл з інтернету, інший — "
                + "малює анімацію на екрані. У Java є два основних способи створити потік: "
                + "успадкувати клас Thread або реалізувати інтерфейс Runnable."));
        uk.add(LessonBlock.code(
                "// Спосіб 1: Створення потоку за допомогою лямбда-виразу (Runnable)\n"
                + "Thread t1 = new Thread(() -> {\n"
                + "    for (int i = 0; i < 5; i++) {\n"
                + "        System.out.println(\"Потік 1 працює: \" + i);\n"
                + "        try {\n"
                + "            Thread.sleep(100); // Пауза на 100 мілісекунд\n"
                + "        } catch (InterruptedException e) {\n"
                + "            e.printStackTrace();\n"
                + "        }\n"
                + "    }\n"
                + "});\n"
                + "\n"
                + "// Спосіб 2: Створення об'єкта Runnable (краще для тестування)\n"
                + "Runnable task = () -> {\n"
                + "    for (int i = 0; i < 5; i++) {\n"
                + "        System.out.println(\"Потік 2 працює: \" + i);\n"
                + "    }\n"
                + "};\n"
                + "Thread t2 = new Thread(task, \"Мій-Потік-2\");\n"
                + "\n"
                + "// ЗАПУСК потоків\n"
                + "t1.start();  // Метод start() створює новий потік та викликає в ньому run()\n"
                + "t2.start();\n"
                + "\n"
                + "try {\n"
                + "    t1.join();  // Головний потік чекає, поки t1 завершиться\n"
                + "    t2.join();  // Головний потік чекає, поки t2 завершиться\n"
                + "} catch (InterruptedException e) {\n"
                + "    e.printStackTrace();\n"
                + "}\n"
                + "System.out.println(\"Обидва потоки завершили свою роботу!\");"));
        uk.add(LessonBlock.warning(
                "Найчастіша помилка новачків: викликати метод run() замість start(). "
                + "Метод start() каже Java створити новий потік. Якщо ви викличете run(), "
                + "код просто виконається у поточному потоці, і ніякої багатопоточності не буде!"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть потік за допомогою Runnable, який просто виводить у консоль \"Привіт з потоку!\". "
                + "Не забудьте запустити його."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "Runnable myTask = () -> System.out.println(\"Привіт з потоку!\");\n"
                + "Thread myThread = new Thread(myTask);\n"
                + "myThread.start();"));
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

    private static Lesson materialSynchronization() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Синхронізація та Race Condition"));
        uk.add(LessonBlock.paragraph(
                "Race condition (стан гонитви) — це помилка, яка виникає, коли два потоки "
                + "одночасно намагаються змінити одну й ту саму змінну. Оскільки операція типу "
                + "count++ складається з трьох кроків (прочитати, додати 1, записати), потоки "
                + "можуть «перебити» один одного, і ви втратите дані. Щоб цього уникнути, "
                + "використовується синхронізація."));
        uk.add(LessonBlock.code(
                "// БЕЗ синхронізації (погана ідея!)\n"
                + "class UnsafeCounter {\n"
                + "    private int count = 0;\n"
                + "    void inc() { count++; }\n"
                + "    int get() { return count; }\n"
                + "}\n"
                + "\n"
                + "// З СИНХРОНІЗАЦІЄЮ (правильний підхід)\n"
                + "class SafeCounter {\n"
                + "    private int count = 0;\n"
                + "    \n"
                + "    // synchronized гарантує, що сюди зможе зайти тільки один потік одночасно\n"
                + "    synchronized void inc() {\n"
                + "        count++;\n"
                + "    }\n"
                + "    \n"
                + "    int get() {\n"
                + "        return count;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "SafeCounter counter = new SafeCounter();\n"
                + "\n"
                + "// Два потоки додають по 1000\n"
                + "Runnable r = () -> { \n"
                + "    for (int i = 0; i < 1000; i++) counter.inc(); \n"
                + "};\n"
                + "\n"
                + "Thread t1 = new Thread(r);\n"
                + "Thread t2 = new Thread(r);\n"
                + "t1.start(); \n"
                + "t2.start();\n"
                + "// Зачекаємо їх ... (тут потрібен join)\n"
                + "System.out.println(counter.get());  // Завжди буде 2000"));
        uk.add(LessonBlock.heading("Атомарні змінні (java.util.concurrent.atomic)"));
        uk.add(LessonBlock.paragraph(
                "Слово synchronized сповільнює програму (один потік працює, інші чекають в черзі). "
                + "Для простих чисел краще використовувати класи на кшталт AtomicInteger, які "
                + "виконують операції безпечно на рівні процесора, без блокування."));
        uk.add(LessonBlock.code(
                "import java.util.concurrent.atomic.AtomicInteger;\n"
                + "\n"
                + "AtomicInteger atomicCount = new AtomicInteger(0);\n"
                + "\n"
                + "// Безпечно додає 1 з будь-якого потоку\n"
                + "atomicCount.incrementAndGet();\n"
                + "\n"
                + "System.out.println(atomicCount.get());"));
        uk.add(LessonBlock.note(
                "Ключове слово volatile гарантує, що зміни змінної відразу будуть видимі "
                + "для всіх інших потоків (відключає кешування процесора), але воно НЕ ЗАХИЩАЄ "
                + "від race condition при зміні (наприклад, при count++)."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "У вас є змінна-лічильник AtomicInteger score = new AtomicInteger(10);. "
                + "Як безпечно додати до неї значення 5?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "AtomicInteger score = new AtomicInteger(10);\n"
                + "// Метод addAndGet безпечно додає число\n"
                + "score.addAndGet(5);\n"
                + "System.out.println(score.get()); // 15"));
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

    private static Lesson materialExecutorService() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("ExecutorService: Пул потоків"));
        uk.add(LessonBlock.paragraph(
                "Створення нового об'єкта Thread для кожної задачі — це дуже «дорого» для системи. "
                + "Краще створити кілька потоків один раз (пул потоків) і передавати їм нові "
                + "завдання по мірі їх надходження. За це в Java відповідає інтерфейс ExecutorService."));
        uk.add(LessonBlock.code(
                "import java.util.concurrent.*;\n"
                + "\n"
                + "// Створюємо пул з 4 потоків, які будуть працювати весь час\n"
                + "ExecutorService pool = Executors.newFixedThreadPool(4);\n"
                + "\n"
                + "// Відправляємо пулу 10 задач\n"
                + "for (int i = 0; i < 10; i++) {\n"
                + "    final int taskNumber = i;\n"
                + "    pool.submit(() -> {\n"
                + "        System.out.println(\"Виконується задача \" + taskNumber \n"
                + "            + \" у потоці \" + Thread.currentThread().getName());\n"
                + "    });\n"
                + "}\n"
                + "\n"
                + "// Обов'язково закриваємо пул, інакше програма не завершиться!\n"
                + "pool.shutdown();"));
        uk.add(LessonBlock.heading("Future: отримання результату з потоку"));
        uk.add(LessonBlock.paragraph(
                "Runnable не може повертати результат. Якщо ви хочете, щоб потік обчислив і повернув "
                + "якесь значення, використовуйте інтерфейс Callable та об'єкт Future."));
        uk.add(LessonBlock.code(
                "ExecutorService pool = Executors.newSingleThreadExecutor();\n"
                + "\n"
                + "// Callable повертає значення (у цьому випадку Integer)\n"
                + "Future<Integer> future = pool.submit(() -> {\n"
                + "    Thread.sleep(2000); // Симуляція довгої роботи (2 секунди)\n"
                + "    return 42;\n"
                + "});\n"
                + "\n"
                + "System.out.println(\"Тут ми можемо робити інші справи...\");\n"
                + "\n"
                + "try {\n"
                + "    // Метод get() БЛОКУЄ головний потік, поки результат не буде готовий!\n"
                + "    Integer result = future.get();\n"
                + "    System.out.println(\"Результат обчислення: \" + result); // 42\n"
                + "} catch (Exception e) {\n"
                + "    e.printStackTrace();\n"
                + "}\n"
                + "pool.shutdown();"));
        uk.add(LessonBlock.warning(
                "Завжди викликайте shutdown() (завершує роботу після виконання поточних задач) або "
                + "shutdownNow() (намагається зупинити все негайно). Активний пул не дасть "
                + "вашій програмі закритися."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть Executors.newSingleThreadExecutor(). Надішліть йому задачу, яка виведе "
                + "\"Hello Executor!\". Після цього правильно закрийте пул."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "ExecutorService executor = Executors.newSingleThreadExecutor();\n"
                + "executor.submit(() -> System.out.println(\"Hello Executor!\"));\n"
                + "executor.shutdown();"));
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

    private static void addChapter12(Course s) {
        Chapter ch = new Chapter("Глава 12. Лямбда-вирази та Stream API",
                "Chapter 12. Lambda and Stream API");
        ch.add(materialLambdaBasics());
        ch.add(materialStreamPipeline());
        ch.add(materialCollectors());
        s.add(ch);
    }

    private static Lesson materialLambdaBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Лямбда-вирази: анонімні функції"));
        uk.add(LessonBlock.paragraph(
                "Лямбда-вираз — це короткий та зручний спосіб запису анонімних класів, що реалізують "
                + "функціональний інтерфейс (інтерфейс, який має ТІЛЬКИ ОДИН абстрактний метод). "
                + "Вони дозволяють передавати логіку (код) як параметр у методи."));
        uk.add(LessonBlock.code(
                "// ДО Java 8: анонімний клас (багато зайвого коду)\n"
                + "Runnable r1 = new Runnable() {\n"
                + "    @Override\n"
                + "    public void run() { \n"
                + "        System.out.println(\"Hello\"); \n"
                + "    }\n"
                + "};\n"
                + "\n"
                + "// З Java 8: лямбда-вираз — те саме, але в один рядок!\n"
                + "Runnable r2 = () -> System.out.println(\"Hello\");\n"
                + "\n"
                + "// З параметрами (типи параметрів компілятор вгадує сам)\n"
                + "Comparator<String> cmp = (a, b) -> a.length() - b.length();\n"
                + "\n"
                + "// Тіло з кількома рядками коду (потрібні фігурні дужки {})\n"
                + "Function<String, Integer> parser = s -> {\n"
                + "    s = s.trim();\n"
                + "    return Integer.parseInt(s);\n"
                + "};\n"
                + "\n"
                + "// Method reference (посилання на метод) — найкоротший запис\n"
                + "Function<String, Integer> len = String::length; // Замість s -> s.length()\n"
                + "Consumer<String> printer = System.out::println; // Замість s -> System.out.println(s)"));
        uk.add(LessonBlock.list(
                "(x, y) -> x + y           — два параметри, неявно повертає результат",
                "x -> x * x                — один параметр (дужки можна не писати)",
                "() -> System.out.println() — без параметрів",
                "x -> { return x * 2; }    — тіло в фігурних дужках вимагає слова return"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Напишіть лямбда-вираз для інтерфейсу Consumer<Integer>, який приймає число x "
                + "і виводить на екран його квадрат (x * x)."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "// Consumer приймає аргумент, але нічого не повертає (void)\n"
                + "Consumer<Integer> printSquare = x -> System.out.println(x * x);\n"
                + "\n"
                + "printSquare.accept(5); // Виведе 25"));
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

    private static Lesson materialStreamPipeline() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Stream API: Конвеєр обробки даних"));
        uk.add(LessonBlock.paragraph(
                "Stream (потік даних) — це «конвеєр» для обробки елементів колекції або масиву. "
                + "Замість того, щоб писати цикли for і багато if, ви описуєте ЩО хочете зробити: "
                + "відфільтрувати, змінити, відсортувати, зібрати."));
        uk.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\n"
                + "    \"Іван\", \"Олена\", \"Андрій\", \"Марія\", \"Богдан\");\n"
                + "\n"
                + "// Ланцюжок: filter → map → sorted → collect\n"
                + "List<String> result = names.stream()                   // 1. Створюємо стрім\n"
                + "    .filter(n -> n.length() > 4)          // 2. Залишаємо імена довші за 4 літери\n"
                + "    .map(String::toUpperCase)             // 3. Переводимо всі літери у верхній регістр\n"
                + "    .sorted()                             // 4. Сортуємо за алфавітом\n"
                + "    .toList();                            // 5. Збираємо назад у список (з Java 16)\n"
                + "// Результат: [АНДРІЙ, БОГДАН, МАРІЯ, ОЛЕНА]\n"
                + "\n"
                + "// Підрахунок суми чисел\n"
                + "int sum = IntStream.rangeClosed(1, 100)   // Числа від 1 до 100 включно\n"
                + "    .sum();                               // Результат: 5050\n"
                + "\n"
                + "// Перевірки (чи є хоч один такий елемент?)\n"
                + "boolean hasLong = names.stream()\n"
                + "    .anyMatch(n -> n.length() > 6);       // true (наприклад, \"Богдан\" має 6)\n"
                + "\n"
                + "// flatMap — розгортання вкладених колекцій/масивів\n"
                + "List<String> sentences = Arrays.asList(\"hello world\", \"java stream\");\n"
                + "List<String> words = sentences.stream()\n"
                + "    .flatMap(s -> Arrays.stream(s.split(\" \")))\n"
                + "    .toList();\n"
                + "// [hello, world, java, stream]"));
        uk.add(LessonBlock.list(
                "Проміжні операції (повертають новий Stream): filter, map, flatMap, sorted, distinct, limit, skip.",
                "Термінальні операції (запускають конвеєр і повертають результат): collect, toList, forEach, sum, count, anyMatch, findFirst."));
        uk.add(LessonBlock.warning(
                "Стрім виконується ЛІНИВО (lazy)! Якщо ви напишете проміжні операції, але "
                + "не викличете термінальну (наприклад, collect або forEach), жоден елемент "
                + "не буде оброблено. Стрім також можна використати лише один раз!"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "У вас є список чисел: Arrays.asList(1, 2, 3, 4, 5, 6). "
                + "За допомогою Stream API відфільтруйте лише парні числа (x % 2 == 0) та "
                + "порахуйте їхню кількість за допомогою методу count()."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6);\n"
                + "long evenCount = nums.stream()\n"
                + "    .filter(x -> x % 2 == 0)\n"
                + "    .count();\n"
                + "System.out.println(evenCount); // 3 (це числа 2, 4, 6)"));
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

    private static Lesson materialCollectors() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Collectors: Збирання результатів"));
        uk.add(LessonBlock.paragraph(
                "Метод collect() — це термінальна операція, яка перетворює Stream у "
                + "іншу структуру даних (List, Set, Map) або об'єднує елементи (String). "
                + "Для цього використовується клас-утиліта Collectors."));
        uk.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\"Іван\", \"Олена\", \"Андрій\", \"Марія\");\n"
                + "\n"
                + "// 1. З'єднання рядків (joining)\n"
                + "String csv = names.stream().collect(Collectors.joining(\", \"));\n"
                + "// Результат: \"Іван, Олена, Андрій, Марія\"\n"
                + "\n"
                + "// 2. Збирання у множину (Set) для видалення дублікатів\n"
                + "Set<String> uniqueNames = names.stream().collect(Collectors.toSet());\n"
                + "\n"
                + "// 3. Групування елементів (groupingBy)\n"
                + "// Групуємо імена за їх довжиною\n"
                + "Map<Integer, List<String>> byLength = names.stream()\n"
                + "    .collect(Collectors.groupingBy(String::length));\n"
                + "// Результат: {4=[Іван, Марія], 5=[Олена], 6=[Андрій]}\n"
                + "\n"
                + "// 4. Поділ на дві групи (partitioningBy)\n"
                + "// Завжди створює Map з двома ключами: true та false\n"
                + "Map<Boolean, List<String>> parts = names.stream()\n"
                + "    .collect(Collectors.partitioningBy(n -> n.length() > 4));\n"
                + "// Результат: {false=[Іван], true=[Олена, Андрій, Марія]}\n"
                + "\n"
                + "// 5. Перетворення на словник (toMap)\n"
                + "Map<String, Integer> nameLengths = names.stream()\n"
                + "    .collect(Collectors.toMap(n -> n, String::length));\n"
                + "// Результат: {Іван=4, Олена=5, Андрій=6, Марія=5}"));
        uk.add(LessonBlock.note(
                "Клас Collectors дуже потужний. Метод groupingBy можна комбінувати. Наприклад, "
                + "якщо вам потрібен не список імен, а просто КІЛЬКІСТЬ імен певної довжини: "
                + "Collectors.groupingBy(String::length, Collectors.counting())."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "У вас є список слів. Як за допомогою Collectors.joining() з'єднати їх в один рядок, "
                + "щоб вони були розділені дефісом \"-\"?"));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "List<String> words = Arrays.asList(\"Java\", \"is\", \"awesome\");\n"
                + "String result = words.stream()\n"
                + "    .collect(Collectors.joining(\"-\"));\n"
                + "System.out.println(result); // \"Java-is-awesome\""));
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

    private static void addChapter13(Course s) {
        Chapter ch = new Chapter("Глава 13. Узагальнення (Generics)",
                "Chapter 13. Generics");
        ch.add(materialGenericBasics());
        ch.add(materialBoundedTypes());
        ch.add(materialWildcards());
        s.add(ch);
    }

    private static Lesson materialGenericBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Generics: Типобезпечність"));
        uk.add(LessonBlock.paragraph(
                "Узагальнення (Generics) дозволяють створювати класи, інтерфейси або методи, "
                + "які можуть працювати з будь-якими типами даних. Головна перевага: "
                + "компілятор сам перевіряє типи під час написання коду, "
                + "запобігаючи виникненню помилки ClassCastException."));
        uk.add(LessonBlock.code(
                "// ДО Generics (як було в старих версіях Java)\n"
                + "class OldBox {\n"
                + "    Object value; // Може зберігати будь-що\n"
                + "    void set(Object v) { value = v; }\n"
                + "    Object get() { return value; }\n"
                + "}\n"
                + "\n"
                + "OldBox box = new OldBox();\n"
                + "box.set(\"Привіт\");\n"
                + "String s = (String) box.get();  // Необхідно робити приведення (кастинг) типу!\n"
                + "// Integer n = (Integer) box.get(); // Викличе ПОМИЛКУ під час роботи програми (ClassCastException)\n"
                + "\n"
                + "// З Generics — набагато безпечніше\n"
                + "// <T> — це \"параметр типу\", який ми вкажемо при створенні об'єкта\n"
                + "class Box<T> {\n"
                + "    private T value;\n"
                + "    void set(T v) { value = v; }\n"
                + "    T get() { return value; }\n"
                + "}\n"
                + "\n"
                + "Box<String> stringBox = new Box<>();\n"
                + "stringBox.set(\"Привіт\");\n"
                + "String s2 = stringBox.get();   // Кастинг більше не потрібен!\n"
                + "// stringBox.set(42);          // Помилка ще на етапі КОМПІЛЯЦІЇ (це добре!)"));
        uk.add(LessonBlock.note(
                "Букви-маркери, які зазвичай використовують у Generics: "
                + "T (Type), E (Element, використовується в колекціях), "
                + "K (Key) та V (Value, для мап), N (Number). Це просто домовленість."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Напишіть клас Pair<K, V>, який може зберігати два значення різних типів: "
                + "перше (first) типу K, друге (second) типу V. Напишіть для них конструктор та гетери."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "class Pair<K, V> {\n"
                + "    private K first;\n"
                + "    private V second;\n"
                + "    \n"
                + "    public Pair(K first, V second) {\n"
                + "        this.first = first;\n"
                + "        this.second = second;\n"
                + "    }\n"
                + "    \n"
                + "    public K getFirst() { return first; }\n"
                + "    public V getSecond() { return second; }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "Pair<String, Integer> p = new Pair<>(\"Вік\", 25);"));
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

    private static Lesson materialBoundedTypes() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Обмеження типів (Bounds)"));
        uk.add(LessonBlock.paragraph(
                "Іноді ви хочете вказати, що параметр типу <T> може бути не абсолютно будь-яким, "
                + "а лише таким, що успадковує певний клас або реалізує певний інтерфейс. "
                + "Для цього використовується ключове слово extends."));
        uk.add(LessonBlock.code(
                "// Метод, який працює тільки з числами (класами, які успадковують Number)\n"
                + "public static <T extends Number> double sum(T num1, T num2) {\n"
                + "    return num1.doubleValue() + num2.doubleValue();\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "System.out.println(sum(10, 20.5)); // 30.5\n"
                + "// sum(\"A\", \"B\"); // Помилка компіляції: String не є нащадком Number\n"
                + "\n"
                + "// Можна обмежувати і інтерфейсами (наприклад, Comparable)\n"
                + "// Це дозволить передавати лише об'єкти, які можна порівнювати\n"
                + "public static <T extends Comparable<T>> T findMax(T a, T b) {\n"
                + "    if (a.compareTo(b) >= 0) {\n"
                + "        return a;\n"
                + "    }\n"
                + "    return b;\n"
                + "}\n"
                + "\n"
                + "System.out.println(findMax(3, 5));      // 5\n"
                + "System.out.println(findMax(\"a\", \"z\"));  // \"z\""));
        uk.add(LessonBlock.note(
                "У Generics слово extends використовується як для класів, так і для інтерфейсів "
                + "(слово implements тут не застосовується). Якщо вам потрібно кілька обмежень "
                + "одночасно, використовуйте символ &: <T extends Number & Comparable<T>>."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Напишіть узагальнений метод printNumber(T item), який приймає лише об'єкти типу Number "
                + "і просто виводить їх на екран."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "public static <T extends Number> void printNumber(T item) {\n"
                + "    System.out.println(\"Число: \" + item);\n"
                + "}\n"
                + "\n"
                + "printNumber(42);   // int (автобоксинг у Integer)\n"
                + "printNumber(3.14); // double (Double)"));
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

    private static Lesson materialWildcards() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Wildcards (Символи підстановки)"));
        uk.add(LessonBlock.paragraph(
                "У Generics типи інваріантні. Це означає, що List<Integer> НЕ є нащадком List<Number>. "
                + "Щоб метод міг приймати списки різних, але пов'язаних типів, використовують wildcards (?)."));
        uk.add(LessonBlock.code(
                "// ? extends Number — \"Тільки читання\"\n"
                + "// Метод приймає List будь-яких об'єктів, які є нащадками Number (Integer, Double...)\n"
                + "static double sum(List<? extends Number> list) {\n"
                + "    double total = 0;\n"
                + "    for (Number n : list) { // Ми можемо БЕЗПЕЧНО читати як Number\n"
                + "        total += n.doubleValue();\n"
                + "    }\n"
                + "    // list.add(10); // ПОМИЛКА! Невідомо, який саме там тип (може це List<Double>)\n"
                + "    return total;\n"
                + "}\n"
                + "\n"
                + "// ? super Integer — \"Запис дозволено\"\n"
                + "// Метод приймає List об'єктів типу Integer або БУДЬ-ЯКОГО його предка (Number, Object)\n"
                + "static void addNumbers(List<? super Integer> list) {\n"
                + "    list.add(1);\n"
                + "    list.add(2);\n"
                + "    // Ми можемо БЕЗПЕЧНО додавати Integer, бо всі \"предки\" його підтримують\n"
                + "    // Number n = list.get(0); // ПОМИЛКА! При читанні гарантовано лише Object\n"
                + "}"));
        uk.add(LessonBlock.heading("Принцип PECS"));
        uk.add(LessonBlock.paragraph(
                "Щоб не плутатися, використовуйте правило PECS (Producer Extends, Consumer Super): "
                + "Якщо колекція ПРОДЮСЕР (ви тільки читаєте з неї) — використовуйте ? extends T. "
                + "Якщо колекція СПОЖИВАЧ (ви тільки записуєте в неї) — використовуйте ? super T."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Напишіть метод copy(List<? extends Number> source, List<? super Number> destination), "
                + "який копіює всі елементи з колекції-джерела (source) у колекцію-призначення (destination)."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "public static void copy(List<? extends Number> source, List<? super Number> destination) {\n"
                + "    for (Number num : source) { // Читаємо з extends\n"
                + "        destination.add(num);   // Пишемо в super\n"
                + "    }\n"
                + "}"));
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

    private static void addChapter14(Course s) {
        Chapter ch = new Chapter("Глава 14. Перерахування enum",
                "Chapter 14. Enumerations");
        ch.add(materialEnumBasics());
        ch.add(materialEnumWithFields());
        s.add(ch);
    }

    private static Lesson materialEnumBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("enum: Перерахування (Обмежений набір значень)"));
        uk.add(LessonBlock.paragraph(
                "Перерахування (enum) — це спеціальний клас, який представляє собою групу "
                + "фіксованих констант (незмінних змінних). Наприклад, дні тижня, пори року "
                + "або стани замовлення (нове, в обробці, доставлене). Це робить ваш код "
                + "безпечнішим, оскільки ви не зможете передати туди якесь випадкове число чи рядок."));
        uk.add(LessonBlock.code(
                "// Оголошення enum (зазвичай робиться в окремому файлі, як клас)\n"
                + "enum Day {\n"
                + "    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY,\n"
                + "    SATURDAY, SUNDAY // Заведено писати ВЕЛИКИМИ літерами\n"
                + "}\n"
                + "\n"
                + "Day today = Day.WEDNESDAY;\n"
                + "\n"
                + "// switch ідеально працює з enum (без ризику помилитися у написанні рядка)\n"
                + "switch (today) {\n"
                + "    case SATURDAY: \n"
                + "    case SUNDAY:\n"
                + "        System.out.println(\"Вихідний!\"); \n"
                + "        break;\n"
                + "    default:\n"
                + "        System.out.println(\"Робочий день\");\n"
                + "}\n"
                + "\n"
                + "// Корисні вбудовані методи\n"
                + "System.out.println(today.name());           // Виведе \"WEDNESDAY\" (як рядок)\n"
                + "System.out.println(today.ordinal());        // Виведе 2 (порядковий номер, починається з 0)\n"
                + "\n"
                + "// Перетворення рядка в об'єкт enum\n"
                + "Day monday = Day.valueOf(\"MONDAY\"); \n"
                + "\n"
                + "// Перебір ВСІХ можливих значень\n"
                + "for (Day d : Day.values()) {\n"
                + "    System.out.println(d.ordinal() + \": \" + d.name());\n"
                + "}"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть enum Color з трьома значеннями: RED, GREEN, BLUE. "
                + "Використовуючи цикл foreach і метод values(), виведіть їх усі на екран."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "enum Color { RED, GREEN, BLUE }\n"
                + "\n"
                + "// ... у методі main:\n"
                + "for (Color c : Color.values()) {\n"
                + "    System.out.println(c);\n"
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

    private static Lesson materialEnumWithFields() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("enum з полями та методами"));
        uk.add(LessonBlock.paragraph(
                "У Java enum — це повноцінний клас. Це означає, що константи можуть "
                + "мати свої власні поля (змінні), конструктори та методи. Це неймовірно потужний інструмент "
                + "для зв'язування даних зі значеннями."));
        uk.add(LessonBlock.code(
                "enum Season {\n"
                + "    WINTER(\"Зима\", -5),\n"
                + "    SPRING(\"Весна\", 15),\n"
                + "    SUMMER(\"Літо\", 30),\n"
                + "    AUTUMN(\"Осінь\", 10);\n"
                + "\n"
                + "    // Поля для кожної константи (краще робити їх final)\n"
                + "    private final String ukrainianName;\n"
                + "    private final int averageTemp;\n"
                + "\n"
                + "    // Конструктор enum ЗАВЖДИ private (компілятор робить це сам).\n"
                + "    // Ви не можете створити Season через оператор new.\n"
                + "    Season(String ukrainianName, int averageTemp) {\n"
                + "        this.ukrainianName = ukrainianName;\n"
                + "        this.averageTemp = averageTemp;\n"
                + "    }\n"
                + "\n"
                + "    // Звичайні гетери\n"
                + "    public String getUkrainianName() { return ukrainianName; }\n"
                + "    public int getAverageTemp() { return averageTemp; }\n"
                + "\n"
                + "    // Звичайний метод\n"
                + "    public boolean isCold() { return averageTemp <= 0; }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "for (Season s : Season.values()) {\n"
                + "    System.out.print(s.getUkrainianName() + \": \" + s.getAverageTemp() + \"°C\");\n"
                + "    if (s.isCold()) {\n"
                + "        System.out.print(\" (холодно!)\");\n"
                + "    }\n"
                + "    System.out.println();\n"
                + "}\n"
                + "// Зима: -5°C (холодно!)\n"
                + "// Весна: 15°C\n"
                + "// Літо: 30°C\n"
                + "// Осінь: 10°C"));
        uk.add(LessonBlock.note(
                "Хоча enum є класом, він НЕ МОЖЕ успадковувати інші класи (бо він вже "
                + "неявно успадковує клас java.lang.Enum). Проте він МОЖЕ реалізовувати інтерфейси."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Додайте до вашого enum Color (RED, GREEN, BLUE) поле String hexCode (наприклад, \"#FF0000\" для RED), "
                + "конструктор та метод getHexCode()."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "enum Color {\n"
                + "    RED(\"#FF0000\"), GREEN(\"#00FF00\"), BLUE(\"#0000FF\");\n"
                + "    \n"
                + "    private final String hex;\n"
                + "    \n"
                + "    Color(String hex) {\n"
                + "        this.hex = hex;\n"
                + "    }\n"
                + "    \n"
                + "    public String getHex() { return hex; }\n"
                + "}\n"
                + "\n"
                + "System.out.println(Color.RED.getHex()); // #FF0000"));
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

    private static void addChapter15(Course s) {
        Chapter ch = new Chapter("Глава 15. Анотації", "Chapter 15. Annotations");
        ch.add(materialStandardAnnotations());
        ch.add(materialCustomAnnotations());
        s.add(ch);
    }

    private static Lesson materialStandardAnnotations() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Стандартні анотації"));
        uk.add(LessonBlock.paragraph(
                "Анотація — це спеціальні метадані (мітки), які ви можете додавати до класів, "
                + "методів, змінних або параметрів. Вони починаються з символу @. Самі по собі "
                + "анотації нічого не робять, але компілятор або різні фреймворки (як Spring чи Hibernate) "
                + "читають їх та змінюють поведінку програми відповідно."));
        uk.add(LessonBlock.code(
                "// @Override — найпопулярніша анотація.\n"
                + "// Вона каже компілятору: \"я планую перевизначити метод батьківського класу\".\n"
                + "class Animal {\n"
                + "    String sound() { return \"...\"; }\n"
                + "}\n"
                + "class Dog extends Animal {\n"
                + "    @Override\n"
                + "    String sound() { return \"Гав!\"; }  // Все добре\n"
                + "    \n"
                + "    // @Override\n"
                + "    // String soudn() { return \"Гав!\"; } // ПОМИЛКА КОМПІЛЯЦІЇ (описка в назві)!\n"
                + "    // Без @Override описка б не помітилася, і це був би просто новий метод.\n"
                + "}\n"
                + "\n"
                + "// @Deprecated — попереджає програмістів, що метод застарів, \n"
                + "// містить баги, або є краща альтернатива, і його не варто використовувати.\n"
                + "@Deprecated\n"
                + "void oldMethod() { }\n"
                + "\n"
                + "// @SuppressWarnings — просить компілятор не показувати певні попередження.\n"
                + "@SuppressWarnings(\"unchecked\")\n"
                + "List<String> list = (List<String>) rawList;\n"
                + "\n"
                + "// @FunctionalInterface — гарантує, що інтерфейс має рівно один абстрактний метод \n"
                + "// (ідеально для лямбда-виразів).\n"
                + "@FunctionalInterface\n"
                + "interface MathOperation {\n"
                + "    int operate(int a, int b);\n"
                + "}"));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть інтерфейс Worker з одним методом void work(). "
                + "Позначте його анотацією @FunctionalInterface. Потім створіть клас Builder, "
                + "який імплементує Worker, і позначте метод work() анотацією @Override."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@FunctionalInterface\n"
                + "interface Worker {\n"
                + "    void work();\n"
                + "}\n"
                + "\n"
                + "class Builder implements Worker {\n"
                + "    @Override\n"
                + "    public void work() {\n"
                + "        System.out.println(\"Будівельник працює.\");\n"
                + "    }\n"
                + "}"));
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

    private static Lesson materialCustomAnnotations() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Власні анотації та Рефлексія"));
        uk.add(LessonBlock.paragraph(
                "Ви можете створювати власні анотації за допомогою ключового слова @interface. "
                + "Щоб вказати, де саме можна застосувати вашу анотацію, та як довго вона має жити, "
                + "використовують мета-анотації @Target та @Retention."));
        uk.add(LessonBlock.code(
                "// 1. Оголошення власної анотації\n"
                + "@Retention(RetentionPolicy.RUNTIME)    // Живе під час виконання програми (щоб можна було прочитати)\n"
                + "@Target(ElementType.METHOD)            // Можна ставити ТІЛЬКИ над методами\n"
                + "@interface LogExecutionTime { \n"
                + "    // Анотація може мати параметри (як методи без тіла)\n"
                + "    // String value() default \"Info\";\n"
                + "}\n"
                + "\n"
                + "// 2. Використання\n"
                + "class Service {\n"
                + "    @LogExecutionTime\n"
                + "    public void processData() {\n"
                + "        System.out.println(\"Обробка даних...\");\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.heading("Магія Рефлексії (Reflection)"));
        uk.add(LessonBlock.paragraph(
                "Рефлексія — це механізм Java, який дозволяє програмі \"дивитися на себе\" "
                + "під час виконання: дізнаватися, які є класи, методи, поля, та які анотації на них висять."));
        uk.add(LessonBlock.code(
                "// 3. Читання анотації через рефлексію в рантаймі\n"
                + "Service service = new Service();\n"
                + "// Отримуємо об'єкт класу, потім дістаємо інформацію про метод\n"
                + "Method method = Service.class.getMethod(\"processData\");\n"
                + "\n"
                + "// Перевіряємо, чи висить над методом наша анотація\n"
                + "if (method.isAnnotationPresent(LogExecutionTime.class)) {\n"
                + "    long start = System.nanoTime();\n"
                + "    \n"
                + "    // Викликаємо метод через рефлексію!\n"
                + "    method.invoke(service);\n"
                + "    \n"
                + "    long elapsed = System.nanoTime() - start;\n"
                + "    System.out.println(\"Час виконання: \" + elapsed + \" наносекунд\");\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Життєвий цикл (@Retention): SOURCE (видаляється компілятором, наприклад @Override), "
                + "CLASS (зберігається в байт-коді, але недоступна в рантаймі - за замовчуванням), "
                + "RUNTIME (доступна в рантаймі через рефлексію)."));
        uk.add(LessonBlock.heading("Практичне завдання"));
        uk.add(LessonBlock.paragraph(
                "Створіть анотацію @Important, яка діє в RUNTIME і може застосовуватися до TYPE (класів). "
                + "Створіть порожній клас MyClass і позначте його цією анотацією."));
        uk.add(LessonBlock.heading("Рішення"));
        uk.add(LessonBlock.code(
                "@Retention(RetentionPolicy.RUNTIME)\n"
                + "@Target(ElementType.TYPE)\n"
                + "@interface Important {}\n"
                + "\n"
                + "@Important\n"
                + "class MyClass {\n"
                + "}"));
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
