package com.ccs.javadroid.learn;

/**
 * Додатковий курс: Generics & Collections, Streams & Lambdas, Design Patterns.
 * Детальні теми з глибокими прикладами, пастками та порадами.
 */
final class AdvancedJavaCourse {

    static Course create() {
        Course c = new Course(
                "advanced_java",
                "Java: Поглиблені теми (Generics, Streams, Patterns)",
                "Java: Advanced Topics (Generics, Streams, Patterns)",
                "Детальне вивчення generics, collections, Stream API, lambda-виразів та "
                + "найважливіших design patterns з практичними JDK 8-сумісними прикладами.",
                "In-depth study of generics, collections, Stream API, lambda expressions, "
                + "and essential design patterns with practical JDK 8-compatible examples.");

        AdvancedTopicsChapters.add(c);
        return c;
    }
}
