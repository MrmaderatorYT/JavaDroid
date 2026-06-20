package com.ccs.javadroid.learn;

/**
 * Курс: Основи Spring Boot та Enterprise розробки.
 */
final class SpringBootCourse {

    static Course create() {
        Course c = new Course(
                "spring_boot",
                "Основи Spring Boot",
                "Spring Boot Basics",
                "Фундаментальний курс з розробки корпоративних додатків: Dependency Injection, REST API, Spring Data JPA.",
                "Fundamental course on enterprise application development: Dependency Injection, REST APIs, Spring Data JPA.");

        SpringBootChapters.add(c);
        return c;
    }
}
