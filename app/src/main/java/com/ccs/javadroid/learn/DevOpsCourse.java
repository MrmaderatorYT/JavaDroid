package com.ccs.javadroid.learn;

/**
 * Курс: Інструменти збірки та розгортання (DevOps Basics).
 */
final class DevOpsCourse {

    static Course create() {
        Course c = new Course(
                "devops",
                "Інструменти збірки та DevOps",
                "Build Tools & DevOps",
                "Курс, що знайомить з інфраструктурою Java-проектів: Maven, Gradle, контейнеризація Docker.",
                "A course introducing Java project infrastructure: Maven, Gradle, Docker containerization.");

        DevOpsChapters.add(c);
        return c;
    }
}
