package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Реєстр матеріалів. Два розділи: Java та Bytecode.
 *
 * <p>Мова — глобальна для всього центру матеріалів (uk/en), зберігається тут.</p>
 */
public final class CourseRegistry {

    /** Українська. */
    public static final int LANG_UK = 0;
    /** Англійська. */
    public static final int LANG_EN = 1;

    private static volatile CourseRegistry instance;

    private final List<Course> courses = new ArrayList<>();
    private volatile int language = LANG_UK;

    private CourseRegistry() {
        // ── Java розділ ──────────────────────────────────────────────────
        Course java = new Course(
                "java",
                "Java",
                "Java",
                "Матеріали з Java: від основ до enterprise-тем, алгоритмів, архітектури, "
                + "мережевого програмування, тестування, Spring Boot та DevOps.",
                "Java materials: from basics to enterprise topics, algorithms, architecture, "
                + "networking, testing, Spring Boot, and DevOps.");

        // JavaTutorials (глави 1-23)
        JrcChapter01Intro.add(java);
        JrcChapter02Basics.add(java);
        JrcChapters03to15.add(java);
        Chapters16to23.add(java);

        // EssentialsCourse (Date/Time, Concurrency, JVM, SQL, JDBC, Web, Servlet)
        EssDateTime.add(java);
        EssConcurrency1.add(java);
        EssConcurrency2.add(java);
        EssJvmMemory.add(java);
        EssRdbms.add(java);
        EssJdbc.add(java);
        EssWeb.add(java);
        EssServletJsp.add(java);

        // AdvancedJava
        AdvancedTopicsChapters.add(java);

        // Algorithms
        AlgorithmsChapters.add(java);

        // Architecture
        ArchitectureChapters.add(java);

        // Network
        NetworkChapters.add(java);

        // Testing
        TestingChapters.add(java);

        // Spring Boot
        SpringBootChapters.add(java);

        // DevOps
        DevOpsChapters.add(java);

        // JDK 8 Deep Dive (non-bytecode частини)
        Jdk8DeepDiveChapters.add(java);
        Jdk8DeepDiveMoreChapters.add(java);
        Jdk8DeepDiveAdvancedChapters.add(java);

        courses.add(java);

        // ── Bytecode розділ ──────────────────────────────────────────────
        Course bytecode = new Course(
                "bytecode",
                "Bytecode",
                "Bytecode",
                "JVM bytecode: від основ class file до розширених тем — class loading, "
                + "StackMapTable, invokedynamic, Jasmin та практичні drills.",
                "JVM bytecode: from class file basics to advanced topics — class loading, "
                + "StackMapTable, invokedynamic, Jasmin, and practical drills.");

        Jdk8BytecodeChapters.add(bytecode);
        Jdk8BytecodeAdvancedChapters.add(bytecode);
        Jdk8BytecodeCookbookChapters.add(bytecode);

        courses.add(bytecode);

        // Додати study-фрейм до всіх матеріалів
        for (Course s : courses) {
            applyStudyFrame(s);
        }
    }

    // ── Beginner-friendly study frame (колишній BeginnerFriendlyContent) ──

    private static final String UK_MARKER = "Як працювати з цим матеріалом";
    private static final String EN_MARKER = "How to study this material";

    private static void applyStudyFrame(Course section) {
        for (Chapter chapter : section.chapters) {
            for (Lesson mat : chapter.materials) {
                addStudyFrame(mat.content[0], mat.title[0], true);
                addStudyFrame(mat.content[1], mat.title[1], false);
            }
        }
    }

    private static void addStudyFrame(List<LessonBlock> blocks, String title, boolean uk) {
        String marker = uk ? UK_MARKER : EN_MARKER;
        for (LessonBlock block : blocks) {
            if (block.type == LessonBlock.HEADING && marker.equals(block.text)) {
                return;
            }
        }

        if (uk) {
            blocks.add(LessonBlock.heading(UK_MARKER));
            blocks.add(LessonBlock.paragraph(
                    "Тема «" + title + "» краще засвоюється маленькими кроками: спершу "
                    + "зрозумійте головну ідею, потім перепишіть приклад вручну, і лише після "
                    + "цього змінюйте код під власну задачу."));
            blocks.add(LessonBlock.list(
                    "Що зрозуміти: яку проблему вирішує тема і де вона трапляється у реальних Java/Android-проектах.",
                    "Що запустити: найменший приклад з матеріалу або його спрощену версію у JavaDroid.",
                    "Що змінити: одне значення, одну умову або один метод, щоб побачити інший результат.",
                    "Що записати собі: нові ключові слова, назви класів і типову помилку з попередження.",
                    "Міні-перевірка: поясніть приклад уголос так, ніби навчаєте людину, яка бачить Java вперше."));
            blocks.add(LessonBlock.note(
                    "Середовище орієнтоване на Android SDK 26 і компілятор JDK 8. "
                    + "Тому використовуйте явні типи замість var, звичайні class замість record, "
                    + "класичний switch замість arrow/switch expression, а для HTTP — Java 8-сумісні API."));
        } else {
            blocks.add(LessonBlock.heading(EN_MARKER));
            blocks.add(LessonBlock.paragraph(
                    "The topic \"" + title + "\" is easiest to learn in small steps: first "
                    + "understand the main idea, then type the example by hand, and only after "
                    + "that adapt the code to your own task."));
            blocks.add(LessonBlock.list(
                    "What to understand: which problem this topic solves and where it appears in real Java/Android projects.",
                    "What to run: the smallest example from the material, or a simplified version, inside JavaDroid.",
                    "What to change: one value, one condition, or one method so you can observe a different result.",
                    "What to write down: new keywords, class names, and the common mistake from the warning block.",
                    "Mini-check: explain the example out loud as if teaching someone who sees Java for the first time."));
            blocks.add(LessonBlock.note(
                    "This environment targets Android SDK 26 and a JDK 8 compiler. Use explicit types "
                    + "instead of var, regular classes instead of records, classic switch instead "
                    + "of arrow/switch expressions, and Java 8-compatible APIs for HTTP."));
        }
    }

    // ── Singleton ────────────────────────────────────────────────────────

    public static CourseRegistry getInstance() {
        if (instance == null) {
            synchronized (CourseRegistry.class) {
                if (instance == null) instance = new CourseRegistry();
            }
        }
        return instance;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public Course getCourse(String id) {
        for (Course s : courses) {
            if (s.id.equals(id)) return s;
        }
        return null;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int lang) {
        if (lang == LANG_UK || lang == LANG_EN) {
            this.language = lang;
        }
    }

    public boolean isEnglish() {
        return language == LANG_EN;
    }
}
