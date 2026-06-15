package com.ccs.javadroid.learn;

/**
 * Єдиний курс Java: від основ до enterprise-тем.
 * 23 глави: 1-15 — Java Complete Reference (JDK 8),
 * 16-23 — Date/Time, Concurrency, JVM Memory, RDBMS/SQL, JDBC/DAO, Web, Servlet/JSP.
 */
final class JavaTutorials {

    static Course create() {
        Course c = new Course(
                "java_tutorials",
                "Java: від основ до Enterprise",
                "Java: from Basics to Enterprise",
                "Повний курс Java: типи, ООП, колекції, Stream API, винятки, багатопоточність, "
                + "Date/Time, JVM Memory, SQL, JDBC, Web, Servlet/JSP.",
                "Complete Java course: types, OOP, collections, Stream API, exceptions, concurrency, "
                + "Date/Time, JVM Memory, SQL, JDBC, Web, Servlet/JSP.");

        JrcChapter01Intro.add(c);
        JrcChapter02Basics.add(c);
        JrcChapters03to15.add(c);
        Chapters16to23.add(c);
        return c;
    }
}
