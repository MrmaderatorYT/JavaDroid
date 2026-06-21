package com.ccs.javadroid.learn;

import java.util.ArrayList;
import java.util.List;

/**
 * Гурток з Maven, Gradle та Docker.
 */
final class DevOpsChapters {

    static void add(Course s) {
        Chapter ch1 = new Chapter("Інструменти збірки", "Build Tools");
        ch1.add(materialMaven());
        ch1.add(materialGradle());
        s.add(ch1);

        Chapter ch2 = new Chapter("Контейнеризація", "Containerization");
        ch2.add(materialDocker());
        s.add(ch2);
    }

    private static Lesson materialMaven() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Apache Maven"));
        uk.add(LessonBlock.paragraph(
                "Maven — це інструмент для збірки проектів (Build tool) та управління залежностями "
                + "(Dependency management). Без Maven вам довелося б завантажувати сторонні бібліотеки "
                + "(JAR-файли) вручну."));
        uk.add(LessonBlock.paragraph(
                "Уся конфігурація Maven зберігається у файлі pom.xml (Project Object Model)."));
        uk.add(LessonBlock.code(
                "<!-- Приклад простого pom.xml -->\n"
                + "<project>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>com.example</groupId>\n"
                + "    <artifactId>my-app</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "    \n"
                + "    <dependencies>\n"
                + "        <!-- Підключення бібліотеки Gson -->\n"
                + "        <dependency>\n"
                + "            <groupId>com.google.code.gson</groupId>\n"
                + "            <artifactId>gson</artifactId>\n"
                + "            <version>2.10.1</version>\n"
                + "        </dependency>\n"
                + "    </dependencies>\n"
                + "</project>"));
        uk.add(LessonBlock.paragraph(
                "Основні фази збірки (Життєвий цикл Maven): "
                + "compile (компіляція), test (запуск тестів), package (створення JAR/WAR), install (збереження у локальний репозиторій)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Apache Maven"));
        en.add(LessonBlock.paragraph(
                "Maven is a build tool and dependency manager. Without Maven, you would have to "
                + "download third-party libraries (JAR files) manually."));
        en.add(LessonBlock.paragraph(
                "All Maven configuration is stored in a pom.xml (Project Object Model) file."));
        en.add(LessonBlock.code(
                "<!-- Example of a simple pom.xml -->\n"
                + "<project>\n"
                + "    <modelVersion>4.0.0</modelVersion>\n"
                + "    <groupId>com.example</groupId>\n"
                + "    <artifactId>my-app</artifactId>\n"
                + "    <version>1.0</version>\n"
                + "    \n"
                + "    <dependencies>\n"
                + "        <!-- Adding the Gson dependency -->\n"
                + "        <dependency>\n"
                + "            <groupId>com.google.code.gson</groupId>\n"
                + "            <artifactId>gson</artifactId>\n"
                + "            <version>2.10.1</version>\n"
                + "        </dependency>\n"
                + "    </dependencies>\n"
                + "</project>"));
        en.add(LessonBlock.paragraph(
                "Main build phases (Maven Lifecycle): "
                + "compile, test (run unit tests), package (create JAR/WAR), install (save to local repository)."));

