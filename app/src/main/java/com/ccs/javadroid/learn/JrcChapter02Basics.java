package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/** Глава 2. Основи програмування. */
final class JrcChapter02Basics {

    static void add(Course c) {
        Chapter ch = new Chapter(
                "Глава 2. Основи програмування",
                "Chapter 2. Programming basics");
        ch.add(lessonOperators());
        ch.add(lessonControlFlow());
        ch.add(lessonArrays());
        ch.add(lessonForLoop());
        c.add(ch);
    }

    private static Lesson lessonOperators() {
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
                "Оператор && — «коротке І»: якщо ліва частина false, права не обчислюється "
                + "(коротке замикання). Те саме стосується ||."));
        uk.add(LessonBlock.heading("Пріоритет операторів (спрощено)"));
        uk.add(LessonBlock.list(
                "Найвищий: ++ -- (пост/префікс), ! ~ (unary)",
                "Далі: * / %",
                "Потім: + -",
                "Зсуви: << >> >>>",
                "Порівняння: < <= > >= instanceof",
                "Рівність: == !=",
                "Побітове І: &",
                "Побітове XOR: ^",
                "Побітове АБО: |",
                "Логічне І: &&",
                "Логічне АБО: ||",
                "Тернарний: ? :",
                "Найнижчий: = += -= ..."));

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
                "The && operator is a short-circuit AND: if the left side is false, the right "
                + "side is not evaluated (short-circuiting). The same applies to ||."));
        en.add(LessonBlock.heading("Operator precedence (simplified)"));
        en.add(LessonBlock.list(
                "Highest: ++ -- (post/prefix), ! ~ (unary)",
                "Then: * / %",
                "Then: + -",
                "Shifts: << >> >>>",
                "Comparison: < <= > >= instanceof",
                "Equality: == !=",
                "Bitwise AND: &",
                "Bitwise XOR: ^",
                "Bitwise OR: |",
                "Logical AND: &&",
                "Logical OR: ||",
                "Ternary: ? :",
                "Lowest: = += -= ..."));

        return new Lesson("2.1", "Оператори", "Operators", uk, en);
    }

    private static Lesson lessonControlFlow() {
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
                "Забуття break у класичному switch призведе до «провалювання» (fall-through) у "
                + "наступний case. Для JDK 8 це базове правило, яке треба довести до автоматизму."));

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
                "Forgetting break in a classic switch leads to fall-through into the next case. "
                + "For JDK 8 this is a basic rule to turn into a habit."));

        return new Lesson("2.2", "Керування потоком", "Control flow", uk, en);
    }

    private static Lesson lessonArrays() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Масиви"));
        uk.add(LessonBlock.paragraph(
                "Масив у Java — об'єкт фіксованої довжини, що зберігає елементи одного типу. "
                + "Довжина задається при створенні й не змінюється."));
        uk.add(LessonBlock.code(
                "int[] nums = { 10, 20, 30, 40 };\n"
                + "System.out.println(nums.length);        // 4\n"
                + "System.out.println(nums[0]);            // 10\n"
                + "\n"
                + "// Створення з заданою довжиною\n"
                + "double[] prices = new double[5];\n"
                + "prices[0] = 9.99;\n"
                + "\n"
                + "// Двовимірний масив\n"
                + "int[][] grid = new int[3][3];\n"
                + "grid[1][2] = 7;\n"
                + "\n"
                + "// Перебір через enhanced-for\n"
                + "for (int n : nums) {\n"
                + "    System.out.println(n);\n"
                + "}"));
        uk.add(LessonBlock.note(
                "java.util.Arrays надає утиліти: Arrays.sort(nums), Arrays.toString(nums), "
                + "Arrays.binarySearch(nums, key)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Arrays"));
        en.add(LessonBlock.paragraph(
                "An array in Java is a fixed-length object storing elements of one type. The "
                + "length is set at creation and never changes."));
        en.add(LessonBlock.code(
                "int[] nums = { 10, 20, 30, 40 };\n"
                + "System.out.println(nums.length);        // 4\n"
                + "System.out.println(nums[0]);            // 10\n"
                + "\n"
                + "// Creating with a fixed length\n"
                + "double[] prices = new double[5];\n"
                + "prices[0] = 9.99;\n"
                + "\n"
                + "// Two-dimensional array\n"
                + "int[][] grid = new int[3][3];\n"
                + "grid[1][2] = 7;\n"
                + "\n"
                + "// Iterating via enhanced-for\n"
                + "for (int n : nums) {\n"
                + "    System.out.println(n);\n"
                + "}"));
        en.add(LessonBlock.note(
                "java.util.Arrays provides utilities: Arrays.sort(nums), Arrays.toString(nums), "
                + "Arrays.binarySearch(nums, key)."));

        return new Lesson("2.3", "Масиви", "Arrays", uk, en);
    }

    private static Lesson lessonForLoop() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Цикли"));
        uk.add(LessonBlock.paragraph("Java підтримує for, while, do-while:"));
        uk.add(LessonBlock.code(
                "// Класичний for\n"
                + "for (int i = 0; i < 5; i++) {\n"
                + "    System.out.println(i);\n"
                + "}\n"
                + "\n"
                + "// while\n"
                + "int n = 3;\n"
                + "while (n > 0) {\n"
                + "    System.out.println(n--);\n"
                + "}\n"
                + "\n"
                + "// do-while — тіло виконується хоча б один раз\n"
                + "int k = 0;\n"
                + "do {\n"
                + "    System.out.println(k);\n"
                + "    k++;\n"
                + "} while (k < 0);"));
        uk.add(LessonBlock.list(
                "break — негайний вихід з циклу.",
                "continue — перехід до наступної ітерації.",
                "break з міткою — вихід із вкладеного циклу: outer: for(...) for(...) break outer;"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Loops"));
        en.add(LessonBlock.paragraph("Java supports for, while and do-while:"));
        en.add(LessonBlock.code(
                "// Classic for\n"
                + "for (int i = 0; i < 5; i++) {\n"
                + "    System.out.println(i);\n"
                + "}\n"
                + "\n"
                + "// while\n"
                + "int n = 3;\n"
                + "while (n > 0) {\n"
                + "    System.out.println(n--);\n"
                + "}\n"
                + "\n"
                + "// do-while — body runs at least once\n"
                + "int k = 0;\n"
                + "do {\n"
                + "    System.out.println(k);\n"
                + "    k++;\n"
                + "} while (k < 0);"));
        en.add(LessonBlock.list(
                "break — immediate exit from the loop.",
                "continue — jumps to the next iteration.",
                "break with a label — exits a nested loop: outer: for(...) for(...) break outer;"));

        return new Lesson("2.4", "Цикли", "Loops", uk, en);
    }
}
