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

        // Український reference-шар для кожного Java-side JDK 8 Deep Dive матеріалу.
        // Англійські списки навмисно не змінюються до окремого перекладацького проходу.
        Jdk8UkrainianReferenceExpansion.apply(java);
        Jdk8RunnableExamples.apply(java);
        JavaCourseRunnableExamples.apply(java);

        // Наративний (Ukrainian-only) розбір тонких курсів у стилі Rust Book.
        AlgorithmsDeepDive.apply(java);
        ArchitectureDeepDive.apply(java);
        TestingDeepDive.apply(java);
        SpringBootDeepDive.apply(java);
        NetworkDeepDive.apply(java);
        DevOpsDeepDive.apply(java);

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