        return new Lesson("dev.1", "Maven", "Maven", uk, en);
    }

    private static Lesson materialGradle() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Gradle"));
        uk.add(LessonBlock.paragraph(
                "Gradle — це сучасніша альтернатива Maven. Замість громіздкого XML він використовує "
                + "лаконічний DSL (Domain Specific Language) на базі Groovy або Kotlin. "
                + "Gradle є офіційним інструментом збірки для Android та дуже популярний у Spring Boot проектах."));
        uk.add(LessonBlock.code(
                "// Приклад build.gradle\n"
                + "plugins {\n"
                + "    id 'java'\n"
                + "}\n"
                + "\n"
                + "group = 'com.example'\n"
                + "version = '1.0'\n"
                + "\n"
                + "repositories {\n"
                + "    mavenCentral() // Звідки качати залежності\n"
                + "}\n"
                + "\n"
                + "dependencies {\n"
                + "    // Підключення бібліотеки\n"
                + "    implementation 'com.google.code.gson:gson:2.10.1'\n"
                + "    \n"
                + "    // Підключення лише для тестів\n"
                + "    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'\n"
                + "}"));
        uk.add(LessonBlock.paragraph(
                "Gradle працює швидше за Maven завдяки інкрементальній збірці (він не перекомпілює "
                + "те, що не змінилося) та фоновому процесу (Gradle Daemon)."));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Gradle"));
        en.add(LessonBlock.paragraph(
                "Gradle is a more modern alternative to Maven. Instead of clunky XML, it uses a "
                + "concise DSL (Domain Specific Language) based on Groovy or Kotlin. "
                + "Gradle is the official build tool for Android and is highly popular in Spring Boot projects."));
        en.add(LessonBlock.code(
                "// Example build.gradle\n"
                + "plugins {\n"
                + "    id 'java'\n"
                + "}\n"
                + "\n"
                + "group = 'com.example'\n"
                + "version = '1.0'\n"
                + "\n"
                + "repositories {\n"
                + "    mavenCentral() // Where to download dependencies from\n"
                + "}\n"
                + "\n"
                + "dependencies {\n"
                + "    // Adding a library\n"
                + "    implementation 'com.google.code.gson:gson:2.10.1'\n"
                + "    \n"
                + "    // Adding a test-only library\n"
                + "    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'\n"
                + "}"));
        en.add(LessonBlock.paragraph(
                "Gradle is faster than Maven due to incremental builds (it does not recompile "
                + "code that hasn't changed) and a background process (Gradle Daemon)."));

        return new Lesson("dev.2", "Gradle", "Gradle", uk, en);
    }

    private static Lesson materialDocker() {
        List<LessonBlock> uk = new ArrayList<>();
        uk.add(LessonBlock.heading("Контейнеризація з Docker"));
        uk.add(LessonBlock.paragraph(
                "До появи Docker додатки часто не працювали на серверах через конфлікти версій Java чи ОС "
                + "('А на моєму комп'ютері працювало!'). Docker упаковує вашу програму разом "
                + "з усіма її залежностями (включаючи саму Java) в ізольований 'Контейнер'."));
        uk.add(LessonBlock.paragraph(
                "Контейнер гарантовано працюватиме однаково на будь-якому комп'ютері."));
        uk.add(LessonBlock.code(
                "# Приклад Dockerfile для Java додатку\n"
                + "# 1. Беремо базовий образ з готовою Java 8\n"
                + "FROM eclipse-temurin:8-jre-alpine\n"
                + "\n"
                + "# 2. Копіюємо наш зібраний JAR-файл у контейнер\n"
                + "COPY target/my-app-1.0.jar /app/my-app.jar\n"
                + "\n"
                + "# 3. Вказуємо команду для запуску при старті контейнера\n"
                + "CMD [\"java\", \"-jar\", \"/app/my-app.jar\"]"));
        uk.add(LessonBlock.note(
                "Щоб запустити такий додаток, розробнику достатньо виконати команду 'docker run'. "
                + "Йому навіть не потрібно мати встановлену Java на своєму комп'ютері!"));

        List<LessonBlock> en = new ArrayList<>();
        en.add(LessonBlock.heading("Containerization with Docker"));
        en.add(LessonBlock.paragraph(
                "Before Docker, applications often failed on servers due to Java or OS version conflicts "
                + "('But it works on my machine!'). Docker packages your application along with "
                + "all its dependencies (including Java itself) into an isolated 'Container'."));
        en.add(LessonBlock.paragraph(
                "A container is guaranteed to run identically on any computer."));
        en.add(LessonBlock.code(
                "# Example Dockerfile for a Java App\n"
                + "# 1. Use a base image that already has Java 8\n"
                + "FROM eclipse-temurin:8-jre-alpine\n"
                + "\n"
                + "# 2. Copy our compiled JAR file into the container\n"
                + "COPY target/my-app-1.0.jar /app/my-app.jar\n"
                + "\n"
                + "# 3. Specify the command to run when the container starts\n"
                + "CMD [\"java\", \"-jar\", \"/app/my-app.jar\"]"));
        en.add(LessonBlock.note(
                "To run this application, a developer only needs to execute the 'docker run' command. "
                + "They do not even need to have Java installed on their machine!"));

        return new Lesson("dev.3", "Docker", "Docker", uk, en);
    }
}
