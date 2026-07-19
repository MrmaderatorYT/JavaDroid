package com.ccs.javadroid.learn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Marks the curated Ukrainian JDK 8 reference examples as safe inline runs. */
final class Jdk8RunnableExamples {

    private static final String MARKER = "Детальний довідник JDK 8";
    private static final Set<String> STATEMENT_SNIPPETS = new HashSet<>(Arrays.asList(
            "jdk8.collections.1",
            "jdk8.collections.2",
            "jdk8.collections.3",
            "jdk8.collections.4",
            "jdk8.collections.5",
            "jdk8.setmap.1",
            "jdk8.setmap.2",
            "jdk8.setmap.3",
            "jdk8.algorithms.1",
            "jdk8.algorithms.2",
            "jdk8.algorithms.3",
            "jdk8.datetime.1",
            "jdk8.concurrent.1",
            "jdk8.concurrent.2",
            "jdk8.functional.1",
            "jdk8.functional.2",
            "jdk8.platform.2"));
    private static final Set<String> BASE_STATEMENT_SNIPPETS = new HashSet<>(Arrays.asList(
            "jdk8.collections.1#1",
            "jdk8.collections.2#1", "jdk8.collections.2#2",
            "jdk8.collections.3#1", "jdk8.collections.4#1",
            "jdk8.collections.5#1", "jdk8.collections.5#2", "jdk8.collections.5#3",
            "jdk8.setmap.1#1", "jdk8.setmap.2#1",
            "jdk8.setmap.3#1", "jdk8.setmap.3#2",
            "jdk8.algorithms.1#1", "jdk8.algorithms.1#2",
            "jdk8.algorithms.3#1", "jdk8.algorithms.3#2",
            "jdk8.generics.1#1", "jdk8.generics.2#1", "jdk8.generics.2#2",
            "jdk8.oop.1#1", "jdk8.datetime.1#1",
            "jdk8.concurrent.2#1",
            "jdk8.functional.1#1", "jdk8.functional.1#2",
            "jdk8.functional.2#1",
            "jdk8.functional.4#1", "jdk8.functional.4#2"));

    private Jdk8RunnableExamples() {
    }

