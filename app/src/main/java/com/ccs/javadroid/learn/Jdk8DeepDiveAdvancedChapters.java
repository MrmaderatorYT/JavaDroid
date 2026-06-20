package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Third wave of the JDK 8 deep-dive course: functional style, Optional,
 * regex, reflection, serialization, classpath, and diagnostics.
 */
final class Jdk8DeepDiveAdvancedChapters {

    private Jdk8DeepDiveAdvancedChapters() {
    }

    static void add(Course c) {
        addFunctionalJava8(c);
        addRuntimeIntrospection(c);
        addPlatformAndDiagnostics(c);
    }

    // ── Functional Java 8 ─────────────────────────────────────────────────

    private static void addFunctionalJava8(Course c) {
        Chapter ch = new Chapter(
                "Java 8 functional style: lambdas, Optional, regex",
                "Java 8 functional style: lambdas, Optional, regex");
        ch.add(lessonFunctionalInterfacesDeep());
        ch.add(lessonMethodReferencesDefaultMethods());
        ch.add(lessonOptionalDeep());
        ch.add(lessonRegexDeep());
        c.add(ch);
    }

    private static Lesson lessonFunctionalInterfacesDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Functional interface: один абстрактний метод"));
        uk.add(LessonBlock.paragraph(
                "Functional interface — інтерфейс з рівно одним abstract методом. Саме такий "
                + "інтерфейс можна реалізувати lambda-виразом. У JDK 8 це основа Stream API, "
                + "Comparator, Runnable, Callable, Predicate, Function, Consumer, Supplier."));
        uk.add(LessonBlock.table(
                "Інтерфейс\tМетод\tСенс",
                Arrays.asList(
                        "Predicate<T>\tboolean test(T t)\tперевірка умови",
                        "Function<T,R>\tR apply(T t)\tперетворення T у R",
                        "Consumer<T>\tvoid accept(T t)\tдія без результату",
                        "Supplier<T>\tT get()\tпостачальник значення",
                        "UnaryOperator<T>\tT apply(T t)\tT -> T",
                        "BinaryOperator<T>\tT apply(T a, T b)\tдві T -> одна T")));
        uk.add(LessonBlock.code(
                "Predicate<String> isLong = s -> s != null && s.length() > 5;\n"
                + "Function<String, Integer> length = s -> s.length();\n"
                + "Consumer<String> print = s -> System.out.println(s);\n"
                + "Supplier<Long> now = () -> System.currentTimeMillis();\n"
                + "\n"
                + "System.out.println(isLong.test(\"JavaDroid\")); // true\n"
                + "System.out.println(length.apply(\"abc\"));      // 3\n"
                + "print.accept(\"hello\");\n"
                + "System.out.println(now.get());"));
        uk.add(LessonBlock.paragraph(
                "Lambda не створює 'окремий синтаксис методу'. Вона дає реалізацію єдиного "
                + "абстрактного методу інтерфейсу. Тому компілятор завжди має знати цільовий тип."));
        uk.add(LessonBlock.code(
                "// Без цільового типу lambda не має сенсу:\n"
                + "// Object x = s -> s.length(); // помилка\n"
                + "\n"
                + "Function<String, Integer> f = s -> s.length(); // OK: є target type"));
        uk.add(LessonBlock.warning(
                "Lambda може читати локальні змінні ззовні тільки якщо вони final або effectively final. "
                + "Тобто змінна після ініціалізації більше не змінюється."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Напишіть Predicate<Integer> для парних чисел.",
                "Напишіть Function<String,String>, яка trim + lowercase.",
                "Напишіть Consumer<List<String>>, який друкує кожен елемент.",
                "Напишіть Supplier<String>, який повертає UUID.randomUUID().toString().",
                "Створіть власний @FunctionalInterface Validator<T>."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Functional interface: one abstract method"));
        en.add(LessonBlock.paragraph(
                "A functional interface has exactly one abstract method. Such an interface can "
                + "be implemented by a lambda expression. In JDK 8 this powers Stream API, "
                + "Comparator, Runnable, Callable, Predicate, Function, Consumer, Supplier."));
        en.add(LessonBlock.table(
                "Interface\tMethod\tMeaning",
                Arrays.asList(
                        "Predicate<T>\tboolean test(T t)\tcondition check",
                        "Function<T,R>\tR apply(T t)\ttransform T into R",
                        "Consumer<T>\tvoid accept(T t)\taction without result",
                        "Supplier<T>\tT get()\tvalue supplier",
                        "UnaryOperator<T>\tT apply(T t)\tT -> T",
                        "BinaryOperator<T>\tT apply(T a, T b)\ttwo T -> one T")));
        en.add(LessonBlock.code(
                "Predicate<String> isLong = s -> s != null && s.length() > 5;\n"
                + "Function<String, Integer> length = s -> s.length();\n"
                + "Consumer<String> print = s -> System.out.println(s);\n"
                + "Supplier<Long> now = () -> System.currentTimeMillis();\n"
                + "\n"
                + "System.out.println(isLong.test(\"JavaDroid\")); // true\n"
                + "System.out.println(length.apply(\"abc\"));      // 3\n"
                + "print.accept(\"hello\");\n"
                + "System.out.println(now.get());"));
        en.add(LessonBlock.paragraph(
                "A lambda is not 'a separate method syntax'. It provides the implementation "
                + "of the single abstract method of an interface. Therefore the compiler must "
                + "always know the target type."));
        en.add(LessonBlock.code(
                "// Without target type a lambda has no meaning:\n"
                + "// Object x = s -> s.length(); // error\n"
                + "\n"
                + "Function<String, Integer> f = s -> s.length(); // OK: target type exists"));
        en.add(LessonBlock.warning(
                "A lambda can read local variables from outside only if they are final or "
                + "effectively final. That means the variable is not changed after initialization."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write Predicate<Integer> for even numbers.",
                "Write Function<String,String> doing trim + lowercase.",
                "Write Consumer<List<String>> printing every element.",
                "Write Supplier<String> returning UUID.randomUUID().toString().",
                "Create your own @FunctionalInterface Validator<T>."));

        return new Lesson("jdk8.functional.1", "Functional interfaces", "Functional interfaces", uk, en);
    }

    private static Lesson lessonMethodReferencesDefaultMethods() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Method references і default methods"));
        uk.add(LessonBlock.paragraph(
                "Method reference — коротка форма lambda, коли lambda просто викликає метод. "
                + "Default methods у Java 8 дозволили додати методи в інтерфейси без ламання "
                + "старих реалізацій. Саме так Collection отримав stream(), removeIf(), forEach()."));
        uk.add(LessonBlock.table(
                "Форма\tПриклад\tАналог lambda",
                Arrays.asList(
                        "static method\tInteger::parseInt\ts -> Integer.parseInt(s)",
                        "instance method конкретного об'єкта\tSystem.out::println\ts -> System.out.println(s)",
                        "instance method типу\tString::toUpperCase\ts -> s.toUpperCase()",
                        "constructor\tArrayList::new\t() -> new ArrayList<>()")));
        uk.add(LessonBlock.code(
                "List<String> words = Arrays.asList(\"1\", \"2\", \"3\");\n"
                + "List<Integer> nums = words.stream()\n"
                + "        .map(Integer::parseInt)\n"
                + "        .collect(Collectors.toList());\n"
                + "\n"
                + "nums.forEach(System.out::println);"));
        uk.add(LessonBlock.code(
                "interface HasName {\n"
                + "    String getName();\n"
                + "\n"
                + "    default String displayName() {\n"
                + "        return getName().trim().toUpperCase();\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class User implements HasName {\n"
                + "    private final String name;\n"
                + "    User(String name) { this.name = name; }\n"
                + "    public String getName() { return name; }\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Default method — не заміна абстрактному класу. Інтерфейс усе ще не повинен "
                + "перетворюватися на місце для складного стану і важкої бізнес-логіки."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Перепишіть 5 lambda-виразів на method references.",
                "Створіть interface Identifiable з default method hasSameId.",
                "Додайте static method в інтерфейс і викличте його через InterfaceName.method().",
                "Поясніть, що буде, якщо клас реалізує два інтерфейси з однаковим default method."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Method references and default methods"));
        en.add(LessonBlock.paragraph(
                "A method reference is a short lambda form when the lambda simply calls a method. "
                + "Default methods in Java 8 allowed adding methods to interfaces without breaking "
                + "old implementations. That is how Collection gained stream(), removeIf(), forEach()."));
        en.add(LessonBlock.table(
                "Form\tExample\tLambda equivalent",
                Arrays.asList(
                        "static method\tInteger::parseInt\ts -> Integer.parseInt(s)",
                        "instance method of object\tSystem.out::println\ts -> System.out.println(s)",
                        "instance method of type\tString::toUpperCase\ts -> s.toUpperCase()",
                        "constructor\tArrayList::new\t() -> new ArrayList<>()")));
        en.add(LessonBlock.code(
                "List<String> words = Arrays.asList(\"1\", \"2\", \"3\");\n"
                + "List<Integer> nums = words.stream()\n"
                + "        .map(Integer::parseInt)\n"
                + "        .collect(Collectors.toList());\n"
                + "\n"
                + "nums.forEach(System.out::println);"));
        en.add(LessonBlock.code(
                "interface HasName {\n"
                + "    String getName();\n"
                + "\n"
                + "    default String displayName() {\n"
                + "        return getName().trim().toUpperCase();\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class User implements HasName {\n"
                + "    private final String name;\n"
                + "    User(String name) { this.name = name; }\n"
                + "    public String getName() { return name; }\n"
                + "}"));
        en.add(LessonBlock.warning(
                "A default method is not a replacement for an abstract class. An interface still "
                + "should not become a place for complex state and heavy business logic."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Rewrite 5 lambda expressions as method references.",
                "Create interface Identifiable with default method hasSameId.",
                "Add static method to an interface and call it through InterfaceName.method().",
                "Explain what happens if a class implements two interfaces with the same default method."));

        return new Lesson("jdk8.functional.2", "Method references і default", "Method references and default", uk, en);
    }

    private static Lesson lessonOptionalDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Optional: явна відсутність значення"));
        uk.add(LessonBlock.paragraph(
                "Optional<T> у Java 8 — контейнер, який може містити T або бути порожнім. "
                + "Він допомагає явно показати: результат може бути відсутнім. Optional не "
                + "прибирає null з Java повністю, але змушує думати про відсутність значення."));
        uk.add(LessonBlock.code(
                "Optional<String> maybeName = findNameById(10);\n"
                + "\n"
                + "String display = maybeName\n"
                + "        .map(String::trim)\n"
                + "        .filter(s -> !s.isEmpty())\n"
                + "        .orElse(\"Unknown\");\n"
                + "\n"
                + "System.out.println(display);"));
        uk.add(LessonBlock.table(
                "Метод\tЩо робить",
                Arrays.asList(
                        "Optional.of(x)\tстворити Optional з non-null x",
                        "Optional.ofNullable(x)\tстворити Optional, дозволяючи null",
                        "empty()\tпорожній Optional",
                        "map(f)\tперетворити значення, якщо воно є",
                        "filter(p)\tзалишити значення, якщо умова true",
                        "orElse(default)\tзначення або default",
                        "orElseGet(supplier)\tзначення або ліниво створений default",
                        "orElseThrow(...)\tзначення або виняток")));
        uk.add(LessonBlock.warning(
                "Не використовуйте Optional як поле entity/model класу без сильної причини. "
                + "Типовий JDK 8 стиль: Optional як return type для методу, який може нічого не знайти."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Напишіть Optional<User> findUserByEmail(String email).",
                "Перепишіть код if (x != null) на Optional map/filter/orElse.",
                "Порівняйте orElse(expensive()) і orElseGet(() -> expensive()).",
                "Створіть метод, який кидає NoSuchElementException через orElseThrow."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Optional: explicit missing value"));
        en.add(LessonBlock.paragraph(
                "Optional<T> in Java 8 is a container that may hold T or be empty. It explicitly "
                + "shows that a result may be absent. Optional does not remove null from Java "
                + "completely, but forces you to think about missing values."));
        en.add(LessonBlock.code(
                "Optional<String> maybeName = findNameById(10);\n"
                + "\n"
                + "String display = maybeName\n"
                + "        .map(String::trim)\n"
                + "        .filter(s -> !s.isEmpty())\n"
                + "        .orElse(\"Unknown\");\n"
                + "\n"
                + "System.out.println(display);"));
        en.add(LessonBlock.table(
                "Method\tWhat it does",
                Arrays.asList(
                        "Optional.of(x)\tcreate Optional from non-null x",
                        "Optional.ofNullable(x)\tcreate Optional allowing null",
                        "empty()\tempty Optional",
                        "map(f)\ttransform value if present",
                        "filter(p)\tkeep value if condition is true",
                        "orElse(default)\tvalue or default",
                        "orElseGet(supplier)\tvalue or lazily created default",
                        "orElseThrow(...)\tvalue or exception")));
        en.add(LessonBlock.warning(
                "Do not use Optional as a field of entity/model class without a strong reason. "
                + "Typical JDK 8 style: Optional as return type for a method that may find nothing."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write Optional<User> findUserByEmail(String email).",
                "Rewrite if (x != null) code with Optional map/filter/orElse.",
                "Compare orElse(expensive()) and orElseGet(() -> expensive()).",
                "Create a method throwing NoSuchElementException via orElseThrow."));

        return new Lesson("jdk8.functional.3", "Optional глибоко", "Optional deep dive", uk, en);
    }

    private static Lesson lessonRegexDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Regex у JDK 8: Pattern і Matcher"));
        uk.add(LessonBlock.paragraph(
                "Регулярні вирази описують шаблон тексту. У Java основні класи — Pattern "
                + "і Matcher. String.matches зручний для дрібних перевірок, але Pattern.compile "
                + "краще, якщо шаблон використовується багато разів."));
        uk.add(LessonBlock.table(
                "Символ\tСенс\tПриклад",
                Arrays.asList(
                        ".\tбудь-який символ\tc.t",
                        "\\d\tцифра\t\\d+",
                        "\\w\tлітера/цифра/_\t\\w+",
                        "\\s\tпробільний символ\t\\s+",
                        "*\t0 або більше\ta*",
                        "+\t1 або більше\ta+",
                        "?\t0 або 1\ta?",
                        "{n,m}\tвід n до m\t\\d{2,4}",
                        "[]\tнабір символів\t[a-z]",
                        "()\tгрупа\t(\\d+)-(\\w+)")));
        uk.add(LessonBlock.code(
                "Pattern emailPattern = Pattern.compile(\"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$\");\n"
                + "Matcher matcher = emailPattern.matcher(\"test@example.com\");\n"
                + "System.out.println(matcher.matches()); // true"));
        uk.add(LessonBlock.code(
                "Pattern p = Pattern.compile(\"(\\\\d{4})-(\\\\d{2})-(\\\\d{2})\");\n"
                + "Matcher m = p.matcher(\"Date: 2026-06-20\");\n"
                + "if (m.find()) {\n"
                + "    System.out.println(m.group(1)); // 2026\n"
                + "    System.out.println(m.group(2)); // 06\n"
                + "    System.out.println(m.group(3)); // 20\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "matches() перевіряє весь рядок, find() шукає входження всередині рядка. "
                + "Це одна з найчастіших помилок у regex-початківців."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Перевірте телефон у форматі +380XXXXXXXXX.",
                "Витягніть усі числа з рядка через Matcher.find().",
                "Замініть кілька пробілів на один через replaceAll(\"\\\\s+\", \" \").",
                "Напишіть regex для простого username: 3-16 символів, letters/digits/_ only."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Regex in JDK 8: Pattern and Matcher"));
        en.add(LessonBlock.paragraph(
                "Regular expressions describe a text pattern. In Java the main classes are "
                + "Pattern and Matcher. String.matches is convenient for tiny checks, but "
                + "Pattern.compile is better when the pattern is reused."));
        en.add(LessonBlock.table(
                "Symbol\tMeaning\tExample",
                Arrays.asList(
                        ".\tany character\tc.t",
                        "\\d\tdigit\t\\d+",
                        "\\w\tletter/digit/_\t\\w+",
                        "\\s\twhitespace\t\\s+",
                        "*\t0 or more\ta*",
                        "+\t1 or more\ta+",
                        "?\t0 or 1\ta?",
                        "{n,m}\tfrom n to m\t\\d{2,4}",
                        "[]\tcharacter set\t[a-z]",
                        "()\tgroup\t(\\d+)-(\\w+)")));
        en.add(LessonBlock.code(
                "Pattern emailPattern = Pattern.compile(\"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$\");\n"
                + "Matcher matcher = emailPattern.matcher(\"test@example.com\");\n"
                + "System.out.println(matcher.matches()); // true"));
        en.add(LessonBlock.code(
                "Pattern p = Pattern.compile(\"(\\\\d{4})-(\\\\d{2})-(\\\\d{2})\");\n"
                + "Matcher m = p.matcher(\"Date: 2026-06-20\");\n"
                + "if (m.find()) {\n"
                + "    System.out.println(m.group(1)); // 2026\n"
                + "    System.out.println(m.group(2)); // 06\n"
                + "    System.out.println(m.group(3)); // 20\n"
                + "}"));
        en.add(LessonBlock.warning(
                "matches() checks the whole string, find() searches for an occurrence inside "
                + "the string. This is one of the most common beginner regex mistakes."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Validate phone format +380XXXXXXXXX.",
                "Extract all numbers from a string using Matcher.find().",
                "Replace multiple spaces with one via replaceAll(\"\\\\s+\", \" \").",
                "Write regex for simple username: 3-16 chars, letters/digits/_ only."));

        return new Lesson("jdk8.functional.4", "Regex у JDK 8", "Regex in JDK 8", uk, en);
    }

    // ── Runtime Introspection ──────────────────────────────────────────────

    private static void addRuntimeIntrospection(Course c) {
        Chapter ch = new Chapter(
                "Runtime: annotations, reflection, serialization",
                "Runtime: annotations, reflection, serialization");
        ch.add(lessonAnnotationsReflectionDeep());
        ch.add(lessonSerializationDeep());
        c.add(ch);
    }

    private static Lesson lessonAnnotationsReflectionDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Annotations + Reflection"));
        uk.add(LessonBlock.paragraph(
                "Анотації — метадані. Reflection — можливість у runtime дивитися на класи, "
                + "методи, поля і анотації. Разом вони лежать в основі JUnit, Spring, JSON-маперів, "
                + "ORM та багатьох Java-фреймворків."));
        uk.add(LessonBlock.code(
                "@Retention(RetentionPolicy.RUNTIME)\n"
                + "@Target(ElementType.METHOD)\n"
                + "@interface Command {\n"
                + "    String value();\n"
                + "}\n"
                + "\n"
                + "class ConsoleActions {\n"
                + "    @Command(\"hello\")\n"
                + "    public void hello() {\n"
                + "        System.out.println(\"Hello!\");\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.code(
                "ConsoleActions actions = new ConsoleActions();\n"
                + "for (Method method : ConsoleActions.class.getDeclaredMethods()) {\n"
                + "    Command cmd = method.getAnnotation(Command.class);\n"
                + "    if (cmd != null && \"hello\".equals(cmd.value())) {\n"
                + "        method.invoke(actions);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.table(
                "Retention\tДе живе анотація",
                Arrays.asList(
                        "SOURCE\tтільки в source, зникає після компіляції",
                        "CLASS\tу .class, але не доступна reflection зазвичай",
                        "RUNTIME\tдоступна через reflection")));
        uk.add(LessonBlock.warning(
                "Reflection потужна, але повільніша і небезпечніша за прямий виклик. "
                + "Не використовуйте reflection там, де звичайний інтерфейс вирішує задачу простіше."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть @Route(path=\"/users\") для методів.",
                "Знайдіть усі методи класу з цією анотацією.",
                "Викличте метод через reflection.",
                "Створіть @NotNull для полів і напишіть простий валідатор."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Annotations + Reflection"));
        en.add(LessonBlock.paragraph(
                "Annotations are metadata. Reflection lets you inspect classes, methods, fields, "
                + "and annotations at runtime. Together they power JUnit, Spring, JSON mappers, "
                + "ORMs, and many Java frameworks."));
        en.add(LessonBlock.code(
                "@Retention(RetentionPolicy.RUNTIME)\n"
                + "@Target(ElementType.METHOD)\n"
                + "@interface Command {\n"
                + "    String value();\n"
                + "}\n"
                + "\n"
                + "class ConsoleActions {\n"
                + "    @Command(\"hello\")\n"
                + "    public void hello() {\n"
                + "        System.out.println(\"Hello!\");\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.code(
                "ConsoleActions actions = new ConsoleActions();\n"
                + "for (Method method : ConsoleActions.class.getDeclaredMethods()) {\n"
                + "    Command cmd = method.getAnnotation(Command.class);\n"
                + "    if (cmd != null && \"hello\".equals(cmd.value())) {\n"
                + "        method.invoke(actions);\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.table(
                "Retention\tWhere annotation lives",
                Arrays.asList(
                        "SOURCE\tonly in source, disappears after compilation",
                        "CLASS\tin .class, usually unavailable through reflection",
                        "RUNTIME\tavailable through reflection")));
        en.add(LessonBlock.warning(
                "Reflection is powerful, but slower and more dangerous than direct calls. "
                + "Do not use reflection where a normal interface solves the task simply."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create @Route(path=\"/users\") for methods.",
                "Find all class methods with this annotation.",
                "Invoke a method through reflection.",
                "Create @NotNull for fields and write a simple validator."));

        return new Lesson("jdk8.runtime.1", "Annotations і Reflection", "Annotations and Reflection", uk, en);
    }

    private static Lesson lessonSerializationDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Serialization у JDK 8"));
        uk.add(LessonBlock.paragraph(
                "Java Serialization перетворює об'єкт у байти і назад. Для цього клас реалізує "
                + "Serializable. Це legacy-механізм, але його треба знати: він зустрічається в "
                + "старих системах, кешах, RMI, сесіях і навчальних задачах."));
        uk.add(LessonBlock.code(
                "class User implements Serializable {\n"
                + "    private static final long serialVersionUID = 1L;\n"
                + "\n"
                + "    private final int id;\n"
                + "    private final String name;\n"
                + "    private transient String password;\n"
                + "\n"
                + "    User(int id, String name, String password) {\n"
                + "        this.id = id;\n"
                + "        this.name = name;\n"
                + "        this.password = password;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.code(
                "User user = new User(1, \"Ira\", \"secret\");\n"
                + "try (ObjectOutputStream out = new ObjectOutputStream(\n"
                + "        new FileOutputStream(\"user.bin\"))) {\n"
                + "    out.writeObject(user);\n"
                + "}\n"
                + "\n"
                + "try (ObjectInputStream in = new ObjectInputStream(\n"
                + "        new FileInputStream(\"user.bin\"))) {\n"
                + "    User restored = (User) in.readObject();\n"
                + "}"));
        uk.add(LessonBlock.table(
                "Ключове слово/поле\tСенс",
                Arrays.asList(
                        "Serializable\tмаркерний інтерфейс: об'єкт можна серіалізувати",
                        "serialVersionUID\tверсія структури класу",
                        "transient\tполе не записується у потік",
                        "ObjectOutputStream\tзаписує об'єкт",
                        "ObjectInputStream\tчитає об'єкт")));
        uk.add(LessonBlock.warning(
                "Не десеріалізуйте неперевірені дані з інтернету. Java deserialization історично "
                + "була джерелом серйозних security-вразливостей. Для обміну даними частіше краще JSON."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Серіалізуйте User у файл і прочитайте назад.",
                "Позначте password як transient і перевірте, що після читання він null.",
                "Змініть serialVersionUID і подивіться, що станеться зі старим файлом.",
                "Поясніть, чому Serializable — marker interface."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Serialization in JDK 8"));
        en.add(LessonBlock.paragraph(
                "Java Serialization converts an object to bytes and back. A class implements "
                + "Serializable for this. It is a legacy mechanism, but important to know: it appears "
                + "in old systems, caches, RMI, sessions, and learning tasks."));
        en.add(LessonBlock.code(
                "class User implements Serializable {\n"
                + "    private static final long serialVersionUID = 1L;\n"
                + "\n"
                + "    private final int id;\n"
                + "    private final String name;\n"
                + "    private transient String password;\n"
                + "\n"
                + "    User(int id, String name, String password) {\n"
                + "        this.id = id;\n"
                + "        this.name = name;\n"
                + "        this.password = password;\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.code(
                "User user = new User(1, \"Ira\", \"secret\");\n"
                + "try (ObjectOutputStream out = new ObjectOutputStream(\n"
                + "        new FileOutputStream(\"user.bin\"))) {\n"
                + "    out.writeObject(user);\n"
                + "}\n"
                + "\n"
                + "try (ObjectInputStream in = new ObjectInputStream(\n"
                + "        new FileInputStream(\"user.bin\"))) {\n"
                + "    User restored = (User) in.readObject();\n"
                + "}"));
        en.add(LessonBlock.table(
                "Keyword/field\tMeaning",
                Arrays.asList(
                        "Serializable\tmarker interface: object can be serialized",
                        "serialVersionUID\tclass structure version",
                        "transient\tfield is not written to stream",
                        "ObjectOutputStream\twrites object",
                        "ObjectInputStream\treads object")));
        en.add(LessonBlock.warning(
                "Do not deserialize untrusted internet data. Java deserialization has historically "
                + "been a source of serious security vulnerabilities. For data exchange, JSON is often better."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Serialize User to a file and read it back.",
                "Mark password as transient and check it becomes null after reading.",
                "Change serialVersionUID and observe what happens to the old file.",
                "Explain why Serializable is a marker interface."));

        return new Lesson("jdk8.runtime.2", "Serialization", "Serialization", uk, en);
    }

    // ── Platform and Diagnostics ───────────────────────────────────────────

    private static void addPlatformAndDiagnostics(Course c) {
        Chapter ch = new Chapter(
                "JDK 8 platform: classpath, JAR, JVM diagnostics",
                "JDK 8 platform: classpath, JAR, JVM diagnostics");
        ch.add(lessonClasspathJarDeep());
        ch.add(lessonStackTracesDiagnostics());
        c.add(ch);
    }

    private static Lesson lessonClasspathJarDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Classpath і JAR"));
        uk.add(LessonBlock.paragraph(
                "У JDK 8 classpath — список місць, де JVM і javac шукають .class файли та JAR. "
                + "Якщо клас є у коді, але його немає у classpath під час запуску, отримаєте "
                + "ClassNotFoundException або NoClassDefFoundError."));
        uk.add(LessonBlock.table(
                "Термін\tСенс",
                Arrays.asList(
                        "source file\t.java файл",
                        "class file\t.class bytecode після javac",
                        "package\tпростір імен, що відповідає папкам",
                        "classpath\tде шукати класи",
                        "JAR\tzip-архів з .class і ресурсами",
                        "MANIFEST.MF\tметадані JAR, зокрема Main-Class")));
        uk.add(LessonBlock.code(
                "# Компіляція\n"
                + "javac -d out src/com/example/Main.java\n"
                + "\n"
                + "# Запуск з classpath\n"
                + "java -cp out com.example.Main\n"
                + "\n"
                + "# Створення jar\n"
                + "jar cfe app.jar com.example.Main -C out .\n"
                + "\n"
                + "# Запуск jar\n"
                + "java -jar app.jar"));
        uk.add(LessonBlock.warning(
                "Назва package має відповідати структурі папок. Якщо package com.example, "
                + "то клас зазвичай лежить у com/example/Main.java, а запуск — com.example.Main."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть пакет com.training і клас Main.",
                "Скомпілюйте через javac -d out.",
                "Запустіть через java -cp out com.training.Main.",
                "Зберіть runnable jar через jar cfe.",
                "Навмисно зламайте classpath і прочитайте помилку."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Classpath and JAR"));
        en.add(LessonBlock.paragraph(
                "In JDK 8, classpath is the list of places where JVM and javac search for .class "
                + "files and JARs. If a class exists in code but is missing from classpath at runtime, "
                + "you get ClassNotFoundException or NoClassDefFoundError."));
        en.add(LessonBlock.table(
                "Term\tMeaning",
                Arrays.asList(
                        "source file\t.java file",
                        "class file\t.class bytecode after javac",
                        "package\tnamespace matching folders",
                        "classpath\twhere to search for classes",
                        "JAR\tzip archive with .class and resources",
                        "MANIFEST.MF\tJAR metadata, including Main-Class")));
        en.add(LessonBlock.code(
                "# Compile\n"
                + "javac -d out src/com/example/Main.java\n"
                + "\n"
                + "# Run with classpath\n"
                + "java -cp out com.example.Main\n"
                + "\n"
                + "# Create jar\n"
                + "jar cfe app.jar com.example.Main -C out .\n"
                + "\n"
                + "# Run jar\n"
                + "java -jar app.jar"));
        en.add(LessonBlock.warning(
                "Package name must match folder structure. If package is com.example, the class "
                + "usually lives in com/example/Main.java, and launch name is com.example.Main."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create package com.training and class Main.",
                "Compile through javac -d out.",
                "Run through java -cp out com.training.Main.",
                "Build runnable jar through jar cfe.",
                "Intentionally break classpath and read the error."));

        return new Lesson("jdk8.platform.1", "Classpath і JAR", "Classpath and JAR", uk, en);
    }

    private static Lesson lessonStackTracesDiagnostics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Stack trace: читати зверху чи знизу?"));
        uk.add(LessonBlock.paragraph(
                "Stack trace — карта викликів, яка показує, де програма впала. Перший рядок "
                + "каже тип винятку і повідомлення. Далі йдуть stack frames: метод, файл, рядок. "
                + "Читати треба зверху до першого рядка вашого коду, але також дивитися cause."));
        uk.add(LessonBlock.code(
                "Exception in thread \"main\" java.lang.NumberFormatException: For input string: \"abc\"\n"
                + "    at java.lang.Integer.parseInt(Integer.java:580)\n"
                + "    at com.training.Parser.parseAge(Parser.java:12)\n"
                + "    at com.training.Main.main(Main.java:6)"));
        uk.add(LessonBlock.table(
                "Фрагмент\tЩо означає",
                Arrays.asList(
                        "NumberFormatException\tтип проблеми",
                        "For input string: \"abc\"\tповідомлення з деталлю",
                        "Integer.parseInt\tJDK метод, де виняток створився",
                        "Parser.java:12\tваш код, який передав погане значення",
                        "Main.java:6\tхто викликав Parser")));
        uk.add(LessonBlock.heading("Базові JVM diagnostics"));
        uk.add(LessonBlock.list(
                "jps — показати Java-процеси.",
                "jstack <pid> — stack traces усіх потоків.",
                "jmap — інформація про heap; у реальних середовищах обережно.",
                "jconsole — простий GUI для memory/thread monitoring.",
                "VisualVM — зручний профайлер/монітор для JDK 8 середовищ."));
        uk.add(LessonBlock.warning(
                "Не гугліть тільки перший рядок винятку. Завжди знайдіть перший frame вашого "
                + "коду і подивіться, які дані прийшли в цей метод."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Навмисно киньте NumberFormatException і прочитайте stack trace.",
                "Навмисно киньте NullPointerException і знайдіть рядок вашого коду.",
                "Створіть cause: throw new RuntimeException(\"wrap\", original).",
                "Запустіть нескінченний sleep і знайдіть процес через jps."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Stack trace: read top or bottom?"));
        en.add(LessonBlock.paragraph(
                "A stack trace is a call map showing where the program failed. The first line "
                + "gives exception type and message. Then come stack frames: method, file, line. "
                + "Read from the top to the first line of your code, but also inspect the cause."));
        en.add(LessonBlock.code(
                "Exception in thread \"main\" java.lang.NumberFormatException: For input string: \"abc\"\n"
                + "    at java.lang.Integer.parseInt(Integer.java:580)\n"
                + "    at com.training.Parser.parseAge(Parser.java:12)\n"
                + "    at com.training.Main.main(Main.java:6)"));
        en.add(LessonBlock.table(
                "Fragment\tMeaning",
                Arrays.asList(
                        "NumberFormatException\tproblem type",
                        "For input string: \"abc\"\tdetailed message",
                        "Integer.parseInt\tJDK method where exception was created",
                        "Parser.java:12\tyour code passed bad value",
                        "Main.java:6\twho called Parser")));
        en.add(LessonBlock.heading("Basic JVM diagnostics"));
        en.add(LessonBlock.list(
                "jps — show Java processes.",
                "jstack <pid> — stack traces of all threads.",
                "jmap — heap information; be careful in real environments.",
                "jconsole — simple GUI for memory/thread monitoring.",
                "VisualVM — convenient profiler/monitor for JDK 8 environments."));
        en.add(LessonBlock.warning(
                "Do not google only the first exception line. Always find the first frame of "
                + "your code and inspect which data entered that method."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Intentionally throw NumberFormatException and read the stack trace.",
                "Intentionally throw NullPointerException and find your code line.",
                "Create a cause: throw new RuntimeException(\"wrap\", original).",
                "Run infinite sleep and find the process through jps."));

        return new Lesson("jdk8.platform.2", "Stack traces і diagnostics", "Stack traces and diagnostics", uk, en);
    }
}
