package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/** Глава 2. Основи програмування. */
final class JrcChapter02Basics {

    static void populateJava() {
        Chapter ch = new Chapter(
                "Глава 2. Основи програмування",
                "Chapter 2. Programming basics");

        ch.add(buildOperators());
        ch.add(buildControlFlow());
        ch.add(buildArrays());
        ch.add(buildForLoop());

        MaterialStore.addJava(ch);
    }

    private static TopicContent buildOperators() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Оператори"));
        uk.add(LessonBlock.paragraph(
                "Java має арифметичні (+ - * / %), порівняння (== != < > <= >=), логічні "
                + "(&& || !), побітові (& | ^ ~ << >> >>>) оператори й оператор присвоєння з "
                + "комбінацією (+= -= *= /= %=)."));
        uk.add(LessonBlock.code(
                "int a = 10, b = 3;\n"
                + "System.out.println(a + b);   // 13\n"
                + "System.out.println(a / b);   // 3   (цілочисельне ділення!)\n"
                + "System.out.println(a % b);   // 1\n"
                + "System.out.println(a / (double) b); // 3.333...\n"
                + "boolean x = (a > b) && (b > 0);\n"
                + "System.out.println(x);       // true"));
        uk.add(LessonBlock.note(
                "Оператор && — «коротке І»: якщо ліва частина false, права не обчислюється."));
        uk.add(LessonBlock.heading("Пріоритет операторів (спрощено)"));
        uk.add(LessonBlock.list(
                "Найвищий: ++ -- (пост/префікс), ! ~ (unary)", "Далі: * / %", "Потім: + -",
                "Зсуви: << >> >>>", "Порівняння: < <= > >= instanceof", "Рівність: == !=",
                "Побітове І: &", "Побітове XOR: ^", "Побітове АБО: |", "Логічне І: &&",
                "Логічне АБО: ||", "Тернарний: ? :", "Найнижчий: = += -= ..."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Operators"));
        en.add(LessonBlock.paragraph(
                "Java has arithmetic (+ - * / %), comparison (== != < > <= >=), logical "
                + "(&& || !), bitwise (& | ^ ~ << >> >>>) operators and compound assignment "
                + "(+= -= *= /= %=)."));
        en.add(LessonBlock.code(
                "int a = 10, b = 3;\n"
                + "System.out.println(a + b);   // 13\n"
                + "System.out.println(a / b);   // 3   (integer division!)\n"
                + "System.out.println(a % b);   // 1\n"
                + "System.out.println(a / (double) b); // 3.333...\n"
                + "boolean x = (a > b) && (b > 0);\n"
                + "System.out.println(x);       // true"));
        en.add(LessonBlock.note(
                "The && operator is a short-circuit AND: if left side is false, right side not evaluated."));
        en.add(LessonBlock.heading("Operator precedence (simplified)"));
        en.add(LessonBlock.list(
                "Highest: ++ -- (post/prefix), ! ~ (unary)", "Then: * / %", "Then: + -",
                "Shifts: << >> >>>", "Comparison: < <= > >= instanceof", "Equality: == !=",
                "Bitwise AND: &", "Bitwise XOR: ^", "Bitwise OR: |", "Logical AND: &&",
                "Logical OR: ||", "Ternary: ? :", "Lowest: = += -= ..."));

        return new TopicContent(concat(uk, en));
    }

    private static TopicContent buildControlFlow() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Керування потоком: if / switch"));
        uk.add(LessonBlock.paragraph("Умовна конструкція if-else:"));
        uk.add(LessonBlock.code(
                "int score = 85;\n"
                + "if (score >= 90) {\n"
                + "    System.out.println(\"Відмінно\");\n"
                + "} else if (score >= 70) {\n"
                + "    System.out.println(\"Добре\");\n"
                + "} else {\n"
                + "    System.out.println(\"Треба більше старатись\");\n"
                + "}"));
        uk.add(LessonBlock.paragraph("Switch у JDK 8 працює з числами, enum та String:"));
        uk.add(LessonBlock.code(
                "String day = \"MON\";\n"
                + "switch (day) {\n"
                + "    case \"MON\": case \"TUE\": case \"WED\":\n"
                + "    case \"THU\": case \"FRI\":\n"
                + "        System.out.println(\"Робочий день\");\n"
                + "        break;\n"
                + "    case \"SAT\": case \"SUN\":\n"
                + "        System.out.println(\"Вихідний\");\n"
                + "        break;\n"
                + "    default:\n"
                + "        System.out.println(\"Невідомо\");\n"
                + "}"));
        uk.add(LessonBlock.warning(
                "Забуття break у класичному switch призведе до «провалювання» (fall-through)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Control flow: if / switch"));
        en.add(LessonBlock.paragraph("The if-else conditional:"));
        en.add(LessonBlock.code(
                "int score = 85;\n"
                + "if (score >= 90) {\n"
                + "    System.out.println(\"Excellent\");\n"
                + "} else if (score >= 70) {\n"
                + "    System.out.println(\"Good\");\n"
                + "} else {\n"
                + "    System.out.println(\"Try harder\");\n"
                + "}"));
        en.add(LessonBlock.paragraph("In JDK 8, switch works with numbers, enums, and String:"));
        en.add(LessonBlock.code(
                "String day = \"MON\";\n"
                + "switch (day) {\n"
                + "    case \"MON\": case \"TUE\": case \"WED\":\n"
                + "    case \"THU\": case \"FRI\":\n"
                + "        System.out.println(\"Weekday\");\n"
                + "        break;\n"
                + "    case \"SAT\": case \"SUN\":\n"
                + "        System.out.println(\"Weekend\");\n"
                + "        break;\n"
                + "    default:\n"
                + "        System.out.println(\"Unknown\");\n"
                + "}"));
        en.add(LessonBlock.warning(
                "Forgetting break in classic switch leads to fall-through."));

        return new TopicContent(concat(uk, en));
    }

