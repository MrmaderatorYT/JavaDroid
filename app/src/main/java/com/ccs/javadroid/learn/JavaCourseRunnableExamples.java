package com.ccs.javadroid.learn;

import java.util.List;
import java.util.Locale;

/** Adds inline execution to standalone Java examples outside JDK8 Deep Dive. */
final class JavaCourseRunnableExamples {

    private JavaCourseRunnableExamples() {
    }

    static void apply(Course course) {
        for (Chapter chapter : course.chapters) {
            for (Lesson lesson : chapter.materials) {
                List<LessonBlock> uk = lesson.content[CourseRegistry.LANG_UK];
                int ordinal = 0;
                for (int i = 0; i < uk.size(); i++) {
                    LessonBlock block = uk.get(i);
                    if (block.type != LessonBlock.CODE) continue;
                    ordinal++;
                    if (block.isRunnable() || !isJavaCandidate(lesson.id, block.text)) continue;
                    String source = explicitSource(lesson.id + "#" + ordinal, block.text);
                    if (source == null) {
                        source = JavaSnippetSourceBuilder.build(block.text, shouldCompileOnly(block.text));
                    }
                    uk.set(i, LessonBlock.runnableCode(block.text, source));
                }
            }
        }
    }

    private static String explicitSource(String key, String displayedCode) {
        if ("3.2#1".equals(key)) {
            return JavaSnippetSourceBuilder.build(displayedCode.replace("s.add", "c.add"), false);
        }
        if ("3.2#2".equals(key)) {
            return JavaSnippetSourceBuilder.build(displayedCode
                    .replace("void changePrimitive", "static void changePrimitive")
                    .replace("void changeObject", "static void changeObject"), false);
        }
        if ("4.2#1".equals(key) || "4.2#2".equals(key)) {
            String types = "class Animal {\n"
                    + "    final String name; Animal(String name) { this.name = name; }\n"
                    + "    void eat() { System.out.println(name + \" їсть\"); }\n"
                    + "}\n"
                    + "class Dog extends Animal {\n"
                    + "    Dog(String name, String breed) { super(name); }\n"
                    + "    @Override void eat() { System.out.println(name + \" жує кістку\"); }\n"
                    + "    void bark() { System.out.println(name + \" гавкає\"); }\n"
                    + "}\n"
                    + "class Cat extends Animal {\n"
                    + "    Cat(String name) { super(name); }\n"
                    + "    @Override void eat() { System.out.println(name + \" п'є молоко\"); }\n"
                    + "}\n";
            return JavaSnippetSourceBuilder.build(types + displayedCode, false);
        }
        if ("6.2#1".equals(key)) {
            String fixed = displayedCode.replace("package com.example.util;", "")
                    .replace("import java.util.ArrayList;", "")
                    .replace("import java.util.*;", "")
                    .replace("import static java.lang.Math.PI;", "")
                    .replace("import static java.lang.Math.*;", "")
                    .replace("PI", "Math.PI").replace("sqrt(16)", "Math.sqrt(16)");
            return JavaSnippetSourceBuilder.buildWithMain(fixed, "new App().run();");
        }
        if ("7.2#2".equals(key)) {
            return JavaSnippetSourceBuilder.build(
                    displayedCode.replace("NumberFormatException | IllegalArgumentException",
                            "NumberFormatException | NullPointerException"), false);
        }
        if ("7.4#1".equals(key)) {
            return source(
                    "    static double balance = 100.0;\n"
                            + "    static final class InsufficientFundsException extends Exception {\n"
                            + "        final double deficit; InsufficientFundsException(double deficit){\n"
                            + "            super(\"Недостатньо коштів. Бракує: \"+deficit); this.deficit=deficit;\n"
                            + "        }\n"
                            + "    }\n"
                            + "    static final class InvalidDataException extends RuntimeException {\n"
                            + "        InvalidDataException(String message){super(message);}\n"
                            + "    }\n"
                            + "    static void withdraw(double amount) throws InsufficientFundsException {\n"
                            + "        if(amount>balance) throw new InsufficientFundsException(amount-balance);\n"
                            + "        balance-=amount;\n"
                            + "    }\n",
                    "try { withdraw(125.0); } catch (InsufficientFundsException e) { System.out.println(e.getMessage()); }");
        }
        if ("8.1#1".equals(key)) {
            return JavaSnippetSourceBuilder.build(displayedCode.replace("a.equals(s)", "a.equals(c)"), false);
        }
        if ("9.4#2".equals(key)) {
            return JavaSnippetSourceBuilder.build(
                    "List<String> list = new ArrayList<String>(Arrays.asList(\"A\", \"B\", \"A\"));\n"
                            + displayedCode + "\nSystem.out.println(list);", false);
        }
        if ("10.1#2".equals(key)) {
            return JavaSnippetSourceBuilder.build(
                    "Path p = Files.createTempFile(\"javadroid-lines-\", \".txt\");\n"
                            + "Files.write(p, Arrays.asList(\"A\", \"B\"), StandardCharsets.UTF_8);\n"
                            + displayedCode + "\nFiles.deleteIfExists(p);", false);
        }
        if ("11.2#2".equals(key)) {
            return source("    static final class State { volatile boolean running = true; }\n",
                    "State state = new State();\n"
                            + "AtomicInteger counter = new AtomicInteger(0);\n"
                            + "counter.incrementAndGet();\n"
                            + "System.out.println(\"running=\" + state.running + \", counter=\" + counter.get());");
        }
        if ("13.1#1".equals(key)) {
            String fixed = displayedCode
                    .replace("Integer n = (Integer) box.get(); // ClassCastException!",
                            "try { Integer broken = (Integer) box.get(); } catch (ClassCastException e) { System.out.println(e.getClass().getSimpleName()); }")
                    .replace("String s = sb.get();", "String safeString = sb.get();")
                    .replace("int n = ib.get();", "int safeNumber = ib.get();");
            return JavaSnippetSourceBuilder.build(fixed
                    + "\nSystem.out.println(safeString + \" / \" + safeNumber);", false);
        }
        if ("13.3#1".equals(key) || "adv.2#1".equals(key)) {
            String fixed = displayedCode.replace("double sum(", "static double sum(")
                    .replace("void addNumbers(", "static void addNumbers(")
                    .replace("sum(Arrays.asList(1, 2, 3));", "System.out.println(sum(Arrays.asList(1, 2, 3)));")
                    .replace("sum(Arrays.asList(1.5, 2.5));", "System.out.println(sum(Arrays.asList(1.5, 2.5)));");
            return JavaSnippetSourceBuilder.build(fixed, false);
        }
        if ("15.1#1".equals(key)) {
            return source(
                    "    static class Animal { String sound() { return \"...\"; } }\n"
                            + "    static class Dog extends Animal { @Override String sound() { return \"Гав!\"; } }\n"
                            + "    @Deprecated static void oldMethod() {}\n"
                            + "    @FunctionalInterface interface Action { void run(); }\n",
                    "List rawList = Arrays.asList(\"Java\");\n"
                            + "@SuppressWarnings(\"unchecked\") List<String> list = (List<String>) rawList;\n"
                            + "System.out.println(new Dog().sound() + \" \" + list);");
        }
        if ("15.2#1".equals(key)) {
            String fixed = displayedCode.replace("void processData()", "public void processData()")
                    .replace("Method m =", "Service serviceInstance = new Service();\nMethod m =");
            return JavaSnippetSourceBuilder.build(fixed, false);
        }
        if ("21.3#1".equals(key) || "jdbc.3#1".equals(key)) {
            return source(
                    "    static final class User {\n"
                            + "        final int id; final String name; final String email;\n"
                            + "        User(int id,String name,String email){this.id=id;this.name=name;this.email=email;}\n"
                            + "    }\n"
                            + "    interface UserDao {\n"
                            + "        User findById(int id) throws SQLException;\n"
                            + "        List<User> findAll(); void save(User u); void delete(int id);\n"
                            + "    }\n"
                            + "    static abstract class JdbcUserDao implements UserDao {\n"
                            + "        final Connection con; JdbcUserDao(Connection con){this.con=con;}\n"
                            + "        public User findById(int id) throws SQLException {\n"
                            + "            try(PreparedStatement ps=con.prepareStatement(\"SELECT id,name,email FROM users WHERE id=?\")){\n"
                            + "                ps.setInt(1,id); try(ResultSet rs=ps.executeQuery()){\n"
                            + "                    return rs.next()?new User(rs.getInt(1),rs.getString(2),rs.getString(3)):null;\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }\n"
                            + "    }\n",
                    "System.out.println(\"✓ DAO contract compiled\");");
        }
        if ("adv.4#1".equals(key)) {
            return JavaSnippetSourceBuilder.build(displayedCode
                    .replace("int v = map.getOrDefault", "int fallback = map.getOrDefault")
                    + "\nSystem.out.println(map);", false);
        }
        if ("adv.4#2".equals(key)) {
            return JavaSnippetSourceBuilder.build(
                    "Map<String,Integer> map = new HashMap<String,Integer>();\n"
                            + "map.put(\"a\", 3); map.put(\"b\", 8);\n" + displayedCode
                            + "\nSystem.out.println(sorted);", false);
        }
        if ("adv.6#1".equals(key)) {
            return JavaSnippetSourceBuilder.build(displayedCode.replace("list.sort(chain);",
                    "List<Student> list = new ArrayList<Student>();\n"
                            + "list.add(new Student()); list.sort(chain);\nSystem.out.println(list.size());"), false);
        }
        if ("adv.8#1".equals(key)) {
            return JavaSnippetSourceBuilder.build(
                    "List<String> list = Arrays.asList(\"short\", \"JavaDroid example\");\n"
                            + displayedCode.replace("Consumer<String> printer", "Consumer<Integer> printer"), false);
        }
        if ("adv.9#1".equals(key)) {
            return JavaSnippetSourceBuilder.build(
                    "List<String> list = Arrays.asList(\"a\", \"b\");\n" + displayedCode
                            + "\nSystem.out.println(s5.collect(Collectors.toList()));", false);
        }
        if ("adv.11#1".equals(key)) {
            return source("",
                    "List<String> names = Arrays.asList(\"Іван\", \"Олена\", \"Андрій\");\n"
                            + "List<String> list = names.stream().collect(Collectors.toList());\n"
                            + "Set<String> set = names.stream().collect(Collectors.toSet());\n"
                            + "String csv = names.stream().collect(Collectors.joining(\", \"));\n"
                            + "Map<Integer,List<String>> byLength = names.stream().collect(Collectors.groupingBy(String::length));\n"
                            + "Map<Boolean,List<String>> parts = names.stream().collect(Collectors.partitioningBy(n -> n.length()>4));\n"
                            + "Map<String,Integer> nameLen = names.stream().collect(Collectors.toMap(n -> n, String::length));\n"
                            + "int totalLen = names.stream().collect(Collectors.summingInt(String::length));\n"
                            + "System.out.println(csv + \" / \" + totalLen + \" / \" + byLength + \" / \" + parts + \" / \" + set + \" / \" + list + \" / \" + nameLen);");
        }
        if ("dp.8#1".equals(key)) {
            String fixed = displayedCode.replace("history.add(s);", "history.add(c);");
            return JavaSnippetSourceBuilder.buildWithMain(fixed,
                    "TextEditor editor = new TextEditor();\n"
                            + "CommandInvoker invoker = new CommandInvoker();\n"
                            + "invoker.executeCommand(new AddTextCommand(editor, \"JavaDroid\"));\n"
                            + "System.out.println(editor.text);");
        }
        if ("mod.3#2".equals(key)) {
            return JavaSnippetSourceBuilder.build("Object obj = \"JavaDroid\";\n" + displayedCode, false);
        }
        if ("alg.2#1".equals(key)) {
            return JavaSnippetSourceBuilder.buildWithMain(
                    displayedCode.replace("void bubbleSort", "static void bubbleSort"),
                    "int[] values={5,1,4,2};\nbubbleSort(values);\nSystem.out.println(Arrays.toString(values));");
        }
        if ("alg.3#1".equals(key)) {
            return JavaSnippetSourceBuilder.buildWithMain(
                    displayedCode.replace("int binarySearch", "static int binarySearch"),
                    "int[] values={1,3,5,7,9};\nSystem.out.println(binarySearch(values,7));");
        }
        if ("alg.4#1".equals(key)) {
            return JavaSnippetSourceBuilder.buildWithMain(
                    displayedCode.replace("void inOrder", "static void inOrder"),
                    "TreeNode root=new TreeNode(2); root.left=new TreeNode(1); root.right=new TreeNode(3);\ninOrder(root);");
        }
        if ("alg.5#1".equals(key)) {
            return JavaSnippetSourceBuilder.buildWithMain(
                    displayedCode.replace("void bfs", "static void bfs"),
                    "Map<Integer,List<Integer>> graph=new HashMap<Integer,List<Integer>>();\n"
                            + "graph.put(1,Arrays.asList(2,3)); graph.put(2,Arrays.asList(4));\n"
                            + "graph.put(3,Collections.<Integer>emptyList()); graph.put(4,Collections.<Integer>emptyList());\n"
                            + "bfs(1,graph);");
        }
        if ("arch.2#1".equals(key)) {
            return source(
                    "    static final class Order { final double total; Order(double total){this.total=total;} double getTotal(){return total;} }\n"
                            + "    interface Discount { double calculate(Order order); }\n"
                            + "    static final class VipDiscount implements Discount { public double calculate(Order o){return o.getTotal()*0.2;} }\n"
                            + "    static final class NewDiscount implements Discount { public double calculate(Order o){return o.getTotal()*0.1;} }\n"
                            + "    static double calculateDiscount(Order order, Discount discount){return discount.calculate(order);}\n",
                    "System.out.println(calculateDiscount(new Order(1000), new VipDiscount()));");
        }
        if ("arch.3#1".equals(key)) {
            return source(
                    "    interface Workable { void work(); }\n"
                            + "    interface Eatable { void eat(); }\n"
                            + "    static final class Human implements Workable,Eatable {\n"
                            + "        public void work(){System.out.println(\"human works\");}\n"
                            + "        public void eat(){System.out.println(\"human eats\");}\n"
                            + "    }\n"
                            + "    static final class Robot implements Workable { public void work(){System.out.println(\"robot works\");} }\n",
                    "new Human().eat();\nnew Robot().work();");
        }
        if ("arch.4#1".equals(key)) {
            return source(
                    "    interface Database { void insert(); }\n"
                            + "    static final class MemoryDatabase implements Database { public void insert(){System.out.println(\"saved\");} }\n"
                            + "    static final class OrderService {\n"
                            + "        private final Database db; OrderService(Database db){this.db=db;}\n"
                            + "        void save(){db.insert();}\n"
                            + "    }\n",
                    "new OrderService(new MemoryDatabase()).save();");
        }
        if ("mod.1#1".equals(key)) {
            return JavaSnippetSourceBuilder.buildWithMain(displayedCode,
                    "Point point = new Point(3, 4);\nSystem.out.println(point);\n"
                            + "System.out.println(\"x=\" + point.getX() + \", y=\" + point.getY());");
        }
        if ("mod.1#2".equals(key)) {
            return JavaSnippetSourceBuilder.buildWithMain(displayedCode,
                    "User user = new User(\"ira\", 21);\n"
                            + "System.out.println(\"adult=\" + user.isAdult());\n"
                            + "try { new User(\"\", -1); }\n"
                            + "catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }");
        }
        return null;
    }

