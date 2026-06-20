package com.ccs.javadroid.learn;

/**
 * Курс: Алгоритми та структури даних.
 */
final class AlgorithmsCourse {

    static Course create() {
        Course c = new Course(
                "algorithms",
                "Алгоритми та структури даних",
                "Algorithms and Data Structures",
                "Фундаментальний університетський курс з алгоритмів, структур даних та оцінки складності (Big-O).",
                "Fundamental university-level course on algorithms, data structures, and complexity estimation (Big-O).");

        AlgorithmsChapters.add(c);
        return c;
    }
}
