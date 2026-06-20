package com.ccs.javadroid.learn;

/**
 * Курс: Архітектура та проектування програмного забезпечення.
 */
final class ArchitectureCourse {

    static Course create() {
        Course c = new Course(
                "architecture",
                "Архітектура та проектування ПЗ",
                "Software Architecture and Design",
                "Університетський курс з принципів проектування: SOLID, DRY, KISS, патерни архітектури (MVC, Clean Architecture).",
                "University course on design principles: SOLID, DRY, KISS, architectural patterns (MVC, Clean Architecture).");

        ArchitectureChapters.add(c);
        return c;
    }
}
