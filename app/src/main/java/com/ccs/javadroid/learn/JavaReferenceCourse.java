package com.ccs.javadroid.learn;

import java.util.Arrays;

/**
 * Курс «Java: The Complete Reference, 9th Edition» (Герберт Шилдт, JDK 8).
 * Двомовний (uk/en). Контент розбито на будівники по главах для керовності.
 */
final class JavaReferenceCourse {

    static Course create() {
        Course c = new Course(
                "java_reference_9",
                "Java: Повний довідник (9-те видання, JDK 8)",
                "Java: The Complete Reference (9th Edition, JDK 8)",
                "Класичний курс Герберта Шилдта для JDK 8: від основ до ООП, колекцій, "
                + "Stream API та лямбда-виразів.",
                "Herbert Schildt's classic JDK 8 course: from basics to OOP, collections, "
                + "the Stream API, and lambda expressions.");

        JrcChapter01Intro.add(c);
        JrcChapter02Basics.add(c);
        JrcChapters03to15.add(c);
        return c;
    }

    // Запобіжник, щоб клас не зникав через «невикористані» посилання при рефакторингу.
    static void keepTypes() {
        Arrays.asList(LessonBlock.heading(""), LessonBlock.paragraph(""));
    }
}
