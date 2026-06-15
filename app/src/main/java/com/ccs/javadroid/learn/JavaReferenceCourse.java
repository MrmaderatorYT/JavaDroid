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
                "Класичний курс Герберта Шилдта — від основ до Stream API, лямбда-виразів та модулів.",
                "Herbert Schildt's classic course — from basics to the Stream API, lambda expressions and modules.");

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
