package com.ccs.javadroid.learn;

/**
 * Курс: Мережеве програмування на Java.
 */
final class NetworkCourse {

    static Course create() {
        Course c = new Course(
                "network",
                "Мережеве програмування (Networking)",
                "Network Programming",
                "Університетський курс з роботи з мережею: Sockets (TCP/UDP), HTTP через "
                + "Java 8-сумісні API та основи NIO.",
                "University course on networking: Sockets (TCP/UDP), HTTP with Java 8-compatible "
                + "APIs, and NIO basics.");

        NetworkChapters.add(c);
        return c;
    }
}