    private static TopicContent buildArrays() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Масиви"));
        uk.add(LessonBlock.paragraph(
                "Масив у Java — об'єкт фіксованої довжини, що зберігає елементи одного типу."));
        uk.add(LessonBlock.code(
                "int[] nums = { 10, 20, 30, 40 };\n"
                + "System.out.println(nums.length);        // 4\n"
                + "System.out.println(nums[0]);            // 10\n"
                + "double[] prices = new double[5];\n"
                + "prices[0] = 9.99;\n"
                + "int[][] grid = new int[3][3];\n"
                + "grid[1][2] = 7;\n"
                + "for (int n : nums) { System.out.println(n); }"));
        uk.add(LessonBlock.note(
                "java.util.Arrays надає утиліти: Arrays.sort(nums), Arrays.toString(nums)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Arrays"));
        en.add(LessonBlock.paragraph(
                "An array in Java is a fixed-length object storing elements of one type."));
        en.add(LessonBlock.code(
                "int[] nums = { 10, 20, 30, 40 };\n"
                + "System.out.println(nums.length);        // 4\n"
                + "System.out.println(nums[0]);            // 10\n"
                + "double[] prices = new double[5];\n"
                + "prices[0] = 9.99;\n"
                + "int[][] grid = new int[3][3];\n"
                + "grid[1][2] = 7;\n"
                + "for (int n : nums) { System.out.println(n); }"));
        en.add(LessonBlock.note(
                "java.util.Arrays provides utilities: Arrays.sort(nums), Arrays.toString(nums)."));

        return new TopicContent(concat(uk, en));
    }

    private static TopicContent buildForLoop() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Цикли"));
        uk.add(LessonBlock.paragraph("Java підтримує for, while, do-while:"));
        uk.add(LessonBlock.code(
                "for (int i = 0; i < 5; i++) {\n"
                + "    System.out.println(i);\n"
                + "}\n"
                + "int n = 3;\n"
                + "while (n > 0) { System.out.println(n--); }\n"
                + "int k = 0;\n"
                + "do { System.out.println(k); k++; } while (k < 0);"));
        uk.add(LessonBlock.list(
                "break — негайний вихід з циклу.", "continue — перехід до наступної ітерації.",
                "break з міткою — вихід із вкладеного циклу: outer: for(...) for(...) break outer;"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Loops"));
        en.add(LessonBlock.paragraph("Java supports for, while and do-while:"));
        en.add(LessonBlock.code(
                "for (int i = 0; i < 5; i++) {\n"
                + "    System.out.println(i);\n"
                + "}\n"
                + "int n = 3;\n"
                + "while (n > 0) { System.out.println(n--); }\n"
                + "int k = 0;\n"
                + "do { System.out.println(k); k++; } while (k < 0);"));
        en.add(LessonBlock.list(
                "break — immediate exit from the loop.", "continue — jumps to the next iteration.",
                "break with a label — exits a nested loop: outer: for(...) for(...) break outer;"));

        return new TopicContent(concat(uk, en));
    }

    // ── Допоміжний метод для об'єднання двох списків ──
    // Оскільки TopicContent очікує один список, потрібно вирішити, як кодувати дві мови.
    // У цій реалізації ми залишаємо одну мову (наприклад, українську) – але це незручно.
    // Краще зробити TopicContent двомовним. Але для простоти я зроблю TopicContent двомовним, з двома полями.
    // Тому нижче я наведу нову версію TopicContent, яка має два списки.
}
