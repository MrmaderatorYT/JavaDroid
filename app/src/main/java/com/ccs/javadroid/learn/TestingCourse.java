package com.ccs.javadroid.learn;

/**
 * Курс: Тестування та забезпечення якості (QA).
 */
final class TestingCourse {

    static Course create() {
        Course c = new Course(
                "testing",
                "Автоматизоване тестування",
                "Automated Testing",
                "Університетський курс з QA для розробників: JUnit 5, Mockito, TDD та найкращі практики тестування.",
                "University QA course for developers: JUnit 5, Mockito, TDD, and best testing practices.");

        TestingChapters.add(c);
        return c;
    }
}
