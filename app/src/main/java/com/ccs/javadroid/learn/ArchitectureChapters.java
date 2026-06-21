package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Гурток з архітектури та принципів проектування.
 */
final class ArchitectureChapters {

    static void add(Course s) {
        Chapter ch1 = new Chapter("Базові принципи розробки", "Basic Development Principles");
        ch1.add(materialDryKissYagni());
        s.add(ch1);

        Chapter ch2 = new Chapter("Принципи SOLID", "SOLID Principles");
        ch2.add(materialSRPandOCP());
        ch2.add(materialLSPandISP());
        ch2.add(materialDIP());
        s.add(ch2);

        Chapter ch3 = new Chapter("Архітектурні патерни", "Architectural Patterns");
        ch3.add(materialMVCandMVVM());
        ch3.add(materialCleanArchitecture());
        s.add(ch3);
    }

    private static Lesson materialDryKissYagni() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("DRY, KISS та YAGNI"));
        uk.add(LessonBlock.paragraph(
                "Ці три акроніми є фундаментом написання чистого, підтримуваного коду. "
                + "Вони допомагають уникнути надмірної складності та дублювання."));
        uk.add(LessonBlock.list(
                "DRY (Don't Repeat Yourself) — Не повторюйся. Кожен шматок знань "
                + "повинен мати єдине, однозначне представлення в системі. Дублювання коду призводить до помилок під час змін.",
                "KISS (Keep It Simple, Stupid) — Роби це простіше. Система працює найкраще, "
                + "якщо вона проста. Уникайте непотрібної складності та хитромудрих рішень там, де підійде очевидне.",
                "YAGNI (You Aren't Gonna Need It) — Вам це не знадобиться. Не пишіть код "
                + "для фіч, які «можливо знадобляться у майбутньому». Реалізуйте лише те, що потрібно зараз."));
        uk.add(LessonBlock.warning(
                "Надмірне застосування DRY може призвести до неправильних абстракцій (Wrong Abstraction). "
                + "Іноді невелике дублювання краще, ніж складна залежність."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("DRY, KISS, and YAGNI"));
        en.add(LessonBlock.paragraph(
                "These three acronyms are the foundation of writing clean, maintainable code. "
                + "They help avoid over-engineering and duplication."));
        en.add(LessonBlock.list(
                "DRY (Don't Repeat Yourself) — Every piece of knowledge must have a "
                + "single, unambiguous, authoritative representation within a system. Code duplication leads to bugs during changes.",
                "KISS (Keep It Simple, Stupid) — Systems work best when they are kept simple. "
                + "Avoid unnecessary complexity and clever solutions where an obvious one will do.",
                "YAGNI (You Aren't Gonna Need It) — Do not write code for features that you "
                + "think you might need in the future. Implement only what you need right now."));
        en.add(LessonBlock.warning(
                "Overapplying DRY can lead to the Wrong Abstraction. Sometimes a little duplication "
                + "is better than a complex dependency."));

        return new Lesson("arch.1", "DRY, KISS, YAGNI", "DRY, KISS, YAGNI", uk, en);
    }

    private static Lesson materialSRPandOCP() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("S & O з SOLID"));
        uk.add(LessonBlock.paragraph(
                "SOLID — це п'ять принципів об'єктно-орієнтованого проектування (від Роберта Мартіна)."));
        uk.add(LessonBlock.paragraph(
                "1. SRP (Single Responsibility Principle) — Принцип єдиної відповідальності. "
                + "Клас повинен мати лише одну причину для змін. Наприклад, клас User не повинен "
                + "одночасно зберігати дані користувача та вміти зберігати себе в базу даних. "
                + "Для БД має бути окремий UserRepository."));
        uk.add(LessonBlock.paragraph(
                "2. OCP (Open/Closed Principle) — Принцип відкритості/закритості. "
                + "Програмні сутності повинні бути відкриті для розширення, але закриті для модифікації. "
                + "Якщо ви хочете додати нову логіку, ви повинні додавати новий код (через інтерфейси та поліморфізм), "
                + "а не переписувати старий."));
        uk.add(LessonBlock.code(
                "// Порушення OCP (треба змінювати код при новому типі знижки)\n"
                + "double calculateDiscount(Order order, String type) {\n"
                + "    if (type.equals(\"VIP\")) return order.getTotal() * 0.2;\n"
                + "    if (type.equals(\"NEW\")) return order.getTotal() * 0.1;\n"
                + "    return 0;\n"
                + "}\n"
                + "\n"
                + "// Згідно з OCP (використання поліморфізму)\n"
                + "interface Discount { double calculate(Order order); }\n"
                + "class VipDiscount implements Discount { ... }\n"
                + "class NewDiscount implements Discount { ... }\n"
                + "double calculateDiscount(Order order, Discount discount) {\n"
                + "    return discount.calculate(order);\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("S & O of SOLID"));
        en.add(LessonBlock.paragraph(
                "SOLID represents five principles of object-oriented design (by Robert C. Martin)."));
        en.add(LessonBlock.paragraph(
                "1. SRP (Single Responsibility Principle) — A class should have one, and only one, "
                + "reason to change. For example, a User class should not both hold user data and "
                + "know how to save itself to a database. Database logic belongs in a UserRepository."));
        en.add(LessonBlock.paragraph(
                "2. OCP (Open/Closed Principle) — Software entities should be open for extension, "
                + "but closed for modification. If you want to add new behavior, you should add new "
                + "code (via interfaces and polymorphism) instead of rewriting existing code."));
        en.add(LessonBlock.code(
                "// OCP Violation (must modify code for a new discount type)\n"
                + "double calculateDiscount(Order order, String type) {\n"
                + "    if (type.equals(\"VIP\")) return order.getTotal() * 0.2;\n"
                + "    if (type.equals(\"NEW\")) return order.getTotal() * 0.1;\n"
                + "    return 0;\n"
                + "}\n"
                + "\n"
                + "// Following OCP (using polymorphism)\n"
                + "interface Discount { double calculate(Order order); }\n"
                + "class VipDiscount implements Discount { ... }\n"
                + "class NewDiscount implements Discount { ... }\n"
                + "double calculateDiscount(Order order, Discount discount) {\n"
                + "    return discount.calculate(order);\n"
                + "}"));

        return new Lesson("arch.2", "SRP та OCP", "SRP and OCP", uk, en);
    }

    private static Lesson materialLSPandISP() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("L & I з SOLID"));
        uk.add(LessonBlock.paragraph(
                "3. LSP (Liskov Substitution Principle) — Принцип підстановки Лісков. "
                + "Об'єкти суперкласу повинні замінюватися об'єктами підкласів без порушення роботи програми. "
                + "Якщо у вас є клас Птах із методом fly(), і клас Пінгвін успадковує Птах, але кидає "
                + "UnsupportedOperationException у fly() — це порушення LSP. Підклас не повинен порушувати "
                + "контракт базового класу."));
        uk.add(LessonBlock.paragraph(
                "4. ISP (Interface Segregation Principle) — Принцип розділення інтерфейсу. "
                + "Клієнти не повинні залежати від методів, які вони не використовують. "
                + "Краще створити кілька дрібних, специфічних інтерфейсів, ніж один «товстий» (fat interface)."));
        uk.add(LessonBlock.code(
                "// Порушення ISP\n"
                + "interface Worker {\n"
                + "    void work();\n"
                + "    void eat();\n"
                + "}\n"
                + "class Robot implements Worker {\n"
                + "    public void work() { /* працює */ }\n"
                + "    public void eat() { throw new UnsupportedException(); // Роботи не їдять! }\n"
                + "}\n"
                + "\n"
                + "// Згідно з ISP\n"
                + "interface Workable { void work(); }\n"
                + "interface Eatable { void eat(); }\n"
                + "class Human implements Workable, Eatable { ... }\n"
                + "class Robot implements Workable { ... }"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("L & I of SOLID"));
        en.add(LessonBlock.paragraph(
                "3. LSP (Liskov Substitution Principle) — Objects in a program should be replaceable "
                + "with instances of their subtypes without altering the correctness of that program. "
                + "If you have a Bird class with a fly() method, and a Penguin subclass throws "
                + "an UnsupportedOperationException in fly() — that violates LSP. A subclass must not "
                + "break the base class contract."));
        en.add(LessonBlock.paragraph(
                "4. ISP (Interface Segregation Principle) — Clients should not be forced to depend "
                + "upon interfaces that they do not use. It is better to have multiple small, specific "
                + "interfaces rather than one large, 'fat' interface."));
        en.add(LessonBlock.code(
                "// ISP Violation\n"
                + "interface Worker {\n"
                + "    void work();\n"
                + "    void eat();\n"
                + "}\n"
                + "class Robot implements Worker {\n"
                + "    public void work() { /* works */ }\n"
                + "    public void eat() { throw new UnsupportedException(); // Robots don't eat! }\n"
                + "}\n"
                + "\n"
                + "// Following ISP\n"
                + "interface Workable { void work(); }\n"
                + "interface Eatable { void eat(); }\n"
                + "class Human implements Workable, Eatable { ... }\n"
                + "class Robot implements Workable { ... }"));

        return new Lesson("arch.3", "LSP та ISP", "LSP and ISP", uk, en);
    }

    private static Lesson materialDIP() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("D з SOLID: Dependency Inversion"));
        uk.add(LessonBlock.paragraph(
                "5. DIP (Dependency Inversion Principle) — Принцип інверсії залежностей. "
                + "Високорівневі модулі не повинні залежати від низькорівневих модулів. Обидва "
                + "повинні залежати від абстракцій (інтерфейсів). Абстракції не повинні залежати "
                + "від деталей. Деталі повинні залежати від абстракцій."));
        uk.add(LessonBlock.paragraph(
                "Цей принцип є основою для Dependency Injection (DI) фреймворків, таких як Spring "
                + "чи Dagger/Hilt. Замість того, щоб клас створював свої залежності (через 'new'), "
                + "вони передаються йому ззовні через конструктор."));
        uk.add(LessonBlock.code(
                "// Порушення DIP: жорстка залежність\n"
                + "class OrderService {\n"
                + "    private MySQLDatabase db = new MySQLDatabase(); // жорстка прив'язка\n"
                + "    void save() { db.insert(); }\n"
                + "}\n"
                + "\n"
                + "// Згідно з DIP: залежність від абстракції\n"
                + "interface Database { void insert(); }\n"
                + "class OrderService {\n"
                + "    private Database db;\n"
                + "    // Dependency Injection через конструктор\n"
                + "    public OrderService(Database db) {\n"
                + "        this.db = db;\n"
                + "    }\n"
                + "    void save() { db.insert(); }\n"
                + "}"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("D of SOLID: Dependency Inversion"));
        en.add(LessonBlock.paragraph(
                "5. DIP (Dependency Inversion Principle) — High-level modules should not depend "
                + "on low-level modules. Both should depend on abstractions (interfaces). Abstractions "
                + "should not depend on details. Details should depend on abstractions."));
        en.add(LessonBlock.paragraph(
                "This principle is the foundation for Dependency Injection (DI) frameworks like Spring "
                + "or Dagger/Hilt. Instead of a class instantiating its dependencies (via 'new'), "
                + "they are injected into it from the outside via the constructor."));
        en.add(LessonBlock.code(
                "// DIP Violation: tight coupling\n"
                + "class OrderService {\n"
                + "    private MySQLDatabase db = new MySQLDatabase(); // hardcoded dependency\n"
                + "    void save() { db.insert(); }\n"
                + "}\n"
                + "\n"
                + "// Following DIP: depending on abstraction\n"
                + "interface Database { void insert(); }\n"
                + "class OrderService {\n"
                + "    private Database db;\n"
                + "    // Dependency Injection via constructor\n"
                + "    public OrderService(Database db) {\n"
                + "        this.db = db;\n"
                + "    }\n"
                + "    void save() { db.insert(); }\n"
                + "}"));

        return new Lesson("arch.4", "DIP (Інверсія)", "DIP (Inversion)", uk, en);
    }

    private static Lesson materialMVCandMVVM() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Патерни UI: MVC, MVP, MVVM"));
        uk.add(LessonBlock.paragraph(
                "Патерни представлення допомагають розділити бізнес-логіку та користувацький інтерфейс."));
        uk.add(LessonBlock.list(
                "MVC (Model-View-Controller) — Модель містить дані, View відображає їх, "
                + "а Controller обробляє ввід користувача і оновлює Модель. У класичному вебі (Spring MVC) працює чудово.",
                "MVP (Model-View-Presenter) — Популярний раніше в Android. View є пасивним (тільки відображає), "
                + "а Presenter містить логіку відображення та оновлює View напряму.",
                "MVVM (Model-View-ViewModel) — Сучасний стандарт (особливо з реактивним UI, як Android LiveData/Flow або React). "
                + "View підписується (спостерігає) за змінами у ViewModel. ViewModel не знає про View безпосередньо."));
        uk.add(LessonBlock.note(
                "MVVM забезпечує найкращу ізоляцію та тестування, оскільки ViewModel не має посилань "
                + "на класи платформи (наприклад, android.view.View)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("UI Patterns: MVC, MVP, MVVM"));
        en.add(LessonBlock.paragraph(
                "Presentation patterns help separate business logic from the user interface."));
        en.add(LessonBlock.list(
                "MVC (Model-View-Controller) — The Model holds data, View displays it, "
                + "and Controller handles user input to update the Model. Works great in classic web (Spring MVC).",
                "MVP (Model-View-Presenter) — Formerly popular in Android. The View is passive, "
                + "and the Presenter contains the UI logic, updating the View directly.",
                "MVVM (Model-View-ViewModel) — The modern standard (especially with reactive UI like Android LiveData/Flow). "
                + "The View observes changes in the ViewModel. The ViewModel has no direct reference to the View."));
        en.add(LessonBlock.note(
                "MVVM provides the best isolation and testability because the ViewModel has no references "
                + "to platform classes (e.g., android.view.View)."));

        return new Lesson("arch.5", "MVC та MVVM", "MVC and MVVM", uk, en);
    }

    private static Lesson materialCleanArchitecture() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Чиста Архітектура (Clean Architecture)"));
        uk.add(LessonBlock.paragraph(
                "Clean Architecture (від Роберта Мартіна) — це підхід до розділення системи на концентричні шари. "
                + "Головне правило: Залежності завжди спрямовані всередину, до ядра бізнес-правил."));
        uk.add(LessonBlock.list(
                "1. Entities (Сутності) — Ядро системи. Бізнес-об'єкти, що не залежать ні від чого.",
                "2. Use Cases (Сценарії) — Бізнес-логіка додатку. Вони оркеструють роботу з сутностями.",
                "3. Interface Adapters — Конвертують дані з формату Use Cases у формат, зручний для БД або UI (Presenters, Controllers, Gateways).",
                "4. Frameworks & Drivers — Зовнішній шар: Бази даних, UI, Web-фреймворки. Деталі реалізації."));
        uk.add(LessonBlock.paragraph(
                "Завдяки цьому підходу, ви можете повністю замінити базу даних (наприклад, MySQL на MongoDB) "
                + "або UI (Web на Mobile) без жодної зміни у ядрі бізнес-логіки (Use Cases та Entities). "
                + "Це досягається завдяки Dependency Inversion Principle (DIP)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Clean Architecture"));
        en.add(LessonBlock.paragraph(
                "Clean Architecture (by Robert C. Martin) is an approach to separating a system into concentric layers. "
                + "The Golden Rule: Dependencies must point inward, toward the core business rules."));
        en.add(LessonBlock.list(
                "1. Entities — The core. Enterprise business objects that depend on nothing.",
                "2. Use Cases — Application-specific business rules. They orchestrate entities.",
                "3. Interface Adapters — Convert data from Use Case format to formats convenient for DBs or UI (Presenters, Controllers, Gateways).",
                "4. Frameworks & Drivers — The outermost layer: Databases, UI, Web Frameworks. Implementation details."));
        en.add(LessonBlock.paragraph(
                "Thanks to this approach, you can completely swap out the database (e.g., MySQL to MongoDB) "
                + "or UI (Web to Mobile) without changing a single line in the core business logic (Use Cases and Entities). "
                + "This is achieved using the Dependency Inversion Principle (DIP)."));

        return new Lesson("arch.6", "Чиста Архітектура", "Clean Architecture", uk, en);
    }
}
