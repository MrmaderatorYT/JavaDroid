package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Second wave of the JDK 8 deep-dive course: generics, OOP contracts,
 * exceptions, IO, date/time, and concurrency.
 */
final class Jdk8DeepDiveMoreChapters {

    private Jdk8DeepDiveMoreChapters() {
    }

    static void add(Course s) {
        addGenericsDeep(s);
        addObjectOrientedContracts(s);
        addExceptionsAndIo(s);
        addDateTimeAndConcurrency(s);
        addCapstonePractice(s);
    }

    // ── Generics Deep Dive ────────────────────────────────────────────────

    private static void addGenericsDeep(Course s) {
        Chapter ch = new Chapter(
                "Generics у JDK 8: type erasure, bounds, wildcards",
                "Generics in JDK 8: type erasure, bounds, wildcards");
        ch.add(materialGenericsMentalModel());
        ch.add(materialTypeErasureHeapPollution());
        ch.add(materialWildcardsPecsDeep());
        s.add(ch);
    }

    private static Lesson materialGenericsMentalModel() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Generics: не магія, а перевірка типів"));
        uk.add(LessonBlock.paragraph(
                "Generics у Java — це спосіб сказати компілятору: 'ця коробка містить саме "
                + "такі об'єкти'. Наприклад, List<String> означає: компілятор не дозволить "
                + "покласти Integer у список рядків, а при get() він знає, що повертається String."));
        uk.add(LessonBlock.paragraph(
                "Для початківця важливо: generics працюють переважно на етапі компіляції. "
                + "Під час виконання JVM здебільшого бачить звичайний List, а не List<String>. "
                + "Це називається type erasure, і про нього буде окремий урок."));
        uk.add(LessonBlock.code(
                "List<String> names = new ArrayList<String>();\n"
                + "names.add(\"Ira\");\n"
                + "names.add(\"Oleh\");\n"
                + "// names.add(123); // помилка компіляції\n"
                + "\n"
                + "String first = names.get(0); // каст не потрібен"));
        uk.add(LessonBlock.heading("Generic class"));
        uk.add(LessonBlock.code(
                "public class Box<T> {\n"
                + "    private T value;\n"
                + "\n"
                + "    public Box(T value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    public T get() {\n"
                + "        return value;\n"
                + "    }\n"
                + "\n"
                + "    public void set(T value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Box<Integer> intBox = new Box<Integer>(10);\n"
                + "Box<String> strBox = new Box<String>(\"hello\");"));
        uk.add(LessonBlock.table(
                "Позначення\tТиповий зміст\tПриклад",
                Arrays.asList(
                        "T\tType, будь-який тип\tBox<T>",
                        "E\tElement у колекції\tList<E>",
                        "K\tKey у Map\tMap<K,V>",
                        "V\tValue у Map\tMap<K,V>",
                        "R\tResult у функції\tFunction<T,R>")));
        uk.add(LessonBlock.warning(
                "Не використовуйте raw type без потреби: List list = new ArrayList(); "
                + "Raw type вимикає перевірку generics і повертає вас у стиль до Java 5."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Напишіть Box<T> і перевірте Box<String>, Box<Integer>, Box<List<String>>.",
                "Напишіть метод static <T> T last(List<T> list).",
                "Напишіть Pair<K,V> з getKey(), getValue(), toString().",
                "Спробуйте raw List і подивіться warning компілятора.",
                "Поясніть словами, яку помилку generics знаходять до запуску програми."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Generics: not magic, type checking"));
        en.add(LessonBlock.paragraph(
                "Generics in Java let you tell the compiler: 'this box contains exactly this "
                + "kind of object'. For example, List<String> means the compiler will not allow "
                + "Integer inside a list of strings, and get() is known to return String."));
        en.add(LessonBlock.paragraph(
                "For beginners, the important point is: generics mostly work at compile time. "
                + "At runtime, the JVM usually sees a plain List, not List<String>. This is "
                + "called type erasure and gets its own lesson."));
        en.add(LessonBlock.code(
                "List<String> names = new ArrayList<String>();\n"
                + "names.add(\"Ira\");\n"
                + "names.add(\"Oleh\");\n"
                + "// names.add(123); // compile error\n"
                + "\n"
                + "String first = names.get(0); // no cast needed"));
        en.add(LessonBlock.heading("Generic class"));
        en.add(LessonBlock.code(
                "public class Box<T> {\n"
                + "    private T value;\n"
                + "\n"
                + "    public Box(T value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    public T get() {\n"
                + "        return value;\n"
                + "    }\n"
                + "\n"
                + "    public void set(T value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "Box<Integer> intBox = new Box<Integer>(10);\n"
                + "Box<String> strBox = new Box<String>(\"hello\");"));
        en.add(LessonBlock.table(
                "Symbol\tTypical meaning\tExample",
                Arrays.asList(
                        "T\tType, any type\tBox<T>",
                        "E\tElement in a collection\tList<E>",
                        "K\tKey in Map\tMap<K,V>",
                        "V\tValue in Map\tMap<K,V>",
                        "R\tResult in a function\tFunction<T,R>")));
        en.add(LessonBlock.warning(
                "Avoid raw types unless necessary: List list = new ArrayList(); Raw types "
                + "disable generic checks and bring you back to pre-Java-5 style."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write Box<T> and test Box<String>, Box<Integer>, Box<List<String>>.",
                "Write static <T> T last(List<T> list).",
                "Write Pair<K,V> with getKey(), getValue(), toString().",
                "Try raw List and observe the compiler warning.",
                "Explain which bug generics catch before the program starts."));

        return new Lesson("jdk8.generics.1", "Generics mental model", "Generics mental model", uk, en);
    }

    private static Lesson materialTypeErasureHeapPollution() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Type erasure: що лишається після компіляції"));
        uk.add(LessonBlock.paragraph(
                "Java generics реалізовані через type erasure. Це означає: компілятор перевіряє "
                + "типи, додає потрібні casts, а потім у bytecode більшість generic-інформації "
                + "стирається. Саме тому не можна зробити new T(), new List<String>[10], "
                + "або перевірити obj instanceof List<String>."));
        uk.add(LessonBlock.code(
                "// У вихідному коді\n"
                + "List<String> names = new ArrayList<String>();\n"
                + "names.add(\"Java\");\n"
                + "String s = names.get(0);\n"
                + "\n"
                + "// Дуже спрощено після erasure це схоже на:\n"
                + "List namesRaw = new ArrayList();\n"
                + "namesRaw.add(\"Java\");\n"
                + "String s2 = (String) namesRaw.get(0);"));
        uk.add(LessonBlock.heading("Heap pollution"));
        uk.add(LessonBlock.paragraph(
                "Heap pollution — ситуація, коли змінна параметризованого типу посилається "
                + "на об'єкт, який не відповідає цьому параметру. Найчастіше це стається через "
                + "raw types, unchecked cast або небезпечні varargs з generics."));
        uk.add(LessonBlock.code(
                "List<String> strings = new ArrayList<String>();\n"
                + "List raw = strings;       // raw type: компілятор попереджає\n"
                + "raw.add(123);             // наче можна, але це небезпечно\n"
                + "\n"
                + "String first = strings.get(0); // ClassCastException під час виконання"));
        uk.add(LessonBlock.table(
                "Обмеження generics\tЧому так",
                Arrays.asList(
                        "new T() не можна\tпісля erasure JVM не знає, який це клас",
                        "new List<String>[10] не можна\tмасиви перевіряють тип runtime, generics стерті",
                        "instanceof List<String> не можна\truntime бачить тільки List",
                        "static T field не можна\tT належить об'єкту/типу, а static спільний для всіх")));
        uk.add(LessonBlock.note(
                "@SuppressWarnings(\"unchecked\") не виправляє проблему, а тільки вимикає попередження. "
                + "Використовуйте його максимально локально і тільки коли ви справді довели безпеку."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть приклад heap pollution через raw List.",
                "Поясніть, чому ClassCastException виникає не на raw.add, а на get.",
                "Напишіть метод <T> List<T> copy(List<T> source), який не використовує raw types.",
                "Спробуйте скомпілювати new T() у generic-класі й зафіксуйте помилку.",
                "Знайдіть у власному коді місця, де є unchecked warning."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Type erasure: what remains after compilation"));
        en.add(LessonBlock.paragraph(
                "Java generics are implemented through type erasure. The compiler checks types, "
                + "adds necessary casts, and then most generic information is erased in bytecode. "
                + "That is why you cannot write new T(), new List<String>[10], or check "
                + "obj instanceof List<String>."));
        en.add(LessonBlock.code(
                "// In source code\n"
                + "List<String> names = new ArrayList<String>();\n"
                + "names.add(\"Java\");\n"
                + "String s = names.get(0);\n"
                + "\n"
                + "// Very simplified after erasure it resembles:\n"
                + "List namesRaw = new ArrayList();\n"
                + "namesRaw.add(\"Java\");\n"
                + "String s2 = (String) namesRaw.get(0);"));
        en.add(LessonBlock.heading("Heap pollution"));
        en.add(LessonBlock.paragraph(
                "Heap pollution happens when a variable of a parameterized type points to an "
                + "object that does not match that parameter. It usually appears through raw "
                + "types, unchecked casts, or unsafe generic varargs."));
        en.add(LessonBlock.code(
                "List<String> strings = new ArrayList<String>();\n"
                + "List raw = strings;       // raw type: compiler warns\n"
                + "raw.add(123);             // seems allowed, but dangerous\n"
                + "\n"
                + "String first = strings.get(0); // ClassCastException at runtime"));
        en.add(LessonBlock.table(
                "Generic limitation\tReason",
                Arrays.asList(
                        "new T() is not allowed\tafter erasure JVM does not know the class",
                        "new List<String>[10] is not allowed\tarrays check runtime type, generics are erased",
                        "instanceof List<String> is not allowed\truntime sees only List",
                        "static T field is not allowed\tT belongs to instance/type parameter, static is shared")));
        en.add(LessonBlock.note(
                "@SuppressWarnings(\"unchecked\") does not fix the problem; it only hides the warning. "
                + "Use it locally and only when you have actually proven safety."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create heap pollution through raw List.",
                "Explain why ClassCastException happens not at raw.add, but at get.",
                "Write <T> List<T> copy(List<T> source) without raw types.",
                "Try compiling new T() inside a generic class and record the error.",
                "Find places in your code with unchecked warning."));

        return new Lesson("jdk8.generics.2", "Type erasure і heap pollution", "Type erasure and heap pollution", uk, en);
    }

    private static Lesson materialWildcardsPecsDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Wildcards: ? extends, ? super, PECS"));
        uk.add(LessonBlock.paragraph(
                "Wildcards потрібні, коли метод має приймати не один точний тип, а сімейство "
                + "типів. Правило PECS: Producer Extends, Consumer Super. Якщо структура "
                + "виробляє значення для читання — ? extends. Якщо структура споживає значення "
                + "для запису — ? super."));
        uk.add(LessonBlock.code(
                "// Producer: читаємо Number з List<Integer>, List<Double>, ...\n"
                + "static double sum(List<? extends Number> numbers) {\n"
                + "    double total = 0;\n"
                + "    for (Number n : numbers) {\n"
                + "        total += n.doubleValue();\n"
                + "    }\n"
                + "    return total;\n"
                + "}\n"
                + "\n"
                + "// Consumer: записуємо Integer у List<Integer>, List<Number>, List<Object>\n"
                + "static void addDefaults(List<? super Integer> target) {\n"
                + "    target.add(1);\n"
                + "    target.add(2);\n"
                + "}"));
        uk.add(LessonBlock.table(
                "Сигнатура\tМожна читати як\tМожна записувати",
                Arrays.asList(
                        "List<T>\tT\tT",
                        "List<? extends Number>\tNumber\tмайже нічого, крім null",
                        "List<? super Integer>\tObject\tInteger і його підкласи",
                        "List<?>\tObject\tтільки null")));
        uk.add(LessonBlock.warning(
                "List<Integer> не є підтипом List<Number>. Інакше можна було б покласти Double "
                + "у List<Integer> через посилання List<Number>, і типобезпечність зламалась би."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Напишіть copy(List<? extends T> src, List<? super T> dst).",
                "Напишіть printAll(List<?> list), який друкує будь-який список.",
                "Спробуйте додати елемент у List<? extends Number> і поясніть помилку.",
                "Спробуйте читати з List<? super Integer> як Integer і поясніть помилку.",
                "Поясніть PECS на прикладі кошика фруктів."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Wildcards: ? extends, ? super, PECS"));
        en.add(LessonBlock.paragraph(
                "Wildcards are needed when a method should accept not one exact type, but a "
                + "family of types. PECS rule: Producer Extends, Consumer Super. If a structure "
                + "produces values for reading — ? extends. If it consumes values for writing — ? super."));
        en.add(LessonBlock.code(
                "// Producer: read Number from List<Integer>, List<Double>, ...\n"
                + "static double sum(List<? extends Number> numbers) {\n"
                + "    double total = 0;\n"
                + "    for (Number n : numbers) {\n"
                + "        total += n.doubleValue();\n"
                + "    }\n"
                + "    return total;\n"
                + "}\n"
                + "\n"
                + "// Consumer: write Integer into List<Integer>, List<Number>, List<Object>\n"
                + "static void addDefaults(List<? super Integer> target) {\n"
                + "    target.add(1);\n"
                + "    target.add(2);\n"
                + "}"));
        en.add(LessonBlock.table(
                "Signature\tCan read as\tCan write",
                Arrays.asList(
                        "List<T>\tT\tT",
                        "List<? extends Number>\tNumber\talmost nothing except null",
                        "List<? super Integer>\tObject\tInteger and subclasses",
                        "List<?>\tObject\tonly null")));
        en.add(LessonBlock.warning(
                "List<Integer> is not a subtype of List<Number>. Otherwise you could put Double "
                + "into List<Integer> through a List<Number> reference and break type-safety."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write copy(List<? extends T> src, List<? super T> dst).",
                "Write printAll(List<?> list), printing any list.",
                "Try adding an element to List<? extends Number> and explain the error.",
                "Try reading from List<? super Integer> as Integer and explain the error.",
                "Explain PECS using a basket of fruits."));

        return new Lesson("jdk8.generics.3", "Wildcards і PECS глибоко", "Wildcards and PECS deep dive", uk, en);
    }

    // ── OOP Contracts ─────────────────────────────────────────────────────

    private static void addObjectOrientedContracts(Course s) {
        Chapter ch = new Chapter(
                "ООП у JDK 8: Object, equals/hashCode, наслідування",
                "OOP in JDK 8: Object, equals/hashCode, inheritance");
        ch.add(materialObjectMethodsContract());
        ch.add(materialInheritanceCompositionInterfaces());
        s.add(ch);
    }

    private static Lesson materialObjectMethodsContract() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Object: корінь усіх класів"));
        uk.add(LessonBlock.paragraph(
                "Кожен клас у Java прямо або непрямо наслідує java.lang.Object. Тому в кожного "
                + "об'єкта є методи equals, hashCode, toString, getClass, wait, notify, notifyAll. "
                + "Для повсякденного коду найважливіші equals/hashCode/toString."));
        uk.add(LessonBlock.table(
                "Метод\tКонтракт\tДе важливо",
                Arrays.asList(
                        "equals\tперевіряє логічну рівність\tcontains, remove, List, Set",
                        "hashCode\tчисло для hash-структур\tHashSet, HashMap",
                        "toString\tлюдське текстове представлення\tлогування, debug",
                        "getClass\truntime-клас об'єкта\tрефлексія, точна перевірка типу")));
        uk.add(LessonBlock.code(
                "final class User {\n"
                + "    private final int id;\n"
                + "    private final String email;\n"
                + "\n"
                + "    User(int id, String email) {\n"
                + "        this.id = id;\n"
                + "        this.email = email;\n"
                + "    }\n"
                + "\n"
                + "    public boolean equals(Object o) {\n"
                + "        if (this == o) return true;\n"
                + "        if (!(o instanceof User)) return false;\n"
                + "        User other = (User) o;\n"
                + "        return id == other.id;\n"
                + "    }\n"
                + "\n"
                + "    public int hashCode() {\n"
                + "        return id;\n"
                + "    }\n"
                + "\n"
                + "    public String toString() {\n"
                + "        return \"User{id=\" + id + \", email='\" + email + \"'}\";\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Якщо перевизначили equals, майже завжди треба перевизначити hashCode. "
                + "Інакше HashSet/HashMap можуть поводитися неправильно: equals каже 'рівні', "
                + "а hashCode розкладає об'єкти у різні buckets."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть User(id, email) і додайте два однакових id у HashSet.",
                "Спершу не пишіть equals/hashCode, потім напишіть і порівняйте результат.",
                "Змініть hashCode так, щоб він завжди повертав 1, і перевірте поведінку.",
                "Поясніть різницю між == і equals на String та на власному класі."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Object: root of all classes"));
        en.add(LessonBlock.paragraph(
                "Every Java class directly or indirectly extends java.lang.Object. Therefore "
                + "every object has equals, hashCode, toString, getClass, wait, notify, notifyAll. "
                + "For everyday code, equals/hashCode/toString matter most."));
        en.add(LessonBlock.table(
                "Method\tContract\tWhere it matters",
                Arrays.asList(
                        "equals\tlogical equality\tcontains, remove, List, Set",
                        "hashCode\tnumber for hash structures\tHashSet, HashMap",
                        "toString\thuman-readable text\tlogging, debug",
                        "getClass\truntime class of object\treflection, exact type check")));
        en.add(LessonBlock.code(
                "final class User {\n"
                + "    private final int id;\n"
                + "    private final String email;\n"
                + "\n"
                + "    User(int id, String email) {\n"
                + "        this.id = id;\n"
                + "        this.email = email;\n"
                + "    }\n"
                + "\n"
                + "    public boolean equals(Object o) {\n"
                + "        if (this == o) return true;\n"
                + "        if (!(o instanceof User)) return false;\n"
                + "        User other = (User) o;\n"
                + "        return id == other.id;\n"
                + "    }\n"
                + "\n"
                + "    public int hashCode() {\n"
                + "        return id;\n"
                + "    }\n"
                + "\n"
                + "    public String toString() {\n"
                + "        return \"User{id=\" + id + \", email='\" + email + \"'}\";\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.warning(
                "If you override equals, almost always override hashCode. Otherwise HashSet/HashMap "
                + "may behave incorrectly: equals says 'equal', while hashCode puts objects into different buckets."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create User(id, email) and add two equal ids into HashSet.",
                "First do not write equals/hashCode, then write them and compare.",
                "Change hashCode to always return 1 and observe behavior.",
                "Explain == vs equals for String and for your own class."));

        return new Lesson("jdk8.oop.1", "Object methods contract", "Object methods contract", uk, en);
    }

    private static Lesson materialInheritanceCompositionInterfaces() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Наслідування, композиція, інтерфейси"));
        uk.add(LessonBlock.paragraph(
                "Наслідування відповідає на питання 'is-a': Cat is an Animal. Композиція "
                + "відповідає на питання 'has-a': Car has an Engine. Інтерфейс описує контракт: "
                + "що об'єкт вміє робити, не диктуючи конкретну реалізацію."));
        uk.add(LessonBlock.table(
                "Підхід\tКоли підходить\tРизик",
                Arrays.asList(
                        "extends class\tсправжнє is-a, спільна базова поведінка\tжорстке зв'язування",
                        "implements interface\tпотрібен контракт без стану\tзабагато дрібних інтерфейсів",
                        "composition\tоб'єкт складається з частин\tтреба писати делегування",
                        "abstract class\tчастина поведінки спільна, частина абстрактна\tодин клас можна extends лише один раз")));
        uk.add(LessonBlock.code(
                "interface PaymentProcessor {\n"
                + "    void pay(int cents);\n"
                + "}\n"
                + "\n"
                + "class CardPaymentProcessor implements PaymentProcessor {\n"
                + "    public void pay(int cents) {\n"
                + "        System.out.println(\"Paid by card: \" + cents);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class CheckoutService {\n"
                + "    private final PaymentProcessor processor;\n"
                + "\n"
                + "    CheckoutService(PaymentProcessor processor) {\n"
                + "        this.processor = processor;\n"
                + "    }\n"
                + "\n"
                + "    void checkout(int cents) {\n"
                + "        processor.pay(cents);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Це основа dependency inversion без Spring: високорівневий CheckoutService "
                + "залежить від інтерфейсу PaymentProcessor, а не від конкретної оплати карткою."));
        uk.add(LessonBlock.warning(
                "Не використовуйте наслідування тільки для 'щоб перевикористати код'. Якщо "
                + "між класами немає справжнього is-a, частіше краще композиція."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть interface Notifier і реалізації EmailNotifier, SmsNotifier.",
                "Створіть OrderService, який приймає Notifier у конструкторі.",
                "Замініть реалізацію без зміни OrderService.",
                "Поясніть, чому це легше тестувати, ніж new EmailNotifier() всередині сервісу."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Inheritance, composition, interfaces"));
        en.add(LessonBlock.paragraph(
                "Inheritance answers 'is-a': Cat is an Animal. Composition answers 'has-a': "
                + "Car has an Engine. An interface describes a contract: what an object can do "
                + "without dictating the implementation."));
        en.add(LessonBlock.table(
                "Approach\tWhen suitable\tRisk",
                Arrays.asList(
                        "extends class\treal is-a, shared base behavior\ttight coupling",
                        "implements interface\tcontract without state needed\ttoo many tiny interfaces",
                        "composition\tobject consists of parts\trequires delegation",
                        "abstract class\tsome behavior shared, some abstract\tcan extend only one class")));
        en.add(LessonBlock.code(
                "interface PaymentProcessor {\n"
                + "    void pay(int cents);\n"
                + "}\n"
                + "\n"
                + "class CardPaymentProcessor implements PaymentProcessor {\n"
                + "    public void pay(int cents) {\n"
                + "        System.out.println(\"Paid by card: \" + cents);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "class CheckoutService {\n"
                + "    private final PaymentProcessor processor;\n"
                + "\n"
                + "    CheckoutService(PaymentProcessor processor) {\n"
                + "        this.processor = processor;\n"
                + "    }\n"
                + "\n"
                + "    void checkout(int cents) {\n"
                + "        processor.pay(cents);\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.note(
                "This is dependency inversion without Spring: high-level CheckoutService depends "
                + "on PaymentProcessor interface, not on concrete card payment."));
        en.add(LessonBlock.warning(
                "Do not use inheritance only 'to reuse code'. If there is no real is-a relation, "
                + "composition is usually better."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create interface Notifier and implementations EmailNotifier, SmsNotifier.",
                "Create OrderService accepting Notifier in constructor.",
                "Replace implementation without changing OrderService.",
                "Explain why this is easier to test than new EmailNotifier() inside service."));

        return new Lesson("jdk8.oop.2", "Наслідування і композиція", "Inheritance and composition", uk, en);
    }

    // ── Exceptions and IO ──────────────────────────────────────────────────

    private static void addExceptionsAndIo(Course s) {
        Chapter ch = new Chapter(
                "Exceptions та IO/NIO у JDK 8",
                "Exceptions and IO/NIO in JDK 8");
        ch.add(materialExceptionsDeep());
        ch.add(materialTryWithResourcesDeep());
        ch.add(materialIoNioDeep());
        s.add(ch);
    }

    private static Lesson materialExceptionsDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Exception hierarchy"));
        uk.add(LessonBlock.paragraph(
                "Винятки — це механізм повідомити: нормальний шлях виконання зірвався. "
                + "У Java є checked exceptions, які компілятор змушує обробити або оголосити "
                + "через throws, і unchecked exceptions, які наслідують RuntimeException."));
        uk.add(LessonBlock.table(
                "Тип\tПриклади\tКоли використовувати",
                Arrays.asList(
                        "checked Exception\tIOException, SQLException\tзовнішня проблема, від якої можна відновитись",
                        "RuntimeException\tNullPointerException, IllegalArgumentException\tпомилка програміста або неправильний аргумент",
                        "Error\tOutOfMemoryError, StackOverflowError\tсерйозна проблема JVM, зазвичай не ловимо")));
        uk.add(LessonBlock.code(
                "static int parseAge(String text) {\n"
                + "    if (text == null) {\n"
                + "        throw new IllegalArgumentException(\"age text is null\");\n"
                + "    }\n"
                + "    int age = Integer.parseInt(text);\n"
                + "    if (age < 0) {\n"
                + "        throw new IllegalArgumentException(\"age must be positive\");\n"
                + "    }\n"
                + "    return age;\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Не ловіть Exception занадто широко без потреби. catch (Exception e) часто "
                + "приховує реальну проблему. Починайте зі специфічних винятків: IOException, "
                + "NumberFormatException, SQLException."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Напишіть parsePositiveInt(String s), який кидає IllegalArgumentException.",
                "Напишіть readFirstLine(Path p), який оголошує throws IOException.",
                "Створіть власний checked exception ValidationException.",
                "Перепишіть catch(Exception) на кілька конкретних catch."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Exception hierarchy"));
        en.add(LessonBlock.paragraph(
                "Exceptions report that the normal execution path failed. Java has checked "
                + "exceptions, which the compiler forces you to handle or declare with throws, "
                + "and unchecked exceptions, which extend RuntimeException."));
        en.add(LessonBlock.table(
                "Type\tExamples\tWhen to use",
                Arrays.asList(
                        "checked Exception\tIOException, SQLException\texternal recoverable problem",
                        "RuntimeException\tNullPointerException, IllegalArgumentException\tprogrammer error or invalid argument",
                        "Error\tOutOfMemoryError, StackOverflowError\tserious JVM problem, usually not caught")));
        en.add(LessonBlock.code(
                "static int parseAge(String text) {\n"
                + "    if (text == null) {\n"
                + "        throw new IllegalArgumentException(\"age text is null\");\n"
                + "    }\n"
                + "    int age = Integer.parseInt(text);\n"
                + "    if (age < 0) {\n"
                + "        throw new IllegalArgumentException(\"age must be positive\");\n"
                + "    }\n"
                + "    return age;\n"
                + "}"));
        en.add(LessonBlock.warning(
                "Do not catch Exception too broadly without a reason. catch (Exception e) often "
                + "hides the real problem. Start with specific exceptions: IOException, "
                + "NumberFormatException, SQLException."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write parsePositiveInt(String s), throwing IllegalArgumentException.",
                "Write readFirstLine(Path p), declaring throws IOException.",
                "Create your own checked exception ValidationException.",
                "Rewrite catch(Exception) into several concrete catch blocks."));

        return new Lesson("jdk8.exceptions.1", "Exceptions глибоко", "Exceptions deep dive", uk, en);
    }

    private static Lesson materialTryWithResourcesDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("try-with-resources і AutoCloseable"));
        uk.add(LessonBlock.paragraph(
                "try-with-resources з Java 7 автоматично закриває ресурси: файли, сокети, "
                + "JDBC Connection, Statement, ResultSet. Ресурс має реалізовувати AutoCloseable "
                + "або Closeable. Закриття відбувається у зворотному порядку створення."));
        uk.add(LessonBlock.code(
                "try (BufferedReader reader = new BufferedReader(new FileReader(\"input.txt\"));\n"
                + "     BufferedWriter writer = new BufferedWriter(new FileWriter(\"out.txt\"))) {\n"
                + "    String line;\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        writer.write(line);\n"
                + "        writer.newLine();\n"
                + "    }\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}"));
        uk.add(LessonBlock.note(
                "Якщо і основний блок, і close() кидають виняток, основний виняток буде головним, "
                + "а виняток із close() стане suppressed. Його можна побачити через getSuppressed()."));
        uk.add(LessonBlock.code(
                "catch (IOException e) {\n"
                + "    System.out.println(\"Main: \" + e);\n"
                + "    for (Throwable suppressed : e.getSuppressed()) {\n"
                + "        System.out.println(\"Suppressed: \" + suppressed);\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Напишіть власний class DemoResource implements AutoCloseable.",
                "У close() виведіть повідомлення і перевірте порядок закриття двох ресурсів.",
                "Змусьте close() кинути Exception і подивіться suppressed.",
                "Перепишіть старий finally-close код на try-with-resources."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("try-with-resources and AutoCloseable"));
        en.add(LessonBlock.paragraph(
                "try-with-resources from Java 7 automatically closes resources: files, sockets, "
                + "JDBC Connection, Statement, ResultSet. A resource must implement AutoCloseable "
                + "or Closeable. Closing happens in reverse creation order."));
        en.add(LessonBlock.code(
                "try (BufferedReader reader = new BufferedReader(new FileReader(\"input.txt\"));\n"
                + "     BufferedWriter writer = new BufferedWriter(new FileWriter(\"out.txt\"))) {\n"
                + "    String line;\n"
                + "    while ((line = reader.readLine()) != null) {\n"
                + "        writer.write(line);\n"
                + "        writer.newLine();\n"
                + "    }\n"
                + "} catch (IOException e) {\n"
                + "    e.printStackTrace();\n"
                + "}"));
        en.add(LessonBlock.note(
                "If both the main block and close() throw exceptions, the main exception is primary, "
                + "and the close() exception becomes suppressed. See it through getSuppressed()."));
        en.add(LessonBlock.code(
                "catch (IOException e) {\n"
                + "    System.out.println(\"Main: \" + e);\n"
                + "    for (Throwable suppressed : e.getSuppressed()) {\n"
                + "        System.out.println(\"Suppressed: \" + suppressed);\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Write your own class DemoResource implements AutoCloseable.",
                "Print a message in close() and check closing order for two resources.",
                "Force close() to throw Exception and observe suppressed.",
                "Rewrite old finally-close code into try-with-resources."));

        return new Lesson("jdk8.exceptions.2", "try-with-resources", "try-with-resources", uk, en);
    }

    private static Lesson materialIoNioDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("IO/NIO: байти, символи, Path, Files"));
        uk.add(LessonBlock.paragraph(
                "У JDK 8 є старий java.io і новіший java.nio.file. Для тексту важливе кодування "
                + "(charset), для бінарних файлів — байти. Reader/Writer працюють із символами, "
                + "InputStream/OutputStream — з байтами."));
        uk.add(LessonBlock.table(
                "API\tДля чого\tПриклади",
                Arrays.asList(
                        "InputStream/OutputStream\tбінарні дані\tjpg, zip, socket bytes",
                        "Reader/Writer\tтекст\tBufferedReader, FileReader",
                        "Path\tшлях до файлу\tPaths.get(\"data\", \"a.txt\")",
                        "Files\tутиліти для файлів\treadAllLines, write, copy, move",
                        "Charset\tкодування тексту\tStandardCharsets.UTF_8")));
        uk.add(LessonBlock.code(
                "Path path = Paths.get(\"data\", \"names.txt\");\n"
                + "List<String> lines = Arrays.asList(\"Ira\", \"Oleh\", \"Anna\");\n"
                + "\n"
                + "Files.createDirectories(path.getParent());\n"
                + "Files.write(path, lines, StandardCharsets.UTF_8);\n"
                + "\n"
                + "List<String> read = Files.readAllLines(path, StandardCharsets.UTF_8);\n"
                + "for (String line : read) {\n"
                + "    System.out.println(line);\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "У JDK 8 немає Files.readString/writeString і Path.of. Використовуйте "
                + "Files.readAllBytes/readAllLines/write та Paths.get."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть файл з 5 рядками через Files.write.",
                "Прочитайте файл через Files.readAllLines.",
                "Скопіюйте файл через Files.copy з REPLACE_EXISTING.",
                "Напишіть copyBinary(Path src, Path dst) через InputStream/OutputStream.",
                "Поясніть різницю між byte stream і character stream."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("IO/NIO: bytes, characters, Path, Files"));
        en.add(LessonBlock.paragraph(
                "JDK 8 has older java.io and newer java.nio.file. For text, charset matters; "
                + "for binary files, bytes matter. Reader/Writer work with characters, "
                + "InputStream/OutputStream with bytes."));
        en.add(LessonBlock.table(
                "API\tPurpose\tExamples",
                Arrays.asList(
                        "InputStream/OutputStream\tbinary data\tjpg, zip, socket bytes",
                        "Reader/Writer\ttext\tBufferedReader, FileReader",
                        "Path\tfile path\tPaths.get(\"data\", \"a.txt\")",
                        "Files\tfile utilities\treadAllLines, write, copy, move",
                        "Charset\ttext encoding\tStandardCharsets.UTF_8")));
        en.add(LessonBlock.code(
                "Path path = Paths.get(\"data\", \"names.txt\");\n"
                + "List<String> lines = Arrays.asList(\"Ira\", \"Oleh\", \"Anna\");\n"
                + "\n"
                + "Files.createDirectories(path.getParent());\n"
                + "Files.write(path, lines, StandardCharsets.UTF_8);\n"
                + "\n"
                + "List<String> read = Files.readAllLines(path, StandardCharsets.UTF_8);\n"
                + "for (String line : read) {\n"
                + "    System.out.println(line);\n"
                + "}"));
        en.add(LessonBlock.warning(
                "JDK 8 does not have Files.readString/writeString or Path.of. Use "
                + "Files.readAllBytes/readAllLines/write and Paths.get."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create a file with 5 lines through Files.write.",
                "Read the file through Files.readAllLines.",
                "Copy the file through Files.copy with REPLACE_EXISTING.",
                "Write copyBinary(Path src, Path dst) using InputStream/OutputStream.",
                "Explain byte stream vs character stream."));

        return new Lesson("jdk8.io.1", "IO/NIO у JDK 8", "IO/NIO in JDK 8", uk, en);
    }

    // ── Date Time and Concurrency ──────────────────────────────────────────

    private static void addDateTimeAndConcurrency(Course s) {
        Chapter ch = new Chapter(
                "Date/Time та Concurrency у JDK 8",
                "Date/Time and Concurrency in JDK 8");
        ch.add(materialDateTimeDeep());
        ch.add(materialThreadsMemoryDeep());
        ch.add(materialExecutorFutureDeep());
        s.add(ch);
    }

    private static Lesson materialDateTimeDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("java.time: людський час і machine time"));
        uk.add(LessonBlock.paragraph(
                "JDK 8 приніс java.time — нормальну сучасну дату/час API. Головне розділення: "
                + "Instant — точка на timeline UTC; LocalDate/LocalTime/LocalDateTime — людське "
                + "локальне подання без часового поясу; ZonedDateTime — local date-time + zone."));
        uk.add(LessonBlock.table(
                "Клас\tЩо означає\tПриклад",
                Arrays.asList(
                        "Instant\tмомент UTC\tлог, timestamp, база даних",
                        "LocalDate\tдата без часу і зони\tдень народження",
                        "LocalTime\tчас без дати і зони\t09:30",
                        "LocalDateTime\tдата+час без зони\tзустріч у локальному календарі",
                        "ZonedDateTime\tдата+час+зона\tподія між країнами",
                        "Duration\tтривалість у секундах/наносекундах\tтаймаут 30 секунд",
                        "Period\tкалендарний період\t1 рік 2 місяці")));
        uk.add(LessonBlock.code(
                "Instant now = Instant.now();\n"
                + "LocalDate birthday = LocalDate.of(2000, 5, 20);\n"
                + "LocalDate today = LocalDate.now();\n"
                + "Period age = Period.between(birthday, today);\n"
                + "\n"
                + "ZoneId kyiv = ZoneId.of(\"Europe/Kyiv\");\n"
                + "ZonedDateTime meeting = LocalDateTime.of(2026, 6, 20, 14, 0)\n"
                + "        .atZone(kyiv);\n"
                + "Instant meetingInstant = meeting.toInstant();"));
        uk.add(LessonBlock.warning(
                "LocalDateTime не є моментом на timeline. Без ZoneId ви не знаєте, який це "
                + "реальний Instant. Для збереження подій у базі часто краще Instant + окремо zone/user preference."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Порахуйте вік через Period.between.",
                "Порахуйте різницю між двома Instant через Duration.",
                "Сконвертуйте LocalDateTime у Instant через ZoneId.",
                "Відформатуйте дату через DateTimeFormatter.ofPattern(\"dd.MM.yyyy HH:mm\").",
                "Поясніть різницю між Period і Duration."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("java.time: human time and machine time"));
        en.add(LessonBlock.paragraph(
                "JDK 8 introduced java.time — a proper modern date/time API. Main separation: "
                + "Instant is a UTC timeline point; LocalDate/LocalTime/LocalDateTime are human "
                + "local representations without time zone; ZonedDateTime is local date-time + zone."));
        en.add(LessonBlock.table(
                "Class\tMeaning\tExample",
                Arrays.asList(
                        "Instant\tUTC moment\tlog, timestamp, database",
                        "LocalDate\tdate without time and zone\tbirthday",
                        "LocalTime\ttime without date and zone\t09:30",
                        "LocalDateTime\tdate+time without zone\tmeeting in local calendar",
                        "ZonedDateTime\tdate+time+zone\tcross-country event",
                        "Duration\tduration in seconds/nanos\t30 second timeout",
                        "Period\tcalendar period\t1 year 2 months")));
        en.add(LessonBlock.code(
                "Instant now = Instant.now();\n"
                + "LocalDate birthday = LocalDate.of(2000, 5, 20);\n"
                + "LocalDate today = LocalDate.now();\n"
                + "Period age = Period.between(birthday, today);\n"
                + "\n"
                + "ZoneId kyiv = ZoneId.of(\"Europe/Kyiv\");\n"
                + "ZonedDateTime meeting = LocalDateTime.of(2026, 6, 20, 14, 0)\n"
                + "        .atZone(kyiv);\n"
                + "Instant meetingInstant = meeting.toInstant();"));
        en.add(LessonBlock.warning(
                "LocalDateTime is not a timeline moment. Without ZoneId you do not know which "
                + "real Instant it means. For database events, often store Instant + separate zone/user preference."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Calculate age through Period.between.",
                "Calculate difference between two Instant values through Duration.",
                "Convert LocalDateTime to Instant through ZoneId.",
                "Format date using DateTimeFormatter.ofPattern(\"dd.MM.yyyy HH:mm\").",
                "Explain Period vs Duration."));

        return new Lesson("jdk8.datetime.1", "java.time глибоко", "java.time deep dive", uk, en);
    }

    private static Lesson materialThreadsMemoryDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Threads, race condition, visibility"));
        uk.add(LessonBlock.paragraph(
                "Потік — незалежна лінія виконання. Головні проблеми багатопоточності: race condition "
                + "(два потоки змінюють стан і результат залежить від порядку), visibility "
                + "(один потік не бачить зміну іншого), atomicity (операція складається з кількох кроків)."));
        uk.add(LessonBlock.code(
                "class Counter {\n"
                + "    private int value;\n"
                + "\n"
                + "    synchronized void increment() {\n"
                + "        value++;\n"
                + "    }\n"
                + "\n"
                + "    synchronized int get() {\n"
                + "        return value;\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.table(
                "Інструмент\tЩо вирішує\tЩо не вирішує",
                Arrays.asList(
                        "synchronized\tatomicity + visibility для блока\tможе блокувати потоки",
                        "volatile\tvisibility для однієї змінної\tне робить count++ атомарним",
                        "AtomicInteger\tатомарні операції над int\tне замінює всі інваріанти об'єкта",
                        "final\tбезпечна публікація після конструктора\tне робить об'єкт mutable-safe")));
        uk.add(LessonBlock.warning(
                "volatile int count; count++ не є атомарним. count++ читає значення, додає 1, "
                + "записує назад. Між цими кроками може втрутитись інший потік."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Запустіть 2 потоки, кожен робить 100000 increment без synchronized.",
                "Повторіть із synchronized і порівняйте результат.",
                "Повторіть з AtomicInteger.",
                "Поясніть, чому volatile не достатній для increment."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Threads, race condition, visibility"));
        en.add(LessonBlock.paragraph(
                "A thread is an independent line of execution. Main concurrency problems: race condition "
                + "(two threads change state and result depends on order), visibility "
                + "(one thread does not see another's change), atomicity (operation has multiple steps)."));
        en.add(LessonBlock.code(
                "class Counter {\n"
                + "    private int value;\n"
                + "\n"
                + "    synchronized void increment() {\n"
                + "        value++;\n"
                + "    }\n"
                + "\n"
                + "    synchronized int get() {\n"
                + "        return value;\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.table(
                "Tool\tSolves\tDoes not solve",
                Arrays.asList(
                        "synchronized\tatomicity + visibility for block\tmay block threads",
                        "volatile\tvisibility for one variable\tdoes not make count++ atomic",
                        "AtomicInteger\tatomic operations over int\tdoes not replace all object invariants",
                        "final\tsafe publication after constructor\tdoes not make mutable object thread-safe")));
        en.add(LessonBlock.warning(
                "volatile int count; count++ is not atomic. count++ reads value, adds 1, writes back. "
                + "Another thread may interfere between these steps."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Run 2 threads, each doing 100000 increment without synchronized.",
                "Repeat with synchronized and compare result.",
                "Repeat with AtomicInteger.",
                "Explain why volatile is not enough for increment."));

        return new Lesson("jdk8.concurrent.1", "Threads і memory basics", "Threads and memory basics", uk, en);
    }

    private static Lesson materialExecutorFutureDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("ExecutorService, Callable, Future"));
        uk.add(LessonBlock.paragraph(
                "У реальному коді рідко створюють Thread вручну для кожної задачі. JDK 8 дає "
                + "ExecutorService — пул потоків. Ви віддаєте задачі, а пул вирішує, який потік "
                + "їх виконає. Runnable не повертає результат, Callable<V> повертає V і може кидати Exception."));
        uk.add(LessonBlock.code(
                "ExecutorService pool = Executors.newFixedThreadPool(2);\n"
                + "try {\n"
                + "    Future<Integer> future = pool.submit(new Callable<Integer>() {\n"
                + "        public Integer call() throws Exception {\n"
                + "            Thread.sleep(500);\n"
                + "            return 42;\n"
                + "        }\n"
                + "    });\n"
                + "\n"
                + "    Integer result = future.get(); // блокує потік, поки результат не готовий\n"
                + "    System.out.println(result);\n"
                + "} finally {\n"
                + "    pool.shutdown();\n"
                + "}"));
        uk.add(LessonBlock.table(
                "Метод\tЩо робить",
                Arrays.asList(
                        "execute(Runnable)\tзапустити без результату",
                        "submit(Runnable)\tповертає Future<?>",
                        "submit(Callable<T>)\tповертає Future<T>",
                        "Future.get()\tчекає результат або кидає ExecutionException",
                        "shutdown()\tне приймає нові задачі, завершує старі",
                        "shutdownNow()\tнамагається перервати виконання")));
        uk.add(LessonBlock.warning(
                "Завжди завершуйте ExecutorService. Якщо забути shutdown(), потоки пулу можуть "
                + "тримати JVM живою. У JDK 8 використовуйте finally."));
        uk.add(LessonBlock.heading("Вправа"));
        uk.add(LessonBlock.list(
                "Створіть fixed thread pool на 3 потоки і відправте 10 задач.",
                "Зробіть Callable, який рахує суму від 1 до n.",
                "Зберіть List<Future<Integer>> і потім прочитайте всі результати.",
                "Додайте timeout у future.get(1, TimeUnit.SECONDS).",
                "Поясніть різницю між submit і execute."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("ExecutorService, Callable, Future"));
        en.add(LessonBlock.paragraph(
                "In real code, you rarely create a Thread manually for each task. JDK 8 provides "
                + "ExecutorService — a thread pool. You submit tasks, and the pool decides which "
                + "thread executes them. Runnable returns no result, Callable<V> returns V and may throw Exception."));
        en.add(LessonBlock.code(
                "ExecutorService pool = Executors.newFixedThreadPool(2);\n"
                + "try {\n"
                + "    Future<Integer> future = pool.submit(new Callable<Integer>() {\n"
                + "        public Integer call() throws Exception {\n"
                + "            Thread.sleep(500);\n"
                + "            return 42;\n"
                + "        }\n"
                + "    });\n"
                + "\n"
                + "    Integer result = future.get(); // blocks until result is ready\n"
                + "    System.out.println(result);\n"
                + "} finally {\n"
                + "    pool.shutdown();\n"
                + "}"));
        en.add(LessonBlock.table(
                "Method\tWhat it does",
                Arrays.asList(
                        "execute(Runnable)\trun without result",
                        "submit(Runnable)\treturns Future<?>",
                        "submit(Callable<T>)\treturns Future<T>",
                        "Future.get()\twaits for result or throws ExecutionException",
                        "shutdown()\taccepts no new tasks, finishes old ones",
                        "shutdownNow()\ttries to interrupt execution")));
        en.add(LessonBlock.warning(
                "Always shut down ExecutorService. If you forget shutdown(), pool threads may "
                + "keep JVM alive. In JDK 8, use finally."));
        en.add(LessonBlock.heading("Exercise"));
        en.add(LessonBlock.list(
                "Create fixed thread pool with 3 threads and submit 10 tasks.",
                "Create Callable calculating sum from 1 to n.",
                "Collect List<Future<Integer>> and then read all results.",
                "Add timeout in future.get(1, TimeUnit.SECONDS).",
                "Explain submit vs execute."));

        return new Lesson("jdk8.concurrent.2", "ExecutorService і Future", "ExecutorService and Future", uk, en);
    }

    // ── Capstone Practice ──────────────────────────────────────────────────

    private static void addCapstonePractice(Course s) {
        Chapter ch = new Chapter(
                "Капстоун: маленькі JDK 8 проєкти",
                "Capstone: small JDK 8 projects");
        ch.add(materialJdk8CapstoneProjects());
        s.add(ch);
    }

    private static Lesson materialJdk8CapstoneProjects() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Проєкти для закріплення"));
        uk.add(LessonBlock.paragraph(
                "Після тем Collections, Generics, IO, Date/Time і Concurrency треба не тільки "
                + "читати, а будувати маленькі завершені програми. Кожен проєкт нижче можна "
                + "написати у JDK 8 без сторонніх бібліотек."));
        uk.add(LessonBlock.list(
                "1. Student Journal: Student, Group, grades; HashMap id->Student; сортування за середнім балом.",
                "2. Word Statistics: прочитати text file, порахувати частоти, top-10 слів, перше неповторне слово.",
                "3. TODO CLI: зберегти задачі у файл, прочитати назад, сортувати за deadline через LocalDate.",
                "4. Mini LRU Cache: LinkedHashMap з removeEldestEntry.",
                "5. Log Analyzer: прочитати файл логів, згрупувати рядки за рівнем INFO/WARN/ERROR.",
                "6. Threaded Sum: розбити масив на частини, порахувати суму через ExecutorService.",
                "7. File Copier: копіювання великих файлів через buffer + progress у відсотках.",
                "8. Config Loader: Properties + валідація обов'язкових ключів + власний ValidationException.",
                "9. Date Planner: список подій, ZoneId, сортування за Instant.",
                "10. Collections Playground: меню, яке демонструє ArrayList/LinkedList/HashSet/TreeSet/HashMap."));
        uk.add(LessonBlock.heading("Правила якості"));
        uk.add(LessonBlock.list(
                "Кожен клас має маленьку відповідальність.",
                "Дані не лежать у public полях без потреби.",
                "equals/hashCode написані для класів, які кладуться в Set або Map key.",
                "Усі ресурси закриваються через try-with-resources.",
                "ExecutorService закривається у finally.",
                "Є мінімум 5 ручних test cases у main або окремих методах.",
                "Кожна помилка має зрозуміле повідомлення для користувача."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Projects for reinforcement"));
        en.add(LessonBlock.paragraph(
                "After Collections, Generics, IO, Date/Time, and Concurrency, you must not only "
                + "read, but build small complete programs. Every project below can be written "
                + "in JDK 8 without external libraries."));
        en.add(LessonBlock.list(
                "1. Student Journal: Student, Group, grades; HashMap id->Student; sort by average grade.",
                "2. Word Statistics: read text file, count frequencies, top-10 words, first non-repeating word.",
                "3. TODO CLI: save tasks to file, read back, sort by deadline through LocalDate.",
                "4. Mini LRU Cache: LinkedHashMap with removeEldestEntry.",
                "5. Log Analyzer: read log file, group lines by INFO/WARN/ERROR.",
                "6. Threaded Sum: split array into parts, calculate sum through ExecutorService.",
                "7. File Copier: copy large files through buffer + percentage progress.",
                "8. Config Loader: Properties + required key validation + custom ValidationException.",
                "9. Date Planner: event list, ZoneId, sort by Instant.",
                "10. Collections Playground: menu demonstrating ArrayList/LinkedList/HashSet/TreeSet/HashMap."));
        en.add(LessonBlock.heading("Quality rules"));
        en.add(LessonBlock.list(
                "Each class has a small responsibility.",
                "Data is not placed in public fields without need.",
                "equals/hashCode are written for classes used in Set or as Map keys.",
                "All resources close through try-with-resources.",
                "ExecutorService closes in finally.",
                "There are at least 5 manual test cases in main or separate methods.",
                "Every error has a clear message for the user."));

        return new Lesson("jdk8.capstone.1", "JDK 8 capstone projects", "JDK 8 capstone projects", uk, en);
    }
}