    private static String source(String members, String mainBody) {
        return "import java.io.*;\nimport java.lang.annotation.*;\nimport java.lang.reflect.*;\n"
                + "import java.math.*;\nimport java.net.*;\nimport java.nio.charset.*;\nimport java.nio.file.*;\n"
                + "import java.sql.*;\nimport java.time.*;\nimport java.util.*;\n"
                + "import java.util.concurrent.*;\nimport java.util.concurrent.atomic.*;\n"
                + "import java.util.function.*;\nimport java.util.stream.*;\n\n"
                + "public final class SnippetRunner {\n" + members
                + "    public static void main(String[] args) throws Exception {\n"
                + "        " + mainBody.replace("\n", "\n        ") + "\n    }\n}\n";
    }

    private static boolean isJavaCandidate(String lessonId, String code) {
        String id = lessonId.toLowerCase(Locale.ROOT);
        if (id.startsWith("sql.") || id.startsWith("20.") || id.startsWith("dev.")
                || id.startsWith("sb.") || id.startsWith("test.")
                || id.startsWith("srv.") || id.startsWith("23.")) {
            return false;
        }
        String trimmed = code.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (trimmed.startsWith("#") || trimmed.startsWith("$") || trimmed.startsWith("<")
                || trimmed.startsWith(".") || trimmed.startsWith("Exception in thread")
                || upper.startsWith("SELECT ") || upper.startsWith("CREATE TABLE ")
                || upper.startsWith("INSERT ") || upper.startsWith("UPDATE ")
                || upper.startsWith("DELETE ") || upper.startsWith("FROM ")
                || upper.startsWith("GET /") || upper.startsWith("POST /")
                || trimmed.startsWith("plugins {") || trimmed.startsWith("dependencies {")) {
            return false;
        }
        return trimmed.contains("class ") || trimmed.contains("interface ")
                || trimmed.contains("enum ") || trimmed.contains("System.out")
                || trimmed.contains("new ") || trimmed.contains("public ")
                || trimmed.contains("private ") || trimmed.contains("static ")
                || trimmed.contains("->") || trimmed.contains("@Override")
                || trimmed.contains("import java.") || trimmed.contains("try (")
                || trimmed.matches("(?s).*\\b(?:int|long|double|boolean|String|List|Set|Map|Path|Instant)\\s+\\w+.*");
    }

    private static boolean shouldCompileOnly(String code) {
        return code.contains("ServerSocket") || code.contains(".accept()")
                || code.contains("HttpURLConnection") || code.contains("new Socket(")
                || code.contains("DatagramSocket") || code.contains("DriverManager")
                || code.contains("executeQuery(") || code.contains("executeUpdate(");
    }
}