    static void apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.materials) {
                if (!lesson.id.startsWith("jdk8.") || lesson.id.startsWith("jdk8.bytecode.")) {
                    continue;
                }
                markBaseExamples(lesson);
                markReferenceExample(lesson);
            }
        }
    }

    private static void markBaseExamples(Lesson lesson) {
        List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
        int codeOrdinal = 0;
        for (int i = 0; i < uk.size(); i++) {
            LessonBlock block = uk.get(i);
            if (block.type == LessonBlock.HEADING && MARKER.equals(block.text)) return;
            if (block.type != LessonBlock.CODE) continue;
            codeOrdinal++;
            String key = lesson.id + "#" + codeOrdinal;
            if (BASE_STATEMENT_SNIPPETS.contains(key)) {
                uk.set(i, LessonBlock.runnableCode(block.text));
                continue;
            }
            String source = baseFullSource(key);
            if (source != null) {
                uk.set(i, LessonBlock.runnableCode(block.text, source));
            }
        }
    }

    private static String baseFullSource(String key) {
        switch (key) {
            case "jdk8.setmap.2#2":
                return source("",
                        "        String[] words = {\"java\", \"list\", \"java\", \"map\"};\n"
                                + "        Map<String, Integer> count2 = new HashMap<String, Integer>();\n"
                                + "        for (String word : words) {\n"
                                + "            count2.merge(word, 1, (a, b) -> a + b);\n"
                                + "        }\n"
                                + "        System.out.println(count2);\n");
            case "jdk8.algorithms.2#1":
                return source("",
                        "        List<String> names = Arrays.asList(\"Ira\", \"Oleh\", \"Anna\", \"Ivan\");\n"
                                + "        List<String> result = names.stream()\n"
                                + "                .filter(s -> s.length() >= 4)\n"
                                + "                .map(String::toUpperCase).sorted()\n"
                                + "                .collect(Collectors.toList());\n"
                                + "        System.out.println(result);\n");
            case "jdk8.generics.1#2":
                return source(
                        "    static final class Box<T> {\n"
                                + "        private T value;\n"
                                + "        Box(T value) { this.value = value; }\n"
                                + "        T get() { return value; }\n"
                                + "        void set(T value) { this.value = value; }\n"
                                + "    }\n",
                        "        Box<Integer> intBox = new Box<Integer>(10);\n"
                                + "        Box<String> strBox = new Box<String>(\"hello\");\n"
                                + "        System.out.println(intBox.get() + \" / \" + strBox.get());\n");
            case "jdk8.generics.3#1":
                return source(
                        "    static double sum(List<? extends Number> numbers) {\n"
                                + "        double total = 0;\n"
                                + "        for (Number n : numbers) total += n.doubleValue();\n"
                                + "        return total;\n"
                                + "    }\n"
                                + "    static void addDefaults(List<? super Integer> target) {\n"
                                + "        target.add(1); target.add(2);\n"
                                + "    }\n",
                        "        List<Number> values = new ArrayList<Number>();\n"
                                + "        addDefaults(values);\n"
                                + "        System.out.println(values + \" sum=\" + sum(values));\n");
            case "jdk8.oop.2#1":
                return source(
                        "    interface PaymentProcessor { void pay(int cents); }\n"
                                + "    static final class CardPaymentProcessor implements PaymentProcessor {\n"
                                + "        public void pay(int cents) { System.out.println(\"Paid by card: \" + cents); }\n"
                                + "    }\n"
                                + "    static final class CheckoutService {\n"
                                + "        private final PaymentProcessor processor;\n"
                                + "        CheckoutService(PaymentProcessor processor) { this.processor = processor; }\n"
                                + "        void checkout(int cents) { processor.pay(cents); }\n"
                                + "    }\n",
                        "        new CheckoutService(new CardPaymentProcessor()).checkout(1250);\n");
            case "jdk8.exceptions.1#1":
                return source(
                        "    static int parseAge(String text) {\n"
                                + "        if (text == null) throw new IllegalArgumentException(\"age text is null\");\n"
                                + "        int age = Integer.parseInt(text);\n"
                                + "        if (age < 0) throw new IllegalArgumentException(\"age must be positive\");\n"
                                + "        return age;\n"
                                + "    }\n",
                        "        System.out.println(parseAge(\"27\"));\n"
                                + "        try { parseAge(\"-1\"); }\n"
                                + "        catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }\n");
            case "jdk8.exceptions.2#1":
                return source("",
                        "        Path input = Files.createTempFile(\"javadroid-input-\", \".txt\");\n"
                                + "        Path output = Files.createTempFile(\"javadroid-output-\", \".txt\");\n"
                                + "        Files.write(input, Arrays.asList(\"A\", \"B\"), StandardCharsets.UTF_8);\n"
                                + "        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8);\n"
                                + "             BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {\n"
                                + "            String line;\n"
                                + "            while ((line = reader.readLine()) != null) { writer.write(line); writer.newLine(); }\n"
                                + "        }\n"
                                + "        System.out.println(Files.readAllLines(output, StandardCharsets.UTF_8));\n"
                                + "        Files.deleteIfExists(input); Files.deleteIfExists(output);\n");
            case "jdk8.exceptions.2#2":
                return source(
                        "    static final class BrokenResource implements AutoCloseable {\n"
                                + "        public void close() throws IOException { throw new IOException(\"close failed\"); }\n"
                                + "    }\n",
                        "        try (BrokenResource ignored = new BrokenResource()) {\n"
                                + "            throw new IOException(\"main failed\");\n"
                                + "        } catch (IOException e) {\n"
                                + "            System.out.println(\"Main: \" + e.getMessage());\n"
                                + "            for (Throwable s : e.getSuppressed())\n"
                                + "                System.out.println(\"Suppressed: \" + s.getMessage());\n"
                                + "        }\n");
            case "jdk8.io.1#1":
                return source("",
                        "        Path directory = Files.createTempDirectory(\"javadroid-data-\");\n"
                                + "        Path path = directory.resolve(\"names.txt\");\n"
                                + "        List<String> lines = Arrays.asList(\"Ira\", \"Oleh\", \"Anna\");\n"
                                + "        Files.write(path, lines, StandardCharsets.UTF_8);\n"
                                + "        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8))\n"
                                + "            System.out.println(line);\n"
                                + "        Files.deleteIfExists(path); Files.deleteIfExists(directory);\n");
            case "jdk8.concurrent.1#1":
                return source(
                        "    static final class Counter {\n"
                                + "        private int value;\n"
                                + "        synchronized void increment() { value++; }\n"
                                + "        synchronized int get() { return value; }\n"
                                + "    }\n",
                        "        Counter counter = new Counter();\n"
                                + "        Thread a = new Thread(() -> { for (int i=0; i<1000; i++) counter.increment(); });\n"
                                + "        Thread b = new Thread(() -> { for (int i=0; i<1000; i++) counter.increment(); });\n"
                                + "        a.start(); b.start(); a.join(); b.join();\n"
                                + "        System.out.println(counter.get());\n");
            case "jdk8.functional.2#2":
                return source(
                        "    interface HasName {\n"
                                + "        String getName();\n"
                                + "        default String displayName() { return getName().trim().toUpperCase(); }\n"
                                + "    }\n"
                                + "    static final class User implements HasName {\n"
                                + "        private final String name; User(String name) { this.name = name; }\n"
                                + "        public String getName() { return name; }\n"
                                + "    }\n",
                        "        System.out.println(new User(\" JavaDroid \" ).displayName());\n");
            case "jdk8.functional.3#1":
                return source(
                        "    static Optional<String> findNameById(int id) {\n"
                                + "        return id == 10 ? Optional.of(\" JavaDroid \" ) : Optional.empty();\n"
                                + "    }\n",
                        "        Optional<String> maybeName = findNameById(10);\n"
                                + "        String display = maybeName.map(String::trim)\n"
                                + "                .filter(s -> !s.isEmpty()).orElse(\"Unknown\");\n"
                                + "        System.out.println(display);\n");
            case "jdk8.runtime.1#1":
            case "jdk8.runtime.1#2":
                return source(
                        "    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)\n"
                                + "    @interface Command { String value(); }\n"
                                + "    static final class ConsoleActions {\n"
                                + "        @Command(\"hello\") public void hello() { System.out.println(\"Hello!\"); }\n"
                                + "    }\n",
                        "        ConsoleActions actions = new ConsoleActions();\n"
                                + "        for (Method method : ConsoleActions.class.getDeclaredMethods()) {\n"
                                + "            Command cmd = method.getAnnotation(Command.class);\n"
                                + "            if (cmd != null && \"hello\".equals(cmd.value())) method.invoke(actions);\n"
                                + "        }\n");
            case "jdk8.runtime.2#1":
            case "jdk8.runtime.2#2":
                return source(
                        "    static final class User implements Serializable {\n"
                                + "        private static final long serialVersionUID = 1L;\n"
                                + "        private final int id; private final String name; private transient String password;\n"
                                + "        User(int id, String name, String password) {\n"
                                + "            this.id=id; this.name=name; this.password=password;\n"
                                + "        }\n"
                                + "        public String toString() { return id + \" \" + name + \" password=\" + password; }\n"
                                + "    }\n",
                        "        User user = new User(1, \"Ira\", \"secret\");\n"
                                + "        ByteArrayOutputStream bytes = new ByteArrayOutputStream();\n"
                                + "        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(user); }\n"
                                + "        try (ObjectInputStream in = new ObjectInputStream(\n"
                                + "                new ByteArrayInputStream(bytes.toByteArray()))) {\n"
                                + "            System.out.println(in.readObject());\n"
                                + "        }\n");
            default:
                return null;
        }
    }

    private static void markReferenceExample(Lesson lesson) {
        List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
        boolean afterMarker = false;
        for (int i = 0; i < uk.size(); i++) {
            LessonBlock block = uk.get(i);
            if (block.type == LessonBlock.HEADING && MARKER.equals(block.text)) {
                afterMarker = true;
                continue;
            }
            if (!afterMarker || block.type != LessonBlock.CODE) continue;

            if (STATEMENT_SNIPPETS.contains(lesson.id)) {
                uk.set(i, LessonBlock.runnableCode(block.text));
            } else {
                String source = fullSource(lesson.id);
                if (source != null) {
                    uk.set(i, LessonBlock.runnableCode(block.text, source));
                }
            }
            return;
        }
    }

    private static String fullSource(String id) {
        switch (id) {
            case "jdk8.1":
                return source(
                        "    static void inspect(List<String> list) {\n"
                                + "        System.out.println(list.getClass().getSimpleName());\n"
                                + "        System.out.println(\"size=\" + list.size());\n"
                                + "        System.out.println(\"allows duplicate: \" + list.add(list.get(0)));\n"
                                + "    }\n",
                        "        inspect(new ArrayList<String>(Arrays.asList(\"A\", \"B\")));\n");
            case "jdk8.practice.1":
                return source(
                        "    static <T> Map<T, Integer> histogram(Iterable<T> source) {\n"
                                + "        Map<T, Integer> result = new LinkedHashMap<T, Integer>();\n"
                                + "        for (T value : source) result.merge(value, 1, Integer::sum);\n"
                                + "        return result;\n"
                                + "    }\n",
                        "        System.out.println(histogram(Arrays.asList(\"a\", \"b\", \"a\")));\n");
            case "jdk8.generics.1":
                return source(
                        "    static <T extends Number & Comparable<T>> T max(T a, T b) {\n"
                                + "        return a.compareTo(b) >= 0 ? a : b;\n"
                                + "    }\n"
                                + "    static <T> T requireFirst(List<T> values) {\n"
                                + "        if (values.isEmpty()) throw new NoSuchElementException();\n"
                                + "        return values.get(0);\n"
                                + "    }\n",
                        "        System.out.println(max(10, 20));\n"
                                + "        System.out.println(requireFirst(Arrays.asList(\"Java\", \"JDK\")));\n");
            case "jdk8.generics.2":
                return source(
                        "    static class Parent<T> { T value() { return null; } }\n"
                                + "    static class Child extends Parent<String> {\n"
                                + "        @Override String value() { return \"ok\"; }\n"
                                + "    }\n",
                        "        Parent<String> value = new Child();\n"
                                + "        System.out.println(value.value());\n"
                                + "        for (Method m : Child.class.getDeclaredMethods())\n"
                                + "            System.out.println(m.getName() + \" bridge=\" + m.isBridge());\n");
            case "jdk8.generics.3":
                return source(
                        "    static double sum(List<? extends Number> values) {\n"
                                + "        double total = 0.0;\n"
                                + "        for (Number n : values) total += n.doubleValue();\n"
                                + "        return total;\n"
                                + "    }\n"
                                + "    static void addDefaults(List<? super Integer> out) { out.add(0); out.add(1); }\n"
                                + "    static void swapFirstTwo(List<?> list) { swap(list, 0, 1); }\n"
                                + "    private static <T> void swap(List<T> list, int a, int b) {\n"
                                + "        list.set(a, list.set(b, list.get(a)));\n"
                                + "    }\n",
                        "        List<Number> values = new ArrayList<Number>(Arrays.asList(10, 2.5));\n"
                                + "        addDefaults(values);\n"
                                + "        swapFirstTwo(values);\n"
                                + "        System.out.println(values + \" sum=\" + sum(values));\n");
            case "jdk8.oop.1":
                return source(
                        "    static final class Point {\n"
                                + "        private final int x, y;\n"
                                + "        Point(int x, int y) { this.x = x; this.y = y; }\n"
                                + "        @Override public boolean equals(Object obj) {\n"
                                + "            if (this == obj) return true;\n"
                                + "            if (!(obj instanceof Point)) return false;\n"
                                + "            Point other = (Point) obj;\n"
                                + "            return x == other.x && y == other.y;\n"
                                + "        }\n"
                                + "        @Override public int hashCode() { return 31 * x + y; }\n"
                                + "        @Override public String toString() { return \"Point(\" + x + \", \" + y + \")\"; }\n"
                                + "    }\n",
                        "        Point a = new Point(2, 3);\n"
                                + "        Point b = new Point(2, 3);\n"
                                + "        System.out.println(a + \" equals \" + b + \" -> \" + a.equals(b));\n"
                                + "        System.out.println(\"hashCodes: \" + a.hashCode() + \" / \" + b.hashCode());\n");
            case "jdk8.oop.2":
                return source(
                        "    static class Base {\n"
                                + "        String field = \"base\";\n"
                                + "        String value() { return \"base\"; }\n"
                                + "        static String kind() { return \"Base\"; }\n"
                                + "    }\n"
                                + "    static class Derived extends Base {\n"
                                + "        String field = \"derived\";\n"
                                + "        @Override String value() { return \"derived\"; }\n"
                                + "        static String kind() { return \"Derived\"; }\n"
                                + "    }\n",
                        "        Base x = new Derived();\n"
                                + "        System.out.println(x.field + \" / \" + x.value() + \" / \" + x.kind());\n");
            case "jdk8.exceptions.1":
                return source(
                        "    static int parsePort(String text) {\n"
                                + "        try {\n"
                                + "            int port = Integer.parseInt(text);\n"
                                + "            if (port < 1 || port > 65535)\n"
                                + "                throw new IllegalArgumentException(\"port out of range: \" + port);\n"
                                + "            return port;\n"
                                + "        } catch (NumberFormatException e) {\n"
                                + "            throw new IllegalArgumentException(\"not a decimal port: \" + text, e);\n"
                                + "        }\n"
                                + "    }\n",
                        "        System.out.println(\"valid: \" + parsePort(\"8080\"));\n"
                                + "        try { parsePort(\"eight\"); }\n"
                                + "        catch (IllegalArgumentException e) {\n"
                                + "            System.out.println(e.getMessage());\n"
                                + "            System.out.println(\"cause: \" + e.getCause().getClass().getSimpleName());\n"
                                + "        }\n");
            case "jdk8.exceptions.2":
                return source("",
                        "        Path path = Files.createTempFile(\"javadroid-resource-\", \".txt\");\n"
                                + "        Files.write(path, Arrays.asList(\"65\"), StandardCharsets.UTF_8);\n"
                                + "        try (InputStream in = Files.newInputStream(path);\n"
                                + "             BufferedInputStream buffered = new BufferedInputStream(in)) {\n"
                                + "            System.out.println(\"first byte=\" + buffered.read());\n"
                                + "        } finally {\n"
                                + "            Files.deleteIfExists(path);\n"
                                + "        }\n");
            case "jdk8.io.1":
                return source("",
                        "        Charset utf8 = StandardCharsets.UTF_8;\n"
                                + "        Path target = Files.createTempFile(\"javadroid-notes-\", \".txt\");\n"
                                + "        try {\n"
                                + "            try (BufferedWriter out = Files.newBufferedWriter(target, utf8,\n"
                                + "                    StandardOpenOption.TRUNCATE_EXISTING)) {\n"
                                + "                out.write(\"Привіт\"); out.newLine();\n"
                                + "            }\n"
                                + "            try (Stream<String> lines = Files.lines(target, utf8)) {\n"
                                + "                lines.forEach(System.out::println);\n"
                                + "            }\n"
                                + "        } finally { Files.deleteIfExists(target); }\n");
            case "jdk8.capstone.1":
                return source(
                        "    interface Repository<K, V> {\n"
                                + "        Optional<V> find(K key) throws IOException;\n"
                                + "        void save(K key, V value) throws IOException;\n"
                                + "    }\n"
                                + "    static final class MemoryRepository<K, V> implements Repository<K, V> {\n"
                                + "        private final Map<K, V> data = new HashMap<K, V>();\n"
                                + "        public Optional<V> find(K key) { return Optional.ofNullable(data.get(key)); }\n"
                                + "        public void save(K key, V value) { data.put(key, value); }\n"
                                + "    }\n"
                                + "    static final class Service<K, V> {\n"
                                + "        private final Repository<K, V> repository;\n"
                                + "        Service(Repository<K, V> repository) {\n"
                                + "            this.repository = Objects.requireNonNull(repository);\n"
                                + "        }\n"
                                + "        Optional<V> find(K key) throws IOException {\n"
                                + "            return repository.find(Objects.requireNonNull(key));\n"
                                + "        }\n"
                                + "    }\n",
                        "        MemoryRepository<Integer, String> repository = new MemoryRepository<Integer, String>();\n"
                                + "        repository.save(7, \"JavaDroid\");\n"
                                + "        Service<Integer, String> service = new Service<Integer, String>(repository);\n"
                                + "        System.out.println(service.find(7).orElse(\"missing\"));\n");
            case "jdk8.functional.3":
                return source(
                        "    static final class User {\n"
                                + "        private final int id; private final String name;\n"
                                + "        User(int id, String name) { this.id = id; this.name = name; }\n"
                                + "        int getId() { return id; } String getName() { return name; }\n"
                                + "    }\n"
                                + "    static Optional<User> findUser(List<User> users, int id) {\n"
                                + "        return users.stream().filter(u -> u.getId() == id).findFirst();\n"
                                + "    }\n",
                        "        int wantedId = 7;\n"
                                + "        List<User> users = Arrays.asList(new User(7, \" JavaDroid \"));\n"
                                + "        String label = findUser(users, wantedId).map(User::getName)\n"
                                + "                .filter(s -> !s.trim().isEmpty()).map(String::trim)\n"
                                + "                .orElseGet(() -> \"user-\" + wantedId);\n"
                                + "        System.out.println(label);\n");
            case "jdk8.functional.4":
                return source(
                        "    private static final Pattern PAIR = Pattern.compile(\n"
                                + "            \"(?<key>[A-Za-z][A-Za-z0-9_]*)\\\\s*=\\\\s*(?<value>[^,]+)\");\n",
                        "        Matcher matcher = PAIR.matcher(\"host=localhost, port=8080\");\n"
                                + "        while (matcher.find())\n"
                                + "            System.out.println(matcher.group(\"key\") + \" -> \"\n"
                                + "                    + matcher.group(\"value\").trim());\n");
            case "jdk8.runtime.1":
                return source(
                        "    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)\n"
                                + "    @interface Setting {}\n"
                                + "    static final class Config { @Setting private String host = \"localhost\"; }\n"
                                + "    static final class Service { void run(String job) { System.out.println(\"run \" + job); } }\n",
                        "        Config config = new Config();\n"
                                + "        for (Field field : Config.class.getDeclaredFields()) {\n"
                                + "            if (!field.isAnnotationPresent(Setting.class)) continue;\n"
                                + "            field.setAccessible(true);\n"
                                + "            System.out.println(field.getName() + \"=\" + field.get(config));\n"
                                + "        }\n"
                                + "        Method method = Service.class.getDeclaredMethod(\"run\", String.class);\n"
                                + "        method.invoke(new Service(), \"example\");\n");
            case "jdk8.runtime.2":
                return source(
                        "    static final class Session implements Serializable {\n"
                                + "        private static final long serialVersionUID = 1L;\n"
                                + "        private final String user; private transient String token;\n"
                                + "        Session(String user, String token) { this.user = user; this.token = token; }\n"
                                + "        public String toString() { return user + \" token=\" + token; }\n"
                                + "        private void readObject(ObjectInputStream in)\n"
                                + "                throws IOException, ClassNotFoundException {\n"
                                + "            in.defaultReadObject();\n"
                                + "            if (user == null || user.isEmpty())\n"
                                + "                throw new InvalidObjectException(\"empty user\");\n"
                                + "        }\n"
                                + "    }\n",
                        "        ByteArrayOutputStream bytes = new ByteArrayOutputStream();\n"
                                + "        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {\n"
                                + "            out.writeObject(new Session(\"ira\", \"secret\"));\n"
                                + "        }\n"
                                + "        try (ObjectInputStream in = new ObjectInputStream(\n"
                                + "                new ByteArrayInputStream(bytes.toByteArray()))) {\n"
                                + "            System.out.println(in.readObject());\n"
                                + "        }\n");
            default:
                return null;
        }
    }

    private static String source(String members, String mainBody) {
        return "import java.io.*;\n"
                + "import java.lang.annotation.*;\n"
                + "import java.lang.reflect.*;\n"
                + "import java.nio.charset.*;\n"
                + "import java.nio.file.*;\n"
                + "import java.time.*;\n"
                + "import java.time.format.*;\n"
                + "import java.time.temporal.*;\n"
                + "import java.util.*;\n"
                + "import java.util.concurrent.*;\n"
                + "import java.util.concurrent.atomic.*;\n"
                + "import java.util.function.*;\n"
                + "import java.util.regex.*;\n"
                + "import java.util.stream.*;\n\n"
                + "public final class SnippetRunner {\n"
                + members
                + "    public static void main(String[] args) throws Exception {\n"
                + mainBody
                + "    }\n"
                + "}\n";
    }
}
