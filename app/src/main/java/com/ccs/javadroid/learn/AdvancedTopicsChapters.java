package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Додаткові глави: Generics & Collections, Streams & Lambdas, Design Patterns.
 * Глибокі теми з прикладами, порадами та пастками.
 */
final class AdvancedTopicsChapters {

    static void add(Course c) {
        addGenericsAndCollections(c);
        addStreamsAndLambdas(c);
        addDesignPatterns(c);
        addAdvancedDesignPatterns(c);
        addModernJavaFeatures(c);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава: Generics та Collections
    // ═══════════════════════════════════════════════════════════════

    private static void addGenericsAndCollections(Course c) {
        Chapter ch = new Chapter(
                "Generics та Collections (детально)",
                "Generics & Collections (in depth)");
        ch.add(lessonGenericBasics());
        ch.add(lessonBoundedWildcards());
        ch.add(lessonCollectionsOverview());
        ch.add(lessonMapDeep());
        ch.add(lessonSetAndSorted());
        ch.add(lessonComparableComparator());
        c.add(ch);
    }

    private static Lesson lessonGenericBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Generics: основи та типобезпечність"));
        uk.add(LessonBlock.paragraph(
                "Generics дозволяють створювати класи, інтерфейси та методи з параметрами типу. "
                + "Кompілятор перевіряє типи на етапі компіляції (compile-time type safety), "
                + "усуваючи потребу в ручному кастингу та знижуючи кількість ClassCastException."));
        uk.add(LessonBlock.code(
                "// Generic клас\n"
                + "public class Box<T> {\n"
                + "    private T value;\n"
                + "    public Box(T value) { this.value = value; }\n"
                + "    public T getValue() { return value; }\n"
                + "    public void setValue(T value) { this.value = value; }\n"
                + "}\n"
                + "\n"
                + "// Використання\n"
                + "Box<String> stringBox = new Box<>(\"Hello\");\n"
                + "String s = stringBox.getValue();  // без кастингу\n"
                + "\n"
                + "Box<Integer> intBox = new Box<>(42);\n"
                + "int n = intBox.getValue();  // автобоксинг: Integer → int"));
        uk.add(LessonBlock.heading("Generic методи"));
        uk.add(LessonBlock.code(
                "// Static generic метод — тип визначається з аргументів\n"
                + "public static <T> T first(List<T> list) {\n"
                + "    return list.isEmpty() ? null : list.get(0);\n"
                + "}\n"
                + "\n"
                + "String name = first(Arrays.asList(\"A\", \"B\"));  // \"A\"\n"
                + "Integer num = first(Arrays.asList(1, 2, 3));      // 1"));
        uk.add(LessonBlock.heading("Обмеження типів (bounds)"));
        uk.add(LessonBlock.code(
                "// T extends Comparable — T має бути Comparable\n"
                + "public static <T extends Comparable<T>> T max(T a, T b) {\n"
                + "    return a.compareTo(b) >= 0 ? a : b;\n"
                + "}\n"
                + "\n"
                + "max(3, 5);          // 5 (Integer implements Comparable)\n"
                + "max(\"a\", \"z\");      // \"z\"\n"
                + "// max(new Object(), new Object());  // помилка компіляції"));
        uk.add(LessonBlock.note(
                "T extends A & B — множинні обмеження (перший must be a class/interface). "
                + "Це дозволяє одночасно вимагати Comparable і Serializable."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Generics: basics and type safety"));
        en.add(LessonBlock.paragraph(
                "Generics let you create classes, interfaces and methods with type parameters. "
                + "The compiler checks types at compile time, eliminating manual casts and "
                + "reducing ClassCastException occurrences."));
        en.add(LessonBlock.code(
                "// Generic class\n"
                + "public class Box<T> {\n"
                + "    private T value;\n"
                + "    public Box(T value) { this.value = value; }\n"
                + "    public T getValue() { return value; }\n"
                + "    public void setValue(T value) { this.value = value; }\n"
                + "}\n"
                + "\n"
                + "// Usage\n"
                + "Box<String> stringBox = new Box<>(\"Hello\");\n"
                + "String s = stringBox.getValue();  // no cast needed\n"
                + "\n"
                + "Box<Integer> intBox = new Box<>(42);\n"
                + "int n = intBox.getValue();  // autoboxing: Integer → int"));
        en.add(LessonBlock.heading("Generic methods"));
        en.add(LessonBlock.code(
                "// Static generic method — type inferred from arguments\n"
                + "public static <T> T first(List<T> list) {\n"
                + "    return list.isEmpty() ? null : list.get(0);\n"
                + "}\n"
                + "\n"
                + "String name = first(Arrays.asList(\"A\", \"B\"));  // \"A\"\n"
                + "Integer num = first(Arrays.asList(1, 2, 3));      // 1"));
        en.add(LessonBlock.heading("Type bounds"));
        en.add(LessonBlock.code(
                "// T extends Comparable — T must be Comparable\n"
                + "public static <T extends Comparable<T>> T max(T a, T b) {\n"
                + "    return a.compareTo(b) >= 0 ? a : b;\n"
                + "}\n"
                + "\n"
                + "max(3, 5);          // 5 (Integer implements Comparable)\n"
                + "max(\"a\", \"z\");      // \"z\"\n"
                + "// max(new Object(), new Object());  // compile error"));
        en.add(LessonBlock.note(
                "T extends A & B — multiple bounds (first must be a class/interface). "
                + "This allows requiring both Comparable and Serializable simultaneously."));
        return new Lesson("adv.1", "Generics основи", "Generics basics", uk, en);
    }

    private static Lesson lessonBoundedWildcards() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Wildcards: ? extends та ? super"));
        uk.add(LessonBlock.paragraph(
                "Wildcards дозволяють працювати з невідомими типами. Дві основні форми:"));
        uk.add(LessonBlock.list(
                "? extends T — Upper bounded: читати T або його підтипи (Producer)",
                "? super T — Lower bounded: записувати T або його батьківські (Consumer)",
                "? — Unbounded: читати як Object, не записувати"));
        uk.add(LessonBlock.code(
                "// ? extends Number — приймає List<Integer>, List<Double>...\n"
                + "double sum(List<? extends Number> list) {\n"
                + "    double total = 0;\n"
                + "    for (Number n : list) total += n.doubleValue();\n"
                + "    return total;\n"
                + "}\n"
                + "sum(Arrays.asList(1, 2, 3));      // OK\n"
                + "sum(Arrays.asList(1.5, 2.5));     // OK\n"
                + "// sum(Arrays.asList(\"a\"));       // помилка компіляції\n"
                + "\n"
                + "// ? super Integer — приймає List<Integer>, List<Number>, List<Object>\n"
                + "void addNumbers(List<? super Integer> list) {\n"
                + "    list.add(1);\n"
                + "    list.add(2);\n"
                + "    list.add(3);\n"
                + "}\n"
                + "addNumbers(new ArrayList<Number>());  // OK\n"
                + "addNumbers(new ArrayList<Object>());  // OK"));
        uk.add(LessonBlock.heading("PECS: Producer Extends, Consumer Super"));
        uk.add(LessonBlock.paragraph(
                "Правило PECS (Effective Java, Joshua Bloch): "
                + "якщо структура виробляє дані — use extends; якщо споживає — use super. "
                + "Наприклад, Collections.copy(List<? super T> dest, List<? extends T> src)."));
        uk.add(LessonBlock.warning(
                "Після запису через ? super компілятор не дозволить читати крім Object. "
                + "Після читання через ? extends не дозволить запис. Обирайте напрямок!"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Wildcards: ? extends and ? super"));
        en.add(LessonBlock.paragraph(
                "Wildcards allow working with unknown types. Two main forms:"));
        en.add(LessonBlock.list(
                "? extends T — Upper bounded: read T or its subtypes (Producer)",
                "? super T — Lower bounded: write T or its supertypes (Consumer)",
                "? — Unbounded: read as Object, cannot write"));
        en.add(LessonBlock.code(
                "// ? extends Number — accepts List<Integer>, List<Double>...\n"
                + "double sum(List<? extends Number> list) {\n"
                + "    double total = 0;\n"
                + "    for (Number n : list) total += n.doubleValue();\n"
                + "    return total;\n"
                + "}\n"
                + "sum(Arrays.asList(1, 2, 3));      // OK\n"
                + "sum(Arrays.asList(1.5, 2.5));     // OK\n"
                + "// sum(Arrays.asList(\"a\"));       // compile error\n"
                + "\n"
                + "// ? super Integer — accepts List<Integer>, List<Number>, List<Object>\n"
                + "void addNumbers(List<? super Integer> list) {\n"
                + "    list.add(1); list.add(2); list.add(3);\n"
                + "}\n"
                + "addNumbers(new ArrayList<Number>());  // OK\n"
                + "addNumbers(new ArrayList<Object>());  // OK"));
        en.add(LessonBlock.heading("PECS: Producer Extends, Consumer Super"));
        en.add(LessonBlock.paragraph(
                "The PECS rule (Effective Java, Joshua Bloch): if a structure produces data — "
                + "use extends; if it consumes — use super. E.g., Collections.copy(List<? super T> dest, List<? extends T> src)."));
        en.add(LessonBlock.warning(
                "After writing through ? super, the compiler won't allow reading except as Object. "
                + "After reading through ? extends, writing is not allowed. Choose your direction!"));
        return new Lesson("adv.2", "Wildcards та PECS", "Wildcards & PECS", uk, en);
    }

    private static Lesson lessonCollectionsOverview() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Ієрархія Collections Framework"));
        uk.add(LessonBlock.table(
                "Інтерфейс\tРеалізація\tОсобливості",
                Arrays.asList(
                    "List\tArrayList\tШвидкий доступ по індексу, повільне видалення з середини",
                    "List\tLinkedList\tШвидке вставляння/видалення, повільний доступ по індексу",
                    "Set\tHashSet\tШвидкий, без порядку, унікальні елементи",
                    "Set\tLinkedHashSet\tЗберігає порядок вставки",
                    "Set\tTreeSet\tВідсортований (Comparable/Comparator)",
                    "Map\tHashMap\tШвидкий, без порядку ключів",
                    "Map\tLinkedHashMap\tЗберігає порядок вставки ключів",
                    "Map\tTreeMap\tКлючі відсортовані",
                    "Queue\tPriorityQueue\tМін-купка (на Comparable)",
                    "Deque\tArrayDeque\tШвидша за Stack і LinkedList для стек/черга")));
        uk.add(LessonBlock.note(
                "ArrayList vs LinkedList: ArrayList кращий для більшості випадків "
                + "(кеш-дружній, O(1) доступ). LinkedList — тільки якщо багато вставок/видалень "
                + "на початку/кінці."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Collections Framework hierarchy"));
        en.add(LessonBlock.table(
                "Interface\tImplementation\tCharacteristics",
                Arrays.asList(
                    "List\tArrayList\tFast indexed access, slow middle removal",
                    "List\tLinkedList\tFast insert/remove, slow indexed access",
                    "Set\tHashSet\tFast, unordered, unique elements",
                    "Set\tLinkedHashSet\tPreserves insertion order",
                    "Set\tTreeSet\tSorted (Comparable/Comparator)",
                    "Map\tHashMap\tFast, no key ordering",
                    "Map\tLinkedHashMap\tPreserves key insertion order",
                    "Map\tTreeMap\tKeys are sorted",
                    "Queue\tPriorityQueue\tMin-heap (via Comparable)",
                    "Deque\tArrayDeque\tFaster than Stack and LinkedList for stack/queue")));
        en.add(LessonBlock.note(
                "ArrayList vs LinkedList: ArrayList is better for most use cases "
                + "(cache-friendly, O(1) access). LinkedList only when many insertions/removals "
                + "at beginning/end."));
        return new Lesson("adv.3", "Collections Framework", "Collections Framework", uk, en);
    }

    private static Lesson lessonMapDeep() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Map: глибоке вивчення"));
        uk.add(LessonBlock.code(
                "Map<String, Integer> map = new HashMap<>();\n"
                + "\n"
                + "// putIfAbsent — додає, якщо ключа немає\n"
                + "map.putIfAbsent(\"a\", 1);\n"
                + "\n"
                + "// compute — обчислює нове значення на основі старого\n"
                + "map.compute(\"a\", (key, val) -> val == null ? 1 : val + 1);\n"
                + "System.out.println(map.get(\"a\"));  // 2\n"
                + "\n"
                + "// merge — об'єднує два значення\n"
                + "map.merge(\"b\", 3, Integer::sum);\n"
                + "map.merge(\"b\", 5, Integer::sum);  // 3 + 5 = 8\n"
                + "\n"
                + "// getOrDefault\n"
                + "int v = map.getOrDefault(\"z\", 0);  // 0 (ключа \"z\" немає)\n"
                + "\n"
                + "// replaceAll — трансформує всі значення\n"
                + "map.replaceAll((k, v) -> v * 2);"));
        uk.add(LessonBlock.heading("Відображення на Java 8+"));
        uk.add(LessonBlock.code(
                "// forEach\n"
                + "map.forEach((k, v) -> System.out.println(k + \" = \" + v));\n"
                + "\n"
                + "// stream()\n"
                + "map.entrySet().stream()\n"
                + "    .filter(e -> e.getValue() > 5)\n"
                + "    .forEach(e -> System.out.println(e.getKey()));\n"
                + "\n"
                + "// Зберігти у відсортований TreeMap\n"
                + "Map<String, Integer> sorted = new TreeMap<>(map);"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Map: deep dive"));
        en.add(LessonBlock.code(
                "Map<String, Integer> map = new HashMap<>();\n"
                + "\n"
                + "// putIfAbsent — adds if key absent\n"
                + "map.putIfAbsent(\"a\", 1);\n"
                + "\n"
                + "// compute — computes new value based on old\n"
                + "map.compute(\"a\", (key, val) -> val == null ? 1 : val + 1);\n"
                + "System.out.println(map.get(\"a\"));  // 2\n"
                + "\n"
                + "// merge — combines two values\n"
                + "map.merge(\"b\", 3, Integer::sum);\n"
                + "map.merge(\"b\", 5, Integer::sum);  // 3 + 5 = 8\n"
                + "\n"
                + "// getOrDefault\n"
                + "int v = map.getOrDefault(\"z\", 0);  // 0 (no key \"z\")\n"
                + "\n"
                + "// replaceAll — transforms all values\n"
                + "map.replaceAll((k, v) -> v * 2);"));
        en.add(LessonBlock.heading("Java 8+ mappings"));
        en.add(LessonBlock.code(
                "// forEach\n"
                + "map.forEach((k, v) -> System.out.println(k + \" = \" + v));\n"
                + "\n"
                + "// stream()\n"
                + "map.entrySet().stream()\n"
                + "    .filter(e -> e.getValue() > 5)\n"
                + "    .forEach(e -> System.out.println(e.getKey()));\n"
                + "\n"
                + "// Collect into sorted TreeMap\n"
                + "Map<String, Integer> sorted = new TreeMap<>(map);"));
        return new Lesson("adv.4", "Map глибоко", "Map deep dive", uk, en);
    }

    private static Lesson lessonSetAndSorted() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Set: HashSet, TreeSet, LinkedHashSet"));
        uk.add(LessonBlock.code(
                "// HashSet — швидкий, без порядку\n"
                + "Set<String> set = new HashSet<>(Arrays.asList(\"b\", \"a\", \"c\"));\n"
                + "set.add(\"a\");  // дублікат — ігнорується\n"
                + "System.out.println(set.size());  // 3\n"
                + "\n"
                + "// TreeSet — відсортований\n"
                + "TreeSet<Integer> sorted = new TreeSet<>(Arrays.asList(5, 1, 3));\n"
                + "System.out.println(sorted.first());  // 1\n"
                + "System.out.println(sorted.last());   // 5\n"
                + "\n"
                + "// LinkedHashSet — зберігає порядок вставки\n"
                + "LinkedHashSet<String> ordered = new LinkedHashSet<>();\n"
                + "ordered.add(\"c\"); ordered.add(\"a\"); ordered.add(\"b\");\n"
                + "System.out.println(ordered);  // [c, a, b]"));
        uk.add(LessonBlock.warning(
                "HashSet використовує hashCode() + equals(). Якщо ваш клас не перевизначає "
                + "ці методи — два різних об'єкти з однаковим станом будуть вважатися різними."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Set: HashSet, TreeSet, LinkedHashSet"));
        en.add(LessonBlock.code(
                "// HashSet — fast, no ordering\n"
                + "Set<String> set = new HashSet<>(Arrays.asList(\"b\", \"a\", \"c\"));\n"
                + "set.add(\"a\");  // duplicate — ignored\n"
                + "System.out.println(set.size());  // 3\n"
                + "\n"
                + "// TreeSet — sorted\n"
                + "TreeSet<Integer> sorted = new TreeSet<>(Arrays.asList(5, 1, 3));\n"
                + "System.out.println(sorted.first());  // 1\n"
                + "System.out.println(sorted.last());   // 5\n"
                + "\n"
                + "// LinkedHashSet — preserves insertion order\n"
                + "LinkedHashSet<String> ordered = new LinkedHashSet<>();\n"
                + "ordered.add(\"c\"); ordered.add(\"a\"); ordered.add(\"b\");\n"
                + "System.out.println(ordered);  // [c, a, b]"));
        en.add(LessonBlock.warning(
                "HashSet uses hashCode() + equals(). If your class doesn't override these "
                + "methods, two distinct objects with the same state will be treated as different."));
        return new Lesson("adv.5", "Set: HashSet, TreeSet", "Set: HashSet, TreeSet", uk, en);
    }

    private static Lesson lessonComparableComparator() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Comparable vs Comparator"));
        uk.add(LessonBlock.paragraph(
                "Comparable — натуральний порядок (визначається класом). "
                + "Comparator — зовнішній порядок (визначається окремо)."));
        uk.add(LessonBlock.code(
                "// Comparable — клас визначає порядок сам\n"
                + "class Student implements Comparable<Student> {\n"
                + "    String name; double gpa;\n"
                + "    @Override\n"
                + "    public int compareTo(Student other) {\n"
                + "        return Double.compare(this.gpa, other.gpa);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Comparator — зовнішній порядок\n"
                + "Comparator<Student> byName = Comparator.comparing(s -> s.name);\n"
                + "Comparator<Student> byGpaDesc = Comparator.comparingDouble(\n"
                + "        (Student s) -> s.gpa).reversed();\n"
                + "\n"
                + "// Комбінування\n"
                + "Comparator<Student> chain = byName.thenComparing(byGpaDesc);\n"
                + "list.sort(chain);"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Comparable vs Comparator"));
        en.add(LessonBlock.paragraph(
                "Comparable — natural ordering (defined by the class). "
                + "Comparator — external ordering (defined separately)."));
        en.add(LessonBlock.code(
                "// Comparable — class defines its own order\n"
                + "class Student implements Comparable<Student> {\n"
                + "    String name; double gpa;\n"
                + "    @Override\n"
                + "    public int compareTo(Student other) {\n"
                + "        return Double.compare(this.gpa, other.gpa);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Comparator — external ordering\n"
                + "Comparator<Student> byName = Comparator.comparing(s -> s.name);\n"
                + "Comparator<Student> byGpaDesc = Comparator.comparingDouble(\n"
                + "        (Student s) -> s.gpa).reversed();\n"
                + "\n"
                + "// Composing\n"
                + "Comparator<Student> chain = byName.thenComparing(byGpaDesc);\n"
                + "list.sort(chain);"));
        return new Lesson("adv.6", "Comparable vs Comparator", "Comparable vs Comparator", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава: Streams & Lambdas
    // ═══════════════════════════════════════════════════════════════

    private static void addStreamsAndLambdas(Course c) {
        Chapter ch = new Chapter(
                "Stream API та Lambda-вирази",
                "Stream API and Lambda expressions");
        ch.add(lessonLambdaBasics());
        ch.add(lessonFunctionalInterfaces());
        ch.add(lessonStreamCreation());
        ch.add(lessonStreamOperations());
        ch.add(lessonCollectors());
        c.add(ch);
    }

    private static Lesson lessonLambdaBasics() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Lambda-вирази: синтаксис"));
        uk.add(LessonBlock.paragraph(
                "Lambda — анонімна функція, що замінює анонімні класи для функціональних "
                + "інтерфейсів. Синтаксис: (parameters) -> expression або (parameters) -> { statements }."));
        uk.add(LessonBlock.code(
                "// До Java 8\n"
                + "Runnable r1 = new Runnable() {\n"
                + "    @Override\n"
                + "    public void run() { System.out.println(\"Hello\"); }\n"
                + "};\n"
                + "\n"
                + "// З Java 8\n"
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
                + "// Method reference (ще коротше)\n"
                + "Function<String, Integer> len = String::length;"));
        uk.add(LessonBlock.list(
                "(x, y) -> x + y          — два параметри",
                "x -> x * x               — один параметр без дужок",
                "() -> System.out.println(\"Hi\") — без параметрів",
                "x -> { return x * 2; }   — явний return"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Lambda expressions: syntax"));
        en.add(LessonBlock.paragraph(
                "A lambda is an anonymous function replacing anonymous classes for functional "
                + "interfaces. Syntax: (parameters) -> expression or (parameters) -> { statements }."));
        en.add(LessonBlock.code(
                "// Before Java 8\n"
                + "Runnable r1 = new Runnable() {\n"
                + "    @Override\n"
                + "    public void run() { System.out.println(\"Hello\"); }\n"
                + "};\n"
                + "\n"
                + "// Since Java 8\n"
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
                + "// Method reference (even shorter)\n"
                + "Function<String, Integer> len = String::length;"));
        en.add(LessonBlock.list(
                "(x, y) -> x + y          — two parameters",
                "x -> x * x               — single parameter without parens",
                "() -> System.out.println(\"Hi\") — no parameters",
                "x -> { return x * 2; }   — explicit return"));
        return new Lesson("adv.7", "Lambda основи", "Lambda basics", uk, en);
    }

    private static Lesson lessonFunctionalInterfaces() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Функціональні інтерфейси (java.util.function)"));
        uk.add(LessonBlock.table(
                "Інтерфейс\tМетод\tПризначення",
                Arrays.asList(
                    "Predicate<T>\tboolean test(T t)\tФільтрація (true/false)",
                    "Function<T,R>\tR apply(T t)\tТрансформація T → R",
                    "Consumer<T>\tvoid accept(T t)\tПобічний ефект (вывід, запис)",
                    "Supplier<T>\tT get()\tСтворення нового об'єкта",
                    "UnaryOperator<T>\tT apply(T t)\tТрансформація T → T",
                    "BinaryOperator<T>\tT apply(T a, T b)\tЗведення двох T → T",
                    "BiFunction<T,U,R>\tR apply(T t, U u)\tДва входи → результат",
                    "BiPredicate<T,U>\tboolean test(T t, U u)\tДва входи → boolean")));
        uk.add(LessonBlock.code(
                "Predicate<String> isLong = s -> s.length() > 10;\n"
                + "Function<String, Integer> len = String::length;\n"
                + "Consumer<String> printer = System.out::println;\n"
                + "Supplier<ArrayList<String>> listFactory = ArrayList::new;\n"
                + "UnaryOperator<String> upper = String::toUpperCase;\n"
                + "BinaryOperator<Integer> sum = Integer::sum;\n"
                + "\n"
                + "list.stream()\n"
                + "    .filter(isLong)           // Predicate\n"
                + "    .map(len)                  // Function\n"
                + "    .forEach(printer);         // Consumer"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Functional interfaces (java.util.function)"));
        en.add(LessonBlock.table(
                "Interface\tMethod\tPurpose",
                Arrays.asList(
                    "Predicate<T>\tboolean test(T t)\tFiltering (true/false)",
                    "Function<T,R>\tR apply(T t)\tTransform T → R",
                    "Consumer<T>\tvoid accept(T t)\tSide effect (output, write)",
                    "Supplier<T>\tT get()\tCreate new object",
                    "UnaryOperator<T>\tT apply(T t)\tTransform T → T",
                    "BinaryOperator<T>\tT apply(T a, T b)\tReduce two T → T",
                    "BiFunction<T,U,R>\tR apply(T t, U u)\tTwo inputs → result",
                    "BiPredicate<T,U>\tboolean test(T t, U u)\tTwo inputs → boolean")));
        en.add(LessonBlock.code(
                "Predicate<String> isLong = s -> s.length() > 10;\n"
                + "Function<String, Integer> len = String::length;\n"
                + "Consumer<String> printer = System.out::println;\n"
                + "Supplier<ArrayList<String>> listFactory = ArrayList::new;\n"
                + "UnaryOperator<String> upper = String::toUpperCase;\n"
                + "BinaryOperator<Integer> sum = Integer::sum;\n"
                + "\n"
                + "list.stream()\n"
                + "    .filter(isLong)           // Predicate\n"
                + "    .map(len)                  // Function\n"
                + "    .forEach(printer);         // Consumer"));
        return new Lesson("adv.8", "Functional interfaces", "Functional interfaces", uk, en);
    }

    private static Lesson lessonStreamCreation() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Створення Stream"));
        uk.add(LessonBlock.code(
                "// З колекції\n"
                + "Stream<String> s1 = list.stream();\n"
                + "Stream<String> s2 = list.parallelStream();  // паралельний\n"
                + "\n"
                + "// З масиву\n"
                + "Stream<int[]> s3 = Stream.of(new int[]{1,2,3});\n"
                + "\n"
                + "// З окремих елементів\n"
                + "Stream<String> s4 = Stream.of(\"a\", \"b\", \"c\");\n"
                + "\n"
                + "// Генерування\n"
                + "Stream<Integer> s5 = Stream.iterate(0, n -> n + 2).limit(5);  // 0,2,4,6,8\n"
                + "Stream<Double> s6 = Stream.generate(Math::random).limit(3);\n"
                + "\n"
                + "// З рядка\n"
                + "Stream<String> s7 = \"hello world\".chars()\n"
                + "    .mapToObj(c -> String.valueOf((char) c));"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Creating a Stream"));
        en.add(LessonBlock.code(
                "// From collection\n"
                + "Stream<String> s1 = list.stream();\n"
                + "Stream<String> s2 = list.parallelStream();  // parallel\n"
                + "\n"
                + "// From array\n"
                + "Stream<int[]> s3 = Stream.of(new int[]{1,2,3});\n"
                + "\n"
                + "// From individual elements\n"
                + "Stream<String> s4 = Stream.of(\"a\", \"b\", \"c\");\n"
                + "\n"
                + "// Generating\n"
                + "Stream<Integer> s5 = Stream.iterate(0, n -> n + 2).limit(5);  // 0,2,4,6,8\n"
                + "Stream<Double> s6 = Stream.generate(Math::random).limit(3);\n"
                + "\n"
                + "// From string\n"
                + "Stream<String> s7 = \"hello world\".chars()\n"
                + "    .mapToObj(c -> String.valueOf((char) c));"));
        return new Lesson("adv.9", "Створення Stream", "Creating Streams", uk, en);
    }

    private static Lesson lessonStreamOperations() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Операції Stream: проміжні та термінальні"));
        uk.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\n"
                + "    \"Іван\", \"Олена\", \"Андрій\", \"Марія\", \"Богдан\");\n"
                + "\n"
                + "// Pipeline: filter → map → sorted → collect\n"
                + "List<String> result = names.stream()\n"
                + "    .filter(n -> n.length() > 4)          // проміжна: фільтрація\n"
                + "    .map(String::toUpperCase)              // проміжна: трансформація\n"
                + "    .sorted()                              // проміжна: сортування\n"
                + "    .collect(Collectors.toList());         // термінальна\n"
                + "// [ОЛЕНА, АНДРІЙ, МАРІЯ]\n"
                + "\n"
                + "// reduce — зведення\n"
                + "int sum = IntStream.rangeClosed(1, 100)\n"
                + "    .reduce(0, Integer::sum);  // 5050\n"
                + "\n"
                + "// anyMatch / allMatch / noneMatch\n"
                + "boolean hasLong = names.stream().anyMatch(n -> n.length() > 6);\n"
                + "\n"
                + "// flatMap — розгортання вбудованих колекцій\n"
                + "List<String> words = Arrays.asList(\"hello world\", \"java stream\");\n"
                + "List<String> allWords = words.stream()\n"
                + "    .flatMap(w -> Arrays.stream(w.split(\" \")))\n"
                + "    .collect(Collectors.toList());\n"
                + "// [hello, world, java, stream]"));
        uk.add(LessonBlock.list(
                "Проміжні операції: filter, map, flatMap, sorted, distinct, peek, limit, skip",
                "Термінальні: collect, forEach, reduce, count, anyMatch, allMatch, noneMatch, findFirst, min, max"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Stream operations: intermediate & terminal"));
        en.add(LessonBlock.code(
                "List<String> names = Arrays.asList(\n"
                + "    \"John\", \"Helen\", \"Andrey\", \"Maria\", \"Bogdan\");\n"
                + "\n"
                + "// Pipeline: filter → map → sorted → collect\n"
                + "List<String> result = names.stream()\n"
                + "    .filter(n -> n.length() > 4)          // intermediate: filter\n"
                + "    .map(String::toUpperCase)              // intermediate: transform\n"
                + "    .sorted()                              // intermediate: sort\n"
                + "    .collect(Collectors.toList());         // terminal\n"
                + "// [HELEN, ANDREY, MARIA]\n"
                + "\n"
                + "// reduce — reduction\n"
                + "int sum = IntStream.rangeClosed(1, 100)\n"
                + "    .reduce(0, Integer::sum);  // 5050\n"
                + "\n"
                + "// anyMatch / allMatch / noneMatch\n"
                + "boolean hasLong = names.stream().anyMatch(n -> n.length() > 6);\n"
                + "\n"
                + "// flatMap — flattening nested collections\n"
                + "List<String> words = Arrays.asList(\"hello world\", \"java stream\");\n"
                + "List<String> allWords = words.stream()\n"
                + "    .flatMap(w -> Arrays.stream(w.split(\" \")))\n"
                + "    .collect(Collectors.toList());\n"
                + "// [hello, world, java, stream]"));
        en.add(LessonBlock.list(
                "Intermediate: filter, map, flatMap, sorted, distinct, peek, limit, skip",
                "Terminal: collect, forEach, reduce, count, anyMatch, allMatch, noneMatch, findFirst, min, max"));
        return new Lesson("adv.10", "Stream операції", "Stream operations", uk, en);
    }

    private static Lesson lessonCollectors() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Collectors: збирання результатів"));
        uk.add(LessonBlock.code(
                "// toList / toSet / toCollection\n"
                + "List<String> list = stream.collect(Collectors.toList());\n"
                + "Set<Integer> set = stream.collect(Collectors.toSet());\n"
                + "\n"
                + "//joining — з'єднання рядків\n"
                + "String csv = names.stream().collect(Collectors.joining(\", \"));\n"
                + "// \"Іван, Олена, Андрій\"\n"
                + "\n"
                + "// groupingBy — групування\n"
                + "Map<Integer, List<String>> byLength = names.stream()\n"
                + "    .collect(Collectors.groupingBy(String::length));\n"
                + "// {3=[Іван], 5=[Олена, Марія], 6=[Андрій, Богдан]}\n"
                + "\n"
                + "// partitioningBy — поділ на дві групи (true/false)\n"
                + "Map<Boolean, List<String>> parts = names.stream()\n"
                + "    .collect(Collectors.partitioningBy(n -> n.length() > 4));\n"
                + "\n"
                + "// toMap\n"
                + "Map<String, Integer> nameLen = names.stream()\n"
                + "    .collect(Collectors.toMap(n -> n, String::length));\n"
                + "\n"
                + "// reducing — агрегація\n"
                + "int totalLen = names.stream()\n"
                + "    .collect(Collectors.summingInt(String::length));"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Collectors: gathering results"));
        en.add(LessonBlock.code(
                "// toList / toSet / toCollection\n"
                + "List<String> list = stream.collect(Collectors.toList());\n"
                + "Set<Integer> set = stream.collect(Collectors.toSet());\n"
                + "\n"
                + "//joining — joining strings\n"
                + "String csv = names.stream().collect(Collectors.joining(\", \"));\n"
                + "// \"John, Helen, Andrey\"\n"
                + "\n"
                + "// groupingBy — grouping\n"
                + "Map<Integer, List<String>> byLength = names.stream()\n"
                + "    .collect(Collectors.groupingBy(String::length));\n"
                + "{4=[John], 5=[Helen, Maria], 6=[Andrey, Bogdan]}\n"
                + "\n"
                + "// partitioningBy — split into two groups (true/false)\n"
                + "Map<Boolean, List<String>> parts = names.stream()\n"
                + "    .collect(Collectors.partitioningBy(n -> n.length() > 4));\n"
                + "\n"
                + "// toMap\n"
                + "Map<String, Integer> nameLen = names.stream()\n"
                + "    .collect(Collectors.toMap(n -> n, String::length));\n"
                + "\n"
                + "// reducing — aggregation\n"
                + "int totalLen = names.stream()\n"
                + "    .collect(Collectors.summingInt(String::length));"));
        return new Lesson("adv.11", "Collectors", "Collectors", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава: Design Patterns
    // ═══════════════════════════════════════════════════════════════

    private static void addDesignPatterns(Course c) {
        Chapter ch = new Chapter(
                "Design Patterns (найважливіші)",
                "Design Patterns (most important)");
        ch.add(lessonSingleton());
        ch.add(lessonBuilder());
        ch.add(lessonObserver());
        ch.add(lessonStrategy());
        ch.add(lessonFactory());
        c.add(ch);
    }

    private static Lesson lessonSingleton() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Singleton — єдиний екземпляр"));
        uk.add(LessonBlock.paragraph(
                "Singleton гарантує, що клас має лише один екземпляр і глобальну точку доступу. "
                + "Використовується для логігера, кеша, з'єднання з БД."));
        uk.add(LessonBlock.code(
                "// Thread-safe singleton (double-checked locking)\n"
                + "public class AppConfig {\n"
                + "    private static volatile AppConfig instance;\n"
                + "    private final String theme;\n"
                + "\n"
                + "    private AppConfig() { this.theme = \"dark\"; }\n"
                + "\n"
                + "    public static AppConfig getInstance() {\n"
                + "        if (instance == null) {\n"
                + "            synchronized (AppConfig.class) {\n"
                + "                if (instance == null) {\n"
                + "                    instance = new AppConfig();\n"
                + "                }\n"
                + "            }\n"
                + "        }\n"
                + "        return instance;\n"
                + "    }\n"
                + "\n"
                + "    public String getTheme() { return theme; }\n"
                + "}\n"
                + "// Використання:\n"
                + "AppConfig config = AppConfig.getInstance();"));
        uk.add(LessonBlock.note(
                "Простіший JDK 8-сумісний варіант: enum Singleton { INSTANCE; }"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Singleton — single instance"));
        en.add(LessonBlock.paragraph(
                "Singleton ensures a class has only one instance and provides a global point "
                + "of access. Used for loggers, caches, database connections."));
        en.add(LessonBlock.code(
                "// Thread-safe singleton (double-checked locking)\n"
                + "public class AppConfig {\n"
                + "    private static volatile AppConfig instance;\n"
                + "    private final String theme;\n"
                + "\n"
                + "    private AppConfig() { this.theme = \"dark\"; }\n"
                + "\n"
                + "    public static AppConfig getInstance() {\n"
                + "        if (instance == null) {\n"
                + "            synchronized (AppConfig.class) {\n"
                + "                if (instance == null) {\n"
                + "                    instance = new AppConfig();\n"
                + "                }\n"
                + "            }\n"
                + "        }\n"
                + "        return instance;\n"
                + "    }\n"
                + "\n"
                + "    public String getTheme() { return theme; }\n"
                + "}\n"
                + "// Usage:\n"
                + "AppConfig config = AppConfig.getInstance();"));
        en.add(LessonBlock.note(
                "Simpler JDK 8-compatible approach: enum Singleton { INSTANCE; }"));
        return new Lesson("dp.1", "Singleton", "Singleton", uk, en);
    }

    private static Lesson lessonBuilder() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Builder — поетапне створення складних об'єктів"));
        uk.add(LessonBlock.paragraph(
                "Builder вирішує проблему «telescoping constructor»: замість десятків "
                + "конструкторів — ланцюжок сеттерів з фінальним build()."));
        uk.add(LessonBlock.code(
                "class HttpRequest {\n"
                + "    final String url;\n"
                + "    final String method;\n"
                + "    final Map<String, String> headers;\n"
                + "    final byte[] body;\n"
                + "\n"
                + "    private HttpRequest(Builder b) {\n"
                + "        this.url = b.url; this.method = b.method;\n"
                + "        this.headers = b.headers; this.body = b.body;\n"
                + "    }\n"
                + "\n"
                + "    static class Builder {\n"
                + "        final String url;\n"
                + "        String method = \"GET\";\n"
                + "        Map<String, String> headers = new HashMap<>();\n"
                + "        byte[] body;\n"
                + "\n"
                + "        Builder(String url) { this.url = url; }\n"
                + "        Builder method(String m) { this.method = m; return this; }\n"
                + "        Builder header(String k, String v) { headers.put(k,v); return this; }\n"
                + "        Builder body(byte[] b) { this.body = b; return this; }\n"
                + "        HttpRequest build() { return new HttpRequest(this); }\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання\n"
                + "HttpRequest req = new HttpRequest.Builder(\"https://api.example.com\")\n"
                + "    .method(\"POST\")\n"
                + "    .header(\"Content-Type\", \"application/json\")\n"
                + "    .body(\"{}\".getBytes())\n"
                + "    .build();"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Builder — step-by-step construction"));
        en.add(LessonBlock.paragraph(
                "Builder solves the \"telescoping constructor\" problem: instead of dozens of "
                + "constructors — a chain of setters with a final build()."));
        en.add(LessonBlock.code(
                "class HttpRequest {\n"
                + "    final String url;\n"
                + "    final String method;\n"
                + "    final Map<String, String> headers;\n"
                + "    final byte[] body;\n"
                + "\n"
                + "    private HttpRequest(Builder b) {\n"
                + "        this.url = b.url; this.method = b.method;\n"
                + "        this.headers = b.headers; this.body = b.body;\n"
                + "    }\n"
                + "\n"
                + "    static class Builder {\n"
                + "        final String url;\n"
                + "        String method = \"GET\";\n"
                + "        Map<String, String> headers = new HashMap<>();\n"
                + "        byte[] body;\n"
                + "\n"
                + "        Builder(String url) { this.url = url; }\n"
                + "        Builder method(String m) { this.method = m; return this; }\n"
                + "        Builder header(String k, String v) { headers.put(k,v); return this; }\n"
                + "        Builder body(byte[] b) { this.body = b; return this; }\n"
                + "        HttpRequest build() { return new HttpRequest(this); }\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Usage\n"
                + "HttpRequest req = new HttpRequest.Builder(\"https://api.example.com\")\n"
                + "    .method(\"POST\")\n"
                + "    .header(\"Content-Type\", \"application/json\")\n"
                + "    .body(\"{}\".getBytes())\n"
                + "    .build();"));
        return new Lesson("dp.2", "Builder", "Builder", uk, en);
    }

    private static Lesson lessonObserver() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Observer — підписка на події"));
        uk.add(LessonBlock.paragraph(
                "Observer встановлює зв'язок «один-до-багатьох»: коли стан об'єкта змінюється, "
                + "усі підписники сповіщаються. Використовується в UI (ClickListener), "
                + "EventBus, Reactive Streams."));
        uk.add(LessonBlock.code(
                "// Listener interface\n"
                + "interface DataListener {\n"
                + "    void onDataChanged(String data);\n"
                + "}\n"
                + "\n"
                + "// Subject (observable)\n"
                + "class DataSource {\n"
                + "    private final List<DataListener> listeners = new ArrayList<>();\n"
                + "    private String data;\n"
                + "\n"
                + "    void addListener(DataListener l) { listeners.add(l); }\n"
                + "    void removeListener(DataListener l) { listeners.remove(l); }\n"
                + "\n"
                + "    void setData(String data) {\n"
                + "        this.data = data;\n"
                + "        for (DataListener l : listeners) l.onDataChanged(data);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання\n"
                + "DataSource src = new DataSource();\n"
                + "src.addListener(d -> System.out.println(\"Отримано: \" + d));\n"
                + "src.setData(\"нові дані\");  // сповіщає всіх слухачів"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Observer — event subscription"));
        en.add(LessonBlock.paragraph(
                "Observer establishes a one-to-many relationship: when an object's state changes, "
                + "all subscribers are notified. Used in UI (ClickListener), EventBus, Reactive Streams."));
        en.add(LessonBlock.code(
                "// Listener interface\n"
                + "interface DataListener {\n"
                + "    void onDataChanged(String data);\n"
                + "}\n"
                + "\n"
                + "// Subject (observable)\n"
                + "class DataSource {\n"
                + "    private final List<DataListener> listeners = new ArrayList<>();\n"
                + "    private String data;\n"
                + "\n"
                + "    void addListener(DataListener l) { listeners.add(l); }\n"
                + "    void removeListener(DataListener l) { listeners.remove(l); }\n"
                + "\n"
                + "    void setData(String data) {\n"
                + "        this.data = data;\n"
                + "        for (DataListener l : listeners) l.onDataChanged(data);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Usage\n"
                + "DataSource src = new DataSource();\n"
                + "src.addListener(d -> System.out.println(\"Received: \" + d));\n"
                + "src.setData(\"new data\");  // notifies all listeners"));
        return new Lesson("dp.3", "Observer", "Observer", uk, en);
    }

    private static Lesson lessonStrategy() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Strategy — алгоритми як об'єкти"));
        uk.add(LessonBlock.paragraph(
                "Strategy дозволяє змінювати алгоритм «на льоту», передаючи різну поведінку "
                + "як параметр. З Java 8 lambda робить це особливо зручним."));
        uk.add(LessonBlock.code(
                "// Интерфейс стратегії\n"
                + "interface SortStrategy {\n"
                + "    void sort(int[] arr);\n"
                + "}\n"
                + "\n"
                + "// Різні реалізації\n"
                + "class BubbleSort implements SortStrategy {\n"
                + "    public void sort(int[] arr) { /* bubble sort */ }\n"
                + "}\n"
                + "\n"
                + "class QuickSort implements SortStrategy {\n"
                + "    public void sort(int[] arr) { /* quicksort */ }\n"
                + "}\n"
                + "\n"
                + "// Контекст\n"
                + "class Sorter {\n"
                + "    private SortStrategy strategy;\n"
                + "    void setStrategy(SortStrategy s) { this.strategy = s; }\n"
                + "    void doSort(int[] arr) { strategy.sort(arr); }\n"
                + "}\n"
                + "\n"
                + "// З lambda (Java 8+)\n"
                + "Sorter sorter = new Sorter();\n"
                + "sorter.setStrategy(arr -> Arrays.sort(arr));  // strategy як lambda"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Strategy — algorithms as objects"));
        en.add(LessonBlock.paragraph(
                "Strategy allows changing the algorithm on the fly by passing different behaviors "
                + "as parameters. With Java 8 lambdas this becomes especially convenient."));
        en.add(LessonBlock.code(
                "// Strategy interface\n"
                + "interface SortStrategy {\n"
                + "    void sort(int[] arr);\n"
                + "}\n"
                + "\n"
                + "// Different implementations\n"
                + "class BubbleSort implements SortStrategy {\n"
                + "    public void sort(int[] arr) { /* bubble sort */ }\n"
                + "}\n"
                + "\n"
                + "class QuickSort implements SortStrategy {\n"
                + "    public void sort(int[] arr) { /* quicksort */ }\n"
                + "}\n"
                + "\n"
                + "// Context\n"
                + "class Sorter {\n"
                + "    private SortStrategy strategy;\n"
                + "    void setStrategy(SortStrategy s) { this.strategy = s; }\n"
                + "    void doSort(int[] arr) { strategy.sort(arr); }\n"
                + "}\n"
                + "\n"
                + "// With lambda (Java 8+)\n"
                + "Sorter sorter = new Sorter();\n"
                + "sorter.setStrategy(arr -> Arrays.sort(arr));  // strategy as lambda"));
        return new Lesson("dp.4", "Strategy", "Strategy", uk, en);
    }

    private static Lesson lessonFactory() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Factory Method — створення об'єктів через фабрику"));
        uk.add(LessonBlock.paragraph(
                "Factory Method відокремлює логіку створення об'єктів від клієнтського коду. "
                + "Клієнт не знає конкретний клас — отримує інтерфейс."));
        uk.add(LessonBlock.code(
                "// Product interface\n"
                + "interface Logger {\n"
                + "    void log(String message);\n"
                + "}\n"
                + "\n"
                + "class FileLogger implements Logger {\n"
                + "    public void log(String msg) { /* write to file */ }\n"
                + "}\n"
                + "\n"
                + "class ConsoleLogger implements Logger {\n"
                + "    public void log(String msg) { System.out.println(msg); }\n"
                + "}\n"
                + "\n"
                + "// Factory\n"
                + "class LoggerFactory {\n"
                + "    static Logger create(String type) {\n"
                + "        switch (type) {\n"
                + "            case \"file\": return new FileLogger();\n"
                + "            case \"console\": return new ConsoleLogger();\n"
                + "            default: throw new IllegalArgumentException(type);\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Клієнтський код не знає конкретний клас\n"
                + "Logger logger = LoggerFactory.create(\"console\");\n"
                + "logger.log(\"Application started\");"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Factory Method — object creation via factory"));
        en.add(LessonBlock.paragraph(
                "Factory Method separates object creation logic from client code. "
                + "The client doesn't know the concrete class — it receives an interface."));
        en.add(LessonBlock.code(
                "// Product interface\n"
                + "interface Logger {\n"
                + "    void log(String message);\n"
                + "}\n"
                + "\n"
                + "class FileLogger implements Logger {\n"
                + "    public void log(String msg) { /* write to file */ }\n"
                + "}\n"
                + "\n"
                + "class ConsoleLogger implements Logger {\n"
                + "    public void log(String msg) { System.out.println(msg); }\n"
                + "}\n"
                + "\n"
                + "// Factory\n"
                + "class LoggerFactory {\n"
                + "    static Logger create(String type) {\n"
                + "        switch (type) {\n"
                + "            case \"file\": return new FileLogger();\n"
                + "            case \"console\": return new ConsoleLogger();\n"
                + "            default: throw new IllegalArgumentException(type);\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Client code doesn't know the concrete class\n"
                + "Logger logger = LoggerFactory.create(\"console\");\n"
                + "logger.log(\"Application started\");"));
        return new Lesson("dp.5", "Factory Method", "Factory Method", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава: Розширені Design Patterns
    // ═══════════════════════════════════════════════════════════════

    private static void addAdvancedDesignPatterns(Course c) {
        Chapter ch = new Chapter(
                "Розширені Design Patterns",
                "Advanced Design Patterns");
        ch.add(lessonDecorator());
        ch.add(lessonProxy());
        ch.add(lessonCommand());
        c.add(ch);
    }

    private static Lesson lessonDecorator() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Decorator — динамічне додавання поведінки"));
        uk.add(LessonBlock.paragraph(
                "Патерн Decorator дозволяє динамічно додавати об'єктам нову функціональність, "
                + "обгортаючи їх у класи-обгортки. Це гнучка альтернатива успадкуванню. "
                + "У стандартній бібліотеці він інтенсивно використовується в java.io (наприклад, "
                + "BufferedReader обгортає FileReader)."));
        uk.add(LessonBlock.code(
                "interface Notifier {\n"
                + "    void send(String msg);\n"
                + "}\n"
                + "\n"
                + "class EmailNotifier implements Notifier {\n"
                + "    public void send(String msg) { System.out.println(\"Email: \" + msg); }\n"
                + "}\n"
                + "\n"
                + "abstract class NotifierDecorator implements Notifier {\n"
                + "    protected Notifier wrappee;\n"
                + "    NotifierDecorator(Notifier n) { this.wrappee = n; }\n"
                + "    public void send(String msg) { wrappee.send(msg); }\n"
                + "}\n"
                + "\n"
                + "class SMSNotifier extends NotifierDecorator {\n"
                + "    SMSNotifier(Notifier n) { super(n); }\n"
                + "    public void send(String msg) {\n"
                + "        super.send(msg);\n"
                + "        System.out.println(\"SMS: \" + msg);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Використання:\n"
                + "Notifier n = new SMSNotifier(new EmailNotifier());\n"
                + "n.send(\"Увага!\"); // Відправить Email та SMS"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Decorator — dynamically adding behavior"));
        en.add(LessonBlock.paragraph(
                "The Decorator pattern allows adding new functionality to objects dynamically "
                + "by wrapping them in wrapper classes. It is a flexible alternative to inheritance. "
                + "In the standard library, it is heavily used in java.io (e.g., BufferedReader "
                + "wraps FileReader)."));
        en.add(LessonBlock.code(
                "interface Notifier {\n"
                + "    void send(String msg);\n"
                + "}\n"
                + "\n"
                + "class EmailNotifier implements Notifier {\n"
                + "    public void send(String msg) { System.out.println(\"Email: \" + msg); }\n"
                + "}\n"
                + "\n"
                + "abstract class NotifierDecorator implements Notifier {\n"
                + "    protected Notifier wrappee;\n"
                + "    NotifierDecorator(Notifier n) { this.wrappee = n; }\n"
                + "    public void send(String msg) { wrappee.send(msg); }\n"
                + "}\n"
                + "\n"
                + "class SMSNotifier extends NotifierDecorator {\n"
                + "    SMSNotifier(Notifier n) { super(n); }\n"
                + "    public void send(String msg) {\n"
                + "        super.send(msg);\n"
                + "        System.out.println(\"SMS: \" + msg);\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "// Usage:\n"
                + "Notifier n = new SMSNotifier(new EmailNotifier());\n"
                + "n.send(\"Alert!\"); // Sends Email and SMS"));
        return new Lesson("dp.6", "Decorator", "Decorator", uk, en);
    }

    private static Lesson lessonProxy() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Proxy — сурогат для іншого об'єкта"));
        uk.add(LessonBlock.paragraph(
                "Proxy (Замісник) надає об'єкт-замінник, який контролює доступ до оригінального "
                + "об'єкта. Використовується для лінивого завантаження, кешування, контролю доступу "
                + "або логування."));
        uk.add(LessonBlock.code(
                "interface Database {\n"
                + "    void query(String sql);\n"
                + "}\n"
                + "\n"
                + "class RealDatabase implements Database {\n"
                + "    public RealDatabase() { /* важке підключення */ }\n"
                + "    public void query(String sql) { System.out.println(\"Exec: \" + sql); }\n"
                + "}\n"
                + "\n"
                + "class DatabaseProxy implements Database {\n"
                + "    private RealDatabase realDb;\n"
                + "    private boolean isAdmin;\n"
                + "\n"
                + "    DatabaseProxy(boolean isAdmin) { this.isAdmin = isAdmin; }\n"
                + "\n"
                + "    public void query(String sql) {\n"
                + "        if (!isAdmin && sql.contains(\"DROP\")) {\n"
                + "            throw new SecurityException(\"Access Denied\");\n"
                + "        }\n"
                + "        if (realDb == null) realDb = new RealDatabase(); // Лінива ініціалізація\n"
                + "        realDb.query(sql);\n"
                + "    }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Proxy — surrogate for another object"));
        en.add(LessonBlock.paragraph(
                "Proxy provides a surrogate object that controls access to the original object. "
                + "It is used for lazy loading, caching, access control, or logging."));
        en.add(LessonBlock.code(
                "interface Database {\n"
                + "    void query(String sql);\n"
                + "}\n"
                + "\n"
                + "class RealDatabase implements Database {\n"
                + "    public RealDatabase() { /* heavy connection setup */ }\n"
                + "    public void query(String sql) { System.out.println(\"Exec: \" + sql); }\n"
                + "}\n"
                + "\n"
                + "class DatabaseProxy implements Database {\n"
                + "    private RealDatabase realDb;\n"
                + "    private boolean isAdmin;\n"
                + "\n"
                + "    DatabaseProxy(boolean isAdmin) { this.isAdmin = isAdmin; }\n"
                + "\n"
                + "    public void query(String sql) {\n"
                + "        if (!isAdmin && sql.contains(\"DROP\")) {\n"
                + "            throw new SecurityException(\"Access Denied\");\n"
                + "        }\n"
                + "        if (realDb == null) realDb = new RealDatabase(); // Lazy initialization\n"
                + "        realDb.query(sql);\n"
                + "    }\n"
                + "}"));
        return new Lesson("dp.7", "Proxy", "Proxy", uk, en);
    }

    private static Lesson lessonCommand() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Command — інкапсуляція запиту"));
        uk.add(LessonBlock.paragraph(
                "Command перетворює запит на самостійний об'єкт. Це дозволяє параметризувати "
                + "методи різними запитами, ставити запити в чергу, логувати їх, а також "
                + "підтримувати скасування операцій (undo)."));
        uk.add(LessonBlock.code(
                "interface Command {\n"
                + "    void execute();\n"
                + "}\n"
                + "\n"
                + "class TextEditor {\n"
                + "    String text = \"\";\n"
                + "    void addText(String s) { text += s; }\n"
                + "}\n"
                + "\n"
                + "class AddTextCommand implements Command {\n"
                + "    private TextEditor editor;\n"
                + "    private String textToAdd;\n"
                + "    \n"
                + "    AddTextCommand(TextEditor editor, String text) {\n"
                + "        this.editor = editor;\n"
                + "        this.textToAdd = text;\n"
                + "    }\n"
                + "    public void execute() { editor.addText(textToAdd); }\n"
                + "}\n"
                + "\n"
                + "class CommandInvoker {\n"
                + "    private List<Command> history = new ArrayList<>();\n"
                + "    void executeCommand(Command c) {\n"
                + "        c.execute();\n"
                + "        history.add(c);\n"
                + "    }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Command — encapsulating a request"));
        en.add(LessonBlock.paragraph(
                "Command turns a request into a stand-alone object. This allows parameterizing "
                + "methods with different requests, queuing or logging requests, and supporting "
                + "undoable operations."));
        en.add(LessonBlock.code(
                "interface Command {\n"
                + "    void execute();\n"
                + "}\n"
                + "\n"
                + "class TextEditor {\n"
                + "    String text = \"\";\n"
                + "    void addText(String s) { text += s; }\n"
                + "}\n"
                + "\n"
                + "class AddTextCommand implements Command {\n"
                + "    private TextEditor editor;\n"
                + "    private String textToAdd;\n"
                + "    \n"
                + "    AddTextCommand(TextEditor editor, String text) {\n"
                + "        this.editor = editor;\n"
                + "        this.textToAdd = text;\n"
                + "    }\n"
                + "    public void execute() { editor.addText(textToAdd); }\n"
                + "}\n"
                + "\n"
                + "class CommandInvoker {\n"
                + "    private List<Command> history = new ArrayList<>();\n"
                + "    void executeCommand(Command c) {\n"
                + "        c.execute();\n"
                + "        history.add(c);\n"
                + "    }\n"
                + "}"));
        return new Lesson("dp.8", "Command", "Command", uk, en);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Глава: Java 8-альтернативи новішим фічам
    // ═══════════════════════════════════════════════════════════════

    private static void addModernJavaFeatures(Course c) {
        Chapter ch = new Chapter(
                "Java 8-альтернативи новішим можливостям",
                "Java 8 alternatives to newer features");
        ch.add(lessonRecords());
        ch.add(lessonSealedClasses());
        ch.add(lessonPatternMatching());
        ch.add(lessonSwitchExpressions());
        c.add(ch);
    }

    private static Lesson lessonRecords() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Record-подібні класи у Java 8"));
        uk.add(LessonBlock.paragraph(
                "У новіших Java є records, але JDK 8 їх не підтримує. Для Android 26/JDK 8 "
                + "пишіть звичайний immutable-клас: private final поля, конструктор, гетери, "
                + "equals(), hashCode() та toString()."));
        uk.add(LessonBlock.code(
                "public final class Point {\n"
                + "    private final int x;\n"
                + "    private final int y;\n"
                + "\n"
                + "    public Point(int x, int y) {\n"
                + "        this.x = x;\n"
                + "        this.y = y;\n"
                + "    }\n"
                + "\n"
                + "    public int getX() { return x; }\n"
                + "    public int getY() { return y; }\n"
                + "\n"
                + "    @Override public String toString() {\n"
                + "        return \"Point{x=\" + x + \", y=\" + y + \"}\";\n"
                + "    }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Валідацію робіть у конструкторі. Так об'єкт одразу створюється у правильному стані:"));
        uk.add(LessonBlock.code(
                "public final class User {\n"
                + "    private final String username;\n"
                + "    private final int age;\n"
                + "\n"
                + "    public User(String username, int age) {\n"
                + "        if (username == null || username.trim().isEmpty()) {\n"
                + "            throw new IllegalArgumentException(\"Empty username\");\n"
                + "        }\n"
                + "        if (age < 0) {\n"
                + "            throw new IllegalArgumentException(\"Age < 0\");\n"
                + "        }\n"
                + "        this.username = username;\n"
                + "        this.age = age;\n"
                + "    }\n"
                + "    \n"
                + "    public boolean isAdult() { return age >= 18; }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Record-like classes in Java 8"));
        en.add(LessonBlock.paragraph(
                "Newer Java versions have records, but JDK 8 does not support them. For Android "
                + "26/JDK 8, write a regular immutable class: private final fields, constructor, "
                + "getters, equals(), hashCode(), and toString()."));
        en.add(LessonBlock.code(
                "public final class Point {\n"
                + "    private final int x;\n"
                + "    private final int y;\n"
                + "\n"
                + "    public Point(int x, int y) {\n"
                + "        this.x = x;\n"
                + "        this.y = y;\n"
                + "    }\n"
                + "\n"
                + "    public int getX() { return x; }\n"
                + "    public int getY() { return y; }\n"
                + "\n"
                + "    @Override public String toString() {\n"
                + "        return \"Point{x=\" + x + \", y=\" + y + \"}\";\n"
                + "    }\n"
                + "}"));
        en.add(LessonBlock.paragraph(
                "Put validation in the constructor. That way the object is created in a valid state:"));
        en.add(LessonBlock.code(
                "public final class User {\n"
                + "    private final String username;\n"
                + "    private final int age;\n"
                + "\n"
                + "    public User(String username, int age) {\n"
                + "        if (username == null || username.trim().isEmpty()) {\n"
                + "            throw new IllegalArgumentException(\"Empty username\");\n"
                + "        }\n"
                + "        if (age < 0) {\n"
                + "            throw new IllegalArgumentException(\"Age < 0\");\n"
                + "        }\n"
                + "        this.username = username;\n"
                + "        this.age = age;\n"
                + "    }\n"
                + "    \n"
                + "    public boolean isAdult() { return age >= 18; }\n"
                + "}"));
        return new Lesson("mod.1", "Immutable-класи замість records", "Immutable classes instead of records", uk, en);
    }

    private static Lesson lessonSealedClasses() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Контроль ієрархії у Java 8"));
        uk.add(LessonBlock.paragraph(
                "У JDK 8 немає sealed classes. Але можна зробити ієрархію зрозумілою: "
                + "базовий abstract class, підкласи final, а конструктор базового класу — "
                + "package-private або protected, залежно від задачі."));
        uk.add(LessonBlock.code(
                "abstract class Shape {\n"
                + "    // protected: створювати Shape напряму не можна, але підкласи можуть\n"
                + "    protected Shape() { }\n"
                + "\n"
                + "    abstract double area();\n"
                + "}\n"
                + "\n"
                + "final class Circle extends Shape {\n"
                + "    private final double radius;\n"
                + "\n"
                + "    Circle(double radius) { this.radius = radius; }\n"
                + "\n"
                + "    @Override double area() {\n"
                + "        return Math.PI * radius * radius;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "final class Rectangle extends Shape {\n"
                + "    private final double width;\n"
                + "    private final double height;\n"
                + "\n"
                + "    Rectangle(double width, double height) {\n"
                + "        this.width = width;\n"
                + "        this.height = height;\n"
                + "    }\n"
                + "\n"
                + "    @Override double area() { return width * height; }\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Якщо всі класи ієрархії лежать в одному пакеті, такий підхід дисциплінує дизайн. "
                + "Для повного контролю нових підкласів у JDK 8 використовуйте code review і документацію."));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Controlling hierarchies in Java 8"));
        en.add(LessonBlock.paragraph(
                "JDK 8 has no sealed classes. But you can still make a hierarchy clear: "
                + "an abstract base class, final subclasses, and a package-private or protected "
                + "base constructor depending on the design."));
        en.add(LessonBlock.code(
                "abstract class Shape {\n"
                + "    // protected: Shape cannot be instantiated directly, but subclasses can\n"
                + "    protected Shape() { }\n"
                + "\n"
                + "    abstract double area();\n"
                + "}\n"
                + "\n"
                + "final class Circle extends Shape {\n"
                + "    private final double radius;\n"
                + "\n"
                + "    Circle(double radius) { this.radius = radius; }\n"
                + "\n"
                + "    @Override double area() {\n"
                + "        return Math.PI * radius * radius;\n"
                + "    }\n"
                + "}\n"
                + "\n"
                + "final class Rectangle extends Shape {\n"
                + "    private final double width;\n"
                + "    private final double height;\n"
                + "\n"
                + "    Rectangle(double width, double height) {\n"
                + "        this.width = width;\n"
                + "        this.height = height;\n"
                + "    }\n"
                + "\n"
                + "    @Override double area() { return width * height; }\n"
                + "}"));
        en.add(LessonBlock.paragraph(
                "If all hierarchy classes live in one package, this approach keeps the design disciplined. "
                + "For full subclass control in JDK 8, rely on code review and documentation."));
        return new Lesson("mod.2", "Ієрархії без sealed", "Hierarchies without sealed", uk, en);
    }

    private static Lesson lessonPatternMatching() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("instanceof та безпечний cast у Java 8"));
        uk.add(LessonBlock.paragraph(
                "У Java 8 після перевірки instanceof треба явно привести тип. Це трохи довше, "
                + "зате повністю сумісно з компілятором JDK 8 і добре показує, що відбувається."));
        uk.add(LessonBlock.code(
                "Object obj = \"JavaDroid\";\n"
                + "\n"
                + "if (obj instanceof String) {\n"
                + "    String s = (String) obj;\n"
                + "    System.out.println(s.length());\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Якщо треба додаткова умова, перевіряйте її після приведення типу або вкладеним if:"));
        uk.add(LessonBlock.code(
                "if (obj instanceof String) {\n"
                + "    String s = (String) obj;\n"
                + "    if (s.length() > 5) {\n"
                + "        System.out.println(\"Довгий рядок: \" + s);\n"
                + "    }\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("instanceof and safe casting in Java 8"));
        en.add(LessonBlock.paragraph(
                "In Java 8, after an instanceof check, you explicitly cast the object. It is a "
                + "little longer, but fully compatible with a JDK 8 compiler and clear for beginners."));
        en.add(LessonBlock.code(
                "Object obj = \"JavaDroid\";\n"
                + "\n"
                + "if (obj instanceof String) {\n"
                + "    String s = (String) obj;\n"
                + "    System.out.println(s.length());\n"
                + "}"));
        en.add(LessonBlock.paragraph(
                "If you need an additional condition, check it after the cast or in a nested if:"));
        en.add(LessonBlock.code(
                "if (obj instanceof String) {\n"
                + "    String s = (String) obj;\n"
                + "    if (s.length() > 5) {\n"
                + "        System.out.println(\"Long string: \" + s);\n"
                + "    }\n"
                + "}"));
        return new Lesson("mod.3", "instanceof у Java 8", "instanceof in Java 8", uk, en);
    }

    private static Lesson lessonSwitchExpressions() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Класичний switch у Java 8"));
        uk.add(LessonBlock.paragraph(
                "У JDK 8 switch є оператором, а не expression. Якщо треба отримати значення, "
                + "створіть змінну перед switch і присвойте її в кожному case."));
        uk.add(LessonBlock.code(
                "String dayOfWeek = \"MONDAY\";\n"
                + "String type;\n"
                + "\n"
                + "switch (dayOfWeek) {\n"
                + "    case \"MONDAY\":\n"
                + "    case \"FRIDAY\":\n"
                + "    case \"SUNDAY\":\n"
                + "        type = \"Гарний день\";\n"
                + "        break;\n"
                + "    case \"TUESDAY\":\n"
                + "        type = \"Робочий день\";\n"
                + "        break;\n"
                + "    default:\n"
                + "        System.out.println(\"Обчислення...\");\n"
                + "        type = \"Звичайний день\";\n"
                + "        break;\n"
                + "}\n"
                + "System.out.println(type);"));
        uk.add(LessonBlock.paragraph(
                "Для розгалуження за типом у Java 8 використовуйте instanceof. Порядок перевірок "
                + "важливий: спершу більш конкретні типи, потім загальніші."));
        uk.add(LessonBlock.code(
                "static String format(Object obj) {\n"
                + "    if (obj == null) return \"null value\";\n"
                + "    if (obj instanceof Integer) {\n"
                + "        return String.format(\"int %d\", (Integer) obj);\n"
                + "    }\n"
                + "    if (obj instanceof Long) {\n"
                + "        return String.format(\"long %d\", (Long) obj);\n"
                + "    }\n"
                + "    if (obj instanceof Double) {\n"
                + "        return String.format(\"double %f\", (Double) obj);\n"
                + "    }\n"
                + "    if (obj instanceof String) {\n"
                + "        return String.format(\"String %s\", (String) obj);\n"
                + "    }\n"
                + "    return obj.toString();\n"
                + "}"));
        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Classic switch in Java 8"));
        en.add(LessonBlock.paragraph(
                "In JDK 8, switch is a statement, not an expression. If you need a value, "
                + "create a variable before switch and assign it in every case."));
        en.add(LessonBlock.code(
                "String dayOfWeek = \"MONDAY\";\n"
                + "String type;\n"
                + "\n"
                + "switch (dayOfWeek) {\n"
                + "    case \"MONDAY\":\n"
                + "    case \"FRIDAY\":\n"
                + "    case \"SUNDAY\":\n"
                + "        type = \"Good day\";\n"
                + "        break;\n"
                + "    case \"TUESDAY\":\n"
                + "        type = \"Working day\";\n"
                + "        break;\n"
                + "    default:\n"
                + "        System.out.println(\"Calculating...\");\n"
                + "        type = \"Regular day\";\n"
                + "        break;\n"
                + "}\n"
                + "System.out.println(type);"));
        en.add(LessonBlock.paragraph(
                "For branching by type in Java 8, use instanceof. The order matters: check "
                + "more specific types first, then more general ones."));
        en.add(LessonBlock.code(
                "static String format(Object obj) {\n"
                + "    if (obj == null) return \"null value\";\n"
                + "    if (obj instanceof Integer) {\n"
                + "        return String.format(\"int %d\", (Integer) obj);\n"
                + "    }\n"
                + "    if (obj instanceof Long) {\n"
                + "        return String.format(\"long %d\", (Long) obj);\n"
                + "    }\n"
                + "    if (obj instanceof Double) {\n"
                + "        return String.format(\"double %f\", (Double) obj);\n"
                + "    }\n"
                + "    if (obj instanceof String) {\n"
                + "        return String.format(\"String %s\", (String) obj);\n"
                + "    }\n"
                + "    return obj.toString();\n"
                + "}"));
        return new Lesson("mod.4", "switch у Java 8", "switch in Java 8", uk, en);
    }
}
